### 10.2.3 实践任务22：CPU中集成DCache（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/10.2.3实践任务22-CPU中集成DCache.md`](../../CPU设计实战：LoongArch版/10.2.3实践任务22-CPU中集成DCache.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现 + DCache 集成）

在实践任务21完成的基础上完成以下工作：

1. 将实践任务20完成的 Cache 模块作为 **DCache** 集成到实践任务21完成的 CPU 中；
2. 在采用 AXI 总线的 SoC 验证环境里完成 exp22 对应 func（n1~n72）的功能验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：原实验环境仍**不提供** `myCPU/` 目录；
Chisel 版只给接口骨架：
`chisel/src/main/scala/exp22/student/MyCpuTop.scala` 中有 **19 处 `TODO(实现 n/19)`**，
第 ⑲ 项就是本实验的核心（DCache 例化 + 写通路 + 脏行写回 + 访存停顿）。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp22/
├── 10.2.3实践任务22-CPU中集成DCache.md   # 本文件
├── code/                                   # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill # sources 引入 ../../chisel-common
    ├── src/main/scala/exp22/
    │   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：19 处 TODO（⑲ DCache）
    └── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**本实验的环境与 exp15~exp21 完全相同**（`soc_axi` 变体、同一套 AXI 模型与判据）。

#### 参考步骤

1. 把 exp20 的 Cache 复制到本实验目录并改包名（或重新实现）。
2. 在访存通路上例化 DCache：
   - 写命中：按 `wstrb` 更新行内数据并置脏；写 miss：先取行（写分配）再写；
   - 替换脏行：通过 `wr_req`/`wr_data`（128 位整行）写回；
   - 访存未完成时冻结 MEM/WB，并与前递/例外/分支冲刷协调。
3. 运行测试：
   ```bash
   cd taskvscode/exp22/chisel
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
