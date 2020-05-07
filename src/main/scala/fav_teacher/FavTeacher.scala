package fav_teacher

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 求最受欢迎的老师
 */
object FavTeacher {

  def main(args: Array[String]): Unit = {
    val config:SparkConf = new SparkConf().setAppName("FavTeacher").setMaster("local[4]")
    val context:SparkContext = new SparkContext(config)
    var lines: RDD[String] = context.textFile(args(0))
    val teacherAndOne: RDD[(String, Int)] = lines.map(line => {
      val splits = line.split('/')
      //val subject = splits(2).split("[.]")(0)//转移 '.'
      val teacher = splits(splits.size - 1)
      (teacher, 1)
    })
    val reduced: RDD[(String, Int)] = teacherAndOne.reduceByKey(_ + _)
    val sorted: RDD[(String, Int)] = reduced.sortBy(_._2,false)
    val results: Array[(String, Int)] = sorted.collect()
    println(results.toBuffer)
    println("最受欢迎的老师是："+results(0)._1)
    context.stop()
  }
}
