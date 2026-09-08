---
exp: 6
title: exp6 Verilog ↔ Chisel 对照表
doc: mapping
baseline: taskvscode/exp6/code/
ver: agent-1.0
intent: 找错
---

# exp6 Verilog ↔ Chisel 对照表

> 基线：`../code/`（`output/exp6` 的原样副本）。规范：`../../CHISEL-CONVENTIONS.md`。
> ⚠️ 静态交付：未编译、未仿真；本表用于人工逐行复核。
> ★ 本实验的核心是 §4「错误映射表」：原 Verilog 的 7 处错误在 Chisel 版中的等价保留。

## 1. 文件级对应

| 原 Verilog / 资产 | 行数 | Chisel 对应 | 备注 |
|---|---:|---|---|
| `code/myCPU/mycpu_top.v` | 275 | `src/main/scala/student/MyCpuTop.scala` | **学生模块：含 3 处错误**；参考解 `solution/MyCpuTop.scala` |
| `code/myCPU/alu.v` | 99 | `src/main/scala/student/Alu.scala` | **学生模块：含 4 处错误**；参考解 `solution/Alu.scala` |
| `code/myCPU/regfile.v` | 27 | `src/main/scala/common/Regfile.scala` | 本身无 bug |
| `code/myCPU/decoder_2_4.v` | 11 | `src/main/scala/common/Decoders.scala` | 端口 `in`/`out`（exp5 是 `co`） |
| `code/myCPU/decoder_4_16.v` | 11 | 同上 | |
| `code/myCPU/decoder_5_32.v` | 11 | 同上 | |
| `code/myCPU/decoder_6_64.v` | 11 | 同上 | |
| `code/soc_verify/soc_dram/rtl/soc_lite_top.v` | 227 | `src/main/scala/soc/SocLiteTop.scala` | SoC 顶层 |
| `code/soc_verify/soc_dram/rtl/BRIDGE/bridge_1x2.v` | 93 | `src/main/scala/soc/Bridge1x2.scala` | 1×2 桥 |
| `code/soc_verify/soc_dram/rtl/CONFREG/confreg.v` | 666 | `src/main/scala/soc/Confreg.scala` | 板级外设全量改写 |
| `code/soc_verify/soc_dram/testbench/async_ram.v` | 89 | `src/main/scala/soc/AsyncRam.scala` | `async_ram`/`inst_ram`/`data_ram` |
| `code/soc_verify/soc_dram/testbench/mycpu_tb.v` | 272 | `src/test/scala/MyCpuTbSpec.scala` | trace 比对 + 测试点监视 |
| `code/soc_verify/soc_dram/rtl/xilinx_ip/*.xci` | — | 不适用 | 仿真用 `async_ram.v` 模型；综合用的 IP 不改写 |
| `code/soc_verify/soc_dram/run_vivado/*` | — | 不适用 | Vivado 工程资产 |
| `code/gettrace/src/*.v` | — | 不适用 | 参考核（FIRRTL 风格 Verilog）+ tb；仅用其产出的 `golden_trace.txt` |
| `code/func/` | — | 保留 + 测试侧读取 | `.mif` 由 `MifLoader` 读入并注入 RAM 模型 |

## 2. 端口对照（关键项）

### `mycpu_top` ↔ `MyCpuTop`

| Verilog | Chisel | 备注 |
|---|---|---|
| `clk` | —（隐式 `clock`） | 规范 §4.3 |
| `resetn` | `io.resetn` | 低有效 |
| `inst_sram_we/addr/wdata/rdata` | 同名 `io.*` | |
| `data_sram_we/addr/wdata/rdata` | 同名 `io.*` | exp6 的 `data_sram_we` 仍为 1 位 |
| `debug_wb_pc` | `io.debug_wb_pc` | |
| `debug_wb_rf_we` | `io.debug_wb_rf_we` | 4 位 |
| `debug_wb_rf_wnum` / `debug_wb_rf_wdata` | 同名 | |

### `alu` ↔ `Alu`

