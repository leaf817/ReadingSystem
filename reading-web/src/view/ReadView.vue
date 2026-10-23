<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  fetchBook,
  fetchBookChapter,
  fetchBookChapters,
  type ChapterContent,
  type ChapterSummary,
} from '@/api/book'
import { ApiError } from '@/api/request'
import { books, type Book } from '@/data/mockBooks'
import { chapterTitleOf, useReadingStore } from '@/stores/reading'
import { useAuthStore } from '@/stores/auth'

type ThemeId = 'paper' | 'eye' | 'night'

const THEMES: { id: ThemeId; label: string }[] = [
  { id: 'paper', label: '纸感' },
  { id: 'eye', label: '护眼' },
  { id: 'night', label: '夜间' },
]
const FONT_MIN = 16
const FONT_MAX = 24
const THEME_KEY = 'yuejie-reader-theme'
const FONT_KEY = 'yuejie-reader-font'

const route = useRoute()
const router = useRouter()
const reading = useReadingStore()
const auth = useAuthStore()

const bookId = computed(() => Number(route.params.id))
const book = ref<Book | null>(null)
const chapter = ref<ChapterContent | null>(null)
const catalog = ref<ChapterSummary[]>([])
const loadError = ref('')
const loading = ref(false)
const catalogOpen = ref(false)
const theme = ref<ThemeId>('paper')
const fontSize = ref(18)

const chapterIndex = computed(() => {
  const q = Number(route.query.chapter)
  if (Number.isFinite(q) && q > 0) return Math.floor(q)
  if (reading.last?.bookId === bookId.value) return reading.last.chapterIndex
  return 1
})

const chapterTitle = computed(() => {
  if (chapter.value?.title) return chapter.value.title
  return book.value ? chapterTitleOf(book.value, chapterIndex.value) : ''
})

const content = computed(() => {
  if (chapter.value?.content) return chapter.value.content
  if (!book.value) return ''
  const title = book.value.title
  return [
    book.value.desc,
    `夜色渐深，${book.value.author} 笔下的世界缓缓展开。`,
    `关于《${title}》的这一章，只是故事长河中的一截浪花。`,
    '你合上书页、离开阅读器后，这里会成为「继续阅读」里的那一部。',
  ].join('\n')
})

const paragraphs = computed(() =>
  content.value
    .split(/\n+/)
    .map((line) => line.replace(/^[\u3000\s]+/, '').trim())
    .filter(Boolean),
)

const progress = computed(() => {
  if (!chapter.value?.total) return 0
  return Math.round((chapter.value.seq / chapter.value.total) * 100)
})

function persist() {
  if (!book.value) return
  reading.remember(book.value, chapterIndex.value, chapterTitle.value)
}

function goChapter(seq: number | null) {
  if (seq == null) return
  catalogOpen.value = false
  void router.replace({ query: { chapter: String(seq) } })
}

function exit() {
  persist()
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push(auth.isLoggedIn && auth.role === 'reader' ? '/mine' : '/')
}

function setTheme(id: ThemeId) {
  theme.value = id
  localStorage.setItem(THEME_KEY, id)
}

function bumpFont(delta: number) {
  fontSize.value = Math.min(FONT_MAX, Math.max(FONT_MIN, fontSize.value + delta))
  localStorage.setItem(FONT_KEY, String(fontSize.value))
}

async function loadCatalog() {
  if (!book.value || catalog.value.length) return
  try {
    catalog.value = await fetchBookChapters(bookId.value)
  } catch {
    catalog.value = []
  }
}

async function toggleCatalog() {
  catalogOpen.value = !catalogOpen.value
  if (catalogOpen.value) await loadCatalog()
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
      chapter.value = await fetchBookChapter(bookId.value, chapterIndex.value)
    } catch (error) {
      chapter.value = null
      if (error instanceof ApiError && error.code !== 404) {
        loadError.value = error.message
      }
    }
    persist()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } finally {
    loading.value = false
  }
}

function onKey(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    catalogOpen.value = false
    return
  }
  const tag = (event.target as HTMLElement | null)?.tagName
  if (tag === 'INPUT' || tag === 'TEXTAREA') return
  if (event.key === 'ArrowLeft') goChapter(chapter.value?.prevSeq ?? null)
  if (event.key === 'ArrowRight') goChapter(chapter.value?.nextSeq ?? null)
}

onMounted(() => {
  const savedTheme = localStorage.getItem(THEME_KEY)
  if (savedTheme === 'paper' || savedTheme === 'eye' || savedTheme === 'night') {
    theme.value = savedTheme
  }
  const savedFont = Number(localStorage.getItem(FONT_KEY))
  if (Number.isFinite(savedFont) && savedFont >= FONT_MIN && savedFont <= FONT_MAX) {
    fontSize.value = savedFont
  }
  reading.hydrate()
  window.addEventListener('keydown', onKey)
  void load()
})

onBeforeUnmount(() => {
  persist()
  window.removeEventListener('keydown', onKey)
})

