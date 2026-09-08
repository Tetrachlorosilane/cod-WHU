// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：各实验 code/myCPU/regfile.v（exp5 起逐字相同）
//
//     reg [31:0] rf[31:0];
//     always @(posedge clk) begin if (we) rf[waddr] <= wdata; end
//     assign rdata1 = (raddr1==5'b0) ? 32'b0 : rf[raddr1];
//     assign rdata2 = (raddr2==5'b0) ? 32'b0 : rf[raddr2];

package envlib.common

import chisel3._

class Regfile extends Module {
  val io = IO(new Bundle {
    val raddr1 = Input(UInt(5.W))
    val rdata1 = Output(UInt(32.W))
    val raddr2 = Input(UInt(5.W))
    val rdata2 = Output(UInt(32.W))
    val we     = Input(Bool())
    val waddr  = Input(UInt(5.W))
    val wdata  = Input(UInt(32.W))
  })

  val rf = Mem(32, UInt(32.W))

  when(io.we) {
    rf.write(io.waddr, io.wdata)
  }

  io.rdata1 := Mux(io.raddr1 === 0.U, 0.U, rf.read(io.raddr1))
  io.rdata2 := Mux(io.raddr2 === 0.U, 0.U, rf.read(io.raddr2))
}
