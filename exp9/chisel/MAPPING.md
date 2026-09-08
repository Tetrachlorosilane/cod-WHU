---
exp: 9
title: exp9 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp9/code/
ver: agent-1.0
intent: 从零实现
layout: default
nav_exclude: true
---

# exp9 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp9` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/myCPU/` | `src/main/scala/exp9/student/MyCpuTop.scala` | **原实验不提供该目录**；Chisel 版只给骨架 + 10 处 `TODO(实现)` |
| `code/soc_verify/soc_bram/rtl/soc_lite_top.v` | `src/main/scala/exp9/soc/SocLiteTop.scala`（薄封装）+ `../../chisel-common/.../envlib/soc/CpuIO.scala`（`SocBramTop`） | 与 exp7/exp8 相同 |
| `code/soc_verify/soc_bram/rtl/BRIDGE/bridge_1x2.v` | `../../chisel-common/.../envlib/soc/Bridge1x2.scala` | 共享库 |
| `code/soc_verify/soc_bram/rtl/CONFREG/confreg.v` | `../../chisel-common/.../envlib/soc/Confreg.scala`（`weWidth = 4`） | 共享库 |
| `code/soc_verify/soc_bram/testbench/sync_ram.v` | `../../chisel-common/.../envlib/soc/SyncRam.scala` | 共享库 |
| `code/soc_verify/soc_bram/testbench/mycpu_tb.v` | `src/test/scala/MyCpuTbSpec.scala` → `envlib.test.TraceHarness` | 共享判据 |
| `code/func/` | 保留 + 测试侧读取（`MifLoader`） | exp9 的 func 检验前递 |
| `code/gettrace/golden_trace.txt` | `TraceLoader` 读取 | exp9 自己的参考结果 |
| `code/soc_verify/soc_bram/run_vivado/*`、`xilinx_ip/*.xci` | 不适用 | Vivado 资产 |

## 2. 与 exp8 的差异

| 项 | exp8 | exp9 |
|---|---|---|
| SoC / RAM / bridge / confreg | `soc_bram` 变体 | **完全相同** |
| 测试平台与判据 | trace 比对 + num_data 监视 | **完全相同** |
| 数据相关处理 | 阻塞（stall） | **前递（forward）+ load-use 阻塞** |
| 骨架 ⑩ 的内容 | RAW 检测与阻塞 | 前递通路 + load-use 阻塞 |
| 骨架 TODO 数 | 10 | 10 |

## 3. 学生模块（从零实现 + 前递）的验收线索

| 检查点 | 期望 |
|---|---|
| 顶层端口 | 同 exp7/exp8（`inst_sram_en` / `data_sram_en` / 4 位字节使能） |
| `TODO(实现 1/10 … 10/10)` | 全部补全后才能 elaborate |
| 流水线结构 | IF/ID/EX/MEM/WB + 4 组流水寄存器（EX/MEM、MEM/WB 需保留 rd/写使能/结果） |
| ⑩ 前递 | EX 级操作数优先旁路 EX/MEM 结果，其次 MEM/WB 结果；`rd == 0` 不前递 |
| ⑩ load-use | load 后紧跟使用其结果时仍需 1 拍阻塞 |
| 通过标准 | `MyCpuTbSpec` 全绿（trace 逐条一致 + `----PASS!!!`） |

## 4. 共享库

见 `../exp7/chisel/MAPPING.md` §6（`CpuIO`/`LACpu`/`SocBramTop`、`Confreg`、`Bridge1x2`、
`SyncRam`、`Decoders`、`Regfile`、`TraceHarness`/`MifLoader`/`TraceLoader`）。

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp9`。
