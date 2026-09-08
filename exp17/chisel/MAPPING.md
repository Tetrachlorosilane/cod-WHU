---
exp: 17
title: exp17 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp17/code/
ver: agent-1.0
intent: 从零实现
---

# exp17 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp17` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/rtl/tlb_top.v`（1001 行） | `src/main/scala/exp17/soc/TlbTop.scala` | 模块级验证环境（写/读/查三类测试 + 数码管显示） |
| `code/testbench/testbench.v` | `src/test/scala/TlbSpec.scala` | 时钟/复位驱动 → ChiselTest；判据变为断言 |
| `code/rtl/`（学生需自行编写 `tlb.v`） | `src/main/scala/exp17/student/Tlb.scala` | **骨架 + 4 处 TODO(实现)** |
| `code/run_vivado/constraints/tlb_top.xdc`、`create_project.tcl` | 不适用 | Vivado 工程资产 |
| Xilinx `clk_pll` | 不建模 | 用隐式 clock |

## 2. `tlb` ↔ `Tlb` 端口对照

| 组 | 信号 | Chisel |
|---|---|---|
| 查找端口 0 | `s0_vppn[18:0]`、`s0_va_bit12`、`s0_asid[9:0]` | `io.s0_*`（Input） |
| | `s0_found`、`s0_index[3:0]`、`s0_ppn[19:0]`、`s0_ps[5:0]`、`s0_plv[1:0]`、`s0_mat[1:0]`、`s0_d`、`s0_v` | `io.s0_*`（Output） |
| 查找端口 1 | `s1_*` | 同上 |
| invtlb | `invtlb_valid`、`invtlb_op[4:0]` | 验证环境固定接 0 |
| 写端口 | `we`、`w_index[3:0]`、`w_e`、`w_vppn`、`w_ps`、`w_asid`、`w_g`、`w_ppn0/1`、`w_plv0/1`、`w_mat0/1`、`w_d0/1`、`w_v0/1` | `io.w_*` |
| 读端口 | `r_index[3:0]` | `io.r_index` |
| | `r_e`、`r_vppn`、`r_ps`、`r_asid`、`r_g`、`r_ppn0/1`、`r_plv0/1`、`r_mat0/1`、`r_d0/1`、`r_v0/1` | `io.r_*` |
| 时钟 | `clk` | 隐式 `clock` |

## 3. `tlb_top` ↔ `TlbTop` 要点

| Verilog | Chisel | 说明 |
|---|---|---|
| 16 项期望值表（`tlb_vppn` … `tlb_v1`，逐项 assign） | `VecInit(Seq(...))` 常量表 | 写测试的输入与读测试的期望值 |
| 26 项查找期望表（`s_test_*`） | `VecInit(Seq(...))` | `s0/s1` 两个端口的期望值 |
| `wait_1s = wait_cnt==27'd0`，重载 5 / 30_000_000 | 同名，参数 `simulation` 选择 | 控制测试推进节奏 |
| `tlb_w/r/s_test_ok` 三个通过标志 | 同名 `RegInit(false.B)` | 与原状态机一致 |
| `r_error` 逐字段比对 | 同名组合表达式 | 15 个字段 |
| `s0_error/s1_error`（含 `found` 异或与命中后逐字段比对） | `searchErr(...)` 辅助函数 | 与原表达式等价 |
| `test_error` | 同名 `RegInit` | 出错后保持 |
| 数码管扫描（`count[19:17]` 选择 `scan_data`/`num_csn`） | `VecInit(...)(count(19,17))` | 显示 `w_cnt/r_cnt/s0_id/s1_id` |
| `led = {~test_error,12'hfff,~w_ok,~r_ok,~s_ok}` | 同名 `Cat` | 上板观察用 |

> `s_test_index` 在原文件中仅用于数码管显示（比对不含 index），Chisel 版同样不参与 `s0_error/s1_error`。

## 4. 学生模块的验收线索

| 检查点 | 期望 |
|---|---|
| 接口 | 与 §2 一致（模块名 `Tlb`，参数 `tlbNum = 16`） |
| `TODO(实现 1/4 … 4/4)` | 全部补全后才能 elaborate |
| 写/读 | 16 个表项写入后逐字段读回一致 |
| 查找 | 26 组查询的 `found/ppn/ps/plv/mat/d/v` 与期望表一致（含 G 位与 `va_bit12` 选择页表项） |
| 通过标准 | `TlbSpec` 打印 `----PASS!!!` |

## 5. 核验标注（✅/⚠️/❓）

> ✅ 核验（例外命名）：TLB 重填（TLBR）、页无效（PIL/PIS）、页修改（PME）、页特权等级不合规（PPI）
> 属 LoongArch 体系结构定义，命名与生态实现一致。
> 依据: 龙架构 32 位精简版参考手册 r1p04；`../../CHISEL-CONVENTIONS.md` §11 V9

> ⚠️ 补充：`TlbTop` 增加了 4 个只读观测输出（`wOk/rOk/sOk/err`）供测试断言，不改变原行为。

> ❓ 存疑：本实验不使用 `invtlb`，验证环境把它固定接 0；`s_test_index` 在原文件中只用于数码管显示、
> 不参与比对，Chisel 版同样不参与。

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp17`。