watch(bookId, () => {
  catalog.value = []
})

watch([bookId, chapterIndex], () => {
  void load()
})
</script>

<template>
  <div class="reader" :class="`is-${theme}`">
    <header class="bar">
      <button type="button" class="back" @click="exit">‹ 退出</button>
      <div v-if="book" class="bar__meta">
        <strong>{{ book.title }}</strong>
        <span>{{ chapterTitle }}</span>
      </div>
      <div class="tools">
        <button type="button" class="tool" title="目录" @click="toggleCatalog">目录</button>
        <button type="button" class="tool" title="减小字号" :disabled="fontSize <= FONT_MIN" @click="bumpFont(-2)">A-</button>
        <button type="button" class="tool" title="增大字号" :disabled="fontSize >= FONT_MAX" @click="bumpFont(2)">A+</button>
        <button
          v-for="item in THEMES"
          :key="item.id"
          type="button"
          class="swatch"
          :class="[`swatch--${item.id}`, { 'is-on': theme === item.id }]"
          :title="item.label"
          @click="setTheme(item.id)"
        />
      </div>
      <div class="bar__progress" :style="{ width: `${progress}%` }" />
    </header>

    <main v-if="book" class="sheet">
      <p class="kicker">{{ book.author }}  ·  {{ book.category }}</p>
      <h1>{{ chapterTitle }}</h1>
      <p class="meta">
        <span v-if="chapter">第 {{ chapter.seq }} / {{ chapter.total }} 章</span>
        <span v-if="chapter">{{ chapter.wordCount.toLocaleString() }} 字</span>
        <span v-else>暂无正文</span>
      </p>
      <div class="ornament" aria-hidden="true">✦</div>

      <p v-if="loading" class="status">正在铺开这一章…</p>
      <p v-else-if="loadError" class="status">{{ loadError }}</p>
      <article class="body" :style="{ fontSize: `${fontSize}px` }">
        <p v-for="(para, index) in paragraphs" :key="index">{{ para }}</p>
      </article>

      <nav v-if="chapter" class="pager">
        <button type="button" class="pager__btn" :disabled="!chapter.prevSeq" @click="goChapter(chapter.prevSeq)">
          上一章
        </button>
        <button type="button" class="pager__mid" @click="toggleCatalog">目录</button>
        <button type="button" class="pager__btn is-next" :disabled="!chapter.nextSeq" @click="goChapter(chapter.nextSeq)">
          下一章
        </button>
      </nav>
      <p class="footnote">左右方向键也可翻章 · 退出后可在「继续阅读」回来</p>
    </main>

    <main v-else class="sheet sheet--empty">
      <p>未找到该作品</p>
      <button type="button" class="link" @click="exit">返回</button>
    </main>

    <div v-if="catalogOpen" class="mask" @click="catalogOpen = false" />
    <aside class="catalog" :class="{ 'is-open': catalogOpen }">
      <header>
        <strong>目录</strong>
        <button type="button" @click="catalogOpen = false">关闭</button>
      </header>
      <p v-if="book" class="catalog__book">{{ book.title }}</p>
      <ul v-if="catalog.length">
        <li v-for="item in catalog" :key="item.seq">
          <button
            type="button"
            :class="{ 'is-current': item.seq === chapterIndex }"
            @click="goChapter(item.seq)"
          >
            <span>{{ item.title }}</span>
            <em>{{ item.wordCount }}字</em>
          </button>
        </li>
      </ul>
      <p v-else class="catalog__empty">暂无章节</p>
    </aside>
  </div>
</template>

<style scoped>
.reader {
  --paper: #f4efe6;
  --sheet: #fbf7f0;
  --ink-read: #2c271f;
  --muted: #8a8174;
  --line-soft: rgba(90, 70, 50, 0.12);
  --bar-bg: rgba(251, 247, 240, 0.92);
  min-height: 100vh;
  color: var(--ink-read);
  background:
    radial-gradient(ellipse 70% 40% at 50% 0%, rgba(255, 77, 58, 0.07), transparent 55%),
    repeating-linear-gradient(90deg, rgba(90, 70, 50, 0.025) 0 1px, transparent 1px 48px),
    var(--paper);
  transition: background 0.25s ease, color 0.25s ease;
}

.reader.is-eye {
  --paper: #cfe3c8;
  --sheet: #e7f0df;
  --ink-read: #2b3826;
  --muted: #6d7d66;
  --line-soft: rgba(50, 80, 40, 0.12);
  --bar-bg: rgba(231, 240, 223, 0.92);
}

.reader.is-night {
  --paper: #161412;
  --sheet: #1e1b18;
  --ink-read: #d8d0c4;
  --muted: #8d8478;
  --line-soft: rgba(255, 255, 255, 0.08);
  --bar-bg: rgba(22, 20, 18, 0.92);
}

.bar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: grid;
  grid-template-columns: 88px 1fr auto;
  align-items: center;
  min-height: 56px;
  padding: 8px 16px 10px;
  background: var(--bar-bg);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--line-soft);
}

