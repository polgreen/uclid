import scala.util.parsing.combinator._

/**
 * Created by Rohit Sinha on 5/23/15.
 */
object UclidMain {
  def main(args: Array[String]) : Unit = {
    val input = scala.io.Source.fromFile("/Users/rohitsinha/research/development/uclid/test/test1.ucl4").mkString
    println("result: " + UclidParser.parseProc(input))
  }
}