package ip

import java.sql.{Connection, DriverManager, PreparedStatement}

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.io.{BufferedSource, Source}

object IpLocation02 {

  def main(args: Array[String]): Unit = {
    //1.创建Spark上下文对象
    val config: SparkConf = new SparkConf().setAppName("IpLocation02").setMaster("local[4]")
    val sc = new SparkContext(config)
    //2.将IP规则广播到Executor中（在Driver中完成）
    //2.1 读取规则文件
    val rule_source: BufferedSource = Source.fromFile(args(0))
    val ruleLines: Iterator[String] = rule_source.getLines()
    val rules: Array[(Long, Long, String)] = ruleLines.map(line => {
      val fields: Array[String] = line.split("[|]]")
      val startNum: Long = fields(2).toLong
      val endNum: Long = fields(3).toLong
      val province: String = fields(6)
      (startNum, endNum, province)
    }).toArray
    //2.2 将规则广播(只是一个引用)
    val broadcastRef: Broadcast[Array[(Long, Long, String)]] = sc.broadcast(rules)

    //3 查找IP属于哪个省份
    //3.1 读取日志文件
    val logFile: RDD[String] = sc.textFile(args(1))
    val provinceAndOne: RDD[(String, Int)] = logFile.map(line => {
      val fields: Array[String] = line.split("[|]")
      //将IP地址转换成数字
      val ip: Long = MyUtils.ip2Long(fields(1))
      //获取规则的广播引用
      val rules: Array[(Long, Long, String)] = broadcastRef.value
      var province = "未知"
      val index = MyUtils.binarySearch(rules, ip)
      if (index != -1) {
        province = rules(index)._3
      }
      (province, 1)
    })

    //4.聚合
    val reduced: RDD[(String, Int)] = provinceAndOne.reduceByKey(_+_)
    //一次拿出一个分区(一个分区用一个连接，可以将一个分区中的多条数据写完在释放jdbc连接，这样更节省资源)
    reduced.foreachPartition(it => {
      val conn: Connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bigdata?characterEncoding=UTF-8", "root", "123568")
      //将数据通过Connection写入到数据库
      val pstm: PreparedStatement = conn.prepareStatement("INSERT INTO access_log VALUES (?, ?)")
      //将一个分区中的每一条数据拿出来
      it.foreach(tp => {
        pstm.setString(1, tp._1)
        pstm.setInt(2, tp._2)
        pstm.executeUpdate()
      })
      pstm.close()
      conn.close()
    })
    sc.stop()
  }
}
