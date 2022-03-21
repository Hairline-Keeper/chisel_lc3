package detection

import chisel3._
import chisel3.util._
// import chisel3.experimental._

// RawModule与Module不同，它不会生成隐式的时钟，由于我们是组合电路，因此现在还没有涉及到时钟的概念，只用RawModule即可
class Detection extends Module {
    val io = IO(new Bundle{
        val in = Input(Bool()) // 输入的0-7信号，二进制表示只需要3bits宽即可
        val out = Output(Bool()) // 输出的译码后信号，8bits宽
    })

    val S0 = 0.U(3.W) // 0
    val S1 = 1.U(3.W) // 1
    val S2 = 2.U(3.W) // 11
    val S3 = 3.U(3.W) // 110

    val stat = RegInit(0.U(3.W))

    switch(stat) {
        is (S0) { when(io.in) {stat := S1} .otherwise {stat := S0} }
        is (S1) { when(io.in) {stat := S2} .otherwise {stat := S0} }
        is (S2) { when(io.in) {stat := S2} .otherwise {stat := S3} }
        is (S3) { when(io.in) {stat := S1} .otherwise {stat := S0} }
    }

    printf(p"in = ${io.in}, out = ${io.out}\n")
    io.out := stat === S3 && io.in
}

object testMain extends App {
    Driver.execute(args, () => new Detection)
}
