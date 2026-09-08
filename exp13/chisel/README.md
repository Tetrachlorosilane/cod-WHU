---
layout: default
nav_exclude: true
---

# exp13 Chisel 版实验环境（实践任务13：添加其它异常支持）

## 本实验速览（TL;DR）

- **实验目标**：其它例外与中断支持（原书实践任务13）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 7.1.2）

在实践任务12实现的 CPU 基础上完成以下工作：

1. 增加取指地址错（ADEF）、地址非对齐（ALE）、断点（BRK）和指令不存在（INE）异常的支持；
2. 增加中断支持，包括 2 个软件中断、8 个硬件中断和定时器中断；
3. 增加控制状态寄存器 `ECFG`、`BADV`、`TID`、`TCFG`、`TVAL`、`TICLR`；
4. 增加 `rdcntvl.w`、`rdcntvh.w` 和 `rdcntid` 指令；
5. 运行 exp13 对应的 func（n1~n58），通过仿真验证与上板验证。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp13/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocBramTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：14 处 TODO（⑬ 例外 + ⑭ 中断/定时器）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp7~exp12 完全相同**；区别只在 func（n1~n58）与 CPU 需要新增的例外/中断支持。

## 3. 怎么做这个实验

1. 实现 ⑬：在取指（ADEF）、译码（INE/BRK）、访存（ALE）各环节检测异常条件，
   写入 `ESTAT.Ecode`、`ERA`、`BADV`，并按例外交付流程跳转 `EENTRY`；
2. 实现 ⑭：`ECFG.LIE` 与 `ESTAT.IS` 按位使能的中断仲裁；`TCFG/TVAL/TID` 定时器
   （`TVAL` 递减到 0 触发 TI，`TICLR` 写 1 清除）；`rdcntvl.w`/`rdcntvh.w`/`rdcntid`；
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

## 5. 与 exp12 的关系

环境、测试平台、判据完全一致；学生任务的差异是新增 4 类例外、中断/定时器与 3 条计数器指令。
