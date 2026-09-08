---
layout: default
nav_exclude: true
---

### 5.1.1 实践任务7：不考虑相关冲突处理的简单流水线CPU（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

在实践任务6实现的单周期 CPU 基础上完成以下工作：

1. 调整 CPU 顶层接口，增加指令 RAM 片选 `inst_sram_en` 与数据 RAM 片选 `data_sram_en`。
2. 把 `inst_sram_we` 和 `data_sram_we` 都从 1 比特写使能调整为 **4 比特字节写使能**。
3. 设计一个不考虑相关引发的冲突的单发射五级流水 CPU。
4. 运行 exp7 对应的 func，要求通过仿真和上板验证。

#### 参考步骤

1. 通读 `chisel/src/main/scala/exp7/student/MyCpuTop.scala` 的骨架注释（IF→ID→EX→MEM→WB 九步）。
2. 参考 exp6 的单周期实现（`../exp6/code/myCPU/`，注意它含错误）与本实验的 SoC 环境；
3. 逐条实现 `TODO(实现 1/9 … 9/9)`，然后运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
4. 本实验**不要求**处理数据相关/控制相关引发的冲突（那是 exp8/exp9 的任务）；
   exp7 的 func 程序在相关指令之间已插入 NOP，因此流水线即使不做冲突处理也能跑通。
5. 上板验证（Vivado 综合/实现/生成比特流/下载）沿用原书流程，需
   `code/soc_verify/soc_bram/run_vivado/` 下的原工程；无硬件实验平台可跳过。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `MyCpuTbSpec` 断言：逐条一致；不一致直接 `fail` 并打印 reference/mycpu 两行 |
| `debug_wb_pc == 32'h1c000100` 时结束，打印 `----PASS!!!` | 到达 `END_PC` 后断言通过并打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | 同样监视，异常计数非 0 即失败 |
