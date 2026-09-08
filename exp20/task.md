---
layout: default
nav_exclude: true
---

### 10.2.1 实践任务20：Cache模块设计（Chisel 版）

> 原书（Verilog 版）同一节说明见《CPU 设计实战：LoongArch 版》。

#### 本实践任务要求

1. 设计 Cache 模块（模块名 `cache`，接口见原书 10.1 节表 10.2/10.3）。
   **设计规格：2 路组相联、每路 4KB、LRU 或伪随机替换算法、推荐硬件初始化。**
2. 利用 Cache 模块级验证环境对所设计的 Cache 进行验证，通过仿真和上板验证。

#### 参考步骤

1. 实现 `Cache` 的五部分：① 存储体；② 命中判断与读通路；③ miss 时的 `rd_req` 填充；
   ④ 写通路与脏行写回；⑤ `addr_ok`/`data_ok` 握手。
2. 验证环境的检查要点（原书 10.2.1.1）：
   - 写请求会先 miss，Cache 发 `rd` 请求，验证环境返回全 1（`0xFFFFFFFF`）；
   - 写请求可能引发替换，验证环境用 `wr_addr`/`wr_data` 与之前的 tag/data 组合比对；
   - 写完后做读请求，验证环境检查读回结果与写入结果是否相同。
3. 运行测试：
   ```bash
   cd chisel
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
