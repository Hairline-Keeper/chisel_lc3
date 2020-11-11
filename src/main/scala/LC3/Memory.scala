package LC3

import chisel3._

class MemIO extends Bundle {
  val raddr = Input(UInt(16.W))
  val rdata = Output(UInt(16.W))
  val waddr = Input(UInt(16.W))
  val wdata = Input(UInt(16.W))
  val wen = Input(Bool())
  val R = Output(Bool())
}

class Memory extends Module {
  val io = IO(new MemIO)

  val mem = Mem(1<<16, UInt(16.W))
  
  io.rdata := mem.read(io.raddr)
  when(io.wen) {
    mem.write(io.waddr, io.wdata)
  }
  io.R := true.B
}