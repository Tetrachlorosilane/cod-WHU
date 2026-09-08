---
exp: 14
title: exp14 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp14/code/
ver: agent-1.0
intent: 从零实现
layout: default
nav_exclude: true
---

# exp14 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp14` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/myCPU/` | `src/main/scala/exp14/student/MyCpuTop.scala` | **原实验不提供该目录**；Chisel 版只给骨架 + 14 处 `TODO(实现)` |
| `code/soc_verify/soc_hs_bram/rtl/soc_lite_top.v` | `src/main/scala/exp14/soc/SocLiteTop.scala`（薄封装）+ `../../chisel-common/.../envlib/soc/SocHsBramTop.scala` | 握手式 SoC 装配 |
| `code/soc_verify/soc_hs_bram/rtl/ram_wrap/sram_wrap.v` | `../../chisel-common/.../envlib/soc/SramWrap.scala` | 握手→简单接口 + 深度 4 读缓冲 |
| `code/soc_verify/soc_hs_bram/rtl/BRIDGE/bridge_1x2.v` | `../../chisel-common/.../envlib/soc/Bridge1x2Hs.scala` | 握手版桥（在途计数深度 15） |
| `code/soc_verify/soc_hs_bram/rtl/CONFREG/confreg.v` | `../../chisel-common/.../envlib/soc/ConfregHsWrap.scala` + `Confreg.scala` | 握手适配器复用寄存器逻辑 |
| `code/soc_verify/soc_hs_bram/testbench/sync_ram.v` | `../../chisel-common/.../envlib/soc/SyncRam.scala` | 共享库 |
| `code/soc_verify/soc_hs_bram/testbench/mycpu_tb.v` | `src/test/scala/MyCpuTbSpec.scala` → `envlib.test.TraceHarness` | 共享判据 |
| `code/func/`（n1~n58） | 保留 + 测试侧读取（`MifLoader`） | exp14 的 func 配置 |
| `code/gettrace/golden_trace.txt` | `TraceLoader` 读取 | exp14 自己的参考结果 |
| `code/soc_verify/soc_hs_bram/run_vivado/*`、`xilinx_ip/*.xci` | 不适用 | Vivado 资产 |

## 2. 接口变化（exp13 → exp14）

| 项 | exp7~exp13（普通 SRAM） | exp14（握手） |
|---|---|---|
| 请求 | `en`、`we[3:0]` | `req`、`wr`、`size[1:0]`、`wstrb[3:0]` |
| 响应 | `rdata`（读数据，同步 RAM 晚一拍） | `addr_ok`、`data_ok`、`rdata`（可多拍返回） |
| CPU 基类 | `envlib.soc.LACpu` | **`envlib.soc.LACpuHs`** |
| SoC 装配 | `SocBramTop` | **`SocHsBramTop`** |
| RAM 前级 | 直接连 RAM | **`SramWrap`**（握手转换 + 读缓冲） |
| 数据通路 | `Bridge1x2` | **`Bridge1x2Hs`**（在途请求计数） |

## 3. `sram_wrap` ↔ `SramWrap` 要点

| Verilog | Chisel | 说明 |
|---|---|---|
| `size_decode`（按 size 生成字节模板） | 同名 `Mux` 嵌套 + `VecInit(...).asUInt` | `size==0` one-hot(addr[1:0])；`size==1` 半字模板；否则 `4'hf` |
| `ram_we = {4{wr}} & wstrb & size_decode` | `Mux(io.wr, io.wstrb, 0.U) & size_decode` | 等价 |
| `buf_wptr/buf_rptr/buf_rdata[3:0]` | `Reg` 环形队列 | 深度 4 |
| `fast_return = ram_en_r && data_ok && buf_empty` | 同名 | 快速返回不占缓冲 |
| `addr_ok = 1'b1 && addr_and && !buf_full` | `!buf_full` | `_RUN_PERF_TEST` 使 `addr_and=1` |
| `data_ok = 1'b1 && data_and && (!buf_empty \|\| ram_en_r)` | `!buf_empty \|\| ram_en_r` | 同上 |
| `rdata = buf_empty ? ram_rdata : buf_rdata[buf_rptr[1:0]]` | 同名 `Mux` | |

## 4. 学生模块（从零实现 + 握手总线）的验收线索

| 检查点 | 期望 |
|---|---|
| 顶层端口 | 握手式（`req/wr/size/wstrb/addr/wdata/addr_ok/data_ok/rdata`） |
| `TODO(实现 1/14 … 14/14)` | 全部补全后才能 elaborate |
| 握手协议 | `req && addr_ok` 才算请求被接收；`data_ok` 到达前相关流水级保持 |
| ⑭ 互锁 | 取指/访存未完成时正确停顿，且不与分支/例外冲刷冲突 |
| 通过标准 | `MyCpuTbSpec` 全绿（trace 逐条一致 + `----PASS!!!`） |

## 5. 共享库

见 `../exp7/chisel/MAPPING.md` §6（本实验新增 `SocHsBramTop`、`SramWrap`、`Bridge1x2Hs`、
`ConfregHsWrap`、`CpuIOHs`/`LACpuHs`）。

## 6. 核验标注（✅/⚠️/❓）

> ✅ 核验（握手语义）：`sram_wrap` 的 `addr_ok = !buf_full`、
> `data_ok = (!buf_empty || ram_en_r)` 与原文件逐行一致；读数据缓冲是深度 4 的环形队列，
> `fast_return` 不占缓冲。
> 依据: `../code/soc_verify/soc_hs_bram/rtl/ram_wrap/sram_wrap.v`（工作区内基线）
> + `../../CHISEL-CONVENTIONS.md` §11 V1（Chisel 同步读语义）

> ⚠️ 补充：原文件顶部 `` `define _RUN_PERF_TEST `` 与 `` `ifdef RUN_PERF_TEST `` 不匹配，
> 随机延迟掩码在 exp14 也生效；本 Chisel 版按"始终 ready"实现并在此记录。

> ❓ 存疑：`bridge_1x2` 的在途计数深度 15（`do_cnt == 4'hf` 判满）是否覆盖全部 func 场景，未实测。

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp14`。
