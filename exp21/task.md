---
layout: default
nav_exclude: true
---

### 10.2.2 实践任务21：在CPU中集成ICache（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/10.2.2实践任务21-在CPU中集成ICache.md`](../../CPU设计实战：LoongArch版/10.2.2实践任务21-在CPU中集成ICache.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现 + ICache 集成）

在实践任务19和实践任务20完成的基础上完成以下工作：

1. 将实践任务20完成的 Cache 模块作为 **ICache** 集成到实践任务19完成的 CPU 中；
2. 修改 CPU 中的 **AXI 转换桥，以支持 Burst 传输**；
3. 在采用 AXI 总线的 SoC 验证环境里完成 exp21 对应 func（n1~n72）的功能验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：原实验环境仍**不提供** `myCPU/` 目录；
Chisel 版只给接口骨架：
`chisel/src/main/scala/exp21/student/MyCpuTop.scala` 中有 **18 处 `TODO(实现 n/18)`**，
第 ⑱ 项就是本实验的核心（ICache 例化 + Burst 读填充 + 取指停顿）。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp21/
├── 10.2.2实践任务21-在CPU中集成ICache.md   # 本文件
├── code/                                     # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill   # sources 引入 ../../chisel-common
    ├── src/main/scala/exp21/
    │   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：18 处 TODO（⑱ ICache + Burst）
    └── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**本实验的环境与 exp15~exp19 完全相同**（`soc_axi` 变体、同一套 AXI 模型与判据）。

#### 参考步骤

1. 把 exp20 的 Cache 复制到本实验目录并改包名（或重新实现）：
   ```bash
   cp ../exp20/chisel/src/main/scala/exp20/student/Cache.scala \
      src/main/scala/exp21/student/Cache.scala
   # 然后把 `package exp20.student` 改为 `package exp21.student`
   ```
2. 在取指通路上例化 ICache（CPU 侧 `valid/op=0/index/tag/offset`），
   把 AXI 读改为 Burst（一次 `arlen=3` 取回整行 16B），用 `rlast` 判断结束；
   ICache miss 时停顿取指直到整行填充完成。
3. 运行测试：
   ```bash
   cd taskvscode/exp21/chisel
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

> ✅ 编译验证：`chisel/` 下的代码已用 Mill 1.0.4 + JDK 17 + Chisel 3.5.6 实测通过
> `chisel.compile` 与 `chisel.test.compile`（**未跑仿真**）。复查报告见仓库根 `verify/REPORT.md`；
> 逐行对照见 `chisel/MAPPING.md`。
