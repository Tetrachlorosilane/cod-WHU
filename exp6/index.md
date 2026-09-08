---
layout: default
title: 实践任务6 · 20 条指令单周期 CPU
parent: 实践任务
nav_order: 6
---

# 实践任务6：20 条指令单周期 CPU

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](chisel/README.md) ｜ [逐行对照](chisel/MAPPING.md)

> **任务类型**：找错 ｜ **待操作代码**：7 处
>
> **代码目录**：`code/`（原 Verilog 实验环境，软链接） ｜ `chisel/`（Chisel 版，软链接）

## 实验目标

1. 结合本章讲述的设计方案，阅读并理解 Chisel 实验环境中提供的 `myCPU` 代码。
2. **完成代码调试**：环境中加入了若干错误，通过仿真调试修复这些错误，使设计通过仿真验证。

## 关键代码

### `Alu.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/student/Alu.scala](chisel/src/main/scala/student/Alu.scala)（软链接到 `taskvscode/exp6/chisel/…`）

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（找错）：ALU —— 对应原 Verilog：code/myCPU/alu.v
// 本实验（实践任务6）的教学意图 = 找错：原 myCPU/ 里被"加入了若干错误"。
// 本文件保留与 alu.v 同族的**功能错误**（共 4 处，见 ../MAPPING.md 错误映射表）。
//   #2 sll_result 的操作数写反          （原 alu.v:80）
//   #3 sr64_result 的操作数写反         （原 alu.v:83）
//   #4 sr_result 位选范围写错           （原 alu.v:85）
//   #1' 原 alu.v:74 的 `or_result = alu_src1 | alu_src2 | alu_result` 是**组合自引用**
//      （与 alu_result 成环）。Chisel/FIRRTL 无法 elaborate 组合环，故按
//      CHISEL-CONVENTIONS.md §6 用等价的"OR 操作数错误"替代，详见 MAPPING.md。
// 请勿修正这些错误——它们就是本实验要你找出来的东西。
// 参考解见 ../solution/Alu.scala。
// ============================================================================

package exp6.student

import chisel3._
import chisel3.util._

class Alu extends Module {
  val io = IO(new Bundle {
    val alu_op     = Input(UInt(12.W))
    val alu_src1   = Input(UInt(32.W))
    val alu_src2   = Input(UInt(32.W))
    val alu_result = Output(UInt(32.W))
  })

  // control code decomposition
  val op_add  = io.alu_op(0)
  val op_sub  = io.alu_op(1)
  val op_slt  = io.alu_op(2)
  val op_sltu = io.alu_op(3)
  val op_and  = io.alu_op(4)
  val op_nor  = io.alu_op(5)
  val op_or   = io.alu_op(6)
  val op_xor  = io.alu_op(7)
  val op_sll  = io.alu_op(8)
  val op_srl  = io.alu_op(9)
  val op_sra  = io.alu_op(10)
  val op_lui  = io.alu_op(11)

  // 32-bit adder
  val adder_b   = Mux(op_sub | op_slt | op_sltu, ~io.alu_src2, io.alu_src2)
  val adder_cin = Mux(op_sub | op_slt | op_sltu, 1.U(1.W), 0.U(1.W))
  val adder_res = io.alu_src1 +& adder_b +& adder_cin
  val adder_result = adder_res(31, 0)
  val adder_cout   = adder_res(32)

  // ADD, SUB result
  val add_sub_result = adder_result

  // SLT result
  val slt_result = Cat(0.U(31.W), (io.alu_src1(31) & ~io.alu_src2(31)) |
                                  ((~(io.alu_src1(31) ^ io.alu_src2(31))) & adder_result(31)))

  // SLTU result
  val sltu_result = Cat(0.U(31.W), ~adder_cout)

