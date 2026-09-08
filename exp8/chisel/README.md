---
exp: 8
title: 阻塞技术解决相关引发的冲突
doc: chisel-env
source: taskvscode/exp8/code/（原 Verilog 实验环境，未改动）
source_url: https://bookdown.org/loongson/_book3/
ver: agent-1.0
intent: 从零实现
keywords: [数据相关, RAW, 阻塞, stall, 气泡]
prereqs: [exp7]
objectives:
  - 检测 RAW 数据相关
  - 用阻塞（stall）消除相关
  - 跑通 exp8 的 golden_trace
layout: default
nav_exclude: true
---

# exp8 Chisel 版实验环境（实践任务8：阻塞技术解决相关引发的冲突）

> 对应原实验：`../code/`（Verilog，源自 `output/exp8`）+ `../5.1.2实践任务8-阻塞技术解决相关引发的冲突.md`。
> 改写规范：`../../CHISEL-CONVENTIONS.md`。对照表：`./MAPPING.md`。


## 本实验速览（TL;DR）

- **实验目标**：阻塞技术解决相关引发的冲突（原书实践任务8）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。
- **教学意图**：从零实现 —— 只给接口骨架 + **10 处 `TODO(实现 n/10)`**，内部逻辑留空（`???`）。
- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。
- **共享依赖**：`../../chisel-common/`（环境库，见 `../../CHISEL-CONVENTIONS.md` §2.2）。
- **⚠️ 未实测**：本目录 Chisel 代码未经编译/仿真，逐行对照见 `MAPPING.md`；运行方式见 §3。

## ⚠️ 未实测声明

> 本目录的 Chisel 代码为**静态交付**：未在 JDK/Mill/Chisel 环境中编译或仿真过。
> 如需实际运行：安装 JDK 17 + Mill，在本目录执行 `./mill chisel.test`。

## 1. 本实验要求（原书 5.1.2）

在实践任务7实现的 CPU 基础上：

1. 加入适当的逻辑处理**寄存器写后读（RAW）数据相关**引发的流水线冲突
   （本任务**只要求使用阻塞技术**，前递留到 exp9）；
2. 运行 exp8 对应的 func，通过仿真验证与上板验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：实验环境仍不提供 `myCPU/`，
`chisel/src/main/scala/exp8/student/MyCpuTop.scala` 给出**接口骨架 + 10 处 `TODO(实现)`**，
第 ⑩ 项即本实验核心（RAW 检测与阻塞）。

## 2. 文件清单

```text
chisel/
├── README.md / MAPPING.md / build.mill     # build.mill 的 sources 引入 ../../chisel-common
├── src/main/scala/exp8/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（装配在共享库 envlib.soc.SocBramTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：10 处 TODO（含 ⑩ 阻塞）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**与 exp7 的环境完全相同**（`soc_bram` 变体、同一份 `sync_ram` 模型、同一套 SoC 装配），
唯一的区别是 **func 程序**：exp8 的相关指令之间不再插入 NOP，因此必须靠阻塞解决冲突。

## 3. 怎么做这个实验

1. 先把 exp7 的流水线做出来（或直接在其基础上继续）。
2. 实现第 ⑩ 项：比较 ID 级要读的 `rj/rk` 与 EX/MEM 级尚未写回的 `rd`；
   相关时冻结 `pc` 与 IF/ID、把 ID/EX 清成气泡。
3. 运行测试：
   ```bash
   cd taskvscode/exp8/chisel
   ./mill chisel.test
   ```
4. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_bram/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp7 的关系

环境、测试平台、判据完全一致；学生任务的差异只有第 ⑩ 项（阻塞）。
共享模块清单见 `../exp7/chisel/MAPPING.md` §6。

## 自检（Agent 快速检查点）

- [ ] `README.md` / `MAPPING.md` / `build.mill` 齐备且非空。
- [ ] 学生模块 TODO 标记数为 **10**（类型：`TODO(实现 n/10)`），与 `MAPPING.md` 记载一致。
- [ ] 顶层端口与 `../code/` 下原 Verilog 的例化端口一一对应（`MAPPING.md` 已列表）。
- [ ] 判据复现方式已写明（见 §4），且与原文 testbench 的检查逻辑一致。
- [ ] 未实测声明已保留（本文件 §⚠️ 与 `MAPPING.md`）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp8`。
