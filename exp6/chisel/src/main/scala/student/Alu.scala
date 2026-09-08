// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（找错）：ALU —— 对应原 Verilog：code/myCPU/alu.v
// 本实验（实践任务6）的教学意图 = 找错：原 myCPU/ 里被"加入了若干错误"。
// 本文件保留与 alu.v 同族的**功能错误**（共 4 处，见 ../MAPPING.md 错误映射表）。
//   #2 sll_result 的操作数写反          （原 alu.v:80）
//   #3 sr64_result 的操作数写反         （原 alu.v:83）
//   #4 sr_result 位选范围写错           （原 alu.v:85）
//   #1' 原 alu.v:74 的 `or_result = alu_src1 | alu_src2 | alu_result` 是**组合自引用**
//      （与 alu_result 成环）。Chisel/FIRRTL 无法 elaborate 组合环，故按
//      CHISEL-CONVENTIONS.md §6 用等价的"OR 操作数错误"替代，详见 MAPPING.md。
// 请勿修正这些错误——它们就是本实验要你找出来的东西。
// 参考解见 ../solution/Alu.scala。
// ============================================================================

package exp6.student

import chisel3._
import chisel3.util._

class Alu extends Module {
  val io = IO(new Bundle {
    val alu_op     = Input(UInt(12.W))
    val alu_src1   = Input(UInt(32.W))
    val alu_src2   = Input(UInt(32.W))
    val alu_result = Output(UInt(32.W))
  })

  // control code decomposition
  val op_add  = io.alu_op(0)
  val op_sub  = io.alu_op(1)
  val op_slt  = io.alu_op(2)
  val op_sltu = io.alu_op(3)
  val op_and  = io.alu_op(4)
  val op_nor  = io.alu_op(5)
  val op_or   = io.alu_op(6)
  val op_xor  = io.alu_op(7)
  val op_sll  = io.alu_op(8)
  val op_srl  = io.alu_op(9)
  val op_sra  = io.alu_op(10)
  val op_lui  = io.alu_op(11)

  // 32-bit adder
  val adder_b   = Mux(op_sub | op_slt | op_sltu, ~io.alu_src2, io.alu_src2)
  val adder_cin = Mux(op_sub | op_slt | op_sltu, 1.U(1.W), 0.U(1.W))
  val adder_res = io.alu_src1 +& adder_b +& adder_cin
  val adder_result = adder_res(31, 0)
  val adder_cout   = adder_res(32)

  // ADD, SUB result
  val add_sub_result = adder_result

  // SLT result
  val slt_result = Cat(0.U(31.W), (io.alu_src1(31) & ~io.alu_src2(31)) |
                                  ((~(io.alu_src1(31) ^ io.alu_src2(31))) & adder_result(31)))

  // SLTU result
  val sltu_result = Cat(0.U(31.W), ~adder_cout)

  // bitwise operation
  val and_result = io.alu_src1 & io.alu_src2

  // TODO(找错 #1)：原 Verilog alu.v:74 为 `or_result = alu_src1 | alu_src2 | alu_result;`
  //   —— 与 alu_result 形成组合自引用（FIRRTL 会报 combinational loop）。
  //   此处保留同族的"OR 操作数错误"（漏掉 alu_src2），效果同样是 or 指令结果错误。
  val or_result = io.alu_src1

  val nor_result = ~or_result
  val xor_result = io.alu_src1 ^ io.alu_src2
  val lui_result = io.alu_src2

  // TODO(找错 #2)：操作数写反，原 alu.v:80 为 `alu_src2 << alu_src1[4:0]`
  val sll_result = io.alu_src2 << io.alu_src1(4, 0)

  // TODO(找错 #3)：被移数与移位量都写反，原 alu.v:83 为
  //   `{{32{op_sra & alu_src2[31]}}, alu_src2[31:0]} >> alu_src1[4:0]`
  val sr64_result = Cat(Fill(32, op_sra & io.alu_src2(31)), io.alu_src2) >> io.alu_src1(4, 0)

  // TODO(找错 #4)：位选范围写错，原 alu.v:85 为 `sr_result = sr64_result[30:0]`
  //   （31 位赋给 32 位线网 → 最高位恒 0，右移结果被截断）
  val sr_result = sr64_result(30, 0).pad(32)

  // final result mux
  io.alu_result := (Fill(32, op_add | op_sub) & add_sub_result) |
    (Fill(32, op_slt)  & slt_result) |
    (Fill(32, op_sltu) & sltu_result) |
    (Fill(32, op_and)  & and_result) |
    (Fill(32, op_nor)  & nor_result) |
    (Fill(32, op_or)   & or_result) |
    (Fill(32, op_xor)  & xor_result) |
    (Fill(32, op_lui)  & lui_result) |
    (Fill(32, op_sll)  & sll_result) |
    (Fill(32, op_srl | op_sra) & sr_result)
}
