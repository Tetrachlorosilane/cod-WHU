---
exp: 5
title: 5 条指令单周期 CPU
doc: chisel-env
source: taskvscode/exp5/code/（原 Verilog 实验环境，未改动）
source_url: https://bookdown.org/loongson/_book3/
ver: agent-1.0
intent: 填空
keywords: [单周期CPU, 填空, 斐波那契, inst_ram, confreg]
prereqs: []
objectives:
  - 补全 9 处空位（6 条 assign + regfile 3 个引脚）
  - 理解单周期数据通路：取指→译码→执行→访存→写回
  - 通过斐波那契程序断言 led = ~f(n)
layout: default
nav_exclude: true
---

# exp5 Chisel 版实验环境（实践任务5：5 条指令单周期 CPU）

> 对应原实验：`../code/`（Verilog，源自 `output/exp5`）+ `../4.3.1实践任务5-5条指令单周期CPU.md`（本实验任务说明）。
> 改写规范：`../../CHISEL-CONVENTIONS.md`。
> 对照表：`./MAPPING.md`。


## 本实验速览（TL;DR）

- **实验目标**：5 条指令单周期 CPU（原书实践任务5）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。
- **教学意图**：填空 —— 保留原 Verilog 的 **9 处空位**（`???` + `TODO(填空 n/9)`），未补全时 elaboration 报错；参考解在 `solution/`。
- **判据复现**：MinicpuTopSpec：led = ~f(n) 断言。
- **共享依赖**：自包含（本目录即完整环境）。
- **✅ 编译验证**：`chisel.compile` / `chisel.test.compile` 已实测通过（未仿真）；逐行对照见 `MAPPING.md`；运行方式见 §3。

## ✅ 编译验证（未仿真）

> 本目录的 Chisel 代码已用 Mill 1.0.4 + JDK 17 + Chisel 3.5.6 实测通过 `chisel.compile` 与 `chisel.test.compile`（未跑仿真）。
> 代码按规范 §3 的 API 子集手写，`MAPPING.md` 提供逐行对照以便人工复核。
> 跑仿真：`./mill chisel.test`（exp6~exp23 需先解包运行件）。复查报告见仓库根 `verify/REPORT.md`。

## 1. 本实验要求（原书 4.3.1）

阅读并理解实验环境提供的代码，**补充代码中缺失的部分**，使设计可以通过仿真和上板验证。
测试程序是斐波那契数列：拨码开关 `switch` 的值相当于 `n`，`led` 输出 `f(n)`
（`f(1)=1, f(2)=2, f(3)=3, f(4)=5, …`）。

**Chisel 版保留的教学意图 = 填空**：`src/main/scala/student/MinicpuTop.scala` 中保留了原 Verilog 的
**9 处空位**（6 条 `assign` + regfile 的 3 个引脚），每处用 `???` + `TODO(填空)` 标记。
未补全时 elaboration 会抛 `NotImplementedError`，等价于原 Verilog 含空位时的语法错误。

## 2. 文件清单

```text
chisel/
├── README.md                       # 本文件
├── MAPPING.md                      # Verilog ↔ Chisel 逐行对照 + 填空清单 + IP 替代说明
├── build.mill                      # Mill 构建（Scala 2.13 + Chisel 3.5.x + ChiselTest 0.6.x）
├── src/main/scala/
│   ├── common/
│   │   ├── Decoders.scala          # decoder_2_4/4_16/5_32/6_64（对应 tools.v）
│   │   └── Regfile.scala           # 两读一写寄存器堆（对应 regfile.v）
│   ├── soc/
│   │   ├── Confreg.scala           # 16 位 LED 控制寄存器（对应 CONFREG/confreg.v）
│   │   ├── InstRam.scala           # inst_ram IP 的行为模型 + func/inst_ram.coe 内容
│   │   └── SocMiniTop.scala        # SoC 顶层（对应 soc_mini_top.v）
│   └── student/
│       └── MinicpuTop.scala        # ★ 学生模块：9 处填空
├── src/test/scala/
│   └── MinicpuTopSpec.scala        # ChiselTest：把"肉眼看 led"变成可断言判据
└── solution/
    └── MinicpuTop.scala            # 参考解（9 处填空的答案，不参与默认编译）
```

## 3. 怎么做这个实验

1. 打开 `src/main/scala/student/MinicpuTop.scala`，按 `TODO(填空 1/9 … 9/9)` 逐个补全。
   每处 TODO 都标注了它在原 Verilog 中的位置（文件:行号）与原注释原文。
2. 需要提示时对照 `MAPPING.md` §4「填空清单」与 `../code/miniCPU/minicpu_top.v`。
3. 补全后（或用参考解）运行测试：
   ```bash
   cp solution/MinicpuTop.scala src/main/scala/student/MinicpuTop.scala   # 用参考解
   ./mill chisel.test
   ```
4. 上板流程（Vivado 综合/实现/下载）不在本改写范围；`../code/soc_verify/run_vivado/` 保留原样。

## 4. 与原实验的判据对应

| 原实验 | Chisel 版 |
|---|---|
| `tb_top` 里 `switch = ~(8'h4)`，观察 `led` | `MinicpuTopSpec` 断言 `led = 0xfffa`（`f(4)=5`，`led = ~5`） |
| 修改 `switch` 观察 `f(n)` | 对 `n = 1,2,3,4,5,6,8,10,12` 逐一断言 `led = ~f(n) & 0xffff` |
| 参考程序 `func/start.S` | Scala 参考模型 `fib(n)`（与 `start.S` 逐句等价，见 `MinicpuTopSpec.scala` 注释） |

## 5. 与 Verilog 版的两点结构性差异

1. `clk` 端口 → Chisel 隐式 `clock`（不再作为 IO）；`resetn` 保留为显式 IO。
2. Chisel 要求"先声明后使用"，故 `pc` 的更新语句移到文件末尾、`rf_wdata`/`nextpc` 等
   前向引用的信号改为 `Wire` 先声明后赋值。信号名与含义不变。

其余差异（Xilinx IP → 行为模型、Vivado 资产不改写）见 `MAPPING.md` §1、§5。

## 自检（Agent 快速检查点）

- [ ] `README.md` / `MAPPING.md` / `build.mill` 齐备且非空。
- [ ] 学生模块 TODO 标记数为 **9**（类型：`TODO(填空 n/9)`），与 `MAPPING.md` 记载一致。
- [ ] 顶层端口与 `../code/` 下原 Verilog 的例化端口一一对应（`MAPPING.md` 已列表）。
- [ ] 判据复现方式已写明（见 §4），且与原文 testbench 的检查逻辑一致。
- [ ] 编译验证声明已保留（本文件 §✅ 与 `MAPPING.md`）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp5`。
