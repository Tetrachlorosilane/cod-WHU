---
layout: default
title: '实践任务5 · 5 条指令单周期 CPU'
nav_order: 5
exp: 5
exp_intent: '填空'
exp_todos: 9
exp_has_chisel: true
exp_orig_judge: '`mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!`'
exp_judge: 'MinicpuTopSpec：led = ~f(n) 断言'
exp_needs_assets: false
---

# 实践任务5：5 条指令单周期 CPU

{% include exp-nav.html %}

## 实验目标

1. 阅读并理解 Chisel 实验环境中提供的代码，**补充代码中缺失的部分**，使设计可以通过仿真验证。

## 关键代码

### `MinicpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/student/MinicpuTop.scala](chisel/src/main/scala/student/MinicpuTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（填空）：miniCPU 单周期 CPU，5 条指令
//   对应原 Verilog：code/miniCPU/minicpu_top.v
//   本实验（实践任务5）的要求：填空：
//   原 Verilog 中有 9 处空位（`assign x = ;` 与 regfile 实例化的空括号），
//   Chisel 版同样保留 9 处空缺，用 `???` + TODO(填空) 标记。
//   —— 未补全时 elaboration 会以 NotImplementedError 终止，等价于原 Verilog
//      含空位时的语法错误：不补全就跑不起来。
//   参考解见 ../solution/MinicpuTop.scala（不参与默认编译）。
//
// 与 Verilog 的两点差异：
//   1. clk 端口 → Chisel 隐式 clock，不再是 IO；
//   2. Chisel 要求「先声明后使用」，故 pc 的更新语句移到文件末尾，
//      组合逻辑顺序做了等价重排（信号名与含义不变）。
// ============================================================================

package exp5.student

import chisel3._
import chisel3.util._
import exp5.common._

class MinicpuTop extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())          // 低有效（原 Verilog 中由 SoC 的 cpu_resetn 驱动）

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
  })

  // --------------------------------------------------------------------------
  // 原 Verilog：
  //   reg reset; always @(posedge clk) reset <= ~resetn;
  //   reg valid; always @(posedge clk) if (reset) valid <= 1'b0; else valid <= 1'b1;
  // （原 Verilog 中这两个 reg 无初值，仿真起始为 X；Chisel 版给确定性初值。）
  // --------------------------------------------------------------------------
  val reset_r = RegNext(!io.resetn, true.B)
  val valid = RegInit(false.B)
  when(reset_r) {
    valid := false.B
  }.otherwise {
    valid := true.B
  }

  // --------------------------------------------------------------------------
  // 原 Verilog：
  //   reg [31:0] pc;
  //   always @(posedge clk) begin
  //       if (reset) pc <= 32'h1bfffffc;   //trick: to make nextpc be 0x1c000000 during reset
  //       else       pc <= nextpc;
  //   end
  // 注意：pc 的更新语句放在本文件末尾（Chisel 需先声明 nextpc）。
  // --------------------------------------------------------------------------
  val pc = RegInit(0x1bfffffcL.U(32.W))

  // --------------------------------------------------------------------------
  // 取指接口
  //   assign inst_sram_we    = 1'b0;
  //   assign inst_sram_addr  = pc;
  //   assign inst_sram_wdata = 32'b0;
  //   assign inst            = inst_sram_rdata;
  // --------------------------------------------------------------------------
  io.inst_sram_we    := false.B
  io.inst_sram_addr  := pc
  io.inst_sram_wdata := 0.U(32.W)
  val inst = io.inst_sram_rdata

  // --------------------------------------------------------------------------
  // 指令字段
  // --------------------------------------------------------------------------
  val op_31_26 = inst(31, 26)
  val op_25_22 = inst(25, 22)
  val op_21_20 = inst(21, 20)
  val op_19_15 = inst(19, 15)
  val rd       = inst(4, 0)
  val rj       = inst(9, 5)
  val rk       = inst(14, 10)
  val i12      = inst(21, 10)
  val i16      = inst(25, 10)

  // --------------------------------------------------------------------------
  // 译码器（对应 code/miniCPU/tools.v 的 4 个 decoder 模块）
  //   decoder_6_64 u_dec0(.in(op_31_26 ), .co(op_31_26_d ));
  //   decoder_4_16 u_dec1(.in(op_25_22 ), .co(op_25_22_d ));
  //   decoder_2_4  u_dec2(.in(op_21_20 ), .co(op_21_20_d ));
  //   decoder_5_32 u_dec3(.in(op_19_15 ), .co(op_19_15_d ));
  // --------------------------------------------------------------------------
  val u_dec0 = Module(new Decoder6_64)
  u_dec0.io.in := op_31_26
  val op_31_26_d = u_dec0.io.co

  val u_dec1 = Module(new Decoder4_16)
  u_dec1.io.in := op_25_22
  val op_25_22_d = u_dec1.io.co

  val u_dec2 = Module(new Decoder2_4)
  u_dec2.io.in := op_21_20
  val op_21_20_d = u_dec2.io.co

  val u_dec3 = Module(new Decoder5_32)
  u_dec3.io.in := op_19_15
  val op_19_15_d = u_dec3.io.co

  // --------------------------------------------------------------------------
  // 指令译码
  // --------------------------------------------------------------------------
  val inst_add_w  = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x00)
  val inst_addi_w = op_31_26_d(0x00) & op_25_22_d(0xa)
  val inst_ld_w   = op_31_26_d(0x0a) & op_25_22_d(0x2)

