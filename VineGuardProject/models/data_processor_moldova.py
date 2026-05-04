import pandas as pd
import numpy as np
import os

# ============================================================
# VINEGUARD — Moldova Data Processor v2
# Converteste datele reale din Moldova in format wide
# compatibil cu feature_engineering.py v3
#
# Format input: long format cu coloane:
#   id, save_time, type, node_name, data, timp, marime, valoare, um
#
# Timestamp: save_time (unix seconds) — mai fiabil decat data+timp
#
# Input:  data/real/data23_04_2026.csv
# Output: data/real/raw_data_real.csv
#
# Rulare: py data_processor_moldova.py
# ============================================================

INPUT_PATH  = "data/real/data23_04_2026.csv"
OUTPUT_PATH = "data/real/raw_data_real.csv"

SENSOR_MAP = {
    'Temperaturamedieaer':               'temperature',
    'Umiditaterelativaaer':              'humidity',
    'Precipitatiidelaultimamasuratoare': 'rain_15min',
    'Umiditatefrunza':                   'leafwetness_raw',
    'Iradiantasolaramedie':              'solar_radiation',
    'Vitezavantmedie':                   'wind_speed',
    'Vitezamaximavant':                  'wind_gust',
    'Directievantmedie(fatadeN)':        'wind_direction',
    'Presiuneatmosferica':               'barometric_pressure',
    'Temp.frunza':                       'leaf_temperature',
    'Temp.sol.':                         'soil_temperature',
    'Temp.sol10cm':                      'soil_temp_10cm',
    'Temp.sol30cm':                      'soil_temp_30cm',
    'Temp.sol50cm':                      'soil_temp_50cm',
    'Temp.sol80cm':                      'soil_temp_80cm',
    'Cont.vol.apa.sol.':                 'soil_moisture',
    'Umid.sol1':                         'soil_moisture_1',
    'Umid.sol2':                         'soil_moisture_2',
    'CO2inaer':                          'co2',
    'pHsol':                             'soil_ph',
    'Oxigen.sol.':                       'soil_oxygen',
    'PAR':                               'par',
    'Presiunevapori':                    'vapor_pressure',
    'Deficitpresiunevapori':             'vpd_raw',
    'Iradiantainundescurte':             'solar_uv',
    'Intens.lumina':                     'light_intensity',
    'Spectr.Verde':                      'spectr_green',
    'Spectr.Rosu':                       'spectr_red',
    'Spectr.Albastru':                   'spectr_blue',
    'Spectr.Violet':                     'spectr_violet',
    'Spectr.Portocaliu':                 'spectr_orange',
    'Spectr.Galben':                     'spectr_yellow',
    'Conc.azotsol':                      'soil_nitrogen',
    'Conc.fosforsol':                    'soil_phosphorus',
    'Conc.potasiusol':                   'soil_potassium',
    'Cond.el.sol.':                      'soil_conductivity',
    'Nr.fulgeredelaultimamasuratoare':   'lightning_count',
    'Altitudine':                        'altitude',
    'Latitudine':                        'latitude',
    'Longitudine':                       'longitude',
}

REQUIRED = ['temperature', 'humidity', 'rain_15min', 'solar_radiation']


def encode_leaf_wetness(lw_raw, humidity, rain):
    """
    Encodeaza leaf wetness din valoare continua.
    Senzorul moldovenesc: mean=3.81
    0-5: dry, 5-25: a_bit_wet, 25+: very_wet
    """
    if lw_raw.notna().sum() > len(lw_raw) * 0.1:
        lw = lw_raw.copy()
        if lw.max() <= 1.1:
            lw = lw * 100
        encoded = pd.cut(
            lw,
            bins=[-0.001, 5, 25, 200],
            labels=[0, 1, 2]
        ).astype(float).fillna(0)
        return encoded
    return pd.Series(
        np.where(
            (rain > 1.0) | (humidity > 92), 2,
            np.where((humidity > 78) | (rain > 0.1), 1, 0)
        ),
        index=lw_raw.index
    )


def process_node(node_df, node_id):
    node_df = node_df.copy()
    node_df['col_name'] = node_df['marime'].map(SENSOR_MAP)
    df_mapped = node_df[node_df['col_name'].notna()].copy()

    if len(df_mapped) == 0:
        print(f"  ⚠️  {node_id}: niciun senzor mapabil!")
        return None

    # Timestamp din save_time (unix seconds)
    df_mapped['ts'] = pd.to_datetime(
        df_mapped['save_time'], unit='s', utc=True
    )
    df_mapped['ts_round'] = df_mapped['ts'].dt.round('1h')

    # Pivot wide
    wide = df_mapped.pivot_table(
        index='ts_round',
        columns='col_name',
        values='valoare',
        aggfunc='mean'
    ).reset_index()
    wide.columns.name = None
    wide = wide.rename(columns={'ts_round': 'datetime'})
    wide = wide.sort_values('datetime').reset_index(drop=True)

    # Coloane lipsa
    for col in REQUIRED:
        if col not in wide.columns:
            wide[col] = np.nan

    # Interpolare
    numeric_cols = wide.select_dtypes(include=[np.number]).columns
    for col in numeric_cols:
        wide[col] = wide[col].interpolate(method='linear', limit=6)

    # Leaf wetness encoded
    lw_raw = wide.get('leafwetness_raw',
                      pd.Series(np.nan, index=wide.index))
    hum    = wide.get('humidity',
                      pd.Series(0.0, index=wide.index))
    rain   = wide.get('rain_15min',
                      pd.Series(0.0, index=wide.index))
    wide['leafwetness_encoded'] = encode_leaf_wetness(
        lw_raw, hum, rain
    ).values

    # Metadate
    wide['node_id']   = f"moldova_{node_id.lower()}"
    wide['timestamp'] = wide['datetime'].astype('int64') // 10**6

    # Coloane finale
    final_cols = [
        'node_id', 'timestamp', 'datetime',
        'temperature', 'humidity', 'wind_speed',
        'solar_radiation', 'rain_15min', 'leafwetness_encoded',
    ]
    bonus = [
        'wind_direction', 'wind_gust', 'barometric_pressure',
        'leaf_temperature', 'soil_temperature', 'soil_moisture',
        'soil_ph', 'co2', 'soil_oxygen', 'par',
        'vpd_raw', 'vapor_pressure', 'solar_uv',
        'soil_temp_10cm', 'soil_temp_30cm',
        'soil_moisture_1', 'leafwetness_raw',
        'light_intensity', 'soil_nitrogen',
        'soil_phosphorus', 'soil_potassium',
    ]
    for col in bonus:
        if col in wide.columns:
            final_cols.append(col)

    final_cols = [c for c in final_cols if c in wide.columns]
    result = wide[final_cols].copy()

    before = len(result)
    result = result.dropna(subset=['temperature', 'humidity'])
    after  = len(result)

    return result, before, after


