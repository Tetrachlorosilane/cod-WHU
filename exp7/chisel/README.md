---
layout: default
nav_exclude: true
---

# exp7 Chisel 版实验环境（实践任务7：不考虑相关冲突处理的简单流水线 CPU）

## 本实验速览（TL;DR）

- **实验目标**：简单流水线 CPU（不考虑相关冲突）（原书实践任务7）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 5.1.1）

在实践任务6（单周期 CPU）的基础上：

1. 调整 CPU 顶层接口，增加指令 RAM 片选 `inst_sram_en` 与数据 RAM 片选 `data_sram_en`；
2. 把 `inst_sram_we` / `data_sram_we` 从 1 位写使能改为 **4 位字节写使能**；
3. 设计一个**不考虑相关冲突**的单发射五级流水 CPU；
4. 运行 exp7 对应的 func，通过仿真验证（`gettrace/golden_trace.txt` 逐条比对）与上板验证。

## 2. 文件清单

```text
chisel/
├── README.md                       # 本文件
├── build.mill                      # sources 引入 ../../chisel-common
├── src/main/scala/exp7/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（装配在共享库 envlib.soc.SocBramTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：从零实现（9 处 TODO）
└── src/test/scala/MyCpuTbSpec.scala  # golden_trace 逐条比对 + 功能测试点监视
```

实验环境的公共模块集中在 `../../chisel-common/src/main/scala/envlib/`：
`common/{Decoders,Regfile}.scala`、`soc/{CpuIO,Bridge1x2,Confreg,SyncRam}.scala`
（`CpuIO`/`LACpu` = 学生 CPU 的统一接口，`SocBramTop` = SoC 装配）；
测试工具 `src/test/scala/envlib/test/Loaders.scala`（`MifLoader` / `TraceLoader`）。
自 exp7 起采用"共享环境库 + 每实验自有学生代码/薄 SoC 封装/测试/文档"的布局，见

## 3. 怎么做这个实验

1. 通读 `src/main/scala/exp7/student/MyCpuTop.scala` 的骨架注释（IF→ID→EX→MEM→WB 九步）。
2. 参考 `../code/` 下 exp6 的 `myCPU/`（单周期版本，注意它含错误）与 exp7 的 SoC 环境，
3. 逐条实现 `TODO(实现 1/9 … 9/9)`，直到能 elaborate 并跑通测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
4. 本实验**不要求**处理数据相关/控制相关引发的冲突（那是 exp8/exp9）；
   func 程序中的相关指令之间已插入 NOP，因此流水线即使"裸奔"也能跑通。
5. 上板流程（Vivado）沿用原书步骤，需 `../code/soc_verify/soc_bram/run_vivado/` 下的原工程。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 比对 | `MyCpuTbSpec` 逐条比对 `debug_wb_pc / wnum / wdata`（字节使能掩码） |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后断言通过并打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | 同样监视 `dut.u_confreg.io.num_data` |
| `$readmemb` 读 `func/obj/*.mif` | 测试侧 `MifLoader` 读入并注入 `SyncRam`（block RAM 行为模型） |

## 5. 与 Verilog 版的结构性差异

1. `clk` → Chisel 隐式 clock；`resetn` 保留为显式 IO。
2. `inst_ram` / `data_ram`（block RAM，同步读）→ `envlib.soc.SyncRam`（字节写 + 同步读，
   初始化由构造参数注入）。
3. `soc_lite_top` 的 `debug_wb_*` 内部线网 → 测试直接访问 `soc.cpu.io.debug_wb_*`。
4. `confreg` 的 `timer` 原用独立 `timer_clk`；本实验 `timer_clk = cpu_clk`，合并为同一时钟域。
5. `.xci/.xdc/.tcl/.xpr` 等 Vivado 资产不改写。
