1)Titlu proiectului - VineGuard

2)Problema:
Sănătatea viței-de-vie este constant amenințată de factori de mediu care favorizează apariția unor boli devastatoare,
precum făinarea, mana sau putregaiul cenușiu. În absența unei monitorizări constante, detectarea acestor afecțiuni are
loc adesea prea târziu, ducând la pierderi masive de productivitate și la necesitatea utilizării excesive a tratamentelor
chimice. Fermierii au nevoie de o metodă precisă și accesibilă pentru a trece de la o gestionare reactivă la una preventivă
a podgoriilor.

3)Sursele Seturilor de Date
Date Meteo Reale (Pipeline XGBoost)
Datele meteo au fost luat din AgriData fiind date meteo luat din vita de vie din Franta(in perioada 2024-2026) si Italia(2022-2025)
Sursa: Zenodo DOI: 10.5281/zenodo.14989522
(Proiect Horizon Europe AgriDataValue, Grant 101086461)
Senzori incluși: Temperatură aer, Umiditate relativă, Precipitații,
Leaf Wetness, Radiație solară, Viteză vânt, Presiune atmosferică,
Temperatură sol, Umiditate sol, Evapotranspirație (pilot_15)

4)Instructiuni de rulare
Cerințe de Sistem:
Python 3.14
Android Studio Hedgehog+ (pentru aplicație)
Pasii de rulare a modellului ML(de rulat in terminal proiectului):
# 1. Procesare date reale (necesita pilot_14_0.csv si pilot_15_0.csv in data/real/)
py data_processor_real.py

# 2. Feature engineering (66 features biologice)
py feature_engineering.py

# 3. Generare labeluri biologice
py label_generator.py

# 4. Antrenare model XGBoost
py train.py

# 5. Predicție demo
py predict.py

# 7. Comparație 9 modele ML (optional, ~20 min)
py compare_models.py

Aplicatie Android:
Este un fisier apk al aplicatiei in: apk/VineGuardApp.apk
Versiune Librarii:
Python    3.14
pandas    2.2.x
numpy    2.x
scikit-learn 1.5.x
xgboost    2.1.x
lightgbm    4.6.0
matplotlib    3.9.x
seaborn    0.13.x
joblib    1.4.x
Aplicatie:
compileSdk    36
targetSdk    35
minSdk    26
Kotlin    2.0.x
Jetpack Compose BOM    2025.x
Firebase BOM    33.12.0

Rezultate(sunt si in folderul VineGuardProject/docs/):
Model XGBoost (Predicție Boli din Date Meteo)
Date reale din vii europene (2 noduri, 51 195 citiri):
Metrică            Valoare
MAE medie(6 boli)  0.0228
R² medie           0.9347
Split              80% train/20% val

Comparație 5 modele ML:
Loc     Model              MAE     R²       Timp
1.      XGBoost            0.0200  0.9543   20s
2.      LightGBM           0.0200  0.9543   16s
3.      Gradient Boosting  0.0200  0.9545   579s
4.      MLP Neural Network 0.0202  0.9536   708s
5.      LASSO              0.0333  0.8792   5s


Cod sursă: MIT License
Date AgriDataValue: CC-BY 4.0
(Citare obligatorie: AgriDataValue Consortium, Zenodo 10.5281/zenodo.14989522)
