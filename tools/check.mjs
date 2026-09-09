// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// site_check.mjs —— cod-WHU.github.io 站点校验（纯静态，无需编译）
//
// 用法（仓库根目录）：node tools/site_check.mjs
//
// 检查项：
//   1. 23 个实践任务子目录齐备，各含 index.md；
//   2. code/ 目录存在且可枚举（本站收录的原 Verilog 源码子集）；
//   3. chisel/ 目录存在（exp5~exp23 应有，exp1~4 不应有）；
//   4. index.md 内**全部相对链接可达**（文件/目录存在；#Lxx 锚点忽略）；
//   5. 代码块开围栏一律带语言标签；
//   6. 页面声明的「待操作代码 N 处」与实际渲染出的待操作小节数一致；
//   7. 页面必备小节齐备（实验目标 / 关键代码 / 待操作代码 / 实验验收 / 参考）；
//   8. 首页列出 23 个任务且链接可达。
// ============================================================================

import { readFileSync, existsSync, readdirSync, statSync } from 'node:fs';
import { join, dirname, resolve, relative } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const SITE = ROOT;

const problems = [];
const stats = { tasks: 0, links: 0, fences: 0, todos: 0 };

function fail(where, msg) { problems.push(`${where}: ${msg}`); }

/** 判断 junction/目录是否存在且可读（读一个条目即可） */
function resolvable(p) {
  if (!existsSync(p)) return false;
  try { statSync(p); readdirSync(p); return true; } catch { return false; }
}

// 判断文件是否会被 Jekyll 渲染（带 YAML front matter 的 markdown）
function isRenderedPage(file) {
  if (!/\.md$/i.test(file)) return false;
  try { return /^---\r?\n/.test(readFileSync(file, 'utf8')); } catch { return false; }
}

