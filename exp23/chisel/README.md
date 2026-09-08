---
layout: default
nav_exclude: true
---

# exp23 Chisel 版实验环境（实践任务23：CPU 中添加 CACOP 指令）

## 本实验速览（TL;DR）

- **实验目标**：CACOP 指令（原书实践任务23）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 10.2.4）

在实践任务22完成的基础上：

1. 在实践任务22完成的 CPU 中增加 **CACOP 指令**实现；
2. 在 AXI SoC 验证环境里完成 exp23 对应 func（n1~n79）的功能验证。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp23/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：20 处 TODO（⑳ CACOP）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp15~exp22 完全相同**（`soc_axi` 变体）；区别只在 CPU 内部新增 CACOP 指令。

## 3. 怎么做这个实验

1. 实现 ⑳：`cacop code, rj, si12`
   - `code[4:3]`：0 = 按地址操作 ICache，1 = 按地址操作 DCache，
     2 = 按索引操作 ICache，3 = 按索引操作 DCache；
   - `code[2:0]`：具体操作（Store Tag / 无效化 / 写回并无效化等）；
   - 按地址操作时虚地址为 `rj + si12`，需先做地址转换（DMW/TLB）；
   - 按索引操作用虚地址中的 index 字段直接定位 Cache 行；
   - 注意与流水线配合（停顿、后续取指/访存的一致性）。
2. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
3. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_axi/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp22 的关系

环境、判据、I/D Cache 与 TLB 完全相同；差异是新增 CACOP 指令。
本实验是 exp5~exp23 改写序列的最后一个实验。
