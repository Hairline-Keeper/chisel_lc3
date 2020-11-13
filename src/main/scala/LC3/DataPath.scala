package LC3

import chisel3._
import chisel3.util._

class FeedBack extends Bundle {
  val sig = Output(UInt(10.W))     // control signal. sig[9:4]: j   sig[3:1]: cond   sig[0]: ird
  val int = Output(Bool())         // high priority device request
  val r   = Output(Bool())         // ready: memory operations is finished
  val ir  = Output(UInt(4.W))      // opcode
  val ben = Output(Bool())         // br can be executed
  val psr = Output(Bool())         // privilege: supervisor or user
}

class DataPath extends Module {
  val io = IO(new Bundle{
    val signal = Input(new signalEntry)
    val mem = Flipped(new MemIO)
    val out = new FeedBack

    val initPC = Input(UInt(16.W))
  })

  val SIG = io.signal
  val time = GTimer()

  
  val regfile = Module(new Regfile)
  val alu = Module(new ALU)
  val bus = Module(new SimpleBus)
  

  val SP = 6.U(3.W)
  val R7 = 7.U(3.W)

  val BEN = RegInit(false.B)
  val N = RegInit(false.B)
  val P = RegInit(false.B)
  val Z = RegInit(true.B)


  val PC  = RegInit(io.initPC) 
  val IR  = RegInit(0.U(16.W))
  val MAR = RegInit(0.U(16.W))
  val MDR = RegInit(0.U(16.W))
  val PSR = RegInit(0.U(16.W))

  val KBDR = RegInit(0.U(16.W))
  val KBSR = RegInit(0.U(16.W))
  val DDR  = RegInit(0.U(16.W))
  val DSR  = RegInit(0.U(16.W))

  val BUSOUT = WireInit(0.U(16.W))
  val BUSEN =  WireInit(false.B)

  /*********** IR Decode ****************/
  val baseR = IR(8,6)
  val src2  = IR(2,0)
  val isImm = IR(5)
  val dst   = IR(11,9)

  val offset5  = SignExt(IR(4,0),  16)  //imm
  val offset6  = SignExt(IR(5,0),  16)
  val offset9  = SignExt(IR(8,0),  16)  // PC offset
  val offset11 = SignExt(IR(10,0), 16)  // (JSP)
  val offset8  = ZeroExt(IR(7,0),  16)  // interrupt vector (TRAP)

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

  val PCMUX = MuxLookup(SIG.PC_MUX, io.initPC, Seq(
    0.U -> (PC + 1.U),//Mux(PC===0.U, io.initPC,  PC + 1.U),
    1.U -> BUSOUT,
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

  val MARMUX = Mux(SIG.MAR_MUX, addrOut, offset8)
  
  val VectorMUX = MuxLookup(SIG.VECTOR_MUX, 0.U, Seq(  // TODO: Interrupt
    0.U -> 0.U,
    1.U -> 0.U,
    2.U -> 0.U
  ))

  val PSRMUX = 0.U

  /*********** ALU ****************/
  alu.io.ina := regfile.io.r1Data
  alu.io.inb := SR2MUX
  alu.io.op := SIG.ALUK

  /*********** Regfile ****************/
  regfile.io.wen    := SIG.LD_REG  //TODO :CHECK
  regfile.io.wAddr  := DRMUX
  regfile.io.r1Addr := SR1MUX
  regfile.io.r2Addr := IR(2, 0)
  regfile.io.wData  := BUSOUT

  val dstData = WireInit(regfile.io.wData)

  /*********** Memory ****************/

  // address control logic that convered by truth table

  //val MEM_EN = SIG.MIO_EN && MAR < 0xfe00.U
  val MEM_RD = SIG.MIO_EN && !SIG.R_W
  val MEM_EN = SIG.MIO_EN && (MAR < 0xfe00.U)

  val IN_MUX = MuxCase(io.mem.rdata, Array(
    (MEM_RD && (MAR === 0xfe00.U)) -> KBSR,
    (MEM_RD && (MAR === 0xfe02.U)) -> KBDR,
    (MEM_RD && (MAR === 0xfe04.U)) -> io.mem.rdata,
    (MEM_EN && !SIG.R_W) -> io.mem.rdata
    ))

  val LD_KBSR = (MAR === 0xfe00.U) && SIG.MIO_EN && SIG.R_W
  val LD_DSR  = (MAR === 0xfe04.U) && SIG.MIO_EN && SIG.R_W
  val LD_DDR  = (MAR === 0xfe06.U) && SIG.MIO_EN && SIG.R_W

  val in_mux = Wire(UInt(16.W)) //  for debug
  val mem_en = Wire(UInt(16.W)) //  for debug
  in_mux:= IN_MUX
  mem_en := MEM_EN
  dontTouch(in_mux)
  dontTouch(mem_en)

  io.mem.raddr   := MAR
  io.mem.waddr   := MAR
  io.mem.wdata  := MDR
  io.mem.wen    := MEM_EN

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
  bus.io.GateData(4) := Cat(1.U(8.W), 0.U)  // TODO: Interrupt
  bus.io.GateData(5) := PC - 1.U
  bus.io.GateData(6) := Cat(Seq(0.U(13.W),PSRMUX))    // TODO: Add some PSR signal
  bus.io.GateData(7) := SPMUX

  BUSOUT := bus.io.out
  BUSEN := bus.io.GateSig.orR

  /*******
  *  LD
  ********/
  when(SIG.LD_MAR) {  MAR := BUSOUT }

  when(SIG.LD_MDR) {  MDR := Mux(SIG.MIO_EN, IN_MUX, BUSOUT) }

  when(SIG.LD_IR)  {  IR  := MDR }
  when(SIG.LD_BEN) {  BEN := IR(11) && N || IR(10) && Z || IR(9) && P }
  when(SIG.LD_PC || time === 0.U)  {
    PC := PCMUX
  }



  when(SIG.LD_CC) {
    N := dstData(15)
    Z := !dstData.orR()
    P := !dstData(15) && dstData.orR()
    //assert(N + Z + P === 1.U, "N,Z,P only one can be true")
  }

  when(LD_KBSR) { KBSR := MDR }
  when(LD_DSR)  { DSR  := MDR }
  when(LD_DDR)  { DDR  := MDR }


  //OUT//
  io.out.sig  := DontCare
  io.out.int  := false.B
  io.out.r    := io.mem.R
  io.out.ir   := IR(15, 12)
  io.out.ben  := BEN
  io.out.psr  := PSR(15)
}

