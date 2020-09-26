package LC3

import chisel3._
import chiseltest._
import org.scalatest._

import scala.util.Random

class CPUtest extends FlatSpec
  with ChiselScalatestTester
  with Matchers
  with ParallelTestExecution
{
  behavior of "CPU"

  it should "dummy test" in {
    test(new Top) { c =>
        c.controller.state
    }
  }

 
}