// 内部链接检查：目标存在 + 指向被渲染页面时必须用 .html / 目录 URL
function checkInternalLinks(baseDir, text, where, stats) {
  const links = [...text.matchAll(/\]\(([^)\s]+)\)/g)].map((m) => m[1]);
  for (const raw of links) {
    if (/^(https?:|mailto:|#)/.test(raw)) continue;
    const clean = raw.split('#')[0];
    if (!clean) continue;
    stats.links += 1;
    let target = resolve(baseDir, clean);
    // .html 链接：允许对应 .md 存在（Jekyll 会渲染成 .html）
    if (!existsSync(target) && /.html$/i.test(clean)) {
      const md = target.replace(/.html$/i, '.md');
      if (existsSync(md)) target = md; else { fail(where, `链接不可达：${raw}`); continue; }
    }
    if (!existsSync(target)) { fail(where, `链接不可达：${raw}`); continue; }
    // 目录链接：需要 index.md（会渲染成 index.html）
    let st = null;
    try { st = statSync(target); } catch { /* ignore */ }
    if (st && st.isDirectory()) {
      if (!existsSync(join(target, 'index.md')) && !existsSync(join(target, 'index.html'))) {
        fail(where, `目录链接没有 index：${raw}`);
      }
      continue;
    }
    // 指向 .md 但该文件会被渲染 → 线上地址是 .html / 目录
    if (/\.md$/i.test(clean) && isRenderedPage(target)) {
      const hint = /(^|\/)index\.md$/i.test(clean) ? clean.replace(/index\.md$/i, '') : clean.replace(/\.md$/i, '.html');
      fail(where, `链接指向 .md，但该页会被渲染：${raw} → 应写 ${hint}`);
    }
  }
}

function checkTask(n) {
  const dir = join(SITE, `exp${n}`);
  const page = join(dir, 'index.md');
  const where = `exp${n}`;

  if (!existsSync(dir)) { fail(where, '缺少子目录'); return; }
  if (!existsSync(page)) { fail(where, '缺少 index.md'); return; }
  stats.tasks += 1;

  const text = readFileSync(page, 'utf8');

  // front matter
  if (!/^---\n[\s\S]*?\n---\n/.test(text)) fail(where, 'index.md 缺 YAML front matter');

  // 必备小节
  for (const sec of ['## 实验目标', '## 关键代码', '## 待操作代码', '## 实验验收', '## 参考']) {
    if (!text.includes(sec)) fail(where, `缺小节 ${sec}`);
  }

  // code/ 与 chisel/ 目录
  const codeDir = join(dir, 'code');
  const chiselDir = join(dir, 'chisel');
  if (!resolvable(codeDir)) fail(where, 'code/ 目录缺失或不可枚举');
  else {
    try { readdirSync(codeDir); } catch { fail(where, 'code/ 不可枚举'); }
  }
  const shouldHaveChisel = n >= 5;
  const hasChisel = resolvable(chiselDir);
  if (shouldHaveChisel && !hasChisel) fail(where, 'chisel/ 目录缺失或不可枚举（exp5 起应有）');
  if (!shouldHaveChisel && hasChisel) fail(where, 'exp1~4 不应有 chisel/');

  // 链接可达性（含 .md 渲染检查）
  checkInternalLinks(dirname(page), text, where, stats);

  // 代码块语言标签
  let open = false;
  for (const line of text.split('\n')) {
    const m = line.match(/^```([a-zA-Z0-9_-]*)\s*$/);
    if (!m) continue;
    if (!open) { open = true; stats.fences += 1; if (!m[1]) fail(where, '代码块缺语言标签'); }
    else open = false;
  }
  if (open) fail(where, '代码块围栏未闭合');

  // 待操作代码数量一致性（数量优先取 front matter 的 exp_todos；版式已交给 _includes）
  const declared = text.match(/^exp_todos:\s*(\d+)\s*$/m) || text.match(/待操作代码\*\*：\s*(\d+)\s*处/);
  const rendered = (text.match(/^### 待操作：/gm) || []).length;
  if (declared) {
    const want = Number(declared[1]);
    stats.todos += want;
    if (want !== rendered) fail(where, `待操作代码数量不符：声明 ${want}，渲染 ${rendered}`);
  } else if (rendered > 0) {
    fail(where, `渲染了 ${rendered} 处待操作但未声明数量`);
  }
}

function checkIndex() {
  const page = join(SITE, 'index.md');
  if (!existsSync(page)) { fail('站点根', '缺少 index.md'); return; }
  const text = readFileSync(page, 'utf8');
  // 首页清单若由 Liquid 循环 _data/nav.yml 渲染，则改为校验数据文件
  if (/\{%\s*for item in site\.data\.nav\.exps\s*%\}/.test(text)) {
    const dataFile = join(SITE, '_data', 'nav.yml');
    if (!existsSync(dataFile)) { fail('站点根', '缺少 _data/nav.yml（首页清单数据源）'); return; }
    const data = readFileSync(dataFile, 'utf8');
    for (let n = 1; n <= 23; n++) {
      if (!data.includes(`url: /exp${n}/`)) fail('站点根', `_data/nav.yml 未包含 exp${n}`);
      if (!existsSync(join(SITE, `exp${n}`, 'index.md'))) fail('站点根', `exp${n}/index.md 不存在`);
    }
  } else {
    for (let n = 1; n <= 23; n++) {
      if (!text.includes(`(exp${n}/index.md)`)) fail('站点根', `首页未链接 exp${n}`);
    }
  }
  checkInternalLinks(SITE, text, '站点根', stats);
}

for (let n = 1; n <= 23; n++) checkTask(n);
checkIndex();


console.log('=== 站点校验 ===');
console.log(`任务页 ${stats.tasks}/23 ｜ 相对链接 ${stats.links} ｜ 代码块 ${stats.fences} ｜ 待操作 ${stats.todos}`);
if (problems.length === 0) {
  console.log('全部检查通过 ✅');
  process.exit(0);
} else {
  for (const p of problems) console.log('  ✗ ' + p);
  console.log(`共 ${problems.length} 个问题 ❌`);
  process.exit(1);
}
