---
layout: default
nav_exclude: true
---

### 7.1.1 实践任务12：添加系统调用异常支持（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/7.1.1实践任务12-添加系统调用异常支持.md`](../../CPU设计实战：LoongArch版/7.1.1实践任务12-添加系统调用异常支持.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现 + 例外支持）

在实践任务11实现的 CPU 基础上完成以下工作：

1. 为 CPU 增加 `csrrd`、`csrwr`、`csrxchg` 和 `ertn` 指令。
2. 为 CPU 增加控制状态寄存器 `CRMD`、`PRMD`、`ESTAT`、`ERA`、`EENTRY`、`SAVE0~3`。
3. 为 CPU 增加 `syscall` 指令，实现系统调用异常支持。
4. 运行 exp12 对应的 func（n1~n47），要求通过仿真和上板验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：原实验环境仍**不提供** `myCPU/` 目录；
Chisel 版只给接口骨架：
`chisel/src/main/scala/exp12/student/MyCpuTop.scala` 中有 **12 处 `TODO(实现 n/12)`**，
其中 ⑪（CSR 寄存器与 CSR 指令）与 ⑫（`syscall` 例外 / `ertn` 返回）是本实验新增的两个核心模块。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp12/
├── 7.1.1实践任务12-添加系统调用异常支持.md   # 本文件
├── code/                                     # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill   # sources 引入 ../../chisel-common
    ├── src/main/scala/exp12/
    │   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocBramTop）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：12 处 TODO（⑪ CSR + ⑫ 例外）
    └── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**本实验的环境与 exp7~exp11 完全相同**（`soc_bram` 变体、同一套 SoC 装配与测试判据）。

#### 参考步骤

1. 实现 ⑪：CSR 寄存器堆（`CRMD`/`PRMD`/`ESTAT`/`ERA`/`EENTRY`/`SAVE0~3`）与
   `csrrd`/`csrwr`/`csrxchg`（`csr_num = inst[23:10]`，`csrxchg` 以 rj 为掩码）。
2. 实现 ⑫：`syscall` 触发例外（`Ecode = 0xB`、`ERA ← PC`、`ESTAT/CRMD/PRMD` 更新、
   `PC ← EENTRY`、流水线冲刷）与 `ertn` 返回（`PRMD` 恢复 PLV/IE、`PC ← ERA`）。
3. 运行测试：
   ```bash
   cd taskvscode/exp12/chisel
   ./mill chisel.test
   ```
4. 上板验证（Vivado）沿用原书流程，需 `code/soc_verify/soc_bram/run_vivado/` 下的原工程；
   无硬件实验平台可跳过。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `TraceHarness.run` 断言逐条一致 |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

> ⚠️ 未实测声明：`chisel/` 下的代码未在 JDK/Mill/Chisel 环境中编译或仿真过（本次为静态交付）。
> 逐行对照见 `chisel/MAPPING.md`；如实际运行报错，请以 `MAPPING.md` 与 `code/` 下的原 Verilog 为准排查。
