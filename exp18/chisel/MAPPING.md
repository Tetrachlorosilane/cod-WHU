---
exp: 18
title: exp18 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp18/code/
ver: agent-1.0
intent: 从零实现
layout: default
nav_exclude: true
---

# exp18 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp18` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/myCPU/` | `src/main/scala/exp18/student/MyCpuTop.scala` | **原实验不提供该目录**；骨架 + 16 处 `TODO(实现)` |
| `code/soc_verify/soc_axi/rtl/soc_lite_top.v` | `src/main/scala/exp18/soc/SocLiteTop.scala`（薄封装）+ `../../chisel-common/.../envlib/soc/SocAxiTop.scala` | 与 exp15/exp16 相同 |
| `axi_wrap` / `axi_wrap_ram` / `axi_crossbar_1x2` / `axi_ram` / AXI `confreg` | 共享库 `AxiWrapRam` / `AxiCrossbar1x2` / `AxiRamSlave` / `AxiConfregWrap` | 见 `../exp15/chisel/MAPPING.md` §5 |
| exp17 的 `tlb.v`（学生实现） | 学生自行复制/实现 `student/Tlb.scala` | 见 `./README.md` §3 |
| `code/soc_verify/soc_axi/testbench/mycpu_tb.v` | `src/test/scala/MyCpuTbSpec.scala` → `envlib.test.TraceHarness` | 共享判据 |
| `code/func/`（n1~n70） | 保留 + 测试侧读取（`MifLoader`） | exp18 的 func 配置 |
| `code/gettrace/golden_trace.txt` | `TraceLoader` 读取 | exp18 自己的参考结果 |

## 2. 本实验新增指令与 CSR

### 指令

| 指令 | 语义 |
|---|---|
| `TLBSRCH` | 用 `TLBEHI.VPPN` + `ASID.ASID` 查 TLB；命中则把表项号写入 `TLBIDX.Index` |
| `TLBRD` | 按 `TLBIDX.Index` 读出表项到 `TLBEHI`/`TLBELO0`/`TLBELO1`/`ASID`（`TLBIDX.NE` 表示表项无效） |
| `TLBWR` | 按 `TLBIDX.Index` 写入表项（数据来自 `TLBEHI`/`TLBELO0`/`TLBELO1`/`ASID`） |
| `TLBFILL` | 写入随机/替换位置的表项 |
| `INVTLB` | 按 `invtlb_op` 作废相应表项 |

### CSR

| CSR | 关键字段 |
|---|---|
| `TLBIDX` | `Index`（表项号）、`NE`（表项无效）、`PS`（页大小） |
| `TLBEHI` | `VPPN`（虚页号）、`V4` |
| `TLBELO0` / `TLBELO1` | `PPN`、`G`、`MAT`、`PLV`、`D`、`V` |
| `ASID` | `ASID`（地址空间标识） |
| `TLBRENTRY` | `PS`、`PPN`、`VA`（TLB 重填例外入口） |

> 字段位宽与 exp17 的 `Tlb` 接口一一对应（`vppn[18:0]`、`asid[9:0]`、`ppn[19:0]`、
> `ps[5:0]`、`plv[1:0]`、`mat[1:0]`、`d`、`v`、`g`）。

## 3. 与 exp16/exp17 的差异

| 项 | exp16 | exp18 |
|---|---|---|
| SoC / AXI 模型 | `soc_axi` 变体 | **完全相同** |
| func 覆盖 | n1~n58 | **n1~n70** |
| TLB | 无 | **集成 exp17 的 TLB** |
| 学生任务 | AXI 随机延迟 | + **5 条 TLB 指令 + 6 个 TLB 相关 CSR** |
| 骨架 TODO 数 | 15 | **16**（新增 ⑯） |

## 4. 学生模块的验收线索

| 检查点 | 期望 |
|---|---|
| 顶层端口 | AXI4 五通道（同 exp16） |
| `TODO(实现 1/16 … 16/16)` | 全部补全后才能 elaborate |
| ⑪ CSR | 新增 6 个 CSR 的读/写/按掩码改写正确 |
| ⑯ TLB 指令 | 5 条指令的数据通路与 TLB 端口一致，结果与 golden_trace 一致 |
| 通过标准 | `MyCpuTbSpec` 全绿（trace 逐条一致 + `----PASS!!!`） |

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp18`。
