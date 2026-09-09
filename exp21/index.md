---
layout: default
title: '实践任务21 · 在 CPU 中集成 ICache'
nav_order: 21
exp: 21
exp_intent: '从零实现'
exp_todos: 18
exp_has_chisel: true
exp_orig_judge: '`mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!`'
exp_judge: 'TraceHarness：golden_trace 逐条比对'
exp_needs_assets: true
---

# 实践任务21：在 CPU 中集成 ICache

{% include exp-nav.html %}

## 实验目标

1. 将实践任务20完成的 Cache 模块作为 **ICache** 集成到实践任务19完成的 CPU 中；
2. 修改 CPU 中的 **AXI 转换桥，以支持 Burst 传输**；
3. 在采用 AXI 总线的 SoC 验证环境里完成 exp21 对应 func（n1~n72）的功能验证。

## 提示与易错点

> **⚠️ 易错**：取指走 Cache 后，miss 期间 pc 必须保持——否则会重复取指或丢掉指令。
>
> **💡 概念**：ICache 是只读的：命中不产生写回，但 `CACOP` 指令要能作废表项。
>
> **⚠️ 易错**：命中判断用物理地址（P 位）；TLB 翻译与 Cache 访问的先后顺序按原书规定，不要自行调换。
>

## 关键代码

下面给出本实验的接口骨架与环境代码。请**先读懂注释里的接口约定**再动手：接口理解错，后面的调试会非常费时。

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现 + ICache）：myCPU —— 五级流水线，AXI 接口，集成 ICache
//   对应原实验：code/myCPU/（原实验环境**不提供** myCPU 目录，由学生自己实现）
//
// 本实验（实践任务21）的要求：从零实现（在 exp19 + exp20 基础上增量）：
//   ① 把 exp20 实现的 Cache 作为 **ICache** 集成到 CPU 的取指通路上；
//   ② 修改 CPU 中的 AXI 转换桥，使其支持 **Burst 传输**（一次 AR 取回整行）；
//   Chisel 版只给接口骨架 + 18 处 TODO(实现)，内部逻辑全部留空（???）。
//
// 实现提示（详细方案见原书 10.2.2）：
//   * ICache 与 CPU 的接口即 exp20 的 `cache` 接口：CPU 侧 valid/op/index/tag/offset/
//     wstrb/wdata → addr_ok/data_ok/rdata；总线侧 rd_req/rd_type/rd_addr →
//     ret_valid/ret_last/ret_data（读填充），wr_req/... → wr_rdy（写回，ICache 通常只读）；
//   * Burst：把 4 拍 32 位读合并为一次 `arlen=3` 的 INCR 突发，用 `rlast` 判断结束；
//   * 取指需要处理 ICache miss 导致的流水线停顿（与分支/例外冲刷的优先级）。
// ============================================================================

package exp21.student

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

