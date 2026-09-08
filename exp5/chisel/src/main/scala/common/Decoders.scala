// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/miniCPU/tools.v（decoder_2_4 / decoder_4_16 / decoder_5_32 / decoder_6_64）
//
// 原实现用 generate for 展开：
//     genvar i;
//     generate for (i=0; i<N; i=i+1) begin : gen_for_dec
//         assign co[i] = (in == i);
//     end endgenerate
// Chisel 用 VecInit + asUInt 等价展开（见 OneHotDecode）。
//
// 注意：本实验（exp5）的译码器端口名为 in / co；exp6 起原 Verilog 改为 in / out，
// 各实验目录下自带的副本会相应调整端口名，保持与各自的 code/ 逐行对应。

package exp5.common

import chisel3._
import chisel3.util._

/** one-hot 译码器公共实现：co[i] = (in === i)。 */
object OneHotDecode {
  /** @param in    待译码的输入
    * @param width in 的位宽；输出位宽为 1 << width
    */
  def apply(in: UInt, width: Int): UInt = {
    val n = 1 << width
    VecInit((0 until n).map(i => (in === i.U(width.W)))).asUInt
  }
}

/** 对应 decoder_2_4：in[1:0] → co[3:0] */
class Decoder2_4 extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(2.W))
    val co = Output(UInt(4.W))
  })
  io.co := OneHotDecode(io.in, 2)
}

/** 对应 decoder_4_16：in[3:0] → co[15:0] */
class Decoder4_16 extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(4.W))
    val co = Output(UInt(16.W))
  })
  io.co := OneHotDecode(io.in, 4)
}

/** 对应 decoder_5_32：in[4:0] → co[31:0] */
class Decoder5_32 extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(5.W))
    val co = Output(UInt(32.W))
  })
  io.co := OneHotDecode(io.in, 5)
}

/** 对应 decoder_6_64：in[5:0] → co[63:0] */
class Decoder6_64 extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(6.W))
    val co = Output(UInt(64.W))
  })
  io.co := OneHotDecode(io.in, 6)
}
