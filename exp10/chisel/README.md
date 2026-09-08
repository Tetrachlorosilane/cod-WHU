---
layout: default
nav_exclude: true
---

# exp10 Chisel 版实验环境（实践任务10：算术逻辑运算指令和乘除法运算指令添加）

## 本实验速览（TL;DR）

- **实验目标**：算术逻辑与乘除指令添加（原书实践任务10）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 6.1.1）

在实践任务9实现的 CPU 基础上完成以下工作：

1. 添加算术逻辑运算类指令：`slti`、`sltui`、`andi`、`ori`、`xori`、`sll`、`srl`、`sra`、`pcaddu12i`；
2. 添加乘除运算类指令：`mul.w`、`mulh.w`、`mulh.wu`、`div.w`、`mod.w`、`div.wu`、`mod.wu`；
3. 运行 exp10 对应的 func（n1~n36），通过仿真验证与上板验证。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp10/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocBramTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：10 处 TODO（含新增指令清单）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp7~exp9 完全相同**（`soc_bram` 变体、同一套 SoC 装配与判据）；区别只在 func 程序
（exp10 覆盖 n1~n36）与 CPU 需要支持的新指令。

## 3. 怎么做这个实验

1. 在 exp9 的流水线基础上扩展译码与 ALU：
   - 立即数：`slti`/`sltui` 用 **si12 符号扩展**；`andi`/`ori`/`xori` 用 **ui12 零扩展**
     （注意与原书 2.2 节的立即数格式一致）；`pcaddu12i` 用 `si20 << 12`；
   - 移位：`sll`/`srl`/`sra` 的移位量来自 `rk[4:0]`；`slli`/`srli`/`srai` 来自 ui5；
   - 乘除：`mul.w`（低 32 位）、`mulh.w`（有符号高位）、`mulh.wu`（无符号高位）、
     `div.w`/`mod.w`（有符号）、`div.wu`/`mod.wu`（无符号）。
2. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
3. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_bram/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp9 的关系

环境、测试平台、判据完全一致；学生任务的差异是新增 16 条指令的译码与运算。
