#!/bin/bash

# Exécuter spark-shell avec les packages nécessaires et charger le script
spark-submit --class AirPollutionJob --packages org.elasticsearch:elasticsearch-spark-20_2.11:8.16.3 job_spark.scala
