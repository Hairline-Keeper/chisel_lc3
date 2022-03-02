package LC3

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

class RegfileTest extends AnyFlatSpec
  with ChiselScalatestTester
{
  // Lab4-task3 
  // 编写Regfile读写测试，即对某个寄存器进行写操作，再读该寄存器进行读操作，对比写入和读出数据是否一样。

}


// 参考实现，尝试作更多方面验证

// class RegfileTest extends AnyFlatSpec
//   with ChiselScalatestTester
// {
//   behavior of "Regfile"
  
//   def TEST_SIZE = 10

//   val data1, data2, addr1, addr2 = Array.fill(TEST_SIZE)(0)

//   for (i <- 0 until TEST_SIZE) {
//     data1(i) = Random.nextInt(0xffff)
//     data2(i) = Random.nextInt(0xffff)
//     addr1(i) = Random.nextInt(7)
//     addr2(i) = Random.nextInt(7)
//   }

//   // 硬件部分
//   it should "test r/w" in {
//     test(new Regfile) { c =>
//       println(s"*******regfile read/write test********")
//       for(i <- 0 until TEST_SIZE) {
//         c.io.wData.poke(data1(i).U)
//         c.io.wAddr.poke(addr1(i).U)
//         c.io.wen.poke(true.B)
//         c.clock.step()

//         c.io.r1Addr.poke(addr1(i).U)
//         c.io.wen.poke(false.B)
//         c.io.r1Data.expect(data1(i).U(15,0))
//         c.clock.step()

//         c.io.wData.poke(data2(i).U)
//         c.io.wAddr.poke(addr2(i).U)
//         c.io.wen.poke(true.B)
//         c.clock.step()
        
//         c.io.r2Addr.poke(addr2(i).U)
//         c.io.wen.poke(false.B)
//         c.io.r2Data.expect(data2(i).U(15,0))
//         c.clock.step()
//       }
//     }
//   }
// }

