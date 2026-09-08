---
layout: default
nav_exclude: true
---

### 8.1.1 实践任务14：添加类SRAM总线支持（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

在实践任务13实现的 CPU 基础上完成以下工作：

1. 将 CPU 对外接口修改为**类 SRAM 总线接口**（握手式）。
2. 在采用握手机制的 block RAM 的 SoC 验证环境中完成 exp14 对应 func 的功能验证。

#### 参考步骤

1. 把取指/访存改为握手请求：`req && addr_ok` 表示请求被接收，此后请求不可撤销；
   读请求的 `data_ok` 可能晚若干拍返回，需冻结相关流水级（互锁）。
2. 注意握手停顿与分支冲刷、例外冲刷的优先级关系。
3. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
4. 上板验证（Vivado）沿用原书流程，需 `code/soc_verify/soc_hs_bram/run_vivado/` 下的原工程；
   无硬件实验平台可跳过。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `TraceHarness.run` 断言逐条一致 |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |
