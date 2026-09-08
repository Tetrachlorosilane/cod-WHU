---
exp: 16
title: exp16 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp16/code/
ver: agent-1.0
intent: 从零实现
---

# exp16 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp16` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/myCPU/` | `src/main/scala/exp16/student/MyCpuTop.scala` | **原实验不提供该目录**；骨架 + 15 处 `TODO(实现)`；CPU 直接产生 AXI4 |
| `code/soc_verify/soc_axi/rtl/soc_lite_top.v` | `src/main/scala/exp16/soc/SocLiteTop.scala`（薄封装）+ `../../chisel-common/.../envlib/soc/SocAxiTop.scala` | 与 exp15 相同 |
| `code/soc_verify/soc_axi/rtl/axi_wrap/axi_wrap.v` | **不建模（直通）** | 原文件无逻辑 |
| `code/soc_verify/soc_axi/rtl/ram_wrap/axi_wrap_ram.v` | `../../chisel-common/.../envlib/soc/AxiWrapRam.scala` | 随机掩码 + 地址重映射 |
| `code/soc_verify/soc_axi/rtl/CONFREG/confreg.v` | `../../chisel-common/.../envlib/soc/AxiConfregWrap.scala` + `Confreg.scala` | AXI 前端 + 随机掩码生成 |
| `xilinx_ip/axi_crossbar_1x2` / `axi_ram` | `AxiCrossbar1x2` / `AxiRamSlave` | 行为模型 |
| `code/soc_verify/soc_axi/testbench/mycpu_tb.v` | `src/test/scala/MyCpuTbSpec.scala` → `envlib.test.TraceHarness` | 共享判据 |
| `code/func/`（n1~n58） | 保留 + 测试侧读取（`MifLoader`） | exp16 的 func 配置（随机延迟） |
| `code/gettrace/golden_trace.txt` | `TraceLoader` 读取 | exp16 自己的参考结果 |

## 2. 与 exp15 的差异

| 项 | exp15 | exp16 |
|---|---|---|
| SoC / AXI 模型 / confreg | `soc_axi` 变体 | **完全相同** |
| 测试平台与判据 | trace 比对 + num_data 监视 | **完全相同** |
| CPU 接口 | AXI4 | **完全相同** |
| func 程序 | 固定延迟验证 | **随机延迟验证** |
| 骨架 ⑮ | 请求未完成时的流水线互锁 | **随机延迟下的等待/互锁与 ID 匹配** |
| 骨架 TODO 数 | 15 | 15 |

> 说明：两个实验的 RTL 文件逐字相同——原 `axi_wrap_ram.v` 中
> ``define _RUN_PERF_TEST``（带下划线）与 ``ifdef RUN_PERF_TEST``（不带下划线）不匹配，
> 因此随机掩码分支在两个实验中都生效；区别只在 func 程序与参考 trace。

## 3. 随机延迟的来源与 CPU 需要满足的约束

| 环节 | 随机性 | CPU 侧约束 |
|---|---|---|
| `axi_wrap_ram` 的 `ar_and/aw_and/w_and` | 随机把 `arvalid/awvalid/wvalid` 挡住 | 请求被挡时信号必须保持不变，直到握手完成 |
| `axi_wrap_ram` 的 `r_and/b_and` | 随机把 `rvalid/bvalid` 挡住 | 等待期间不得推进依赖该数据的流水级 |
| `arready/awready/wready` | 由下游从机决定 | 不得假设"一拍必被接收" |
| 多 outstanding | 允许 | 返回数据需按 `rid` 匹配 |

## 4. 学生模块的验收线索

| 检查点 | 期望 |
|---|---|
| 顶层端口 | AXI4 五通道（同 exp15） |
| `TODO(实现 1/15 … 15/15)` | 全部补全后才能 elaborate |
| ⑮ 随机延迟 | 请求保持、等待不提前推进、按 ID 匹配 |
| 通过标准 | `MyCpuTbSpec` 全绿（trace 逐条一致 + `----PASS!!!`） |

## 5. 共享库

见 `../exp15/chisel/MAPPING.md` §5（`CpuIOAxi`/`LACpuAxi`、`AxiCrossbar1x2`、
`AxiWrapRam`、`AxiRamSlave`、`AxiConfregWrap`、`SocAxiTop`）。

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp16`。
