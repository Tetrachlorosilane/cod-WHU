// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：exp14~exp16 的 code/soc_verify/*/rtl/BRIDGE/bridge_1x2.v（握手版本）
//
// 原 Verilog 关键逻辑：
//     hit_conf = (cpu_data_addr & 32'h1fff_0000) == 32'h1faf_0000;
//     sel_conf = hit_conf && (!is_doing || sel_conf_reg);
//     sel_sram = !hit_conf && (!is_doing || !sel_conf_reg);
//     do_cnt   <= do_cnt + (cpu_data_req&&cpu_data_addr_ok) - (is_doing && cpu_data_data_ok);
//     is_full  = do_cnt == 4'hf;
//     data_sram_req = cpu_data_req && sel_sram && !is_full;   （conf 同理）
//     cpu_data_addr_ok = (!is_full&&sel_conf&&conf_addr_ok) || (!is_full&&sel_sram&&data_sram_addr_ok);
//     cpu_data_data_ok = sel_conf_reg ? conf_data_ok : data_sram_data_ok;
//     cpu_data_rdata   = sel_conf_reg ? conf_rdata   : data_sram_rdata;
//
// 即：bridge 内部维护一个深度 15 的"在途请求"计数与一次地址译码的锁存，
//     从而支持背靠背请求（CPU 侧只需保证不超过缓冲深度）。

package envlib.soc

import chisel3._
import chisel3.util._

class Bridge1x2Hs extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())
    // master : cpu data（握手）
    val cpu_data_req     = Input(Bool())
    val cpu_data_wr      = Input(Bool())
    val cpu_data_size    = Input(UInt(2.W))
    val cpu_data_wstrb   = Input(UInt(4.W))
    val cpu_data_addr    = Input(UInt(32.W))
    val cpu_data_wdata   = Input(UInt(32.W))
    val cpu_data_addr_ok = Output(Bool())
    val cpu_data_data_ok = Output(Bool())
    val cpu_data_rdata   = Output(UInt(32.W))
    // slave : data ram
    val data_sram_req     = Output(Bool())
    val data_sram_wr      = Output(Bool())
    val data_sram_size    = Output(UInt(2.W))
    val data_sram_wstrb   = Output(UInt(4.W))
    val data_sram_addr    = Output(UInt(32.W))
    val data_sram_wdata   = Output(UInt(32.W))
    val data_sram_addr_ok = Input(Bool())
    val data_sram_data_ok = Input(Bool())
    val data_sram_rdata   = Input(UInt(32.W))
    // slave : confreg
    val conf_req     = Output(Bool())
    val conf_wr      = Output(Bool())
    val conf_size    = Output(UInt(2.W))
    val conf_wstrb   = Output(UInt(4.W))
    val conf_addr    = Output(UInt(32.W))
    val conf_wdata   = Output(UInt(32.W))
    val conf_addr_ok = Input(Bool())
    val conf_data_ok = Input(Bool())
    val conf_rdata   = Input(UInt(32.W))
  })

  val CONF_ADDR_BASE = 0x1faf0000L.U(32.W)
  val CONF_ADDR_MASK = 0x1fff0000L.U(32.W)

  val hit_conf = (io.cpu_data_addr & CONF_ADDR_MASK) === CONF_ADDR_BASE

  val sel_conf_reg = Reg(Bool())
  val do_cnt       = Reg(UInt(4.W))
  val is_doing     = do_cnt =/= 0.U
  val is_full      = do_cnt === 0xf.U

  val sel_conf = hit_conf && (!is_doing || sel_conf_reg)
  val sel_sram = !hit_conf && (!is_doing || !sel_conf_reg)

  when(!io.resetn) {
    do_cnt := 0.U
  }.otherwise {
    do_cnt := do_cnt + (io.cpu_data_req && io.cpu_data_addr_ok).asUInt -
                        (is_doing && io.cpu_data_data_ok).asUInt
  }

  when(!io.resetn) {
    sel_conf_reg := false.B
  }.elsewhen(io.cpu_data_req && io.cpu_data_addr_ok) {
    sel_conf_reg := sel_conf
  }

  // data sram
  io.data_sram_req   := io.cpu_data_req && sel_sram && !is_full
  io.data_sram_wr    := io.cpu_data_wr
  io.data_sram_size  := io.cpu_data_size
  io.data_sram_wstrb := io.cpu_data_wstrb
  io.data_sram_addr  := io.cpu_data_addr
  io.data_sram_wdata := io.cpu_data_wdata

  // confreg
  io.conf_req   := io.cpu_data_req && sel_conf && !is_full
  io.conf_wr    := io.cpu_data_wr
  io.conf_size  := io.cpu_data_size
  io.conf_wstrb := io.cpu_data_wstrb
  io.conf_addr  := io.cpu_data_addr
  io.conf_wdata := io.cpu_data_wdata

  // 握手响应
  io.cpu_data_addr_ok := (!is_full && sel_conf && io.conf_addr_ok) ||
                         (!is_full && sel_sram && io.data_sram_addr_ok)
  io.cpu_data_data_ok := Mux(sel_conf_reg, io.conf_data_ok, io.data_sram_data_ok)
  io.cpu_data_rdata   := Mux(sel_conf_reg, io.conf_rdata, io.data_sram_rdata)
}
