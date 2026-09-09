---
layout: default
title: '实践任务1 · 跑马灯'
nav_order: 1
exp: 1
exp_intent: 'Verilog 版（Chisel 改写自 exp5 起）'
exp_todos: 0
exp_has_chisel: false
exp_orig_judge: '见原任务说明'
exp_judge: '上板观察 LED 流水；testbench 仿真波形'
exp_needs_assets: false
---

# 实践任务1：跑马灯

{% include exp-nav.html %}

## 实验目标

- 完成实践任务1：跑马灯。

## 提示与易错点

> **💡 概念**：LED 一般是低有效：`led = ~led_data`，位为 0 时点亮。动手前先确认板级极性，否则现象会反过来。
>
> **⚠️ 易错**：分频要用计数器实现，比较值按实际时钟频率计算（本平台 100 MHz，闪烁 1 Hz 需要计到 10^8）；仿真里的 `#delay` 在综合中会被忽略。
>
> **⚠️ 易错**：复位后第一个周期计数器的取值取决于复位是同步还是异步，先仿真确认初值再上板。
>

## 关键代码

下面给出本实验的接口骨架与环境代码。请**先读懂注释里的接口约定**再动手：接口理解错，后面的调试会非常费时。

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

下表列出本实验全部需要补全的位置，每处都附了上下文。请对照教材与本实验的 `chisel/README.md` 逐项完成。

> 本任务是 Verilog 版实验（Chisel 改写自 exp5 起），待操作内容见 [原任务说明](task.html)。

## 实验验收

完成后按下面的判据自检——能复现原实验的验收条件，才算真正完成。

{% include exp-accept.html %}

## 参考

{% include exp-refs.html %}

{% include exp-pager.html %}
