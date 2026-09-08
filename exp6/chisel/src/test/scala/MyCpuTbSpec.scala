// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 对应原 Verilog 测试平台：code/soc_verify/soc_dram/testbench/mycpu_tb.v（tb_top）
//
// 原判据（逐条复现）：
//   1. 每个时钟沿，若 debug_wb_rf_we 非 0 且 debug_wb_rf_wnum != 0 且未结束，
//      从 gettrace/golden_trace.txt 取下一行参考结果，比对
//          debug_wb_pc / debug_wb_rf_wnum / debug_wb_rf_wdata（按字节写使能掩码）
//      不一致则打印 Error 并 $finish。
//   2. debug_wb_pc == 32'h1c000100（END_PC）时结束，打印 ----PASS!!!。
//   3. 另有一个"功能测试点"监视器：confreg 的 num_data 每次变化时，
//      低 8 位应 +1、高 8 位（测试点编号）应 +1，否则计一次错误。
//
// 差异说明：
//   * 原 tb 用 $readmemb 初始化 inst_ram/data_ram；Chisel 版由本文件读取 .mif 后
//     通过构造参数传给 SocLiteTop（见 ../src/main/scala/soc/AsyncRam.scala 注释）。
//   * 原 tb 通过层次化引用读 confreg 的 open_trace / num_monitor；本实验的 func
//     程序不写这两个控制位，二者复位后恒为 1，故 Chisel 版直接按"始终开启"处理。
// ============================================================================

package exp6.test

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import exp6.soc._
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/** 读取 $readmemb 使用的 .mif（每行一个 32 位二进制字，MSB 在前）。 */
object MifLoader {
  def load(path: String): Seq[BigInt] = {
    val p = Paths.get(path)
    require(Files.exists(p), s"找不到 .mif 文件：$path（请从 chisel/ 目录运行）")
    Files.readAllLines(p).asScala.iterator.map(_.trim).filter(_.nonEmpty).map(l => BigInt(l, 2)).toSeq
  }
}

/** 读取 gettrace/golden_trace.txt（格式：`<flag> <pc> <wnum> <wdata>`，十六进制）。 */
object TraceLoader {
  final case class Entry(pc: BigInt, wnum: Int, wdata: BigInt)

  def load(path: String): Seq[Entry] = {
    val p = Paths.get(path)
    require(Files.exists(p), s"找不到 golden_trace.txt：$path（请从 chisel/ 目录运行）")
    Files.readAllLines(p).asScala.iterator.map(_.trim).filter(_.nonEmpty).map { l =>
      val f = l.split("\\s+")
      Entry(BigInt(f(1), 16), Integer.parseInt(f(2), 16), BigInt(f(3), 16))
    }.toSeq
  }
}

class MyCpuTbSpec extends AnyFlatSpec with ChiselScalatestTester {

  // 原 tb 的相对路径：chisel/ → ../code/...
  private val instMifPath  = "../code/func/obj/inst_ram.mif"
  private val dataMifPath  = "../code/func/obj/data_ram.mif"
  private val tracePath    = "../code/gettrace/golden_trace.txt"

  private val END_PC = BigInt("1c000100", 16)

  /** 原 tb 的字节写使能掩码：只有 we 有效的字节参与比较。 */
  private def byteMask(we: Int): Long = {
    var m = 0L
    for (i <- 0 until 4) if (((we >> i) & 1) != 0) m |= (0xffL << (8 * i))
    m
  }

  behavior of "SocLiteTop + MyCpuTop (exp6, 20 条指令单周期 CPU)"

  it should "debug trace 与 gettrace/golden_trace.txt 逐条一致（修复全部错误后）" in {
    val instInit = MifLoader.load(instMifPath)
    val dataInit = MifLoader.load(dataMifPath)
    val trace    = TraceLoader.load(tracePath)

    test(new SocLiteTop(instInit, dataInit, simulation = true)) { dut =>
      // 原 tb 的固定激励
      dut.io.switch.poke(0xff.U)
      dut.io.btn_key_row.poke(0.U)
      dut.io.btn_step.poke(3.U)

      dut.io.resetn.poke(false.B)
      dut.clock.step(4)
      dut.io.resetn.poke(true.B)

      var idx       = 0
      var cycles    = 0
      var ended     = false
      var numErr    = 0
      var numPrev   = BigInt(0)
      val maxCycles = 4000000

      while (!ended && cycles < maxCycles) {
        val rfWe  = dut.cpu.io.debug_wb_rf_we.peek().litValue.toInt & 0xf
        val wnum  = dut.cpu.io.debug_wb_rf_wnum.peek().litValue.toInt
        val pc    = dut.cpu.io.debug_wb_pc.peek().litValue
        val wdata = dut.cpu.io.debug_wb_rf_wdata.peek().litValue

        // 1) 写回 trace 比对（原 tb 的 always @(posedge soc_clk) 比较块）
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

        // 2) 功能测试点监视（原 tb 的 num_monitor 逻辑）
        val numNow = dut.u_confreg.io.num_data.peek().litValue
        if (numNow != numPrev) {
          val ok = (numNow & 0xff) == ((numPrev & 0xff) + 1) &&
                   (numNow >> 24) == ((numPrev >> 24) + 1)
          if (!ok) numErr += 1
          numPrev = numNow
        }

        // 3) 结束条件
        if (pc == END_PC) ended = true
        dut.clock.step()
        cycles += 1
      }

      assert(ended, s"$cycles 个周期内未到达 END_PC=0x1c000100（CPU 可能停住或跑飞）")
      assert(numErr == 0, s"功能测试点监视发现 $numErr 次异常（num_data 变化不连续）")
      println(s"----PASS!!! trace 比对 $idx 条，共 $cycles 个周期")
    }
  }
}
