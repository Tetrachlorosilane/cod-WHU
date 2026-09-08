// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：exp7~exp16 的 code/soc_verify/*/testbench/sync_ram.v
//
// 原 Verilog：
//     for (i = 0; i < NUM_BYTES; i = i + 1)
//       always @(posedge clk) if (we[i] && en) ram[address][i*8+:8] <= wdata[i*8+:8];
//     always @(posedge clk) if (en) rdata <= ram[address];        // 同步读
//     initial $readmemb(".../func/obj/inst_ram.mif", sync_ram.ram);
//
// Chisel 差异：
//   1. 存储器初始化由构造参数 init 传入（测试侧读取 .mif），不用 experimental API。
//   2. 字节写用「读-改-写」等价实现：`ram(addr)(i*8+7, i*8) := wdata(...)` 这种
//      动态索引 + 子字赋值在 Chisel 中不可靠，故先复制旧值再逐字节覆盖。
//   3. 深度按 init 长度向上取 2 的幂（addrWidth 参数控制），不影响仿真行为。

package envlib.soc

import chisel3._
import chisel3.util._

class SyncRam(
  init:      Seq[BigInt] = Nil,
  addrWidth: Int         = 16,
  dataWidth: Int         = 32
) extends Module {
  val depth    = 1 << addrWidth
  val numBytes = dataWidth / 8

  val io = IO(new Bundle {
    val address = Input(UInt(addrWidth.W))
    val we      = Input(UInt(numBytes.W))
    val en      = Input(Bool())
    val wdata   = Input(UInt(dataWidth.W))
    val rdata   = Output(UInt(dataWidth.W))
  })

  val ram = RegInit(VecInit(init.padTo(depth, BigInt(0)).map(_.U(dataWidth.W))))

  // 字节写使能：读-改-写
  when(io.en && io.we.orR) {
    val next = Wire(UInt(dataWidth.W))
    next := ram(io.address)
    for (i <- 0 until numBytes) {
      when(io.we(i)) {
        next(i * 8 + 7, i * 8) := io.wdata(i * 8 + 7, i * 8)
      }
    }
    ram(io.address) := next
  }

  // 同步读（读数据打一拍）
  val rdata_r = Reg(UInt(dataWidth.W))
  when(io.en) {
    rdata_r := ram(io.address)
  }
  io.rdata := rdata_r
}

/** 对应 sync_ram.v 的 `inst_ram` 包装（端口名与原文一致）。 */
class InstRam(init: Seq[BigInt] = Nil, addrWidth: Int = 18, dataWidth: Int = 32) extends Module {
  val io = IO(new Bundle {
    val ena   = Input(Bool())
    val wea   = Input(UInt(4.W))
    val addra = Input(UInt(addrWidth.W))
    val dina  = Input(UInt(dataWidth.W))
    val douta = Output(UInt(dataWidth.W))
  })

  val m = Module(new SyncRam(init, addrWidth, dataWidth))
  m.io.address := io.addra
  m.io.we      := io.wea
  m.io.en      := io.ena
  m.io.wdata   := io.dina
  io.douta     := m.io.rdata
}

/** 对应 sync_ram.v 的 `data_ram` 包装。 */
class DataRam(init: Seq[BigInt] = Nil, addrWidth: Int = 16, dataWidth: Int = 32) extends Module {
  val io = IO(new Bundle {
    val ena   = Input(Bool())
    val wea   = Input(UInt(4.W))
    val addra = Input(UInt(addrWidth.W))
    val dina  = Input(UInt(dataWidth.W))
    val douta = Output(UInt(dataWidth.W))
  })

  val m = Module(new SyncRam(init, addrWidth, dataWidth))
  m.io.address := io.addra
  m.io.we      := io.wea
  m.io.en      := io.ena
  m.io.wdata   := io.dina
  io.douta     := m.io.rdata
}
