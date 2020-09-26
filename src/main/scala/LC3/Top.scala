package LC3

import chisel3._
import chisel3.util._

class Top extends Module {
  val io = IO(new Bundle {})

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