---
layout: default
nav_exclude: true
---

### 6.1.2 实践任务11：转移指令和访存指令添加（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

在实践任务10实现的 CPU 基础上完成以下工作：

1. 添加转移指令 `blt`、`bge`、`bltu`、`bgeu`。
2. 添加访存指令 `ld.b`、`ld.h`、`ld.bu`、`ld.hu`、`st.b`、`st.h`。
3. 运行 exp11 对应的 func（n1~n46），要求通过仿真和上板验证。

#### 参考步骤

1. 在 exp10 的流水线基础上扩展：
   - 分支：`blt`/`bge` 用**有符号**比较，`bltu`/`bgeu` 用**无符号**比较；
     目标地址 = `pc + (si16 << 2)`；
   - 访存（数据 RAM 32 位宽、按字节使能写）：
     `st.b` → `data_sram_we = 4'b0001 << addr[1:0]`；`st.h` → 选中 `addr[1]` 对应的两字节；
     `ld.b`/`ld.h` 读回后**符号扩展**，`ld.bu`/`ld.hu` **零扩展**。
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