| Verilog | Chisel |
|---|---|
| `alu_op[11:0]` | `io.alu_op` |
| `alu_src1` / `alu_src2` | 同名 |
| `alu_result` | `io.alu_result` |
| `{adder_cout, adder_result} = a + b + cin` | `val adder_res = a +& b +& cin`；`adder_result = adder_res(31,0)`、`adder_cout = adder_res(32)` |
| `{{32{op_sra & alu_src2[31]}}, alu_src2[31:0]}` | `Cat(Fill(32, op_sra & io.alu_src2(31)), io.alu_src2)` |

## 3. `confreg` ↔ `Confreg` 要点

| Verilog | Chisel |
|---|---|
| `conf_addr[15:0] == 16'h8000` 等地址比较 | `a16 === 0x8000.U(16.W)` |
| `conf_rdata = (…) ? … : 32'd0` 长三目链 | `MuxCase(0.U, Seq((cond -> data), …))` |
| `case(count[19:17])` 扫描数码管 | `VecInit(...)(count(19,17))` |
| `always @(posedge clk) if(!resetn) … else if(wr) …` | `when(!io.resetn){…}.elsewhen(wr){…}` |
| `timer` 用 `timer_clk` | 同一隐式时钟域（本实验 `timer_clk = cpu_clk`） |
| `open_trace` / `num_monitor` 复位后为 1 | 同；测试按"始终开启"处理（func 程序不写这两位） |

## 4. ★错误映射表（本实验的核心）

原 Verilog 共 7 处错误。Chisel 版**逐条等价保留**；其中 #1 因 Chisel 无法 elaborate 组合环而按
规范 §6 做了等价替换，其余为逐字保留。

| # | 原 Verilog 位置 | 原错误 | 原表现 | Chisel 位置与写法 | Chisel 表现 |
|---:|---|---|---|---|---|
| 1 | `alu.v:74` | `or_result = alu_src1 \| alu_src2 \| alu_result;`（组合自引用） | 组合环，or 结果 X/振荡 | `student/Alu.scala` `val or_result = io.alu_src1` | **等价替换**：or 指令只取 alu_src1，结果错误。理由：`\| alu_result` 与 `alu_result` 成环，FIRRTL 会在 elaboration 阶段报 combinational loop，无法作为"可运行找错"的题面 |
| 2 | `alu.v:80` | `sll_result = alu_src2 << alu_src1[4:0]`（操作数写反） | slli.w 结果错误 | `val sll_result = io.alu_src2 << io.alu_src1(4, 0)` | 同（逐字保留） |
| 3 | `alu.v:83` | `sr64_result = {…alu_src2…} >> alu_src1[4:0]`（被移数与移位量都写反） | srli.w/srai.w 结果错误 | `val sr64_result = Cat(Fill(32, op_sra & io.alu_src2(31)), io.alu_src2) >> io.alu_src1(4, 0)` | 同（逐字保留） |
| 4 | `alu.v:85` | `sr_result = sr64_result[30:0]`（位选范围错，31 位赋 32 位线网） | 右移结果最高位恒 0 | `val sr_result = sr64_result(30, 0).pad(32)` | 同（`.pad(32)` 等价于 Verilog 的零扩展） |
| 5 | `mycpu_top.v:253` | `.alu_src1(alu_src2)`（端口接错） | 所有用 ALU 的指令结果错误 | `u_alu.io.alu_src1 := alu_src2` | 同（逐字保留） |
| 6 | `mycpu_top.v:271` | `assign debug_wb_rf_wen = …`（端口名拼错 → `debug_wb_rf_we` 从未被驱动） | 波形为 Z，trace 比对永不触发 | `io.debug_wb_rf_we := 0.U(4.W)` | 等价替换：Chisel 无 Z，未驱动 Output 会报 "not fully initialized"，故显式恒 0；效果同为"debug 写使能失效" |
| 7 | `mycpu_top.v:263` | `final_result` 未声明 → Verilog 隐式 1 位线网 | 写回值只保留最低位 | `Mux(res_from_mem, mem_result, alu_result)(0).asUInt.pad(32)` | 等价保留：显式取 bit 0 再零扩展，与隐式 1 位线网行为一致 |

