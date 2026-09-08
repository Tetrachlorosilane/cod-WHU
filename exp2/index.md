---
layout: default
title: 实践任务2 · 寄存器堆仿真
nav_title: 实践任务2 寄存器堆仿真
nav_order: 2
---

# 实践任务2：寄存器堆仿真

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](../index.md) ｜ [逐行对照](../index.md)

> **任务类型**：Verilog 版（Chisel 改写自 exp5 起）
>
> **代码目录**：`code/`（本站收录的原 Verilog 源码）

## 实验目标

1. 对一个寄存器堆设计进行功能仿真，通过观察其仿真波形了解行为特征。
2. 使用Vivado新建一个工程。
3. 点击“Add Sources”，选择添加设计源码（design sources），加入regfile.v。
4. 点击“Add Sources”，选择添加仿真源码（simulation sources），加入rf\_tb.v。
5. 对工程进行仿真测试，结合波形观察寄存器堆的读写行为。

## 关键代码

### `regfile.v`

> 源文件：[code/regfile.v](code/regfile.v)

```verilog
module regfile(
  input         clk,
  input  [ 4:0] raddr1,
  output [31:0] rdata1,
  input  [ 4:0] raddr2,
  output [31:0] rdata2,
  input         we,
  input  [ 4:0] waddr,
  input  [31:0] wdata
);
reg [31:0] rf[31:0];
// WRITE
always @(posedge clk) begin
    if (we) rf[waddr]<= wdata;
end
// READ OUT 1
assign rdata1 = (raddr1==5'b0) ? 32'b0 : rf[raddr1];
// READ OUT 2
assign rdata2 = (raddr2==5'b0) ? 32'b0 : rf[raddr2];
endmodule
```

### `rf_tb.v`

> 源文件：[code/rf_tb.v](code/rf_tb.v)

```verilog
`timescale 1ns / 1ps

module tb_top();

reg         clk;

reg  [ 4:0] raddr1;
wire [31:0] rdata1;
reg  [ 4:0] raddr2;
wire [31:0] rdata2;
reg         we;
reg  [ 4:0] waddr;
reg  [31:0] wdata;

reg  [ 3:0] task_phase;

regfile u_regfile(
    .clk      (clk       ),
    .raddr1   (raddr1    ),
    .rdata1   (rdata1    ),
    .raddr2   (raddr2    ),
    .rdata2   (rdata2    ),
    .we       (we        ),
    .waddr    (waddr     ),
    .wdata    (wdata     ) 
);

//clk
initial 
begin
    clk = 1'b1;
end
always #5 clk = ~clk;
					
initial 
begin
    raddr1 =  5'd0;
    raddr2 =  5'd0;
	waddr  =  5'd0;
	wdata  = 32'd0;
	we     =  1'd0;
	task_phase =  4'd0;
	#2000;
	
	$display("=============================");
	$display("Test Begin");
	#1;
	// Part 0 Begin
	#10;
	task_phase = 4'd0;
	we         = 1'b0;
	waddr      = 5'd1;
	wdata      = 32'hffffffff;
    raddr1     = 5'd1;
    #10;
	we         = 1'b1;
	waddr      = 5'd1;
	wdata      = 32'h1111ffff;
    #10;
	we         = 1'b0;
    raddr1     = 5'd2;
    raddr2     = 5'd1;
	#10;
    raddr1     = 5'd1;
    
    #200;
    // Part 1 Begin
    #10;
	task_phase = 4'd1;
	we         = 1'b1;
// …（共 132 行，其余见源文件）
```

## 待操作代码（TODO）

> 本任务是 Verilog 版实验（Chisel 改写自 exp5 起），待操作内容见 [原任务说明](task.md)。

## 实验验收

| 项 | 说明 |
|---|---|
| 原实验判据 | 见原任务说明 |
| Chisel 版判据 | rf_tb 仿真通过 |
| 运行方式 | 见原任务说明（Vivado 仿真） |
| ✅ 编译验证 | — |

## 参考

- 原任务说明：[`task.md`](task.md)（硬链接到 `taskvscode/exp2/3.1.1实践任务2-寄存器堆仿真.md`）
- 原书（LoongArch 版）：https://bookdown.org/loongson/_book3/

---

[← 实践任务1](../exp1/index.md) ｜ [实践任务3 →](../exp3/index.md)
