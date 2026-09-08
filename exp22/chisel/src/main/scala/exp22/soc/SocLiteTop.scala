// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_axi/rtl/soc_lite_top.v（与 exp15~exp21 相同）
//
// SoC 装配在共享库 envlib.soc.SocAxiTop；本文件只把学生 CPU 绑上去。
// exp22 的 func 覆盖 n1~n72，CPU 内部在 ICache 之外再把 exp20 的 Cache 作为
// DCache 集成到访存通路；SoC 环境本身不变。

package exp22.soc

import envlib.soc.SocAxiTop
import exp22.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true,
  perfTest:   Boolean     = false
) extends SocAxiTop(() => new MyCpuTop, instInit, dataInit, simulation, perfTest)
