// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_dram/rtl/CONFREG/confreg.v（666 行）
//
// 覆盖：cr0~cr7、led、led_rg0/1、数码管扫描、switch、按键消抖状态机、step 按键、
//       timer、仿真标志（simu_flag/io_simu/open_trace/num_monitor）、虚拟串口。
//
// 与 Verilog 的差异（见 ../MAPPING.md）：
//   1. timer 原用独立的 timer_clk；本实验 `SIMU_USE_PLL=0 ⇒ timer_clk = cpu_clk`，
//      故 Chisel 版把 timer 逻辑放在同一隐式时钟域，行为等价。
//   2. 复位为同步复位，按 CHISEL-CONVENTIONS.md §4.3 手写 when(!resetn)。
//   3. Verilog 的 case 语句用 VecInit + 索引等价实现。

package exp6.soc

import chisel3._
import chisel3.util._

class Confreg(simulation: Boolean = false) extends Module {
  val io = IO(new Bundle {
    val resetn = Input(Bool())
    // read and write from cpu
    val conf_en    = Input(Bool())
    val conf_we    = Input(Bool())
    val conf_addr  = Input(UInt(32.W))
    val conf_wdata = Input(UInt(32.W))
    val conf_rdata = Output(UInt(32.W))
    // read and write to device on board
    val led        = Output(UInt(16.W))
    val led_rg0    = Output(UInt(2.W))
    val led_rg1    = Output(UInt(2.W))
    val num_csn    = Output(UInt(8.W))
    val num_a_g    = Output(UInt(7.W))
    val num_data   = Output(UInt(32.W))
    val switch     = Input(UInt(8.W))
    val btn_key_col = Output(UInt(4.W))
    val btn_key_row = Input(UInt(4.W))
    val btn_step    = Input(UInt(2.W))
  })

  // --------------------------------------------------------------------------
  // 地址常量（原 `define 均为 conf_addr[15:0] 的偏移）
  // --------------------------------------------------------------------------
  val a16 = io.conf_addr(15, 0)

  val CR0_ADDR       = 0x8000.U(16.W)
  val CR1_ADDR       = 0x8010.U(16.W)
  val CR2_ADDR       = 0x8020.U(16.W)
  val CR3_ADDR       = 0x8030.U(16.W)
  val CR4_ADDR       = 0x8040.U(16.W)
  val CR5_ADDR       = 0x8050.U(16.W)
  val CR6_ADDR       = 0x8060.U(16.W)
  val CR7_ADDR       = 0x8070.U(16.W)
  val LED_ADDR       = 0xf020.U(16.W)
  val LED_RG0_ADDR   = 0xf030.U(16.W)
  val LED_RG1_ADDR   = 0xf040.U(16.W)
  val NUM_ADDR       = 0xf050.U(16.W)
  val SWITCH_ADDR    = 0xf060.U(16.W)
  val BTN_KEY_ADDR   = 0xf070.U(16.W)
  val BTN_STEP_ADDR  = 0xf080.U(16.W)
  val SW_INTER_ADDR  = 0xf090.U(16.W)
  val TIMER_ADDR     = 0xe000.U(16.W)
  val IO_SIMU_ADDR   = 0xff00.U(16.W)
  val VIRTUAL_UART_ADDR = 0xff10.U(16.W)
  val SIMU_FLAG_ADDR = 0xff20.U(16.W)
  val OPEN_TRACE_ADDR = 0xff30.U(16.W)
  val NUM_MONITOR_ADDR = 0xff40.U(16.W)

  // --------------------------------------------------------------------------
  // 写使能
  // --------------------------------------------------------------------------
  val conf_write = io.conf_en && io.conf_we

  def wr(addr: UInt) = conf_write && (a16 === addr)

  // --------------------------------------------------------------------------
  // confreg 寄存器 cr0 ~ cr7
  // --------------------------------------------------------------------------
  val cr0 = Reg(UInt(32.W))
  val cr1 = Reg(UInt(32.W))
  val cr2 = Reg(UInt(32.W))
  val cr3 = Reg(UInt(32.W))
  val cr4 = Reg(UInt(32.W))
  val cr5 = Reg(UInt(32.W))
  val cr6 = Reg(UInt(32.W))
  val cr7 = Reg(UInt(32.W))

