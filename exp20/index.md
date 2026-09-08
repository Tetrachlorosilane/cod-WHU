---
layout: default
title: 实践任务20 · Cache 模块设计
parent: 实践任务
nav_order: 20
---

# 实践任务20：Cache 模块设计

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](chisel/README.md) ｜ [逐行对照](chisel/MAPPING.md)

> **任务类型**：从零实现（模块级） ｜ **待操作代码**：5 处
>
> **代码目录**：`code/`（本站收录的原 Verilog 源码） ｜ `chisel/`（本站收录的 Chisel 版环境）

## 实验目标

1. 设计 Cache 模块（模块名 `cache`，接口见原书 10.1 节表 10.2/10.3）。
2. 利用 Cache 模块级验证环境对所设计的 Cache 进行验证，通过仿真和上板验证。

## 关键代码

### `Cache.scala`（学生模块接口骨架）

> 源文件：[chisel/src/main/scala/exp20/student/Cache.scala](chisel/src/main/scala/exp20/student/Cache.scala)（本站内副本）

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现）：Cache —— 2 路组相联、每路 4KB、LRU/伪随机替换
//   对应原实验：本实验要求自行编写 `cache.v`，模块名固定为 `cache`
//   （原实验环境只提供 cache_top.v 验证环境，不提供 Cache 本体）。
//
// 本实验（实践任务20）的教学意图 = 从零实现：
//   Chisel 版只给接口骨架 + TODO(实现)，内部逻辑全部留空（???）。
//   未实现时 elaboration 会以 NotImplementedError 终止，这是预期行为。
//
// 设计规格（原书 10.2.1）：
//   * 2 路组相联，每路 4KB，共 8KB；行大小 16 字节（4 个 32 位字）；
//   * 替换算法：LRU 或伪随机；建议硬件初始化（复位后全部无效）；
//   * 与 CPU 侧接口：valid/op/index/tag/offset/wstrb/wdata → addr_ok/data_ok/rdata；
//   * 与总线侧接口：rd_req/rd_type/rd_addr（读请求，ret_valid/ret_last/ret_data 返回数据）、
//     wr_req/wr_type/wr_addr/wr_wstrb/wr_data（写回，wr_rdy 握手）。
//
// 实现提示（见原书 10.1/10.2 / ../../chisel4agent/06-进阶写法.md）：
//   * 命中判断：index 选组，tag 比两路；命中且 op=0 直接返回 rdata；
//   * miss：op=1（写）时先发 rd 请求把整行读入（验证环境返回全 1），再写入并置脏；
//     op=0（读）时发 rd 请求读入整行后返回数据；
//   * 写回：脏行被替换时发 wr 请求；
//   * `data_ok`（addr_ok）的时序要与验证环境配合：请求被接收用 addr_ok，
//     数据返回用 data_ok。
// ============================================================================

package exp20.student

import chisel3._
import chisel3.util._

class Cache extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())

    // ---- CPU 侧访问 ----
    val valid  = Input(Bool())
    val op     = Input(Bool())        // 1 = 写，0 = 读
    val index  = Input(UInt(8.W))
    val tag    = Input(UInt(20.W))
    val offset = Input(UInt(4.W))
    val wstrb  = Input(UInt(4.W))
    val wdata  = Input(UInt(32.W))
    val addr_ok = Output(Bool())
    val data_ok = Output(Bool())
    val rdata   = Output(UInt(32.W))

    // ---- 总线侧：读请求（Cache → 外部） ----
    val rd_req   = Output(Bool())
    val rd_type  = Output(UInt(3.W))
    val rd_addr  = Output(UInt(32.W))
    val rd_rdy   = Input(Bool())
    val ret_valid = Input(Bool())
    val ret_last  = Input(Bool())
    val ret_data  = Input(UInt(32.W))

    // ---- 总线侧：写回（Cache → 外部） ----
    val wr_req   = Output(Bool())
    val wr_type  = Output(UInt(3.W))
    val wr_addr  = Output(UInt(32.W))
    val wr_wstrb = Output(UInt(4.W))
    val wr_data  = Output(UInt(128.W))
    val wr_rdy   = Input(Bool())
  })

  // --------------------------------------------------------------------------
  // ① 存储体：2 路 × 256 组 × 16B 行
  // --------------------------------------------------------------------------
// …（以下为待实现的 TODO 部分，见「待操作代码」）
```

### `CacheTop.scala`（本实验环境/验证环境）

> 源文件：[chisel/src/main/scala/exp20/soc/CacheTop.scala](chisel/src/main/scala/exp20/soc/CacheTop.scala)

```scala
// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 对应原 Verilog：code/rtl/cache_top.v（417 行，Cache 模块级验证环境）
//
// 该环境按 index 递增地做"先写后读"验证：
//   PREPARE：用伪随机数准备 4 组 (tag, data[127:0])；
//   WRITE  ：对每个 index 发出 4 次写请求（每次写 1 个 32 位字，第 4 次用 wstrb=4'b0111），
//            并检查 Cache 是否正确地以 0xFFFFFFFF 响应 miss 时的 rd 请求、以及替换时的数据；
//   READ   ：对每个 index 发出 4 次读请求，比对读回数据与期望值。
// index 从 0 递增到 0xff 全部通过后由 testbench 打印 ----PASS!!!。
//
// Chisel 差异：
//   * clk_pll 不建模（隐式 clock）；
//   * wait_cnt 重载值用参数 simulation 区分（5 / 800_000）；
//   * 为便于测试平台观测，增加了 4 个只读输出（testIndex/roundFinish/replaceWrong/cacheresWrong）。
// ============================================================================

