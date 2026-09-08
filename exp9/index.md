---
layout: default
title: 实践任务9 · 前递技术解决相关引发的冲突
parent: 实践任务
nav_order: 9
---

# 实践任务9：前递技术解决相关引发的冲突

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](chisel/README.md) ｜ [逐行对照](chisel/MAPPING.md)

> **任务类型**：从零实现 ｜ **待操作代码**：10 处
>
> **代码目录**：`code/`（本站收录的原 Verilog 源码） ｜ `chisel/`（本站收录的 Chisel 版环境）

## 实验目标

1. 加入适当的**数据前递通路**来减少阻塞。
2. 运行 exp9 对应的 func，要求通过仿真和上板验证，并且**仿真运行时间较 exp8 有下降**。

## 关键代码

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp9/student/MyCpuTop.scala](chisel/src/main/scala/exp9/student/MyCpuTop.scala)（本站内副本）

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现 + 前递）：myCPU —— 20 条指令、五级流水线、用前递解决数据相关
//   对应原实验：code/myCPU/（原实验环境**不提供** myCPU 目录，由学生自己实现）
//
// 本实验（实践任务9）的教学意图 = 从零实现（在 exp8 基础上增量）：
//   把 exp8 中"用阻塞硬等"的 RAW 相关处理，改为**前递（forwarding / bypass）**：
//   当 EX 级需要的数据还在 EX/MEM 或 MEM/WB 流水寄存器里时，直接把结果旁路到 ALU 输入端，
//   从而避免流水线停顿。无法前递的情况（如 load-use 相关）仍需保留必要的阻塞。
//   Chisel 版只给接口骨架 + 10 处 TODO(实现)，内部逻辑全部留空（???）。
//
// 与 exp8 的差异：
//   1. 第 ⑩ 项从"阻塞"改为"前递 + 必要的 load-use 阻塞"；
//   2. 需要把 EX/MEM、MEM/WB 的结果与目的寄存器号送到 EX 级做旁路选择。
//
// 实现提示（详细方案见原书 5.1.3 / ../../chisel4agent/08-五级流水线CPU串讲.md）：
//   * 优先前递 EX/MEM 的结果（更近），其次 MEM/WB；
//   * 前递条件是「上游目的寄存器 != 0 且 上游写使能有效 且 上游 rd == 当前 rs/rt」；
//   * load 指令的数据在 MEM 级才可用，紧跟其后的使用指令仍需 1 拍阻塞（load-use hazard）。
// ============================================================================

package exp9.student

import chisel3._
import chisel3.util._
import envlib.soc.LACpu

class MyCpuTop extends LACpu {
  // io 由基类 LACpu 提供（类型 envlib.soc.CpuIO），端口与各实验 soc_lite_top.v 的例化端口一致：
  //   resetn / inst_sram_{en,we[3:0],addr,wdata,rdata} / data_sram_{en,we[3:0],addr,wdata,rdata}
  //   / debug_wb_{pc,rf_we[3:0],rf_wnum,rf_wdata}

  // --------------------------------------------------------------------------
  // ① IF 级：取指（pc / nextpc / 取指接口）
  // --------------------------------------------------------------------------
// …（以下为待实现的 TODO 部分，见「待操作代码」）
```

### `SocLiteTop.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/exp9/soc/SocLiteTop.scala](chisel/src/main/scala/exp9/soc/SocLiteTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_bram/rtl/soc_lite_top.v（与 exp7/exp8 相同）
//
// SoC 装配在共享库 envlib.soc.SocBramTop；本文件只把学生 CPU 绑上去。
// 与 exp8 的唯一区别是 func 程序与 CPU 内部的数据相关处理方式（前递 vs 阻塞），
// 环境本身不变。

package exp9.soc

import envlib.soc.SocBramTop
import exp9.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true
) extends SocBramTop(() => new MyCpuTop, instInit, dataInit, simulation)
```

### `MyCpuTbSpec.scala`（判据复现）

> 源文件：[chisel/src/test/scala/MyCpuTbSpec.scala](chisel/src/test/scala/MyCpuTbSpec.scala)

```scala
  behavior of "SocLiteTop + MyCpuTop (exp9, 前递解决数据相关)"

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
| 1 | `MyCpuTop.scala` | 36 | `TODO(实现 1/10)` |
| 2 | `MyCpuTop.scala` | 45 | `TODO(实现 2/10)` |
| 3 | `MyCpuTop.scala` | 50 | `TODO(实现 3/10)` |
| 4 | `MyCpuTop.scala` | 55 | `TODO(实现 4/10)` |
| 5 | `MyCpuTop.scala` | 60 | `TODO(实现 5/10)` |
| 6 | `MyCpuTop.scala` | 65 | `TODO(实现 6/10)` |
| 7 | `MyCpuTop.scala` | 70 | `TODO(实现 7/10)` |
| 8 | `MyCpuTop.scala` | 79 | `TODO(实现 8/10)` |
| 9 | `MyCpuTop.scala` | 84 | `TODO(实现 9/10)` |
| 10 | `MyCpuTop.scala` | 95 | `TODO(实现 10/10)` |

