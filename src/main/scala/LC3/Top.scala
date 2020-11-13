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
    val uartRx = Module(new BufferedUartTX)
    val uartTx = Module(new UartTX)
  } else {
    val uart = Module(new UARTHelper)
    uart.io.clk := clock

    uart.io.sendData := 0.U
    uart.io.sendData_valid := false.B

    dataPath.io.uartRx.bits  := uart.io.recvData
    dataPath.io.uartRx.valid := uart.io.recvData_valid
    uart.io.recvData_ready   := dataPath.io.uartRx.ready

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