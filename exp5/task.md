### 4.3.1 实践任务5：5条指令单周期CPU（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/4.3.1实践任务5-5条指令单周期CPU.md`](../../CPU设计实战：LoongArch版/4.3.1实践任务5-5条指令单周期CPU.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：填空）

1. 阅读并理解 Chisel 实验环境中提供的代码，**补充代码中缺失的部分**，使设计可以通过仿真验证。

原 Verilog 版的空位是 `assign x = ;` 与 regfile 实例化里的空括号；**Chisel 版保留同样的填空意图**：
`chisel/src/main/scala/student/MinicpuTop.scala` 中留有 **9 处空位**，每处写作

```scala
// TODO(填空 n/9)：<原 Verilog 中的注释原文> @ code/miniCPU/minicpu_top.v:<行号>
xxx := ???
```

未补全时 elaboration 会以 `NotImplementedError` 终止 —— 这等价于原 Verilog 含空位时的语法错误：
不补全就跑不起来。参考解在 `chisel/solution/MinicpuTop.scala`。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp5/
├── 4.3.1实践任务5-5条指令单周期CPU.md   # 本文件
├── code/                                # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/                              # Chisel 版实验环境
    ├── README.md                        # 使用说明 + 未实测声明
    ├── MAPPING.md                       # Verilog ↔ Chisel 逐行对照 + 填空清单
    ├── build.mill                       # Mill 构建
    ├── src/main/scala/
    │   ├── common/Decoders.scala        # 4 个译码器（对应 code/miniCPU/tools.v）
    │   ├── common/Regfile.scala         # 寄存器堆（对应 code/miniCPU/regfile.v）
    │   ├── soc/Confreg.scala            # LED 控制寄存器（对应 CONFREG/confreg.v）
    │   ├── soc/InstRam.scala            # inst_ram IP 行为模型 + func/inst_ram.coe 内容
    │   ├── soc/SocMiniTop.scala         # SoC 顶层（对应 soc_mini_top.v）
    │   └── student/MinicpuTop.scala     # ★ 学生模块：9 处填空
    ├── src/test/scala/MinicpuTopSpec.scala   # ChiselTest 验证
    └── solution/MinicpuTop.scala        # 参考解
```

Chisel 侧**全量改写**了原实验环境（SoC 顶层、CONFREG、寄存器堆、译码器、测试平台），
Xilinx IP（`inst_ram`、`clk_pll`）用 Chisel 行为模型替代；
Vivado 资产（`.xci`/`.xdc`/`.tcl`/`.xpr`）不在改写范围，仍保留在 `code/` 下。

#### 参考步骤

1. 打开 `chisel/src/main/scala/student/MinicpuTop.scala`，按 `TODO(填空 1/9 … 9/9)` 逐个补全。
   每处 TODO 都标注了它在原 Verilog 中的位置（`code/miniCPU/minicpu_top.v:行号`）与原注释原文，
   可随时对照 `code/` 或 `chisel/MAPPING.md` §4「填空清单」。
2. 补全后运行 ChiselTest（需自行安装 JDK 17 + Mill；本目录为静态交付，未实测）：
   ```bash
   cd taskvscode/exp5/chisel
   ./mill chisel.test
   ```
   或用参考解验证环境本身：`cp solution/MinicpuTop.scala src/main/scala/student/MinicpuTop.scala && ./mill chisel.test`
3. 测试程序仍是斐波那契程序（`code/func/start.S`，已内嵌为 `chisel/src/main/scala/soc/InstRam.scala`
   中的 `InstRamProgram`）：拨码开关 `switch` 的值相当于 `n`，`led` 输出 `f(n)`
   （`f(1)=1, f(2)=2, f(3)=3, f(4)=5, …`）。
   原实验靠人眼看波形/看 led，Chisel 版把它变成断言：
   - 原 `tb_top` 的固定激励 `switch = ~(8'h4)` ⇒ `n=4` ⇒ `f(4)=5` ⇒ `led = 0xfffa`；
   - 另对 `n = 1,2,3,4,5,6,8,10,12` 逐一断言 `led = ~f(n) & 0xffff`。
4. 上板验证（Vivado 综合/实现/生成比特流/下载）沿用原书流程，需用 `code/soc_verify/run_vivado/`
   下的原工程；无硬件实验平台可跳过。Chisel 版不覆盖上板流程。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| 仿真波形正确、`led` 显示 `f(n)` | `./mill chisel.test` 全部用例通过 |
| 修改 `tb_top` 的 `switch` 观察 `led` | 修改 `MinicpuTopSpec` 中的 `n` 列表即可 |

> ⚠️ 未实测声明：`chisel/` 下的代码未在 JDK/Mill/Chisel 环境中编译或仿真过（本次为静态交付）。
> 逐行对照见 `chisel/MAPPING.md`；如实际运行报错，请以 `MAPPING.md` 的对照关系与
> `code/` 下的原 Verilog 为准排查。
