// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_dram/rtl/soc_lite_top.v
//
// 结构：cpu → inst_ram；cpu.data → bridge_1x2 → {data_ram, confreg}
//
// 与 Verilog 的差异（见 ../MAPPING.md）：
//   1. clk 端口 → Chisel 隐式 clock；原 `SIMU_USE_PLL=0` 的加速分支
//      （cpu_clk = timer_clk = clk）直接作为唯一时钟域，不建模 clk_pll。
//   2. 原 soc_lite_top 把 debug_wb_* 作为内部线网引给 testbench；
//      Chisel 版由测试直接访问 `soc.cpu.io.debug_wb_*`。
//   3. inst_ram / data_ram 的地址：原实例化把 `[17:2]`（16 位）接到 15 位端口，
//      实际使用 `[16:2]`；Chisel 直接写 `(16, 2)`。
//   4. 存储器内容由构造参数 instInit / dataInit 传入（对应原 $readmemb 的 .mif）。

package exp6.soc

import chisel3._
import chisel3.util._
import exp6.student.MyCpuTop

class SocLiteTop(
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true,
  addrWidth:  Int         = 15
) extends Module {

  val io = IO(new Bundle {
    val resetn = Input(Bool())
    // ------gpio-------
    val led         = Output(UInt(16.W))
    val led_rg0     = Output(UInt(2.W))
    val led_rg1     = Output(UInt(2.W))
    val num_csn     = Output(UInt(8.W))
    val num_a_g     = Output(UInt(7.W))
    val num_data    = Output(UInt(32.W))
    val switch      = Input(UInt(8.W))
    val btn_key_col = Output(UInt(4.W))
    val btn_key_row = Input(UInt(4.W))
    val btn_step    = Input(UInt(2.W))
  })

  // clk and resetn
  val cpu_resetn = RegNext(io.resetn, false.B)

  // ---------------- cpu ----------------
  val cpu = Module(new MyCpuTop)
  cpu.io.resetn := cpu_resetn

  // ---------------- inst ram ----------------
  val inst_ram = Module(new InstRam(instInit, addrWidth))
  inst_ram.io.we := cpu.io.inst_sram_we
  inst_ram.io.a  := cpu.io.inst_sram_addr(16, 2)
  inst_ram.io.d  := cpu.io.inst_sram_wdata
  cpu.io.inst_sram_rdata := inst_ram.io.spo

  // ---------------- bridge ----------------
  val bridge_1x2 = Module(new Bridge1x2)
  bridge_1x2.io.resetn         := cpu_resetn
  bridge_1x2.io.cpu_data_we    := cpu.io.data_sram_we
  bridge_1x2.io.cpu_data_addr  := cpu.io.data_sram_addr
  bridge_1x2.io.cpu_data_wdata := cpu.io.data_sram_wdata
  cpu.io.data_sram_rdata       := bridge_1x2.io.cpu_data_rdata

  // ---------------- data ram ----------------
  val data_ram = Module(new DataRam(dataInit, addrWidth))
  data_ram.io.we := bridge_1x2.io.data_sram_we && bridge_1x2.io.data_sram_en
  data_ram.io.a  := bridge_1x2.io.data_sram_addr(16, 2)
  data_ram.io.d  := bridge_1x2.io.data_sram_wdata
  bridge_1x2.io.data_sram_rdata := data_ram.io.spo

  // ---------------- confreg ----------------
  val u_confreg = Module(new Confreg(simulation))
  u_confreg.io.resetn      := cpu_resetn
  u_confreg.io.conf_en     := bridge_1x2.io.conf_en
  u_confreg.io.conf_we     := bridge_1x2.io.conf_we
  u_confreg.io.conf_addr   := bridge_1x2.io.conf_addr
  u_confreg.io.conf_wdata  := bridge_1x2.io.conf_wdata
  bridge_1x2.io.conf_rdata := u_confreg.io.conf_rdata

  u_confreg.io.switch      := io.switch
  u_confreg.io.btn_key_row := io.btn_key_row
  u_confreg.io.btn_step    := io.btn_step

  io.led         := u_confreg.io.led
  io.led_rg0     := u_confreg.io.led_rg0
  io.led_rg1     := u_confreg.io.led_rg1
  io.num_csn     := u_confreg.io.num_csn
  io.num_a_g     := u_confreg.io.num_a_g
  io.num_data    := u_confreg.io.num_data
  io.btn_key_col := u_confreg.io.btn_key_col
}
