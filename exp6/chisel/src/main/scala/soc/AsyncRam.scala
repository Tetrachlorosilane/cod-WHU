// SPDX-License-Identifier: BSD-3-Clause
// 对应原 Verilog：code/soc_verify/soc_dram/testbench/async_ram.v
//   提供 async_ram / inst_ram / data_ram 三个模块（仿真用的分布式 RAM 模型）。
//
// 原 Verilog：
//     assign rdata = (!we) ? data_out : {DATA_WIDTH{1'bz}};   // 写周期输出 Z
//     always @(posedge clk) if (we) ram[address] <= wdata;    // 同步写
//     always @*             if (!we) data_out = ram[address]; // 异步读
//     initial $readmemb(".../func/obj/inst_ram.mif", async_ram.ram);
//
// Chisel 差异（见 ../MAPPING.md）：
//   1. Chisel 没有三态/Z，写周期读数据取 0（原为 Z；CPU 在写周期不会读数据）。
//   2. 存储器初始化：原用 $readmemb 读 .mif；Chisel 版把内容作为构造参数 init 传入，
//      由测试侧读取 .mif 文件（见 src/test/scala/MifLoader.scala），
//      这样不需要任何 experimental API。
//   3. 深度按 init 长度向上取 2 的幂（addrWidth 参数控制），不影响仿真行为。

package exp6.soc

import chisel3._
import chisel3.util._

class AsyncRam(
  init:      Seq[BigInt] = Nil,
  addrWidth: Int         = 15,
  dataWidth: Int         = 32
) extends Module {
  val depth = 1 << addrWidth

  val io = IO(new Bundle {
    val address = Input(UInt(addrWidth.W))
    val we      = Input(Bool())
    val wdata   = Input(UInt(dataWidth.W))
    val rdata   = Output(UInt(dataWidth.W))
  })

  val ram = RegInit(VecInit(init.padTo(depth, BigInt(0)).map(_.U(dataWidth.W))))

  // MEM_WRITE：同步写
  when(io.we) {
    ram(io.address) := io.wdata
  }

  // MEM_READ：异步读（写周期原输出 Z，Chisel 取 0）
  io.rdata := Mux(io.we, 0.U(dataWidth.W), ram(io.address))
}

/** 对应 async_ram.v 的 `inst_ram` 包装（同步写 + 异步读，端口 we/a/d/spo）。 */
class InstRam(init: Seq[BigInt] = Nil, addrWidth: Int = 15, dataWidth: Int = 32) extends Module {
  val io = IO(new Bundle {
    val we  = Input(Bool())
    val a   = Input(UInt(addrWidth.W))
    val d   = Input(UInt(dataWidth.W))
    val spo = Output(UInt(dataWidth.W))
  })

  val ram = Module(new AsyncRam(init, addrWidth, dataWidth))
  ram.io.address := io.a
  ram.io.we      := io.we
  ram.io.wdata   := io.d
  io.spo         := ram.io.rdata
}

/** 对应 async_ram.v 的 `data_ram` 包装。 */
class DataRam(init: Seq[BigInt] = Nil, addrWidth: Int = 15, dataWidth: Int = 32) extends Module {
  val io = IO(new Bundle {
    val we  = Input(Bool())
    val a   = Input(UInt(addrWidth.W))
    val d   = Input(UInt(dataWidth.W))
    val spo = Output(UInt(dataWidth.W))
  })

  val ram = Module(new AsyncRam(init, addrWidth, dataWidth))
  ram.io.address := io.a
  ram.io.we      := io.we
  ram.io.wdata   := io.d
  io.spo         := ram.io.rdata
}
