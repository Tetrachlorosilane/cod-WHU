---
layout: default
nav_exclude: true
---

# exp6 Chisel 版实验环境（实践任务6：20 条指令单周期 CPU）

## 本实验速览（TL;DR）

- **实验目标**：20 条指令单周期 CPU（原书实践任务6）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。
- **共享依赖**：自包含（本目录即完整环境）。

## 1. 本实验要求（原书 4.3.2）

阅读并理解 `myCPU/` 目录下提供的代码，**通过仿真波形调试修复其中被故意加入的错误**，
使设计通过仿真验证（`gettrace/golden_trace.txt` 逐条比对）与上板验证。

运行测试会失败（trace 比对报错），这正是起点：修好全部 7 处后应打印 `----PASS!!!`。

## 2. 文件清单

```text
chisel/
├── README.md                       # 本文件
├── build.mill
├── src/main/scala/
│   ├── common/
│   │   ├── Decoders.scala          # decoder_2_4/4_16/5_32/6_64（端口名 out）
│   │   └── Regfile.scala           # 寄存器堆（本身无 bug）
│   ├── soc/
│   │   ├── AsyncRam.scala          # async_ram / inst_ram / data_ram 行为模型
│   │   ├── Bridge1x2.scala         # 1×2 桥（cpu_data → data_ram / confreg）
│   │   ├── Confreg.scala           # 板级外设（led/数码管/按键/timer/串口/仿真标志）
│   │   └── SocLiteTop.scala        # SoC 顶层
│   └── student/
│       ├── Alu.scala               # ★ 含 4 处错误
│       └── MyCpuTop.scala          # ★ 含 3 处错误
├── src/test/scala/
│   └── MyCpuTbSpec.scala           # 复现原 tb_top 的 golden_trace 比对 + 功能测试点监视
└── solution/
    ├── Alu.scala                   # 参考解（修正 #1'~#4）
    └── MyCpuTop.scala              # 参考解（修正 #5~#7）
```

## 3. 怎么做这个实验

2. 运行测试观察失败信息（trace 第一条不一致的位置就是线索）：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
3. 逐处修复 `TODO(找错 #n)` 标记的错误，直到测试通过。
4. 用参考解验证环境本身：
   ```bash
   cp solution/*.scala src/main/scala/student/ && ./mill chisel.test
   ```
5. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_dram/run_vivado/` 下的原工程；
   Chisel 版不覆盖上板流程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对（9776 条） | `MyCpuTbSpec` 逐条比对 `debug_wb_pc / wnum / wdata`（按字节写使能掩码） |
| `debug_wb_pc == 32'h1c000100` 时结束并打印 `----PASS!!!` | 到达 `END_PC` 后断言通过并打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` 变化（低 8 位 +1、高 8 位 +1） | 同样监视 `dut.u_confreg.io.num_data`，异常计数非 0 即失败 |
| `$readmemb` 读 `func/obj/*.mif` 初始化 RAM | 测试侧 `MifLoader` 读取同一 `.mif`，经构造参数注入 RAM 模型 |

## 5. 与 Verilog 版的结构性差异

1. `clk` → Chisel 隐式 clock；`resetn` 保留为显式 IO，同步复位手写（规范 §4.3）。
2. `soc_lite_top` 的 `debug_wb_*` 内部线网 → 测试直接访问 `soc.cpu.io.debug_wb_*`。
3. `confreg` 的 `timer` 原用独立 `timer_clk`；本实验 `SIMU_USE_PLL=0 ⇒ timer_clk = cpu_clk`，
   Chisel 版合并为同一时钟域。
4. RAM 深度按程序长度取 2 的幂；`.xci/.xdc/.tcl/.xpr` 等 Vivado 资产不改写。
5. 原 `async_ram` 写周期读数据输出 `Z`；Chisel 无三态，取 0（写周期 CPU 不读数据）。
