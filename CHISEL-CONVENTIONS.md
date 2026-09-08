---
doc: conventions
title: Chisel 改写规范（Verilog → Chisel 对照改写约定）
scope: taskvscode/exp5 ~ exp23
source: taskvscode/*/code/（原 Verilog 实验环境，未改动）+ output/
source_url: https://bookdown.org/loongson/_book3/
ver: agent-1.0
keywords: [Chisel, Verilog, 教学意图保留, TODO 标记, MAPPING, 静态验收]
prereqs: []
objectives:
  - 知道每个实验该保留什么（填空/找错/骨架）与不该改什么（code/、保真目录）
  - 能按统一命名与编码规则读懂并续写任意一个 expN/chisel/
  - 能用一条命令复现静态验收结论
---

# Chisel 改写规范（Verilog → Chisel 对照改写约定）

> 适用范围：`taskvscode/exp5 ~ exp23`。
> 本文是**所有实验共用的改写契约**：命名、编码、教学意图保留、文档格式、静态验收。
> 每个实验的 `chisel/MAPPING.md` 负责记录该实验的具体对照表；本文只定规则。

## 本文速览（TL;DR）

- **一句话目标**：实验环境**全量**改 Chisel，"学生要做的那部分"**意图原样保留**（填空留空 / 找错留错 / 从零实现留骨架）。
- **交付形态**：纯静态——不装 JDK/Mill、不编译不仿真；每份 `chisel/README.md` 带未实测声明，
  `MAPPING.md` 提供逐行对照供人工复核。
- **布局**：exp5/exp6 自包含；exp7~exp23 = 每实验自有学生代码/薄 SoC 封装/测试/文档 + 共享环境库 `chisel-common/`。
- **验收**：`node tools/chisel_static_check.mjs`（19 实验、20 项检查、0 失败）。
- **Agent 提示**：改任一实验前先读该实验的 `README.md` 速览 + `MAPPING.md` 自检清单；
  补代码时只填 `TODO` 处，不要动 `code/` 与保真目录。

---

## 0. 一句话目标

把每个实验的 Verilog 实验环境**全量**改写为 Chisel，同时**原样保留该实验"要学生做的那部分"的教学意图**
（填空保持空缺、找错保留同类错误、从零实现保留骨架），并就地改写该实验文件夹内的 markdown 说明。

## 1. 交付方式（重要）

- **纯静态交付**：不安装 JDK / Mill，不编译、不仿真。Chisel 代码按"可编译子集"手写，
  每个实验的 `chisel/README.md` 必须带**未实测声明**（见 §7）。
- 不改动 `output/`、`CPU设计实战：LoongArch版/`、`raw_book3/`、`verilog4agent/`、`chisel4agent/`
  （保真溯源基线）。
- `taskvscode/expN/code/`（原 Verilog 副本）**保持不动**，作为对照基线；Chisel 版写在同级的 `chisel/`。

## 2. 目录布局

### 2.1 自包含模板（exp5 / exp6，已交付）

```text
taskvscode/expN/
├── <原任务说明>.md          # 就地改写为 Chisel 版实验说明
├── code/                    # 原 Verilog 副本（不动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill
    ├── src/main/scala/{common,soc,student}/   # 环境 + 公共模块 + 学生部分（自包含）
    ├── src/test/scala/                        # ChiselTest：复现原实验判据
    └── solution/                              # 参考解（不参与默认编译）
```

### 2.2 共享环境库（exp7 ~ exp23）

exp7 起实验环境在实验之间几乎逐字相同（`confreg` 约 690 行、`bridge_1x2`、`sync_ram` 等），
逐实验复制会产生十几份等价代码，故把环境公共模块集中到：

```text
taskvscode/chisel-common/
├── src/main/scala/envlib/common/{Decoders,Regfile}.scala
├── src/main/scala/envlib/soc/{Confreg,Bridge1x2,SyncRam}.scala
└── src/test/scala/envlib/test/Loaders.scala      # MifLoader / TraceLoader

taskvscode/expN/                                   # N = 7 … 23
├── <原任务说明>.md          # 就地改写为 Chisel 版实验说明
├── code/                    # 原 Verilog 副本（不动）
└── chisel/
    ├── README.md / MAPPING.md
    ├── build.mill           # 用 sources 引入 ../../chisel-common/src/{main,test}/scala
    ├── src/main/scala/expN/
    │   ├── soc/             # 本实验特有的 SoC 变体（dram/bram/hs_bram/axi/module_verify）
    │   └── student/         # 学生部分（留空骨架 / 模块骨架）
    └── src/test/scala/      # ChiselTest：复现原实验判据
```

每个实验目录仍可**独立构建**（只依赖本目录 + `chisel-common`）。
`confreg` 的写使能位宽用参数区分：`Confreg(simulation, weWidth = 1)`（exp6 语义）/
`weWidth = 4`（exp7 起）。

## 3. 目标版本与 API 子集

| 项 | 取值 |
|---|---|
| 语言 | Scala 2.13.x |
| 硬件库 | Chisel 3.5.x（`import chisel3._`） |
| 测试 | ChiselTest 0.6.x（`import chiseltest._`），treadle 后端（纯 JVM，无需 Verilator） |
| 构建 | Mill 0.11.x（`build.mill`） |

**只允许使用下列稳定 API**（避免版本漂移导致编译失败）：
`Module` / `IO` / `Bundle` / `UInt` / `SInt` / `Bool` / `Vec` / `Wire` / `Reg` / `RegInit` / `RegNext` /
`Mem` / `SyncReadMem` / `Mux` / `MuxCase` / `MuxLookup` / `Cat` / `VecInit` / `when` / `otherwise` /
`switch` / `is` / `printf` / `assert` / `Decoupled`。

**禁止**：`chisel3.util.experimental.*`、`ChiselEnum`、`circt` 相关 API、`import chisel3.iotesters._`、
以及任何依赖具体 Chisel 6/7 语义的写法。若确需使用，必须在 `MAPPING.md` 中显式说明替代方案。

## 4. 命名映射规则

### 4.1 模块名

| Verilog | Chisel 类名 | 说明 |
|---|---|---|
| `minicpu_top` | `MinicpuTop` | 学生模块（exp5） |
| `mycpu_top` | `MyCpuTop` | 学生模块（exp6~exp23） |
| `tlb` | `Tlb` | 学生模块（exp17~exp19） |
| `cache` | `Cache` | 学生模块（exp20~exp23） |
| `soc_mini_top` | `SocMiniTop` | 环境 |
| `soc_lite_top` | `SocLiteTop` | 环境 |
| `confreg` | `Confreg` | 环境 |
| `bridge_1x2` | `Bridge1x2` | 环境 |
| `regfile` | `Regfile` | 共用 |
| `decoder_2_4` / `decoder_4_16` / `decoder_5_32` / `decoder_6_64` | `Decoder2_4` / `Decoder4_16` / `Decoder5_32` / `Decoder6_64` | 共用 |
| `inst_ram` / `data_ram` | `InstRam` / `DataRam` | Xilinx IP → Chisel 行为模型 |
| `sync_ram` / `async_ram` | `SyncRam` / `AsyncRam` | 环境（仿真用 RAM 模型） |
| `axi_wrap` / `axi_wrap_ram` / `sram_wrap` | `AxiWrap` / `AxiWrapRam` / `SramWrap` | 环境 |
| `tlb_top` / `cache_top` | `TlbTop` / `CacheTop` | 模块级验证顶层 |
| `tb_top` / `mycpu_tb` / `testbench` | `MyCpuTb` / `TlbTestbench` / `CacheTestbench` | 测试平台 |

### 4.2 信号名

**端口名与内部信号名逐字保留原 Verilog 的 snake_case**（`inst_sram_we`、`data_sram_addr`、
`br_taken`、`alu_src2` …），以便与 `code/` 逐行对照。Chisel 允许下划线标识符。

### 4.3 时钟与复位

- Verilog 的 `clk` 端口 → Chisel **隐式 `clock`**（不再作为 IO）；在 `MAPPING.md` 中注明。
- `resetn`（低有效）**保留为显式 IO**，并在模块内按原 Verilog 手写同步复位：
  ```scala
  val led_data = Reg(UInt(32.W))
  when(!io.resetn) { led_data := 0.U }
    .elsewhen(io.conf_we) { led_data := io.conf_wdata }
  ```
  **不使用** `RegInit` 的隐式复位，以保证与 Verilog 复位语义逐条对应。

## 5. 编码规则

| Verilog 写法 | Chisel 写法 |
|---|---|
| `assign x = e;` | `val x = e` 或 `val x = Wire(...); x := e` |
| `always @(*)` + `case` | `Wire` + 默认赋值 + `when/elsewhen/otherwise`（防 latch） |
| `always @(posedge clk)` | `Reg(...)` / `RegInit(...)` + `when` |
| `generate for` 展开 | `VecInit((0 until n).map(...))` |
| `{a, b}` 拼接 | `Cat(a, b)` |
| `{{n{x}}, y}` 符号扩展 | `Cat(Fill(n, x), y)` 或 `x.asSInt.pad(n + y.getWidth)` |
| `x[i]` / `x[i:j]` | `x(i)` / `x(i, j)` |
| `x == const` | `x === const.U` |
| `Mux` / 三目 | `Mux(cond, a, b)` |
| `inst_ram` 等 IP | `RegInit(VecInit(常量表))`（异步读 ROM）或 `SyncReadMem`（同步读） |

- 组合默认值必须给全，避免 Chisel 的 "not fully initialized" 报错。
- 宽度：所有端口与寄存器显式给位宽（`.W`）；位宽变换用 `.apply(hi, lo)`、`.pad()`、`.asUInt`。
- 常量：`0.U` / `0x1c000000L.U(32.W)` / `true.B`。

## 6. 教学意图保留契约（核心）

| 原实验类型 | 出现于 | Verilog 原状 | Chisel 版做法 |
|---|---|---|---|
| **填空** | exp5 | `assign x = ;` + `//在这里实现…` | `x := ???` + `// TODO(填空)：<原注释原文> @ <原文件:行号>`。Scala 可编译，elaboration 抛 `NotImplementedError`（等价于原 Verilog 的语法错误：不补全就跑不起来）。参考解放 `solution/` |
| **找错** | exp6 | 语法合法、功能错误 | 保留**同类功能错误**且仍能 elaborate（组合自引用、操作数顺序颠倒、位选范围错误、信号接错、位宽截断）。逐条记入 `MAPPING.md` 的「错误映射表」（原 bug ↔ Chisel 等价 bug）。参考解放 `solution/` |
| **从零实现（CPU）** | exp7~16、18、19、21~23 | `myCPU/` 目录不存在 | 只给端口骨架 + 内部 `???`/TODO，不实现流水线逻辑 |
| **模块级从零实现** | exp17、exp20 | 只给 `tlb_top.v`/`cache_top.v` + testbench | `Tlb`/`Cache` 只给接口骨架 + TODO；顶层与验证环境全量 Chisel 化 |
| **环境** | exp5~exp23 | 完整 Verilog | 全量 Chisel 化、行为等价 |

### 6.1 TODO 标记格式（静态检查依赖）

```text
// TODO(填空)：在这里实现inst_st_w指令的译码 @ code/miniCPU/minicpu_top.v:109
// TODO(找错)：见 MAPPING.md 错误映射表 #3（保持错误，勿修正）
// TODO(实现)：本实验要求自行实现的流水线逻辑 @ code/myCPU/mycpu_top.v:1
```

### 6.2 Xilinx IP 的替代

| IP | 替代 |
|---|---|
| `inst_ram` / `data_ram`（single_port_ram，depth 32 / 8192，非寄存输出） | `RegInit(VecInit(常量表))` + 异步读；或 `SyncReadMem` |
| `clk_pll` | 测试平台直接驱动同一时钟（原 `SIMU_USE_PLL=0` 的加速分支） |
| `axi_ram` / `axi_crossbar_1x2` | Chisel 行为模型（`SyncReadMem` + AXI 握手） |

`.xci` / `.xpr` / `.xdc` / `.tcl` 等 Vivado 工程资产不改写，在 `MAPPING.md` 中列为"不适用"。

## 7. 未实测声明（每个 chisel/README.md 必须包含）

> ⚠️ 本目录的 Chisel 代码为**静态交付**：未在 JDK/Mill/Chisel 环境中编译或仿真过。
> 代码按 §3 的 API 子集手写，`MAPPING.md` 提供逐行对照以便人工复核。
> 如需实际运行，请自行安装 JDK 17 + Mill，并在 `chisel/` 下执行 `./mill chisel.test`。

## 8. 静态验收清单（每个实验）

1. `chisel/` 下 Chisel 文件与 `code/` 下 Verilog 模块一一对应（Vivado 资产除外，需在 `MAPPING.md` 说明）。
2. 学生部分保留原意图，每处带统一 `TODO` 标记；exp5/exp6 另有 `solution/`。
3. 新增文件均为 UTF-8、非空；括号/引号配平；无未闭合注释。
4. `taskvscode/expN/<原任务说明>.md` 已改写，含：任务目标、Chisel 文件清单、映射摘要、
   意图保留说明、验证方式、未实测声明。
5. `MAPPING.md` 覆盖该实验全部模块与关键信号。
6. 保真目录（`output/` 等）文件数与字节数未变。

## 9. 进度

| exp | chisel 树 | md 改写 | MAPPING | 状态 |
|---|---|---|---|---|
| 5 | ✅ | ✅ | ✅ | 完成（自包含模板；9 处 TODO(填空) + solution） |
| 6 | ✅ | ✅ | ✅ | 完成（自包含模板；7 处 TODO(找错) + 错误映射表 + solution） |
| 7 | ✅ | ✅ | ✅ | 完成（共享库布局起点；9 处 TODO(实现) 骨架） |
| 8 | ✅ | ✅ | ✅ | 完成（10 处 TODO(实现)，含 ⑩ 阻塞） |
| 9 | ✅ | ✅ | ✅ | 完成（10 处 TODO(实现)，含 ⑩ 前递 + load-use 阻塞） |
| 10 | ✅ | ✅ | ✅ | 完成（新增 16 条算术逻辑/乘除指令） |
| 11 | ✅ | ✅ | ✅ | 完成（新增 4 条转移 + 6 条访存指令） |
| 12 | ✅ | ✅ | ✅ | 完成（12 处 TODO：CSR + syscall/ertn） |
| 13 | ✅ | ✅ | ✅ | 完成（14 处 TODO：ADEF/ALE/BRK/INE + 中断/定时器） |
| 14 | ✅ | ✅ | ✅ | 完成（握手总线：SocHsBramTop/SramWrap/Bridge1x2Hs/ConfregHsWrap） |
| 15 | ✅ | ✅ | ✅ | 完成（AXI：CpuIOAxi/SocAxiTop/AxiCrossbar1x2/AxiWrapRam/AxiRamSlave/AxiConfregWrap） |
| 16 | ✅ | ✅ | ✅ | 完成（AXI 随机延迟；RTL 与 exp15 相同，仅 func 不同） |
| 17 | ✅ | ✅ | ✅ | 完成（TLB 模块级验证环境 + `Tlb` 骨架 4 处 TODO） |
| 18 | ✅ | ✅ | ✅ | 完成（AXI + TLB 指令/CSR，16 处 TODO） |
| 19 | ✅ | ✅ | ✅ | 完成（DMW + 地址映射 + TLB 例外，17 处 TODO） |
| 20 | ✅ | ✅ | ✅ | 完成（Cache 模块级验证环境 + `Cache` 骨架 5 处 TODO） |
| 21 | ✅ | ✅ | ✅ | 完成（ICache 集成 + Burst，18 处 TODO） |
| 22 | ✅ | ✅ | ✅ | 完成（DCache 集成，19 处 TODO） |
| 23 | ✅ | ✅ | ✅ | 完成（CACOP，20 处 TODO）——**exp5~exp23 全部改写完毕** |

> 详细进度见 `taskvscode/README.md` 的「Chisel 改写进度」表。
> exp5/exp6 为自包含模板；exp7~exp23 共用 `taskvscode/chisel-common/` 中的环境库（见 §2.2）。
>
> **静态验收脚本**：`node tools/chisel_static_check.mjs`（检查项见 §8；报告输出
> `tools/chisel-check-report.json`）。最近一次运行：19 个实验全部 PASS，20 项检查 0 失败，
> `TODO(填空)=9`、`TODO(找错)=7`、`TODO(实现)=218`。

---

## 10. 术语速查表（Verilog ↔ Chisel ↔ 本项目）

> 与 `chisel4agent/README.md` §1.4 的术语表对齐；本表只补充改写中反复出现的对照。

| 概念 | 原 Verilog 写法 | Chisel 写法（本项目） | 说明 / 易错点 |
|---|---|---|---|
| 时钟 | `input clk` + `always @(posedge clk)` | **隐式 `clock`**，不出现在 IO | 本规范 §4.3；`clk` 不建模 |
| 复位 | `if(!resetn) …` | `io.resetn: Input(Bool)` + `when(!io.resetn){…}` | 手写同步复位，不用 `RegInit` 的隐式复位 |
| 寄存器 | `always @(posedge clk) x <= y;` | `val x = Reg(UInt(w.W))` + `when{ x := y }` | 未驱动分支会触发 "not fully initialized"（R6） |
| 复位值 | 复位分支里的常量赋值 | `RegInit(init)` | 只对需要复位值的寄存器用 |
| 组合连线 | `assign a = b;` | `a := b`（`Wire`/`Output`） | 用 `:=` 而非 `=`；`=` 是 Scala 赋值 |
| 线网 | `wire [31:0] x;` | `val x = Wire(UInt(32.W))` | 必须**完整驱动**，否则编译期报错 |
| 位选/拼接 | `x[7:0]`、`{a,b}` | `x(7,0)`、`Cat(a,b)` | 位选是 `(hi,lo)`，不是 `[hi:lo]` |
| 条件选择 | `? :`、`case` | `Mux`、`MuxCase`、`MuxLookup`、`Mux1H` | `Mux1H` 要求恰一位有效（R8） |
| 优先级 | `if/else if` | `when/elsewhen/otherwise` | 后写覆盖先写（last-connect） |
| 数组/表 | `reg [31:0] t[0:15];` | `VecInit(Seq(...))` / `Reg(Vec(n, UInt(32.W)))` | 常量表用 `VecInit`，可读性好 |
| 存储器 | `reg [31:0] ram[0:255];` + 同步读 | `RegInit(VecInit(init))` + `Reg` 读数据 | 初始化由构造参数注入（§6.4） |
| 常量字面量 | `32'h1faf_0000` | `0x1faf0000L.U(32.W)` | 超过 2^31 要加 `L` |
| 无驱动/占位 | `1'bz`、悬空 | `???`（`NotImplementedError`） | 学生骨架用它表达"待实现" |
| 测试 | `initial`/`always` + `$display` | ChiselTest `test(...){}` + `assert`/`fail` | 判据复现见 §8；`TraceHarness` |
| 生成 vs 运行 | （无此区分） | Scala 生成阶段 / 硬件运行阶段 | 见 `chisel4agent/01-从Verilog到Chisel.md` §1.2 |

---

## 11. 核验记录（联网/工作区内可溯源）

> 图例：`✅ 核验`=有权威依据；`⚠️ 修订/补充`=原书或直觉说法需要修正；`❓ 存疑`=尚无权威依据，按工作区材料处理。
> 工作区内依据指已在本仓库、可自行打开的对照材料。

| # | 论断 | 结论 | 依据 |
|---|---|---|---|
| V1 | Chisel 的 `SyncReadMem`/`Mem` 读延迟语义 | 同步读（读数据打一拍）与异步读是**两种不同原语**，本项目用 `RegInit(VecInit(...))` + `Reg` 读数据显式表达"同步读" | ✅ [Chisel Memories 文档](http://www.chisel-lang.org/docs/explanations/memories)、[chisel3.util.SRAM API](https://www.chisel-lang.org/api/latest/chisel3/util/SRAM$.html) |
| V2 | `RegInit(VecInit(Seq(...)))` 作为存储器初值是否合法 | 合法且常用：`RegInit` 可接受 `Vec`；`0.U.asTypeOf(bundle)` 清零是官方 unconnected-wires 文档同款手法 | ✅ [chisel3.RegInit API](https://www.chisel-lang.org/api/snapshot-scala3/chisel3/RegInit$.html)、`chisel4agent/README.md` §3 R6/R7 |
| V3 | Chisel 复位语义 | 3.2+ 同步/异步由 reset 类型决定，默认同步；`Reg()` 无复位值合法 | ✅ `chisel4agent/README.md` §3 R4、[chisel PR #1011（异步复位）](https://github.com/chipsalliance/chisel/pull/1011) |
| V4 | `Mux1H`/`MuxCase`/`MuxLookup` 语义 | `MuxCase` 顺序优先；`MuxLookup` 为逐 key 比较的 Mux 链；`Mux1H` 要求恰一位有效、否则未定义 | ✅ `chisel4agent/README.md` §3 R8 |
| V5 | 未完整驱动会被编译期拒绝 | FIRRTL 报 "not fully initialized"；这正是 exp6 里"隐式线网 bug 无法原样保留"的技术原因 | ✅ `chisel4agent/README.md` §3 R6 |
| V6 | AXI4 的突发长度字段 | 规范中 `AxLEN` 同时表示 `ARLEN`/`AWLEN`，编码为"拍数−1"（`arlen=3` ⇒ 4 拍 = 16B 行） | ✅ [ARM AMBA AXI 协议规范 IHI 0022](https://developer.arm.com/documentation/ihi0022/)、[IHI0022H 规范文本](https://fliphtml5.com/mwjei/yruo/IHI0022H_amba_axi_protocol_spec/) |
| V7 | Mill 构建文件与版本 | 官方支持 `build.mill`（Mill 1.x）与 `build.sc`（0.11.x）两种文件名；本项目给的是 `build.mill` 并在注释里写明 0.11.x 的改名方式 | ✅ [mill：build.sc 支持提交](https://github.com/com-lihaoyi/mill/commit/d7e471e3ae02c209a7b2e61a7efb6dcfa2e2d78b)、[mill 版本升级讨论](https://github.com/SpinalHDL/SpinalHDL/issues/1803)、`chisel4agent/README.md` §3 R13 |
| V8 | ChiselTest 生态现状 | `ucb-bar/chiseltest` 已归档；本项目仍按 0.6.2（treadle 后端）书写，并在 README 中声明版本语境 | ✅ `chisel4agent/README.md` §3 R15 |
| V9 | LoongArch TLB 例外的命名与类别 | TLB 重填（TLBR）、页无效（PIL/PIS）、页修改（PME）、页特权等级不合规（PPI）属 LoongArch 体系结构定义；`PIL` 等常量名在生态代码中一致 | ✅ [龙架构 32 位精简版参考手册 r1p04](https://mirrors.qlu.edu.cn/loongson/docs/loongarch/%E9%BE%99%E6%9E%B6%E6%9E%8432%E4%BD%8D%E7%B2%BE%E7%AE%80%E7%89%88%E5%8F%82%E8%80%83%E6%89%8B%E5%86%8C_r1p04.pdf)、[TLB 内存页表（社区整理）](https://zhuanlan.zhihu.com/p/607788944)、[EDK2 补丁中的 `EXCEPT_LOONGARCH_PIL`](https://patchew.org/EDK2/20231106032521.2251143-1-lichao@loongson.cn/diff/20240105094118.2279380-1-lichao@loongson.cn/) |
| V10 | 各例外 `Ecode` 的精确数值与优先级 | **未逐项联网核验**：`MAPPING.md` 中按原书表格转述，并标注"以原书表格为准" | ❓ 待核验；工作区内依据：`taskvscode/exp13/7.1.2…md`、`taskvscode/exp19/9.2.3…md` |
| V11 | 原 exp6 的两处 bug 无法在 Chisel 原样保留 | 组合自引用环会被 FIRRTL 拒绝；端口名拼错会变成"未驱动"而报错——均为**工具约束**而非教学取舍 | ✅ `chisel4agent/README.md` §3 R6 + 本项目 `exp6/chisel/MAPPING.md` §4 |
| V12 | 原 exp15/exp16 的 RTL 是否相同 | 相同：`axi_wrap_ram.v` 中 `` `define _RUN_PERF_TEST ``（带下划线）与 `` `ifdef RUN_PERF_TEST ``（不带）不匹配，两个实验都走随机掩码分支 | ✅ 工作区内依据：`output/exp15`、`output/exp16` 同名文件逐行比对 |

---

## 12. 快速导航（按任务类型）

| 我要做的事 | 去哪看 |
|---|---|
| 了解改写契约与命名规则 | 本文 §3–§6 |
| 补某个实验的代码 | `taskvscode/expN/chisel/README.md`（速览 + §3 步骤）→ `student/*.scala` 的 `TODO` 处 |
| 核对某个信号/模块的对应关系 | `taskvscode/expN/chisel/MAPPING.md` |
| 理解共享环境库 | `taskvscode/chisel-common/src/main/scala/envlib/`；本文 §2.2、`exp7/chisel/MAPPING.md` §6 |
| 复现静态验收 | `node tools/chisel_static_check.mjs`（报告：`tools/chisel-check-report.json`） |
| 查"为什么这样写"的技术依据 | 本文 §11 核验记录 |
| 学习 Chisel 语法/流水线写法 | `chisel4agent/01…08`；`verilog4agent/`（Verilog 语法与陷阱） |
| 查原实验要求 | `taskvscode/expN/<任务说明>.md`（已改写）与 `CPU设计实战：LoongArch版/`（原文） |

---

## 13. 整体自检（交付级 Agent 快速检查点）

- [ ] `taskvscode/exp5~exp23` 每个实验都有 `chisel/{README.md,MAPPING.md,build.mill,src/}`。
- [ ] 每个 `chisel/README.md` 带 YAML front matter + 速览 + 自检清单 + 未实测声明。
- [ ] 每个 `chisel/MAPPING.md` 带 front matter + 文件级/端口级对照 + 自检清单。
- [ ] 学生模块 TODO 标记数与 `MAPPING.md` 记载一致（9 填空 / 7 找错 / 218 实现）。
- [ ] 19 份任务说明 md 已就地改写（含「Chisel 版」与「未实测声明」）。
- [ ] `code/` 与 `output/`、`CPU设计实战：LoongArch版/`、`raw_book3/`、`verilog4agent/`、`chisel4agent/` 未改动。
- [ ] `node tools/chisel_static_check.mjs` 全绿。
