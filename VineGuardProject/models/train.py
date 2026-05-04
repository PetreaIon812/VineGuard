import pandas as pd
import numpy as np
import os
import json
import joblib
from sklearn.metrics import mean_absolute_error, r2_score
from sklearn.multioutput import MultiOutputRegressor
import xgboost as xgb

# ============================================================
# VINEGUARD — Train v3
# Antreneaza XGBoost multi-output pe 66 features v3
# Input:  data/synthetic/labeled_data.csv
# Output: models/vineguard_model.joblib
#         models/model_metadata.json
# ============================================================

INPUT_PATH    = "data/real/labeled_data_real.csv"
MODEL_PATH    = "models/vineguard_model.joblib"
METADATA_PATH = "models/model_metadata.json"

DISEASES = [
    "downy_mildew",
    "powdery_mildew",
    "botrytis",
    "black_rot",
    "phomopsis",
    "anthracnose",
]

# ============================================================
# FEATURE COLS — toate cele 66 din feature_engineering v3
# ============================================================

FEATURE_COLS = [
    # ── Layer A: Meteo de baza (30) ──────────────────────
    "temperature",
    "humidity",
    "wind_speed",
    "solar_radiation",
    "rain_15min",
    "leafwetness_encoded",
    "vpd",
    "dew_point",
    "grade_hours_24h",
    "grade_hours_72h",
    "temp_band_21_30_hours_24h",
    "temp_band_15_25_hours_24h",
    "temp_mean_24h",
    "temp_min_24h",
    "temp_max_24h",
    "temp_mean_72h",
    "humidity_mean_24h",
    "humidity_max_24h",
    "humidity_mean_72h",
    "rain_sum_6h",
    "rain_sum_24h",
    "rain_sum_48h",
    "rain_sum_72h",
    "rain_sum_7d",
    "wind_mean_24h",
    "solar_mean_24h",
    "solar_3d_mean",
    "night_rh_above_90_hours",
    "leaf_wetness_hours_nocturnal",
    "rain_event_binary_48h",

    # ── Layer B: Per boala (19) ───────────────────────────
    "dm_sporulation_hours",
    "dm_infection_window_binary",
    "dm_rule_3_10_timer",
    "dm_sporangia_survival",          # NOU v3
    "pm_lethal_exposure_hours",
    "pm_favorable_band_hours_6h",
    "pm_ascospore_release_binary",
    "bot_rule_15_15_binary",
    "bot_sev1_conditions",            # FIX v3: Mf corect
    "bot_berry_to_berry_risk",        # FIX v3: exponenti exacti
    "br_spotts_lookup_score",
    "br_picnidium_conditions",
    "br_primary_ascospore_risk",      # NOU v3
    "phom_analytis_beta_output",
    "phom_wetness_sufficient_binary",
    "phom_early_season_risk",
    "anth_carisse_incubation_rate",
    "anth_infection_window_binary",
    "anth_leaf_age_susceptibility",   # FIX v3: DD baza 6C

    # ── Layer C: Modele mecaniciste (7) ───────────────────
    "mech_gubler_thomas_pm",
    "mech_analytis_beta_phom",
    "mech_ucsc_dm_primary",           # FIX v3: sporangi survival
    "mech_ucsc_botrytis_sev1",
    "mech_spotts_lookup_br",
    "mech_carisse_anth_risk",
    "mech_br_primary_risk",           # NOU v3

    # ── Layer D: Context / Memorie (10) ───────────────────
    "degree_days_base_0",
    "degree_days_base_8",
    "degree_days_base_10",
    "bbch_growth_stage",
    "growth_stage",
    "schultz_leaf_stage",             # NOU v3
    "prev_season_symptoms_dm",
    "prev_season_symptoms_pm",
    "prev_season_symptoms_bot",
    "onset_date_dm_logged",
]

LABEL_COLS = [f"label_{d}" for d in DISEASES]

# ============================================================
# CONFIGURARE XGBOOST
# ============================================================

XGB_PARAMS = {
    "n_estimators":     300,
    "max_depth":        6,
    "learning_rate":    0.05,
    "subsample":        0.8,
    "colsample_bytree": 0.8,
    "min_child_weight": 5,
    "gamma":            0.1,
    "reg_alpha":        0.1,
    "reg_lambda":       1.0,
    "objective":        "reg:squarederror",
    "eval_metric":      "mae",
    "random_state":     42,
    "n_jobs":           -1,
    "verbosity":        0,
}


# ============================================================
# EVALUARE
# ============================================================

def evaluate_model(model, X_test, y_test):
    y_pred = np.clip(model.predict(X_test), 0.0, 1.0)
    results = {}
    for i, disease in enumerate(DISEASES):
        results[disease] = {
            "mae": round(mean_absolute_error(y_test[:, i], y_pred[:, i]), 4),
            "r2":  round(r2_score(y_test[:, i], y_pred[:, i]), 4),
        }
    return results, y_pred


# ============================================================
# FEATURE IMPORTANCE
# ============================================================

def get_feature_importance(model, feature_cols):
    importances = np.zeros(len(feature_cols))
    for estimator in model.estimators_:
        importances += estimator.feature_importances_
    importances /= len(model.estimators_)
    return pd.DataFrame({
        "feature":    feature_cols,
        "importance": importances,
    }).sort_values("importance", ascending=False).reset_index(drop=True)


