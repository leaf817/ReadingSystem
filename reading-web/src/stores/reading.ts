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
  updatedAt: number
}

const STORAGE_PREFIX = 'yuejie-last-read'

function storageKey(username?: string) {
  return `${STORAGE_PREFIX}:${username || 'guest'}`
}

function load(username?: string): LastReading | null {
  try {
    const raw = localStorage.getItem(storageKey(username))
    if (!raw) return null
    const data = JSON.parse(raw) as LastReading
    if (!data?.bookId || !data?.title) return null
    return data
  } catch {
    return null
  }
}

function save(data: LastReading, username?: string) {
  localStorage.setItem(storageKey(username), JSON.stringify(data))
}

function clear(username?: string) {
  localStorage.removeItem(storageKey(username))
}

export function chapterTitleOf(book: Book, chapterIndex: number) {
  const n = Math.max(1, chapterIndex)
  const tip = book.desc?.trim().slice(0, 18) || book.title
  return `第${n}章 ${tip}${book.desc && book.desc.length > 18 ? '…' : ''}`
}

export const useReadingStore = defineStore('reading', () => {
  const auth = useAuthStore()
  const last = ref<LastReading | null>(null)

  const username = computed(() => auth.user?.username)

  function hydrate() {
    last.value = load(username.value)
  }

  function remember(book: Book, chapterIndex = 1) {
    const record: LastReading = {
      bookId: book.id,
      title: book.title,
      cover: book.cover,
      author: book.author,
      chapterIndex,
      chapterTitle: chapterTitleOf(book, chapterIndex),
      updatedAt: Date.now(),
    }
    save(record, username.value)
    last.value = record
  }

  function clearLast() {
    clear(username.value)
    last.value = null
  }

  hydrate()

  return {
    last,
    hydrate,
    remember,
    clearLast,
  }
})
