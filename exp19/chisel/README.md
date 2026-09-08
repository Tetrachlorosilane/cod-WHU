---
layout: default
nav_exclude: true
---

# exp19 Chisel 版实验环境（实践任务19：添加 TLB 相关例外支持）

## 本实验速览（TL;DR）

- **实验目标**：TLB 例外与地址映射（原书实践任务19）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 9.2.3）

在实践任务18所实现 CPU 的基础上完成以下工作：

1. 为 CPU 增加 TLB 相关异常：TLB 重填例外、load/store/取指操作页无效例外、
   页修改例外、页特权等级不合规例外；
2. 在 CPU 中增加 `DMW` CSR 寄存器；
3. 为 CPU 增加虚实地址映射的功能；
4. 在采用 AXI 总线的 SoC 验证环境里完成 exp19 对应 func（n1~n72）的功能验证。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp19/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：17 处 TODO（⑰ 地址映射 + TLB 例外）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**环境与 exp15~exp18 完全相同**（`soc_axi` 变体）；区别只在 func（n1~n72）与
CPU 需要新增的地址映射/TLB 例外。

## 3. 怎么做这个实验

1. 沿用 exp18 的 TLB 与 TLB 指令实现；
2. 实现 ⑰：
   - 地址转换顺序：**DMW 直接映射优先**（按虚地址高位与 PLV 匹配）→ **TLB 查找**；
   - 例外：TLB 未命中 → TLBR；命中但 `V=0` → PIL（取指）/ PIS（load/store）；
     写操作但 `D=0` → PME；`PLV` 不满足 → PPI；
   - `ERA` 记录出错指令 PC，`BADV` 记录出错虚地址；注意流水线冲刷。
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

## 5. 与 exp18 的关系

环境、判据、TLB 指令与 CSR 完全相同；差异是新增 DMW、虚实地址映射与 5 类 TLB 例外。