  // bitwise operation
  val and_result = io.alu_src1 & io.alu_src2

// …（以下为待实现的 TODO 部分，见「待操作代码」）
```

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/student/MyCpuTop.scala](chisel/src/main/scala/student/MyCpuTop.scala)（软链接到 `taskvscode/exp6/chisel/…`）

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（找错）：myCPU 单周期 CPU（20 条指令）
//   对应原 Verilog：code/myCPU/mycpu_top.v
//
// 本实验（实践任务6）的教学意图 = 找错。原 myCPU/ 目录被"加入了若干错误"，
// 学生通过仿真波形 + golden_trace.txt 比对找出并修复。Chisel 版保留同族错误：
//   #5  ALU 的 alu_src1 端口误接成 alu_src2        （原 mycpu_top.v:253）
//   #6  debug_wb_rf_we 未被正确驱动（原端口名拼错） （原 mycpu_top.v:271）
//   #7  final_result 位宽被截断成 1 位             （原 mycpu_top.v:263，未声明 → 隐式 1 位线网）
//   其余 4 处错误在 ../student/Alu.scala 中，见 ../MAPPING.md 错误映射表。
//
// 请勿修正这些错误——它们就是本实验要你找出来的东西。
// 参考解见 ../solution/MyCpuTop.scala 与 ../solution/Alu.scala。
// ============================================================================

package exp6.student

import chisel3._
import chisel3.util._
import exp6.common._

class MyCpuTop extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())

    // inst sram interface
    val inst_sram_we    = Output(Bool())
    val inst_sram_addr  = Output(UInt(32.W))
    val inst_sram_wdata = Output(UInt(32.W))
    val inst_sram_rdata = Input(UInt(32.W))

    // data sram interface
    val data_sram_we    = Output(Bool())
    val data_sram_addr  = Output(UInt(32.W))
    val data_sram_wdata = Output(UInt(32.W))
    val data_sram_rdata = Input(UInt(32.W))

    // trace debug interface
    val debug_wb_pc       = Output(UInt(32.W))
    val debug_wb_rf_we    = Output(UInt(4.W))
    val debug_wb_rf_wnum  = Output(UInt(5.W))
    val debug_wb_rf_wdata = Output(UInt(32.W))
  })

  // reg reset; always @(posedge clk) reset <= ~resetn;
  val reset = RegNext(!io.resetn, true.B)
  val valid = RegInit(false.B)
  when(reset) {
    valid := false.B
  }.otherwise {
    valid := true.B
  }

  val pc = RegInit(0x1bfffffcL.U(32.W))
  val seq_pc = pc + 4.U

  // ---------------- 取指 ----------------
  io.inst_sram_we    := false.B
  io.inst_sram_addr  := pc
  io.inst_sram_wdata := 0.U(32.W)
  val inst = io.inst_sram_rdata

  // ---------------- 指令字段 ----------------
  val op_31_26 = inst(31, 26)
  val op_25_22 = inst(25, 22)
  val op_21_20 = inst(21, 20)
  val op_19_15 = inst(19, 15)
  val rd       = inst(4, 0)
  val rj       = inst(9, 5)
  val rk       = inst(14, 10)
  val i12      = inst(21, 10)
  val i20      = inst(24, 5)
  val i16      = inst(25, 10)
  val i26      = Cat(inst(9, 0), inst(25, 10))

  val u_dec0 = Module(new Decoder6_64)
  u_dec0.io.in := op_31_26
  val op_31_26_d = u_dec0.io.out

  val u_dec1 = Module(new Decoder4_16)
  u_dec1.io.in := op_25_22
  val op_25_22_d = u_dec1.io.out

  val u_dec2 = Module(new Decoder2_4)
  u_dec2.io.in := op_21_20
  val op_21_20_d = u_dec2.io.out

  val u_dec3 = Module(new Decoder5_32)
  u_dec3.io.in := op_19_15
  val op_19_15_d = u_dec3.io.out

  // ---------------- 指令译码 ----------------
  val inst_add_w  = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x00)
  val inst_sub_w  = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x02)
  val inst_slt    = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x04)
  val inst_sltu   = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x05)
  val inst_nor    = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x08)
  val inst_and    = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x09)
  val inst_or     = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x0a)
  val inst_xor    = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x0b)
  val inst_slli_w = op_31_26_d(0x00) & op_25_22_d(0x1) & op_21_20_d(0x0) & op_19_15_d(0x01)
  val inst_srli_w = op_31_26_d(0x00) & op_25_22_d(0x1) & op_21_20_d(0x0) & op_19_15_d(0x09)
  val inst_srai_w = op_31_26_d(0x00) & op_25_22_d(0x1) & op_21_20_d(0x0) & op_19_15_d(0x11)
  val inst_addi_w = op_31_26_d(0x00) & op_25_22_d(0x0a)
  val inst_ld_w   = op_31_26_d(0x0a) & op_25_22_d(0x2)
  val inst_st_w   = op_31_26_d(0x0a) & op_25_22_d(0x6)
  val inst_jirl   = op_31_26_d(0x13)
  val inst_b      = op_31_26_d(0x14)
  val inst_bl     = op_31_26_d(0x15)
  val inst_beq    = op_31_26_d(0x16)
  val inst_bne    = op_31_26_d(0x17)
  val inst_lu12i_w = op_31_26_d(0x05) & ~inst(25)

  // ---------------- ALU 控制码 ----------------
  val alu_op = Cat(
    inst_lu12i_w,                                                          // [11]
    inst_srai_w,                                                           // [10]
    inst_srli_w,                                                           // [ 9]
    inst_slli_w,                                                           // [ 8]
    inst_xor,                                                              // [ 7]
    inst_or,                                                               // [ 6]
    inst_nor,                                                              // [ 5]
    inst_and,                                                              // [ 4]
    inst_sltu,                                                             // [ 3]
    inst_slt,                                                              // [ 2]
    inst_sub_w,                                                            // [ 1]
    inst_add_w | inst_addi_w | inst_ld_w | inst_st_w | inst_jirl | inst_bl  // [ 0]
  )

  val need_ui5  = inst_slli_w | inst_srli_w | inst_srai_w
  val need_si12 = inst_addi_w | inst_ld_w | inst_st_w
  val need_si16 = inst_jirl | inst_beq | inst_bne
  val need_si20 = inst_lu12i_w
  val need_si26 = inst_b | inst_bl
  val src2_is_4 = inst_jirl | inst_bl

  val imm = Mux(src2_is_4, 4.U(32.W),
            Mux(need_si20, Cat(i20, 0.U(12.W)),
              Cat(Fill(20, i12(11)), i12)))
```

### `AsyncRam.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/soc/AsyncRam.scala](chisel/src/main/scala/soc/AsyncRam.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_dram/testbench/async_ram.v
//   提供 async_ram / inst_ram / data_ram 三个模块（仿真用的分布式 RAM 模型）。
//
// 原 Verilog：
//     assign rdata = (!we) ? data_out : {DATA_WIDTH{1'bz}};   // 写周期输出 Z
//     always @(posedge clk) if (we) ram[address] <= wdata;    // 同步写
//     always @*             if (!we) data_out = ram[address]; // 异步读
//     initial $readmemb(".../func/obj/inst_ram.mif", async_ram.ram);
//
// Chisel 差异（见 ../MAPPING.md）：
//   1. Chisel 没有三态/Z，写周期读数据取 0（原为 Z；CPU 在写周期不会读数据）。
//   2. 存储器初始化：原用 $readmemb 读 .mif；Chisel 版把内容作为构造参数 init 传入，
//      由测试侧读取 .mif 文件（见 src/test/scala/MifLoader.scala），
//      这样不需要任何 experimental API。
//   3. 深度按 init 长度向上取 2 的幂（addrWidth 参数控制），不影响仿真行为。

package exp6.soc

import chisel3._
import chisel3.util._

class AsyncRam(
  init:      Seq[BigInt] = Nil,
  addrWidth: Int         = 15,
  dataWidth: Int         = 32
) extends Module {
  val depth = 1 << addrWidth

  val io = IO(new Bundle {
    val address = Input(UInt(addrWidth.W))
    val we      = Input(Bool())
    val wdata   = Input(UInt(dataWidth.W))
    val rdata   = Output(UInt(dataWidth.W))
  })

  val ram = RegInit(VecInit(init.padTo(depth, BigInt(0)).map(_.U(dataWidth.W))))

  // MEM_WRITE：同步写
  when(io.we) {
    ram(io.address) := io.wdata
  }

  // MEM_READ：异步读（写周期原输出 Z，Chisel 取 0）
  io.rdata := Mux(io.we, 0.U(dataWidth.W), ram(io.address))
}

/** 对应 async_ram.v 的 `inst_ram` 包装（同步写 + 异步读，端口 we/a/d/spo）。 */
class InstRam(init: Seq[BigInt] = Nil, addrWidth: Int = 15, dataWidth: Int = 32) extends Module {
  val io = IO(new Bundle {
    val we  = Input(Bool())
    val a   = Input(UInt(addrWidth.W))
    val d   = Input(UInt(dataWidth.W))
    val spo = Output(UInt(dataWidth.W))
  })

