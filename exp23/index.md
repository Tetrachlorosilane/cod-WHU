---
layout: default
title: 实践任务23 · CPU 中添加 CACOP 指令
nav_title: 实践任务23 CPU 中添加 CACOP 指令
nav_order: 23
---

# 实践任务23：CPU 中添加 CACOP 指令

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](chisel/README.md) ｜ [逐行对照](chisel/MAPPING.md)

> **任务类型**：从零实现 ｜ **待操作代码**：20 处
>
> **代码目录**：`code/`（本站收录的原 Verilog 源码） ｜ `chisel/`（本站收录的 Chisel 版环境）

## 实验目标

1. 在实践任务22完成的 CPU 中增加 **CACOP 指令**实现；
2. 在采用 AXI 总线的 SoC 验证环境里完成 exp23 对应 func（n1~n79）的功能验证。

## 关键代码

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)（本站内副本）

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现 + CACOP）：myCPU —— 五级流水线，AXI 接口，I/D Cache，CACOP
//   对应原实验：code/myCPU/（原实验环境**不提供** myCPU 目录，由学生自己实现）
//
// 本实验（实践任务23）的教学意图 = 从零实现（在 exp22 基础上增量）：
//   在已集成 I/D Cache 的 CPU 中增加 **CACOP 指令**（Cache 操作）实现。
//   Chisel 版只给接口骨架 + 20 处 TODO(实现)，内部逻辑全部留空（???）。
//
// CACOP 语义（详细见原书 10.2.4 / ../../chisel4agent/09-CPU开发场景速查.md）：
//   * 指令格式：`cacop code, rj, si12`，其中 `code[4:3]` 选择 Cache：
//     0 = 按地址操作 ICache，1 = 按地址操作 DCache，
//     2 = 按索引操作 ICache，3 = 按索引操作 DCache；
//     `code[2:0]` 为具体操作（如 Store Tag / 无效化 / 写回并无效化等）；
//   * 按地址操作时，虚地址为 `rj + si12`，需要先做地址转换（DMW/TLB）；
//   * 按索引操作时，用虚地址中的 index 字段直接定位 Cache 行；
//   * 操作需要与流水线配合：可能要停顿，并保证后续取指/访存看到一致的数据。
// ============================================================================

package exp23.student

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

