// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog 测试平台：code/soc_verify/soc_bram/testbench/mycpu_tb.v（与 exp7/exp8 相同）
//
// 判据复现逻辑在共享库 envlib.test.TraceHarness（见 chisel-common/src/test/scala）。
// exp9 的 func 程序用于检验前递：相关指令紧邻，理想情况下流水线不停顿也能得到正确结果。

package exp9.test

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import exp9.soc._
import envlib.test._

class MyCpuTbSpec extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "SocLiteTop + MyCpuTop (exp9, 前递解决数据相关)"

  it should "debug trace 与 gettrace/golden_trace.txt 逐条一致" in {
    val (instInit, dataInit) =
      TraceHarness.loadInit("../code/func/obj/inst_ram.mif", "../code/func/obj/data_ram.mif")

    test(new SocLiteTop(instInit, dataInit, simulation = true)) { dut =>
      TraceHarness.run(dut, "../code/gettrace/golden_trace.txt")
    }
  }
}
