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
                             0xc, 0xc, 0xc, 0xc, 0xc, 0xc, 0xc, 0x15).map(_.U(6.W)))
  val tlb_asid = VecInit((0 until 16).map(_.U(10.W)))
  val tlb_g    = VecInit(Seq(0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0).map(_.U(1.W)))
  val tlb_ppn0 = VecInit(Seq(0x1000, 0x222, 0x333, 0x444, 0x555, 0x666, 0x777, 0x888,
                             0x999, 0xaaa, 0xbbb, 0xccc, 0xddd, 0xeee, 0xfff, 0x2000).map(_.U(20.W)))
  val tlb_plv0 = VecInit(Seq.fill(16)(0.U(2.W)))
  val tlb_mat0 = VecInit(Seq.fill(16)(1.U(2.W)))
  val tlb_d0   = VecInit(Seq.fill(16)(true.B))
  val tlb_v0   = VecInit(Seq.fill(16)(true.B))
  val tlb_ppn1 = VecInit(Seq(0x1100, 0x033, 0x044, 0x055, 0x066, 0x077, 0x088, 0x099,
                             0x0aa, 0x0bb, 0x0cc, 0x0dd, 0x0ee, 0x0ff, 0x000, 0x2100).map(_.U(20.W)))
  val tlb_plv1 = VecInit(Seq.fill(16)(0.U(2.W)))
  val tlb_mat1 = VecInit(Seq.fill(16)(1.U(2.W)))
  val tlb_d1   = VecInit(Seq.fill(16)(true.B))
  val tlb_v1   = VecInit(Seq.fill(16)(true.B))

  // --------------------------------------------------------------------------
  // 查找测试的期望值表（26 项；未命中的项其期望数据字段填 0，不参与比对）
  // --------------------------------------------------------------------------
  val s_test_vppn = VecInit(Seq(
    0x1000, 0x1100, 0x1000, 0x111, 0x111, 0x222, 0x222, 0x333, 0x333,
    0x444, 0x444, 0x555, 0x666, 0x666, 0x666, 0x777, 0x888, 0x999,
    0xaaa, 0xbbb, 0xccc, 0xddd, 0xeee, 0xf000, 0xabc, 0x123).map(_.U(19.W)))
  val s_test_va_bit12 = VecInit(Seq(
    1, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1).map(_.B))
  val s_test_asid = VecInit(Seq(
    0x0, 0x0, 0x1, 0x0, 0x1, 0x0, 0x2, 0x3, 0x4, 0x4, 0x5, 0x5, 0x6,
    0x7, 0x8, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0xf, 0x3).map(_.U(10.W)))
  val s_test_found = VecInit(Seq(
    1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0).map(_.B))
  val s_test_ppn = VecInit(Seq(
    0x1000, 0x1100, 0x0, 0x033, 0x222, 0x0, 0x333, 0x444, 0x055, 0x555, 0x666, 0x0,
    0x777, 0x099, 0x0, 0x0, 0x999, 0x0bb, 0xbbb, 0x0dd, 0xddd, 0x0ff, 0xfff, 0x2000, 0x0, 0x0).map(_.U(20.W)))
  val s_test_ps = VecInit(Seq(
    0x15, 0x15, 0x0, 0xc, 0xc, 0x0, 0xc, 0xc, 0xc, 0xc, 0xc, 0x0, 0xc, 0xc, 0x0, 0x0,
    0xc, 0xc, 0xc, 0xc, 0xc, 0xc, 0xc, 0x15, 0x0, 0x0).map(_.U(6.W)))
  val s_test_plv = VecInit(Seq.fill(26)(0.U(2.W)))
  val s_test_mat = VecInit(Seq.fill(26)(1.U(2.W)))
  val s_test_d   = VecInit(Seq.fill(26)(true.B))
  val s_test_v   = VecInit(Seq.fill(26)(true.B))

  val s0_test_id = Cat(tlb_s_cnt, false.B)
  val s1_test_id = Cat(tlb_s_cnt, true.B)

  // --------------------------------------------------------------------------
  // 被验证的 TLB（学生模块）
  // --------------------------------------------------------------------------
  val tlb = Module(new exp17.student.Tlb(tlbNum))

  tlb.io.resetn := io.resetn

  // 写端口（写测试驱动）
  tlb.io.we      := !tlb_w_test_ok
  tlb.io.w_index := tlb_w_cnt
  tlb.io.w_e     := tlb_e(tlb_w_cnt)
  tlb.io.w_ps    := tlb_ps(tlb_w_cnt)
  tlb.io.w_vppn  := tlb_vppn(tlb_w_cnt)
  tlb.io.w_asid  := tlb_asid(tlb_w_cnt)
  tlb.io.w_g     := tlb_g(tlb_w_cnt)
  tlb.io.w_ppn0  := tlb_ppn0(tlb_w_cnt)
  tlb.io.w_plv0  := tlb_plv0(tlb_w_cnt)
  tlb.io.w_mat0  := tlb_mat0(tlb_w_cnt)
  tlb.io.w_d0    := tlb_d0(tlb_w_cnt)
  tlb.io.w_v0    := tlb_v0(tlb_w_cnt)
  tlb.io.w_ppn1  := tlb_ppn1(tlb_w_cnt)
  tlb.io.w_plv1  := tlb_plv1(tlb_w_cnt)
  tlb.io.w_mat1  := tlb_mat1(tlb_w_cnt)
  tlb.io.w_d1    := tlb_d1(tlb_w_cnt)
  tlb.io.w_v1    := tlb_v1(tlb_w_cnt)

  // 读端口（读测试驱动）
  tlb.io.r_index := tlb_r_cnt
  tlb.io.r_e     := true.B

  // 查找端口（查找测试驱动）
  tlb.io.s0_vppn     := s_test_vppn(s0_test_id)
  tlb.io.s0_va_bit12 := s_test_va_bit12(s0_test_id)
  tlb.io.s0_asid     := s_test_asid(s0_test_id)
  tlb.io.s1_vppn     := s_test_vppn(s1_test_id)
  tlb.io.s1_va_bit12 := s_test_va_bit12(s1_test_id)
  tlb.io.s1_asid     := s_test_asid(s1_test_id)

  // 本实验不使用 invtlb
  tlb.io.invtlb_valid := false.B
  tlb.io.invtlb_op    := 0.U(5.W)

  // --------------------------------------------------------------------------
  // 读测试比对
  // --------------------------------------------------------------------------
  val r_error = (tlb.io.r_e    =/= tlb_e(tlb_r_cnt))    ||
                (tlb.io.r_vppn =/= tlb_vppn(tlb_r_cnt)) ||
                (tlb.io.r_ps   =/= tlb_ps(tlb_r_cnt))   ||
                (tlb.io.r_asid =/= tlb_asid(tlb_r_cnt)) ||
                (tlb.io.r_g    =/= tlb_g(tlb_r_cnt))    ||
                (tlb.io.r_ppn0 =/= tlb_ppn0(tlb_r_cnt)) ||
                (tlb.io.r_plv0 =/= tlb_plv0(tlb_r_cnt)) ||
                (tlb.io.r_mat0 =/= tlb_mat0(tlb_r_cnt)) ||
                (tlb.io.r_d0   =/= tlb_d0(tlb_r_cnt))   ||
                (tlb.io.r_v0   =/= tlb_v0(tlb_r_cnt))   ||
                (tlb.io.r_ppn1 =/= tlb_ppn1(tlb_r_cnt)) ||
                (tlb.io.r_plv1 =/= tlb_plv1(tlb_r_cnt)) ||
                (tlb.io.r_mat1 =/= tlb_mat1(tlb_r_cnt)) ||
                (tlb.io.r_d1   =/= tlb_d1(tlb_r_cnt))   ||
                (tlb.io.r_v1   =/= tlb_v1(tlb_r_cnt))

  // --------------------------------------------------------------------------
  // 查找测试比对
  // --------------------------------------------------------------------------
  def searchErr(id: UInt, found: Bool, ppn: UInt, ps: UInt,
                plv: UInt, mat: UInt, d: Bool, v: Bool): Bool = {
    val hit = s_test_found(id)
    (hit ^ found) || (hit && (!found ||
      (ppn =/= s_test_ppn(id)) || (ps =/= s_test_ps(id)) || (plv =/= s_test_plv(id)) ||
      (mat =/= s_test_mat(id)) || (d =/= s_test_d(id)) || (v =/= s_test_v(id))))
  }

  val s0_error = searchErr(s0_test_id, tlb.io.s0_found, tlb.io.s0_ppn,
                           tlb.io.s0_ps, tlb.io.s0_plv, tlb.io.s0_mat, tlb.io.s0_d, tlb.io.s0_v)
  val s1_error = searchErr(s1_test_id, tlb.io.s1_found, tlb.io.s1_ppn,
                           tlb.io.s1_ps, tlb.io.s1_plv, tlb.io.s1_mat, tlb.io.s1_d, tlb.io.s1_v)

  // --------------------------------------------------------------------------
  // 三个测试的推进与通过标志
  // --------------------------------------------------------------------------
  when(!io.resetn) {
    tlb_w_test_ok := false.B
    tlb_w_cnt     := 0.U
  }.elsewhen(tlb_w_cnt === 0xf.U) {
    tlb_w_test_ok := true.B
  }.elsewhen(!tlb_w_test_ok && wait_1s) {
    tlb_w_cnt := tlb_w_cnt + 1.U
  }

  when(!io.resetn) {
    tlb_r_test_ok := false.B
    tlb_r_cnt     := 0.U
  }.elsewhen(tlb_r_cnt === 0xf.U && !r_error) {
    tlb_r_test_ok := true.B
  }.elsewhen(tlb_w_test_ok && !tlb_r_test_ok && !test_error && !r_error && wait_1s) {
    tlb_r_cnt := tlb_r_cnt + 1.U
  }

  when(!io.resetn) {
    tlb_s_test_ok := false.B
    tlb_s_cnt     := 0.U
  }.elsewhen(tlb_s_cnt === 0xc.U && !s0_error && !s1_error) {
    tlb_s_test_ok := true.B
  }.elsewhen(tlb_w_test_ok && !tlb_s_test_ok && !test_error && !s0_error && !s1_error && wait_1s) {
    tlb_s_cnt := tlb_s_cnt + 1.U
  }

  when(!io.resetn) {
    test_error := false.B
  }.elsewhen(tlb_w_test_ok && !tlb_r_test_ok && r_error) {
    test_error := true.B
  }.elsewhen(tlb_w_test_ok && !tlb_s_test_ok && (s0_error || s1_error)) {
    test_error := true.B
  }

  // --------------------------------------------------------------------------
  // 数码管显示（与原文件一致：显示 w_cnt / r_cnt / s0_id / s1_id）
  // --------------------------------------------------------------------------
  val count = RegInit(0.U(20.W))
  count := count + 1.U

  val scan_data = Reg(UInt(4.W))
  val num_csn   = Reg(UInt(8.W))

  val scan_sel = VecInit(Seq(
    Cat(0.U(3.W), s1_test_id(4)),
    s1_test_id(3, 0),
    Cat(0.U(3.W), s0_test_id(4)),
    s0_test_id(3, 0),
    0.U(4.W),
    tlb_r_cnt,
    0.U(4.W),
    tlb_w_cnt))
  val csn_sel = VecInit(Seq(
    0x7f.U(8.W), 0xbf.U(8.W), 0xdf.U(8.W), 0xef.U(8.W),
    0xf7.U(8.W), 0xfb.U(8.W), 0xfd.U(8.W), 0xfe.U(8.W)))

  when(!io.resetn) {
    scan_data := 0.U
    num_csn   := 0xff.U
  }.otherwise {
    scan_data := scan_sel(count(19, 17))
    num_csn   := csn_sel(count(19, 17))
  }

  val num_a_g = Reg(UInt(7.W))
  val seg_rom = VecInit(Seq(
    0x7e.U(7.W), 0x30.U(7.W), 0x6d.U(7.W), 0x79.U(7.W),
    0x33.U(7.W), 0x5b.U(7.W), 0x5f.U(7.W), 0x70.U(7.W),
    0x7f.U(7.W), 0x7b.U(7.W), 0x77.U(7.W), 0x1f.U(7.W),
    0x4e.U(7.W), 0x3d.U(7.W), 0x4f.U(7.W), 0x47.U(7.W)))

  when(!io.resetn) {
    num_a_g := 0.U
  }.otherwise {
    num_a_g := seg_rom(scan_data)
  }

  io.num_csn := num_csn
  io.num_a_g := num_a_g
  io.led := Cat(~test_error, 0xfff.U(12.W), ~tlb_w_test_ok, ~tlb_r_test_ok, ~tlb_s_test_ok)

  io.wOk := tlb_w_test_ok
  io.rOk := tlb_r_test_ok
  io.sOk := tlb_s_test_ok
  io.err := test_error
}
