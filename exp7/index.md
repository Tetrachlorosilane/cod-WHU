---
layout: default
title: '实践任务7 · 不考虑相关冲突处理的简单流水线 CPU'
nav_order: 7
exp: 7
exp_intent: '从零实现'
exp_todos: 9
exp_has_chisel: true
exp_orig_judge: '`mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!`'
exp_judge: 'TraceHarness：golden_trace 逐条比对 + num_data 监视'
exp_needs_assets: true
---

# 实践任务7：不考虑相关冲突处理的简单流水线 CPU

{% include exp-nav.html %}

## 实验目标

1. 调整 CPU 顶层接口，增加指令 RAM 片选 `inst_sram_en` 与数据 RAM 片选 `data_sram_en`。
2. 把 `inst_sram_we` 和 `data_sram_we` 都从 1 比特写使能调整为 **4 比特字节写使能**。
3. 设计一个不考虑相关引发的冲突的单发射五级流水 CPU。
4. 运行 exp7 对应的 func，要求通过仿真和上板验证。

## 关键代码

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp7/student/MyCpuTop.scala](chisel/src/main/scala/exp7/student/MyCpuTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现）：myCPU —— 20 条指令、不考虑相关冲突的五级流水线 CPU
//   对应原实验：code/myCPU/（原实验环境中**不提供** myCPU 目录，由学生自己实现）
//
// 本实验（实践任务7）的要求：从零实现：
//   Chisel 版只给出**接口骨架**与**流水线结构提示**，内部逻辑全部留空（??? + TODO(实现)）。
//   未实现时 elaboration 会以 NotImplementedError 终止，这是预期行为。
//
// 与 exp6 相比，顶层接口有 4 处变化：
//   1. 增加 inst_sram_en / data_sram_en（1 位，高有效）；
//   2. inst_sram_we / data_sram_we 从 1 位改为 4 位字节写使能；
//   3. 指令 RAM / 数据 RAM 改为 block RAM（同步读，读数据比地址晚一拍）；
//   4. debug_wb_* 语义不变（写回级信息）。
//
// 建议的实现顺序（详细方案见原书第 5 章）：
//   IF  → ID  → EX  → MEM → WB
//   ① IF：pc / nextpc / 取指接口（注意同步 RAM 的读延迟）；
//   ② IF/ID 流水寄存器；
//   ③ ID：译码（decoder_*）、立即数、寄存器堆读、分支判断；
//   ④ ID/EX 流水寄存器；
//   ⑤ EX：ALU（可复用 exp6 的 alu 思路）；
//   ⑥ EX/MEM 流水寄存器；
//   ⑦ MEM：data_sram 访问（字节使能）；
//   ⑧ MEM/WB 流水寄存器；
//   ⑨ WB：写回寄存器堆 + debug_wb_* 输出。
// ============================================================================

package exp7.student

import chisel3._
import chisel3.util._
import envlib.soc.LACpu

class MyCpuTop extends LACpu {
  // io 由基类 LACpu 提供（类型 envlib.soc.CpuIO），端口与各实验 soc_lite_top.v 的例化端口一致：
  //   resetn / inst_sram_{en,we[3:0],addr,wdata,rdata} / data_sram_{en,we[3:0],addr,wdata,rdata}
  //   / debug_wb_{pc,rf_we[3:0],rf_wnum,rf_wdata}

  // --------------------------------------------------------------------------
  // ① IF 级：取指
  //    原 exp6 的单周期实现：pc 复位值 0x1bfffffc（使复位后 nextpc = 0x1c000000），
  //    inst_sram_we 恒 0、inst_sram_addr = pc、inst = inst_sram_rdata。
  //    本实验改用 block RAM：读数据比地址晚一拍，需要据此安排流水线。
  // --------------------------------------------------------------------------
// …（以下为待实现的 TODO 部分，见「待操作代码」）
```

### `SocLiteTop.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/exp7/soc/SocLiteTop.scala](chisel/src/main/scala/exp7/soc/SocLiteTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_bram/rtl/soc_lite_top.v
//
// exp7 的 SoC 装配已下沉到共享库 envlib.soc.SocBramTop（cpu → inst_ram；
// cpu.data → bridge_1x2 → {data_ram, confreg}），本文件只是把学生 CPU 绑上去的薄封装。
// 端口、连接关系、与 exp6（soc_dram）的差异说明

package exp7.soc

import envlib.soc.SocBramTop
import exp7.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true
) extends SocBramTop(() => new MyCpuTop, instInit, dataInit, simulation)
```

### `MyCpuTbSpec.scala`（判据复现）

> 源文件：[chisel/src/test/scala/MyCpuTbSpec.scala](chisel/src/test/scala/MyCpuTbSpec.scala)

```scala
  behavior of "SocLiteTop + MyCpuTop (exp7, 五级流水线 CPU)"

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

共 **9** 处，全部位于学生模块中；下表为索引，代码块给出每处的上下文。

| # | 文件 | 行号 | 标记 |
|---|---|---|---|
| 1 | `MyCpuTop.scala` | 46 | `TODO(实现 1/9)` |
| 2 | `MyCpuTop.scala` | 55 | `TODO(实现 2/9)` |
| 3 | `MyCpuTop.scala` | 61 | `TODO(实现 3/9)` |
| 4 | `MyCpuTop.scala` | 66 | `TODO(实现 4/9)` |
| 5 | `MyCpuTop.scala` | 71 | `TODO(实现 5/9)` |
| 6 | `MyCpuTop.scala` | 76 | `TODO(实现 6/9)` |
| 7 | `MyCpuTop.scala` | 81 | `TODO(实现 7/9)` |
| 8 | `MyCpuTop.scala` | 90 | `TODO(实现 8/9)` |
| 9 | `MyCpuTop.scala` | 95 | `TODO(实现 9/9)` |

### 待操作：`MyCpuTop.scala:46` — `TODO(实现 1/9)`

> 源文件：[chisel/src/main/scala/exp7/student/MyCpuTop.scala](chisel/src/main/scala/exp7/student/MyCpuTop.scala)#L46

```scala
  //    inst_sram_we 恒 0、inst_sram_addr = pc、inst = inst_sram_rdata。
  //    本实验改用 block RAM：读数据比地址晚一拍，需要据此安排流水线。
  // --------------------------------------------------------------------------
  // TODO(实现 1/9)：pc / nextpc / 取指接口   // <<< 待操作
  io.inst_sram_en    := ???
  io.inst_sram_we    := ???
  io.inst_sram_addr  := ???
  io.inst_sram_wdata := ???
```

### 待操作：`MyCpuTop.scala:55` — `TODO(实现 2/9)`

> 源文件：[chisel/src/main/scala/exp7/student/MyCpuTop.scala](chisel/src/main/scala/exp7/student/MyCpuTop.scala)#L55

```scala
  // --------------------------------------------------------------------------
  // ② IF/ID 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 2/9)：IF/ID 流水寄存器（至少保存 pc、inst）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆
  //    可复用 envlib.common 的 Decoder2_4/4_16/5_32/6_64 与 Regfile。
```

### 待操作：`MyCpuTop.scala:61` — `TODO(实现 3/9)`

> 源文件：[chisel/src/main/scala/exp7/student/MyCpuTop.scala](chisel/src/main/scala/exp7/student/MyCpuTop.scala)#L61

```scala
  // ③ ID 级：译码 + 读寄存器堆
  //    可复用 envlib.common 的 Decoder2_4/4_16/5_32/6_64 与 Regfile。
  // --------------------------------------------------------------------------
  // TODO(实现 3/9)：指令译码、立即数生成、寄存器堆读、分支/跳转判断   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:66` — `TODO(实现 4/9)`

> 源文件：[chisel/src/main/scala/exp7/student/MyCpuTop.scala](chisel/src/main/scala/exp7/student/MyCpuTop.scala)#L66

```scala
  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 4/9)：ID/EX 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:71` — `TODO(实现 5/9)`

> 源文件：[chisel/src/main/scala/exp7/student/MyCpuTop.scala](chisel/src/main/scala/exp7/student/MyCpuTop.scala)#L71

```scala
  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
  // TODO(实现 5/9)：ALU 运算（exp6 的 Alu 可作参考，注意其操作数方向）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:76` — `TODO(实现 6/9)`

> 源文件：[chisel/src/main/scala/exp7/student/MyCpuTop.scala](chisel/src/main/scala/exp7/student/MyCpuTop.scala)#L76

```scala
  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 6/9)：EX/MEM 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（字节写使能 data_sram_we[3:0]）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:81` — `TODO(实现 7/9)`

> 源文件：[chisel/src/main/scala/exp7/student/MyCpuTop.scala](chisel/src/main/scala/exp7/student/MyCpuTop.scala)#L81

```scala
  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（字节写使能 data_sram_we[3:0]）
  // --------------------------------------------------------------------------
  // TODO(实现 7/9)：数据访存接口   // <<< 待操作
  io.data_sram_en    := ???
  io.data_sram_we    := ???
  io.data_sram_addr  := ???
  io.data_sram_wdata := ???
```

### 待操作：`MyCpuTop.scala:90` — `TODO(实现 8/9)`

> 源文件：[chisel/src/main/scala/exp7/student/MyCpuTop.scala](chisel/src/main/scala/exp7/student/MyCpuTop.scala)#L90

```scala
  // --------------------------------------------------------------------------
  // ⑧ MEM/WB 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 8/9)：MEM/WB 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:95` — `TODO(实现 9/9)`

> 源文件：[chisel/src/main/scala/exp7/student/MyCpuTop.scala](chisel/src/main/scala/exp7/student/MyCpuTop.scala)#L95

```scala
  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
  // TODO(实现 9/9)：写回寄存器堆 + debug_wb_* 输出   // <<< 待操作
  io.debug_wb_pc       := ???
  io.debug_wb_rf_we    := ???
  io.debug_wb_rf_wnum  := ???
  io.debug_wb_rf_wdata := ???
```

## 实验验收

{% include exp-accept.html %}

## 参考

{% include exp-refs.html %}

{% include exp-pager.html %}
