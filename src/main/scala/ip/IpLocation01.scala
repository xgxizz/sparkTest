package ip

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object IpLocation01 {

  def main(args: Array[String]): Unit = {

    val config: SparkConf = new SparkConf().setAppName("IpLocation01").setMaster("local[4]")
    val sc = new SparkContext(config)

    val rules: Array[(Long, Long, String)] = MyUtils.readRules(args(0))
    //将Driver端的数据广播到Executor中
    //调用sc上的广播方法
    //广播变量的引用（还在Driver中）
    val broadcastRef: Broadcast[Array[(Long, Long, String)]] = sc.broadcast(rules)

    //创建RDD,读取日志文件
    val accessLines: RDD[String] = sc.textFile(args(1))
    //整理数据
    val proviceAndOne: RDD[(String, Int)] = accessLines.map(line => {
      val fields = line.split("[|]")
      val ip = fields(1)
      //将ip转换成十进制
      val ipNum = MyUtils.ip2Long(ip)
      //进行二分法查找，通过Driver端的引用或取到Executor中的广播变量
      //（该函数中的代码是在Executor中别调用执行的，通过广播变量的引用，就可以拿到当前Executor中的广播的规则了）
      val rulesInExecutor: Array[(Long, Long, String)] = broadcastRef.value //引用广播变量
      //查找
      var province = "未知"
      val index = MyUtils.binarySearch(rulesInExecutor, ipNum)
      if (index != -1) {
        province = rulesInExecutor(index)._3
      }
      (province, 1)
    })
    //聚合
    val reduced: RDD[(String, Int)] = proviceAndOne.reduceByKey(_+_)
    //将结果打印
    val r = reduced.collect()
    println(r.toBuffer)
    sc.stop()
  }
}
