---
layout: default
title: 实践任务17 · TLB 模块设计
nav_title: 实践任务17 TLB 模块设计
nav_order: 17
---

# 实践任务17：TLB 模块设计

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](chisel/README.md)

> **任务类型**：从零实现（模块级） ｜ **待操作代码**：4 处

## 实验目标

1. 设计 TLB 模块（模块名 `tlb`，接口见原书 9.1 节）。
2. 利用 TLB 模块级验证环境对所设计的 TLB 进行验证，通过仿真和上板验证。

## 关键代码

### `Tlb.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp17/student/Tlb.scala](chisel/src/main/scala/exp17/student/Tlb.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现）：TLB —— 16 项、双查找端口 + 读写端口
//   对应原实验：本实验要求自行编写 `tlb.v`，模块名固定为 `tlb`
//   （原实验环境只提供 tlb_top.v 验证环境，不提供 TLB 本体）。
//
// 本实验（实践任务17）的要求：从零实现：
//   Chisel 版只给接口骨架 + TODO(实现)，内部逻辑全部留空（???）。
//   未实现时 elaboration 会以 NotImplementedError 终止，这是预期行为。
//
// 接口（与原书 9.1 节 TLB 模块接口一致）：
//   查找端口 0/1：输入 {vppn, va_bit12, asid}，输出 {found, index, ppn, ps, plv, mat, d, v}
//   写端口：we 有效时按 w_index 写入整个表项（含两个页表项 ppn0/1 及其属性）
//   读端口：r_e 有效时按 r_index 读出整个表项
//   invtlb：本实验不使用（验证环境固定接 0）
//
// 实现提示（见原书 9.1）：
//   * 表项可用 Vec of Bundle（或若干并列的 Reg(Vec(...))）表达；
//   * 查找：把 16 个表项的 {vppn, asid} 与输入比对，考虑 G（全局）位；
//     `va_bit12` 用于选择页表项 0/1，ps 决定 vppn 的有效位数；
//   * 命中时 index 为命中的表项号；未命中时 found=0，其余输出无意义。
// ============================================================================

package exp17.student

import chisel3._
import chisel3.util._

/** TLB 表项的字段（供学生实现时参考；也可以自行用并列的 Reg(Vec) 表达）。 */
class TlbEntry extends Bundle {
  val e    = Bool()
  val vppn = UInt(19.W)
  val ps   = UInt(6.W)
  val asid = UInt(10.W)
  val g    = Bool()
  val ppn0 = UInt(20.W)
  val plv0 = UInt(2.W)
  val mat0 = UInt(2.W)
  val d0   = Bool()
  val v0   = Bool()
  val ppn1 = UInt(20.W)
  val plv1 = UInt(2.W)
  val mat1 = UInt(2.W)
  val d1   = Bool()
  val v1   = Bool()
}

class Tlb(tlbNum: Int = 16) extends Module {
  val idxBits = log2Ceil(tlbNum)

