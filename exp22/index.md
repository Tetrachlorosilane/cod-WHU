---
layout: default
title: 实践任务22 · CPU 中集成 DCache
nav_order: 22
exp: 22
exp_intent: 从零实现
exp_todos: 19
exp_has_chisel: true
exp_orig_judge: `mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!`
exp_judge: TraceHarness：golden_trace 逐条比对
exp_needs_assets: true
---

# 实践任务22：CPU 中集成 DCache

{% include exp-nav.html %}

## 实验目标

1. 将实践任务20完成的 Cache 模块作为 **DCache** 集成到实践任务21完成的 CPU 中；
2. 在采用 AXI 总线的 SoC 验证环境里完成 exp22 对应 func（n1~n72）的功能验证。

## 关键代码

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现 + DCache）：myCPU —— 五级流水线，AXI 接口，集成 I/D Cache
//   对应原实验：code/myCPU/（原实验环境**不提供** myCPU 目录，由学生自己实现）
//
// 本实验（实践任务22）的要求：从零实现（在 exp21 基础上增量）：
//   把 exp20 实现的 Cache 作为 **DCache** 集成到 CPU 的访存通路上
//   （ICache 已在 exp21 集成），并处理写命中/写分配与脏行写回。
//   Chisel 版只给接口骨架 + 19 处 TODO(实现)，内部逻辑全部留空（???）。
//
// 实现提示（详细方案见原书 10.2.3）：
//   * DCache 与 CPU 的接口即 exp20 的 `cache` 接口（op=1 表示写）；
//   * 写命中：按 wstrb 更新行内数据并置脏；写 miss：先取行（写分配）再写；
//   * 替换脏行时通过 wr_req/wr_data（128 位整行）写回；
//   * 访存停顿：DCache 未完成时冻结 MEM/WB，且要与前递/例外/分支冲刷协调。
// ============================================================================

package exp22.student

import chisel3._
import chisel3.util._
import envlib.soc.LACpuAxi

class MyCpuTop extends LACpuAxi {
  // io 由基类 LACpuAxi 提供（类型 envlib.soc.CpuIOAxi）：ar/r/aw/w/b 五通道 + debug_wb_*

  // --------------------------------------------------------------------------
  // ① IF 级：取指（AR/R 通道）
  // --------------------------------------------------------------------------
// …（以下为待实现的 TODO 部分，见「待操作代码」）
```

### `SocLiteTop.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/exp22/soc/SocLiteTop.scala](chisel/src/main/scala/exp22/soc/SocLiteTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_axi/rtl/soc_lite_top.v（与 exp15~exp21 相同）
//
// SoC 装配在共享库 envlib.soc.SocAxiTop；本文件只把学生 CPU 绑上去。
// exp22 的 func 覆盖 n1~n72，CPU 内部在 ICache 之外再把 exp20 的 Cache 作为
// DCache 集成到访存通路；SoC 环境本身不变。

package exp22.soc

import envlib.soc.SocAxiTop
import exp22.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true,
  perfTest:   Boolean     = false
) extends SocAxiTop(() => new MyCpuTop, instInit, dataInit, simulation, perfTest)
```

### `MyCpuTbSpec.scala`（判据复现）

> 源文件：[chisel/src/test/scala/MyCpuTbSpec.scala](chisel/src/test/scala/MyCpuTbSpec.scala)

```scala
  behavior of "SocLiteTop + MyCpuTop (exp22, 集成 DCache)"

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

共 **19** 处，全部位于学生模块中；下表为索引，代码块给出每处的上下文。

| # | 文件 | 行号 | 标记 |
|---|---|---|---|
| 1 | `MyCpuTop.scala` | 30 | `TODO(实现 1/19)` |
| 2 | `MyCpuTop.scala` | 45 | `TODO(实现 2/19)` |
| 3 | `MyCpuTop.scala` | 50 | `TODO(实现 3/19)` |
| 4 | `MyCpuTop.scala` | 55 | `TODO(实现 4/19)` |
| 5 | `MyCpuTop.scala` | 60 | `TODO(实现 5/19)` |
| 6 | `MyCpuTop.scala` | 65 | `TODO(实现 6/19)` |
| 7 | `MyCpuTop.scala` | 70 | `TODO(实现 7/19)` |
| 8 | `MyCpuTop.scala` | 90 | `TODO(实现 8/19)` |
| 9 | `MyCpuTop.scala` | 95 | `TODO(实现 9/19)` |
| 10 | `MyCpuTop.scala` | 104 | `TODO(实现 10/19)` |
| 11 | `MyCpuTop.scala` | 109 | `TODO(实现 11/19)` |
| 12 | `MyCpuTop.scala` | 114 | `TODO(实现 12/19)` |
| 13 | `MyCpuTop.scala` | 119 | `TODO(实现 13/19)` |
| 14 | `MyCpuTop.scala` | 124 | `TODO(实现 14/19)` |
| 15 | `MyCpuTop.scala` | 129 | `TODO(实现 15/19)` |
| 16 | `MyCpuTop.scala` | 134 | `TODO(实现 16/19)` |
| 17 | `MyCpuTop.scala` | 139 | `TODO(实现 17/19)` |
| 18 | `MyCpuTop.scala` | 144 | `TODO(实现 18/19)` |
| 19 | `MyCpuTop.scala` | 151 | `TODO(实现 19/19)` |

### 待操作：`MyCpuTop.scala:30` — `TODO(实现 1/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L30

```scala
  // --------------------------------------------------------------------------
  // ① IF 级：取指（AR/R 通道）
  // --------------------------------------------------------------------------
  // TODO(实现 1/19)：pc / nextpc / 取指的 AR 与 R 通道   // <<< 待操作
  io.arid    := ???
  io.araddr  := ???
  io.arlen   := ???
  io.arsize  := ???
```

