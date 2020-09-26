package LC3

import chisel3._
import chisel3.util._

class MemIO extends Bundle {
  val addr = Input(UInt(16.W))
  val wen = Input(Bool())
  val wdata = Input(UInt(16.W))
  val R = Output(Bool())
  val rdata = Output(UInt(16.W))
}

class Memory extends Module {
  val io = IO(new MemIO)
  
  val mem = Mem(1<<16, UInt(16.W))
  when(io.wen) {
    mem(io.addr) := io.wdata
  }
  io.rdata := mem(io.addr)
  io.R := true.B
}