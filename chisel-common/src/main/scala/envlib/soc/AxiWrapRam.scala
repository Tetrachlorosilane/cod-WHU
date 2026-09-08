// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 对应原 Verilog：exp15/exp16 的 code/soc_verify/soc_axi/rtl/ram_wrap/axi_wrap_ram.v
//
// 作用：AXI 请求/响应的**延迟掩码**与**地址重映射**包装。
//   * 掩码：把 arvalid/rready/awvalid/wvalid/bready 与对应的 ready/valid 响应
//     分别与 ar_and/r_and/aw_and/w_and/b_and 相与，从而制造"随机延迟"；
//     被掩码挡住的请求会在下一拍通过 nomask 标记放行（保证最终完成）。
//   * 地址重映射：RAM 侧地址 = (addr[31:28] ∈ {0,1,7}) ? addr : {12'b0, 4'hf, addr[31:28], addr[11:0]}。
//
// 原文件顶部为 ``define _RUN_PERF_TEST``（注意：`ifdef 检查的是 `RUN_PERF_TEST`，未定义），
// 因此实际走的是 **else 分支（启用随机掩码）**。本实现用参数 perfTest 表达该选择：
//   perfTest = false（默认，对应原文件的真实行为）→ 使用 ram_random_mask；
//   perfTest = true                                → 固定延迟（r/b 用 pf 计数器）。
// ============================================================================

package envlib.soc

import chisel3._
import chisel3.util._

