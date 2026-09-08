### 9.2.3 实践任务19：添加TLB相关例外支持（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/9.2.3实践任务19-添加TLB相关例外支持.md`](../../CPU设计实战：LoongArch版/9.2.3实践任务19-添加TLB相关例外支持.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现 + 地址映射/TLB 例外）

在实践任务18所实现 CPU 的基础上完成以下工作：

1. 为 CPU 增加 TLB 相关异常：TLB 重填例外、load/store/取指操作页无效例外、
   页修改例外、页特权等级不合规例外；
2. 在 CPU 中增加 `DMW` CSR 寄存器；
3. 为 CPU 增加虚实地址映射的功能；
4. 在采用 AXI 总线的 SoC 验证环境里完成 exp19 对应 func（n1~n72）的功能验证。

**Chisel 版保留的教学意图 = 从零实现（增量）**：原实验环境仍**不提供** `myCPU/` 目录；
Chisel 版只给接口骨架：
`chisel/src/main/scala/exp19/student/MyCpuTop.scala` 中有 **17 处 `TODO(实现 n/17)`**，
第 ⑰ 项就是本实验的核心（DMW + TLB 地址转换 + TLB 例外）。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp19/
├── 9.2.3实践任务19-添加TLB相关例外支持.md   # 本文件
├── code/                                     # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill   # sources 引入 ../../chisel-common
    ├── src/main/scala/exp19/
    │   ├── soc/SocLiteTop.scala        # SoC 薄封装（共享库 envlib.soc.SocAxiTop）
    │   └── student/MyCpuTop.scala      # ★ 学生模块：17 处 TODO（⑰ 地址映射 + TLB 例外）
    └── src/test/scala/MyCpuTbSpec.scala  # 复用 envlib.test.TraceHarness
```

**本实验的环境与 exp15~exp18 完全相同**（`soc_axi` 变体、同一套 AXI 模型与判据）。

#### 参考步骤

1. 沿用 exp18 的 TLB 与 TLB 指令实现（把 `Tlb.scala` 复制过来并改包名）。
2. 实现 ⑰：
   - 地址转换顺序：**DMW 直接映射优先**（按虚地址高位与当前 PLV 匹配）→ **TLB 查找**；
   - 例外：TLB 未命中 → TLBR；命中但 `V=0` → PIL（取指）/ PIS（load/store）；
     写操作但 `D=0` → PME；`PLV` 不满足 → PPI；
   - `ERA` 记录出错指令 PC、`BADV` 记录出错虚地址；注意流水线冲刷与例外优先级。
3. 运行测试：
   ```bash
   cd taskvscode/exp19/chisel
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

> ⚠️ 未实测声明：`chisel/` 下的代码未在 JDK/Mill/Chisel 环境中编译或仿真过（本次为静态交付）。
> 逐行对照见 `chisel/MAPPING.md`；如实际运行报错，请以 `MAPPING.md` 与 `code/` 下的原 Verilog 为准排查。
