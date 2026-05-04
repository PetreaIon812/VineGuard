import numpy as np
import pandas as pd
import os

# ============================================================
# VINEGUARD — Feature Engineering v3
# ============================================================
# Corecții față de v2 bazate pe literatura validată:
#
# [FIX 1] Botrytis Mf factor: continuu (ore umede/24)
#         nu binar — Gonzalez et al. 2015, PLOS ONE
#         O ora e "umeda" daca: rain>=0.2mm SAU LWD>=30min
#         SAU RH>=90%
#
# [FIX 2] MYGR exponenti exacti: 3.78 * Teq^0.9 * (1-Teq)^-0.475 * Mf
#         SPOR exponenti exacti: 3.7 * Teq^0.9 * (1-Teq)^-10.49
#
# [FIX 3] Supravietuire sporangi DM: solar dependent
#         cer senin (solar>400): viabili max 6-8h
#         cer acoperit (solar<100): viabili 12-24h
#         UV direct letal in 15 minute
#
# [FIX 4] Fereastra oospori DM: activi de la DOY>=75
#         fara limita superioara (Rossi 2008)
#
# [FIX 5] Anthracnoza susceptibilitate: DD baza 6°C de la 1 apr
#         la 525-560 DD susceptibilitate ~50%
#         zero la veraison (BBCH 81 ~ 1200 DD baza 10)
#
# [FIX 6] Black Rot primar: ascospori din mumii declansati
#         de ploaie in fereastra dezmugurit-inflorire
#
# [NOU] Schultz 1992: start la 160 DD baza 8°C
#       rata aparitie frunze din DD
#
# Input:  data/synthetic/raw_data.csv
# Output: data/synthetic/features.csv
# ============================================================

INPUT_PATH        = "data/real/raw_data_real.csv"
OUTPUT_PATH       = "data/real/features_real.csv"
READINGS_PER_HOUR = 4
READINGS_PER_DAY  = 96


# ============================================================
# FUNCȚII AUXILIARE
# ============================================================

def compute_vpd(temp_c: np.ndarray, rh_pct: np.ndarray) -> np.ndarray:
    """Vapor Pressure Deficit in kPa (Tetens)."""
    es  = 0.6108 * np.exp(17.27 * temp_c / (temp_c + 237.3))
    return np.clip(es * (1.0 - rh_pct / 100.0), 0.0, None)


def compute_dew_point(temp_c: np.ndarray, rh_pct: np.ndarray) -> np.ndarray:
    """Punct de roua Celsius (Magnus-Tetens)."""
    a, b  = 17.625, 243.04
    alpha = np.log(np.clip(rh_pct / 100.0, 1e-6, 1.0)) + (a * temp_c) / (b + temp_c)
    return (b * alpha) / (a - alpha)


def consecutive_counter(condition_series: pd.Series) -> pd.Series:
    """Numara valori consecutive True."""
    groups = (~condition_series).cumsum()
    return condition_series.groupby(groups).cumsum()


def hours_in_band(temp: pd.Series, t_min: float, t_max: float,
                  window_hours: int = 24) -> pd.Series:
    """Ore rolling in care T e in [t_min, t_max]."""
    in_band = ((temp >= t_min) & (temp <= t_max)).astype(float)
    return in_band.rolling(window_hours * READINGS_PER_HOUR, min_periods=1).sum() / READINGS_PER_HOUR


def compute_mf_botrytis(df: pd.DataFrame) -> pd.Series:
    """
    [FIX 1] Factorul de umiditate Mf pentru Botrytis
    (Gonzalez et al. 2015, PLOS ONE).

    O ora e 'umeda' daca oricare din conditii e adevarata:
    - Precipitatii R >= 0.2 mm (suma pe ora)
    - LWD >= 30 min (>= 2 citiri la 15 min cu LW >= 1)
    - RH medie orara >= 90%

    Mf = numar_ore_umede / 24
    """
    # Suma ploaie per ora (4 citiri)
    rain_hourly = df['rain_15min'].rolling(READINGS_PER_HOUR, min_periods=1).sum()
    rain_wet    = (rain_hourly >= 0.2).astype(float)

    # LWD >= 30 min per ora (>= 2 citiri din 4 cu LW >= 1)
    lw_binary   = (df['leafwetness_encoded'] >= 1).astype(float)
    lwd_hourly  = lw_binary.rolling(READINGS_PER_HOUR, min_periods=1).sum()
    lwd_wet     = (lwd_hourly >= 2).astype(float)

    # RH >= 90% medie orara
    rh_hourly   = df['humidity'].rolling(READINGS_PER_HOUR, min_periods=1).mean()
    rh_wet      = (rh_hourly >= 90).astype(float)

    # O ora e umeda daca oricare conditie e True
    hour_wet    = ((rain_wet == 1) | (lwd_wet == 1) | (rh_wet == 1)).astype(float)

    # Mf = ore umede / 24 pe fereastra 24h
    wet_hours_24h = hour_wet.rolling(24 * READINGS_PER_HOUR, min_periods=1).sum() / READINGS_PER_HOUR
    return np.clip(wet_hours_24h / 24.0, 0.0, 1.0)


