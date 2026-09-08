---
exp: 18
title: TLB 指令与 CSR
doc: chisel-env
source: taskvscode/exp18/code/（原 Verilog 实验环境，未改动）
source_url: https://bookdown.org/loongson/_book3/
ver: agent-1.0
intent: 从零实现
keywords: [TLB 指令, TLBIDX, TLBEHI, TLBELO, TLBRENTRY]
prereqs: [exp16, exp17]
objectives:
  - 集成 exp17 的 TLB
  - 新增 TLBSRCH/TLBRD/TLBWR/TLBFILL/INVTLB
  - 新增 TLBIDX/TLBEHI/TLBELO0/1/ASID/TLBRENTRY
---

# exp18 Chisel 版实验环境（实践任务18：添加 TLB 相关指令和 CSR 寄存器）

> 对应原实验：`../code/`（Verilog，源自 `output/exp18`）+ `../9.2.2实践任务18-添加TLB相关指令和CSR寄存器.md`。
> 改写规范：`../../CHISEL-CONVENTIONS.md`。对照表：`./MAPPING.md`。


## 本实验速览（TL;DR）

- **实验目标**：TLB 指令与 CSR（原书实践任务18）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。
- **教学意图**：从零实现 —— 只给接口骨架 + **16 处 `TODO(实现 n/16)`**，内部逻辑留空（`???`）。
- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。
- **共享依赖**：`../../chisel-common/`（环境库，见 `../../CHISEL-CONVENTIONS.md` §2.2）。
- **⚠️ 未实测**：本目录 Chisel 代码未经编译/仿真，逐行对照见 `MAPPING.md`；运行方式见 §3。

## ⚠️ 未实测声明

> 本目录的 Chisel 代码为**静态交付**：未在 JDK/Mill/Chisel 环境中编译或仿真过。
> 如需实际运行：安装 JDK 17 + Mill，在本目录执行 `./mill chisel.test`。

## 1. 本实验要求（原书 9.2.2）

在实践任务16（AXI CPU）和实践任务17（TLB 模块）的基础上完成以下工作：

1. 把 TLB 模块集成到 CPU 中；
2. 增加 `TLBSRCH`、`TLBRD`、`TLBWR`、`TLBFILL`、`INVTLB` 指令；
3. 增加 `TLBIDX`、`TLBEHI`、`TLBELO0`、`TLBELO1`、`ASID`、`TLBRENTRY` CSR；
4. 在 AXI SoC 验证环境里完成 exp18 对应 func（n1~n70）的功能验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：实验环境仍不提供 `myCPU/`，
`chisel/src/main/scala/exp18/student/MyCpuTop.scala` 给出**接口骨架 + 16 处 `TODO(实现)`**，
⑯ 即本实验核心（TLB 例化 + 5 条 TLB 指令）。

## 2. 文件清单

```text
chisel/
├── README.md / MAPPING.md / build.mill     # build.mill 的 sources 引入 ../../chisel-common
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
   （字段定义见 `./MAPPING.md` §2）。
3. 运行测试：
   ```bash
   cd taskvscode/exp18/chisel
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
TLB 指令与 CSR。共享模块清单见 `../exp15/chisel/MAPPING.md` §5。

## 自检（Agent 快速检查点）

- [ ] `README.md` / `MAPPING.md` / `build.mill` 齐备且非空。
- [ ] 学生模块 TODO 标记数为 **16**（类型：`TODO(实现 n/16)`），与 `MAPPING.md` 记载一致。
- [ ] 顶层端口与 `../code/` 下原 Verilog 的例化端口一一对应（`MAPPING.md` 已列表）。
- [ ] 判据复现方式已写明（见 §4），且与原文 testbench 的检查逻辑一致。
- [ ] 未实测声明已保留（本文件 §⚠️ 与 `MAPPING.md`）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp18`。
