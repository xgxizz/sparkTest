package fav_teacher

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object GroupFavTeacher03 {

  def main(args: Array[String]): Unit = {
//    val topN = 3
//    val subjects = Array("bigdata","javaee","php")
    val config:SparkConf = new SparkConf().setAppName("FavTeacher").setMaster("local[4]")
    val context:SparkContext = new SparkContext(config)
    var lines: RDD[String] = context.textFile(args(0))
    val subjectTeacherAndOne: RDD[((String, String),Int)] = lines.map(line => {
      val splits = line.split('/')
      val subject = splits(2).split("[.]")(0)//转移 '.'
      val teacher = splits(splits.size - 1)
      ((subject, teacher),1)
    })
    val reduced: RDD[((String, String), Int)] = subjectTeacherAndOne.reduceByKey(_+_)
    val subjects: Array[String] = reduced.map(_._1._1).distinct().collect()
    val partitioner = new SubjectPartitioner(subjects)
    val partitioned: RDD[((String, String), Int)] = reduced.partitionBy(partitioner)
    val sorted: RDD[((String, String), Int)] = partitioned.mapPartitions(it => {
      it.toList.sortBy(_._2).reverse.take(3).iterator
    })
    println(sorted.collect().toBuffer)
  }
}