  val io = IO(new Bundle {
    val resetn = Input(Bool())

    // ---- 查找端口 0 ----
    val s0_vppn     = Input(UInt(19.W))
    val s0_va_bit12 = Input(Bool())
    val s0_asid     = Input(UInt(10.W))
    val s0_found    = Output(Bool())
    val s0_index    = Output(UInt(idxBits.W))
    val s0_ppn      = Output(UInt(20.W))
    val s0_ps       = Output(UInt(6.W))
    val s0_plv      = Output(UInt(2.W))
    val s0_mat      = Output(UInt(2.W))
    val s0_d        = Output(Bool())
    val s0_v        = Output(Bool())

    // ---- 查找端口 1 ----
    val s1_vppn     = Input(UInt(19.W))
    val s1_va_bit12 = Input(Bool())
    val s1_asid     = Input(UInt(10.W))
    val s1_found    = Output(Bool())
    val s1_index    = Output(UInt(idxBits.W))
    val s1_ppn      = Output(UInt(20.W))
    val s1_ps       = Output(UInt(6.W))
    val s1_plv      = Output(UInt(2.W))
    val s1_mat      = Output(UInt(2.W))
    val s1_d        = Output(Bool())
    val s1_v        = Output(Bool())

    // ---- invtlb（本实验固定为 0） ----
    val invtlb_valid = Input(Bool())
    val invtlb_op    = Input(UInt(5.W))

    // ---- 写端口 ----
    val we      = Input(Bool())
    val w_index = Input(UInt(idxBits.W))
    val w_e     = Input(Bool())
    val w_vppn  = Input(UInt(19.W))
    val w_ps    = Input(UInt(6.W))
    val w_asid  = Input(UInt(10.W))
    val w_g     = Input(Bool())
    val w_ppn0  = Input(UInt(20.W))
    val w_plv0  = Input(UInt(2.W))
    val w_mat0  = Input(UInt(2.W))
    val w_d0    = Input(Bool())
    val w_v0    = Input(Bool())
    val w_ppn1  = Input(UInt(20.W))
    val w_plv1  = Input(UInt(2.W))
    val w_mat1  = Input(UInt(2.W))
    val w_d1    = Input(Bool())
    val w_v1    = Input(Bool())

    // ---- 读端口 ----
    val r_index = Input(UInt(idxBits.W))
    val r_e     = Output(Bool())
    val r_vppn  = Output(UInt(19.W))
    val r_ps    = Output(UInt(6.W))
    val r_asid  = Output(UInt(10.W))
    val r_g     = Output(Bool())
    val r_ppn0  = Output(UInt(20.W))
    val r_plv0  = Output(UInt(2.W))
    val r_mat0  = Output(UInt(2.W))
    val r_d0    = Output(Bool())
    val r_v0    = Output(Bool())
    val r_ppn1  = Output(UInt(20.W))
    val r_plv1  = Output(UInt(2.W))
    val r_mat1  = Output(UInt(2.W))
    val r_d1    = Output(Bool())
    val r_v1    = Output(Bool())
  })

