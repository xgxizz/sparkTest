package fav_teacher

object TestSplit {

  var splits: Array[String] = _

  var url: Array[String] = _

  var strs: Array[String] = _

  def main(args: Array[String]): Unit = {
    val line = "http://bigdata.edu360.cn/laozhang"
    val splits = line.split('/')
    val subject = splits(2).split("[.]")(0)//转移 '.
    val teacher = splits(splits.size-1)
    println(subject + "\t" + teacher)
  }
}
