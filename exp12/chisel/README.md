---
exp: 12
title: syscall 例外支持
doc: chisel-env
source: taskvscode/exp12/code/（原 Verilog 实验环境，未改动）
source_url: https://bookdown.org/loongson/_book3/
ver: agent-1.0
intent: 从零实现
keywords: [syscall, CSR, csrrd/csrwr/csrxchg, ertn]
prereqs: [exp11]
objectives:
  - 新增 csrrd/csrwr/csrxchg/ertn
  - 新增 CRMD/PRMD/ESTAT/ERA/EENTRY/SAVE0~3
  - 实现 syscall 例外与 ertn 返回
layout: default
nav_exclude: true
---

# exp12 Chisel 版实验环境（实践任务12：添加系统调用异常支持）

> 对应原实验：`../code/`（Verilog，源自 `output/exp12`）+ `../7.1.1实践任务12-添加系统调用异常支持.md`。
> 改写规范：`../../CHISEL-CONVENTIONS.md`。对照表：`./MAPPING.md`。


## 本实验速览（TL;DR）

- **实验目标**：syscall 例外支持（原书实践任务12）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。
- **教学意图**：从零实现 —— 只给接口骨架 + **12 处 `TODO(实现 n/12)`**，内部逻辑留空（`???`）。
- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。
- **共享依赖**：`../../chisel-common/`（环境库，见 `../../CHISEL-CONVENTIONS.md` §2.2）。
- **⚠️ 未实测**：本目录 Chisel 代码未经编译/仿真，逐行对照见 `MAPPING.md`；运行方式见 §3。

## ⚠️ 未实测声明

> 本目录的 Chisel 代码为**静态交付**：未在 JDK/Mill/Chisel 环境中编译或仿真过。
> 如需实际运行：安装 JDK 17 + Mill，在本目录执行 `./mill chisel.test`。

## 1. 本实验要求（原书 7.1.1）

在实践任务11实现的 CPU 基础上完成以下工作：

1. 为 CPU 增加 `csrrd`、`csrwr`、`csrxchg` 和 `ertn` 指令；
2. 为 CPU 增加控制状态寄存器 `CRMD`、`PRMD`、`ESTAT`、`ERA`、`EENTRY`、`SAVE0~3`；
3. 为 CPU 增加 `syscall` 指令，实现系统调用异常支持；
4. 运行 exp12 对应的 func（n1~n47），通过仿真验证与上板验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：实验环境仍不提供 `myCPU/`，
`chisel/src/main/scala/exp12/student/MyCpuTop.scala` 给出**接口骨架 + 12 处 `TODO(实现)`**，
其中 ⑪（CSR）与 ⑫（syscall/ertn）是本实验新增的两个核心模块。

## 2. 文件清单

```text
chisel/
├── README.md / MAPPING.md / build.mill     # build.mill 的 sources 引入 ../../chisel-common
├── src/main/scala/exp12/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocBramTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：12 处 TODO（⑪ CSR + ⑫ syscall/ertn）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp7~exp11 完全相同**；区别只在 func（n1~n47）与 CPU 需要新增的 CSR/例外支持。

## 3. 怎么做这个实验

1. 实现 ⑪：CSR 寄存器堆 + `csrrd`/`csrwr`/`csrxchg`（`csr_num = inst[23:10]`，
   `csrxchg` 用 rj 作为掩码）；
2. 实现 ⑫：`syscall` 触发例外（`Ecode = 0xB`、`ERA ← PC`、`ESTAT/CRMD/PRMD` 更新、
   `PC ← EENTRY`、流水线冲刷）与 `ertn` 返回（从 `PRMD` 恢复 PLV/IE、`PC ← ERA`）；
3. 运行测试：
   ```bash
   cd taskvscode/exp12/chisel
   ./mill chisel.test
   ```
4. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_bram/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp11 的关系

环境、测试平台、判据完全一致；学生任务的差异是新增 4 条 CSR 指令 + `syscall` 例外 + `ertn`。
共享模块清单见 `../exp7/chisel/MAPPING.md` §6。

## 自检（Agent 快速检查点）

- [ ] `README.md` / `MAPPING.md` / `build.mill` 齐备且非空。
- [ ] 学生模块 TODO 标记数为 **12**（类型：`TODO(实现 n/12)`），与 `MAPPING.md` 记载一致。
- [ ] 顶层端口与 `../code/` 下原 Verilog 的例化端口一一对应（`MAPPING.md` 已列表）。
- [ ] 判据复现方式已写明（见 §4），且与原文 testbench 的检查逻辑一致。
- [ ] 未实测声明已保留（本文件 §⚠️ 与 `MAPPING.md`）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp12`。
