import requests
import json
import time
from kafka import KafkaProducer
import os
from dotenv import load_dotenv

load_dotenv()

# Remplacez par votre clé API
API_KEY = os.environ['WEATHER_DATA_API_KEY']
# Configuration Kafka
KAFKA_BROKER = os.environ['KAFKA_BROKER']
# Adresse du broker Kafka
KAFKA_TOPIC = os.environ['KAFKA_TOPIC']
CITY = "Paris"
URL = f"http://api.openweathermap.org/data/2.5/weather?q={CITY}&appid={API_KEY}"

#Configuration du producteur Kafka
producer = KafkaProducer(
    bootstrap_servers=KAFKA_BROKER,
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)


def fetch_weather_data():
    response = requests.get(URL)
    data = response.json()
    return data


if __name__ == "__main__":
    while True:
        weather_data = fetch_weather_data()
        print(weather_data)
        # Envoi des données au topic "weather_topic"
        producer.send(KAFKA_TOPIC, weather_data)
        producer.flush()

        # Attendre 60 secondes avant la prochaine requête (exemple)
        time.sleep(60)
