---
layout: default
title: 实践任务18 · 添加 TLB 相关指令和 CSR 寄存器
nav_title: 实践任务18 添加 TLB 相关指令和 CSR 寄存器
nav_order: 18
---

# 实践任务18：添加 TLB 相关指令和 CSR 寄存器

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](chisel/README.md) ｜ [逐行对照](chisel/MAPPING.md)

> **任务类型**：从零实现 ｜ **待操作代码**：16 处
>
> **代码目录**：`code/`（本站收录的原 Verilog 源码） ｜ `chisel/`（本站收录的 Chisel 版环境）

## 实验目标

1. 将实践任务17完成的 TLB 模块集成到实践任务16完成的 CPU 中；
2. 在 CPU 中增加 `TLBSRCH`、`TLBRD`、`TLBWR`、`TLBFILL`、`INVTLB` 指令；
3. 在 CPU 中增加 `TLBIDX`、`TLBEHI`、`TLBELO0`、`TLBELO1`、`ASID`、`TLBRENTRY` CSR 寄存器；
4. 在采用 AXI 总线的 SoC 验证环境里完成 exp18 对应 func（n1~n70）的功能验证。

## 关键代码

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)（本站内副本）

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现 + TLB 指令/CSR）：myCPU —— 五级流水线，AXI 接口，集成 TLB
//   对应原实验：code/myCPU/（原实验环境**不提供** myCPU 目录，由学生自己实现）
//
// 本实验（实践任务18）的教学意图 = 从零实现（在 exp16 + exp17 基础上增量）：
//   ① 把 exp17 的 TLB 模块集成进 CPU；
//   ② 新增指令：TLBSRCH、TLBRD、TLBWR、TLBFILL、INVTLB；
//   ③ 新增 CSR：TLBIDX、TLBEHI、TLBELO0、TLBELO1、ASID、TLBRENTRY。
//   Chisel 版只给接口骨架 + 16 处 TODO(实现)，内部逻辑全部留空（???）。
//
// 实现提示（详细方案见原书 9.2.2 / ../../chisel4agent/09-CPU开发场景速查.md）：
//   * TLBIDX.Index / .NE / .PS；TLBEHI.VPPN / .V4；TLBELO0/1.PPN/PLV/MAT/G/D/V；
//     ASID.ASID；TLBRENTRY.PS/PPN/VA —— 与 exp17 的 Tlb 接口逐字段对应；
//   * TLBSRCH：用 TLBEHI.VPPN + ASID 查 TLB，命中则写 TLBIDX.Index；
//   * TLBRD：按 TLBIDX.Index 读表项到 TLBEHI/TLBELO0/TLBELO1/ASID；
//   * TLBWR：按 TLBIDX.Index 写表项；TLBFILL：写入随机/替换位置；
//   * INVTLB：按 invtlb_op 作废相应表项；
//   * 取指/访存需要经过 TLB 做虚实地址转换（本实验仅要求 TLB 指令本身可工作，
//     完整的地址映射在 exp19 实现）。
// ============================================================================

package exp18.student

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