  val write_cr0 = wr(CR0_ADDR)
  val write_cr1 = wr(CR1_ADDR)
  val write_cr2 = wr(CR2_ADDR)
  val write_cr3 = wr(CR3_ADDR)
  val write_cr4 = wr(CR4_ADDR)
  val write_cr5 = wr(CR5_ADDR)
  val write_cr6 = wr(CR6_ADDR)
  val write_cr7 = wr(CR7_ADDR)

  when(!io.resetn) {
    cr0 := 0.U; cr1 := 0.U; cr2 := 0.U; cr3 := 0.U
    cr4 := 0.U; cr5 := 0.U; cr6 := 0.U; cr7 := 0.U
  }.otherwise {
    when(write_cr0) { cr0 := io.conf_wdata }
    when(write_cr1) { cr1 := io.conf_wdata }
    when(write_cr2) { cr2 := io.conf_wdata }
    when(write_cr3) { cr3 := io.conf_wdata }
    when(write_cr4) { cr4 := io.conf_wdata }
    when(write_cr5) { cr5 := io.conf_wdata }
    when(write_cr6) { cr6 := io.conf_wdata }
    when(write_cr7) { cr7 := io.conf_wdata }
  }

  // --------------------------------------------------------------------------
  // timer（原用 timer_clk；本实验 timer_clk = cpu_clk，合并为同一时钟域）
  // --------------------------------------------------------------------------
  val write_timer = wr(TIMER_ADDR)

  val write_timer_begin   = Reg(Bool())
  val write_timer_end_r1  = Reg(Bool())
  val write_timer_end_r2  = Reg(Bool())
  val write_timer_begin_r1 = Reg(Bool())
  val write_timer_begin_r2 = Reg(Bool())
  val write_timer_begin_r3 = Reg(Bool())
  val conf_wdata_r  = Reg(UInt(32.W))
  val conf_wdata_r1 = Reg(UInt(32.W))
  val conf_wdata_r2 = Reg(UInt(32.W))
  val timer    = Reg(UInt(32.W))
  val timer_r1 = Reg(UInt(32.W))
  val timer_r2 = Reg(UInt(32.W))

  when(!io.resetn) {
    write_timer_begin := false.B
  }.elsewhen(write_timer) {
    write_timer_begin := true.B
    conf_wdata_r      := io.conf_wdata
  }.elsewhen(write_timer_end_r2) {
    write_timer_begin := false.B
  }

  write_timer_end_r1 := write_timer_begin_r2
  write_timer_end_r2 := write_timer_end_r1

  write_timer_begin_r1 := write_timer_begin
  write_timer_begin_r2 := write_timer_begin_r1
  write_timer_begin_r3 := write_timer_begin_r2
  conf_wdata_r1        := conf_wdata_r
  conf_wdata_r2        := conf_wdata_r1

  when(!io.resetn) {
    timer := 0.U
  }.elsewhen(write_timer_begin_r2 && !write_timer_begin_r3) {
    timer := conf_wdata_r2
  }.otherwise {
    timer := timer + 1.U
  }

  timer_r1 := timer
  timer_r2 := timer_r1

  // --------------------------------------------------------------------------
  // 仿真标志
  // --------------------------------------------------------------------------
  val simu_flag = Reg(UInt(32.W))
  when(!io.resetn) {
    simu_flag := Fill(32, simulation.B)
  }

  val io_simu = Reg(UInt(32.W))
  when(!io.resetn) {
    io_simu := 0.U
  }.elsewhen(wr(IO_SIMU_ADDR)) {
    io_simu := Cat(io.conf_wdata(15, 0), io.conf_wdata(31, 16))
  }

  val open_trace = Reg(Bool())
  when(!io.resetn) {
    open_trace := true.B
  }.elsewhen(wr(OPEN_TRACE_ADDR)) {
    open_trace := io.conf_wdata.orR
  }

  val num_monitor = Reg(Bool())
  when(!io.resetn) {
    num_monitor := true.B
  }.elsewhen(wr(NUM_MONITOR_ADDR)) {
    num_monitor := io.conf_wdata(0)
  }

