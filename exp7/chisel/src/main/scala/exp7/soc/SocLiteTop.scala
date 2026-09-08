// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_bram/rtl/soc_lite_top.v
//
// exp7 的 SoC 装配已下沉到共享库 envlib.soc.SocBramTop（cpu → inst_ram；
// cpu.data → bridge_1x2 → {data_ram, confreg}），本文件只是把学生 CPU 绑上去的薄封装。
// 端口、连接关系、与 exp6（soc_dram）的差异说明

package exp7.soc

import envlib.soc.SocBramTop
import exp7.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true
) extends SocBramTop(() => new MyCpuTop, instInit, dataInit, simulation)
