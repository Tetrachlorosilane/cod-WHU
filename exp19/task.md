---
layout: default
nav_exclude: true
---

### 9.2.3 实践任务19：添加TLB相关例外支持（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

在实践任务18所实现 CPU 的基础上完成以下工作：

1. 为 CPU 增加 TLB 相关异常：TLB 重填例外、load/store/取指操作页无效例外、
   页修改例外、页特权等级不合规例外；
2. 在 CPU 中增加 `DMW` CSR 寄存器；
3. 为 CPU 增加虚实地址映射的功能；
4. 在采用 AXI 总线的 SoC 验证环境里完成 exp19 对应 func（n1~n72）的功能验证。

#### 参考步骤

1. 沿用 exp18 的 TLB 与 TLB 指令实现（把 `Tlb.scala` 复制过来并改包名）。
2. 实现 ⑰：
   - 地址转换顺序：**DMW 直接映射优先**（按虚地址高位与当前 PLV 匹配）→ **TLB 查找**；
   - 例外：TLB 未命中 → TLBR；命中但 `V=0` → PIL（取指）/ PIS（load/store）；
     写操作但 `D=0` → PME；`PLV` 不满足 → PPI；
   - `ERA` 记录出错指令 PC、`BADV` 记录出错虚地址；注意流水线冲刷与例外优先级。
3. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
4. 上板验证（Vivado）沿用原书流程，需 `code/soc_verify/soc_axi/run_vivado/` 下的原工程；
   无硬件实验平台可跳过。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| `mycpu_tb.v` 每个写回与 `gettrace/golden_trace.txt` 逐条比对 | `TraceHarness.run` 断言逐条一致 |
| 到达 `debug_wb_pc == 32'h1c000100` 打印 `----PASS!!!` | 到达 `END_PC` 后打印 `----PASS!!!` |
| `num_monitor` 监视 `confreg.num_data` | `TraceHarness` 同样监视 |
