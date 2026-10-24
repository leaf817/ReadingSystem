<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { fetchBook, fetchBookChapters, type ChapterSummary } from '@/api/book'
import { books, type Book } from '@/data/mockBooks'
import { useReadingStore } from '@/stores/reading'
import { useShelfStore } from '@/stores/shelf'
import { useBookmarkStore } from '@/stores/bookmark'
import { useAuthStore } from '@/stores/auth'
import { contentTagsOf, fieldValuesOf } from '@/utils/bookSearch'

const route = useRoute()
const router = useRouter()
const reading = useReadingStore()
const shelf = useShelfStore()
const bookmarks = useBookmarkStore()
const auth = useAuthStore()
const favorited = computed(() => (book.value ? shelf.has(book.value.id) : false))

function chapterBookmarked(seq: number) {
  return book.value ? bookmarks.has(book.value.id, seq) : false
}

const bookId = computed(() => Number(route.params.id))
const book = ref<Book | null>(null)
const chapters = ref<ChapterSummary[]>([])
const loading = ref(false)
const loadError = ref('')
const reviewCount = ref(0)
const tip = ref('')
let tipTimer = 0

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

const contentTags = computed(() => (book.value ? contentTagsOf(book.value) : []))

const extraTags = computed(() => (book.value ? fieldValuesOf(book.value, '其他') : []))

const protagonistNames = computed(() => (book.value ? fieldValuesOf(book.value, '主角') : []))

const supportingNames = computed(() => (book.value ? fieldValuesOf(book.value, '配角') : []))

function goRead(seq: number) {
  if (!book.value) return
  reading.remember(
    book.value,
    seq,
    chapters.value.find((item) => item.seq === seq)?.title,
    chapters.value.length,
  )
  void router.push({ name: 'read', params: { id: String(book.value.id) }, query: { chapter: String(seq) } })
}

function toggleFavorite() {
  if (!book.value) return
  shelf.toggle(book.value)
}

function openReviews() {
  tip.value = '书评功能暂未开放'
  window.clearTimeout(tipTimer)
  tipTimer = window.setTimeout(() => {
    tip.value = ''
  }, 1600)
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
  bookmarks.hydrate()
  void load()
})

onBeforeUnmount(() => {
  window.clearTimeout(tipTimer)
})

watch(bookId, () => {
  void load()
})
</script>

<template>
  <div class="catalog-page">
    <AppHeader show-back :page-title="book?.title || '作品目录'" />

    <main v-if="book" class="wrap">
      <section class="hero">
        <img v-if="book.cover" class="cover" :src="book.cover" :alt="book.title" />
        <div v-else class="cover cover--fallback">{{ book.title.slice(0, 1) }}</div>
        <div class="hero__meta">
          <div class="hero__top">
            <p class="kicker">{{ book.category }} · {{ book.status }}</p>
            <button type="button" class="rating" @click="openReviews">
              <strong>0.0</strong>
              <span class="rating__stars" aria-hidden="true">
                <svg v-for="n in 5" :key="n" viewBox="0 0 24 24" width="13" height="13">
                  <path
                    d="M12 2.6 14.7 8.2l6.2.9-4.5 4.4 1.1 6.2L12 16.8 6.5 19.7 7.6 13.5 3.1 9.1l6.2-.9L12 2.6z"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.6"
                    stroke-linejoin="round"
                  />
                </svg>
              </span>
              <em>暂无评分</em>
            </button>
          </div>
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
          <div v-if="protagonistNames.length" class="tag-row">
            <em>主角</em>
            <div class="tags">
              <span v-for="name in protagonistNames" :key="name">{{ name }}</span>
            </div>
          </div>
          <div v-if="supportingNames.length" class="tag-row">
            <em>配角</em>
            <div class="tags">
              <span v-for="name in supportingNames" :key="name">{{ name }}</span>
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
            <button type="button" class="btn-fav" @click="openReviews">
              <svg class="star" viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
                <path
                  d="M5 6.5h14a1.5 1.5 0 0 1 1.5 1.5v8.2a1.5 1.5 0 0 1-1.5 1.5H10l-3.8 2.6V17.7H5A1.5 1.5 0 0 1 3.5 16.2V8A1.5 1.5 0 0 1 5 6.5z"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.6"
                  stroke-linejoin="round"
                />
              </svg>
              书评 {{ reviewCount }}
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
              <svg
                v-if="chapterBookmarked(item.seq)"
                class="mark"
                viewBox="0 0 24 24"
                width="14"
                height="14"
                aria-label="已加书签"
              >
                <path
                  d="M6 3.5h12a1 1 0 0 1 1 1V20l-7-3.4L6 20V4.5a1 1 0 0 1 1-1z"
                  fill="currentColor"
                />
              </svg>
              <i v-else class="mark-slot" />
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

    <p v-if="tip" class="page-tip" role="status">{{ tip }}</p>
  </div>
</template>

<style scoped>
.catalog-page {
  min-height: 100vh;
  background:
    radial-gradient(ellipse 70% 40% at 50% 0%, rgba(255, 77, 58, 0.07), transparent 55%),
    var(--page);
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
  background: var(--surface);
  box-shadow: var(--shadow-soft);
  border: 1px solid var(--line);
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

.hero__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.kicker {
  font-size: 12px;
  letter-spacing: 0.14em;
  color: var(--ink-muted);
}

.rating {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  margin: -2px 0 0;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--brand);
  cursor: pointer;
  flex-shrink: 0;
}

.rating strong {
  font-size: 22px;
  font-weight: 600;
  line-height: 1;
  letter-spacing: 0.04em;
}

.rating__stars {
  display: flex;
  gap: 1px;
}

.rating em {
  font-style: normal;
  font-size: 11px;
  color: var(--ink-muted);
}

.rating:hover em {
  color: var(--brand);
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
  color: var(--ink-secondary);
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
  background: var(--chip);
  color: var(--ink-secondary);
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
  background: var(--surface);
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

.page-tip {
  position: fixed;
  left: 50%;
  bottom: 28px;
  z-index: 40;
  transform: translateX(-50%);
  padding: 8px 16px;
  border-radius: 999px;
  background: rgba(28, 24, 20, 0.82);
  color: #fff;
  font-size: 13px;
  pointer-events: none;
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
  background: var(--surface);
  box-shadow: var(--shadow-soft);
  border: 1px solid var(--line);
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
  grid-template-columns: 48px minmax(0, 1fr) 16px auto;
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

.mark {
  display: block;
  color: var(--brand);
}

.mark-slot {
  width: 14px;
  height: 14px;
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
  .hero__top,
  .stats,
  .tags,
  .actions {
    justify-content: center;
  }

  .rating {
    align-items: center;
  }
}
</style>
