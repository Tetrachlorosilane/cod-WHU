---
layout: default
nav_exclude: true
---

### 5.1.2 实践任务8：阻塞技术解决相关引发的冲突（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

在实践任务7实现的 CPU 基础上完成以下工作：

1. 加入适当的逻辑处理**寄存器写后读（RAW）数据相关**引发的流水线冲突
   （本任务中**只要求使用阻塞技术**，前递留到实践任务9）。
2. 运行 exp8 对应的 func，要求通过仿真和上板验证。

#### 参考步骤

1. 先完成 exp7 的五级流水线（或不处理冲突的版本）。
2. 实现第 ⑩ 项：比较 ID 级要读的 `rj/rk` 与 EX/MEM 级尚未写回的 `rd`；
   相关时冻结 `pc` 与 IF/ID 流水寄存器、把 ID/EX 清成气泡（插入 NOP）。
3. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
4. 上板验证（Vivado）沿用原书流程，需 `code/soc_verify/soc_bram/run_vivado/` 下的原工程；
   无硬件实验平台可跳过。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `TraceHarness.run` 断言逐条一致，不一致直接 `fail` 并打印两行对照 |
| `debug_wb_pc == 32'h1c000100` 时结束，打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视，异常计数非 0 即失败 |
