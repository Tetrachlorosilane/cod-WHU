---
exp: 10
title: 算术逻辑与乘除指令添加
doc: chisel-env
source: taskvscode/exp10/code/（原 Verilog 实验环境，未改动）
source_url: https://bookdown.org/loongson/_book3/
ver: agent-1.0
intent: 从零实现
keywords: [算术指令, 乘除法, 立即数扩展, ui12, si12]
prereqs: [exp9]
objectives:
  - 新增 slti/sltui/andi/ori/xori/sll/srl/sra/pcaddu12i
  - 新增 mul/mulh/div/mod 共 7 条
  - 区分 si12 与 ui12 的扩展方式
---

# exp10 Chisel 版实验环境（实践任务10：算术逻辑运算指令和乘除法运算指令添加）

> 对应原实验：`../code/`（Verilog，源自 `output/exp10`）+ `../6.1.1实践任务10-算术逻辑运算指令和乘除法运算指令添加.md`。
> 改写规范：`../../CHISEL-CONVENTIONS.md`。对照表：`./MAPPING.md`。


## 本实验速览（TL;DR）

- **实验目标**：算术逻辑与乘除指令添加（原书实践任务10）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。
- **教学意图**：从零实现 —— 只给接口骨架 + **10 处 `TODO(实现 n/10)`**，内部逻辑留空（`???`）。
- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。
- **共享依赖**：`../../chisel-common/`（环境库，见 `../../CHISEL-CONVENTIONS.md` §2.2）。
- **⚠️ 未实测**：本目录 Chisel 代码未经编译/仿真，逐行对照见 `MAPPING.md`；运行方式见 §3。

## ⚠️ 未实测声明

> 本目录的 Chisel 代码为**静态交付**：未在 JDK/Mill/Chisel 环境中编译或仿真过。
> 如需实际运行：安装 JDK 17 + Mill，在本目录执行 `./mill chisel.test`。

## 1. 本实验要求（原书 6.1.1）

在实践任务9实现的 CPU 基础上完成以下工作：

1. 添加算术逻辑运算类指令：`slti`、`sltui`、`andi`、`ori`、`xori`、`sll`、`srl`、`sra`、`pcaddu12i`；
2. 添加乘除运算类指令：`mul.w`、`mulh.w`、`mulh.wu`、`div.w`、`mod.w`、`div.wu`、`mod.wu`；
3. 运行 exp10 对应的 func（n1~n36），通过仿真验证与上板验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：实验环境仍不提供 `myCPU/`，
`chisel/src/main/scala/exp10/student/MyCpuTop.scala` 给出**接口骨架 + 10 处 `TODO(实现)`**，
其中 ③ 与 ⑤ 明确列出本实验新增的 16 条指令。

## 2. 文件清单

```text
chisel/
├── README.md / MAPPING.md / build.mill     # build.mill 的 sources 引入 ../../chisel-common
├── src/main/scala/exp10/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocBramTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：10 处 TODO（含新增指令清单）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp7~exp9 完全相同**（`soc_bram` 变体、同一套 SoC 装配与判据）；区别只在 func 程序
（exp10 覆盖 n1~n36）与 CPU 需要支持的新指令。

## 3. 怎么做这个实验

1. 在 exp9 的流水线基础上扩展译码与 ALU：
   - 立即数：`slti`/`sltui` 用 **si12 符号扩展**；`andi`/`ori`/`xori` 用 **ui12 零扩展**
     （注意与原书 2.2 节的立即数格式一致）；`pcaddu12i` 用 `si20 << 12`；
   - 移位：`sll`/`srl`/`sra` 的移位量来自 `rk[4:0]`；`slli`/`srli`/`srai` 来自 ui5；
   - 乘除：`mul.w`（低 32 位）、`mulh.w`（有符号高位）、`mulh.wu`（无符号高位）、
     `div.w`/`mod.w`（有符号）、`div.wu`/`mod.wu`（无符号）。
2. 运行测试：
   ```bash
   cd taskvscode/exp10/chisel
   ./mill chisel.test
   ```
3. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_bram/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp9 的关系

环境、测试平台、判据完全一致；学生任务的差异是新增 16 条指令的译码与运算。
共享模块清单见 `../exp7/chisel/MAPPING.md` §6。

## 自检（Agent 快速检查点）

- [ ] `README.md` / `MAPPING.md` / `build.mill` 齐备且非空。
- [ ] 学生模块 TODO 标记数为 **10**（类型：`TODO(实现 n/10)`），与 `MAPPING.md` 记载一致。
- [ ] 顶层端口与 `../code/` 下原 Verilog 的例化端口一一对应（`MAPPING.md` 已列表）。
- [ ] 判据复现方式已写明（见 §4），且与原文 testbench 的检查逻辑一致。
- [ ] 未实测声明已保留（本文件 §⚠️ 与 `MAPPING.md`）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp10`。
