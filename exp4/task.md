---
layout: default
nav_exclude: true
---

### 3.1.3 实践任务4：数字逻辑电路的设计与调试

本实践任务要求：

1.  调试并修正一个给定数字逻辑电路设计中的功能错误。
2.  上板后可以实现正确的功能。

请参照第[2.3.1](https://bookdown.org/loongson/_book3/chapter-fpga-board.html#subsec-how-to-get-ede)节中介绍的方式获取本次实践任务所需的实验开发环境。具体的实验环境位于 dc\_env/exp4/ 目录下，其目录结构如下：

```text
|--show_sw.v             数字电路设计的源码文件。
|--show_sw.xdc           数字电路设计的约束文件，用于综合和实现。
|--tb.v                  数字电路设计的仿真文件，用于仿真。
```

show\_sw.v中的设计共有5个功能错误。该设计的正确功能是：

1.  获取开发板最右侧4个拨码开关的状态（记为“拨上为1，拨下为0”，实际开发板上拨码开关的电平是“拨上为低电平，拨下为高电平”），共有16个状态（数字编号是0~15）。
2.  最左侧数码管实时显示4个拨码开关的状态。数码管只支持显示0~9，如果拨码开关状态是10~15，则数码管的显示状态不更改（显示上一次的显示值）。
3.  最右侧的4个单色LED灯会显示上一次的拨码开关的状态，支持显示0~15（拨码开关拨上，对应LED灯亮）。

比如，初始状态下，4个拨码开关拨下，按复位键，则数码管显示0，LED灯都不亮；拨码开关拨为1，则数码管显示1，LED灯还是都不亮；拨码开关再拨为3，则数码管显示3，LED灯显示1。

提供的设计源码中包含5个bug，其中4个是第[**??**](#subsec-wave-error-debug)节中提到的波形异常的前4种情况：波形为“Z”、波形为“X”、波形停止和越沿采样，另外的1个bug是功能bug。

本任务提供的示例设计的顶层接口如表[3.3](https://bookdown.org/loongson/_book3/chapter-digital-logic.html#tab:debug-example-top-signals)所示。

表 3.3: RAM包封后顶层接口信号列表

| 名称 | 宽度 | 方向 | 描述 |
| --- | --- | --- | --- |
| clk | 1 | input | 时钟信号 |
| resetn | 1 | input | 复位信号 |
| switch | 4 | input | 对应开发板上最右侧4个拨码开关 |
| num\_csn | 8 | output | 数码管的片选信号 |
| num\_a\_g | 7 | output | 数码管的7段信号 |
| led | 4 | output | 对应开发板上最右侧4个单色LED灯 |

请参考下列步骤完成本实践任务：

1.  学习本章第[**??**](#sec-digital-circuit-debug-skills)节和附录[D](https://bookdown.org/loongson/_book3/appendix-vivado-advanced-usage.html#appendix-vivado-advanced-usage)第[D.1](https://bookdown.org/loongson/_book3/appendix-vivado-advanced-usage.html#sec-vivado-generate-ram-ip)节的内容。
2.  使用Vivado新建一个工程。
3.  点击“Add Sources”，选择添加设计源码（design sources），加入show\_sw.v。
4.  点击“Add Sources”，选择添加约束文件（constraints），加入show\_sw.xdc。
5.  点击“Add Sources”，选择添加仿真源码（simulation sources），加入tb.v。
6.  理解示例设计的功能，分析仿真顶层tb.v，并理解仿真的行为。注意，开发板上拨码开关的电平是“拨上为低电平，拨下为高电平”，单色LED灯的电平行为是“高电平不亮，低电平亮”。仿真顶层tb.v也是按此电平设计的。
7.  进行仿真，并充分利用仿真的辅助小技巧（分割、分组、颜色变化、标志等）进行调试，找出所有的bug。
8.  仿真完成后，进行综合、实现并生成比特流文件。
9.  生成比特流文件后，连接开发板，进行上板验证。（如果没有本地或远程FPGA实验平台或者不进行上板实验，可以跳过此步骤。）
