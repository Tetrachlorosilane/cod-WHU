// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_axi/rtl/soc_lite_top.v（与 exp15~exp22 相同）
//
// SoC 装配在共享库 envlib.soc.SocAxiTop；本文件只把学生 CPU 绑上去。
// exp23 的 func 覆盖 n1~n79，CPU 内部在 I/D Cache 之外再支持 CACOP 指令；
// SoC 环境本身不变。

package exp23.soc

import envlib.soc.SocAxiTop
import exp23.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true,
  perfTest:   Boolean     = false
) extends SocAxiTop(() => new MyCpuTop, instInit, dataInit, simulation, perfTest)
