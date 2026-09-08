---
exp: 5
title: exp5 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp5/code/
ver: agent-1.0
intent: 填空
layout: default
nav_exclude: true
---

# exp5 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp5` 的原样副本）。
> 规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。

## 1. 文件级对应

| 原 Verilog / 资产 | 行数 | Chisel 对应 | 备注 |
|---|---:|---|---|
| `code/miniCPU/minicpu_top.v` | 150 | `src/main/scala/student/MinicpuTop.scala` | **学生模块（9 处填空）**；参考解 `solution/MinicpuTop.scala` |
| `code/miniCPU/regfile.v` | 27 | `src/main/scala/common/Regfile.scala` | 两读一写、读地址 0 恒 0 |
| `code/miniCPU/tools.v` | 53 | `src/main/scala/common/Decoders.scala` | 4 个 one-hot 译码器（`co` 端口名） |
| `code/soc_verify/rtl/soc_mini_top.v` | 167 | `src/main/scala/soc/SocMiniTop.scala` | SoC 顶层 |
| `code/soc_verify/rtl/CONFREG/confreg.v` | 74 | `src/main/scala/soc/Confreg.scala` | 16 位 LED |
| `code/soc_verify/rtl/xilinx_ip/inst_ram/inst_ram.xci` | — | `src/main/scala/soc/InstRam.scala` | IP → 行为模型（含 `InstRamProgram`） |
| `code/soc_verify/rtl/xilinx_ip/clk_pll/clk_pll.xci` | — | 不建模 | Chisel 隐式 clock 直连（对应原 `SIMU_USE_PLL=0` 加速分支） |
| `code/soc_verify/testbench/mycpu_tb.v` | 68 | `src/test/scala/MinicpuTopSpec.scala` | 把"肉眼看 led"变成可断言判据 |
| `code/soc_verify/run_vivado/constraints/soc_mini_top.xdc` | — | 不适用 | 上板约束，不在改写范围 |
| `code/soc_verify/run_vivado/create_project.tcl` | — | 不适用 | Vivado 工程脚本 |
| `code/func/`（`start.S`/`inst_ram.coe`/`Makefile`/`bin.lds`/`convert.c`） | — | 保留 + 内容内嵌 | 汇编程序未改写；`inst_ram.coe` 的 12 个字以常量表内嵌进 `InstRamProgram` |

**未改写的 Vivado 资产**：`.xci`（IP 配置）、`.xdc`（约束）、`.tcl`（工程脚本）、`.xpr`（工程文件）。
它们与"实验环境的 RTL 行为"无关，Chisel 侧用行为模型替代（见 §5）。

## 2. `minicpu_top` ↔ `MinicpuTop` 端口对照

| Verilog 端口 | 方向 | Chisel | 备注 |
|---|---|---|---|
| `clk` | input | —（隐式 `clock`） | 按规范 §4.3 不建 IO |
| `resetn` | input | `io.resetn` | 低有效；原 SoC 传 `cpu_resetn` |
| `inst_sram_we` | output | `io.inst_sram_we` | 恒 0 |
| `inst_sram_addr` | output | `io.inst_sram_addr` | = `pc` |
| `inst_sram_wdata` | output | `io.inst_sram_wdata` | 恒 0 |
| `inst_sram_rdata` | input | `io.inst_sram_rdata` | |
| `data_sram_we` | output | `io.data_sram_we` | = `mem_we` |
| `data_sram_addr` | output | `io.data_sram_addr` | = `alu_result` |
| `data_sram_wdata` | output | `io.data_sram_wdata` | = `rkd_value` |
| `data_sram_rdata` | input | `io.data_sram_rdata` | |

## 3. `minicpu_top` 内部信号对照（关键项）

| Verilog | 行 | Chisel | 说明 |
|---|---:|---|---|
| `reg reset; always @(posedge clk) reset <= ~resetn;` | 16-17 | `val reset = RegNext(!io.resetn, true.B)` | 原无初值（起始 X），Chisel 给确定性初值 |
| `reg valid; … if (reset) valid<=0 else valid<=1` | 19-27 | `val valid = RegInit(false.B)` + `when/otherwise` | |
| `reg [31:0] pc` | 29 | `val pc = RegInit(0x1bfffffcL.U(32.W))` | 更新语句移到文件末尾 |
| `nextpc` | 30 | `val nextpc = Wire(UInt(32.W))` | **填空 7/9** |
| `op_31_26` … `i16` | 34-46 | `inst(31,26)` … `inst(25,10)` | 位选 `[i:j]` → `(i, j)` |
| `decoder_* u_dec0..3` | 101-104 | `Module(new Decoder6_64/4_16/2_4/5_32)` | 端口 `co` |
| `inst_add_w/addi_w/ld_w` | 106-108 | 同名字段 | `&` → `&`，`[i]` → `(i)` |
| `inst_st_w` | 109 | **填空 1/9** | 参考解：`op_31_26_d(0x0a) & op_25_22_d(0x6)` |
| `src2_is_imm` | 112 | **填空 2/9** | 参考解：`inst_addi_w | inst_ld_w | inst_st_w` |
| `res_from_mem` / `gr_we` / `mem_we` / `src_reg_is_rd` | 113-116 | 同名字段 | |
| `rf_raddr1` / `rf_raddr2` | 118-119 | 同名字段 | `Mux(src_reg_is_rd, rd, rk)` |
| `regfile` 实例化 | 120-129 | `Module(new Regfile)` | 引脚填空 3/9、4/9、5/9 |
| `rj_value` / `rkd_value` | 60-61 | `u_regfile.io.rdata1/2` | |
| `br_offs` | 131 | **填空 6/9** | 参考解：`Cat(Fill(14,i16(15)), i16, 0.U(2.W))` |
| `br_target` / `rj_eq_rd` / `br_taken` | 132-134 | 同名字段 | |
| `nextpc` | 135 | **填空 7/9** | 参考解：`Mux(br_taken, br_target, pc + 4.U)` |
| `imm = {{20{i12[11]}}, i12[11:0]}` | 137 | `Cat(Fill(20, i12(11)), i12)` | |
| `alu_src2` | 139 | **填空 8/9** | 参考解：`Mux(src2_is_imm, imm, rkd_value)` |
| `alu_result = alu_src1+alu_src2` | 141 | `alu_src1 + alu_src2` | |
| `rf_wdata` | 147 | **填空 9/9** | 参考解：`Mux(res_from_mem, io.data_sram_rdata, alu_result)` |