  // --------------------------------------------------------------------------
  // 虚拟串口
  // --------------------------------------------------------------------------
  val write_uart_valid = wr(VIRTUAL_UART_ADDR)
  val write_uart_data  = io.conf_wdata(7, 0)

  val virtual_uart_data = Reg(UInt(8.W))
  val confreg_uart_data = Reg(UInt(8.W))
  val confreg_uart_valid = Reg(Bool())

  when(!io.resetn) {
    virtual_uart_data  := 0.U
    confreg_uart_data  := 0.U
    confreg_uart_valid := false.B
  }.elsewhen(write_uart_valid) {
    virtual_uart_data  := write_uart_data
    confreg_uart_data  := write_uart_data
    confreg_uart_valid := write_uart_valid
  }

  // --------------------------------------------------------------------------
  // led / led_rg
  // --------------------------------------------------------------------------
  val led_data = Reg(UInt(32.W))
  when(!io.resetn) {
    led_data := 0.U
  }.elsewhen(wr(LED_ADDR)) {
    led_data := io.conf_wdata
  }
  io.led := led_data(15, 0)

  val led_rg0_data = Reg(UInt(32.W))
  val led_rg1_data = Reg(UInt(32.W))
  when(!io.resetn) {
    led_rg0_data := 0.U
    led_rg1_data := 0.U
  }.otherwise {
    when(wr(LED_RG0_ADDR)) { led_rg0_data := io.conf_wdata }
    when(wr(LED_RG1_ADDR)) { led_rg1_data := io.conf_wdata }
  }
  io.led_rg0 := led_rg0_data(1, 0)
  io.led_rg1 := led_rg1_data(1, 0)

  // --------------------------------------------------------------------------
  // switch / switch interleave
  // --------------------------------------------------------------------------
  val switch_data = Cat(0.U(24.W), io.switch)
  val sw_inter_data = Cat(
    0.U(16.W),
    io.switch(7), 0.U(1.W), io.switch(6), 0.U(1.W),
    io.switch(5), 0.U(1.W), io.switch(4), 0.U(1.W),
    io.switch(3), 0.U(1.W), io.switch(2), 0.U(1.W),
    io.switch(1), 0.U(1.W), io.switch(0), 0.U(1.W)
  )

  // --------------------------------------------------------------------------
  // 数码管显示
  // --------------------------------------------------------------------------
  val num_data = Reg(UInt(32.W))
  when(!io.resetn) {
    num_data := 0.U
  }.elsewhen(wr(NUM_ADDR)) {
    num_data := io.conf_wdata
  }
  io.num_data := num_data

  val count = Reg(UInt(20.W))
  when(!io.resetn) {
    count := 0.U
  }.otherwise {
    count := count + 1.U
  }

  val scan_data = Reg(UInt(4.W))
  val num_csn   = Reg(UInt(8.W))

  val scan_sel = VecInit((0 until 8).map(i => num_data(31 - 4 * i, 28 - 4 * i)))(count(19, 17))
  val csn_sel  = VecInit(Seq(
    0x7f.U(8.W), 0xbf.U(8.W), 0xdf.U(8.W), 0xef.U(8.W),
    0xf7.U(8.W), 0xfb.U(8.W), 0xfd.U(8.W), 0xfe.U(8.W)))(count(19, 17))

  when(!io.resetn) {
    scan_data := 0.U
    num_csn   := 0xff.U
  }.otherwise {
    scan_data := scan_sel
    num_csn   := csn_sel
  }
  io.num_csn := num_csn

  val num_a_g = Reg(UInt(7.W))
  val seg_rom = VecInit(Seq(
    0x7e.U(7.W), 0x30.U(7.W), 0x6d.U(7.W), 0x79.U(7.W),
    0x33.U(7.W), 0x5b.U(7.W), 0x5f.U(7.W), 0x70.U(7.W),
    0x7f.U(7.W), 0x7b.U(7.W), 0x77.U(7.W), 0x1f.U(7.W),
    0x4e.U(7.W), 0x3d.U(7.W), 0x4f.U(7.W), 0x47.U(7.W)))
  when(!io.resetn) {
    num_a_g := 0.U
  }.otherwise {
    num_a_g := seg_rom(scan_data)
  }
  io.num_a_g := num_a_g

