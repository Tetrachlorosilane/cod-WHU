---
layout: default
nav_exclude: true
---

### 8.1.3 实践任务16：完成AXI随机延迟验证（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/8.1.3实践任务16-完成AXI随机延迟验证.md`](../../CPU设计实战：LoongArch版/8.1.3实践任务16-完成AXI随机延迟验证.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现 + 随机延迟）

在实践任务15实现的 CPU 基础上完成以下工作：

1. 完善 AXI 总线接口设计，使其在采用 AXI 总线的 SoC 验证环境里完成 exp16 对应
   func（n1~n58）的**随机延迟**功能验证，要求通过仿真和上板验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：原实验环境仍**不提供** `myCPU/` 目录；
Chisel 版只给接口骨架：
`chisel/src/main/scala/exp16/student/MyCpuTop.scala` 中有 **15 处 `TODO(实现 n/15)`**，
第 ⑮ 项就是本实验的核心（随机延迟下的等待/互锁与 ID 匹配）。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp16/
├── 8.1.3实践任务16-完成AXI随机延迟验证.md   # 本文件
├── code/                                     # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill   # sources 引入 ../../chisel-common
    ├── src/main/scala/exp16/
    │   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：15 处 TODO（⑮ 随机延迟正确性）
    └── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp15 完全相同**（`soc_axi` 变体、同一套 AXI 模型与判据）；原实验两个 exp 的 RTL
逐字相同，区别只在 func 程序（随机延迟验证）与参考 trace —— 详见 `chisel/MAPPING.md` §2。

#### 参考步骤

1. 在 exp15 的 AXI 状态机上强化等待逻辑：
   - 请求被挡（`arready/awready/wready` 随机为低）时，请求信号必须保持不变；
   - 读数据/写响应延迟返回时，相关流水级保持停顿，不得提前推进；
   - 多个在途请求时按 ID 匹配返回数据。
2. 运行测试：
   ```bash
   cd taskvscode/exp16/chisel
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
