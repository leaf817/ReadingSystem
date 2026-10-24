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
import { useShelfStore } from '@/stores/shelf'
import { useBookmarkStore } from '@/stores/bookmark'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'

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
const SLIDE_KEY = 'yuejie-reader-slide-speed'
const SLIDE_MIN = 1
const SLIDE_MAX = 10
const BRIGHT_KEY = 'yuejie-reader-brightness'
const FOLLOW_BRIGHT_KEY = 'yuejie-reader-brightness-follow'
const BRIGHT_MIN = 20
const BRIGHT_MAX = 100

const route = useRoute()
const router = useRouter()
const reading = useReadingStore()
const shelf = useShelfStore()
const bookmarks = useBookmarkStore()
const auth = useAuthStore()
const moreOpen = ref(false)

const bookId = computed(() => Number(route.params.id))
const book = ref<Book | null>(null)
const chapter = ref<ChapterContent | null>(null)
const catalog = ref<ChapterSummary[]>([])
const loadError = ref('')
const loading = ref(false)
const catalogOpen = ref(false)
const chromeVisible = ref(false)
const userMenuOpen = ref(false)
const switchOpen = ref(false)
const themeOpen = ref(false)
const fontOpen = ref(false)
const autoSlide = ref(false)
const slideBarVisible = ref(false)
const slideSpeed = ref(5)
const listenTip = ref('')
let listenTipTimer = 0
let slideTimer = 0
const sliderSeq = ref(1)
const originSeq = ref(1)
const theme = ref<ThemeId>('paper')
const fontSize = ref(18)
const brightness = ref(100)
const followSystemBright = ref(false)

const dimOpacity = computed(() => {
  if (followSystemBright.value) return 0
  return ((BRIGHT_MAX - brightness.value) / (BRIGHT_MAX - BRIGHT_MIN)) * 0.62
})

const displayName = computed(() => auth.user?.displayName?.trim() || auth.user?.username || '用户')
const avatarText = computed(() => displayName.value.slice(0, 1) || '阅')
const favorited = computed(() => (book.value ? shelf.has(book.value.id) : false))
const bookmarked = computed(() =>
  book.value && chapter.value ? bookmarks.has(book.value.id, chapter.value.seq) : false,
)

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

const chapterTotal = computed(() => chapter.value?.total || catalog.value.length || 1)

const switchLabel = computed(() => {
  const seq = switchOpen.value ? sliderSeq.value : (chapter.value?.seq || chapterIndex.value)
  const fromCatalog = catalog.value.find((item) => item.seq === seq)?.title || ''
  const title = (fromCatalog || (seq === chapterIndex.value ? chapterTitle.value : '')).trim()
  if (!title) return `第${seq}章`
  if (/第.+章/.test(title)) return title
  return `第${seq}章 ${title}`
})

function persist() {
  if (!book.value) return
  const seq = chapter.value?.seq || chapterIndex.value
  const title = chapter.value?.title || chapterTitle.value
  const total = chapter.value?.total || catalog.value.length || book.value.chapters
  reading.remember(book.value, seq, title, total)
}

function goChapter(seq: number | null) {
  if (seq == null) return
  const next = Math.min(chapterTotal.value, Math.max(1, Math.round(seq)))
  catalogOpen.value = false
  sliderSeq.value = next
  void router.replace({ query: { chapter: String(next) } })
}

function closeToolPanels() {
  switchOpen.value = false
  themeOpen.value = false
  fontOpen.value = false
}

async function toggleSwitchPanel() {
  const next = !switchOpen.value
  closeToolPanels()
  switchOpen.value = next
  if (switchOpen.value) {
    originSeq.value = chapterIndex.value
    sliderSeq.value = chapterIndex.value
    catalogOpen.value = false
    await loadCatalog()
  }
}

function toggleThemePanel() {
  const next = !themeOpen.value
  closeToolPanels()
  themeOpen.value = next
  if (themeOpen.value) catalogOpen.value = false
}

function toggleFontPanel() {
  const next = !fontOpen.value
  closeToolPanels()
  fontOpen.value = next
  if (fontOpen.value) catalogOpen.value = false
}

function onBrightness(event: Event) {
  if (followSystemBright.value) return
  const value = Number((event.target as HTMLInputElement).value)
  brightness.value = Math.min(BRIGHT_MAX, Math.max(BRIGHT_MIN, value))
  localStorage.setItem(BRIGHT_KEY, String(brightness.value))
}