参考解逐条修正见 `solution/Alu.scala`（#1'~#4）与 `solution/MyCpuTop.scala`（#5~#7）。

## 5. RAM / 测试平台对应

| 原 Verilog | Chisel | 差异 |
|---|---|---|
| `async_ram #(ADDR_WIDTH=15, DEPTH=32768)` | `AsyncRam(init, addrWidth=15)` | 深度按 `init` 长度向上取 2 的幂；异步读、同步写一致 |
| `rdata = (!we) ? data_out : {32{1'bz}}` | `Mux(io.we, 0.U, ram.read(...))` | Chisel 无 Z → 写周期取 0 |
| `$readmemb("../../func/obj/inst_ram.mif", …)` | 测试侧 `MifLoader.load(...)` → 构造参数 `init` | 不依赖 experimental API |
| `inst_ram .a(cpu_inst_addr[17:2])`（15 位端口） | `inst_ram.io.a := cpu.io.inst_sram_addr(16, 2)` | 原连接高 1 位被截断，Chisel 直接写 `(16,2)` |
| `data_ram .we(data_sram_we & data_sram_en)` | 同名 | |
| `mycpu_tb.v` 的 trace 比对 | `MyCpuTbSpec` | 逻辑等价：写回有效且 wnum≠0 时逐条比对；按字节使能掩码 |
| `mycpu_tb.v` 的 `num_monitor` | `MyCpuTbSpec` 监视 `dut.u_confreg.io.num_data` | 低 8 位 +1、高 8 位 +1 |
| `mycpu_tb.v` 的 `CONFREG_OPEN_TRACE/NUM_MONITOR` 层次化引用 | 按"恒为 1"处理 | func 程序不写这两个控制位 |

## 6. 行为等价性检查点

| 原 Verilog | Chisel | 等价 |
|---|---|---|
| `cpu_resetn <= resetn`（1 拍） | `RegNext(io.resetn, false.B)` | ✅ |
| `bridge` 的 `sel_conf/sel_sram` + 掩码读数据 | `Mux(sel_sram, data_sram_rdata, conf_rdata)` | ✅（两信号互斥） |
| `pc` 复位值 `0x1bfffffc` | `RegInit(0x1bfffffcL.U(32.W))` | ✅ |
| `imm` / `br_offs` / `jirl_offs` 三目链 | 同名 `Mux` 嵌套 | ✅ |
| `alu_op[11:0]` 位拼接 | `Cat(...)`（MSB→LSB） | ✅ |
| `gr_we = ~st & ~beq & ~bne & ~b & ~bl` | `~inst_st_w & ~inst_beq & …` | ✅ |

## 7. 核验标注（✅/⚠️/❓）

> ✅ 核验（工具约束）：原 `alu.v` 的组合自引用（`or_result = src1 | src2 | alu_result`）与
> 端口名拼错（`debug_wb_rf_wen`）在 Verilog 里分别表现为组合环与隐式线网，而 Chisel/FIRRTL 会在
> elaboration 阶段直接拒绝（"not fully initialized" / 组合环检测），**无法原样保留**。
> 故按"同族功能错误"等价替换，逐条记录于 §4。
> 依据: `../../CHISEL-CONVENTIONS.md` §11 V5、V11；
> https://www.chisel-lang.org/docs/explanations/unconnected-wires

> ⚠️ 修订：错误编号沿用原 bug 在 `alu.v` / `mycpu_top.v` 中的出现顺序，**不代表难度或优先级**。

> ❓ 存疑：`alu.v` 中 `sr64_result` 的移位量位宽裁剪是否属原书有意为之，未逐条核验（按原文照抄）。

## 自检（Agent 快速检查点）

- [ ] 文件级对应表覆盖 `code/` 下全部 Verilog 模块（Vivado 资产已标注"不适用"）。
- [ ] 端口/信号对照覆盖学生模块的全部顶层端口。
- [ ] 与前一实验的差异已列表（新增/变化项逐条）。
- [ ] 原实验意图的保留方式已写明（填空/找错/骨架）。
- [ ] 静态检查通过：`node ../../../tools/chisel_static_check.mjs exp6`。
