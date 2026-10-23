<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchBook, fetchBookChapters, type ChapterSummary } from '@/api/book'
import { books, type Book } from '@/data/mockBooks'
import { useReadingStore } from '@/stores/reading'
import { useShelfStore } from '@/stores/shelf'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const reading = useReadingStore()
const shelf = useShelfStore()
const auth = useAuthStore()
const favorited = computed(() => (book.value ? shelf.has(book.value.id) : false))

const bookId = computed(() => Number(route.params.id))
const book = ref<Book | null>(null)
const chapters = ref<ChapterSummary[]>([])
const loading = ref(false)
const loadError = ref('')

const continueSeq = computed(() => {
  if (!book.value || reading.last?.bookId !== book.value.id) return null
  return reading.last.chapterIndex
})

const continueTitle = computed(() => {
  if (!continueSeq.value) return ''
  return (
    chapters.value.find((item) => item.seq === continueSeq.value)?.title ||
    reading.last?.chapterTitle ||
    ''
  )
})

const totalWords = computed(() => chapters.value.reduce((sum, item) => sum + (item.wordCount || 0), 0))

const contentTags = computed(() =>
  (book.value?.tags ?? []).filter((tag) => !tag.startsWith('其他:') && !tag.startsWith('其他：')),
)

const extraTags = computed(() =>
  (book.value?.tags ?? [])
    .filter((tag) => tag.startsWith('其他:') || tag.startsWith('其他：'))
    .map((tag) => tag.replace(/^其他[:：]/, '').trim())
    .filter(Boolean),
)

function goRead(seq: number) {
  if (!book.value) return
  reading.remember(book.value, seq, chapters.value.find((item) => item.seq === seq)?.title)
  void router.push({ name: 'read', params: { id: String(book.value.id) }, query: { chapter: String(seq) } })
}

function toggleFavorite() {
  if (!book.value) return
  shelf.toggle(book.value)
}

function exit() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push(auth.isLoggedIn && auth.role === 'admin' ? '/admin/books' : '/')
}

async function load() {
  if (!Number.isFinite(bookId.value) || bookId.value <= 0) {
    book.value = null
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    try {
      book.value = await fetchBook(bookId.value)
    } catch {
      book.value = books.find((item) => item.id === bookId.value) || null
    }
    if (!book.value) return
    try {
      chapters.value = await fetchBookChapters(bookId.value)
    } catch {
      chapters.value = []
    }
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  reading.hydrate()
  shelf.hydrate()
  void load()
})

watch(bookId, () => {
  void load()
})
</script>

<template>
  <div class="catalog-page">
    <header class="bar">
      <button type="button" class="back" @click="exit">‹ 返回</button>
      <strong>{{ book?.title || '作品目录' }}</strong>
      <span />
    </header>

    <main v-if="book" class="wrap">
      <section class="hero">
        <img v-if="book.cover" class="cover" :src="book.cover" :alt="book.title" />
        <div v-else class="cover cover--fallback">{{ book.title.slice(0, 1) }}</div>
        <div class="hero__meta">
          <p class="kicker">{{ book.category }} · {{ book.status }}</p>
          <h1>{{ book.title }}</h1>
          <p class="author">{{ book.author }}</p>
          <p class="desc">{{ book.desc }}</p>
          <div class="stats">
            <span>{{ chapters.length || book.chapters || 0 }} 章</span>
            <span v-if="totalWords">{{ totalWords.toLocaleString() }} 字</span>
            <span>{{ book.heat }}热度</span>
          </div>
          <div v-if="contentTags.length" class="tag-row">
            <em>标签</em>
            <div class="tags">
              <span v-for="tag in contentTags" :key="tag">{{ tag }}</span>
            </div>
          </div>
          <div v-if="extraTags.length" class="tag-row">
            <em>其他</em>
            <div class="tags">
              <span v-for="tag in extraTags" :key="tag">{{ tag }}</span>
            </div>
          </div>
          <div class="actions">
            <button
              v-if="chapters.length"
              type="button"
              class="btn-primary"
              @click="goRead(continueSeq || chapters[0].seq)"
            >
              {{ continueSeq ? '继续阅读' : '开始阅读' }}
            </button>
            <button type="button" class="btn-fav" :class="{ 'is-on': favorited }" @click="toggleFavorite">
              <svg class="star" viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
                <path
                  d="M12 2.6 14.7 8.2l6.2.9-4.5 4.4 1.1 6.2L12 16.8 6.5 19.7 7.6 13.5 3.1 9.1l6.2-.9L12 2.6z"
                  :fill="favorited ? 'currentColor' : 'none'"
                  stroke="currentColor"
                  stroke-width="1.6"
                  stroke-linejoin="round"
                />
              </svg>
              {{ favorited ? '已收藏' : '收藏' }}
            </button>
          </div>
        </div>
        <p v-if="continueTitle" class="resume">上次读到：{{ continueTitle }}</p>
      </section>

      <section class="list">
        <header>
          <h2>目录</h2>
          <span>{{ chapters.length }} 章</span>
        </header>
        <p v-if="loading" class="empty">正在载入目录…</p>
        <p v-else-if="loadError" class="empty">{{ loadError }}</p>
        <p v-else-if="!chapters.length" class="empty">本书暂无章节</p>
        <ol v-else>
          <li v-for="item in chapters" :key="item.seq">
            <button type="button" :class="{ 'is-current': item.seq === continueSeq }" @click="goRead(item.seq)">
              <em>{{ String(item.seq).padStart(2, '0') }}</em>
              <span>{{ item.title }}</span>
              <small>{{ item.wordCount.toLocaleString() }} 字</small>
            </button>
          </li>
        </ol>
      </section>
    </main>

    <main v-else class="wrap wrap--empty">
      <p>{{ loading ? '加载中…' : '未找到该作品' }}</p>
      <button type="button" class="back" @click="exit">返回</button>
    </main>
  </div>
