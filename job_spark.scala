import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object AirPollutionJob {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("AirPollutionJob")
      .master("local[*]") // ou supprime pour utiliser le cluster
      .getOrCreate()

    val airPollutionSchema = new StructType()
      .add("co", FloatType)
      .add("no", FloatType)
      .add("no2", FloatType)
      .add("o3", FloatType)
      .add("so2", FloatType)
      .add("pm2_5", FloatType)
      .add("pm10", FloatType)
      .add("nh3", FloatType)

    // Lecture des données depuis Elasticsearch
    val airPollutionDf = spark.read
      .format("org.elasticsearch.spark.sql")
      .option("es.nodes", "elasticsearch:9200")
      .option("es.resource", "air_pollution_data")
      .load()

    // Transformation des données
    val parsedDF = airPollutionDf
      .withColumn("pollution_data", from_json(col("message"), airPollutionSchema))
      .select(
        col("@timestamp").alias("timestamp"),
        col("pollution_data.*")
      )

    // Affichage des résultats
    parsedDF.show()

    // Enregistrement des résultats dans un fichier CSV dans /home/ubuntu
    parsedDF.write
      .option("header", "true")
      .csv("/home/ubuntu/air_pollution_data.csv")

    spark.stop()
  }
}
