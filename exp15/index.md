---
layout: default
title: 实践任务15 · 添加 AXI 总线支持
parent: 实践任务
nav_order: 15
---

# 实践任务15：添加 AXI 总线支持

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](chisel/README.md) ｜ [逐行对照](chisel/MAPPING.md)

> **任务类型**：从零实现 ｜ **待操作代码**：15 处
>
> **代码目录**：`code/`（本站收录的原 Verilog 源码） ｜ `chisel/`（本站收录的 Chisel 版环境）

## 实验目标

1. 将 CPU 顶层接口修改为 **AXI 总线接口**。CPU 对外只有一个 AXI 接口，需在内部完成
2. 在采用 AXI 总线的 SoC 验证环境里完成 exp15 对应 func（n1~n58）的**固定延迟**功能验证。

## 关键代码

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)（本站内副本）

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现 + AXI 总线）：myCPU —— 五级流水线，对外接口为 AXI4
//   对应原实验：code/myCPU/（原实验环境**不提供** myCPU 目录，由学生自己实现）
//
// 本实验（实践任务15）的教学意图 = 从零实现（在 exp14 基础上增量）：
//   把 CPU 的访存接口从"握手式类 SRAM"改为**直接产生 AXI4 请求**：
//     AR/R 读通道、AW/W/B 写通道（ID/长度/大小/突发类型/缓存属性/保护属性）。
//   并保证在 AXI 的请求-响应分离、可多拍返回下仍能正确工作。
//   Chisel 版只给接口骨架 + 15 处 TODO(实现)，内部逻辑全部留空（???）。
//
// AXI4 要点（详细方案见原书 8.1.2 / ../../chisel4agent/06-进阶写法.md）：
//   * 读：AR 通道发出 {arid,araddr,arlen,arsize,arburst}，R 通道逐拍返回
//     {rid,rdata,rresp,rlast}；`arlen=0` 表示单拍传输；
//   * 写：AW 发地址、W 发数据（wstrb 为字节使能、wlast 标记最后一拍），
//     B 通道返回写响应；
//   * 每个通道都是 valid/ready 握手，读写通道彼此独立；
//   * 本实验外部为**固定延迟**响应（exp16 改为随机延迟）。
// ============================================================================

package exp15.student

import chisel3._
import chisel3.util._
import envlib.soc.LACpuAxi

class MyCpuTop extends LACpuAxi {
  // io 由基类 LACpuAxi 提供（类型 envlib.soc.CpuIOAxi）：
  //   arid/araddr/arlen/arsize/arburst/arlock/arcache/arprot/arvalid/arready
  //   rid/rdata/rresp/rlast/rvalid/rready
  //   awid/awaddr/awlen/awsize/awburst/awlock/awcache/awprot/awvalid/awready
  //   wid/wdata/wstrb/wlast/wvalid/wready
  //   bid/bresp/bvalid/bready
  //   debug_wb_*

