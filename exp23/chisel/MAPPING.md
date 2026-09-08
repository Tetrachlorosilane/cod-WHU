---
exp: 23
title: exp23 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp23/code/
ver: agent-1.0
intent: 从零实现
---

# exp23 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp23` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/myCPU/` | `src/main/scala/exp23/student/MyCpuTop.scala` | **原实验不提供该目录**；骨架 + 20 处 `TODO(实现)` |
| `code/soc_verify/soc_axi/rtl/soc_lite_top.v` | `src/main/scala/exp23/soc/SocLiteTop.scala`（薄封装）+ `../../chisel-common/.../envlib/soc/SocAxiTop.scala` | 与 exp15~exp22 相同 |
| AXI wrap / crossbar / RAM / confreg | 共享库 `AxiWrapRam` / `AxiCrossbar1x2` / `AxiRamSlave` / `AxiConfregWrap` | 见 `../exp15/chisel/MAPPING.md` §5 |
| exp20 的 `cache.v`（学生实现） | 学生自行复制/实现 `student/Cache.scala` | 作为 I/D Cache 使用 |
| exp17 的 `tlb.v`（学生实现） | 学生自行复制/实现 `student/Tlb.scala` | 同 exp18~exp22 |
| `code/soc_verify/soc_axi/testbench/mycpu_tb.v` | `src/test/scala/MyCpuTbSpec.scala` → `envlib.test.TraceHarness` | 共享判据 |
| `code/func/`（n1~n79） | 保留 + 测试侧读取（`MifLoader`） | exp23 的 func 配置 |
| `code/gettrace/golden_trace.txt` | `TraceLoader` 读取 | exp23 自己的参考结果 |

## 2. 本实验新增内容：CACOP

| 字段 | 取值 | 含义 |
|---|---|---|
| `code[4:3]` | `2'b00` | 按地址操作 ICache |
| | `2'b01` | 按地址操作 DCache |
| | `2'b10` | 按索引操作 ICache |
| | `2'b11` | 按索引操作 DCache |
| `code[2:0]` | — | 具体 Cache 操作（Store Tag / 无效化 / 写回并无效化等，以原书表格为准） |
| 地址 | `rj + si12` | 按地址操作时的虚地址（需先做 DMW/TLB 转换） |
| 索引 | 虚地址 index 字段 | 按索引操作时直接定位 Cache 行 |

## 3. 与 exp22 的差异

| 项 | exp22 | exp23 |
|---|---|---|
| SoC / AXI 模型 / 判据 | `soc_axi` 变体 | **完全相同** |
| func 覆盖 | n1~n72 | **n1~n79** |
| Cache | I/D Cache 已集成 | 沿用 + **支持 CACOP 操作** |
| 骨架 TODO 数 | 19 | **20**（新增 ⑳） |

## 4. 学生模块的验收线索

| 检查点 | 期望 |
|---|---|
| 顶层端口 | AXI4 五通道（同 exp16~exp22） |
| `TODO(实现 1/20 … 20/20)` | 全部补全后才能 elaborate |
| ⑳ 译码 | `code[4:3]` 正确选择 Cache 与"按地址/按索引" |
| ⑳ 操作 | 按 `code[2:0]` 执行对应 Cache 操作；按地址操作前完成地址转换 |
| ⑳ 流水线 | 操作与后续取指/访存一致（必要时停顿） |
| 通过标准 | `MyCpuTbSpec` 全绿（trace 逐条一致 + `----PASS!!!`） |

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp23`。
