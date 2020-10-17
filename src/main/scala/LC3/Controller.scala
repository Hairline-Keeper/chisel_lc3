package LC3

import chisel3._
import chisel3.util._

trait ControlParam {
  val stateBits = 6
}

class signalEntry extends Bundle {
  val LD_MAR      = Bool()
  val LD_MDR      = Bool()
  val LD_IR       = Bool()
  val LD_BEN      = Bool()
  val LD_REG      = Bool()
  val LD_CC       = Bool()
  val LD_PC       = Bool()
  val LD_PRIV     = Bool()
  val LD_SAVEDSSP = Bool()
  val LD_SAVEDUSP = Bool()
  val LD_VECTOR   = Bool()
  val GATE_PC     = Bool()
  val GATE_MDR    = Bool()
  val GATE_ALU    = Bool()
  val GATE_MARMUX = Bool()
  val GATE_VECTOR = Bool()
  val GATE_PC1    = Bool()
  val GATE_PSR    = Bool()
  val GATE_SP     = Bool()
  val PC_MUX      = UInt(2.W)
  val DR_MUX      = UInt(2.W)
  val SR1_MUX     = UInt(2.W)
  val ADDR1_MUX   = Bool()
  val ADDR2_MUX   = UInt(2.W)
  val SP_MUX      = UInt(2.W)
  val MAR_MUX     = Bool()
  val VECTOR_MUX  = UInt(2.W)
  val PSR_MUX     = Bool()
  val ALUK        = UInt(2.W)
  val MIO_EN      = Bool()
  val R_W         = Bool()
  val SET_PRIV    = Bool()
}

class ctrlSigRom extends Module with ControlParam {
  val io = IO(new Bundle{
    val sel = Input(UInt(stateBits.W))
    val out = Output(new signalEntry)
  })

  val signalTable = VecInit(
    "b00000000000_00000000_000000000000000_00000".U,
    "b00001100000_00100000_000001000000000_00000".U,
    "b10000000000_00010000_000000010001000_00000".U,
    "b10000000000_00010000_000000010001000_00000".U,
    "b00001000000_10000000_000100000000000_00000".U,
    "b00001100000_00100000_000001000000000_01000".U,
    "b10000000000_00010000_000001101001000_00000".U,
    "b10000000000_00010000_000001101001000_00000".U,
    "b10000000000_00100000_000010000000000_11000".U,
    "b00001100000_00100000_000001000000000_10000".U,
    "b10000000000_00010000_000000010001000_00000".U,
    "b10000000000_00010000_000000010001000_00000".U,
    "b00000010000_00000000_100001100000000_00000".U,
    "b01000001001_00000010_000000000000100_00000".U,
    "b00001100000_00010000_000000010001000_00000".U,
    "b10000000000_00010000_000000000000000_00000".U,
    "b00000000000_00000000_000000000000000_00110".U,
    "b00000000000_00000000_000000000000000_00000".U,
    "b10000010000_10000000_000000000000000_00000".U,
    "b00000000000_00000000_000000000000000_00000".U,
    "b00001010000_10000000_100101100000000_00000".U,
    "b00000010000_00000000_100000011000000_00000".U,
    "b00000010000_00000000_100000010000000_00000".U,
    "b01000000000_00100000_000000000000000_11000".U,
    "b01000000000_00000000_000000000000000_00100".U,
    "b01000000000_00000000_000000000000000_00100".U,
    "b10000000000_01000000_000000000000000_00000".U,
    "b00001100000_01000000_000000000000000_00000".U,
    "b01001000000_10000000_000100000000000_00100".U,
    "b01000000000_00000000_000000000000000_00100".U,
    "b00000010000_01000000_010000000000000_00000".U,
    "b10000000000_01000000_000000000000000_00000".U,
    "b00010000000_00000000_000000000000000_00000".U,
    "b01000000000_00000000_000000000000000_00100".U,
    "b00001000000_00000001_001010000000000_00000".U,
    "b00100000000_01000000_000000000000000_00000".U,
    "b01000000000_00000000_000000000000000_00100".U,
    "b10001000000_00000001_001010000010000_00000".U,
    "b00000010000_01000000_010000000000000_00000".U,
    "b10001000000_00000001_001010000000000_00000".U,
    "b01000000000_00000000_000000000000000_00100".U,
    "b00000000000_00000000_000000000000001_00110".U,
    "b00000101000_01000000_000000000000000_00000".U,
    "b01000000000_00000100_000000000000000_00000".U,
    "b01000001001_00000010_000000000000010_00000".U,
    "b00001000010_00000001_001010000100000_00000".U,
    "b00000000000_00000000_000000000000000_00000".U,
    "b10001000000_00000001_001010000100000_00000".U,
    "b00000000000_00000000_000000000000000_00110".U,
    "b01000001001_00000010_000000000000000_00000".U,
    "b10000000000_00001000_000000000000000_00000".U,
    "b00000000000_00000000_000000000000000_00000".U,
    "b01000000000_00000000_000000000000000_00100".U,
    "b00000000000_00000000_000000000000000_00000".U,
    "b00000010000_01000000_010000000000000_00000".U,
    "b00000000000_00000000_000000000000000_00000".U,
    "b00000000000_00000000_000000000000000_00000".U,
    "b00000000000_00000000_000000000000000_00000".U,
    "b00000000000_00000000_000000000000000_00000".U,
    "b00001000100_00000001_001010000110000_00000".U,
    "b00000000000_00000000_000000000000000_00000".U,
    "b00000000000_00000000_000000000000000_00000".U,
    "b00000000000_00000000_000000000000000_00000".U,
    "b00000000000_00000000_000000000000000_00000".U
  )

