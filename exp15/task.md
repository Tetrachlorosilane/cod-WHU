---
layout: default
nav_exclude: true
---

### 8.1.2 实践任务15：添加AXI总线支持（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/8.1.2实践任务15-添加AXI总线支持.md`](../../CPU设计实战：LoongArch版/8.1.2实践任务15-添加AXI总线支持.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现 + AXI 总线）

在实践任务14实现的 CPU 基础上完成以下工作：

1. 将 CPU 顶层接口修改为 **AXI 总线接口**。CPU 对外只有一个 AXI 接口，需在内部完成
   取指和数据访问的仲裁（推荐做法：实现一个类 SRAM-AXI 的 2×1 转接桥，
   把 exp14 的类 SRAM 接口 CPU 封装成 AXI 接口）。
2. 在采用 AXI 总线的 SoC 验证环境里完成 exp15 对应 func（n1~n58）的**固定延迟**功能验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：原实验环境仍**不提供** `myCPU/` 目录；
Chisel 版只给接口骨架：
`chisel/src/main/scala/exp15/student/MyCpuTop.scala` 中有 **15 处 `TODO(实现 n/15)`**，
⑭（AXI 事务状态机与 ID 管理）与 ⑮（未完成请求导致的流水线互锁）是本实验新增的核心。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp15/
├── 8.1.2实践任务15-添加AXI总线支持.md   # 本文件
├── code/                                 # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill   # sources 引入 ../../chisel-common
    ├── src/main/scala/exp15/
    │   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：15 处 TODO（⑭ AXI 状态机 + ⑮ 互锁）
    └── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境变化**：CPU 对外接口由握手式类 SRAM 改为 AXI4（`ar/r/aw/w/b` 五通道）；
SoC 侧由 `axi_crossbar_1x2` 按地址分发给 `axi_wrap_ram → axi_ram` 与 `confreg`（AXI 从机）；
`confreg` 产生的 `ram_random_mask` 送给 `axi_wrap_ram` 制造随机延迟。
Xilinx IP（`axi_crossbar_1x2`、`axi_ram`）在 Chisel 版中用行为模型替代（见 `chisel/MAPPING.md` §5）。

#### 参考步骤

1. 实现 AXI 读写通道：AR/R 取指、AW/W/B 访存。注意 `arlen=0` 为单拍传输、
   `wlast` 标记写数据结束、`bvalid` 为写响应；读写通道彼此独立。
2. 实现 ⑮ 的互锁：AR 已发但 R 未回、或写未收到 B 响应时，相关流水级必须停顿。
3. 运行测试：
   ```bash
   cd taskvscode/exp15/chisel
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
