---
exp: 15
title: AXI 总线支持（固定延迟）
doc: chisel-env
source: taskvscode/exp15/code/（原 Verilog 实验环境，未改动）
source_url: https://bookdown.org/loongson/_book3/
ver: agent-1.0
intent: 从零实现
keywords: [AXI4, 交叉开关, axi_wrap_ram, Burst]
prereqs: [exp14]
objectives:
  - CPU 直接产生 AXI4 五通道
  - 实现读写事务状态机与 ID 管理
  - 跑通 exp15 的 golden_trace
layout: default
nav_exclude: true
---

# exp15 Chisel 版实验环境（实践任务15：添加 AXI 总线支持）

> 对应原实验：`../code/`（Verilog，源自 `output/exp15`）+ `../8.1.2实践任务15-添加AXI总线支持.md`。
> 改写规范：`../../CHISEL-CONVENTIONS.md`。对照表：`./MAPPING.md`。


## 本实验速览（TL;DR）

- **实验目标**：AXI 总线支持（固定延迟）（原书实践任务15）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。
- **教学意图**：从零实现 —— 只给接口骨架 + **15 处 `TODO(实现 n/15)`**，内部逻辑留空（`???`）。
- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。
- **共享依赖**：`../../chisel-common/`（环境库，见 `../../CHISEL-CONVENTIONS.md` §2.2）。
- **✅ 编译验证**：`chisel.compile` / `chisel.test.compile` 已实测通过（未仿真）；逐行对照见 `MAPPING.md`；运行方式见 §3。

## ✅ 编译验证（未仿真）

> 本目录的 Chisel 代码已用 **Mill 1.0.4 + JDK 17 + Chisel 3.5.6 + Scala 2.13.12** 实测通过
> `./mill chisel.compile` 与 `./mill chisel.test.compile`（**尚未跑仿真**）。
> 复查报告：仓库根 `verify/REPORT.md`。跑仿真：`./mill chisel.test`
> （exp6~exp23 需先解包运行件：`node ../../../cod-WHU.github.io/tools/unpack-assets.mjs`）。

## 1. 本实验要求（原书 8.1.2）

把 CPU 的对外接口改为 **AXI4 总线**（CPU 直接产生 ar/r/aw/w/b 五通道），
在 AXI 验证环境中完成 exp15 对应 func（n1~n58）的功能验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：实验环境仍不提供 `myCPU/`，
`chisel/src/main/scala/exp15/student/MyCpuTop.scala` 给出**接口骨架 + 15 处 `TODO(实现)`**，
⑭（AXI 事务状态机/ID 管理）与 ⑮（未完成请求导致的流水线互锁）是本实验新增的核心。

## 2. 文件清单

```text
chisel/
├── README.md / MAPPING.md / build.mill     # build.mill 的 sources 引入 ../../chisel-common
├── src/main/scala/exp15/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：15 处 TODO（⑭ AXI 状态机 + ⑮ 互锁）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**共享库新增**（本实验首次使用）：
`CpuIOAxi`/`LACpuAxi`（AXI 版 CPU 接口）、`AxiCrossbar1x2`（地址选路）、
`AxiWrapRam`（延迟掩码 + 地址重映射）、`AxiRamSlave`（AXI RAM 行为模型）、
`AxiConfregWrap`（AXI 从机前端 + 复用 `Confreg` 寄存器逻辑 + 随机掩码生成）。

## 3. 怎么做这个实验

1. 实现 AXI 读写通道：AR/R 取指、AW/W/B 访存；注意 `arlen=0` 为单拍传输、
   `wlast` 标记写数据结束、`bvalid` 为写响应。
2. 实现 ⑮ 的互锁：AR 已发但 R 未回、写未收到 B 响应时，相关流水级必须停顿。
3. 运行测试：
   ```bash
   cd taskvscode/exp15/chisel
   ./mill chisel.test
   ```
4. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_axi/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp14 的关系

判据与测试平台一致；差异是 CPU 对外接口由"握手式类 SRAM"改为 AXI4，
SoC 侧换成交叉开关 + `axi_wrap_ram` + AXI RAM 模型 + AXI confreg。
共享模块清单见 `../exp7/chisel/MAPPING.md` §6 与 `./MAPPING.md` §5。

## 自检（Agent 快速检查点）

- [ ] `README.md` / `MAPPING.md` / `build.mill` 齐备且非空。
- [ ] 学生模块 TODO 标记数为 **15**（类型：`TODO(实现 n/15)`），与 `MAPPING.md` 记载一致。
- [ ] 顶层端口与 `../code/` 下原 Verilog 的例化端口一一对应（`MAPPING.md` 已列表）。
- [ ] 判据复现方式已写明（见 §4），且与原文 testbench 的检查逻辑一致。
- [ ] 编译验证声明已保留（本文件 §✅ 与 `MAPPING.md`）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp15`。
