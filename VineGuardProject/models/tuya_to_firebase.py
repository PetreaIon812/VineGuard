from tuya_connector import TuyaOpenAPI
import firebase_admin
from firebase_admin import credentials, db
import time

ACCESS_ID  = "7ev4gwtkdppqxyrkuajg"
ACCESS_KEY = "9938b2f530ac4b99aedb4e858e10ab22"
DEVICE_ID  = "bf05e842dd3c31ca45utfe"
API_ENDPOINT = "https://openapi.tuyaeu.com"  # Central sau Western Europe

# --- Firebase config ---
cred = credentials.Certificate("firebase_key.json")
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://vineguard-f8483-default-rtdb.europe-west1.firebasedatabase.app/'
})

# --- Conectare Tuya ---
openapi = TuyaOpenAPI(API_ENDPOINT, ACCESS_ID, ACCESS_KEY)
openapi.connect()

def parse_weather(result):
    """Convertește valorile Tuya (x10) în valori reale"""
    raw = {dp["code"]: dp["value"] for dp in result}
    return {
        "temp_c":        raw.get("temp_current_external", 0) / 10,
        "humidity_pct":  raw.get("humidity_outdoor", 0),
        "pressure_hpa":  raw.get("atmospheric_presstre", raw.get("atmospheric_pressture", 0)),
        "wind_avg_ms":   raw.get("windspeed_avg", 0) / 10,
        "wind_gust_ms":  raw.get("windspeed_gust", 0) / 10,
        "rain_1h_mm":    raw.get("rain_1h", 0) / 10,
        "rain_24h_mm":   raw.get("rain_24h", 0) / 10,
        "uv_index":      raw.get("uv_index", 0),
        "dew_point_c":   raw.get("dew_point_temp", 0) / 10,
        "feels_like_c":  raw.get("feellike_temp", 0) / 10,
        "timestamp":     int(time.time() * 1000)
    }

def fetch_and_store():
    response = openapi.get(f"/v1.0/iot-03/devices/{DEVICE_ID}/status")
    if not response.get("success"):
        print("Eroare Tuya:", response)
        return

    data = parse_weather(response["result"])
    print(f"[{time.strftime('%H:%M:%S')}] {data['temp_c']}°C | {data['humidity_pct']}% | {data['pressure_hpa']}hPa | Rain 1h: {data['rain_1h_mm']}mm")

    ref = db.reference("weather_station/readings")
    ref.push(data)

# --- Loop la fiecare 10 minute ---
INTERVAL = 600

while True:
    try:
        fetch_and_store()
    except Exception as e:
        print("Eroare:", e)
    time.sleep(INTERVAL)