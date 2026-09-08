// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/myCPU/decoder_2_4.v / decoder_4_16.v / decoder_5_32.v / decoder_6_64.v
//
// 注意：exp6 起原 Verilog 的译码器输出端口名是 `out`（exp5 的 tools.v 里叫 `co`），
// 此处与 exp6 的 code/ 保持一致。

package exp6.common

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
