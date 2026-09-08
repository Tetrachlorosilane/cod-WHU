// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 复现原 mycpu_tb.v 的验证流程（exp7~exp23 共用）
//
// 原 Verilog tb_top：
//   1. 每个时钟沿，若 debug_wb_rf_we 非 0 且 debug_wb_rf_wnum != 0 且未结束，
//      从 gettrace/golden_trace.txt 取下一行参考结果，比对
//          debug_wb_pc / debug_wb_rf_wnum / debug_wb_rf_wdata（按字节写使能掩码）；
//   2. debug_wb_pc == 32'h1c000100（END_PC）时结束并打印 ----PASS!!!；
//   3. num_monitor 监视 confreg 的 num_data：低 8 位 +1、高 8 位 +1。
//
// 用法（各实验的 Spec 只需几行）：
//   val (instInit, dataInit) = TraceHarness.loadInit("../code/func/obj/inst_ram.mif",
//                                                   "../code/func/obj/data_ram.mif")
//   test(new SocLiteTop(instInit, dataInit)) { dut =>
//     TraceHarness.run(dut, "../code/gettrace/golden_trace.txt")
//   }
// ============================================================================

package envlib.test

import chisel3._
import chiseltest._
import org.scalatest.Assertions.fail
import envlib.soc.SocBase

object TraceHarness {

  /** 原 tb 的 `define END_PC 32'h1c000100 */
  val END_PC = BigInt("1c000100", 16)

  /** 原 tb 的字节写使能掩码：只有 we 有效的字节参与比较。 */
  def byteMask(we: Int): Long = {
    var m = 0L
    for (i <- 0 until 4) if (((we >> i) & 1) != 0) m |= (0xffL << (8 * i))
    m
  }

  /** 读取 func/obj 下的 .mif（对应原 testbench 的 $readmemb 初始化）。 */
  def loadInit(instMif: String, dataMif: String): (Seq[BigInt], Seq[BigInt]) =
    (MifLoader.load(instMif), MifLoader.load(dataMif))

  /** 原 tb_top 的固定激励：switch = 8'hff、btn_key_row = 0、btn_step = 2'd3。 */
  private def driveGpio(dut: SocBase): Unit = {
    dut.io.switch.poke(0xff.U)
    dut.io.btn_key_row.poke(0.U)
    dut.io.btn_step.poke(3.U)
  }

  /**
    * 复位 DUT 后逐周期比对 golden_trace，直到 debug_wb_pc == END_PC。
    *
    * @return (比对的写回条数, 消耗的周期数)
    */
  def run(dut: SocBase, traceFile: String, maxCycles: Int = 8000000): (Int, Int) = {
    val trace = TraceLoader.load(traceFile)

    driveGpio(dut)
    dut.io.resetn.poke(false.B)
    dut.clock.step(4)
    dut.io.resetn.poke(true.B)

    var idx     = 0
    var cycles  = 0
    var ended   = false
    var numErr  = 0
    var numPrev = BigInt(0)

    while (!ended && cycles < maxCycles) {
      val rfWe  = dut.cpu.debugWbWe.peek().litValue.toInt & 0xf
      val wnum  = dut.cpu.debugWbWnum.peek().litValue.toInt
      val pc    = dut.cpu.debugWbPc.peek().litValue
      val wdata = dut.cpu.debugWbWdata.peek().litValue

      if (rfWe != 0 && wnum != 0 && !ended) {
        if (idx >= trace.length) {
          fail(s"trace 条目用尽：golden_trace.txt 共 ${trace.length} 条，DUT 仍有写回")
        }
        val ref  = trace(idx)
        val mask = byteMask(rfWe)
        val gotV = wdata & BigInt(mask)
        val refV = ref.wdata & BigInt(mask)
        if (pc != ref.pc || wnum != ref.wnum || gotV != refV) {
          fail(
            s"trace 不一致（第 $idx 条，cycle=$cycles）\n" +
              f"  reference: PC=0x${ref.pc}%08x, wb_rf_wnum=0x${ref.wnum}%02x, wb_rf_wdata=0x${refV}%08x\n" +
              f"  mycpu    : PC=0x${pc}%08x, wb_rf_wnum=0x${wnum}%02x, wb_rf_wdata=0x${gotV}%08x"
          )
        }
        idx += 1
      }

      val numNow = dut.numData.peek().litValue
      if (numNow != numPrev) {
        val ok = (numNow & 0xff) == ((numPrev & 0xff) + 1) &&
                 (numNow >> 24) == ((numPrev >> 24) + 1)
        if (!ok) numErr += 1
        numPrev = numNow
      }

      if (pc == END_PC) ended = true
      dut.clock.step()
      cycles += 1
    }

    if (!ended) {
      fail(s"$cycles 个周期内未到达 END_PC=0x1c000100（CPU 可能停住或跑飞）")
    }
    if (numErr != 0) {
      fail(s"功能测试点监视发现 $numErr 次异常（num_data 变化不连续）")
    }
    println(s"----PASS!!! trace 比对 $idx 条，共 $cycles 个周期")
    (idx, cycles)
  }
}
