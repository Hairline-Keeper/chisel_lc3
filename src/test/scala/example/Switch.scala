package example

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

// 示例Switch模块
class Switch extends Module {
  val io = IO(new Bundle {
    val ina     = Input(UInt(4.W))
    val inb     = Input(UInt(4.W))
    val select  = Input(UInt(1.W)) 
    val result  = Output(UInt(4.W))
  })

  io.result := 0.U

  switch (io.select) {
    is(0.U) {io.result := io.ina}
    is(1.U) {io.result := io.inb}
  }
}

// 示例测试
class SwitchTest extends AnyFlatSpec 
  with ChiselScalatestTester {

  it should "test switch" in {
    test(new Switch) { c =>
      c.io.ina.poke(3.U)
      c.io.inb.poke(6.U)
      c.io.select.poke(0.U)
      println(s"select为${c.io.select.peek}, " +
              s"result为${c.io.result.peek}")

      c.io.select.poke(1.U)
      println(s"select为${c.io.select.peek}, " +
              s"result为${c.io.result.peek}")
    }
  }

}