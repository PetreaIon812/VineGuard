import pandas as pd
import numpy as np
import os
import sys

# ============================================================
# VINEGUARD — Real Data Processor
# Converteste datele reale AgriDataValue din format long
# (un rand per citire per senzor) in format wide
# (un rand per timestamp cu toti senzorii ca coloane)
# compatibil cu feature_engineering.py v3
#
# Input:  fisiere CSV AgriDataValue (pilot_XX_0.csv)
# Output: data/real/raw_data_real.csv
#
# Rulare: py data_processor_real.py
# ============================================================

OUTPUT_DIR  = "data/real"
OUTPUT_PATH = "data/real/raw_data_real.csv"

# Fisierele de procesat — adauga sau elimina dupa nevoie
INPUT_FILES = {
    "pilot_14": "data/real/pilot_14_0.csv",  # Saint-Emilion, Franta
    "pilot_15": "data/real/pilot_15_0.csv",  # Tebano, Emilia-Romagna, Italia
}

# ============================================================
# MAPARE SENZORI — din metric_type la coloanele VineGuard
# ============================================================

SENSOR_MAP = {
    "Air temperature":        "temperature",
    "Air humidity":           "humidity",
    "Rain":                   "rain_15min",
    "Wind speed":             "wind_speed",
    "Wind direction":         "wind_direction",
    "Solar radiation level":  "solar_radiation",
    "Solar radiation":        "solar_radiation",
    "Leaf wetness":           "leafwetness_raw",
    "Barometric Pressure":    "barometric_pressure",
    "Evapotranspiration":     "et0",
    "Wind gust":              "wind_gust",
    "Soil temperature":       "soil_temperature",
    "Water content":          "soil_moisture",
}

# Senzorii necesari pentru VineGuard (minim)
REQUIRED_SENSORS = [
    "temperature",
    "humidity",
    "rain_15min",
    "wind_speed",
    "solar_radiation",
]

# ============================================================
# ENCODARE LEAF WETNESS
# ============================================================

def encode_leaf_wetness(lw_series, humidity, rain, wind):
    if lw_series.notna().sum() > 0:
        lw = lw_series.copy()
        if lw.max() <= 1.0:
            lw = lw * 100
        # Praguri ajustate: 0-10=dry, 10-40=a bit wet, 40+=extremely wet
        encoded = pd.cut(
            lw,
            bins=[-1, 10, 40, 101],
            labels=[0, 1, 2]
        ).astype(float).fillna(0)
        return encoded
    else:
        return pd.Series(
            np.where(
                (rain > 1.0) | (humidity > 92), 2,
                np.where(
                    (humidity > 78) | (rain > 0.1) | (wind < 1.5), 1, 0
                )
            ),
            index=lw_series.index
        )
# ============================================================
# PROCESEAZA UN FISIER
# ============================================================

def process_file(filepath: str, node_id: str) -> pd.DataFrame:
    """
    Citeste un fisier AgriDataValue format long si
    returneaza un DataFrame wide compatibil cu VineGuard.
    """
    print(f"\n  Incarc: {filepath}")
    df = pd.read_csv(filepath, low_memory=False)
    print(f"  Randuri: {len(df):,}")

    # Parse timestamp
    df['ts'] = pd.to_datetime(df['metric_timestamp_utc'], format='ISO8601')

    # Afiseaza locatia
    loc = df['complex_location'].iloc[0] if 'complex_location' in df.columns else 'Unknown'
    print(f"  Locatie: {loc}")

    # Filtreaza doar senzorii relevanti
    df['col_name'] = df['metric_type'].map(SENSOR_MAP)
    df_filtered = df[df['col_name'].notna()].copy()

    print(f"  Tipuri senzori gasiti:")
    for st, col in SENSOR_MAP.items():
        count = (df['metric_type'] == st).sum()
        if count > 0:
            print(f"    {st:<30} → {col:<20} ({count:,} citiri)")

    # Rotunjeste la interval de 15 minute
    df_filtered['ts_rounded'] = df_filtered['ts'].dt.round('15min')

    # Pivot — wide format
    # Daca exista duplicate per (timestamp, sensor), ia media
    wide = df_filtered.pivot_table(
        index='ts_rounded',
        columns='col_name',
        values='metric_value',
        aggfunc='mean'
    ).reset_index()

    wide.columns.name = None
    wide = wide.rename(columns={'ts_rounded': 'datetime'})

    # Sorteaza cronologic
    wide = wide.sort_values('datetime').reset_index(drop=True)

    # Adauga coloane lipsa cu NaN
    for col in REQUIRED_SENSORS:
        if col not in wide.columns:
            print(f"  ⚠️  Senzor lipsa: {col} — va fi interpolat")
            wide[col] = np.nan

    # Interpolare pentru gaps mici (max 4 citiri = 1 ora)
    for col in REQUIRED_SENSORS:
        if col in wide.columns:
            wide[col] = wide[col].interpolate(
                method='linear', limit=4
            )

    # Leaf wetness encoded
    lw_raw   = wide.get('leafwetness_raw', pd.Series(np.nan, index=wide.index))
    humidity = wide.get('humidity',        pd.Series(0.0,   index=wide.index))
    rain     = wide.get('rain_15min',      pd.Series(0.0,   index=wide.index))
    wind     = wide.get('wind_speed',      pd.Series(3.0,   index=wide.index))

    wide['leafwetness_encoded'] = encode_leaf_wetness(
        lw_raw, humidity, rain, wind
    ).values

    # Coloane obligatorii VineGuard
    wide['node_id']   = node_id
    wide['timestamp'] = wide['datetime'].astype('int64') // 10**6  # unix ms

    # Coloane finale in ordinea corecta
    final_cols = [
        'node_id', 'timestamp', 'datetime',
        'temperature', 'humidity', 'wind_speed',
        'solar_radiation', 'rain_15min', 'leafwetness_encoded',
    ]

    # Adauga coloane optionale daca exista
    optional = [
        'wind_direction', 'barometric_pressure',
        'soil_temperature', 'soil_moisture',
        'et0', 'wind_gust',
    ]
    for col in optional:
        if col in wide.columns:
            final_cols.append(col)

    # Pastreaza doar coloanele existente
    final_cols = [c for c in final_cols if c in wide.columns]
    result = wide[final_cols].copy()

    # Elimina randurile cu valori lipsa la senzorii critici
    before = len(result)
    result = result.dropna(subset=['temperature', 'humidity'])
    after  = len(result)

    print(f"  Randuri finale: {after:,} (eliminate: {before-after:,})")
    print(f"  Perioada: {result['datetime'].min()} -> {result['datetime'].max()}")
    print(f"  Coloane: {list(result.columns)}")

    return result


