// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 对应原 Verilog：exp15~exp16 的 Xilinx IP `axi_ram`（code/.../xilinx_ip/axi_ram/axi_ram.xci）
// 的**行为模型**（AXI4 从机 + 同步 RAM）。
//
// 简化与约束：
//   * 支持 INCR 突发（arburst=2'b01）、单 outstanding 事务（AXI 握手一次处理一个请求）；
//   * 数据宽度固定 32 位，地址按 4 字节步进；
//   * 读延迟 1 拍（RAM 同步读），写 1 拍后返回 B；
//   * 存储器内容由构造参数 init 注入（对应原 func/obj/*.mif 的 $readmemb）。
// ============================================================================

package envlib.soc

import chisel3._
import chisel3.util._

class AxiRamSlave(
  init:      Seq[BigInt] = Nil,
  addrWidth: Int         = 16,
  dataWidth: Int         = 32
) extends Module {

  val strbWidth = dataWidth / 8

  val io = IO(new Bundle {
    val resetn = Input(Bool())
    // AR
    val arid    = Input(UInt(4.W))
    val araddr  = Input(UInt(32.W))
    val arlen   = Input(UInt(8.W))
    val arsize  = Input(UInt(3.W))
    val arburst = Input(UInt(2.W))
    val arvalid = Input(Bool())
    val arready = Output(Bool())
    // R
    val rid    = Output(UInt(4.W))
    val rdata  = Output(UInt(dataWidth.W))
    val rresp  = Output(UInt(2.W))
    val rlast  = Output(Bool())
    val rvalid = Output(Bool())
    val rready = Input(Bool())
    // AW
    val awid    = Input(UInt(4.W))
    val awaddr  = Input(UInt(32.W))
    val awlen   = Input(UInt(8.W))
    val awsize  = Input(UInt(3.W))
    val awburst = Input(UInt(2.W))
    val awvalid = Input(Bool())
    val awready = Output(Bool())
    // W
    val wid    = Input(UInt(4.W))
    val wdata  = Input(UInt(dataWidth.W))
    val wstrb  = Input(UInt(strbWidth.W))
    val wlast  = Input(Bool())
    val wvalid = Input(Bool())
    val wready  = Output(Bool())
    // B
    val bid    = Output(UInt(4.W))
    val bresp  = Output(UInt(2.W))
    val bvalid = Output(Bool())
    val bready = Input(Bool())
  })

  val ram = Module(new SyncRam(init, addrWidth, dataWidth))

  // --------------------------------------------------------------------------
  // 读通道状态机
  // --------------------------------------------------------------------------
  val rBusy     = Reg(Bool())
  val rBeatLeft = Reg(UInt(8.W))
  val rAddr     = Reg(UInt(32.W))
  val rId       = Reg(UInt(4.W))
  val rDataReg  = Reg(UInt(dataWidth.W))
  val rValidReg = Reg(Bool())
  val rLastReg  = Reg(Bool())
  val rWaitData = Reg(Bool())   // 已发出读请求，等 RAM 的同步读数据

  val arEnter = io.arvalid && io.arready
  val rFire   = rValidReg && io.rready

  io.arready := !rBusy && !rWaitData && !rValidReg

  // RAM 读端口默认不使能
  ram.io.en    := false.B
  ram.io.we    := 0.U
  ram.io.address := rAddr(addrWidth + 1, 2)
  ram.io.wdata := 0.U

  when(!io.resetn) {
    rBusy     := false.B
    rWaitData := false.B
    rValidReg := false.B
    rLastReg  := false.B
    rBeatLeft := 0.U
  }.otherwise {
    // 1) 接受 AR
    when(arEnter) {
      rBusy     := true.B
      rWaitData := true.B
      rBeatLeft := io.arlen
      rAddr     := io.araddr
      rId       := io.arid
      ram.io.en    := true.B
      ram.io.address := io.araddr(addrWidth + 1, 2)
    }

    // 2) 等 RAM 读数据 → 产生 R beat
    when(rWaitData) {
      rWaitData := false.B
      rValidReg := true.B
      rDataReg  := ram.io.rdata
      rLastReg  := rBeatLeft === 0.U
    }

    // 3) R beat 被接收：继续下一拍或结束
    when(rFire) {
      when(rLastReg) {
        rValidReg := false.B
        rBusy     := false.B
      }.otherwise {
        rBeatLeft := rBeatLeft - 1.U
        rAddr     := rAddr + 4.U
        rWaitData := true.B
        ram.io.en      := true.B
        ram.io.address := (rAddr + 4.U)(addrWidth + 1, 2)
      }
    }
  }

  io.rid    := rId
  io.rdata  := rDataReg
  io.rresp  := 0.U
  io.rlast  := rLastReg
  io.rvalid := rValidReg

  // --------------------------------------------------------------------------
  // 写通道状态机
  // --------------------------------------------------------------------------
  val wBusy     = Reg(Bool())
  val wAddr     = Reg(UInt(32.W))
  val wId       = Reg(UInt(4.W))
  val bValidReg = Reg(Bool())

  val awEnter = io.awvalid && io.awready
  val wFire   = io.wvalid && io.wready
  val bFire   = io.bvalid && io.bready

  io.awready := !wBusy && !bValidReg
  io.wready  := wBusy && !bValidReg

  when(!io.resetn) {
    wBusy     := false.B
    bValidReg := false.B
    wAddr     := 0.U
    wId       := 0.U
  }.otherwise {
    when(awEnter) {
      wBusy := true.B
      wAddr := io.awaddr
      wId   := io.awid
    }

    when(wFire) {
      // 字节写：SyncRam 内部按 we 逐字节覆盖
      ram.io.en      := true.B
      ram.io.we      := io.wstrb
      ram.io.address := wAddr(addrWidth + 1, 2)
      ram.io.wdata   := io.wdata
      wAddr := wAddr + 4.U
      when(io.wlast) {
        bValidReg := true.B
        wBusy     := false.B
      }
    }

    when(bFire) {
      bValidReg := false.B
    }
  }

  io.bid    := wId
  io.bresp  := 0.U
  io.bvalid := bValidReg
}
