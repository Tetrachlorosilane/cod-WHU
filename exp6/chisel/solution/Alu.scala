// SPDX-License-Identifier: BSD-3-Clause
// 参考解（exp6 找错）：Alu.scala 的修正版——不参与默认编译。
// 用法：复制覆盖 ../src/main/scala/student/Alu.scala
// 修正点（与 ../MAPPING.md 错误映射表 #1'~#4 对应）：
//   #1' or_result   = alu_src1 | alu_src2          （原为自引用/操作数错误）
//   #2  sll_result  = alu_src1 << alu_src2[4:0]    （原操作数写反）
//   #3  sr64_result = {32{op_sra & alu_src1[31]}, alu_src1} >> alu_src2[4:0]（原操作数写反）
//   #4  sr_result   = sr64_result[31:0]            （原位选范围写错）

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

  val adder_b   = Mux(op_sub | op_slt | op_sltu, ~io.alu_src2, io.alu_src2)
  val adder_cin = Mux(op_sub | op_slt | op_sltu, 1.U(1.W), 0.U(1.W))
  val adder_res = io.alu_src1 +& adder_b +& adder_cin
  val adder_result = adder_res(31, 0)
  val adder_cout   = adder_res(32)

  val add_sub_result = adder_result

  val slt_result = Cat(0.U(31.W), (io.alu_src1(31) & ~io.alu_src2(31)) |
                                  ((~(io.alu_src1(31) ^ io.alu_src2(31))) & adder_result(31)))
  val sltu_result = Cat(0.U(31.W), ~adder_cout)

  val and_result = io.alu_src1 & io.alu_src2
  val or_result  = io.alu_src1 | io.alu_src2          // 修正 #1'
  val nor_result = ~or_result
  val xor_result = io.alu_src1 ^ io.alu_src2
  val lui_result = io.alu_src2

  val sll_result = io.alu_src1 << io.alu_src2(4, 0)   // 修正 #2

  val sr64_result = Cat(Fill(32, op_sra & io.alu_src1(31)), io.alu_src1) >> io.alu_src2(4, 0) // 修正 #3
  val sr_result   = sr64_result(31, 0)                // 修正 #4

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
