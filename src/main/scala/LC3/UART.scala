package LC3

import chisel3._
import chisel3.util._

class UARTBundle extends Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
}