def compute_sporangia_survival(df: pd.DataFrame) -> pd.Series:
    """
    [FIX 3] Rata de supravietuire a sporangilor DM
    in functie de radiatia solara.

    cer senin (solar > 400 W/m2): viabili max 6-8h
    cer partial (100-400):        viabili 8-16h
    cer acoperit (< 100):         viabili 12-24h
    UV direct lethal in 15 min

    Returneaza un factor 0-1: 1=supravietuire maxima
    """
    solar = df['solar_radiation']

    # Factor supravietuire invers proportional cu radiatia
    # La solar=0 (noapte/innorirat): factor=1.0
    # La solar=600+ (cer senin): factor=0.1
    survival = np.where(
        solar < 100,  1.0,
        np.where(
            solar < 400,  1.0 - 0.5 * (solar - 100) / 300,
            np.maximum(0.1, 1.0 - 0.9 * (solar - 400) / 600)
        )
    )

    # Penalizare suplimentara pentru expunere UV prelungita (>4h solar>400)
    high_solar_hours = (solar > 400).astype(float).rolling(
        4 * READINGS_PER_HOUR, min_periods=1).sum() / READINGS_PER_HOUR
    uv_penalty = np.clip(1.0 - high_solar_hours / 8.0, 0.1, 1.0)

    return pd.Series(survival * uv_penalty, index=df.index)


def compute_anthracnose_susceptibility(df: pd.DataFrame) -> pd.Series:
    """
    [FIX 5] Susceptibilitate Antracnoza bazata pe DD baza 6C
    (Carisse 2021/2024).

    Frunze < 6 zile: susceptibilitate maxima (1.0)
    La 525-560 DD (baza 6C): susceptibilitate ~50%
    La veraison (~1200 DD baza 10C = BBCH 81): ~0
    Scade brusc dupa inflorire, nu liniar.
    """
    # DD baza 6C cumulate de la 1 aprilie (DOY 91)
    t      = df['temperature']
    dd6    = np.clip(t - 6.0, 0, None) / (24.0 * READINGS_PER_HOUR)

    if 'day_of_year' in df.columns:
        reset = (df['day_of_year'] == 91).astype(int).cumsum()
        dd6_cum = dd6.groupby(reset).cumsum()
    else:
        dd6_cum = dd6.cumsum()

    # Functie de susceptibilitate — scade rapid dupa 500 DD
    # La 0 DD: 1.0 (frunze tinere)
    # La 525 DD: ~0.5
    # La 1000+ DD: ~0
    susc = 1.0 / (1.0 + np.exp((dd6_cum - 525) / 80))

    return pd.Series(np.clip(susc, 0.0, 1.0), index=df.index)


def compute_schultz_leaf_stage(df: pd.DataFrame) -> pd.Series:
    """
    [NOU] Modelul de crestere Schultz 1992.
    Porneste la 160 DD baza 8C de la dezmugurit.
    Returneaza numarul estimat de frunze desfacute.
    """
    t   = df['temperature']
    dd8 = np.clip(t - 8.0, 0, None) / (24.0 * READINGS_PER_HOUR)

    if 'day_of_year' in df.columns:
        reset   = (df['day_of_year'] == 91).astype(int).cumsum()
        dd8_cum = dd8.groupby(reset).cumsum()
    else:
        dd8_cum = dd8.cumsum()

    # Inainte de 160 DD: muguri inca inchisi
    # Dupa 160 DD: ~1 frunza noua per 40 DD
    leaves = np.where(
        dd8_cum < 160, 0,
        (dd8_cum - 160) / 40.0
    )

    # Normalizeaza la [0,1] (max ~20 frunze = 800 DD dupa start)
    return pd.Series(np.clip(leaves / 20.0, 0.0, 1.0), index=df.index)


# ============================================================
# LAYER A — FEATURES METEO DE BAZA (28 features)
# ============================================================

