package LC3

import chisel3._
import chisel3.util._

class Boot extends Module {
  val io = IO(new Bundle() {
    val uart_rxd = Input(Bool())

    val work    = Output(Bool())
    val initPC  = ValidIO(UInt(16.W))
    val initMem = ValidIO(new MemIO)
  })

  val uartRecv = Module(new UartRecv)
  val uartData = uartRecv.io.uart_data
  val uartDone = uartRecv.io.uart_done
  uartRecv.io.sys_clk := clock
  uartRecv.io.sys_rst_n := reset.asBool()
  uartRecv.io.uart_rxd := io.uart_rxd

  val initpc :: initmem :: work :: Nil = Enum(3)
  val lc3State = RegInit(initpc)
  when(reset.asBool()){ lc3State := initpc }

  val memAddr = Reg(UInt(16.W))
  val second    = RegInit(init = false.B)     // receive uart data twice and connect the data to 16bits
  val firstData = RegNext(uartData, uartDone)
  val fullData  = Cat(uartData, firstData)    // (secondData, firstData)
  val fullDond  = second && uartDone

  when(uartDone){ second := !second }

  when(lc3State === initpc && fullDond){
    memAddr := fullData
    lc3State := initmem
  }

  when(lc3State === initmem && fullDond){
    memAddr := memAddr + 1.U
    when(uartData === 0x60.U){
      lc3State := work
    }
  }

  io.initPC.valid := (lc3State === initpc) && fullDond
  io.initPC.bits := fullData

  io.initMem <> DontCare
  io.initMem.valid := (lc3State === initmem)
  io.initMem.bits.wea := fullDond
  io.initMem.bits.dina := fullData
  io.initMem.bits.addra := memAddr

  io.work := (lc3State === work)
}
