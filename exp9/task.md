---
layout: default
nav_exclude: true
---

### 5.1.3 实践任务9：前递技术解决相关引发的冲突（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

在实践任务8实现的 CPU 基础上完成以下工作：

1. 加入适当的**数据前递通路**来减少阻塞。
2. 运行 exp9 对应的 func，要求通过仿真和上板验证，并且**仿真运行时间较 exp8 有下降**。

#### 参考步骤

1. 在 exp8 的流水线基础上，把"阻塞硬等"改为前递：
   - EX 级操作数优先取 **EX/MEM** 的结果，其次 **MEM/WB** 的结果；
   - 前递条件：上游 `rd != 0` 且上游写使能有效 且 上游 `rd == 当前 rs/rt`；
   - **load-use 相关**（load 后紧跟使用其结果）数据在 MEM 级才可用，仍需 1 拍阻塞。
2. 运行测试：
   ```bash
   cd chisel
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