def layer_a_base_meteo(df: pd.DataFrame) -> pd.DataFrame:
    out = pd.DataFrame(index=df.index)
    t, h, r, s, w = (df['temperature'], df['humidity'],
                     df['rain_15min'], df['solar_radiation'], df['wind_speed'])
    lw = (df['leafwetness_encoded'] >= 1).astype(float)

    # Instantanee
    out['temperature']         = t
    out['humidity']            = h
    out['wind_speed']          = w
    out['solar_radiation']     = s
    out['rain_15min']          = r
    out['leafwetness_encoded'] = df['leafwetness_encoded']

    # Derivate instantanee
    out['vpd']       = compute_vpd(t.values, h.values)
    out['dew_point'] = compute_dew_point(t.values, h.values)

    # Grade-hours (Schumacher 2022 — prag 50)
    gh = t * lw / READINGS_PER_HOUR
    out['grade_hours_24h'] = gh.rolling(24 * READINGS_PER_HOUR, min_periods=1).sum()
    out['grade_hours_72h'] = gh.rolling(72 * READINGS_PER_HOUR, min_periods=1).sum()

    # Ore in benzi de temperatura
    out['temp_band_21_30_hours_24h'] = hours_in_band(t, 21.0, 30.0, 24)
    out['temp_band_15_25_hours_24h'] = hours_in_band(t, 15.0, 25.0, 24)

    # Rolling temperatura
    out['temp_mean_24h'] = t.rolling(24 * READINGS_PER_HOUR, min_periods=1).mean()
    out['temp_min_24h']  = t.rolling(24 * READINGS_PER_HOUR, min_periods=1).min()
    out['temp_max_24h']  = t.rolling(24 * READINGS_PER_HOUR, min_periods=1).max()
    out['temp_mean_72h'] = t.rolling(72 * READINGS_PER_HOUR, min_periods=1).mean()

    # Rolling umiditate
    out['humidity_mean_24h'] = h.rolling(24 * READINGS_PER_HOUR, min_periods=1).mean()
    out['humidity_max_24h']  = h.rolling(24 * READINGS_PER_HOUR, min_periods=1).max()
    out['humidity_mean_72h'] = h.rolling(72 * READINGS_PER_HOUR, min_periods=1).mean()

    # Rolling ploaie
    out['rain_sum_6h']  = r.rolling(6  * READINGS_PER_HOUR, min_periods=1).sum()
    out['rain_sum_24h'] = r.rolling(24 * READINGS_PER_HOUR, min_periods=1).sum()
    out['rain_sum_48h'] = r.rolling(48 * READINGS_PER_HOUR, min_periods=1).sum()
    out['rain_sum_72h'] = r.rolling(72 * READINGS_PER_HOUR, min_periods=1).sum()
    out['rain_sum_7d']  = r.rolling(7  * READINGS_PER_DAY,  min_periods=1).sum()

    # Rolling vant si solar
    out['wind_mean_24h']  = w.rolling(24 * READINGS_PER_HOUR, min_periods=1).mean()
    out['solar_mean_24h'] = s.rolling(24 * READINGS_PER_HOUR, min_periods=1).mean()
    out['solar_3d_mean']  = s.rolling(3  * READINGS_PER_DAY,  min_periods=1).mean()

    # Umiditate nocturna (sporulare DM)
    hour     = df['hour'].values if 'hour' in df.columns else np.full(len(df), 12)
    is_night = ((hour >= 22) | (hour <= 6)).astype(float)
    night_rh = ((h >= 90) & (is_night == 1)).astype(float)
    out['night_rh_above_90_hours']     = night_rh.rolling(
        24 * READINGS_PER_HOUR, min_periods=1).sum() / READINGS_PER_HOUR
    out['leaf_wetness_hours_nocturnal'] = (lw * is_night).rolling(
        24 * READINGS_PER_HOUR, min_periods=1).sum() / READINGS_PER_HOUR

    # Eveniment ploaie (regula 3-10 Baldacci)
    out['rain_event_binary_48h'] = (out['rain_sum_48h'] >= 10.0).astype(float)

    return out


# ============================================================
# LAYER B — FEATURES PER BOALA (18 features + 4 noi = 22)
# ============================================================

