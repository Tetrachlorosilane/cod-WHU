// SPDX-License-Identifier: BSD-3-Clause
// ============================================================================
// tarlib.mjs —— 极简 tar(ustar) + gzip 读写（零依赖，Node 内置 zlib）
//
// 为什么自己写：站点仓库要"独立可跑"，不能依赖系统 tar 或 npm 包。
// ustar 格式足够简单：每个文件 512B 头 + 512B 对齐数据，末尾两个零块。
// ============================================================================

import { gzipSync, gunzipSync } from 'node:zlib';
import { writeFileSync, readFileSync, mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';

const BLOCK = 512;

function pad(str, len) {
  const b = Buffer.alloc(len, 0);
  Buffer.from(str, 'utf8').copy(b, 0, 0, Math.min(len, Buffer.byteLength(str)));
  return b;
}
function octal(n, len) {
  // len-1 位八进制 + '\0'
  const s = n.toString(8).padStart(len - 1, '0').slice(-(len - 1));
  return pad(s + '\0', len);
}

/** 单个文件头 */
function header(name, size, mtime = Math.floor(Date.now() / 1000)) {
  if (Buffer.byteLength(name) > 100) throw new Error(`tar 路径过长(>100)：${name}`);
  const h = Buffer.alloc(BLOCK, 0);
  pad(name, 100).copy(h, 0);
  octal(0o644, 8).copy(h, 100);          // mode
  octal(0, 8).copy(h, 108);              // uid
  octal(0, 8).copy(h, 116);              // gid
  octal(size, 12).copy(h, 124);          // size
  octal(mtime, 12).copy(h, 136);         // mtime
  h.write('        ', 148, 8, 'ascii');  // chksum 先填空格
  h.write('0', 156, 1, 'ascii');         // typeflag: 普通文件
  pad('ustar\0', 6).copy(h, 257);        // magic
  pad('00', 2).copy(h, 263);             // version
  let sum = 0;
  for (const b of h) sum += b;
  pad(sum.toString(8).padStart(6, '0') + '\0 ', 8).copy(h, 148);
  return h;
}

/**
 * 写入 .tar.gz
 * @param {{name:string, data:Buffer}[]} entries 条目（name 用 / 分隔的相对路径）
 * @param {string} outPath 输出路径
 */
export function writeTarGz(entries, outPath) {
  const chunks = [];
  const mtime = Math.floor(Date.now() / 1000);
  for (const e of entries) {
    chunks.push(header(e.name, e.data.length, mtime));
    chunks.push(e.data);
    const rem = e.data.length % BLOCK;
    if (rem) chunks.push(Buffer.alloc(BLOCK - rem, 0));
  }
  chunks.push(Buffer.alloc(BLOCK * 2, 0));   // 结束块
  const tgz = gzipSync(Buffer.concat(chunks), { level: 9 });
  mkdirSync(dirname(outPath), { recursive: true });
  writeFileSync(outPath, tgz);
  return tgz.length;
}

/**
 * 读取 .tar.gz
 * @param {string} path
 * @returns {{name:string, data:Buffer}[]}
 */
export function readTarGz(path) {
  const buf = gunzipSync(readFileSync(path));
  const out = [];
  let off = 0;
  while (off + BLOCK <= buf.length) {
    const h = buf.subarray(off, off + BLOCK);
    if (h.every(b => b === 0)) break;                 // 结束块
    const name = h.subarray(0, 100).toString('utf8').replace(/\0.*$/, '');
    const prefix = h.subarray(345, 500).toString('utf8').replace(/\0.*$/, '');
    const size = parseInt(h.subarray(124, 136).toString('ascii').replace(/\0.*$/, '').trim() || '0', 8);
    const type = String.fromCharCode(h[156]);
    off += BLOCK;
    const data = buf.subarray(off, off + size);
    off += Math.ceil(size / BLOCK) * BLOCK;
    if (type === '0' || type === '\0' || type === '') {
      out.push({ name: prefix ? `${prefix}/${name}` : name, data: Buffer.from(data) });
    }
  }
  return out;
}

/** 解包到目标目录（保持相对路径） */
export function extractTarGz(path, destDir) {
  const entries = readTarGz(path);
  for (const e of entries) {
    const target = join(destDir, e.name);
    mkdirSync(dirname(target), { recursive: true });
    writeFileSync(target, e.data);
  }
  return entries;
}