  val ram = Module(new AsyncRam(init, addrWidth, dataWidth))
  ram.io.address := io.a
  ram.io.we      := io.we
  ram.io.wdata   := io.d
  io.spo         := ram.io.rdata
}

/** 对应 async_ram.v 的 `data_ram` 包装。 */
class DataRam(init: Seq[BigInt] = Nil, addrWidth: Int = 15, dataWidth: Int = 32) extends Module {
  val io = IO(new Bundle {
    val we  = Input(Bool())
    val a   = Input(UInt(addrWidth.W))
    val d   = Input(UInt(dataWidth.W))
    val spo = Output(UInt(dataWidth.W))
// …（共 79 行，其余见源文件）
```

### `Bridge1x2.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/soc/Bridge1x2.scala](chisel/src/main/scala/soc/Bridge1x2.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_dram/rtl/BRIDGE/bridge_1x2.v
//
// 原 Verilog：
//     `define CONF_ADDR_BASE 32'h1faf_0000
//     `define CONF_ADDR_MASK 32'h1fff_0000
//     assign sel_conf = (cpu_data_addr & CONF_ADDR_MASK) == CONF_ADDR_BASE;
//     assign sel_sram = !sel_conf;
//     ... 各端口直连 ...
//     assign cpu_data_rdata = {32{sel_sram}} & data_sram_rdata
//                           | {32{sel_conf}} & conf_rdata;
// sel_sram / sel_conf 互斥，故 Chisel 用 Mux 等价实现读数据选择。

package exp6.soc

import chisel3._

class Bridge1x2 extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())          // 原接口保留，未使用
    // master : cpu data
    val cpu_data_we    = Input(Bool())
    val cpu_data_addr  = Input(UInt(32.W))
    val cpu_data_wdata = Input(UInt(32.W))
    val cpu_data_rdata = Output(UInt(32.W))
    // slave : data ram
    val data_sram_en    = Output(Bool())
    val data_sram_we    = Output(Bool())
    val data_sram_addr  = Output(UInt(32.W))
    val data_sram_wdata = Output(UInt(32.W))
    val data_sram_rdata = Input(UInt(32.W))
    // slave : confreg
    val conf_en    = Output(Bool())
    val conf_we    = Output(Bool())
    val conf_addr  = Output(UInt(32.W))
    val conf_wdata = Output(UInt(32.W))
    val conf_rdata = Input(UInt(32.W))
  })

