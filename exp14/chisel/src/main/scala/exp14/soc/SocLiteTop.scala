// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_hs_bram/rtl/soc_lite_top.v
//
// SoC 装配在共享库 envlib.soc.SocHsBramTop（握手式访存 + sram_wrap + 握手 bridge）；
// 本文件只把学生 CPU 绑上去。exp14 的 func 覆盖 n1~n58，环境改用握手总线。

package exp14.soc

import envlib.soc.SocHsBramTop
import exp14.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true
) extends SocHsBramTop(() => new MyCpuTop, instInit, dataInit, simulation)
