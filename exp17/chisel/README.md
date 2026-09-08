---
layout: default
nav_exclude: true
---

# exp17 Chisel 版实验环境（实践任务17：TLB 模块设计）

## 本实验速览（TL;DR）

- **实验目标**：TLB 模块设计（模块级）（原书实践任务17）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TlbSpec：16 写 / 16 读 / 26 查 → `----PASS!!!`。
- **共享依赖**：不依赖共享库（模块级独立环境）。

## 1. 本实验要求（原书 9.2.1）

1. 设计 **TLB 模块**（模块名 `tlb`，接口见原书 9.1 节）；
2. 利用 TLB 模块级验证环境（`tlb_top.v` + `testbench.v`）验证，通过仿真和上板验证。

## 2. 文件清单

```text
chisel/
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
   cd chisel
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
