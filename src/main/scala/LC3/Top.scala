package LC3

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Top extends Module {
  val io = IO(new UARTBundle)

  val uart_recv_done = Wire(Bool())
  val uart_recv_data = Wire(UInt(8.W))
  val uart_send_en = Wire(Bool())
  val uart_send_data = Wire(UInt(8.W))
  val uart_tx_busy = Wire(Bool())

  val uartRecv = Module(new UartRecv)
  val uartSend = Module(new UartSend)
  val uartLoop = Module(new UartLoop)

  uartRecv.io.sys_clk := clock
  uartSend.io.sys_clk := clock
  uartLoop.io.sys_clk := clock
  uartRecv.io.sys_rst_n := reset
  uartSend.io.sys_rst_n := reset
  uartLoop.io.sys_rst_n := reset

  uartRecv.io.uart_rxd := io.uart_rxd
  uart_recv_done := uartRecv.io.uart_done
  uart_recv_data := uartRecv.io.uart_data

  uartSend.io.uart_en := uart_send_en
  uartSend.io.uart_din := uart_send_data
  uart_tx_busy := uartSend.io.uart_tx_busy
  io.uart_txd := uartSend.io.uart_txd

  uartLoop.io.recv_done := uart_recv_done
  uartLoop.io.recv_data := uart_recv_data
  uartLoop.io.tx_busy   := uart_tx_busy
  uart_send_en := uartLoop.io.send_en
  uart_send_data := uartLoop.io.send_data

  val controller = Module(new Controller)
  val dataPath = Module(new DataPath)
  val memory = Module(new Memory)

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