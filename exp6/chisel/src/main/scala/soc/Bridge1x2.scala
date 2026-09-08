// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_dram/rtl/BRIDGE/bridge_1x2.v
//
// 原 Verilog：
//     `define CONF_ADDR_BASE 32'h1faf_0000
//     `define CONF_ADDR_MASK 32'h1fff_0000
//     assign sel_conf = (cpu_data_addr & CONF_ADDR_MASK) == CONF_ADDR_BASE;
//     assign sel_sram = !sel_conf;
//     ... 各端口直连 ...
//     assign cpu_data_rdata = {32{sel_sram}} & data_sram_rdata
//                           | {32{sel_conf}} & conf_rdata;
// sel_sram / sel_conf 互斥，故 Chisel 用 Mux 等价实现读数据选择。

package exp6.soc

import chisel3._

class Bridge1x2 extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())          // 原接口保留，未使用
    // master : cpu data
    val cpu_data_we    = Input(Bool())
    val cpu_data_addr  = Input(UInt(32.W))
    val cpu_data_wdata = Input(UInt(32.W))
    val cpu_data_rdata = Output(UInt(32.W))
    // slave : data ram
    val data_sram_en    = Output(Bool())
    val data_sram_we    = Output(Bool())
    val data_sram_addr  = Output(UInt(32.W))
    val data_sram_wdata = Output(UInt(32.W))
    val data_sram_rdata = Input(UInt(32.W))
    // slave : confreg
    val conf_en    = Output(Bool())
    val conf_we    = Output(Bool())
    val conf_addr  = Output(UInt(32.W))
    val conf_wdata = Output(UInt(32.W))
    val conf_rdata = Input(UInt(32.W))
  })

  val CONF_ADDR_BASE = 0x1faf0000L.U(32.W)
  val CONF_ADDR_MASK = 0x1fff0000L.U(32.W)

  val sel_conf = (io.cpu_data_addr & CONF_ADDR_MASK) === CONF_ADDR_BASE
  val sel_sram = !sel_conf

  // data sram
  io.data_sram_en    := sel_sram
  io.data_sram_we    := io.cpu_data_we
  io.data_sram_addr  := io.cpu_data_addr
  io.data_sram_wdata := io.cpu_data_wdata

  // confreg
  io.conf_en    := sel_conf
  io.conf_we    := io.cpu_data_we
  io.conf_addr  := io.cpu_data_addr
  io.conf_wdata := io.cpu_data_wdata

  io.cpu_data_rdata := Mux(sel_sram, io.data_sram_rdata, io.conf_rdata)
}
