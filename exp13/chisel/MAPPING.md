---
exp: 13
title: exp13 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp13/code/
ver: agent-1.0
intent: 从零实现
---

# exp13 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp13` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/myCPU/` | `src/main/scala/exp13/student/MyCpuTop.scala` | **原实验不提供该目录**；Chisel 版只给骨架 + 14 处 `TODO(实现)` |
| `code/soc_verify/soc_bram/rtl/soc_lite_top.v` | `src/main/scala/exp13/soc/SocLiteTop.scala`（薄封装）+ `../../chisel-common/.../envlib/soc/CpuIO.scala`（`SocBramTop`） | 与 exp7~exp12 相同 |
| `code/soc_verify/soc_bram/rtl/BRIDGE/bridge_1x2.v` | `../../chisel-common/.../envlib/soc/Bridge1x2.scala` | 共享库 |
| `code/soc_verify/soc_bram/rtl/CONFREG/confreg.v` | `../../chisel-common/.../envlib/soc/Confreg.scala`（`weWidth = 4`） | 共享库（含 timer/timer_clk 相关逻辑） |
| `code/soc_verify/soc_bram/testbench/sync_ram.v` | `../../chisel-common/.../envlib/soc/SyncRam.scala` | 共享库 |
| `code/soc_verify/soc_bram/testbench/mycpu_tb.v` | `src/test/scala/MyCpuTbSpec.scala` → `envlib.test.TraceHarness` | 共享判据 |
| `code/func/`（n1~n58） | 保留 + 测试侧读取（`MifLoader`） | exp13 的 func 配置 |
| `code/gettrace/golden_trace.txt` | `TraceLoader` 读取 | exp13 自己的参考结果 |
| `code/soc_verify/soc_bram/run_vivado/*`、`xilinx_ip/*.xci` | 不适用 | Vivado 资产 |

## 2. 本实验新增内容与实现要点

| 项 | 说明 |
|---|---|
| `ADEF` | 取指地址错：PC 非 4 字节对齐或越界；`BADV` 记录出错地址 |
| `ALE` | 地址非对齐：访存地址与访问宽度不匹配（`ld.h`/`st.h` 需半字对齐，`ld.w`/`st.w` 需字对齐） |
| `BRK` | 断点指令触发 |
| `INE` | 指令不存在（译码无法识别） |
| 软件中断 | 2 个（`ESTAT.IS[1:0]`，经 CSR 写触发） |
| 硬件中断 | 8 个（`ESTAT.IS[9:2]`） |
| 定时器中断 | `TCFG/TVAL/TID` 定时器到期触发 TI |
| `ECFG` | 中断使能配置（`LIE` 按位） |
| `BADV` | 触发 ADEF/ALE 的出错地址 |
| `TID` | 定时器 ID |
| `TCFG` | 定时器配置（使能/周期模式） |
| `TVAL` | 定时器当前值（递减到 0 触发） |
| `TICLR` | 定时器中断清除（写 1 清） |
| `rdcntvl.w` / `rdcntvh.w` | 读稳定计数器低/高 32 位 |
| `rdcntid` | 读 CPU ID 与计数器高位 |

## 3. 与 exp12 的差异

| 项 | exp12 | exp13 |
|---|---|---|
| SoC / RAM / bridge / confreg | `soc_bram` 变体 | **完全相同** |
| 测试平台与判据 | trace 比对 + num_data 监视 | **完全相同** |
| func 覆盖 | n1~n47 | **n1~n58** |
| 学生任务 | CSR + syscall | + **ADEF/ALE/BRK/INE + 中断/定时器 + rdcnt*** |
| 骨架 TODO 数 | 12 | **14**（新增 ⑬ ⑭） |

## 4. 学生模块的验收线索

| 检查点 | 期望 |
|---|---|
| 顶层端口 | 同 exp7~exp12 |
| `TODO(实现 1/14 … 14/14)` | 全部补全后才能 elaborate |
| 例外优先级 | 同一指令同时满足多个例外条件时，按原书规定的优先级取一个 |
| 中断 | `ECFG.LIE` 与 `ESTAT.IS` 对应位同时有效才响应；响应后 `IS` 清除时机正确 |
| 定时器 | `TVAL` 递减到 0 触发 TI；`TICLR` 写 1 清除 |
| 通过标准 | `MyCpuTbSpec` 全绿（trace 逐条一致 + `----PASS!!!`） |

## 5. 共享库

见 `../exp7/chisel/MAPPING.md` §6。

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp13`。
