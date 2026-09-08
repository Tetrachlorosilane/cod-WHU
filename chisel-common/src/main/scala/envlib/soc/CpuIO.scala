// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// exp7 起 myCPU 的统一顶层接口 + SoC 变体（共享库）
//
// 原 Verilog 中每个实验的 soc_lite_top.v 都要例化学生的 mycpu_top，端口固定；
// Chisel 版把「接口定义 + SoC 装配」下沉到共享库，各实验只需：
//     class MyCpuTop extends LACpu { ... }              // 学生模块
//     class SocLiteTop(...) extends SocBramTop(() => new MyCpuTop, ...)  // 薄封装
// 这样 exp8~exp23 不必重复 100 行的 SoC 代码。
//
// 接口对应原书 5.1.1：在 exp6 基础上增加 inst_sram_en / data_sram_en，
// 并把 inst_sram_we / data_sram_we 从 1 位改为 4 位字节写使能。
// ============================================================================

package envlib.soc

import chisel3._
import chisel3.util._

/** exp7 起 myCPU 的顶层接口（与各实验 soc_lite_top.v 的例化端口一一对应）。 */
class CpuIO extends Bundle {
  val resetn = Input(Bool())

  // inst sram interface（block RAM：en 片选、we 字节使能、同步读）
  val inst_sram_en    = Output(Bool())
  val inst_sram_we    = Output(UInt(4.W))
  val inst_sram_addr  = Output(UInt(32.W))
  val inst_sram_wdata = Output(UInt(32.W))
  val inst_sram_rdata = Input(UInt(32.W))

  // data sram interface
  val data_sram_en    = Output(Bool())
  val data_sram_we    = Output(UInt(4.W))
  val data_sram_addr  = Output(UInt(32.W))
  val data_sram_wdata = Output(UInt(32.W))
  val data_sram_rdata = Input(UInt(32.W))

  // trace debug interface（写回级信息）
  val debug_wb_pc       = Output(UInt(32.W))
  val debug_wb_rf_we    = Output(UInt(4.W))
  val debug_wb_rf_wnum  = Output(UInt(5.W))
  val debug_wb_rf_wdata = Output(UInt(32.W))
}

/** 测试平台需要的写回级调试信号（普通接口与握手接口都有）。 */
trait HasDebugWb {
  def debugWbPc: UInt
  def debugWbWe: UInt
  def debugWbWnum: UInt
  def debugWbWdata: UInt
}

/** 学生 CPU 的基类（exp7~exp13 的普通 SRAM 接口）。 */
abstract class LACpu extends Module with HasDebugWb {
  val io = IO(new CpuIO)

  def debugWbPc    = io.debug_wb_pc
  def debugWbWe    = io.debug_wb_rf_we
  def debugWbWnum  = io.debug_wb_rf_wnum
  def debugWbWdata = io.debug_wb_rf_wdata
}

/**
  * exp14 起（握手类 SRAM 总线 / AXI 总线）的 myCPU 顶层接口。
  * 对应原书 8.1.1 起各实验 soc_lite_top.v 的例化端口：
  *   req/wr/size/wstrb/addr/wdata 为请求侧，addr_ok/data_ok/rdata 为响应侧。
  */
class CpuIOHs extends Bundle {
  val resetn = Input(Bool())

  // inst sram interface（握手）
  val inst_sram_req     = Output(Bool())
  val inst_sram_wr      = Output(Bool())
  val inst_sram_size    = Output(UInt(2.W))
  val inst_sram_wstrb   = Output(UInt(4.W))
  val inst_sram_addr    = Output(UInt(32.W))
  val inst_sram_wdata   = Output(UInt(32.W))
  val inst_sram_addr_ok = Input(Bool())
  val inst_sram_data_ok = Input(Bool())
  val inst_sram_rdata   = Input(UInt(32.W))

  // data sram interface（握手）
  val data_sram_req     = Output(Bool())
  val data_sram_wr      = Output(Bool())
  val data_sram_size    = Output(UInt(2.W))
  val data_sram_wstrb   = Output(UInt(4.W))
  val data_sram_addr    = Output(UInt(32.W))
  val data_sram_wdata   = Output(UInt(32.W))
  val data_sram_addr_ok = Input(Bool())
  val data_sram_data_ok = Input(Bool())
  val data_sram_rdata   = Input(UInt(32.W))

