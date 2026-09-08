// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog 测试平台：code/soc_verify/soc_axi/testbench/mycpu_tb.v（与 exp15 相同）
//
// 判据复现逻辑在共享库 envlib.test.TraceHarness。
// exp16 的 func 让 confreg 的 ram_random_mask 生效，从而在 AXI 侧产生随机延迟。

package exp16.test

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import exp16.soc._
import envlib.test._

class MyCpuTbSpec extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "SocLiteTop + MyCpuTop (exp16, AXI 随机延迟)"

  it should "debug trace 与 gettrace/golden_trace.txt 逐条一致" in {
    val (instInit, dataInit) =
      TraceHarness.loadInit("../code/func/obj/inst_ram.mif", "../code/func/obj/data_ram.mif")

    test(new SocLiteTop(instInit, dataInit, simulation = true)) { dut =>
      TraceHarness.run(dut, "../code/gettrace/golden_trace.txt")
    }
  }
}
