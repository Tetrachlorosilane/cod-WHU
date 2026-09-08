---
exp: 19
title: TLB 例外与地址映射
doc: chisel-env
source: taskvscode/exp19/code/（原 Verilog 实验环境，未改动）
source_url: https://bookdown.org/loongson/_book3/
ver: agent-1.0
intent: 从零实现
keywords: [DMW, 虚实地址映射, TLB 例外, TLBR, PPI]
prereqs: [exp18]
objectives:
  - 新增 DMW0~3 与虚实地址映射
  - 实现 TLBR/PIL/PIS/PME/PPI 五类例外
  - 跑通 exp19 的 golden_trace
layout: default
nav_exclude: true
---

# exp19 Chisel 版实验环境（实践任务19：添加 TLB 相关例外支持）

> 对应原实验：`../code/`（Verilog，源自 `output/exp19`）+ `../9.2.3实践任务19-添加TLB相关例外支持.md`。
> 改写规范：`../../CHISEL-CONVENTIONS.md`。对照表：`./MAPPING.md`。


## 本实验速览（TL;DR）

- **实验目标**：TLB 例外与地址映射（原书实践任务19）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。
- **教学意图**：从零实现 —— 只给接口骨架 + **17 处 `TODO(实现 n/17)`**，内部逻辑留空（`???`）。
- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。
- **共享依赖**：`../../chisel-common/`（环境库，见 `../../CHISEL-CONVENTIONS.md` §2.2）。
- **✅ 编译验证**：`chisel.compile` / `chisel.test.compile` 已实测通过（未仿真）；逐行对照见 `MAPPING.md`；运行方式见 §3。

## ✅ 编译验证（未仿真）

> 本目录的 Chisel 代码已用 **Mill 1.0.4 + JDK 17 + Chisel 3.5.6 + Scala 2.13.12** 实测通过
> `./mill chisel.compile` 与 `./mill chisel.test.compile`（**尚未跑仿真**）。
> 复查报告：仓库根 `verify/REPORT.md`。跑仿真：`./mill chisel.test`
> （exp6~exp23 需先解包运行件：`node ../../../cod-WHU.github.io/tools/unpack-assets.mjs`）。

## 1. 本实验要求（原书 9.2.3）

在实践任务18所实现 CPU 的基础上完成以下工作：

1. 为 CPU 增加 TLB 相关异常：TLB 重填例外、load/store/取指操作页无效例外、
   页修改例外、页特权等级不合规例外；
2. 在 CPU 中增加 `DMW` CSR 寄存器；
3. 为 CPU 增加虚实地址映射的功能；
4. 在采用 AXI 总线的 SoC 验证环境里完成 exp19 对应 func（n1~n72）的功能验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：实验环境仍不提供 `myCPU/`，
`chisel/src/main/scala/exp19/student/MyCpuTop.scala` 给出**接口骨架 + 17 处 `TODO(实现)`**，
⑰ 即本实验核心（DMW + TLB 地址转换 + TLB 例外）。

## 2. 文件清单

```text
chisel/
├── README.md / MAPPING.md / build.mill     # build.mill 的 sources 引入 ../../chisel-common
├── src/main/scala/exp19/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：17 处 TODO（⑰ 地址映射 + TLB 例外）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp15~exp18 完全相同**（`soc_axi` 变体）；区别只在 func（n1~n72）与
CPU 需要新增的地址映射/TLB 例外。

## 3. 怎么做这个实验

1. 沿用 exp18 的 TLB 与 TLB 指令实现；
2. 实现 ⑰：
   - 地址转换顺序：**DMW 直接映射优先**（按虚地址高位与 PLV 匹配）→ **TLB 查找**；
   - 例外：TLB 未命中 → TLBR；命中但 `V=0` → PIL（取指）/ PIS（load/store）；
     写操作但 `D=0` → PME；`PLV` 不满足 → PPI；
   - `ERA` 记录出错指令 PC，`BADV` 记录出错虚地址；注意流水线冲刷。
3. 运行测试：
   ```bash
   cd taskvscode/exp19/chisel
   ./mill chisel.test
   ```
4. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_axi/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp18 的关系

环境、判据、TLB 指令与 CSR 完全相同；差异是新增 DMW、虚实地址映射与 5 类 TLB 例外。
共享模块清单见 `../exp15/chisel/MAPPING.md` §5。

## 自检（Agent 快速检查点）

- [ ] `README.md` / `MAPPING.md` / `build.mill` 齐备且非空。
- [ ] 学生模块 TODO 标记数为 **17**（类型：`TODO(实现 n/17)`），与 `MAPPING.md` 记载一致。
- [ ] 顶层端口与 `../code/` 下原 Verilog 的例化端口一一对应（`MAPPING.md` 已列表）。
- [ ] 判据复现方式已写明（见 §4），且与原文 testbench 的检查逻辑一致。
- [ ] 编译验证声明已保留（本文件 §✅ 与 `MAPPING.md`）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp19`。