  io.out := signalTable(io.sel).asTypeOf(new signalEntry)
}

object ctrlSigRom {
  def apply(sel: UInt): signalEntry = {
    val ctrlSig = Module(new ctrlSigRom)
    ctrlSig.io.sel := sel
    ctrlSig.io.out
  }
}

class Controller extends Module with ControlParam {
  val io = IO(new Bundle{
    val in  = Flipped(new FeedBack)
    val out = Output(new signalEntry)     // output control signal
  })

  val (sig, int, r, ir, ben, psr, out) = (io.in.sig, io.in.int, io.in.r, io.in.ir, io.in.ben, io.in.psr, io.out)
  val state = RegInit(0.U(stateBits.W))
  out := ctrlSigRom(state)

  switch (state) {
    is (0.U) { state := Mux(ben, 22.U, 18.U) }
    is (1.U) { state := 18.U }
    is (2.U) { state := 25.U }
    is (3.U) { state := 23.U }
    is (4.U) { state := Mux(ir(0), 21.U, 20.U) }
    is (5.U) { state := 18.U }
    is (6.U) { state := 25.U }
    is (7.U) { state := 23.U }
    is (8.U) { state := Mux(psr, 44.U, 36.U)}         //RTI
    is (9.U) { state := 18.U }
    is (10.U) { state := 24.U }
    is (11.U) { state := 29.U }
    is (12.U) { state := 18.U }
    is (13.U) { state := Mux(psr, 45.U, 37.U) }        //To13
    is (14.U) { state := 18.U }
    is (15.U) { state := 28.U }
    is (16.U) { state := Mux(r, 18.U, 16.U) }
    // no 17 state
    is (18.U) { state := Mux(int, 49.U, 33.U) }
    // no 19 state
    is (20.U) { state := 18.U }
    is (21.U) { state := 18.U }
    is (22.U) { state := 18.U }
    is (23.U) { state := 16.U }
    is (24.U) { state := Mux(r, 26.U, 24.U) }
    is (25.U) { state := Mux(r, 27.U, 25.U) }
    is (26.U) { state := 25.U }
    is (27.U) { state := 18.U }
    is (28.U) { state := Mux(r, 30.U, 28.U) }
    is (29.U) { state := Mux(r, 31.U, 29.U) }
    is (30.U) { state := 18.U }
    is (31.U) { state := 23.U }
    is (32.U) { state := ir }
    is (33.U) { state := Mux(r, 35.U, 33.U) }
    is (34.U) { state := Mux(psr, 59.U, 51.U) }
    is (35.U) { state := 32.U }
    is (36.U) { state := Mux(r, 38.U, 36.U)}
    is (37.U) { state := 41.U }
    is (38.U) { state := 39.U }
    is (39.U) { state := 40.U }
    is (40.U) { state := Mux(r, 42.U, 40.U) }
    is (41.U) { state := Mux(r, 43.U, 41.U) }
    is (42.U) { state := 34.U }
    is (43.U) { state := 47.U }
    is (44.U) { state := 45.U }
    is (45.U) { state := 37.U }
    // no 46 state
    is (47.U) { state := 48.U }
    is (48.U) { state := Mux(r, 50.U, 48.U) }
    is (49.U) { state := Mux(psr, 45.U, 37.U) }
    is (50.U) { state := 52.U }
    is (51.U) { state := 18.U }
    is (52.U) { state := Mux(r, 54.U, 52.U) }
    // no 53 state
    is (54.U) { state := 18.U }
    // no 55-58 state
    is (59.U) { state := 18.U }
    // no 60-64 state
  }
}
