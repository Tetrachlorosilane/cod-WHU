// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog 测试平台：code/soc_verify/soc_bram/testbench/mycpu_tb.v（与 exp7~exp11 相同）
//
// 判据复现逻辑在共享库 envlib.test.TraceHarness。
// exp12 的 func 覆盖 n1~n47（含 syscall 例外与 CSR 指令）。

package exp12.test

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import exp12.soc._
import envlib.test._

class MyCpuTbSpec extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "SocLiteTop + MyCpuTop (exp12, syscall 例外支持)"

  it should "debug trace 与 gettrace/golden_trace.txt 逐条一致" in {
    val (instInit, dataInit) =
      TraceHarness.loadInit("../code/func/obj/inst_ram.mif", "../code/func/obj/data_ram.mif")

    test(new SocLiteTop(instInit, dataInit, simulation = true)) { dut =>
      TraceHarness.run(dut, "../code/gettrace/golden_trace.txt")
    }
  }
}
