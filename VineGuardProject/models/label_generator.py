import pandas as pd
import numpy as np
import os

# ============================================================
# VINEGUARD — Label Generator v3
# Input:  data/real/features_real.csv
# Output: data/real/labeled_data_real.csv
# ============================================================

INPUT_PATH  = "data/real/features_real.csv"
OUTPUT_PATH = "data/real/labeled_data_real.csv"

DISEASES = [
    "downy_mildew",
    "powdery_mildew",
    "botrytis",
    "black_rot",
    "phomopsis",
    "anthracnose",
]

GROWTH_MULTIPLIERS = {
    "downy_mildew":   {0: 0.0, 1: 1.0, 2: 0.8, 3: 0.5},
    "powdery_mildew": {0: 0.0, 1: 0.7, 2: 1.0, 3: 0.8},
    "botrytis":       {0: 0.0, 1: 0.4, 2: 0.7, 3: 1.0},
    "black_rot":      {0: 0.0, 1: 0.8, 2: 1.0, 3: 0.6},
    "phomopsis":      {0: 0.0, 1: 1.0, 2: 0.6, 3: 0.3},
    "anthracnose":    {0: 0.0, 1: 0.9, 2: 1.0, 3: 0.5},
}


# ============================================================
# SCORURI BIOLOGICE
# ============================================================

def score_downy_mildew(df):
    sporul_score   = np.clip(df['dm_sporulation_hours'].values / 8.0, 0, 1)
    infect_score   = df['dm_infection_window_binary'].values
    baldacci_score = df['dm_rule_3_10_timer'].values
    survival_score = df['dm_sporangia_survival'].values
    mech_score     = df['mech_ucsc_dm_primary'].values
    night_rh_score = np.clip(df['night_rh_above_90_hours'].values / 6.0, 0, 1)
    rain_score     = np.clip(df['rain_sum_72h'].values / 15.0, 0, 1)
    light_penalty  = 1.0 - np.clip(df['solar_mean_24h'].values / 600.0, 0, 0.3)
    gh_score       = np.clip(df['grade_hours_24h'].values / 50.0, 0, 1)
    return np.clip(
        sporul_score   * 0.18 + infect_score   * 0.12 +
        baldacci_score * 0.15 + survival_score * 0.10 +
        mech_score     * 0.15 + night_rh_score * 0.10 +
        rain_score     * 0.10 + gh_score       * 0.05 +
        light_penalty  * 0.05, 0.0, 1.0)


def score_powdery_mildew(df):
    gti_score       = df['mech_gubler_thomas_pm'].values
    band_score      = np.clip(df['pm_favorable_band_hours_6h'].values / 12.0, 0, 1)
    ascospore_score = df['pm_ascospore_release_binary'].values
    lethal_penalty  = 1.0 - np.clip(df['pm_lethal_exposure_hours'].values / 4.0, 0, 0.8)
    rain_penalty    = 1.0 - np.clip(df['rain_sum_24h'].values / 10.0, 0, 0.8)
    vpd_score       = np.clip(df['vpd'].values / 2.0, 0, 1)
    temp_band_score = np.clip(df['temp_band_15_25_hours_24h'].values / 12.0, 0, 1)
    return np.clip(
        gti_score * 0.25 + band_score * 0.20 + ascospore_score * 0.10 +
        lethal_penalty * 0.15 + rain_penalty * 0.15 +
        vpd_score * 0.10 + temp_band_score * 0.05, 0.0, 1.0)


def score_botrytis(df):
    bulit_score    = df['bot_rule_15_15_binary'].values
    sev1_score     = df['bot_sev1_conditions'].values
    btb_score      = df['bot_berry_to_berry_risk'].values
    mech_score     = df['mech_ucsc_botrytis_sev1'].values
    night_rh_score = np.clip(df['night_rh_above_90_hours'].values / 8.0, 0, 1)
    rain_score     = np.clip(df['rain_sum_24h'].values / 8.0, 0, 1)
    lw_noct_score  = np.clip(df['leaf_wetness_hours_nocturnal'].values / 6.0, 0, 1)
    return np.clip(
        bulit_score * 0.15 + sev1_score * 0.20 + btb_score * 0.20 +
        mech_score * 0.15 + night_rh_score * 0.10 +
        rain_score * 0.10 + lw_noct_score * 0.10, 0.0, 1.0)


def score_black_rot(df):
    spotts_score  = np.clip(df['br_spotts_lookup_score'].values / 2.0, 0, 1)
    mech_score    = df['mech_spotts_lookup_br'].values
    picn_score    = np.clip(df['br_picnidium_conditions'].values / 8.0, 0, 1)
    primary_score = df['br_primary_ascospore_risk'].values
    mech_prim     = df['mech_br_primary_risk'].values
    rain_score    = np.clip(df['rain_sum_72h'].values / 10.0, 0, 1)
    gh_score      = np.clip(df['grade_hours_24h'].values / 40.0, 0, 1)
    return np.clip(
        spotts_score * 0.25 + mech_score * 0.20 + picn_score * 0.15 +
        primary_score * 0.15 + mech_prim * 0.10 +
        rain_score * 0.10 + gh_score * 0.05, 0.0, 1.0)


