package fav_teacher

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object ScalaWordCount {

  def main(args: Array[String]): Unit = {
    //创建spark配置，设置应用程序名字
    val conf = new SparkConf().setAppName("ScalaWordCount").setMaster("local[4]")
    //创建spark执行的入口
    val sc = new SparkContext(conf)

    val lines: RDD[String] = sc.textFile(args(0))
    //切分压平
    val words: RDD[String] = lines.flatMap(_.split(" "))
    //将单词和一组合
    val wordAndOne: RDD[(String, Int)] = words.map((_, 1))
    //按key进行聚合
    val reduced:RDD[(String, Int)] = wordAndOne.reduceByKey(_+_)
    //排序
    val sorted: RDD[(String, Int)] = reduced.sortBy(_._2, false)

    val results:Array[(String, Int)] = sorted.collect()
    println(results.toBuffer)
    //将结果保存到HDFS中
    //sorted.saveAsTextFile(args(1))
    //sorted.combineByKey()
    //释放资源
    sc.stop()
  }
}
