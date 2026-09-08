---
layout: default
title: 实践任务19 · 添加 TLB 相关例外支持
parent: 实践任务
nav_order: 19
---

# 实践任务19：添加 TLB 相关例外支持

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](chisel/README.md) ｜ [逐行对照](chisel/MAPPING.md)

> **任务类型**：从零实现 ｜ **待操作代码**：17 处
>
> **代码目录**：`code/`（本站收录的原 Verilog 源码） ｜ `chisel/`（本站收录的 Chisel 版环境）

## 实验目标

1. 为 CPU 增加 TLB 相关异常：TLB 重填例外、load/store/取指操作页无效例外、
2. 在 CPU 中增加 `DMW` CSR 寄存器；
3. 为 CPU 增加虚实地址映射的功能；
4. 在采用 AXI 总线的 SoC 验证环境里完成 exp19 对应 func（n1~n72）的功能验证。

## 关键代码

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)（本站内副本）

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现 + TLB 例外/地址映射）：myCPU —— 五级流水线，AXI 接口
//   对应原实验：code/myCPU/（原实验环境**不提供** myCPU 目录，由学生自己实现）
//
// 本实验（实践任务19）的教学意图 = 从零实现（在 exp18 基础上增量）：
//   ① 新增 TLB 相关例外：TLB 重填（TLBR）、页无效（取指 PIL / load PIS / store PIS）、
//      页修改（PME）、页特权等级不合规（PPI）；
//   ② 新增 DMW CSR（直接映射窗口）；
//   ③ 实现虚实地址映射（DMW 优先，其次 TLB 查找）；
//   Chisel 版只给接口骨架 + 17 处 TODO(实现)，内部逻辑全部留空（???）。
//
// 实现提示（详细方案见原书 9.2.3 / ../../chisel4agent/09-CPU开发场景速查.md）：
//   * 地址转换顺序：先查 DMW（DMW0~3，按虚地址高位与 PLV 匹配），命中则直接映射；
//     未命中则查 TLB；TLB 未命中触发 TLBR，命中但 V=0 触发 PIL/PIS，
//     D=0 且是写操作触发 PME，PLV 不满足触发 PPI；
//   * 各例外的 Ecode：PIL(0x1)、PIS(0x2)、PME(0x3)、PPI(0x7)、TLBR(0x3F) 等
//     （以原书表格为准）；ERA 记录出错指令 PC，BADV 记录出错虚地址；
//   * 取指与访存都需要转换，注意流水线冲刷与例外优先级。
// ============================================================================

package exp19.student

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

