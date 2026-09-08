---
layout: default
title: 实践任务1 · 跑马灯
nav_title: 实践任务1 跑马灯
nav_order: 1
---

# 实践任务1：跑马灯

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](../index.md)

> **任务类型**：Verilog 版（Chisel 改写自 exp5 起）

## 实验目标

- 完成实践任务1：跑马灯。

## 关键代码

### `scroller.v`

> 源文件：[code/scroller.v](code/scroller.v)

```verilog
`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 2017/12/11 00:10:26
// Design Name: 
// Module Name: scroller
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////


module scroller #(
    parameter CNT_1S = 27'd100_000_000
)(
    input         clk,
    input         resetn,
    output reg [15:0] led
);

reg [26:0] cnt;
wire cnt_eq_1s;
assign cnt_eq_1s = (cnt==(CNT_1S-1));
always @(posedge clk)
begin
    if (!resetn)
    begin
        cnt <= 27'd0;
    end
    else if (cnt_eq_1s)
    begin
        cnt <= 27'd0;
    end
    else
    begin
        cnt <= cnt + 1'b1;
    end
end

always @(posedge clk)
begin
    if (!resetn)
    begin
        led <= 16'hfffe;
    end
    else if (cnt_eq_1s)
    begin
        led <= {led[14:0],led[15]};
    end
end
endmodule
```

### `testbench.v`

> 源文件：[code/testbench.v](code/testbench.v)

```verilog
`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 2017/12/11 00:21:48
// Design Name: 
// Module Name: testbench
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////


module testbench(

    );
reg clk;
reg resetn;
initial
begin
    $dumpfile("dump.vcd");
    $dumpvars;
    clk = 0;
    resetn = 1'b0;
    #200;
    resetn = 1'b1;
end
    always #5 clk <= ~clk;
    
    scroller #(
        .CNT_1S(  27'd100 )
    ) u_scroller (
        .clk(clk),
        .resetn(resetn),
        .led()
    );
endmodule
```

## 待操作代码（TODO）

> 本任务是 Verilog 版实验（Chisel 改写自 exp5 起），待操作内容见 [原任务说明](task.md)。

## 实验验收

| 项 | 说明 |
|---|---|
| 原实验判据 | 见原任务说明 |
| Chisel 版判据 | 上板观察 LED 流水；testbench 仿真波形 |
| 运行方式 | 见原任务说明（Vivado 仿真） |

## 参考

- 原任务说明：[`task.md`](task.md)
- 教材：[《CPU 设计实战：LoongArch 版》](https://bookdown.org/loongson/_book3/)（汪文祥、邢金璋 等著）
- Chisel 写法参考：[Verilog to Chisel（黄治豪）](https://zihaojf.github.io/Chisel-/chisel/%E7%AE%80%E4%BB%8B/)

---

[实践任务2 →](../exp2/index.md)
