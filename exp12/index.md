---
layout: default
title: 实践任务12 · 添加系统调用异常支持
nav_title: 实践任务12 添加系统调用异常支持
nav_order: 12
---

# 实践任务12：添加系统调用异常支持

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](chisel/README.md) ｜ [逐行对照](chisel/MAPPING.md)

> **任务类型**：从零实现 ｜ **待操作代码**：12 处
>
> **代码目录**：`code/`（本站收录的原 Verilog 源码） ｜ `chisel/`（本站收录的 Chisel 版环境）

## 实验目标

1. 为 CPU 增加 `csrrd`、`csrwr`、`csrxchg` 和 `ertn` 指令。
2. 为 CPU 增加控制状态寄存器 `CRMD`、`PRMD`、`ESTAT`、`ERA`、`EENTRY`、`SAVE0~3`。
3. 为 CPU 增加 `syscall` 指令，实现系统调用异常支持。
4. 运行 exp12 对应的 func（n1~n47），要求通过仿真和上板验证。

## 关键代码

### `MyCpuTop.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp12/student/MyCpuTop.scala](chisel/src/main/scala/exp12/student/MyCpuTop.scala)（本站内副本）

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现 + 例外支持）：myCPU —— 五级流水线，新增 CSR 指令与 syscall 例外
//   对应原实验：code/myCPU/（原实验环境**不提供** myCPU 目录，由学生自己实现）
//
// 本实验（实践任务12）的教学意图 = 从零实现（在 exp11 基础上增量）：
//   ① 新增指令：csrrd、csrwr、csrxchg、ertn、syscall；
//   ② 新增控制状态寄存器：CRMD、PRMD、ESTAT、ERA、EENTRY、SAVE0~3；
//   ③ 实现 syscall 例外（进入例外处理入口、ertn 返回）。
//   Chisel 版只给接口骨架 + 12 处 TODO(实现)，内部逻辑全部留空（???）。
//
// 实现提示（详细方案见原书 7.1.1 / ../../chisel4agent/09-CPU开发场景速查.md）：
//   * CSR 指令格式：csr_num 在 inst[23:10]；csrrd 只读、csrwr 写、csrxchg 按掩码改写
//     （掩码为 rj 的值）；写 CSR 可能改变特权级等全局状态；
//   * syscall 例外：Ecode = 0xB(SYS)；ERA ← 触发指令的 PC；ESTAT.Ecode/IS 更新；
//     CRMD.PLV 提升、PRMD 保存旧的 PLV/IE；PC ← EENTRY，并冲刷流水线；
//   * ertn：从 PRMD 恢复 PLV/IE，PC ← ERA，并冲刷流水线；
//   * 例外/ertn 都需要在流水线中做"冲刷"（flush）处理，且例外指令之后的指令必须被取消。
// ============================================================================

package exp12.student

import chisel3._
import chisel3.util._
import envlib.soc.LACpu

class MyCpuTop extends LACpu {
  // io 由基类 LACpu 提供（类型 envlib.soc.CpuIO）

  // --------------------------------------------------------------------------
  // ① IF 级：取指（pc / nextpc / 取指接口）
  // --------------------------------------------------------------------------
// …（以下为待实现的 TODO 部分，见「待操作代码」）
```

### `SocLiteTop.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/exp12/soc/SocLiteTop.scala](chisel/src/main/scala/exp12/soc/SocLiteTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_bram/rtl/soc_lite_top.v（与 exp7~exp11 相同）
//
// SoC 装配在共享库 envlib.soc.SocBramTop；本文件只把学生 CPU 绑上去。
// exp12 的 func 覆盖 n1~n47（新增 syscall 例外与 CSR 指令），环境本身不变。

package exp12.soc

import envlib.soc.SocBramTop
import exp12.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true
) extends SocBramTop(() => new MyCpuTop, instInit, dataInit, simulation)
```

### `MyCpuTbSpec.scala`（判据复现）

> 源文件：[chisel/src/test/scala/MyCpuTbSpec.scala](chisel/src/test/scala/MyCpuTbSpec.scala)

```scala
  behavior of "SocLiteTop + MyCpuTop (exp12, syscall 例外支持)"

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

共 **12** 处，全部位于学生模块中；下表为索引，代码块给出每处的上下文。