package exp20.soc

import chisel3._
import chisel3.util._

class CacheTop(simulation: Boolean = true) extends Module {

  val PREPARE = 0.U(2.W)
  val WRITE   = 1.U(2.W)
  val READ    = 2.U(2.W)

  val io = IO(new Bundle {
    val resetn  = Input(Bool())
    val led     = Output(UInt(16.W))
    val switch  = Input(UInt(8.W))
    val num_csn = Output(UInt(8.W))
    val num_a_g = Output(UInt(7.W))
    // 测试平台观测（不改变原行为）
    val testIndex    = Output(UInt(8.W))
    val roundFinish  = Output(Bool())
    val replaceWrong = Output(Bool())
    val cacheresWrong = Output(Bool())
  })

  // --------------------------------------------------------------------------
  // 伪随机序列（与原文件一致）
  // --------------------------------------------------------------------------
  val switch_led = Cat(
    Fill(2, io.switch(7)), Fill(2, io.switch(6)), Fill(2, io.switch(5)), Fill(2, io.switch(4)),
    Fill(2, io.switch(3)), Fill(2, io.switch(2)), Fill(2, io.switch(1)), Fill(2, io.switch(0)))
  val led_r_n = ~switch_led

  val pseudo_random_23 = Reg(UInt(23.W))
  when(!io.resetn) {
    pseudo_random_23 := Mux(simulation.B,
                            Cat(0x55.U(7.W), 0x00ff.U(16.W)),
                            Cat(0x55.U(7.W), led_r_n))
  }.otherwise {
    pseudo_random_23 := Cat(pseudo_random_23(21, 0), pseudo_random_23(22) ^ pseudo_random_23(17))
  }

  // --------------------------------------------------------------------------
  // wait_1s
  // --------------------------------------------------------------------------
  val wait_cnt = Reg(UInt(27.W))
  val wait_1s  = wait_cnt === 0.U
  when(!io.resetn || wait_1s) {
    wait_cnt := (if (simulation) 5.U(27.W) else 800000.U(27.W))
  }.otherwise {
    wait_cnt := wait_cnt - 1.U
  }

  // --------------------------------------------------------------------------
// …（共 320 行，其余见源文件）
```

### `CacheSpec.scala`（判据复现）

> 源文件：[chisel/src/test/scala/CacheSpec.scala](chisel/src/test/scala/CacheSpec.scala)

```scala
  behavior of "CacheTop (exp20, Cache 模块级验证)"

  it should "每个 index 先写后读全部正确，直到 index = 0xff（对应原实验 ----PASS!!!）" in {
    test(new CacheTop(simulation = true)) { dut =>
      dut.io.switch.poke(0xff.U)
      dut.io.resetn.poke(false.B)
      dut.clock.step(8)
      dut.io.resetn.poke(true.B)

      var cycles    = 0
      val maxCycles = 20000000
      var lastIdx   = -1

      def pass: Boolean = dut.io.testIndex.peek().litValue.toInt == 0xff &&
                          dut.io.roundFinish.peek().litToBoolean

      while (!pass && cycles < maxCycles) {
        assert(!dut.io.replaceWrong.peek().litToBoolean,
               s"replace wrong at index ${dut.io.testIndex.peek().litValue.toInt.toHexString}")
        assert(!dut.io.cacheresWrong.peek().litToBoolean,
               s"cacheres wrong at index ${dut.io.testIndex.peek().litValue.toInt.toHexString}")

        if (dut.io.roundFinish.peek().litToBoolean) {
          val idx = dut.io.testIndex.peek().litValue.toInt
          if (idx != lastIdx) {
            lastIdx = idx
            println(f"index $idx%02x finished")
          }
        }
        dut.clock.step()
        cycles += 1
      }

      assert(dut.io.roundFinish.peek().litToBoolean, s"$cycles 个周期内未完成全部 index")
      assert(dut.io.testIndex.peek().litValue.toInt == 0xff,
             s"最终 index = ${dut.io.testIndex.peek().litValue.toInt.toHexString}（期望 0xff）")
      println("=========================================================")
      println("Test end!")
      println("----PASS!!!")
    }
  }
}
```

## 待操作代码（TODO）

共 **5** 处，全部位于学生模块中；下表为索引，代码块给出每处的上下文。

| # | 文件 | 行号 | 标记 |
|---|---|---|---|
| 1 | `Cache.scala` | 69 | `TODO(实现 1/5)` |
| 2 | `Cache.scala` | 74 | `TODO(实现 2/5)` |
| 3 | `Cache.scala` | 79 | `TODO(实现 3/5)` |
| 4 | `Cache.scala` | 84 | `TODO(实现 4/5)` |
| 5 | `Cache.scala` | 89 | `TODO(实现 5/5)` |

### 待操作：`Cache.scala:69` — `TODO(实现 1/5)`

> 源文件：[chisel/src/main/scala/exp20/student/Cache.scala](chisel/src/main/scala/exp20/student/Cache.scala)#L69

```scala
  // --------------------------------------------------------------------------
  // ① 存储体：2 路 × 256 组 × 16B 行
  // --------------------------------------------------------------------------
  // TODO(实现 1/5)：定义 tag/valid/dirty/data 存储（建议 Vec 或若干 Reg(Vec)）   // <<< 待操作

  // --------------------------------------------------------------------------
  // ② 命中判断与读通路
  // --------------------------------------------------------------------------