// …（以下为待实现的 TODO 部分，见「待操作代码」）
```

### `Confreg.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/soc/Confreg.scala](chisel/src/main/scala/soc/Confreg.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/rtl/CONFREG/confreg.v
//
// 原 Verilog：
//     wire write_led = conf_we;
//     assign led = led_data[15:0];
//     always @(posedge clk) begin
//         if(!resetn)          led_data <= 32'h0;
//         else if(write_led)   led_data <= conf_wdata[31:0];
//     end
//
// 复位为同步复位（原 always @(posedge clk) 内判断），故 Chisel 版手写同步复位，
// 不使用 RegInit 的隐式复位。

package exp5.soc

import chisel3._

/** 板级外设控制寄存器：目前只有 16 位 LED。 */
class Confreg extends Module {
  val io = IO(new Bundle {
    val resetn     = Input(Bool())        // 低有效，同步复位
    val conf_we    = Input(Bool())        // 来自 CPU 的写使能
    val conf_wdata = Input(UInt(32.W))    // 来自 CPU 的写数据
    val led        = Output(UInt(16.W))   // 到开发板 LED
  })

  val led_data = Reg(UInt(32.W))

  when(!io.resetn) {
    led_data := 0.U
  }.elsewhen(io.conf_we) {
    led_data := io.conf_wdata
  }

  io.led := led_data(15, 0)
}
```

### `InstRam.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/soc/InstRam.scala](chisel/src/main/scala/soc/InstRam.scala)

```scala
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
// Chisel 替代方案：
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
// …（共 76 行，其余见源文件）
```

### `SocMiniTop.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/soc/SocMiniTop.scala](chisel/src/main/scala/soc/SocMiniTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/rtl/soc_mini_top.v
//
// 原 Verilog 结构（仿真时 `SIMU_USE_PLL = 0，tb_top 用默认参数 SIMULATION=1'b0）：
//     wire cpu_clk;  reg cpu_resetn;
//     always @(posedge cpu_clk) cpu_resetn <= resetn;
//     generate if(SIMULATION && `SIMU_USE_PLL==0) assign cpu_clk = clk;
//              else clk_pll clk_pll(.clk_in1(clk), .cpu_clk(cpu_clk), .timer_clk()); endgenerate
//     assign cpu_data_rdata = (cpu_data_addr == 12'd1024) ? {24'b0, ~switch[7:0]} : 32'b0;
//     inst_ram  : .a(cpu_inst_addr[17:2])
//     assign conf_we    = cpu_data_we && cpu_data_addr == 12'd1028;
//     assign conf_wdata = cpu_data_wdata;
//     assign led = ~conf_led;
//
// 时钟：Chisel 使用隐式 clock，等价于原 generate 的 speedup_simulation 分支
//（clk_pll 不建模；usePll 参数仅作记录用途）。

