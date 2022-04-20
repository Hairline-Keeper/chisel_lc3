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

  val SP = 6.U(3.W)
  val R7 = 7.U(3.W)

  // 初始化
  val PC  = RegInit("h3000".U(16.W)) // TODO: Maybe the PC can be dynamically specified by the image
  val RESET_PC  = RegInit("h3000".U(16.W))
  when(io.initPC.valid) {
    PC := io.initPC.bits
    RESET_PC := io.initPC.bits
  }
  val IR  = RegInit(0.U(16.W))
  val MAR = RegInit(0.U(16.W))
  val MDR = RegInit(0.U(16.W))
  val PSR = RegInit(0.U(16.W))

  val KBDR = RegInit(0.U(16.W))
  val KBSR = RegInit(0.U(16.W))
  val DDR  = RegInit(0.U(16.W))
  val DSR  = RegInit(0.U(16.W))

  val BEN = RegInit(false.B)
  val N = RegInit(false.B)
  val P = RegInit(false.B)
  val Z = RegInit(true.B)

  val ADDR1MUX  = Wire(UInt(16.W))
  val ADDR2MUX  = Wire(UInt(16.W))

  val PCMUX     = Wire(UInt(16.W))
  val DRMUX     = Wire(UInt(16.W))
  val SR1MUX    = Wire(UInt(16.W))
  val SR2MUX    = Wire(UInt(16.W))
  val SPMUX     = Wire(UInt(16.W))
  val MARMUX    = Wire(UInt(16.W))
  val VectorMUX = Wire(UInt(16.W))
  val PSRMUX    = Wire(UInt(16.W))
  val addrOut   = Wire(UInt(16.W))
  val aluOut    = Wire(UInt(16.W))
  val GATEOUT   = WireInit(0.U(16.W))
  val r1Data    = WireInit(0.U(16.W))
  val r2Data    = WireInit(0.U(16.W))

  // IR Decode
  val offset5  = SignExt(IR(4,0),  16)  //imm
  val offset6  = SignExt(IR(5,0),  16)
  val offset9  = SignExt(IR(8,0),  16)  // PC offset
  val offset11 = SignExt(IR(10,0), 16)  // (JSP)
  val offset8  = ZeroExt(IR(7,0),  16)  // interrupt vector (TRAP)

  /********  Mux  ********/

  ADDR1MUX := Mux(SIG.ADDR1_MUX, r1Data, PC)

  ADDR2MUX := MuxLookup(SIG.ADDR2_MUX, 0.U, Seq(
    0.U -> 0.U,
    1.U -> offset6,
    2.U -> offset9,
    3.U -> offset11
  ))

  addrOut := ADDR1MUX + ADDR2MUX

  PCMUX := MuxLookup(SIG.PC_MUX, RESET_PC, Seq(
    0.U -> Mux(PC===0.U, RESET_PC,  PC + 1.U),
    1.U -> GATEOUT,
    2.U -> addrOut
  ))

  // 实验五-任务一：选择器连接

  DRMUX :=

  SR1MUX :=

  SR2MUX :=

  SPMUX :=

  MARMUX := Mux(SIG.MAR_MUX, addrOut, offset8)

  VectorMUX := MuxLookup(SIG.VECTOR_MUX, 0.U, Seq(  // TODO: Interrupt
    0.U -> 0.U,
    1.U -> 0.U,
    2.U -> 0.U
  ))

  PSRMUX := 0.U

  /*********** ALU Interface ****************/
  val alu = Module(new ALU)
  alu.io.ina := r1Data
  alu.io.inb := SR2MUX
  alu.io.op := SIG.ALUK
  aluOut := alu.io.out

  /*********** Regfile Interface ****************/
  // 实验五-任务二：寄存器堆例化与端口连接
  val regfile =

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
  DSR := Cat(io.uartTx.ready, 0.U(15.W))
  io.uartTx.valid := RegNext(LD_DDR)
  io.uartTx.bits  := DDR(7, 0)

  io.mem.raddr   := MAR
  io.mem.waddr   := MAR
  io.mem.wdata  := MDR
  io.mem.wen    := SIG.MIO_EN && SIG.R_W
  io.mem.mio_en := SIG.MIO_EN


  // 实验五-任务三：连接总线


  /********  LD  ********/

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