  // --------------------------------------------------------------------------
  // 按键（消抖 + 状态机）
  // --------------------------------------------------------------------------
  val btn_key_r = Reg(UInt(16.W))
  val btn_key_data = Cat(0.U(16.W), btn_key_r)

  val state      = Reg(UInt(3.W))
  val next_state = Wire(UInt(3.W))
  val key_flag   = Reg(Bool())
  val key_count  = Reg(UInt(20.W))
  val state_count = Reg(UInt(4.W))

  val key_start  = (state === 0.U) && !io.btn_key_row.andR
  val key_end    = (state === 7.U) && io.btn_key_row.andR
  val key_sample = key_count(19)

  when(!io.resetn) {
    key_flag := false.B
  }.elsewhen(key_sample && state_count(3)) {
    key_flag := false.B
  }.elsewhen(key_start || key_end) {
    key_flag := true.B
  }

  when(!io.resetn || !key_flag) {
    key_count := 0.U
  }.otherwise {
    key_count := key_count + 1.U
  }

  when(!io.resetn || state_count(3)) {
    state_count := 0.U
  }.otherwise {
    state_count := state_count + 1.U
  }

  when(!io.resetn) {
    state := 0.U
  }.elsewhen(state_count(3)) {
    state := next_state
  }

  next_state := MuxLookup(state, 0.U(3.W), Seq(
    0.U -> Mux(key_sample && !io.btn_key_row.andR, 1.U(3.W), 0.U(3.W)),
    1.U -> Mux(!io.btn_key_row.andR, 7.U(3.W), 2.U(3.W)),
    2.U -> Mux(!io.btn_key_row.andR, 7.U(3.W), 3.U(3.W)),
    3.U -> Mux(!io.btn_key_row.andR, 7.U(3.W), 4.U(3.W)),
    4.U -> Mux(!io.btn_key_row.andR, 7.U(3.W), 0.U(3.W)),
    7.U -> Mux(key_sample && io.btn_key_row.andR, 0.U(3.W), 7.U(3.W))
  ))

  io.btn_key_col := MuxLookup(state, 0.U(4.W), Seq(
    0.U -> 0x0.U(4.W),
    1.U -> 0xe.U(4.W),
    2.U -> 0xd.U(4.W),
    3.U -> 0xb.U(4.W),
    4.U -> 0x7.U(4.W)
  ))

  val btn_key_tmp = Wire(UInt(16.W))
  btn_key_tmp := MuxLookup(Cat(state, io.btn_key_row), 0.U(16.W), Seq(
    Cat(1.U(3.W), 0xe.U(4.W)) -> 0x0001.U(16.W),
    Cat(1.U(3.W), 0xd.U(4.W)) -> 0x0010.U(16.W),
    Cat(1.U(3.W), 0xb.U(4.W)) -> 0x0100.U(16.W),
    Cat(1.U(3.W), 0x7.U(4.W)) -> 0x1000.U(16.W),
    Cat(2.U(3.W), 0xe.U(4.W)) -> 0x0002.U(16.W),
    Cat(2.U(3.W), 0xd.U(4.W)) -> 0x0020.U(16.W),
    Cat(2.U(3.W), 0xb.U(4.W)) -> 0x0200.U(16.W),
    Cat(2.U(3.W), 0x7.U(4.W)) -> 0x2000.U(16.W),
    Cat(3.U(3.W), 0xe.U(4.W)) -> 0x0004.U(16.W),
    Cat(3.U(3.W), 0xd.U(4.W)) -> 0x0040.U(16.W),
    Cat(3.U(3.W), 0xb.U(4.W)) -> 0x0400.U(16.W),
    Cat(3.U(3.W), 0x7.U(4.W)) -> 0x4000.U(16.W),
    Cat(4.U(3.W), 0xe.U(4.W)) -> 0x0008.U(16.W),
    Cat(4.U(3.W), 0xd.U(4.W)) -> 0x0080.U(16.W),
    Cat(4.U(3.W), 0xb.U(4.W)) -> 0x0800.U(16.W),
    Cat(4.U(3.W), 0x7.U(4.W)) -> 0x8000.U(16.W)
  ))

