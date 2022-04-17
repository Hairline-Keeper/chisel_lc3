package LC3

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

class ALUTest extends AnyFlatSpec with ChiselScalatestTester {

  def TEST_SIZE = 10

  val ina, inb, add_out, and_out, not_out, pass_out = Array.fill(TEST_SIZE)(0)

  // 生成正确模型结果
  for (i <- 0 until TEST_SIZE) {
    ina(i) = Random.nextInt(0xffff)
    inb(i) = Random.nextInt(0xffff)
    add_out(i) = ina(i) + inb(i)
    (i) = ina(i) & inb(i)
    not_out(i) = 0xffff-ina(i)
    pass_out(i) = ina(i)
  }

  // 硬件部分
  it should "test add" in {
    test(new ALU) { c =>
      for(i <- 0 until TEST_SIZE) {
        c.io.ina.poke(ina(i).U)
        c.io.inb.poke(inb(i).U)
        c.io.out.expect(add_out(i).U(15,0))
        println(s"${i}. ina=${ina(i)}  inb=${inb(i)}")
        println(s"${i}. 标准结果 add_out=${add_out(i) % (1<<16)} 模块结果 io.out=${c.io.out.peek}")
      }
    }
  }

  // 实验三 任务二
  // 在此编写 and not pass 逻辑的测试用例

  it should "test and" in {
    test(new ALU) { c =>
      c.io.ina.poke(ina(i).U)
      c.io.inb.poke(inb(i).U)
      c.io.out.expect(and_out(i).U(15,0))
    }
  }

  it should "test not" in {
    test(new ALU) { c =>
      c.io.ina.poke(ina(i).U)
      c.io.inb.poke(inb(i).U)
      c.io.out.expect(not_out(i).U(15,0))
    }
  }

  it should "test pass" in {
    test(new ALU) { c =>
      c.io.ina.poke(ina(i).U)
      c.io.inb.poke(inb(i).U)
      c.io.out.expect(pass_out(i).U(15,0))
    }
  }
}

