package LC3

import chisel3._
import chisel3.tester._
import org.scalatest._

import scala.util.Random

class ALUtest extends FlatSpec
  with ChiselScalatestTester
  with Matchers
  with ParallelTestExecution
{
  behavior of "ALU"

  def TEST_SIZE = 10

  val ina, inb, add_out, and_out, not_out, pass_out= Array.fill(TEST_SIZE)(0)

  for (i <- 0 until TEST_SIZE) {
    ina(i) = Random.nextInt(0xffff)
    inb(i) = Random.nextInt(0xffff)
    add_out(i) = ina(i) + inb(i)
    and_out(i) = ina(i) & inb(i)
    not_out(i) = ~ina(i)
    pass_out(i) = ina(i)
  }


  it should "test add" in {
    test(new ALU) { c =>
      println(s"\n*******test add********\n")
      for
      c.io.ina.poke()
    }
  }

  it should "test and" in {
    test(new ALU) { c =>
      // test body here
    }
  }

  it should "test not" in {
    test(new ALU) { c =>
      // test body here
    }
  }

  it should "test pass" in {
    test(new ALU) { c =>
      // test body here
    }
  }
}