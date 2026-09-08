---
exp: 13
title: 其它例外与中断支持
doc: chisel-env
source: taskvscode/exp13/code/（原 Verilog 实验环境，未改动）
source_url: https://bookdown.org/loongson/_book3/
ver: agent-1.0
intent: 从零实现
keywords: [例外, 中断, 定时器, rdcnt, ADEF, ALE, INE, BRK]
prereqs: [exp12]
objectives:
  - 新增 ADEF/ALE/BRK/INE 例外
  - 新增 ECFG/BADV/TID/TCFG/TVAL/TICLR 与中断仲裁
  - 新增 rdcntvl.w/rdcntvh.w/rdcntid
layout: default
nav_exclude: true
---

# exp13 Chisel 版实验环境（实践任务13：添加其它异常支持）

> 对应原实验：`../code/`（Verilog，源自 `output/exp13`）+ `../7.1.2实践任务13-添加其它异常支持.md`。
> 改写规范：`../../CHISEL-CONVENTIONS.md`。对照表：`./MAPPING.md`。


## 本实验速览（TL;DR）

- **实验目标**：其它例外与中断支持（原书实践任务13）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。
- **教学意图**：从零实现 —— 只给接口骨架 + **14 处 `TODO(实现 n/14)`**，内部逻辑留空（`???`）。
- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。
- **共享依赖**：`../../chisel-common/`（环境库，见 `../../CHISEL-CONVENTIONS.md` §2.2）。
- **✅ 编译验证**：`chisel.compile` / `chisel.test.compile` 已实测通过（未仿真）；逐行对照见 `MAPPING.md`；运行方式见 §3。

## ✅ 编译验证（未仿真）

> 本目录的 Chisel 代码已用 **Mill 1.0.4 + JDK 17 + Chisel 3.5.6 + Scala 2.13.12** 实测通过
> `./mill chisel.compile` 与 `./mill chisel.test.compile`（**尚未跑仿真**）。
> 复查报告：仓库根 `verify/REPORT.md`。跑仿真：`./mill chisel.test`
> （exp6~exp23 需先解包运行件：`node ../../../cod-WHU.github.io/tools/unpack-assets.mjs`）。

## 1. 本实验要求（原书 7.1.2）

在实践任务12实现的 CPU 基础上完成以下工作：

1. 增加取指地址错（ADEF）、地址非对齐（ALE）、断点（BRK）和指令不存在（INE）异常的支持；
2. 增加中断支持，包括 2 个软件中断、8 个硬件中断和定时器中断；
3. 增加控制状态寄存器 `ECFG`、`BADV`、`TID`、`TCFG`、`TVAL`、`TICLR`；
4. 增加 `rdcntvl.w`、`rdcntvh.w` 和 `rdcntid` 指令；
5. 运行 exp13 对应的 func（n1~n58），通过仿真验证与上板验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：实验环境仍不提供 `myCPU/`，
`chisel/src/main/scala/exp13/student/MyCpuTop.scala` 给出**接口骨架 + 14 处 `TODO(实现)`**，
其中 ⑬（ADEF/ALE/BRK/INE）与 ⑭（中断 + 定时器 + rdcnt*）是本实验新增的两个核心模块。

## 2. 文件清单

```text
chisel/
├── README.md / MAPPING.md / build.mill     # build.mill 的 sources 引入 ../../chisel-common
├── src/main/scala/exp13/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocBramTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：14 处 TODO（⑬ 例外 + ⑭ 中断/定时器）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp7~exp12 完全相同**；区别只在 func（n1~n58）与 CPU 需要新增的例外/中断支持。

## 3. 怎么做这个实验

1. 实现 ⑬：在取指（ADEF）、译码（INE/BRK）、访存（ALE）各环节检测异常条件，
   写入 `ESTAT.Ecode`、`ERA`、`BADV`，并按例外交付流程跳转 `EENTRY`；
2. 实现 ⑭：`ECFG.LIE` 与 `ESTAT.IS` 按位使能的中断仲裁；`TCFG/TVAL/TID` 定时器
   （`TVAL` 递减到 0 触发 TI，`TICLR` 写 1 清除）；`rdcntvl.w`/`rdcntvh.w`/`rdcntid`；
3. 运行测试：
   ```bash
   cd taskvscode/exp13/chisel
   ./mill chisel.test
   ```
4. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_bram/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp12 的关系

环境、测试平台、判据完全一致；学生任务的差异是新增 4 类例外、中断/定时器与 3 条计数器指令。
共享模块清单见 `../exp7/chisel/MAPPING.md` §6。

## 自检（Agent 快速检查点）

- [ ] `README.md` / `MAPPING.md` / `build.mill` 齐备且非空。
- [ ] 学生模块 TODO 标记数为 **14**（类型：`TODO(实现 n/14)`），与 `MAPPING.md` 记载一致。
- [ ] 顶层端口与 `../code/` 下原 Verilog 的例化端口一一对应（`MAPPING.md` 已列表）。
- [ ] 判据复现方式已写明（见 §4），且与原文 testbench 的检查逻辑一致。
- [ ] 编译验证声明已保留（本文件 §✅ 与 `MAPPING.md`）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp13`。