  // trace debug interface
  val debug_wb_pc       = Output(UInt(32.W))
  val debug_wb_rf_we    = Output(UInt(4.W))
  val debug_wb_rf_wnum  = Output(UInt(5.W))
  val debug_wb_rf_wdata = Output(UInt(32.W))
}

/** 学生 CPU 的基类（exp14~exp16 的握手接口；exp17/exp20 为模块级实验）。 */
abstract class LACpuHs extends Module with HasDebugWb {
  val io = IO(new CpuIOHs)

  def debugWbPc    = io.debug_wb_pc
  def debugWbWe    = io.debug_wb_rf_we
  def debugWbWnum  = io.debug_wb_rf_wnum
  def debugWbWdata = io.debug_wb_rf_wdata
}

/**
  * exp15 起（AXI 总线）的 myCPU 顶层接口。
  * 对应原书 8.1.2 起各实验 soc_lite_top.v 中 `mycpu_top u_cpu(...)` 的例化端口：
  * CPU 直接产生 AXI4 的 ar/r/aw/w/b 五个通道。
  */
class CpuIOAxi extends Bundle {
  val resetn = Input(Bool())

  // AR channel
  val arid    = Output(UInt(4.W))
  val araddr  = Output(UInt(32.W))
  val arlen   = Output(UInt(8.W))
  val arsize  = Output(UInt(3.W))
  val arburst = Output(UInt(2.W))
  val arlock  = Output(UInt(2.W))
  val arcache = Output(UInt(4.W))
  val arprot  = Output(UInt(3.W))
  val arvalid = Output(Bool())
  val arready = Input(Bool())
  // R channel
  val rid     = Input(UInt(4.W))
  val rdata   = Input(UInt(32.W))
  val rresp   = Input(UInt(2.W))
  val rlast   = Input(Bool())
  val rvalid  = Input(Bool())
  val rready  = Output(Bool())
  // AW channel
  val awid    = Output(UInt(4.W))
  val awaddr  = Output(UInt(32.W))
  val awlen   = Output(UInt(8.W))
  val awsize  = Output(UInt(3.W))
  val awburst = Output(UInt(2.W))
  val awlock  = Output(UInt(2.W))
  val awcache = Output(UInt(4.W))
  val awprot  = Output(UInt(3.W))
  val awvalid = Output(Bool())
  val awready = Input(Bool())
  // W channel
  val wid     = Output(UInt(4.W))
  val wdata   = Output(UInt(32.W))
  val wstrb   = Output(UInt(4.W))
  val wlast   = Output(Bool())
  val wvalid  = Output(Bool())
  val wready   = Input(Bool())
  // B channel
  val bid     = Input(UInt(4.W))
  val bresp   = Input(UInt(2.W))
  val bvalid  = Input(Bool())
  val bready  = Output(Bool())

  // trace debug interface
  val debug_wb_pc       = Output(UInt(32.W))
  val debug_wb_rf_we    = Output(UInt(4.W))
  val debug_wb_rf_wnum  = Output(UInt(5.W))
  val debug_wb_rf_wdata = Output(UInt(32.W))
}

/** 学生 CPU 的基类（exp15/16、exp18/19、exp21~23 的 AXI 接口）。 */
abstract class LACpuAxi extends Module with HasDebugWb {
  val io = IO(new CpuIOAxi)

  def debugWbPc    = io.debug_wb_pc
  def debugWbWe    = io.debug_wb_rf_we
  def debugWbWnum  = io.debug_wb_rf_wnum
  def debugWbWdata = io.debug_wb_rf_wdata
}

/** SoC 的板级接口（resetn + gpio），各实验 soc_lite_top.v 的端口一致。 */
class SocIO extends Bundle {
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
}

/**
  * SoC 变体的公共基类：便于测试代码以统一方式访问「板级接口」「学生 CPU」与「confreg」，
  * 从而复用同一套 golden_trace 比对流程（见 envlib.test.TraceHarness）。
  */
