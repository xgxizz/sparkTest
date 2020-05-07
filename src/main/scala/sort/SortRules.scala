package sort

import sort.CustomSort04.XianRou

object SortRules {

  implicit object orderingXianRou extends Ordering[XianRou]{
    override def compare(x: XianRou, y: XianRou): Int = {
      if (x.fv == x.fv){
        x.age - y.age  //正序排列
      }else{
        - (x.fv - y.fv)//按照fv逆序
      }
    }
  }
}
