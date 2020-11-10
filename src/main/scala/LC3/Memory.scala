package LC3

import chisel3._

// class RAMHelper() extends BlackBox {
//   val io = IO(new Bundle {
//     val clk = Input(Clock())
//     val rIdx = Input(UInt(16.W))
//     val rdata = Output(UInt(16.W))
//     val wIdx = Input(UInt(16.W))
//     val wdata = Input(UInt(16.W))
//     //val wmask = Input(UInt(16.W))
//     val wen = Input(Bool())
//   })
// }

class MemIO extends Bundle {
  val rIdx = Input(UInt(16.W))
  val rdata = Output(UInt(16.W))
  val wIdx = Input(UInt(16.W))
  val wdata = Input(UInt(16.W))
  // val wmask = Input(UInt(16.W))
  val wen = Input(Bool())
  val R = Output(Bool())
}

class Memory extends Module {
  val io = IO(new MemIO)
  
  // val mem = Module(new RAMHelper())
  val mem = Mem(65536, UInt(16.W))
  // mem.io.clk := clock
  // mem.io.rIdx := io.rIdx
  // io.rdata := mem.io.rdata
  // mem.io.wIdx := io.wIdx
  // mem.io.wdata := io.wdata
  //mem.io.wmask := "b11".U
  // mem.io.wen := io.wen
  io.rdata := mem.read(io.rIdx)
  when(io.wen) {
    mem.write(io.wIdx, io.wdata)
  }
  io.R := true.B
}