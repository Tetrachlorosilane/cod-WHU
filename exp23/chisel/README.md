---
exp: 23
title: CACOP 指令
doc: chisel-env
source: taskvscode/exp23/code/（原 Verilog 实验环境，未改动）
source_url: https://bookdown.org/loongson/_book3/
ver: agent-1.0
intent: 从零实现
keywords: [CACOP, Cache 操作, 按索引, 按地址]
prereqs: [exp22]
objectives:
  - 译码 cacop code, rj, si12
  - 按 code[4:3] 选择 ICache/DCache 与按地址/按索引
  - 跑通 exp23 的 golden_trace
layout: default
nav_exclude: true
---

# exp23 Chisel 版实验环境（实践任务23：CPU 中添加 CACOP 指令）

> 对应原实验：`../code/`（Verilog，源自 `output/exp23`）+ `../10.2.4实践任务23-CPU中添加CACOP指令.md`。
> 改写规范：`../../CHISEL-CONVENTIONS.md`。对照表：`./MAPPING.md`。


## 本实验速览（TL;DR）

- **实验目标**：CACOP 指令（原书实践任务23）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。
- **教学意图**：从零实现 —— 只给接口骨架 + **20 处 `TODO(实现 n/20)`**，内部逻辑留空（`???`）。
- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。
- **共享依赖**：`../../chisel-common/`（环境库，见 `../../CHISEL-CONVENTIONS.md` §2.2）。
- **⚠️ 未实测**：本目录 Chisel 代码未经编译/仿真，逐行对照见 `MAPPING.md`；运行方式见 §3。

## ⚠️ 未实测声明

> 本目录的 Chisel 代码为**静态交付**：未在 JDK/Mill/Chisel 环境中编译或仿真过。
> 如需实际运行：安装 JDK 17 + Mill，在本目录执行 `./mill chisel.test`。

## 1. 本实验要求（原书 10.2.4）

在实践任务22完成的基础上：

1. 在实践任务22完成的 CPU 中增加 **CACOP 指令**实现；
2. 在 AXI SoC 验证环境里完成 exp23 对应 func（n1~n79）的功能验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：实验环境仍不提供 `myCPU/`，
`chisel/src/main/scala/exp23/student/MyCpuTop.scala` 给出**接口骨架 + 20 处 `TODO(实现)`**，
⑳ 即本实验核心（CACOP 译码 + Cache 操作通路 + 流水线配合）。

## 2. 文件清单

```text
chisel/
├── README.md / MAPPING.md / build.mill     # build.mill 的 sources 引入 ../../chisel-common
├── src/main/scala/exp23/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：20 处 TODO（⑳ CACOP）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp15~exp22 完全相同**（`soc_axi` 变体）；区别只在 CPU 内部新增 CACOP 指令。

## 3. 怎么做这个实验

1. 实现 ⑳：`cacop code, rj, si12`
   - `code[4:3]`：0 = 按地址操作 ICache，1 = 按地址操作 DCache，
     2 = 按索引操作 ICache，3 = 按索引操作 DCache；
   - `code[2:0]`：具体操作（Store Tag / 无效化 / 写回并无效化等）；
   - 按地址操作时虚地址为 `rj + si12`，需先做地址转换（DMW/TLB）；
   - 按索引操作用虚地址中的 index 字段直接定位 Cache 行；
   - 注意与流水线配合（停顿、后续取指/访存的一致性）。
2. 运行测试：
   ```bash
   cd taskvscode/exp23/chisel
   ./mill chisel.test
   ```
3. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_axi/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp22 的关系

环境、判据、I/D Cache 与 TLB 完全相同；差异是新增 CACOP 指令。
本实验是 exp5~exp23 改写序列的最后一个实验。
共享模块清单见 `../exp15/chisel/MAPPING.md` §5。

## 自检（Agent 快速检查点）

- [ ] `README.md` / `MAPPING.md` / `build.mill` 齐备且非空。
- [ ] 学生模块 TODO 标记数为 **20**（类型：`TODO(实现 n/20)`），与 `MAPPING.md` 记载一致。
- [ ] 顶层端口与 `../code/` 下原 Verilog 的例化端口一一对应（`MAPPING.md` 已列表）。
- [ ] 判据复现方式已写明（见 §4），且与原文 testbench 的检查逻辑一致。
- [ ] 未实测声明已保留（本文件 §⚠️ 与 `MAPPING.md`）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp23`。