# ============================================================
# MAIN
# ============================================================

def main():
    print("=" * 55)
    print("  VineGuard — Model Training v3")
    print("=" * 55)

    if not os.path.exists(INPUT_PATH):
        print(f"\n  EROARE: {INPUT_PATH} nu exista.")
        print("  Ruleaza mai intai: py label_generator.py")
        return

    print(f"\n  Incarc date din: {INPUT_PATH}")
    df = pd.read_csv(INPUT_PATH)
    print(f"  Samples totale : {len(df):,}")
    print(f"  Features       : {len(FEATURE_COLS)}")
    print(f"  Boli (outputs) : {len(DISEASES)}")

    # Verificare coloane
    missing = [c for c in FEATURE_COLS if c not in df.columns]
    if missing:
        print(f"\n  EROARE: Features lipsa: {missing}")
        return

    X = df[FEATURE_COLS].values
    y = df[LABEL_COLS].values

    print(f"\n  X shape: {X.shape}")
    print(f"  y shape: {y.shape}")

    nodes = df["node_id"].unique()
    np.random.seed(42)

    print("\n  Split 80/20 cronologic (per nod)...")
    train_parts_X, train_parts_y = [], []
    test_parts_X,  test_parts_y  = [], []

    for node in nodes:
        node_mask = df["node_id"] == node
        node_X    = X[node_mask]
        node_y    = y[node_mask]
        split_idx = int(len(node_X) * 0.8)
        train_parts_X.append(node_X[:split_idx])
        train_parts_y.append(node_y[:split_idx])
        test_parts_X.append(node_X[split_idx:])
        test_parts_y.append(node_y[split_idx:])

    X_train = np.vstack(train_parts_X)
    y_train = np.vstack(train_parts_y)
    X_test  = np.vstack(test_parts_X)
    y_test  = np.vstack(test_parts_y)

    print(f"  Train : {len(X_train):,} samples (80% per nod)")
    print(f"  Test  : {len(X_test):,} samples  (20% per nod)")
    

    # Antrenare
    print("\n  Antrenez XGBoost MultiOutput...")
    print("  (poate dura 2-5 minute)\n")
    model = MultiOutputRegressor(
        xgb.XGBRegressor(**XGB_PARAMS), n_jobs=-1
    )
    model.fit(X_train, y_train)
    print("  Antrenare finalizata!")

    # Evaluare
    print("\n  Evaluez pe test set...")
    metrics, y_pred = evaluate_model(model, X_test, y_test)

    print("\n" + "=" * 55)
    print(f"  {'Boala':<20} {'MAE':>8} {'R2':>8}")
    print("  " + "-" * 38)
    for disease, m in metrics.items():
        print(f"  {disease:<20} {m['mae']:>8.4f} {m['r2']:>8.4f}")

    avg_mae = np.mean([m["mae"] for m in metrics.values()])
    avg_r2  = np.mean([m["r2"]  for m in metrics.values()])
    print("  " + "-" * 38)
    print(f"  {'MEDIE':<20} {avg_mae:>8.4f} {avg_r2:>8.4f}")
    print("=" * 55)

    # Feature importance
    fi_df = get_feature_importance(model, FEATURE_COLS)
    print("\n  Top 10 Features:")
    print(f"  {'Feature':<35} {'Importance':>10}")
    print("  " + "-" * 47)
    for _, row in fi_df.head(10).iterrows():
        bar = "█" * int(row["importance"] * 100)
        print(f"  {row['feature']:<35} {row['importance']:>10.4f}  {bar}")

    # Salveaza
    os.makedirs("models", exist_ok=True)
    joblib.dump(model, MODEL_PATH)
    print(f"\n  Model salvat: {MODEL_PATH}")

    metadata = {
        "model_type":          "XGBoost MultiOutputRegressor",
        "feature_engineering": "v3",
        "diseases":            DISEASES,
        "feature_cols":        FEATURE_COLS,
        "n_features":          len(FEATURE_COLS),
        "train_samples":       int(len(X_train)),
        "test_samples":        int(len(X_test)),
        "train_nodes": list(nodes),
        "test_nodes":  list(nodes),
        "metrics":             metrics,
        "avg_mae":             round(float(avg_mae), 4),
        "avg_r2":              round(float(avg_r2), 4),
        "xgb_params":          XGB_PARAMS,
        "feature_importance":  fi_df.to_dict(orient="records"),
        "data_source":         "synthetic",
        "version":             "3.0.0",
    }

    with open(METADATA_PATH, "w") as f:
        json.dump(metadata, f, indent=2)
    print(f"  Metadata salvata: {METADATA_PATH}")

    # Exemplu predictie
    print("\n  Exemplu predictie (primul sample din test):")
    pred = np.clip(model.predict(X_test[0:1])[0], 0.0, 1.0)
    print(f"  {'Boala':<20} {'Probabilitate':>15}")
    print("  " + "-" * 36)
    for disease, prob in zip(DISEASES, pred):
        level = "RIDICAT" if prob > 0.6 else ("MEDIU" if prob > 0.3 else "SCAZUT")
        print(f"  {disease:<20} {prob:>6.1%}  {level}")

    print("\n  Done! Ruleaza predict.py urmator.\n")


if __name__ == "__main__":
    main()