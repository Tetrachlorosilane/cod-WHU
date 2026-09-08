---
layout: default
nav_exclude: true
---

# exp20 Chisel 版实验环境（实践任务20：Cache 模块设计）

## 本实验速览（TL;DR）

- **实验目标**：Cache 模块设计（模块级）（原书实践任务20）。
- **Chisel 交付物**：本目录 `chisel/`（学生模块 + 本实验 SoC 变体 + 测试 + 文档）。

- **判据复现**：CacheSpec：index 0→0xff 先写后读 → `----PASS!!!`。
- **共享依赖**：不依赖共享库（模块级独立环境）。

## 1. 本实验要求（原书 10.2.1）

1. 设计 **Cache 模块**（模块名 `cache`，接口见原书 10.1 节表 10.2/10.3）；
   设计规格：**2 路组相联、每路 4KB、LRU 或伪随机替换、建议硬件初始化**；
2. 利用 Cache 模块级验证环境（`cache_top.v` + `testbench.v`）验证，通过仿真和上板验证。

## 2. 文件清单

```text
chisel/
├── src/main/scala/exp20/
│   ├── soc/CacheTop.scala          # 模块级验证环境（对应 cache_top.v，417 行）
│   └── student/Cache.scala         # ★ 学生模块：Cache 骨架（5 处 TODO）
└── src/test/scala/CacheSpec.scala  # 复现"每个 index 先写后读 → ----PASS!!!"
```

**本实验为模块级独立环境**，不使用 `chisel-common`。

## 3. 怎么做这个实验

1. 实现 `Cache` 的五个部分（`TODO(实现 1/5 … 5/5)`）：
   存储体、命中判断与读通路、miss 时的 rd 请求填充、写通路与脏行写回、`addr_ok`/`data_ok` 握手。
2. 运行测试：
   ```bash
   cd chisel
   ./mill chisel.test
   ```
3. 上板流程（Vivado）沿用原书步骤，需 `../code/run_vivado/` 下的原工程
   （上板时数码管左边两位显示当前 index，到 0xff 停止，见原书 10.2.1.2）。

## 4. 判据（与原实验一一对应）

| 原实验 | Chisel 版 |
|---|---|
| 每个 index 完成时打印 `index %x finished` | `CacheSpec` 打印同样进度 |
| `test_index == 8'hff` 时打印 `----PASS!!!` | 断言 `testIndex == 0xff` 且 `roundFinish`，打印 `----PASS!!!` |
| `replace_wrong` → `----FAIL!!!` | 断言 `replaceWrong == 0` |
| `cacheres_wrong` → `----FAIL!!!` | 断言 `cacheresWrong == 0` |

## 5. 与原实验的结构性差异

1. `clk_pll` 不建模（用隐式 clock）。
2. `wait_cnt` 的重载值由参数 `simulation` 控制（5 / 800_000），与原 `SIMULATION 一致。
3. 为便于测试平台观测，`CacheTop` 增加了 4 个只读输出
   （`testIndex/roundFinish/replaceWrong/cacheresWrong`），不影响原有行为。
