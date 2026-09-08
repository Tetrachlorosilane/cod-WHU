---
layout: default
nav_exclude: true
---

### 10.2.2 实践任务21：在CPU中集成ICache（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

在实践任务19和实践任务20完成的基础上完成以下工作：

1. 将实践任务20完成的 Cache 模块作为 **ICache** 集成到实践任务19完成的 CPU 中；
2. 修改 CPU 中的 **AXI 转换桥，以支持 Burst 传输**；
3. 在采用 AXI 总线的 SoC 验证环境里完成 exp21 对应 func（n1~n72）的功能验证。

#### 参考步骤

1. 把 exp20 的 Cache 复制到本实验目录并改包名（或重新实现）：
   ```bash
   cp ../exp20/chisel/src/main/scala/exp20/student/Cache.scala \
      src/main/scala/exp21/student/Cache.scala
   # 然后把 `package exp20.student` 改为 `package exp21.student`
   ```
2. 在取指通路上例化 ICache（CPU 侧 `valid/op=0/index/tag/offset`），
   把 AXI 读改为 Burst（一次 `arlen=3` 取回整行 16B），用 `rlast` 判断结束；
   ICache miss 时停顿取指直到整行填充完成。
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