def layer_b_downy_mildew(df: pd.DataFrame) -> pd.DataFrame:
    """
    Plasmopara viticola — Mana.
    [FIX 3] Supravietuire sporangi solar-dependenta.
    [FIX 4] Oospori activi de la DOY>=75 fara limita superioara.
    """
    out      = pd.DataFrame(index=df.index)
    hour     = df['hour'].values if 'hour' in df.columns else np.full(len(df), 12)
    is_night = ((hour >= 22) | (hour <= 6)).astype(float)
    lw       = (df['leafwetness_encoded'] >= 1).astype(float)

    # F1: ore sporulare nocturna (RH>=95% + T in [12,30] + noapte)
    sporul = ((df['humidity'] >= 95) &
              (df['temperature'] >= 12) &
              (df['temperature'] <= 30) &
              (is_night == 1)).astype(float)
    out['dm_sporulation_hours'] = sporul.rolling(
        24 * READINGS_PER_HOUR, min_periods=1).sum() / READINGS_PER_HOUR

    # F2: fereastra infectie (LWD>=2h la T in [4,30] noapte)
    lwd_ok = ((lw == 1) &
              (df['temperature'] >= 4) &
              (df['temperature'] <= 30) &
              (is_night == 1)).astype(float)
    roll2h = lwd_ok.rolling(2 * READINGS_PER_HOUR, min_periods=1).sum()
    out['dm_infection_window_binary'] = (roll2h >= 1.5 * READINGS_PER_HOUR).astype(float)

    # F3: timer 3-10 Baldacci
    rain_48h = df['rain_15min'].rolling(48 * READINGS_PER_HOUR, min_periods=1).sum()
    t_mean24 = df['temperature'].rolling(24 * READINGS_PER_HOUR, min_periods=1).mean()
    trigger  = ((rain_48h >= 10.0) & (t_mean24 >= 10.0)).astype(int)
    MAX_VAL  = 288.0
    timer    = np.zeros(len(df))
    for i in range(len(df)):
        timer[i] = MAX_VAL if trigger.iloc[i] == 1 else max(
            0.0, (timer[i-1] if i > 0 else 0.0) - 1.0)
    out['dm_rule_3_10_timer'] = timer / MAX_VAL

    # F4 [FIX 3]: supravietuire sporangi solar-dependenta
    out['dm_sporangia_survival'] = compute_sporangia_survival(df)

    return out


def layer_b_powdery_mildew(df: pd.DataFrame) -> pd.DataFrame:
    """
    Erysiphe necator — Fainare.
    Gubler-Thomas cu penalizare corecta la 38°C/2h.
    """
    out = pd.DataFrame(index=df.index)

    # F1: ore expunere letala (T>=38°C pentru 2h = letal per carte)
    lethal = (df['temperature'] >= 38).astype(float)
    out['pm_lethal_exposure_hours'] = lethal.rolling(
        24 * READINGS_PER_HOUR, min_periods=1).sum() / READINGS_PER_HOUR

    # F2: ore in banda favorabila Gubler-Thomas (21-30°C, RH<85%)
    favorable = ((df['temperature'] >= 21) &
                 (df['temperature'] <= 30) &
                 (df['humidity'] < 85)).astype(bool)
    consec = consecutive_counter(pd.Series(favorable, index=df.index))
    out['pm_favorable_band_hours_6h'] = (consec / READINGS_PER_HOUR).clip(upper=24.0)

    # F3: eliberare ascospori (2.5mm + 8-12h LWD + T in [15,25])
    rain_24h  = df['rain_15min'].rolling(24 * READINGS_PER_HOUR, min_periods=1).sum()
    lwd_24h   = (df['leafwetness_encoded'] >= 1).astype(float).rolling(
        24 * READINGS_PER_HOUR, min_periods=1).sum() / READINGS_PER_HOUR
    out['pm_ascospore_release_binary'] = (
        (rain_24h >= 2.5) &
        (lwd_24h >= 8.0) & (lwd_24h <= 14.0) &
        (df['temperature'] >= 15) & (df['temperature'] <= 25)
    ).astype(float)

    return out


def layer_b_botrytis(df: pd.DataFrame) -> pd.DataFrame:
    """
    Botrytis cinerea — Putregai cenusiu.
    [FIX 1] Mf factor continuu (Gonzalez 2015 exact).
    [FIX 2] Exponenti MYGR si SPOR exacti din carte.
    """
    out = pd.DataFrame(index=df.index)
    lw  = (df['leafwetness_encoded'] >= 1).astype(float)
    t   = df['temperature']

    # F1: regula 15-15 Bulit (LWD>=15h AND T>=15°C)
    lwd_24h    = lw.rolling(24 * READINGS_PER_HOUR, min_periods=1).sum() / READINGS_PER_HOUR
    lwd_sum    = lw.rolling(24 * READINGS_PER_HOUR, min_periods=1).sum()
    t_wet      = (t * lw).rolling(24 * READINGS_PER_HOUR, min_periods=1).sum()
    t_mean_wet = np.where(lwd_sum > 0, t_wet / lwd_sum, 0.0)
    out['bot_rule_15_15_binary'] = ((lwd_24h >= 15.0) & (t_mean_wet >= 15.0)).astype(float)

    # [FIX 1+2] Calculeaza Mf corect (Gonzalez 2015)
    mf = compute_mf_botrytis(df)

    # F2: MYGR — rata de crestere miceliu cu Mf corect si exponenti exacti
    # Formula: MYGR = 3.78 * Teq^0.9 * (1-Teq)^(-0.475) * Mf
    teq      = np.clip((t - 0) / 35.0, 0.01, 0.99)
    t_mygr   = 3.78 * np.power(teq, 0.9) * np.power(np.clip(1 - teq, 0.01, None), -0.475)
    mygr     = t_mygr * mf.values
    # Normalizeaza — valoarea maxima teoretica ~3.78 * 0.9^0.9 * 0.1^-0.475 ≈ 12
    out['bot_sev1_conditions'] = pd.Series(
        np.clip(mygr / 12.0, 0.0, 1.0), index=df.index)

    # F3: SPOR — sporulare cu exponenti exacti din carte
    # Formula: SPOR = 3.7 * Teq^0.9 * (1-Teq)^(-10.49) * [factor_RH]
    rh_factor = 1.0 / (1.0 + np.exp(35.36 - 0.26 * df['humidity']))
    t_spor    = 3.7 * np.power(teq, 0.9) * np.power(np.clip(1 - teq, 0.01, None), -10.49)
    spor      = t_spor * rh_factor
    # Normalizeaza — max teoretic mult mai mare din cauza exponentului -10.49
    out['bot_berry_to_berry_risk'] = pd.Series(
        np.clip(spor / 50.0, 0.0, 1.0), index=df.index)

    return out


