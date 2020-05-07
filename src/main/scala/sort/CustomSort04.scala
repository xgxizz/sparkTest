package sort

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object CustomSort04 {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("CustomSort04").setMaster("local[*]")

    val sc = new SparkContext(conf)

    //排序规则：首先按照颜值的降序，如果颜值相等，再按照年龄的升序
    val users= Array("laoduan 30 99", "laozhao 29 9999", "laozhang 28 98", "laoyang 28 99")
    val lines: RDD[String] = sc.parallelize(users)
    val userRDD: RDD[(String, Int, Int)] = lines.map(line => {
      val fields: Array[String] = line.split(" ")
      val name: String = fields(0)
      val age: Int = fields(1).toInt
      val fv: Int = fields(2).toInt //faceValue 颜值
      (name, age, fv)
    })
    import SortRules.orderingXianRou
    val r: Array[(String, Int, Int)] = userRDD.sortBy(tp=>XianRou(tp._2, tp._3)).collect()
    println(r.toBuffer)
    sc.stop()
  }

  //使用case class
  case class XianRou(age: Int, fv: Int)
}
