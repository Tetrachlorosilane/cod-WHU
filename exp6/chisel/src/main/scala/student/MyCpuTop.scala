// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（找错）：myCPU 单周期 CPU（20 条指令）
//   对应原 Verilog：code/myCPU/mycpu_top.v
//
// 本实验（实践任务6）的要求：找错。原 myCPU/ 目录被"加入了若干错误"，
// 学生通过仿真波形 + golden_trace.txt 比对找出并修复。Chisel 版保留同族错误：
//   #5  ALU 的 alu_src1 端口误接成 alu_src2        （原 mycpu_top.v:253）
//   #6  debug_wb_rf_we 未被正确驱动（原端口名拼错） （原 mycpu_top.v:271）
//   #7  final_result 位宽被截断成 1 位             （原 mycpu_top.v:263，未声明 → 隐式 1 位线网）
//   其余 4 处错误在 ../student/Alu.scala 中
//
// 请勿修正这些错误——它们就是本实验要你找出来的东西。
// 参考解见 ../solution/MyCpuTop.scala 与 ../solution/Alu.scala。
// ============================================================================

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

  // reg reset; always @(posedge clk) reset <= ~resetn;
  val reset_r = RegNext(!io.resetn, true.B)
  val valid = RegInit(false.B)
  when(reset_r) {
    valid := false.B
  }.otherwise {
    valid := true.B
  }

  val pc = RegInit(0x1bfffffcL.U(32.W))
  val seq_pc = pc + 4.U

  // ---------------- 取指 ----------------
  io.inst_sram_we    := false.B
  io.inst_sram_addr  := pc
  io.inst_sram_wdata := 0.U(32.W)
  val inst = io.inst_sram_rdata

  // ---------------- 指令字段 ----------------
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

  // ---------------- 指令译码 ----------------
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

  // ---------------- ALU 控制码 ----------------
  val alu_op = Cat(
    inst_lu12i_w,                                                          // [11]
    inst_srai_w,                                                           // [10]
    inst_srli_w,                                                           // [ 9]
    inst_slli_w,                                                           // [ 8]
    inst_xor,                                                              // [ 7]
    inst_or,                                                               // [ 6]
    inst_nor,                                                              // [ 5]
    inst_and,                                                              // [ 4]
    inst_sltu,                                                             // [ 3]
    inst_slt,                                                              // [ 2]
    inst_sub_w,                                                            // [ 1]
    inst_add_w | inst_addi_w | inst_ld_w | inst_st_w | inst_jirl | inst_bl  // [ 0]
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

  // ---------------- 寄存器堆 ----------------
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

  // ---------------- 分支 ----------------
  val rj_eq_rd = (rj_value === rkd_value)
  val br_taken = ((inst_beq && rj_eq_rd) || (inst_bne && !rj_eq_rd) ||
                  inst_jirl || inst_bl || inst_b) && valid
  val br_target = Mux(inst_beq | inst_bne | inst_bl | inst_b,
                      pc + br_offs,
                      rj_value + jirl_offs)
  val nextpc = Mux(br_taken, br_target, seq_pc)

  // ---------------- ALU ----------------
  val alu_src1 = Mux(src1_is_pc, pc, rj_value)
  val alu_src2 = Mux(src2_is_imm, imm, rkd_value)

  val u_alu = Module(new Alu)
  u_alu.io.alu_op := alu_op
  // TODO(找错 #5)：原 mycpu_top.v:253 把 alu_src1 端口误接成 alu_src2
  u_alu.io.alu_src1 := alu_src2
  u_alu.io.alu_src2 := alu_src2
  val alu_result = u_alu.io.alu_result

  // ---------------- 访存 ----------------
  io.data_sram_we    := mem_we && valid
  io.data_sram_addr  := alu_result
  io.data_sram_wdata := rkd_value

  val mem_result = io.data_sram_rdata

  // TODO(找错 #7)：原 mycpu_top.v:263 的 final_result 未声明 → Verilog 隐式生成
  //   1 位线网，写回值只保留最低位。Chisel 用 (0) 显式保留同样的截断行为。
  val final_result = Mux(res_from_mem, mem_result, alu_result)(0).asUInt.pad(32)

  rf_we    := gr_we && valid
  rf_waddr := dest
  rf_wdata := final_result

  // ---------------- debug info generate ----------------
  io.debug_wb_pc := pc
  // TODO(找错 #6)：原 mycpu_top.v:271 写成 debug_wb_rf_wen（与端口名 debug_wb_rf_we 不符），
  //   该输出从未被驱动（波形为 Z）。Chisel 无 Z，等价表现为 debug 写使能恒 0。
  io.debug_wb_rf_we    := 0.U(4.W)
  io.debug_wb_rf_wnum  := dest
  io.debug_wb_rf_wdata := final_result

  // ---------------- pc 更新 ----------------
  when(reset_r) {
    pc := 0x1bfffffcL.U(32.W)
  }.otherwise {
    pc := nextpc
  }
}
