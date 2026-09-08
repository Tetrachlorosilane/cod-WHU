// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 对应原 Verilog：exp15~exp16、exp18/19、exp21~23 的
//                 code/soc_verify/soc_axi/rtl/soc_lite_top.v
//
// 结构：
//     cpu ──(AXI)──> axi_wrap(直通) ──> axi_crossbar_1x2 ──> axi_wrap_ram ──> axi_ram
//                                                          └──> confreg(AXI 从机)
//     confreg 产生 ram_random_mask，送给 axi_wrap_ram 制造随机延迟。
//
// 说明：
//   * 原 `axi_wrap.v` 是纯直通（m_* 与 s_* 逐位相连），因此本实现不建该模块，
//     标注为"直通，未建模"。
//   * `axi_crossbar_1x2` / `axi_ram` 为 Xilinx IP，本实现用
//     AxiCrossbar1x2 / AxiRamSlave 行为模型替代。
//   * exp15 与 exp16 的 RTL 完全相同（原文件 `define _RUN_PERF_TEST 但 `ifdef 检查
//     RUN_PERF_TEST，故随机掩码实际生效）；两者的区别只在 func 程序与参考 trace。
// ============================================================================

package envlib.soc

import chisel3._
import chisel3.util._

class SocAxiTop(
  cpuGen:     () => LACpuAxi,
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true,
  perfTest:   Boolean     = false   // 传给 AxiWrapRam：false=随机掩码（原文件实际行为）
) extends SocBase {

  val io = IO(new SocIO)

  val cpu_resetn = RegNext(io.resetn, false.B)

  // ---------------- cpu ----------------
  val cpu = Module(cpuGen())
  cpu.io.resetn := cpu_resetn

  // ---------------- 交叉开关（1 主 2 从） ----------------
  val xbar = Module(new AxiCrossbar1x2)
  xbar.io.resetn := cpu_resetn

  xbar.io.m_arid    := cpu.io.arid
  xbar.io.m_araddr  := cpu.io.araddr
  xbar.io.m_arlen   := cpu.io.arlen
  xbar.io.m_arsize  := cpu.io.arsize
  xbar.io.m_arburst := cpu.io.arburst
  xbar.io.m_arlock  := cpu.io.arlock
  xbar.io.m_arcache := cpu.io.arcache
  xbar.io.m_arprot  := cpu.io.arprot
  xbar.io.m_arvalid := cpu.io.arvalid
  cpu.io.arready    := xbar.io.m_arready

  cpu.io.rid    := xbar.io.m_rid
  cpu.io.rdata  := xbar.io.m_rdata
  cpu.io.rresp  := xbar.io.m_rresp
  cpu.io.rlast  := xbar.io.m_rlast
  cpu.io.rvalid := xbar.io.m_rvalid
  xbar.io.m_rready := cpu.io.rready

  xbar.io.m_awid    := cpu.io.awid
  xbar.io.m_awaddr  := cpu.io.awaddr
  xbar.io.m_awlen   := cpu.io.awlen
  xbar.io.m_awsize  := cpu.io.awsize
  xbar.io.m_awburst := cpu.io.awburst
  xbar.io.m_awlock  := cpu.io.awlock
  xbar.io.m_awcache := cpu.io.awcache
  xbar.io.m_awprot  := cpu.io.awprot
  xbar.io.m_awvalid := cpu.io.awvalid
  cpu.io.awready    := xbar.io.m_awready

  xbar.io.m_wid    := cpu.io.wid
  xbar.io.m_wdata  := cpu.io.wdata
  xbar.io.m_wstrb  := cpu.io.wstrb
  xbar.io.m_wlast  := cpu.io.wlast
  xbar.io.m_wvalid := cpu.io.wvalid
  cpu.io.wready    := xbar.io.m_wready

  cpu.io.bid    := xbar.io.m_bid
  cpu.io.bresp  := xbar.io.m_bresp
  cpu.io.bvalid := xbar.io.m_bvalid
  xbar.io.m_bready := cpu.io.bready

  // ---------------- slave0：AXI RAM（inst/data 合一） ----------------
  val u_axi_ram = Module(new AxiWrapRam(perfTest))
  u_axi_ram.io.resetn := cpu_resetn

  u_axi_ram.io.m_arid    := xbar.io.s0_arid
  u_axi_ram.io.m_araddr  := xbar.io.s0_araddr
  u_axi_ram.io.m_arlen   := xbar.io.s0_arlen
  u_axi_ram.io.m_arsize  := xbar.io.s0_arsize
  u_axi_ram.io.m_arburst := xbar.io.s0_arburst
  u_axi_ram.io.m_arlock  := xbar.io.s0_arlock
  u_axi_ram.io.m_arcache := xbar.io.s0_arcache
  u_axi_ram.io.m_arprot  := xbar.io.s0_arprot
  u_axi_ram.io.m_arvalid := xbar.io.s0_arvalid
  xbar.io.s0_arready     := u_axi_ram.io.m_arready

  xbar.io.s0_rid    := u_axi_ram.io.m_rid
  xbar.io.s0_rdata  := u_axi_ram.io.m_rdata
  xbar.io.s0_rresp  := u_axi_ram.io.m_rresp
  xbar.io.s0_rlast  := u_axi_ram.io.m_rlast
  xbar.io.s0_rvalid := u_axi_ram.io.m_rvalid
  u_axi_ram.io.m_rready := xbar.io.s0_rready

  u_axi_ram.io.m_awid    := xbar.io.s0_awid
  u_axi_ram.io.m_awaddr  := xbar.io.s0_awaddr
  u_axi_ram.io.m_awlen   := xbar.io.s0_awlen
  u_axi_ram.io.m_awsize  := xbar.io.s0_awsize
  u_axi_ram.io.m_awburst := xbar.io.s0_awburst
  u_axi_ram.io.m_awlock  := xbar.io.s0_awlock
  u_axi_ram.io.m_awcache := xbar.io.s0_awcache
  u_axi_ram.io.m_awprot  := xbar.io.s0_awprot
  u_axi_ram.io.m_awvalid := xbar.io.s0_awvalid
  xbar.io.s0_awready     := u_axi_ram.io.m_awready

  u_axi_ram.io.m_wid    := xbar.io.s0_wid
  u_axi_ram.io.m_wdata  := xbar.io.s0_wdata
  u_axi_ram.io.m_wstrb  := xbar.io.s0_wstrb
  u_axi_ram.io.m_wlast  := xbar.io.s0_wlast
  u_axi_ram.io.m_wvalid := xbar.io.s0_wvalid
  xbar.io.s0_wready     := u_axi_ram.io.m_wready

  xbar.io.s0_bid    := u_axi_ram.io.m_bid
  xbar.io.s0_bresp  := u_axi_ram.io.m_bresp
  xbar.io.s0_bvalid := u_axi_ram.io.m_bvalid
  u_axi_ram.io.m_bready := xbar.io.s0_bready

  // AXI RAM 从机（inst 与 data 共用同一块存储器，地址空间不重叠）
  val ramInit = if (instInit.nonEmpty) instInit else dataInit
  val u_ram = Module(new AxiRamSlave(ramInit, addrWidth = 20))
  u_ram.io.resetn := cpu_resetn

  u_ram.io.arid    := u_axi_ram.io.s_arid
  u_ram.io.araddr  := u_axi_ram.io.s_araddr
  u_ram.io.arlen   := u_axi_ram.io.s_arlen
  u_ram.io.arsize  := u_axi_ram.io.s_arsize
  u_ram.io.arburst := u_axi_ram.io.s_arburst
  u_ram.io.arvalid := u_axi_ram.io.s_arvalid
  u_axi_ram.io.s_arready := u_ram.io.arready

  u_axi_ram.io.s_rid    := u_ram.io.rid
  u_axi_ram.io.s_rdata  := u_ram.io.rdata
  u_axi_ram.io.s_rresp  := u_ram.io.rresp
  u_axi_ram.io.s_rlast  := u_ram.io.rlast
  u_axi_ram.io.s_rvalid := u_ram.io.rvalid
  u_ram.io.rready       := u_axi_ram.io.s_rready

  u_ram.io.awid    := u_axi_ram.io.s_awid
  u_ram.io.awaddr  := u_axi_ram.io.s_awaddr
  u_ram.io.awlen   := u_axi_ram.io.s_awlen
  u_ram.io.awsize  := u_axi_ram.io.s_awsize
  u_ram.io.awburst := u_axi_ram.io.s_awburst
  u_ram.io.awvalid := u_axi_ram.io.s_awvalid
  u_axi_ram.io.s_awready := u_ram.io.awready

  u_ram.io.wid    := u_axi_ram.io.s_wid
  u_ram.io.wdata  := u_axi_ram.io.s_wdata
  u_ram.io.wstrb  := u_axi_ram.io.s_wstrb
  u_ram.io.wlast  := u_axi_ram.io.s_wlast
  u_ram.io.wvalid := u_axi_ram.io.s_wvalid
  u_axi_ram.io.s_wready := u_ram.io.wready

  u_axi_ram.io.s_bid    := u_ram.io.bid
  u_axi_ram.io.s_bresp  := u_ram.io.bresp
  u_axi_ram.io.s_bvalid := u_ram.io.bvalid
  u_ram.io.bready       := u_axi_ram.io.s_bready

  // ---------------- slave1：confreg（AXI 从机） ----------------
  val u_confreg = Module(new AxiConfregWrap(simulation))
  u_confreg.io.resetn := cpu_resetn

  u_confreg.io.arid    := xbar.io.s1_arid
  u_confreg.io.araddr  := xbar.io.s1_araddr
  u_confreg.io.arlen   := xbar.io.s1_arlen
  u_confreg.io.arsize  := xbar.io.s1_arsize
  u_confreg.io.arburst := xbar.io.s1_arburst
  u_confreg.io.arlock  := xbar.io.s1_arlock
  u_confreg.io.arcache := xbar.io.s1_arcache
  u_confreg.io.arprot  := xbar.io.s1_arprot
  u_confreg.io.arvalid := xbar.io.s1_arvalid
  xbar.io.s1_arready   := u_confreg.io.arready

  xbar.io.s1_rid    := u_confreg.io.rid
  xbar.io.s1_rdata  := u_confreg.io.rdata
  xbar.io.s1_rresp  := u_confreg.io.rresp
  xbar.io.s1_rlast  := u_confreg.io.rlast
  xbar.io.s1_rvalid := u_confreg.io.rvalid
  u_confreg.io.rready := xbar.io.s1_rready

  u_confreg.io.awid    := xbar.io.s1_awid
  u_confreg.io.awaddr  := xbar.io.s1_awaddr
  u_confreg.io.awlen   := xbar.io.s1_awlen
  u_confreg.io.awsize  := xbar.io.s1_awsize
  u_confreg.io.awburst := xbar.io.s1_awburst
  u_confreg.io.awlock  := xbar.io.s1_awlock
  u_confreg.io.awcache := xbar.io.s1_awcache
  u_confreg.io.awprot  := xbar.io.s1_awprot
  u_confreg.io.awvalid := xbar.io.s1_awvalid
  xbar.io.s1_awready   := u_confreg.io.awready

  u_confreg.io.wid    := xbar.io.s1_wid
  u_confreg.io.wdata  := xbar.io.s1_wdata
  u_confreg.io.wstrb  := xbar.io.s1_wstrb
  u_confreg.io.wlast  := xbar.io.s1_wlast
  u_confreg.io.wvalid := xbar.io.s1_wvalid
  xbar.io.s1_wready   := u_confreg.io.wready

  xbar.io.s1_bid    := u_confreg.io.bid
  xbar.io.s1_bresp  := u_confreg.io.bresp
  xbar.io.s1_bvalid := u_confreg.io.bvalid
  u_confreg.io.bready := xbar.io.s1_bready

  // 随机掩码：confreg → axi_wrap_ram
  u_axi_ram.io.ram_random_mask := u_confreg.io.ram_random_mask

  // gpio
  u_confreg.io.switch      := io.switch
  u_confreg.io.btn_key_row := io.btn_key_row
  u_confreg.io.btn_step    := io.btn_step

  io.led         := u_confreg.io.led
  io.led_rg0     := u_confreg.io.led_rg0
  io.led_rg1     := u_confreg.io.led_rg1
  io.num_csn     := u_confreg.io.num_csn
  io.num_a_g     := u_confreg.io.num_a_g
  io.num_data    := u_confreg.io.num_data
  io.btn_key_col := u_confreg.io.btn_key_col

  def numData: UInt = u_confreg.io.num_data
}