  val CONF_ADDR_BASE = 0x1faf0000L.U(32.W)
  val CONF_ADDR_MASK = 0x1fff0000L.U(32.W)

  val sel_conf = (io.cpu_data_addr & CONF_ADDR_MASK) === CONF_ADDR_BASE
  val sel_sram = !sel_conf

  // data sram
  io.data_sram_en    := sel_sram
  io.data_sram_we    := io.cpu_data_we
  io.data_sram_addr  := io.cpu_data_addr
  io.data_sram_wdata := io.cpu_data_wdata

  // confreg
  io.conf_en    := sel_conf
  io.conf_we    := io.cpu_data_we
  io.conf_addr  := io.cpu_data_addr
  io.conf_wdata := io.cpu_data_wdata

  io.cpu_data_rdata := Mux(sel_sram, io.data_sram_rdata, io.conf_rdata)
}
```

### `Confreg.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/soc/Confreg.scala](chisel/src/main/scala/soc/Confreg.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_dram/rtl/CONFREG/confreg.v（666 行）
//
// 覆盖：cr0~cr7、led、led_rg0/1、数码管扫描、switch、按键消抖状态机、step 按键、
//       timer、仿真标志（simu_flag/io_simu/open_trace/num_monitor）、虚拟串口。
//
// 与 Verilog 的差异（见 ../MAPPING.md）：
//   1. timer 原用独立的 timer_clk；本实验 `SIMU_USE_PLL=0 ⇒ timer_clk = cpu_clk`，
//      故 Chisel 版把 timer 逻辑放在同一隐式时钟域，行为等价。
//   2. 复位为同步复位，按 CHISEL-CONVENTIONS.md §4.3 手写 when(!resetn)。
//   3. Verilog 的 case 语句用 VecInit + 索引等价实现。

package exp6.soc

import chisel3._
import chisel3.util._

class Confreg(simulation: Boolean = false) extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())
    // read and write from cpu
    val conf_en    = Input(Bool())
    val conf_we    = Input(Bool())
    val conf_addr  = Input(UInt(32.W))
    val conf_wdata = Input(UInt(32.W))
    val conf_rdata = Output(UInt(32.W))
    // read and write to device on board
    val led        = Output(UInt(16.W))
    val led_rg0    = Output(UInt(2.W))
    val led_rg1    = Output(UInt(2.W))
    val num_csn    = Output(UInt(8.W))
    val num_a_g    = Output(UInt(7.W))
    val num_data   = Output(UInt(32.W))
    val switch     = Input(UInt(8.W))
    val btn_key_col = Output(UInt(4.W))
    val btn_key_row = Input(UInt(4.W))
    val btn_step    = Input(UInt(2.W))
  })

  // --------------------------------------------------------------------------
  // 地址常量（原 `define 均为 conf_addr[15:0] 的偏移）
  // --------------------------------------------------------------------------
  val a16 = io.conf_addr(15, 0)

  val CR0_ADDR       = 0x8000.U(16.W)
  val CR1_ADDR       = 0x8010.U(16.W)
  val CR2_ADDR       = 0x8020.U(16.W)
  val CR3_ADDR       = 0x8030.U(16.W)
  val CR4_ADDR       = 0x8040.U(16.W)
  val CR5_ADDR       = 0x8050.U(16.W)
  val CR6_ADDR       = 0x8060.U(16.W)
  val CR7_ADDR       = 0x8070.U(16.W)
  val LED_ADDR       = 0xf020.U(16.W)
  val LED_RG0_ADDR   = 0xf030.U(16.W)
  val LED_RG1_ADDR   = 0xf040.U(16.W)
  val NUM_ADDR       = 0xf050.U(16.W)
  val SWITCH_ADDR    = 0xf060.U(16.W)
  val BTN_KEY_ADDR   = 0xf070.U(16.W)
  val BTN_STEP_ADDR  = 0xf080.U(16.W)
  val SW_INTER_ADDR  = 0xf090.U(16.W)
  val TIMER_ADDR     = 0xe000.U(16.W)
  val IO_SIMU_ADDR   = 0xff00.U(16.W)
  val VIRTUAL_UART_ADDR = 0xff10.U(16.W)
  val SIMU_FLAG_ADDR = 0xff20.U(16.W)
  val OPEN_TRACE_ADDR = 0xff30.U(16.W)
  val NUM_MONITOR_ADDR = 0xff40.U(16.W)

  // --------------------------------------------------------------------------
  // 写使能
  // --------------------------------------------------------------------------
