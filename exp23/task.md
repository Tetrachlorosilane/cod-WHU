---
layout: default
nav_exclude: true
---

### 10.2.4 实践任务23：CPU中添加CACOP指令（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/10.2.4实践任务23-CPU中添加CACOP指令.md`](../../CPU设计实战：LoongArch版/10.2.4实践任务23-CPU中添加CACOP指令.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现 + CACOP）

在实践任务22完成的基础上完成以下工作：

1. 在实践任务22完成的 CPU 中增加 **CACOP 指令**实现；
2. 在采用 AXI 总线的 SoC 验证环境里完成 exp23 对应 func（n1~n79）的功能验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：原实验环境仍**不提供** `myCPU/` 目录；
Chisel 版只给接口骨架：
`chisel/src/main/scala/exp23/student/MyCpuTop.scala` 中有 **20 处 `TODO(实现 n/20)`**，
第 ⑳ 项就是本实验的核心（CACOP 译码 + Cache 操作通路 + 流水线配合）。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp23/
├── 10.2.4实践任务23-CPU中添加CACOP指令.md   # 本文件
├── code/                                     # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill   # sources 引入 ../../chisel-common
    ├── src/main/scala/exp23/
    │   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：20 处 TODO（⑳ CACOP）
    └── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**本实验的环境与 exp15~exp22 完全相同**（`soc_axi` 变体、同一套 AXI 模型与判据）。

#### 参考步骤

1. 实现 ⑳：`cacop code, rj, si12`
   - `code[4:3]`：0 = 按地址操作 ICache，1 = 按地址操作 DCache，
     2 = 按索引操作 ICache，3 = 按索引操作 DCache；
   - `code[2:0]`：具体 Cache 操作（Store Tag / 无效化 / 写回并无效化等，以原书表格为准）；
   - 按地址操作时虚地址为 `rj + si12`，需先做地址转换（DMW/TLB）；
   - 按索引操作用虚地址中的 index 字段直接定位 Cache 行；
   - 注意与流水线配合（必要时停顿，保证后续取指/访存看到一致的数据）。
2. 运行测试：
   ```bash
   cd taskvscode/exp23/chisel
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

> ✅ 编译验证：`chisel/` 下的代码已用 Mill 1.0.4 + JDK 17 + Chisel 3.5.6 实测通过
> `chisel.compile` 与 `chisel.test.compile`（**未跑仿真**）。复查报告见仓库根 `verify/REPORT.md`；
> 逐行对照见 `chisel/MAPPING.md`。
