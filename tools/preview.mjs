// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// preview_site.mjs —— cod-WHU.github.io 本地预览服务（无第三方依赖）
//
// 用法：node tools/preview_site.mjs [port]      # 默认 8123
//      浏览器打开 http://127.0.0.1:8123/
//
// 作用：
//   1. 把站点里的 .md 渲染成 HTML，页面组成**对标**参考站点
//      （左侧任务导航 + 右侧内容 + 底部上一页/下一页）；
//   2. 非 .md 文件（含 junction 后的 .scala/.v/任务说明）按原样静态服务，
//      用于验证"软链接展开到 markdown"里的链接确实可达；
//   3. 极简 markdown 渲染器（标题/列表/表格/代码块/引用/链接/行内代码），
//      不引入任何依赖，保证离线可跑。
// ============================================================================

import { createServer } from 'node:http';
import { fileURLToPath } from 'node:url';
import { readFileSync, existsSync, statSync, readdirSync } from 'node:fs';
import { join, extname, resolve, dirname } from 'node:path';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const SITE = ROOT;
const PORT = Number(process.argv[2] || 8123);

const MIME = {
  '.html': 'text/html; charset=utf-8', '.md': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8', '.js': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8', '.yml': 'text/yaml; charset=utf-8',
  '.scala': 'text/plain; charset=utf-8', '.v': 'text/plain; charset=utf-8',
  '.sv': 'text/plain; charset=utf-8', '.mif': 'text/plain; charset=utf-8',
  '.coe': 'text/plain; charset=utf-8', '.txt': 'text/plain; charset=utf-8',
  '.png': 'image/png', '.jpg': 'image/jpeg', '.pdf': 'application/pdf',
};

const esc = s => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

