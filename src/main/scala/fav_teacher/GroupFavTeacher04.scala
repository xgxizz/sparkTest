package fav_teacher

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 减少shuffle次数
 */
object GroupFavTeacher04 {

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
    val subjects: Array[String] = subjectTeacherAndOne.map(_._1._1).distinct().collect()
    val partitioner = new SubjectPartitioner(subjects)
    val reduced: RDD[((String, String), Int)] = subjectTeacherAndOne.reduceByKey(partitioner,_+_)
    val partitioned: RDD[((String, String), Int)] = reduced.partitionBy(partitioner)
    val sorted: RDD[((String, String), Int)] = partitioned.mapPartitions(it => {
      //此处如果数据较多可能会造成数据溢出，可以每次从迭代器中读出少量（topN个），
      // 放在数组中进行排序，之后每次都出一个就和集合中的元素作比较（插入排序）
      it.toList.sortBy(_._2).reverse.take(3).iterator
    })
    println(sorted.collect().toBuffer)
  }

}
