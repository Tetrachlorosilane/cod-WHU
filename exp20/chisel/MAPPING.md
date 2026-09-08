---
exp: 20
title: exp20 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp20/code/
ver: agent-1.0
intent: 从零实现
---

# exp20 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp20` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | Chisel 对应 | 备注 |
|---|---|---|
| `code/rtl/cache_top.v`（417 行） | `src/main/scala/exp20/soc/CacheTop.scala` | 模块级验证环境（PREPARE/WRITE/READ 三轮） |
| `code/testbench/testbench.v`（54 行） | `src/test/scala/CacheSpec.scala` | 时钟/复位驱动 → ChiselTest；判据变为断言 |
| `code/rtl/`（学生需自行编写 `cache.v`） | `src/main/scala/exp20/student/Cache.scala` | **骨架 + 5 处 TODO(实现)** |
| `code/run_vivado/constraints/cache_top.xdc`、`create_project.tcl` | 不适用 | Vivado 工程资产 |
| Xilinx `clk_pll` | 不建模 | 用隐式 clock |

## 2. `cache` ↔ `Cache` 端口对照

| 组 | 信号 | 方向（相对 cache） | Chisel |
|---|---|---|---|
| CPU 侧 | `valid`、`op`、`index[7:0]`、`tag[19:0]`、`offset[3:0]`、`wstrb[3:0]`、`wdata[31:0]` | 输入 | `io.valid/op/index/tag/offset/wstrb/wdata` |
| | `addr_ok`、`data_ok`、`rdata[31:0]` | 输出 | `io.addr_ok/data_ok/rdata` |
| 读总线 | `rd_req`、`rd_type[2:0]`、`rd_addr[31:0]` | 输出 | `io.rd_req/rd_type/rd_addr` |
| | `rd_rdy`、`ret_valid`、`ret_last`、`ret_data[31:0]` | 输入 | `io.rd_rdy/ret_valid/ret_last/ret_data` |
| 写回总线 | `wr_req`、`wr_type[2:0]`、`wr_addr[31:0]`、`wr_wstrb[3:0]`、`wr_data[127:0]` | 输出 | `io.wr_*` |
| | `wr_rdy` | 输入 | `io.wr_rdy` |
| 时钟 | `clk` | — | 隐式 `clock` |

## 3. `cache_top` ↔ `CacheTop` 要点

| Verilog | Chisel | 说明 |
|---|---|---|
| `PREPARE/WRITE/READ` 三态 | `round_state` + 同名常量 | 状态机一致 |
| `pseudo_random_23` LFSR | 同名 `Reg` | 种子：`simulation` 时为 `{7'b1010101,16'h00FF}`，否则含 `led_r_n` |
| `wait_1s`（重载 5 / 800_000） | 同名，参数 `simulation` | |
| `tag[3:0]`、`data[3:0]`（4 组 128 位行） | `Reg(Vec(4, ...))` | 验证环境的参考模型 |
| `in_index/in_tag/in_offset/memref_data/memref_wstrb` | 同名组合信号；`memref_data` 用 `Mux1H` | `wstrb`：第 4 个字用 `4'b0111` |
| `write_start/read_start/memref_valid` | 同名 | 请求握手 |
| `rd_rdy/ret_valid/ret_last/ret_data` | 同名 | 读响应：写轮次返回 `32'hffffffff`，读轮次返回期望数据 |
| `wr_rdy/data_right/replace_wrong` | 同名 | 写回比对 |
| `cacheres_right/cacheres_wrong` | 同名 | 读数据比对 |
| 数码管显示 `test_index` | `scan_sel`/`csn_sel` + `seg_rom` | 与 exp17 同款显示逻辑 |
| `assign led = 16'hffff` | `io.led := 0xffff.U` | |

## 4. 学生模块的验收线索

| 检查点 | 期望 |
|---|---|
| 接口 | 与 §2 一致（模块名 `Cache`） |
| `TODO(实现 1/5 … 5/5)` | 全部补全后才能 elaborate |
| 规格 | 2 路组相联、每路 4KB、行 16B；替换算法 LRU 或伪随机；复位后全部无效 |
| miss 填充 | 写/读 miss 时发 `rd_req`，接收 `ret_valid/ret_last/ret_data` 填行 |
| 写回 | 脏行被替换时发 `wr_req`，`wr_data` 为整行 128 位 |
| 通过标准 | `CacheSpec` 打印 `----PASS!!!`（index 到 0xff 且无 replace/cacheres 错误） |

## 5. 核验标注（✅/⚠️/❓）

> ✅ 核验（写回/写分配）：验证环境在写 miss 时让 Cache 发 `rd` 并返回全 1（`0xFFFFFFFF`），
> 写命中后以 `wr_data`（128 位整行）比对 —— 与"写分配 + 脏行写回"的 Cache 结构一致。
> 依据: `../code/rtl/cache_top.v`（工作区内基线）+ 原书 10.2.1

> ⚠️ 补充：原书允许 LRU **或** 伪随机替换，Chisel 骨架在 TODO 中同时给出两种选项；
> 验证环境本身不检查替换算法，只检查替换数据正确性。

> ❓ 存疑：`CacheTop` 的 `rd_true_value` 用 `Mux1H` 复刻原文件的期望值选择，
> 当 `rd_index_r != test_index` 时无分支命中（返回 0）——原文件同样如此，未逐条核验其边界。

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp20`。
