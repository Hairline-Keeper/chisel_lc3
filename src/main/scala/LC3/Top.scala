package LC3

import chisel3._
import chisel3.util._

object CoreConfig {
  val FPGAPlatform = true
  println("FPGAPlatform = " + FPGAPlatform)
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

    // dataPath.io.uartRx.bits   := uartRx.io.channel.bits
    // dataPath.io.uartRx.valid  := uartRx.io.channel.valid
    // uartRx.io.channel.ready   := dataPath.io.uartRx.ready

    // uartTx.io.channel.bits    := dataPath.io.uartTx.bits
    // uartTx.io.channel.valid   := dataPath.io.uartTx.valid
    // dataPath.io.uartTx.ready  := uartTx.io.channel.ready

    // boot.io.uartRx.valid := uartRx.io.channel.valid
    // boot.io.uartRx.bits := uartRx.io.channel.bits
  } else {
    val uart = Module(new UARTHelper)
    uart.io.clk := clock

    dataPath.io.uartRx.bits  := uart.io.recvData
    dataPath.io.uartRx.valid := uart.io.recvData_valid
    uart.io.recvData_ready   := dataPath.io.uartRx.ready

    uart.io.sendData := dataPath.io.uartTx.bits
    uart.io.sendData_valid := dataPath.io.uartTx.valid
    dataPath.io.uartTx.ready := uart.io.sendData_ready

    io.uart_txd := true.B

    boot.io.uartRx.valid := false.B
    boot.io.uartRx.bits := DontCare
  }

  controller.io.in <> dataPath.io.out
  controller.io.work := boot.io.work

  dataPath.io.signal <> controller.io.out
  dataPath.io.initPC := boot.io.initPC.bits

  memory.io <> dataPath.io.mem
  memory.io.waddr := Mux(boot.io.work, dataPath.io.mem.waddr, boot.io.initMem.waddr)
  memory.io.wdata := Mux(boot.io.work, dataPath.io.mem.wdata, boot.io.initMem.wdata)
  memory.io.wen := Mux(boot.io.work, dataPath.io.mem.wen, boot.io.initMem.wen)

  // io.uart_txd := io.uart_rxd
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
