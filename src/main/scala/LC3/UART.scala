package LC3

import chisel3._
import chisel3.util._

class UARTBundle extends Bundle {
  val uart_rxd = Input(Bool())
  val uart_txd = Output(Bool())
}

class UartRecv extends BlackBox(Map("CLK_FREQ" -> 50000000, "UART_BPS" -> 9600)) {
  val io = IO(new Bundle {
    val sys_clk = Input(Clock())
    val sys_rst_n = Input(Bool())

    val uart_rxd = Input(Bool())
    val uart_done = Output(Bool())
    val uart_data = Output(UInt(8.W))
  })
}

class UartSend extends BlackBox(Map("CLK_FREQ" -> 50000000, "UART_BPS" -> 9600)) {
  val io = IO(new Bundle {
    val sys_clk = Input(Clock())
    val sys_rst_n = Input(Bool())

    val uart_en = Input(Bool())
    val uart_din = Input(UInt(8.W))
    val uart_tx_busy = Output(Bool())
    val uart_txd = Output(Bool())
  })
}

class UartLoop extends BlackBox {
  val io = IO(new Bundle {
    val sys_clk = Input(Clock())
    val sys_rst_n = Input(Bool())
    
    val recv_done = Input(Bool())
    val recv_data = Input(UInt(8.W))
    val tx_busy = Input(Bool())
    val send_en = Output(Bool())
    val send_data = Output(UInt(8.W))
  })
}