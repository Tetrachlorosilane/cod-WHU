// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog 测试平台：code/soc_verify/soc_hs_bram/testbench/mycpu_tb.v
//
// 判据复现逻辑在共享库 envlib.test.TraceHarness（与 exp7~exp13 相同）。

package exp14.test

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import exp14.soc._
import envlib.test._

class MyCpuTbSpec extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "SocLiteTop + MyCpuTop (exp14, 握手类 SRAM 总线)"

  it should "debug trace 与 gettrace/golden_trace.txt 逐条一致" in {
    val (instInit, dataInit) =
      TraceHarness.loadInit("../code/func/obj/inst_ram.mif", "../code/func/obj/data_ram.mif")

    test(new SocLiteTop(instInit, dataInit, simulation = true)) { dut =>
      TraceHarness.run(dut, "../code/gettrace/golden_trace.txt")
    }
  }
}
