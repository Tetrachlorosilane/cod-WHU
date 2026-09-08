# 来源与许可说明

本仓库是**实践任务站点**，内容来源如下：

| 内容 | 来源 | 说明 |
|---|---|---|
| 实践任务说明（`expN/task.md`） | 《CPU 设计实战：LoongArch 版》配套实验 | 就地改写为 Chisel 版；原书说明见 https://bookdown.org/loongson/_book3/ |
| 原始 Verilog 实验环境（`expN/code/`） | 原书配套实验工程 | 仅收录**文本源码**；生成的存储器文件（`func/obj/*.mif/.coe`）与 Vivado 工程二进制未收录（见 `.gitignore`） |
| Chisel 版环境（`expN/chisel/`） | 本项目的 Verilog→Chisel 改写 | 静态交付：未编译、未仿真；逐行对照见各 `chisel/MAPPING.md` |
| 页面排版参考 | [NJU dlco-lecture-note](https://nju-projectn.github.io/dlco-lecture-note/index.html) | 仅参照页面组成（首页清单 + 每任务独立页），未复制其内容 |

> 原始教材与实验代码的著作权归原作者所有；本仓库用于教学与学习目的。
> 如需完整可运行的实验环境（含 Vivado 工程与生成物），请使用主仓库 `taskvscode/` 下的完整副本。
