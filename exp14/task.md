---
layout: default
nav_exclude: true
---

### 8.1.1 实践任务14：添加类SRAM总线支持（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/8.1.1实践任务14-添加类SRAM总线支持.md`](../../CPU设计实战：LoongArch版/8.1.1实践任务14-添加类SRAM总线支持.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现 + 握手总线）

在实践任务13实现的 CPU 基础上完成以下工作：

1. 将 CPU 对外接口修改为**类 SRAM 总线接口**（握手式）。
2. 在采用握手机制的 block RAM 的 SoC 验证环境中完成 exp14 对应 func 的功能验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：原实验环境仍**不提供** `myCPU/` 目录；
Chisel 版只给接口骨架：
`chisel/src/main/scala/exp14/student/MyCpuTop.scala` 中有 **14 处 `TODO(实现 n/14)`**，
第 ⑭ 项就是本实验的核心（握手总线导致的流水线互锁）。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp14/
├── 8.1.1实践任务14-添加类SRAM总线支持.md   # 本文件
├── code/                                   # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill # sources 引入 ../../chisel-common
    ├── src/main/scala/exp14/
    │   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocHsBramTop）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：14 处 TODO（⑭ 握手互锁）
    └── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境变化**：CPU 访存接口由 `en/we/addr/wdata/rdata` 改为
`req/wr/size/wstrb/addr/wdata` + `addr_ok/data_ok/rdata`；
SoC 增加 `sram_wrap`（握手转换 + 深度 4 读数据缓冲）与握手版 `bridge_1x2`
（在途请求计数深度 15）；`confreg` 增加握手适配（`conf_req/conf_addr_ok/conf_data_ok`）。

#### 参考步骤

1. 把取指/访存改为握手请求：`req && addr_ok` 表示请求被接收，此后请求不可撤销；
   读请求的 `data_ok` 可能晚若干拍返回，需冻结相关流水级（互锁）。
2. 注意握手停顿与分支冲刷、例外冲刷的优先级关系。
3. 运行测试：
   ```bash
   cd taskvscode/exp14/chisel
   ./mill chisel.test
   ```
4. 上板验证（Vivado）沿用原书流程，需 `code/soc_verify/soc_hs_bram/run_vivado/` 下的原工程；
   无硬件实验平台可跳过。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `TraceHarness.run` 断言逐条一致 |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

> ✅ 编译验证：`chisel/` 下的代码已用 Mill 1.0.4 + JDK 17 + Chisel 3.5.6 实测通过
> `chisel.compile` 与 `chisel.test.compile`（**未跑仿真**）。复查报告见仓库根 `verify/REPORT.md`；
> 逐行对照见 `chisel/MAPPING.md`。
