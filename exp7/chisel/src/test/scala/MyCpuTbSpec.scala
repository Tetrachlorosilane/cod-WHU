// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 对应原 Verilog 测试平台：code/soc_verify/soc_bram/testbench/mycpu_tb.v（tb_top）
//
// 判据复现逻辑集中在共享库 envlib.test.TraceHarness（exp7~exp23 共用）：
//   1. 每个时钟沿，若 debug_wb_rf_we 非 0 且 wnum != 0 且未结束，
//      与 gettrace/golden_trace.txt 的下一行比对 PC / wnum / wdata（字节使能掩码）；
//   2. debug_wb_pc == 32'h1c000100（END_PC）时结束并打印 ----PASS!!!；
//   3. 监视 confreg.num_data（功能测试点）：低 8 位 +1、高 8 位 +1。
//
// 本实验的 myCPU 需要学生从零实现；未实现（骨架含 ???）时 elaboration 即失败，
// 实现完成后本测试才可能通过。
// ============================================================================

package exp7.test

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import exp7.soc._
import envlib.test._

class MyCpuTbSpec extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "SocLiteTop + MyCpuTop (exp7, 五级流水线 CPU)"

  it should "debug trace 与 gettrace/golden_trace.txt 逐条一致" in {
    val (instInit, dataInit) =
      TraceHarness.loadInit("../code/func/obj/inst_ram.mif", "../code/func/obj/data_ram.mif")

    test(new SocLiteTop(instInit, dataInit, simulation = true)) { dut =>
      TraceHarness.run(dut, "../code/gettrace/golden_trace.txt")
    }
  }
}
