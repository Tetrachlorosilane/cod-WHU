// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog 测试平台：code/soc_verify/soc_bram/testbench/mycpu_tb.v（与 exp7 相同）
//
// 判据复现逻辑在共享库 envlib.test.TraceHarness（见 chisel-common/src/test/scala）。
// exp8 的 func 程序相关指令之间不插 NOP，因此本测试会真正检验阻塞逻辑。

package exp8.test

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import exp8.soc._
import envlib.test._

class MyCpuTbSpec extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "SocLiteTop + MyCpuTop (exp8, 阻塞解决数据相关)"

  it should "debug trace 与 gettrace/golden_trace.txt 逐条一致" in {
    val (instInit, dataInit) =
      TraceHarness.loadInit("../code/func/obj/inst_ram.mif", "../code/func/obj/data_ram.mif")

    test(new SocLiteTop(instInit, dataInit, simulation = true)) { dut =>
      TraceHarness.run(dut, "../code/gettrace/golden_trace.txt")
    }
  }
}
