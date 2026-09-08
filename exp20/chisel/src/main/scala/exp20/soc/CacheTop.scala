// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// 对应原 Verilog：code/rtl/cache_top.v（417 行，Cache 模块级验证环境）
//
// 该环境按 index 递增地做"先写后读"验证：
//   PREPARE：用伪随机数准备 4 组 (tag, data[127:0])；
//   WRITE  ：对每个 index 发出 4 次写请求（每次写 1 个 32 位字，第 4 次用 wstrb=4'b0111），
//            并检查 Cache 是否正确地以 0xFFFFFFFF 响应 miss 时的 rd 请求、以及替换时的数据；
//   READ   ：对每个 index 发出 4 次读请求，比对读回数据与期望值。
// index 从 0 递增到 0xff 全部通过后由 testbench 打印 ----PASS!!!。
//
// Chisel 差异：
//   * clk_pll 不建模（隐式 clock）；
//   * wait_cnt 重载值用参数 simulation 区分（5 / 800_000）；
//   * 为便于测试平台观测，增加了 4 个只读输出（testIndex/roundFinish/replaceWrong/cacheresWrong）。
// ============================================================================

package exp20.soc

import chisel3._
import chisel3.util._

class CacheTop(simulation: Boolean = true) extends Module {

  val PREPARE = 0.U(2.W)
  val WRITE   = 1.U(2.W)
  val READ    = 2.U(2.W)

  val io = IO(new Bundle {
    val resetn  = Input(Bool())
    val led     = Output(UInt(16.W))
    val switch  = Input(UInt(8.W))
    val num_csn = Output(UInt(8.W))
    val num_a_g = Output(UInt(7.W))
    // 测试平台观测（不改变原行为）
    val testIndex    = Output(UInt(8.W))
    val roundFinish  = Output(Bool())
    val replaceWrong = Output(Bool())
    val cacheresWrong = Output(Bool())
  })

  // --------------------------------------------------------------------------
  // 伪随机序列（与原文件一致）
  // --------------------------------------------------------------------------
  val switch_led = Cat(
    Fill(2, io.switch(7)), Fill(2, io.switch(6)), Fill(2, io.switch(5)), Fill(2, io.switch(4)),
    Fill(2, io.switch(3)), Fill(2, io.switch(2)), Fill(2, io.switch(1)), Fill(2, io.switch(0)))
  val led_r_n = ~switch_led

  val pseudo_random_23 = Reg(UInt(23.W))
  when(!io.resetn) {
    pseudo_random_23 := Mux(simulation.B,
                            Cat(0x55.U(7.W), 0x00ff.U(16.W)),
                            Cat(0x55.U(7.W), led_r_n))
  }.otherwise {
    pseudo_random_23 := Cat(pseudo_random_23(21, 0), pseudo_random_23(22) ^ pseudo_random_23(17))
  }

  // --------------------------------------------------------------------------
  // wait_1s
  // --------------------------------------------------------------------------
  val wait_cnt = Reg(UInt(27.W))
  val wait_1s  = wait_cnt === 0.U
  when(!io.resetn || wait_1s) {
    wait_cnt := (if (simulation) 5.U(27.W) else 800000.U(27.W))
  }.otherwise {
    wait_cnt := wait_cnt - 1.U
  }

  // --------------------------------------------------------------------------
  // 状态与计数
  // --------------------------------------------------------------------------
  val tag           = Reg(Vec(4, UInt(20.W)))
  val data          = Reg(Vec(4, UInt(128.W)))
  val counter_i     = Reg(UInt(2.W))
  val counter_j     = Reg(UInt(2.W))
  val res_counter_i = Reg(UInt(2.W))
  val res_counter_j = Reg(UInt(2.W))
  val round_state   = Reg(UInt(2.W))
  val test_index    = Reg(UInt(8.W))
  val new_state     = Reg(Bool())
  val memref_valid  = Reg(Bool())

  // --------------------------------------------------------------------------
  // 被验证的 Cache（学生模块）
  // --------------------------------------------------------------------------
  val cache = Module(new exp20.student.Cache)

  cache.io.resetn := io.resetn
  cache.io.valid  := memref_valid
  cache.io.op     := round_state === WRITE
  cache.io.index  := test_index
  cache.io.tag    := tag(counter_i)
  cache.io.offset := Cat(counter_j, 0.U(2.W))
  cache.io.wstrb  := Mux(counter_j === 3.U, "b0111".U(4.W), "b1111".U(4.W))
  cache.io.wdata  := Mux1H(Seq(
    (counter_j === 0.U) -> data(counter_i)(31, 0),
    (counter_j === 1.U) -> data(counter_i)(63, 32),
    (counter_j === 2.U) -> data(counter_i)(95, 64),
    (counter_j === 3.U) -> data(counter_i)(127, 96)))

