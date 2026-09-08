// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/rtl/CONFREG/confreg.v
//
// 原 Verilog：
//     wire write_led = conf_we;
//     assign led = led_data[15:0];
//     always @(posedge clk) begin
//         if(!resetn)          led_data <= 32'h0;
//         else if(write_led)   led_data <= conf_wdata[31:0];
//     end
//
// 复位为同步复位（原 always @(posedge clk) 内判断），故 Chisel 版手写同步复位，
// 不使用 RegInit 的隐式复位（见 CHISEL-CONVENTIONS.md §4.3）。

package exp5.soc

import chisel3._

/** 板级外设控制寄存器：目前只有 16 位 LED。 */
class Confreg extends Module {
  val io = IO(new Bundle {
    val resetn     = Input(Bool())        // 低有效，同步复位
    val conf_we    = Input(Bool())        // 来自 CPU 的写使能
    val conf_wdata = Input(UInt(32.W))    // 来自 CPU 的写数据
    val led        = Output(UInt(16.W))   // 到开发板 LED
  })

  val led_data = Reg(UInt(32.W))

  when(!io.resetn) {
    led_data := 0.U
  }.elsewhen(io.conf_we) {
    led_data := io.conf_wdata
  }

  io.led := led_data(15, 0)
}
