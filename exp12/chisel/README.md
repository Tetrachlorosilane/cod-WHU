---
layout: default
nav_exclude: true
---

# exp12 Chisel 版实验环境（实践任务12：添加系统调用异常支持）

## 本实验速览（TL;DR）

- **实验目标**：syscall 例外支持（原书实践任务12）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 7.1.1）

在实践任务11实现的 CPU 基础上完成以下工作：

1. 为 CPU 增加 `csrrd`、`csrwr`、`csrxchg` 和 `ertn` 指令；
2. 为 CPU 增加控制状态寄存器 `CRMD`、`PRMD`、`ESTAT`、`ERA`、`EENTRY`、`SAVE0~3`；
3. 为 CPU 增加 `syscall` 指令，实现系统调用异常支持；
4. 运行 exp12 对应的 func（n1~n47），通过仿真验证与上板验证。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp12/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocBramTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：12 处 TODO（⑪ CSR + ⑫ syscall/ertn）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp7~exp11 完全相同**；区别只在 func（n1~n47）与 CPU 需要新增的 CSR/例外支持。

## 3. 怎么做这个实验

1. 实现 ⑪：CSR 寄存器堆 + `csrrd`/`csrwr`/`csrxchg`（`csr_num = inst[23:10]`，
   `csrxchg` 用 rj 作为掩码）；
2. 实现 ⑫：`syscall` 触发例外（`Ecode = 0xB`、`ERA ← PC`、`ESTAT/CRMD/PRMD` 更新、
   `PC ← EENTRY`、流水线冲刷）与 `ertn` 返回（从 `PRMD` 恢复 PLV/IE、`PC ← ERA`）；
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

## 5. 与 exp11 的关系

环境、测试平台、判据完全一致；学生任务的差异是新增 4 条 CSR 指令 + `syscall` 例外 + `ertn`。
