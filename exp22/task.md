---
layout: default
nav_exclude: true
---

### 10.2.3 实践任务22：CPU中集成DCache（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

在实践任务21完成的基础上完成以下工作：

1. 将实践任务20完成的 Cache 模块作为 **DCache** 集成到实践任务21完成的 CPU 中；
2. 在采用 AXI 总线的 SoC 验证环境里完成 exp22 对应 func（n1~n72）的功能验证。

#### 参考步骤

1. 把 exp20 的 Cache 复制到本实验目录并改包名（或重新实现）。
2. 在访存通路上例化 DCache：
   - 写命中：按 `wstrb` 更新行内数据并置脏；写 miss：先取行（写分配）再写；
   - 替换脏行：通过 `wr_req`/`wr_data`（128 位整行）写回；
   - 访存未完成时冻结 MEM/WB，并与前递/例外/分支冲刷协调。
3. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
4. 上板验证（Vivado）沿用原书流程，需 `code/soc_verify/soc_axi/run_vivado/` 下的原工程；
   无硬件实验平台可跳过。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `TraceHarness.run` 断言逐条一致 |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |
