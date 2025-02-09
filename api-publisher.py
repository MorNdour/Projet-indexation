import json
import time
import requests
from kafka import KafkaProducer
from datetime import datetime
import os
from dotenv import load_dotenv

load_dotenv()

# Configuration Kafka
KAFKA_BROKER = os.environ['KAFKA_BROKER']
# Adresse du broker Kafka
KAFKA_TOPIC = os.environ['KAFKA_TOPIC']

# URL de l'API de géolocalisation (remplacez par une API réelle)
BASE_URL = os.environ['BASE_URL']
API_KEY = os.environ['API_KEY']

params = {
    "lat": 48.8566,         # Latitude (e.g., Paris)
    "lon": 2.3522,
    "start": 1672531200,    # Start timestamp (UNIX format)
    "end": 1672617600,           # Longitude (e.g., Paris)
    "appid": API_KEY        # API key
}

def fetch_geolocation_data():
    """
    Fonction pour récupérer les données de l'API de géolocalisation.
    """
    try:
        # Make the API request
        response = requests.get(BASE_URL, params=params)
        # Check if the request was successful
        if response.status_code == 200:
            # Parse the JSON response
            pollution_data = response.json()
            process_data = process_pollution_data(pollution_data)
            return process_data
        else:
            print(f"Error: {response.status_code}, {response.text}")

    except KeyboardInterrupt:
        print("Streaming stopped by the user.")
    except Exception as e:
        print(f"An error occurred: {e}")

def process_pollution_data(data):
    """
    Processes and displays air pollution data in real-time.
    
    :param data: JSON response from the OpenWeather API.
    """
    for item in data.get("list", [])[:1]:
        dt = item["dt"]  # Timestamp
        aqi = item["main"]["aqi"]  # Air Quality Index
        components = item["components"]
        components["datetime"]=datetime.utcfromtimestamp(dt).strftime('%Y-%m-%d %H:%M:%S')
        return components
       


def publish_to_kafka(producer, topic, message):
    """
    Publie un message sur le topic Kafka.
    """
    try:
        producer.send(topic, value=message)
        print(f"Message publié sur Kafka: {message}")
    except Exception as e:
        print(f"Erreur lors de l'envoi à Kafka : {e}")

def main():
    #Initialisation du producteur Kafka
    producer = KafkaProducer(
        bootstrap_servers=KAFKA_BROKER,
        value_serializer=lambda v: json.dumps(v).encode('utf-8')  # Sérialise les données en JSON
    )
    
    print("Démarrage de la collecte et publication des données de géolocalisation...")
    
    try:
        while True:
            # Récupération des données de l'API
            geolocation_data = fetch_geolocation_data()
            print(geolocation_data)
            if geolocation_data:
                # Publication des données sur Kafka
                publish_to_kafka(producer, KAFKA_TOPIC, geolocation_data)
            
            #Pause entre deux appels API (ajustez l'intervalle selon vos besoins)
            time.sleep(5)
    
    except KeyboardInterrupt:
        print("\nArrêt de la publication...")
    finally:
        producer.close()

if __name__ == "__main__":
    main()
