package fav_teacher

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object GroupFavTeacher02 {

  def main(args: Array[String]): Unit = {
    val topN = 3
    val subjects = Array("bigdata","javaee","php")
    val config:SparkConf = new SparkConf().setAppName("FavTeacher").setMaster("local[4]")
    val context:SparkContext = new SparkContext(config)
    var lines: RDD[String] = context.textFile(args(0))
    val subjectTeacherAndOne: RDD[((String, String),Int)] = lines.map(line => {
      val splits = line.split('/')
      val subject = splits(2).split("[.]")(0)//转移 '.'
      val teacher = splits(splits.size - 1)
      ((subject, teacher),1)
    })
    //和1组合在一起 下面这种方式不好，因为调用了两次map
    //val map: RDD[((String, String), Int)] = subjectAndTeacher.map((_, 1))

    val reduced: RDD[((String, String), Int)] = subjectTeacherAndOne.reduceByKey(_+_)
    //scala排序是在内存中进行，但是内存有可能不够

    for (sb <- subjects){
      //可以调用RDD的sortBy方法，内存+磁盘进行排序
      val filtered: RDD[((String, String), Int)] = reduced.filter(_._1._1 == sb)
      //take是一个action，会触发提交
      val favTeacher: Array[((String, String), Int)] = filtered.sortBy(_._2, false).take(topN)
      println(favTeacher.toBuffer)
    }
  }
}
