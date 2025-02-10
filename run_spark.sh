#!/bin/bash

# Exécuter spark-shell avec les packages nécessaires et charger le script
spark-submit --class AirPollutionJob --master local[*] --packages org.elasticsearch:elasticsearch-spark-20_2.11:8.16.3 job_spark.jar