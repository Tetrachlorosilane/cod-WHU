// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 对应原 Verilog：exp15~exp16 的 code/soc_verify/soc_axi/rtl/CONFREG/confreg.v 的
// **AXI 从机接口**（该文件 838 行 = AXI 前端 + 与 exp6 起相同的寄存器逻辑 + 随机掩码生成）。
//
// Chisel 做法：
//   * AXI 前端（busy/R_or_W/buf_*/wready/rvalid/bvalid）按原文件逐行改写；
//   * 寄存器逻辑复用 envlib.soc.Confreg（exp7 起已在用），避免重复维护 ~700 行；
//   * 随机延迟掩码生成器（pseudo_random_23 / no_mask / short_delay / ram_random_mask）
//     按原文件逐行改写。
//
// 与原文的一处差异：原 confreg 的 led_data 复位值为 {16'h0, switch_led}
// （而非 0）；本适配器复用 Confreg，复位值为 0。该差异只影响复位后的初始灯态，
// 不影响任何读写语义与 trace 比对。
// ============================================================================

package envlib.soc

import chisel3._
import chisel3.util._

class AxiConfregWrap(simulation: Boolean = false) extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())
    // ---- AXI 从机 ----
    val arid    = Input(UInt(4.W))
    val araddr  = Input(UInt(32.W))
    val arlen   = Input(UInt(8.W))
    val arsize  = Input(UInt(3.W))
    val arburst = Input(UInt(2.W))
    val arlock  = Input(UInt(2.W))
    val arcache = Input(UInt(4.W))
    val arprot  = Input(UInt(3.W))
    val arvalid = Input(Bool())
    val arready = Output(Bool())

    val rid    = Output(UInt(4.W))
    val rdata  = Output(UInt(32.W))
    val rresp  = Output(UInt(2.W))
    val rlast  = Output(Bool())
    val rvalid = Output(Bool())
    val rready = Input(Bool())

    val awid    = Input(UInt(4.W))
    val awaddr  = Input(UInt(32.W))
    val awlen   = Input(UInt(8.W))
    val awsize  = Input(UInt(3.W))
    val awburst = Input(UInt(2.W))
    val awlock  = Input(UInt(2.W))
    val awcache = Input(UInt(4.W))
    val awprot  = Input(UInt(3.W))
    val awvalid = Input(Bool())
    val awready = Output(Bool())

    val wid    = Input(UInt(4.W))
    val wdata  = Input(UInt(32.W))
    val wstrb  = Input(UInt(4.W))
    val wlast  = Input(Bool())
    val wvalid = Input(Bool())
    val wready = Output(Bool())

    val bid    = Output(UInt(4.W))
    val bresp  = Output(UInt(2.W))
    val bvalid = Output(Bool())
    val bready = Input(Bool())

    // ---- 板级设备 ----
    val led         = Output(UInt(16.W))
    val led_rg0     = Output(UInt(2.W))
    val led_rg1     = Output(UInt(2.W))
    val num_csn     = Output(UInt(8.W))
    val num_a_g     = Output(UInt(7.W))
    val num_data    = Output(UInt(32.W))
    val switch      = Input(UInt(8.W))
    val btn_key_col = Output(UInt(4.W))
    val btn_key_row = Input(UInt(4.W))
    val btn_step    = Input(UInt(2.W))

    // ---- 随机延迟掩码（给 axi_wrap_ram） ----
    val ram_random_mask = Output(UInt(5.W))
  })

  // --------------------------------------------------------------------------
  // AXI 前端（原文件 161~288 行）
  // --------------------------------------------------------------------------
  val busy    = RegInit(false.B)
  val R_or_W  = RegInit(false.B)
  val buf_id  = RegInit(0.U(4.W))
  val buf_addr = RegInit(0.U(32.W))
  val buf_len = RegInit(0.U(8.W))
  val buf_size = RegInit(0.U(3.W))

  val ar_enter = io.arvalid && io.arready
  val r_retire = io.rvalid && io.rready && io.rlast
  val aw_enter = io.awvalid && io.awready
  val w_enter  = io.wvalid && io.wready && io.wlast
  val b_retire = io.bvalid && io.bready

  io.arready := !busy && (!R_or_W || !io.awvalid)
  io.awready := !busy && (R_or_W || !io.arvalid)

  when(!io.resetn) {
    busy := false.B
  }.elsewhen(ar_enter || aw_enter) {
    busy := true.B
  }.elsewhen(r_retire || b_retire) {
    busy := false.B
  }

  when(!io.resetn) {
    R_or_W   := false.B
    buf_id   := 0.U
    buf_addr := 0.U
    buf_len  := 0.U
    buf_size := 0.U
  }.elsewhen(ar_enter || aw_enter) {
    R_or_W   := ar_enter
    buf_id   := Mux(ar_enter, io.arid, io.awid)
    buf_addr := Mux(ar_enter, io.araddr, io.awaddr)
    buf_len  := Mux(ar_enter, io.arlen, io.awlen)
    buf_size := Mux(ar_enter, io.arsize, io.awsize)
  }

  // W 通道 ready
  val wreadyReg = RegInit(false.B)
  when(!io.resetn) {
    wreadyReg := false.B
  }.elsewhen(aw_enter) {
    wreadyReg := true.B
  }.elsewhen(w_enter) {
    wreadyReg := false.B
  }
  io.wready := wreadyReg

  // --------------------------------------------------------------------------
  // 寄存器逻辑（复用共享库 Confreg）
  // --------------------------------------------------------------------------
  val core = Module(new Confreg(simulation, weWidth = 4))
  core.io.resetn     := io.resetn
  core.io.conf_en    := w_enter                       // 原 assign conf_we = w_enter
  core.io.conf_we    := Mux(w_enter, 0xf.U(4.W), 0.U(4.W))
  core.io.conf_addr  := buf_addr
  core.io.conf_wdata := io.wdata
  core.io.switch      := io.switch
  core.io.btn_key_row := io.btn_key_row
  core.io.btn_step    := io.btn_step

  // 读数据（原文件：busy & R_or_W & !r_retire 时用 buf_addr 译码并打一拍）
  val rdataReg  = RegInit(0.U(32.W))
  val rvalidReg = RegInit(false.B)
  val rlastReg  = RegInit(false.B)
  when(!io.resetn) {
    rdataReg  := 0.U
    rvalidReg := false.B
    rlastReg  := false.B
  }.elsewhen(busy && R_or_W && !r_retire) {
    rvalidReg := true.B
    rlastReg  := true.B
    rdataReg  := core.io.conf_rdata
  }.elsewhen(r_retire) {
    rvalidReg := false.B
  }

  io.rid    := buf_id
  io.rdata  := rdataReg
  io.rresp  := 0.U
  io.rlast  := rlastReg
  io.rvalid := rvalidReg

  // B 通道
  val bvalidReg = RegInit(false.B)
  when(!io.resetn) {
    bvalidReg := false.B
  }.elsewhen(w_enter) {
    bvalidReg := true.B
  }.elsewhen(b_retire) {
    bvalidReg := false.B
  }

  io.bid    := buf_id
  io.bresp  := 0.U
  io.bvalid := bvalidReg

  // --------------------------------------------------------------------------
  // 板级输出
  // --------------------------------------------------------------------------
  io.led         := core.io.led
  io.led_rg0     := core.io.led_rg0
  io.led_rg1     := core.io.led_rg1
  io.num_csn     := core.io.num_csn
  io.num_a_g     := core.io.num_a_g
  io.num_data    := core.io.num_data
  io.btn_key_col := core.io.btn_key_col

  // --------------------------------------------------------------------------
  // 随机延迟掩码（原文件 450~480 行）
  //   RANDOM_SEED = {7'b1010101, 16'h00FF}
  // --------------------------------------------------------------------------
  val switch_led = Cat(
    Fill(2, io.switch(7)), Fill(2, io.switch(6)), Fill(2, io.switch(5)), Fill(2, io.switch(4)),
    Fill(2, io.switch(3)), Fill(2, io.switch(2)), Fill(2, io.switch(1)), Fill(2, io.switch(0)))
  val led_r_n = ~switch_led

  val RANDOM_SEED = Cat(0x55.U(7.W), 0x00ff.U(16.W))   // {7'b1010101, 16'h00FF}

  val pseudo_random_23 = Reg(UInt(23.W))
  val no_mask          = RegInit(false.B)
  val short_delay      = RegInit(false.B)

  when(!io.resetn) {
    pseudo_random_23 := Mux(simulation.B, RANDOM_SEED, Cat(0x55.U(7.W), led_r_n))
    no_mask          := (pseudo_random_23(15, 0) === 0x00ff.U)
    short_delay      := (pseudo_random_23(7, 0) === 0xff.U)
  }.otherwise {
    pseudo_random_23 := Cat(pseudo_random_23(21, 0), pseudo_random_23(22) ^ pseudo_random_23(17))
  }

  // 逐位照抄原文件的 5 个掩码表达式（|no_mask 表示"全部无掩码"）
  val mask0 = (pseudo_random_23(10) & pseudo_random_23(20)) & (short_delay | (pseudo_random_23(11) ^ pseudo_random_23(5))) | no_mask
  val mask1 = (pseudo_random_23(9)  & pseudo_random_23(17)) & (short_delay | (pseudo_random_23(12) ^ pseudo_random_23(4))) | no_mask
  val mask2 = (pseudo_random_23(8)  ^ pseudo_random_23(22)) & (short_delay | (pseudo_random_23(13) ^ pseudo_random_23(3))) | no_mask
  val mask3 = (pseudo_random_23(7)  & pseudo_random_23(19)) & (short_delay | (pseudo_random_23(14) ^ pseudo_random_23(2))) | no_mask
  val mask4 = (pseudo_random_23(6)  ^ pseudo_random_23(16)) & (short_delay | (pseudo_random_23(15) ^ pseudo_random_23(1))) | no_mask

  io.ram_random_mask := Cat(mask4, mask3, mask2, mask1, mask0)
}