package exp5.soc

import chisel3._
import chisel3.util._
import exp5.student.MinicpuTop

class SocMiniTop(
  program:  Seq[BigInt] = InstRamProgram.words,
  usePll:   Boolean     = false,   // 对应原 SIMU_USE_PLL；Chisel 版不建模 clk_pll
  depth:    Int         = 32       // 对应 IP 的 PARAM_VALUE.depth
) extends Module {

  val io = IO(new Bundle {
    val resetn = Input(Bool())        // 低有效
    // ------gpio-------
    val led    = Output(UInt(16.W))
    val switch = Input(UInt(8.W))
  })

  // clk and resetn：原 always @(posedge cpu_clk) cpu_resetn <= resetn;
  val cpu_resetn = RegNext(io.resetn, false.B)

  // ---------------- cpu ----------------
  val cpu = Module(new MinicpuTop)
  cpu.io.resetn := cpu_resetn

  // ---------------- inst ram ----------------
  // 原实例化：.a(cpu_inst_addr[17:2])
  val instRam = Module(new InstRam(program, depth))
  instRam.io.we := cpu.io.inst_sram_we
  instRam.io.a  := cpu.io.inst_sram_addr(17, 2)
  instRam.io.d  := cpu.io.inst_sram_wdata
  cpu.io.inst_sram_rdata := instRam.io.spo

  // ---------------- confreg ----------------
  // 原 assign conf_we = cpu_data_we && cpu_data_addr == 12'd1028;
  val conf_we = cpu.io.data_sram_we && (cpu.io.data_sram_addr === 1028.U)

  val confreg = Module(new Confreg)
  confreg.io.resetn     := cpu_resetn
  confreg.io.conf_we    := conf_we
  confreg.io.conf_wdata := cpu.io.data_sram_wdata

  // 原 assign led = ~conf_led;
  io.led := ~confreg.io.led

  // ---------------- data read mux ----------------
  // 原 assign cpu_data_rdata = (cpu_data_addr == 12'd1024) ? {24'b0, ~switch[7:0]} : 32'b0;
  // 地址 1024（0x400）读拨码开关（低有效，取反后送 CPU）；其余地址读 0。
  cpu.io.data_sram_rdata :=
    Mux(cpu.io.data_sram_addr === 1024.U, Cat(0.U(24.W), ~io.switch), 0.U(32.W))
}
```

### `MinicpuTopSpec.scala`（判据复现）

> 源文件：[chisel/src/test/scala/MinicpuTopSpec.scala](chisel/src/test/scala/MinicpuTopSpec.scala)

```scala
  behavior of "SocMiniTop + MinicpuTop (exp5, 5 条指令单周期 CPU)"

  // 原 tb_top 的固定激励：switch = ~(8'h4) ⇒ n = 4 ⇒ f(4) = 5 ⇒ led = 0xFFFA
  it should "复现原 tb_top 的 switch = ~(8'h4)：led = ~f(4) = 0xfffa" in {
    test(new SocMiniTop()) { dut =>
      dut.io.switch.poke(0xfb.U(8.W))
      dut.io.resetn.poke(false.B)
      dut.clock.step(8)
      dut.io.resetn.poke(true.B)
      dut.clock.step(4 * 4 + 64)
      dut.io.led.expect(0xfffa.U(16.W))
    }
  }

  for (n <- Seq(1, 2, 3, 5, 6, 8, 10, 12)) {
    it should s"n = $n 时 led = ~f($n)" in {
      test(new SocMiniTop()) { dut => runOne(dut, n) }
    }
  }
}
```

## 待操作代码（TODO）

共 **9** 处，全部位于学生模块中；下表为索引，代码块给出每处的上下文。

| # | 文件 | 行号 | 标记 |
|---|---|---|---|
| 1 | `MinicpuTop.scala` | 121 | `TODO(填空 1/9)` |
| 2 | `MinicpuTop.scala` | 127 | `TODO(填空 2/9)` |
| 3 | `MinicpuTop.scala` | 154 | `TODO(填空 3/9)` |
| 4 | `MinicpuTop.scala` | 156 | `TODO(填空 4/9)` |
| 5 | `MinicpuTop.scala` | 159 | `TODO(填空 5/9)` |
| 6 | `MinicpuTop.scala` | 174 | `TODO(填空 6/9)` |
| 7 | `MinicpuTop.scala` | 182 | `TODO(填空 7/9)` |
| 8 | `MinicpuTop.scala` | 196 | `TODO(填空 8/9)` |
| 9 | `MinicpuTop.scala` | 216 | `TODO(填空 9/9)` |

### 待操作：`MinicpuTop.scala:121` — `TODO(填空 1/9)`

> 源文件：[chisel/src/main/scala/student/MinicpuTop.scala](chisel/src/main/scala/student/MinicpuTop.scala)#L121

```scala
  val inst_addi_w = op_31_26_d(0x00) & op_25_22_d(0xa)
  val inst_ld_w   = op_31_26_d(0x0a) & op_25_22_d(0x2)

  // TODO(填空 1/9)：在这里实现inst_st_w指令的译码 @ code/miniCPU/minicpu_top.v:109   // <<< 待操作
  val inst_st_w = Wire(Bool())
  inst_st_w := ???

  val inst_bne = op_31_26_d(0x17)
