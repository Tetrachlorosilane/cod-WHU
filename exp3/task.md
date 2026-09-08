---
layout: default
nav_exclude: true
---

### 3.1.2 实践任务3：同步RAM和异步RAM仿真、综合与实现

本实践任务要求：

1.  调用Xilinx库IP实例化一个同步RAM，进行仿真以观察行为，进行综合和实现后查看时序结果和资源利用率。
2.  调用Xilinx库IP实例化一个异步RAM，进行仿真以观察行为，进行综合和实现后查看时序结果和资源利用率。
3.  对观察到的现象进行对比分析。

请参照第[2.3.1](https://bookdown.org/loongson/_book3/chapter-fpga-board.html#subsec-how-to-get-ede)节中介绍的方式获取本次实践任务所需的实验开发环境。具体的实验环境位于 dc\_env/exp3/ 目录下，其目录结构如下：

```text
|--block_ram_top.v       同步RAM（Block RAM）的源码顶层文件。
|--distributed_ram_top.v 异步RAM（Distributed RAM）的源码顶层文件。
|--ram.xdc               两种RAM的仿真约束文件，用于综合和实现。
|--ram_tb.v              两种RAM的仿真文件，用于仿真。
```

实验环境提供的设计顶层文件用于将两种类型的RAM封装成相同的模块名和接口。封装后的RAM顶层接口信号如表[3.2](https://bookdown.org/loongson/_book3/chapter-digital-logic.html#tab:ram-interface-signals)所示。

表 3.2: RAM包封后顶层接口信号列表

| 名称 | 宽度 | 方向 | 描述 |
| --- | --- | --- | --- |
| clk | 1 | Input | 时钟信号 |
| ram\_wen | 1 | Input | RAM的写使能信号：为1表示写入操作，为0表示读取操作 |
| ram\_addr | 16 | Input | RAM的地址信号，读和写的地址都由该信号指示 |
| ram\_wdata | 32 | Input | RAM写入的数据 |
| ram\_rdata | 32 | Output | RAM读出的数据 |

注意：包封后的RAM接口没有片选信号，即片选使能始终有效（其内部实例化的具体RAM的片选使能信号恒为1）。

建立同步RAM工程的参考步骤如下：

1.  使用Vivado新建一个工程。
2.  点击“Add Sources”，选择添加设计源码（design sources），加入block\_ram\_top.v。
3.  点击“Add Sources”，选择添加约束文件（constraints），加入ram.xdc。
4.  点击“Add Sources”，选择添加仿真源码（simulation sources），加入ram\_tb.v。
5.  参考第[**??**](#sec-digital-circuit-verilog-coding)节，调用Xilinx库IP生成同步RAM（Block RAM，深度为65536，宽度为32，片选使能信号设为一直有效）。

建立异步RAM工程的参考步骤如下：

1.  使用Vivado新建一个工程。
2.  点击“Add Sources”，选择添加设计源码（design sources），加入distributed\_ram\_top.v。
3.  点击“Add Sources”，选择添加约束文件（constraints），加入ram.xdc。
4.  点击“Add Sources”，选择添加仿真源码（simulation sources），加入ram\_tb.v。
5.  参考附录[D](https://bookdown.org/loongson/_book3/appendix-vivado-advanced-usage.html#appendix-vivado-advanced-usage)第[D.1](https://bookdown.org/loongson/_book3/appendix-vivado-advanced-usage.html#sec-vivado-generate-ram-ip)节，调用Xilinx库IP生成异步RAM（Distributed RAM，深度为65536，宽度为32）。

在完成工程的创建后，对它们进行仿真，对比读写行为的异同。在完成工程的仿真后，对它们进行综合和实现，参考附录[D](https://bookdown.org/loongson/_book3/appendix-vivado-advanced-usage.html#appendix-vivado-advanced-usage)第[D.1](https://bookdown.org/loongson/_book3/appendix-vivado-advanced-usage.html#sec-vivado-generate-ram-ip)节介绍的方法查看时序结果和资源利用率，并结合读写时序进行分析。

在实践过程中，应特别注意以下几点：

-   生成IP时，请将对应IP命名为block\_ram和distributed\_ram，如命名错误，IP将会报错。若遇到已生成IP无法改名的情况，可以删除该IP，重新生成。
-   生成IP时，可以点击窗口左侧的图查看接口信息。当参数正确时，端口名和宽度应与指定的顶层文件中的调用相对应。
-   有兴趣的读者可以自行调研、参考同步/异步RAM定制的资料，并根据仿真波形对比参数的作用。
-   对程序进行综合之前请确保已正确加载约束文件（ram.xdc）。
-   添加testbench时请注意选择add simulation source，否则会导致顶层文件错误，综合结果不正确。
-   对程序进行综合时，所用的计算机不同，综合时间会有一定的差异，有可能会耗费大量时间，所以应提前计划，安排好时间。
-   时序报告和资源报告的生成需要查看综合、实现完成后的结果。
