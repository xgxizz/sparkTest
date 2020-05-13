package sql

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.types.{DoubleType, IntegerType, LongType, StringType, StructField, StructType}

object Spark2SqlDemo {

  def main(args: Array[String]): Unit = {
    val session: SparkSession = SparkSession.builder().appName("Spark2SqlDemo").master("local[2]").getOrCreate()

    val lines: RDD[String] = session.sparkContext.textFile("F://teacher.txt")
    //整理数据
    val rowRDD: RDD[Row] = lines.map(line => {
      val fields: Array[String] = line.split(",")
      val id = fields(0).toLong
      val name = fields(1)
      val age = fields(2).toInt
      val fv = fields(3).toDouble
      Row(id, name, age, fv)
    })

    val schema = StructType(List(
      StructField("id", LongType, true),
      StructField("name", StringType, true),
      StructField("age", IntegerType, true),
      StructField("fv", DoubleType, true)
    ))
    val frame: DataFrame = session.createDataFrame(rowRDD, schema)

    import session.implicits._
    val r: Dataset[Row] = frame.where($"fv" >  98).orderBy($"fv" desc, $"age" asc)
    r.show()
    session.stop()
  }
}
