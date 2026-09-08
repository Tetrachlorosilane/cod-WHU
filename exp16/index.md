---
layout: default
title: 实践任务16 · 完成 AXI 随机延迟验证
nav_title: 实践任务16 完成 AXI 随机延迟验证
nav_order: 16
---

# 实践任务16：完成 AXI 随机延迟验证

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](chisel/README.md) ｜ [逐行对照](chisel/MAPPING.md)

> **任务类型**：从零实现 ｜ **待操作代码**：15 处
>
> **代码目录**：`code/`（本站收录的原 Verilog 源码） ｜ `chisel/`（本站收录的 Chisel 版环境）

## 实验目标

1. 完善 AXI 总线接口设计，使其在采用 AXI 总线的 SoC 验证环境里完成 exp16 对应

## 关键代码

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)（本站内副本）

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现 + AXI 随机延迟）：myCPU —— 五级流水线，对外接口为 AXI4
//   对应原实验：code/myCPU/（原实验环境**不提供** myCPU 目录，由学生自己实现）
//
// 本实验（实践任务16）的教学意图 = 从零实现（在 exp15 基础上增量）：
//   在 exp15 的 AXI 接口基础上，使 CPU 能在**随机延迟**的响应下正确工作：
//   请求可能被"挡"若干拍才被接收（arready/awready/wready 随机拉低），
//   读数据/写响应也可能延迟返回（rvalid/bvalid 随机延后）。
//   Chisel 版只给接口骨架 + 15 处 TODO(实现)，内部逻辑全部留空（???）。
//
// 与 exp15 的差异（RTL 完全相同，见 ../MAPPING.md §2）：
//   * exp15 的 func 为固定延迟；exp16 的 func 会让 confreg 的
//     `ram_random_mask` 生效，从而在 AXI 侧制造随机延迟；
//   * 因此 CPU 不得假设"请求一拍必被接收"或"读数据一拍必回"，
//     必须用 valid/ready 握手与超时无关的等待逻辑。
// ============================================================================

package exp16.student

import chisel3._
import chisel3.util._
import envlib.soc.LACpuAxi

class MyCpuTop extends LACpuAxi {
  // io 由基类 LACpuAxi 提供（类型 envlib.soc.CpuIOAxi）：
  //   ar/r/aw/w/b 五通道 + debug_wb_*

