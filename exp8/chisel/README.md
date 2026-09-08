---
layout: default
nav_exclude: true
---

# exp8 Chisel 版实验环境（实践任务8：阻塞技术解决相关引发的冲突）

## 本实验速览（TL;DR）

- **实验目标**：阻塞技术解决相关引发的冲突（原书实践任务8）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 5.1.2）

在实践任务7实现的 CPU 基础上：

1. 加入适当的逻辑处理**寄存器写后读（RAW）数据相关**引发的流水线冲突
   （本任务**只要求使用阻塞技术**，前递留到 exp9）；
2. 运行 exp8 对应的 func，通过仿真验证与上板验证。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp8/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（装配在共享库 envlib.soc.SocBramTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：10 处 TODO（含 ⑩ 阻塞）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**与 exp7 的环境完全相同**（`soc_bram` 变体、同一份 `sync_ram` 模型、同一套 SoC 装配），
唯一的区别是 **func 程序**：exp8 的相关指令之间不再插入 NOP，因此必须靠阻塞解决冲突。

## 3. 怎么做这个实验

1. 先把 exp7 的流水线做出来（或直接在其基础上继续）。
2. 实现第 ⑩ 项：比较 ID 级要读的 `rj/rk` 与 EX/MEM 级尚未写回的 `rd`；
   相关时冻结 `pc` 与 IF/ID、把 ID/EX 清成气泡。
3. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
4. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_bram/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp7 的关系

环境、测试平台、判据完全一致；学生任务的差异只有第 ⑩ 项（阻塞）。
