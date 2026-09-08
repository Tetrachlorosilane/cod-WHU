### 5.1.1 实践任务7：不考虑相关冲突处理的简单流水线CPU（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/5.1.1实践任务7-不考虑相关冲突处理的简单流水线CPU.md`](../../CPU设计实战：LoongArch版/5.1.1实践任务7-不考虑相关冲突处理的简单流水线CPU.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现）

在实践任务6实现的单周期 CPU 基础上完成以下工作：

1. 调整 CPU 顶层接口，增加指令 RAM 片选 `inst_sram_en` 与数据 RAM 片选 `data_sram_en`。
2. 把 `inst_sram_we` 和 `data_sram_we` 都从 1 比特写使能调整为 **4 比特字节写使能**。
3. 设计一个不考虑相关引发的冲突的单发射五级流水 CPU。
4. 运行 exp7 对应的 func，要求通过仿真和上板验证。

**Chisel 版保留的教学意图 = 从零实现**：原实验环境**不提供** `myCPU/` 目录（由学生自己写），
Chisel 版同样只给接口骨架：
`chisel/src/main/scala/exp7/student/MyCpuTop.scala` 中有 **9 处 `TODO(实现 n/9)`**，
内部流水线逻辑全部留空（`???`）；未实现时 elaboration 以 `NotImplementedError` 终止。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp7/
├── 5.1.1实践任务7-不考虑相关冲突处理的简单流水线CPU.md   # 本文件
├── code/                                                  # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/                                                # Chisel 版实验环境
    ├── README.md / MAPPING.md / build.mill
    ├── src/main/scala/exp7/
    │   ├── soc/SocLiteTop.scala        # SoC 顶层（soc_bram 变体：block RAM + 片选 + 4 位字节使能）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：从零实现（9 处 TODO）
    └── src/test/scala/MyCpuTbSpec.scala  # 复现原 tb_top 的 golden_trace 比对
```

实验环境的公共模块集中在 [`chisel-common/`](../chisel-common/)（`Confreg`、`Bridge1x2`、
`SyncRam`/`InstRam`/`DataRam`、`Decoders`、`Regfile`、测试工具），
由 `build.mill` 通过 `sources` 引入；布局约定见 [`CHISEL-CONVENTIONS.md`](../CHISEL-CONVENTIONS.md) §2。

#### 参考步骤

1. 通读 `chisel/src/main/scala/exp7/student/MyCpuTop.scala` 的骨架注释（IF→ID→EX→MEM→WB 九步）。
2. 参考 exp6 的单周期实现（`../exp6/code/myCPU/`，注意它含错误）与本实验的 SoC 环境；
   流水线写法可参考 [`chisel4agent/08-五级流水线CPU串讲.md`](../../chisel4agent/08-五级流水线CPU串讲.md)、
   [`09-CPU开发场景速查.md`](../../chisel4agent/09-CPU开发场景速查.md)。
3. 逐条实现 `TODO(实现 1/9 … 9/9)`，然后运行测试：
   ```bash
   cd taskvscode/exp7/chisel
   ./mill chisel.test
   ```
4. 本实验**不要求**处理数据相关/控制相关引发的冲突（那是 exp8/exp9 的任务）；
   exp7 的 func 程序在相关指令之间已插入 NOP，因此流水线即使不做冲突处理也能跑通。
5. 上板验证（Vivado 综合/实现/生成比特流/下载）沿用原书流程，需
   `code/soc_verify/soc_bram/run_vivado/` 下的原工程；无硬件实验平台可跳过。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `MyCpuTbSpec` 断言：逐条一致；不一致直接 `fail` 并打印 reference/mycpu 两行 |
| `debug_wb_pc == 32'h1c000100` 时结束，打印 `----PASS!!!` | 到达 `END_PC` 后断言通过并打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | 同样监视，异常计数非 0 即失败 |

> ⚠️ 未实测声明：`chisel/` 下的代码未在 JDK/Mill/Chisel 环境中编译或仿真过（本次为静态交付）。
> 逐行对照见 `chisel/MAPPING.md`；如实际运行报错，请以 `MAPPING.md` 与 `code/` 下的原 Verilog 为准排查。