// …（共 463 行，其余见源文件）
```

### `SocLiteTop.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/soc/SocLiteTop.scala](chisel/src/main/scala/soc/SocLiteTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_dram/rtl/soc_lite_top.v
//
// 结构：cpu → inst_ram；cpu.data → bridge_1x2 → {data_ram, confreg}
//
// 与 Verilog 的差异（见 ../MAPPING.md）：
//   1. clk 端口 → Chisel 隐式 clock；原 `SIMU_USE_PLL=0` 的加速分支
//      （cpu_clk = timer_clk = clk）直接作为唯一时钟域，不建模 clk_pll。
//   2. 原 soc_lite_top 把 debug_wb_* 作为内部线网引给 testbench；
//      Chisel 版由测试直接访问 `soc.cpu.io.debug_wb_*`。
//   3. inst_ram / data_ram 的地址：原实例化把 `[17:2]`（16 位）接到 15 位端口，
//      实际使用 `[16:2]`；Chisel 直接写 `(16, 2)`。
//   4. 存储器内容由构造参数 instInit / dataInit 传入（对应原 $readmemb 的 .mif）。

package exp6.soc

import chisel3._
import chisel3.util._
import exp6.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true,
  addrWidth:  Int         = 15
) extends Module {

  val io = IO(new Bundle {
    val resetn = Input(Bool())
    // ------gpio-------
    val led         = Output(UInt(16.W))
    val led_rg0     = Output(UInt(2.W))
    val led_rg1     = Output(UInt(2.W))
    val num_csn     = Output(UInt(8.W))
    val num_a_g     = Output(UInt(7.W))
    val num_data    = Output(UInt(32.W))
    val switch      = Input(UInt(8.W))
    val btn_key_col = Output(UInt(4.W))
    val btn_key_row = Input(UInt(4.W))
    val btn_step    = Input(UInt(2.W))
  })

  // clk and resetn
  val cpu_resetn = RegNext(io.resetn, false.B)

  // ---------------- cpu ----------------
  val cpu = Module(new MyCpuTop)
  cpu.io.resetn := cpu_resetn

  // ---------------- inst ram ----------------
  val inst_ram = Module(new InstRam(instInit, addrWidth))
  inst_ram.io.we := cpu.io.inst_sram_we
  inst_ram.io.a  := cpu.io.inst_sram_addr(16, 2)
  inst_ram.io.d  := cpu.io.inst_sram_wdata
  cpu.io.inst_sram_rdata := inst_ram.io.spo

  // ---------------- bridge ----------------
  val bridge_1x2 = Module(new Bridge1x2)
  bridge_1x2.io.resetn         := cpu_resetn
  bridge_1x2.io.cpu_data_we    := cpu.io.data_sram_we
  bridge_1x2.io.cpu_data_addr  := cpu.io.data_sram_addr
  bridge_1x2.io.cpu_data_wdata := cpu.io.data_sram_wdata
  cpu.io.data_sram_rdata       := bridge_1x2.io.cpu_data_rdata

  // ---------------- data ram ----------------
  val data_ram = Module(new DataRam(dataInit, addrWidth))
  data_ram.io.we := bridge_1x2.io.data_sram_we && bridge_1x2.io.data_sram_en
  data_ram.io.a  := bridge_1x2.io.data_sram_addr(16, 2)
  data_ram.io.d  := bridge_1x2.io.data_sram_wdata
  bridge_1x2.io.data_sram_rdata := data_ram.io.spo
// …（共 93 行，其余见源文件）
```

### `MyCpuTbSpec.scala`（判据复现）

> 源文件：[chisel/src/test/scala/MyCpuTbSpec.scala](chisel/src/test/scala/MyCpuTbSpec.scala)

```scala
  behavior of "SocLiteTop + MyCpuTop (exp6, 20 条指令单周期 CPU)"

  it should "debug trace 与 gettrace/golden_trace.txt 逐条一致（修复全部错误后）" in {
    val instInit = MifLoader.load(instMifPath)
    val dataInit = MifLoader.load(dataMifPath)
    val trace    = TraceLoader.load(tracePath)

    test(new SocLiteTop(instInit, dataInit, simulation = true)) { dut =>
      // 原 tb 的固定激励
      dut.io.switch.poke(0xff.U)
      dut.io.btn_key_row.poke(0.U)
      dut.io.btn_step.poke(3.U)

      dut.io.resetn.poke(false.B)
      dut.clock.step(4)
      dut.io.resetn.poke(true.B)

      var idx       = 0
      var cycles    = 0
      var ended     = false
      var numErr    = 0
      var numPrev   = BigInt(0)
      val maxCycles = 4000000

      while (!ended && cycles < maxCycles) {
        val rfWe  = dut.cpu.io.debug_wb_rf_we.peek().litValue.toInt & 0xf
        val wnum  = dut.cpu.io.debug_wb_rf_wnum.peek().litValue.toInt
        val pc    = dut.cpu.io.debug_wb_pc.peek().litValue
        val wdata = dut.cpu.io.debug_wb_rf_wdata.peek().litValue

        // 1) 写回 trace 比对（原 tb 的 always @(posedge soc_clk) 比较块）
        if (rfWe != 0 && wnum != 0 && !ended) {
          if (idx >= trace.length) {
            fail(s"trace 条目用尽：golden_trace.txt 共 ${trace.length} 条，DUT 仍有写回")
          }
          val ref  = trace(idx)
          val mask = byteMask(rfWe)
          val gotV = wdata & BigInt(mask)
          val refV = ref.wdata & BigInt(mask)
          if (pc != ref.pc || wnum != ref.wnum || gotV != refV) {
            fail(
              s"trace 不一致（第 $idx 条，cycle=$cycles）\n" +
                f"  reference: PC=0x${ref.pc}%08x, wb_rf_wnum=0x${ref.wnum}%02x, wb_rf_wdata=0x${refV}%08x\n" +
                f"  mycpu    : PC=0x${pc}%08x, wb_rf_wnum=0x${wnum}%02x, wb_rf_wdata=0x${gotV}%08x"
            )
          }
          idx += 1
        }

        // 2) 功能测试点监视（原 tb 的 num_monitor 逻辑）
        val numNow = dut.u_confreg.io.num_data.peek().litValue
        if (numNow != numPrev) {
          val ok = (numNow & 0xff) == ((numPrev & 0xff) + 1) &&
                   (numNow >> 24) == ((numPrev >> 24) + 1)
          if (!ok) numErr += 1
```

## 待操作代码（TODO）

共 **7** 处，全部位于学生模块中；下表为索引，代码块给出每处的上下文。

| # | 文件 | 行号 | 标记 |
|---|---|---|---|
| 1 | `Alu.scala` | 63 | `TODO(找错 #1)` |
| 2 | `Alu.scala` | 72 | `TODO(找错 #2)` |
| 3 | `Alu.scala` | 75 | `TODO(找错 #3)` |
| 4 | `Alu.scala` | 79 | `TODO(找错 #4)` |
| 5 | `MyCpuTop.scala` | 190 | `TODO(找错 #5)` |
| 6 | `MyCpuTop.scala` | 202 | `TODO(找错 #7)` |
| 7 | `MyCpuTop.scala` | 212 | `TODO(找错 #6)` |

### 待操作：`Alu.scala:63` — `TODO(找错 #1)`

> 源文件：[chisel/src/main/scala/student/Alu.scala](chisel/src/main/scala/student/Alu.scala)#L63

```scala
  // bitwise operation
  val and_result = io.alu_src1 & io.alu_src2

  // TODO(找错 #1)：原 Verilog alu.v:74 为 `or_result = alu_src1 | alu_src2 | alu_result;`   // <<< 待操作
  //   —— 与 alu_result 形成组合自引用（FIRRTL 会报 combinational loop）。
  //   此处保留同族的"OR 操作数错误"（漏掉 alu_src2），效果同样是 or 指令结果错误。
  val or_result = io.alu_src1
```

### 待操作：`Alu.scala:72` — `TODO(找错 #2)`

> 源文件：[chisel/src/main/scala/student/Alu.scala](chisel/src/main/scala/student/Alu.scala)#L72

```scala
  val xor_result = io.alu_src1 ^ io.alu_src2
  val lui_result = io.alu_src2

  // TODO(找错 #2)：操作数写反，原 alu.v:80 为 `alu_src2 << alu_src1[4:0]`   // <<< 待操作
  val sll_result = io.alu_src2 << io.alu_src1(4, 0)

  // TODO(找错 #3)：被移数与移位量都写反，原 alu.v:83 为
  //   `{{32{op_sra & alu_src2[31]}}, alu_src2[31:0]} >> alu_src1[4:0]`
```

### 待操作：`Alu.scala:75` — `TODO(找错 #3)`

> 源文件：[chisel/src/main/scala/student/Alu.scala](chisel/src/main/scala/student/Alu.scala)#L75

```scala
  // TODO(找错 #2)：操作数写反，原 alu.v:80 为 `alu_src2 << alu_src1[4:0]`
  val sll_result = io.alu_src2 << io.alu_src1(4, 0)

  // TODO(找错 #3)：被移数与移位量都写反，原 alu.v:83 为   // <<< 待操作
  //   `{{32{op_sra & alu_src2[31]}}, alu_src2[31:0]} >> alu_src1[4:0]`
  val sr64_result = Cat(Fill(32, op_sra & io.alu_src2(31)), io.alu_src2) >> io.alu_src1(4, 0)

  // TODO(找错 #4)：位选范围写错，原 alu.v:85 为 `sr_result = sr64_result[30:0]`
```

### 待操作：`Alu.scala:79` — `TODO(找错 #4)`

> 源文件：[chisel/src/main/scala/student/Alu.scala](chisel/src/main/scala/student/Alu.scala)#L79

```scala
  //   `{{32{op_sra & alu_src2[31]}}, alu_src2[31:0]} >> alu_src1[4:0]`
  val sr64_result = Cat(Fill(32, op_sra & io.alu_src2(31)), io.alu_src2) >> io.alu_src1(4, 0)

  // TODO(找错 #4)：位选范围写错，原 alu.v:85 为 `sr_result = sr64_result[30:0]`   // <<< 待操作
  //   （31 位赋给 32 位线网 → 最高位恒 0，右移结果被截断）
  val sr_result = sr64_result(30, 0).pad(32)

  // final result mux
```

### 待操作：`MyCpuTop.scala:190` — `TODO(找错 #5)`

> 源文件：[chisel/src/main/scala/student/MyCpuTop.scala](chisel/src/main/scala/student/MyCpuTop.scala)#L190

```scala

  val u_alu = Module(new Alu)
  u_alu.io.alu_op := alu_op
  // TODO(找错 #5)：原 mycpu_top.v:253 把 alu_src1 端口误接成 alu_src2   // <<< 待操作
  u_alu.io.alu_src1 := alu_src2
  u_alu.io.alu_src2 := alu_src2
  val alu_result = u_alu.io.alu_result
```

### 待操作：`MyCpuTop.scala:202` — `TODO(找错 #7)`

> 源文件：[chisel/src/main/scala/student/MyCpuTop.scala](chisel/src/main/scala/student/MyCpuTop.scala)#L202

```scala

  val mem_result = io.data_sram_rdata

  // TODO(找错 #7)：原 mycpu_top.v:263 的 final_result 未声明 → Verilog 隐式生成   // <<< 待操作
  //   1 位线网，写回值只保留最低位。Chisel 用 (0) 显式保留同样的截断行为。
  val final_result = Mux(res_from_mem, mem_result, alu_result)(0).asUInt.pad(32)

  rf_we    := gr_we && valid
```

### 待操作：`MyCpuTop.scala:212` — `TODO(找错 #6)`

> 源文件：[chisel/src/main/scala/student/MyCpuTop.scala](chisel/src/main/scala/student/MyCpuTop.scala)#L212

```scala

  // ---------------- debug info generate ----------------
  io.debug_wb_pc := pc
  // TODO(找错 #6)：原 mycpu_top.v:271 写成 debug_wb_rf_wen（与端口名 debug_wb_rf_we 不符），   // <<< 待操作
  //   该输出从未被驱动（波形为 Z）。Chisel 无 Z，等价表现为 debug 写使能恒 0。
  io.debug_wb_rf_we    := 0.U(4.W)
  io.debug_wb_rf_wnum  := dest
  io.debug_wb_rf_wdata := final_result
```

## 实验验收

| 项 | 说明 |
|---|---|
| 原实验判据 | `mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!` |
| Chisel 版判据 | MyCpuTbSpec：golden_trace 逐条比对（9776 条） |
| 运行方式 | `cd chisel && ./mill chisel.test`（需 JDK 17 + Mill） |
| ⚠️ 未实测 | Chisel 代码为静态交付，未编译/仿真；逐行对照见 `chisel/MAPPING.md` |

## 参考

- 原任务说明：[`task.md`](task.md)（硬链接到 `taskvscode/exp6/4.3.2实践任务6-20条指令单周期CPU.md`）
- Chisel 环境说明：[`chisel/README.md`](chisel/README.md)（速览 / 步骤 / 判据 / 自检）
- Verilog ↔ Chisel 逐行对照：[`chisel/MAPPING.md`](chisel/MAPPING.md)
- 改写规范与核验记录：[CHISEL-CONVENTIONS.md](../CHISEL-CONVENTIONS.md)
- 原书（LoongArch 版）：https://bookdown.org/loongson/_book3/

---

[← 实践任务5](../exp5/index.md) ｜ [实践任务7 →](../exp7/index.md)
