---
exp: 16
title: AXI 随机延迟验证
doc: chisel-env
source: taskvscode/exp16/code/（原 Verilog 实验环境，未改动）
source_url: https://bookdown.org/loongson/_book3/
ver: agent-1.0
intent: 从零实现
keywords: [AXI4, 随机延迟, ID 匹配, 握手保持]
prereqs: [exp15]
objectives:
  - 在随机延迟下保持请求与等待语义
  - 按 ID 匹配返回数据
  - 跑通 exp16 的 golden_trace
layout: default
nav_exclude: true
---

# exp16 Chisel 版实验环境（实践任务16：完成 AXI 随机延迟验证）

> 对应原实验：`../code/`（Verilog，源自 `output/exp16`）+ `../8.1.3实践任务16-完成AXI随机延迟验证.md`。
> 改写规范：`../../CHISEL-CONVENTIONS.md`。对照表：`./MAPPING.md`。


## 本实验速览（TL;DR）

- **实验目标**：AXI 随机延迟验证（原书实践任务16）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。
- **教学意图**：从零实现 —— 只给接口骨架 + **15 处 `TODO(实现 n/15)`**，内部逻辑留空（`???`）。
- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。
- **共享依赖**：`../../chisel-common/`（环境库，见 `../../CHISEL-CONVENTIONS.md` §2.2）。
- **⚠️ 未实测**：本目录 Chisel 代码未经编译/仿真，逐行对照见 `MAPPING.md`；运行方式见 §3。

## ⚠️ 未实测声明

> 本目录的 Chisel 代码为**静态交付**：未在 JDK/Mill/Chisel 环境中编译或仿真过。
> 如需实际运行：安装 JDK 17 + Mill，在本目录执行 `./mill chisel.test`。

## 1. 本实验要求（原书 8.1.3）

在 exp15 的 AXI 接口 CPU 基础上，使其能在 **AXI 随机延迟**的响应下正确工作，
完成 exp16 对应 func（n1~n58）的功能验证与上板验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：实验环境仍不提供 `myCPU/`，
`chisel/src/main/scala/exp16/student/MyCpuTop.scala` 给出**接口骨架 + 15 处 `TODO(实现)`**，
⑮ 即本实验核心（随机延迟下的等待/互锁与 ID 匹配）。

## 2. 文件清单

```text
chisel/
├── README.md / MAPPING.md / build.mill     # build.mill 的 sources 引入 ../../chisel-common
├── src/main/scala/exp16/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：15 处 TODO（⑮ 随机延迟正确性）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

## 3. 怎么做这个实验

1. 在 exp15 的 AXI 状态机上强化等待逻辑：
   - 请求被挡（`arready/awready/wready` 随机为低）时，请求信号必须保持不变；
   - 读数据/写响应延迟返回时，相关流水级保持停顿，不得提前推进；
   - 多个在途请求时按 ID 匹配返回数据。
2. 运行测试：
   ```bash
   cd taskvscode/exp16/chisel
   ./mill chisel.test
   ```
3. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_axi/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp15 的关系

**SoC 与测试平台完全相同**（`soc_axi` 变体、同一套 AXI 模型与判据）；RTL 也相同——
原实验的 exp15/exp16 只差 func 程序（`_RUN_PERF_TEST` 与 `ifdef RUN_PERF_TEST` 的写法
使随机掩码在两者中实际都生效，见 `./MAPPING.md` §2）。
共享模块清单见 `../exp15/chisel/MAPPING.md` §5。

## 自检（Agent 快速检查点）

- [ ] `README.md` / `MAPPING.md` / `build.mill` 齐备且非空。
- [ ] 学生模块 TODO 标记数为 **15**（类型：`TODO(实现 n/15)`），与 `MAPPING.md` 记载一致。
- [ ] 顶层端口与 `../code/` 下原 Verilog 的例化端口一一对应（`MAPPING.md` 已列表）。
- [ ] 判据复现方式已写明（见 §4），且与原文 testbench 的检查逻辑一致。
- [ ] 未实测声明已保留（本文件 §⚠️ 与 `MAPPING.md`）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp16`。
