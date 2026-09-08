---
exp: 14
title: 类 SRAM 握手总线
doc: chisel-env
source: taskvscode/exp14/code/（原 Verilog 实验环境，未改动）
source_url: https://bookdown.org/loongson/_book3/
ver: agent-1.0
intent: 从零实现
keywords: [握手总线, 类SRAM, sram_wrap, 互锁]
prereqs: [exp13]
objectives:
  - 把访存接口改为 req/wr/size/wstrb/addr/wdata + addr_ok/data_ok/rdata
  - 实现握手未完成时的流水线互锁
  - 跑通 exp14 的 golden_trace
layout: default
nav_exclude: true
---

# exp14 Chisel 版实验环境（实践任务14：添加类 SRAM 总线支持）

> 对应原实验：`../code/`（Verilog，源自 `output/exp14`）+ `../8.1.1实践任务14-添加类SRAM总线支持.md`。
> 改写规范：`../../CHISEL-CONVENTIONS.md`。对照表：`./MAPPING.md`。


## 本实验速览（TL;DR）

- **实验目标**：类 SRAM 握手总线（原书实践任务14）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。
- **教学意图**：从零实现 —— 只给接口骨架 + **14 处 `TODO(实现 n/14)`**，内部逻辑留空（`???`）。
- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。
- **共享依赖**：`../../chisel-common/`（环境库，见 `../../CHISEL-CONVENTIONS.md` §2.2）。
- **⚠️ 未实测**：本目录 Chisel 代码未经编译/仿真，逐行对照见 `MAPPING.md`；运行方式见 §3。

## ⚠️ 未实测声明

> 本目录的 Chisel 代码为**静态交付**：未在 JDK/Mill/Chisel 环境中编译或仿真过。
> 如需实际运行：安装 JDK 17 + Mill，在本目录执行 `./mill chisel.test`。

## 1. 本实验要求（原书 8.1.1）

把 CPU 的访存接口改为**握手式类 SRAM 总线**，并运行 exp14 对应的 func（n1~n58），
通过仿真验证与上板验证：

- 请求侧：`req`、`wr`、`size[1:0]`、`wstrb[3:0]`、`addr`、`wdata`；
- 响应侧：`addr_ok`（请求已被接收）、`data_ok`（读数据有效）、`rdata`。

**Chisel 版保留的教学意图 = 从零实现（增量）**：实验环境仍不提供 `myCPU/`，
`chisel/src/main/scala/exp14/student/MyCpuTop.scala` 给出**接口骨架 + 14 处 `TODO(实现)`**，
第 ⑭ 项即本实验核心（握手总线的流水线互锁）。

## 2. 文件清单

```text
chisel/
├── README.md / MAPPING.md / build.mill     # build.mill 的 sources 引入 ../../chisel-common
├── src/main/scala/exp14/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocHsBramTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：14 处 TODO（⑭ 握手互锁）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境变化**（相比 exp7~exp13）：CPU 接口由普通 SRAM 接口改为握手式；SoC 增加
`sram_wrap`（含深度 4 读数据缓冲）与握手版 `bridge_1x2`（在途请求计数深度 15）。

## 3. 怎么做这个实验

1. 把取指/访存改为握手请求：`req && addr_ok` 表示请求被接收；读请求的 `data_ok`
   可能晚若干拍返回，需冻结相关流水级（互锁）。
2. 注意与分支冲刷、例外冲刷的优先级。
3. 运行测试：
   ```bash
   cd taskvscode/exp14/chisel
   ./mill chisel.test
   ```
4. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_hs_bram/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp13 的关系

判据与测试平台一致；差异是访存接口（普通 → 握手）与 SoC 中的 `sram_wrap`/握手 bridge。
共享模块清单见 `../exp7/chisel/MAPPING.md` §6。

## 自检（Agent 快速检查点）

- [ ] `README.md` / `MAPPING.md` / `build.mill` 齐备且非空。
- [ ] 学生模块 TODO 标记数为 **14**（类型：`TODO(实现 n/14)`），与 `MAPPING.md` 记载一致。
- [ ] 顶层端口与 `../code/` 下原 Verilog 的例化端口一一对应（`MAPPING.md` 已列表）。
- [ ] 判据复现方式已写明（见 §4），且与原文 testbench 的检查逻辑一致。
- [ ] 未实测声明已保留（本文件 §⚠️ 与 `MAPPING.md`）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp14`。
