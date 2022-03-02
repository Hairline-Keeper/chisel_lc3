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

    val initPC = Flipped(ValidIO(Input(UInt(16.W))))
    val uartRx = Flipped(DecoupledIO(UInt(8.W)))
    val uartTx = DecoupledIO(UInt(8.W))

    val end = Output(Bool())
  })

  val SIG = io.signal
  val time = GTimer()

  val regfile = Module(new Regfile)
  val alu = Module(new ALU)
  
  val SP = 6.U(3.W)
  val R7 = 7.U(3.W)

  val BEN = RegInit(false.B)
  val N = RegInit(false.B)
  val P = RegInit(false.B)
  val Z = RegInit(true.B)


  // 初始化
  val PC  = RegInit("h3000".U(16.W)) // TODO: Maybe the PC can be dynamically specified by the image
  val RESET_PC  = RegInit("h3000".U(16.W))
  when(io.initPC.valid) {
    PC := io.initPC.bits
    RESET_PC := io.initPC.bits
  }
  val IR  = RegInit(0.U(16.W))
  val MAR = RegInit(0.U(16.W))
  // val MAR_REG = RegInit(0.U(16.W))
  val MDR = RegInit(0.U(16.W))
  val PSR = RegInit(0.U(16.W))

  val KBDR = RegInit(0.U(16.W))
  val KBSR = RegInit(0.U(16.W))
  val DDR  = RegInit(0.U(16.W))
  val DSR  = RegInit(0.U(16.W))

  val GATEOUT = WireInit(0.U(16.W))


  // IR Decode
  val offset5  = SignExt(IR(4,0),  16)  //imm
  val offset6  = SignExt(IR(5,0),  16)
  val offset9  = SignExt(IR(8,0),  16)  // PC offset
  val offset11 = SignExt(IR(10,0), 16)  // (JSP)
  val offset8  = ZeroExt(IR(7,0),  16)  // interrupt vector (TRAP)

  /********  Mux  ********/
  // lab4-task4
  // 请在下方填写数据通的MUX部件

  // ADDR1MUX

  // ADDR2MUX

  // PCMUX

  // DRMUX

  // SR1MUX

  // SR2MUX

  // SPMUX

  // MARMUX

  // VectorMUX

  // PSRMUX
  
  val ADDR1MUX = Mux(SIG.ADDR1_MUX, regfile.io.r1Data, PC)

  val ADDR2MUX = MuxLookup(SIG.ADDR2_MUX, 0.U, Seq(
    0.U -> 0.U,
    1.U -> offset6,
    2.U -> offset9,
    3.U -> offset11
  ))

  val addrOut = ADDR1MUX + ADDR2MUX

  val PCMUX = MuxLookup(SIG.PC_MUX, RESET_PC, Seq(
    0.U -> Mux(PC===0.U, RESET_PC,  PC + 1.U),
    1.U -> GATEOUT,
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

  val SR2MUX = Mux(IR(5), offset5, regfile.io.r2Data)

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

  /*********** ALU Interface ****************/
  alu.io.ina := regfile.io.r1Data
  alu.io.inb := SR2MUX
  alu.io.op := SIG.ALUK

  /*********** Regfile Interface ****************/
  regfile.io.wen    := SIG.LD_REG  //TODO :CHECK
  regfile.io.wAddr  := DRMUX
  regfile.io.r1Addr := SR1MUX
  regfile.io.r2Addr := IR(2, 0)
  regfile.io.wData  := GATEOUT

  val dstData = WireInit(regfile.io.wData)

  /*********** Memory ****************/

  // address control logic that convered by truth table

  //val MEM_EN = SIG.MIO_EN && MAR < 0xfe00.U
  val MEM_RD = SIG.MIO_EN && !SIG.R_W
  val MEM_EN = SIG.MIO_EN && (MAR < 0xfe00.U)

  val IN_MUX = MuxCase(io.mem.rdata, Array(
    (MEM_RD && (MAR === 0xfe00.U)) -> KBSR,
    (MEM_RD && (MAR === 0xfe02.U)) -> KBDR,
    (MEM_RD && (MAR === 0xfe04.U)) -> DSR,
    (MEM_EN && !SIG.R_W) -> io.mem.rdata
    ))

  // UART Input
  io.uartRx.ready := !KBSR(15).asBool
  when(io.uartRx.fire) {
    KBDR := Cat(0.U(8.W), io.uartRx.bits)
    KBSR := Cat(1.U(1.W), 0.U(15.W))
  }

  val LD_KBSR = (MAR === 0xfe00.U) && SIG.MIO_EN && SIG.R_W
  val LD_DSR  = (MAR === 0xfe04.U) && SIG.MIO_EN && SIG.R_W
  val LD_DDR  = (MAR === 0xfe06.U) && SIG.MIO_EN && SIG.R_W

  // UART Output
  // io.uartTx.valid := DSR(15).asBool

  DSR := Cat(io.uartTx.ready, 0.U(15.W))
  io.uartTx.valid := RegNext(LD_DDR)
  io.uartTx.bits  := DDR(7, 0)

  io.mem.raddr   := MAR
  io.mem.waddr   := MAR
  io.mem.wdata  := MDR
  io.mem.wen    := SIG.MIO_EN && SIG.R_W
  io.mem.mio_en := SIG.MIO_EN

  /*************  Gate *************/
  // Lab4-task5
  // 编写八选一逻辑 根据 Gate*信号，从八个数据中选出一个


  val GateSig = Cat(Seq(
    SIG.GATE_PC,
    SIG.GATE_MDR,
    SIG.GATE_ALU,
    SIG.GATE_MARMUX,
    SIG.GATE_VECTOR,
    SIG.GATE_PC1,
    SIG.GATE_PSR,
    SIG.GATE_SP
  ).reverse)

  GATEOUT := Mux1H(GateSig, Seq(
    PC,
    MDR,
    alu.io.out,
    MARMUX,
    Cat(1.U(8.W), 0.U),
    PC - 1.U,
    Cat(Seq(0.U(13.W),PSRMUX)),
    SPMUX
  ))


  /********  LD  ********/
  // lab4-task6
  // 根据LD条件编写寄存器值更改的逻辑

  // SIG.LD_MAR

  // SIG.LD_MDR

  // SIG.LD_IR

  // SIG.LD_BEN

  // SIG.LD_PC 此信号需要 || time === 0.U

  // SIG.LD_CC(N Z P)


  when(SIG.LD_MAR) { MAR := GATEOUT }
  when(SIG.LD_MDR) { MDR := Mux(SIG.MIO_EN, IN_MUX, GATEOUT) }

  when(SIG.LD_IR)  { IR  := MDR }
  when(SIG.LD_BEN) { BEN := IR(11) && N || IR(10) && Z || IR(9) && P }
  when(SIG.LD_PC || time === 0.U)  { PC := PCMUX }

  when(SIG.LD_CC) {
    N := dstData(15)
    Z := !dstData.orR()
    P := !dstData(15) && dstData.orR()
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

  // Stop LC3 when program run end
  val PRE_IR = RegNext(IR)
  val END = RegInit(false.B)
  io.end := END
  when(IR === 0.U && PRE_IR =/= 0.U) {
    END := true.B
  }
}

