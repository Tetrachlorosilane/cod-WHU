---
layout: default
title: 实践任务3 · 同步 RAM 和异步 RAM 仿真、综合与实现
nav_title: 实践任务3 同步 RAM 和异步 RAM 仿真、综合与实现
nav_order: 3
---

# 实践任务3：同步 RAM 和异步 RAM 仿真、综合与实现

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](../index.md) ｜ [逐行对照](../index.md)

> **任务类型**：Verilog 版（Chisel 改写自 exp5 起）
>
> **代码目录**：`code/`（本站收录的原 Verilog 源码）

## 实验目标

1. 调用Xilinx库IP实例化一个同步RAM，进行仿真以观察行为，进行综合和实现后查看时序结果和资源利用率。
2. 调用Xilinx库IP实例化一个异步RAM，进行仿真以观察行为，进行综合和实现后查看时序结果和资源利用率。
3. 对观察到的现象进行对比分析。
4. 使用Vivado新建一个工程。
5. 点击“Add Sources”，选择添加设计源码（design sources），加入block\_ram\_top.v。
6. 点击“Add Sources”，选择添加约束文件（constraints），加入ram.xdc。
7. 点击“Add Sources”，选择添加仿真源码（simulation sources），加入ram\_tb.v。
8. 参考第[**??**](#sec-digital-circuit-verilog-coding)节，调用Xilinx库IP生成同步RAM（Block RAM，深度为65536，宽度为32，片选使能信号设为一直有效）。
9. 使用Vivado新建一个工程。
10. 点击“Add Sources”，选择添加设计源码（design sources），加入distributed\_ram\_top.v。
11. 点击“Add Sources”，选择添加约束文件（constraints），加入ram.xdc。
12. 点击“Add Sources”，选择添加仿真源码（simulation sources），加入ram\_tb.v。
13. 参考附录[D](https://bookdown.org/loongson/_book3/appendix-vivado-advanced-usage.html#appendix-vivado-advanced-usage)第[D.1](https://bookdown.org/loongson/_book3/appendix-vivado-advanced-usage.html#sec-vivado-generate-ram-ip)节，调用Xilinx库IP生成异步RAM（Distributed RAM，深度为65536，宽度为32）。

## 关键代码

### `block_ram_top.v`

> 源文件：[code/block_ram_top.v](code/block_ram_top.v)

```verilog
module ram_top (
    input         clk      ,
    input  [15:0] ram_addr ,
    input  [31:0] ram_wdata,
    input         ram_wen  ,
    output [31:0] ram_rdata
);
					   
block_ram block_ram (
    .clka (clk       ),
    .wea  (ram_wen   ),
    .addra(ram_addr  ),
    .dina (ram_wdata ),
    .douta(ram_rdata ) 
);
endmodule
```

### `distributed_ram_top.v`

> 源文件：[code/distributed_ram_top.v](code/distributed_ram_top.v)

```verilog
module ram_top (
    input         clk      ,
    input  [15:0] ram_addr ,
    input  [31:0] ram_wdata,
    input         ram_wen  ,
    output [31:0] ram_rdata		   
);
					   
distributed_ram distributed_ram(
    .clk (clk       ),
    .we  (ram_wen   ),
    .a   (ram_addr  ),
    .d   (ram_wdata ),
    .spo (ram_rdata ) 
);

endmodule
```

### `ram_tb.v`

> 源文件：[code/ram_tb.v](code/ram_tb.v)

```verilog
`timescale 1ns / 1ps

module tb_top();

reg         clk;

reg         ram_wen;
reg  [15:0] ram_addr;
reg  [31:0] ram_wdata;
wire [31:0] ram_rdata;
reg  [3 :0] task_phase;

ram_top u_ram_top(
    .clk      (clk       ),
    .ram_wen  (ram_wen   ),
    .ram_addr (ram_addr  ),
    .ram_wdata(ram_wdata ),
    .ram_rdata(ram_rdata ) 
);

//clk
initial 
begin
    clk = 1'b1;
end
always #5 clk = ~clk;
					
initial 
begin
	ram_addr   = 16'd0;
	ram_wdata  = 32'd0;
	ram_wen    =  1'd0;
	task_phase =  4'd0;
	#2000;
	
	$display("=============================");
	$display("Test Begin");
	#1;
	// Part 0 Begin
	#10;
	task_phase = 4'd0;
	ram_wen    = 1'b0;
	ram_addr   = 16'hf0;
	ram_wdata  = 32'hffffffff;
    #10;
	ram_wen    = 1'b1;
	ram_addr   = 16'hf0;
	ram_wdata  = 32'h11223344;
    #10;
	ram_wen    = 1'b0;
	ram_addr   = 16'hf1;
	#10;
	ram_wen    = 1'b0;
	ram_addr   = 16'hf0;
    
    #200;
    // Part 1 Begin
    #10;
	task_phase = 4'd1;
	ram_wen    = 1'b1;
	ram_wdata  = 32'hff00;
	ram_addr   = 16'hf0;
	#10;
	ram_wdata  = 32'hff11;
	ram_addr   = 16'hf1;
	#10;
	ram_wdata  = 32'hff22;
	ram_addr   = 16'hf2;
	#10;
	ram_wdata  = 32'hff33;
// …（共 100 行，其余见源文件）
```

## 待操作代码（TODO）

> 本任务是 Verilog 版实验（Chisel 改写自 exp5 起），待操作内容见 [原任务说明](task.md)。

## 实验验收

| 项 | 说明 |
|---|---|
| 原实验判据 | 见原任务说明 |
| Chisel 版判据 | ram_tb 仿真通过 + 综合实现报告 |
| 运行方式 | 见原任务说明（Vivado 仿真） |
| ✅ 编译验证 | — |

## 参考

- 原任务说明：[`task.md`](task.md)（硬链接到 `taskvscode/exp3/3.1.2实践任务3-同步RAM和异步RAM仿真、综合与实现.md`）
- 原书（LoongArch 版）：https://bookdown.org/loongson/_book3/

---

[← 实践任务2](../exp2/index.md) ｜ [实践任务4 →](../exp4/index.md)
