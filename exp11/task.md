---
layout: default
nav_exclude: true
---

### 6.1.2 实践任务11：转移指令和访存指令添加（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/6.1.2实践任务11-转移指令和访存指令添加.md`](../../CPU设计实战：LoongArch版/6.1.2实践任务11-转移指令和访存指令添加.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现 + 指令扩展）

在实践任务10实现的 CPU 基础上完成以下工作：

1. 添加转移指令 `blt`、`bge`、`bltu`、`bgeu`。
2. 添加访存指令 `ld.b`、`ld.h`、`ld.bu`、`ld.hu`、`st.b`、`st.h`。
3. 运行 exp11 对应的 func（n1~n46），要求通过仿真和上板验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：原实验环境仍**不提供** `myCPU/` 目录；
Chisel 版只给接口骨架：
`chisel/src/main/scala/exp11/student/MyCpuTop.scala` 中有 **10 处 `TODO(实现 n/10)`**，
其中 ③（译码/分支）与 ⑦（访存宽度与符号扩展）列出了本实验新增的 10 条指令及实现要点。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp11/
├── 6.1.2实践任务11-转移指令和访存指令添加.md   # 本文件
├── code/                                       # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill     # sources 引入 ../../chisel-common
    ├── src/main/scala/exp11/
    │   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocBramTop）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：10 处 TODO（含新增指令清单）
    └── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**本实验的环境与 exp7~exp10 完全相同**（`soc_bram` 变体、同一套 SoC 装配与测试判据）。

#### 参考步骤

1. 在 exp10 的流水线基础上扩展：
   - 分支：`blt`/`bge` 用**有符号**比较，`bltu`/`bgeu` 用**无符号**比较；
     目标地址 = `pc + (si16 << 2)`；
   - 访存（数据 RAM 32 位宽、按字节使能写）：
     `st.b` → `data_sram_we = 4'b0001 << addr[1:0]`；`st.h` → 选中 `addr[1]` 对应的两字节；
     `ld.b`/`ld.h` 读回后**符号扩展**，`ld.bu`/`ld.hu` **零扩展**。
2. 运行测试：
   ```bash
   cd taskvscode/exp11/chisel
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

> ✅ 编译验证：`chisel/` 下的代码已用 Mill 1.0.4 + JDK 17 + Chisel 3.5.6 实测通过
> `chisel.compile` 与 `chisel.test.compile`（**未跑仿真**）。复查报告见仓库根 `verify/REPORT.md`；
> 逐行对照见 `chisel/MAPPING.md`。
