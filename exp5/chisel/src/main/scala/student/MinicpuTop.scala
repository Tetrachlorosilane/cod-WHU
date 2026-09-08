// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 学生模块（填空）：miniCPU 单周期 CPU，5 条指令
//   对应原 Verilog：code/miniCPU/minicpu_top.v
//   本实验（实践任务5）的教学意图 = 填空：
//   原 Verilog 中有 9 处空位（`assign x = ;` 与 regfile 实例化的空括号），
//   Chisel 版同样保留 9 处空缺，用 `???` + TODO(填空) 标记。
//   —— 未补全时 elaboration 会以 NotImplementedError 终止，等价于原 Verilog
//      含空位时的语法错误：不补全就跑不起来。
//   参考解见 ../solution/MinicpuTop.scala（不参与默认编译）。
//
// 与 Verilog 的两点差异（见 ../MAPPING.md）：
//   1. clk 端口 → Chisel 隐式 clock，不再是 IO；
//   2. Chisel 要求「先声明后使用」，故 pc 的更新语句移到文件末尾，
//      组合逻辑顺序做了等价重排（信号名与含义不变）。
// ============================================================================

package exp5.student

import chisel3._
import chisel3.util._
import exp5.common._

class MinicpuTop extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())          // 低有效（原 Verilog 中由 SoC 的 cpu_resetn 驱动）

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

  // --------------------------------------------------------------------------
  // 原 Verilog：
  //   reg reset; always @(posedge clk) reset <= ~resetn;
  //   reg valid; always @(posedge clk) if (reset) valid <= 1'b0; else valid <= 1'b1;
  // （原 Verilog 中这两个 reg 无初值，仿真起始为 X；Chisel 版给确定性初值。）
  // --------------------------------------------------------------------------
  val reset = RegNext(!io.resetn, true.B)
  val valid = RegInit(false.B)
  when(reset) {
    valid := false.B
  }.otherwise {
    valid := true.B
  }

  // --------------------------------------------------------------------------
  // 原 Verilog：
  //   reg [31:0] pc;
  //   always @(posedge clk) begin
  //       if (reset) pc <= 32'h1bfffffc;   //trick: to make nextpc be 0x1c000000 during reset
  //       else       pc <= nextpc;
  //   end
  // 注意：pc 的更新语句放在本文件末尾（Chisel 需先声明 nextpc）。
  // --------------------------------------------------------------------------
  val pc = RegInit(0x1bfffffcL.U(32.W))

  // --------------------------------------------------------------------------
  // 取指接口
  //   assign inst_sram_we    = 1'b0;
  //   assign inst_sram_addr  = pc;
  //   assign inst_sram_wdata = 32'b0;
  //   assign inst            = inst_sram_rdata;
  // --------------------------------------------------------------------------
  io.inst_sram_we    := false.B
  io.inst_sram_addr  := pc
  io.inst_sram_wdata := 0.U(32.W)
  val inst = io.inst_sram_rdata

  // --------------------------------------------------------------------------
  // 指令字段
  // --------------------------------------------------------------------------
  val op_31_26 = inst(31, 26)
  val op_25_22 = inst(25, 22)
  val op_21_20 = inst(21, 20)
  val op_19_15 = inst(19, 15)
  val rd       = inst(4, 0)
  val rj       = inst(9, 5)
  val rk       = inst(14, 10)
  val i12      = inst(21, 10)
  val i16      = inst(25, 10)

  // --------------------------------------------------------------------------
  // 译码器（对应 code/miniCPU/tools.v 的 4 个 decoder 模块）
  //   decoder_6_64 u_dec0(.in(op_31_26 ), .co(op_31_26_d ));
  //   decoder_4_16 u_dec1(.in(op_25_22 ), .co(op_25_22_d ));
  //   decoder_2_4  u_dec2(.in(op_21_20 ), .co(op_21_20_d ));
  //   decoder_5_32 u_dec3(.in(op_19_15 ), .co(op_19_15_d ));
  // --------------------------------------------------------------------------
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

  // --------------------------------------------------------------------------
  // 指令译码
  // --------------------------------------------------------------------------
  val inst_add_w  = op_31_26_d(0x00) & op_25_22_d(0x0) & op_21_20_d(0x1) & op_19_15_d(0x00)
  val inst_addi_w = op_31_26_d(0x00) & op_25_22_d(0xa)
  val inst_ld_w   = op_31_26_d(0x0a) & op_25_22_d(0x2)

  // TODO(填空 1/9)：在这里实现inst_st_w指令的译码 @ code/miniCPU/minicpu_top.v:109
  val inst_st_w = Wire(Bool())
  inst_st_w := ???

  val inst_bne = op_31_26_d(0x17)

  // TODO(填空 2/9)：在这里实现立即数选择信号 @ code/miniCPU/minicpu_top.v:112
  val src2_is_imm = Wire(Bool())
  src2_is_imm := ???

  // --------------------------------------------------------------------------
  // 控制信号（原 Verilog 顺序：res_from_mem / gr_we / mem_we / src_reg_is_rd）
  // --------------------------------------------------------------------------
  val res_from_mem  = inst_ld_w
  val gr_we         = inst_add_w | inst_ld_w | inst_addi_w
  val mem_we        = inst_st_w
  val src_reg_is_rd = inst_bne | inst_st_w

  // --------------------------------------------------------------------------
  // 寄存器堆
  //   assign rf_raddr1 = rj;
  //   assign rf_raddr2 = src_reg_is_rd ? rd : rk;
  //   regfile u_regfile(.clk(clk), .raddr1( ), .rdata1(rj_value),
  //                     .raddr2( ), .rdata2(rkd_value), .we(gr_we),
  //                     .waddr( ), .wdata(rf_wdata));
  // --------------------------------------------------------------------------
  val rf_raddr1 = rj
  val rf_raddr2 = Mux(src_reg_is_rd, rd, rk)

  // 先声明后赋值：rf_wdata 由末尾的填空 9 驱动（Chisel 需先声明后使用）
  val rf_wdata = Wire(UInt(32.W))

  val u_regfile = Module(new Regfile)
  // TODO(填空 3/9)：在空出的括号里完成引脚匹配 @ code/miniCPU/minicpu_top.v:121
  u_regfile.io.raddr1 := ???
  // TODO(填空 4/9)：在空出的括号里完成引脚匹配 @ code/miniCPU/minicpu_top.v:123
  u_regfile.io.raddr2 := ???
  u_regfile.io.we     := gr_we
  // TODO(填空 5/9)：在空出的括号里完成引脚匹配 @ code/miniCPU/minicpu_top.v:127
  u_regfile.io.waddr  := ???
  u_regfile.io.wdata  := rf_wdata

  val rj_value  = u_regfile.io.rdata1
  val rkd_value = u_regfile.io.rdata2

  // --------------------------------------------------------------------------
  // 分支
  //   assign br_offs   = ;
  //   assign br_target = pc + br_offs;
  //   assign rj_eq_rd  = (rj_value == rkd_value);
  //   assign br_taken  = valid && inst_bne && !rj_eq_rd;
  //   assign nextpc    = ;
  // --------------------------------------------------------------------------
  // TODO(填空 6/9)：在这里完成br_offs信号的生成 @ code/miniCPU/minicpu_top.v:131
  val br_offs = Wire(UInt(32.W))
  br_offs := ???

  val br_target = pc + br_offs
  val rj_eq_rd  = (rj_value === rkd_value)
  val br_taken  = valid && inst_bne && !rj_eq_rd

  // TODO(填空 7/9)：在这里实现nextpc信号的生成 @ code/miniCPU/minicpu_top.v:135
  val nextpc = Wire(UInt(32.W))
  nextpc := ???

  // --------------------------------------------------------------------------
  // 运算
  //   assign imm      = {{20{i12[11]}}, i12[11:0]};
  //   assign alu_src1 = rj_value;
  //   assign alu_src2 = ;
  //   assign alu_result = alu_src1 + alu_src2;
  // --------------------------------------------------------------------------
  val imm      = Cat(Fill(20, i12(11)), i12)
  val alu_src1 = rj_value

  // TODO(填空 8/9)：在这里实现alu_src2信号 @ code/miniCPU/minicpu_top.v:139
  val alu_src2 = Wire(UInt(32.W))
  alu_src2 := ???

  val alu_result = alu_src1 + alu_src2

  // --------------------------------------------------------------------------
  // 访存
  //   assign data_sram_we    = mem_we;
  //   assign data_sram_addr  = alu_result;
  //   assign data_sram_wdata = rkd_value;
  // --------------------------------------------------------------------------
  io.data_sram_we    := mem_we
  io.data_sram_addr  := alu_result
  io.data_sram_wdata := rkd_value

  // --------------------------------------------------------------------------
  // 写回
  //   assign rf_wdata = ;   //在这里完成写回寄存器值的选择
  // --------------------------------------------------------------------------
  // TODO(填空 9/9)：在这里完成写回寄存器值的选择 @ code/miniCPU/minicpu_top.v:147
  rf_wdata := ???

  // --------------------------------------------------------------------------
  // pc 更新（原 Verilog 中位于文件前部；Chisel 需先声明 nextpc，故移到这里）
  // --------------------------------------------------------------------------
  when(reset) {
    pc := 0x1bfffffcL.U(32.W)
  }.otherwise {
    pc := nextpc
  }
}
