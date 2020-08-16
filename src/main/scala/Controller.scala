import chisel3._
import chisel3.experimental._
import chisel3.util._

trait ControlParam { }

class Controller extends ControlParam {
  val io = IO(new Bundle{
    val sig = Input(UInt(10.W))     // control signal. sig[9:4]: j   sig[3:1]: cond   sig[0]: ird
    val int = Input(Bool())         // high priority device request
    val r   = Input(Bool())         // ready: memory operations is finished
    val ir  = Input(UInt(5.W))      // opcode
    val ben = Input(Bool())         // br can be executed
    val psr = Input(Bool())         // privilege: supervisor or user
    val out = Output(UInt(49.W))    // output control signal: out[48:39]:next input control signal   out[38:0]: to datapath
  })

  val (sig, int, r, ir, ben, psr, out) = (io.sig, io.int, io.r, io.ir, io.ben, io.psr, io.out)
  val state = RegInit(18.U(6.W))

  switch (state) {
    is (0.U) { state := Mux(ben, 22.U, 18.U) }
    is (1.U) { state := 18.U }
    is (2.U) { state := 25.U }
    is (3.U) { state := 23.U}
    is (4.U) { state := Mux(ir(0), 21.U, 20.U) }
    is (5.U) { state := 18.U }
    is (6.U) { state := 25.U }
    is (7.U) { state := 23.U }
    is (8.U) { state := Mux(psr(4), 44.U, 36.U)}         //RTI
    is (9.U) { state := 18.U }
    is (10.U) { state := 24.U }
    is (11.U) { state := 29.U }
    is (12.U) { state := 18.U }
    is (13.U) { state := Mux(psr(4), 45.U, 37.U) }        //To13
    is (14.U) { state := 18.U }
    is (15.U) { state := 28.U }
    is (16.U) { state := Mux(r, 18.U, 16.U)}
    is (17.U) { state := }
    is (18.U) { state := Mux(int, 49.U, 33.U) }
    is (19.U) { state := }
    is (20.U) { state := 18.U }
    is (21.U) { state := 18.U }
    is (22.U) { state := 18.U }
    is (23.U) { state := 16.U }
    is (24.U) { state := Mux(r, 26.U, 24.U) }
    is (25.U) { state := Mux(r, 27.U, 25.U) }
    is (26.U) { state := 25.U }
    is (27.U) { state := 18.U }
    is (28.U) { state := Mux(r, 30.U, 28.U) }
    is (29.U) { state := Mux(r, 31.U, 29.U) }
    is (30.U) { state := 18.U }
    is (31.U) { state := 23.U }
    is (32.U) { state := Cat(0.U(2.W), ir)}
    is (33.U) { state := Mux(r, 35.U, 33.U)}
    is (34.U) { state := Mux(psr(4), 59.U, 51.U) }
    is (35.U) { state := 32.U }
    is (36.U) { state := Mux(r, 38.U, 36.U)}
    is (37.U) { state := 41.U }
    is (38.U) { state := 39.U }
    is (39.U) { state := 40.U }
    is (40.U) { state := Mux(r, 42.U, 40.U) }
    is (41.U) { state := Mux(r, 43.U, 41.U) }
    is (42.U) { state := 34.U }
    is (43.U) { state := 47.U }
    is (44.U) { state := 45.U }
    is (45.U) { state := 37.U }
    is (47.U) { state := 48.U }
    is (48.U) { state := Mux(r, 50.U, 48.U) }
    is (49.U) { state := Mux(psr, 45.U, 37.U) }
    is (50.U) { state := Mux(r, 54.U, 52.U) }
    is (51.U) { state := 18.U }
    is (54.U) { state := 18.U}
    is (59.U) { state := 18.U }
  }
}
