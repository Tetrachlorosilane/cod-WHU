// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 参考解（exp5 填空的答案）—— 不参与默认编译（位于 src/main/scala 之外）。
// 用法：把本文件复制覆盖 src/main/scala/student/MinicpuTop.scala 后运行
//       ./mill chisel.test
//
// 9 处填空的答案（与 code/miniCPU/minicpu_top.v 的填空位置一一对应）：
//   1. inst_st_w  : op_31_26_d(0x0a) & op_25_22_d(0x6)
//   2. src2_is_imm: inst_addi_w | inst_ld_w | inst_st_w
//   3. regfile.raddr1 : rf_raddr1
//   4. regfile.raddr2 : rf_raddr2
//   5. regfile.waddr  : rd
//   6. br_offs    : {{14{i16[15]}}, i16[15:0], 2'b0}
//   7. nextpc     : br_taken ? br_target : pc + 4
//   8. alu_src2   : src2_is_imm ? imm : rkd_value
//   9. rf_wdata   : res_from_mem ? data_sram_rdata : alu_result
// ============================================================================

package exp5.student

import chisel3._
import chisel3.util._
import exp5.common._

class MinicpuTop extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())          // 低有效

    // inst sram interface
    val inst_sram_we    = Output(Bool())
    val inst_sram_addr  = Output(UInt(32.W))
    val inst_sram_wdata = Output(UInt(32.W))
    val inst_sram_rdata = Input(UInt(32.W))

    // data sram interface
    val data_sram_we    = Output(Bool())
    val data_sram_addr  = Output(UInt(32.W))
    val data_sram_wdata = Output(UInt(32.W))
    val data_sram_rdata = Input(UInt(32.W))
  })

  val reset_r = RegNext(!io.resetn, true.B)
  val valid = RegInit(false.B)
  when(reset_r) {
    valid := false.B
  }.otherwise {
    valid := true.B
  }

  val pc = RegInit(0x1bfffffcL.U(32.W))

  io.inst_sram_we    := false.B
  io.inst_sram_addr  := pc
  io.inst_sram_wdata := 0.U(32.W)
  val inst = io.inst_sram_rdata

  val op_31_26 = inst(31, 26)
  val op_25_22 = inst(25, 22)
  val op_21_20 = inst(21, 20)
  val op_19_15 = inst(19, 15)
  val rd       = inst(4, 0)
  val rj       = inst(9, 5)
  val rk       = inst(14, 10)
  val i12      = inst(21, 10)
  val i16      = inst(25, 10)

  val u_dec0 = Module(new Decoder6_64)
  u_dec0.io.in := op_31_26
  val op_31_26_d = u_dec0.io.co

  val u_dec1 = Module(new Decoder4_16)
  u_dec1.io.in := op_25_22
  val op_25_22_d = u_dec1.io.co

  val u_dec2 = Module(new Decoder2_4)
  u_dec2.io.in := op_21_20
  val op_21_20_d = u_dec2.io.co

  val u_dec3 = Module(new Decoder5_32)
  u_dec3.io.in := op_19_15
  val op_19_15_d = u_dec3.io.co

  val inst_add_w  = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x00)
  val inst_addi_w = op_31_26_d(0x00) & op_25_22_d(0xa)
  val inst_ld_w   = op_31_26_d(0x0a) & op_25_22_d(0x2)

  // 填空 1/9
  val inst_st_w = op_31_26_d(0x0a) & op_25_22_d(0x6)

  val inst_bne = op_31_26_d(0x17)

  // 填空 2/9
  val src2_is_imm = inst_addi_w | inst_ld_w | inst_st_w

  val res_from_mem  = inst_ld_w
  val gr_we         = inst_add_w | inst_ld_w | inst_addi_w
  val mem_we        = inst_st_w
  val src_reg_is_rd = inst_bne | inst_st_w

  val rf_raddr1 = rj
  val rf_raddr2 = Mux(src_reg_is_rd, rd, rk)

  val rf_wdata = Wire(UInt(32.W))

  val u_regfile = Module(new Regfile)
  u_regfile.io.raddr1 := rf_raddr1   // 填空 3/9
  u_regfile.io.raddr2 := rf_raddr2   // 填空 4/9
  u_regfile.io.we     := gr_we
  u_regfile.io.waddr  := rd          // 填空 5/9
  u_regfile.io.wdata  := rf_wdata

  val rj_value  = u_regfile.io.rdata1
  val rkd_value = u_regfile.io.rdata2

  // 填空 6/9
  val br_offs = Cat(Fill(14, i16(15)), i16, 0.U(2.W))

  val br_target = pc + br_offs
  val rj_eq_rd  = (rj_value === rkd_value)
  val br_taken  = valid && inst_bne && !rj_eq_rd

  // 填空 7/9
  val nextpc = Mux(br_taken, br_target, pc + 4.U)

  val imm      = Cat(Fill(20, i12(11)), i12)
  val alu_src1 = rj_value

  // 填空 8/9
  val alu_src2 = Mux(src2_is_imm, imm, rkd_value)

  val alu_result = alu_src1 + alu_src2

  io.data_sram_we    := mem_we
  io.data_sram_addr  := alu_result
  io.data_sram_wdata := rkd_value

  // 填空 9/9
  rf_wdata := Mux(res_from_mem, io.data_sram_rdata, alu_result)

  when(reset_r) {
    pc := 0x1bfffffcL.U(32.W)
  }.otherwise {
    pc := nextpc
  }
}
