// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// chisel-common：exp7~exp23 共用的「实验环境」模块库
//
// 为什么共享：原 Verilog 实验环境在 exp6~exp23 之间几乎逐字相同（confreg 666~690 行、
// bridge_1x2、sync/async RAM 模型），逐实验复制会产生十几份等价代码。
// 故自 exp7 起把环境公共模块集中到本目录，各实验的 chisel/build.mill 通过
// `sources` 引入本目录。
//
// exp5/exp6 在共享库建立之前已按「自包含」方式交付，其 common/ 与 soc/ 下的同名模块
// 与本库等价（exp6 的 Confreg 相当于 weWidth = 1 的实例），保留不动。
//
// 对应原 Verilog：各实验 code/myCPU/decoder_*.v（端口 in/out）。
// ============================================================================

package envlib.common

import chisel3._
import chisel3.util._

/** one-hot 译码器公共实现：out[i] = (in === i)。 */
object OneHotDecode {
  def apply(in: UInt, width: Int): UInt = {
    val n = 1 << width
    VecInit((0 until n).map(i => (in === i.U(width.W)))).asUInt
  }
}

class Decoder2_4 extends Module {
  val io = IO(new Bundle {
    val in  = Input(UInt(2.W))
    val out = Output(UInt(4.W))
  })
  io.out := OneHotDecode(io.in, 2)
}

class Decoder4_16 extends Module {
  val io = IO(new Bundle {
    val in  = Input(UInt(4.W))
    val out = Output(UInt(16.W))
  })
  io.out := OneHotDecode(io.in, 4)
}

class Decoder5_32 extends Module {
  val io = IO(new Bundle {
    val in  = Input(UInt(5.W))
    val out = Output(UInt(32.W))
  })
  io.out := OneHotDecode(io.in, 5)
}

class Decoder6_64 extends Module {
  val io = IO(new Bundle {
    val in  = Input(UInt(6.W))
    val out = Output(UInt(64.W))
  })
  io.out := OneHotDecode(io.in, 6)
}
