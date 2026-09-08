---
exp: 7
title: exp7 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp7/code/
ver: agent-1.0
intent: 从零实现
---

# exp7 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp7` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | 行数 | Chisel 对应 | 备注 |
|---|---:|---|---|
| `code/myCPU/` | — | `src/main/scala/exp7/student/MyCpuTop.scala` | **原实验不提供该目录**；Chisel 版只给接口骨架 + 9 处 `TODO(实现)` |
| `code/soc_verify/soc_bram/rtl/soc_lite_top.v` | 233 | `src/main/scala/exp7/soc/SocLiteTop.scala`（薄封装）+ `../../chisel-common/.../envlib/soc/CpuIO.scala`（`CpuIO`/`LACpu`/`SocBramTop`） | SoC 顶层（bram 变体），装配在共享库 |
| `code/soc_verify/soc_bram/rtl/BRIDGE/bridge_1x2.v` | 111 | `../../chisel-common/.../envlib/soc/Bridge1x2.scala` | 共享库（含 `cpu_data_en`、读选择打一拍） |
| `code/soc_verify/soc_bram/rtl/CONFREG/confreg.v` | ~690 | `../../chisel-common/.../envlib/soc/Confreg.scala` | 共享库，`weWidth = 4` |
| `code/soc_verify/soc_bram/testbench/sync_ram.v` | 94 | `../../chisel-common/.../envlib/soc/SyncRam.scala` | 共享库：`SyncRam` + `InstRam` + `DataRam` |
| `code/soc_verify/soc_bram/testbench/mycpu_tb.v` | ~272 | `src/test/scala/MyCpuTbSpec.scala` | trace 比对 + 测试点监视 |
| `code/myCPU/regfile.v`（exp6 提供） | 27 | `../../chisel-common/.../envlib/common/Regfile.scala` | 共享库 |
| `code/soc_verify/soc_bram/rtl/xilinx_ip/*.xci` | — | 不适用 | 仿真用 `sync_ram.v` 模型；综合用 IP 不改写 |
| `code/soc_verify/soc_bram/run_vivado/*` | — | 不适用 | Vivado 工程资产 |
| `code/gettrace/src/*.v` | — | 不适用 | 参考核（FIRRTL 风格 Verilog）+ tb；仅用其产出的 `golden_trace.txt` |
| `code/func/` | — | 保留 + 测试侧读取 | `.mif` 由 `MifLoader` 读入并注入 RAM 模型 |

## 2. 顶层接口变化（exp6 → exp7）

| 信号 | exp6（soc_dram） | exp7（soc_bram） | Chisel |
|---|---|---|---|
| `inst_sram_en` | 无 | 1 位，高有效 | `io.inst_sram_en` |
| `inst_sram_we` | 1 位 | **4 位字节使能** | `io.inst_sram_we: UInt(4.W)` |
| `data_sram_en` | 无 | 1 位，高有效 | `io.data_sram_en` |
| `data_sram_we` | 1 位 | **4 位字节使能** | `io.data_sram_we: UInt(4.W)` |
| `inst_sram_addr` | 32 位，接 `[17:2]` | 32 位，接 `[19:2]`（18 位） | `io.inst_sram_addr(19, 2)` |
| `data_sram_addr` | 32 位，接 `[17:2]` | 32 位，接 `[17:2]`（16 位） | `io.data_sram_addr(17, 2)` |
| `debug_wb_*` | 4 个端口 | 同 | 同（写回级信息） |

## 3. SoC 连接对照（`soc_lite_top.v` → `SocLiteTop.scala`）

| Verilog | Chisel | 备注 |
|---|---|---|
| `always @(posedge cpu_clk) cpu_resetn <= resetn;` | `val cpu_resetn = RegNext(io.resetn, false.B)` | 1 拍延迟 |
| `inst_ram inst_ram(.ena(cpu_inst_en), .wea(cpu_inst_we), .addra(cpu_inst_addr[19:2]), …)` | `inst_ram.io.ena/wea/addra(19,2)/dina/douta` | 18 位地址 |
| `bridge_1x2(... cpu_data_en ...)` | `bridge_1x2.io.cpu_data_en := cpu.io.data_sram_en` | |
| `data_ram data_ram(.ena(data_sram_en), .wea(data_sram_we), .addra(data_sram_addr[17:2]), …)` | `data_ram.io.ena/wea/addra(17,2)/dina/douta` | 16 位地址 |
| `confreg #(.SIMULATION(SIMULATION)) u_confreg(...)` | `Module(new Confreg(simulation, weWidth = 4))` | 共享库 |
| `debug_wb_*`（内部线网） | 测试访问 `soc.cpu.io.debug_wb_*` | Chisel 无内部线网暴露 |

## 4. `SyncRam` ↔ `sync_ram.v`

| Verilog | Chisel | 差异 |
|---|---|---|
| `for i: always @(posedge clk) if (we[i] && en) ram[address][i*8+:8] <= wdata[i*8+:8]` | `when(en && we.orR){ 复制旧值后逐字节覆盖 }` | 读-改-写等价实现（动态索引子字赋值在 Chisel 中不可靠） |
| `always @(posedge clk) if (en) rdata <= ram[address]` | `when(io.en){ rdata_r := ram(io.address) }` | 同步读一致 |
| `initial $readmemb(".../inst_ram.mif", sync_ram.ram)` | 构造参数 `init`（测试侧 `MifLoader` 读入） | 不用 experimental API |
| `inst_ram` ADDR_WIDTH=18 / `data_ram` ADDR_WIDTH=16 | `InstRam(addrWidth=18)` / `DataRam(addrWidth=16)` | 深度按 init 长度取 2 的幂 |

## 5. 学生模块（从零实现）的验收线索

| 检查点 | 期望 |
|---|---|
| 顶层端口 | 与 §2 的 exp7 列一致（含 `en` 与 4 位 `we`） |
| `TODO(实现 1/9 … 9/9)` | 全部补全后才能 elaborate |
| 流水线结构 | IF/ID/EX/MEM/WB 五级 + 4 组流水寄存器 |
| 同步 RAM 时序 | 取指/访存需考虑读数据比地址晚一拍 |
| 本实验**不要求** | 数据相关/控制相关的冲突处理（exp8/exp9 才做） |
| 通过标准 | `MyCpuTbSpec` 全绿（trace 逐条一致 + `----PASS!!!`） |

## 6. 共享库说明

自 exp7 起，实验环境公共模块集中在 `taskvscode/chisel-common/`：

| 模块 | 覆盖的实验 |
|---|---|
| `envlib.soc.CpuIO` / `LACpu` | exp7~exp23（学生 CPU 的统一顶层接口） |
| `envlib.soc.SocBramTop` | exp7~exp13（`soc_bram` 变体，含 inst_ram/bridge/data_ram/confreg 装配） |
| `envlib.common.Decoders` / `Regfile` | exp7~exp23 |
| `envlib.soc.Confreg(simulation, weWidth)` | exp7~exp23 |
| `envlib.soc.Bridge1x2(weWidth)` | exp7~exp14 |
| `envlib.soc.SyncRam` / `InstRam` / `DataRam` | exp7~exp13、exp18/19、exp21~23 的仿真环境 |
| `envlib.test.MifLoader` / `TraceLoader` | exp7~exp23 的测试 |

各实验的 `build.mill` 用 `sources` 把 `../../chisel-common/src/{main,test}/scala` 加进来，
因此每个实验目录仍是"可独立构建"的（只依赖本目录 + 共享库）。

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp7`。
