---
exp: 21
title: exp21 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp21/code/
ver: agent-1.0
intent: 从零实现
---

# exp21 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp21` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/myCPU/` | `src/main/scala/exp21/student/MyCpuTop.scala` | **原实验不提供该目录**；骨架 + 18 处 `TODO(实现)` |
| `code/soc_verify/soc_axi/rtl/soc_lite_top.v` | `src/main/scala/exp21/soc/SocLiteTop.scala`（薄封装）+ `../../chisel-common/.../envlib/soc/SocAxiTop.scala` | 与 exp15~exp19 相同 |
| AXI wrap / crossbar / RAM / confreg | 共享库 `AxiWrapRam` / `AxiCrossbar1x2` / `AxiRamSlave` / `AxiConfregWrap` | 见 `../exp15/chisel/MAPPING.md` §5 |
| exp20 的 `cache.v`（学生实现） | 学生自行复制/实现 `student/Cache.scala` | 作为 ICache 使用 |
| exp17 的 `tlb.v`（学生实现） | 学生自行复制/实现 `student/Tlb.scala` | 同 exp18/exp19 |
| `code/soc_verify/soc_axi/testbench/mycpu_tb.v` | `src/test/scala/MyCpuTbSpec.scala` → `envlib.test.TraceHarness` | 共享判据 |
| `code/func/`（n1~n72） | 保留 + 测试侧读取（`MifLoader`） | exp21 的 func 配置 |
| `code/gettrace/golden_trace.txt` | `TraceLoader` 读取 | exp21 自己的参考结果 |

## 2. 本实验新增内容

| 项 | 说明 |
|---|---|
| ICache 集成 | 把 exp20 的 Cache 接在取指通路上（`index/tag/offset` 来自 PC；`op=0`） |
| Burst 传输 | 一次 `AR`（`arlen=3`、`arsize=2`、`arburst=INCR`）取回整行 16B，`rlast` 结束 |
| 取指停顿 | ICache miss 时冻结 PC/IF-ID，直到整行填充完成；与分支/例外冲刷的优先级需正确 |

## 3. 与 exp19 的差异

| 项 | exp19 | exp21 |
|---|---|---|
| SoC / AXI 模型 / 判据 | `soc_axi` 变体 | **完全相同** |
| func 覆盖 | n1~n72 | **n1~n72**（同范围，但需要 ICache 才能通过） |
| 取指通路 | 直接 AXI 单拍读 | **经 ICache + Burst 填充** |
| 骨架 TODO 数 | 17 | **18**（新增 ⑱） |

## 4. 学生模块的验收线索

| 检查点 | 期望 |
|---|---|
| 顶层端口 | AXI4 五通道（同 exp16~exp19） |
| `TODO(实现 1/18 … 18/18)` | 全部补全后才能 elaborate |
| ⑱ ICache | 命中时一拍返回指令；miss 时发 Burst 读并停顿取指 |
| ⑱ Burst | `arlen/arsize/arburst` 正确，按 `rlast` 结束，数据按 `offset` 组装 |
| 通过标准 | `MyCpuTbSpec` 全绿（trace 逐条一致 + `----PASS!!!`） |

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp21`。
