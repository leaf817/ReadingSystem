<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BookCard from '@/components/BookCard.vue'
import { fetchBooks } from '@/api/book'
import { books as mockBooks, type Book } from '@/data/mockBooks'
import { useBookmarkStore } from '@/stores/bookmark'
import { useReadingStore } from '@/stores/reading'
import { contentTagsOf } from '@/utils/bookSearch'

const reading = useReadingStore()
const bookmarks = useBookmarkStore()
const router = useRouter()
const catalog = ref<Book[]>([])
const lastReading = computed(() => reading.last)

const bookmarkBooks = computed(() => {
  const latest = new Map<number, (typeof bookmarks.items)[number]>()
  for (const item of bookmarks.items) {
    const prev = latest.get(item.bookId)
    if (!prev || item.addedAt > prev.addedAt) latest.set(item.bookId, item)
  }
  return [...latest.values()]
    .sort((a, b) => b.addedAt - a.addedAt)
    .map((mark) => {
      const book = catalog.value.find((item) => item.id === mark.bookId)
      const rec = reading.of(mark.bookId)
      return {
        bookId: mark.bookId,
        title: book?.title || rec?.title || '未知作品',
        cover: book?.cover || rec?.cover || '',
        author: book?.author || rec?.author || '',
        seq: mark.seq,
        chapterTitle: mark.title,
      }
    })
})

const tasteTags = computed(() => {
  const readIds = new Set(reading.records.map((item) => item.bookId))
  const score = new Map<string, number>()
  for (const book of catalog.value) {
    if (!readIds.has(book.id)) continue
    for (const tag of [book.category, ...contentTagsOf(book)]) {
      const key = tag.trim()
      if (!key) continue
      score.set(key, (score.get(key) || 0) + 1)
    }
  }
  return [...score.entries()].sort((a, b) => b[1] - a[1]).map(([tag]) => tag)
})

const recommendBooks = computed(() => {
  const occupied = new Set([
    ...reading.records.map((item) => item.bookId),
    ...bookmarkBooks.value.map((item) => item.bookId),
  ])
  const weights = new Map(tasteTags.value.map((tag, index) => [tag, tasteTags.value.length - index]))
  const scored = catalog.value
    .filter((book) => !occupied.has(book.id))
    .map((book) => {
      const types = [book.category, ...contentTagsOf(book)]
      const score = types.reduce((sum, tag) => sum + (weights.get(tag) || 0), 0)
      return { book, score }
    })
  const matched = scored.filter((item) => item.score > 0).sort((a, b) => b.score - a.score)
  const pool = matched.length ? matched : scored
  return pool.slice(0, 5).map((item) => item.book)
})

const recommendHint = computed(() => {
  const tags = tasteTags.value.slice(0, 3)
  if (!tags.length) return '先读几本书，之后会按你的阅读类型来推荐'
  return `根据你读过的「${tags.join('、')}」推荐`
})

const recentReads = computed(() =>
  [...reading.records].sort((a, b) => (b.updatedAt || 0) - (a.updatedAt || 0)).slice(0, 5),
)

const hotBooks = computed(() =>
  [...catalog.value]
    .sort((a, b) => heatValue(b.heat) - heatValue(a.heat))
    .slice(0, 8),
)

function heatValue(heat?: string) {
  const raw = String(heat || '0').replace(/万/g, '')
  const n = Number.parseFloat(raw)
  if (!Number.isFinite(n)) return 0
  return String(heat).includes('万') ? n * 10000 : n
}

function continueReading() {
  if (!lastReading.value) return
  router.push({
    name: 'read',
    params: { id: String(lastReading.value.bookId) },
    query: { chapter: String(lastReading.value.chapterIndex) },
  })
}

function openBookmark(item: (typeof bookmarkBooks.value)[number]) {
  router.push({
    name: 'read',
    params: { id: String(item.bookId) },
    query: { chapter: String(item.seq) },
  })
}

function openRecent(item: (typeof recentReads.value)[number]) {
  router.push({
    name: 'read',
    params: { id: String(item.bookId) },
    query: { chapter: String(item.chapterIndex) },
  })
}

function openBook(id: number) {
  router.push({ name: 'book', params: { id: String(id) } })
}

onMounted(async () => {
  reading.hydrate()
  bookmarks.hydrate()
  try {
    catalog.value = await fetchBooks()
  } catch {
    catalog.value = mockBooks
  }
})
</script>

