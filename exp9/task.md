---
layout: default
nav_exclude: true
---

### 5.1.3 实践任务9：前递技术解决相关引发的冲突（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/5.1.3实践任务9-前递技术解决相关引发的冲突.md`](../../CPU设计实战：LoongArch版/5.1.3实践任务9-前递技术解决相关引发的冲突.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现 + 前递）

在实践任务8实现的 CPU 基础上完成以下工作：

1. 加入适当的**数据前递通路**来减少阻塞。
2. 运行 exp9 对应的 func，要求通过仿真和上板验证，并且**仿真运行时间较 exp8 有下降**。

**Chisel 版保留的教学意图 = 从零实现（增量）**：原实验环境仍**不提供** `myCPU/` 目录；
Chisel 版只给接口骨架：
`chisel/src/main/scala/exp9/student/MyCpuTop.scala` 中有 **10 处 `TODO(实现 n/10)`**，
第 ⑩ 项就是本实验的核心（前递通路 + load-use 阻塞）。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp9/
├── 5.1.3实践任务9-前递技术解决相关引发的冲突.md   # 本文件
├── code/                                          # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill        # sources 引入 ../../chisel-common
    ├── src/main/scala/exp9/
    │   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocBramTop）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：10 处 TODO（含 ⑩ 前递）
    └── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**本实验的环境与 exp7/exp8 完全相同**（`soc_bram` 变体、同一套 SoC 装配与测试判据）。

#### 参考步骤

1. 在 exp8 的流水线基础上，把"阻塞硬等"改为前递：
   - EX 级操作数优先取 **EX/MEM** 的结果，其次 **MEM/WB** 的结果；
   - 前递条件：上游 `rd != 0` 且上游写使能有效 且 上游 `rd == 当前 rs/rt`；
   - **load-use 相关**（load 后紧跟使用其结果）数据在 MEM 级才可用，仍需 1 拍阻塞。
2. 运行测试：
   ```bash
   cd taskvscode/exp9/chisel
   ./mill chisel.test
   ```
   `TraceHarness` 会打印通过时的总周期数（对应原书"运行时间较 exp8 下降"的判据）。
3. 上板验证（Vivado）沿用原书流程，需 `code/soc_verify/soc_bram/run_vivado/` 下的原工程；
   无硬件实验平台可跳过。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `TraceHarness.run` 断言逐条一致 |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| 仿真运行时间较 exp8 下降 | 比较两个实验 `TraceHarness.run` 打印的周期数（exp9 应更少） |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

> ✅ 编译验证：`chisel/` 下的代码已用 Mill 1.0.4 + JDK 17 + Chisel 3.5.6 实测通过
> `chisel.compile` 与 `chisel.test.compile`（**未跑仿真**）。复查报告见仓库根 `verify/REPORT.md`；
> 逐行对照见 `chisel/MAPPING.md`。