```

### 待操作：`MinicpuTop.scala:127` — `TODO(填空 2/9)`

> 源文件：[chisel/src/main/scala/student/MinicpuTop.scala](chisel/src/main/scala/student/MinicpuTop.scala)#L127

```scala

  val inst_bne = op_31_26_d(0x17)

  // TODO(填空 2/9)：在这里实现立即数选择信号 @ code/miniCPU/minicpu_top.v:112   // <<< 待操作
  val src2_is_imm = Wire(Bool())
  src2_is_imm := ???

  // --------------------------------------------------------------------------
```

### 待操作：`MinicpuTop.scala:154` — `TODO(填空 3/9)`

> 源文件：[chisel/src/main/scala/student/MinicpuTop.scala](chisel/src/main/scala/student/MinicpuTop.scala)#L154

```scala
  val rf_wdata = Wire(UInt(32.W))

  val u_regfile = Module(new Regfile)
  // TODO(填空 3/9)：在空出的括号里完成引脚匹配 @ code/miniCPU/minicpu_top.v:121   // <<< 待操作
  u_regfile.io.raddr1 := ???
  // TODO(填空 4/9)：在空出的括号里完成引脚匹配 @ code/miniCPU/minicpu_top.v:123
  u_regfile.io.raddr2 := ???
  u_regfile.io.we     := gr_we
```

### 待操作：`MinicpuTop.scala:156` — `TODO(填空 4/9)`

> 源文件：[chisel/src/main/scala/student/MinicpuTop.scala](chisel/src/main/scala/student/MinicpuTop.scala)#L156

```scala
  val u_regfile = Module(new Regfile)
  // TODO(填空 3/9)：在空出的括号里完成引脚匹配 @ code/miniCPU/minicpu_top.v:121
  u_regfile.io.raddr1 := ???
  // TODO(填空 4/9)：在空出的括号里完成引脚匹配 @ code/miniCPU/minicpu_top.v:123   // <<< 待操作
  u_regfile.io.raddr2 := ???
  u_regfile.io.we     := gr_we
  // TODO(填空 5/9)：在空出的括号里完成引脚匹配 @ code/miniCPU/minicpu_top.v:127
  u_regfile.io.waddr  := ???
