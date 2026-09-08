---
layout: default
title: 实践任务4 · 数字逻辑电路的设计与调试
parent: 实践任务
nav_order: 4
---

# 实践任务4：数字逻辑电路的设计与调试

[← 返回首页](../index.md) ｜ [原任务说明](task.md) ｜ [Chisel 环境说明](../index.md) ｜ [逐行对照](../index.md)

> **任务类型**：Verilog 版（Chisel 改写自 exp5 起）
>
> **代码目录**：`code/`（本站收录的原 Verilog 源码）

## 实验目标

1. 调试并修正一个给定数字逻辑电路设计中的功能错误。
2. 上板后可以实现正确的功能。
3. 获取开发板最右侧4个拨码开关的状态（记为“拨上为1，拨下为0”，实际开发板上拨码开关的电平是“拨上为低电平，拨下为高电平”），共有16个状态（数字编号是0~15）。
4. 最左侧数码管实时显示4个拨码开关的状态。数码管只支持显示0~9，如果拨码开关状态是10~15，则数码管的显示状态不更改（显示上一次的显示值）。
5. 最右侧的4个单色LED灯会显示上一次的拨码开关的状态，支持显示0~15（拨码开关拨上，对应LED灯亮）。
6. 学习本章第[**??**](#sec-digital-circuit-debug-skills)节和附录[D](https://bookdown.org/loongson/_book3/appendix-vivado-advanced-usage.html#appendix-vivado-advanced-usage)第[D.1](https://bookdown.org/loongson/_book3/appendix-vivado-advanced-usage.html#sec-vivado-generate-ram-ip)节的内容。
7. 使用Vivado新建一个工程。
8. 点击“Add Sources”，选择添加设计源码（design sources），加入show\_sw.v。
9. 点击“Add Sources”，选择添加约束文件（constraints），加入show\_sw.xdc。
10. 点击“Add Sources”，选择添加仿真源码（simulation sources），加入tb.v。
11. 理解示例设计的功能，分析仿真顶层tb.v，并理解仿真的行为。注意，开发板上拨码开关的电平是“拨上为低电平，拨下为高电平”，单色LED灯的电平行为是“高电平不亮，低电平亮”。仿真顶层tb.v也是按此电平设计的。
12. 进行仿真，并充分利用仿真的辅助小技巧（分割、分组、颜色变化、标志等）进行调试，找出所有的bug。
13. 仿真完成后，进行综合、实现并生成比特流文件。
14. 生成比特流文件后，连接开发板，进行上板验证。（如果没有本地或远程FPGA实验平台或者不进行上板实验，可以跳过此步骤。）

## 关键代码

### `show_sw.v`

> 源文件：[code/show_sw.v](code/show_sw.v)

```verilog

module show_sw (
    input             clk,          
    input             resetn,     

    input      [3 :0] switch,    //input

    output     [7 :0] num_csn,   //new value   
    output     [6 :0] num_a_g,      

    output     [3 :0] led        //previous value
);
//1. get switch data
//2. show switch data in digital number:
//   only show 0~9
//   if >=10, digital number keep old data.
//3. show previous switch data in led.
//   can show any switch data.

reg [3:0] show_data;
reg [3:0] show_data_r;
reg [3:0] prev_data;

//new value
always @(posedge clk)
begin
//    show_data   <= ~switch;
end

always @(posedge clk)
begin
    show_data_r = show_data;
end
//previous value
always @(posedge clk)
begin
    if(!resetn)
    begin
        prev_data <= 4'd0;
    end
    else if(show_data_r != show_data)
    begin
        prev_data <= show_data_r;
    end
end

//show led: previous value
assign led = ~prev_data;

//show number: new value
show_num u_show_num(
        .clk        (clk      ),
        .resetn     (resetn   ),
        
        .show_data  (show_data),
        .num_csn    (num_scn  ),
        .num_a_g    (num_a_g  )
);

endmodule

//---------------------------{digital number}begin-----------------------//
module show_num (
    input             clk,          
    input             resetn,     

    input      [3 :0] show_data,
    output     [7 :0] num_csn,      
    output reg [6 :0] num_a_g      
);
// …（共 104 行，其余见源文件）
```

### `tb.v`

> 源文件：[code/tb.v](code/tb.v)

```verilog
module tb;
reg        clk   ;
reg        resetn;     
reg [3 :0] switch;    //input

initial
begin
    #100;
    clk    = 1'b0;
    resetn = 1'b0;

    #500;
    resetn = 1'b1;
end
always #5 clk = ~clk;

//set switch
initial
begin
    #100;
    switch = 4'hf;
    #500;
    #1;
    switch = 4'h8;  //~switch: 7
    #100;
    switch = 4'h9;  //~switch: 6
    #100;
    switch = 4'he;  //~switch: 1
    #100;
    switch = 4'h2;  //~switch: d
    #100;
    switch = 4'h0;  //~switch: f
end

show_sw  u_show_sw(
    .clk    (clk    ),          
    .resetn (resetn ),     

    .switch (switch ),    //input

    .num_csn(),   //new value   
    .num_a_g(),      

    .led    ()    //previous value
);
endmodule
```

## 待操作代码（TODO）

> 本任务是 Verilog 版实验（Chisel 改写自 exp5 起），待操作内容见 [原任务说明](task.md)。

## 实验验收

| 项 | 说明 |
|---|---|
| 原实验判据 | 见原任务说明 |
| Chisel 版判据 | tb 仿真通过 + 上板 |
| 运行方式 | 见原任务说明（Vivado 仿真） |
| ⚠️ 未实测 | — |

## 参考

- 原任务说明：[`task.md`](task.md)（硬链接到 `taskvscode/exp4/3.1.3实践任务4-数字逻辑电路的设计与调试.md`）
- 原书（LoongArch 版）：https://bookdown.org/loongson/_book3/

---

[← 实践任务3](../exp3/index.md) ｜ [实践任务5 →](../exp5/index.md)
