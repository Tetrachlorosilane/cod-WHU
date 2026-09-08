---
layout: default
title: 实践任务14 · 添加类 SRAM 总线支持
parent: 实践任务
nav_order: 14
---

# 实践任务14：添加类 SRAM 总线支持

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](chisel/README.md) ｜ [逐行对照](chisel/MAPPING.md)

> **任务类型**：从零实现 ｜ **待操作代码**：14 处
>
> **代码目录**：`code/`（原 Verilog 实验环境，软链接） ｜ `chisel/`（Chisel 版，软链接）

## 实验目标

1. 将 CPU 对外接口修改为**类 SRAM 总线接口**（握手式）。
2. 在采用握手机制的 block RAM 的 SoC 验证环境中完成 exp14 对应 func 的功能验证。

## 关键代码

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)（软链接到 `taskvscode/exp14/chisel/…`）

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现 + 握手总线）：myCPU —— 五级流水线，访存接口改为握手式
//   对应原实验：code/myCPU/（原实验环境**不提供** myCPU 目录，由学生自己实现）
//
// 本实验（实践任务14）的教学意图 = 从零实现（在 exp13 基础上增量）：
//   把 CPU 的访存接口从「en/we/addr/wdata/rdata」改为**握手式类 SRAM 总线**：
//       请求侧：req、wr、size[1:0]、wstrb[3:0]、addr、wdata
//       响应侧：addr_ok（请求已被接收）、data_ok（读数据有效）、rdata
//   并保证在多拍响应（data_ok 延迟返回）下仍能正确工作。
//   Chisel 版只给接口骨架 + 14 处 TODO(实现)，内部逻辑全部留空（???）。
//
// 握手协议要点（详细方案见原书 8.1.1 / ../../chisel4agent/06-进阶写法.md）：
//   * req 与 addr_ok 同时有效表示请求被接收；此后该请求不可撤销；
//   * 读请求的 data_ok 可能晚若干拍返回，CPU 需要维护"在途请求"的状态；
//   * size 表示访问宽度（0=字节、1=半字、2=字），wstrb 为字节写使能；
//   * 一个典型做法：为指令/数据各维护一个简单状态机（发请求 → 等待 data_ok）。
// ============================================================================

package exp14.student

import chisel3._
import chisel3.util._
import envlib.soc.LACpuHs

class MyCpuTop extends LACpuHs {
  // io 由基类 LACpuHs 提供（类型 envlib.soc.CpuIOHs）：
  //   resetn / inst_sram_{req,wr,size,wstrb,addr,wdata,addr_ok,data_ok,rdata}
  //   / data_sram_{同上} / debug_wb_*

  // --------------------------------------------------------------------------
  // ① IF 级：取指（握手请求 + 等待 data_ok）
  // --------------------------------------------------------------------------
// …（以下为待实现的 TODO 部分，见「待操作代码」）
```

### `SocLiteTop.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/exp14/soc/SocLiteTop.scala](chisel/src/main/scala/exp14/soc/SocLiteTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_hs_bram/rtl/soc_lite_top.v
//
// SoC 装配在共享库 envlib.soc.SocHsBramTop（握手式访存 + sram_wrap + 握手 bridge）；
// 本文件只把学生 CPU 绑上去。exp14 的 func 覆盖 n1~n58，环境改用握手总线。

package exp14.soc

import envlib.soc.SocHsBramTop
import exp14.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true
) extends SocHsBramTop(() => new MyCpuTop, instInit, dataInit, simulation)
```

### `MyCpuTbSpec.scala`（判据复现）

> 源文件：[chisel/src/test/scala/MyCpuTbSpec.scala](chisel/src/test/scala/MyCpuTbSpec.scala)

```scala
  behavior of "SocLiteTop + MyCpuTop (exp14, 握手类 SRAM 总线)"

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
| 1 | `MyCpuTop.scala` | 34 | `TODO(实现 1/14)` |
| 2 | `MyCpuTop.scala` | 45 | `TODO(实现 2/14)` |
| 3 | `MyCpuTop.scala` | 50 | `TODO(实现 3/14)` |
| 4 | `MyCpuTop.scala` | 55 | `TODO(实现 4/14)` |
| 5 | `MyCpuTop.scala` | 60 | `TODO(实现 5/14)` |
| 6 | `MyCpuTop.scala` | 65 | `TODO(实现 6/14)` |
| 7 | `MyCpuTop.scala` | 70 | `TODO(实现 7/14)` |
| 8 | `MyCpuTop.scala` | 81 | `TODO(实现 8/14)` |
| 9 | `MyCpuTop.scala` | 86 | `TODO(实现 9/14)` |
| 10 | `MyCpuTop.scala` | 95 | `TODO(实现 10/14)` |
| 11 | `MyCpuTop.scala` | 100 | `TODO(实现 11/14)` |
| 12 | `MyCpuTop.scala` | 105 | `TODO(实现 12/14)` |
| 13 | `MyCpuTop.scala` | 110 | `TODO(实现 13/14)` |
| 14 | `MyCpuTop.scala` | 117 | `TODO(实现 14/14)` |

