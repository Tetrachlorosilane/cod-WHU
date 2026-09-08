---
exp: 19
title: exp19 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp19/code/
ver: agent-1.0
intent: 从零实现
layout: default
nav_exclude: true
---

# exp19 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp19` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/myCPU/` | `src/main/scala/exp19/student/MyCpuTop.scala` | **原实验不提供该目录**；骨架 + 17 处 `TODO(实现)` |
| `code/soc_verify/soc_axi/rtl/soc_lite_top.v` | `src/main/scala/exp19/soc/SocLiteTop.scala`（薄封装）+ `../../chisel-common/.../envlib/soc/SocAxiTop.scala` | 与 exp15~exp18 相同 |
| AXI wrap / crossbar / RAM / confreg | 共享库 `AxiWrapRam` / `AxiCrossbar1x2` / `AxiRamSlave` / `AxiConfregWrap` | 见 `../exp15/chisel/MAPPING.md` §5 |
| exp17 的 `tlb.v`（学生实现） | 学生自行复制/实现 `student/Tlb.scala` | 同 exp18 |
| `code/soc_verify/soc_axi/testbench/mycpu_tb.v` | `src/test/scala/MyCpuTbSpec.scala` → `envlib.test.TraceHarness` | 共享判据 |
| `code/func/`（n1~n72） | 保留 + 测试侧读取（`MifLoader`） | exp19 的 func 配置 |
| `code/gettrace/golden_trace.txt` | `TraceLoader` 读取 | exp19 自己的参考结果 |

## 2. 本实验新增内容

| 项 | 说明 |
|---|---|
| `DMW` CSR（DMW0~3） | 直接映射窗口：`PLV0/PLV3`、`MAT`、`PSEG`、`VSEG` |
| 虚实地址映射 | 先查 DMW（按虚地址高位与当前 PLV 匹配），命中直接映射；否则查 TLB |
| `TLBR`（TLB 重填） | TLB 查找未命中时触发；入口为 `TLBRENTRY` |
| `PIL` | 取指操作页无效（TLB 命中但 `V=0`） |
| `PIS` | load/store 操作页无效 |
| `PME` | 页修改例外（写操作但 `D=0`） |
| `PPI` | 页特权等级不合规（`PLV` 不满足） |

> 各例外的 `Ecode` 与优先级以原书 7.2/9.2 节表格为准；`ERA` 记录出错指令 PC，
> `BADV` 记录出错虚地址。

## 3. 与 exp18 的差异

| 项 | exp18 | exp19 |
|---|---|---|
| SoC / AXI 模型 / 判据 | `soc_axi` 变体 | **完全相同** |
| func 覆盖 | n1~n70 | **n1~n72** |
| TLB 指令与 CSR | 已实现 | 沿用 |
| 学生任务 | TLB 指令 | + **DMW + 虚实地址映射 + 5 类 TLB 例外** |
| 骨架 TODO 数 | 16 | **17**（新增 ⑰） |

## 4. 学生模块的验收线索

| 检查点 | 期望 |
|---|---|
| 顶层端口 | AXI4 五通道（同 exp16~exp18） |
| `TODO(实现 1/17 … 17/17)` | 全部补全后才能 elaborate |
| ⑪ CSR | 新增 `DMW0~3` 读/写正确 |
| ⑰ 地址映射 | DMW 优先于 TLB；转换结果与 golden_trace 一致 |
| ⑰ TLB 例外 | TLBR/PIL/PIS/PME/PPI 的触发条件与优先级正确，`ERA`/`BADV` 正确 |
| 通过标准 | `MyCpuTbSpec` 全绿（trace 逐条一致 + `----PASS!!!`） |

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp19`。