> 源文件：[chisel/src/main/scala/exp18/soc/SocLiteTop.scala](chisel/src/main/scala/exp18/soc/SocLiteTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_axi/rtl/soc_lite_top.v（与 exp15/exp16 相同）
//
// SoC 装配在共享库 envlib.soc.SocAxiTop；本文件只把学生 CPU 绑上去。
// exp18 的 func 覆盖 n1~n70（新增 TLB 指令与 CSR），环境本身不变。

package exp18.soc

import envlib.soc.SocAxiTop
import exp18.student.MyCpuTop

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
  behavior of "SocLiteTop + MyCpuTop (exp18, TLB 指令与 CSR)"

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

共 **16** 处，全部位于学生模块中；下表为索引，代码块给出每处的上下文。

| # | 文件 | 行号 | 标记 |
|---|---|---|---|
| 1 | `MyCpuTop.scala` | 35 | `TODO(实现 1/16)` |
| 2 | `MyCpuTop.scala` | 50 | `TODO(实现 2/16)` |
| 3 | `MyCpuTop.scala` | 56 | `TODO(实现 3/16)` |
| 4 | `MyCpuTop.scala` | 61 | `TODO(实现 4/16)` |
| 5 | `MyCpuTop.scala` | 66 | `TODO(实现 5/16)` |
| 6 | `MyCpuTop.scala` | 71 | `TODO(实现 6/16)` |
| 7 | `MyCpuTop.scala` | 76 | `TODO(实现 7/16)` |
| 8 | `MyCpuTop.scala` | 96 | `TODO(实现 8/16)` |
| 9 | `MyCpuTop.scala` | 101 | `TODO(实现 9/16)` |
| 10 | `MyCpuTop.scala` | 110 | `TODO(实现 10/16)` |
| 11 | `MyCpuTop.scala` | 116 | `TODO(实现 11/16)` |
| 12 | `MyCpuTop.scala` | 121 | `TODO(实现 12/16)` |
| 13 | `MyCpuTop.scala` | 126 | `TODO(实现 13/16)` |
| 14 | `MyCpuTop.scala` | 131 | `TODO(实现 14/16)` |
| 15 | `MyCpuTop.scala` | 136 | `TODO(实现 15/16)` |
| 16 | `MyCpuTop.scala` | 147 | `TODO(实现 16/16)` |

### 待操作：`MyCpuTop.scala:35` — `TODO(实现 1/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L35

```scala
  // --------------------------------------------------------------------------
  // ① IF 级：取指（AR/R 通道）
  // --------------------------------------------------------------------------
  // TODO(实现 1/16)：pc / nextpc / 取指的 AR 与 R 通道   // <<< 待操作
  io.arid    := ???
  io.araddr  := ???
  io.arlen   := ???
  io.arsize  := ???
```

### 待操作：`MyCpuTop.scala:50` — `TODO(实现 2/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L50

```scala
  // --------------------------------------------------------------------------
  // ② IF/ID 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 2/16)：IF/ID 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  //    本实验需**新增译码**：TLBSRCH、TLBRD、TLBWR、TLBFILL、INVTLB。
```

### 待操作：`MyCpuTop.scala:56` — `TODO(实现 3/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L56

```scala
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  //    本实验需**新增译码**：TLBSRCH、TLBRD、TLBWR、TLBFILL、INVTLB。
  // --------------------------------------------------------------------------
  // TODO(实现 3/16)：指令译码（含 5 条 TLB 指令）、立即数、寄存器堆读、分支判断   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:61` — `TODO(实现 4/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L61

```scala
  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 4/16)：ID/EX 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:66` — `TODO(实现 5/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L66

```scala
  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
  // TODO(实现 5/16)：ALU 运算   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:71` — `TODO(实现 6/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L71

```scala
  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 6/16)：EX/MEM 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（AW/W/B 通道）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:76` — `TODO(实现 7/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L76

```scala
  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（AW/W/B 通道）
  // --------------------------------------------------------------------------
  // TODO(实现 7/16)：数据的 AW/W/B 通道 + ALE 检测   // <<< 待操作
  io.awid    := ???
  io.awaddr  := ???
  io.awlen   := ???
  io.awsize  := ???
```

### 待操作：`MyCpuTop.scala:96` — `TODO(实现 8/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L96

```scala
  // --------------------------------------------------------------------------
  // ⑧ MEM/WB 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 8/16)：MEM/WB 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:101` — `TODO(实现 9/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L101

```scala
  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
  // TODO(实现 9/16)：写回寄存器堆 + debug_wb_* 输出   // <<< 待操作
  io.debug_wb_pc       := ???
  io.debug_wb_rf_we    := ???
  io.debug_wb_rf_wnum  := ???
  io.debug_wb_rf_wdata := ???
```

### 待操作：`MyCpuTop.scala:110` — `TODO(实现 10/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L110

```scala
  // --------------------------------------------------------------------------
  // ⑩ 数据相关处理：前递 + load-use 阻塞
  // --------------------------------------------------------------------------
  // TODO(实现 10/16)：前递通路 + load-use 阻塞   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑪ CSR 寄存器堆与 CSR 指令
  //    本实验需**新增 CSR**：TLBIDX、TLBEHI、TLBELO0、TLBELO1、ASID、TLBRENTRY。
```

### 待操作：`MyCpuTop.scala:116` — `TODO(实现 11/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L116

```scala
  // ⑪ CSR 寄存器堆与 CSR 指令
  //    本实验需**新增 CSR**：TLBIDX、TLBEHI、TLBELO0、TLBELO1、ASID、TLBRENTRY。
  // --------------------------------------------------------------------------
  // TODO(实现 11/16)：CSR 寄存器堆（含 6 个新增 TLB 相关 CSR）+ csrrd/csrwr/csrxchg   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:121` — `TODO(实现 12/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L121

```scala
  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn
  // --------------------------------------------------------------------------
  // TODO(实现 12/16)：例外入口 + ertn 返回   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:126` — `TODO(实现 13/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L126

```scala
  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器
  // --------------------------------------------------------------------------
  // TODO(实现 13/16)：中断使能与仲裁 + 定时器 + rdcnt* 指令   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑭ AXI 事务状态机（含 ID 管理）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:131` — `TODO(实现 14/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L131

```scala
  // --------------------------------------------------------------------------
  // ⑭ AXI 事务状态机（含 ID 管理）
  // --------------------------------------------------------------------------
  // TODO(实现 14/16)：AXI 读写事务状态机与 ID 管理   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑮ AXI 请求未完成时的流水线互锁
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:136` — `TODO(实现 15/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L136

```scala
  // --------------------------------------------------------------------------
  // ⑮ AXI 请求未完成时的流水线互锁
  // --------------------------------------------------------------------------
  // TODO(实现 15/16)：AXI 请求未完成时的流水线停顿/互锁   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑯ TLB 集成与 TLB 指令（本实验核心）
  //    做法：把 exp17 的 TLB 实现复制到本实验目录并改用本包名，例如
```

### 待操作：`MyCpuTop.scala:147` — `TODO(实现 16/16)`

> 源文件：[chisel/src/main/scala/exp18/student/MyCpuTop.scala](chisel/src/main/scala/exp18/student/MyCpuTop.scala)#L147

```scala
  //      （或在 `MyCpuTop` 内直接实现 TLB）。
  //    然后把 TLBSRCH/TLBRD/TLBWR/TLBFILL/INVTLB 接到 TLB 的查找/读/写端口。
  // --------------------------------------------------------------------------
  // TODO(实现 16/16)：TLB 例化 + 5 条 TLB 指令的数据通路   // <<< 待操作
}
```

## 实验验收

| 项 | 说明 |
|---|---|
| 原实验判据 | `mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!` |
| Chisel 版判据 | TraceHarness：golden_trace 逐条比对 |
| 运行方式 | `cd chisel && ./mill chisel.test`（需 JDK 17 + Mill） |
| 运行所需运行件 | 本实验的 Chisel 测试会读取 `code/func/obj/inst_ram.mif`、`code/func/obj/data_ram.mif` 与 `code/gettrace/golden_trace.txt`。它们未直接入库（107MB），但已打包为 [`assets/sim-assets.tar.gz`](../assets/sim-assets.tar.gz)（13.6MB）：在仓库根执行 `node tools/unpack-assets.mjs` 即解包就位（可加 `--check` 只校验 sha256） |
| ✅ 编译验证 | `chisel.compile` / `chisel.test.compile` 已用 Mill 1.0.4 + JDK 17 + Chisel 3.5.6 实测通过（**未跑仿真**）；复查报告见 [编译复查报告](../编译复查报告.md) |

## 参考

- 原任务说明：[`task.md`](task.md)（硬链接到 `taskvscode/exp18/9.2.2实践任务18-添加TLB相关指令和CSR寄存器.md`）
- Chisel 环境说明：[`chisel/README.md`](chisel/README.md)（速览 / 步骤 / 判据 / 自检）
- Verilog ↔ Chisel 逐行对照：[`chisel/MAPPING.md`](chisel/MAPPING.md)
- 改写规范与核验记录：[CHISEL-CONVENTIONS.md](../CHISEL-CONVENTIONS.md)
- 原书（LoongArch 版）：https://bookdown.org/loongson/_book3/

---

[← 实践任务17](../exp17/index.md) ｜ [实践任务19 →](../exp19/index.md)
