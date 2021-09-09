package LC3

import chisel3._
import chisel3.util._

object CoreConfig {
  val FPGAPlatform = false
  val REPLACE_MEM = false
  println("FPGAPlatform = " + FPGAPlatform)
  println("REPLACE_MEM = " + REPLACE_MEM)
}

class Top extends Module{
  val io = IO(new UARTBundle)
  dontTouch(io)

  val boot = Module(new Boot)
  val controller = Module(new Controller)
  val dataPath = Module(new DataPath)
  val memory = Module(new Memory)

  if(CoreConfig.FPGAPlatform) {
    val uartRx = Module(new UartRX)
    val uartTx = Module(new BufferedUartTX)

    uartRx.io.rxd := io.uart_rxd
    io.uart_txd   := uartTx.io.txd
    
    when(boot.io.work) {
      dataPath.io.uartRx <> uartRx.io.channel
      boot.io.uartRx <> DontCare
      boot.io.uartRx.valid := false.B
    }.otherwise {
      dataPath.io.uartRx <> DontCare
      dataPath.io.uartRx.valid := false.B
      boot.io.uartRx <> uartRx.io.channel
    }

    uartTx.io.channel <> dataPath.io.uartTx
    controller.io.end := dataPath.io.end
  } else {
    io.uart_txd := true.B

    val uartRx = Module(new UartRX)
    val uartTx = Module(new BufferedUartTX)
    
    val soc_uartTx = Module(new SOC_UartTx)
    val soc_uartRx = Module(new SOC_UartRx)
    
    soc_uartTx.io.clk := clock
    soc_uartRx.io.clk := clock

    uartRx.io.rxd := soc_uartTx.io.txd
    soc_uartRx.io.rxd := uartTx.io.txd
    
    when(boot.io.work) {
      dataPath.io.uartRx <> uartRx.io.channel
      boot.io.uartRx <> DontCare
      boot.io.uartRx.valid := false.B
    }.otherwise {
      dataPath.io.uartRx <> DontCare
      dataPath.io.uartRx.valid := false.B
      boot.io.uartRx <> uartRx.io.channel
    }

    uartTx.io.channel <> dataPath.io.uartTx
    
    controller.io.end := dataPath.io.end
  }

  controller.io.in <> dataPath.io.out
  controller.io.work := boot.io.work

  dataPath.io.signal <> controller.io.out
  dataPath.io.initPC <> boot.io.initPC

  memory.io <> dataPath.io.mem
  memory.io.waddr := Mux(boot.io.work, dataPath.io.mem.waddr, boot.io.initMem.waddr)
  memory.io.wdata := Mux(boot.io.work, dataPath.io.mem.wdata, boot.io.initMem.wdata)
  memory.io.wen := Mux(boot.io.work, dataPath.io.mem.wen, boot.io.initMem.wen)

  boot.io.initMem.rdata := DontCare
  boot.io.initMem.R := DontCare

  dontTouch(controller.io)
  dontTouch(dataPath.io)
  dontTouch(memory.io)
  dontTouch(boot.io)
}

object SimMain extends App {
  Driver.execute(args, () => new Top)
}
