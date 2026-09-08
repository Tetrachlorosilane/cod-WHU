// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_bram/rtl/soc_lite_top.v（与 exp7 相同）
//
// SoC 装配在共享库 envlib.soc.SocBramTop；本文件只把学生 CPU 绑上去。
// 与 exp7 的唯一区别是 func 程序（exp8 的相关指令之间不再插 NOP，
// 因此 CPU 必须自己用阻塞技术处理写后读数据相关），环境本身不变。

package exp8.soc

import envlib.soc.SocBramTop
import exp8.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true
) extends SocBramTop(() => new MyCpuTop, instInit, dataInit, simulation)
