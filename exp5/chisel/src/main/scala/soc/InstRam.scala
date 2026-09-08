// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog 环境中的 Xilinx IP：code/soc_verify/rtl/xilinx_ip/inst_ram/inst_ram.xci
//
// IP 参数（摘自 inst_ram.xci）：
//     PARAM_VALUE.memory_type        = single_port_ram
//     PARAM_VALUE.depth              = 32
//     MODELPARAM_VALUE.C_ADDR_WIDTH  = 5
//     MODELPARAM_VALUE.C_WIDTH       = 32
//     PARAM_VALUE.output_options     = non_registered   → 读数据不经寄存器（异步读）
//     PARAM_VALUE.coefficient_file   = ../../../../func/inst_ram.coe
//
// 原 soc_mini_top.v 中的实例化：
//     inst_ram inst_ram (.clk(cpu_clk), .we(cpu_inst_we), .a(cpu_inst_addr[17:2]),
//                        .d(cpu_inst_wdata), .spo(cpu_inst_rdata));
// 地址端口 a 由 IP 定义为 5 位，实例化时传入 16 位的 inst_addr[17:2]，
// 因此高 11 位被截断（本实验程序中地址恒落在 0..11，行为一致）。
//
// Chisel 替代方案（见 CHISEL-CONVENTIONS.md §6.2）：
//     用 RegInit(VecInit(常量表)) 实现「复位即有初值、异步读、同步写」的 RAM，
//     与 single_port_ram + coe 初始化 + 非寄存输出 的行为等价，且不需要任何文件 I/O。

package exp5.soc

import chisel3._
import chisel3.util._

/** func/inst_ram.coe 的内容（memory_initialization_radix = 16，共 12 个 32 位字）。
  *
  * 与 func/start.S 逐条对应：
  *   addi.w $t0,$zero,0x0      ld.w   $a0,$zero,1024    add.w  $s0,$s0,$s1
  *   addi.w $t1,$zero,0x1  loop: add.w $t2,$t0,$t1      bne    $s0,$a0,loop
  *   addi.w $s0,$zero,0x0      addi.w $t0,$t1,0x0       st.w   $t2,$zero,1028
  *   addi.w $s1,$zero,0x1      addi.w $t1,$t2,0x0   end: bne    $s1,$zero,end
  */
object InstRamProgram {
  val words: Seq[BigInt] = Seq(
    0x0280000cL, // [0] addi.w $t0, $zero, 0x0
    0x0280040dL, // [1] addi.w $t1, $zero, 0x1
    0x02800017L, // [2] addi.w $s0, $zero, 0x0
    0x02800418L, // [3] addi.w $s1, $zero, 0x1
    0x28900004L, // [4] ld.w   $a0, $zero, 1024
    0x0010358eL, // [5] add.w  $t2, $t0, $t1        ← loop
    0x028001acL, // [6] addi.w $t0, $t1, 0x0
    0x028001cdL, // [7] addi.w $t1, $t2, 0x0
    0x001062f7L, // [8] add.w  $s0, $s0, $s1
    0x5ffff2e4L, // [9] bne    $s0, $a0, loop
    0x2990100eL, // [10] st.w  $t2, $zero, 1028
    0x5c000300L  // [11] bne    $s1, $zero, end     ← end
  )
}

/** 指令 RAM 的行为模型（替代 Xilinx single_port_ram IP）。 */
class InstRam(program: Seq[BigInt] = InstRamProgram.words, depth: Int = 32) extends Module {
  require(depth > 0 && (depth & (depth - 1)) == 0, "depth 必须是 2 的幂")

  val io = IO(new Bundle {
    val we  = Input(Bool())
    val a   = Input(UInt(16.W))       // 与 soc_mini_top 的 .a(inst_addr[17:2]) 对齐
    val d   = Input(UInt(32.W))
    val spo = Output(UInt(32.W))
  })

  val addrWidth = log2Ceil(depth)

  // 复位后即为 coe 的内容（不足部分补 0），对应 IP 的 memory_initialization_vector
  val mem = RegInit(VecInit(program.padTo(depth, BigInt(0)).map(_.U(32.W))))

  // 同步写：与原 single_port_ram 的写端口一致（本实验 CPU 恒不写指令 RAM）
  when(io.we) {
    mem(io.a(addrWidth - 1, 0)) := io.d
  }

  // 异步读：对应 output_options = non_registered
  io.spo := mem(io.a(addrWidth - 1, 0))
}
