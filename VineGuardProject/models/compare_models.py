import pandas as pd
import numpy as np
import os
import json
import joblib
import time
import warnings
warnings.filterwarnings('ignore')

from sklearn.multioutput import MultiOutputRegressor
from sklearn.tree import DecisionTreeRegressor
from sklearn.ensemble import (
    RandomForestRegressor, ExtraTreesRegressor,
    GradientBoostingRegressor
)
from sklearn.linear_model import Ridge, Lasso
from sklearn.neural_network import MLPRegressor
from sklearn.metrics import mean_absolute_error, r2_score
from sklearn.preprocessing import StandardScaler
import xgboost as xgb
import lightgbm as lgb

# ============================================================
# VINEGUARD — Model Comparison for ONIA
# Compara 10 modele ML pe features v3 (66 features)
# Genereaza tabel comparativ + grafice pentru prezentare
#
# Rulare: py compare_models.py
# Output: docs/model_comparison.json
#         docs/model_comparison_results.csv
# ============================================================

INPUT_PATH = "data/synthetic/labeled_data.csv"
OUTPUT_DIR = "docs"

DISEASES = [
    "downy_mildew",
    "powdery_mildew",
    "botrytis",
    "black_rot",
    "phomopsis",
    "anthracnose",
]

FEATURE_COLS = [
    "temperature", "humidity", "wind_speed", "solar_radiation",
    "rain_15min", "leafwetness_encoded", "vpd", "dew_point",
    "grade_hours_24h", "grade_hours_72h",
    "temp_band_21_30_hours_24h", "temp_band_15_25_hours_24h",
    "temp_mean_24h", "temp_min_24h", "temp_max_24h", "temp_mean_72h",
    "humidity_mean_24h", "humidity_max_24h", "humidity_mean_72h",
    "rain_sum_6h", "rain_sum_24h", "rain_sum_48h",
    "rain_sum_72h", "rain_sum_7d",
    "wind_mean_24h", "solar_mean_24h", "solar_3d_mean",
    "night_rh_above_90_hours", "leaf_wetness_hours_nocturnal",
    "rain_event_binary_48h",
    "dm_sporulation_hours", "dm_infection_window_binary",
    "dm_rule_3_10_timer", "dm_sporangia_survival",
    "pm_lethal_exposure_hours", "pm_favorable_band_hours_6h",
    "pm_ascospore_release_binary",
    "bot_rule_15_15_binary", "bot_sev1_conditions",
    "bot_berry_to_berry_risk",
    "br_spotts_lookup_score", "br_picnidium_conditions",
    "br_primary_ascospore_risk",
    "phom_analytis_beta_output", "phom_wetness_sufficient_binary",
    "phom_early_season_risk",
    "anth_carisse_incubation_rate", "anth_infection_window_binary",
    "anth_leaf_age_susceptibility",
    "mech_gubler_thomas_pm", "mech_analytis_beta_phom",
    "mech_ucsc_dm_primary", "mech_ucsc_botrytis_sev1",
    "mech_spotts_lookup_br", "mech_carisse_anth_risk",
    "mech_br_primary_risk",
    "degree_days_base_0", "degree_days_base_8", "degree_days_base_10",
    "bbch_growth_stage", "growth_stage", "schultz_leaf_stage",
    "prev_season_symptoms_dm", "prev_season_symptoms_pm",
    "prev_season_symptoms_bot", "onset_date_dm_logged",
]

LABEL_COLS = [f"label_{d}" for d in DISEASES]


# ============================================================
# DEFINITII MODELE
# ============================================================