class AxiWrapRam(perfTest: Boolean = false) extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())
    // master 侧（来自 CPU/交叉开关）
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
    val m_bready = Input(Bool())

    // slave 侧（去往 AXI RAM）
    val s_arid    = Output(UInt(4.W))
    val s_araddr  = Output(UInt(32.W))
    val s_arlen   = Output(UInt(8.W))
    val s_arsize  = Output(UInt(3.W))
    val s_arburst = Output(UInt(2.W))
    val s_arlock  = Output(UInt(2.W))
    val s_arcache = Output(UInt(4.W))
    val s_arprot  = Output(UInt(3.W))
    val s_arvalid = Output(Bool())
    val s_arready = Input(Bool())

    val s_rid    = Input(UInt(4.W))
    val s_rdata  = Input(UInt(32.W))
    val s_rresp  = Input(UInt(2.W))
    val s_rlast  = Input(Bool())
    val s_rvalid = Input(Bool())
    val s_rready = Output(Bool())

    val s_awid    = Output(UInt(4.W))
    val s_awaddr  = Output(UInt(32.W))
    val s_awlen   = Output(UInt(8.W))
    val s_awsize  = Output(UInt(3.W))
    val s_awburst = Output(UInt(2.W))
    val s_awlock  = Output(UInt(2.W))
    val s_awcache = Output(UInt(4.W))
    val s_awprot  = Output(UInt(3.W))
    val s_awvalid = Output(Bool())
    val s_awready = Input(Bool())

    val s_wid    = Output(UInt(4.W))
    val s_wdata  = Output(UInt(32.W))
    val s_wstrb  = Output(UInt(4.W))
    val s_wlast  = Output(Bool())
    val s_wvalid = Output(Bool())
    val s_wready = Input(Bool())

    val s_bid    = Input(UInt(4.W))
    val s_bresp  = Input(UInt(2.W))
    val s_bvalid = Input(Bool())
    val s_bready = Output(Bool())

    // 来自 confreg 的随机延迟掩码
    val ram_random_mask = Input(UInt(5.W))
  })

  // --------------------------------------------------------------------------
  // 掩码与 nomask 标记
  // --------------------------------------------------------------------------
  val ar_nomask = Reg(Bool())
  val aw_nomask = Reg(Bool())
  val w_nomask  = Reg(Bool())
  val pf_r2r    = Reg(UInt(5.W))
  val pf_b2b    = Reg(UInt(2.W))

  val pf_r2r_nomask = pf_r2r === 0.U
  val pf_b2b_nomask = pf_b2b === 0.U

  val ar_and = if (perfTest) true.B else io.ram_random_mask(4) | ar_nomask
  val r_and  = if (perfTest) pf_r2r_nomask else io.ram_random_mask(3)
  val aw_and = if (perfTest) true.B else io.ram_random_mask(2) | aw_nomask
  val w_and  = if (perfTest) true.B else io.ram_random_mask(1) | w_nomask
  val b_and  = if (perfTest) pf_b2b_nomask else io.ram_random_mask(0)

  val arvalid_m = io.m_arvalid & ar_and
  val rready_m  = io.m_rready  & r_and
  val awvalid_m = io.m_awvalid & aw_and
  val wvalid_m  = io.m_wvalid  & w_and
  val bready_m  = io.m_bready  & b_and

  when(!io.resetn) {
    ar_nomask := false.B
    aw_nomask := false.B
    w_nomask  := false.B
    pf_r2r    := 0.U
    pf_b2b    := 0.U
  }.otherwise {
    ar_nomask := Mux(arvalid_m && io.m_arready, false.B, Mux(arvalid_m, true.B, ar_nomask))
    aw_nomask := Mux(awvalid_m && io.m_awready, false.B, Mux(awvalid_m, true.B, aw_nomask))
    w_nomask  := Mux(wvalid_m && io.m_wready, false.B, Mux(wvalid_m, true.B, w_nomask))

    pf_r2r := Mux(arvalid_m && io.m_arready, 25.U,
              Mux(!pf_r2r_nomask, pf_r2r - 1.U, pf_r2r))
    pf_b2b := Mux(awvalid_m && io.m_awready, 3.U,
              Mux(!pf_b2b_nomask, pf_b2b - 1.U, pf_b2b))
  }

  // master → slave（请求侧）
  io.s_arid    := io.m_arid
  io.s_araddr  := Mux(io.m_araddr(31, 28) === 0.U || io.m_araddr(31, 28) === 1.U ||
                      io.m_araddr(31, 28) === 7.U,
                      io.m_araddr,
                      Cat(0.U(12.W), 0xf.U(4.W), io.m_araddr(31, 28), io.m_araddr(11, 0)))
  io.s_arlen   := io.m_arlen
  io.s_arsize  := io.m_arsize
  io.s_arburst := io.m_arburst
  io.s_arlock  := io.m_arlock
  io.s_arcache := io.m_arcache
  io.s_arprot  := io.m_arprot
  io.s_arvalid := arvalid_m
  io.s_rready  := rready_m

  io.s_awid    := io.m_awid
  io.s_awaddr  := Mux(io.m_awaddr(31, 28) === 0.U || io.m_awaddr(31, 28) === 1.U ||
                      io.m_awaddr(31, 28) === 7.U,
                      io.m_awaddr,
                      Cat(0.U(12.W), 0xf.U(4.W), io.m_awaddr(31, 28), io.m_awaddr(11, 0)))
  io.s_awlen   := io.m_awlen
  io.s_awsize  := io.m_awsize
  io.s_awburst := io.m_awburst
  io.s_awlock  := io.m_awlock
  io.s_awcache := io.m_awcache
  io.s_awprot  := io.m_awprot
  io.s_awvalid := awvalid_m
  io.s_wid     := io.m_wid
  io.s_wdata   := io.m_wdata
  io.s_wstrb   := io.m_wstrb
  io.s_wlast   := io.m_wlast
  io.s_wvalid  := wvalid_m
  io.s_bready  := bready_m

  // slave → master（响应侧，按原文件同样做掩码）
  io.m_arready := io.s_arready & ar_and
  io.m_awready := io.s_awready & aw_and
  io.m_wready  := io.s_wready  & w_and

  val rvalidM = io.s_rvalid & r_and
  val bvalidM = io.s_bvalid & b_and

  io.m_rid    := Mux(rvalidM, io.s_rid, 0.U)
  io.m_rdata  := Mux(rvalidM, io.s_rdata, 0.U)
  io.m_rresp  := Mux(rvalidM, io.s_rresp, 0.U)
  io.m_rlast  := Mux(rvalidM, io.s_rlast, false.B)
  io.m_rvalid := rvalidM

  io.m_bid    := Mux(bvalidM, io.s_bid, 0.U)
  io.m_bresp  := Mux(bvalidM, io.s_bresp, 0.U)
  io.m_bvalid := bvalidM
}