<template>
  <div class="home">
  <div class="overview">
    <button
      v-if="lastReading"
      type="button"
      class="continue"
      @click="continueReading"
    >
      <img :src="lastReading.cover" :alt="lastReading.title" class="continue__cover" />
      <div class="continue__meta">
        <strong>{{ lastReading.title }}</strong>
        <span>{{ lastReading.chapterTitle }}</span>
      </div>
      <span class="continue__action">
        继续阅读
        <svg viewBox="0 0 12 12" width="12" height="12" aria-hidden="true">
          <path
            d="M4.2 2.5L7.8 6 4.2 9.5"
            fill="none"
            stroke="currentColor"
            stroke-width="1.6"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </span>
    </button>

    <section class="block">
      <div class="block__head">
        <h2>我的书签</h2>
      </div>
      <p v-if="!bookmarkBooks.length" class="empty">还没有书签，阅读时点书签后会出现在这里</p>
      <div v-else class="mark-list">
        <button
          v-for="item in bookmarkBooks"
          :key="`${item.bookId}-${item.seq}`"
          type="button"
          class="mark"
          @click="openBookmark(item)"
        >
          <img v-if="item.cover" :src="item.cover" :alt="item.title" />
          <span v-else class="mark__fallback">{{ item.title.slice(0, 1) }}</span>
          <div>
            <strong>{{ item.title }}</strong>
            <em>{{ item.author }}</em>
            <small>书签 · {{ item.chapterTitle }}</small>
          </div>
        </button>
      </div>
    </section>

    <section class="block">
      <div class="block__head">
        <h2>小说推荐</h2>
        <span>{{ recommendHint }}</span>
      </div>
      <p v-if="!recommendBooks.length" class="empty">暂时没有可推荐的作品</p>
      <div v-else class="grid">
        <BookCard v-for="book in recommendBooks" :key="book.id" :book="book" compact />
      </div>
    </section>
  </div>

  <aside class="rail">
    <section class="block">
      <div class="block__head">
        <h2>最近阅读</h2>
      </div>
      <p v-if="!recentReads.length" class="empty">还没有阅读记录</p>
      <ul v-else class="rail-list">
        <li v-for="item in recentReads" :key="item.bookId">
          <button type="button" @click="openRecent(item)">
            <img v-if="item.cover" :src="item.cover" :alt="item.title" />
            <span v-else class="rail-fallback">{{ item.title.slice(0, 1) }}</span>
            <div>
              <strong>{{ item.title }}</strong>
              <small>{{ item.chapterTitle }}</small>
            </div>
          </button>
        </li>
      </ul>
    </section>

    <section class="block">
      <div class="block__head">
        <h2>热门榜</h2>
      </div>
      <p v-if="!hotBooks.length" class="empty">暂无热门作品</p>
      <ol v-else class="hot-list">
        <li v-for="(book, index) in hotBooks" :key="book.id">
          <button type="button" @click="openBook(book.id)">
            <em :class="{ 'is-top': index < 3 }">{{ index + 1 }}</em>
            <span>{{ book.title }}</span>
          </button>
        </li>
      </ol>
    </section>
  </aside>
  </div>
</template>

<style scoped>
.home {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  align-items: start;
  gap: 18px;
}

.overview,
.rail {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.rail {
  position: sticky;
  top: 124px;
}

.continue {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 12px 16px;
  border: 0;
  border-radius: 12px;
  background: var(--surface);
  text-align: left;
  cursor: pointer;
  box-shadow: var(--shadow-soft);
  transition: background 0.2s;
}

.continue:hover {
  background: var(--elevated);
}

.continue__cover {
  width: 42px;
  height: 56px;
  border-radius: 4px;
  object-fit: cover;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.continue__meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.continue__meta strong {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.continue__meta span {
  font-size: 13px;
  color: var(--ink-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.continue__action {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  font-size: 13px;
  color: var(--ink-muted);
}

.continue:hover .continue__action {
  color: var(--brand);
}

.block {
  background: var(--surface);
  border-radius: 16px;
  padding: 22px 24px 24px;
  border: 1px solid var(--line);
  box-shadow: var(--shadow-soft);
}

.block__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.block__head h2 {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.block__head span {
  font-size: 13px;
  color: var(--ink-muted);
}

.empty {
  margin-top: 16px;
  color: var(--ink-muted);
  font-size: 13px;
}

.mark-list {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.mark {
  display: grid;
  grid-template-columns: 48px 1fr;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 10px 12px;
  border: 0;
  border-radius: 12px;
  background: var(--chip);
  text-align: left;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
}

.mark:hover {
  background: var(--brand-soft);
  transform: translateY(-1px);
}

.mark img,
.mark__fallback {
  width: 48px;
  height: 64px;
  border-radius: 6px;
  object-fit: cover;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
}

.mark__fallback {
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 700;
}

.mark strong,
.mark em,
.mark small {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mark strong {
  font-size: 14px;
  color: var(--ink);
}

.mark em {
  margin-top: 3px;
  font-style: normal;
  font-size: 12px;
  color: var(--ink-muted);
}

.mark small {
  margin-top: 5px;
  font-size: 12px;
  color: var(--brand);
}

.grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px 12px;
}

.rail-list,
.hot-list {
  margin-top: 12px;
  list-style: none;
}

.rail-list button,
.hot-list button {
  display: grid;
  width: 100%;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.rail-list button {
  grid-template-columns: 40px 1fr;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
}

.rail-list img,
.rail-fallback {
  width: 40px;
  height: 54px;
  border-radius: 5px;
  object-fit: cover;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
}

.rail-fallback {
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.rail-list strong,
.rail-list small {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rail-list strong {
  font-size: 13px;
}

.rail-list small {
  margin-top: 4px;
  font-size: 12px;
  color: var(--ink-muted);
}

.rail-list button:hover strong {
  color: var(--brand);
}

.hot-list button {
  grid-template-columns: 22px 1fr;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}

.hot-list em {
  font-style: normal;
  font-size: 13px;
  font-weight: 700;
  color: #b8bcc4;
}

.hot-list em.is-top {
  color: var(--brand);
}

.hot-list span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.hot-list button:hover span {
  color: var(--brand);
}

@media (max-width: 1100px) {
  .home {
    grid-template-columns: 1fr;
  }

  .rail {
    position: static;
  }

  .grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 980px) {
  .mark-list,
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 720px) {
  .mark-list {
    grid-template-columns: 1fr;
  }

  .grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .continue__action {
    font-size: 12px;
  }
}
</style>
