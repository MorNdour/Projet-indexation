from kafka.admin import KafkaAdminClient, NewTopic
import os
from dotenv import load_dotenv

load_dotenv()

# Configuration Kafka
KAFKA_BROKER = os.environ['KAFKA_BROKER']
# Adresse du broker Kafka
KAFKA_TOPIC = os.environ['KAFKA_TOPIC']

# Kafka admin setup
admin = KafkaAdminClient(
    bootstrap_servers=KAFKA_BROKER ,
)
topics_list = admin.list_topics()
# Admin logic

if KAFKA_TOPIC not in topics_list:
  topics = [NewTopic(name="air_pollution_data", num_partitions=2, 
replication_factor=1)]
  admin.create_topics(new_topics=topics, validate_only=False)
  print("air_pollution_data topic is successful created")

else:
       print(topics_list)
