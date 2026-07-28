<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { books } from '@/data/mockBooks'
import { chapterTitleOf, useReadingStore } from '@/stores/reading'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const reading = useReadingStore()
const auth = useAuthStore()

const bookId = computed(() => Number(route.params.id))
const book = computed(() => books.find((b) => b.id === bookId.value) || null)

const chapterIndex = computed(() => {
  const q = Number(route.query.chapter)
  if (Number.isFinite(q) && q > 0) return Math.floor(q)
  if (reading.last?.bookId === bookId.value) return reading.last.chapterIndex
  return 1
})

const chapterTitle = computed(() =>
  book.value ? chapterTitleOf(book.value, chapterIndex.value) : '',
)

const content = computed(() => {
  if (!book.value) return ''
  const title = book.value.title
  const lines = [
    `　　${book.value.desc}`,
    '',
    `　　夜色渐深，${book.value.author} 笔下的世界缓缓展开。`,
    `　　关于《${title}》的这一章，只是故事长河中的一截浪花。`,
    '　　你合上书页、离开阅读器后，这里会成为「继续阅读」里的那一部。',
    '　　下次回来，点一下就能接着看。',
  ]
  return lines.join('\n')
})

function persist() {
  if (!book.value) return
  reading.remember(book.value, chapterIndex.value)
}

function exit() {
  persist()
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push(auth.isLoggedIn && auth.role === 'reader' ? '/mine' : '/')
}

onMounted(() => {
  reading.hydrate()
  persist()
})

onBeforeUnmount(() => {
  persist()
})

watch([bookId, chapterIndex], () => {
  persist()
})
</script>

<template>
  <div class="reader">
    <header class="bar">
      <button type="button" class="back" @click="exit">‹ 退出</button>
      <div class="bar__meta" v-if="book">
        <strong>{{ book.title }}</strong>
        <span>{{ chapterTitle }}</span>
      </div>
      <span class="bar__spacer" />
    </header>

    <main v-if="book" class="page">
      <h1>{{ chapterTitle }}</h1>
      <p class="author">{{ book.author }}</p>
      <pre class="body">{{ content }}</pre>
      <p class="hint">退出阅读后，将在「我的 · 继续阅读」显示本书。</p>
    </main>

    <main v-else class="page page--empty">
      <p>未找到该作品</p>
      <button type="button" class="link" @click="exit">返回</button>
    </main>
  </div>
</template>

<style scoped>
.reader {
  min-height: 100vh;
  background:
    radial-gradient(ellipse 70% 40% at 50% 0%, rgba(255, 77, 58, 0.06), transparent 55%),
    #f7f4ef;
}

.bar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: grid;
  grid-template-columns: 88px 1fr 88px;
  align-items: center;
  height: 52px;
  padding: 0 12px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.back {
  border: 0;
  background: transparent;
  color: var(--ink-secondary);
  font-size: 14px;
  cursor: pointer;
  text-align: left;
  padding: 8px;
}

.back:hover {
  color: var(--brand);
}

.bar__meta {
  min-width: 0;
  text-align: center;
}

.bar__meta strong {
  display: block;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bar__meta span {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  color: var(--ink-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.page {
  width: min(720px, calc(100% - 32px));
  margin: 0 auto;
  padding: 28px 0 64px;
}

.page h1 {
  font-family: var(--font-serif);
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 0.04em;
  line-height: 1.4;
}

.author {
  margin-top: 10px;
  font-size: 13px;
  color: var(--ink-muted);
}

.body {
  margin-top: 28px;
  font-family: var(--font-serif);
  font-size: 18px;
  line-height: 1.95;
  color: #2a2a2a;
  white-space: pre-wrap;
  word-break: break-word;
}

.hint {
  margin-top: 36px;
  font-size: 12px;
  color: #a8afbb;
}

.page--empty {
  text-align: center;
  padding-top: 80px;
  color: var(--ink-muted);
}

.link {
  margin-top: 16px;
  border: 0;
  background: transparent;
  color: var(--brand);
  cursor: pointer;
}
</style>