  // --------------------------------------------------------------------------
  // ① 表项存储：tlbNum 个表项（复位后全部无效）
  // --------------------------------------------------------------------------
// …（以下为待实现的 TODO 部分，见「待操作代码」）
```

### `TlbTop.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/exp17/soc/TlbTop.scala](chisel/src/main/scala/exp17/soc/TlbTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 对应原 Verilog：code/rtl/tlb_top.v（1001 行，TLB 模块级验证环境）
//
// 该环境把三类测试写在同一模块里：
//   ① 写测试：依次向 16 个 TLB 表项写入期望值（we=1、w_index=计数）；
//   ② 读测试：依次读回 16 个表项并与期望值逐字段比对（r_error）；
//   ③ 查找测试：26 组 (vppn, va_bit12, asid) 同时打到两个查找端口，
//      与期望的 found/ppn/ps/plv/mat/d/v 比对（s0_error / s1_error）。
// 三者都通过后 tlb_w/r/s_test_ok 置 1、test_error 保持 0（对应原实验的"打印 PASS"）。
//
// Chisel 差异：
//   * clk_pll 不建模（用隐式 clock，对应仿真时 PLL 加速）；
//   * wait_cnt 的重载值用参数 simulation 区分（5 / 30_000_000），与原 `SIMULATION 一致；
//   * 所有期望值表用 VecInit 常量表表达（与原 assign 表逐项对应）；
//   * Chisel 要求"先声明后使用"，故寄存器与组合逻辑的书写顺序做了等价重排。
// ============================================================================

package exp17.soc

import chisel3._
import chisel3.util._

class TlbTop(
  tlbNum:     Int     = 16,
  simulation: Boolean = true
) extends Module {

  val io = IO(new Bundle {
    val resetn  = Input(Bool())
    val led     = Output(UInt(16.W))
    val num_csn = Output(UInt(8.W))
    val num_a_g = Output(UInt(7.W))
    // 便于测试平台观测（不改变原行为）
    val wOk = Output(Bool())
    val rOk = Output(Bool())
    val sOk = Output(Bool())
    val err = Output(Bool())
  })

  // --------------------------------------------------------------------------
  // 测试状态寄存器（先声明，供后续组合逻辑引用）
  // --------------------------------------------------------------------------
  val tlb_w_test_ok = RegInit(false.B)
  val tlb_w_cnt     = RegInit(0.U(4.W))
  val tlb_r_test_ok = RegInit(false.B)
  val tlb_r_cnt     = RegInit(0.U(4.W))
  val tlb_s_test_ok = RegInit(false.B)
  val tlb_s_cnt     = RegInit(0.U(4.W))
  val test_error    = RegInit(false.B)

  // --------------------------------------------------------------------------
  // wait_1s：每 N 拍产生一个"节拍"，控制三个测试的推进速度
  // --------------------------------------------------------------------------
  val wait_cnt = Reg(UInt(27.W))
  val wait_1s  = wait_cnt === 0.U

  when(!io.resetn || wait_1s) {
    wait_cnt := (if (simulation) 5.U(27.W) else 30000000.U(27.W))
  }.otherwise {
    wait_cnt := wait_cnt - 1.U
  }

  // --------------------------------------------------------------------------
  // 期望值表（写测试用，16 项）
  // --------------------------------------------------------------------------
  val tlb_vppn = VecInit(Seq(0x1000, 0x111, 0x222, 0x333, 0x444, 0x444, 0x666, 0x666,
                             0x888, 0x999, 0xaaa, 0xbbb, 0xccc, 0xddd, 0xeee, 0xf000).map(_.U(19.W)))
  val tlb_e    = VecInit(Seq.fill(16)(true.B))
  val tlb_ps   = VecInit(Seq(0x15, 0xc, 0xc, 0xc, 0xc, 0xc, 0xc, 0xc,
// …（共 282 行，其余见源文件）
```

### `TlbSpec.scala`（判据复现）

> 源文件：[chisel/src/test/scala/TlbSpec.scala](chisel/src/test/scala/TlbSpec.scala)

```scala
  behavior of "TlbTop (exp17, TLB 模块级验证)"

  it should "写 / 读 / 查三类测试全部通过（对应原实验 ----PASS!!!）" in {
    test(new TlbTop(tlbNum = 16, simulation = true)) { dut =>
      dut.io.resetn.poke(false.B)
      dut.clock.step(4)
      dut.io.resetn.poke(true.B)

      var cycles    = 0
      val maxCycles = 200000

      def done: Boolean =
        dut.io.wOk.peek().litToBoolean &&
        dut.io.rOk.peek().litToBoolean &&
        dut.io.sOk.peek().litToBoolean

      while (!done && cycles < maxCycles) {
        assert(!dut.io.err.peek().litToBoolean,
               s"TLB 测试出错（cycle=$cycles）：写=${dut.io.wOk.peek().litToBoolean} " +
                 s"读=${dut.io.rOk.peek().litToBoolean} 查=${dut.io.sOk.peek().litToBoolean}")
        dut.clock.step()
        cycles += 1
      }

      assert(dut.io.wOk.peek().litToBoolean, "16 次写测试未通过")
      assert(dut.io.rOk.peek().litToBoolean, "16 次读测试未通过")
      assert(dut.io.sOk.peek().litToBoolean, "26 次查找测试未通过")
      assert(!dut.io.err.peek().litToBoolean, "存在比对错误")
      println(s"----PASS!!! TLB 写/读/查测试全部通过（$cycles 个周期）")
    }
  }
}
```

## 待操作代码（TODO）

共 **4** 处，全部位于学生模块中；下表为索引，代码块给出每处的上下文。

| # | 文件 | 行号 | 标记 |
|---|---|---|---|
| 1 | `Tlb.scala` | 125 | `TODO(实现 1/4)` |
| 2 | `Tlb.scala` | 130 | `TODO(实现 2/4)` |
| 3 | `Tlb.scala` | 135 | `TODO(实现 3/4)` |
| 4 | `Tlb.scala` | 141 | `TODO(实现 4/4)` |

### 待操作：`Tlb.scala:125` — `TODO(实现 1/4)`

> 源文件：[chisel/src/main/scala/exp17/student/Tlb.scala](chisel/src/main/scala/exp17/student/Tlb.scala)#L125

```scala
  // --------------------------------------------------------------------------
  // ① 表项存储：tlbNum 个表项（复位后全部无效）
  // --------------------------------------------------------------------------
  // TODO(实现 1/4)：定义表项存储（建议 RegInit(VecInit(Seq.fill(tlbNum)(0.U.asTypeOf(new TlbEntry))))）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ② 写端口：we 有效时把 w_* 写入 w_index 对应的表项
  // --------------------------------------------------------------------------
```

### 待操作：`Tlb.scala:130` — `TODO(实现 2/4)`

> 源文件：[chisel/src/main/scala/exp17/student/Tlb.scala](chisel/src/main/scala/exp17/student/Tlb.scala)#L130

```scala
  // --------------------------------------------------------------------------
  // ② 写端口：we 有效时把 w_* 写入 w_index 对应的表项
  // --------------------------------------------------------------------------
  // TODO(实现 2/4)：写端口逻辑   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ 读端口：r_index 直接索引，输出整个表项
  // --------------------------------------------------------------------------
```

### 待操作：`Tlb.scala:135` — `TODO(实现 3/4)`

> 源文件：[chisel/src/main/scala/exp17/student/Tlb.scala](chisel/src/main/scala/exp17/student/Tlb.scala)#L135

```scala
  // --------------------------------------------------------------------------
  // ③ 读端口：r_index 直接索引，输出整个表项
  // --------------------------------------------------------------------------
  // TODO(实现 3/4)：读端口逻辑   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ 双查找端口：按 {vppn, asid}（考虑 G 位与 va_bit12/ps）匹配，
  //    输出 found/index/ppn/ps/plv/mat/d/v
```

### 待操作：`Tlb.scala:141` — `TODO(实现 4/4)`

> 源文件：[chisel/src/main/scala/exp17/student/Tlb.scala](chisel/src/main/scala/exp17/student/Tlb.scala)#L141

```scala
  // ④ 双查找端口：按 {vppn, asid}（考虑 G 位与 va_bit12/ps）匹配，
  //    输出 found/index/ppn/ps/plv/mat/d/v
  // --------------------------------------------------------------------------
  // TODO(实现 4/4)：查找逻辑（两个端口各自独立）   // <<< 待操作
  io.s0_found := ???
  io.s0_index := ???
  io.s0_ppn   := ???
  io.s0_ps    := ???
```

## 实验验收

| 项 | 说明 |
|---|---|
| 原实验判据 | `mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!` |
| Chisel 版判据 | TlbSpec：16 写 / 16 读 / 26 查 → ----PASS!!! |
| 运行方式 | `cd chisel && ./mill chisel.test`（需 JDK 17 + Mill） |
| 运行准备 | 无需额外文件，直接运行即可 |

## 参考

- 原任务说明：[`task.md`](task.md)
- Chisel 环境说明：[`chisel/README.md`](chisel/README.md)（文件清单 / 步骤 / 判据）
- 教材：[《CPU 设计实战：LoongArch 版》](https://bookdown.org/loongson/_book3/)（汪文祥、邢金璋 等著）
- Chisel 写法参考：[Verilog to Chisel（黄治豪）](https://zihaojf.github.io/Chisel-/chisel/%E7%AE%80%E4%BB%8B/)

---

[← 实践任务16](../exp16/index.md) ｜ [实践任务18 →](../exp18/index.md)