def get_models():
    """Returneaza dictionarul de modele de comparat."""
    return {
        # ── Baselines ────────────────────────────────────
        "Ridge Regression": MultiOutputRegressor(
            Ridge(alpha=1.0), n_jobs=-1
        ),
        "LASSO Regression": MultiOutputRegressor(
            Lasso(alpha=0.01, max_iter=2000), n_jobs=-1
        ),
        "Decision Tree": MultiOutputRegressor(
            DecisionTreeRegressor(max_depth=8, random_state=42),
            n_jobs=-1
        ),

        # ── Ensemble Trees ────────────────────────────────
        "Random Forest": RandomForestRegressor(
            n_estimators=100, max_depth=10,
            random_state=42, n_jobs=-1
        ),
        "Extra Trees": ExtraTreesRegressor(
            n_estimators=100, max_depth=10,
            random_state=42, n_jobs=-1
        ),
        "Gradient Boosting": MultiOutputRegressor(
            GradientBoostingRegressor(
                n_estimators=100, max_depth=5,
                learning_rate=0.1, random_state=42
            ), n_jobs=-1
        ),

        # ── Advanced Boosting ─────────────────────────────
        "XGBoost": MultiOutputRegressor(
            xgb.XGBRegressor(
                n_estimators=300, max_depth=6,
                learning_rate=0.05, subsample=0.8,
                colsample_bytree=0.8, random_state=42,
                n_jobs=-1, verbosity=0,
            ), n_jobs=-1
        ),
        "LightGBM": MultiOutputRegressor(
            lgb.LGBMRegressor(
                n_estimators=300, max_depth=6,
                learning_rate=0.05, subsample=0.8,
                colsample_bytree=0.8, random_state=42,
                n_jobs=-1, verbose=-1,
            ), n_jobs=-1
        ),

        # ── Neural Network ────────────────────────────────
        "MLP Neural Network": MLPRegressor(
            hidden_layer_sizes=(256, 128, 64),
            activation='relu',
            max_iter=200,
            random_state=42,
            early_stopping=True,
            validation_fraction=0.1,
        ),
    }


# ============================================================
# EVALUARE
# ============================================================

def evaluate(model, X_test, y_test, needs_scaling=False,
             scaler=None) -> dict:
    if needs_scaling and scaler:
        X_test = scaler.transform(X_test)

    y_pred = np.clip(model.predict(X_test), 0.0, 1.0)

    results = {}
    for i, disease in enumerate(DISEASES):
        mae = mean_absolute_error(y_test[:, i], y_pred[:, i])
        r2  = r2_score(y_test[:, i], y_pred[:, i])
        results[disease] = {
            "mae": round(float(mae), 4),
            "r2":  round(float(r2), 4),
        }

    avg_mae = float(np.mean([v["mae"] for v in results.values()]))
    avg_r2  = float(np.mean([v["r2"]  for v in results.values()]))

    return {
        "per_disease": results,
        "avg_mae":     round(avg_mae, 4),
        "avg_r2":      round(avg_r2, 4),
    }


# ============================================================
# MAIN
# ============================================================

