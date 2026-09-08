---
layout: default
nav_exclude: true
---

### 7.1.2 实践任务13：添加其它异常支持（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

在实践任务12实现的 CPU 基础上完成以下工作：

1. 为 CPU 增加取指地址错（ADEF）、地址非对齐（ALE）、断点（BRK）和指令不存在（INE）异常的支持。
2. 为 CPU 增加中断的支持，包括 2 个软件中断、8 个硬件中断和定时器中断。
3. 为 CPU 增加控制状态寄存器 `ECFG`、`BADV`、`TID`、`TCFG`、`TVAL`、`TICLR`。
4. 为 CPU 增加 `rdcntvl.w`、`rdcntvh.w` 和 `rdcntid` 指令。
5. 运行 exp13 对应的 func（n1~n58），要求通过仿真和上板验证。

#### 参考步骤

1. 实现 ⑬：在取指（ADEF）、译码（INE/BRK）、访存（ALE）各环节检测异常条件，
   写入 `ESTAT.Ecode`、`ERA`、`BADV`，并按例外交付流程跳转 `EENTRY`。
2. 实现 ⑭：`ECFG.LIE` 与 `ESTAT.IS` 按位使能的中断仲裁；`TCFG/TVAL/TID` 定时器
   （`TVAL` 递减到 0 触发 TI，`TICLR` 写 1 清除）；`rdcntvl.w`/`rdcntvh.w`/`rdcntid`。
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