abstract class SocBase extends Module {
  val io: SocIO
  def cpu: HasDebugWb
  /** confreg 的 num_data（功能测试点监视用） */
  def numData: UInt
}

/**
  * exp7 ~ exp13 的 SoC（`soc_bram` 变体）：cpu → inst_ram；cpu.data → bridge → {data_ram, confreg}
  * 对应原 Verilog：code/soc_verify/soc_bram/rtl/soc_lite_top.v
  *
  * @param cpuGen     学生 CPU 的工厂函数（每次 elaboration 生成一个新实例）
  * @param instInit   指令 RAM 初始内容（对应原 $readmemb 的 inst_ram.mif）
  * @param dataInit   数据 RAM 初始内容（对应 data_ram.mif）
  * @param simulation 对应原 Verilog 的 #(parameter SIMULATION)
  */
class SocBramTop(
  cpuGen:     () => LACpu,
  instInit:   Seq[BigInt] = Nil,
  dataInit:   Seq[BigInt] = Nil,
  simulation: Boolean     = true
) extends SocBase {

  val io = IO(new SocIO)

  val cpu_resetn = RegNext(io.resetn, false.B)

  // ---------------- cpu ----------------
  val cpu = Module(cpuGen())
  cpu.io.resetn := cpu_resetn

  // ---------------- inst ram（block RAM，同步读） ----------------
  // 原实例化：.addra(cpu_inst_addr[19:2])（18 位）
  val inst_ram = Module(new InstRam(instInit, addrWidth = 18))
  inst_ram.io.ena   := cpu.io.inst_sram_en
  inst_ram.io.wea   := cpu.io.inst_sram_we
  inst_ram.io.addra := cpu.io.inst_sram_addr(19, 2)
  inst_ram.io.dina  := cpu.io.inst_sram_wdata
  cpu.io.inst_sram_rdata := inst_ram.io.douta

  // ---------------- bridge ----------------
  val bridge_1x2 = Module(new Bridge1x2(weWidth = 4))
  bridge_1x2.io.resetn         := cpu_resetn
  bridge_1x2.io.cpu_data_en    := cpu.io.data_sram_en
  bridge_1x2.io.cpu_data_we    := cpu.io.data_sram_we
  bridge_1x2.io.cpu_data_addr  := cpu.io.data_sram_addr
  bridge_1x2.io.cpu_data_wdata := cpu.io.data_sram_wdata
  cpu.io.data_sram_rdata       := bridge_1x2.io.cpu_data_rdata

  // ---------------- data ram（block RAM，同步读） ----------------
  // 原实例化：.addra(data_sram_addr[17:2])（16 位）
  val data_ram = Module(new DataRam(dataInit, addrWidth = 16))
  data_ram.io.ena   := bridge_1x2.io.data_sram_en
  data_ram.io.wea   := bridge_1x2.io.data_sram_we
  data_ram.io.addra := bridge_1x2.io.data_sram_addr(17, 2)
  data_ram.io.dina  := bridge_1x2.io.data_sram_wdata
  bridge_1x2.io.data_sram_rdata := data_ram.io.douta

  // ---------------- confreg ----------------
  val u_confreg = Module(new Confreg(simulation, weWidth = 4))
  u_confreg.io.resetn      := cpu_resetn
  u_confreg.io.conf_en     := bridge_1x2.io.conf_en
  u_confreg.io.conf_we     := bridge_1x2.io.conf_we
  u_confreg.io.conf_addr   := bridge_1x2.io.conf_addr
  u_confreg.io.conf_wdata  := bridge_1x2.io.conf_wdata
  bridge_1x2.io.conf_rdata := u_confreg.io.conf_rdata

  u_confreg.io.switch      := io.switch
  u_confreg.io.btn_key_row := io.btn_key_row
  u_confreg.io.btn_step    := io.btn_step

  def numData: UInt = u_confreg.io.num_data

  io.led         := u_confreg.io.led
  io.led_rg0     := u_confreg.io.led_rg0
  io.led_rg1     := u_confreg.io.led_rg1
  io.num_csn     := u_confreg.io.num_csn
  io.num_a_g     := u_confreg.io.num_a_g
  io.num_data    := u_confreg.io.num_data
  io.btn_key_col := u_confreg.io.btn_key_col
}
