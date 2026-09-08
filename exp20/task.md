### 10.2.1 实践任务20：Cache模块设计（Chisel 版）

> **本说明已改写为 Chisel 版。** 原书（Verilog 版）的同一节说明保留在
> [`../../CPU设计实战：LoongArch版/10.2.1实践任务20-Cache模块设计.md`](../../CPU设计实战：LoongArch版/10.2.1实践任务20-Cache模块设计.md)，
> 原 Verilog 代码保留在同级 `code/` 目录，作为对照基线。

#### 本实践任务要求（教学意图：从零实现）

1. 设计 Cache 模块（模块名 `cache`，接口见原书 10.1 节表 10.2/10.3）。
   **设计规格：2 路组相联、每路 4KB、LRU 或伪随机替换算法、推荐硬件初始化。**
2. 利用 Cache 模块级验证环境对所设计的 Cache 进行验证，通过仿真和上板验证。

**Chisel 版保留的教学意图 = 从零实现**：原实验环境只提供验证环境（`cache_top.v` +
`testbench.v`），**不提供 Cache 本体**；Chisel 版同样只给接口骨架：
`chisel/src/main/scala/exp20/student/Cache.scala` 中有 **5 处 `TODO(实现 n/5)`**，
内部逻辑全部留空（`???`）；未实现时 elaboration 以 `NotImplementedError` 终止。

#### 实验环境（Chisel 版目录结构）

```text
taskvscode/exp20/
├── 10.2.1实践任务20-Cache模块设计.md   # 本文件
├── code/                                # 原 Verilog 实验环境（对照基线，未改动）
└── chisel/
    ├── README.md / MAPPING.md / build.mill
    ├── src/main/scala/exp20/
    │   ├── soc/CacheTop.scala          # 模块级验证环境（对应 cache_top.v）
    │   └── student/Cache.scala         # ★ 学生模块：Cache 骨架（5 处 TODO）
    └── src/test/scala/CacheSpec.scala  # 复现"每个 index 先写后读 → ----PASS!!!"
```

**本实验是模块级独立验证环境**（不涉及 CPU/SoC），因此不使用 `chisel-common`。

#### 参考步骤

1. 实现 `Cache` 的五部分：① 存储体；② 命中判断与读通路；③ miss 时的 `rd_req` 填充；
   ④ 写通路与脏行写回；⑤ `addr_ok`/`data_ok` 握手。
2. 验证环境的检查要点（原书 10.2.1.1）：
   - 写请求会先 miss，Cache 发 `rd` 请求，验证环境返回全 1（`0xFFFFFFFF`）；
   - 写请求可能引发替换，验证环境用 `wr_addr`/`wr_data` 与之前的 tag/data 组合比对；
   - 写完后做读请求，验证环境检查读回结果与写入结果是否相同。
3. 运行测试：
   ```bash
   cd taskvscode/exp20/chisel
   ./mill chisel.test
   ```
4. 上板验证（Vivado）沿用原书流程，需 `code/run_vivado/` 下的原工程；
   正确现象见原书 10.2.1.2（数码管左边两位显示 index，到 0xff 停止）。

#### 结果判断

| 原实验判据 | Chisel 版判据 |
|---|---|
| 每个 index 完成打印 `index %x finished` | `CacheSpec` 打印同样进度 |
| `test_index == 8'hff` 时打印 `----PASS!!!` | 断言 `testIndex == 0xff` 且 `roundFinish`，打印 `----PASS!!!` |
| `replace_wrong` → `----FAIL!!!` | 断言 `replaceWrong == 0` |
| `cacheres_wrong` → `----FAIL!!!` | 断言 `cacheresWrong == 0` |

> ⚠️ 未实测声明：`chisel/` 下的代码未在 JDK/Mill/Chisel 环境中编译或仿真过（本次为静态交付）。
> 逐行对照见 `chisel/MAPPING.md`；如实际运行报错，请以 `MAPPING.md` 与 `code/` 下的原 Verilog 为准排查。