  // --------------------------------------------------------------------------
  // ① IF 级：取指（AR/R 通道）
  // --------------------------------------------------------------------------
// …（以下为待实现的 TODO 部分，见「待操作代码」）
```

### `SocLiteTop.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/exp15/soc/SocLiteTop.scala](chisel/src/main/scala/exp15/soc/SocLiteTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_axi/rtl/soc_lite_top.v
//
// SoC 装配在共享库 envlib.soc.SocAxiTop（AXI 交叉开关 + axi_wrap_ram + AXI RAM 模型 +
// AXI confreg）；本文件只把学生 CPU 绑上去。
// exp15 的 func 覆盖 n1~n58，CPU 需直接产生 AXI4 的 ar/r/aw/w/b 五通道。

package exp15.soc

import envlib.soc.SocAxiTop
import exp15.student.MyCpuTop

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
  behavior of "SocLiteTop + MyCpuTop (exp15, AXI 总线)"

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
| 1 | `MyCpuTop.scala` | 39 | `TODO(实现 1/15)` |
| 2 | `MyCpuTop.scala` | 54 | `TODO(实现 2/15)` |
| 3 | `MyCpuTop.scala` | 59 | `TODO(实现 3/15)` |
| 4 | `MyCpuTop.scala` | 64 | `TODO(实现 4/15)` |
| 5 | `MyCpuTop.scala` | 69 | `TODO(实现 5/15)` |
| 6 | `MyCpuTop.scala` | 74 | `TODO(实现 6/15)` |
| 7 | `MyCpuTop.scala` | 79 | `TODO(实现 7/15)` |
| 8 | `MyCpuTop.scala` | 99 | `TODO(实现 8/15)` |
| 9 | `MyCpuTop.scala` | 104 | `TODO(实现 9/15)` |
| 10 | `MyCpuTop.scala` | 113 | `TODO(实现 10/15)` |
| 11 | `MyCpuTop.scala` | 118 | `TODO(实现 11/15)` |
| 12 | `MyCpuTop.scala` | 123 | `TODO(实现 12/15)` |
| 13 | `MyCpuTop.scala` | 128 | `TODO(实现 13/15)` |
| 14 | `MyCpuTop.scala` | 134 | `TODO(实现 14/15)` |
| 15 | `MyCpuTop.scala` | 141 | `TODO(实现 15/15)` |

### 待操作：`MyCpuTop.scala:39` — `TODO(实现 1/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L39

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

### 待操作：`MyCpuTop.scala:54` — `TODO(实现 2/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L54

```scala
  // --------------------------------------------------------------------------
  // ② IF/ID 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 2/15)：IF/ID 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:59` — `TODO(实现 3/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L59

```scala
  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  // --------------------------------------------------------------------------
  // TODO(实现 3/15)：指令译码、立即数、寄存器堆读、分支判断   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:64` — `TODO(实现 4/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L64

```scala
  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 4/15)：ID/EX 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:69` — `TODO(实现 5/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L69

```scala
  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
  // TODO(实现 5/15)：ALU 运算   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:74` — `TODO(实现 6/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L74

```scala
  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 6/15)：EX/MEM 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（AW/W/B 通道）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:79` — `TODO(实现 7/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L79

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

### 待操作：`MyCpuTop.scala:99` — `TODO(实现 8/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L99

```scala
  // --------------------------------------------------------------------------
  // ⑧ MEM/WB 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 8/15)：MEM/WB 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:104` — `TODO(实现 9/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L104

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

### 待操作：`MyCpuTop.scala:113` — `TODO(实现 10/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L113

```scala
  // --------------------------------------------------------------------------
  // ⑩ 数据相关处理：前递 + load-use 阻塞
  // --------------------------------------------------------------------------
  // TODO(实现 10/15)：前递通路 + load-use 阻塞   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑪ 控制状态寄存器与 CSR 指令
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:118` — `TODO(实现 11/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L118

```scala
  // --------------------------------------------------------------------------
  // ⑪ 控制状态寄存器与 CSR 指令
  // --------------------------------------------------------------------------
  // TODO(实现 11/15)：CSR 寄存器堆 + csrrd/csrwr/csrxchg   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:123` — `TODO(实现 12/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L123

```scala
  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn
  // --------------------------------------------------------------------------
  // TODO(实现 12/15)：例外入口 + ertn 返回   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:128` — `TODO(实现 13/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L128

```scala
  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器
  // --------------------------------------------------------------------------
  // TODO(实现 13/15)：中断使能与仲裁 + 定时器 + rdcnt* 指令   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑭ AXI 事务状态机（本实验核心之一）
  //    读写通道独立；同一时刻可有多个在途请求时需按 ID 匹配返回。
```

### 待操作：`MyCpuTop.scala:134` — `TODO(实现 14/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L134

```scala
  // ⑭ AXI 事务状态机（本实验核心之一）
  //    读写通道独立；同一时刻可有多个在途请求时需按 ID 匹配返回。
  // --------------------------------------------------------------------------
  // TODO(实现 14/15)：AXI 读写事务状态机与 ID 管理   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑮ 访存/取指未完成时的流水线互锁（本实验核心之二）
  //    AR 已发但 R 未回、AW/W 未完成 B 未回时，相关流水级必须停顿；
```

### 待操作：`MyCpuTop.scala:141` — `TODO(实现 15/15)`

> 源文件：[chisel/src/main/scala/exp15/student/MyCpuTop.scala](chisel/src/main/scala/exp15/student/MyCpuTop.scala)#L141

```scala
  //    AR 已发但 R 未回、AW/W 未完成 B 未回时，相关流水级必须停顿；
  //    注意与分支冲刷、例外冲刷的优先级。
  // --------------------------------------------------------------------------
  // TODO(实现 15/15)：AXI 请求未完成时的流水线停顿/互锁   // <<< 待操作
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

- 原任务说明：[`task.md`](task.md)（硬链接到 `taskvscode/exp15/8.1.2实践任务15-添加AXI总线支持.md`）
- Chisel 环境说明：[`chisel/README.md`](chisel/README.md)（速览 / 步骤 / 判据 / 自检）
- Verilog ↔ Chisel 逐行对照：[`chisel/MAPPING.md`](chisel/MAPPING.md)
- 改写规范与核验记录：[CHISEL-CONVENTIONS.md](../CHISEL-CONVENTIONS.md)
- 原书（LoongArch 版）：https://bookdown.org/loongson/_book3/

---

[← 实践任务14](../exp14/index.md) ｜ [实践任务16 →](../exp16/index.md)
