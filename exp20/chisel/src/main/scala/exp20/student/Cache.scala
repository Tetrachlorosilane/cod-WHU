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
  // TODO(实现 1/5)：定义 tag/valid/dirty/data 存储（建议 Vec 或若干 Reg(Vec)）

  // --------------------------------------------------------------------------
  // ② 命中判断与读通路
  // --------------------------------------------------------------------------
  // TODO(实现 2/5)：按 index/tag 判断命中，命中时按 offset 选择 32 位字输出

  // --------------------------------------------------------------------------
  // ③ miss 处理：向总线发 rd 请求填充整行
  // --------------------------------------------------------------------------
  // TODO(实现 3/5)：rd_req/rd_type/rd_addr 与 ret_valid/ret_last/ret_data 的接收

  // --------------------------------------------------------------------------
  // ④ 写通路与脏行写回
  // --------------------------------------------------------------------------
  // TODO(实现 4/5)：写命中/写分配、wstrb 字节写、替换时 wr_req 写回

  // --------------------------------------------------------------------------
  // ⑤ 与验证环境握手：addr_ok / data_ok
  // --------------------------------------------------------------------------
  // TODO(实现 5/5)：addr_ok（请求被接收）与 data_ok（数据有效）的产生
  io.addr_ok := ???
  io.data_ok := ???
  io.rdata   := ???

  io.rd_req  := ???
  io.rd_type := ???
  io.rd_addr := ???

  io.wr_req   := ???
  io.wr_type  := ???
  io.wr_addr  := ???
  io.wr_wstrb := ???
  io.wr_data  := ???
}
