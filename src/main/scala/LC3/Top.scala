package LC3

import chisel3._
import chisel3.util._

class Top extends Module {
  val io = IO(new Bundle {})

  val controller = new Controller
  val dataPath = new DataPath
  val memory = new Memory

  dataPath.io.signal <> controller.io.out
  memory.io <> dataPath.io.mem
}
