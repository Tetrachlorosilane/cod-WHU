---
layout: default
nav_exclude: true
---

### 4.3.1 实践任务5：5条指令单周期CPU（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

1. 阅读并理解 Chisel 实验环境中提供的代码，**补充代码中缺失的部分**，使设计可以通过仿真验证。

原 Verilog 版的空位是 `assign x = ;` 与 regfile 实例化里的空括号；**Chisel 版保留同样的填空意图**：
`chisel/src/main/scala/student/MinicpuTop.scala` 中留有 **9 处空位**，每处写作

```scala
// TODO(填空 n/9)：<原 Verilog 中的注释原文> @ code/miniCPU/minicpu_top.v:<行号>
xxx := ???
```

未补全时 elaboration 会以 `NotImplementedError` 终止 —— 这等价于原 Verilog 含空位时的语法错误：
不补全就跑不起来。参考解在 `chisel/solution/MinicpuTop.scala`。

#### 参考步骤

1. 打开 `chisel/src/main/scala/student/MinicpuTop.scala`，按 `TODO(填空 1/9 … 9/9)` 逐个补全。
   每处 TODO 都标注了它在原 Verilog 中的位置（`code/miniCPU/minicpu_top.v:行号`）与原注释原文，
2. 补全后运行 ChiselTest（已实测通过编译：Mill 1.0.4 + JDK 17 + Chisel 3.6.0；未跑仿真）：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
   或用参考解验证环境本身：`cp solution/MinicpuTop.scala src/main/scala/student/MinicpuTop.scala && ./mill chisel.test`
3. 测试程序仍是斐波那契程序（`code/func/start.S`，已内嵌为 `chisel/src/main/scala/soc/InstRam.scala`
   中的 `InstRamProgram`）：拨码开关 `switch` 的值相当于 `n`，`led` 输出 `f(n)`
   （`f(1)=1, f(2)=2, f(3)=3, f(4)=5, …`）。
   原实验靠人眼看波形/看 led，Chisel 版把它变成断言：
   - 原 `tb_top` 的固定激励 `switch = ~(8'h4)` ⇒ `n=4` ⇒ `f(4)=5` ⇒ `led = 0xfffa`；
   - 另对 `n = 1,2,3,4,5,6,8,10,12` 逐一断言 `led = ~f(n) & 0xffff`。
4. 上板验证（Vivado 综合/实现/生成比特流/下载）沿用原书流程，需用 `code/soc_verify/run_vivado/`
   下的原工程；无硬件实验平台可跳过。Chisel 版不覆盖上板流程。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| 仿真波形正确、`led` 显示 `f(n)` | `./mill chisel.test` 全部用例通过 |
| 修改 `tb_top` 的 `switch` 观察 `led` | 修改 `MinicpuTopSpec` 中的 `n` 列表即可 |
