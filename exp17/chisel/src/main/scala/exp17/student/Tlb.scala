// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（从零实现）：TLB —— 16 项、双查找端口 + 读写端口
//   对应原实验：本实验要求自行编写 `tlb.v`，模块名固定为 `tlb`
//   （原实验环境只提供 tlb_top.v 验证环境，不提供 TLB 本体）。
//
// 本实验（实践任务17）的要求：从零实现：
//   Chisel 版只给接口骨架 + TODO(实现)，内部逻辑全部留空（???）。
//   未实现时 elaboration 会以 NotImplementedError 终止，这是预期行为。
//
// 接口（与原书 9.1 节 TLB 模块接口一致）：
//   查找端口 0/1：输入 {vppn, va_bit12, asid}，输出 {found, index, ppn, ps, plv, mat, d, v}
//   写端口：we 有效时按 w_index 写入整个表项（含两个页表项 ppn0/1 及其属性）
//   读端口：r_e 有效时按 r_index 读出整个表项
//   invtlb：本实验不使用（验证环境固定接 0）
//
// 实现提示（见原书 9.1）：
//   * 表项可用 Vec of Bundle（或若干并列的 Reg(Vec(...))）表达；
//   * 查找：把 16 个表项的 {vppn, asid} 与输入比对，考虑 G（全局）位；
//     `va_bit12` 用于选择页表项 0/1，ps 决定 vppn 的有效位数；
//   * 命中时 index 为命中的表项号；未命中时 found=0，其余输出无意义。
// ============================================================================

package exp17.student

import chisel3._
import chisel3.util._

/** TLB 表项的字段（供学生实现时参考；也可以自行用并列的 Reg(Vec) 表达）。 */
class TlbEntry extends Bundle {
  val e    = Bool()
  val vppn = UInt(19.W)
  val ps   = UInt(6.W)
  val asid = UInt(10.W)
  val g    = Bool()
  val ppn0 = UInt(20.W)
  val plv0 = UInt(2.W)
  val mat0 = UInt(2.W)
  val d0   = Bool()
  val v0   = Bool()
  val ppn1 = UInt(20.W)
  val plv1 = UInt(2.W)
  val mat1 = UInt(2.W)
  val d1   = Bool()
  val v1   = Bool()
}

class Tlb(tlbNum: Int = 16) extends Module {
  val idxBits = log2Ceil(tlbNum)

  val io = IO(new Bundle {
    val resetn = Input(Bool())

    // ---- 查找端口 0 ----
    val s0_vppn     = Input(UInt(19.W))
    val s0_va_bit12 = Input(Bool())
    val s0_asid     = Input(UInt(10.W))
    val s0_found    = Output(Bool())
    val s0_index    = Output(UInt(idxBits.W))
    val s0_ppn      = Output(UInt(20.W))
    val s0_ps       = Output(UInt(6.W))
    val s0_plv      = Output(UInt(2.W))
    val s0_mat      = Output(UInt(2.W))
    val s0_d        = Output(Bool())
    val s0_v        = Output(Bool())

    // ---- 查找端口 1 ----
    val s1_vppn     = Input(UInt(19.W))
    val s1_va_bit12 = Input(Bool())
    val s1_asid     = Input(UInt(10.W))
    val s1_found    = Output(Bool())
    val s1_index    = Output(UInt(idxBits.W))
    val s1_ppn      = Output(UInt(20.W))
    val s1_ps       = Output(UInt(6.W))
    val s1_plv      = Output(UInt(2.W))
    val s1_mat      = Output(UInt(2.W))
    val s1_d        = Output(Bool())
    val s1_v        = Output(Bool())

    // ---- invtlb（本实验固定为 0） ----
    val invtlb_valid = Input(Bool())
    val invtlb_op    = Input(UInt(5.W))

    // ---- 写端口 ----
    val we      = Input(Bool())
    val w_index = Input(UInt(idxBits.W))
    val w_e     = Input(Bool())
    val w_vppn  = Input(UInt(19.W))
    val w_ps    = Input(UInt(6.W))
    val w_asid  = Input(UInt(10.W))
    val w_g     = Input(Bool())
    val w_ppn0  = Input(UInt(20.W))
    val w_plv0  = Input(UInt(2.W))
    val w_mat0  = Input(UInt(2.W))
    val w_d0    = Input(Bool())
    val w_v0    = Input(Bool())
    val w_ppn1  = Input(UInt(20.W))
    val w_plv1  = Input(UInt(2.W))
    val w_mat1  = Input(UInt(2.W))
    val w_d1    = Input(Bool())
    val w_v1    = Input(Bool())

    // ---- 读端口 ----
    val r_index = Input(UInt(idxBits.W))
    val r_e     = Output(Bool())
    val r_vppn  = Output(UInt(19.W))
    val r_ps    = Output(UInt(6.W))
    val r_asid  = Output(UInt(10.W))
    val r_g     = Output(Bool())
    val r_ppn0  = Output(UInt(20.W))
    val r_plv0  = Output(UInt(2.W))
    val r_mat0  = Output(UInt(2.W))
    val r_d0    = Output(Bool())
    val r_v0    = Output(Bool())
    val r_ppn1  = Output(UInt(20.W))
    val r_plv1  = Output(UInt(2.W))
    val r_mat1  = Output(UInt(2.W))
    val r_d1    = Output(Bool())
    val r_v1    = Output(Bool())
  })

  // --------------------------------------------------------------------------
  // ① 表项存储：tlbNum 个表项（复位后全部无效）
  // --------------------------------------------------------------------------
  // TODO(实现 1/4)：定义表项存储（建议 RegInit(VecInit(Seq.fill(tlbNum)(0.U.asTypeOf(new TlbEntry))))）

  // --------------------------------------------------------------------------
  // ② 写端口：we 有效时把 w_* 写入 w_index 对应的表项
  // --------------------------------------------------------------------------
  // TODO(实现 2/4)：写端口逻辑

  // --------------------------------------------------------------------------
  // ③ 读端口：r_index 直接索引，输出整个表项
  // --------------------------------------------------------------------------
  // TODO(实现 3/4)：读端口逻辑

  // --------------------------------------------------------------------------
  // ④ 双查找端口：按 {vppn, asid}（考虑 G 位与 va_bit12/ps）匹配，
  //    输出 found/index/ppn/ps/plv/mat/d/v
  // --------------------------------------------------------------------------
  // TODO(实现 4/4)：查找逻辑（两个端口各自独立）
  io.s0_found := ???
  io.s0_index := ???
  io.s0_ppn   := ???
  io.s0_ps    := ???
  io.s0_plv   := ???
  io.s0_mat   := ???
  io.s0_d     := ???
  io.s0_v     := ???

  io.s1_found := ???
  io.s1_index := ???
  io.s1_ppn   := ???
  io.s1_ps    := ???
  io.s1_plv   := ???
  io.s1_mat   := ???
  io.s1_d     := ???
  io.s1_v     := ???

  io.r_e    := ???
  io.r_vppn := ???
  io.r_ps   := ???
  io.r_asid := ???
  io.r_g    := ???
  io.r_ppn0 := ???
  io.r_plv0 := ???
  io.r_mat0 := ???
  io.r_d0   := ???
  io.r_v0   := ???
  io.r_ppn1 := ???
  io.r_plv1 := ???
  io.r_mat1 := ???
  io.r_d1   := ???
  io.r_v1   := ???
}
