/**
 * 将《暗格里的秘密》txt 按章节写入 reading_system.book_chapter
 * 用法：node scripts/import-ange-secret.cjs
 */
const { spawnSync } = require('child_process')
const fs = require('fs')
const path = require('path')

const ROOT = path.resolve(__dirname, '..')
const NOVEL = path.join(ROOT, 'uploads', 'novel', '《暗格里的秘密》作者：耳东兔子.txt')
const MYSQL = process.env.MYSQL_BIN || 'E:\\SQL\\mysql\\bin\\mysql.exe'
const DB_USER = process.env.DB_USERNAME || 'root'
const DB_PASS = process.env.DB_PASSWORD || ''
const DB_NAME = 'reading_system'
const BOOK_TITLE = '暗格里的秘密'

function decodeNovel(filePath) {
  const buf = fs.readFileSync(filePath)
  try {
    return new TextDecoder('gbk').decode(buf)
  } catch {
    return buf.toString('utf8')
  }
}

function strip(line) {
  return line.replace(/[\u3000\s]+/g, '').trim()
}

function headingKind(raw) {
  const s = strip(raw)
  if (!s) return null
  if (s === '文案' || s === '楔子') return { type: 'start', title: s }
  if (s === '番外完' || s === '番外完。' || s === '（全书完）' || s === '全书完') {
    return { type: 'end', title: s }
  }
  if (/^第.+章/.test(s) && s.length <= 24) {
    return { type: 'start', title: cleanTitle(s) }
  }
  return null
}

function cleanTitle(s) {
  if (s === '第52章第五十一章') return '第五十一章'
  if (s === '第六十一章番外非正文') return '第六十一章 番外'
  if (s === '第八十七章番外') return '第八十七章 番外'
  return s.replace(/（修）$/, '').replace(/（[一二]更）$/, '')
}

function parseChapters(text) {
  const lines = text.split(/\r?\n/)
  const chapters = []
  let blurb = ''
  let current = null

  const push = () => {
    if (!current) return
    current.content = current.lines.join('\n').replace(/^\n+|\n+$/g, '')
    delete current.lines
    if (current.title === '文案') {
      blurb = current.content.replace(/[\u3000]/g, '').trim()
      current = null
      return
    }
    if (current.content.replace(/\s/g, '').length === 0) {
      current = null
      return
    }
    chapters.push(current)
    current = null
  }

  for (const line of lines) {
    const kind = headingKind(line)
    if (kind?.type === 'end') {
      if (current && kind.title.startsWith('番外完')) {
        current.lines.push(line)
      }
      continue
    }
    if (kind?.type === 'start') {
      if (current && current.title === kind.title && current.title === '楔子') {
        continue
      }
      push()
      current = { title: kind.title, lines: [] }
      continue
    }
    if (current) current.lines.push(line)
  }
  push()
  return { blurb, chapters }
}

function wordCount(content) {
  return content.replace(/\s/g, '').length
}

function esc(value) {
  return String(value)
    .replace(/\\/g, '\\\\')
    .replace(/'/g, "\\'")
    .replace(/\n/g, '\\n')
    .replace(/\r/g, '\\r')
}

function mysqlArgs() {
  const args = ['-N', '-B', `--default-character-set=utf8mb4`, `-u${DB_USER}`]
  if (DB_PASS) args.push(`-p${DB_PASS}`)
  args.push(DB_NAME)
  return args
}

function runSql(sql) {
  const result = spawnSync(MYSQL, mysqlArgs(), {
    input: sql,
    encoding: 'utf8',
    maxBuffer: 32 * 1024 * 1024,
  })
  if (result.status !== 0) {
    throw new Error(result.stderr || result.stdout || `mysql exit ${result.status}`)
  }
  return (result.stdout || '').trim()
}

function main() {
  if (!fs.existsSync(NOVEL)) {
    throw new Error(`找不到小说文件：${NOVEL}`)
  }
  if (!fs.existsSync(MYSQL)) {
    throw new Error(`找不到 mysql：${MYSQL}`)
  }

  const { chapters } = parseChapters(decodeNovel(NOVEL))
  if (chapters.length === 0) {
    throw new Error('没有解析到章节')
  }

  const bookIdRaw = runSql(
    `SELECT id FROM book WHERE title='${esc(BOOK_TITLE)}' ORDER BY id LIMIT 1;`
  )
  const bookId = Number(String(bookIdRaw).split(/\s+/).pop())
  if (!Number.isFinite(bookId) || bookId <= 0) {
    throw new Error(`数据库中没有《${BOOK_TITLE}》，请先导入书籍列表`)
  }

  const desc = [
    '1、我是一个怪物，他们说我不配拥有爱情。 可他们却不知道，当人们将手指指向我时，我甘愿卸下臂膀，只为了能让他走的更远一点。',
    '2、年少时的爱，像风，看不见，却感受的到； 就像刻在桌板上的名字，怕你看见，又怕你看不见。 ——节选自《小怪兽日记》',
    '我们半世相逢，依旧少年如风。',
    '拽痞注孤生VS伪善少女',
    '主角：丁羡、周斯越 ┃ 配角：蒋沉、杨纯子',
  ].join('\n')
  const tags = '都市,校园,正剧,其他:校园文'

  const inserts = chapters.map((ch, i) => {
    const seq = i + 1
    const content = ch.content
    return `(${bookId}, ${seq}, '${esc(ch.title)}', '${esc(content)}', ${wordCount(content)})`
  })

  const sql = [
    'SET NAMES utf8mb4;',
    'SET FOREIGN_KEY_CHECKS=1;',
    `DELETE FROM book_chapter WHERE book_id=${bookId};`,
    `INSERT INTO book_chapter (book_id, seq, title, content, word_count) VALUES\n${inserts.join(',\n')};`,
    `UPDATE book SET chapters=${chapters.length}, description='${esc(desc)}', tags='${esc(tags)}', status='完结' WHERE id=${bookId};`,
    `SELECT ${bookId} AS book_id, COUNT(*) AS chapter_count, SUM(word_count) AS word_count FROM book_chapter WHERE book_id=${bookId};`,
  ].join('\n')

  const out = runSql(sql)
  console.log(out)
  console.log(`已写入《${BOOK_TITLE}》 ${chapters.length} 章，book_id=${bookId}`)
  console.log(chapters.map((ch, i) => `${String(i + 1).padStart(3, ' ')}  ${ch.title}`).join('\n'))
}

main()