def layer_b_black_rot(df: pd.DataFrame) -> pd.DataFrame:
    """
    Guignardia bidwellii — Black Rot.
    [FIX 6] Adaugat feature pentru ascospori primari din mumii.
    """
    out     = pd.DataFrame(index=df.index)
    lw      = (df['leafwetness_encoded'] >= 1).astype(float)
    lwd_24h = lw.rolling(24 * READINGS_PER_HOUR, min_periods=1).sum() / READINGS_PER_HOUR
    lwd_sum = lw.rolling(24 * READINGS_PER_HOUR, min_periods=1).sum()
    t_wet   = (df['temperature'] * lw).rolling(24 * READINGS_PER_HOUR, min_periods=1).sum()
    t_mean_wet = np.where(lwd_sum > 0, t_wet / lwd_sum, 0.0)

    # F1: scor Spotts lookup (tabelul LWD x T, Spotts 1977)
    def spotts_required_lwd(t_arr):
        t_arr    = np.asarray(t_arr, dtype=float)
        required = np.full_like(t_arr, 999.0)
        valid    = (t_arr >= 7) & (t_arr <= 32)
        for i in range(len(t_arr)):
            if not valid[i]: continue
            tv = t_arr[i]
            if   tv <= 10:   required[i] = 24.0
            elif tv <= 15:   required[i] = 24 + (12 - 24) * (tv - 10) / 5
            elif tv <= 21:   required[i] = 12 + (9  - 12) * (tv - 15) / 6
            elif tv <= 26.5: required[i] = 9  + (6  -  9) * (tv - 21) / 5.5
            else:            required[i] = 6  + (12 -  6) * (tv - 26.5) / 5.5
        return required

    req_lwd = spotts_required_lwd(t_mean_wet)
    out['br_spotts_lookup_score'] = np.clip(
        lwd_24h / np.maximum(req_lwd, 1e-6), 0.0, 2.0)

    # F2: conditii picnidie (RH>=90% AND T in [10,30])
    picn = ((df['humidity'] >= 90) &
            (df['temperature'] >= 10) &
            (df['temperature'] <= 30)).astype(float)
    out['br_picnidium_conditions'] = picn.rolling(
        24 * READINGS_PER_HOUR, min_periods=1).sum() / READINGS_PER_HOUR

    # F3 [FIX 6]: ascospori primari din mumii
    # Declansati de ploaie in fereastra dezmugurit-inflorire (spring stage)
    # Fereastra: growth_stage==1 (DOY 90-149)
    if 'growth_stage' in df.columns:
        in_primary_window = (df['growth_stage'] == 1).astype(float)
    else:
        doy = df['day_of_year'] if 'day_of_year' in df.columns else pd.Series(150, index=df.index)
        in_primary_window = ((doy >= 90) & (doy <= 149)).astype(float)

    rain_trigger = (df['rain_15min'] > 0).astype(float)
    out['br_primary_ascospore_risk'] = (rain_trigger * in_primary_window).rolling(
        24 * READINGS_PER_HOUR, min_periods=1).mean()

    return out


