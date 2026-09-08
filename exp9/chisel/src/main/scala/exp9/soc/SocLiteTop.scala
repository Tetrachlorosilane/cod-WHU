// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_bram/rtl/soc_lite_top.v（与 exp7/exp8 相同）
//
// SoC 装配在共享库 envlib.soc.SocBramTop；本文件只把学生 CPU 绑上去。
// 与 exp8 的唯一区别是 func 程序与 CPU 内部的数据相关处理方式（前递 vs 阻塞），
// 环境本身不变。

package exp9.soc

import envlib.soc.SocBramTop
import exp9.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true
) extends SocBramTop(() => new MyCpuTop, instInit, dataInit, simulation)
