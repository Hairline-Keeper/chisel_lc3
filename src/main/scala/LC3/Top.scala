package LC3

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Top extends Module {
  val io = IO(new UARTBundle)

  val boot = Module(new Boot)
  val controller = Module(new Controller)
  val dataPath = Module(new DataPath)
  val memory = Module(new Memory)

  controller.io.in <> dataPath.io.out
  controller.io.work := boot.io.work

  dataPath.io.signal <> controller.io.out
  dataPath.io.initPC := boot.io.initPC.bits
  dataPath.io.initMem <> boot.io.initMem

  memory.io <> dataPath.io.mem


  boot.io.uart_rxd := io.uart_rxd
  io.uart_txd := io.uart_rxd

//  dontTouch(controller.io)
//  dontTouch(dataPath.io)
//  dontTouch(memory.io)
}

object SimMain extends App {
  Driver.execute(args, () => new Top)
}