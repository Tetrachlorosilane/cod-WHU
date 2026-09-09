---
layout: default
title: '实践任务13 · 添加其它异常支持'
nav_order: 13
exp: 13
exp_intent: '从零实现'
exp_todos: 14
exp_has_chisel: true
exp_orig_judge: '`mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!`'
exp_judge: 'TraceHarness：golden_trace 逐条比对'
exp_needs_assets: true
---

# 实践任务13：添加其它异常支持

{% include exp-nav.html %}

## 实验目标

1. 为 CPU 增加取指地址错（ADEF）、地址非对齐（ALE）、断点（BRK）和指令不存在（INE）异常的支持。
2. 为 CPU 增加中断的支持，包括 2 个软件中断、8 个硬件中断和定时器中断。
3. 为 CPU 增加控制状态寄存器 `ECFG`、`BADV`、`TID`、`TCFG`、`TVAL`、`TICLR`。
4. 为 CPU 增加 `rdcntvl.w`、`rdcntvh.w` 和 `rdcntid` 指令。
5. 运行 exp13 对应的 func（n1~n58），要求通过仿真和上板验证。

## 关键代码

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现 + 更多例外/中断）：myCPU —— 五级流水线
//   对应原实验：code/myCPU/（原实验环境**不提供** myCPU 目录，由学生自己实现）
//
// 本实验（实践任务13）的要求：从零实现（在 exp12 基础上增量）：
//   ① 新增例外：取指地址错 ADEF、地址非对齐 ALE、断点 BRK、指令不存在 INE；
//   ② 新增中断：2 个软件中断、8 个硬件中断、定时器中断；
//   ③ 新增 CSR：ECFG、BADV、TID、TCFG、TVAL、TICLR；
//   ④ 新增指令：rdcntvl.w、rdcntvh.w、rdcntid。
//   Chisel 版只给接口骨架 + 14 处 TODO(实现)，内部逻辑全部留空（???）。
//
// 实现提示（详细方案见原书 7.1.2）：
//   * 各例外的 Ecode：ADEF(0x8)、ALE(0x9)、BRK(0xC)、INE(0xD)、TI(0x0)、
//     SWI(0x1)、HWI(0x2~0x9)、IPI(0xB)、SYS(0xB) —— 以原书表格为准；
//   * ADEF：取指地址非 4 字节对齐 / 越界；ALE：访存地址与访问宽度不对齐；
//   * BRK：断点指令；INE：译码无法识别的指令；
//   * 中断：ESTAT.IS 与 ECFG.LIE 按位对应，TICLR 清除定时器中断；
//   * TCFG/TVAL/TID 构成定时器：TVAL 递减到 0 触发 TI；TICLR 写 1 清中断；
//   * rdcntvl.w/rdcntvh.w 读 64 位稳定计数器的高低 32 位；rdcntid 读 CPU ID。
// ============================================================================

package exp13.student

import chisel3._
import chisel3.util._
import envlib.soc.LACpu

class MyCpuTop extends LACpu {
  // io 由基类 LACpu 提供（类型 envlib.soc.CpuIO）

  // --------------------------------------------------------------------------
  // ① IF 级：取指（pc / nextpc / 取指接口）
  // --------------------------------------------------------------------------
// …（以下为待实现的 TODO 部分，见「待操作代码」）
```

### `SocLiteTop.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/exp13/soc/SocLiteTop.scala](chisel/src/main/scala/exp13/soc/SocLiteTop.scala)

```scala
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
```

### `MyCpuTbSpec.scala`（判据复现）

> 源文件：[chisel/src/test/scala/MyCpuTbSpec.scala](chisel/src/test/scala/MyCpuTbSpec.scala)

```scala
  behavior of "SocLiteTop + MyCpuTop (exp13, 更多例外与中断支持)"

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

共 **14** 处，全部位于学生模块中；下表为索引，代码块给出每处的上下文。

| # | 文件 | 行号 | 标记 |
|---|---|---|---|
| 1 | `MyCpuTop.scala` | 35 | `TODO(实现 1/14)` |
| 2 | `MyCpuTop.scala` | 44 | `TODO(实现 2/14)` |
| 3 | `MyCpuTop.scala` | 50 | `TODO(实现 3/14)` |
| 4 | `MyCpuTop.scala` | 55 | `TODO(实现 4/14)` |
| 5 | `MyCpuTop.scala` | 60 | `TODO(实现 5/14)` |
| 6 | `MyCpuTop.scala` | 65 | `TODO(实现 6/14)` |
| 7 | `MyCpuTop.scala` | 70 | `TODO(实现 7/14)` |
| 8 | `MyCpuTop.scala` | 79 | `TODO(实现 8/14)` |
| 9 | `MyCpuTop.scala` | 84 | `TODO(实现 9/14)` |
| 10 | `MyCpuTop.scala` | 93 | `TODO(实现 10/14)` |
| 11 | `MyCpuTop.scala` | 98 | `TODO(实现 11/14)` |
| 12 | `MyCpuTop.scala` | 103 | `TODO(实现 12/14)` |
| 13 | `MyCpuTop.scala` | 109 | `TODO(实现 13/14)` |
| 14 | `MyCpuTop.scala` | 116 | `TODO(实现 14/14)` |

### 待操作：`MyCpuTop.scala:35` — `TODO(实现 1/14)`

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)#L35

```scala
  // --------------------------------------------------------------------------
  // ① IF 级：取指（pc / nextpc / 取指接口）
  // --------------------------------------------------------------------------
  // TODO(实现 1/14)：pc / nextpc / 取指接口   // <<< 待操作
  io.inst_sram_en    := ???
  io.inst_sram_we    := ???
  io.inst_sram_addr  := ???
  io.inst_sram_wdata := ???