### 待操作：`MyCpuTop.scala:45` — `TODO(实现 2/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L45

```scala
  // --------------------------------------------------------------------------
  // ② IF/ID 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 2/19)：IF/ID 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断（含 5 条 TLB 指令）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:50` — `TODO(实现 3/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L50

```scala
  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断（含 5 条 TLB 指令）
  // --------------------------------------------------------------------------
  // TODO(实现 3/19)：指令译码、立即数、寄存器堆读、分支判断   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:55` — `TODO(实现 4/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L55

```scala
  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 4/19)：ID/EX 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:60` — `TODO(实现 5/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L60

```scala
  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
  // TODO(实现 5/19)：ALU 运算   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:65` — `TODO(实现 6/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L65

```scala
  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 6/19)：EX/MEM 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（AW/W/B 通道）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:70` — `TODO(实现 7/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L70

```scala
  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（AW/W/B 通道）
  // --------------------------------------------------------------------------
  // TODO(实现 7/19)：数据的 AW/W/B 通道 + ALE 检测   // <<< 待操作
  io.awid    := ???
  io.awaddr  := ???
  io.awlen   := ???
  io.awsize  := ???
```

### 待操作：`MyCpuTop.scala:90` — `TODO(实现 8/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L90

```scala
  // --------------------------------------------------------------------------
  // ⑧ MEM/WB 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 8/19)：MEM/WB 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:95` — `TODO(实现 9/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L95

```scala
  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
  // TODO(实现 9/19)：写回寄存器堆 + debug_wb_* 输出   // <<< 待操作
  io.debug_wb_pc       := ???
  io.debug_wb_rf_we    := ???
  io.debug_wb_rf_wnum  := ???
  io.debug_wb_rf_wdata := ???
```

### 待操作：`MyCpuTop.scala:104` — `TODO(实现 10/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L104

```scala
  // --------------------------------------------------------------------------
  // ⑩ 数据相关处理：前递 + load-use 阻塞
  // --------------------------------------------------------------------------
  // TODO(实现 10/19)：前递通路 + load-use 阻塞   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑪ CSR 寄存器堆与 CSR 指令（含 TLB 相关 CSR 与 DMW）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:109` — `TODO(实现 11/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L109

```scala
  // --------------------------------------------------------------------------
  // ⑪ CSR 寄存器堆与 CSR 指令（含 TLB 相关 CSR 与 DMW）
  // --------------------------------------------------------------------------
  // TODO(实现 11/19)：CSR 寄存器堆 + csrrd/csrwr/csrxchg   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:114` — `TODO(实现 12/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L114

```scala
  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn
  // --------------------------------------------------------------------------
  // TODO(实现 12/19)：例外入口 + ertn 返回   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:119` — `TODO(实现 13/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L119

```scala
  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器
  // --------------------------------------------------------------------------
  // TODO(实现 13/19)：中断使能与仲裁 + 定时器 + rdcnt* 指令   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑭ AXI 事务状态机（含 ID 管理）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:124` — `TODO(实现 14/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L124

```scala
  // --------------------------------------------------------------------------
  // ⑭ AXI 事务状态机（含 ID 管理）
  // --------------------------------------------------------------------------
  // TODO(实现 14/19)：AXI 读写事务状态机与 ID 管理   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑮ AXI 请求未完成时的流水线互锁
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:129` — `TODO(实现 15/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L129

```scala
  // --------------------------------------------------------------------------
  // ⑮ AXI 请求未完成时的流水线互锁
  // --------------------------------------------------------------------------
  // TODO(实现 15/19)：AXI 请求未完成时的流水线停顿/互锁   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑯ TLB 集成与 TLB 指令（exp18）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:134` — `TODO(实现 16/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L134

```scala
  // --------------------------------------------------------------------------
  // ⑯ TLB 集成与 TLB 指令（exp18）
  // --------------------------------------------------------------------------
  // TODO(实现 16/19)：TLB 例化 + TLBSRCH/TLBRD/TLBWR/TLBFILL/INVTLB   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑰ 虚实地址映射与 TLB 例外（exp19）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:139` — `TODO(实现 17/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L139

```scala
  // --------------------------------------------------------------------------
  // ⑰ 虚实地址映射与 TLB 例外（exp19）
  // --------------------------------------------------------------------------
  // TODO(实现 17/19)：DMW + TLB 地址转换 + TLB 例外（TLBR/PIL/PIS/PME/PPI）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑱ ICache 集成与 Burst 取指（exp21）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:144` — `TODO(实现 18/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L144

```scala
  // --------------------------------------------------------------------------
  // ⑱ ICache 集成与 Burst 取指（exp21）
  // --------------------------------------------------------------------------
  // TODO(实现 18/19)：ICache 例化 + Burst 读填充 + 取指停顿   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑲ DCache 集成（本实验核心）
  //    把 exp20 的 Cache 接到访存通路（op=1 写 / op=0 读），
```

### 待操作：`MyCpuTop.scala:151` — `TODO(实现 19/19)`

> 源文件：[chisel/src/main/scala/exp22/student/MyCpuTop.scala](chisel/src/main/scala/exp22/student/MyCpuTop.scala)#L151

```scala
  //    把 exp20 的 Cache 接到访存通路（op=1 写 / op=0 读），
  //    处理写命中/写分配、脏行写回与访存停顿。
  // --------------------------------------------------------------------------
  // TODO(实现 19/19)：DCache 例化 + 写通路 + 脏行写回 + 访存停顿   // <<< 待操作
}
```

## 实验验收

{% include exp-accept.html %}

## 参考

{% include exp-refs.html %}

{% include exp-pager.html %}