### 待操作：`MyCpuTop.scala:36` — `TODO(实现 1/10)`

> 源文件：[chisel/src/main/scala/exp9/student/MyCpuTop.scala](chisel/src/main/scala/exp9/student/MyCpuTop.scala)#L36

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

### 待操作：`MyCpuTop.scala:45` — `TODO(实现 2/10)`

> 源文件：[chisel/src/main/scala/exp9/student/MyCpuTop.scala](chisel/src/main/scala/exp9/student/MyCpuTop.scala)#L45

```scala
  // --------------------------------------------------------------------------
  // ② IF/ID 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 2/10)：IF/ID 流水寄存器（至少保存 pc、inst）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:50` — `TODO(实现 3/10)`

> 源文件：[chisel/src/main/scala/exp9/student/MyCpuTop.scala](chisel/src/main/scala/exp9/student/MyCpuTop.scala)#L50

```scala
  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  // --------------------------------------------------------------------------
  // TODO(实现 3/10)：指令译码、立即数生成、寄存器堆读、分支/跳转判断   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:55` — `TODO(实现 4/10)`

> 源文件：[chisel/src/main/scala/exp9/student/MyCpuTop.scala](chisel/src/main/scala/exp9/student/MyCpuTop.scala)#L55

```scala
  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 4/10)：ID/EX 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU（前递后的操作数在此参与运算）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:60` — `TODO(实现 5/10)`

> 源文件：[chisel/src/main/scala/exp9/student/MyCpuTop.scala](chisel/src/main/scala/exp9/student/MyCpuTop.scala)#L60

```scala
  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU（前递后的操作数在此参与运算）
  // --------------------------------------------------------------------------
  // TODO(实现 5/10)：ALU 运算   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:65` — `TODO(实现 6/10)`

> 源文件：[chisel/src/main/scala/exp9/student/MyCpuTop.scala](chisel/src/main/scala/exp9/student/MyCpuTop.scala)#L65

```scala
  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 6/10)：EX/MEM 流水寄存器（需保留 rd、写使能、结果，供前递使用）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（字节写使能 data_sram_we[3:0]）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:70` — `TODO(实现 7/10)`

> 源文件：[chisel/src/main/scala/exp9/student/MyCpuTop.scala](chisel/src/main/scala/exp9/student/MyCpuTop.scala)#L70

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

> 源文件：[chisel/src/main/scala/exp9/student/MyCpuTop.scala](chisel/src/main/scala/exp9/student/MyCpuTop.scala)#L79

```scala
  // --------------------------------------------------------------------------
  // ⑧ MEM/WB 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 8/10)：MEM/WB 流水寄存器（需保留 rd、写使能、结果，供前递使用）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:84` — `TODO(实现 9/10)`

> 源文件：[chisel/src/main/scala/exp9/student/MyCpuTop.scala](chisel/src/main/scala/exp9/student/MyCpuTop.scala)#L84

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

### 待操作：`MyCpuTop.scala:95` — `TODO(实现 10/10)`

> 源文件：[chisel/src/main/scala/exp9/student/MyCpuTop.scala](chisel/src/main/scala/exp9/student/MyCpuTop.scala)#L95

```scala
  //    EX 级操作数优先取 EX/MEM 的结果，其次 MEM/WB 的结果；
  //    load-use 相关仍需 1 拍阻塞。
  // --------------------------------------------------------------------------
  // TODO(实现 10/10)：前递通路 + load-use 阻塞   // <<< 待操作
}
```

## 实验验收

| 项 | 说明 |
|---|---|
| 原实验判据 | `mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!` |
| Chisel 版判据 | TraceHarness：trace 逐条比对 + 周期数下降 |
| 运行方式 | `cd chisel && ./mill chisel.test`（需 JDK 17 + Mill） |
| 运行所需运行件 | 本实验的 Chisel 测试会读取 `code/func/obj/inst_ram.mif`、`code/func/obj/data_ram.mif` 与 `code/gettrace/golden_trace.txt`。它们未直接入库（107MB），但已打包为 [`assets/sim-assets.tar.gz`](../assets/sim-assets.tar.gz)（13.6MB）：在仓库根执行 `node tools/unpack-assets.mjs` 即解包就位（可加 `--check` 只校验 sha256） |
| ⚠️ 未实测 | Chisel 代码为静态交付，未编译/仿真；逐行对照见 `chisel/MAPPING.md` |

## 参考

- 原任务说明：[`task.md`](task.md)（硬链接到 `taskvscode/exp9/5.1.3实践任务9-前递技术解决相关引发的冲突.md`）
- Chisel 环境说明：[`chisel/README.md`](chisel/README.md)（速览 / 步骤 / 判据 / 自检）
- Verilog ↔ Chisel 逐行对照：[`chisel/MAPPING.md`](chisel/MAPPING.md)
- 改写规范与核验记录：[CHISEL-CONVENTIONS.md](../CHISEL-CONVENTIONS.md)
- 原书（LoongArch 版）：https://bookdown.org/loongson/_book3/

---

[← 实践任务8](../exp8/index.md) ｜ [实践任务10 →](../exp10/index.md)
