package fav_teacher

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object GroupFavTeacher01 {

  def main(args: Array[String]): Unit = {
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
    //[学科,该学科对应的老师的数据]
    val grouped: RDD[(String, Iterable[((String, String), Int)])] = reduced.groupBy(_._1._1)
    //经过分区后，一个分区内可能有多个学科的数据，一个学科就是一个迭代器
    //将每一个组拿出来进行排序
    //为什么可以调用scala的sortBy呢？因为一个学科的数据已经在一台机器上的一个scala集合里了
    val sorted: RDD[(String, List[((String, String), Int)])] = grouped.mapValues(_.toList.sortBy(_._2).reverse.take(3))
    val tuples: Array[(String, List[((String, String), Int)])] = sorted.collect()
    println(tuples.toBuffer)
    context.stop()
  }
}
