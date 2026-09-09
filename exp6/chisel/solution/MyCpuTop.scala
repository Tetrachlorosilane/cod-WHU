// SPDX-License-Identifier: BSD-3-Clause
// 参考解（exp6 找错）：MyCpuTop.scala 的修正版——不参与默认编译。
// 用法：复制覆盖 ../src/main/scala/student/MyCpuTop.scala
// 修正点（与 ../MAPPING.md 错误映射表 #5~#7 对应）：
//   #5 u_alu.io.alu_src1 := alu_src1                （原误接 alu_src2）
//   #6 io.debug_wb_rf_we := Fill(4, rf_we)          （原恒 0 / 端口名拼错）
//   #7 final_result = Mux(res_from_mem, mem_result, alu_result)（原被截断成 1 位）
// 其余错误在 ../solution/Alu.scala。

package exp6.student

import chisel3._
import chisel3.util._
import exp6.common._

class MyCpuTop extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())

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

    // trace debug interface
    val debug_wb_pc       = Output(UInt(32.W))
    val debug_wb_rf_we    = Output(UInt(4.W))
    val debug_wb_rf_wnum  = Output(UInt(5.W))
    val debug_wb_rf_wdata = Output(UInt(32.W))
  })

  val reset_r = RegNext(!io.resetn, true.B)
  val valid = RegInit(false.B)
  when(reset_r) {
    valid := false.B
  }.otherwise {
    valid := true.B
  }

  val pc = RegInit(0x1bfffffcL.U(32.W))
  val seq_pc = pc + 4.U

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
  val i20      = inst(24, 5)
  val i16      = inst(25, 10)
  val i26      = Cat(inst(9, 0), inst(25, 10))

  val u_dec0 = Module(new Decoder6_64)
  u_dec0.io.in := op_31_26
  val op_31_26_d = u_dec0.io.out

  val u_dec1 = Module(new Decoder4_16)
  u_dec1.io.in := op_25_22
  val op_25_22_d = u_dec1.io.out

  val u_dec2 = Module(new Decoder2_4)
  u_dec2.io.in := op_21_20
  val op_21_20_d = u_dec2.io.out

  val u_dec3 = Module(new Decoder5_32)
  u_dec3.io.in := op_19_15
  val op_19_15_d = u_dec3.io.out

  val inst_add_w  = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x00)
  val inst_sub_w  = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x02)
  val inst_slt    = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x04)
  val inst_sltu   = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x05)
  val inst_nor    = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x08)
  val inst_and    = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x09)
  val inst_or     = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x0a)
  val inst_xor    = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x0b)
  val inst_slli_w = op_31_26_d(0x00) & op_25_22_d(0x1) & op_21_20_d(0x0) & op_19_15_d(0x01)
  val inst_srli_w = op_31_26_d(0x00) & op_25_22_d(0x1) & op_21_20_d(0x0) & op_19_15_d(0x09)
  val inst_srai_w = op_31_26_d(0x00) & op_25_22_d(0x1) & op_21_20_d(0x0) & op_19_15_d(0x11)
  val inst_addi_w = op_31_26_d(0x00) & op_25_22_d(0x0a)
  val inst_ld_w   = op_31_26_d(0x0a) & op_25_22_d(0x2)
  val inst_st_w   = op_31_26_d(0x0a) & op_25_22_d(0x6)
  val inst_jirl   = op_31_26_d(0x13)
  val inst_b      = op_31_26_d(0x14)
  val inst_bl     = op_31_26_d(0x15)
  val inst_beq    = op_31_26_d(0x16)
  val inst_bne    = op_31_26_d(0x17)
  val inst_lu12i_w = op_31_26_d(0x05) & ~inst(25)

  val alu_op = Cat(
    inst_lu12i_w,
    inst_srai_w,
    inst_srli_w,
    inst_slli_w,
    inst_xor,
    inst_or,
    inst_nor,
    inst_and,
    inst_sltu,
    inst_slt,
    inst_sub_w,
    inst_add_w | inst_addi_w | inst_ld_w | inst_st_w | inst_jirl | inst_bl
  )

  val need_ui5  = inst_slli_w | inst_srli_w | inst_srai_w
  val need_si12 = inst_addi_w | inst_ld_w | inst_st_w
  val need_si16 = inst_jirl | inst_beq | inst_bne
  val need_si20 = inst_lu12i_w
  val need_si26 = inst_b | inst_bl
  val src2_is_4 = inst_jirl | inst_bl

  val imm = Mux(src2_is_4, 4.U(32.W),
            Mux(need_si20, Cat(i20, 0.U(12.W)),
              Cat(Fill(20, i12(11)), i12)))

  val br_offs = Mux(need_si26, Cat(Fill(4, i26(25)), i26, 0.U(2.W)),
                                Cat(Fill(14, i16(15)), i16, 0.U(2.W)))

  val jirl_offs = Cat(Fill(14, i16(15)), i16, 0.U(2.W))

  val src_reg_is_rd = inst_beq | inst_bne | inst_st_w
  val src1_is_pc    = inst_jirl | inst_bl
  val src2_is_imm   = inst_slli_w | inst_srli_w | inst_srai_w | inst_addi_w |
                      inst_ld_w   | inst_st_w   | inst_lu12i_w | inst_jirl   | inst_bl

  val res_from_mem = inst_ld_w
  val dst_is_r1    = inst_bl
  val gr_we        = ~inst_st_w & ~inst_beq & ~inst_bne & ~inst_b & ~inst_bl
  val mem_we       = inst_st_w
  val dest         = Mux(dst_is_r1, 1.U(5.W), rd)

  val rf_raddr1 = rj
  val rf_raddr2 = Mux(src_reg_is_rd, rd, rk)
  val rf_we     = Wire(Bool())
  val rf_waddr  = Wire(UInt(5.W))
  val rf_wdata  = Wire(UInt(32.W))

  val u_regfile = Module(new Regfile)
  u_regfile.io.raddr1 := rf_raddr1
  u_regfile.io.raddr2 := rf_raddr2
  u_regfile.io.we     := rf_we
  u_regfile.io.waddr  := rf_waddr
  u_regfile.io.wdata  := rf_wdata

  val rj_value  = u_regfile.io.rdata1
  val rkd_value = u_regfile.io.rdata2

  val rj_eq_rd = (rj_value === rkd_value)
  val br_taken = ((inst_beq && rj_eq_rd) || (inst_bne && !rj_eq_rd) ||
                  inst_jirl || inst_bl || inst_b) && valid
  val br_target = Mux(inst_beq | inst_bne | inst_bl | inst_b,
                      pc + br_offs,
                      rj_value + jirl_offs)
  val nextpc = Mux(br_taken, br_target, seq_pc)

  val alu_src1 = Mux(src1_is_pc, pc, rj_value)
  val alu_src2 = Mux(src2_is_imm, imm, rkd_value)

  val u_alu = Module(new Alu)
  u_alu.io.alu_op := alu_op
  u_alu.io.alu_src1 := alu_src1          // 修正 #5
  u_alu.io.alu_src2 := alu_src2
  val alu_result = u_alu.io.alu_result

  io.data_sram_we    := mem_we && valid
  io.data_sram_addr  := alu_result
  io.data_sram_wdata := rkd_value

  val mem_result = io.data_sram_rdata

  val final_result = Mux(res_from_mem, mem_result, alu_result)   // 修正 #7

  rf_we    := gr_we && valid
  rf_waddr := dest
  rf_wdata := final_result

  io.debug_wb_pc := pc
  io.debug_wb_rf_we    := Fill(4, rf_we)   // 修正 #6
  io.debug_wb_rf_wnum  := dest
  io.debug_wb_rf_wdata := final_result

  when(reset_r) {
    pc := 0x1bfffffcL.U(32.W)
  }.otherwise {
    pc := nextpc
  }
}
