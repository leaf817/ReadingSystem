import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { useAuthStore } from '@/stores/auth'

export interface BookmarkItem {
  bookId: number
  seq: number
  title: string
  addedAt: number
}

const STORAGE_PREFIX = 'yuejie-bookmark'

function storageKey(username?: string) {
  return `${STORAGE_PREFIX}:${username || 'guest'}`
}

function load(username?: string): BookmarkItem[] {
  try {
    const raw = localStorage.getItem(storageKey(username))
    if (!raw) return []
    const data = JSON.parse(raw) as BookmarkItem[]
    if (!Array.isArray(data)) return []
    return data.filter((item) => item?.bookId && item?.seq)
  } catch {
    return []
  }
}

function save(items: BookmarkItem[], username?: string) {
  localStorage.setItem(storageKey(username), JSON.stringify(items))
}

export const useBookmarkStore = defineStore('bookmark', () => {
  const auth = useAuthStore()
  const items = ref<BookmarkItem[]>([])
  const username = computed(() => auth.user?.username)

  function hydrate() {
    items.value = load(username.value)
  }

  function has(bookId: number, seq: number) {
    return items.value.some((item) => item.bookId === bookId && item.seq === seq)
  }

  function toggle(bookId: number, seq: number, title: string) {
    hydrate()
    if (has(bookId, seq)) {
      items.value = items.value.filter((item) => !(item.bookId === bookId && item.seq === seq))
    } else {
      items.value = [{ bookId, seq, title, addedAt: Date.now() }, ...items.value]
    }
    save(items.value, username.value)
    return has(bookId, seq)
  }

  hydrate()

  return { items, hydrate, has, toggle }
})
