import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { books, type Book } from '@/data/mockBooks'
import { useAuthStore } from '@/stores/auth'

export interface LastReading {
  bookId: number
  title: string
  cover: string
  author: string
  chapterIndex: number
  chapterTitle: string
  total?: number
  updatedAt: number
}

const LAST_PREFIX = 'yuejie-last-read'
const PROGRESS_PREFIX = 'yuejie-progress'

function lastKey(username?: string) {
  return `${LAST_PREFIX}:${username || 'guest'}`
}

function progressKey(username?: string) {
  return `${PROGRESS_PREFIX}:${username || 'guest'}`
}

function loadLast(username?: string): LastReading | null {
  try {
    const raw = localStorage.getItem(lastKey(username))
    if (!raw) return null
    const data = JSON.parse(raw) as LastReading
    if (!data?.bookId || !data?.title) return null
    return data
  } catch {
    return null
  }
}

function loadProgress(username?: string): LastReading[] {
  try {
    const raw = localStorage.getItem(progressKey(username))
    if (!raw) return []
    const data = JSON.parse(raw) as LastReading[]
    if (!Array.isArray(data)) return []
    return data.filter((item) => item?.bookId && item?.chapterIndex)
  } catch {
    return []
  }
}

function saveLast(data: LastReading, username?: string) {
  localStorage.setItem(lastKey(username), JSON.stringify(data))
}

function saveProgress(items: LastReading[], username?: string) {
  localStorage.setItem(progressKey(username), JSON.stringify(items))
}

function chapterCountOf(value?: number) {
  if (!Number.isFinite(value) || value == null) return undefined
  const n = Math.round(value)
  if (n < 1 || n > 400) return undefined
  return n
}

const CN: Record<string, number> = {
  零: 0,
  〇: 0,
  一: 1,
  二: 2,
  两: 2,
  三: 3,
  四: 4,
  五: 5,
  六: 6,
  七: 7,
  八: 8,
  九: 9,
}

function chineseToInt(raw: string) {
  if (!raw) return null
  if (/^\d+$/.test(raw)) return Number(raw)
  if (raw === '十') return 10
  if (raw.startsWith('十')) return 10 + (CN[raw.slice(1)] ?? 0)
  const idx = raw.indexOf('十')
  if (idx >= 0) {
    const tens = idx === 0 ? 1 : CN[raw.slice(0, idx)]
    if (tens == null) return null
    const rest = raw.slice(idx + 1)
    const ones = rest ? CN[rest] : 0
    if (ones == null) return null
    return tens * 10 + ones
  }
  return CN[raw] ?? null
}

function seqFromTitle(title?: string) {
  if (!title) return null
  if (title.includes('楔子')) return 1
  const match = title.match(/第([零〇一二两三四五六七八九十\d]+)章/)
  if (!match) return null
  const n = chineseToInt(match[1])
  return n && n > 0 ? n : null
}

function resolvedSeq(item: LastReading) {
  const fromTitle = seqFromTitle(item.chapterTitle)
  if (fromTitle && fromTitle > item.chapterIndex) return fromTitle
  return item.chapterIndex
}

export function chapterTitleOf(book: Book, chapterIndex: number) {
  const n = Math.max(1, chapterIndex)
  const tip = book.desc?.trim().slice(0, 18) || book.title
  return `第${n}章 ${tip}${book.desc && book.desc.length > 18 ? '…' : ''}`
}

export const useReadingStore = defineStore('reading', () => {
  const auth = useAuthStore()
  const last = ref<LastReading | null>(null)
  const records = ref<LastReading[]>([])
  const username = computed(() => auth.user?.username)

  function hydrate() {
    last.value = loadLast(username.value)
    records.value = loadProgress(username.value).map((item) => {
      const seq = resolvedSeq(item)
      return seq === item.chapterIndex ? item : { ...item, chapterIndex: seq }
    })
    if (last.value) {
      const seq = resolvedSeq(last.value)
      if (seq !== last.value.chapterIndex) last.value = { ...last.value, chapterIndex: seq }
    }
    if (last.value && !records.value.some((item) => item.bookId === last.value?.bookId)) {
      records.value = [last.value, ...records.value]
    }
    saveProgress(records.value, username.value)
  }

  function remember(book: Book, chapterIndex = 1, chapterTitle?: string, total?: number) {
    const prev = records.value.find((item) => item.bookId === book.id)
    const title = chapterTitle || chapterTitleOf(book, chapterIndex)
    const seq = Math.max(chapterIndex, seqFromTitle(title) || 0)
    const record: LastReading = {
      bookId: book.id,
      title: book.title,
      cover: book.cover,
      author: book.author,
      chapterIndex: seq || chapterIndex,
      chapterTitle: title,
      total: chapterCountOf(total) ?? chapterCountOf(book.chapters) ?? chapterCountOf(prev?.total),
      updatedAt: Date.now(),
    }
    saveLast(record, username.value)
    last.value = record
    records.value = [record, ...records.value.filter((item) => item.bookId !== book.id)]
    saveProgress(records.value, username.value)
  }

  function ensureTotal(bookId: number, total?: number) {
    const count = chapterCountOf(total)
    if (!count) return
    const item = of(bookId)
    if (!item || item.total === count) return
    const next = { ...item, total: count }
    records.value = records.value.map((row) => (row.bookId === bookId ? next : row))
    saveProgress(records.value, username.value)
    if (last.value?.bookId === bookId) {
      last.value = next
      saveLast(next, username.value)
    }
  }

  function of(bookId: number) {
    return records.value.find((item) => item.bookId === bookId) || null
  }

  function percent(bookId: number) {
    const item = of(bookId)
    const total = chapterCountOf(item?.total)
    if (!item || !total) return null
    return Math.min(100, Math.max(1, Math.round((resolvedSeq(item) / total) * 100)))
  }

  function clearLast() {
    localStorage.removeItem(lastKey(username.value))
    last.value = null
  }

  hydrate()

  return {
    last,
    records,
    hydrate,
    remember,
    ensureTotal,
    of,
    percent,
    clearLast,
  }
})