function toggleFollowSystemBright() {
  followSystemBright.value = !followSystemBright.value
  localStorage.setItem(FOLLOW_BRIGHT_KEY, followSystemBright.value ? '1' : '0')
}

function onSwitchSlide(event: Event) {
  sliderSeq.value = Number((event.target as HTMLInputElement).value)
}

function commitSwitchSlide() {
  if (sliderSeq.value !== chapterIndex.value) goChapter(sliderSeq.value)
}

function restartChapter() {
  goChapter(originSeq.value)
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

const appTheme = useThemeStore()
const themeMenuOpen = ref(false)
let themeMenuTimer = 0

function toggleUserMenu() {
  userMenuOpen.value = !userMenuOpen.value
  if (!userMenuOpen.value) {
    window.clearTimeout(themeMenuTimer)
    themeMenuOpen.value = false
  }
}

function openThemeMenu() {
  window.clearTimeout(themeMenuTimer)
  themeMenuOpen.value = true
}

function closeThemeMenuSoon() {
  window.clearTimeout(themeMenuTimer)
  themeMenuTimer = window.setTimeout(() => {
    themeMenuOpen.value = false
  }, 220)
}

function pickAppTheme(mode: 'light' | 'dark') {
  window.clearTimeout(themeMenuTimer)
  appTheme.setMode(mode)
  themeMenuOpen.value = false
}

const accountLabel = computed(() => (auth.role === 'reader' ? '个人中心' : '工作台'))

function goAccount() {
  userMenuOpen.value = false
  if (!auth.role) {
    void router.push({ name: 'login' })
    return
  }
  if (auth.role === 'reader') {
    void router.push('/mine')
    return
  }
  void router.push(auth.roleHome[auth.role])
}

function logoutUser() {
  userMenuOpen.value = false
  persist()
  auth.logout()
  void router.push({ name: 'login' })
}

function toggleFavorite() {
  if (!book.value) return
  shelf.toggle(book.value)
}

function toggleBookmark() {
  if (!book.value || !chapter.value) return
  bookmarks.toggle(book.value.id, chapter.value.seq, chapter.value.title)
}

function goBookHome() {
  moreOpen.value = false
  if (!book.value) return
  void router.push({ name: 'book', params: { id: String(book.value.id) } })
}

async function openChapterList() {
  moreOpen.value = false
  await toggleCatalog()
}

function bumpFont(delta: number) {
  fontSize.value = Math.min(FONT_MAX, Math.max(FONT_MIN, fontSize.value + delta))
  localStorage.setItem(FONT_KEY, String(fontSize.value))
}

function onFontSlide(event: Event) {
  fontSize.value = Number((event.target as HTMLInputElement).value)
  localStorage.setItem(FONT_KEY, String(fontSize.value))
}

function showFootTip(message: string) {
  listenTip.value = message
  window.clearTimeout(listenTipTimer)
  listenTipTimer = window.setTimeout(() => {
    listenTip.value = ''
  }, 1600)
}

function stopAutoSlide() {
  window.clearInterval(slideTimer)
  slideTimer = 0
}

function startAutoSlide() {
  stopAutoSlide()
  const speed = slideSpeed.value
  const delay = Math.max(8, 78 - speed * 7)
  const step = speed >= 10 ? 5 : speed >= 8 ? 3 : speed >= 6 ? 2 : 1
  slideTimer = window.setInterval(() => {
    const max = document.documentElement.scrollHeight - window.innerHeight
    if (window.scrollY >= max - 2) {
      if (chapter.value?.nextSeq) goChapter(chapter.value.nextSeq)
      else endAutoSlide()
      return
    }
    window.scrollBy(0, step)
  }, delay)
}

function toggleAutoSlide() {
  if (autoSlide.value) {
    endAutoSlide()
    return
  }
  autoSlide.value = true
  slideBarVisible.value = false
  fontOpen.value = false
  chromeVisible.value = false
  startAutoSlide()
}

function endAutoSlide() {
  autoSlide.value = false
  slideBarVisible.value = false
  stopAutoSlide()
}

function setSlideSpeed(value: number) {
  slideSpeed.value = Math.min(SLIDE_MAX, Math.max(SLIDE_MIN, Math.round(value)))
  localStorage.setItem(SLIDE_KEY, String(slideSpeed.value))
  if (autoSlide.value) startAutoSlide()
}

function onSlideSpeed(event: Event) {
  setSlideSpeed(Number((event.target as HTMLInputElement).value))
}

function toggleListen() {
  showFootTip('听书功能暂未开放')
}

function toggleAdvanced() {
  showFootTip('高级选项尚未开发')
}

function onReaderClick(event: MouseEvent) {
  if (event.button !== 0) return
  if (catalogOpen.value) return
  const selected = window.getSelection()?.toString()
  if (selected) return
  const target = event.target as HTMLElement | null
  if (target?.closest('.bar, .bar-cover, .foot, .catalog, .mask, .slide-bar, .pager')) return
  userMenuOpen.value = false
  moreOpen.value = false
  if (autoSlide.value) {
    slideBarVisible.value = !slideBarVisible.value
    return
  }
  chromeVisible.value = !chromeVisible.value
  if (!chromeVisible.value) {
    moreOpen.value = false
    closeToolPanels()
  }
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
  if (catalogOpen.value) {
    chromeVisible.value = true
    closeToolPanels()
    await loadCatalog()
  }
}

async function load() {
  const id = bookId.value
  const seq = chapterIndex.value
  if (!Number.isFinite(id) || id <= 0) {
    book.value = null
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    try {
      book.value = await fetchBook(id)
    } catch {
      book.value = books.find((item) => item.id === id) || null
    }
    if (!book.value) return
    try {
      const data = await fetchBookChapter(id, seq)
      if (id !== bookId.value || seq !== chapterIndex.value) return
      chapter.value = data
    } catch (error) {
      if (id !== bookId.value || seq !== chapterIndex.value) return
      chapter.value = null
      if (error instanceof ApiError && error.code !== 404) {
        loadError.value = error.message
      }
    }
    persist()
    window.scrollTo({ top: 0, behavior: autoSlide.value ? 'auto' : 'smooth' })
  } finally {
    loading.value = false
  }
}

function onKey(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    if (catalogOpen.value) {
      catalogOpen.value = false
      return
    }
    if (userMenuOpen.value) {
      userMenuOpen.value = false
      return
    }
    if (moreOpen.value) {
      moreOpen.value = false
      return
    }
    if (switchOpen.value) {
      switchOpen.value = false
      return
    }
    if (themeOpen.value) {
      themeOpen.value = false
      return
    }
    if (fontOpen.value) {
      fontOpen.value = false
      return
    }
    if (autoSlide.value) {
      endAutoSlide()
      return
    }
    chromeVisible.value = false
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
  const savedBright = Number(localStorage.getItem(BRIGHT_KEY))
  if (Number.isFinite(savedBright) && savedBright >= BRIGHT_MIN && savedBright <= BRIGHT_MAX) {
    brightness.value = savedBright
  }
  followSystemBright.value = localStorage.getItem(FOLLOW_BRIGHT_KEY) === '1'
  const savedSpeed = Number(localStorage.getItem(SLIDE_KEY))
  if (Number.isFinite(savedSpeed) && savedSpeed >= SLIDE_MIN && savedSpeed <= SLIDE_MAX) {
    slideSpeed.value = savedSpeed
  }
  reading.hydrate()
  shelf.hydrate()
  bookmarks.hydrate()
  window.addEventListener('keydown', onKey)
  void load()
})

