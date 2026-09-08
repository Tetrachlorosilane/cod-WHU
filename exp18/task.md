---
layout: default
nav_exclude: true
---

### 9.2.2 实践任务18：添加TLB相关指令和CSR寄存器（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/9.2.2实践任务18-添加TLB相关指令和CSR寄存器.md`](../../CPU设计实战：LoongArch版/9.2.2实践任务18-添加TLB相关指令和CSR寄存器.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现 + TLB 集成）

在实践任务16（AXI CPU）和实践任务17（TLB 模块）的基础上完成以下工作：

1. 将实践任务17完成的 TLB 模块集成到实践任务16完成的 CPU 中；
2. 在 CPU 中增加 `TLBSRCH`、`TLBRD`、`TLBWR`、`TLBFILL`、`INVTLB` 指令；
3. 在 CPU 中增加 `TLBIDX`、`TLBEHI`、`TLBELO0`、`TLBELO1`、`ASID`、`TLBRENTRY` CSR 寄存器；
4. 在采用 AXI 总线的 SoC 验证环境里完成 exp18 对应 func（n1~n70）的功能验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：原实验环境仍**不提供** `myCPU/` 目录；
Chisel 版只给接口骨架：
`chisel/src/main/scala/exp18/student/MyCpuTop.scala` 中有 **16 处 `TODO(实现 n/16)`**，
第 ⑯ 项就是本实验的核心（TLB 例化 + 5 条 TLB 指令）。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp18/
├── 9.2.2实践任务18-添加TLB相关指令和CSR寄存器.md   # 本文件
├── code/                                            # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill          # sources 引入 ../../chisel-common
    ├── src/main/scala/exp18/
    │   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：16 处 TODO（⑯ TLB 指令）
    └── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**本实验的环境与 exp15/exp16 完全相同**（`soc_axi` 变体、同一套 AXI 模型与判据）。

#### 参考步骤

1. 把 exp17 的 TLB 实现复制到本实验目录并改包名：
   ```bash
   cp ../exp17/chisel/src/main/scala/exp17/student/Tlb.scala \
      src/main/scala/exp18/student/Tlb.scala
   # 然后把文件里的 `package exp17.student` 改为 `package exp18.student`
   ```
2. 在 `MyCpuTop` 中例化 `Tlb`，实现 5 条 TLB 指令与 6 个 TLB 相关 CSR
   （字段定义见 `chisel/MAPPING.md` §2）。
3. 运行测试：
   ```bash
   cd taskvscode/exp18/chisel
   ./mill chisel.test
   ```
4. 上板验证（Vivado）沿用原书流程，需 `code/soc_verify/soc_axi/run_vivado/` 下的原工程；
   无硬件实验平台可跳过。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `TraceHarness.run` 断言逐条一致 |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

> ⚠️ 未实测声明：`chisel/` 下的代码未在 JDK/Mill/Chisel 环境中编译或仿真过（本次为静态交付）。
> 逐行对照见 `chisel/MAPPING.md`；如实际运行报错，请以 `MAPPING.md` 与 `code/` 下的原 Verilog 为准排查。
