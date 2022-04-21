package LC3

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class IOMaptest extends AnyFlatSpec
  with ChiselScalatestTester
{
  behavior of "DataPath"

  // 输入测试用例
  var mar_seq = Array.fill[String](4)("hfe00") ++
                Array.fill[String](4)("hfe02") ++
                Array.fill[String](4)("hfe04") ++
                Array.fill[String](4)("hfe06") ++
                Array.fill[String](4)("h3000") ++
                Array.fill[String](4)("hffff")



  var mio_en_seq = Array(1,0,1,0)
  var r_w_seq = Array(1,1,0,0)

  // 输出参考结果
  val r_kbsr_res = Array.fill[Bool](mar_seq.length)(false.B)
  r_kbsr_res(2) = true.B
  val r_kbdr_res = Array.fill[Bool](mar_seq.length)(false.B)
  r_kbdr_res(6) = true.B
  val r_dsr_res = Array.fill[Bool](mar_seq.length)(false.B)
  r_dsr_res(10) = true.B
  val r_mem_res = Array.fill[Bool](mar_seq.length)(false.B)
  r_mem_res(18) = true.B

  val w_kbsr_res = Array.fill[Bool](mar_seq.length)(false.B)
  w_kbsr_res(0) = true.B
  val w_dsr_res = Array.fill[Bool](mar_seq.length)(false.B)
  w_dsr_res(8) = true.B
  val w_ddr_res = Array.fill[Bool](mar_seq.length)(false.B)
  w_ddr_res(12) = true.B

  it should "test IOMap" in {
    test(new IOMap) { c =>
      for (i <- 0 until mar_seq.length) {
      
        // 为IOMap模块注入测试用例
        c.io.mar.poke(mar_seq(i).U)
        c.io.mio_en.poke(mio_en_seq(i%4).B)
        c.io.r_w.poke(r_w_seq(i%4).B)

        // 检查IOMap输出是否与参考结果相同
        c.io.r_kbsr.expect(r_kbsr_res(i))
        c.io.r_kbdr.expect(r_kbdr_res(i))
        c.io.r_dsr.expect(r_dsr_res(i))
        c.io.r_mem.expect(r_mem_res(i))

        c.io.w_kbsr.expect(w_kbsr_res(i))
        c.io.w_dsr.expect(w_dsr_res(i))
        c.io.w_ddr.expect(w_ddr_res(i))

        // println(s"en:${mio_en_seq(i%4)}, r_w:${r_w_seq(i%4)}, mar:${mar_seq(i)}, out:${c.io.r_kbsr.peek()}${c.io.r_kbdr.peek()}${c.io.r_dsr.peek()}${c.io.r_mem.peek()} ${c.io.w_kbsr.peek()}${c.io.w_dsr.peek()}${c.io.w_ddr.peek()}")
      }
    }
  }

}
