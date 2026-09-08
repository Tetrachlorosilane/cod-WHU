// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 对应原 Verilog 测试平台：code/testbench/testbench.v（对 tlb_top 的仿真驱动）
//
// 原实验的判据（见原书 9.2.1.1）：仿真中完成 16 次写、16 次读、26 次查操作，
// 全部无误后打印
//     Test end!
//     ----PASS!!!
//
// Chisel 版把它变成断言：TlbTop 的三类测试标志（wOk/rOk/sOk）全部置位且
// 错误标志（err）始终为 0 时打印 ----PASS!!!。
// ============================================================================

package exp17.test

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import exp17.soc._

class TlbSpec extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "TlbTop (exp17, TLB 模块级验证)"

  it should "写 / 读 / 查三类测试全部通过（对应原实验 ----PASS!!!）" in {
    test(new TlbTop(tlbNum = 16, simulation = true)) { dut =>
      dut.io.resetn.poke(false.B)
      dut.clock.step(4)
      dut.io.resetn.poke(true.B)

      var cycles    = 0
      val maxCycles = 200000

      def done: Boolean =
        dut.io.wOk.peek().litToBoolean &&
        dut.io.rOk.peek().litToBoolean &&
        dut.io.sOk.peek().litToBoolean

      while (!done && cycles < maxCycles) {
        assert(!dut.io.err.peek().litToBoolean,
               s"TLB 测试出错（cycle=$cycles）：写=${dut.io.wOk.peek().litToBoolean} " +
                 s"读=${dut.io.rOk.peek().litToBoolean} 查=${dut.io.sOk.peek().litToBoolean}")
        dut.clock.step()
        cycles += 1
      }

      assert(dut.io.wOk.peek().litToBoolean, "16 次写测试未通过")
      assert(dut.io.rOk.peek().litToBoolean, "16 次读测试未通过")
      assert(dut.io.sOk.peek().litToBoolean, "26 次查找测试未通过")
      assert(!dut.io.err.peek().litToBoolean, "存在比对错误")
      println(s"----PASS!!! TLB 写/读/查测试全部通过（$cycles 个周期）")
    }
  }
}