| # | 文件 | 行号 | 标记 |
|---|---|---|---|
| 1 | `MyCpuTop.scala` | 33 | `TODO(实现 1/12)` |
| 2 | `MyCpuTop.scala` | 42 | `TODO(实现 2/12)` |
| 3 | `MyCpuTop.scala` | 48 | `TODO(实现 3/12)` |
| 4 | `MyCpuTop.scala` | 53 | `TODO(实现 4/12)` |
| 5 | `MyCpuTop.scala` | 58 | `TODO(实现 5/12)` |
| 6 | `MyCpuTop.scala` | 63 | `TODO(实现 6/12)` |
| 7 | `MyCpuTop.scala` | 68 | `TODO(实现 7/12)` |
| 8 | `MyCpuTop.scala` | 77 | `TODO(实现 8/12)` |
| 9 | `MyCpuTop.scala` | 82 | `TODO(实现 9/12)` |
| 10 | `MyCpuTop.scala` | 91 | `TODO(实现 10/12)` |
| 11 | `MyCpuTop.scala` | 98 | `TODO(实现 11/12)` |
| 12 | `MyCpuTop.scala` | 105 | `TODO(实现 12/12)` |

### 待操作：`MyCpuTop.scala:33` — `TODO(实现 1/12)`

> 源文件：[chisel/src/main/scala/exp12/student/MyCpuTop.scala](chisel/src/main/scala/exp12/student/MyCpuTop.scala)#L33

```scala
  // --------------------------------------------------------------------------
  // ① IF 级：取指（pc / nextpc / 取指接口）
  // --------------------------------------------------------------------------
  // TODO(实现 1/12)：pc / nextpc / 取指接口   // <<< 待操作
  io.inst_sram_en    := ???
  io.inst_sram_we    := ???
  io.inst_sram_addr  := ???
  io.inst_sram_wdata := ???
```

### 待操作：`MyCpuTop.scala:42` — `TODO(实现 2/12)`

> 源文件：[chisel/src/main/scala/exp12/student/MyCpuTop.scala](chisel/src/main/scala/exp12/student/MyCpuTop.scala)#L42

```scala
  // --------------------------------------------------------------------------
  // ② IF/ID 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 2/12)：IF/ID 流水寄存器（至少保存 pc、inst）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  //    本实验需**新增译码**：csrrd、csrwr、csrxchg、ertn、syscall。
```

### 待操作：`MyCpuTop.scala:48` — `TODO(实现 3/12)`

> 源文件：[chisel/src/main/scala/exp12/student/MyCpuTop.scala](chisel/src/main/scala/exp12/student/MyCpuTop.scala)#L48

```scala
  // ③ ID 级：译码 + 读寄存器堆 + 分支判断
  //    本实验需**新增译码**：csrrd、csrwr、csrxchg、ertn、syscall。
  // --------------------------------------------------------------------------
  // TODO(实现 3/12)：指令译码（含 4 条 CSR 指令 + syscall）、立即数生成、寄存器堆读、分支判断   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:53` — `TODO(实现 4/12)`

> 源文件：[chisel/src/main/scala/exp12/student/MyCpuTop.scala](chisel/src/main/scala/exp12/student/MyCpuTop.scala)#L53

```scala
  // --------------------------------------------------------------------------
  // ④ ID/EX 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 4/12)：ID/EX 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:58` — `TODO(实现 5/12)`

> 源文件：[chisel/src/main/scala/exp12/student/MyCpuTop.scala](chisel/src/main/scala/exp12/student/MyCpuTop.scala)#L58

```scala
  // --------------------------------------------------------------------------
  // ⑤ EX 级：ALU
  // --------------------------------------------------------------------------
  // TODO(实现 5/12)：ALU 运算   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:63` — `TODO(实现 6/12)`

> 源文件：[chisel/src/main/scala/exp12/student/MyCpuTop.scala](chisel/src/main/scala/exp12/student/MyCpuTop.scala)#L63

```scala
  // --------------------------------------------------------------------------
  // ⑥ EX/MEM 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 6/12)：EX/MEM 流水寄存器（保留 rd、写使能、访存宽度/符号、结果）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:68` — `TODO(实现 7/12)`

> 源文件：[chisel/src/main/scala/exp12/student/MyCpuTop.scala](chisel/src/main/scala/exp12/student/MyCpuTop.scala)#L68

