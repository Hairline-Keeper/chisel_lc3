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

  io.r1Data := DontCare
  io.r2Data := DontCare

  // lab4-task1
  // 在此编写寄存器堆逻辑

}
