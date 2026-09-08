---
layout: default
title: 实践任务8 · 阻塞技术解决相关引发的冲突
parent: 实践任务
nav_order: 8
---

# 实践任务8：阻塞技术解决相关引发的冲突

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](chisel/README.md) ｜ [逐行对照](chisel/MAPPING.md)

> **任务类型**：从零实现 ｜ **待操作代码**：10 处
>
> **代码目录**：`code/`（本站收录的原 Verilog 源码） ｜ `chisel/`（本站收录的 Chisel 版环境）

## 实验目标

1. 加入适当的逻辑处理**寄存器写后读（RAW）数据相关**引发的流水线冲突
2. 运行 exp8 对应的 func，要求通过仿真和上板验证。

## 关键代码

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp8/student/MyCpuTop.scala](chisel/src/main/scala/exp8/student/MyCpuTop.scala)（本站内副本）

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现 + 阻塞）：myCPU —— 20 条指令、五级流水线、用阻塞技术解决数据相关
//   对应原实验：code/myCPU/（原实验环境**不提供** myCPU 目录，由学生自己实现）
//
// 本实验（实践任务8）的教学意图 = 从零实现（在 exp7 基础上增量）：
//   在 exp7 的五级流水 CPU 基础上，加入**寄存器写后读（RAW）数据相关**的冲突处理，
//   本任务**只要求使用阻塞（stall）技术**（前递留到 exp9）。
//   Chisel 版只给接口骨架 + 10 处 TODO(实现)，内部逻辑全部留空（???）。
//
// 与 exp7 的差异：
//   1. func 程序的相关指令之间**不再插入 NOP**，因此流水线必须自己检测 RAW 相关；
//   2. 检测到相关时需冻结 IF/ID（以及可能的 EX）并插入气泡，直到写回完成。
//
// 实现提示（详细方案见原书 5.1.2 / ../../chisel4agent/08-五级流水线CPU串讲.md）：
//   * 需要比较「当前 ID 级要读的 rj/rk」与「EX/MEM/WB 级正在写的 rd」；
//   * 写回寄存器堆为「前半周期写、后半周期读」时，WB 级与本级的冲突可以天然规避，
//     但 EX 级、MEM 级与本级的冲突必须阻塞；
//   * 阻塞实现通常为：hold IF/ID 与 pc、把 ID/EX 清零（插入气泡）。
// ============================================================================

package exp8.student

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

> 源文件：[chisel/src/main/scala/exp8/soc/SocLiteTop.scala](chisel/src/main/scala/exp8/soc/SocLiteTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_bram/rtl/soc_lite_top.v（与 exp7 相同）
//
// SoC 装配在共享库 envlib.soc.SocBramTop；本文件只把学生 CPU 绑上去。
// 与 exp7 的唯一区别是 func 程序（exp8 的相关指令之间不再插 NOP，
// 因此 CPU 必须自己用阻塞技术处理写后读数据相关），环境本身不变。

package exp8.soc

import envlib.soc.SocBramTop
import exp8.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true
) extends SocBramTop(() => new MyCpuTop, instInit, dataInit, simulation)
```

### `MyCpuTbSpec.scala`（判据复现）

> 源文件：[chisel/src/test/scala/MyCpuTbSpec.scala](chisel/src/test/scala/MyCpuTbSpec.scala)

```scala
  behavior of "SocLiteTop + MyCpuTop (exp8, 阻塞解决数据相关)"

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
| 3 | `MyCpuTop.scala` | 51 | `TODO(实现 3/10)` |
| 4 | `MyCpuTop.scala` | 56 | `TODO(实现 4/10)` |
| 5 | `MyCpuTop.scala` | 61 | `TODO(实现 5/10)` |
| 6 | `MyCpuTop.scala` | 66 | `TODO(实现 6/10)` |
| 7 | `MyCpuTop.scala` | 71 | `TODO(实现 7/10)` |
| 8 | `MyCpuTop.scala` | 80 | `TODO(实现 8/10)` |
| 9 | `MyCpuTop.scala` | 85 | `TODO(实现 9/10)` |
| 10 | `MyCpuTop.scala` | 96 | `TODO(实现 10/10)` |

### 待操作：`MyCpuTop.scala:36` — `TODO(实现 1/10)`

> 源文件：[chisel/src/main/scala/exp8/student/MyCpuTop.scala](chisel/src/main/scala/exp8/student/MyCpuTop.scala)#L36

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

> 源文件：[chisel/src/main/scala/exp8/student/MyCpuTop.scala](chisel/src/main/scala/exp8/student/MyCpuTop.scala)#L45