onBeforeUnmount(() => {
  persist()
  stopAutoSlide()
  window.clearTimeout(listenTipTimer)
  window.clearTimeout(themeMenuTimer)
  window.removeEventListener('keydown', onKey)
})

watch(bookId, () => {
  catalog.value = []
  closeToolPanels()
  autoSlide.value = false
  slideBarVisible.value = false
  stopAutoSlide()
})

watch(chapterIndex, (seq) => {
  sliderSeq.value = seq
})

watch([bookId, chapterIndex], () => {
  void load()
})
</script>

<template>
  <div class="reader" :class="[`is-${theme}`, { 'chrome-on': chromeVisible }]" @click="onReaderClick">
    <div class="dim" :style="{ opacity: dimOpacity }" aria-hidden="true" />
    <header class="bar">
      <button type="button" class="back" @click="exit">‹ 退出</button>
      <div v-if="book" class="bar__meta">
        <strong>{{ book.title }}</strong>
        <span>{{ chapterTitle }}</span>
      </div>
      <div v-if="auth.isLoggedIn" class="skin" @click.stop>
        <button type="button" class="skin__btn" :class="{ 'is-open': userMenuOpen }" @click="toggleUserMenu">
          <span v-if="auth.user?.avatar" class="skin__avatar">
            <img :src="auth.user.avatar" :alt="displayName" />
          </span>
          <span v-else class="skin__mark" :class="`is-${theme}`">{{ avatarText }}</span>
          <span class="skin__name">{{ displayName }}</span>
          <svg class="skin__caret" viewBox="0 0 12 12" width="11" height="11" aria-hidden="true">
            <path d="M2.5 4.2L6 7.8l3.5-3.6" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
          </svg>
        </button>
        <div v-if="userMenuOpen" class="skin__menu">
          <button type="button" @click="goAccount">{{ accountLabel }}</button>
          <div class="theme-item" @mouseenter="openThemeMenu" @mouseleave="closeThemeMenuSoon">
            <button type="button" @click.stop="themeMenuOpen = !themeMenuOpen">
              主题: {{ appTheme.label }}
            </button>
            <div v-if="themeMenuOpen" class="theme-flyout">
              <button type="button" :class="{ 'is-on': appTheme.mode === 'dark' }" @click="pickAppTheme('dark')">
                深色
              </button>
              <button type="button" :class="{ 'is-on': appTheme.mode === 'light' }" @click="pickAppTheme('light')">
                浅色
              </button>
            </div>
          </div>
          <button type="button" @click="logoutUser">退出登录</button>
        </div>
      </div>
      <button v-else type="button" class="skin__btn" @click.stop="router.push({ name: 'login' })">登录</button>
    </header>

    <header class="bar-cover" :class="{ 'is-on': chromeVisible }" @click.stop>
      <button type="button" class="back" @click="exit">‹ 退出</button>
      <div class="cover-actions">
        <button type="button" class="cover-btn" :class="{ 'is-on': favorited }" @click="toggleFavorite">
          <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
            <path
              d="M12 2.6 14.7 8.2l6.2.9-4.5 4.4 1.1 6.2L12 16.8 6.5 19.7 7.6 13.5 3.1 9.1l6.2-.9L12 2.6z"
              :fill="favorited ? 'currentColor' : 'none'"
              stroke="currentColor"
              stroke-width="1.6"
              stroke-linejoin="round"
            />
          </svg>
          收藏
        </button>
        <button type="button" class="cover-btn" :class="{ 'is-on': bookmarked }" @click="toggleBookmark">
          <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
            <path
              d="M6 3.5h12a1 1 0 0 1 1 1V20l-7-3.4L6 20V4.5a1 1 0 0 1 1-1z"
              :fill="bookmarked ? 'currentColor' : 'none'"
              stroke="currentColor"
              stroke-width="1.6"
              stroke-linejoin="round"
            />
          </svg>
          书签
        </button>
        <div class="more">
          <button type="button" class="cover-btn cover-btn--dots" title="更多" @click="moreOpen = !moreOpen">
            <span /><span /><span />
          </button>
          <div v-if="moreOpen" class="more__menu">
            <button type="button" @click="goBookHome">作品目录</button>
            <button type="button" @click="openChapterList">章节目录</button>
          </div>
        </div>
      </div>
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
      <nav class="pager">
        <button type="button" class="pager__btn" :disabled="!chapter?.prevSeq" @click="goChapter(chapter?.prevSeq ?? null)">上一章</button>
        <button type="button" class="pager__btn" :disabled="!chapter?.nextSeq" @click="goChapter(chapter?.nextSeq ?? null)">下一章</button>
      </nav>
    </main>

    <main v-else class="sheet sheet--empty">
      <p>未找到该作品</p>
      <button type="button" class="link" @click="exit">返回</button>
    </main>

    <footer class="foot" :class="{ 'is-on': chromeVisible }">
      <div v-if="fontOpen" class="font-pad">
        <div class="theme-pad__row">
          <span>字号</span>
          <button type="button" class="tool" :disabled="fontSize <= FONT_MIN" @click="bumpFont(-1)">A-</button>
          <input
            class="switch-pad__range"
            type="range"
            :min="FONT_MIN"
            :max="FONT_MAX"
            step="1"
            :value="fontSize"
            @input="onFontSlide"
          />
          <button type="button" class="tool" :disabled="fontSize >= FONT_MAX" @click="bumpFont(1)">A+</button>
        </div>
        <div class="font-pad__actions">
          <button type="button" class="font-pad__pill" :class="{ 'is-on': autoSlide }" @click="toggleAutoSlide">自动下滑</button>
          <button type="button" class="font-pad__pill" @click="toggleAdvanced">高级选项</button>
        </div>
      </div>
      <div v-else-if="themeOpen" class="theme-pad">
        <div class="theme-pad__row">
          <span>背景</span>
          <div class="theme-pad__swatches">
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
          <button
            type="button"
            class="follow-sys"
            :class="{ 'is-on': followSystemBright }"
            @click="toggleFollowSystemBright"
          >
            <i />
            亮度随系统
          </button>
        </div>
        <div class="theme-pad__row">
          <span>亮度</span>
          <input
            class="switch-pad__range"
            type="range"
            :min="BRIGHT_MIN"
            :max="BRIGHT_MAX"
            step="1"
            :value="followSystemBright ? BRIGHT_MAX : brightness"
            :disabled="followSystemBright"
            @input="onBrightness"
          />
          <em>{{ followSystemBright ? BRIGHT_MAX : brightness }}%</em>
        </div>
      </div>
      <div v-else-if="switchOpen" class="switch-pad">
        <div class="switch-pad__nav">
          <button type="button" class="switch-pad__arrow" :disabled="sliderSeq <= 1" @click="goChapter(sliderSeq - 1)">上一章</button>
          <strong>{{ switchLabel }}</strong>
          <button type="button" class="switch-pad__arrow" :disabled="sliderSeq >= chapterTotal" @click="goChapter(sliderSeq + 1)">下一章</button>
        </div>
        <div class="switch-pad__row">
          <input
            class="switch-pad__range"
            type="range"
            min="1"
            :max="chapterTotal"
            step="1"
            :value="sliderSeq"
            :disabled="chapterTotal <= 1"
            @input="onSwitchSlide"
            @change="commitSwitchSlide"
          />
          <button type="button" class="switch-pad__replay" title="回到原来章节" @click="restartChapter">
            <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
              <path d="M7.2 7.2A6.8 6.8 0 1 1 5.4 12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
              <path d="M7.2 4.2v3.6H3.8" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </button>
        </div>
      </div>
      <div v-else class="foot__progress">
        <span>{{ chapter ? `本章进度 ${chapter.seq}/${chapter.total}` : '本章进度' }}</span>
        <div class="foot__track">
          <i :style="{ width: `${progress}%` }" />
        </div>
      </div>
      <nav class="foot__tools">
        <button type="button" class="foot__item" :class="{ 'is-on': fontOpen }" @click="toggleFontPanel">
          <span class="aa-ico">Aa</span>
          <em>字号</em>
        </button>
        <button type="button" class="foot__item" :class="{ 'is-on': themeOpen }" @click="toggleThemePanel">
          <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
            <path
              d="M9 17.6h6M10.2 19.6h3.6"
              fill="none"
              stroke="currentColor"
              stroke-width="1.8"
              stroke-linecap="round"
            />
            <path
              d="M8.2 13.4A5.2 5.2 0 1 1 15.8 13.4c-.8.8-1.3 1.7-1.5 2.8H9.7c-.2-1.1-.7-2-1.5-2.8Z"
              fill="none"
              stroke="currentColor"
              stroke-width="1.8"
              stroke-linejoin="round"
            />
          </svg>
          <em>背景</em>
        </button>
        <button type="button" class="foot__item" :class="{ 'is-on': switchOpen }" @click="toggleSwitchPanel">
          <svg class="swap" viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
            <path d="M19 7.2H8.4M11.2 4.6 7.6 7.2l3.6 2.6" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
            <path d="M5 16.8h10.6M12.8 14.2 16.4 16.8l-3.6 2.6" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <em>切换章节</em>
        </button>
        <button type="button" class="foot__item" @click="toggleListen">
          <span class="listen-ico">
            <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
              <path
                d="M5 13.2a7 7 0 0 1 14 0"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
              />
              <path
                d="M5 13.2v4.1c0 .9.7 1.6 1.6 1.6h.7c.9 0 1.6-.7 1.6-1.6v-2.5c0-.9-.7-1.6-1.6-1.6H5Zm14 0v4.1c0 .9-.7 1.6-1.6 1.6h-.7c-.9 0-1.6-.7-1.6-1.6v-2.5c0-.9.7-1.6 1.6-1.6H19Z"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linejoin="round"
              />
            </svg>
          </span>
          <em>听书</em>
        </button>
        <button type="button" class="foot__item" @click="toggleCatalog">
          <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
            <path
              d="M5 7h14M5 12h14M5 17h10"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
            />
          </svg>
          <em>目录</em>
        </button>
      </nav>
    </footer>

    <div v-if="autoSlide" class="slide-bar" :class="{ 'is-on': slideBarVisible }" @click.stop>
      <div class="slide-bar__speed">
        <button type="button" class="slide-bar__hint" :disabled="slideSpeed <= SLIDE_MIN" @click="setSlideSpeed(slideSpeed - 1)">慢</button>
        <input
          class="switch-pad__range"
          type="range"
          :min="SLIDE_MIN"
          :max="SLIDE_MAX"
          step="1"
          :value="slideSpeed"
          @input="onSlideSpeed"
        />
        <button type="button" class="slide-bar__hint" :disabled="slideSpeed >= SLIDE_MAX" @click="setSlideSpeed(slideSpeed + 1)">快</button>
      </div>
      <button type="button" class="slide-bar__end" @click="endAutoSlide">结束自动下滑</button>
    </div>

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
            <svg
              v-if="bookmarks.has(bookId, item.seq)"
              class="catalog__mark"
              viewBox="0 0 24 24"
              width="13"
              height="13"
              aria-label="已加书签"
            >
              <path d="M6 3.5h12a1 1 0 0 1 1 1V20l-7-3.4L6 20V4.5a1 1 0 0 1 1-1z" fill="currentColor" />
            </svg>
            <em>{{ item.wordCount }}字</em>
          </button>
        </li>
      </ul>
      <p v-else class="catalog__empty">暂无章节</p>
    </aside>

    <p v-if="listenTip" class="listen-tip" role="status">{{ listenTip }}</p>
  </div>