// ------------------------------ 极简 markdown → HTML ------------------------------
function renderMarkdown(md) {
  const lines = md.replace(/\r\n/g, '\n').split('\n');
  let html = '', i = 0, inCode = false, codeBuf = [], codeLang = '', list = null, table = [];

  const flushList = () => { if (list) { html += `</${list}>\n`; list = null; } };
  const flushTable = () => {
    if (!table.length) return;
    const rows = table.filter(r => !/^\s*\|?[\s:-]+\|/.test(r));
    html += '<table>\n';
    rows.forEach((r, k) => {
      const cells = r.replace(/^\s*\|/, '').replace(/\|\s*$/, '').split('|').map(c => inline(c.trim()));
      html += '<tr>' + cells.map(c => (k === 0 ? `<th>${c}</th>` : `<td>${c}</td>`)).join('') + '</tr>\n';
    });
    html += '</table>\n';
    table = [];
  };
  const inline = s => esc(s)
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, (m, t, u) => `<a href="${u}">${t}</a>`)
    .replace(/^&gt;\s?/, '');

  while (i < lines.length) {
    const line = lines[i];

    if (/^```/.test(line)) {
      if (!inCode) {
        flushList(); flushTable();
        inCode = true; codeBuf = []; codeLang = line.replace(/^```/, '').trim();
      } else {
        html += `<pre class="code"><code data-lang="${esc(codeLang)}">${esc(codeBuf.join('\n'))}</code></pre>\n`;
        inCode = false;
      }
      i++; continue;
    }
    if (inCode) { codeBuf.push(line); i++; continue; }

    if (/^\s*\|.*\|\s*$/.test(line)) { table.push(line); i++; continue; }
    else flushTable();

    let m;
    if ((m = line.match(/^(#{1,6})\s+(.*)$/))) {
      flushList(); html += `<h${m[1].length}>${inline(m[2])}</h${m[1].length}>\n`;
    } else if (/^\s*[-*]\s+\[[ x]\]\s+/.test(line)) {
      if (list !== 'ul') { flushList(); html += '<ul class="checklist">\n'; list = 'ul'; }
      html += `<li>${inline(line.replace(/^\s*[-*]\s+\[[ x]\]\s+/, ''))}</li>\n`;
    } else if ((m = line.match(/^\s*[-*]\s+(.*)$/))) {
      if (list !== 'ul') { flushList(); html += '<ul>\n'; list = 'ul'; }
      html += `<li>${inline(m[1])}</li>\n`;
    } else if ((m = line.match(/^\s*(\d+)\.\s+(.*)$/))) {
      if (list !== 'ol') { flushList(); html += '<ol>\n'; list = 'ol'; }
      html += `<li>${inline(m[2])}</li>\n`;
    } else if (/^>\s?/.test(line)) {
      flushList(); html += `<blockquote>${inline(line)}</blockquote>\n`;
    } else if (/^(---|\*\*\*)\s*$/.test(line)) {
      flushList(); html += '<hr>\n';
    } else if (line.trim() === '') {
      flushList();
    } else {
      flushList(); html += `<p>${inline(line)}</p>\n`;
    }
    i++;
  }
  flushList(); flushTable();
  return html;
}

// ------------------------------ 页面外壳（对标参考站点） ------------------------------
// 侧边栏顺序（与线上 just-the-docs 一致）：
//   首页 → 实践任务1~23 → 改写规范 → 附录（来源与许可 / 对标审计 / 子页索引）
function navItems() {
  const out = [{ href: 'index.md', title: '首页', depth: 0 }];
  for (let n = 1; n <= 23; n++) {
    const p = join(SITE, `exp${n}`, 'index.md');
    let title = `实践任务${n}`;
    if (existsSync(p)) {
      const t = readFileSync(p, 'utf8').match(/^nav_title:\s*(.+)$/m);
      if (t) title = t[1].trim();
    }
    out.push({ href: `exp${n}/index.md`, title, depth: 0 });
  }
  out.push({ href: 'CHISEL-CONVENTIONS.md', title: '改写规范', depth: 0 });
  out.push({ href: '附录.md', title: '附录', depth: 0 });
  for (const [href, title] of [
    ['LICENSE-NOTICE.md', '来源与许可'],
    ['对标审计.md', '对标审计'],
    ['子页索引.md', '子页索引'],
  ]) out.push({ href, title, depth: 1 });
  return out;
}

function shell(title, bodyHtml, relPrefix) {
  const nav = navItems().map(({ href, title, depth }) =>
    `<li class="d${depth}"><a href="${relPrefix}${encodeURI(href)}">${esc(title)}</a></li>`).join('\n');
  return `<!DOCTYPE html>
<html lang="zh-CN"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${esc(title)} — 计算机组成原理实践任务4Chisel</title>
<style>
  :root { --fg:#1f2328; --muted:#59636e; --line:#d1d9e0; --bg:#fff; --side:#f6f8fa; --link:#0969da; }
  * { box-sizing: border-box; }
  body { margin:0; font:15px/1.7 -apple-system,"Segoe UI",system-ui,"Microsoft YaHei",sans-serif; color:var(--fg); background:var(--bg); }
  .wrap { display:flex; min-height:100vh; }
  aside { width:300px; flex:0 0 300px; background:var(--side); border-right:1px solid var(--line); padding:18px 14px; overflow:auto; }
  aside h1 { font-size:15px; margin:0 0 4px; }
  aside p { font-size:12px; color:var(--muted); margin:0 0 14px; }
  aside ol { padding-left:22px; margin:0; }
  aside li { margin:5px 0; font-size:13.5px; }
  aside li.d1 { margin-left:16px; font-size:12.8px; }
  aside a { color:var(--link); text-decoration:none; }
  aside a:hover { text-decoration:underline; }
  main { flex:1; min-width:0; padding:28px 40px 80px; max-width:960px; }
  main h1 { font-size:26px; border-bottom:1px solid var(--line); padding-bottom:8px; }
  main h2 { font-size:20px; margin-top:34px; border-bottom:1px solid var(--line); padding-bottom:6px; }
  main h3 { font-size:16.5px; margin-top:24px; }
  a { color:var(--link); }
  code { background:#eff1f3; padding:.12em .35em; border-radius:4px; font-size:13px;
         font-family:ui-monospace,SFMono-Regular,Consolas,"Liberation Mono",monospace; }
  pre.code { background:#f6f8fa; border:1px solid var(--line); border-radius:6px; padding:12px 14px; overflow:auto; }
  pre.code code { background:none; padding:0; font-size:12.8px; line-height:1.55; }
  pre.code::before { content:attr(data-lang); }
  blockquote { margin:14px 0; padding:8px 14px; color:var(--muted); border-left:4px solid var(--line); background:#fbfcfd; }
  table { border-collapse:collapse; margin:14px 0; font-size:14px; }
  th,td { border:1px solid var(--line); padding:6px 10px; text-align:left; vertical-align:top; }
  th { background:var(--side); }
  ul.checklist { list-style:none; padding-left:4px; }
  hr { border:0; border-top:1px solid var(--line); margin:26px 0; }
  footer { color:var(--muted); font-size:12.5px; margin-top:40px; }
</style></head>
<body><div class="wrap">
<aside>
  <h1>计算机组成原理实践任务4Chisel</h1>
  <p>Chisel 版 ｜ 基于《CPU 设计实战：LoongArch 版》</p>
  <ol>
${nav}
  </ol>
</aside>
<main>
${bodyHtml}
<footer>本地预览（tools/preview_site.mjs）｜ 站点根目录：cod-WHU.github.io/</footer>
</main></div></body></html>`;
}

// ------------------------------ 服务器 ------------------------------
const server = createServer((req, res) => {
  let urlPath = decodeURIComponent(req.url.split('?')[0]);
  if (urlPath === '/') urlPath = '/index.md';
  let file = resolve(SITE, '.' + urlPath);
  if (!file.startsWith(resolve(SITE))) { res.writeHead(403).end('forbidden'); return; }

  if (existsSync(file) && statSync(file).isDirectory()) {
    const idx = join(file, 'index.md');
    if (existsSync(idx)) file = idx;
  }
  if (!existsSync(file)) { res.writeHead(404, { 'content-type': 'text/plain; charset=utf-8' }).end('404 ' + urlPath); return; }

  const ext = extname(file).toLowerCase();
  if (ext === '.md') {
    const md = readFileSync(file, 'utf8').replace(/^---\n[\s\S]*?\n---\n/, '');
    const title = (readFileSync(file, 'utf8').match(/^title:\s*(.+)$/m) || [, '实践任务'])[1].trim();
    const depth = file.slice(resolve(SITE).length).split(/[\\/]/).filter(Boolean).length - 1;
    const prefix = '../'.repeat(depth);
    res.writeHead(200, { 'content-type': MIME['.md'] }).end(shell(title, renderMarkdown(md), prefix));
    return;
  }
  res.writeHead(200, { 'content-type': MIME[ext] || 'application/octet-stream' }).end(readFileSync(file));
});

server.listen(PORT, '127.0.0.1', () => {
  console.log(`站点预览：http://127.0.0.1:${PORT}/`);
  console.log(`任务数：${readdirSync(SITE).filter(d => /^exp\d+$/.test(d)).length}`);
});
