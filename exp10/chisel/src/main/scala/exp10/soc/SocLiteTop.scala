// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_bram/rtl/soc_lite_top.v（与 exp7~exp9 相同）
//
// SoC 装配在共享库 envlib.soc.SocBramTop；本文件只把学生 CPU 绑上去。
// exp10 的 func 覆盖 n1~n36（新增算术逻辑类与乘除类指令），环境本身不变。

package exp10.soc

import envlib.soc.SocBramTop
import exp10.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true
) extends SocBramTop(() => new MyCpuTop, instInit, dataInit, simulation)
