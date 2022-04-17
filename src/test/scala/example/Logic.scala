package example

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

// 示例Logic模块
class Logic extends Module {
  val io = IO(new Bundle {
    val ina  = Input(UInt(2.W))
    val inb  = Input(UInt(2.W))
    val add  = Output(UInt(2.W))
    val and  = Output(UInt(2.W))
    val or   = Output(UInt(2.W))
    val not  = Output(UInt(2.W))
    val xor  = Output(UInt(2.W))
  })

  io.add := io.ina + io.inb
  io.and := io.ina & io.inb
  io.or  := io.ina | io.inb
  io.not := ~io.ina
  io.xor := io.ina ^ io.inb

}

// 示例测试
class LogicTest extends AnyFlatSpec with ChiselScalatestTester {

  it should "test logic" in {
    test(new Logic) { c =>
      c.io.ina.poke("b01".U)
      c.io.ina.poke("b10".U)
      println(s"输入ina: ${c.io.ina.peek}, 输入inb: ${c.io.inb.peek}")
      println(s"输出add: ${c.io.add.peek}")
      println(s"输出and: ${c.io.and.peek}")
      println(s"输出or : ${c.io.or.peek}")
      println(s"输出not: ${c.io.not.peek}")
      println(s"输出xor: ${c.io.xor.peek}")
    }
  }

}