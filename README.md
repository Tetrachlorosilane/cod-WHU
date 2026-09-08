# cod-WHU.github.io

计算机组成原理**实践任务站点**（Chisel 版）。页面组成参照
[南京大学《数字逻辑与计算机组成》课程实验](https://nju-projectn.github.io/dlco-lecture-note/index.html)：
**首页给出任务清单，每个实践任务一个独立页面**，页面内含
「实验目标 / 关键代码 / 待操作代码 / 实验验收 / 参考」与上一页·下一页导航。

## 1. 仓库结构

```text
cod-WHU.github.io/
├── index.md                  # 首页：23 个实践任务清单
├── README.md                 # 本文件
├── LICENSE-NOTICE.md         # 来源与许可说明
├── 404.md                    # 404 页面
├── _config.yml               # GitHub Pages / Jekyll 配置
├── .gitignore / .gitattributes
├── CHISEL-CONVENTIONS.md     # Chisel 改写规范（含术语表 / 核验记录 / 自检）
├── tools/
│   ├── preview.mjs           # 本地预览服务（零依赖，渲染 markdown 并带侧边栏）
│   └── check.mjs             # 站点自检（目录 / 链接 / 代码块 / 待操作计数）
├── exp1/
│   ├── index.md              # 实践任务页
│   ├── task.md               # 原任务说明（改写为 Chisel 版）
│   └── code/                 # 原 Verilog 实验环境（文本源码子集）
├── exp5/
│   ├── index.md
│   ├── task.md
│   ├── code/                 # 原 Verilog 实验环境
│   └── chisel/               # Chisel 版环境（学生骨架 + SoC + 测试 + 文档）
└── … exp23/
```

> `exp1 ~ exp4` 为 Verilog 版任务（Chisel 改写自 `exp5` 起），因此没有 `chisel/`。

## 2. 内容收录策略（重要）

本仓库**独立成库**，收录的是"阅读与练习所需的内容"，不含可再生的生成物：

| 收录 | 不收录（见 `.gitignore`） |
|---|---|
| RTL 与测试平台（`.v/.sv/.vh`）、任务说明（`.md`）、`Makefile` | `func/obj/**`（`*.mif` / `*.coe` / `*.s` 等编译产物，单实验可达数十 MB） |
| Chisel 全部源码与文档（`.scala/.mill/README/MAPPING`） | `gettrace/golden_trace.txt`（由 gettrace 工程重新生成） |
| 约束与脚本（`.xdc/.tcl/.sh`）、`func/` 源码（`.c/.h/.s`） | Vivado 工程与二进制（`.xpr/.xci/.dcp/.bit/.rpt/.jou/.log`…） |
| 单文件 ≤ 512 KB 的文本 | 图片/压缩包等二进制 |

- 目的：站点体积可控（全库约 **10 MB** 量级），克隆快、Pages 构建快。
- 需要**完整可运行**的实验环境（含 Vivado 工程与生成物）时，请使用主仓库的
  `taskvscode/expN/`（本仓库的 `expN/code`、`expN/chisel` 即从中按上表筛选而来）。

## 3. 代码如何进入 markdown

标准 markdown 没有 include 指令，因此采用**双轨**（两者都做了）：

| 方式 | 做法 | 效果 |
|---|---|---|
| 可点击链接 | 每段代码前给 `> 源文件：[chisel/src/…/Tlb.scala](chisel/src/…/Tlb.scala)#L125` | 点开即到仓库内的真实源文件（含行号锚点） |
| 硬内嵌（默认） | 关键代码与**逐处待操作代码**直接写进围栏代码块 | 任何 markdown 渲染器都能看，不依赖外链 |
| Jekyll include（可选） | 片段抽到 `_includes/`，页面写 `{% include_relative … %}` | GitHub Pages 渲染时"展开"文件内容；需启用 Jekyll（不要放 `.nojekyll`） |

> 硬内嵌的片段由主仓库的 `node tools/build_site.mjs --copy` 从源文件重新生成，
> 因此与源码保持同步（等价于"软链接展开到 markdown"）。

## 4. 本地预览与自检

```bash
node tools/preview.mjs            # 默认 http://127.0.0.1:8123/ ，带侧边栏的任务文档视图
node tools/preview.mjs 9000       # 指定端口
node tools/check.mjs              # 自检：23 个任务页 / 全部相对链接可达 / 代码块语言标签 / 待操作计数
```

## 5. 发布到 GitHub Pages

仓库名即 `cod-WHU.github.io`（用户/组织站点），因此**根目录即为站点根**：

1. `git init && git add -A && git commit -m "site: 实践任务页面"`
2. `git remote add origin git@github.com:<owner>/cod-WHU.github.io.git`
3. `git push -u origin main`
4. 仓库 **Settings → Pages**：Source 选择 `Deploy from a branch`，分支 `main`、目录 `/`。

- Jekyll 默认处理 `.md`（本仓库 `_config.yml` 使用白名单主题 `jekyll-theme-minimal`）；
  想要更接近参考站点的**侧边栏文档布局**，把 `_config.yml` 里注释掉的
  `remote_theme: just-the-docs/just-the-docs` 方案打开即可。
- 请**不要**添加 `.nojekyll`，否则 `.md` 不会渲染成页面。

## 6. 来源与许可

见 [LICENSE-NOTICE.md](LICENSE-NOTICE.md)。实践任务与原始 Verilog 代码来自
《CPU 设计实战：LoongArch 版》配套实验工程；Chisel 版为本项目的对照改写（静态交付，未编译/未仿真）。
