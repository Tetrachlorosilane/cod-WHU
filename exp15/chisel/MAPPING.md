---
exp: 15
title: exp15 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp15/code/
ver: agent-1.0
intent: 从零实现
layout: default
nav_exclude: true
---

# exp15 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp15` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/myCPU/` | `src/main/scala/exp15/student/MyCpuTop.scala` | **原实验不提供该目录**；骨架 + 15 处 `TODO(实现)`；CPU 直接产生 AXI4 |
| `code/soc_verify/soc_axi/rtl/soc_lite_top.v` | `src/main/scala/exp15/soc/SocLiteTop.scala`（薄封装）+ `../../chisel-common/.../envlib/soc/SocAxiTop.scala` | AXI SoC 装配 |
| `code/soc_verify/soc_axi/rtl/axi_wrap/axi_wrap.v` | **不建模（直通）** | 原文件 m_* 与 s_* 逐位相连，无逻辑 |
| `code/soc_verify/soc_axi/rtl/ram_wrap/axi_wrap_ram.v` | `../../chisel-common/.../envlib/soc/AxiWrapRam.scala` | 掩码 + 地址重映射 |
| `code/soc_verify/soc_axi/rtl/CONFREG/confreg.v`（838 行） | `../../chisel-common/.../envlib/soc/AxiConfregWrap.scala` + `Confreg.scala` | AXI 前端 + 随机掩码；寄存器逻辑复用共享 `Confreg` |
| `xilinx_ip/axi_crossbar_1x2` | `../../chisel-common/.../envlib/soc/AxiCrossbar1x2.scala` | 行为模型（单 outstanding） |
| `xilinx_ip/axi_ram` | `../../chisel-common/.../envlib/soc/AxiRamSlave.scala` | AXI 从机 + 同步 RAM |
| `code/soc_verify/soc_axi/testbench/mycpu_tb.v` | `src/test/scala/MyCpuTbSpec.scala` → `envlib.test.TraceHarness` | 共享判据 |
| `code/func/`（n1~n58） | 保留 + 测试侧读取（`MifLoader`） | exp15 的 func 配置 |
| `code/gettrace/golden_trace.txt` | `TraceLoader` 读取 | exp15 自己的参考结果 |
| `code/soc_verify/soc_axi/run_vivado/*` | 不适用 | Vivado 工程资产 |

## 2. CPU 接口（AXI4）

| 通道 | 信号 | 方向 |
|---|---|---|
| AR | `arid[3:0]`、`araddr[31:0]`、`arlen[7:0]`、`arsize[2:0]`、`arburst[1:0]`、`arlock[1:0]`、`arcache[3:0]`、`arprot[2:0]`、`arvalid` / `arready` | 出 / 入 |
| R | `rid[3:0]`、`rdata[31:0]`、`rresp[1:0]`、`rlast`、`rvalid` / `rready` | 入 / 出 |
| AW | `awid[3:0]`、`awaddr[31:0]`、`awlen[7:0]`、`awsize[2:0]`、`awburst[1:0]`、`awlock[1:0]`、`awcache[3:0]`、`awprot[2:0]`、`awvalid` / `awready` | 出 / 入 |
| W | `wid[3:0]`、`wdata[31:0]`、`wstrb[3:0]`、`wlast`、`wvalid` / `wready` | 出 / 入 |
| B | `bid[3:0]`、`bresp[1:0]`、`bvalid` / `bready` | 入 / 出 |

> 与 exp14 的握手式类 SRAM 接口相比：读写通道分离、请求与响应解耦、带 ID 与突发长度。

## 3. `axi_wrap_ram` ↔ `AxiWrapRam` 要点

| Verilog | Chisel | 说明 |
|---|---|---|
| ``define _RUN_PERF_TEST`` + ``ifdef RUN_PERF_TEST`` | `perfTest: Boolean = false` | 注意：**检查的宏未定义**，实际走 else 分支（启用随机掩码） |
| `ar_and = ram_random_mask[4] \| ar_nomask` 等 | 同名 | 掩码挡住请求后由 nomask 标记在下一拍放行 |
| `pf_r2r/pf_b2b` 计数器 | 同名 `Reg` | perfTest=true 时作为固定延迟 |
| `ram_araddr = (addr[31:28]∈{0,1,7}) ? addr : {12'b0,4'hf,addr[31:28],addr[11:0]}` | 同名 `Mux` | 地址重映射 |
| `axi_r* = rvalid ? ram_r* : 0` | 同名 `Mux` | 响应掩码 |

