---
layout: default
nav_exclude: true
---

### 4.3.2 实践任务6：20条指令单周期CPU（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

1. 结合本章讲述的设计方案，阅读并理解 Chisel 实验环境中提供的 `myCPU` 代码。
2. **完成代码调试**：环境中加入了若干错误，通过仿真调试修复这些错误，使设计通过仿真验证。

原 Verilog 版把错误埋在 `myCPU/` 的 Verilog 代码里；**Chisel 版保留同样的找错意图**：
`chisel/src/main/scala/student/MyCpuTop.scala` 与 `chisel/src/main/scala/student/Alu.scala`
中**等价保留了原 Verilog 的 7 处错误**，每处带 `TODO(找错 #n)` 标记；
参考解在 `chisel/solution/`。**请勿直接抄参考解——先自己用 trace 比对定位。**

#### 参考步骤

1. 修改 func 配置（原书步骤：`func/include/test_config.h` 选择 exp6 并编译）：
   Chisel 版直接使用已编译好的 `code/func/obj/inst_ram.mif`、`data_ram.mif`，
   由测试侧 `MifLoader` 读入并注入 RAM 模型，无需重新编译 func。
2. 参考结果文件 `code/gettrace/golden_trace.txt`（9776 条写回记录）已就位，
   由 `MyCpuTbSpec` 逐条比对（等价于原 `mycpu_tb.v`）。
3. 运行测试并调试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
   失败信息会给出第一条不一致的 `PC / wb_rf_wnum / wb_rf_wdata`，据此定位错误。
4. 修好全部 7 处错误后测试应打印 `----PASS!!!`；
   也可用参考解验证环境本身：`cp solution/*.scala src/main/scala/student/ && ./mill chisel.test`。
5. 上板验证（Vivado 综合/实现/生成比特流/下载）沿用原书流程，需 `code/soc_verify/soc_dram/run_vivado/`
   下的原工程；无硬件实验平台可跳过。Chisel 版不覆盖上板流程。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `golden_trace.txt` 比对，不一致即报 Error 并终止 | `MyCpuTbSpec` 断言：逐条一致；不一致直接 `fail` 并打印 reference/mycpu 两行 |
| `debug_wb_pc == 32'h1c000100` 时结束，打印 `----PASS!!!` | 到达 `END_PC` 后断言通过并打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data`（低 8 位 +1、高 8 位 +1） | 同样监视，异常计数非 0 即失败 |
