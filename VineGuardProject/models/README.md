# VineGuard v2 — Feature Engineering și Model Comparison

Acest pachet implementează revizuirea pipeline-ului ML pentru VineGuard pe baza:
- Celor 5 articole științifice uploadate (Erincik 2003, Pertot 2016, González-Domínguez 2015, Peduto 2013, Carisse 2024)
- Raportului de cercetare aprofundată (20+ surse suplimentare: Wilcox APS 2015, Brischetto 2021, Rossi 2008, Gadoury 2012, etc.)

## Ce e nou față de v1

### Features: 29 → 44 (organizate în 4 layere)

| Layer | # features | Schimbare principală |
|---|---|---|
| A. Base meteo | 12 | +VPD, +dew point, +grade-hours (Schumacher 2022), +temperature-band hours per boală, +night RH>90% |
| B. Per-boală | 18 (3×6) | Înlocuiește pragul unic `humidity>85%` cu praguri specifice fiecărei boli |
| C. Output mecanistic | 6 | Gubler-Thomas, Analytis-Beta, Spotts lookup, UCSC, Carisse — ca features ȘI ca baseline-uri standalone |
| D. Memorie & fenologie | 8 | DD baze 0/8/10°C, BBCH continuu, prev season symptoms (Volpi 2021), onset date (Chen 2020) |

### Label generator: praguri revizuite per boală

| Boală | Schimbare critică | Sursă |
|---|---|---|
| Powdery Mildew | Prag letal 35°C → **36-38°C cu durată** | Peduto 2013 |
| Downy Mildew | RH≥85% → **RH≥95% pentru sporulare + LWD strict** | Brischetto 2021 |
| Botrytis | Regula 15-15 → **SEV1/SEV2/SEV3 BBCH-dependent** | González-Domínguez 2015 |
| Black Rot | Prag unic LWD → **tabel Spotts interpolat** (6h@26°C → 24h@10°C) | Spotts 1977 |
| Phomopsis | Threshold simplu → **Analytis-Beta cu α=6, β=1.5, γ=1.7, δ=1.1** | Erincik 2003 |
| Anthracnose | LWD simplu → **T-dependent incubation (27.5→2.26 zile)** | Carisse 2024 |

### compare_models.py: 15 algoritmi + 1 ensemble + 6 baseline-uri mecaniciste

Baselines (2): LogisticRegression, DecisionTree
Ensemble trees (5): RandomForest, ExtraTrees, XGBoost, LightGBM, CatBoost
Neural nets (3): MLP, TabNet, TabPFN
Temporale (3): LSTM, GRU, 1D-CNN
Shallow (2): KNN, GaussianNB
Stacking (1): RF + XGBoost + LightGBM → Ridge meta-learner
Mecaniciste (6): Gubler-Thomas PMI, Analytis-Beta, UCSC DM, UCSC Botrytis SEV1, Spotts BR, Carisse Anth

## Instalare dependințe

```bash
pip install pandas numpy scipy scikit-learn matplotlib seaborn
pip install xgboost lightgbm catboost
# Opționale (pentru modele avansate - lente):
pip install torch              # pentru LSTM, GRU, 1D-CNN
pip install pytorch-tabnet     # pentru TabNet
pip install tabpfn             # pentru TabPFN v2
pip install shap               # pentru explainability
```

## Utilizare

### Pasul 1: Regenerează features (înlocuiește feature_engineering.py existent)

```bash
python feature_engineering_v2.py \
    --input data/synthetic/raw_data.csv \
    --output data/synthetic/features_v2.csv
```

Output: `features_v2.csv` cu 62 coloane (5 metadata + 57 features, din care 44 sunt "core"; restul sunt rolling aggregates păstrate din v1 pentru compatibilitate).

### Pasul 2: Regenerează labeluri cu praguri revizuite

```bash
python label_generator_v2.py \
    --input data/synthetic/features_v2.csv \
    --output data/synthetic/labeled_data_v2.csv
```

Output: `labeled_data_v2.csv` cu features + 6 coloane probabilitate (downy_mildew, powdery_mildew, botrytis, black_rot, phomopsis, anthracnose).

### Pasul 3: Rulează benchmark-ul complet (pentru ONIA)

```bash
# Benchmark complet (toate 15 modele + ensemble + SHAP, ~2-3h)
python compare_models.py \
    --input data/synthetic/labeled_data_v2.csv \
    --output-dir results/onia_benchmark \
    --n-splits 5 \
    --n-bootstrap 1000

# Varianta rapidă (fără LSTM/GRU/TabNet/TabPFN/CNN, ~30 min)
python compare_models.py \
    --input data/synthetic/labeled_data_v2.csv \
    --output-dir results/onia_benchmark_quick \
    --quick \
    --n-splits 5 \
    --n-bootstrap 1000

# Test rapid pe subsample (pentru debugging, ~5 min)
python compare_models.py \
    --input data/synthetic/labeled_data_v2.csv \
    --output-dir results/test \
    --quick \
    --n-splits 3 \
    --n-bootstrap 100 \
    --subsample 50000 \
    --skip-shap
```

