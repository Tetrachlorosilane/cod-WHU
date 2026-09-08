---
exp: 22
title: exp22 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp22/code/
ver: agent-1.0
intent: 从零实现
---

# exp22 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp22` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/myCPU/` | `src/main/scala/exp22/student/MyCpuTop.scala` | **原实验不提供该目录**；骨架 + 19 处 `TODO(实现)` |
| `code/soc_verify/soc_axi/rtl/soc_lite_top.v` | `src/main/scala/exp22/soc/SocLiteTop.scala`（薄封装）+ `../../chisel-common/.../envlib/soc/SocAxiTop.scala` | 与 exp15~exp21 相同 |
| AXI wrap / crossbar / RAM / confreg | 共享库 `AxiWrapRam` / `AxiCrossbar1x2` / `AxiRamSlave` / `AxiConfregWrap` | 见 `../exp15/chisel/MAPPING.md` §5 |
| exp20 的 `cache.v`（学生实现） | 学生自行复制/实现 `student/Cache.scala` | 作为 DCache 使用（ICache 已在 exp21） |
| exp17 的 `tlb.v`（学生实现） | 学生自行复制/实现 `student/Tlb.scala` | 同 exp18~exp21 |
| `code/soc_verify/soc_axi/testbench/mycpu_tb.v` | `src/test/scala/MyCpuTbSpec.scala` → `envlib.test.TraceHarness` | 共享判据 |
| `code/func/`（n1~n72） | 保留 + 测试侧读取（`MifLoader`） | exp22 的 func 配置 |
| `code/gettrace/golden_trace.txt` | `TraceLoader` 读取 | exp22 自己的参考结果 |

## 2. 本实验新增内容

| 项 | 说明 |
|---|---|
| DCache 集成 | 把 exp20 的 Cache 接在访存通路（`op=1` 写 / `op=0` 读） |
| 写命中 | 按 `wstrb` 更新行内数据并置脏 |
| 写 miss | 先取行（写分配）再写 |
| 脏行写回 | 替换时发 `wr_req`，`wr_data` 为整行 128 位 |
| 访存停顿 | DCache 未完成时冻结 MEM/WB，与前递/例外/分支冲刷协调 |

## 3. 与 exp21 的差异

| 项 | exp21 | exp22 |
|---|---|---|
| SoC / AXI 模型 / 判据 | `soc_axi` 变体 | **完全相同** |
| func 覆盖 | n1~n72 | **n1~n72** |
| Cache 使用 | 仅 ICache（取指通路） | **+ DCache（访存通路）** |
| 骨架 TODO 数 | 18 | **19**（新增 ⑲） |

## 4. 学生模块的验收线索

| 检查点 | 期望 |
|---|---|
| 顶层端口 | AXI4 五通道（同 exp16~exp21） |
| `TODO(实现 1/19 … 19/19)` | 全部补全后才能 elaborate |
| ⑲ 写通路 | 写命中/写分配正确，`wstrb` 字节写正确 |
| ⑲ 写回 | 脏行被替换时整行写回，数据与 golden_trace 一致 |
| 通过标准 | `MyCpuTbSpec` 全绿（trace 逐条一致 + `----PASS!!!`） |

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp22`。
