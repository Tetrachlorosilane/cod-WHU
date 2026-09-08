// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_bram/rtl/soc_lite_top.v（与 exp7~exp12 相同）
//
// SoC 装配在共享库 envlib.soc.SocBramTop；本文件只把学生 CPU 绑上去。
// exp13 的 func 覆盖 n1~n58（新增 ADEF/ALE/BRK/INE 例外、中断、定时器与计数器指令）。

package exp13.soc

import envlib.soc.SocBramTop
import exp13.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true
) extends SocBramTop(() => new MyCpuTop, instInit, dataInit, simulation)
