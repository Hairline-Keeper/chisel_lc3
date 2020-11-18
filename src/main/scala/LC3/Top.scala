package LC3

import chisel3._
import chisel3.util._

object CoreConfig {
  val FPGAPlatform = false
}

class Top extends Module{
  val io = IO(new UARTBundle)

  val controller = Module(new Controller)
  val dataPath = Module(new DataPath)
  val memory = Module(new Memory)

  if(CoreConfig.FPGAPlatform) {
    val uartRx = Module(new UartRX)
    val uartTx = Module(new BufferedUartTX)

    uartRx.io.rxd := io.uart_rxd
    io.uart_txd   := uartTx.io.txd

    dataPath.io.uartRx.bits   := uartRx.io.channel.bits
    dataPath.io.uartRx.valid  := uartRx.io.channel.valid
    uartRx.io.channel.ready   := dataPath.io.uartRx.ready

    uartTx.io.channel.bits    := dataPath.io.uartTx.bits
    uartTx.io.channel.valid   := dataPath.io.uartTx.valid
    dataPath.io.uartTx.ready  := uartTx.io.channel.ready
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
  }

  controller.io.in <> dataPath.io.out

  dataPath.io.signal <> controller.io.out
  memory.io <> dataPath.io.mem

  dontTouch(controller.io)
  dontTouch(dataPath.io)
  dontTouch(memory.io)
}

object SimMain extends App {
  Driver.execute(args, () => new Top)
}