def quality_report(df):
    print("\n" + "=" * 65)
    print("  RAPORT CALITATE DATE MOLDOVA")
    print("=" * 65)
    print(f"\n  Noduri       : {df['node_id'].nunique()}")
    print(f"  Total citiri : {len(df):,}")

    ts = pd.to_datetime(df['timestamp'], unit='s', utc=True)
    print(f"  Perioada     : {ts.min()} -> {ts.max()}")
    print(f"  Ani          : {ts.dt.year.min()} - {ts.dt.year.max()}")

    month    = ts.dt.month
    veg_mask = month.between(4, 10)
    print(f"  Apr-Oct      : {veg_mask.sum():,} "
          f"({veg_mask.mean()*100:.1f}%)")

    print(f"\n  {'Coloana':<25} {'%':>6} "
          f"{'Min':>8} {'Max':>8} {'Mean':>8}")
    print("  " + "-" * 57)

    for col in ['temperature', 'humidity', 'rain_15min',
                'solar_radiation', 'wind_speed',
                'leafwetness_encoded', 'leafwetness_raw',
                'leaf_temperature', 'soil_ph', 'co2', 'par']:
        if col in df.columns:
            v = df[col]
            if v.notna().sum() > 0:
                print(f"  {col:<25} {v.notna().mean()*100:>5.1f}% "
                      f"{v.min():>8.2f} {v.max():>8.2f} "
                      f"{v.mean():>8.2f}")

    lw     = df['leafwetness_encoded']
    labels = {0: 'dry', 1: 'a_bit_wet', 2: 'very_wet'}
    print("\n  Distributie Leaf Wetness:")
    for val in [0, 1, 2]:
        count = (lw == val).sum()
        print(f"    {labels[val]:<12}: {count:,} "
              f"({count/len(lw)*100:.1f}%)")

    print("\n  Senzori bonus unici Moldova:")
    for s in ['leaf_temperature', 'soil_ph', 'co2', 'par',
              'soil_nitrogen', 'soil_phosphorus', 'soil_potassium']:
        if s in df.columns and df[s].notna().sum() > 0:
            print(f"    ✅ {s}: mean={df[s].mean():.2f}")


def main():
    print("=" * 65)
    print("  VineGuard — Moldova Data Processor v2")
    print("=" * 65)

    if not os.path.exists(INPUT_PATH):
        print(f"\n  EROARE: {INPUT_PATH} nu exista!")
        print(f"  Copiaza fisierul CSV in: {INPUT_PATH}")
        return None

    os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)

    print(f"\n  Incarc: {INPUT_PATH}")
    df = pd.read_csv(INPUT_PATH, low_memory=False)
    print(f"  Total randuri brute: {len(df):,}")

    nodes = df['node_name'].unique()
    print(f"  Noduri: {list(nodes)}")

    # Afiseaza senzorii mapati
    print(f"\n  Senzori mapati pentru VineGuard:")
    for marime, col in SENSOR_MAP.items():
        count = (df['marime'] == marime).sum()
        if count > 0:
            vals = df[df['marime'] == marime]['valoare']
            print(f"    {marime:<42} -> {col:<25} "
                  f"({count:,} citiri, mean={vals.mean():.1f})")

    # Proceseaza per nod
    all_nodes = []
    for node_id in nodes:
        node_df = df[df['node_name'] == node_id].copy()
        print(f"\n  Procesez {node_id}: {len(node_df):,} randuri...")

        result = process_node(node_df, node_id)
        if result is None:
            continue

        result_df, before, after = result
        print(f"  ✅ {node_id}: {after:,} citiri orare "
              f"(eliminate: {before - after:,})")
        print(f"     Perioada: "
              f"{result_df['datetime'].min()} -> "
              f"{result_df['datetime'].max()}")
        all_nodes.append(result_df)

    if not all_nodes:
        print("\n  EROARE: Niciun nod procesat!")
        return None

    # Combina
    combined = pd.concat(all_nodes, ignore_index=True)
    combined = combined.sort_values(['node_id', 'timestamp'])

    # Raport
    quality_report(combined)

    # Salveaza
    combined.to_csv(OUTPUT_PATH, index=False)

    print(f"\n  ✅ Salvat: {OUTPUT_PATH}")
    print(f"  ✅ {combined['node_id'].nunique()} noduri")
    print(f"  ✅ {len(combined):,} citiri totale")
    print("\n  Pasii urmatori:")
    print("  1. py feature_engineering.py")
    print("  2. py label_generator.py")
    print("  3. py train.py")
    print("\n  Done!\n")

    return combined


if __name__ == "__main__":
    main()