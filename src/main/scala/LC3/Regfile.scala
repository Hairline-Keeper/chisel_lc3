package LC3

import chisel3._
import chisel3.experimental.IO

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

  // val regfile = Vec(8, RegInit(0.U(16.W)))
  val regfile = RegInit(VecInit(Seq.fill(8)(0.U(16.W))))

  when(io.wen){
    regfile(io.wAddr) := io.wData
  }
  regfile(io.r1Addr) := io.r1Data
  regfile(io.r2Addr) := io.r2Data
}
