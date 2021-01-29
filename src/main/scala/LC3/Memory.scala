package LC3

import chisel3._
import chisel3.util.experimental._

class RAMHelper() extends BlackBox {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val rIdx = Input(UInt(16.W))
    val rdata = Output(UInt(16.W))
    val wIdx = Input(UInt(16.W))
    val wdata = Input(UInt(16.W))
    //val wmask = Input(UInt(16.W))
    val wen = Input(Bool())
  })
}

class dual_mem extends BlackBox {
  val io = IO(new Bundle {
    val clka = Input(Clock())
    val wea = Input(Bool())
    val addra = Input(UInt(16.W))
    val dina = Input(UInt(16.W))
    val clkb = Input(Clock())
    val addrb = Input(UInt(16.W))
    val doutb = Output(UInt(16.W))
  })
}

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
  
  if(CoreConfig.FPGAPlatform) {
    // val mem = Mem(65536, UInt(16.W))
    // io.rdata := mem.read(io.rIdx)
    // when(io.wen) {
    //   mem.write(io.wIdx, io.wdata)
    // }
    val mem = Module(new dual_mem())
    mem.io.clka   := clock
    mem.io.wea    := io.wen
    mem.io.addra  := io.waddr
    mem.io.dina   := io.wdata
    mem.io.clkb   := clock
    mem.io.addrb  := io.raddr
    io.rdata      := mem.io.doutb
  }else {
    val mem = Module(new RAMHelper())
    mem.io.clk := clock
    mem.io.rIdx := io.raddr
    io.rdata := mem.io.rdata
    mem.io.wIdx := io.waddr
    mem.io.wdata := io.wdata
    // mem.io.wmask := "b11".U
    mem.io.wen := io.wen

    // val meminit = Mem(1024, UInt(16.W))
    // loadMemoryFromFile(meminit, "/home/zjr/chisel_lc3/resource/init.txt")

    // for (i <- 0 until 10) {
      // printf(p"${meminit.read(1.U)}\n")
    // }
  }
  
  io.R := true.B
}