def layer_b_phomopsis(df: pd.DataFrame) -> pd.DataFrame:
    """Phomopsis viticola — Excorioza (Erincik 2003)."""
    out     = pd.DataFrame(index=df.index)
    lw      = (df['leafwetness_encoded'] >= 1).astype(float)
    lwd_24h = lw.rolling(24 * READINGS_PER_HOUR, min_periods=1).sum() / READINGS_PER_HOUR
    t       = df['temperature']

    ALPHA, BETA, GAMMA, DELTA = 6.0, 1.5, 1.7, 1.1
    TMIN, TMAX = 5.0, 35.5
    teq    = np.clip((t - TMIN) / (TMAX - TMIN), 1e-3, 1 - 1e-3)
    t_comp = np.power(teq, BETA) * np.power(1 - teq, GAMMA)
    w_comp = np.power(np.maximum(lwd_24h, 0.01), DELTA)
    out['phom_analytis_beta_output'] = np.clip(ALPHA * t_comp * w_comp / 150.0, 0.0, 1.0)

    band_high = (t >= 16) & (t <= 22) & (lwd_24h >= 5.0)
    band_low  = (t >= 10) & (t <  16) & (lwd_24h >= 10.0)
    out['phom_wetness_sufficient_binary'] = (band_high | band_low).astype(float)

    out['phom_early_season_risk'] = (
        (df['growth_stage'] == 1).astype(float)
        if 'growth_stage' in df.columns else 0.0
    )

    return out


def layer_b_anthracnose(df: pd.DataFrame) -> pd.DataFrame:
    """
    Elsinoe ampelina — Antracnoza.
    [FIX 5] Susceptibilitate DD baza 6°C, scade la 525 DD ~50%,
            zero la veraison (Carisse 2021/2024).
    """
    out     = pd.DataFrame(index=df.index)
    lw      = (df['leafwetness_encoded'] >= 1).astype(float)
    lwd_24h = lw.rolling(24 * READINGS_PER_HOUR, min_periods=1).sum() / READINGS_PER_HOUR
    t       = df['temperature']

    # F1: rata incubatie Carisse (fit exponential Carisse 2024)
    t_mean24 = t.rolling(24 * READINGS_PER_HOUR, min_periods=1).mean()
    incub    = 65.2 * np.exp(-0.112 * np.clip(t_mean24, 2, 32))
    out['anth_carisse_incubation_rate'] = 1.0 / np.maximum(incub, 1.0)

    # F2: fereastra infectie (Carisse 2020)
    cond_a = (lwd_24h >= 4.0) & (t >= 10)
    cond_b = (lwd_24h >= 6.0) & (t >= 15) & (t <= 30)
    out['anth_infection_window_binary'] = (cond_a | cond_b).astype(float)

    # F3 [FIX 5]: susceptibilitate corecta bazata pe DD baza 6°C
    out['anth_leaf_age_susceptibility'] = compute_anthracnose_susceptibility(df)

    return out


# ============================================================
# LAYER C — OUTPUTURI MODELE MECANICISTE (7 features)
# ============================================================

def layer_c_mechanistic(df: pd.DataFrame,
                        layer_a: pd.DataFrame,
                        layer_b: pd.DataFrame) -> pd.DataFrame:
    out = pd.DataFrame(index=df.index)

    # 1. Gubler-Thomas PMI (UC Davis PM)
    # +20 per zi cu >6h in 21-30C, -20 cand T>=38C pentru 2h
    daily_fav = (layer_b['pm_favorable_band_hours_6h'] >= 6.0).astype(float)
    daily_let = (layer_b['pm_lethal_exposure_hours'] >= 2.0).astype(float)
    # Scor 14 zile, normalizat la [0,1]
    score_14d = (daily_fav * 20 - daily_let * 20).rolling(
        14 * READINGS_PER_DAY, min_periods=1).mean()
    out['mech_gubler_thomas_pm'] = np.clip((score_14d + 20) / 40.0, 0.0, 1.0)

    # 2. Analytis-Beta Phomopsis
    out['mech_analytis_beta_phom'] = layer_b['phom_analytis_beta_output']

    # 3. UCSC DM primar — [FIX 4] oospori activi de la DOY>=75
    doy = df['day_of_year'] if 'day_of_year' in df.columns else pd.Series(150, index=df.index)
    # Fara limita superioara per Rossi 2008
    oospore_mat  = (doy >= 75).astype(float)
    prim         = layer_b['dm_rule_3_10_timer'] * oospore_mat
    # [FIX 3] Modulat de supravietuirea sporangilor
    sec          = layer_b['dm_sporulation_hours'] / 24.0 * layer_b['dm_sporangia_survival']
    out['mech_ucsc_dm_primary'] = np.clip(0.6 * prim + 0.4 * sec, 0.0, 1.0)

    # 4. UCSC Botrytis SEV1 acumulat (Gonzalez 2015)
    if 'growth_stage' in df.columns:
        in_window = (df['growth_stage'] == 1).astype(float)
    else:
        in_window = 1.0
    sev1 = layer_b['bot_sev1_conditions'] * in_window
    out['mech_ucsc_botrytis_sev1'] = sev1.rolling(
        14 * READINGS_PER_DAY, min_periods=1).sum() / (14 * READINGS_PER_DAY)

    # 5. Spotts Black Rot
    out['mech_spotts_lookup_br'] = layer_b['br_spotts_lookup_score'].clip(0, 1)

    # 6. Carisse Antracnoza
    out['mech_carisse_anth_risk'] = np.clip(
        0.5 * layer_b['anth_carisse_incubation_rate'] +
        0.5 * layer_b['anth_infection_window_binary'], 0.0, 1.0)

    # 7. [NOU] Black Rot primar (ascospori din mumii)
    out['mech_br_primary_risk'] = layer_b['br_primary_ascospore_risk']

    return out