def score_phomopsis(df):
    analytis_score = df['phom_analytis_beta_output'].values
    mech_score     = df['mech_analytis_beta_phom'].values
    wetness_score  = df['phom_wetness_sufficient_binary'].values
    early_score    = df['phom_early_season_risk'].values
    rain_score     = np.clip(df['rain_sum_72h'].values / 12.0, 0, 1)
    lw_noct_score  = np.clip(df['leaf_wetness_hours_nocturnal'].values / 4.0, 0, 1)
    return np.clip(
        analytis_score * 0.30 + mech_score * 0.20 + wetness_score * 0.15 +
        early_score * 0.15 + rain_score * 0.10 + lw_noct_score * 0.10, 0.0, 1.0)


def score_anthracnose(df):
    incub_score     = np.clip(df['anth_carisse_incubation_rate'].values * 3.0, 0, 1)
    infect_score    = df['anth_infection_window_binary'].values
    leaf_susc_score = df['anth_leaf_age_susceptibility'].values
    mech_score      = df['mech_carisse_anth_risk'].values
    intense_rain    = np.clip(df['rain_sum_6h'].values / 10.0, 0, 1)
    rain_score      = np.clip(df['rain_sum_24h'].values / 15.0, 0, 1)
    gh_score        = np.clip(df['grade_hours_24h'].values / 45.0, 0, 1)
    return np.clip(
        incub_score * 0.20 + infect_score * 0.15 + leaf_susc_score * 0.20 +
        mech_score * 0.20 + intense_rain * 0.10 +
        rain_score * 0.10 + gh_score * 0.05, 0.0, 1.0)


SCORE_FUNCTIONS = {
    "downy_mildew":   score_downy_mildew,
    "powdery_mildew": score_powdery_mildew,
    "botrytis":       score_botrytis,
    "black_rot":      score_black_rot,
    "phomopsis":      score_phomopsis,
    "anthracnose":    score_anthracnose,
}


def generate_labels(df):
    result = df.copy()
    rng    = np.random.default_rng(42)
    for disease in DISEASES:
        score_fn    = SCORE_FUNCTIONS[disease]
        growth_map  = GROWTH_MULTIPLIERS[disease]
        base_scores = score_fn(df)
        stages      = df['growth_stage'].values.astype(int)
        growth_mult = np.array([growth_map.get(int(s), 0.5) for s in stages])
        final       = np.clip(base_scores * growth_mult +
                              rng.normal(0, 0.025, len(df)), 0.0, 1.0)
        result[f"label_{disease}"] = final
        print(f"    {disease:<22} done (mean={final.mean():.3f})")
    return result


# ============================================================
# MAIN
# ============================================================

def main():
    print("=" * 55)
    print("  VineGuard — Label Generator v3")
    print("=" * 55)

    if not os.path.exists(INPUT_PATH):
        print(f"\n  EROARE: {INPUT_PATH} nu exista.")
        print("  Ruleaza mai intai: py feature_engineering.py")
        return None

    print(f"\n  Incarc features din: {INPUT_PATH}")
    df = pd.read_csv(INPUT_PATH)
    print(f"  Total citiri: {len(df):,}")

    # Filtreaza doar perioada vegetativa (aprilie-octombrie)
    # din timestamp unix milliseconds
    if 'datetime' in df.columns:
        ts = pd.to_datetime(df['datetime'], utc=True)
    else:
        sample_ts = df['timestamp'].iloc[0]
        unit = 'ms' if sample_ts > 1e11 else 's'
        ts = pd.to_datetime(df['timestamp'], unit=unit, utc=True)

    month = ts.dt.month
    veg_mask = (month >= 4) & (month <= 10)
    n_veg = veg_mask.sum()
    print(f"  Filtrez perioada vegetativa (Apr-Oct): {n_veg:,} din {len(df):,} citiri")

    if n_veg == 0:
        print("\n  EROARE: Nicio citire in perioada vegetativa!")
        print("  Verifica datele — poate timestamp-ul e in alt format.")
        print(f"  Sample timestamp: {df['timestamp'].iloc[0]}")
        print(f"  Sample luna: {ts.iloc[0]}")
        return None

    df = df[veg_mask].reset_index(drop=True)
    print(f"  Samples finale : {len(df):,}")
    print(f"  Noduri         : {df['node_id'].nunique()}")
    print(f"  Features       : {len(df.columns)}")

    print("\n  Generez labeluri (vectorizat)...")
    labeled_df = generate_labels(df)

    labeled_df.to_csv(OUTPUT_PATH, index=False)

    label_cols = [f"label_{d}" for d in DISEASES]

    print("\n" + "=" * 55)
    print(f"  Fisier salvat: {OUTPUT_PATH}")
    print("=" * 55)
    print(f"\n  {'Boala':<22} {'Min':>6} {'Mean':>6} "
          f"{'Max':>6} {'Risc>0.5':>10}")
    print("  " + "-" * 50)

    for col in label_cols:
        disease       = col.replace("label_", "")
        vals          = labeled_df[col]
        high_risk_pct = (vals > 0.5).mean() * 100
        print(f"  {disease:<22} {vals.min():>6.3f} "
              f"{vals.mean():>6.3f} {vals.max():>6.3f} "
              f"{high_risk_pct:>9.1f}%")

    print("\n  Risc mediu Downy Mildew per growth stage:")
    for stage, label in {0:"dormant",1:"spring",
                          2:"summer",3:"harvest"}.items():
        mask = labeled_df['growth_stage'] == stage
        if mask.sum() > 0:
            mean_val = labeled_df.loc[mask, 'label_downy_mildew'].mean()
            print(f"    {label:<12}: {mean_val:.3f}")

    print("\n  Done! Ruleaza train.py urmator.\n")
    return labeled_df


if __name__ == "__main__":
    main()