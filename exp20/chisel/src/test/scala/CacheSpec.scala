// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 对应原 Verilog 测试平台：code/testbench/testbench.v（对 cache_top 的仿真驱动）
//
// 原 tb_top 的判据（每个时钟沿检查）：
//   * `read_round_finish` 有效 → 打印 "index %x finished"；当 `test_index == 8'hff` 时
//     打印 "Test end! / ----PASS!!!" 并结束；
//   * `replace_wrong` 有效 → 打印 "replace wrong at index %x" + "----FAIL!!!" 并结束；
//   * `cacheres_wrong` 有效 → 打印 "cacheres wrong at index %x" + "----FAIL!!!" 并结束。
//
// Chisel 版把这些变成断言 + 进度打印。
// ============================================================================

package exp20.test

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import exp20.soc._

class CacheSpec extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "CacheTop (exp20, Cache 模块级验证)"

  it should "每个 index 先写后读全部正确，直到 index = 0xff（对应原实验 ----PASS!!!）" in {
    test(new CacheTop(simulation = true)) { dut =>
      dut.io.switch.poke(0xff.U)
      dut.io.resetn.poke(false.B)
      dut.clock.step(8)
      dut.io.resetn.poke(true.B)

      var cycles    = 0
      val maxCycles = 20000000
      var lastIdx   = -1

      def pass: Boolean = dut.io.testIndex.peek().litValue.toInt == 0xff &&
                          dut.io.roundFinish.peek().litToBoolean

      while (!pass && cycles < maxCycles) {
        assert(!dut.io.replaceWrong.peek().litToBoolean,
               s"replace wrong at index ${dut.io.testIndex.peek().litValue.toInt.toHexString}")
        assert(!dut.io.cacheresWrong.peek().litToBoolean,
               s"cacheres wrong at index ${dut.io.testIndex.peek().litValue.toInt.toHexString}")

        if (dut.io.roundFinish.peek().litToBoolean) {
          val idx = dut.io.testIndex.peek().litValue.toInt
          if (idx != lastIdx) {
            lastIdx = idx
            println(f"index $idx%02x finished")
          }
        }
        dut.clock.step()
        cycles += 1
      }

      assert(dut.io.roundFinish.peek().litToBoolean, s"$cycles 个周期内未完成全部 index")
      assert(dut.io.testIndex.peek().litValue.toInt == 0xff,
             s"最终 index = ${dut.io.testIndex.peek().litValue.toInt.toHexString}（期望 0xff）")
      println("=========================================================")
      println("Test end!")
      println("----PASS!!!")
    }
  }
}