```

### 待操作：`MyCpuTop.scala:44` — `TODO(实现 2/14)`

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)#L44

```scala
  // --------------------------------------------------------------------------
  // ② IF/ID 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 2/14)：IF/ID 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  //    本实验需**新增译码**：rdcntvl.w、rdcntvh.w、rdcntid（以及 BRK 等例外触发指令）。
```

### 待操作：`MyCpuTop.scala:50` — `TODO(实现 3/14)`

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)#L50

```scala
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  //    本实验需**新增译码**：rdcntvl.w、rdcntvh.w、rdcntid（以及 BRK 等例外触发指令）。
  // --------------------------------------------------------------------------
  // TODO(实现 3/14)：指令译码（含 rdcnt* 与例外触发指令）、立即数、寄存器堆读、分支判断   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:55` — `TODO(实现 4/14)`

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)#L55

```scala
  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 4/14)：ID/EX 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:60` — `TODO(实现 5/14)`

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)#L60

```scala
  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
  // TODO(实现 5/14)：ALU 运算   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:65` — `TODO(实现 6/14)`

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)#L65

```scala
  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 6/14)：EX/MEM 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（需检测 ALE 地址非对齐）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:70` — `TODO(实现 7/14)`

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)#L70

```scala
  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（需检测 ALE 地址非对齐）
  // --------------------------------------------------------------------------
  // TODO(实现 7/14)：数据访存接口 + ALE 检测   // <<< 待操作
  io.data_sram_en    := ???
  io.data_sram_we    := ???
  io.data_sram_addr  := ???
  io.data_sram_wdata := ???
```

### 待操作：`MyCpuTop.scala:79` — `TODO(实现 8/14)`

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)#L79

```scala
  // --------------------------------------------------------------------------
  // ⑧ MEM/WB 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 8/14)：MEM/WB 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:84` — `TODO(实现 9/14)`

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)#L84

```scala
  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
  // TODO(实现 9/14)：写回寄存器堆 + debug_wb_* 输出   // <<< 待操作
  io.debug_wb_pc       := ???
  io.debug_wb_rf_we    := ???
  io.debug_wb_rf_wnum  := ???
  io.debug_wb_rf_wdata := ???
```

### 待操作：`MyCpuTop.scala:93` — `TODO(实现 10/14)`

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)#L93

```scala
  // --------------------------------------------------------------------------
  // ⑩ 数据相关处理：前递 + load-use 阻塞
  // --------------------------------------------------------------------------
  // TODO(实现 10/14)：前递通路 + load-use 阻塞   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑪ 控制状态寄存器与 CSR 指令（exp12）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:98` — `TODO(实现 11/14)`

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)#L98

```scala
  // --------------------------------------------------------------------------
  // ⑪ 控制状态寄存器与 CSR 指令（exp12）
  // --------------------------------------------------------------------------
  // TODO(实现 11/14)：CSR 寄存器堆 + csrrd/csrwr/csrxchg   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑫ syscall 例外与 ertn（exp12）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:103` — `TODO(实现 12/14)`

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)#L103

```scala
  // --------------------------------------------------------------------------
  // ⑫ syscall 例外与 ertn（exp12）
  // --------------------------------------------------------------------------
  // TODO(实现 12/14)：syscall 例外入口 + ertn 返回   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑬ 新增例外（本实验核心之一）：ADEF、ALE、BRK、INE
  //    需在取指/译码/访存各处检测异常条件，并写入 ESTAT.Ecode、ERA、BADV 等。
```

### 待操作：`MyCpuTop.scala:109` — `TODO(实现 13/14)`

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)#L109

```scala
  // ⑬ 新增例外（本实验核心之一）：ADEF、ALE、BRK、INE
  //    需在取指/译码/访存各处检测异常条件，并写入 ESTAT.Ecode、ERA、BADV 等。
  // --------------------------------------------------------------------------
  // TODO(实现 13/14)：ADEF / ALE / BRK / INE 例外检测与处理   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑭ 中断与定时器/计数器（本实验核心之二）
  //    ECFG.LIE 与 ESTAT.IS 按位使能；TCFG/TVAL/TID 定时器；TICLR 清中断；
```

### 待操作：`MyCpuTop.scala:116` — `TODO(实现 14/14)`

> 源文件：[chisel/src/main/scala/exp13/student/MyCpuTop.scala](chisel/src/main/scala/exp13/student/MyCpuTop.scala)#L116

```scala
  //    ECFG.LIE 与 ESTAT.IS 按位使能；TCFG/TVAL/TID 定时器；TICLR 清中断；
  //    rdcntvl.w / rdcntvh.w / rdcntid 指令。
  // --------------------------------------------------------------------------
  // TODO(实现 14/14)：中断使能与仲裁 + 定时器 + rdcnt* 指令   // <<< 待操作
}
```

## 实验验收

{% include exp-accept.html %}

## 参考

{% include exp-refs.html %}

{% include exp-pager.html %}
