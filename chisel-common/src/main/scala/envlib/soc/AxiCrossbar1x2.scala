// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 对应原 Verilog：exp15~exp16 的 Xilinx IP `axi_crossbar_1x2`
// （code/.../xilinx_ip/axi_crossbar_1x2/axi_crossbar_1x2.xci）
//
// 作用：把 CPU 的 1 个 AXI 主口按地址分发给 2 个从口：
//     slave0 = AXI RAM（inst/data），slave1 = confreg。
//   地址判定：`sel_conf = (addr & 32'h1fff_0000) == 32'h1faf_0000`（与 bridge 一致）。
//
// 简化：CPU 为单发射、同一时刻只有一个在途事务，
// 因此交叉开关只需"按 AR/AW 的地址选路 + 用锁存的目标把 R/B 送回"，
// 不实现多 outstanding、乱序返回与 ID 重排。
// ============================================================================

package envlib.soc

import chisel3._
import chisel3.util._

class AxiCrossbar1x2 extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())
    // ---- master（来自 CPU / axi_wrap） ----
    val m_arid    = Input(UInt(4.W))
    val m_araddr  = Input(UInt(32.W))
    val m_arlen   = Input(UInt(8.W))
    val m_arsize  = Input(UInt(3.W))
    val m_arburst = Input(UInt(2.W))
    val m_arlock  = Input(UInt(2.W))
    val m_arcache = Input(UInt(4.W))
    val m_arprot  = Input(UInt(3.W))
    val m_arvalid = Input(Bool())
    val m_arready = Output(Bool())

    val m_rid    = Output(UInt(4.W))
    val m_rdata  = Output(UInt(32.W))
    val m_rresp  = Output(UInt(2.W))
    val m_rlast  = Output(Bool())
    val m_rvalid = Output(Bool())
    val m_rready = Input(Bool())

    val m_awid    = Input(UInt(4.W))
    val m_awaddr  = Input(UInt(32.W))
    val m_awlen   = Input(UInt(8.W))
    val m_awsize  = Input(UInt(3.W))
    val m_awburst = Input(UInt(2.W))
    val m_awlock  = Input(UInt(2.W))
    val m_awcache = Input(UInt(4.W))
    val m_awprot  = Input(UInt(3.W))
    val m_awvalid = Input(Bool())
    val m_awready = Output(Bool())

    val m_wid    = Input(UInt(4.W))
    val m_wdata  = Input(UInt(32.W))
    val m_wstrb  = Input(UInt(4.W))
    val m_wlast  = Input(Bool())
    val m_wvalid = Input(Bool())
    val m_wready = Output(Bool())

    val m_bid    = Output(UInt(4.W))
    val m_bresp  = Output(UInt(2.W))
    val m_bvalid = Output(Bool())
    val m_bready  = Input(Bool())

    // ---- slave 0（AXI RAM） ----
    val s0_arid    = Output(UInt(4.W))
    val s0_araddr  = Output(UInt(32.W))
    val s0_arlen   = Output(UInt(8.W))
    val s0_arsize  = Output(UInt(3.W))
    val s0_arburst = Output(UInt(2.W))
    val s0_arlock  = Output(UInt(2.W))
    val s0_arcache = Output(UInt(4.W))
    val s0_arprot  = Output(UInt(3.W))
    val s0_arvalid = Output(Bool())
    val s0_arready = Input(Bool())
    val s0_rid     = Input(UInt(4.W))
    val s0_rdata   = Input(UInt(32.W))
    val s0_rresp   = Input(UInt(2.W))
    val s0_rlast   = Input(Bool())
    val s0_rvalid  = Input(Bool())
    val s0_rready  = Output(Bool())
    val s0_awid    = Output(UInt(4.W))
    val s0_awaddr  = Output(UInt(32.W))
    val s0_awlen   = Output(UInt(8.W))
    val s0_awsize  = Output(UInt(3.W))
    val s0_awburst = Output(UInt(2.W))
    val s0_awlock  = Output(UInt(2.W))
    val s0_awcache = Output(UInt(4.W))
    val s0_awprot  = Output(UInt(3.W))
    val s0_awvalid = Output(Bool())
    val s0_awready = Input(Bool())
    val s0_wid     = Output(UInt(4.W))
    val s0_wdata   = Output(UInt(32.W))
    val s0_wstrb   = Output(UInt(4.W))
    val s0_wlast   = Output(Bool())
    val s0_wvalid  = Output(Bool())
    val s0_wready  = Input(Bool())
    val s0_bid     = Input(UInt(4.W))
    val s0_bresp   = Input(UInt(2.W))
    val s0_bvalid  = Input(Bool())
    val s0_bready  = Output(Bool())

    // ---- slave 1（confreg） ----
    val s1_arid    = Output(UInt(4.W))
    val s1_araddr  = Output(UInt(32.W))
    val s1_arlen   = Output(UInt(8.W))
    val s1_arsize  = Output(UInt(3.W))
    val s1_arburst = Output(UInt(2.W))
    val s1_arlock  = Output(UInt(2.W))
    val s1_arcache = Output(UInt(4.W))
    val s1_arprot  = Output(UInt(3.W))
    val s1_arvalid = Output(Bool())
    val s1_arready = Input(Bool())
    val s1_rid     = Input(UInt(4.W))
    val s1_rdata   = Input(UInt(32.W))
    val s1_rresp   = Input(UInt(2.W))
    val s1_rlast   = Input(Bool())
    val s1_rvalid  = Input(Bool())
    val s1_rready  = Output(Bool())
    val s1_awid    = Output(UInt(4.W))
    val s1_awaddr  = Output(UInt(32.W))
    val s1_awlen   = Output(UInt(8.W))
    val s1_awsize  = Output(UInt(3.W))
    val s1_awburst = Output(UInt(2.W))
    val s1_awlock  = Output(UInt(2.W))
    val s1_awcache = Output(UInt(4.W))
    val s1_awprot  = Output(UInt(3.W))
    val s1_awvalid = Output(Bool())
    val s1_awready = Input(Bool())
    val s1_wid     = Output(UInt(4.W))
    val s1_wdata   = Output(UInt(32.W))
    val s1_wstrb   = Output(UInt(4.W))
    val s1_wlast   = Output(Bool())
    val s1_wvalid  = Output(Bool())
    val s1_wready  = Input(Bool())
    val s1_bid     = Input(UInt(4.W))
    val s1_bresp   = Input(UInt(2.W))
    val s1_bvalid  = Input(Bool())
    val s1_bready  = Output(Bool())
  })

  val CONF_ADDR_BASE = 0x1faf0000L.U(32.W)
  val CONF_ADDR_MASK = 0x1fff0000L.U(32.W)

  val ar_conf = (io.m_araddr & CONF_ADDR_MASK) === CONF_ADDR_BASE
  val aw_conf = (io.m_awaddr & CONF_ADDR_MASK) === CONF_ADDR_BASE

  // 锁存事务目标（用于 R/B 返回路由）
  val ar_conf_r = RegInit(false.B)
  val aw_conf_r = RegInit(false.B)
  when(io.m_arvalid && io.m_arready) { ar_conf_r := ar_conf }
  when(io.m_awvalid && io.m_awready) { aw_conf_r := aw_conf }

  // ---- AR ----
  io.s0_arid := io.m_arid; io.s1_arid := io.m_arid
  io.s0_araddr := io.m_araddr; io.s1_araddr := io.m_araddr
  io.s0_arlen := io.m_arlen; io.s1_arlen := io.m_arlen
  io.s0_arsize := io.m_arsize; io.s1_arsize := io.m_arsize
  io.s0_arburst := io.m_arburst; io.s1_arburst := io.m_arburst
  io.s0_arlock := io.m_arlock; io.s1_arlock := io.m_arlock
  io.s0_arcache := io.m_arcache; io.s1_arcache := io.m_arcache
  io.s0_arprot := io.m_arprot; io.s1_arprot := io.m_arprot
  io.s0_arvalid := io.m_arvalid && !ar_conf
  io.s1_arvalid := io.m_arvalid && ar_conf
  io.m_arready  := Mux(ar_conf, io.s1_arready, io.s0_arready)

  // ---- R ----
  io.m_rid    := Mux(ar_conf_r, io.s1_rid, io.s0_rid)
  io.m_rdata  := Mux(ar_conf_r, io.s1_rdata, io.s0_rdata)
  io.m_rresp  := Mux(ar_conf_r, io.s1_rresp, io.s0_rresp)
  io.m_rlast  := Mux(ar_conf_r, io.s1_rlast, io.s0_rlast)
  io.m_rvalid := Mux(ar_conf_r, io.s1_rvalid, io.s0_rvalid)
  io.s0_rready := io.m_rready && !ar_conf_r
  io.s1_rready := io.m_rready && ar_conf_r

  // ---- AW / W（W 按最近一次 AW 的目标路由） ----
  io.s0_awid := io.m_awid; io.s1_awid := io.m_awid
  io.s0_awaddr := io.m_awaddr; io.s1_awaddr := io.m_awaddr
  io.s0_awlen := io.m_awlen; io.s1_awlen := io.m_awlen
  io.s0_awsize := io.m_awsize; io.s1_awsize := io.m_awsize
  io.s0_awburst := io.m_awburst; io.s1_awburst := io.m_awburst
  io.s0_awlock := io.m_awlock; io.s1_awlock := io.m_awlock
  io.s0_awcache := io.m_awcache; io.s1_awcache := io.m_awcache
  io.s0_awprot := io.m_awprot; io.s1_awprot := io.m_awprot
  io.s0_awvalid := io.m_awvalid && !aw_conf
  io.s1_awvalid := io.m_awvalid && aw_conf
  io.m_awready  := Mux(aw_conf, io.s1_awready, io.s0_awready)

  io.s0_wid := io.m_wid; io.s1_wid := io.m_wid
  io.s0_wdata := io.m_wdata; io.s1_wdata := io.m_wdata
  io.s0_wstrb := io.m_wstrb; io.s1_wstrb := io.m_wstrb
  io.s0_wlast := io.m_wlast; io.s1_wlast := io.m_wlast
  io.s0_wvalid := io.m_wvalid && !aw_conf_r
  io.s1_wvalid := io.m_wvalid && aw_conf_r
  io.m_wready  := Mux(aw_conf_r, io.s1_wready, io.s0_wready)

  // ---- B ----
  io.m_bid    := Mux(aw_conf_r, io.s1_bid, io.s0_bid)
  io.m_bresp  := Mux(aw_conf_r, io.s1_bresp, io.s0_bresp)
  io.m_bvalid := Mux(aw_conf_r, io.s1_bvalid, io.s0_bvalid)
  io.s0_bready := io.m_bready && !aw_conf_r
  io.s1_bready := io.m_bready && aw_conf_r
}