```scala
  // --------------------------------------------------------------------------
  // ⑦ MEM 级：访存
  // --------------------------------------------------------------------------
  // TODO(实现 7/12)：数据访存接口（字节/半字/字 + 符号/零扩展）   // <<< 待操作
  io.data_sram_en    := ???
  io.data_sram_we    := ???
  io.data_sram_addr  := ???
  io.data_sram_wdata := ???
```

### 待操作：`MyCpuTop.scala:77` — `TODO(实现 8/12)`

> 源文件：[chisel/src/main/scala/exp12/student/MyCpuTop.scala](chisel/src/main/scala/exp12/student/MyCpuTop.scala)#L77

```scala
  // --------------------------------------------------------------------------
  // ⑧ MEM/WB 流水寄存器
  // --------------------------------------------------------------------------
  // TODO(实现 8/12)：MEM/WB 流水寄存器   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
```

### 待操作：`MyCpuTop.scala:82` — `TODO(实现 9/12)`

> 源文件：[chisel/src/main/scala/exp12/student/MyCpuTop.scala](chisel/src/main/scala/exp12/student/MyCpuTop.scala)#L82

```scala
  // --------------------------------------------------------------------------
  // ⑨ WB 级：写回 + 调试信息
  // --------------------------------------------------------------------------
  // TODO(实现 9/12)：写回寄存器堆 + debug_wb_* 输出   // <<< 待操作
  io.debug_wb_pc       := ???
  io.debug_wb_rf_we    := ???
  io.debug_wb_rf_wnum  := ???
  io.debug_wb_rf_wdata := ???
```

### 待操作：`MyCpuTop.scala:91` — `TODO(实现 10/12)`

> 源文件：[chisel/src/main/scala/exp12/student/MyCpuTop.scala](chisel/src/main/scala/exp12/student/MyCpuTop.scala)#L91

```scala
  // --------------------------------------------------------------------------
  // ⑩ 数据相关处理：前递 + load-use 阻塞（沿用 exp9~exp11）
  // --------------------------------------------------------------------------
  // TODO(实现 10/12)：前递通路 + load-use 阻塞   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑪ 控制状态寄存器与 CSR 指令（本实验核心之一）
  //    需实现 CRMD、PRMD、ESTAT、ERA、EENTRY、SAVE0~3 的读/写/按掩码改写，
```

### 待操作：`MyCpuTop.scala:98` — `TODO(实现 11/12)`

> 源文件：[chisel/src/main/scala/exp12/student/MyCpuTop.scala](chisel/src/main/scala/exp12/student/MyCpuTop.scala)#L98

```scala
  //    需实现 CRMD、PRMD、ESTAT、ERA、EENTRY、SAVE0~3 的读/写/按掩码改写，
  //    以及 csrrd / csrwr / csrxchg 三条指令的语义。
  // --------------------------------------------------------------------------
  // TODO(实现 11/12)：CSR 寄存器堆 + csrrd/csrwr/csrxchg   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑫ syscall 例外与 ertn（本实验核心之二）
  //    syscall：Ecode=0xB、ERA←PC、ESTAT/CRMD/PRMD 更新、PC←EENTRY、流水线冲刷；
```

### 待操作：`MyCpuTop.scala:105` — `TODO(实现 12/12)`

> 源文件：[chisel/src/main/scala/exp12/student/MyCpuTop.scala](chisel/src/main/scala/exp12/student/MyCpuTop.scala)#L105

```scala
  //    syscall：Ecode=0xB、ERA←PC、ESTAT/CRMD/PRMD 更新、PC←EENTRY、流水线冲刷；
  //    ertn：从 PRMD 恢复 PLV/IE、PC←ERA、流水线冲刷。
  // --------------------------------------------------------------------------
  // TODO(实现 12/12)：syscall 例外入口 + ertn 返回   // <<< 待操作
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

- 原任务说明：[`task.md`](task.md)（硬链接到 `taskvscode/exp12/7.1.1实践任务12-添加系统调用异常支持.md`）
- Chisel 环境说明：[`chisel/README.md`](chisel/README.md)（速览 / 步骤 / 判据 / 自检）
- Verilog ↔ Chisel 逐行对照：[`chisel/MAPPING.md`](chisel/MAPPING.md)
- 改写规范与核验记录：[CHISEL-CONVENTIONS.md](../CHISEL-CONVENTIONS.md)
- 原书（LoongArch 版）：https://bookdown.org/loongson/_book3/

---

[← 实践任务11](../exp11/index.md) ｜ [实践任务13 →](../exp13/index.md)
