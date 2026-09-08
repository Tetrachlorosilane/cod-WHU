---
layout: default
nav_exclude: true
---

### 7.1.2 实践任务13：添加其它异常支持（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/7.1.2实践任务13-添加其它异常支持.md`](../../CPU设计实战：LoongArch版/7.1.2实践任务13-添加其它异常支持.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现 + 例外/中断扩展）

在实践任务12实现的 CPU 基础上完成以下工作：

1. 为 CPU 增加取指地址错（ADEF）、地址非对齐（ALE）、断点（BRK）和指令不存在（INE）异常的支持。
2. 为 CPU 增加中断的支持，包括 2 个软件中断、8 个硬件中断和定时器中断。
3. 为 CPU 增加控制状态寄存器 `ECFG`、`BADV`、`TID`、`TCFG`、`TVAL`、`TICLR`。
4. 为 CPU 增加 `rdcntvl.w`、`rdcntvh.w` 和 `rdcntid` 指令。
5. 运行 exp13 对应的 func（n1~n58），要求通过仿真和上板验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：原实验环境仍**不提供** `myCPU/` 目录；
Chisel 版只给接口骨架：
`chisel/src/main/scala/exp13/student/MyCpuTop.scala` 中有 **14 处 `TODO(实现 n/14)`**，
其中 ⑬（ADEF/ALE/BRK/INE 例外）与 ⑭（中断 + 定时器 + `rdcnt*`）是本实验新增的两个核心模块。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp13/
├── 7.1.2实践任务13-添加其它异常支持.md   # 本文件
├── code/                                 # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill   # sources 引入 ../../chisel-common
    ├── src/main/scala/exp13/
    │   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocBramTop）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：14 处 TODO（⑬ 例外 + ⑭ 中断/定时器）
    └── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**本实验的环境与 exp7~exp12 完全相同**（`soc_bram` 变体、同一套 SoC 装配与测试判据）。

#### 参考步骤

1. 实现 ⑬：在取指（ADEF）、译码（INE/BRK）、访存（ALE）各环节检测异常条件，
   写入 `ESTAT.Ecode`、`ERA`、`BADV`，并按例外交付流程跳转 `EENTRY`。
2. 实现 ⑭：`ECFG.LIE` 与 `ESTAT.IS` 按位使能的中断仲裁；`TCFG/TVAL/TID` 定时器
   （`TVAL` 递减到 0 触发 TI，`TICLR` 写 1 清除）；`rdcntvl.w`/`rdcntvh.w`/`rdcntid`。
3. 运行测试：
   ```bash
   cd taskvscode/exp13/chisel
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

> ✅ 编译验证：`chisel/` 下的代码已用 Mill 1.0.4 + JDK 17 + Chisel 3.5.6 实测通过
> `chisel.compile` 与 `chisel.test.compile`（**未跑仿真**）。复查报告见仓库根 `verify/REPORT.md`；
> 逐行对照见 `chisel/MAPPING.md`。