  val addr_ok   = cache.io.addr_ok && memref_valid
  val out_valid = cache.io.data_ok
  val cacheres  = cache.io.rdata

  // --------------------------------------------------------------------------
  // 轮次完成条件
  // --------------------------------------------------------------------------
  val write_finish      = (round_state === WRITE) && out_valid
  val cacheres_right    = out_valid && (cacheres === data(res_counter_i)(31, 0))
  val cacheres_wrong    = out_valid && (cacheres =/= data(res_counter_i)(31, 0)) && (round_state === READ)
  val read_finish       = (round_state === READ) && cacheres_right
  val prepare_finish    = (round_state === PREPARE) && (counter_i === 3.U) && wait_1s
  val write_round_finish = (round_state === WRITE) && (res_counter_i === 3.U) &&
                           (res_counter_j === 3.U) && write_finish
  val read_round_finish  = (round_state === READ) && (res_counter_i === 3.U) && read_finish

  val write_start = (round_state === WRITE) &&
                    (new_state || (addr_ok && !(counter_i === 3.U && counter_j === 3.U)))
  val read_start  = (round_state === READ) && (new_state || (addr_ok && !(counter_i === 3.U)))

  // --------------------------------------------------------------------------
  // test_index / 计数器 / 状态机
  // --------------------------------------------------------------------------
  when(!io.resetn) {
    test_index := 0.U
  }.elsewhen(read_round_finish && !test_index.andR) {
    test_index := test_index + 1.U
  }

  when(!io.resetn) {
    counter_i := 0.U
    counter_j := 0.U
  }.elsewhen((round_state === PREPARE) && wait_1s) {
    counter_i := counter_i + 1.U
  }.elsewhen((round_state === WRITE) && addr_ok) {
    counter_j := counter_j + 1.U
    when(counter_j === 3.U) {
      counter_i := counter_i + 1.U
    }
  }.elsewhen((round_state === READ) && addr_ok) {
    counter_i := counter_i + 1.U
  }

  when(!io.resetn) {
    res_counter_i := 0.U
    res_counter_j := 0.U
  }.elsewhen((round_state === WRITE) && write_finish) {
    res_counter_j := res_counter_j + 1.U
    when(res_counter_j === 3.U) {
      res_counter_i := res_counter_i + 1.U
    }
  }.elsewhen((round_state === READ) && read_finish) {
    res_counter_i := res_counter_i + 1.U
  }

  when(prepare_finish || write_round_finish || read_round_finish) {
    new_state := true.B
  }.elsewhen(new_state) {
    new_state := false.B
  }

  when(!io.resetn) {
    round_state := PREPARE
  }.elsewhen(prepare_finish) {
    round_state := WRITE
  }.elsewhen(write_round_finish) {
    round_state := READ
  }.elsewhen(read_round_finish && !test_index.andR) {
    round_state := PREPARE
  }

  // --------------------------------------------------------------------------
  // prepare：准备 4 组 (tag, data)
  // --------------------------------------------------------------------------
  when(!io.resetn) {
    for (i <- 0 until 4) {
      tag(i)  := 0.U
      data(i) := 0.U
    }
  }.elsewhen((round_state === PREPARE) && wait_1s) {
    tag(counter_i)  := pseudo_random_23(19, 0)
    data(counter_i) := Cat(Fill(5, pseudo_random_23), pseudo_random_23(12, 0))
  }

  // --------------------------------------------------------------------------
  // memref_valid
  // --------------------------------------------------------------------------
  when(!io.resetn) {
    memref_valid := false.B
  }.elsewhen(write_start) {
    memref_valid := true.B
  }.elsewhen(read_start) {
    memref_valid := true.B
  }.elsewhen(addr_ok) {
    memref_valid := false.B
  }

  // --------------------------------------------------------------------------
  // Cache 侧 rd 请求响应（模拟外部存储器：返回全 1 或期望数据）
  // --------------------------------------------------------------------------
  val do_rd     = Reg(Bool())
  val rd_cnt    = Reg(UInt(2.W))
  val rd_tag_r  = Reg(UInt(20.W))
  val rd_index_r = Reg(UInt(8.W))

