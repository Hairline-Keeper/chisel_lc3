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
  
  if(CoreConfig.FPGAPlatform) {
    val mem = Mem(65536, UInt(16.W))
    io.rdata := mem.read(io.rIdx)
    when(io.wen) {
      mem.write(io.wIdx, io.wdata)
    }
  }else {
    val mem = Module(new RAMHelper())
    mem.io.clk := clock
    mem.io.rIdx := io.rIdx
    io.rdata := mem.io.rdata
    mem.io.wIdx := io.wIdx
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