## 4. `confreg`(AXI) ↔ `AxiConfregWrap` 要点

| Verilog | Chisel | 说明 |
|---|---|---|
| `arready = ~busy & (!R_or_W \| !awvalid)`；`awready = ~busy & (R_or_W \| !arvalid)` | 同名 | 单事务串行化 |
| `buf_id/buf_addr/buf_len/buf_size` | 同名 `Reg` | 地址/ID 锁存 |
| `wready` 在 `aw_enter` 置位、`w_enter&wlast` 清零 | 同名 `Reg` | |
| `rvalid/rlast/rdata` 在 `busy & R_or_W & !r_retire` 时置位并译码 | 同名；`rdata := core.io.conf_rdata` | 读数据打一拍 |
| `bvalid` 在 `w_enter` 置位、`b_retire` 清零 | 同名 `Reg` | |
| `conf_we = w_enter`（整字写） | `core.io.conf_en := w_enter`、`conf_we := 0xf` | 复用 `Confreg` |
| `pseudo_random_23`/`no_mask`/`short_delay`/`ram_random_mask` | 同名，逐位照抄 | 随机延迟掩码 |
| `led_data` 复位值 `{16'h0, switch_led}` | 复位值 0（复用 `Confreg`） | 仅影响复位初始灯态，不影响读写语义 |

## 5. 共享库新增

| 模块 | 说明 |
|---|---|
| `envlib.soc.CpuIOAxi` / `LACpuAxi` | AXI 版 CPU 接口与基类 |
| `envlib.soc.AxiCrossbar1x2` | 按地址选路（`sel_conf` 与 bridge 一致） |
| `envlib.soc.AxiWrapRam` | 延迟掩码 + 地址重映射 |
| `envlib.soc.AxiRamSlave` | AXI4 从机 + `SyncRam`（支持 INCR、单 outstanding） |
| `envlib.soc.AxiConfregWrap` | AXI 从机前端 + `Confreg` + 随机掩码 |
| `envlib.soc.SocAxiTop` | AXI SoC 装配 |

## 6. 学生模块的验收线索

| 检查点 | 期望 |
|---|---|
| 顶层端口 | AXI4 五通道（见 §2） |
| `TODO(实现 1/15 … 15/15)` | 全部补全后才能 elaborate |
| ⑭ AXI 状态机 | 读写通道独立推进；返回数据按 ID 匹配 |
| ⑮ 互锁 | AR/R、AW/W/B 未完成时相关流水级停顿，不与冲刷冲突 |
| 通过标准 | `MyCpuTbSpec` 全绿（trace 逐条一致 + `----PASS!!!`） |

## 7. 核验标注（✅/⚠️/❓）

> ✅ 核验（AXI4 突发）：`AxLEN` 表示 `ARLEN`/`AWLEN`，编码为"拍数−1"，故 `arlen=3` 对应 4 拍、
> 即 16B 一行；`RLAST`/`WLAST` 标记突发末拍。
> 依据: https://developer.arm.com/documentation/ihi0022/ ；`../../CHISEL-CONVENTIONS.md` §11 V6

> ⚠️ 修订：`axi_wrap.v` 是纯直通（m_* 与 s_* 逐位相连），故未建模为模块，在 §1 标注"不建模（直通）"。

> ⚠️ 补充：`axi_wrap_ram.v` 中 `` `define _RUN_PERF_TEST ``（带下划线）与
> `` `ifdef RUN_PERF_TEST ``（不带）不匹配 ⇒ 随机掩码分支实际生效；参数 `perfTest` 默认 `false`
> 即对应该真实行为（exp15 与 exp16 的 RTL 因此逐字相同）。

> ❓ 存疑：Xilinx `axi_crossbar_1x2` / `axi_ram` 的行为模型只覆盖单 outstanding、INCR 突发，
> 未建模乱序返回与 ID 重排（见 `AxiCrossbar1x2.scala` 头注释）。

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp15`。
