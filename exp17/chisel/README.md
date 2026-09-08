---
exp: 17
title: TLB 模块设计（模块级）
doc: chisel-env
source: taskvscode/exp17/code/（原 Verilog 实验环境，未改动）
source_url: https://bookdown.org/loongson/_book3/
ver: agent-1.0
intent: 从零实现
keywords: [TLB, 组相联, 查找端口, ASID, va_bit12]
prereqs: [exp13]
objectives:
  - 实现 16 项 TLB 表项存储与读写端口
  - 实现双查找端口（含 G 位与 va_bit12）
  - 通过 16 写 / 16 读 / 26 查 → PASS
layout: default
nav_exclude: true
---

# exp17 Chisel 版实验环境（实践任务17：TLB 模块设计）

> 对应原实验：`../code/`（Verilog，源自 `output/exp17`）+ `../9.2.1实践任务17-TLB模块设计.md`。
> 改写规范：`../../CHISEL-CONVENTIONS.md`。对照表：`./MAPPING.md`。


## 本实验速览（TL;DR）

- **实验目标**：TLB 模块设计（模块级）（原书实践任务17）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。
- **教学意图**：从零实现 —— 只给接口骨架 + **4 处 `TODO(实现 n/4)`**，内部逻辑留空（`???`）。
- **判据复现**：TlbSpec：16 写 / 16 读 / 26 查 → `----PASS!!!`。
- **共享依赖**：不依赖共享库（模块级独立环境）。
- **⚠️ 未实测**：本目录 Chisel 代码未经编译/仿真，逐行对照见 `MAPPING.md`；运行方式见 §3。

## ⚠️ 未实测声明

> 本目录的 Chisel 代码为**静态交付**：未在 JDK/Mill/Chisel 环境中编译或仿真过。
> 如需实际运行：安装 JDK 17 + Mill，在本目录执行 `./mill chisel.test`。

## 1. 本实验要求（原书 9.2.1）

1. 设计 **TLB 模块**（模块名 `tlb`，接口见原书 9.1 节）；
2. 利用 TLB 模块级验证环境（`tlb_top.v` + `testbench.v`）验证，通过仿真和上板验证。

**Chisel 版保留的教学意图 = 从零实现**：原实验环境只提供验证环境、不提供 TLB 本体；
Chisel 版同样只给接口骨架：
`chisel/src/main/scala/exp17/student/Tlb.scala` 中有 **4 处 `TODO(实现 n/4)`**，
内部逻辑全部留空（`???`）；未实现时 elaboration 以 `NotImplementedError` 终止。

## 2. 文件清单

```text
chisel/
├── README.md / MAPPING.md / build.mill
├── src/main/scala/exp17/
│   ├── soc/TlbTop.scala            # 模块级验证环境（对应 tlb_top.v，1001 行）
│   └── student/Tlb.scala           # ★ 学生模块：TLB 骨架（4 处 TODO）
└── src/test/scala/TlbSpec.scala    # 复现"16 写 / 16 读 / 26 查 → PASS"
```

**本实验为模块级独立环境**，不使用 `chisel-common` 中的 SoC/CPU 相关模块
（`build.mill` 因此没有引入共享库）。

## 3. 怎么做这个实验

1. 实现 `Tlb` 的四个部分：表项存储、写端口、读端口、双查找端口
   （`TODO(实现 1/4 … 4/4)`）。
2. 运行测试：
   ```bash
   cd taskvscode/exp17/chisel
   ./mill chisel.test
   ```
3. 上板流程（Vivado）沿用原书步骤，需 `../code/run_vivado/` 下的原工程
   （上板时应看到数码管显示 0x19180f0f，见原书 9.2.1.2）。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| 仿真完成 16 次写、16 次读、26 次查，全部无误后打印 `----PASS!!!` | `TlbSpec` 等待 `wOk && rOk && sOk` 全部置位，并断言 `err == 0`，打印 `----PASS!!!` |
| 读测试逐字段比对 `r_e/r_vppn/.../r_v1` | `TlbTop` 中的 `r_error` 逐字段比对（与原文件一致） |
| 查找测试按期望表比对 found/ppn/ps/plv/mat/d/v | `TlbTop` 中的 `s0_error` / `s1_error`（与原文件一致） |
| 上板数码管显示 0x19180f0f | 沿用原工程（Chisel 版不覆盖上板流程） |

## 5. 与原实验的结构性差异

1. `clk_pll` 不建模（用隐式 clock；仿真时 PLL 加速）。
2. `wait_cnt` 的重载值由参数 `simulation` 控制（5 / 30_000_000），与原 `SIMULATION 一致。
3. 为便于测试平台观测，`TlbTop` 增加了 4 个只读输出（`wOk/rOk/sOk/err`），
   不影响原有行为。

## 自检（Agent 快速检查点）

- [ ] `README.md` / `MAPPING.md` / `build.mill` 齐备且非空。
- [ ] 学生模块 TODO 标记数为 **4**（类型：`TODO(实现 n/4)`），与 `MAPPING.md` 记载一致。
- [ ] 顶层端口与 `../code/` 下原 Verilog 的例化端口一一对应（`MAPPING.md` 已列表）。
- [ ] 判据复现方式已写明（见 §4），且与原文 testbench 的检查逻辑一致。
- [ ] 未实测声明已保留（本文件 §⚠️ 与 `MAPPING.md`）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp17`。
