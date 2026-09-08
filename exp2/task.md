### 3.1.1 实践任务2：寄存器堆仿真

本实践任务要求：

1.  对一个寄存器堆设计进行功能仿真，通过观察其仿真波形了解行为特征。

实验环境中提供的寄存器堆源码为“两读一写”的结构，也就是有两个读端口（读端口没有使能位控制，表示永远使能）和一个写端口。接口信号如表[3.1](https://bookdown.org/loongson/_book3/chapter-digital-logic.html#tab:regfile-interface-signals)所示。

表 3.1: 寄存器堆接口信号列表

| 名称 | 宽度 | 方向 | 描述 |
| --- | --- | --- | --- |
| clk | 1 | input | 时钟信号 |
| raddr1 | 5 | input | 寄存器堆读地址1 |
| rdata1 | 32 | output | 寄存器堆读返回数据1 |
| raddr2 | 5 | input | 寄存器堆读地址2 |
| rdata2 | 32 | output | 寄存器堆读返回数据2 |
| we | 1 | input | 寄存器堆写使能 |
| waddr | 5 | input | 寄存器堆写地址 |
| wdata | 32 | input | 寄存器堆写数据 |

请参照第[2.3.1](https://bookdown.org/loongson/_book3/chapter-fpga-board.html#subsec-how-to-get-ede)节中介绍的方式获取本次实践任务所需的实验开发环境。具体的实验环境位于 dc\_env/exp2/ 目录下，其目录结构如下：

```text
|--regfile.v             寄存器堆源码文件。
|--rf_tb.v               寄存器堆仿真文件。
```

建议参考下列步骤完成本实践任务：

1.  使用Vivado新建一个工程。
2.  点击“Add Sources”，选择添加设计源码（design sources），加入regfile.v。
3.  点击“Add Sources”，选择添加仿真源码（simulation sources），加入rf\_tb.v。
4.  对工程进行仿真测试，结合波形观察寄存器堆的读写行为。