# ============================================================
# STATISTICI CALITATE DATE
# ============================================================

def quality_report(df: pd.DataFrame):
    """Afiseaza raport de calitate pentru datele procesate."""
    print("\n  Raport calitate date reale:")
    print(f"  {'Coloana':<25} {'Non-null':>10} {'%':>8} {'Min':>8} {'Max':>8} {'Mean':>8}")
    print("  " + "-" * 65)

    numeric_cols = df.select_dtypes(include=[np.number]).columns
    for col in numeric_cols:
        if col in ['timestamp']:
            continue
        non_null = df[col].notna().sum()
        pct      = non_null / len(df) * 100
        if non_null > 0:
            print(f"  {col:<25} {non_null:>10,} {pct:>7.1f}% "
                  f"{df[col].min():>8.2f} {df[col].max():>8.2f} "
                  f"{df[col].mean():>8.2f}")

    print()
    lw_dist = df['leafwetness_encoded'].value_counts().sort_index()
    lw_labels = {0: 'dry', 1: 'a_bit_wet', 2: 'extremely_wet'}
    print("  Distributie Leaf Wetness:")
    for val, count in lw_dist.items():
        pct = count / len(df) * 100
        print(f"    {lw_labels.get(int(val), str(val)):<15}: "
              f"{count:,} ({pct:.1f}%)")


# ============================================================
# MAIN
# ============================================================

def main():
    print("=" * 60)
    print("  VineGuard — Real Data Processor")
    print("=" * 60)

    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # Verifica fisierele de input
    available = {}
    for node_id, path in INPUT_FILES.items():
        if os.path.exists(path):
            available[node_id] = path
            print(f"  ✅ Gasit: {path}")
        else:
            print(f"  ❌ Lipsa: {path}")
            print(f"     → Copiaza fisierul in: {path}")

    if not available:
        print("\n  EROARE: Niciun fisier disponibil.")
        print("  Copiaza fisierele CSV in folderul data/real/")
        return None

    # Proceseaza fiecare fisier
    all_dfs = []
    for node_id, path in available.items():
        try:
            df = process_file(path, node_id)
            all_dfs.append(df)
        except Exception as e:
            print(f"  EROARE la {node_id}: {e}")

    if not all_dfs:
        print("\n  EROARE: Niciun fisier procesat cu succes.")
        return None

    # Combina toate nodurile
    combined = pd.concat(all_dfs, ignore_index=True)
    combined = combined.sort_values(['node_id', 'timestamp'])

    print("\n" + "=" * 60)
    print(f"  SUMAR FINAL")
    print("=" * 60)
    print(f"  Noduri reale   : {combined['node_id'].nunique()}")
    print(f"  Total citiri   : {len(combined):,}")
    print(f"  Perioada totala: "
          f"{combined['datetime'].min()} -> "
          f"{combined['datetime'].max()}")

    quality_report(combined)

    # Salveaza
    combined.to_csv(OUTPUT_PATH, index=False)
    print(f"\n  Salvat: {OUTPUT_PATH}")
    print("\n  Pasii urmatori:")
    print("  1. py feature_engineering.py  "
          "(modifica INPUT_PATH la data/real/raw_data_real.csv)")
    print("  2. py label_generator.py")
    print("  3. py train.py")
    print("  4. py compare_models.py")
    print("\n  Done!\n")

    return combined


if __name__ == "__main__":
    main()