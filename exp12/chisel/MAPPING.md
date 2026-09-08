---
exp: 12
title: exp12 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp12/code/
ver: agent-1.0
intent: 从零实现
---

# exp12 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp12` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/myCPU/` | `src/main/scala/exp12/student/MyCpuTop.scala` | **原实验不提供该目录**；Chisel 版只给骨架 + 12 处 `TODO(实现)` |
| `code/soc_verify/soc_bram/rtl/soc_lite_top.v` | `src/main/scala/exp12/soc/SocLiteTop.scala`（薄封装）+ `../../chisel-common/.../envlib/soc/CpuIO.scala`（`SocBramTop`） | 与 exp7~exp11 相同 |
| `code/soc_verify/soc_bram/rtl/BRIDGE/bridge_1x2.v` | `../../chisel-common/.../envlib/soc/Bridge1x2.scala` | 共享库 |
| `code/soc_verify/soc_bram/rtl/CONFREG/confreg.v` | `../../chisel-common/.../envlib/soc/Confreg.scala`（`weWidth = 4`） | 共享库 |
| `code/soc_verify/soc_bram/testbench/sync_ram.v` | `../../chisel-common/.../envlib/soc/SyncRam.scala` | 共享库 |
| `code/soc_verify/soc_bram/testbench/mycpu_tb.v` | `src/test/scala/MyCpuTbSpec.scala` → `envlib.test.TraceHarness` | 共享判据 |
| `code/func/`（n1~n47） | 保留 + 测试侧读取（`MifLoader`） | exp12 的 func 配置 |
| `code/gettrace/golden_trace.txt` | `TraceLoader` 读取 | exp12 自己的参考结果 |
| `code/soc_verify/soc_bram/run_vivado/*`、`xilinx_ip/*.xci` | 不适用 | Vivado 资产 |

## 2. 本实验新增内容与实现要点

| 项 | 说明 |
|---|---|
| `csrrd rd, csr_num` | 读 CSR 到 rd；`csr_num = inst[23:10]` |
| `csrwr rd, csr_num` | 把 rd 写入 CSR，并把旧值写回 rd |
| `csrxchg rd, rj, csr_num` | 用 rj 作掩码改写 CSR，并把旧值写回 rd |
| `ertn` | 从例外返回：`PRMD` 恢复 PLV/IE，`PC ← ERA`，冲刷流水线 |
| `syscall` | 触发系统调用例外：`Ecode = 0xB(SYS)`，`ERA ← PC`，`ESTAT/CRMD/PRMD` 更新，`PC ← EENTRY`，冲刷流水线 |
| `CRMD` | 当前模式（PLV/IE/DA/PG/DATF/DATM） |
| `PRMD` | 例外前的模式（PPLV/PIE） |
| `ESTAT` | 例外状态（Ecode/IS） |
| `ERA` | 例外返回地址 |
| `EENTRY` | 例外入口地址 |
| `SAVE0~3` | 供例外处理程序暂存的数据寄存器 |

## 3. 与 exp11 的差异

| 项 | exp11 | exp12 |
|---|---|---|
| SoC / RAM / bridge / confreg | `soc_bram` 变体 | **完全相同** |
| 测试平台与判据 | trace 比对 + num_data 监视 | **完全相同** |
| func 覆盖 | n1~n46 | **n1~n47** |
| 学生任务 | 转移/访存指令 | + **CSR 指令 + syscall 例外 + ertn** |
| 骨架 TODO 数 | 10 | **12**（新增 ⑪ CSR、⑫ 例外） |

## 4. 学生模块的验收线索

| 检查点 | 期望 |
|---|---|
| 顶层端口 | 同 exp7~exp11 |
| `TODO(实现 1/12 … 12/12)` | 全部补全后才能 elaborate |
| CSR 读写 | 三条 CSR 指令语义正确（含 `csrxchg` 掩码语义） |
| 例外流程 | `syscall` 进入 EENTRY、`ertn` 返回 ERA，流水线正确冲刷 |
| 通过标准 | `MyCpuTbSpec` 全绿（trace 逐条一致 + `----PASS!!!`） |

## 5. 共享库

见 `../exp7/chisel/MAPPING.md` §6。

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp12`。
