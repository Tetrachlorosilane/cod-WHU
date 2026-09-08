// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：exp14~exp16 的 code/soc_verify/*/rtl/ram_wrap/sram_wrap.v
//
// 作用：把 CPU 的握手式访存请求（req/wr/size/wstrb/addr/wdata）转换成简单 RAM 接口
//       （en/we/addr/wdata/rdata），并用一个深度为 4 的读数据缓冲队列吸收 RAM 的读延迟。
//
// 原 Verilog 中定义了 `_RUN_PERF_TEST，因此 addr_and = data_and = 1
// （随机延迟掩码 ram_random_mask 被屏蔽）。Chisel 版同样按"始终 ready"实现，
// 。
//
// 关键行为（与原文件逐行对应）：
//     size_decode = size==0 ? one-hot(addr[1:0]) : size==1 ? {addr[1],addr[1],~addr[1],~addr[1]} : 4'hf
//     ram_en      = req && addr_ok
//     ram_we      = {4{wr}} & wstrb & size_decode
//     addr_ok     = !buf_full
//     data_ok     = (!buf_empty || ram_en_r)
//     rdata       = buf_empty ? ram_rdata : buf_rdata[buf_rptr[1:0]]

package envlib.soc

import chisel3._
import chisel3.util._

class SramWrap extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())
    // master（CPU 侧，握手）
    val req    = Input(Bool())
    val wr     = Input(Bool())
    val size   = Input(UInt(2.W))
    val wstrb  = Input(UInt(4.W))
    val addr   = Input(UInt(32.W))
    val wdata  = Input(UInt(32.W))
    val addr_ok = Output(Bool())
    val data_ok = Output(Bool())
    val rdata   = Output(UInt(32.W))
    // slave（RAM 侧，简单接口）
    val ram_en    = Output(Bool())
    val ram_we    = Output(UInt(4.W))
    val ram_addr  = Output(UInt(32.W))
    val ram_wdata = Output(UInt(32.W))
    val ram_rdata = Input(UInt(32.W))
  })

  // size_decode：按访问宽度生成字节使能模板
  val size_decode = Mux(io.size === 0.U,
    VecInit((0 until 4).map(i => io.addr(1, 0) === i.U)).asUInt,
    Mux(io.size === 1.U,
      Cat(io.addr(1), io.addr(1), ~io.addr(1), ~io.addr(1)),
      0xf.U(4.W)))

  io.ram_en    := io.req && io.addr_ok
  io.ram_we    := Mux(io.wr, io.wstrb, 0.U(4.W)) & size_decode
  io.ram_addr  := io.addr
  io.ram_wdata := io.wdata

  val ram_en_r = RegNext(io.ram_en, false.B)

  // 读数据缓冲（深度 4 的环形队列）
  val buf_wptr  = Reg(UInt(3.W))
  val buf_rptr  = Reg(UInt(3.W))
  val buf_rdata = Reg(Vec(4, UInt(32.W)))

  val buf_empty   = buf_wptr === buf_rptr
  val buf_full    = buf_wptr === Cat(~buf_rptr(2), buf_rptr(1, 0))
  val fast_return = ram_en_r && io.data_ok && buf_empty

  when(!io.resetn) {
    buf_wptr := 0.U
  }.elsewhen(ram_en_r && !fast_return) {
    buf_wptr := buf_wptr + 1.U
  }

  when(ram_en_r && !fast_return) {
    buf_rdata(buf_wptr(1, 0)) := io.ram_rdata
  }

  when(!io.resetn) {
    buf_rptr := 0.U
  }.elsewhen(!buf_empty && io.data_ok) {
    buf_rptr := buf_rptr + 1.U
  }

  io.addr_ok := !buf_full
  io.data_ok := !buf_empty || ram_en_r
  io.rdata   := Mux(buf_empty, io.ram_rdata, buf_rdata(buf_rptr(1, 0)))
}
