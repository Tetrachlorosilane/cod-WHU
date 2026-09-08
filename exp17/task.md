### 9.2.1 实践任务17：TLB模块设计（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/9.2.1实践任务17-TLB模块设计.md`](../../CPU设计实战：LoongArch版/9.2.1实践任务17-TLB模块设计.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现）

1. 设计 TLB 模块（模块名 `tlb`，接口见原书 9.1 节）。
2. 利用 TLB 模块级验证环境对所设计的 TLB 进行验证，通过仿真和上板验证。

**Chisel 版保留的教学意图 = 从零实现**：原实验环境只提供验证环境（`tlb_top.v` +
`testbench.v`），**不提供 TLB 本体**；Chisel 版同样只给接口骨架：
`chisel/src/main/scala/exp17/student/Tlb.scala` 中有 **4 处 `TODO(实现 n/4)`**，
内部逻辑全部留空（`???`）；未实现时 elaboration 以 `NotImplementedError` 终止。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp17/
├── 9.2.1实践任务17-TLB模块设计.md   # 本文件
├── code/                             # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill
    ├── src/main/scala/exp17/
    │   ├── soc/TlbTop.scala          # 模块级验证环境（对应 tlb_top.v）
    │   └── student/Tlb.scala         # ★ 学生模块：TLB 骨架（4 处 TODO）
    └── src/test/scala/TlbSpec.scala  # 复现"16 写 / 16 读 / 26 查 → PASS"
```

**本实验是模块级独立验证环境**（不涉及 CPU/SoC），因此不使用 `chisel-common`。

#### 参考步骤

1. 实现 `Tlb` 的四部分：① 表项存储；② 写端口；③ 读端口；④ 双查找端口。
   查找需考虑 `G`（全局）位、`va_bit12`（选择页表项 0/1）与 `ps`（页大小）。
2. 运行测试：
   ```bash
   cd taskvscode/exp17/chisel
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

> ⚠️ 未实测声明：`chisel/` 下的代码未在 JDK/Mill/Chisel 环境中编译或仿真过（本次为静态交付）。
> 逐行对照见 `chisel/MAPPING.md`；如实际运行报错，请以 `MAPPING.md` 与 `code/` 下的原 Verilog 为准排查。
