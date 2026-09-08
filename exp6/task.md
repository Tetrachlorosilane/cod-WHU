---
layout: default
nav_exclude: true
---

### 4.3.2 实践任务6：20条指令单周期CPU（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/4.3.2实践任务6-20条指令单周期CPU.md`](../../CPU设计实战：LoongArch版/4.3.2实践任务6-20条指令单周期CPU.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：找错）

1. 结合本章讲述的设计方案，阅读并理解 Chisel 实验环境中提供的 `myCPU` 代码。
2. **完成代码调试**：环境中加入了若干错误，通过仿真调试修复这些错误，使设计通过仿真验证。

原 Verilog 版把错误埋在 `myCPU/` 的 Verilog 代码里；**Chisel 版保留同样的找错意图**：
`chisel/src/main/scala/student/MyCpuTop.scala` 与 `chisel/src/main/scala/student/Alu.scala`
中**等价保留了原 Verilog 的 7 处错误**，每处带 `TODO(找错 #n)` 标记；
逐条对照见 `chisel/MAPPING.md` §4「错误映射表」（含原 bug 位置、表现、Chisel 等价写法）。
参考解在 `chisel/solution/`。**请勿直接抄参考解——先自己用 trace 比对定位。**

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp6/
├── 4.3.2实践任务6-20条指令单周期CPU.md   # 本文件
├── code/                                  # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/                                # Chisel 版实验环境
    ├── README.md / MAPPING.md / build.mill
    ├── src/main/scala/
    │   ├── common/Decoders.scala          # decoder_2_4/4_16/5_32/6_64
    │   ├── common/Regfile.scala           # 寄存器堆（本身无 bug）
    │   ├── soc/AsyncRam.scala             # async_ram / inst_ram / data_ram 行为模型
    │   ├── soc/Bridge1x2.scala            # 1×2 桥
    │   ├── soc/Confreg.scala              # 板级外设（led/数码管/按键/timer/串口）
    │   ├── soc/SocLiteTop.scala           # SoC 顶层
    │   ├── student/Alu.scala              # ★ 含 4 处错误
    │   └── student/MyCpuTop.scala         # ★ 含 3 处错误
    ├── src/test/scala/MyCpuTbSpec.scala   # 复现原 tb_top 的 golden_trace 比对
    └── solution/{Alu,MyCpuTop}.scala      # 参考解
```

Chisel 侧**全量改写**了原实验环境（SoC 顶层、桥、CONFREG、异步 RAM 模型、测试平台），
Xilinx IP 在仿真中用 `AsyncRam` 行为模型替代；Vivado 资产（`.xci`/`.xdc`/`.tcl`/`.xpr`）不改写。

#### 参考步骤

1. 修改 func 配置（原书步骤：`func/include/test_config.h` 选择 exp6 并编译）：
   Chisel 版直接使用已编译好的 `code/func/obj/inst_ram.mif`、`data_ram.mif`，
   由测试侧 `MifLoader` 读入并注入 RAM 模型，无需重新编译 func。
2. 参考结果文件 `code/gettrace/golden_trace.txt`（9776 条写回记录）已就位，
   由 `MyCpuTbSpec` 逐条比对（等价于原 `mycpu_tb.v`）。
3. 运行测试并调试：
   ```bash
   cd taskvscode/exp6/chisel
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

> ⚠️ 未实测声明：`chisel/` 下的代码未在 JDK/Mill/Chisel 环境中编译或仿真过（本次为静态交付）。
> 逐行对照与错误映射见 `chisel/MAPPING.md`；如实际运行报错，请以 `MAPPING.md` 与
> `code/` 下的原 Verilog 为准排查。
