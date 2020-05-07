package sort

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object CustomSort01 {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("CustomSort1").setMaster("local[*]")

    val sc = new SparkContext(conf)

    //排序规则：首先按照颜值的降序，如果颜值相等，再按照年龄的升序
    val users= Array("laoduan 30 99", "laozhao 29 9999", "laozhang 28 98", "laoyang 28 99")
    val lines: RDD[String] = sc.parallelize(users)
    val userRDD: RDD[User] = lines.map(line => {
      val fields: Array[String] = line.split(" ")
      val name: String = fields(0)
      val age: Int = fields(1).toInt
      val fv: Int = fields(2).toInt //faceValue 颜值
      new User(name, age, fv)
    })
    //不满足题设，只能按照其中一个字段进行排序
    //val sorted: RDD[(String, Int, Int)] = userRDD.sortBy(_._3, false)//默认为true升序
    //println(sorted.collect().toBuffer)
    val r: Array[User] = userRDD.sortBy(usr=>usr).collect()
    println(r.toBuffer)
    sc.stop()
  }

  class User(val name: String, val age: Int, val fv: Int) extends Ordered[User] with Serializable {
    override def compare(that: User): Int = {
      if (this.fv == that.fv){
        this.age - that.age  //正序排列
      }else{
        - (this.fv - that.fv)//按照fv逆序
      }
    }
    override def toString = s"name:$name, age: $age , fv: $fv"
  }
}
