package LC3

import chisel3._

class MemIO extends Bundle {
  val clka  = Input(Clock())
  val ena   = Input(Bool())
  val wea   = Input(Bool())
  val addra = Input(UInt(16.W))
  val dina  = Input(UInt(16.W))
  val douta = Output(UInt(16.W))
}

class Memory extends BlackBox {
  val io = IO(new MemIO)

  //dontTouch(io)
}