package LC3

import chisel3._
import chisel3.util._

class FeedBack extends Bundle {
  val sig = Output(UInt(10.W))     // control signal. sig[9:4]: j   sig[3:1]: cond   sig[0]: ird
  val int = Output(Bool())         // high priority device request
  val r   = Output(Bool())         // ready: memory operations is finished
  val ir  = Output(UInt(4.W))     // opcode
  val ben = Output(Bool())         // br can be executed
  val psr = Output(Bool())         // privilege: supervisor or user
}

class DataPath extends Module {
  val io = IO(new Bundle{
    val signal = Input(new signalEntry)
    val mem = Flipped(new MemIO)
    val out = new FeedBack
  })

  val SIG = io.signal

  val bus = Module(new SimpleBus)
  val regfile = Module(new Regfile)
  val alu = Module(new ALU)

  val SP = 6.U(3.W)
  val R7 = 7.U(3.W)

  val BEN = RegInit(false.B)
  val N = RegInit(false.B)
  val P = RegInit(false.B)
  val Z = RegInit(false.B)

  val PC  = RegInit("h3000".U(16.W)) // TODO: Maybe the PC can be dynamically specified by the image
  val IR  = RegInit(0.U(16.W))
  val MAR = RegInit(0.U(16.W))
  val MDR = RegInit(0.U(16.W))
  val PSR = RegInit(0.U(16.W))

  /*********** IR Decode ****************/
  val baseR = IR(8,6)
  val src2  = IR(2,0)
  val isImm = IR(5)
  val dst   = IR(11,9)

  //offset
  val offset5  = SignExt(IR(4,0),  16)  //imm
  val offset6  = SignExt(IR(5,0),  16)
  val offset9  = SignExt(IR(8,0),  16)
  val offset11 = SignExt(IR(10,0), 16)
  val offset8  = ZeroExt(IR(7,0),  16)  //int vec

  /*******
  *  Mux
  ********/
  
  val ADDR1MUX = Mux(SIG.ADDR1_MUX, regfile.io.r1Data, PC)

  val ADDR2MUX = MuxLookup(SIG.ADDR2_MUX, 0.U, Seq(
    0.U -> 0.U,
    1.U -> offset6,
    2.U -> offset9,
    3.U -> offset11
  ))

  val addrOut = ADDR1MUX + ADDR2MUX

  val PCMUX = MuxLookup(SIG.PC_MUX, PC + 1.U, Seq(
    0.U -> (PC + 1.U),
    1.U -> bus.io.out,
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

  val SR2MUX = Mux(isImm, offset5, regfile.io.r2Data)

  val SPMUX = MuxLookup(SIG.SP_MUX, regfile.io.r1Data+1.U, Seq(
    0.U -> (regfile.io.r1Data+1.U),
    1.U -> (regfile.io.r1Data-1.U),
    2.U -> SP, // TODO: Supervisor StackPointer
    3.U -> SP  // TODO: User StackPointer
  ))

  val MARMUX = Mux(SIG.MAR_MUX, offset8, addrOut)
  
  val VectorMUX = MuxLookup(SIG.VECTOR_MUX, 0.U, Seq(  // TODO: Interrupt
    0.U -> 0.U,
    1.U -> 0.U,
    2.U -> 0.U
  ))

  val PSRMUX = 0.U

  /*********** ALU ****************/
  alu.io.ina := regfile.io.r1Data
  alu.io.inb := regfile.io.r2Data
  alu.io.op := SIG.ALUK

  /*********** Regfile ****************/
  regfile.io.wen := !SIG.LD_REG
  regfile.io.wAddr := DRMUX
  regfile.io.r1Addr := SR1MUX
  regfile.io.r2Addr := IR(2, 0)
  regfile.io.wData := Mux(SIG.LD_REG, bus.io.out, 0.U)

  /*********** Memory ****************/
  io.mem.rIdx := DontCare // TODO: Do what you need do
  io.mem.wIdx := DontCare
  io.mem.wdata := Mux(SIG.LD_MDR, bus.io.out, 0.U)
  io.mem.wen := SIG.MIO_EN && !SIG.R_W
  
  io.out.sig := DontCare
  io.out.int := false.B
  io.out.r := io.mem.R
  io.out.ir := IR(15, 12)
  io.out.ben := BEN
  io.out.psr := PSR(15)


  val dstData = WireInit(regfile.io.wData)

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

  // when(SIG.LD_MDR) {
  //   when(SIG.GATE_ALU)  { MDR := alu.io.out }  //23
  //   when(SIG.GATE_PC1)  { MDR := PC - 1.U }
  //   when(SIG.GATE_PSR)  { MDR := PSR }
  //   //when(SIG.MIO_EN)    { MDR := MEMORY.IO. } //要先写memory
  // }

  MDR := Mux(SIG.MIO_EN, 0.U, bus.io.out) // TODO: Add MIO input

  when(SIG.LD_IR) {
    when(SIG.GATE_MDR) { IR := MDR }
  }

  when(SIG.LD_BEN){
    BEN := IR(11) & N + IR(10) & Z + IR(9) & P
  }

  regfile.io.wen := true.B // TODO: Remove this
  regfile.io.wAddr := DontCare
  when(SIG.LD_REG) {
    regfile.io.wen := true.B
    regfile.io.wAddr := DRMUX
    when(SIG.GATE_PC)     { regfile.io.wAddr := PC }
    when(SIG.GATE_MDR)    { regfile.io.wAddr := MDR }   //27
    when(SIG.GATE_ALU)    { regfile.io.wAddr := alu.io.out }
    when(SIG.GATE_MARMUX) { regfile.io.wAddr := MARMUX }   //14
    //when(SIG.GATE_SP)     { regfile.io.wAddr := SPMUX }
  }

  when(SIG.LD_CC) {
    N := dstData(15)
    Z := !dstData.orR()
    P := !dstData(15) && dstData.orR()
    assert(N + Z + P === 1.U, "N,Z,P only one can be true")
  }

  when(SIG.LD_PC) {
    PC := PCMUX
    when(SIG.GATE_MDR) { PC := MDR }
  }

  //*************
  //  SimpleBus
  //*************
  bus.io.GateSig := Cat(Seq(
    SIG.GATE_PC,
    SIG.GATE_MDR,
    SIG.GATE_ALU,
    SIG.GATE_MARMUX,
    SIG.GATE_VECTOR,
    SIG.GATE_PC1,
    SIG.GATE_PSR,
    SIG.GATE_SP
  ))
  bus.io.GateData(0) := PC
  bus.io.GateData(1) := MDR
  bus.io.GateData(2) := alu.io.out
  bus.io.GateData(3) := MARMUX
  bus.io.GateData(4) := Cat(1.U(8.W), 0.U) // TODO: Interrupt
  bus.io.GateData(5) := PC-1.U
  bus.io.GateData(6) := Cat(Seq(  // TODO: Add some PSR signal
    0.U(13.W),
    PSRMUX
  ))
  bus.io.GateData(7) := SPMUX
}
