import joblib
import json
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

# ============================================================
# VINEGUARD — Feature Importance Visualizer
# Încarcă modelul antrenat și afișează importanța features
# în mai multe moduri.
#
# Rulare: py feature_importance.py
# ============================================================

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

DISEASE_LABELS = {
    "downy_mildew":   "Mană",
    "powdery_mildew": "Făinare",
    "botrytis":       "Botrytis",
    "black_rot":      "Black Rot",
    "phomopsis":      "Phomopsis",
    "anthracnose":    "Antracnoză",
}

# Grupuri de features pentru colorare
FEATURE_GROUPS = {
    "Temporale":      ["day_of_year", "hour", "growth_stage",
                       "degree_days_accumulated"],
    "Temperatură":    ["temperature", "temp_mean_24h", "temp_min_24h",
                       "temp_max_24h", "temp_mean_72h"],
    "Umiditate":      ["humidity", "humidity_mean_24h", "humidity_max_24h",
                       "humidity_mean_72h", "high_humidity_hours_24h",
                       "high_humidity_hours_72h"],
    "Ploaie":         ["rain_15min", "rain_sum_6h", "rain_sum_24h",
                       "rain_sum_72h", "rain_sum_7d",
                       "consecutive_wet_days", "consecutive_dry_days"],
    "Leaf Wetness":   ["leafwetness_encoded", "leaf_wetness_hours_24h",
                       "leaf_wetness_hours_72h"],
    "Vânt & Solar":   ["wind_speed", "wind_mean_24h",
                       "solar_radiation", "solar_mean_24h"],
}

GROUP_COLORS = {
    "Temporale":    "#4CAF50",
    "Temperatură":  "#F44336",
    "Umiditate":    "#2196F3",
    "Ploaie":       "#9C27B0",
    "Leaf Wetness": "#00BCD4",
    "Vânt & Solar": "#FF9800",
}


def get_feature_group(feature_name):
    for group, features in FEATURE_GROUPS.items():
        if feature_name in features:
            return group
    return "Altele"


def load_model_and_metadata():
    print("  Încarc modelul...")
    model    = joblib.load(MODEL_PATH)
    with open(METADATA_PATH) as f:
        metadata = json.load(f)
    print(f"  Model: {metadata['model_type']}")
    print(f"  Features: {metadata['n_features']}")
    print(f"  Train samples: {metadata['train_samples']:,}")
    return model, metadata


def compute_importance(model, feature_cols):
    """
    Calculează feature importance per boală și medie globală.
    """
    importances_per_disease = {}
    global_importance       = np.zeros(len(feature_cols))

    for i, (estimator, disease) in enumerate(
        zip(model.estimators_, DISEASES)
    ):
        fi = estimator.feature_importances_
        importances_per_disease[disease] = fi
        global_importance += fi

    global_importance /= len(DISEASES)

    return global_importance, importances_per_disease


# ============================================================
# GRAFIC 1 — Feature Importance Globală (toate bolile)
# ============================================================

def plot_global_importance(global_importance, feature_cols):
    fi_df = pd.DataFrame({
        "feature":    feature_cols,
        "importance": global_importance,
    }).sort_values("importance", ascending=True)

    colors = [
        GROUP_COLORS.get(get_feature_group(f), "#888888")
        for f in fi_df["feature"]
    ]

    fig, ax = plt.subplots(figsize=(10, 12))

    bars = ax.barh(
        fi_df["feature"],
        fi_df["importance"],
        color=colors,
        edgecolor="white",
        linewidth=0.5,
    )

    # Valori pe bare
    for bar, val in zip(bars, fi_df["importance"]):
        ax.text(
            bar.get_width() + 0.001,
            bar.get_y() + bar.get_height() / 2,
            f"{val:.3f}",
            va="center", ha="left",
            fontsize=8, color="#333333",
        )

    # Legendă grupuri
    legend_patches = [
        mpatches.Patch(color=color, label=group)
        for group, color in GROUP_COLORS.items()
    ]
    ax.legend(
        handles=legend_patches,
        loc="lower right",
        fontsize=9,
        title="Grup feature",
    )

    ax.set_xlabel("Importanță medie (toate bolile)", fontsize=11)
    ax.set_title(
        "VineGuard — Feature Importance Globală\n"
        "(medie peste cele 6 boli)",
        fontsize=13, fontweight="bold",
    )
    ax.grid(axis="x", alpha=0.3)
    ax.set_xlim(0, fi_df["importance"].max() * 1.15)

    plt.tight_layout()
    plt.savefig("docs/feature_importance_global.png", dpi=150,
                bbox_inches="tight")
    plt.show()
    print("  Salvat: docs/feature_importance_global.png")


# ============================================================
# GRAFIC 2 — Top 10 Features per Boală (heatmap)
# ============================================================