</template>

<style scoped>
.catalog-page {
  min-height: 100vh;
  background:
    radial-gradient(ellipse 70% 40% at 50% 0%, rgba(255, 77, 58, 0.07), transparent 55%),
    #f6f3ef;
}

.bar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: grid;
  grid-template-columns: 88px 1fr 88px;
  align-items: center;
  height: 56px;
  padding: 0 16px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.bar strong {
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 15px;
}

.back {
  border: 0;
  background: transparent;
  color: var(--ink-secondary);
  cursor: pointer;
  text-align: left;
}

.back:hover {
  color: var(--brand);
}

.wrap {
  width: min(920px, calc(100% - 32px));
  margin: 24px auto 64px;
}

.hero {
  display: grid;
  grid-template-columns: 168px 1fr;
  gap: 28px;
  padding: 24px;
  border-radius: 18px;
  background: #fff;
  box-shadow: var(--shadow-soft);
}

.cover {
  width: 168px;
  height: 224px;
  object-fit: cover;
  border-radius: 10px;
  box-shadow: 0 12px 28px rgba(26, 26, 26, 0.16);
  background: #ece7e0;
}

.cover--fallback {
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 48px;
  font-family: var(--font-display);
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
}

.kicker {
  font-size: 12px;
  letter-spacing: 0.14em;
  color: var(--ink-muted);
}

.hero h1 {
  margin-top: 6px;
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 400;
  letter-spacing: 0.06em;
}

.author {
  margin-top: 6px;
  color: var(--ink-secondary);
}

.desc {
  margin-top: 12px;
  font-size: 14px;
  line-height: 1.85;
  color: #555;
  white-space: pre-line;
}

.stats,
.tags,
.actions,
.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.stats {
  margin-top: 14px;
  font-size: 12px;
  color: var(--ink-muted);
}

.tag-row {
  margin-top: 12px;
}

.tag-row em {
  flex-shrink: 0;
  font-style: normal;
  font-size: 12px;
  color: var(--ink-muted);
}

.tags span {
  padding: 3px 10px;
  border-radius: 999px;
  background: #f3efe8;
  color: #6b6570;
  font-size: 12px;
}

.actions {
  margin-top: 18px;
  gap: 14px;
}

.btn-primary {
  height: 40px;
  padding: 0 22px;
  border: 0;
  border-radius: 999px;
  background: var(--brand);
  color: #fff;
  cursor: pointer;
}

.btn-primary:hover {
  background: var(--brand-dark);
}

.btn-fav {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 40px;
  padding: 0 18px;
  border-radius: 999px;
  border: 1px solid rgba(255, 77, 58, 0.35);
  background: #fff;
  color: var(--brand);
  cursor: pointer;
}

.btn-fav .star {
  display: block;
  flex-shrink: 0;
}

.btn-fav:hover,
.btn-fav.is-on {
  background: var(--brand-soft);
}

.resume {
  grid-column: 1 / -1;
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--ink-muted);
}

.list {
  margin-top: 18px;
  padding: 8px 8px 16px;
  border-radius: 18px;
  background: #fff;
  box-shadow: var(--shadow-soft);
}

.list > header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 16px 16px 8px;
}

.list h2 {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 400;
}

.list header span,
.empty {
  color: var(--ink-muted);
  font-size: 13px;
}

.empty {
  padding: 28px 16px;
}

.list ol {
  list-style: none;
}

.list li button {
  display: grid;
  grid-template-columns: 48px 1fr auto;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 12px 16px;
  border: 0;
  border-radius: 12px;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.list li button:hover,
.list li button.is-current {
  background: rgba(255, 77, 58, 0.06);
}

.list li button.is-current span {
  color: var(--brand);
}

.list em {
  font-style: normal;
  font-family: var(--font-serif);
  color: var(--ink-muted);
}

.list span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.list small {
  color: var(--ink-muted);
  font-size: 12px;
}

.wrap--empty {
  padding-top: 80px;
  text-align: center;
  color: var(--ink-muted);
}

@media (max-width: 720px) {
  .hero {
    grid-template-columns: 1fr;
    justify-items: center;
    text-align: center;
  }

  .hero__meta,
  .stats,
  .tags,
  .actions {
    justify-content: center;
  }
}
</style>