```scala
  // --------------------------------------------------------------------------
  // ② IF/ID 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 2/10)：IF/ID 流水寄存器（至少保存 pc、inst）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  //    可复用 envlib.common 的 Decoder2_4/4_16/5_32/6_64 与 Regfile。
```

### 待操作：`MyCpuTop.scala:51` — `TODO(实现 3/10)`

> 源文件：[chisel/src/main/scala/exp8/student/MyCpuTop.scala](chisel/src/main/scala/exp8/student/MyCpuTop.scala)#L51

```scala
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  //    可复用 envlib.common 的 Decoder2_4/4_16/5_32/6_64 与 Regfile。
  // --------------------------------------------------------------------------
  // TODO(实现 3/10)：指令译码、立即数生成、寄存器堆读、分支/跳转判断   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:56` — `TODO(实现 4/10)`

> 源文件：[chisel/src/main/scala/exp8/student/MyCpuTop.scala](chisel/src/main/scala/exp8/student/MyCpuTop.scala)#L56

```scala
  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 4/10)：ID/EX 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:61` — `TODO(实现 5/10)`

> 源文件：[chisel/src/main/scala/exp8/student/MyCpuTop.scala](chisel/src/main/scala/exp8/student/MyCpuTop.scala)#L61

```scala
  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
  // TODO(实现 5/10)：ALU 运算   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:66` — `TODO(实现 6/10)`

> 源文件：[chisel/src/main/scala/exp8/student/MyCpuTop.scala](chisel/src/main/scala/exp8/student/MyCpuTop.scala)#L66

```scala
  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 6/10)：EX/MEM 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（字节写使能 data_sram_we[3:0]）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:71` — `TODO(实现 7/10)`

> 源文件：[chisel/src/main/scala/exp8/student/MyCpuTop.scala](chisel/src/main/scala/exp8/student/MyCpuTop.scala)#L71

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

### 待操作：`MyCpuTop.scala:80` — `TODO(实现 8/10)`

> 源文件：[chisel/src/main/scala/exp8/student/MyCpuTop.scala](chisel/src/main/scala/exp8/student/MyCpuTop.scala)#L80

```scala
  // --------------------------------------------------------------------------
  // ⑧ MEM/WB 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 8/10)：MEM/WB 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:85` — `TODO(实现 9/10)`

> 源文件：[chisel/src/main/scala/exp8/student/MyCpuTop.scala](chisel/src/main/scala/exp8/student/MyCpuTop.scala)#L85

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

### 待操作：`MyCpuTop.scala:96` — `TODO(实现 10/10)`

> 源文件：[chisel/src/main/scala/exp8/student/MyCpuTop.scala](chisel/src/main/scala/exp8/student/MyCpuTop.scala)#L96

```scala
  //    检测 ID 级要读的寄存器与流水线中尚未写回的 rd 是否相同；
  //    若相关，则冻结 pc 与 IF/ID、清空 ID/EX（插入气泡）。
  // --------------------------------------------------------------------------
  // TODO(实现 10/10)：RAW 数据相关的检测与阻塞（stall）   // <<< 待操作
}
```

## 实验验收

| 项 | 说明 |
|---|---|
| 原实验判据 | `mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!` |
| Chisel 版判据 | TraceHarness：golden_trace 逐条比对 |
| 运行方式 | `cd chisel && ./mill chisel.test`（需 JDK 17 + Mill） |
| 运行所需运行件 | 本实验的 Chisel 测试会读取 `code/func/obj/inst_ram.mif`、`code/func/obj/data_ram.mif` 与 `code/gettrace/golden_trace.txt`，**这三类运行件未收录在本站仓库**（体积 107MB，见 README §2）；请从主仓库 `taskvscode/exp8/` 获取，否则 `MifLoader`/`TraceLoader` 会直接报"找不到 .mif / golden_trace.txt" |
| ⚠️ 未实测 | Chisel 代码为静态交付，未编译/仿真；逐行对照见 `chisel/MAPPING.md` |

## 参考

- 原任务说明：[`task.md`](task.md)（硬链接到 `taskvscode/exp8/5.1.2实践任务8-阻塞技术解决相关引发的冲突.md`）
- Chisel 环境说明：[`chisel/README.md`](chisel/README.md)（速览 / 步骤 / 判据 / 自检）
- Verilog ↔ Chisel 逐行对照：[`chisel/MAPPING.md`](chisel/MAPPING.md)
- 改写规范与核验记录：[CHISEL-CONVENTIONS.md](../CHISEL-CONVENTIONS.md)
- 原书（LoongArch 版）：https://bookdown.org/loongson/_book3/

---

[← 实践任务7](../exp7/index.md) ｜ [实践任务9 →](../exp9/index.md)
