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

  val regfile = RegInit(VecInit(Seq.fill(8)(0.U(16.W))))

  when(io.wen){
    regfile(io.wAddr) := io.wData
  }
  io.r1Data := regfile(io.r1Addr)
  io.r2Data := regfile(io.r2Addr)
  val R0 = regfile(0);
  val R1 = regfile(1);
  val R2 = regfile(2);
  val R3 = regfile(3);
  val R4 = regfile(4);
  val wenLatch = RegNext(io.wen)

  val lastR = Reg(UInt(80.W));
  lastR := Cat(R0, R1, R2, R3, R4)
  val newR = Cat(R0, R1, R2, R3, R4)
  // when(wenLatch && lastR =/= newR) {
  //   printf("R0=%d,R1=%d,R2=%d,R3=%d,R4=%d\n", R0, R1, R2, R3, R4)
  // }
}
