// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_axi/rtl/soc_lite_top.v（与 exp15 相同）
//
// SoC 装配在共享库 envlib.soc.SocAxiTop；本文件只把学生 CPU 绑上去。
// exp16 的 func 覆盖 n1~n58，并在 AXI **随机延迟**下验证 CPU 的正确性。

package exp16.soc

import envlib.soc.SocAxiTop
import exp16.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true,
  perfTest:   Boolean     = false
) extends SocAxiTop(() => new MyCpuTop, instInit, dataInit, simulation, perfTest)
