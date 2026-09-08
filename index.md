---
layout: default
title: 首页
nav_order: 0
---

# 计算机组成原理实践任务4Chisel

> 站点结构参照 [南京大学《数字逻辑与计算机组成》课程实验](https://nju-projectn.github.io/dlco-lecture-note/index.html)
> 的页面组成：**首页（任务清单）+ 每个任务独立页面**；每个任务页含目标 / 内容 / 关键代码 / 待操作代码 / 验收 / 参考。

## 实践任务清单

1. [实践任务1 跑马灯](exp1/index.md) — Verilog
2. [实践任务2 寄存器堆仿真](exp2/index.md) — Verilog
3. [实践任务3 同步 RAM 和异步 RAM 仿真、综合与实现](exp3/index.md) — Verilog
4. [实践任务4 数字逻辑电路的设计与调试](exp4/index.md) — Verilog
5. [实践任务5 5 条指令单周期 CPU](exp5/index.md) — Chisel · 填空
6. [实践任务6 20 条指令单周期 CPU](exp6/index.md) — Chisel · 找错
7. [实践任务7 不考虑相关冲突处理的简单流水线 CPU](exp7/index.md) — Chisel · 从零实现
8. [实践任务8 阻塞技术解决相关引发的冲突](exp8/index.md) — Chisel · 从零实现
9. [实践任务9 前递技术解决相关引发的冲突](exp9/index.md) — Chisel · 从零实现
10. [实践任务10 算术逻辑运算指令和乘除法运算指令添加](exp10/index.md) — Chisel · 从零实现
11. [实践任务11 转移指令和访存指令添加](exp11/index.md) — Chisel · 从零实现
12. [实践任务12 添加系统调用异常支持](exp12/index.md) — Chisel · 从零实现
13. [实践任务13 添加其它异常支持](exp13/index.md) — Chisel · 从零实现
14. [实践任务14 添加类 SRAM 总线支持](exp14/index.md) — Chisel · 从零实现
15. [实践任务15 添加 AXI 总线支持](exp15/index.md) — Chisel · 从零实现
16. [实践任务16 完成 AXI 随机延迟验证](exp16/index.md) — Chisel · 从零实现
17. [实践任务17 TLB 模块设计](exp17/index.md) — Chisel · 从零实现（模块级）
18. [实践任务18 添加 TLB 相关指令和 CSR 寄存器](exp18/index.md) — Chisel · 从零实现
19. [实践任务19 添加 TLB 相关例外支持](exp19/index.md) — Chisel · 从零实现
20. [实践任务20 Cache 模块设计](exp20/index.md) — Chisel · 从零实现（模块级）
21. [实践任务21 在 CPU 中集成 ICache](exp21/index.md) — Chisel · 从零实现
22. [实践任务22 CPU 中集成 DCache](exp22/index.md) — Chisel · 从零实现
23. [实践任务23 CPU 中添加 CACOP 指令](exp23/index.md) — Chisel · 从零实现

## 目录约定

每个 `expN/` 子目录包含：

- `index.md`：实践任务页（实验目标 / 关键代码 / 待操作代码 / 实验验收 / 参考）；
- `task.md`：原任务说明（已改写为 Chisel 版）；
- `code/`：本站收录的**原 Verilog 源码子集**（RTL/测试/约束/func 源码；不含生成物与 Vivado 工程）；
- `chisel/`：本站收录的 **Chisel 版环境**（学生骨架 + SoC + 测试 + 文档，exp5 起）。

> 收录策略与排除清单见 [README.md](README.md) §2 与 [.gitignore](.gitignore)；
> markdown 中内嵌的代码片段由生成脚本从源文件重新生成（见 [README.md](README.md) §3）。

## 许可与来源

- 实践任务与原始 Verilog 代码来源：《CPU 设计实战：LoongArch 版》配套实验工程。
- Chisel 改写版：`exp5~exp23` 的 `chisel/`，改写规范见 [CHISEL-CONVENTIONS.md](CHISEL-CONVENTIONS.md)。
- 站点排版参考：南京大学课程实验站点（Sphinx + Read the Docs 主题）。
