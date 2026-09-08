---
exp: 7
title: 简单流水线 CPU（不考虑相关冲突）
doc: chisel-env
source: taskvscode/exp7/code/（原 Verilog 实验环境，未改动）
source_url: https://bookdown.org/loongson/_book3/
ver: agent-1.0
intent: 从零实现
keywords: [五级流水线, IF/ID/EX/MEM/WB, block RAM, 同步读]
prereqs: [exp6]
objectives:
  - 实现 IF/ID/EX/MEM/WB 五级流水线
  - 处理 block RAM 同步读的时序
  - 跑通 exp7 的 golden_trace
layout: default
nav_exclude: true
---

# exp7 Chisel 版实验环境（实践任务7：不考虑相关冲突处理的简单流水线 CPU）

> 对应原实验：`../code/`（Verilog，源自 `output/exp7`）+ `../5.1.1实践任务7-不考虑相关冲突处理的简单流水线CPU.md`。
> 改写规范：`../../CHISEL-CONVENTIONS.md`。对照表：`./MAPPING.md`。


## 本实验速览（TL;DR）

- **实验目标**：简单流水线 CPU（不考虑相关冲突）（原书实践任务7）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。
- **教学意图**：从零实现 —— 只给接口骨架 + **9 处 `TODO(实现 n/9)`**，内部逻辑留空（`???`）。
- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。
- **共享依赖**：`../../chisel-common/`（环境库，见 `../../CHISEL-CONVENTIONS.md` §2.2）。
- **⚠️ 未实测**：本目录 Chisel 代码未经编译/仿真，逐行对照见 `MAPPING.md`；运行方式见 §3。

## ⚠️ 未实测声明

> 本目录的 Chisel 代码为**静态交付**：未在 JDK/Mill/Chisel 环境中编译或仿真过。
> 如需实际运行：安装 JDK 17 + Mill，在本目录执行 `./mill chisel.test`。

## 1. 本实验要求（原书 5.1.1）

在实践任务6（单周期 CPU）的基础上：

1. 调整 CPU 顶层接口，增加指令 RAM 片选 `inst_sram_en` 与数据 RAM 片选 `data_sram_en`；
2. 把 `inst_sram_we` / `data_sram_we` 从 1 位写使能改为 **4 位字节写使能**；
3. 设计一个**不考虑相关冲突**的单发射五级流水 CPU；
4. 运行 exp7 对应的 func，通过仿真验证（`gettrace/golden_trace.txt` 逐条比对）与上板验证。

**Chisel 版保留的教学意图 = 从零实现**：原实验环境**不提供** `myCPU/`，
`chisel/src/main/scala/exp7/student/MyCpuTop.scala` 只给出**接口骨架 + 9 处 `TODO(实现)`**，
内部流水线逻辑全部留空（`???`）。未实现时 elaboration 直接以 `NotImplementedError` 终止。

## 2. 文件清单

```text
chisel/
├── README.md                       # 本文件
├── MAPPING.md                      # Verilog ↔ Chisel 对照 + 接口变化 + 共享模块说明
├── build.mill                      # sources 引入 ../../chisel-common
├── src/main/scala/exp7/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（装配在共享库 envlib.soc.SocBramTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：从零实现（9 处 TODO）
└── src/test/scala/MyCpuTbSpec.scala  # golden_trace 逐条比对 + 功能测试点监视
```

实验环境的公共模块集中在 `../../chisel-common/src/main/scala/envlib/`：
`common/{Decoders,Regfile}.scala`、`soc/{CpuIO,Bridge1x2,Confreg,SyncRam}.scala`
（`CpuIO`/`LACpu` = 学生 CPU 的统一接口，`SocBramTop` = SoC 装配）；
测试工具 `src/test/scala/envlib/test/Loaders.scala`（`MifLoader` / `TraceLoader`）。
自 exp7 起采用"共享环境库 + 每实验自有学生代码/薄 SoC 封装/测试/文档"的布局，见
`../../CHISEL-CONVENTIONS.md` §2。

## 3. 怎么做这个实验

1. 通读 `src/main/scala/exp7/student/MyCpuTop.scala` 的骨架注释（IF→ID→EX→MEM→WB 九步）。
2. 参考 `../code/` 下 exp6 的 `myCPU/`（单周期版本，注意它含错误）与 exp7 的 SoC 环境，
   以及 `../../chisel4agent/08-五级流水线CPU串讲.md`、`09-CPU开发场景速查.md`。
3. 逐条实现 `TODO(实现 1/9 … 9/9)`，直到能 elaborate 并跑通测试：
   ```bash
   cd taskvscode/exp7/chisel
   ./mill chisel.test
   ```
4. 本实验**不要求**处理数据相关/控制相关引发的冲突（那是 exp8/exp9）；
   func 程序中的相关指令之间已插入 NOP，因此流水线即使"裸奔"也能跑通。
5. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_bram/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `MyCpuTbSpec` 逐条比对 `debug_wb_pc / wnum / wdata`（字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后断言通过并打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | 同样监视 `dut.u_confreg.io.num_data` |
| `$readmemb` 读 `func/obj/*.mif` | 测试侧 `MifLoader` 读入并注入 `SyncRam`（block RAM 行为模型） |

## 5. 与 Verilog 版的结构性差异

1. `clk` → Chisel 隐式 clock；`resetn` 保留为显式 IO。
2. `inst_ram` / `data_ram`（block RAM，同步读）→ `envlib.soc.SyncRam`（字节写 + 同步读，
   初始化由构造参数注入）。
3. `soc_lite_top` 的 `debug_wb_*` 内部线网 → 测试直接访问 `soc.cpu.io.debug_wb_*`。
4. `confreg` 的 `timer` 原用独立 `timer_clk`；本实验 `timer_clk = cpu_clk`，合并为同一时钟域。
5. `.xci/.xdc/.tcl/.xpr` 等 Vivado 资产不改写。

## 自检（Agent 快速检查点）

- [ ] `README.md` / `MAPPING.md` / `build.mill` 齐备且非空。
- [ ] 学生模块 TODO 标记数为 **9**（类型：`TODO(实现 n/9)`），与 `MAPPING.md` 记载一致。
- [ ] 顶层端口与 `../code/` 下原 Verilog 的例化端口一一对应（`MAPPING.md` 已列表）。
- [ ] 判据复现方式已写明（见 §4），且与原文 testbench 的检查逻辑一致。
- [ ] 未实测声明已保留（本文件 §⚠️ 与 `MAPPING.md`）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp7`。
