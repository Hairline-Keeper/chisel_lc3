package LC3

import chisel3._
import chisel3.util._

class SimpleBusIO extends Bundle {
	val GateSig = Input(UInt(8.W))
	val GateData = Input(Vec(8, UInt(16.W)))
	val out = Output(UInt(16.W))
}

class SimpleBus extends Module {
	val io = IO(new SimpleBusIO)

	io.out := Mux(io.GateSig === 0.U, 0.U, io.GateData(OHToUInt(Reverse(io.GateSig))))
}