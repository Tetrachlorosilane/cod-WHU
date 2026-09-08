// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/rtl/soc_mini_top.v
//
// 原 Verilog 结构（仿真时 `SIMU_USE_PLL = 0，tb_top 用默认参数 SIMULATION=1'b0）：
//     wire cpu_clk;  reg cpu_resetn;
//     always @(posedge cpu_clk) cpu_resetn <= resetn;
//     generate if(SIMULATION && `SIMU_USE_PLL==0) assign cpu_clk = clk;
//              else clk_pll clk_pll(.clk_in1(clk), .cpu_clk(cpu_clk), .timer_clk()); endgenerate
//     assign cpu_data_rdata = (cpu_data_addr == 12'd1024) ? {24'b0, ~switch[7:0]} : 32'b0;
//     inst_ram  : .a(cpu_inst_addr[17:2])
//     assign conf_we    = cpu_data_we && cpu_data_addr == 12'd1028;
//     assign conf_wdata = cpu_data_wdata;
//     assign led = ~conf_led;
//
// 时钟：Chisel 使用隐式 clock，等价于原 generate 的 speedup_simulation 分支
//（clk_pll 不建模，见 CHISEL-CONVENTIONS.md §6.2；usePll 参数仅作记录用途）。

package exp5.soc

import chisel3._
import chisel3.util._
import exp5.student.MinicpuTop

class SocMiniTop(
  program:  Seq[BigInt] = InstRamProgram.words,
  usePll:   Boolean     = false,   // 对应原 SIMU_USE_PLL；Chisel 版不建模 clk_pll
  depth:    Int         = 32       // 对应 IP 的 PARAM_VALUE.depth
) extends Module {

  val io = IO(new Bundle {
    val resetn = Input(Bool())        // 低有效
    // ------gpio-------
    val led    = Output(UInt(16.W))
    val switch = Input(UInt(8.W))
  })

  // clk and resetn：原 always @(posedge cpu_clk) cpu_resetn <= resetn;
  val cpu_resetn = RegNext(io.resetn, false.B)

  // ---------------- cpu ----------------
  val cpu = Module(new MinicpuTop)
  cpu.io.resetn := cpu_resetn

  // ---------------- inst ram ----------------
  // 原实例化：.a(cpu_inst_addr[17:2])
  val instRam = Module(new InstRam(program, depth))
  instRam.io.we := cpu.io.inst_sram_we
  instRam.io.a  := cpu.io.inst_sram_addr(17, 2)
  instRam.io.d  := cpu.io.inst_sram_wdata
  cpu.io.inst_sram_rdata := instRam.io.spo

  // ---------------- confreg ----------------
  // 原 assign conf_we = cpu_data_we && cpu_data_addr == 12'd1028;
  val conf_we = cpu.io.data_sram_we && (cpu.io.data_sram_addr === 1028.U)

  val confreg = Module(new Confreg)
  confreg.io.resetn     := cpu_resetn
  confreg.io.conf_we    := conf_we
  confreg.io.conf_wdata := cpu.io.data_sram_wdata

  // 原 assign led = ~conf_led;
  io.led := ~confreg.io.led

  // ---------------- data read mux ----------------
  // 原 assign cpu_data_rdata = (cpu_data_addr == 12'd1024) ? {24'b0, ~switch[7:0]} : 32'b0;
  // 地址 1024（0x400）读拨码开关（低有效，取反后送 CPU）；其余地址读 0。
  cpu.io.data_sram_rdata :=
    Mux(cpu.io.data_sram_addr === 1024.U, Cat(0.U(24.W), ~io.switch), 0.U(32.W))
}
