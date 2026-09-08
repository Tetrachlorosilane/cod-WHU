---
exp: 11
title: exp11 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp11/code/
ver: agent-1.0
intent: 从零实现
layout: default
nav_exclude: true
---

# exp11 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp11` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/myCPU/` | `src/main/scala/exp11/student/MyCpuTop.scala` | **原实验不提供该目录**；Chisel 版只给骨架 + 10 处 `TODO(实现)` |
| `code/soc_verify/soc_bram/rtl/soc_lite_top.v` | `src/main/scala/exp11/soc/SocLiteTop.scala`（薄封装）+ `../../chisel-common/.../envlib/soc/CpuIO.scala`（`SocBramTop`） | 与 exp7~exp10 相同 |
| `code/soc_verify/soc_bram/rtl/BRIDGE/bridge_1x2.v` | `../../chisel-common/.../envlib/soc/Bridge1x2.scala` | 共享库 |
| `code/soc_verify/soc_bram/rtl/CONFREG/confreg.v` | `../../chisel-common/.../envlib/soc/Confreg.scala`（`weWidth = 4`） | 共享库 |
| `code/soc_verify/soc_bram/testbench/sync_ram.v` | `../../chisel-common/.../envlib/soc/SyncRam.scala` | 共享库（字节写使能语义与访存指令直接相关） |
| `code/soc_verify/soc_bram/testbench/mycpu_tb.v` | `src/test/scala/MyCpuTbSpec.scala` → `envlib.test.TraceHarness` | 共享判据 |
| `code/func/`（n1~n46） | 保留 + 测试侧读取（`MifLoader`） | exp11 的 func 配置 |
| `code/gettrace/golden_trace.txt` | `TraceLoader` 读取 | exp11 自己的参考结果 |
| `code/soc_verify/soc_bram/run_vivado/*`、`xilinx_ip/*.xci` | 不适用 | Vivado 资产 |

## 2. 本实验新增指令与实现要点

| 指令 | 类型 | 立即数 | 实现要点 |
|---|---|---|---|
| `blt` / `bge` | 转移 | si16 | **有符号**比较后相对转移，目标 `pc + (si16<<2)` |
| `bltu` / `bgeu` | 转移 | si16 | **无符号**比较后相对转移 |
| `ld.b` | 访存 | si12 | 取字节 → **符号扩展**到 32 位 |
| `ld.h` | 访存 | si12 | 取半字 → 符号扩展 |
| `ld.bu` | 访存 | si12 | 取字节 → **零扩展** |
| `ld.hu` | 访存 | si12 | 取半字 → 零扩展 |
| `st.b` | 访存 | si12 | `data_sram_we = 4'b0001 << addr[1:0]` |
| `st.h` | 访存 | si12 | 字节使能选中 `addr[1]` 对应的两个字节 |

> 对齐：`ld.h`/`st.h` 需地址半字对齐，`ld.w`/`st.w` 需字对齐；对齐违例属于 exp13 的例外内容。

## 3. 与 exp10 的差异

| 项 | exp10 | exp11 |
|---|---|---|
| SoC / RAM / bridge / confreg | `soc_bram` 变体 | **完全相同** |
| 测试平台与判据 | trace 比对 + num_data 监视 | **完全相同** |
| func 覆盖 | n1~n36 | **n1~n46** |
| 学生任务 | 算术逻辑/乘除指令 | + **4 条转移 + 6 条访存指令** |
| 骨架 TODO 数 | 10 | 10（③⑦ 中列明新增指令） |

## 4. 学生模块的验收线索

| 检查点 | 期望 |
|---|---|
| 顶层端口 | 同 exp7~exp10 |
| `TODO(实现 1/10 … 10/10)` | 全部补全后才能 elaborate |
| 分支比较 | 有符号/无符号分支各 2 条，结果与 golden_trace 一致 |
| 访存扩展 | 符号扩展 2 条、零扩展 2 条；`st.b`/`st.h` 的字节使能正确 |
| 通过标准 | `MyCpuTbSpec` 全绿（trace 逐条一致 + `----PASS!!!`） |

## 5. 共享库

见 `../exp7/chisel/MAPPING.md` §6。

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp11`。