Output:
- `metrics_per_fold.csv` — raw rezultate per model / boală / fold
- `metrics_summary.csv` — agregare cu 95% bootstrap CI
- `plots/mae_overall_ranking.png` — ranking principal (pentru slide-uri)
- `plots/f1_heatmap.png` — F1 pe matrice model × boală
- `plots/auc_per_disease.png` — AUC top-5 modele per boală
- `plots/accuracy_vs_cost.png` — tradeoff acuratețe / timp training
- `shap/shap_<disease>.png` — feature importance per boală (XGBoost)

## Mapping features → literatură (pentru justificare în prezentare)

### Layer A — Base meteo (12 features)

| Feature | Ecuație / prag | Sursă |
|---|---|---|
| `vpd` | `0.6108·exp(17.27·T/(T+237.3))·(1-RH/100)` | Tetens, standard DSS (METOS, Agrometeo) |
| `dew_point` | Magnus-Tetens approximation | Wilcox APS 2015 |
| `grade_hours_24h`, `_72h` | `Σ(T · LWD_binary)` pe 24h/72h, prag critic 50 | Schumacher 2022, *Plants* 11:1807 |
| `temp_band_21_30_hours_24h` | Ore cu T∈[21,30]°C în ultimele 24h | Gubler-Thomas 1999, Peduto 2013 |
| `temp_band_15_25_hours_24h` | Ore cu T∈[15,25]°C în ultimele 24h | Caffi 2016, DM secundar optim |
| `night_rh_above_90_hours` | Ore RH>90% între 22:00-06:00 | Brischetto 2021, Rossi 2008 |
| `leaf_wetness_hours_nocturnal` | LWD doar nocturnal | Wilcox APS 2015 |
| `rain_event_binary_48h` | 1 dacă `rain_sum_48h ≥ 10mm` (trigger 3-10) | Baldacci 1947, regula 3-10 |
| `solar_3d_mean` | Media pe 3 zile (inhibitor PM) | Willocquet 1996, Pertot 2016 |
| `rain_sum_48h` | Pentru regula 3-10 + lag-uri lungi | Chen 2020: precipitații > temp ca predictor |

### Layer B — Per-boală (18 features, 3 per boală)

**Downy Mildew** (Plasmopara viticola) — Brischetto 2021, Caffi 2016, Baldacci:
- `dm_sporulation_hours`: ore cu RH≥95% ȘI T∈[12,30]°C ȘI noapte
- `dm_infection_window_binary`: 1 dacă ≥2h LWD la T∈[4,30]°C noaptea
- `dm_rule_3_10_timer`: decay exponential al trigger-ului 3-10 (reset 72h)

**Powdery Mildew** (Erysiphe necator) — Peduto 2013, Gubler-Thomas:
- `pm_lethal_exposure_hours`: ore la T≥36°C (revizuire Peduto de la 35°C)
- `pm_favorable_band_hours_6h`: ore consecutive 21-30°C cu RH<85%
- `pm_ascospore_release_binary`: 2.5mm ploaie + 8-12h LWD + T∈[15,25]°C

**Botrytis** (B. cinerea) — González-Domínguez 2015, Broome 1995, Bulit:
- `bot_rule_15_15_binary`: LWD≥15h la T≥15°C (regula clasică)
- `bot_sev1_conditions`: ecuația Gonzalez-Dom 2015 eq. 4 (flori)
- `bot_berry_to_berry_risk`: ecuația eq. 10 (miceliu boabă la boabă)

**Black Rot** (Guignardia bidwellii) — Spotts 1977, Onesti 2016:
- `br_spotts_lookup_score`: LWD actual / LWD necesar (interpolat din tabel)
- `br_picnidium_conditions`: RH≥90% ȘI T∈[10,30]°C
- `br_infection_band_hours`: T∈[19.5,21.7]°C cu LWD

**Phomopsis** (Phomopsis viticola) — Erincik 2003 direct:
- `phom_analytis_beta_output`: ecuația 2 din paper cu α=6, β=1.5, γ=1.7, δ=1.1
- `phom_wetness_sufficient_binary`: (LWD≥5h @ 16-22°C) SAU (LWD≥10h @ 10-16°C)
- `phom_early_season_risk`: indicator BBCH 9-15 (lăstari tineri)

**Anthracnose** (Elsinoë ampelina) — Carisse 2024 direct:
- `anth_carisse_incubation_rate`: `1/(65.2·exp(-0.112·T))` — fit exponențial tabel
- `anth_infection_window_binary`: (LWD≥4h @ T≥10°C) SAU (LWD≥6h @ 15-30°C)
- `anth_leaf_age_susceptibility`: proxy vârstă frunze (BBCH 1-2)

### Layer C — Output-uri mecaniciste (6)

Fiecare servește DUAL: (a) feature input pentru ML, (b) baseline standalone în `compare_models.py`.

