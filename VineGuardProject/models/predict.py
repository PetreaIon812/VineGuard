import pandas as pd
import numpy as np
import os
import json
import joblib
from datetime import datetime

# ============================================================
# VINEGUARD — Predict v2
# Primeste date recente pentru un nod si returneaza
# probabilitatile de risc pentru cele 6 boli.
#
# Importa functiile din feature_engineering.py — nu
# recalculeaza nimic, reutilizeaza acelasi pipeline.
#
# Mod de utilizare:
#   1. Demo local : py predict.py
#   2. Ca modul   : from predict import predict_node
# ============================================================

MODEL_PATH    = "models/vineguard_model.joblib"
METADATA_PATH = "models/model_metadata.json"
FEATURES_PATH = "data/synthetic/features.csv"

DISEASES = [
    "downy_mildew",
    "powdery_mildew",
    "botrytis",
    "black_rot",
    "phomopsis",
    "anthracnose",
]

RISK_LEVELS = [
    (0.70, "CRITIC",  "🔴"),
    (0.50, "RIDICAT", "🟠"),
    (0.30, "MEDIU",   "🟡"),
    (0.00, "SCAZUT",  "🟢"),
]

READINGS_PER_HOUR = 4
READINGS_PER_DAY  = 96


# ============================================================
# DERIVE LEAF WETNESS (folosit cand senzorul lipseste)
# ============================================================

def derive_leaf_wetness(humidity: float,
                        rain_15min: float,
                        wind_speed: float) -> int:
    if rain_15min > 1.0 or humidity > 92:
        return 2
    elif humidity > 78 or rain_15min > 0.1 or wind_speed < 1.5:
        return 1
    else:
        return 0


# ============================================================
# CONSTRUIESTE FEATURE VECTOR DIN CITIRI RECENTE
# Reutilizeaza exact aceleasi functii din feature_engineering.py
# ============================================================

def build_feature_vector(readings: list) -> pd.DataFrame:
    """
    Primeste o lista de citiri recente (dict) si returneaza
    un DataFrame cu toate features v2 calculate, gata de
    pasat modelului.

    Fiecare citire trebuie sa contina:
    - timestamp (unix ms)
    - temperature, humidity, wind_speed, solar_radiation, rain_15min
    - leafwetness_encoded (optional — derivat daca lipseste)
    """
    if len(readings) < 1:
        raise ValueError("Sunt necesare cel putin 1 citire.")

    # Importa functiile din feature_engineering
    from feature_engineering import (
        layer_a_base_meteo,
        layer_b_downy_mildew,
        layer_b_powdery_mildew,
        layer_b_botrytis,
        layer_b_black_rot,
        layer_b_phomopsis,
        layer_b_anthracnose,
        layer_c_mechanistic,
        layer_d_context,
    )

    df = pd.DataFrame(readings)
    df = df.sort_values("timestamp").reset_index(drop=True)

    # Deriva leaf wetness daca lipseste
    if "leafwetness_encoded" not in df.columns:
        df["leafwetness_encoded"] = df.apply(
            lambda r: derive_leaf_wetness(
                r["humidity"], r["rain_15min"], r["wind_speed"]
            ), axis=1
        )

    # Adauga coloane calendar
    ts = pd.to_datetime(df["timestamp"], unit="ms")
    df["day_of_year"] = ts.dt.dayofyear
    df["hour"]        = ts.dt.hour
    doy               = df["day_of_year"]
    df["growth_stage"] = np.select(
        [doy < 90, doy < 150, doy < 240, doy <= 310],
        [0, 1, 2, 3], default=0
    )
    df["node_id"] = "realtime"

    # Calculeaza toate layers
    la = layer_a_base_meteo(df)
    lb = pd.concat([
        layer_b_downy_mildew(df),
        layer_b_powdery_mildew(df),
        layer_b_botrytis(df),
        layer_b_black_rot(df),
        layer_b_phomopsis(df),
        layer_b_anthracnose(df),
    ], axis=1)
    lc = layer_c_mechanistic(df, la, lb)
    ld = layer_d_context(df)

    features = pd.concat([
        la.reset_index(drop=True),
        lb.reset_index(drop=True),
        lc.reset_index(drop=True),
        ld.reset_index(drop=True),
    ], axis=1)

    features = features.fillna(0.0)

    # Returneaza doar ultimul rand (citirea curenta)
    return features.iloc[[-1]]


# ============================================================
# PREDICTIE PRINCIPALA
# ============================================================