  // --------------------------------------------------------------------------
  // ① IF 级：取指（AR/R 通道）
  // --------------------------------------------------------------------------
// …（以下为待实现的 TODO 部分，见「待操作代码」）
```

### `SocLiteTop.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/exp16/soc/SocLiteTop.scala](chisel/src/main/scala/exp16/soc/SocLiteTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_axi/rtl/soc_lite_top.v（与 exp15 相同）
//
// SoC 装配在共享库 envlib.soc.SocAxiTop；本文件只把学生 CPU 绑上去。
// exp16 的 func 覆盖 n1~n58，并在 AXI **随机延迟**下验证 CPU 的正确性。

package exp16.soc

import envlib.soc.SocAxiTop
import exp16.student.MyCpuTop

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
  behavior of "SocLiteTop + MyCpuTop (exp16, AXI 随机延迟)"

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

共 **15** 处，全部位于学生模块中；下表为索引，代码块给出每处的上下文。

| # | 文件 | 行号 | 标记 |
|---|---|---|---|
| 1 | `MyCpuTop.scala` | 32 | `TODO(实现 1/15)` |
| 2 | `MyCpuTop.scala` | 47 | `TODO(实现 2/15)` |
| 3 | `MyCpuTop.scala` | 52 | `TODO(实现 3/15)` |
| 4 | `MyCpuTop.scala` | 57 | `TODO(实现 4/15)` |
| 5 | `MyCpuTop.scala` | 62 | `TODO(实现 5/15)` |
| 6 | `MyCpuTop.scala` | 67 | `TODO(实现 6/15)` |
| 7 | `MyCpuTop.scala` | 72 | `TODO(实现 7/15)` |
| 8 | `MyCpuTop.scala` | 92 | `TODO(实现 8/15)` |
| 9 | `MyCpuTop.scala` | 97 | `TODO(实现 9/15)` |
| 10 | `MyCpuTop.scala` | 106 | `TODO(实现 10/15)` |
| 11 | `MyCpuTop.scala` | 111 | `TODO(实现 11/15)` |
| 12 | `MyCpuTop.scala` | 116 | `TODO(实现 12/15)` |
| 13 | `MyCpuTop.scala` | 121 | `TODO(实现 13/15)` |
| 14 | `MyCpuTop.scala` | 126 | `TODO(实现 14/15)` |
| 15 | `MyCpuTop.scala` | 134 | `TODO(实现 15/15)` |

### 待操作：`MyCpuTop.scala:32` — `TODO(实现 1/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L32

```scala
  // --------------------------------------------------------------------------
  // ① IF 级：取指（AR/R 通道）
  // --------------------------------------------------------------------------
  // TODO(实现 1/15)：pc / nextpc / 取指的 AR 与 R 通道   // <<< 待操作
  io.arid    := ???
  io.araddr  := ???
  io.arlen   := ???
  io.arsize  := ???
```

### 待操作：`MyCpuTop.scala:47` — `TODO(实现 2/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L47

```scala
  // --------------------------------------------------------------------------
  // ② IF/ID 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 2/15)：IF/ID 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:52` — `TODO(实现 3/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L52

```scala
  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  // --------------------------------------------------------------------------
  // TODO(实现 3/15)：指令译码、立即数、寄存器堆读、分支判断   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:57` — `TODO(实现 4/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L57

```scala
  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 4/15)：ID/EX 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:62` — `TODO(实现 5/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L62

```scala
  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
  // TODO(实现 5/15)：ALU 运算   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:67` — `TODO(实现 6/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L67

```scala
  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 6/15)：EX/MEM 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（AW/W/B 通道）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:72` — `TODO(实现 7/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L72

```scala
  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（AW/W/B 通道）
  // --------------------------------------------------------------------------
  // TODO(实现 7/15)：数据的 AW/W/B 通道 + ALE 检测   // <<< 待操作
  io.awid    := ???
  io.awaddr  := ???
  io.awlen   := ???
  io.awsize  := ???
```

### 待操作：`MyCpuTop.scala:92` — `TODO(实现 8/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L92

```scala
  // --------------------------------------------------------------------------
  // ⑧ MEM/WB 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 8/15)：MEM/WB 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:97` — `TODO(实现 9/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L97

```scala
  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
  // TODO(实现 9/15)：写回寄存器堆 + debug_wb_* 输出   // <<< 待操作
  io.debug_wb_pc       := ???
  io.debug_wb_rf_we    := ???
  io.debug_wb_rf_wnum  := ???
  io.debug_wb_rf_wdata := ???
```

### 待操作：`MyCpuTop.scala:106` — `TODO(实现 10/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L106

```scala
  // --------------------------------------------------------------------------
  // ⑩ 数据相关处理：前递 + load-use 阻塞
  // --------------------------------------------------------------------------
  // TODO(实现 10/15)：前递通路 + load-use 阻塞   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑪ 控制状态寄存器与 CSR 指令
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:111` — `TODO(实现 11/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L111

```scala
  // --------------------------------------------------------------------------
  // ⑪ 控制状态寄存器与 CSR 指令
  // --------------------------------------------------------------------------
  // TODO(实现 11/15)：CSR 寄存器堆 + csrrd/csrwr/csrxchg   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:116` — `TODO(实现 12/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L116

```scala
  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn
  // --------------------------------------------------------------------------
  // TODO(实现 12/15)：例外入口 + ertn 返回   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:121` — `TODO(实现 13/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L121

```scala
  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器
  // --------------------------------------------------------------------------
  // TODO(实现 13/15)：中断使能与仲裁 + 定时器 + rdcnt* 指令   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑭ AXI 事务状态机（含 ID 管理）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:126` — `TODO(实现 14/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L126

```scala
  // --------------------------------------------------------------------------
  // ⑭ AXI 事务状态机（含 ID 管理）
  // --------------------------------------------------------------------------
  // TODO(实现 14/15)：AXI 读写事务状态机与 ID 管理   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑮ 随机延迟下的正确性（本实验核心）
  //    请求被挡（ready 随机为低）时保持请求信号不变；
```

### 待操作：`MyCpuTop.scala:134` — `TODO(实现 15/15)`

> 源文件：[chisel/src/main/scala/exp16/student/MyCpuTop.scala](chisel/src/main/scala/exp16/student/MyCpuTop.scala)#L134

```scala
  //    读数据/写响应延迟返回时保持等待，不得提前推进流水线；
  //    多个在途请求时按 ID 匹配返回数据。
  // --------------------------------------------------------------------------
  // TODO(实现 15/15)：随机延迟下的等待/互锁与 ID 匹配   // <<< 待操作
}
```

## 实验验收

| 项 | 说明 |
|---|---|
| 原实验判据 | `mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!` |
| Chisel 版判据 | TraceHarness：随机延迟下 golden_trace 逐条比对 |
| 运行方式 | `cd chisel && ./mill chisel.test`（需 JDK 17 + Mill） |
| 运行所需运行件 | 本实验的 Chisel 测试会读取 `code/func/obj/inst_ram.mif`、`code/func/obj/data_ram.mif` 与 `code/gettrace/golden_trace.txt`。它们未直接入库（107MB），但已打包为 [`assets/sim-assets.tar.gz`](../assets/sim-assets.tar.gz)（13.6MB）：在仓库根执行 `node tools/unpack-assets.mjs` 即解包就位（可加 `--check` 只校验 sha256） |
| ⚠️ 未实测 | Chisel 代码为静态交付，未编译/仿真；逐行对照见 `chisel/MAPPING.md` |

## 参考

- 原任务说明：[`task.md`](task.md)（硬链接到 `taskvscode/exp16/8.1.3实践任务16-完成AXI随机延迟验证.md`）
- Chisel 环境说明：[`chisel/README.md`](chisel/README.md)（速览 / 步骤 / 判据 / 自检）
- Verilog ↔ Chisel 逐行对照：[`chisel/MAPPING.md`](chisel/MAPPING.md)
- 改写规范与核验记录：[CHISEL-CONVENTIONS.md](../CHISEL-CONVENTIONS.md)
- 原书（LoongArch 版）：https://bookdown.org/loongson/_book3/

---

[← 实践任务15](../exp15/index.md) ｜ [实践任务17 →](../exp17/index.md)
