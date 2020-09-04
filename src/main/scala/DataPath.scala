
import chisel3._
import chisel3.util._

class DataPath extends Module {
  val io = IO(new Bundle{
    val signal = Input(new signalEntry)
  })

  val regfile = new Regfile
  val alu = new ALU




  val SP = 6.U(3.W)
  val R7 = 7.U(3.W)


  val SIG = io.signal

  val BEN = RegInit(false.B)
  val N = RegInit(false.B)
  val P = RegInit(false.B)
  val Z = RegInit(false.B)



  val PC  = RegInit(0.U(16.W))
  val IR  = RegInit(0.U(16.W))
  val MAR = RegInit(0.U(16.W))
  val MDR = RegInit(0.U(16.W))
  val PSR = RegInit(0.U(16.W))


  val dstData = Wire(regfile.io.wdata)

  /*********** IR Decode ****************/
  val baseR = IR(8,6)
  val src2  = IR(2,0)
  val isImm = IR(5)
  val dst   = IR(11,9)

  //offset
  val offset5  = SignExt(IR(4,0), 5)  //imm
  val offset6  = SignExt(IR(5,0), 6)
  val offset9  = SignExt(IR(8,0), 9)
  val offset11 = SignExt(IR(10,0), 11)
  val offset8  = ZeroExt(IR(7,0), 8)  //int vec

  /*********** Datapath ****************/

  /*******
  *  Mux
  ********/
  val PCMUX = MuxLookup(SIG.PC_MUX, PC + 1.U, Seq(
    0.U -> (PC + 1.U),
    1.U -> baseR,
    2.U -> addrOut
  ))

  val DRMUX = MuxLookup(SIG.DR_MUX, IR(11,9), Seq(
    0.U -> IR(11,9),
    1.U -> R7,
    2.U -> SP
  ))

  val SR1MUX = MuxLookup(SIG.SR1_MUX, IR(11,9), Seq(
    0.U -> IR(11,9),
    1.U -> IR(8,6),
    2.U -> SP
  ))

  val ADDR1MUX = Mux(SIG.ADDR1_MUX, baseR, PC)

  val ADDR2MUX = MuxLookup(SIG.ADDR2_MUX, 0.U, Seq(
    0.U -> 0.U,
    1.U -> offset6,
    2.U -> offset9,
    3.U -> offset11
  ))

  //val SPMUX

  val MARMUX = Mux(SIG.MAR_MUX, addrOut, offset8)
  
  //val VectorMUX

  //val PSRMUX



  //other mux

  val addrOut = addr1Mux + addr2Mux
  val src2Mux = Mux(isImm, offset5, src2)

  /*******
  *  LD
  ********/

  when(SIG.LD_MAR) {
    when(SIG.GATE_PC)     { MAR := PC }
    when(SIG.GATE_MDR)    { MAR := MDR }
    when(SIG.GATE_ALU)    { MAR := alu.io.out }
    when(SIG.GATE_MARMUX) { MAR := MARMUX }
    when(SIG.GATE_VECTOR) { MAR := Cat(1.U(8.W), offset8) }   //50
    //when(SIG.GATE_SP)     { MAR :=  SPMUX }                   //37 39 47 59
  }

  when(SIG.LD_MDR) {
    when(SIG.GATE_ALU)  { MDR := alu.io.out }  //23
    when(SIG.GATE_PC1)  { MDR := PC - 1 }
    when(SIG.GATE_PSR)  { MDR := PSR }
    //when(SIG.MIO_EN)    { MDR := MEMORY.IO. } //要先写memory
  }

  when(SIG.LD_IR) {
    when(SIG.GATE_MDR) { IR := MDR }
  }

  when(SIG.LD_BEN){
    BEN := IR(11) & N + IR(10) & Z + IR(9) & P
  }

  when(SIG.LD_REG) {
    regfile.io.wen := true.B
    regfile.io.waddr := DRMUX
    when(SIG.GATE_PC)     { regfile.io.wdata := PC }
    when(SIG.GATE_MDR)    { regfile.io.wdata := MDR }   //27
    when(SIG.GATE_ALU)    { regfile.io.wdata := alu.io.out }
    when(SIG.GATE_MARMUX) { regfile.io.wdata := MARMUX }   //14
    when(SIG.GATE_SP)     { regfile.io.wdata := SPMUX }
  }

  when(SIG.LD_CC) {
    N := dstData(15)
    Z := !dstData.orR()
    P := !dstData(15) && dstData.orR()
  }

  when(SIG.LD_PC) {
    PC := PCMUX
    when(SIG.GATE_MDR) { PC := MDR }
  }
//
//
//
//


}
