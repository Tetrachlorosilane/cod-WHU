---
layout: default
nav_exclude: true
---

# exp11 Chisel 版实验环境（实践任务11：转移指令和访存指令添加）

## 本实验速览（TL;DR）

- **实验目标**：转移与访存指令添加（原书实践任务11）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 6.1.2）

在实践任务10实现的 CPU 基础上完成以下工作：

1. 添加转移指令 `blt`、`bge`、`bltu`、`bgeu`；
2. 添加访存指令 `ld.b`、`ld.h`、`ld.bu`、`ld.hu`、`st.b`、`st.h`；
3. 运行 exp11 对应的 func（n1~n46），通过仿真验证与上板验证。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp11/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocBramTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：10 处 TODO（含新增指令清单）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp7~exp10 完全相同**（`soc_bram` 变体、同一套 SoC 装配与判据）；区别只在 func
（exp11 覆盖 n1~n46）与 CPU 需要支持的新指令。

## 3. 怎么做这个实验

1. 在 exp10 的流水线基础上扩展：
   - 分支：`blt`/`bge` 有符号比较，`bltu`/`bgeu` 无符号比较；目标 = `pc + (si16 << 2)`；
   - 访存宽度与对齐（数据 RAM 为 32 位宽、按字节使能写）：
     `st.b` → `we = 4'b0001 << addr[1:0]`；`st.h` → 选中 `addr[1]` 对应的两字节；
     `ld.b`/`ld.h` 符号扩展，`ld.bu`/`ld.hu` 零扩展；读回后按 `addr[1:0]` 选择字节/半字。
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

## 5. 与 exp10 的关系

环境、测试平台、判据完全一致；学生任务的差异是新增 4 条转移指令与 6 条访存指令。
