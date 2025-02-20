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
      col("location").getField("lat").alias("latitude"),
      col("location").getField("lon").alias("longitude"),
      col("weather").getItem(0).getField("main").alias("weather_condition"), // Si 'weather' est un tableau
      col("main").getField("temp").alias("temperature"),
      col("main").getField("feels_like").alias("feels_like"),
      col("main").getField("pressure").alias("pressure"),
      col("main").getField("humidity").alias("humidity"),
      col("wind").getField("speed").alias("wind_speed"),
      col("wind").getField("deg").alias("wind_direction")
    )

        // Convertir le timestamp en date (sans l'heure)
    val dfWithDate = parsedDF.withColumn("date", to_date(col("timestamp")))

    // Grouper par jour et calculer les moyennes
    val aggregatedDF = dfWithDate.groupBy("date")
      .agg(
        avg("temperature").alias("avg_temperature"),
        avg("pressure").alias("avg_pressure"),
        avg("humidity").alias("avg_humidity"),
        avg("wind_speed").alias("avg_wind_speed")
      )
      .orderBy("date") // Optionnel : trier par date

    // Afficher le résultat
    aggregatedDF.show()

    // Sauvegarde des résultats au format CSV
    parsedDF.write
      .option("header", "true")
      .csv("/home/ubuntu/static_weather_data.csv")

    // Arrêt de Spark
    spark.stop()
  }
}
