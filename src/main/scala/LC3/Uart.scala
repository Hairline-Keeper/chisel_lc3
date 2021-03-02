package LC3

import chisel3._
import chisel3.util._

trait HasUartCst {
  val frequency = 50000000
  val baudRate = 115200
}

class UARTBundle extends Bundle {
  val uart_rxd = Input(Bool())
  val uart_txd = Output(Bool())
}

class UARTHelper extends BlackBox {
  val io = IO(new Bundle {
    val clk = Input(Clock())

    val sendData = Input(UInt(8.W))
    val sendData_valid = Input(Bool())
    val sendData_ready = Output(Bool())

    val recvData = Output(UInt(8.W))
    val recvData_valid = Output(Bool())
    val recvData_ready = Input(Bool())
  })
}

class SOC_UartRx extends BlackBox {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val rxd = Input(Bool())
  })
}

class SOC_UartTx extends BlackBox {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val txd = Output(Bool())
  })
}

class UartTX extends Module with HasUartCst {
  val io = IO(new Bundle {
    val txd = Output(Bool())
    val channel = Flipped(DecoupledIO(UInt(8.W)))
  })

  // val BIT_CNT = ((frequency + baudRate / 2) / baudRate - 1).asUInt()
  val BIT_CNT = (frequency/baudRate).U - 1.U

  val shiftReg = RegInit(0x7ff.U)
  val cntReg = RegInit(0.U(20.W))
  val bitsReg = RegInit(0.U(4.W))

  io.channel.ready := (cntReg === 0.U) && (bitsReg === 0.U)
  io.txd := shiftReg(0)

  when(cntReg === 0.U) {

    cntReg := BIT_CNT
    when(bitsReg =/= 0.U) {
      val shift = shiftReg >> 1
      shiftReg := Cat(1.U, shift(9, 0))
      bitsReg := bitsReg - 1.U
    }.otherwise {
      when(io.channel.valid) {
        shiftReg := Cat(Cat(3.U, io.channel.bits), 0.U) // two stop bits, data, one start bit
        bitsReg := 11.U
      }.otherwise {
        shiftReg := 0x7ff.U
      }
    }

  }.otherwise {
    cntReg := cntReg - 1.U
  }
}

/**
  * Receive part of the UART.
  * A minimal version without any additional buffering.
  * Use a ready/valid handshaking.
  *
  * The following code is inspired by Tommy's receive code at:
  * https://github.com/tommythorn/yarvi
  */
class UartRX extends Module with HasUartCst {
  val io = IO(new Bundle {
    val rxd = Input(Bool())
    val channel = DecoupledIO(UInt(8.W))
  })

  val BIT_CNT = (frequency/baudRate).U - 1.U
  val START_CNT = ((3*frequency/baudRate)/2).U

  // Sync in the asynchronous RX data, reset to 1 to not start reading after a reset
  val rxReg = RegNext(RegNext(io.rxd, 1.U), 1.U)

  val shiftReg = RegInit(0.U(8.W))
  val cntReg = RegInit(0.U(20.W))
  val bitsReg = RegInit(0.U(4.W))
  val valReg = RegInit(false.B)

  when(cntReg =/= 0.U) {
    cntReg := cntReg - 1.U
  }.elsewhen(bitsReg =/= 0.U) {
    cntReg := BIT_CNT
    shiftReg := Cat(rxReg, shiftReg >> 1)
    bitsReg := bitsReg - 1.U
    // the last shifted in
    when(bitsReg === 1.U) {
      valReg := true.B
    }
  }.elsewhen(rxReg === 0.U) { // wait 1.5 bits after falling edge of start
    cntReg := START_CNT
    bitsReg := 8.U
  }

  when(valReg && io.channel.ready) {
    valReg := false.B
  }

  io.channel.bits := shiftReg
  io.channel.valid := valReg
}

/**
  * A single byte buffer with a ready/valid interface
  */
class Buffer extends Module {
  val io = IO(new Bundle {
    val in = Flipped(DecoupledIO(UInt(8.W)))
    val out = DecoupledIO(UInt(8.W))
  })

  val empty :: full :: Nil = Enum(2)
  val stateReg = RegInit(empty)
  val dataReg = RegInit(0.U(8.W))

  io.in.ready := stateReg === empty
  io.out.valid := stateReg === full

  when(stateReg === empty) {
    when(io.in.valid) {
      dataReg := io.in.bits
      stateReg := full
    }
  }.otherwise { // full
    when(io.out.ready) {
      stateReg := empty
    }
  }
  io.out.bits := dataReg
}

/**
  * A transmitter with a single buffer.
  */
class BufferedUartTX extends Module {
  val io = IO(new Bundle {
    val txd = Output(Bool())
    val channel = Flipped(DecoupledIO(UInt(8.W)))
  })
  val tx = Module(new UartTX)
  val buf = Module(new Buffer())

  buf.io.in <> io.channel
  tx.io.channel <> buf.io.out
  io.txd <> tx.io.txd
}

/**
  * Send a string.
  */
class Sender extends Module {
  val io = IO(new Bundle {
    val txd = Output(Bool())
  })

  val tx = Module(new BufferedUartTX)

  io.txd := tx.io.txd

  val msg = "Hello World!"
  val text = VecInit(msg.map(_.U))
  val len = msg.length.U

  val cntReg = RegInit(0.U(8.W))

  tx.io.channel.bits := text(cntReg)
  tx.io.channel.valid := cntReg =/= len

  when(tx.io.channel.ready && cntReg =/= len) {
    when(cntReg === len - 1.U) {
      cntReg := 0.U
    }.otherwise {
      cntReg := cntReg + 1.U
    }
  }
}

class Echo extends Module {
  val io = IO(new Bundle {
    val txd = Output(Bool())
    val rxd = Input(Bool())
  })
  // io.txd := RegNext(io.rxd)
  val tx = Module(new BufferedUartTX)
  val rx = Module(new UartRX)
  io.txd := tx.io.txd
  rx.io.rxd := io.rxd
  tx.io.channel <> rx.io.channel
}