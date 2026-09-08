// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 测试侧工具（exp7~exp23 共用）：读取原实验的仿真输入/参考结果文件。
//
// 原 Verilog testbench 用 $readmemb 读 func/obj/*.mif、用 $fscanf 读 gettrace/golden_trace.txt；
// Chisel 版在测试代码里用普通文件 I/O 读入，再通过构造参数注入 RAM 模型 / 逐条比对。
// ============================================================================

package envlib.test

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/** 读取 $readmemb 使用的 .mif（每行一个 32 位二进制字，MSB 在前）。 */
object MifLoader {
  def load(path: String): Seq[BigInt] = {
    val p = Paths.get(path)
    require(Files.exists(p), s"找不到 .mif 文件：$path（请从 expN/chisel 目录运行）")
    Files.readAllLines(p).asScala.iterator.map(_.trim).filter(_.nonEmpty).map(l => BigInt(l, 2)).toSeq
  }
}

/** 读取 gettrace/golden_trace.txt（格式：`<flag> <pc> <wnum> <wdata>`，十六进制）。 */
object TraceLoader {
  final case class Entry(pc: BigInt, wnum: Int, wdata: BigInt)

  def load(path: String): Seq[Entry] = {
    val p = Paths.get(path)
    require(Files.exists(p), s"找不到 golden_trace.txt：$path（请从 expN/chisel 目录运行）")
    Files.readAllLines(p).asScala.iterator.map(_.trim).filter(_.nonEmpty).map { l =>
      val f = l.split("\\s+")
      Entry(BigInt(f(1), 16), Integer.parseInt(f(2), 16), BigInt(f(3), 16))
    }.toSeq
  }
}
