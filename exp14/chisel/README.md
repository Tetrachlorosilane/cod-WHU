---
layout: default
nav_exclude: true
---

# exp14 Chisel 版实验环境（实践任务14：添加类 SRAM 总线支持）

## 本实验速览（TL;DR）

- **实验目标**：类 SRAM 握手总线（原书实践任务14）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 8.1.1）

把 CPU 的访存接口改为**握手式类 SRAM 总线**，并运行 exp14 对应的 func（n1~n58），
通过仿真验证与上板验证：

- 请求侧：`req`、`wr`、`size[1:0]`、`wstrb[3:0]`、`addr`、`wdata`；
- 响应侧：`addr_ok`（请求已被接收）、`data_ok`（读数据有效）、`rdata`。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp14/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocHsBramTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：14 处 TODO（⑭ 握手互锁）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境变化**（相比 exp7~exp13）：CPU 接口由普通 SRAM 接口改为握手式；SoC 增加
`sram_wrap`（含深度 4 读数据缓冲）与握手版 `bridge_1x2`（在途请求计数深度 15）。

## 3. 怎么做这个实验

1. 把取指/访存改为握手请求：`req && addr_ok` 表示请求被接收；读请求的 `data_ok`
   可能晚若干拍返回，需冻结相关流水级（互锁）。
2. 注意与分支冲刷、例外冲刷的优先级。
3. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
4. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_hs_bram/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp13 的关系

判据与测试平台一致；差异是访存接口（普通 → 握手）与 SoC 中的 `sram_wrap`/握手 bridge。
