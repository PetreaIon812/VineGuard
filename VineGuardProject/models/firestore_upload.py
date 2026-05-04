"""
VineGuard Firestore Upload
===========================
Preia predicțiile din predict.py și le urcă în Firestore
în formatul pe care îl citește aplicația Android.

Structura Firestore generată:
  predictions/
    {node_id}/
      latest/          ← ultima predicție (pentru dashboard principal)
        - timestamp
        - downy_mildew:  { probability, risk_level }
        - powdery_mildew: { probability, risk_level }
        - botrytis:      { probability, risk_level }
        - black_rot:     { probability, risk_level }
        - phomopsis:     { probability, risk_level }
        - anthracnose:   { probability, risk_level }
      history/
        {timestamp}/   ← istoric complet (pentru grafice 14 zile)
          (același format ca latest)

Niveluri de risc (compatibile cu app-ul Android existent):
  CRITIC  >= 0.70
  RIDICAT >= 0.50
  MEDIU   >= 0.30
  SCĂZUT  <  0.30

Utilizare:
  # Upload predicție curentă pentru toate nodurile:
  python firestore_upload.py --predictions data/predictions.csv \\
                              --key firebase_key.json

  # Upload un singur nod (pentru testare):
  python firestore_upload.py --predictions data/predictions.csv \\
                              --key firebase_key.json \\
                              --node node_01

  # Mod dry-run (nu scrie în Firestore, doar afișează ce ar urca):
  python firestore_upload.py --predictions data/predictions.csv \\
                              --key firebase_key.json \\
                              --dry-run
"""

import argparse
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

import pandas as pd
import numpy as np


# =============================================================================
# RISK LEVEL MAPPING
# =============================================================================

RISK_THRESHOLDS = {
    'CRITIC':   0.70,
    'RIDICAT':  0.50,
    'MEDIU':    0.30,
    'SCAZUT':   0.00,
}

DISEASE_LABELS_RO = {
    'downy_mildew':   'Mană',
    'powdery_mildew': 'Făinare',
    'botrytis':       'Putregai cenușiu',
    'black_rot':      'Putregai negru',
    'phomopsis':      'Excorioza',
    'anthracnose':    'Antracnoză',
}

DISEASE_TARGETS = [
    'downy_mildew', 'powdery_mildew', 'botrytis',
    'black_rot', 'phomopsis', 'anthracnose'
]


def get_risk_level(probability: float) -> str:
    """Convertește probabilitatea continuă în nivel de risc categorial."""
    if probability >= RISK_THRESHOLDS['CRITIC']:
        return 'CRITIC'
    elif probability >= RISK_THRESHOLDS['RIDICAT']:
        return 'RIDICAT'
    elif probability >= RISK_THRESHOLDS['MEDIU']:
        return 'MEDIU'
    else:
        return 'SCAZUT'


def build_prediction_doc(row: pd.Series, timestamp_ms: int) -> dict:
    """Construiește documentul Firestore din un rând de predicții."""
    diseases = {}
    for disease in DISEASE_TARGETS:
        if disease in row.index:
            prob = float(np.clip(row[disease], 0.0, 1.0))
        else:
            prob = 0.0

        diseases[disease] = {
            'probability': round(prob, 4),
            'risk_level': get_risk_level(prob),
            'label_ro': DISEASE_LABELS_RO[disease],
        }

    # Calculează riscul global (max din toate bolile)
    max_prob = max(d['probability'] for d in diseases.values())

    return {
        'timestamp': timestamp_ms,
        'timestamp_iso': datetime.fromtimestamp(
            timestamp_ms / 1000, tz=timezone.utc).isoformat(),
        'node_id': str(row['node_id']) if 'node_id' in row.index else 'unknown',
        'predictions': diseases,
        'global_risk_level': get_risk_level(max_prob),
        'global_max_probability': round(max_prob, 4),
        'model_version': 'v2',
    }


# =============================================================================
# FIREBASE INIT
# =============================================================================

def init_firebase(key_path: str):
    """Inițializează conexiunea Firebase din service account key."""
    try:
        import firebase_admin
        from firebase_admin import credentials, firestore
    except ImportError:
        print('[ERROR] firebase-admin nu este instalat.')
        print('        Rulează: pip install firebase-admin')
        sys.exit(1)

    key_file = Path(key_path)
    if not key_file.exists():
        print(f'[ERROR] Fișierul de cheie nu există: {key_path}')
        print('        Mergi în Firebase Console → Project Settings →')
        print('        Service accounts → Generate new private key')
        sys.exit(1)

    # Inițializare (evită reinițializare dacă e deja activ)
    if not firebase_admin._apps:
        cred = credentials.Certificate(str(key_file))
        firebase_admin.initialize_app(cred)

    db = firestore.client()
    print(f'[firebase] Conectat la Firestore.')
    return db


# =============================================================================
# UPLOAD LOGIC
# =============================================================================

