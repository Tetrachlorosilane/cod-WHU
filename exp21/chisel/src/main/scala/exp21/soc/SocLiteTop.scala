// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_axi/rtl/soc_lite_top.v（与 exp15~exp19 相同）
//
// SoC 装配在共享库 envlib.soc.SocAxiTop；本文件只把学生 CPU 绑上去。
// exp21 的 func 覆盖 n1~n72，CPU 内部把 exp20 的 Cache 作为 ICache 集成，
// 并让 AXI 转换桥支持 Burst 传输；SoC 环境本身不变。

package exp21.soc

import envlib.soc.SocAxiTop
import exp21.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true,
  perfTest:   Boolean     = false
) extends SocAxiTop(() => new MyCpuTop, instInit, dataInit, simulation, perfTest)
