// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_axi/rtl/soc_lite_top.v
//
// SoC 装配在共享库 envlib.soc.SocAxiTop（AXI 交叉开关 + axi_wrap_ram + AXI RAM 模型 +
// AXI confreg）；本文件只把学生 CPU 绑上去。
// exp15 的 func 覆盖 n1~n58，CPU 需直接产生 AXI4 的 ar/r/aw/w/b 五通道。

package exp15.soc

import envlib.soc.SocAxiTop
import exp15.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true,
  perfTest:   Boolean     = false
) extends SocAxiTop(() => new MyCpuTop, instInit, dataInit, simulation, perfTest)
