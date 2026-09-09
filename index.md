---
layout: default
nav_order: 0
---

# 计算机组成原理实践任务（Chisel 版）

《CPU 设计实战：LoongArch 版》用 Verilog 给出了从跑马灯到带 Cache、TLB 的完整 CPU 设计流程。
本站点在**保留原实验意图与验收标准**的前提下，把其中 23 个实践任务逐一对等地改写为 Chisel 版本，
并按「一个任务一页」的方式组织，便于按序学习与自查。

> 每个实践任务都提供独立资料包（代码 + 说明 + 该实验的仿真运行件），可在
> [Releases](https://github.com/Tetrachlorosilane/cod-WHU/releases/latest) 下载，解压后即可编译与仿真。

## 怎样使用本站

1. 先读[工作流指南](工作流指南.md)，把工具链装好（JDK 17 + Mill 1.0.4），并跑通第一个实验；
2. 按顺序做实践任务：前四个是 Verilog 基础实验，实践任务5起进入 Chisel 版；
3. 每个任务页给出四样东西——**实验目标**（要做什么）、**提示与易错点**（动手前先看这一节）、
   **关键代码**（接口与环境）、**待操作代码**（你需要补全的位置）；
4. 做完后用「实验验收」里的判据自检：能复现原实验的验收条件，才算真正完成。

## 学习建议

- 每个任务都保留了原实验的教学意图：**实践任务5是填空、实践任务6是找错、实践任务7起从零实现**。
  三者的难点不同——填空考接口理解，找错考调试方法，从零实现考结构设计，不要用同一种方式对付。
- Chisel 是「用 Scala 描述电路」的语言，写的是**硬件结构**而不是程序流程。看到 `when`、`RegNext`、
  `Wire` 时，请先想清楚对应的硬件是什么，再动手写。
- 建议每完成一级流水线就在波形上看一眼：**信号名对不对、时序差几拍**，比反复读代码更有效。

## 贡献与致谢

| 贡献 | 说明 |
|---|---|
| [Verilog to Chisel（黄治豪）](https://zihaojf.github.io/Chisel-/chisel/%E7%AE%80%E4%BB%8B/) | Chisel 入门与五级流水线写法参考 |
| [CPU 设计实战：LoongArch 版](https://bookdown.org/loongson/_book3/)（汪文祥、邢金璋 等著） | 实践任务与原始 Verilog 实验工程来源 |

## 实践任务清单

{% for item in site.data.nav.exps %}
{{ forloop.index }}. [{{ item.label }}]({{ item.url | relative_url }}) — {{ item.tag }}
{% endfor %}

## 目录与约定

每个 `expN/` 子目录包含：

- `index.md`：任务页（实验目标 / 提示与易错点 / 关键代码 / 待操作代码 / 实验验收 / 参考）；
- `task.md`：该任务的原说明（已改写为 Chisel 版，保留原书要求与参考步骤）；
- `code/`：原 Verilog 实验环境的文本源码（对照基线；不含生成物与 Vivado 工程二进制）；
- `chisel/`：Chisel 版环境（学生骨架 + SoC + 测试 + 环境说明，实践任务5起）。

> 收录策略与排除清单见 [README.md](README.md) 与 [.gitignore](.gitignore)；
> 页面上内嵌的代码片段由生成脚本从源文件重新生成，保证与仓库中的代码一致。

## 许可与来源

- 实践任务与原始 Verilog 代码来源：《CPU 设计实战：LoongArch 版》（汪文祥、邢金璋 等著）配套实验工程。
- Chisel 改写版：`exp5~exp23` 的 `chisel/`（学生骨架 + 环境 + 测试）。
- Chisel 写法参考：[Verilog to Chisel（黄治豪）](https://zihaojf.github.io/Chisel-/chisel/%E7%AE%80%E4%BB%8B/)。
