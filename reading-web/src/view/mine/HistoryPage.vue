<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchBook } from '@/api/book'
import { useReadingStore } from '@/stores/reading'

const router = useRouter()
const reading = useReadingStore()

const items = computed(() =>
  [...reading.records].sort((a, b) => (b.updatedAt || 0) - (a.updatedAt || 0)),
)

onMounted(async () => {
  reading.hydrate()
  await Promise.all(
    items.value.map(async (item) => {
      try {
        const book = await fetchBook(item.bookId)
        reading.ensureTotal(item.bookId, book.chapters)
      } catch {
        /* ignore */
      }
    }),
  )
})

function openItem(bookId: number, seq: number) {
  void router.push({ name: 'read', params: { id: String(bookId) }, query: { chapter: String(seq) } })
}

function progressText(bookId: number) {
  const item = reading.of(bookId)
  if (!item) return ''
  const pct = reading.percent(bookId)
  if (pct != null) return `已读至 ${item.chapterTitle} · ${pct}%`
  return `已读至 ${item.chapterTitle}`
}
</script>

<template>
  <section class="history-page">
    <header>
      <h2>浏览历史</h2>
      <span>{{ items.length }} 本</span>
    </header>
    <p v-if="!items.length" class="empty">还没有阅读记录，去书城打开一本书就会出现在这里。</p>
    <ul v-else>
      <li v-for="item in items" :key="item.bookId">
        <button type="button" @click="openItem(item.bookId, item.chapterIndex)">
          <img v-if="item.cover" :src="item.cover" :alt="item.title" />
          <span v-else class="cover-fallback">{{ item.title.slice(0, 1) }}</span>
          <div>
            <strong>{{ item.title }}</strong>
            <em>{{ item.author }}</em>
            <small>{{ progressText(item.bookId) }}</small>
          </div>
        </button>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.history-page {
  background: var(--surface);
  border-radius: 14px;
  padding: 24px;
  box-shadow: var(--shadow-soft);
}

header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

h2 {
  font-size: 18px;
}

header span,
.empty,
em,
small {
  color: var(--ink-muted);
  font-size: 13px;
}

.empty {
  margin-top: 24px;
}

ul {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
  margin-top: 18px;
  list-style: none;
}

li button {
  display: grid;
  grid-template-columns: 52px 1fr;
  gap: 12px;
  width: 100%;
  padding: 10px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--elevated);
  text-align: left;
  cursor: pointer;
}

li button:hover {
  border-color: rgba(255, 77, 58, 0.3);
}

img,
.cover-fallback {
  width: 52px;
  height: 70px;
  border-radius: 6px;
  object-fit: cover;
}

.cover-fallback {
  display: grid;
  place-items: center;
  color: #fff;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
}

strong,
em,
small {
  display: block;
}

em {
  margin-top: 4px;
  font-style: normal;
}

small {
  margin-top: 6px;
  line-height: 1.4;
}
</style>
