---
layout: default
nav_exclude: true
---

### 6.1.1 实践任务10：算术逻辑运算指令和乘除法运算指令添加（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

在实践任务9实现的 CPU 基础上完成以下工作：

1. 添加算术逻辑运算类指令：`slti`、`sltui`、`andi`、`ori`、`xori`、`sll`、`srl`、`sra`、`pcaddu12i`。
2. 添加乘除运算类指令：`mul.w`、`mulh.w`、`mulh.wu`、`div.w`、`mod.w`、`div.wu`、`mod.wu`。
3. 运行 exp10 对应的 func（n1~n36），要求通过仿真和上板验证。

#### 参考步骤

1. 在 exp9 的流水线基础上扩展译码与 ALU：
   - 立即数：`slti`/`sltui` 用 **si12 符号扩展**；`andi`/`ori`/`xori` 用 **ui12 零扩展**；
     `pcaddu12i` 用 `si20 << 12`；
   - 移位：`sll`/`srl`/`sra` 的移位量来自 `rk[4:0]`；`slli`/`srli`/`srai` 来自 ui5；
   - 乘除：`mul.w`、`mulh.w`、`mulh.wu`、`div.w`、`mod.w`、`div.wu`、`mod.wu`。
2. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
3. 上板验证（Vivado）沿用原书流程，需 `code/soc_verify/soc_bram/run_vivado/` 下的原工程；
   无硬件实验平台可跳过。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `TraceHarness.run` 断言逐条一致 |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |
