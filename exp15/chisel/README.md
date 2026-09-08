---
layout: default
nav_exclude: true
---

# exp15 Chisel 版实验环境（实践任务15：添加 AXI 总线支持）

## 本实验速览（TL;DR）

- **实验目标**：AXI 总线支持（固定延迟）（原书实践任务15）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：TraceHarness：golden_trace 逐条比对 + num_data 监视。

## 1. 本实验要求（原书 8.1.2）

把 CPU 的对外接口改为 **AXI4 总线**（CPU 直接产生 ar/r/aw/w/b 五通道），
在 AXI 验证环境中完成 exp15 对应 func（n1~n58）的功能验证。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp15/
│   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
│   └── student/MyCpuTop.scala      # ★ 学生模块：15 处 TODO（⑭ AXI 状态机 + ⑮ 互锁）
└── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**共享库新增**（本实验首次使用）：
`CpuIOAxi`/`LACpuAxi`（AXI 版 CPU 接口）、`AxiCrossbar1x2`（地址选路）、
`AxiWrapRam`（延迟掩码 + 地址重映射）、`AxiRamSlave`（AXI RAM 行为模型）、
`AxiConfregWrap`（AXI 从机前端 + 复用 `Confreg` 寄存器逻辑 + 随机掩码生成）。

## 3. 怎么做这个实验

1. 实现 AXI 读写通道：AR/R 取指、AW/W/B 访存；注意 `arlen=0` 为单拍传输、
   `wlast` 标记写数据结束、`bvalid` 为写响应。
2. 实现 ⑮ 的互锁：AR 已发但 R 未回、写未收到 B 响应时，相关流水级必须停顿。
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

## 5. 与 exp14 的关系

判据与测试平台一致；差异是 CPU 对外接口由"握手式类 SRAM"改为 AXI4，
SoC 侧换成交叉开关 + `axi_wrap_ram` + AXI RAM 模型 + AXI confreg。
