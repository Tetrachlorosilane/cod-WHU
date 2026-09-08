---
layout: default
nav_exclude: true
---

### 7.1.1 实践任务12：添加系统调用异常支持（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

在实践任务11实现的 CPU 基础上完成以下工作：

1. 为 CPU 增加 `csrrd`、`csrwr`、`csrxchg` 和 `ertn` 指令。
2. 为 CPU 增加控制状态寄存器 `CRMD`、`PRMD`、`ESTAT`、`ERA`、`EENTRY`、`SAVE0~3`。
3. 为 CPU 增加 `syscall` 指令，实现系统调用异常支持。
4. 运行 exp12 对应的 func（n1~n47），要求通过仿真和上板验证。

#### 参考步骤

1. 实现 ⑪：CSR 寄存器堆（`CRMD`/`PRMD`/`ESTAT`/`ERA`/`EENTRY`/`SAVE0~3`）与
   `csrrd`/`csrwr`/`csrxchg`（`csr_num = inst[23:10]`，`csrxchg` 以 rj 为掩码）。
2. 实现 ⑫：`syscall` 触发例外（`Ecode = 0xB`、`ERA ← PC`、`ESTAT/CRMD/PRMD` 更新、
   `PC ← EENTRY`、流水线冲刷）与 `ertn` 返回（`PRMD` 恢复 PLV/IE、`PC ← ERA`）。
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
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `TraceHarness.run` 断言逐条一致 |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |
