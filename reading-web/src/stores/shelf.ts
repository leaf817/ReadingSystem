import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { Book } from '@/data/mockBooks'
import { useAuthStore } from '@/stores/auth'

export interface ShelfItem {
  bookId: number
  title: string
  cover: string
  author: string
  addedAt: number
}

const STORAGE_PREFIX = 'yuejie-shelf'

function storageKey(username?: string) {
  return `${STORAGE_PREFIX}:${username || 'guest'}`
}

function load(username?: string): ShelfItem[] {
  try {
    const raw = localStorage.getItem(storageKey(username))
    if (!raw) return []
    const data = JSON.parse(raw) as ShelfItem[]
    if (!Array.isArray(data)) return []
    return data.filter((item) => item?.bookId && item?.title)
  } catch {
    return []
  }
}

function save(items: ShelfItem[], username?: string) {
  localStorage.setItem(storageKey(username), JSON.stringify(items))
}

export const useShelfStore = defineStore('shelf', () => {
  const auth = useAuthStore()
  const items = ref<ShelfItem[]>([])
  const username = computed(() => auth.user?.username)

  function hydrate() {
    items.value = load(username.value)
  }

  function has(bookId: number) {
    return items.value.some((item) => item.bookId === bookId)
  }

  function toggle(book: Book) {
    hydrate()
    if (has(book.id)) {
      items.value = items.value.filter((item) => item.bookId !== book.id)
    } else {
      items.value = [
        {
          bookId: book.id,
          title: book.title,
          cover: book.cover,
          author: book.author,
          addedAt: Date.now(),
        },
        ...items.value,
      ]
    }
    save(items.value, username.value)
    return has(book.id)
  }

  hydrate()

  return { items, hydrate, has, toggle }
})
