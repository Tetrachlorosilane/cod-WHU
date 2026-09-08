// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：exp14 起 code/soc_verify/*/rtl/CONFREG/confreg.v 的**握手接口变体**。
//
// 原 Verilog（握手版）的接口差异：
//     input  conf_req / conf_wr / conf_size[1:0] / conf_wstrb[3:0] / conf_addr / conf_wdata
//     output conf_addr_ok / conf_data_ok / conf_rdata
//     output ram_random_mask[4:0]（仿真用的随机延迟掩码）
//   内部：
//     assign conf_addr_ok = 1'b1;
//     assign conf_data_ok = conf_req_reg;      // 请求后一拍返回数据
//     assign conf_rdata   = conf_rdata_reg;    // 读数据打一拍
//     assign conf_we      = conf_req & conf_wr & conf_addr_ok;
//
// Chisel 做法：把握手信号适配到 envlib.soc.Confreg 的 en/we 接口
// （confreg 的寄存器逻辑本身在 exp7~exp13 与 exp14 起是相同的），
// 从而避免重复维护 ~700 行的外设逻辑。
//
// 说明：`_RUN_PERF_TEST 在原 sram_wrap.v 中被定义，因此 ram_random_mask 被忽略，
//       本适配器把它固定输出 0。

package envlib.soc

import chisel3._
import chisel3.util._

class ConfregHsWrap(simulation: Boolean = false) extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())
    // 握手接口
    val conf_req     = Input(Bool())
    val conf_wr      = Input(Bool())
    val conf_size    = Input(UInt(2.W))
    val conf_wstrb   = Input(UInt(4.W))
    val conf_addr    = Input(UInt(32.W))
    val conf_wdata   = Input(UInt(32.W))
    val conf_addr_ok = Output(Bool())
    val conf_data_ok = Output(Bool())
    val conf_rdata   = Output(UInt(32.W))
    val ram_random_mask = Output(UInt(5.W))
    // 板级设备
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

  val core = Module(new Confreg(simulation, weWidth = 4))

  core.io.resetn     := io.resetn
  core.io.conf_en    := io.conf_req
  // 原 Verilog 的 conf_we = conf_req & conf_wr & conf_addr_ok（整字写）
  core.io.conf_we    := Mux(io.conf_wr, 0xf.U(4.W), 0.U(4.W))
  core.io.conf_addr  := io.conf_addr
  core.io.conf_wdata := io.conf_wdata

  core.io.switch      := io.switch
  core.io.btn_key_row := io.btn_key_row
  core.io.btn_step    := io.btn_step

  // 握手响应：addr_ok 恒 1；data_ok / rdata 各打一拍
  io.conf_addr_ok     := true.B
  io.conf_data_ok     := RegNext(io.conf_req, false.B)
  io.conf_rdata       := RegNext(core.io.conf_rdata)
  io.ram_random_mask  := 0.U(5.W)

  io.led         := core.io.led
  io.led_rg0     := core.io.led_rg0
  io.led_rg1     := core.io.led_rg1
  io.num_csn     := core.io.num_csn
  io.num_a_g     := core.io.num_a_g
  io.num_data    := core.io.num_data
  io.btn_key_col := core.io.btn_key_col
}
