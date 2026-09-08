---
layout: default
nav_exclude: true
---

### 5.1.2 实践任务8：阻塞技术解决相关引发的冲突（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/5.1.2实践任务8-阻塞技术解决相关引发的冲突.md`](../../CPU设计实战：LoongArch版/5.1.2实践任务8-阻塞技术解决相关引发的冲突.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现 + 阻塞）

在实践任务7实现的 CPU 基础上完成以下工作：

1. 加入适当的逻辑处理**寄存器写后读（RAW）数据相关**引发的流水线冲突
   （本任务中**只要求使用阻塞技术**，前递留到实践任务9）。
2. 运行 exp8 对应的 func，要求通过仿真和上板验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：原实验环境仍**不提供** `myCPU/` 目录；
Chisel 版只给接口骨架：
`chisel/src/main/scala/exp8/student/MyCpuTop.scala` 中有 **10 处 `TODO(实现 n/10)`**，
其中第 ⑩ 项就是本实验的核心（RAW 相关检测与阻塞）。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp8/
├── 5.1.2实践任务8-阻塞技术解决相关引发的冲突.md   # 本文件
├── code/                                          # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill        # sources 引入 ../../chisel-common
    ├── src/main/scala/exp8/
    │   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocBramTop）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：10 处 TODO（含 ⑩ 阻塞）
    └── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**本实验的环境与 exp7 完全相同**（`soc_bram` 变体、同一份 `sync_ram` 模型、同一套 SoC 装配与
测试判据）；唯一区别是 **func 程序**：exp8 的相关指令之间不再插入 NOP，因此流水线必须
自己用阻塞技术处理 RAW 相关，否则 trace 比对会失败。

#### 参考步骤

1. 先完成 exp7 的五级流水线（或不处理冲突的版本）。
2. 实现第 ⑩ 项：比较 ID 级要读的 `rj/rk` 与 EX/MEM 级尚未写回的 `rd`；
   相关时冻结 `pc` 与 IF/ID 流水寄存器、把 ID/EX 清成气泡（插入 NOP）。
3. 运行测试：
   ```bash
   cd taskvscode/exp8/chisel
   ./mill chisel.test
   ```
4. 上板验证（Vivado）沿用原书流程，需 `code/soc_verify/soc_bram/run_vivado/` 下的原工程；
   无硬件实验平台可跳过。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `TraceHarness.run` 断言逐条一致，不一致直接 `fail` 并打印两行对照 |
| `debug_wb_pc == 32'h1c000100` 时结束，打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视，异常计数非 0 即失败 |

> ⚠️ 未实测声明：`chisel/` 下的代码未在 JDK/Mill/Chisel 环境中编译或仿真过（本次为静态交付）。
> 逐行对照见 `chisel/MAPPING.md`；如实际运行报错，请以 `MAPPING.md` 与 `code/` 下的原 Verilog 为准排查。
