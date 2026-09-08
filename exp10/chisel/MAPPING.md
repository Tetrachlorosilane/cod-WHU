---
exp: 10
title: exp10 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp10/code/
ver: agent-1.0
intent: 从零实现
---

# exp10 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp10` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/myCPU/` | `src/main/scala/exp10/student/MyCpuTop.scala` | **原实验不提供该目录**；Chisel 版只给骨架 + 10 处 `TODO(实现)` |
| `code/soc_verify/soc_bram/rtl/soc_lite_top.v` | `src/main/scala/exp10/soc/SocLiteTop.scala`（薄封装）+ `../../chisel-common/.../envlib/soc/CpuIO.scala`（`SocBramTop`） | 与 exp7~exp9 相同 |
| `code/soc_verify/soc_bram/rtl/BRIDGE/bridge_1x2.v` | `../../chisel-common/.../envlib/soc/Bridge1x2.scala` | 共享库 |
| `code/soc_verify/soc_bram/rtl/CONFREG/confreg.v` | `../../chisel-common/.../envlib/soc/Confreg.scala`（`weWidth = 4`） | 共享库 |
| `code/soc_verify/soc_bram/testbench/sync_ram.v` | `../../chisel-common/.../envlib/soc/SyncRam.scala` | 共享库 |
| `code/soc_verify/soc_bram/testbench/mycpu_tb.v` | `src/test/scala/MyCpuTbSpec.scala` → `envlib.test.TraceHarness` | 共享判据 |
| `code/func/`（n1~n36） | 保留 + 测试侧读取（`MifLoader`） | exp10 的 func 配置 |
| `code/gettrace/golden_trace.txt` | `TraceLoader` 读取 | exp10 自己的参考结果 |
| `code/soc_verify/soc_bram/run_vivado/*`、`xilinx_ip/*.xci` | 不适用 | Vivado 资产 |

## 2. 本实验新增指令与实现要点

| 指令 | 类型 | 立即数/操作数 | 实现要点 |
|---|---|---|---|
| `slti` / `sltui` | 算术逻辑 | si12 | 有符号/无符号比较后置 0/1 |
| `andi` / `ori` / `xori` | 算术逻辑 | **ui12（零扩展）** | 注意与 si12 的扩展方式不同 |
| `sll` / `srl` / `sra` | 算术逻辑 | rk[4:0] | 移位量为寄存器低 5 位；`sra` 需算术右移 |
| `pcaddu12i` | 算术逻辑 | si20<<12 | `rd = pc + (si20 << 12)` |
| `mul.w` | 乘除 | — | 32×32 乘积的低 32 位 |
| `mulh.w` | 乘除 | — | 有符号乘积的高 32 位 |
| `mulh.wu` | 乘除 | — | 无符号乘积的高 32 位 |
| `div.w` / `mod.w` | 乘除 | — | 有符号除/余（除零行为按原书规定） |
| `div.wu` / `mod.wu` | 乘除 | — | 无符号除/余 |

## 3. 与 exp9 的差异

| 项 | exp9 | exp10 |
|---|---|---|
| SoC / RAM / bridge / confreg | `soc_bram` 变体 | **完全相同** |
| 测试平台与判据 | trace 比对 + num_data 监视 | **完全相同** |
| func 覆盖 | n1~n20 | **n1~n36** |
| 学生任务 | 前递 | 前递 + **新增 16 条指令** |
| 骨架 TODO 数 | 10 | 10（③⑤ 中列明新增指令） |

## 4. 学生模块的验收线索

| 检查点 | 期望 |
|---|---|
| 顶层端口 | 同 exp7~exp9 |
| `TODO(实现 1/10 … 10/10)` | 全部补全后才能 elaborate |
| 立即数生成 | si12 / ui12 / si20 三种扩展方式正确区分 |
| 乘除 | 7 条乘除指令结果与 golden_trace 一致 |
| 通过标准 | `MyCpuTbSpec` 全绿（trace 逐条一致 + `----PASS!!!`） |

## 5. 共享库

见 `../exp7/chisel/MAPPING.md` §6。

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp10`。