</template>

<style scoped>
.reader {
  --brand: #ff4d3a;
  --paper: #f4efe6;
  --sheet: #fbf7f0;
  --ink-read: #2c271f;
  --muted: #8a8174;
  --line-soft: rgba(90, 70, 50, 0.12);
  --bar-bg: rgba(251, 247, 240, 0.92);
  position: relative;
  min-height: 100vh;
  color: var(--ink-read);
  background: var(--paper);
  transition: background 0.25s ease, color 0.25s ease;
}

.dim {
  position: fixed;
  inset: 0;
  z-index: 5;
  background: #000;
  pointer-events: none;
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
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 20;
  display: grid;
  grid-template-columns: 88px 1fr auto;
  align-items: center;
  min-height: 52px;
  padding: 8px clamp(20px, 4vw, 48px);
  background: var(--bar-bg);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--line-soft);
}

.bar-cover {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 26;
  display: grid;
  grid-template-columns: 88px 1fr auto;
  align-items: center;
  min-height: 52px;
  padding: 8px clamp(20px, 4vw, 48px);
  background: var(--bar-bg);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--line-soft);
  transform: translateY(-100%);
  opacity: 0;
  pointer-events: none;
  transition: transform 0.22s ease, opacity 0.22s ease;
}

.bar-cover.is-on {
  transform: none;
  opacity: 1;
  pointer-events: auto;
}

