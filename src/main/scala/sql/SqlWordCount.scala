package sql

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object SqlWordCount {


  def main(args: Array[String]): Unit = {
    val session: SparkSession = SparkSession.builder().appName("SqlWordCount").master("local[2]").getOrCreate()
    val lines: Dataset[String] = session.read.textFile("E://tmp//hello.txt")
    import session.implicits._
    val words: Dataset[String] = lines.flatMap(_.split(" "))
    //注册视图
    words.createTempView("v_wc")
    //执行SQL（Transformation，lazy）
    val result: DataFrame = session.sql("SELECT value, COUNT(*) counts FROM v_wc GROUP BY value ORDER BY counts DESC")

    //执行Action
    result.show()
    session.stop()
  }
}
