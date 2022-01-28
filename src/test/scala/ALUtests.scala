package LC3

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

class ALUtest extends AnyFlatSpec
  with ChiselScalatestTester
{
  behavior of "ALU"

  def TEST_SIZE = 10

  val ina, inb, add_out, and_out, not_out, pass_out= Array.fill(TEST_SIZE)(0)

  for (i <- 0 until TEST_SIZE) {
    ina(i) = Random.nextInt(0xffff)
    inb(i) = Random.nextInt(0xffff)
    add_out(i) = ina(i) + inb(i)
    and_out(i) = ina(i) & inb(i)
    not_out(i) = 0xffff-ina(i)
    pass_out(i) = ina(i)
  }


  it should "test add" in {
    test(new ALU) { c =>
      println(s"*******test add********")
      c.io.op.poke(0.U)
      for(i <- 0 until TEST_SIZE) {
//        println(s"${i}. ina=${ina(i)}  inb=${inb(i)}  add_out=${add_out(i)}\n")
        c.io.ina.poke(ina(i).U)
        c.io.inb.poke(inb(i).U)
        c.io.out.expect(add_out(i).U(15,0))
        c.io.c.expect((add_out(i)>0xffff).B)
//        println(s"${i}. io.out=${c.io.out.peek}  io.c=${c.io.c.peek}\n")
      }
    }
  }

  it should "test and" in {
    test(new ALU) { c =>
      println(s"*******test and********")
      c.io.op.poke(1.U)
      for(i <- 0 until TEST_SIZE) {
        c.io.ina.poke(ina(i).U)
        c.io.inb.poke(inb(i).U)
        c.io.out.expect(and_out(i).U)
      }
    }
  }

  it should "test not" in {
    test(new ALU) { c =>
      println(s"*******test not********")
      c.io.op.poke(2.U)
      for(i <- 0 until TEST_SIZE) {
        c.io.ina.poke(ina(i).U)
        c.io.out.expect(not_out(i).U(15, 0))
      }
    }
  }

  it should "test pass" in {
    test(new ALU) { c =>
      println(s"*******test pass*******")
      c.io.op.poke(3.U)
      for(i <- 0 until TEST_SIZE) {
        c.io.ina.poke(ina(i).U)
        c.io.out.expect(pass_out(i).U)
      }
    }
  }
}

