package LC3

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ControllerTest extends AnyFlatSpec
  with ChiselScalatestTester
{
  behavior of "Controller"

  
  it should "test state machine" in {
    test(new Controller) { c =>

      // 初始状态
      c.io.work.poke(true.B) 
      c.io.end.poke(false.B)
      c.clock.step()
      println(s"io.state=${c.io.state.peek}")
      
      // add指令状态转移
      c.io.in.int.poke(false.B)
      c.clock.step()
      println(s"io.state=${c.io.state.peek}")

      c.io.in.r.poke(true.B)
      c.clock.step()
      println(s"io.state=${c.io.state.peek}")

      c.clock.step()
      println(s"io.state=${c.io.state.peek}")
      
      c.io.in.ir.poke(1.U)
      c.clock.step()
      println(s"io.state=${c.io.state.peek}")

      c.clock.step()
      println(s"io.state=${c.io.state.peek}")

    }
  }
}