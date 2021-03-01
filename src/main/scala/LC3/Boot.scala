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

  val uartDone = io.uartRx.valid
  val uartData = io.uartRx.bits
  io.uartRx.ready := true.B

  val outTimeCnt = RegInit(0.U(log2Up((frequency/baudRate) - 1).W))
  val inTransStart = RegInit(false.B)

  when(uartDone) {
    outTimeCnt := 0.U
  }.otherwise {
    outTimeCnt := outTimeCnt + 1.U
  }

  when(uartDone && !inTransStart) { inTransStart := true.B }

  def isOutTime(cnt: UInt, inTransStart: Bool): Bool = {
    Mux(inTransStart,
      cnt > ((frequency/baudRate) - 1).U,
      false.B)
  }

  val initpc :: initmem :: work :: Nil = Enum(3)
  val lc3State = RegInit(initpc)
  when(reset.asBool()){ lc3State := initpc }

  val memAddr = Reg(UInt(16.W))
  val second    = RegInit(init = false.B)     // receive uart data twice and connect the data to 16bits
  val firstData = RegNext(uartData, uartDone)
  val fullData  = Cat(uartData, firstData)    // (secondData, firstData)
  val fullDone  = second && uartDone

  when(uartDone){ second := !second }

  when(lc3State === initpc && fullDone){
    memAddr := fullData
    lc3State := initmem  }

  when(lc3State === initmem && fullDone){
    memAddr := memAddr + 1.U
    when(isOutTime(outTimeCnt, inTransStart)){
      lc3State := work
    }
  }

  if(CoreConfig.FPGAPlatform) {
    io.initPC.valid := (lc3State === initpc) && fullDone
    io.initPC.bits := fullData

    io.initMem <> DontCare
    io.initMem.wen := (lc3State === initmem) && fullDone
    io.initMem.wdata := fullData 
    io.initMem.waddr := memAddr

    io.work := (lc3State === work)
  }else {
    io.initPC.valid := true.B
    io.initPC.bits := "h3000".U(16.W)

    io.initMem <> DontCare
    io.initMem.wen := false.B

    io.work := true.B
  }
}
