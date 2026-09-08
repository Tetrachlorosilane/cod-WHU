---
layout: default
nav_exclude: true
---

# exp16 Chisel 版实验环境（实践任务16：完成 AXI 随机延迟验证）

## 本实验速览（TL;DR）

- **实验目标**：AXI 随机延迟验证（原书实践任务16）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 8.1.3）

在 exp15 的 AXI 接口 CPU 基础上，使其能在 **AXI 随机延迟**的响应下正确工作，
完成 exp16 对应 func（n1~n58）的功能验证与上板验证。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp16/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：15 处 TODO（⑮ 随机延迟正确性）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

## 3. 怎么做这个实验

1. 在 exp15 的 AXI 状态机上强化等待逻辑：
   - 请求被挡（`arready/awready/wready` 随机为低）时，请求信号必须保持不变；
   - 读数据/写响应延迟返回时，相关流水级保持停顿，不得提前推进；
   - 多个在途请求时按 ID 匹配返回数据。
2. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
3. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_axi/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp15 的关系

**SoC 与测试平台完全相同**（`soc_axi` 变体、同一套 AXI 模型与判据）；RTL 也相同——
原实验的 exp15/exp16 只差 func 程序（`_RUN_PERF_TEST` 与 `ifdef RUN_PERF_TEST` 的写法
