---
layout: default
nav_exclude: true
---

### 8.1.2 实践任务15：添加AXI总线支持（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

在实践任务14实现的 CPU 基础上完成以下工作：

1. 将 CPU 顶层接口修改为 **AXI 总线接口**。CPU 对外只有一个 AXI 接口，需在内部完成
   取指和数据访问的仲裁（推荐做法：实现一个类 SRAM-AXI 的 2×1 转接桥，
   把 exp14 的类 SRAM 接口 CPU 封装成 AXI 接口）。
2. 在采用 AXI 总线的 SoC 验证环境里完成 exp15 对应 func（n1~n58）的**固定延迟**功能验证。

#### 参考步骤

1. 实现 AXI 读写通道：AR/R 取指、AW/W/B 访存。注意 `arlen=0` 为单拍传输、
   `wlast` 标记写数据结束、`bvalid` 为写响应；读写通道彼此独立。
2. 实现 ⑮ 的互锁：AR 已发但 R 未回、或写未收到 B 响应时，相关流水级必须停顿。
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