### 待操作：`MyCpuTop.scala:34` — `TODO(实现 1/14)`

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)#L34

```scala
  // --------------------------------------------------------------------------
  // ① IF 级：取指（握手请求 + 等待 data_ok）
  // --------------------------------------------------------------------------
  // TODO(实现 1/14)：pc / nextpc / 取指握手接口   // <<< 待操作
  io.inst_sram_req    := ???
  io.inst_sram_wr     := ???
  io.inst_sram_size   := ???
  io.inst_sram_wstrb  := ???
```

### 待操作：`MyCpuTop.scala:45` — `TODO(实现 2/14)`

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)#L45

```scala
  // --------------------------------------------------------------------------
  // ② IF/ID 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 2/14)：IF/ID 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:50` — `TODO(实现 3/14)`

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)#L50

```scala
  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  // --------------------------------------------------------------------------
  // TODO(实现 3/14)：指令译码、立即数、寄存器堆读、分支判断   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:55` — `TODO(实现 4/14)`

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)#L55

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

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)#L60

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

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)#L65

```scala
  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 6/14)：EX/MEM 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（握手请求 + 等待 data_ok；ALE 检测）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:70` — `TODO(实现 7/14)`

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)#L70

```scala
  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（握手请求 + 等待 data_ok；ALE 检测）
  // --------------------------------------------------------------------------
  // TODO(实现 7/14)：数据访存握手接口 + ALE 检测   // <<< 待操作
  io.data_sram_req    := ???
  io.data_sram_wr     := ???
  io.data_sram_size   := ???
  io.data_sram_wstrb  := ???
```

### 待操作：`MyCpuTop.scala:81` — `TODO(实现 8/14)`

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)#L81

```scala
  // --------------------------------------------------------------------------
  // ⑧ MEM/WB 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 8/14)：MEM/WB 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:86` — `TODO(实现 9/14)`

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)#L86

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

### 待操作：`MyCpuTop.scala:95` — `TODO(实现 10/14)`

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)#L95

```scala
  // --------------------------------------------------------------------------
  // ⑩ 数据相关处理：前递 + load-use 阻塞（沿用 exp9~exp13）
  // --------------------------------------------------------------------------
  // TODO(实现 10/14)：前递通路 + load-use 阻塞   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑪ 控制状态寄存器与 CSR 指令（exp12）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:100` — `TODO(实现 11/14)`

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)#L100

```scala
  // --------------------------------------------------------------------------
  // ⑪ 控制状态寄存器与 CSR 指令（exp12）
  // --------------------------------------------------------------------------
  // TODO(实现 11/14)：CSR 寄存器堆 + csrrd/csrwr/csrxchg   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn（exp12/exp13）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:105` — `TODO(实现 12/14)`

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)#L105

```scala
  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn（exp12/exp13）
  // --------------------------------------------------------------------------
  // TODO(实现 12/14)：例外入口（syscall/ADEF/ALE/BRK/INE 等）+ ertn 返回   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器（exp13）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:110` — `TODO(实现 13/14)`

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)#L110

```scala
  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器（exp13）
  // --------------------------------------------------------------------------
  // TODO(实现 13/14)：中断使能与仲裁 + 定时器 + rdcnt* 指令   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑭ 握手总线的流水线互锁（本实验核心）
  //    取指/访存请求返回 data_ok 之前必须冻结相关流水级；
```

### 待操作：`MyCpuTop.scala:117` — `TODO(实现 14/14)`

> 源文件：[chisel/src/main/scala/exp14/student/MyCpuTop.scala](chisel/src/main/scala/exp14/student/MyCpuTop.scala)#L117

```scala
  //    取指/访存请求返回 data_ok 之前必须冻结相关流水级；
  //    注意与分支冲刷、例外冲刷的优先级关系。
  // --------------------------------------------------------------------------
  // TODO(实现 14/14)：访存握手导致的流水线停顿/互锁   // <<< 待操作
}
```

## 实验验收

| 项 | 说明 |
|---|---|
| 原实验判据 | `mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!` |
| Chisel 版判据 | TraceHarness：golden_trace 逐条比对 |
| 运行方式 | `cd chisel && ./mill chisel.test`（需 JDK 17 + Mill） |
| ⚠️ 未实测 | Chisel 代码为静态交付，未编译/仿真；逐行对照见 `chisel/MAPPING.md` |

## 参考

- 原任务说明：[`task.md`](task.md)（硬链接到 `taskvscode/exp14/8.1.1实践任务14-添加类SRAM总线支持.md`）
- Chisel 环境说明：[`chisel/README.md`](chisel/README.md)（速览 / 步骤 / 判据 / 自检）
- Verilog ↔ Chisel 逐行对照：[`chisel/MAPPING.md`](chisel/MAPPING.md)
- 改写规范与核验记录：[CHISEL-CONVENTIONS.md](../CHISEL-CONVENTIONS.md)
- 原书（LoongArch 版）：https://bookdown.org/loongson/_book3/

---

[← 实践任务13](../exp13/index.md) ｜ [实践任务15 →](../exp15/index.md)
