package example

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

// 示例Reg模块
class Reg extends Module {
  val io = IO(new Bundle {
    val in   = Input(UInt(2.W))
    val out  = Output(UInt(2.W))
  })

  val aReg = RegInit(1.U(2.W))

  aReg := io.in
  io.out := aReg

}

class CondReg extends Module {
  val io = IO(new Bundle {
    val in   = Input(UInt(2.W))
    val cond = Input(Bool())
    val out  = Output(UInt(2.W))
  })

  val aReg = RegInit(1.U(2.W))

  when(io.cond) {
    aReg := io.in
  }
  io.out := aReg

}

// 示例测试
class RegTest extends AnyFlatSpec 
  with ChiselScalatestTester {

  it should "test reg" in {
    test(new Reg) { c =>
      c.io.in.poke(2.U)
      println(s"时钟到来前输出：${c.io.out.peek}")
      c.clock.step()
      println(s"时钟到来后输出：${c.io.out.peek}")
    }
  }
}

// 示例测试
class CondRegTest extends AnyFlatSpec 
  with ChiselScalatestTester {

  it should "test condreg" in {
    test(new CondReg) { c =>
      c.io.in.poke(2.U)
      c.io.cond.poke(false.B)
      c.clock.step()
      println(s"cond==false输出：${c.io.out.peek}")

      c.io.in.poke(2.U)
      c.io.cond.poke(true.B)
      c.clock.step()
      println(s"cond==true输出：${c.io.out.peek}")
    }
  }
}