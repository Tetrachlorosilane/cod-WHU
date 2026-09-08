---
layout: default
nav_exclude: true
---

# exp21 Chisel 版实验环境（实践任务21：在 CPU 中集成 ICache）

## 本实验速览（TL;DR）

- **实验目标**：集成 ICache（原书实践任务21）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 10.2.2）

在实践任务19和实践任务20完成的基础上：

1. 将实践任务20完成的 Cache 模块作为 **ICache** 集成到实践任务19完成的 CPU 中；
2. 修改 CPU 中的 **AXI 转换桥，以支持 Burst 传输**；
3. 在 AXI SoC 验证环境里完成 exp21 对应 func（n1~n72）的功能验证。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp21/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：18 处 TODO（⑱ ICache + Burst）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp15~exp19 完全相同**（`soc_axi` 变体）；区别只在 CPU 内部新增 ICache 与 Burst。

## 3. 怎么做这个实验

1. 把 exp20 的 Cache 复制到本实验目录并改包名（或重新实现）：
   ```bash
   cp ../exp20/chisel/src/main/scala/exp20/student/Cache.scala \
      src/main/scala/exp21/student/Cache.scala
   # 然后把 `package exp20.student` 改为 `package exp21.student`
   ```
2. 在取指通路上例化 ICache；把 AXI 读请求改为 Burst（一次 `arlen=3` 取回整行 16B），
   用 `rlast` 判断结束；ICache miss 时停顿取指直到整行填充完成。
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

## 5. 与 exp19/exp20 的关系

环境沿用 exp19（AXI + TLB + 地址映射），Cache 模块沿用 exp20 的实现；
