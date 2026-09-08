---
layout: default
nav_exclude: true
---

# exp22 Chisel 版实验环境（实践任务22：CPU 中集成 DCache）

## 本实验速览（TL;DR）

- **实验目标**：集成 DCache（原书实践任务22）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 10.2.3）

在实践任务21完成的基础上：

1. 将实践任务20完成的 Cache 模块作为 **DCache** 集成到实践任务21完成的 CPU 中；
2. 在 AXI SoC 验证环境里完成 exp22 对应 func（n1~n72）的功能验证。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp22/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：19 处 TODO（⑲ DCache）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp15~exp21 完全相同**（`soc_axi` 变体）；区别只在 CPU 内部新增 DCache。

## 3. 怎么做这个实验

1. 把 exp20 的 Cache 复制到本实验目录并改包名（或重新实现）：
   ```bash
   cp ../exp20/chisel/src/main/scala/exp20/student/Cache.scala \
      src/main/scala/exp22/student/Cache.scala
   # 然后把 `package exp20.student` 改为 `package exp22.student`
   ```
2. 在访存通路上例化 DCache：
   - 写命中：按 `wstrb` 更新行内数据并置脏；
   - 写 miss：先取行（写分配）再写；
   - 替换脏行：通过 `wr_req`/`wr_data`（128 位整行）写回；
   - 访存未完成时冻结 MEM/WB，并与前递/例外/分支冲刷协调。
3. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
4. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_axi/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `TraceHarness.run` 逐条比对（含字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |

## 5. 与 exp20/exp21 的关系

Cache 模块沿用 exp20 的实现；取指通路沿用 exp21 的 ICache；
本实验把同一个 Cache 模块再实例化为 DCache 接入访存通路。
