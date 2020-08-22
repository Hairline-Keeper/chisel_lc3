
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



  val PC  = RegNext(pcMux, 0.U(16.W))
  val IR  = RegInit(0.U(16.W))
  val MAR = RegInit(0.U(16.W))
  val MDR = RegInit(0.U(16.W))

  /*********** IR Decode ****************/
  val baseR = IR(8,6)
  val src2 = IR(2,0)
  val isImm = IR(5)

  //offset
  val offset5  = SignExt(IR(4,0), 5)  //imm
  val offset6  = SignExt(IR(5,0), 6)
  val offset8  = ZeroExt(IR(7,0), 8)  //int vec
  val offset9  = SignExt(IR(8,0), 9)
  val offset11 = SignExt(IR(10,0), 11)

  /*********** Datapath ****************/
  val pcMux = MuxLookup(SIG.PC_MUX, PC + 1.U, Seq(
    0.U -> (PC + 1.U),
    1.U -> baseR,
    2.U -> addrOut
  ))
  val addr1Mux = Mux(SIG.ADDR1_MUX, baseR, PC)
  val addr2Mux = MuxLookup(SIG.ADDR2_MUX, 0.U, Seq(
    0.U -> 0.U,
    1.U -> offset6,
    2.U -> offset9,
    3.U -> offset11
  ))
  val addrOut = addr1Mux + addr2Mux
  val marMux = Mux(SIG.MAR_MUX, addrOut, offset8)

  val dstMux = MuxLookup(SIG.DR_MUX, IR(11,9), Seq(
    0.U -> IR(11,9),
    1.U -> R7,
    2.U -> SP
  ))
  val src1Mux = MuxLookup(SIG.SR1_MUX, IR(11,9), Seq(
    0.U -> IR(11,9),
    1.U -> IR(8,6),
    2.U -> SP
  ))
  val src2Mux = Mux(isImm, offset5, src2)



  //LD

  when(SIG.LD_MAR) {
    when(SIG.GATE_ALU) { MAR := alu.io.out }
    when(SIG.GATE_SP) { MAR :=  }
    when(SIG.GATE_MARMUX) { MAR := marMux }
    when(SIG.GATE_PC) { MAR := PC }
    when(SIG.GATE_MDR) { MAR := MDR }
    when(SIG.GATE_VECTOR) { MAR := Cat(1.U(8.W), offset8) }
  }

  when(SIG.LD_MDR) {

  }

  when(SIG.LD_IR) {
    when(SIG.GATE_MDR) { IR := MDR }
  }

  when(SIG.LD_BEN){
    BEN := IR(11) & N + IR(10) & Z + IR(9) & P
  }

  when(SIG.LD_REG) {

  }

  val dstData = Wire(UInt(16.W))
  when(SIG.LD_CC) {
    when(SIG.GATE_ALU) { dstData := alu.io.out }
    when(SIG.GATE_MDR) { dstData := MDR }
    N := dstData(15)
    Z := !dstData.orR()
    P := !dstData(15) && dstData.orR()
  }

  when(SIG.LD_PC) {
    when(SIG.GATE_MDR) { PC := MDR }
    PC := pcMux    // GATE_ALU
  }

}