```

### 待操作：`Cache.scala:74` — `TODO(实现 2/5)`

> 源文件：[chisel/src/main/scala/exp20/student/Cache.scala](chisel/src/main/scala/exp20/student/Cache.scala)#L74

```scala
  // --------------------------------------------------------------------------
  // ② 命中判断与读通路
  // --------------------------------------------------------------------------
  // TODO(实现 2/5)：按 index/tag 判断命中，命中时按 offset 选择 32 位字输出   // <<< 待操作

  // --------------------------------------------------------------------------
  // ③ miss 处理：向总线发 rd 请求填充整行
  // --------------------------------------------------------------------------
```

### 待操作：`Cache.scala:79` — `TODO(实现 3/5)`

> 源文件：[chisel/src/main/scala/exp20/student/Cache.scala](chisel/src/main/scala/exp20/student/Cache.scala)#L79

```scala
  // --------------------------------------------------------------------------
  // ③ miss 处理：向总线发 rd 请求填充整行
  // --------------------------------------------------------------------------
  // TODO(实现 3/5)：rd_req/rd_type/rd_addr 与 ret_valid/ret_last/ret_data 的接收   // <<< 待操作

  // --------------------------------------------------------------------------
  // ④ 写通路与脏行写回
  // --------------------------------------------------------------------------
```

### 待操作：`Cache.scala:84` — `TODO(实现 4/5)`

> 源文件：[chisel/src/main/scala/exp20/student/Cache.scala](chisel/src/main/scala/exp20/student/Cache.scala)#L84

```scala
  // --------------------------------------------------------------------------
  // ④ 写通路与脏行写回
  // --------------------------------------------------------------------------
  // TODO(实现 4/5)：写命中/写分配、wstrb 字节写、替换时 wr_req 写回   // <<< 待操作

  // --------------------------------------------------------------------------
  // ⑤ 与验证环境握手：addr_ok / data_ok
  // --------------------------------------------------------------------------
```

### 待操作：`Cache.scala:89` — `TODO(实现 5/5)`

> 源文件：[chisel/src/main/scala/exp20/student/Cache.scala](chisel/src/main/scala/exp20/student/Cache.scala)#L89

```scala
  // --------------------------------------------------------------------------
  // ⑤ 与验证环境握手：addr_ok / data_ok
  // --------------------------------------------------------------------------
  // TODO(实现 5/5)：addr_ok（请求被接收）与 data_ok（数据有效）的产生   // <<< 待操作
  io.addr_ok := ???
  io.data_ok := ???
  io.rdata   := ???
```

## 实验验收

| 项 | 说明 |
|---|---|
| 原实验判据 | `mycpu_tb.v` 与 `gettrace/golden_trace.txt` 逐条比对，到达 END_PC 打印 `----PASS!!!` |
| Chisel 版判据 | CacheSpec：index 0→0xff 先写后读 → ----PASS!!! |
| 运行方式 | `cd chisel && ./mill chisel.test`（需 JDK 17 + Mill） |
| 运行所需运行件 | 本实验**不需要**外部运行件（exp5 用内嵌常量表；exp17/exp20 为模块级环境） |
| ⚠️ 未实测 | Chisel 代码为静态交付，未编译/仿真；逐行对照见 `chisel/MAPPING.md` |

## 参考

- 原任务说明：[`task.md`](task.md)（硬链接到 `taskvscode/exp20/10.2.1实践任务20-Cache模块设计.md`）
- Chisel 环境说明：[`chisel/README.md`](chisel/README.md)（速览 / 步骤 / 判据 / 自检）
- Verilog ↔ Chisel 逐行对照：[`chisel/MAPPING.md`](chisel/MAPPING.md)
- 改写规范与核验记录：[CHISEL-CONVENTIONS.md](../CHISEL-CONVENTIONS.md)
- 原书（LoongArch 版）：https://bookdown.org/loongson/_book3/

---

[← 实践任务19](../exp19/index.md) ｜ [实践任务21 →](../exp21/index.md)