def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    print("=" * 65)
    print("  VineGuard — Model Comparison for ONIA")
    print("=" * 65)

    # Incarca date
    print(f"\n  Incarc date din: {INPUT_PATH}")
    df = pd.read_csv(INPUT_PATH)
    print(f"  Samples: {len(df):,} | Features: {len(FEATURE_COLS)}")

    missing = [c for c in FEATURE_COLS if c not in df.columns]
    if missing:
        print(f"\n  EROARE: Features lipsa: {missing}")
        return

    X = df[FEATURE_COLS].values
    y = df[LABEL_COLS].values

    # Split pe noduri (evita data leakage)
    nodes      = df["node_id"].unique()
    np.random.seed(42)
    test_nodes  = np.random.choice(nodes, size=2, replace=False)
    train_nodes = [n for n in nodes if n not in test_nodes]

    train_mask = df["node_id"].isin(train_nodes)
    test_mask  = df["node_id"].isin(test_nodes)

    X_train, y_train = X[train_mask], y[train_mask]
    X_test,  y_test  = X[test_mask],  y[test_mask]

    print(f"  Train: {len(X_train):,} | Test: {len(X_test):,}")
    print(f"  Test noduri: {list(test_nodes)}\n")

    # Scaler pentru modele liniare si MLP
    scaler  = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled  = scaler.transform(X_test)

    models_needing_scaling = {"Ridge Regression", "LASSO Regression",
                               "MLP Neural Network"}

    # Antreneaza si evalueaza fiecare model
    models  = get_models()
    results = {}

    print(f"  {'Model':<25} {'MAE':>8} {'R²':>8} {'Timp(s)':>10}")
    print("  " + "-" * 55)

    for name, model in models.items():
        needs_scaling = name in models_needing_scaling

        try:
            t_start = time.time()

            if needs_scaling:
                model.fit(X_train_scaled, y_train)
                metrics = evaluate(model, X_test_scaled, y_test)
            else:
                model.fit(X_train, y_train)
                metrics = evaluate(model, X_test, y_test)

            elapsed = time.time() - t_start

            results[name] = {
                "metrics": metrics,
                "train_time_s": round(elapsed, 1),
                "needs_scaling": needs_scaling,
            }

            print(f"  {name:<25} {metrics['avg_mae']:>8.4f} "
                  f"{metrics['avg_r2']:>8.4f} {elapsed:>9.1f}s")

        except Exception as e:
            print(f"  {name:<25} EROARE: {e}")
            results[name] = {"error": str(e)}

    # ── Ranking ──────────────────────────────────────────────
    print("\n" + "=" * 65)
    print("  RANKING FINAL (dupa MAE crescator = mai bun)")
    print("=" * 65)

    valid   = {k: v for k, v in results.items() if "metrics" in v}
    ranking = sorted(valid.items(),
                     key=lambda x: x[1]["metrics"]["avg_mae"])

    print(f"\n  {'#':<4} {'Model':<25} {'MAE':>8} {'R²':>8} {'Timp':>8}")
    print("  " + "-" * 57)

    for i, (name, data) in enumerate(ranking, 1):
        m      = data["metrics"]
        medal  = ["🥇", "🥈", "🥉"][i-1] if i <= 3 else f"#{i} "
        print(f"  {medal} {name:<25} {m['avg_mae']:>8.4f} "
              f"{m['avg_r2']:>8.4f} {data['train_time_s']:>7.1f}s")

    # ── Detalii per boala pentru castigator ──────────────────
    best_name, best_data = ranking[0]
    print(f"\n  Detalii {best_name} (castigator) per boala:")
    print(f"  {'Boala':<22} {'MAE':>8} {'R²':>8}")
    print("  " + "-" * 40)
    for disease, m in best_data["metrics"]["per_disease"].items():
        print(f"  {disease:<22} {m['mae']:>8.4f} {m['r2']:>8.4f}")

    # ── Salveaza rezultate ────────────────────────────────────
    output = {
        "ranking": [
            {
                "rank":          i + 1,
                "model":         name,
                "avg_mae":       data["metrics"]["avg_mae"],
                "avg_r2":        data["metrics"]["avg_r2"],
                "train_time_s":  data["train_time_s"],
                "per_disease":   data["metrics"]["per_disease"],
            }
            for i, (name, data) in enumerate(ranking)
        ],
        "test_nodes":    list(test_nodes),
        "train_samples": int(len(X_train)),
        "test_samples":  int(len(X_test)),
        "n_features":    len(FEATURE_COLS),
        "n_diseases":    len(DISEASES),
    }

    json_path = os.path.join(OUTPUT_DIR, "model_comparison.json")
    with open(json_path, "w") as f:
        json.dump(output, f, indent=2)
    print(f"\n  Rezultate salvate: {json_path}")

    # CSV pentru ONIA
    rows = []
    for i, (name, data) in enumerate(ranking):
        row = {
            "rank":         i + 1,
            "model":        name,
            "avg_mae":      data["metrics"]["avg_mae"],
            "avg_r2":       data["metrics"]["avg_r2"],
            "train_time_s": data["train_time_s"],
        }
        for disease, m in data["metrics"]["per_disease"].items():
            row[f"mae_{disease}"] = m["mae"]
            row[f"r2_{disease}"]  = m["r2"]
        rows.append(row)

    csv_path = os.path.join(OUTPUT_DIR, "model_comparison.csv")
    pd.DataFrame(rows).to_csv(csv_path, index=False)
    print(f"  CSV salvat: {csv_path}")

    # ── Grafic ────────────────────────────────────────────────
    try:
        import matplotlib.pyplot as plt
        import matplotlib.patches as mpatches

        names   = [r[0] for r in ranking]
        mae_vals = [r[1]["metrics"]["avg_mae"] for r in ranking]
        r2_vals  = [r[1]["metrics"]["avg_r2"]  for r in ranking]
        times    = [r[1]["train_time_s"]        for r in ranking]

        colors = ["#2ecc71" if i == 0 else "#3498db" if i == 1
                  else "#e74c3c" if i >= len(names) - 2
                  else "#95a5a6"
                  for i in range(len(names))]

        fig, axes = plt.subplots(1, 3, figsize=(18, 6))
        fig.suptitle("VineGuard — Comparatie Modele ML (ONIA 2026)",
                     fontsize=14, fontweight="bold")

        # MAE
        bars = axes[0].barh(names[::-1], mae_vals[::-1],
                            color=colors[::-1], edgecolor="white")
        for bar, val in zip(bars, mae_vals[::-1]):
            axes[0].text(bar.get_width() + 0.0005,
                         bar.get_y() + bar.get_height() / 2,
                         f"{val:.4f}", va="center", fontsize=8)
        axes[0].set_xlabel("MAE (mai mic = mai bun)")
        axes[0].set_title("MAE Medie")
        axes[0].grid(axis="x", alpha=0.3)

        # R²
        bars2 = axes[1].barh(names[::-1], r2_vals[::-1],
                             color=colors[::-1], edgecolor="white")
        for bar, val in zip(bars2, r2_vals[::-1]):
            axes[1].text(bar.get_width() + 0.001,
                         bar.get_y() + bar.get_height() / 2,
                         f"{val:.4f}", va="center", fontsize=8)
        axes[1].set_xlabel("R² (mai mare = mai bun)")
        axes[1].set_title("R² Mediu")
        axes[1].grid(axis="x", alpha=0.3)

        # Timp antrenare
        bars3 = axes[2].barh(names[::-1], times[::-1],
                             color=colors[::-1], edgecolor="white")
        for bar, val in zip(bars3, times[::-1]):
            axes[2].text(bar.get_width() + 0.5,
                         bar.get_y() + bar.get_height() / 2,
                         f"{val:.1f}s", va="center", fontsize=8)
        axes[2].set_xlabel("Timp antrenare (secunde)")
        axes[2].set_title("Eficienta")
        axes[2].grid(axis="x", alpha=0.3)

        legend = [
            mpatches.Patch(color="#2ecc71", label="Loc 1"),
            mpatches.Patch(color="#3498db", label="Loc 2"),
            mpatches.Patch(color="#95a5a6", label="Restul"),
            mpatches.Patch(color="#e74c3c", label="Ultimele 2"),
        ]
        fig.legend(handles=legend, loc="lower center",
                   ncol=4, fontsize=9, bbox_to_anchor=(0.5, -0.02))

        plt.tight_layout()
        chart_path = os.path.join(OUTPUT_DIR, "model_comparison_chart.png")
        plt.savefig(chart_path, dpi=150, bbox_inches="tight")
        plt.show()
        print(f"  Grafic salvat: {chart_path}")

    except Exception as e:
        print(f"  Grafic indisponibil: {e}")

    print("\n  Done!\n")
    return results


if __name__ == "__main__":
    main()