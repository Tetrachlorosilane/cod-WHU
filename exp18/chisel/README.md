---
layout: default
nav_exclude: true
---

# exp18 Chisel 版实验环境（实践任务18：添加 TLB 相关指令和 CSR 寄存器）

## 本实验速览（TL;DR）

- **实验目标**：TLB 指令与 CSR（原书实践任务18）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 9.2.2）

在实践任务16（AXI CPU）和实践任务17（TLB 模块）的基础上完成以下工作：

1. 把 TLB 模块集成到 CPU 中；
2. 增加 `TLBSRCH`、`TLBRD`、`TLBWR`、`TLBFILL`、`INVTLB` 指令；
3. 增加 `TLBIDX`、`TLBEHI`、`TLBELO0`、`TLBELO1`、`ASID`、`TLBRENTRY` CSR；
4. 在 AXI SoC 验证环境里完成 exp18 对应 func（n1~n70）的功能验证。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp18/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：16 处 TODO（⑯ TLB 指令）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp15/exp16 完全相同**（`soc_axi` 变体）；区别只在 func（n1~n70）与
CPU 需要新增的 TLB 指令/CSR。

## 3. 怎么做这个实验

1. 把 exp17 的 TLB 实现复制到本实验目录并改包名：
   ```bash
   cp ../exp17/chisel/src/main/scala/exp17/student/Tlb.scala \
      src/main/scala/exp18/student/Tlb.scala
   # 然后把文件里的 `package exp17.student` 改为 `package exp18.student`
   ```
2. 在 `MyCpuTop` 中例化 `Tlb`，实现 5 条 TLB 指令与 6 个 TLB 相关 CSR
3. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
4. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_axi/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp16/exp17 的关系

环境沿用 exp16（AXI），TLB 模块沿用 exp17 的实现；本实验把它们接起来并新增
