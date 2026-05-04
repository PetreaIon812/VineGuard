import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import os
import json

# ============================================================
# VINEGUARD — Synthetic Data Generator
# Generează date climatice simulate pentru 2 sezoane
# × N noduri virtuale cu distribuții realiste pentru
# clima Moldovei/României
# ============================================================

RANDOM_SEED = 42
np.random.seed(RANDOM_SEED)

# Configurare generală
NUM_NODES = 10
INTERVAL_MINUTES = 15
SEASONS = [2023, 2024]
SEASON_START_MONTH = 4   # Aprilie
SEASON_END_MONTH = 10    # Octombrie

OUTPUT_DIR = "data/synthetic"

# ============================================================
# PROFIL CLIMATIC LUNAR — Moldova/România
# Valorile reprezintă (medie, std) pentru fiecare parametru
# ============================================================

MONTHLY_CLIMATE = {
    #  luna: (temp_mean, temp_std, humidity_mean, humidity_std,
    #         wind_mean, solar_mean, rain_prob)
    4:  (12.0, 4.0,  72.0, 12.0, 3.2, 350.0, 0.35),  # Aprilie
    5:  (17.5, 4.5,  68.0, 11.0, 3.0, 480.0, 0.30),  # Mai
    6:  (22.0, 4.0,  65.0, 10.0, 2.8, 580.0, 0.25),  # Iunie
    7:  (24.5, 3.5,  60.0,  9.0, 2.5, 620.0, 0.20),  # Iulie
    8:  (24.0, 3.5,  62.0,  9.0, 2.6, 560.0, 0.22),  # August
    9:  (18.5, 4.0,  70.0, 10.0, 3.0, 400.0, 0.30),  # Septembrie
    10: (12.0, 4.5,  78.0, 12.0, 3.5, 220.0, 0.38),  # Octombrie
}

# ============================================================
# GROWTH STAGE — bazat pe day of year
# ============================================================

def get_growth_stage(doy: int) -> int:
    """
    Returnează stadiul de creștere bazat pe ziua din an.
    0 = dormant, 1 = spring (muguri/frunze tinere),
    2 = summer (înflorit/berry growth), 3 = harvest/pre-recoltă
    """
    if doy < 90 or doy > 310:
        return 0  # dormant
    elif doy < 150:
        return 1  # primăvară — risc maxim infecție
    elif doy < 240:
        return 2  # vară — înflorit, berry growth
    else:
        return 3  # pre-recoltă


# ============================================================
# LEAF WETNESS — derivat din humidity și ploaie
# ============================================================

def derive_leaf_wetness(humidity: float,
                        rain_15min: float,
                        wind_speed: float) -> int:
    """
    Estimează leaf wetness din parametrii atmosferici.
    0 = dry, 1 = a_bit_wet, 2 = extremely_wet
    Va fi înlocuit cu senzor real în producție.
    """
    if rain_15min > 1.0 or humidity > 92:
        return 2  # extremely wet
    elif humidity > 78 or rain_15min > 0.1 or wind_speed < 1.5:
        return 1  # a bit wet
    else:
        return 0  # dry


# ============================================================
# GENERATOR CITIRE SINGULARĂ (15 minute)
# ============================================================

def generate_reading(month: int,
                     hour: int,
                     prev_rain: float = 0.0) -> dict:
    """
    Generează o citire de senzor pentru o lună și oră date.
    Simulează variații diurne realiste.
    """
    climate = MONTHLY_CLIMATE[month]
    temp_mean, temp_std, hum_mean, hum_std, wind_mean, solar_mean, rain_prob = climate

    # Variație diurnă temperatură (mai cald ziua, mai rece noaptea)
    diurnal_temp = 4.0 * np.sin((hour - 6) * np.pi / 12)
    temperature = np.random.normal(temp_mean + diurnal_temp, temp_std)
    temperature = np.clip(temperature, -5.0, 42.0)

    # Variație diurnă umiditate (mai umed noaptea)
    diurnal_hum = -8.0 * np.sin((hour - 6) * np.pi / 12)
    humidity = np.random.normal(hum_mean + diurnal_hum, hum_std)
    humidity = np.clip(humidity, 20.0, 100.0)

    # Viteză vânt
    wind_speed = np.random.exponential(wind_mean)
    wind_speed = np.clip(wind_speed, 0.0, 20.0)

    # Direcție vânt (grade)
    wind_direction = np.random.uniform(0, 360)

    # Radiație solară (0 noaptea, peak la prânz)
    if 6 <= hour <= 20:
        solar_factor = np.sin((hour - 6) * np.pi / 14)
        solar_radiation = np.random.normal(
            solar_mean * solar_factor,
            solar_mean * 0.2
        )
        solar_radiation = max(0.0, solar_radiation)
    else:
        solar_radiation = 0.0

    # Ploaie — eveniment stochastic cu persistență
    # (dacă a plouat anterior, probabilitate mai mare să continue)
    rain_prob_adjusted = rain_prob * 1.5 if prev_rain > 0 else rain_prob
    rain_prob_adjusted = min(rain_prob_adjusted, 0.75)

    if np.random.random() < rain_prob_adjusted:
        rain_15min = np.random.exponential(1.2)
        rain_15min = min(rain_15min, 25.0)
    else:
        rain_15min = 0.0

    # Leaf wetness derivat
    leafwetness_encoded = derive_leaf_wetness(
        humidity, rain_15min, wind_speed
    )

    return {
        "temperature": round(temperature, 2),
        "humidity": round(humidity, 2),
        "wind_speed": round(wind_speed, 2),
        "wind_direction": round(wind_direction, 1),
        "solar_radiation": round(solar_radiation, 1),
        "rain_15min": round(rain_15min, 3),
        "leafwetness_encoded": leafwetness_encoded,
    }


