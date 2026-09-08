// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：exp14 的 code/soc_verify/soc_hs_bram/rtl/soc_lite_top.v
//
// 结构（与 exp7~exp13 的 soc_bram 变体的差异在于访存接口）：
//     cpu ──(握手)──> sram_wrap ──(简单接口)──> inst_ram
//     cpu ──(握手)──> bridge_1x2(握手版) ──(握手)──> sram_wrap ──> data_ram
//                                        └──(握手)──> confreg(握手适配)
//
// 与 exp7~exp13 的关键差异：
//   1. CPU 访存接口从"en/we/addr/wdata/rdata"改为握手式
//      "req/wr/size/wstrb/addr/wdata + addr_ok/data_ok/rdata"；
//   2. 指令/数据 RAM 前置 sram_wrap（含深度 4 的读数据缓冲），支持背靠背请求；
//   3. bridge 增加在途请求计数（深度 15）与地址译码锁存。

package envlib.soc

import chisel3._
import chisel3.util._

class SocHsBramTop(
  cpuGen:     () => LACpuHs,
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true
) extends SocBase {

  val io = IO(new SocIO)

  val cpu_resetn = RegNext(io.resetn, false.B)

  // ---------------- cpu ----------------
  val cpu = Module(cpuGen())
  cpu.io.resetn := cpu_resetn

  // ---------------- inst path ----------------
  val u_inst_sram_wrap = Module(new SramWrap)
  u_inst_sram_wrap.io.resetn := cpu_resetn
  u_inst_sram_wrap.io.req    := cpu.io.inst_sram_req
  u_inst_sram_wrap.io.wr     := cpu.io.inst_sram_wr
  u_inst_sram_wrap.io.size   := cpu.io.inst_sram_size
  u_inst_sram_wrap.io.wstrb  := cpu.io.inst_sram_wstrb
  u_inst_sram_wrap.io.addr   := cpu.io.inst_sram_addr
  u_inst_sram_wrap.io.wdata  := cpu.io.inst_sram_wdata
  cpu.io.inst_sram_addr_ok   := u_inst_sram_wrap.io.addr_ok
  cpu.io.inst_sram_data_ok   := u_inst_sram_wrap.io.data_ok
  cpu.io.inst_sram_rdata     := u_inst_sram_wrap.io.rdata

  val inst_ram = Module(new InstRam(instInit, addrWidth = 18))
  inst_ram.io.ena   := u_inst_sram_wrap.io.ram_en
  inst_ram.io.wea   := u_inst_sram_wrap.io.ram_we
  inst_ram.io.addra := u_inst_sram_wrap.io.ram_addr(19, 2)
  inst_ram.io.dina  := u_inst_sram_wrap.io.ram_wdata
  u_inst_sram_wrap.io.ram_rdata := inst_ram.io.douta

  // ---------------- data path ----------------
  val bridge_1x2 = Module(new Bridge1x2Hs)
  bridge_1x2.io.resetn          := cpu_resetn
  bridge_1x2.io.cpu_data_req    := cpu.io.data_sram_req
  bridge_1x2.io.cpu_data_wr     := cpu.io.data_sram_wr
  bridge_1x2.io.cpu_data_size   := cpu.io.data_sram_size
  bridge_1x2.io.cpu_data_wstrb  := cpu.io.data_sram_wstrb
  bridge_1x2.io.cpu_data_addr   := cpu.io.data_sram_addr
  bridge_1x2.io.cpu_data_wdata  := cpu.io.data_sram_wdata
  cpu.io.data_sram_addr_ok      := bridge_1x2.io.cpu_data_addr_ok
  cpu.io.data_sram_data_ok      := bridge_1x2.io.cpu_data_data_ok
  cpu.io.data_sram_rdata        := bridge_1x2.io.cpu_data_rdata

  val u_data_sram_wrap = Module(new SramWrap)
  u_data_sram_wrap.io.resetn := cpu_resetn
  u_data_sram_wrap.io.req    := bridge_1x2.io.data_sram_req
  u_data_sram_wrap.io.wr     := bridge_1x2.io.data_sram_wr
  u_data_sram_wrap.io.size   := bridge_1x2.io.data_sram_size
  u_data_sram_wrap.io.wstrb  := bridge_1x2.io.data_sram_wstrb
  u_data_sram_wrap.io.addr   := bridge_1x2.io.data_sram_addr
  u_data_sram_wrap.io.wdata  := bridge_1x2.io.data_sram_wdata
  bridge_1x2.io.data_sram_addr_ok := u_data_sram_wrap.io.addr_ok
  bridge_1x2.io.data_sram_data_ok := u_data_sram_wrap.io.data_ok
  bridge_1x2.io.data_sram_rdata   := u_data_sram_wrap.io.rdata

  val data_ram = Module(new DataRam(dataInit, addrWidth = 16))
  data_ram.io.ena   := u_data_sram_wrap.io.ram_en
  data_ram.io.wea   := u_data_sram_wrap.io.ram_we
  data_ram.io.addra := u_data_sram_wrap.io.ram_addr(17, 2)
  data_ram.io.dina  := u_data_sram_wrap.io.ram_wdata
  u_data_sram_wrap.io.ram_rdata := data_ram.io.douta

  // ---------------- confreg ----------------
  val u_confreg = Module(new ConfregHsWrap(simulation))
  u_confreg.io.resetn     := cpu_resetn
  u_confreg.io.conf_req   := bridge_1x2.io.conf_req
  u_confreg.io.conf_wr    := bridge_1x2.io.conf_wr
  u_confreg.io.conf_size  := bridge_1x2.io.conf_size
  u_confreg.io.conf_wstrb := bridge_1x2.io.conf_wstrb
  u_confreg.io.conf_addr  := bridge_1x2.io.conf_addr
  u_confreg.io.conf_wdata := bridge_1x2.io.conf_wdata
  bridge_1x2.io.conf_addr_ok := u_confreg.io.conf_addr_ok
  bridge_1x2.io.conf_data_ok := u_confreg.io.conf_data_ok
  bridge_1x2.io.conf_rdata   := u_confreg.io.conf_rdata

  u_confreg.io.switch      := io.switch
  u_confreg.io.btn_key_row := io.btn_key_row
  u_confreg.io.btn_step    := io.btn_step

  def numData: UInt = u_confreg.io.num_data

  io.led         := u_confreg.io.led
  io.led_rg0     := u_confreg.io.led_rg0
  io.led_rg1     := u_confreg.io.led_rg1
  io.num_csn     := u_confreg.io.num_csn
  io.num_a_g     := u_confreg.io.num_a_g
  io.num_data    := u_confreg.io.num_data
  io.btn_key_col := u_confreg.io.btn_key_col
}
