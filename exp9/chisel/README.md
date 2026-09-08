---
layout: default
nav_exclude: true
---

# exp9 Chisel 版实验环境（实践任务9：前递技术解决相关引发的冲突）

## 本实验速览（TL;DR）

- **实验目标**：前递技术解决相关引发的冲突（原书实践任务9）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 5.1.3）

在实践任务8实现的 CPU 基础上：

1. 用**前递（forwarding / bypass）**技术优化数据相关的处理，减少不必要的流水线停顿；
2. 运行 exp9 对应的 func，通过仿真验证与上板验证。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp9/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocBramTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：10 处 TODO（含 ⑩ 前递）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp7/exp8 完全相同**（`soc_bram` 变体、同一套 SoC 装配与判据）；
区别只在学生 CPU 内部的数据相关处理方式与 func 程序。

## 3. 怎么做这个实验

1. 在 exp8 的流水线基础上，把"阻塞硬等"改为前递：
   - EX 级操作数优先取 **EX/MEM** 的结果，其次 **MEM/WB** 的结果；
   - 前递条件：上游 `rd != 0` 且上游写使能有效 且 上游 `rd == 当前 rs/rt`；
   - **load-use 相关**（load 后紧跟使用其结果）数据在 MEM 级才可用，仍需 1 拍阻塞。
2. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
3. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_bram/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp8 的关系

环境、测试平台、判据完全一致；学生任务的差异只有第 ⑩ 项（阻塞 → 前递）。
