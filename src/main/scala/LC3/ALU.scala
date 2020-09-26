package LC3

import chisel3._
import chisel3.experimental._
import chisel3.util._

class ALU extends Module{
  val io = IO(new Bundle{
    val ina = Input(UInt(16.W))
    val inb = Input(UInt(16.W))
    val op  = Input(UInt(2.W))    //ADD,AND,NOT,PASSA
    val out = Output(UInt(16.W))
    val c   = Output(UInt(1.W))
  })
  val result = Wire(UInt(17.W))

  io.out := DontCare
  io.c := DontCare
  result := DontCare
  switch (io.op) {
    is (0.U) {
      result := io.ina +& io.inb
      io.out := result(15,0)
      io.c := result(16)
    }
    is (1.U) { io.out := io.ina &io.inb }
    is (2.U) { io.out := ~io.ina }
    is (3.U) { io.out := io.ina }
  }
}
