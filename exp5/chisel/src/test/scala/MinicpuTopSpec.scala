// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog 测试平台：code/soc_verify/testbench/mycpu_tb.v（tb_top）
//
// 原 tb_top：
//     initial begin clk = 1'b0; resetn = 1'b0; #2000; resetn = 1'b1; end
//     always #5 clk=~clk;
//     initial begin switch = ~(8'h4); end      // ← 原实验靠肉眼观察 led
//     soc_mini_top soc_mini(.resetn(resetn), .clk(clk), .led(led), .switch(switch));
//
// 原实验的判据是「人眼看 led」，没有自动化检查。本文件把它变成可断言的形式：
//   func/start.S 的语义（n = ~switch[7:0]）：
//       t0 = 0, t1 = 1, s0 = 0, s1 = 1
//       a0 = 从地址 1024 读入的 ~switch[7:0]      （即 n）
//   loop: t2 = t0 + t1; t0 = t1; t1 = t2; s0 = s0 + 1; if (s0 != a0) goto loop
//       st.w t2 → 地址 1028（led_data）
//   ⇒ 循环执行 n 次后 t2 = t1 = f(n)，其中 f(1)=1, f(2)=2, f(n)=f(n-1)+f(n-2)
//   ⇒ led_data = f(n)，且 soc_mini_top 的 led = ~led_data（低有效）
//
// 因此断言：led === ~f(n) & 0xffff。

package exp5.test

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import exp5.soc._

class MinicpuTopSpec extends AnyFlatSpec with ChiselScalatestTester {

  /** 与 func/start.S 等价的软件参考模型：返回循环 n 次后 st.w 写入的 t2 值。 */
  private def fib(n: Int): BigInt = {
    var t0 = BigInt(0)
    var t1 = BigInt(1)
    var i  = 0
    while (i < n) {
      val t2 = t0 + t1
      t0 = t1
      t1 = t2
      i += 1
    }
    t1
  }

  /** 原 tb_top 的 switch 是低有效：n = ~switch[7:0]。 */
  private def switchFor(n: Int): Int = (~n) & 0xff

  private def runOne(dut: SocMiniTop, n: Int): Unit = {
    dut.io.switch.poke(switchFor(n).U(8.W))
    // 对应原 tb_top：先拉低 resetn 2000ns（≈200 个 10ns 周期），再放开
    dut.io.resetn.poke(false.B)
    dut.clock.step(8)
    dut.io.resetn.poke(true.B)
    // 单周期 CPU：约 4n + 7 条指令，留足余量
    dut.clock.step(4 * n + 64)
    dut.io.led.expect((~fib(n) & BigInt(0xffff)).U(16.W))
  }

  behavior of "SocMiniTop + MinicpuTop (exp5, 5 条指令单周期 CPU)"

  // 原 tb_top 的固定激励：switch = ~(8'h4) ⇒ n = 4 ⇒ f(4) = 5 ⇒ led = 0xFFFA
  it should "复现原 tb_top 的 switch = ~(8'h4)：led = ~f(4) = 0xfffa" in {
    test(new SocMiniTop()) { dut =>
      dut.io.switch.poke(0xfb.U(8.W))
      dut.io.resetn.poke(false.B)
      dut.clock.step(8)
      dut.io.resetn.poke(true.B)
      dut.clock.step(4 * 4 + 64)
      dut.io.led.expect(0xfffa.U(16.W))
    }
  }

  for (n <- Seq(1, 2, 3, 5, 6, 8, 10, 12)) {
    it should s"n = $n 时 led = ~f($n)" in {
      test(new SocMiniTop()) { dut => runOne(dut, n) }
    }
  }
}
