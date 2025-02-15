import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object WeatherDataJob {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("WeatherDataJob")
      .master("local[*]") // Utilisation du mode local
      .config("spark.es.nodes", "elasticsearch") // Adresse du cluster Elasticsearch
      .config("spark.es.port", "9200") // Port Elasticsearch
      .config("spark.es.nodes.wan.only", "true") // Important si Elasticsearch est hors cluster Spark
      .getOrCreate()

    // Chargement des données depuis Elasticsearch
    val weatherDataDf = spark.read
      .format("org.elasticsearch.spark.sql")
      .option("es.resource", "weather_data") // Correcte pour les versions modernes d'Elasticsearch
      .option("es.read.field.as.array.include", "weather") // Si weather est une liste
      .load()

    // Sélection des colonnes pertinentes
    val parsedDF = weatherDataDf.select(
      col("@timestamp").alias("timestamp"),
      col("coord.lat").alias("latitude"),
      col("coord.lon").alias("longitude"),
      col("weather.main").alias("weather_condition"),
      col("main.temp").alias("temperature"),
      col("main.feels_like").alias("feels_like"),
      col("main.pressure").alias("pressure"),
      col("main.humidity").alias("humidity"),
      col("wind.speed").alias("wind_speed"),
      col("wind.deg").alias("wind_direction")
    )

    // Affichage des résultats
    parsedDF.show()

    // Sauvegarde des résultats au format CSV
    parsedDF.write
      .option("header", "true")
      .csv("/home/ubuntu/weather_data.csv")

    // Arrêt de Spark
    spark.stop()
  }
}