# ============================================================
# LAYER D — CONTEXT / MEMORIE (12 features)
# ============================================================

def layer_d_context(df: pd.DataFrame) -> pd.DataFrame:
    out = pd.DataFrame(index=df.index)
    t   = df['temperature']

    # Degree-days cu 3 baze
    dd0  = np.clip(t,        0, None) / (24.0 * READINGS_PER_HOUR)
    dd8  = np.clip(t - 8.0,  0, None) / (24.0 * READINGS_PER_HOUR)
    dd10 = np.clip(t - 10.0, 0, None) / (24.0 * READINGS_PER_HOUR)

    if 'day_of_year' in df.columns:
        reset = (df['day_of_year'] == 1).astype(int).cumsum()
        out['degree_days_base_0']  = dd0.groupby(reset).cumsum()
        out['degree_days_base_8']  = dd8.groupby(reset).cumsum()
        out['degree_days_base_10'] = dd10.groupby(reset).cumsum()
    else:
        out['degree_days_base_0']  = dd0.cumsum()
        out['degree_days_base_8']  = dd8.cumsum()
        out['degree_days_base_10'] = dd10.cumsum()

    # BBCH continuu din DD baza 10
    d = out['degree_days_base_10'].values
    bbch = np.where(d < 20,   0,
           np.where(d < 80,   9  + (d - 20)   * 6  / 60,
           np.where(d < 250,  15 + (d - 80)   * 38 / 170,
           np.where(d < 450,  53 + (d - 250)  * 12 / 200,
           np.where(d < 650,  65 + (d - 450)  * 8  / 200,
           np.where(d < 900,  73 + (d - 650)  * 6  / 250,
           np.where(d < 1200, 79 + (d - 900)  * 4  / 300,
           np.where(d < 1600, 83 + (d - 1200) * 6  / 400,
                    89))))))))
    out['bbch_growth_stage'] = np.clip(bbch, 0, 99)

    # Growth stage categorial
    out['growth_stage'] = df['growth_stage'] if 'growth_stage' in df.columns else 0

    # [NOU] Modelul Schultz 1992 — nr. frunze estimate
    out['schultz_leaf_stage'] = compute_schultz_leaf_stage(df)

    # Simptome sezon precedent (Volpi 2021)
    if 'node_id' in df.columns:
        rng      = np.random.default_rng(42)
        nodes    = df['node_id'].unique()
        prev_dm  = {n: float(rng.beta(2, 5)) for n in nodes}
        prev_pm  = {n: float(rng.beta(2, 5)) for n in nodes}
        prev_bot = {n: float(rng.beta(2, 5)) for n in nodes}
        out['prev_season_symptoms_dm']  = df['node_id'].map(prev_dm).values
        out['prev_season_symptoms_pm']  = df['node_id'].map(prev_pm).values
        out['prev_season_symptoms_bot'] = df['node_id'].map(prev_bot).values
    else:
        out['prev_season_symptoms_dm']  = 0.3
        out['prev_season_symptoms_pm']  = 0.3
        out['prev_season_symptoms_bot'] = 0.3

    # Onset date proxy (Chen 2020)
    if 'node_id' in df.columns and 'day_of_year' in df.columns:
        rng2    = np.random.default_rng(123)
        nodes   = df['node_id'].unique()
        onset   = {n: int(rng2.integers(120, 180)) for n in nodes}
        onset_s = df['node_id'].map(onset).values
        days_s  = np.maximum(0, df['day_of_year'].values - onset_s)
        out['onset_date_dm_logged'] = np.clip(days_s / 100.0, 0.0, 1.0)
    else:
        out['onset_date_dm_logged'] = 0.0

    return out


# ============================================================
# PIPELINE PRINCIPAL
# ============================================================

