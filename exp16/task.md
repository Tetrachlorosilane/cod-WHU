---
layout: default
nav_exclude: true
---

### 8.1.3 实践任务16：完成AXI随机延迟验证（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

在实践任务15实现的 CPU 基础上完成以下工作：

1. 完善 AXI 总线接口设计，使其在采用 AXI 总线的 SoC 验证环境里完成 exp16 对应
   func（n1~n58）的**随机延迟**功能验证，要求通过仿真和上板验证。

#### 参考步骤

1. 在 exp15 的 AXI 状态机上强化等待逻辑：
   - 请求被挡（`arready/awready/wready` 随机为低）时，请求信号必须保持不变；
   - 读数据/写响应延迟返回时，相关流水级保持停顿，不得提前推进；
   - 多个在途请求时按 ID 匹配返回数据。
2. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
3. 上板验证（Vivado）沿用原书流程，需 `code/soc_verify/soc_axi/run_vivado/` 下的原工程；
   无硬件实验平台可跳过。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `TraceHarness.run` 断言逐条一致 |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |
