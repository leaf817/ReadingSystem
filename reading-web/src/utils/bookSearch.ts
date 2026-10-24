import type { Book } from '@/data/mockBooks'

export type SearchBy = 'title' | 'author' | 'tags' | 'protagonist' | 'supporting' | 'other'

export const SEARCH_BY_OPTIONS: {
  id: SearchBy
  label: string
  short: string
  placeholder: string
}[] = [
  { id: 'title', label: '按书名', short: '书名', placeholder: '搜索书名' },
  { id: 'author', label: '按作者', short: '作者', placeholder: '搜索作者' },
  { id: 'tags', label: '按标签', short: '标签', placeholder: '搜索标签' },
  { id: 'protagonist', label: '按主角', short: '主角', placeholder: '搜索主角' },
  { id: 'supporting', label: '按配角', short: '配角', placeholder: '搜索配角' },
  { id: 'other', label: '按其他', short: '其他', placeholder: '搜索其他' },
]

const PREFIX: Record<'protagonist' | 'supporting' | 'other', string> = {
  protagonist: '主角',
  supporting: '配角',
  other: '其他',
}

export function parseSearchBy(value: unknown): SearchBy {
  const hit = SEARCH_BY_OPTIONS.find((item) => item.id === value)
  return hit?.id ?? 'title'
}

export function searchByMeta(by: SearchBy) {
  return SEARCH_BY_OPTIONS.find((item) => item.id === by) ?? SEARCH_BY_OPTIONS[0]
}

export function matchBookBy(book: Book, keyword: string, by: SearchBy) {
  const q = keyword.trim().toLowerCase()
  if (!q) return false
  switch (by) {
    case 'author':
      return includes(book.author, q)
    case 'tags':
      return contentTagsOf(book).some((tag) => includes(tag, q)) || includes(book.category, q)
    case 'protagonist':
    case 'supporting':
    case 'other':
      return fieldValuesOf(book, PREFIX[by]).some((item) => includes(item, q))
    default:
      return includes(book.title, q)
  }
}

export function contentTagsOf(book: Book) {
  return (book.tags ?? []).filter((tag) => !/^(主角|配角|其他)[:：]/.test(tag.trim()))
}

export function fieldValuesOf(book: Book, prefix: string) {
  const seen = new Set<string>()
  const out: string[] = []
  for (const item of [...prefixedTagValues(book.tags, prefix), ...labeledValues(book.desc, prefix)]) {
    const key = item.toLowerCase()
    if (seen.has(key)) continue
    seen.add(key)
    out.push(item)
  }
  return out
}

function includes(value: string | undefined, q: string) {
  return Boolean(value && value.toLowerCase().includes(q))
}

function prefixedTagValues(tags: string[] | undefined, prefix: string) {
  return (tags ?? [])
    .filter((tag) => tag.startsWith(`${prefix}:`) || tag.startsWith(`${prefix}：`))
    .flatMap((tag) => splitNames(tag.replace(new RegExp(`^${prefix}[:：]`), '').trim()))
}

function labeledValues(text: string | undefined, prefix: string) {
  if (!text) return []
  const match = text.match(new RegExp(`${prefix}\\s*[:：]\\s*([^\\n┃|]+)`))
  return match ? splitNames(match[1]) : []
}

function splitNames(raw: string) {
  return raw
    .split(/[、，,／/]/)
    .map((item) => item.trim())
    .filter(Boolean)
}
