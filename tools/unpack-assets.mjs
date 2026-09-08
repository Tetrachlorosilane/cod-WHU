// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// unpack-assets.mjs —— 解包"跑 Chisel 仿真所需的运行件"（零依赖）
//
// 用法（仓库根目录）：
//     node tools/unpack-assets.mjs            # 解包到仓库根（路径已对齐 expN/code/...）
//     node tools/unpack-assets.mjs --check    # 只校验，不写文件
//     node tools/unpack-assets.mjs --dest D   # 解包到指定目录
//
// 行为：
//   1. 读取 assets/sim-assets.manifest.json（文件清单 + sha256）；
//   2. 解包 assets/sim-assets.tar.gz；
//   3. 逐个文件校验 sha256 与体积，输出统计；
//   4. 解包后的文件落在 expN/code/func/obj/ 与 expN/code/gettrace/，
//      这些路径已被 .gitignore 忽略（不会污染仓库）。
// ============================================================================

import { readFileSync, existsSync, mkdirSync, writeFileSync } from 'node:fs';
import { join, dirname, resolve } from 'node:path';
import { createHash } from 'node:crypto';
import { fileURLToPath } from 'node:url';
import { readTarGz } from './tarlib.mjs';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const args = process.argv.slice(2);
const CHECK_ONLY = args.includes('--check');
const destIdx = args.indexOf('--dest');
const DEST = destIdx >= 0 && args[destIdx + 1] ? resolve(args[destIdx + 1]) : ROOT;

const bundle = join(ROOT, 'assets', 'sim-assets.tar.gz');
const manifestPath = join(ROOT, 'assets', 'sim-assets.manifest.json');

if (!existsSync(bundle) || !existsSync(manifestPath)) {
  console.error(`找不到 ${bundle} 或 ${manifestPath}`);
  console.error('请在主仓库执行 node tools/pack_sim_assets.mjs 生成，或从 Release 下载。');
  process.exit(1);
}

const manifest = JSON.parse(readFileSync(manifestPath, 'utf8'));
const entries = readTarGz(bundle);

let ok = 0, bad = 0, bytes = 0;
const byName = new Map(entries.map(e => [e.name, e]));

for (const f of manifest.files) {
  const e = byName.get(f.name);
  if (!e) { console.error(`✗ 缺文件：${f.name}`); bad++; continue; }
  const sha = createHash('sha256').update(e.data).digest('hex');
  if (sha !== f.sha256 || e.data.length !== f.bytes) {
    console.error(`✗ 校验失败：${f.name}（期望 ${f.bytes}B/${f.sha256.slice(0, 12)}…，实际 ${e.data.length}B/${sha.slice(0, 12)}…）`);
    bad++;
    continue;
  }
  ok++;
  bytes += e.data.length;
  if (!CHECK_ONLY) {
    const target = join(DEST, f.name);
    mkdirSync(dirname(target), { recursive: true });
    writeFileSync(target, e.data);
  }
}

console.log(`运行件${CHECK_ONLY ? '校验' : '解包'}：${ok}/${manifest.files.length} 个，${(bytes / 1048576).toFixed(1)} MB` +
            `${CHECK_ONLY ? '（未写入文件）' : ` → ${DEST}`}`);
if (bad) { console.error(`失败 ${bad} 个`); process.exit(1); }
console.log('现在可以在各实验目录运行：cd expN/chisel && ./mill chisel.test（需 JDK 17 + Mill）');