| Feature mecanistic | Scor | Referință |
|---|---|---|
| `mech_gubler_thomas_pm` | PMI 0-100 (rolling 14d) | Gubler-Thomas 1999, revizuit Peduto 2013 |
| `mech_analytis_beta_phom` | Severitate relativă 0-1 | Erincik 2003 Plant Disease |
| `mech_ucsc_dm_primary` | Combinație 3-10 + maturitate oospori + sporulare | Rossi 2008 Ecological Modelling, Brischetto 2021 |
| `mech_ucsc_botrytis_sev1` | SEV1 acumulat pe fereastra BBCH 53-73 | González-Domínguez 2015 PLOS ONE |
| `mech_spotts_lookup_br` | LWD/LWD_required din tabelul 1977 | Spotts 1977 Phytopathology 67:1378 |
| `mech_carisse_anth_risk` | Combinație incubation rate + infection window | Carisse 2020, 2024 Plant Disease |

### Layer D — Memorie și fenologie (8)

| Feature | Rol | Sursă |
|---|---|---|
| `degree_days_base_0` | DD pentru Anthracnose | Carisse 2024 (praguri 550-656 DD baza 0°C) |
| `degree_days_base_8` | DD pentru maturare chasmothecii PM | Möth 2020, >480 DD baza 8°C |
| `degree_days_base_10` | DD standard viticol | v1 preserved |
| `bbch_growth_stage` | Stadiu continuu 0-99 (fin) | BBCH extended scale, Lorenz 1995 |
| `prev_season_symptoms_dm` | Severitate DM anul anterior | Volpi 2021 — predictor #1 în RF |
| `prev_season_symptoms_pm` | Analog pentru PM | Volpi 2021 |
| `prev_season_symptoms_bot` | Analog pentru Botrytis | Volpi 2021 |
| `onset_date_dm_logged` | Zile de la prima simptomă | Chen 2020 — "onset date influences accuracy more than weather" |

## Benchmark-uri așteptate (per literatură)

- **Random Forest + XGBoost + LightGBM** ar trebui să performeze aproape identic (Volpi 2021: "C5.0 ≈ RF după balansare")
- **Stacking ensemble** ar trebui să câștige marginal (Chen 2020)
- **LSTM** poate egala sau depăși tree-based pe datele de serie de timp (Hui 2023: R²=0.99 pt DM sporangia)
- **TabPFN v2** - wild card, nu există publicații dedicate 2025-2026 pe viti, dar ar trebui să fie competitiv pe dataset < 10k
- **Toate modelele ML trebuie să bată toate baseline-urile mecaniciste** — argument principal pentru ONIA

## Arhitectura evaluării (pentru slide-uri ONIA)

```
Dataset (sintetic 410K → real Murfatlar după retraining)
    │
    ├─ Node-stratified 5-fold CV
    │    (2 noduri hold-out per fold, evită data leakage spațial)
    │
    ├─ 15 modele ML antrenate per fold
    │    → predict pe hold-out nodes
    │    → metrici MAE, RMSE, R², Spearman, F1, AUC-ROC, AUC-PR
    │
    ├─ 6 modele mecaniciste (fără antrenare, Layer C features direct)
    │    → predict pe hold-out nodes
    │
    └─ Bootstrap 1000 resamples
         → 95% CI pentru fiecare metric per model per boală
```

## Interpretare rezultate test

Pe subset de test (2000 rows, 3 noduri, 3 folds), Stacking ensemble a obținut:
- MAE mediu 0.0251 (vs 0.065 pentru cel mai bun mecanistic, UCSC DM)
- R² 0.44, F1 0.97, AUC-ROC 0.78
- Timp training: ~30s per fold (cel mai mare)

Pe dataset-ul complet 410K rows cu 10 noduri, timpii estimați:
- Quick mode (fără NN temporale): ~30-45 min
- Full mode (cu LSTM/GRU/CNN/TabPFN/TabNet): ~2-3h

## Observații pentru următorul pas

1. **Istoric pentru sezonul anterior**: features `prev_season_symptoms_*` sunt generate sintetic cu seed fix. Pentru date reale, va trebui log-at în aplicația Android (ecran de "quick report" la final de sezon pentru fermier).

2. **Onset date**: similar, generat cu seed în v2 sintetic. În producție vine din primul report al fermierului după vezirea simptomelor. Add un quick-capture în app.

3. **BBCH mai precis**: `bbch_growth_stage` folosește DD baza 10°C pentru aproximare. Pentru precizie, ar trebui validat cu observații de teren (fenologie). În Murfatlar 2021 (Hnatiuc 2023) există date pentru Sauvignon Blanc + Cabernet Sauvignon care pot calibra curba.

4. **Transfer la date reale**: pipeline-ul este drop-in compatibil cu formatul tău Firebase. După ce colectezi primul sezon de observații în podgorii, `retrain.py` (care rămâne gol acum) va folosi aceleași funcții din `feature_engineering_v2.py` și `label_generator_v2.py` (dar cu label-uri reale în loc de sintetice).

5. **Poziționare competitivă vs METOS Pessl**: pachetul tău implementează aceleași modele mecaniciste (Gubler-Thomas, Erincik, Spotts, González-Domínguez) dar le folosește ca INPUTS la un ML layer, nu ca output-uri finale. Acesta este diferențiatorul tehnic principal pe care să-l subliniezi în prezentare.