def plot_heatmap(importances_per_disease, feature_cols):
    matrix = np.array([
        importances_per_disease[d] for d in DISEASES
    ])  # shape: (6, 29)

    df = pd.DataFrame(
        matrix,
        index=[DISEASE_LABELS[d] for d in DISEASES],
        columns=feature_cols,
    )

    # Selectează top 15 features după importanță globală
    top15 = df.mean(axis=0).nlargest(15).index
    df_top = df[top15]

    fig, ax = plt.subplots(figsize=(14, 5))

    im = ax.imshow(
        df_top.values,
        cmap="YlOrRd",
        aspect="auto",
    )

    ax.set_xticks(range(len(top15)))
    ax.set_xticklabels(top15, rotation=45, ha="right", fontsize=9)
    ax.set_yticks(range(len(DISEASES)))
    ax.set_yticklabels([DISEASE_LABELS[d] for d in DISEASES], fontsize=10)

    # Valori în celule
    for i in range(len(DISEASES)):
        for j in range(len(top15)):
            val = df_top.values[i, j]
            text_color = "white" if val > 0.1 else "black"
            ax.text(j, i, f"{val:.3f}",
                    ha="center", va="center",
                    fontsize=7, color=text_color)

    plt.colorbar(im, ax=ax, label="Feature Importance")
    ax.set_title(
        "VineGuard — Feature Importance per Boală (Top 15 features)",
        fontsize=13, fontweight="bold", pad=15,
    )

    plt.tight_layout()
    plt.savefig("docs/feature_importance_heatmap.png", dpi=150,
                bbox_inches="tight")
    plt.show()
    print("  Salvat: docs/feature_importance_heatmap.png")


# ============================================================
# GRAFIC 3 — Importanță per Grup de Features
# ============================================================

def plot_group_importance(global_importance, feature_cols):
    group_totals = {}
    for feat, imp in zip(feature_cols, global_importance):
        group = get_feature_group(feat)
        group_totals[group] = group_totals.get(group, 0) + imp

    # Normalizează la 100%
    total = sum(group_totals.values())
    group_pct = {k: v / total * 100 for k, v in group_totals.items()}
    group_pct = dict(sorted(group_pct.items(),
                            key=lambda x: x[1], reverse=True))

    fig, ax = plt.subplots(figsize=(8, 5))

    bars = ax.bar(
        group_pct.keys(),
        group_pct.values(),
        color=[GROUP_COLORS.get(g, "#888888") for g in group_pct.keys()],
        edgecolor="white",
        linewidth=0.8,
    )

    for bar, val in zip(bars, group_pct.values()):
        ax.text(
            bar.get_x() + bar.get_width() / 2,
            bar.get_height() + 0.5,
            f"{val:.1f}%",
            ha="center", va="bottom",
            fontsize=10, fontweight="bold",
        )

    ax.set_ylabel("Importanță relativă (%)", fontsize=11)
    ax.set_title(
        "VineGuard — Importanță per Grup de Features",
        fontsize=13, fontweight="bold",
    )
    ax.set_ylim(0, max(group_pct.values()) * 1.15)
    ax.grid(axis="y", alpha=0.3)

    plt.tight_layout()
    plt.savefig("docs/feature_importance_groups.png", dpi=150,
                bbox_inches="tight")
    plt.show()
    print("  Salvat: docs/feature_importance_groups.png")


# ============================================================
# PRINT — Tabel text în terminal
# ============================================================

def print_importance_table(global_importance, importances_per_disease,
                           feature_cols):
    fi_df = pd.DataFrame({
        "feature": feature_cols,
        "global":  global_importance,
    })
    for disease in DISEASES:
        fi_df[DISEASE_LABELS[disease]] = importances_per_disease[disease]

    fi_df = fi_df.sort_values("global", ascending=False).reset_index(
        drop=True
    )

    print("\n" + "=" * 90)
    print(f"  {'#':>3}  {'Feature':<28} {'Global':>7}  " +
          "  ".join(f"{DISEASE_LABELS[d]:>8}" for d in DISEASES))
    print("  " + "-" * 86)

    for i, row in fi_df.iterrows():
        group = get_feature_group(row["feature"])
        vals  = "  ".join(
            f"{row[DISEASE_LABELS[d]]:>8.4f}" for d in DISEASES
        )
        print(f"  {i+1:>3}. {row['feature']:<28} "
              f"{row['global']:>7.4f}  {vals}")

    print("=" * 90)

    print("\n  Importanță per grup:")
    group_totals = {}
    for feat, imp in zip(feature_cols, global_importance):
        g = get_feature_group(feat)
        group_totals[g] = group_totals.get(g, 0) + imp
    total = sum(group_totals.values())
    for g, v in sorted(group_totals.items(),
                       key=lambda x: x[1], reverse=True):
        bar = "█" * int(v / total * 40)
        print(f"    {g:<15} {v/total*100:>5.1f}%  {bar}")


# ============================================================
# MAIN
# ============================================================

def main():
    import os
    os.makedirs("docs", exist_ok=True)

    print("=" * 55)
    print("  VineGuard — Feature Importance Visualizer")
    print("=" * 55 + "\n")

    model, metadata = load_model_and_metadata()
    feature_cols    = metadata["feature_cols"]

    global_importance, importances_per_disease = compute_importance(
        model, feature_cols
    )

    # Tabel în terminal
    print_importance_table(
        global_importance, importances_per_disease, feature_cols
    )

    # Grafice
    print("\n  Generez grafice...")
    plot_global_importance(global_importance, feature_cols)
    plot_heatmap(importances_per_disease, feature_cols)
    plot_group_importance(global_importance, feature_cols)

    print("\n  Done! Graficele sunt în folderul /docs\n")


if __name__ == "__main__":
    main()