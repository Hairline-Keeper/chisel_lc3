package example

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

// 示例Wire模块
class Wire extends Module {
  val io = IO(new Bundle {
    val in   = Input(UInt(1.W))
    val out  = Output(UInt(1.W))
  })

  val aWire = Wire(UInt(1.W)) // 线定义

  aWire := io.in              // 线连接
  io.out := aWire

}

// 示例测试
class WireTest extends AnyFlatSpec with ChiselScalatestTester {

  it should "test wire" in {
    test(new Wire) { c =>
      c.io.in.poke(0.U)
      println(s"输入：${c.io.in.peek}, 输出：${c.io.out.peek}")

      c.io.in.poke(1.U)
      println(s"输入：${c.io.in.peek}, 输出：${c.io.out.peek}")
    }
  }

}