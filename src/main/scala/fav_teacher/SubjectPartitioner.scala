package fav_teacher

import org.apache.spark.Partitioner

import scala.collection.mutable

/**
 * 根据学科自定义分区器
 * @param sbs 学科数组
 */
class SubjectPartitioner(sbs:Array[String]) extends Partitioner{
    override def numPartitions: Int = sbs.length
    private val rules = new mutable.HashMap[String, Int]()

    var partition:Int = 0
    for (sb <- sbs){
      rules(sb) = partition
      partition += 1
    }
    override def getPartition(key: Any): Int = {
      val subject: String = key.asInstanceOf[(String, String)]._1
      rules(subject)
    }
  }