  val rd_hit_data = Mux1H(Seq(
    (rd_tag_r === tag(0)) -> data(0),
    (rd_tag_r === tag(1)) -> data(1),
    (rd_tag_r === tag(2)) -> data(2),
    (rd_tag_r === tag(3)) -> data(3)))

  val rd_true_value = Mux1H(Seq(
    ((rd_cnt === 0.U) && (rd_index_r === test_index)) -> rd_hit_data(31, 0),
    ((rd_cnt === 1.U) && (rd_index_r === test_index)) -> rd_hit_data(63, 32),
    ((rd_cnt === 2.U) && (rd_index_r === test_index)) -> rd_hit_data(95, 64),
    ((rd_cnt === 3.U) && (rd_index_r === test_index)) -> Cat(0xff.U(8.W), rd_hit_data(119, 96))))

  cache.io.rd_rdy    := !do_rd
  cache.io.ret_valid := do_rd
  cache.io.ret_last  := rd_cnt === 3.U
  cache.io.ret_data  := Mux(round_state === WRITE, 0xffffffffL.U(32.W), rd_true_value)

  when(!io.resetn) {
    do_rd := false.B
  }.otherwise {
    when(cache.io.rd_req && !do_rd) {
      do_rd      := true.B
      rd_tag_r   := cache.io.rd_addr(31, 12)
      rd_index_r := cache.io.rd_addr(11, 4)
    }.elsewhen(do_rd && rd_cnt === 3.U) {
      do_rd := false.B
    }
  }

  when(!io.resetn) {
    rd_cnt := 0.U
  }.elsewhen(do_rd) {
    rd_cnt := rd_cnt + 1.U
  }

  // --------------------------------------------------------------------------
  // Cache 侧 wr 请求响应（检查替换/写入数据是否正确）
  // --------------------------------------------------------------------------
  val do_wr      = Reg(Bool())
  val wr_data_r  = Reg(UInt(128.W))
  val wr_tag_r   = Reg(UInt(20.W))
  val wr_index_r = Reg(UInt(8.W))

  val wr_hit_data = Mux1H(Seq(
    ((wr_tag_r === tag(0)) && (wr_index_r === test_index)) -> data(0),
    ((wr_tag_r === tag(1)) && (wr_index_r === test_index)) -> data(1),
    ((wr_tag_r === tag(2)) && (wr_index_r === test_index)) -> data(2),
    ((wr_tag_r === tag(3)) && (wr_index_r === test_index)) -> data(3)))

  val data_right    = Cat(0xff.U(8.W), wr_hit_data(119, 0)) === wr_data_r
  val replace_wrong = do_wr && (Cat(0xff.U(8.W), wr_hit_data(119, 0)) =/= wr_data_r)

  cache.io.wr_rdy := !do_wr

  when(!io.resetn) {
    do_wr := false.B
  }.otherwise {
    when(cache.io.wr_req && !do_wr) {
      do_wr      := true.B
      wr_data_r  := cache.io.wr_data
      wr_tag_r   := cache.io.wr_addr(31, 12)
      wr_index_r := cache.io.wr_addr(11, 4)
    }.elsewhen(do_wr && data_right) {
      do_wr := false.B
    }
  }

  // --------------------------------------------------------------------------
  // 数码管显示 + led
  // --------------------------------------------------------------------------
  val count = RegInit(0.U(20.W))
  count := count + 1.U

  val scan_data = Reg(UInt(4.W))
  val num_csn   = Reg(UInt(8.W))

  val scan_sel = VecInit(Seq(
    test_index(7, 4), test_index(3, 0),
    0.U(4.W), 0.U(4.W), 0.U(4.W), 0.U(4.W), 0.U(4.W), 0.U(4.W)))
  val csn_sel = VecInit(Seq(
    0x7f.U(8.W), 0xbf.U(8.W), 0xdf.U(8.W), 0xef.U(8.W),
    0xf7.U(8.W), 0xfb.U(8.W), 0xfd.U(8.W), 0xfe.U(8.W)))

  when(!io.resetn) {
    scan_data := 0.U
    num_csn   := 0xff.U
  }.otherwise {
    scan_data := scan_sel(count(19, 17))
    num_csn   := csn_sel(count(19, 17))
  }

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

  io.num_csn := num_csn
  io.num_a_g := num_a_g
  io.led     := 0xffff.U(16.W)

  io.testIndex     := test_index
  io.roundFinish   := read_round_finish
  io.replaceWrong  := replace_wrong
  io.cacheresWrong := cacheres_wrong
}