.bar__progress {
  position: absolute;
  left: 0;
  bottom: 0;
  height: 2px;
  background: var(--brand);
}

.back,
.tool,
.catalog header button,
.link {
  border: 0;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
}

.back {
  text-align: left;
  padding: 8px 4px;
  font-size: 14px;
}

.back:hover,
.tool:hover,
.catalog header button:hover,
.link:hover {
  color: var(--brand);
}

.bar__meta {
  min-width: 0;
  text-align: center;
}

.bar__meta strong,
.bar__meta span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bar__meta strong {
  font-size: 14px;
  font-weight: 600;
}

.bar__meta span {
  margin-top: 2px;
  font-size: 12px;
  color: var(--muted);
}

.tools {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
}

.tool {
  min-width: 36px;
  height: 28px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 12px;
}

.tool:disabled {
  opacity: 0.35;
  cursor: default;
}

.swatch {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1px solid rgba(0, 0, 0, 0.12);
  cursor: pointer;
}

.swatch--paper {
  background: #f4efe6;
}

.swatch--eye {
  background: #cfe3c8;
}

.swatch--night {
  background: #2a2622;
}

.swatch.is-on {
  outline: 2px solid var(--brand);
  outline-offset: 2px;
}

.sheet {
  width: min(760px, calc(100% - 28px));
  margin: 24px auto 72px;
  padding: 36px 40px 40px;
  border-radius: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.35), transparent 18%),
    var(--sheet);
  box-shadow:
    0 18px 50px rgba(40, 28, 16, 0.1),
    inset 0 0 0 1px var(--line-soft);
}

.kicker,
.meta,
.footnote,
.status,
.catalog__empty,
.catalog__book {
  color: var(--muted);
}

.kicker {
  font-size: 12px;
  letter-spacing: 0.18em;
}

.sheet h1 {
  margin-top: 10px;
  font-family: var(--font-display);
  font-size: 30px;
  font-weight: 400;
  letter-spacing: 0.08em;
  line-height: 1.35;
}

.meta {
  display: flex;
  gap: 14px;
  margin-top: 10px;
  font-size: 12px;
}

.ornament {
  margin: 22px 0 8px;
  color: rgba(255, 77, 58, 0.55);
  text-align: center;
  letter-spacing: 0.4em;
}

.status {
  margin-top: 24px;
  font-size: 13px;
}

.body {
  margin-top: 8px;
  font-family: var(--font-serif);
  line-height: 2.05;
}

.body p {
  margin: 0 0 0.9em;
  text-indent: 2em;
  text-align: justify;
  word-break: break-word;
}

.pager {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 10px;
  margin-top: 40px;
}

.pager__btn,
.pager__mid {
  height: 40px;
  border-radius: 999px;
  border: 1px solid var(--line-soft);
  background: transparent;
  color: var(--ink-read);
  cursor: pointer;
}

.pager__btn.is-next {
  justify-self: end;
  min-width: 108px;
}

.pager__btn:first-child {
  justify-self: start;
  min-width: 108px;
}

.pager__mid {
  min-width: 72px;
  color: var(--muted);
}

.pager__btn:hover:not(:disabled),
.pager__mid:hover {
  color: var(--brand);
  border-color: rgba(255, 77, 58, 0.35);
}

.pager__btn:disabled {
  opacity: 0.35;
  cursor: default;
}

.footnote {
  margin-top: 18px;
  font-size: 12px;
  text-align: center;
}

.sheet--empty {
  text-align: center;
  padding-top: 80px;
}

.mask {
  position: fixed;
  inset: 0;
  z-index: 30;
  background: rgba(20, 14, 10, 0.35);
}

.catalog {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 40;
  width: min(360px, 88vw);
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--sheet);
  box-shadow: -16px 0 40px rgba(0, 0, 0, 0.16);
  transform: translateX(104%);
  transition: transform 0.24s ease;
}

.catalog.is-open {
  transform: none;
}

.catalog header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 18px 8px;
}

.catalog__book {
  padding: 0 18px 12px;
  font-size: 12px;
}

.catalog ul {
  overflow: auto;
  padding: 0 10px 24px;
  list-style: none;
}

.catalog li button {
  display: flex;
  width: 100%;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 8px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.catalog li button span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.catalog li button em {
  flex-shrink: 0;
  font-style: normal;
  font-size: 11px;
  color: var(--muted);
}

.catalog li button:hover,
.catalog li button.is-current {
  background: rgba(255, 77, 58, 0.08);
  color: var(--brand);
}

@media (max-width: 720px) {
  .bar {
    grid-template-columns: 64px 1fr;
    gap: 4px;
  }

  .tools {
    grid-column: 1 / -1;
    justify-content: flex-end;
  }

  .sheet {
    padding: 24px 18px 32px;
    margin-top: 12px;
  }

  .sheet h1 {
    font-size: 24px;
  }
}
</style>