```

### 待操作：`MinicpuTop.scala:159` — `TODO(填空 5/9)`

> 源文件：[chisel/src/main/scala/student/MinicpuTop.scala](chisel/src/main/scala/student/MinicpuTop.scala)#L159

```scala
  // TODO(填空 4/9)：在空出的括号里完成引脚匹配 @ code/miniCPU/minicpu_top.v:123
  u_regfile.io.raddr2 := ???
  u_regfile.io.we     := gr_we
  // TODO(填空 5/9)：在空出的括号里完成引脚匹配 @ code/miniCPU/minicpu_top.v:127   // <<< 待操作
  u_regfile.io.waddr  := ???
  u_regfile.io.wdata  := rf_wdata

  val rj_value  = u_regfile.io.rdata1
```

### 待操作：`MinicpuTop.scala:174` — `TODO(填空 6/9)`

> 源文件：[chisel/src/main/scala/student/MinicpuTop.scala](chisel/src/main/scala/student/MinicpuTop.scala)#L174

```scala
  //   assign br_taken  = valid && inst_bne && !rj_eq_rd;
  //   assign nextpc    = ;
  // --------------------------------------------------------------------------
  // TODO(填空 6/9)：在这里完成br_offs信号的生成 @ code/miniCPU/minicpu_top.v:131   // <<< 待操作
  val br_offs = Wire(UInt(32.W))
  br_offs := ???

  val br_target = pc + br_offs
```

### 待操作：`MinicpuTop.scala:182` — `TODO(填空 7/9)`

> 源文件：[chisel/src/main/scala/student/MinicpuTop.scala](chisel/src/main/scala/student/MinicpuTop.scala)#L182

```scala
  val rj_eq_rd  = (rj_value === rkd_value)
  val br_taken  = valid && inst_bne && !rj_eq_rd

  // TODO(填空 7/9)：在这里实现nextpc信号的生成 @ code/miniCPU/minicpu_top.v:135   // <<< 待操作
  val nextpc = Wire(UInt(32.W))
  nextpc := ???

  // --------------------------------------------------------------------------
```

### 待操作：`MinicpuTop.scala:196` — `TODO(填空 8/9)`

> 源文件：[chisel/src/main/scala/student/MinicpuTop.scala](chisel/src/main/scala/student/MinicpuTop.scala)#L196

```scala
  val imm      = Cat(Fill(20, i12(11)), i12)
  val alu_src1 = rj_value

  // TODO(填空 8/9)：在这里实现alu_src2信号 @ code/miniCPU/minicpu_top.v:139   // <<< 待操作
  val alu_src2 = Wire(UInt(32.W))
  alu_src2 := ???

  val alu_result = alu_src1 + alu_src2
```

### 待操作：`MinicpuTop.scala:216` — `TODO(填空 9/9)`

> 源文件：[chisel/src/main/scala/student/MinicpuTop.scala](chisel/src/main/scala/student/MinicpuTop.scala)#L216

```scala
  // 写回
  //   assign rf_wdata = ;   //在这里完成写回寄存器值的选择
  // --------------------------------------------------------------------------
  // TODO(填空 9/9)：在这里完成写回寄存器值的选择 @ code/miniCPU/minicpu_top.v:147   // <<< 待操作
  rf_wdata := ???

  // --------------------------------------------------------------------------
  // pc 更新（原 Verilog 中位于文件前部；Chisel 需先声明 nextpc，故移到这里）
```

## 实验验收

{% include exp-accept.html %}

## 参考

{% include exp-refs.html %}

{% include exp-pager.html %}