> 源文件：[chisel/src/main/scala/exp21/soc/SocLiteTop.scala](chisel/src/main/scala/exp21/soc/SocLiteTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_axi/rtl/soc_lite_top.v（与 exp15~exp19 相同）
//
// SoC 装配在共享库 envlib.soc.SocAxiTop；本文件只把学生 CPU 绑上去。
// exp21 的 func 覆盖 n1~n72，CPU 内部把 exp20 的 Cache 作为 ICache 集成，
// 并让 AXI 转换桥支持 Burst 传输；SoC 环境本身不变。

package exp21.soc

import envlib.soc.SocAxiTop
import exp21.student.MyCpuTop

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
  behavior of "SocLiteTop + MyCpuTop (exp21, 集成 ICache)"

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

下表列出本实验全部需要补全的位置，每处都附了上下文。请对照教材与本实验的 `chisel/README.md` 逐项完成。

共 **18** 处，全部位于学生模块中；下表为索引，代码块给出每处的上下文。

| # | 文件 | 行号 | 标记 |
|---|---|---|---|
| 1 | `MyCpuTop.scala` | 31 | `TODO(实现 1/18)` |
| 2 | `MyCpuTop.scala` | 46 | `TODO(实现 2/18)` |
| 3 | `MyCpuTop.scala` | 51 | `TODO(实现 3/18)` |
| 4 | `MyCpuTop.scala` | 56 | `TODO(实现 4/18)` |
| 5 | `MyCpuTop.scala` | 61 | `TODO(实现 5/18)` |
| 6 | `MyCpuTop.scala` | 66 | `TODO(实现 6/18)` |
| 7 | `MyCpuTop.scala` | 71 | `TODO(实现 7/18)` |
| 8 | `MyCpuTop.scala` | 91 | `TODO(实现 8/18)` |
| 9 | `MyCpuTop.scala` | 96 | `TODO(实现 9/18)` |
| 10 | `MyCpuTop.scala` | 105 | `TODO(实现 10/18)` |
| 11 | `MyCpuTop.scala` | 110 | `TODO(实现 11/18)` |
| 12 | `MyCpuTop.scala` | 115 | `TODO(实现 12/18)` |
| 13 | `MyCpuTop.scala` | 120 | `TODO(实现 13/18)` |
| 14 | `MyCpuTop.scala` | 125 | `TODO(实现 14/18)` |
| 15 | `MyCpuTop.scala` | 130 | `TODO(实现 15/18)` |
| 16 | `MyCpuTop.scala` | 135 | `TODO(实现 16/18)` |
| 17 | `MyCpuTop.scala` | 140 | `TODO(实现 17/18)` |
| 18 | `MyCpuTop.scala` | 147 | `TODO(实现 18/18)` |

### 待操作：`MyCpuTop.scala:31` — `TODO(实现 1/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L31

```scala
  // --------------------------------------------------------------------------
  // ① IF 级：取指（AR/R 通道）
  // --------------------------------------------------------------------------
  // TODO(实现 1/18)：pc / nextpc / 取指的 AR 与 R 通道   // <<< 待操作
  io.arid    := ???
  io.araddr  := ???
  io.arlen   := ???
  io.arsize  := ???
```

### 待操作：`MyCpuTop.scala:46` — `TODO(实现 2/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L46

```scala
  // --------------------------------------------------------------------------
  // ② IF/ID 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 2/18)：IF/ID 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断（含 5 条 TLB 指令）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:51` — `TODO(实现 3/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L51

```scala
  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断（含 5 条 TLB 指令）
  // --------------------------------------------------------------------------
  // TODO(实现 3/18)：指令译码、立即数、寄存器堆读、分支判断   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:56` — `TODO(实现 4/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L56

```scala
  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 4/18)：ID/EX 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:61` — `TODO(实现 5/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L61

```scala
  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
  // TODO(实现 5/18)：ALU 运算   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:66` — `TODO(实现 6/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L66

```scala
  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 6/18)：EX/MEM 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（AW/W/B 通道）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:71` — `TODO(实现 7/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L71

```scala
  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（AW/W/B 通道）
  // --------------------------------------------------------------------------
  // TODO(实现 7/18)：数据的 AW/W/B 通道 + ALE 检测   // <<< 待操作
  io.awid    := ???
  io.awaddr  := ???
  io.awlen   := ???
  io.awsize  := ???
```

### 待操作：`MyCpuTop.scala:91` — `TODO(实现 8/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L91

```scala
  // --------------------------------------------------------------------------
  // ⑧ MEM/WB 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 8/18)：MEM/WB 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:96` — `TODO(实现 9/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L96

```scala
  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
  // TODO(实现 9/18)：写回寄存器堆 + debug_wb_* 输出   // <<< 待操作
  io.debug_wb_pc       := ???
  io.debug_wb_rf_we    := ???
  io.debug_wb_rf_wnum  := ???
  io.debug_wb_rf_wdata := ???
```

### 待操作：`MyCpuTop.scala:105` — `TODO(实现 10/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L105

```scala
  // --------------------------------------------------------------------------
  // ⑩ 数据相关处理：前递 + load-use 阻塞
  // --------------------------------------------------------------------------
  // TODO(实现 10/18)：前递通路 + load-use 阻塞   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑪ CSR 寄存器堆与 CSR 指令（含 TLB 相关 CSR 与 DMW）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:110` — `TODO(实现 11/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L110

```scala
  // --------------------------------------------------------------------------
  // ⑪ CSR 寄存器堆与 CSR 指令（含 TLB 相关 CSR 与 DMW）
  // --------------------------------------------------------------------------
  // TODO(实现 11/18)：CSR 寄存器堆 + csrrd/csrwr/csrxchg   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:115` — `TODO(实现 12/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L115

```scala
  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn
  // --------------------------------------------------------------------------
  // TODO(实现 12/18)：例外入口 + ertn 返回   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:120` — `TODO(实现 13/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L120

```scala
  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器
  // --------------------------------------------------------------------------
  // TODO(实现 13/18)：中断使能与仲裁 + 定时器 + rdcnt* 指令   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑭ AXI 事务状态机（含 ID 管理）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:125` — `TODO(实现 14/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L125

```scala
  // --------------------------------------------------------------------------
  // ⑭ AXI 事务状态机（含 ID 管理）
  // --------------------------------------------------------------------------
  // TODO(实现 14/18)：AXI 读写事务状态机与 ID 管理   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑮ AXI 请求未完成时的流水线互锁
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:130` — `TODO(实现 15/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L130

```scala
  // --------------------------------------------------------------------------
  // ⑮ AXI 请求未完成时的流水线互锁
  // --------------------------------------------------------------------------
  // TODO(实现 15/18)：AXI 请求未完成时的流水线停顿/互锁   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑯ TLB 集成与 TLB 指令（exp18）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:135` — `TODO(实现 16/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L135

```scala
  // --------------------------------------------------------------------------
  // ⑯ TLB 集成与 TLB 指令（exp18）
  // --------------------------------------------------------------------------
  // TODO(实现 16/18)：TLB 例化 + TLBSRCH/TLBRD/TLBWR/TLBFILL/INVTLB   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑰ 虚实地址映射与 TLB 例外（exp19）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:140` — `TODO(实现 17/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L140

```scala
  // --------------------------------------------------------------------------
  // ⑰ 虚实地址映射与 TLB 例外（exp19）
  // --------------------------------------------------------------------------
  // TODO(实现 17/18)：DMW + TLB 地址转换 + TLB 例外（TLBR/PIL/PIS/PME/PPI）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑱ ICache 集成与 Burst 取指（本实验核心）
  //    例化 exp20 的 Cache 作为 ICache；把 AXI 读改为 Burst（一次 AR 取回整行）；
```

### 待操作：`MyCpuTop.scala:147` — `TODO(实现 18/18)`

> 源文件：[chisel/src/main/scala/exp21/student/MyCpuTop.scala](chisel/src/main/scala/exp21/student/MyCpuTop.scala)#L147

```scala
  //    例化 exp20 的 Cache 作为 ICache；把 AXI 读改为 Burst（一次 AR 取回整行）；
  //    miss 时停顿取指，直到整行填充完成。
  // --------------------------------------------------------------------------
  // TODO(实现 18/18)：ICache 例化 + Burst 读填充 + 取指停顿   // <<< 待操作
}
```

## 实验验收

完成后按下面的判据自检——能复现原实验的验收条件，才算真正完成。

{% include exp-accept.html %}

## 参考

{% include exp-refs.html %}

{% include exp-pager.html %}