# ============================================================
# GENERATOR SEZON COMPLET
# ============================================================

def generate_season(year: int, node_id: str) -> pd.DataFrame:
    """
    Generează un sezon complet (aprilie–octombrie) pentru un nod.
    Returnează DataFrame cu toate citirile la 15 minute.
    """
    records = []

    start_date = datetime(year, SEASON_START_MONTH, 1, 0, 0)
    end_date = datetime(year, SEASON_END_MONTH, 31, 23, 45)

    current_dt = start_date
    prev_rain = 0.0

    while current_dt <= end_date:
        month = current_dt.month
        if month not in MONTHLY_CLIMATE:
            current_dt += timedelta(minutes=INTERVAL_MINUTES)
            continue

        hour = current_dt.hour
        doy = current_dt.timetuple().tm_yday

        reading = generate_reading(month, hour, prev_rain)
        prev_rain = reading["rain_15min"]

        record = {
            "node_id": node_id,
            "timestamp": int(current_dt.timestamp() * 1000),  # unix ms
            "datetime": current_dt.isoformat(),
            "year": year,
            "month": month,
            "day": current_dt.day,
            "hour": hour,
            "day_of_year": doy,
            "growth_stage": get_growth_stage(doy),
            **reading,
        }

        records.append(record)
        current_dt += timedelta(minutes=INTERVAL_MINUTES)

    return pd.DataFrame(records)


# ============================================================
# MAIN — generează toate nodurile și sezoanele
# ============================================================

def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    all_dfs = []
    total_records = 0

    print("=" * 55)
    print("  VineGuard — Synthetic Data Generator")
    print("=" * 55)

    for year in SEASONS:
        for node_idx in range(NUM_NODES):
            node_id = f"node_{node_idx:02d}"
            print(f"  Generez: {year} | {node_id} ...", end=" ")

            df = generate_season(year, node_id)
            all_dfs.append(df)
            total_records += len(df)

            print(f"{len(df):,} citiri")

    # Combină toate datele
    full_df = pd.concat(all_dfs, ignore_index=True)

    # Sortează după timestamp
    full_df = full_df.sort_values(
        ["node_id", "timestamp"]
    ).reset_index(drop=True)

    # Salvează CSV complet
    output_path = os.path.join(OUTPUT_DIR, "raw_data.csv")
    full_df.to_csv(output_path, index=False)

    # Statistici sumar
    print("\n" + "=" * 55)
    print(f"  Total citiri generate : {total_records:,}")
    print(f"  Noduri                : {NUM_NODES}")
    print(f"  Sezoane               : {SEASONS}")
    print(f"  Coloane               : {len(full_df.columns)}")
    print(f"  Fișier salvat         : {output_path}")
    print("=" * 55)

    # Preview distribuții
    print("\n  Statistici parametri principali:")
    print(full_df[[
        "temperature", "humidity",
        "wind_speed", "solar_radiation",
        "rain_15min", "leafwetness_encoded"
    ]].describe().round(2).to_string())

    # Distribuție leaf wetness
    lw_dist = full_df["leafwetness_encoded"].value_counts().sort_index()
    lw_labels = {0: "dry", 1: "a_bit_wet", 2: "extremely_wet"}
    print("\n  Distribuție Leaf Wetness:")
    for val, count in lw_dist.items():
        pct = count / total_records * 100
        print(f"    {lw_labels[val]:15s}: {count:8,} ({pct:.1f}%)")

    # Distribuție growth stage
    gs_dist = full_df["growth_stage"].value_counts().sort_index()
    gs_labels = {0: "dormant", 1: "spring", 2: "summer", 3: "harvest"}
    print("\n  Distribuție Growth Stage:")
    for val, count in gs_dist.items():
        pct = count / total_records * 100
        print(f"    {gs_labels[val]:15s}: {count:8,} ({pct:.1f}%)")

    print("\n  Done! Rulează feature_engineering.py următor.\n")

    return full_df


if __name__ == "__main__":
    main()