---
layout: default
title: '实践任务10 · 算术逻辑运算指令和乘除法运算指令添加'
nav_order: 10
exp: 10
exp_intent: '从零实现'
exp_todos: 10
exp_has_chisel: true
exp_orig_judge: '`mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!`'
exp_judge: 'TraceHarness：golden_trace 逐条比对'
exp_needs_assets: true
---

# 实践任务10：算术逻辑运算指令和乘除法运算指令添加

{% include exp-nav.html %}

## 实验目标

1. 添加算术逻辑运算类指令：`slti`、`sltui`、`andi`、`ori`、`xori`、`sll`、`srl`、`sra`、`pcaddu12i`。
2. 添加乘除运算类指令：`mul.w`、`mulh.w`、`mulh.wu`、`div.w`、`mod.w`、`div.wu`、`mod.wu`。
3. 运行 exp10 对应的 func（n1~n36），要求通过仿真和上板验证。

## 关键代码

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp10/student/MyCpuTop.scala](chisel/src/main/scala/exp10/student/MyCpuTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现 + 指令扩展）：myCPU —— 五级流水线，新增算术逻辑类与乘除类指令
//   对应原实验：code/myCPU/（原实验环境**不提供** myCPU 目录，由学生自己实现）
//
// 本实验（实践任务10）的要求：从零实现（在 exp9 基础上增量）：
//   在 exp9 的流水线基础上添加指令：
//     ① 算术逻辑类：slti、sltui、andi、ori、xori、sll、srl、sra、pcaddu12i；
//     ② 乘除类：mul.w、mulh.w、mulh.wu、div.w、mod.w、div.wu、mod.wu。
//   Chisel 版只给接口骨架 + 10 处 TODO(实现)，内部逻辑全部留空（???）。
//
// 实现提示（详细方案见原书 6.1.1）：
//   * 立即数类新增 slti/sltui（si12 符号扩展）、andi/ori/xori（ui12 零扩展）；
//     —— 注意 ui12 与 si12 的扩展方式不同，立即数生成需按指令区分；
//   * sll/srl/sra 的移位量来自 rk[4:0]；slli/srli/srai 的移位量来自 ui5；
//   * pcaddu12i：rd = pc + (si20 << 12)；
//   * mul.w/mulh.w/mulh.wu 为有符号/无符号高位/低位乘法；div/mod 为有符号/无符号除余；
//     Chisel 里可用 `*`、`/`、`%` 直接描述，或按原书给出的多周期方案实现。
// ============================================================================

package exp10.student

import chisel3._
import chisel3.util._
import envlib.soc.LACpu

class MyCpuTop extends LACpu {
  // io 由基类 LACpu 提供（类型 envlib.soc.CpuIO），端口与各实验 soc_lite_top.v 的例化端口一致

  // --------------------------------------------------------------------------
  // ① IF 级：取指（pc / nextpc / 取指接口）
  // --------------------------------------------------------------------------
// …（以下为待实现的 TODO 部分，见「待操作代码」）
```

### `SocLiteTop.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/exp10/soc/SocLiteTop.scala](chisel/src/main/scala/exp10/soc/SocLiteTop.scala)

```scala
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
```

### `MyCpuTbSpec.scala`（判据复现）

> 源文件：[chisel/src/test/scala/MyCpuTbSpec.scala](chisel/src/test/scala/MyCpuTbSpec.scala)

```scala
  behavior of "SocLiteTop + MyCpuTop (exp10, 算术逻辑与乘除指令)"

  it should "debug trace 与 gettrace/golden_trace.txt 逐条一致" in {
    val (instInit, dataInit) =
      TraceHarness.loadInit("../code/func/obj/inst_ram.mif", "../code/func/obj/data_ram.mif")

    test(new SocLiteTop(instInit, dataInit, simulation = true)) { dut =>
      TraceHarness.run(dut, "../code/gettrace/golden_trace.txt")
    }
  }
}
```

## 待操作代码（TODO）

共 **10** 处，全部位于学生模块中；下表为索引，代码块给出每处的上下文。

| # | 文件 | 行号 | 标记 |
|---|---|---|---|
| 1 | `MyCpuTop.scala` | 33 | `TODO(实现 1/10)` |
| 2 | `MyCpuTop.scala` | 42 | `TODO(实现 2/10)` |
| 3 | `MyCpuTop.scala` | 50 | `TODO(实现 3/10)` |
| 4 | `MyCpuTop.scala` | 55 | `TODO(实现 4/10)` |
| 5 | `MyCpuTop.scala` | 60 | `TODO(实现 5/10)` |
| 6 | `MyCpuTop.scala` | 65 | `TODO(实现 6/10)` |
| 7 | `MyCpuTop.scala` | 70 | `TODO(实现 7/10)` |
| 8 | `MyCpuTop.scala` | 79 | `TODO(实现 8/10)` |
| 9 | `MyCpuTop.scala` | 84 | `TODO(实现 9/10)` |
| 10 | `MyCpuTop.scala` | 93 | `TODO(实现 10/10)` |

### 待操作：`MyCpuTop.scala:33` — `TODO(实现 1/10)`

> 源文件：[chisel/src/main/scala/exp10/student/MyCpuTop.scala](chisel/src/main/scala/exp10/student/MyCpuTop.scala)#L33

```scala
  // --------------------------------------------------------------------------
  // ① IF 级：取指（pc / nextpc / 取指接口）
  // --------------------------------------------------------------------------
  // TODO(实现 1/10)：pc / nextpc / 取指接口   // <<< 待操作
  io.inst_sram_en    := ???
  io.inst_sram_we    := ???
  io.inst_sram_addr  := ???
  io.inst_sram_wdata := ???
```

### 待操作：`MyCpuTop.scala:42` — `TODO(实现 2/10)`

> 源文件：[chisel/src/main/scala/exp10/student/MyCpuTop.scala](chisel/src/main/scala/exp10/student/MyCpuTop.scala)#L42

```scala
  // --------------------------------------------------------------------------
  // ② IF/ID 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 2/10)：IF/ID 流水寄存器（至少保存 pc、inst）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  //    本实验需在 exp9 基础上**新增译码**：slti、sltui、andi、ori、xori、sll、srl、sra、
```

### 待操作：`MyCpuTop.scala:50` — `TODO(实现 3/10)`

> 源文件：[chisel/src/main/scala/exp10/student/MyCpuTop.scala](chisel/src/main/scala/exp10/student/MyCpuTop.scala)#L50

```scala
  //    pcaddu12i，以及乘除类 mul.w、mulh.w、mulh.wu、div.w、mod.w、div.wu、mod.wu。
  //    立即数：slti/sltui 用 si12；andi/ori/xori 用 **ui12 零扩展**；pcaddu12i 用 si20<<12。
  // --------------------------------------------------------------------------
  // TODO(实现 3/10)：指令译码（含新增 9 条算术逻辑类 + 7 条乘除类）、立即数生成、寄存器堆读   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:55` — `TODO(实现 4/10)`

> 源文件：[chisel/src/main/scala/exp10/student/MyCpuTop.scala](chisel/src/main/scala/exp10/student/MyCpuTop.scala)#L55

```scala
  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 4/10)：ID/EX 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU（含新增运算）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:60` — `TODO(实现 5/10)`

> 源文件：[chisel/src/main/scala/exp10/student/MyCpuTop.scala](chisel/src/main/scala/exp10/student/MyCpuTop.scala)#L60

```scala
  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU（含新增运算）
  // --------------------------------------------------------------------------
  // TODO(实现 5/10)：ALU 运算（slt/sltu/and/or/xor/移位/pcaddu12i + mul/mulh/div/mod）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:65` — `TODO(实现 6/10)`

> 源文件：[chisel/src/main/scala/exp10/student/MyCpuTop.scala](chisel/src/main/scala/exp10/student/MyCpuTop.scala)#L65

```scala
  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 6/10)：EX/MEM 流水寄存器（保留 rd、写使能、结果，供前递使用）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（字节写使能 data_sram_we[3:0]）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:70` — `TODO(实现 7/10)`

> 源文件：[chisel/src/main/scala/exp10/student/MyCpuTop.scala](chisel/src/main/scala/exp10/student/MyCpuTop.scala)#L70

```scala
  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（字节写使能 data_sram_we[3:0]）
  // --------------------------------------------------------------------------
  // TODO(实现 7/10)：数据访存接口   // <<< 待操作
  io.data_sram_en    := ???
  io.data_sram_we    := ???
  io.data_sram_addr  := ???
  io.data_sram_wdata := ???
```

### 待操作：`MyCpuTop.scala:79` — `TODO(实现 8/10)`

> 源文件：[chisel/src/main/scala/exp10/student/MyCpuTop.scala](chisel/src/main/scala/exp10/student/MyCpuTop.scala)#L79

```scala
  // --------------------------------------------------------------------------
  // ⑧ MEM/WB 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 8/10)：MEM/WB 流水寄存器（保留 rd、写使能、结果，供前递使用）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:84` — `TODO(实现 9/10)`

> 源文件：[chisel/src/main/scala/exp10/student/MyCpuTop.scala](chisel/src/main/scala/exp10/student/MyCpuTop.scala)#L84

```scala
  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
  // TODO(实现 9/10)：写回寄存器堆 + debug_wb_* 输出   // <<< 待操作
  io.debug_wb_pc       := ???
  io.debug_wb_rf_we    := ???
  io.debug_wb_rf_wnum  := ???
  io.debug_wb_rf_wdata := ???
```

### 待操作：`MyCpuTop.scala:93` — `TODO(实现 10/10)`

> 源文件：[chisel/src/main/scala/exp10/student/MyCpuTop.scala](chisel/src/main/scala/exp10/student/MyCpuTop.scala)#L93

```scala
  // --------------------------------------------------------------------------
  // ⑩ 数据相关处理：前递 + load-use 阻塞（沿用 exp9）
  // --------------------------------------------------------------------------
  // TODO(实现 10/10)：前递通路 + load-use 阻塞   // <<< 待操作
}
```

## 实验验收

{% include exp-accept.html %}

## 参考

{% include exp-refs.html %}

{% include exp-pager.html %}
