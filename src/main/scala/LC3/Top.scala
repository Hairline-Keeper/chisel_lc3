package LC3

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Top extends Module {
  val io = IO(new UARTBundle)

  val setpc :: init :: work :: Nil = Enum(3)
  val lc3State = RegInit(setpc)
  when(reset.asBool()){ lc3State := setpc }

  val pos = RegInit(false.B)

  val PC = Reg(UInt(16.W))

  val offset = RegInit(0.U)
  val lowData = Reg(UInt(8.W))

  val controller = Module(new Controller)
  val dataPath = Module(new DataPath)
  val memory = Module(new Memory)

  controller.io.in <> dataPath.io.out
  controller.io.work := (lc3State === work)

  dataPath.io.signal <> controller.io.out
  dataPath.io.initPC := PC

  memory.io <> dataPath.io.mem

  val uartRecv = Module(new UartRecv)
  val uartData = uartRecv.io.uart_data
  val uartDone = uartRecv.io.uart_done
  uartRecv.io.sys_clk := clock
  uartRecv.io.sys_rst_n := reset
  uartRecv.io.uart_rxd := io.uart_rxd

  
  when(lc3State === setpc){
    when(uartDone){
      pos := !pos
      lowData := Mux(pos, lowData, uartData)
      when(pos){
        lc3State := init
        PC := Cat(uartData, lowData)
      }
    }
  }

  when(lc3State === init && uartDone === true.B){
    pos := !pos
    lowData := Mux(pos, lowData, uartData)
    memory.io.wen := pos //&& uartDone
    memory.io.wdata := Cat(uartData, lowData)
    memory.io.waddr := PC + offset
    offset := Mux(pos, offset + 1.U, offset)
    when(uartData === 0x60.U){
      lc3State := work
    }
  }


  io.uart_txd := io.uart_rxd

  dontTouch(controller.io)
  dontTouch(dataPath.io)
  dontTouch(memory.io)
}

object SimMain extends App {
  Driver.execute(args, () => new Top)
}