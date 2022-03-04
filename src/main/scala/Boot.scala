package LC3

import chisel3._
import chisel3.util._

class Boot extends Module with HasUartCst{
  val io = IO(new Bundle() {
    val uartRx = Flipped(DecoupledIO(UInt(8.W)))

    val work    = Output(Bool())
    val initPC  = ValidIO(UInt(16.W))
    val initMem = Flipped(new MemIO)
  })

  val uartValid = io.uartRx.valid
  val uartBits = io.uartRx.bits
  io.uartRx.ready := true.B

  val timeCount = RegInit(0.U(log2Up(16*((frequency/baudRate) - 1)).W))
  timeCount := Mux(uartValid, 0.U, timeCount + 1.U)

  val inTransStart = RegInit(false.B)
  inTransStart := inTransStart | uartValid
  
  val timeOut = inTransStart && (timeCount > (((frequency/baudRate) - 1)*16).U)

  val memAddr     = Reg(UInt(16.W))
  val secondValid = RegInit(init = false.B)     // receive uart data twice and connect the data to 16bits
  val firstData   = RegEnable(uartBits, uartValid)
  val wordData    = Cat(firstData, uartBits)    // (secondData, firstData)
  val wordValid   = second && uartValid

  when(uartValid){ second := !second }
  when(wordValid){ printf("wordValid: %x\n", wordData) }

  val initpc :: initmem :: work :: Nil = Enum(3)
  val lc3State = RegInit(initpc)
  when(reset.asBool()){ lc3State := initpc }

  // lab5-task1
  // 在此编写状态转换
  

  // control memory
  io.initPC.valid := (lc3State === initpc) && wordValid
  io.initPC.bits := wordData

  io.initMem <> DontCare
  io.initMem.wen := (lc3State === initmem) && wordValid
  io.initMem.wdata := wordData
  io.initMem.waddr := memAddr

  io.work := (lc3State === work)
}