.cover-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  grid-column: 3;
  justify-self: end;
}

.cover-btn {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  min-width: 48px;
  padding: 2px 8px;
  border: 0;
  background: transparent;
  color: var(--ink-read);
  cursor: pointer;
  font-size: 11px;
}

.cover-btn:hover,
.cover-btn.is-on {
  color: var(--brand);
}

.cover-btn--dots {
  min-width: 40px;
  flex-direction: row;
  gap: 4px;
  height: 36px;
}

.cover-btn--dots span {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: currentColor;
}

.more {
  position: relative;
}

.more__menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 128px;
  padding: 6px;
  border-radius: 12px;
  background: var(--sheet);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.18);
  border: 1px solid var(--line-soft);
}

.more__menu button {
  display: block;
  width: 100%;
  height: 34px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--ink-read);
  cursor: pointer;
}

.more__menu button:hover {
  color: var(--brand);
  background: rgba(255, 77, 58, 0.08);
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
  font-weight: 500;
  letter-spacing: 0.04em;
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

.skin {
  position: relative;
  justify-self: end;
}

.skin__btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 8px 0 4px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--ink-read);
  cursor: pointer;
}

.skin__btn:hover,
.skin__btn.is-open {
  color: var(--brand);
}

.skin__name {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.skin__avatar,
.skin__mark {
  display: grid;
  place-items: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.skin__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.skin__mark {
  font-size: 11px;
  font-weight: 700;
  color: #fff;
}

.skin__mark.is-paper {
  background: linear-gradient(145deg, #e8b07a, #c47a4a);
}

.skin__mark.is-eye {
  background: linear-gradient(145deg, #7cbc6a, #3d8a4a);
}

.skin__mark.is-night {
  background: linear-gradient(145deg, #5b6578, #2a303c);
}

.skin__caret {
  opacity: 0.7;
  transition: transform 0.2s;
}

.skin__btn.is-open .skin__caret {
  transform: rotate(180deg);
}

.skin__menu {
  position: absolute;
  top: calc(100% + 8px);
  left: 50%;
  z-index: 30;
  width: 168px;
  padding: 6px 0;
  border-radius: 12px;
  background: var(--surface);
  box-shadow: 0 12px 28px rgba(30, 40, 60, 0.12);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transform: translateX(-50%);
}

.skin__menu button {
  display: block;
  width: 100%;
  border: 0;
  background: transparent;
  text-align: center;
  padding: 8px 12px;
  font-family: inherit;
  font-size: 16px;
  font-weight: 400;
  letter-spacing: 0;
  line-height: 1.5;
  color: var(--ink);
  cursor: pointer;
  white-space: nowrap;
}

.skin__menu button:hover {
  background: var(--brand-soft);
  color: var(--brand);
}

.theme-item {
  position: relative;
}

.theme-flyout {
  position: absolute;
  top: -6px;
  right: calc(100% - 2px);
  min-width: 132px;
  padding: 6px;
  border-radius: 12px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: 0 12px 28px rgba(30, 40, 60, 0.14);
}

.theme-flyout button {
  display: flex !important;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 8px;
  font-size: 14px;
  text-align: left;
}

.theme-flyout button.is-on,
.theme-flyout button:hover {
  background: var(--chip);
  color: var(--ink);
}

.slide-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 28;
  background: var(--bar-bg);
  backdrop-filter: blur(12px);
  border-top: 1px solid var(--line-soft);
  transform: translateY(110%);
  transition: transform 0.22s ease;
}

.slide-bar.is-on {
  transform: none;
}

.slide-bar__speed {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px 10px;
  font-size: 14px;
}

.slide-bar__hint {
  flex-shrink: 0;
  min-width: 2em;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font-size: 14px;
}

.slide-bar__hint:last-child {
  text-align: right;
}

.slide-bar__hint:hover:not(:disabled) {
  color: var(--brand);
}

.slide-bar__hint:disabled {
  opacity: 0.35;
  cursor: default;
}

.slide-bar__end {
  width: 100%;
  height: 48px;
  border: 0;
  border-top: 1px solid var(--line-soft);
  background: transparent;
  color: inherit;
  cursor: pointer;
  font-size: 16px;
}

.slide-bar__end:hover {
  color: var(--brand);
}

.foot {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  padding: 12px 20px 16px;
  background: var(--bar-bg);
  backdrop-filter: blur(12px);
  border-top: 1px solid var(--line-soft);
  transform: translateY(110%);
  transition: transform 0.22s ease;
}

.foot.is-on {
  transform: none;
}

.foot__progress {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
  font-size: 12px;
  color: var(--muted);
}

.foot__track {
  flex: 1;
  height: 4px;
  border-radius: 999px;
  background: var(--line-soft);
  overflow: hidden;
}

.foot__track i {
  display: block;
  height: 100%;
  background: var(--brand);
}

.foot__tools {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  align-items: end;
}

.foot__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  border: 0;
  background: transparent;
  color: var(--ink-read);
  cursor: pointer;
}

.foot__item:disabled {
  opacity: 0.35;
  cursor: default;
}

.foot__item.is-on {
  color: var(--brand);
}

.foot__item.is-on em {
  color: var(--brand);
}

.foot__item em {
  font-style: normal;
  font-size: 11px;
  color: var(--muted);
}

.foot__row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 28px;
}

.swap {
  display: block;
}

.listen-ico {
  display: grid;
  place-items: center;
  width: 22px;
  height: 22px;
}

.listen-tip {
  position: fixed;
  left: 50%;
  bottom: 108px;
  z-index: 30;
  transform: translateX(-50%);
  padding: 8px 16px;
  border-radius: 999px;
  background: rgba(28, 24, 20, 0.82);
  color: #fff;
  font-size: 13px;
  pointer-events: none;
}

.aa-ico {
  display: grid;
  place-items: center;
  height: 22px;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: -0.04em;
  line-height: 1;
}

.font-pad {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 12px;
}

.font-pad__actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 2px 0 0;
}

.font-pad__pill {
  min-width: 96px;
  height: 30px;
  padding: 0 14px;
  border: 1px solid color-mix(in srgb, currentColor 35%, transparent);
  border-radius: 999px;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font-size: 13px;
}

.font-pad__pill:hover,
.font-pad__pill.is-on {
  color: var(--brand);
  border-color: var(--brand);
}


.theme-pad {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 12px;
}

.theme-pad__row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.follow-sys {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font-size: 13px;
  white-space: nowrap;
}

.follow-sys i {
  width: 16px;
  height: 16px;
  border: 1.5px solid currentColor;
  border-radius: 50%;
}

.follow-sys.is-on {
  color: var(--brand);
}

.follow-sys.is-on i {
  border-color: var(--brand);
  background: radial-gradient(circle, var(--brand) 0 5px, transparent 6px);
}

.follow-sys:hover {
  color: var(--brand);
}

.theme-pad__row > span {
  flex-shrink: 0;
  width: 36px;
  font-size: 13px;
}

.theme-pad__row em {
  flex-shrink: 0;
  width: 40px;
  font-style: normal;
  font-size: 12px;
  color: var(--muted);
  text-align: right;
}

.theme-pad__swatches {
  display: flex;
  align-items: center;
  gap: 14px;
}

.theme-pad .swatch {
  width: 22px;
  height: 22px;
}

.switch-pad {
  margin-bottom: 12px;
}

.switch-pad__nav {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.switch-pad__nav strong {
  min-width: 0;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.switch-pad__arrow,
.switch-pad__replay {
  display: grid;
  place-items: center;
  min-width: 52px;
  height: 32px;
  padding: 0 4px;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font-size: 13px;
}

.switch-pad__replay {
  min-width: 36px;
  width: 36px;
  padding: 0;
}

.switch-pad__arrow:hover:not(:disabled),
.switch-pad__replay:hover {
  color: var(--brand);
}

.switch-pad__arrow:disabled {
  opacity: 0.3;
  cursor: default;
}

.switch-pad__row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.switch-pad__range {
  flex: 1;
  height: 18px;
  margin: 0;
  accent-color: currentColor;
  cursor: pointer;
}

.switch-pad__range:disabled {
  opacity: 0.35;
  cursor: default;
}

.switch-pad__range::-webkit-slider-runnable-track {
  height: 2px;
  background: color-mix(in srgb, currentColor 35%, transparent);
}

.switch-pad__range::-webkit-slider-thumb {
  appearance: none;
  width: 14px;
  height: 14px;
  margin-top: -6px;
  border-radius: 50%;
  background: currentColor;
}

.switch-pad__range::-moz-range-track {
  height: 2px;
  background: color-mix(in srgb, currentColor 35%, transparent);
}

.switch-pad__range::-moz-range-thumb {
  width: 14px;
  height: 14px;
  border: 0;
  border-radius: 50%;
  background: currentColor;
}

.tool {
  min-width: 32px;
  height: 28px;
  padding: 0 6px;
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
  width: min(720px, calc(100% - 40px));
  margin: 0 auto;
  padding: 68px 8px 140px;
  background: transparent;
}

.kicker,
.meta,
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
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-top: 48px;
  padding-top: 28px;
  border-top: 1px dashed var(--line-soft);
}

.pager__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 132px;
  height: 42px;
  padding: 0 22px;
  border: 1px solid var(--line-soft);
  border-radius: 999px;
  background: color-mix(in srgb, var(--sheet) 78%, transparent);
  color: var(--ink-read);
  cursor: pointer;
  font-size: 14px;
  letter-spacing: 0.08em;
  transition: color 0.2s, border-color 0.2s, background 0.2s, box-shadow 0.2s;
}

.pager__btn:hover:not(:disabled) {
  color: var(--brand);
  border-color: color-mix(in srgb, var(--brand) 45%, var(--line-soft));
  background: color-mix(in srgb, var(--brand) 8%, var(--sheet));
  box-shadow: 0 8px 18px rgba(255, 77, 58, 0.08);
}

.pager__btn:disabled {
  opacity: 0.38;
  cursor: default;
  box-shadow: none;
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
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.catalog__mark {
  flex-shrink: 0;
  color: var(--brand);
  transform: translateY(1px);
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
    grid-template-columns: 64px 1fr auto;
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