> 源文件：[chisel/src/main/scala/exp23/soc/SocLiteTop.scala](chisel/src/main/scala/exp23/soc/SocLiteTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_axi/rtl/soc_lite_top.v（与 exp15~exp22 相同）
//
// SoC 装配在共享库 envlib.soc.SocAxiTop；本文件只把学生 CPU 绑上去。
// exp23 的 func 覆盖 n1~n79，CPU 内部在 I/D Cache 之外再支持 CACOP 指令；
// SoC 环境本身不变。

package exp23.soc

import envlib.soc.SocAxiTop
import exp23.student.MyCpuTop

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
  behavior of "SocLiteTop + MyCpuTop (exp23, CACOP 指令)"

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

共 **20** 处，全部位于学生模块中；下表为索引，代码块给出每处的上下文。

| # | 文件 | 行号 | 标记 |
|---|---|---|---|
| 1 | `MyCpuTop.scala` | 32 | `TODO(实现 1/20)` |
| 2 | `MyCpuTop.scala` | 47 | `TODO(实现 2/20)` |
| 3 | `MyCpuTop.scala` | 53 | `TODO(实现 3/20)` |
| 4 | `MyCpuTop.scala` | 58 | `TODO(实现 4/20)` |
| 5 | `MyCpuTop.scala` | 63 | `TODO(实现 5/20)` |
| 6 | `MyCpuTop.scala` | 68 | `TODO(实现 6/20)` |
| 7 | `MyCpuTop.scala` | 73 | `TODO(实现 7/20)` |
| 8 | `MyCpuTop.scala` | 93 | `TODO(实现 8/20)` |
| 9 | `MyCpuTop.scala` | 98 | `TODO(实现 9/20)` |
| 10 | `MyCpuTop.scala` | 107 | `TODO(实现 10/20)` |
| 11 | `MyCpuTop.scala` | 112 | `TODO(实现 11/20)` |
| 12 | `MyCpuTop.scala` | 117 | `TODO(实现 12/20)` |
| 13 | `MyCpuTop.scala` | 122 | `TODO(实现 13/20)` |
| 14 | `MyCpuTop.scala` | 127 | `TODO(实现 14/20)` |
| 15 | `MyCpuTop.scala` | 132 | `TODO(实现 15/20)` |
| 16 | `MyCpuTop.scala` | 137 | `TODO(实现 16/20)` |
| 17 | `MyCpuTop.scala` | 142 | `TODO(实现 17/20)` |
| 18 | `MyCpuTop.scala` | 147 | `TODO(实现 18/20)` |
| 19 | `MyCpuTop.scala` | 152 | `TODO(实现 19/20)` |
| 20 | `MyCpuTop.scala` | 159 | `TODO(实现 20/20)` |

### 待操作：`MyCpuTop.scala:32` — `TODO(实现 1/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L32

```scala
  // --------------------------------------------------------------------------
  // ① IF 级：取指（AR/R 通道）
  // --------------------------------------------------------------------------
  // TODO(实现 1/20)：pc / nextpc / 取指的 AR 与 R 通道   // <<< 待操作
  io.arid    := ???
  io.araddr  := ???
  io.arlen   := ???
  io.arsize  := ???
```

### 待操作：`MyCpuTop.scala:47` — `TODO(实现 2/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L47

```scala
  // --------------------------------------------------------------------------
  // ② IF/ID 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 2/20)：IF/ID 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  //    本实验需**新增译码**：CACOP。
```

### 待操作：`MyCpuTop.scala:53` — `TODO(实现 3/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L53

```scala
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  //    本实验需**新增译码**：CACOP。
  // --------------------------------------------------------------------------
  // TODO(实现 3/20)：指令译码（含 CACOP）、立即数、寄存器堆读、分支判断   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:58` — `TODO(实现 4/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L58

```scala
  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 4/20)：ID/EX 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:63` — `TODO(实现 5/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L63

```scala
  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
  // TODO(实现 5/20)：ALU 运算   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:68` — `TODO(实现 6/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L68

```scala
  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 6/20)：EX/MEM 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（AW/W/B 通道）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:73` — `TODO(实现 7/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L73

```scala
  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（AW/W/B 通道）
  // --------------------------------------------------------------------------
  // TODO(实现 7/20)：数据的 AW/W/B 通道 + ALE 检测   // <<< 待操作
  io.awid    := ???
  io.awaddr  := ???
  io.awlen   := ???
  io.awsize  := ???
```

### 待操作：`MyCpuTop.scala:93` — `TODO(实现 8/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L93

```scala
  // --------------------------------------------------------------------------
  // ⑧ MEM/WB 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 8/20)：MEM/WB 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:98` — `TODO(实现 9/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L98

```scala
  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
  // TODO(实现 9/20)：写回寄存器堆 + debug_wb_* 输出   // <<< 待操作
  io.debug_wb_pc       := ???
  io.debug_wb_rf_we    := ???
  io.debug_wb_rf_wnum  := ???
  io.debug_wb_rf_wdata := ???
```

### 待操作：`MyCpuTop.scala:107` — `TODO(实现 10/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L107

```scala
  // --------------------------------------------------------------------------
  // ⑩ 数据相关处理：前递 + load-use 阻塞
  // --------------------------------------------------------------------------
  // TODO(实现 10/20)：前递通路 + load-use 阻塞   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑪ CSR 寄存器堆与 CSR 指令（含 TLB 相关 CSR 与 DMW）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:112` — `TODO(实现 11/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L112

```scala
  // --------------------------------------------------------------------------
  // ⑪ CSR 寄存器堆与 CSR 指令（含 TLB 相关 CSR 与 DMW）
  // --------------------------------------------------------------------------
  // TODO(实现 11/20)：CSR 寄存器堆 + csrrd/csrwr/csrxchg   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:117` — `TODO(实现 12/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L117

```scala
  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn
  // --------------------------------------------------------------------------
  // TODO(实现 12/20)：例外入口 + ertn 返回   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:122` — `TODO(实现 13/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L122

```scala
  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器
  // --------------------------------------------------------------------------
  // TODO(实现 13/20)：中断使能与仲裁 + 定时器 + rdcnt* 指令   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑭ AXI 事务状态机（含 ID 管理）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:127` — `TODO(实现 14/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L127

```scala
  // --------------------------------------------------------------------------
  // ⑭ AXI 事务状态机（含 ID 管理）
  // --------------------------------------------------------------------------
  // TODO(实现 14/20)：AXI 读写事务状态机与 ID 管理   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑮ AXI 请求未完成时的流水线互锁
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:132` — `TODO(实现 15/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L132

```scala
  // --------------------------------------------------------------------------
  // ⑮ AXI 请求未完成时的流水线互锁
  // --------------------------------------------------------------------------
  // TODO(实现 15/20)：AXI 请求未完成时的流水线停顿/互锁   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑯ TLB 集成与 TLB 指令（exp18）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:137` — `TODO(实现 16/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L137

```scala
  // --------------------------------------------------------------------------
  // ⑯ TLB 集成与 TLB 指令（exp18）
  // --------------------------------------------------------------------------
  // TODO(实现 16/20)：TLB 例化 + TLBSRCH/TLBRD/TLBWR/TLBFILL/INVTLB   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑰ 虚实地址映射与 TLB 例外（exp19）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:142` — `TODO(实现 17/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L142

```scala
  // --------------------------------------------------------------------------
  // ⑰ 虚实地址映射与 TLB 例外（exp19）
  // --------------------------------------------------------------------------
  // TODO(实现 17/20)：DMW + TLB 地址转换 + TLB 例外（TLBR/PIL/PIS/PME/PPI）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑱ ICache 集成与 Burst 取指（exp21）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:147` — `TODO(实现 18/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L147

```scala
  // --------------------------------------------------------------------------
  // ⑱ ICache 集成与 Burst 取指（exp21）
  // --------------------------------------------------------------------------
  // TODO(实现 18/20)：ICache 例化 + Burst 读填充 + 取指停顿   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑲ DCache 集成（exp22）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:152` — `TODO(实现 19/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L152

```scala
  // --------------------------------------------------------------------------
  // ⑲ DCache 集成（exp22）
  // --------------------------------------------------------------------------
  // TODO(实现 19/20)：DCache 例化 + 写通路 + 脏行写回 + 访存停顿   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑳ CACOP 指令（本实验核心）
  //    按 code[4:3] 选择 ICache/DCache 与"按地址/按索引"，
```

### 待操作：`MyCpuTop.scala:159` — `TODO(实现 20/20)`

> 源文件：[chisel/src/main/scala/exp23/student/MyCpuTop.scala](chisel/src/main/scala/exp23/student/MyCpuTop.scala)#L159

```scala
  //    按 code[4:3] 选择 ICache/DCache 与"按地址/按索引"，
  //    按 code[2:0] 执行具体 Cache 操作；按地址操作需先做地址转换。
  // --------------------------------------------------------------------------
  // TODO(实现 20/20)：CACOP 译码 + Cache 操作通路 + 流水线配合   // <<< 待操作
}
```

## 实验验收

| 项 | 说明 |
|---|---|
| 原实验判据 | `mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!` |
| Chisel 版判据 | TraceHarness：golden_trace 逐条比对 |
| 运行方式 | `cd chisel && ./mill chisel.test`（需 JDK 17 + Mill） |
| 运行所需运行件 | 本实验的 Chisel 测试会读取 `code/func/obj/inst_ram.mif`、`code/func/obj/data_ram.mif` 与 `code/gettrace/golden_trace.txt`。它们未直接入库（107MB），但已打包为 [`assets/sim-assets.tar.gz`](../assets/sim-assets.tar.gz)（13.6MB）：在仓库根执行 `node tools/unpack-assets.mjs` 即解包就位（可加 `--check` 只校验 sha256） |
| ⚠️ 未实测 | Chisel 代码为静态交付，未编译/仿真；逐行对照见 `chisel/MAPPING.md` |

## 参考

- 原任务说明：[`task.md`](task.md)（硬链接到 `taskvscode/exp23/10.2.4实践任务23-CPU中添加CACOP指令.md`）
- Chisel 环境说明：[`chisel/README.md`](chisel/README.md)（速览 / 步骤 / 判据 / 自检）
- Verilog ↔ Chisel 逐行对照：[`chisel/MAPPING.md`](chisel/MAPPING.md)
- 改写规范与核验记录：[CHISEL-CONVENTIONS.md](../CHISEL-CONVENTIONS.md)
- 原书（LoongArch 版）：https://bookdown.org/loongson/_book3/

---

[← 实践任务22](../exp22/index.md)
