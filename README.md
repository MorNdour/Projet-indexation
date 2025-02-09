## Guide de configuration des conteneurs ELK, Kafka et Spark
Ce document explique les étapes pour configurer et lancer un environnement composé 
de conteneurs Docker exécutant les services ELK (Elasticsearch, Logstash, Kibana), Kafka, Zookeeper et Spark.

### Étape 1 : Création des conteneurs ELK et Kafka
Nous utilisons un fichier docker-compose.yml pour lancer un groupe de conteneurs, chacun exécutant un service spécifique. 
Les services déployés sont les suivants :

1. Logstash : Un conteneur pour exécuter une instance de Logstash.
2. Elasticsearch : Un conteneur pour exécuter une instance d'Elasticsearch.
3. Kibana : Un conteneur pour exécuter une instance de Kibana.
4. Kafka : Un conteneur pour exécuter une instance de Kafka.
5. Zookeeper : Un conteneur pour exécuter une instance de Zookeeper (nécessaire pour Kafka).

Pour démarrer ces services, exécutez la commande suivante :
  docker-compose up -d

### Etape 2: Création des fichiers Python pour la production et la publication de données sur Kafka
Après avoir testé nos différents services, nous avons créé les fichiers suivants :
- topic-creation.py : Ce script permet de se connecter au broker Kafka et de créer un topic.
- api-publisher.py : Ce script récupère des données depuis une API et les publie dans le topic précédemment créé.
- .env : Fichier contenant les paramètres de connexion, notamment :
  KAFKA_BROKER = " "
  KAFKA_TOPIC = " "
  BASE_URL = " "
  API_KEY = " "

### Étape 3 : Démarrage d'un conteneur pour une instance Spark

- Télécharger l'image de base de spark

  docker pull ubuntu
- création du conteneur spark: Créez un conteneur à partir de l'image Ubuntu et connectez-le au réseau du groupe de conteneurs pour permettre la communication
  
    docker run -itd --network=projet_default -p 8080:8080 --name spark --hostname spark ubuntu

- connection au conteneur spark en mode bash

  docker exec -it spark /bin/bash

- Installation de Java

    apt update
    
    apt -y upgrade
    
    apt install openjdk-8-jdk

- Installation de  Scala

    apt install scala

    apt install curl

    curl -O https://archive.apache.org/dist/spark/spark-2.4.5/spark-2.4.5-bin-hadoop2.7.tgz
    
    tar xvf spark-2.4.5-bin-hadoop2.7.tgz
    
    mv spark-2.4.5-bin-hadoop2.7 /opt/spark
   
    rm spark-2.4.5-bin-hadoop2.7.tgz

-  Mise en place de l'environnement Spark

    vim ~/.bashrc

    export SPARK_HOME=/opt/spark

    export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin

    source ~/.bashrc

### Étape 4 : Création et exécution d'un job Spark
Après avoir installé et configuré notre cluster Spark, nous avons créé les fichiers suivants :

- Job_spark.scala : Ce fichier permet de se connecter à un index Elasticsearch, de récupérer les données sous forme de DataFrame Spark et d'y appliquer des transformations.
- run_spark.sh : Ce script exécute le job Spark en lançant le shell sur notre cluster Spark.

Nous avons ensuite copié ces deux fichiers dans un répertoire du conteneur Spark à l'aide des commandes suivantes :

  docker cp  run_spark.sh spark:/home/ubuntu
  
  docker cp job_spark.scala  spark:/home/ubuntu

Enfin, nous avons donné les permissions d'exécution au script et l'avons lancé :
  
  chmod +x run_spark.sh
  
  ./run_spark.sh
