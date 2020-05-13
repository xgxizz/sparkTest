package sql

import org.apache.parquet.format.IntType
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{DoubleType, IntegerType, LongType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

object SqlDemo02 {

  def main(args: Array[String]): Unit = {
    //提交的这个程序可以连接到Spark集群中
    val conf: SparkConf = new SparkConf().setAppName("SqlDemo01").setMaster("local[4]")
    //创建SparkSQL的连接（程序执行的入口）
    val sc = new SparkContext(conf)
    //sparkContext不能创建特殊的RDD（DataFrame）
    //将SparkContext包装进而增强
    val sqlContext = new SQLContext(sc)
    //创建特殊的RDD（DataFrame），就是有schema信息的RDD

    //先有一个普通的RDD，然后在关联上schema，进而转成DataFrame
    val lines: RDD[String] = sc.textFile("F://teacher.txt")
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
    //将RowRDD关联schema
    val bdf: DataFrame = sqlContext.createDataFrame(rowRDD, schema)
    //把DataFrame先注册临时表
    bdf.registerTempTable("t_boy")
    val result: DataFrame = sqlContext.sql("select * from t_boy order by fv desc, age asc")
    result.show();
    sc.stop()
  }
}

//case class Boy(id: Long, name: String, age: Int, fv: Double)