  when(!io.resetn) {
    btn_key_r := 0.U
  }.elsewhen(next_state === 0.U) {
    btn_key_r := 0.U
  }.elsewhen(next_state === 7.U && state =/= 7.U && state_count(3)) {
    btn_key_r := btn_key_tmp
  }

  // --------------------------------------------------------------------------
  // step 按键（两路独立消抖）
  // --------------------------------------------------------------------------
  val btn_step0_r = Reg(Bool())
  val btn_step1_r = Reg(Bool())
  val btn_step_data = Cat(0.U(30.W), ~btn_step0_r, ~btn_step1_r)

  val step0_flag  = Reg(Bool())
  val step0_count = Reg(UInt(20.W))
  val step0_start  = btn_step0_r && !io.btn_step(0)
  val step0_end    = !btn_step0_r && io.btn_step(0)
  val step0_sample = step0_count(19)

  when(!io.resetn) {
    step0_flag := false.B
  }.elsewhen(step0_sample) {
    step0_flag := false.B
  }.elsewhen(step0_start || step0_end) {
    step0_flag := true.B
  }

  when(!io.resetn || !step0_flag) {
    step0_count := 0.U
  }.otherwise {
    step0_count := step0_count + 1.U
  }

  when(!io.resetn) {
    btn_step0_r := true.B
  }.elsewhen(step0_sample) {
    btn_step0_r := io.btn_step(0)
  }

  val step1_flag  = Reg(Bool())
  val step1_count = Reg(UInt(20.W))
  val step1_start  = btn_step1_r && !io.btn_step(1)
  val step1_end    = !btn_step1_r && io.btn_step(1)
  val step1_sample = step1_count(19)

  when(!io.resetn) {
    step1_flag := false.B
  }.elsewhen(step1_sample) {
    step1_flag := false.B
  }.elsewhen(step1_start || step1_end) {
    step1_flag := true.B
  }

  when(!io.resetn || !step1_flag) {
    step1_count := 0.U
  }.otherwise {
    step1_count := step1_count + 1.U
  }

  when(!io.resetn) {
    btn_step1_r := true.B
  }.elsewhen(step1_sample) {
    btn_step1_r := io.btn_step(1)
  }

  // --------------------------------------------------------------------------
  // conf_rdata
  // --------------------------------------------------------------------------
  io.conf_rdata := MuxCase(0.U(32.W), Seq(
    (a16 === CR0_ADDR)          -> cr0,
    (a16 === CR1_ADDR)          -> cr1,
    (a16 === CR2_ADDR)          -> cr2,
    (a16 === CR3_ADDR)          -> cr3,
    (a16 === CR4_ADDR)          -> cr4,
    (a16 === CR5_ADDR)          -> cr5,
    (a16 === CR6_ADDR)          -> cr6,
    (a16 === CR7_ADDR)          -> cr7,
    (a16 === LED_ADDR)          -> led_data,
    (a16 === LED_RG0_ADDR)      -> led_rg0_data,
    (a16 === LED_RG1_ADDR)      -> led_rg1_data,
    (a16 === NUM_ADDR)          -> num_data,
    (a16 === SWITCH_ADDR)       -> switch_data,
    (a16 === BTN_KEY_ADDR)      -> btn_key_data,
    (a16 === BTN_STEP_ADDR)     -> btn_step_data,
    (a16 === SW_INTER_ADDR)     -> sw_inter_data,
    (a16 === TIMER_ADDR)        -> timer_r2,
    (a16 === SIMU_FLAG_ADDR)    -> simu_flag,
    (a16 === IO_SIMU_ADDR)      -> io_simu,
    (a16 === VIRTUAL_UART_ADDR) -> Cat(0.U(24.W), virtual_uart_data),
    (a16 === OPEN_TRACE_ADDR)   -> Cat(0.U(31.W), open_trace),
    (a16 === NUM_MONITOR_ADDR)  -> Cat(0.U(31.W), num_monitor)
  ))
}
