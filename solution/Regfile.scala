package LC3

import chisel3._
import chisel3.util._

class Regfile extends Module{
  val io = IO(new Bundle {
    val wen = Input(Bool())
    val wAddr = Input(UInt(3.W))
    val r1Addr = Input(UInt(3.W))
    val r2Addr = Input(UInt(3.W))
    val wData = Input(UInt(16.W))
    val r1Data = Output(UInt(16.W))
    val r2Data = Output(UInt(16.W))
  })


  // lab4-task1
  // 在此编写寄存器堆逻辑

  val regfile = RegInit(VecInit(Array.fill(8)(0.U(16.W))))

  when(io.wen){
    regfile(io.wAddr) := io.wData
  }
  io.r1Data := regfile(io.r1Addr)
  io.r2Data := regfile(io.r2Addr)
}