> 源文件：[chisel/src/main/scala/exp19/soc/SocLiteTop.scala](chisel/src/main/scala/exp19/soc/SocLiteTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_axi/rtl/soc_lite_top.v（与 exp15~exp18 相同）
//
// SoC 装配在共享库 envlib.soc.SocAxiTop；本文件只把学生 CPU 绑上去。
// exp19 的 func 覆盖 n1~n72（新增 TLB 例外、DMW 与虚实地址映射），环境本身不变。

package exp19.soc

import envlib.soc.SocAxiTop
import exp19.student.MyCpuTop

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
  behavior of "SocLiteTop + MyCpuTop (exp19, TLB 例外与地址映射)"

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

共 **17** 处，全部位于学生模块中；下表为索引，代码块给出每处的上下文。

| # | 文件 | 行号 | 标记 |
|---|---|---|---|
| 1 | `MyCpuTop.scala` | 34 | `TODO(实现 1/17)` |
| 2 | `MyCpuTop.scala` | 49 | `TODO(实现 2/17)` |
| 3 | `MyCpuTop.scala` | 54 | `TODO(实现 3/17)` |
| 4 | `MyCpuTop.scala` | 59 | `TODO(实现 4/17)` |
| 5 | `MyCpuTop.scala` | 64 | `TODO(实现 5/17)` |
| 6 | `MyCpuTop.scala` | 69 | `TODO(实现 6/17)` |
| 7 | `MyCpuTop.scala` | 74 | `TODO(实现 7/17)` |
| 8 | `MyCpuTop.scala` | 94 | `TODO(实现 8/17)` |
| 9 | `MyCpuTop.scala` | 99 | `TODO(实现 9/17)` |
| 10 | `MyCpuTop.scala` | 108 | `TODO(实现 10/17)` |
| 11 | `MyCpuTop.scala` | 114 | `TODO(实现 11/17)` |
| 12 | `MyCpuTop.scala` | 119 | `TODO(实现 12/17)` |
| 13 | `MyCpuTop.scala` | 124 | `TODO(实现 13/17)` |
| 14 | `MyCpuTop.scala` | 129 | `TODO(实现 14/17)` |
| 15 | `MyCpuTop.scala` | 134 | `TODO(实现 15/17)` |
| 16 | `MyCpuTop.scala` | 139 | `TODO(实现 16/17)` |
| 17 | `MyCpuTop.scala` | 145 | `TODO(实现 17/17)` |

### 待操作：`MyCpuTop.scala:34` — `TODO(实现 1/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L34

```scala
  // --------------------------------------------------------------------------
  // ① IF 级：取指（AR/R 通道）
  // --------------------------------------------------------------------------
  // TODO(实现 1/17)：pc / nextpc / 取指的 AR 与 R 通道   // <<< 待操作
  io.arid    := ???
  io.araddr  := ???
  io.arlen   := ???
  io.arsize  := ???
```

### 待操作：`MyCpuTop.scala:49` — `TODO(实现 2/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L49

```scala
  // --------------------------------------------------------------------------
  // ② IF/ID 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 2/17)：IF/ID 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断（含 5 条 TLB 指令）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:54` — `TODO(实现 3/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L54

```scala
  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断（含 5 条 TLB 指令）
  // --------------------------------------------------------------------------
  // TODO(实现 3/17)：指令译码、立即数、寄存器堆读、分支判断   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:59` — `TODO(实现 4/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L59

```scala
  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 4/17)：ID/EX 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:64` — `TODO(实现 5/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L64

```scala
  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
  // TODO(实现 5/17)：ALU 运算   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:69` — `TODO(实现 6/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L69

```scala
  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 6/17)：EX/MEM 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（AW/W/B 通道）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:74` — `TODO(实现 7/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L74

```scala
  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存（AW/W/B 通道）
  // --------------------------------------------------------------------------
  // TODO(实现 7/17)：数据的 AW/W/B 通道 + ALE 检测   // <<< 待操作
  io.awid    := ???
  io.awaddr  := ???
  io.awlen   := ???
  io.awsize  := ???
```

### 待操作：`MyCpuTop.scala:94` — `TODO(实现 8/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L94

```scala
  // --------------------------------------------------------------------------
  // ⑧ MEM/WB 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 8/17)：MEM/WB 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:99` — `TODO(实现 9/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L99

```scala
  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
  // TODO(实现 9/17)：写回寄存器堆 + debug_wb_* 输出   // <<< 待操作
  io.debug_wb_pc       := ???
  io.debug_wb_rf_we    := ???
  io.debug_wb_rf_wnum  := ???
  io.debug_wb_rf_wdata := ???
```

### 待操作：`MyCpuTop.scala:108` — `TODO(实现 10/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L108

```scala
  // --------------------------------------------------------------------------
  // ⑩ 数据相关处理：前递 + load-use 阻塞
  // --------------------------------------------------------------------------
  // TODO(实现 10/17)：前递通路 + load-use 阻塞   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑪ CSR 寄存器堆与 CSR 指令
  //    本实验需**新增 DMW CSR**（DMW0~3），并保留 exp18 的 TLB 相关 CSR。
```

### 待操作：`MyCpuTop.scala:114` — `TODO(实现 11/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L114

```scala
  // ⑪ CSR 寄存器堆与 CSR 指令
  //    本实验需**新增 DMW CSR**（DMW0~3），并保留 exp18 的 TLB 相关 CSR。
  // --------------------------------------------------------------------------
  // TODO(实现 11/17)：CSR 寄存器堆（含 DMW0~3）+ csrrd/csrwr/csrxchg   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:119` — `TODO(实现 12/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L119

```scala
  // --------------------------------------------------------------------------
  // ⑫ 例外与 ertn
  // --------------------------------------------------------------------------
  // TODO(实现 12/17)：例外入口 + ertn 返回   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:124` — `TODO(实现 13/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L124

```scala
  // --------------------------------------------------------------------------
  // ⑬ 中断与定时器/计数器
  // --------------------------------------------------------------------------
  // TODO(实现 13/17)：中断使能与仲裁 + 定时器 + rdcnt* 指令   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑭ AXI 事务状态机（含 ID 管理）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:129` — `TODO(实现 14/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L129

```scala
  // --------------------------------------------------------------------------
  // ⑭ AXI 事务状态机（含 ID 管理）
  // --------------------------------------------------------------------------
  // TODO(实现 14/17)：AXI 读写事务状态机与 ID 管理   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑮ AXI 请求未完成时的流水线互锁
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:134` — `TODO(实现 15/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L134

```scala
  // --------------------------------------------------------------------------
  // ⑮ AXI 请求未完成时的流水线互锁
  // --------------------------------------------------------------------------
  // TODO(实现 15/17)：AXI 请求未完成时的流水线停顿/互锁   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑯ TLB 集成与 TLB 指令（exp18）
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:139` — `TODO(实现 16/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L139

```scala
  // --------------------------------------------------------------------------
  // ⑯ TLB 集成与 TLB 指令（exp18）
  // --------------------------------------------------------------------------
  // TODO(实现 16/17)：TLB 例化 + TLBSRCH/TLBRD/TLBWR/TLBFILL/INVTLB   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑰ 虚实地址映射与 TLB 例外（本实验核心）
  //    DMW 直接映射优先 → TLB 查找 → 未命中/无效/脏/权限不合规时触发对应例外。
```

### 待操作：`MyCpuTop.scala:145` — `TODO(实现 17/17)`

> 源文件：[chisel/src/main/scala/exp19/student/MyCpuTop.scala](chisel/src/main/scala/exp19/student/MyCpuTop.scala)#L145

```scala
  // ⑰ 虚实地址映射与 TLB 例外（本实验核心）
  //    DMW 直接映射优先 → TLB 查找 → 未命中/无效/脏/权限不合规时触发对应例外。
  // --------------------------------------------------------------------------
  // TODO(实现 17/17)：DMW + TLB 地址转换 + TLB 例外（TLBR/PIL/PIS/PME/PPI）   // <<< 待操作
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

- 原任务说明：[`task.md`](task.md)（硬链接到 `taskvscode/exp19/9.2.3实践任务19-添加TLB相关例外支持.md`）
- Chisel 环境说明：[`chisel/README.md`](chisel/README.md)（速览 / 步骤 / 判据 / 自检）
- Verilog ↔ Chisel 逐行对照：[`chisel/MAPPING.md`](chisel/MAPPING.md)
- 改写规范与核验记录：[CHISEL-CONVENTIONS.md](../CHISEL-CONVENTIONS.md)
- 原书（LoongArch 版）：https://bookdown.org/loongson/_book3/

---

[← 实践任务18](../exp18/index.md) ｜ [实践任务20 →](../exp20/index.md)
