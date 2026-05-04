from tuya_connector import TuyaOpenAPI

ACCESS_ID  = "7ev4gwtkdppqxyrkuajg"
ACCESS_KEY = "9938b2f530ac4b99aedb4e858e10ab22"
DEVICE_ID  = "bf05e842dd3c31ca45utfe"
API_ENDPOINT = "https://openapi.tuyaeu.com"  # Central sau Western Europe

openapi = TuyaOpenAPI(API_ENDPOINT, ACCESS_ID, ACCESS_KEY)
openapi.connect()

response = openapi.get(f"/v1.0/iot-03/devices/{DEVICE_ID}/status")
print(response)