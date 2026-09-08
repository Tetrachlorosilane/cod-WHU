// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_bram/rtl/soc_lite_top.v（与 exp7~exp11 相同）
//
// SoC 装配在共享库 envlib.soc.SocBramTop；本文件只把学生 CPU 绑上去。
// exp12 的 func 覆盖 n1~n47（新增 syscall 例外与 CSR 指令），环境本身不变。

package exp12.soc

import envlib.soc.SocBramTop
import exp12.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true
) extends SocBramTop(() => new MyCpuTop, instInit, dataInit, simulation)
