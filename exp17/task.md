---
layout: default
nav_exclude: true
---

### 9.2.1 实践任务17：TLB模块设计（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

1. 设计 TLB 模块（模块名 `tlb`，接口见原书 9.1 节）。
2. 利用 TLB 模块级验证环境对所设计的 TLB 进行验证，通过仿真和上板验证。

#### 参考步骤

1. 实现 `Tlb` 的四部分：① 表项存储；② 写端口；③ 读端口；④ 双查找端口。
   查找需考虑 `G`（全局）位、`va_bit12`（选择页表项 0/1）与 `ps`（页大小）。
2. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
3. 上板验证（Vivado）沿用原书流程，需 `code/run_vivado/` 下的原工程；
   正确的上板现象见原书 9.2.1.2（数码管最终显示 0x19180f0f）。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| 仿真完成 16 次写、16 次读、26 次查，全部无误后打印 `----PASS!!!` | `TlbSpec` 等待 `wOk && rOk && sOk` 全部置位并断言 `err == 0`，打印 `----PASS!!!` |
| 读测试逐字段比对 | `TlbTop` 的 `r_error`（与原文件一致） |
| 查找测试按期望表比对 | `TlbTop` 的 `s0_error` / `s1_error`（与原文件一致） |