## 4. 填空清单（9 处，与原 Verilog 一一对应）

| # | 原 Verilog 位置 | 原注释 | Chisel 位置 | 参考解 |
|---:|---|---|---|---|
| 1 | `minicpu_top.v:109` | 在这里实现inst_st_w指令的译码 | `MinicpuTop.scala` 填空 1 | `op_31_26_d(0x0a) & op_25_22_d(0x6)` |
| 2 | `minicpu_top.v:112` | 在这里实现立即数选择信号 | 填空 2 | `inst_addi_w | inst_ld_w | inst_st_w` |
| 3 | `minicpu_top.v:121` | 在空出的括号里完成引脚匹配（`.raddr1`） | 填空 3 | `rf_raddr1` |
| 4 | `minicpu_top.v:123` | 同上（`.raddr2`） | 填空 4 | `rf_raddr2` |
| 5 | `minicpu_top.v:127` | 同上（`.waddr`） | 填空 5 | `rd` |
| 6 | `minicpu_top.v:131` | 在这里完成br_offs信号的生成 | 填空 6 | `Cat(Fill(14, i16(15)), i16, 0.U(2.W))` |
| 7 | `minicpu_top.v:135` | 在这里实现nextpc信号的生成 | 填空 7 | `Mux(br_taken, br_target, pc + 4.U)` |
| 8 | `minicpu_top.v:139` | 在这里实现alu_src2信号 | 填空 8 | `Mux(src2_is_imm, imm, rkd_value)` |
| 9 | `minicpu_top.v:147` | 在这里完成写回寄存器值的选择 | 填空 9 | `Mux(res_from_mem, io.data_sram_rdata, alu_result)` |

> 原书描述与代码的差异：`4.3.1实践任务5-5条指令单周期CPU.md` 只写"补充代码中缺失的部分"，
> 未列出空位数量；逐行核对原 Verilog 后确认共 **9 处**（6 条 `assign` + regfile 3 个引脚）。

## 5. Xilinx IP ↔ Chisel 行为模型

| IP 参数（`inst_ram.xci`） | 取值 | Chisel 实现 |
|---|---|---|
| `memory_type` | `single_port_ram` | `RegInit(VecInit(...))` + 同步写 + 异步读 |
| `depth` | 32 | `depth = 32`（构造参数） |
| `C_ADDR_WIDTH` | 5 | 索引 `io.a(4, 0)`；原实例化传 `inst_addr[17:2]`（16 位，高 11 位被 IP 截断） |
| `C_WIDTH` | 32 | `UInt(32.W)` |
| `output_options` | `non_registered` | 读数据不经寄存器（组合输出 `io.spo := mem(...)`） |
| `coefficient_file` | `../../../../func/inst_ram.coe` | `InstRamProgram.words`（12 个字常量表） |
| `clk_pll` | — | 不建模；`SocMiniTop` 用隐式 clock（等价 `SIMU_USE_PLL=0`） |

## 6. 行为等价性检查点

| 原 Verilog 行为 | Chisel 对应 | 是否等价 |
|---|---|---|
| `cpu_resetn <= resetn`（1 拍延迟） | `RegNext(io.resetn, false.B)` | ✅ |
| 地址 1024 读 `{24'b0, ~switch[7:0]}` | `Mux(addr === 1024.U, Cat(0.U(24.W), ~io.switch), 0.U(32.W))` | ✅ |
| `conf_we = data_we && addr == 1028` | 同名 `val conf_we` | ✅ |
| `led = ~conf_led` | `io.led := ~confreg.io.led` | ✅ |
| `regfile` 读地址 0 恒 0、写 x0 不屏蔽 | `Mux(raddr === 0.U, 0.U, rf.read(raddr))` | ✅ |
| 复位值 `pc = 0x1bfffffc` | `RegInit(0x1bfffffcL.U(32.W))` | ✅ |

## 7. 与原实验判据的对应

| 原判据 | Chisel 判据 |
|---|---|
| `tb_top` 固定 `switch = ~(8'h4)`，人眼看 `led` | `MinicpuTopSpec`：`switch=0xfb` ⇒ `led = 0xfffa` |
| 改 `switch` 看 `f(n)` | `n ∈ {1,2,3,4,5,6,8,10,12}` 逐一断言 `led = ~f(n) & 0xffff` |
| 参考模型 | `func/start.S` 的等价 Scala 实现 `fib(n)` |

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp5`。
