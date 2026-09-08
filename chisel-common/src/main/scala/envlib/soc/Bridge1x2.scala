// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：exp7~exp16 的 code/soc_verify/*/rtl/BRIDGE/bridge_1x2.v
// （exp6 的 soc_dram 版本没有 cpu_data_en、读数据选择是组合逻辑、we 为 1 位，
//   该版本保留在 exp6 自包含目录中；本库版本对应 exp7 起的接口。）
//
// 原 Verilog：
//     assign sel_conf = (cpu_data_addr & 32'h1fff_0000) == 32'h1faf_0000;
//     assign sel_sram = !sel_conf;
//     assign data_sram_en = cpu_data_en & sel_sram;
//     assign conf_en      = cpu_data_en & sel_conf;
//     always @(posedge clk) begin
//         if (!resetn) begin sel_sram_r <= 0; sel_conf_r <= 0; end
//         else         begin sel_sram_r <= sel_sram; sel_conf_r <= sel_conf; end
//     end
//     assign cpu_data_rdata = {32{sel_sram_r}} & data_sram_rdata
//                           | {32{sel_conf_r}} & conf_rdata;

package envlib.soc

import chisel3._

class Bridge1x2(weWidth: Int = 4) extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())
    // master : cpu data
    val cpu_data_en    = Input(Bool())
    val cpu_data_we    = Input(UInt(weWidth.W))
    val cpu_data_addr  = Input(UInt(32.W))
    val cpu_data_wdata = Input(UInt(32.W))
    val cpu_data_rdata = Output(UInt(32.W))
    // slave : data ram
    val data_sram_en    = Output(Bool())
    val data_sram_we    = Output(UInt(weWidth.W))
    val data_sram_addr  = Output(UInt(32.W))
    val data_sram_wdata = Output(UInt(32.W))
    val data_sram_rdata = Input(UInt(32.W))
    // slave : confreg
    val conf_en    = Output(Bool())
    val conf_we    = Output(UInt(weWidth.W))
    val conf_addr  = Output(UInt(32.W))
    val conf_wdata = Output(UInt(32.W))
    val conf_rdata = Input(UInt(32.W))
  })

  val CONF_ADDR_BASE = 0x1faf0000L.U(32.W)
  val CONF_ADDR_MASK = 0x1fff0000L.U(32.W)

  val sel_conf = (io.cpu_data_addr & CONF_ADDR_MASK) === CONF_ADDR_BASE
  val sel_sram = !sel_conf

  // data sram
  io.data_sram_en    := io.cpu_data_en && sel_sram
  io.data_sram_we    := io.cpu_data_we
  io.data_sram_addr  := io.cpu_data_addr
  io.data_sram_wdata := io.cpu_data_wdata

  // confreg
  io.conf_en    := io.cpu_data_en && sel_conf
  io.conf_we    := io.cpu_data_we
  io.conf_addr  := io.cpu_data_addr
  io.conf_wdata := io.cpu_data_wdata

  // 读数据选择：地址译码打一拍（与同步 RAM 的读延迟对齐）
  val sel_sram_r = Reg(Bool())
  val sel_conf_r = Reg(Bool())
  when(!io.resetn) {
    sel_sram_r := false.B
    sel_conf_r := false.B
  }.otherwise {
    sel_sram_r := sel_sram
    sel_conf_r := sel_conf
  }

  io.cpu_data_rdata := Mux(sel_sram_r, io.data_sram_rdata, io.conf_rdata)
}
