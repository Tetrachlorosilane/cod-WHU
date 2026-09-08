// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/miniCPU/regfile.v
//
// 原 Verilog（两读一写；读地址为 0 时恒读 0）：
//     reg [31:0] rf[31:0];
//     always @(posedge clk) begin
//         if (we) rf[waddr] <= wdata;
//     end
//     assign rdata1 = (raddr1==5'b0) ? 32'b0 : rf[raddr1];
//     assign rdata2 = (raddr2==5'b0) ? 32'b0 : rf[raddr2];
//
// 注意：原 regfile 没有复位端口，也没有对 waddr==0 的写保护
//（写入 x0 是允许的，只是读出恒为 0），Chisel 版逐条保持一致。

package exp5.common

import chisel3._

/** 两读一写寄存器堆，读端口恒使能，读地址 0 恒返回 0。 */
class Regfile extends Module {
  val io = IO(new Bundle {
    // READ PORT 1
    val raddr1 = Input(UInt(5.W))
    val rdata1 = Output(UInt(32.W))
    // READ PORT 2
    val raddr2 = Input(UInt(5.W))
    val rdata2 = Output(UInt(32.W))
    // WRITE PORT
    val we    = Input(Bool())      // write enable, HIGH valid
    val waddr = Input(UInt(5.W))
    val wdata = Input(UInt(32.W))
  })

  // 原 Verilog 的 rf 是无复位寄存器堆；Mem 提供组合读（异步读），与原 assign 等价。
  val rf = Mem(32, UInt(32.W))

  // WRITE：与原 always @(posedge clk) if (we) rf[waddr] <= wdata; 等价
  when(io.we) {
    rf.write(io.waddr, io.wdata)
  }

  // READ OUT 1 / READ OUT 2
  io.rdata1 := Mux(io.raddr1 === 0.U, 0.U, rf.read(io.raddr1))
  io.rdata2 := Mux(io.raddr2 === 0.U, 0.U, rf.read(io.raddr2))
}