def predict_node(readings: list,
                 node_id: str = "unknown") -> dict:
    """
    Ruleaza predictia pentru un nod dat.

    Args:
        readings: Lista de citiri recente (max 14 zile = 1344 citiri)
        node_id:  ID-ul nodului

    Returns:
        Dict cu probabilitatile per boala si metadata Firebase-ready
    """
    if not os.path.exists(MODEL_PATH):
        raise FileNotFoundError(
            f"Modelul nu exista: {MODEL_PATH}\n"
            "Ruleaza mai intai: py train.py"
        )

    model = joblib.load(MODEL_PATH)

    with open(METADATA_PATH) as f:
        metadata = json.load(f)

    feature_cols = metadata["feature_cols"]

    # Calculeaza features v2
    features_df = build_feature_vector(readings)

    # Verifica ca toate coloanele necesare exista
    missing = [c for c in feature_cols if c not in features_df.columns]
    if missing:
        raise ValueError(f"Features lipsa dupa calcul: {missing}")

    X    = features_df[feature_cols].values
    probs = np.clip(model.predict(X)[0], 0.0, 1.0)

    # Construieste rezultatul
    predictions = {}
    for disease, prob in zip(DISEASES, probs):
        level = next(
            label for threshold, label, _ in RISK_LEVELS
            if prob >= threshold
        )
        predictions[disease] = {
            "probability": round(float(prob), 4),
            "risk_level":  level,
        }

    result = {
        "node_id":       node_id,
        "timestamp":     int(readings[-1]["timestamp"]),
        "datetime":      datetime.fromtimestamp(
            int(readings[-1]["timestamp"]) / 1000
        ).isoformat(),
        "predictions":   predictions,
        "model_version": metadata.get("version", "2.0.0"),
        "data_source":   metadata.get("data_source", "synthetic"),
    }

    return result


# ============================================================
# FORMAT OUTPUT
# ============================================================

def print_predictions(result: dict):
    print("\n" + "=" * 55)
    print("  VineGuard — Predictie Risc Boli")
    print("=" * 55)
    print(f"  Nod       : {result['node_id']}")
    print(f"  Timestamp : {result['datetime']}")
    print(f"  Model     : {result['model_version']} "
          f"({result['data_source']})")
    print("=" * 55)
    print(f"  {'Boala':<22} {'Probabilitate':>13} {'Nivel':>10}")
    print("  " + "-" * 48)

    for disease, data in result["predictions"].items():
        prob  = data["probability"]
        level = data["risk_level"]
        emoji = next(e for t, l, e in RISK_LEVELS if l == level)
        print(f"  {disease:<22} {prob:>8.1%}    {emoji} {level:<8}")

    print("=" * 55)


# ============================================================
# DEMO LOCAL — ia date din features.csv
# ============================================================

def run_demo():
    print("=" * 55)
    print("  VineGuard — Predict Demo v2")
    print("=" * 55)

    if not os.path.exists(FEATURES_PATH):
        print(f"\n  EROARE: {FEATURES_PATH} nu exista.")
        print("  Ruleaza mai intai pipeline-ul complet.")
        return

    df = pd.read_csv(FEATURES_PATH)

    # Ultimele 14 zile din node_05
    demo_node = "node_05"
    node_df = df[df["node_id"] == demo_node]
    # Extrage day_of_year din timestamp
    node_df = node_df.copy()
    node_df['day_of_year'] = pd.to_datetime(node_df['timestamp'], unit='ms').dt.dayofyear
    node_df = node_df[node_df['day_of_year'].between(120, 134)]

    print(f"\n  Demo pe nodul : {demo_node}")
    print(f"  Citiri folosite: {len(node_df)}")

    # Converteste la formatul asteptat de predict_node
    readings = []
    for _, row in node_df.iterrows():
        readings.append({
            "timestamp":           int(row["timestamp"]),
            "temperature":         float(row["temperature"]),
            "humidity":            float(row["humidity"]),
            "wind_speed":          float(row["wind_speed"]),
            "solar_radiation":     float(row["solar_radiation"]),
            "rain_15min":          float(row["rain_15min"]),
            "leafwetness_encoded": int(row["leafwetness_encoded"]),
        })

    result = predict_node(readings, node_id=demo_node)
    print_predictions(result)

    print("\n  Output JSON (structura Firebase):")
    print(json.dumps(result, indent=2, ensure_ascii=False))

    print("\n  Done!\n")
    return result


# ============================================================
# MAIN
# ============================================================

if __name__ == "__main__":
    run_demo()