def upload_predictions(db, predictions_df: pd.DataFrame,
                       dry_run: bool = False,
                       node_filter: str = None) -> int:
    """Urcă predicțiile în Firestore.

    Structura:
      predictions/{node_id}/latest  ← suprascris mereu
      predictions/{node_id}/history/{timestamp}  ← append

    Returns: numărul de documente scrise.
    """
    from firebase_admin import firestore as fs

    written = 0
    timestamp_ms = int(datetime.now(timezone.utc).timestamp() * 1000)

    # Grupează per nod (ultimul rând per nod = cea mai recentă predicție)
    if 'node_id' in predictions_df.columns:
        grouped = predictions_df.groupby('node_id').last().reset_index()
    else:
        predictions_df['node_id'] = 'node_01'
        grouped = predictions_df

    if node_filter:
        grouped = grouped[grouped['node_id'] == node_filter]
        if len(grouped) == 0:
            print(f'[WARN] Nodul {node_filter} nu a fost găsit în predicții.')
            return 0

    for _, row in grouped.iterrows():
        node_id = str(row['node_id'])

        # Folosește timestamp-ul din date dacă există
        if 'timestamp' in row.index and pd.notna(row['timestamp']):
            ts = int(row['timestamp'])
        else:
            ts = timestamp_ms

        doc = build_prediction_doc(row, ts)

        if dry_run:
            print(f'\n[dry-run] predictions/{node_id}/latest:')
            print(json.dumps(doc, indent=2, ensure_ascii=False))
            written += 1
            continue

        # Scrie în Firestore
        node_ref = db.collection('predictions').document(node_id)

        # 1. Suprascrie "latest"
        node_ref.collection('latest').document('current').set(doc)

        # 2. Adaugă în "history" cu timestamp ca cheie
        history_key = str(ts)
        node_ref.collection('history').document(history_key).set(doc)

        print(f'[upload] {node_id}: '
              f'global_risk={doc["global_risk_level"]} '
              f'({doc["global_max_probability"]:.3f}) '
              f'→ Firestore OK')
        written += 1

    return written


# =============================================================================
# BATCH HISTORY UPLOAD (pentru datele sintetice existente)
# =============================================================================

def upload_history_batch(db, predictions_df: pd.DataFrame,
                         dry_run: bool = False,
                         sample_every_n: int = 96) -> int:
    """Urcă istoricul complet al predicțiilor în Firestore.

    Util pentru a popula graficele din app cu date sintetice
    înainte de a colecta date reale.

    sample_every_n: 96 = un rând la 24h (din 96 citiri la 15 min)
                    4 = un rând la 1h
    """
    written = 0

    # Subsample pentru a nu urca 410K documente
    df_sampled = predictions_df.iloc[::sample_every_n].copy()
    print(f'[history] Uploading {len(df_sampled)} documente istorice '
          f'(din {len(predictions_df)}, la fiecare {sample_every_n} rânduri)...')

    for _, row in df_sampled.iterrows():
        node_id = str(row['node_id']) if 'node_id' in row.index else 'node_01'
        ts = int(row['timestamp']) if 'timestamp' in row.index else 0
        doc = build_prediction_doc(row, ts)

        if dry_run:
            written += 1
            continue

        node_ref = db.collection('predictions').document(node_id)
        node_ref.collection('history').document(str(ts)).set(doc)
        written += 1

        if written % 50 == 0:
            print(f'  ... {written} documente urcate')

    return written


# =============================================================================
# ENTRY POINT
# =============================================================================

def main():
    parser = argparse.ArgumentParser(
        description='VineGuard — upload predicții în Firestore')
    parser.add_argument('--predictions', type=str, required=True,
                        help='CSV cu predicțiile (output predict.py sau labeled_data_v2.csv)')
    parser.add_argument('--key', type=str, default='firebase_key.json',
                        help='Calea spre service account key JSON (default: firebase_key.json)')
    parser.add_argument('--node', type=str, default=None,
                        help='Urcă doar un nod specific (ex: node_01)')
    parser.add_argument('--dry-run', action='store_true',
                        help='Afișează ce ar urca fără să scrie în Firestore')
    parser.add_argument('--history', action='store_true',
                        help='Urcă și istoricul complet (pentru populare inițială)')
    parser.add_argument('--history-sample', type=int, default=96,
                        help='Sample la fiecare N rânduri pentru history (default 96 = 1/zi)')
    args = parser.parse_args()

    # Încarcă predicțiile
    pred_path = Path(args.predictions)
    if not pred_path.exists():
        print(f'[ERROR] Fișierul de predicții nu există: {pred_path}')
        sys.exit(1)

    print(f'[main] Încarc predicțiile din {pred_path}...')
    df = pd.read_csv(pred_path)
    print(f'[main] {len(df):,} rânduri, '
          f'{df["node_id"].nunique() if "node_id" in df.columns else "?"} noduri')

    # Verifică că avem coloanele de boli
    missing = [d for d in DISEASE_TARGETS if d not in df.columns]
    if missing:
        print(f'[ERROR] Coloanele lipsă: {missing}')
        print(f'        Asigură-te că rulezi predict.py înainte de firestore_upload.py')
        sys.exit(1)

    # Inițializează Firebase
    if args.dry_run:
        print('[main] Mod DRY-RUN — nu se scrie în Firestore')
        db = None
    else:
        db = init_firebase(args.key)

    # Upload latest
    n = upload_predictions(db, df, dry_run=args.dry_run, node_filter=args.node)
    print(f'\n[main] Latest: {n} noduri procesate.')

    # Upload history (opțional)
    if args.history:
        nh = upload_history_batch(db, df, dry_run=args.dry_run,
                                  sample_every_n=args.history_sample)
        print(f'[main] History: {nh} documente istorice urcate.')

    print('\n[main] Done.')


if __name__ == '__main__':
    main()
