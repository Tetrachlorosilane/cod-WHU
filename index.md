---
layout: default
nav_order: 0
---

# 计算机组成原理实践任务4Chisel

> 本站点把《CPU 设计实战：LoongArch 版》的 23 个实践任务整理为独立页面：
> **首页（任务清单）+ 每个任务独立页面**；每个任务页含目标 / 内容 / 关键代码 / 待操作代码 / 验收 / 参考。

## 贡献与致谢

| 贡献 | 说明 |
|---|---|
| [Verilog to Chisel（黄治豪）](https://zihaojf.github.io/Chisel-/chisel/%E7%AE%80%E4%BB%8B/) | Chisel 入门与五级流水线写法参考 |
| [CPU 设计实战：LoongArch 版](https://bookdown.org/loongson/_book3/)（汪文祥、邢金璋 等著） | 实践任务与原始 Verilog 实验工程来源 |

## 导航

- 实践任务：见下方清单（每个任务一页）
- [Chisel 入门教程（黄治豪）](https://zihaojf.github.io/Chisel-/chisel/%E7%AE%80%E4%BB%8B/) ｜ [工作流指南](工作流指南.md)
- [附录](附录/index.md) ｜ [来源与许可](附录/来源与许可.md) ｜ [子页索引](附录/子页索引.md)

## 实践任务清单

{% for item in site.data.nav.exps %}
{{ forloop.index }}. [{{ item.label }}]({{ item.url | relative_url }}) — {{ item.tag }}
{% endfor %}

## 目录约定

每个 `expN/` 子目录包含：

- `index.md`：实践任务页（实验目标 / 关键代码 / 待操作代码 / 实验验收 / 参考）；
- `task.md`：原任务说明（已改写为 Chisel 版）；
- `code/`：本站收录的**原 Verilog 源码子集**（RTL/测试/约束/func 源码；不含生成物与 Vivado 工程）；
- `chisel/`：本站收录的 **Chisel 版环境**（学生骨架 + SoC + 测试 + 文档，exp5 起）。

> 收录策略与排除清单见 [README.md](README.md) §2 与 [.gitignore](.gitignore)；
> markdown 中内嵌的代码片段由生成脚本从源文件重新生成（见 [README.md](README.md) §3）。

## 许可与来源

- 实践任务与原始 Verilog 代码来源：《CPU 设计实战：LoongArch 版》（汪文祥、邢金璋 等著）配套实验工程。
- Chisel 改写版：`exp5~exp23` 的 `chisel/`（学生骨架 + 环境 + 测试）。
- Chisel 写法参考：[Verilog to Chisel（黄治豪）](https://zihaojf.github.io/Chisel-/chisel/%E7%AE%80%E4%BB%8B/)。
