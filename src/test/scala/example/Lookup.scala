package example

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

// 示例MuxLookup模块
class Lookup extends Module {
  val io = IO(new Bundle {
    val ina     = Input(UInt(4.W))
    val inb     = Input(UInt(4.W))
    val inc     = Input(UInt(4.W))
    val ind     = Input(UInt(4.W))
    val select  = Input(UInt(2.W)) 
    val result  = Output(UInt(4.W))
  })

  io.result := MuxLookup(io.select, 0.U, Seq(
    0.U -> io.ina,
    1.U -> io.inb,
    2.U -> io.inc,
    3.U -> io.ind
  ))

}

// 示例测试
class LookupTest extends AnyFlatSpec 
  with ChiselScalatestTester {

  it should "test lookup" in {
    test(new Lookup) { c =>
      c.io.ina.poke(1.U)
      c.io.inb.poke(2.U)
      c.io.inc.poke(4.U)
      c.io.ind.poke(8.U)
      c.io.select.poke(0.U)
      println(s"select:${c.io.select.peek}, "+s"result:${c.io.result.peek}")
      c.io.select.poke(1.U)
      println(s"select:${c.io.select.peek}, "+s"result:${c.io.result.peek}")
      c.io.select.poke(2.U)
      println(s"select:${c.io.select.peek}, "+s"result:${c.io.result.peek}")
      c.io.select.poke(3.U)
      println(s"select:${c.io.select.peek}, "+s"result:${c.io.result.peek}")
    }
  }

}