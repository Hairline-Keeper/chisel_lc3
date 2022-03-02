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
  })
  val result = Wire(UInt(17.W))

  io.out := DontCare
  result := DontCare

  // lab4-task1
  // 在此编写运算器逻辑

}
