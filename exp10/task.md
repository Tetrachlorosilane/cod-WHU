### 6.1.1 实践任务10：算术逻辑运算指令和乘除法运算指令添加（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/6.1.1实践任务10-算术逻辑运算指令和乘除法运算指令添加.md`](../../CPU设计实战：LoongArch版/6.1.1实践任务10-算术逻辑运算指令和乘除法运算指令添加.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现 + 指令扩展）

在实践任务9实现的 CPU 基础上完成以下工作：

1. 添加算术逻辑运算类指令：`slti`、`sltui`、`andi`、`ori`、`xori`、`sll`、`srl`、`sra`、`pcaddu12i`。
2. 添加乘除运算类指令：`mul.w`、`mulh.w`、`mulh.wu`、`div.w`、`mod.w`、`div.wu`、`mod.wu`。
3. 运行 exp10 对应的 func（n1~n36），要求通过仿真和上板验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：原实验环境仍**不提供** `myCPU/` 目录；
Chisel 版只给接口骨架：
`chisel/src/main/scala/exp10/student/MyCpuTop.scala` 中有 **10 处 `TODO(实现 n/10)`**，
其中 ③（译码）与 ⑤（ALU）明确列出本实验新增的 16 条指令及实现要点。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp10/
├── 6.1.1实践任务10-算术逻辑运算指令和乘除法运算指令添加.md   # 本文件
├── code/                                                     # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill        # sources 引入 ../../chisel-common
    ├── src/main/scala/exp10/
    │   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocBramTop）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：10 处 TODO（含新增指令清单）
    └── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**本实验的环境与 exp7~exp9 完全相同**（`soc_bram` 变体、同一套 SoC 装配与测试判据）。

#### 参考步骤

1. 在 exp9 的流水线基础上扩展译码与 ALU：
   - 立即数：`slti`/`sltui` 用 **si12 符号扩展**；`andi`/`ori`/`xori` 用 **ui12 零扩展**；
     `pcaddu12i` 用 `si20 << 12`；
   - 移位：`sll`/`srl`/`sra` 的移位量来自 `rk[4:0]`；`slli`/`srli`/`srai` 来自 ui5；
   - 乘除：`mul.w`、`mulh.w`、`mulh.wu`、`div.w`、`mod.w`、`div.wu`、`mod.wu`。
2. 运行测试：
   ```bash
   cd taskvscode/exp10/chisel
   ./mill chisel.test
   ```
3. 上板验证（Vivado）沿用原书流程，需 `code/soc_verify/soc_bram/run_vivado/` 下的原工程；
   无硬件实验平台可跳过。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `TraceHarness.run` 断言逐条一致 |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

> ⚠️ 未实测声明：`chisel/` 下的代码未在 JDK/Mill/Chisel 环境中编译或仿真过（本次为静态交付）。
> 逐行对照见 `chisel/MAPPING.md`；如实际运行报错，请以 `MAPPING.md` 与 `code/` 下的原 Verilog 为准排查。
