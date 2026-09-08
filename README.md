# cod-WHU

计算机组成原理**实践任务站点**（Chisel 版）：
**首页给出任务清单，每个实践任务一个独立页面**，页面内含
「实验目标 / 关键代码 / 待操作代码 / 实验验收 / 参考」与上一页·下一页导航。

## 1. 仓库结构

> 仓库：https://github.com/Tetrachlorosilane/cod-WHU （原名 `cod-WHU.github.io`）。
> 本地目录名沿用 `cod-WHU.github.io/`，不影响仓库地址。

```text
cod-WHU.github.io/
├── index.md                  # 首页：23 个实践任务清单
├── README.md                 # 本文件
├── LICENSE-NOTICE.md         # 来源与许可说明
├── 404.md                    # 404 页面
├── _config.yml               # GitHub Pages / Jekyll 配置
├── .gitignore / .gitattributes
├── CHISEL-CONVENTIONS.md     # Chisel 改写规范（含术语表 / 核验记录 / 自检）
├── chisel-common/            # 共享环境库（exp7~exp23 的 chisel/ 通过 ../../chisel-common/ 引用）
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

- 目的：站点体积可控（全库约 **23 MB**），克隆快、Pages 构建快。

### 2.1 对"能不能跑仿真"的影响（重要）

| 流程 | 是否受影响 | 说明 |
|---|---|---|
| 浏览本站页面 | ❌ 不受影响 | 代码片段已内嵌；链接指向站内真实副本 |
| Chisel 仿真 **exp5 / exp17 / exp20** | ❌ 不受影响 | exp5 用内嵌常量表（`InstRamProgram`）；exp17/exp20 是模块级环境，判据只用激励 |
| Chisel 仿真 **exp6~exp16、exp18/19、exp21~23** | ✅ **受影响** | 测试会读 `code/func/obj/inst_ram.mif`、`code/func/obj/data_ram.mif`、`code/gettrace/golden_trace.txt`；缺失时 `MifLoader`/`TraceLoader` 的 `require(Files.exists(...))` 直接报"找不到 .mif / golden_trace.txt" |
| 原 Verilog 仿真 / Vivado 综合上板 | ✅ **受影响** | `create_project.tcl` 执行 `add_files [glob ../rtl/xilinx_ip/*/*.xci]`（本站 `.xci` 数量为 0）；`clk_pll` 等 IP 也缺失 |

缺失的运行件：每实验 5 个文件（`func/obj/{inst_ram,data_ram}.{mif,coe}` + `gettrace/golden_trace.txt`），
共 **80 个文件 107 MB**。本仓库已把它打包为 **`assets/sim-assets.tar.gz`（13.6 MB）**：

```bash
node tools/unpack-assets.mjs           # 解包到仓库根（路径已对齐 expN/code/...）
node tools/unpack-assets.mjs --check   # 只校验 sha256，不写文件
node tools/unpack-assets.mjs --dest D  # 解包到指定目录
```

- 解包后文件落在 `expN/code/func/obj/` 与 `expN/code/gettrace/`，这些路径已在 `.gitignore` 中，
  **不会污染仓库**（`git status` 保持干净）。
- 清单与哈希见 `assets/sim-assets.manifest.json`（80 个文件的 sha256 + 体积）。
- 另外两种获取方式：① 从主仓库 `taskvscode/expN/` 直接拷贝；
  ② 用 LoongArch 工具链在 `func/` 下 `make` 重新生成 `.mif/.coe`，
  并用 gettrace 工程重新生成 `golden_trace.txt`。

> 需要**完整可运行**的实验环境（含 Vivado 工程与生成物）时，请使用主仓库
> `taskvscode/expN/`（本仓库的 `expN/code`、`expN/chisel` 即从中按上表筛选而来）。

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

本仓库地址：**https://github.com/Tetrachlorosilane/cod-WHU**（原名 `cod-WHU.github.io`，已改名）。
由于仓库名不是 `<用户名>.github.io`，它发布为**项目站点**，访问地址为：

    https://tetrachlorosilane.github.io/cod-WHU/

启用步骤：

1. 仓库 **Settings → Pages**；
2. Source 选择 **Deploy from a branch**；
3. Branch 选 **`main`**、目录 **`/ (root)`** → Save；
4. 等 Actions 里的 `pages build and deployment` 跑完即可访问上面的地址。

> 若要改用根域名 `https://tetrachlorosilane.github.io/`，需要把仓库改名为
> `Tetrachlorosilane.github.io`（或把内容迁到该仓库）。

- `_config.yml` 使用 `remote_theme: just-the-docs/just-the-docs`（侧边栏文档布局，
  最接近参考站点的观感）；侧边栏抬头取自 `title`，侧边导航取自各 `.md` 的 front matter。
- 请**不要**添加 `.nojekyll`，否则 `.md` 不会渲染成页面。

## 6. 来源与许可

见 [LICENSE-NOTICE.md](LICENSE-NOTICE.md)。实践任务与原始 Verilog 代码来自
《CPU 设计实战：LoongArch 版》配套实验工程；Chisel 版为本项目的对照改写（静态交付，未编译/未仿真）。
