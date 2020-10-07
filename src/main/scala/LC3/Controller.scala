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
  val J           = UInt(6.W)
  val COND        = UInt(3.W)
  val IRD         = Bool()
}

class ctrlSigRom extends Module with ControlParam{
  val io = IO(new Bundle{
    val sel = Input(UInt(stateBits.W))
    val out = Output(new signalEntry)
  })

  val signalTable = VecInit(
    "b0010010010000000000000000000000000000000000000000".U,
    "b0000010010000011000000010000000000100000000000000".U,
    "b0000011001100000000000001000000000001000100000000".U,
    "b0000010111100000000000001000000000001000100000000".U,
    "b0011010100000010000001000000000010000000000000000".U,
    "b0000010010000011000000010000000000100000000001000".U,
    "b0000011001100000000000001000000000110100100000000".U,
    "b0000010111100000000000001000000000110100100000000".U,
    "b0100100100100000000000010000000001000000000011000".U,
    "b0000010010000011000000010000000000100000000010000".U,
    "b0000011000100000000000001000000000001000100000000".U,
    "b0000011101100000000000001000000000001000100000000".U,
    "b0000010010000000100000000000010000110000000000000".U,
    "b0100100101010000010010000001000000000000010000000".U,
    "b0000010010000011000000001000000000001000100000000".U,
    "b0000011100100000000000001000000000000000000000000".U,
    "b0001010000000000000000000000000000000000000000110".U,
    "b0000000000000000000000000000000000000000000000000".U,
    "b0101100001100000100001000000000000000000000000000".U,
    "b0000000000000000000000000000000000000000000000000".U,
    "b0000010010000010100001000000010010110000000000000".U,
    "b0000010010000000100000000000010000001100000000000".U,
    "b0000010010000000100000000000010000001000000000000".U,
    "b0000010000010000000000010000000000000000000011000".U,
    "b0001011000010000000000000000000000000000000000100".U,
    "b0001011001010000000000000000000000000000000000100".U,
    "b0000011001100000000000100000000000000000000000000".U,
    "b0000010010000011000000100000000000000000000000000".U,
    "b0001011100010010000001000000000010000000000000100".U,
    "b0001011101010000000000000000000000000000000000100".U,
    "b0000010010000000100000100000001000000000000000000".U,
    "b0000010111100000000000100000000000000000000000000".U,
    "b1000000000000100000000000000000000000000000000000".U,
    "b0001100001010000000000000000000000000000000000100".U,
    "b0100110011000010000000000000100101000000000000000".U,
    "b0000100000001000000000100000000000000000000000000".U,
    "b0001100100010000000000000000000000000000000000100".U,
    "b0000101001100010000000000000100101000001000000000".U,
    "b0000100111000000100000100000001000000000000000000".U,
    "b0000101000100010000000000000100101000000000000000".U,
    "b0001101000010000000000000000000000000000000000100".U,
    "b0001101001000000000000000000000000000000000100110".U,
    "b0000100010000001010000100000000000000000000000000".U,
    "b0000101111010000000000000010000000000000000000000".U,
    "b0000101101010000010010000001000000000000001000000".U,
    "b0000100101000010000100000000100101000010000000000".U,
    "b0000000000000000000000000000000000000000000000000".U,
    "b0000110000100010000000000000100101000010000000000".U,
    "b0001110000000000000000000000000000000000000000110".U,
    "b0100100101010000010010000001000000000000000000000".U,
    "b0000110100100000000000000100000000000000000000000".U,
    "b0000010010000000000000000000000000000000000000000".U,
    "b0001110100010000000000000000000000000000000000100".U,
    "b0000000000000000000000000000000000000000000000000".U,
    "b0000010010000000100000100000001000000000000000000".U,
    "b0000000000000000000000000000000000000000000000000".U,
    "b0000000000000000000000000000000000000000000000000".U,
    "b0000000000000000000000000000000000000000000000000".U,
    "b0000000000000000000000000000000000000000000000000".U,
    "b0000010010000010001000000000100101000011000000000".U,
    "b0000000000000000000000000000000000000000000000000".U,
    "b0000000000000000000000000000000000000000000000000".U,
    "b0000000000000000000000000000000000000000000000000".U,
    "b0000000000000000000000000000000000000000000000000".U
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

  val (int, r, ir, ben, psr) = (io.in.int, io.in.r, io.in.ir, io.in.ben, io.in.psr)
  val state = RegInit(18.U(stateBits.W))
  io.out := ctrlSigRom(state)

  // **new state machine**
  val currentSig = ctrlSigRom(state)
  val IRD = currentSig.IRD
  val COND = currentSig.COND
  val J = currentSig.J

  val nextstate = Cat(Seq(J(5),
                          J(4)|( COND(2) & !COND(1) &  COND(0) & int)   ,
                          J(3)|( COND(2) & !COND(1) & !COND(0) & psr)  ,
                          J(2)|(!COND(2) &  COND(1) & !COND(0) & ben)   ,
                          J(1)|(!COND(2) & !COND(1) &  COND(0) & r)     ,
                          J(0)|(!COND(2) &  COND(1) &  COND(0) & ir(0))  
                          ) 
                      )
                  

  state := Mux(IRD, Cat(0.U(2.W), ir), nextstate)


  // **old state machine** 
  // switch (state) {
  //   is (0.U) { state := Mux(ben, 22.U, 18.U) }
  //   is (1.U) { state := 18.U }
  //   is (2.U) { state := 25.U }
  //   is (3.U) { state := 23.U}
  //   is (4.U) { state := Mux(ir(0), 21.U, 20.U) }
  //   is (5.U) { state := 18.U }
  //   is (6.U) { state := 25.U }
  //   is (7.U) { state := 23.U }
  //   is (8.U) { state := Mux(psr, 44.U, 36.U)}         //RTI
  //   is (9.U) { state := 18.U }
  //   is (10.U) { state := 24.U }
  //   is (11.U) { state := 29.U }
  //   is (12.U) { state := 18.U }
  //   is (13.U) { state := Mux(psr, 45.U, 37.U) }        //To13
  //   is (14.U) { state := 18.U }
  //   is (15.U) { state := 28.U }
  //   is (16.U) { state := Mux(r, 18.U, 16.U)}
  //   //is (17.U) { state := }??
  //   is (18.U) { state := Mux(int, 49.U, 33.U) }
  //   //is (19.U) { state := }
  //   is (20.U) { state := 18.U }
  //   is (21.U) { state := 18.U }
  //   is (22.U) { state := 18.U }
  //   is (23.U) { state := 16.U }
  //   is (24.U) { state := Mux(r, 26.U, 24.U) }
  //   is (25.U) { state := Mux(r, 27.U, 25.U) }
  //   is (26.U) { state := 25.U }
  //   is (27.U) { state := 18.U }
  //   is (28.U) { state := Mux(r, 30.U, 28.U) }
  //   is (29.U) { state := Mux(r, 31.U, 29.U) }
  //   is (30.U) { state := 18.U }
  //   is (31.U) { state := 23.U }
  //   is (32.U) { state := ir }
  //   is (33.U) { state := Mux(r, 35.U, 33.U)}
  //   is (34.U) { state := Mux(psr, 59.U, 51.U) }
  //   is (35.U) { state := 32.U }
  //   is (36.U) { state := Mux(r, 38.U, 36.U)}
  //   is (37.U) { state := 41.U }
  //   is (38.U) { state := 39.U }
  //   is (39.U) { state := 40.U }
  //   is (40.U) { state := Mux(r, 42.U, 40.U) }
  //   is (41.U) { state := Mux(r, 43.U, 41.U) }
  //   is (42.U) { state := 34.U }
  //   is (43.U) { state := 47.U }
  //   is (44.U) { state := 45.U }
  //   is (45.U) { state := 37.U }
  //   is (47.U) { state := 48.U }
  //   is (48.U) { state := Mux(r, 50.U, 48.U) }
  //   is (49.U) { state := Mux(psr, 45.U, 37.U) }
  //   is (50.U) { state := 52.U }
  //   is (51.U) { state := 18.U }
  //   is (52.U) { state := Mux(r, 54.U, 52.U) }
  //   is (54.U) { state := 18.U}
  //   is (59.U) { state := 18.U }
  // }
}