def engineer_features(df: pd.DataFrame) -> pd.DataFrame:
    all_features = []
    nodes        = df['node_id'].unique()
    print(f"  Procesez {len(nodes)} noduri...")

    for node_id in nodes:
        node_df = df[df['node_id'] == node_id].copy()
        node_df = node_df.sort_values('timestamp').reset_index(drop=True)

        if 'day_of_year' not in node_df.columns:
            unit = 'ms' if node_df['timestamp'].iloc[0] > 1e11 else 's'
            ts = pd.to_datetime(node_df['timestamp'], unit=unit)
            node_df['day_of_year'] = ts.dt.dayofyear
        if 'hour' not in node_df.columns:
            unit = 'ms' if node_df['timestamp'].iloc[0] > 1e11 else 's'
            ts = pd.to_datetime(node_df['timestamp'], unit=unit)
            node_df['hour'] = ts.dt.hour
        if 'growth_stage' not in node_df.columns:
            doy = node_df['day_of_year']
            node_df['growth_stage'] = np.select(
                [doy < 90, doy < 150, doy < 240, doy <= 310],
                [0, 1, 2, 3], default=0
            )

        print(f"    {node_id}: {len(node_df):,} citiri", end=" -> ")

        la = layer_a_base_meteo(node_df)
        lb = pd.concat([
            layer_b_downy_mildew(node_df),
            layer_b_powdery_mildew(node_df),
            layer_b_botrytis(node_df),
            layer_b_black_rot(node_df),
            layer_b_phomopsis(node_df),
            layer_b_anthracnose(node_df),
        ], axis=1)
        lc = layer_c_mechanistic(node_df, la, lb)
        ld = layer_d_context(node_df)

        feat = pd.concat([
            node_df[['node_id', 'timestamp', 'datetime']].reset_index(drop=True),
            la.reset_index(drop=True),
            lb.reset_index(drop=True),
            lc.reset_index(drop=True),
            ld.reset_index(drop=True),
        ], axis=1).fillna(0.0)

        n_feat = len(feat.columns) - 2
        print(f"{n_feat} features")
        all_features.append(feat)

    return pd.concat(all_features, ignore_index=True)


# ============================================================
# MAIN
# ============================================================

def main():
    print("=" * 55)
    print("  VineGuard — Feature Engineering v3")
    print("=" * 55)

    if not os.path.exists(INPUT_PATH):
        print(f"\n  EROARE: {INPUT_PATH} nu exista.")
        print("  Ruleaza mai intai: py synthetic_data_generator.py")
        return None

    print(f"\n  Incarc date din: {INPUT_PATH}")
    df = pd.read_csv(INPUT_PATH)
    print(f"  Citiri brute : {len(df):,}")
    print(f"  Noduri       : {df['node_id'].nunique()}")

    print("\n  Calculez features...")
    features_df = engineer_features(df)

    before = len(features_df)
    features_df = features_df.dropna()
    after  = len(features_df)
    if before > after:
        print(f"\n  Randuri eliminate (NaN): {before - after:,}")

    os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)
    features_df.to_csv(OUTPUT_PATH, index=False)

    feat_cols = [c for c in features_df.columns
                 if c not in ['node_id', 'timestamp']]

    print("\n" + "=" * 55)
    print(f"  Features calculate : {len(feat_cols)}")
    print(f"  Samples totale     : {len(features_df):,}")
    print(f"  Fisier salvat      : {OUTPUT_PATH}")
    print("=" * 55)

    # Sumar pe grupuri
    groups = {
        "Layer A - Meteo baza":    [c for c in feat_cols if not any(
            c.startswith(p) for p in ['dm_','pm_','bot_','br_','phom_','anth_',
                                       'mech_','degree_','bbch_','growth_',
                                       'prev_','onset_','schultz_'])],
        "Layer B - Per boala":     [c for c in feat_cols if any(
            c.startswith(p) for p in ['dm_','pm_','bot_','br_','phom_','anth_'])],
        "Layer C - Mecaniciste":   [c for c in feat_cols if c.startswith('mech_')],
        "Layer D - Context":       [c for c in feat_cols if any(
            c.startswith(p) for p in ['degree_','bbch_','growth_','prev_',
                                       'onset_','schultz_'])],
    }

    print()
    for group, cols in groups.items():
        print(f"  {group} ({len(cols)} features):")
        for c in cols:
            print(f"    - {c}")
        print()

    print("  Corecții v3 aplicate:")
    print("    [FIX 1] Botrytis Mf factor continuu (ore umede/24)")
    print("    [FIX 2] MYGR: 3.78*Teq^0.9*(1-Teq)^-0.475*Mf")
    print("           SPOR: 3.7*Teq^0.9*(1-Teq)^-10.49*RH_factor")
    print("    [FIX 3] DM sporangi: supravietuire solar-dependenta")
    print("    [FIX 4] DM oospori: activi de la DOY>=75 fara limita")
    print("    [FIX 5] Antracnoza: susceptibilitate DD baza 6C")
    print("    [FIX 6] Black Rot primar: ascospori din mumii")
    print("    [NOU]  Schultz 1992: rata aparitie frunze")
    print("\n  Done! Ruleaza label_generator.py urmator.\n")

    return features_df


if __name__ == "__main__":
    main()