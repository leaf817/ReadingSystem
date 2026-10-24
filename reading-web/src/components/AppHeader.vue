<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchBooks } from '@/api/book'
import { books as mockBooks, categories, type Book } from '@/data/mockBooks'
import { useAuthStore } from '@/stores/auth'
import { useShelfStore } from '@/stores/shelf'
import { useReadingStore } from '@/stores/reading'
import { useThemeStore } from '@/stores/theme'
import {
  SEARCH_BY_OPTIONS,
  contentTagsOf,
  matchBookBy,
  parseSearchBy,
  searchByMeta,
  type SearchBy,
} from '@/utils/bookSearch'

const props = withDefaults(
  defineProps<{
    /** 仅首页显示分类导航 */
    withCats?: boolean
    /** 目录等页在首页前显示返回 */
    showBack?: boolean
    /** 目录页用书名替换搜索框 */
    pageTitle?: string
  }>(),
  { withCats: false, showBack: false, pageTitle: '' },
)

const keyword = ref('')
const searchBy = ref<SearchBy>('title')
const typeOpen = ref(false)
const activeCategory = ref('推荐')
const userOpen = ref(false)
const themeOpen = ref(false)
const avatarOpen = ref(false)
const userMenu = ref<HTMLElement | null>(null)
const searchWrap = ref<HTMLElement | null>(null)
const quickWrap = ref<HTMLElement | null>(null)
const quickPanel = ref<'shelf' | 'history' | ''>('')
const suggestions = ref<Book[]>([])
const suggestOpen = ref(false)
const auth = useAuthStore()
const shelf = useShelfStore()
const reading = useReadingStore()
const appTheme = useThemeStore()
const route = useRoute()
const router = useRouter()
let quickTimer = 0

let suggestTimer = 0
let suggestSeq = 0

const avatarText = computed(() => {
  const name = auth.user?.displayName?.trim() || ''
  return name.slice(0, 1) || '?'
})

const showWorkspace = computed(() => auth.role === 'admin' || auth.role === 'author')
const showMine = computed(() => auth.role === 'reader')
const shelfItems = computed(() => shelf.items.slice(0, 8))
const historyItems = computed(() =>
  [...reading.records].sort((a, b) => (b.updatedAt || 0) - (a.updatedAt || 0)).slice(0, 8),
)

const searchPlaceholder = computed(() => searchByMeta(searchBy.value).placeholder)
const searchByShort = computed(() => searchByMeta(searchBy.value).short)

function syncKeywordFromRoute() {
  if (route.name !== 'search') return
  const q = typeof route.query.q === 'string' ? route.query.q : ''
  keyword.value = q
  searchBy.value = parseSearchBy(route.query.by)
}

function goSearch() {
  const q = keyword.value.trim()
  if (!q) return
  suggestOpen.value = false
  typeOpen.value = false
  void router.push({ name: 'search', query: { q, by: searchBy.value } })
}

function selectSearchBy(id: SearchBy) {
  searchBy.value = id
  typeOpen.value = false
  const q = keyword.value.trim()
  if (q) void loadSuggestions(q)
}

function openSuggestBook(book: Book) {
  suggestOpen.value = false
  keyword.value = book.title
  void router.push({ name: 'book', params: { id: String(book.id) } })
}

function visibleTags(book: Book) {
  return contentTagsOf(book)
}

function matchLocal(q: string) {
  return mockBooks.filter((book) => matchBookBy(book, q, searchBy.value))
}

async function loadSuggestions(q: string) {
  const seq = ++suggestSeq
  try {
    const list = await fetchBooks(q, searchBy.value)
    if (seq !== suggestSeq) return
    suggestions.value = list.slice(0, 8)
  } catch {
    if (seq !== suggestSeq) return
    suggestions.value = matchLocal(q).slice(0, 8)
  }
  suggestOpen.value = suggestions.value.length > 0
}

function closeMenu() {
  window.clearTimeout(themeTimer)
  userOpen.value = false
  themeOpen.value = false
}

let themeTimer = 0

function openTheme() {
  window.clearTimeout(themeTimer)
  themeOpen.value = true
}

function closeThemeSoon() {
  window.clearTimeout(themeTimer)
  themeTimer = window.setTimeout(() => {
    themeOpen.value = false
  }, 220)
}

function pickTheme(mode: 'light' | 'dark') {
  window.clearTimeout(themeTimer)
  appTheme.setMode(mode)
  themeOpen.value = false
}

function openQuick(id: 'shelf' | 'history') {
  if (!auth.isLoggedIn) return
  window.clearTimeout(quickTimer)
  shelf.hydrate()
  reading.hydrate()
  quickPanel.value = id
}

function closeQuickSoon() {
  window.clearTimeout(quickTimer)
  quickTimer = window.setTimeout(() => {
    quickPanel.value = ''
  }, 180)
}

function keepQuick() {
  window.clearTimeout(quickTimer)
}

function goQuick(id: 'shelf' | 'history') {
  if (!auth.isLoggedIn) {
    void router.push({ name: 'login', query: { redirect: id === 'shelf' ? '/mine/bookshelf' : '/mine/history' } })
    return
  }
  quickPanel.value = ''
  void router.push({ name: id === 'shelf' ? 'mine-bookshelf' : 'mine-history' })
}

function openShelfBook(bookId: number) {
  quickPanel.value = ''
  void router.push({ name: 'book', params: { id: String(bookId) } })
}

function openHistoryBook(bookId: number, seq: number) {
  quickPanel.value = ''
  void router.push({ name: 'read', params: { id: String(bookId) }, query: { chapter: String(seq) } })
}

function progressText(bookId: number) {
  const item = reading.of(bookId)
  if (!item) return '尚未阅读'
  const pct = reading.percent(bookId)
  if (pct != null) return `已读至 ${item.chapterTitle} · ${pct}%`
  return `已读至 ${item.chapterTitle}`
}

function formatTime(ts: number) {
  if (!ts) return ''
  const d = new Date(ts)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function toggleUser() {
  userOpen.value = !userOpen.value
}

function openAvatarPreview() {
  closeMenu()
  avatarOpen.value = true
}

function closeAvatarPreview() {
  avatarOpen.value = false
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  void router.push('/')
}

function goMine() {
  closeMenu()
  if (auth.role !== 'reader') return
  router.push('/mine')
}

function goWorkspace() {
  closeMenu()
  if (!auth.role || auth.role === 'reader') return
  router.push(auth.roleHome[auth.role])
}

function logout() {
  closeMenu()
  auth.logout()
  router.push('/login')
}

function onDocClick(event: MouseEvent) {
  const target = event.target as Node
  if (userOpen.value && userMenu.value && !userMenu.value.contains(target)) {
    closeMenu()
  }
  if (searchWrap.value && !searchWrap.value.contains(target)) {
    suggestOpen.value = false
    typeOpen.value = false
  }
  if (quickPanel.value && quickWrap.value && !quickWrap.value.contains(target)) {
    quickPanel.value = ''
  }
}

function onDocKey(event: KeyboardEvent) {
  if (event.key === 'Escape') closeAvatarPreview()
}

onMounted(() => {
  syncKeywordFromRoute()
  shelf.hydrate()
  reading.hydrate()
  document.addEventListener('click', onDocClick)
  document.addEventListener('keydown', onDocKey)
})

watch(
  () => [route.name, route.query.q, route.query.by],
  () => {
    syncKeywordFromRoute()
  },
)

watch([keyword, searchBy], ([value]) => {
  window.clearTimeout(suggestTimer)
  const q = String(value).trim()
  if (!q) {
    suggestions.value = []
    suggestOpen.value = false
    return
  }
  suggestTimer = window.setTimeout(() => {
    void loadSuggestions(q)
  }, 160)
})

watch(
  () => route.fullPath,
  () => {
    quickPanel.value = ''
    avatarOpen.value = false
  },
)

onBeforeUnmount(() => {
  window.clearTimeout(suggestTimer)
  window.clearTimeout(quickTimer)
  window.clearTimeout(themeTimer)
  document.removeEventListener('click', onDocClick)
  document.removeEventListener('keydown', onDocKey)
})
</script>

<template>
  <header class="header">
    <div class="header__bar">
      <div class="header__lead">
        <button v-if="props.showBack" type="button" class="back-brand" @click="goBack">
          ‹ 返回
        </button>
        <RouterLink v-else to="/" class="brand" aria-label="阅界首页">
          <span class="brand__mark" aria-hidden="true">
            <svg viewBox="0 0 48 48" width="36" height="36">
              <defs>
                <linearGradient id="logoBook" x1="0" y1="0" x2="1" y2="1">
                  <stop offset="0%" stop-color="#ff6a4d" />
                  <stop offset="100%" stop-color="#e63a28" />
                </linearGradient>
              </defs>
              <rect x="4" y="6" width="40" height="36" rx="10" fill="url(#logoBook)" />
              <path
                d="M24 12c-6.2 3.2-10 4.6-14 5.2V34c4.2-.7 8.2-2.1 14-5.2 5.8 3.1 9.8 4.5 14 5.2V17.2C34 16.6 30.2 15.2 24 12z"
                fill="#fff"
                opacity="0.96"
              />
              <path d="M24 12v16.8" stroke="#ff4d3a" stroke-width="1.6" stroke-linecap="round" />
              <circle cx="36" cy="12" r="5.5" fill="#fff3" />
            </svg>
          </span>
          <span class="brand__text">
            <span class="brand__name">阅界</span>
            <span class="brand__sub">NOVEL WORLD</span>
          </span>
        </RouterLink>
        <nav class="header__nav" aria-label="书城导航">
          <RouterLink
            to="/"
            class="nav-item"
            :class="{ 'is-on': route.name === 'home' }"
          >
            首页
          </RouterLink>
          <RouterLink
            to="/category"
            class="nav-item"
            :class="{ 'is-on': route.name === 'category' }"
          >
            分类
          </RouterLink>
          <RouterLink
            to="/ranks"
            class="nav-item"
            :class="{ 'is-on': route.name === 'ranks' }"
          >
            排行榜
          </RouterLink>
        </nav>
      </div>

      <div ref="searchWrap" class="search-wrap">
        <div v-if="props.pageTitle" class="title-wrap" :title="props.pageTitle">
          <strong class="title-bar">{{ props.pageTitle }}</strong>
        </div>
        <form v-else class="search" @submit.prevent="goSearch">
          <div class="search__type">
            <button
              type="button"
              class="search__type-btn"
              :class="{ 'is-open': typeOpen }"
              aria-haspopup="listbox"
              :aria-expanded="typeOpen"
              @click.stop="typeOpen = !typeOpen"
            >
              <span>{{ searchByShort }}</span>
              <svg class="search__caret" viewBox="0 0 12 12" width="10" height="10" aria-hidden="true">
                <path
                  d="M2.5 4.2L6 7.8l3.5-3.6"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.6"
                  stroke-linecap="round"
                />
              </svg>
            </button>
          </div>
          <span class="search__split" aria-hidden="true" />
          <input
            v-model="keyword"
            type="search"
            :placeholder="searchPlaceholder"
            autocomplete="off"
            @focus="suggestOpen = suggestions.length > 0"
          />
          <button type="submit" class="search__btn">搜索</button>
        </form>
        <div v-if="!props.pageTitle && typeOpen" class="search__type-menu" role="listbox">
          <button
            v-for="item in SEARCH_BY_OPTIONS"
            :key="item.id"
            type="button"
            role="option"
            :class="{ 'is-on': searchBy === item.id }"
            @click.stop="selectSearchBy(item.id)"
          >
            {{ item.label }}
          </button>
        </div>
        <ul v-if="!props.pageTitle && suggestOpen && suggestions.length" class="suggest">
          <li v-for="book in suggestions" :key="book.id">
            <button type="button" @mousedown.prevent="openSuggestBook(book)">
              <img v-if="book.cover" :src="book.cover" :alt="book.title" />
              <span v-else class="suggest__fallback">{{ book.title.slice(0, 1) }}</span>
              <span class="suggest__meta">
                <strong>{{ book.title }}</strong>
                <em>{{ book.author }}</em>
                <span v-if="visibleTags(book).length" class="suggest__tags">
                  <i
                    v-for="tag in visibleTags(book).slice(0, 4)"
                    :key="tag"
                    :class="{ 'is-hit': tag.toLowerCase().includes(keyword.trim().toLowerCase()) }"
                  >{{ tag }}</i>
                </span>
              </span>
            </button>
          </li>
        </ul>
      </div>

      <nav class="actions">
        <div ref="quickWrap" class="quick" @mouseleave="closeQuickSoon">
          <button
            type="button"
            class="nav-item"
            :class="{ 'is-on': quickPanel === 'shelf' || route.name === 'mine-bookshelf' }"
            @mouseenter="openQuick('shelf')"
            @click="goQuick('shelf')"
          >
            书架
          </button>
          <button
            type="button"
            class="nav-item"
            :class="{ 'is-on': quickPanel === 'history' || route.name === 'mine-history' }"
            @mouseenter="openQuick('history')"
            @click="goQuick('history')"
          >
            历史
          </button>

          <div v-if="quickPanel" class="quick__drop" @mouseenter="keepQuick">
            <header v-if="quickPanel === 'shelf'">最近收藏</header>
            <header v-else>最近阅读</header>

            <p v-if="!auth.isLoggedIn" class="quick__empty">登录后即可查看</p>
            <p v-else-if="quickPanel === 'shelf' && !shelfItems.length" class="quick__empty">书架还是空的，去收藏一本书吧</p>
            <p v-else-if="quickPanel === 'history' && !historyItems.length" class="quick__empty">还没有阅读记录</p>

            <ul v-else-if="quickPanel === 'shelf'">
              <li v-for="item in shelfItems" :key="item.bookId">
                <button type="button" @click="openShelfBook(item.bookId)">
                  <img v-if="item.cover" :src="item.cover" :alt="item.title" />
                  <span v-else class="quick__fallback">{{ item.title.slice(0, 1) }}</span>
                  <div>
                    <strong>{{ item.title }}</strong>
                    <em>{{ item.author }}</em>
                    <small>{{ progressText(item.bookId) }}</small>
                  </div>
                </button>
              </li>
            </ul>
            <ul v-else>
              <li v-for="item in historyItems" :key="item.bookId">
                <button type="button" @click="openHistoryBook(item.bookId, item.chapterIndex)">
                  <img v-if="item.cover" :src="item.cover" :alt="item.title" />
                  <span v-else class="quick__fallback">{{ item.title.slice(0, 1) }}</span>
                  <div>
                    <strong>{{ item.title }}</strong>
                    <em>{{ progressText(item.bookId) }}</em>
                    <small>{{ formatTime(item.updatedAt) }}</small>
                  </div>
                </button>
              </li>
            </ul>
          </div>
        </div>

        <div v-if="auth.isLoggedIn" ref="userMenu" class="user-menu">
          <div class="user-entry" :class="{ 'is-open': userOpen }">
            <button
              type="button"
              class="user-avatar"
              title="查看头像"
              @click.stop="openAvatarPreview"
            >
              <span v-if="auth.user?.avatar" class="avatar avatar--img">
                <img :src="auth.user.avatar" :alt="auth.user.displayName" />
              </span>
              <span v-else class="avatar">{{ avatarText }}</span>
            </button>
            <button
              type="button"
              class="user-name"
              aria-haspopup="menu"
              :aria-expanded="userOpen"
              @click.stop="toggleUser"
            >
              <span>{{ auth.user?.displayName }}</span>
              <svg class="caret" viewBox="0 0 12 12" width="11" height="11" aria-hidden="true">
                <path
                  d="M2.5 4.2L6 7.8l3.5-3.6"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.6"
                  stroke-linecap="round"
                />
              </svg>
            </button>
          </div>
          <div v-if="userOpen" class="dropdown" role="menu">
            <button
              v-if="showMine"
              type="button"
              role="menuitem"
              @click="goMine"
            >
              个人中心
            </button>
            <button
              v-if="showWorkspace"
              type="button"
              role="menuitem"
              @click="goWorkspace"
            >
              工作台
            </button>
            <div class="theme-item" @mouseenter="openTheme" @mouseleave="closeThemeSoon">
              <button type="button" role="menuitem" @click.stop="themeOpen = !themeOpen">
                主题: {{ appTheme.label }}
              </button>
              <div v-if="themeOpen" class="theme-flyout" role="menu">
                <button
                  type="button"
                  role="menuitem"
                  :class="{ 'is-on': appTheme.mode === 'dark' }"
                  @click="pickTheme('dark')"
                >
                  深色
                </button>
                <button
                  type="button"
                  role="menuitem"
                  :class="{ 'is-on': appTheme.mode === 'light' }"
                  @click="pickTheme('light')"
                >
                  浅色
                </button>
              </div>
            </div>
            <button type="button" role="menuitem" class="is-danger" @click="logout">退出登录</button>
          </div>
        </div>

        <RouterLink v-else to="/login" class="login-btn">登录</RouterLink>
      </nav>
    </div>

    <div v-if="props.withCats" class="header__cats">
      <nav class="cats" aria-label="分类导航">
        <button
          v-for="cat in categories"
          :key="cat"
          type="button"
          class="cats__item"
          :class="{ 'is-active': activeCategory === cat }"
          @click="activeCategory = cat"
        >
          {{ cat }}
        </button>
      </nav>
    </div>
  </header>
  <div class="header-spacer" :class="{ 'has-cats': props.withCats }" aria-hidden="true" />
  <div
    v-if="avatarOpen"
    class="avatar-preview"
    role="dialog"
    aria-modal="true"
    :aria-label="auth.user?.displayName || '头像'"
    @click="closeAvatarPreview"
  >
    <span v-if="auth.user?.avatar" class="avatar-preview__img">
      <img :src="auth.user.avatar" :alt="auth.user.displayName" />
    </span>
    <span v-else class="avatar-preview__letter">{{ avatarText }}</span>
    <p>{{ auth.user?.displayName }}</p>
  </div>
</template>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  width: 100%;
  background: var(--header-bg);
  border-bottom: 1px solid var(--header-line);
  box-shadow: var(--header-shadow);
}

.header-spacer {
  height: 60px;
  flex-shrink: 0;
  pointer-events: none;
}

.header-spacer.has-cats {
  height: calc(60px + 45px);
}

.header__bar {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr minmax(280px, 520px) 1fr;
  align-items: center;
  gap: 16px;
  height: 60px;
  padding: 0 clamp(20px, 4vw, 48px);
}

.header__lead {
  display: flex;
  align-items: center;
  gap: 32px;
  min-width: 0;
  justify-self: start;
}

.header__nav {
  display: flex;
  align-items: center;
  gap: 18px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.back-brand {
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 4px;
  border: 0;
  background: transparent;
  color: #8a93a3;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.04em;
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.2s;
}

.back-brand:hover {
  color: var(--brand);
}

.nav-item {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  height: 36px;
  padding: 0 2px;
  border: 0;
  background: transparent;
  font-family: inherit;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: var(--nav-ink);
  text-decoration: none;
  cursor: pointer;
  transition: color 0.2s;
}

.nav-item::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 4px;
  width: 0;
  height: 2px;
  border-radius: 2px;
  background: var(--brand);
  transform: translateX(-50%);
  transition: width 0.2s;
}

.nav-item:hover,
.nav-item.is-on {
  color: var(--brand);
}

.nav-item.is-on::after,
.nav-item:hover::after {
  width: 1.15em;
}

.brand__mark {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  filter: drop-shadow(0 4px 10px rgba(255, 77, 58, 0.22));
  transition: transform 0.3s ease;
}

.brand:hover .brand__mark {
  transform: scale(1.04);
}

.brand__text {
  display: flex;
  flex-direction: column;
  line-height: 1.05;
}

.brand__name {
  font-family: var(--font-display);
  font-size: 26px;
  color: var(--brand);
  letter-spacing: 0.1em;
  white-space: nowrap;
}

.brand__sub {
  font-size: 9px;
  color: #c4a8a0;
  letter-spacing: 0.24em;
  margin-top: 2px;
  white-space: nowrap;
}

.search-wrap {
  position: relative;
  width: 100%;
  max-width: 520px;
  justify-self: center;
  grid-column: 2;
}

.search {
  display: flex;
  align-items: center;
  width: 100%;
  height: 38px;
  border: 1px solid var(--input-border);
  border-radius: 999px;
  overflow: hidden;
  background: var(--input-bg);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.search:focus-within {
  border-color: var(--brand);
  box-shadow: 0 0 0 3px rgba(255, 77, 58, 0.1);
}

.title-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-width: 0;
}

.title-bar {
  min-width: 0;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search__type {
  position: relative;
  flex-shrink: 0;
  height: 100%;
}

.search__type-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 100%;
  padding: 0 4px 0 14px;
  border: 0;
  background: transparent;
  color: var(--nav-ink);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.search__caret {
  color: #8aa0bc;
  transition: transform 0.2s;
}

.search__type-btn.is-open .search__caret {
  transform: rotate(180deg);
}

.search__type-menu {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  z-index: 80;
  min-width: 132px;
  padding: 4px;
  border-radius: 12px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: 0 12px 28px rgba(30, 40, 60, 0.14);
}

.search__type-menu button {
  display: block;
  width: 100%;
  border: 0;
  border-bottom: 1px solid var(--line);
  background: transparent;
  text-align: left;
  padding: 11px 14px;
  border-radius: 0;
  font-size: 14px;
  color: var(--ink);
  cursor: pointer;
}

.search__type-menu button:last-child {
  border-bottom: 0;
}

.search__type-menu button:hover,
.search__type-menu button.is-on {
  color: var(--brand);
  background: var(--brand-soft);
}

.search__split {
  width: 1px;
  height: 16px;
  flex-shrink: 0;
  background: var(--input-border);
}

.search input {
  flex: 1;
  min-width: 0;
  border: 0;
  outline: 0;
  padding: 0 10px;
  background: transparent;
  color: var(--ink);
  font-size: 13px;
}

.search__btn {
  border: 0;
  height: 100%;
  padding: 0 22px;
  background: var(--brand);
  color: #fff;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
  transition: background 0.2s;
}

.search__btn:hover {
  background: var(--brand-dark);
}

.suggest {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  z-index: 60;
  margin: 0;
  padding: 8px;
  list-style: none;
  border-radius: 14px;
  background: var(--surface);
  box-shadow: 0 16px 40px rgba(26, 26, 26, 0.14);
  border: 1px solid var(--line);
}

.suggest li button {
  display: grid;
  grid-template-columns: 36px 1fr;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 8px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.suggest li button:hover {
  background: rgba(255, 77, 58, 0.06);
}

.suggest img,
.suggest__fallback {
  width: 36px;
  height: 48px;
  border-radius: 4px;
  object-fit: cover;
  background: #ece7e0;
}

.suggest__fallback {
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 12px;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
}

.suggest__meta strong,
.suggest__meta em {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.suggest__meta strong {
  font-size: 13px;
}

.suggest__meta em {
  margin-top: 2px;
  font-style: normal;
  font-size: 12px;
  color: var(--ink-muted);
}

.suggest__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
}

.suggest__tags i {
  font-style: normal;
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 999px;
  background: var(--chip);
  color: var(--ink-secondary);
}

.suggest__tags i.is-hit {
  background: var(--brand-soft);
  color: var(--brand);
}

.actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  justify-self: end;
  gap: 18px;
  white-space: nowrap;
  min-width: 0;
}

.quick {
  position: relative;
  display: flex;
  align-items: center;
  gap: 18px;
}

.quick__drop {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  z-index: 70;
  width: 360px;
  max-height: min(72vh, 520px);
  overflow: auto;
  padding: 12px 12px 8px;
  border-radius: 14px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.28);
}

.quick__drop header {
  padding: 4px 8px 10px;
  font-size: 13px;
  color: var(--ink-muted);
}

.quick__empty {
  padding: 28px 12px;
  text-align: center;
  color: var(--ink-muted);
  font-size: 13px;
}

.quick__drop ul {
  list-style: none;
}

.quick__drop li button {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 12px;
  width: 100%;
  padding: 8px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.quick__drop li button:hover {
  background: var(--chip);
}

.quick__drop img,
.quick__fallback {
  width: 72px;
  height: 54px;
  border-radius: 8px;
  object-fit: cover;
  background: #ece7e0;
}

.quick__fallback {
  display: grid;
  place-items: center;
  color: #fff;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
}

.quick__drop strong,
.quick__drop em,
.quick__drop small {
  display: block;
}

.quick__drop strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: var(--ink);
}

.quick__drop em,
.quick__drop small {
  margin-top: 4px;
  font-style: normal;
  font-size: 12px;
  color: var(--ink-muted);
}

.user-menu {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.user-entry {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 8px 0 4px;
}

.user-avatar {
  border: 0;
  padding: 0;
  background: transparent;
  cursor: pointer;
  display: grid;
  place-items: center;
  border-radius: 50%;
}

.user-name {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 32px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--nav-ink);
  font-family: inherit;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.08em;
  cursor: pointer;
  transition: color 0.2s;
}

.user-name:hover,
.user-entry.is-open .user-name {
  color: var(--brand);
}

.caret {
  color: currentColor;
  transition: transform 0.2s;
}

.user-entry.is-open .caret {
  transform: rotate(180deg);
}

.dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 50%;
  z-index: 60;
  width: 168px;
  box-sizing: border-box;
  padding: 6px 0;
  border-radius: 12px;
  background: var(--surface);
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 12px 28px rgba(30, 40, 60, 0.12);
  transform: translateX(-50%);
}

.dropdown button {
  display: block;
  width: 100%;
  border: 0;
  background: transparent;
  text-align: center;
  padding: 8px 12px;
  border-radius: 0;
  font-family: inherit;
  font-size: 16px;
  font-weight: 400;
  letter-spacing: 0;
  line-height: 1.5;
  color: var(--ink);
  cursor: pointer;
  white-space: nowrap;
}

.dropdown button:hover {
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
  justify-content: space-between !important;
  gap: 10px;
  width: 100%;
  padding: 9px 12px !important;
  border-radius: 8px !important;
  font-size: 14px !important;
  text-align: left !important;
}

.theme-flyout button.is-on,
.theme-flyout button:hover {
  background: var(--chip);
  color: var(--ink);
}

.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: inline-grid;
  place-items: center;
  flex-shrink: 0;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  overflow: hidden;
}

.avatar--img {
  background: #f0eeea;
}

.avatar--img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-preview {
  position: fixed;
  inset: 0;
  z-index: 80;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 32px;
  background: rgba(18, 14, 12, 0.72);
  cursor: pointer;
}

.avatar-preview__img,
.avatar-preview__letter {
  width: min(72vw, 360px);
  height: min(72vw, 360px);
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.45);
}

.avatar-preview__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  background: #f0eeea;
}

.avatar-preview__letter {
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
  color: #fff;
  font-size: 120px;
  font-weight: 700;
}

.avatar-preview p {
  margin: 0;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.4);
}

.login-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  height: 32px;
  margin-left: 6px;
  padding: 0 16px;
  border-radius: 999px;
  background: var(--surface);
  color: var(--brand);
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(255, 77, 58, 0.12);
  transition: background 0.2s, color 0.2s, box-shadow 0.2s;
}

.login-btn:hover {
  background: var(--brand);
  color: #fff;
  box-shadow: 0 6px 16px rgba(255, 77, 58, 0.22);
}

.header__cats {
  width: 100%;
  border-top: 1px solid var(--header-line);
  background: var(--header-cats);
}

.cats {
  width: 100%;
  display: flex;
  align-items: stretch;
  justify-content: center;
  gap: 4px;
  padding: 0 clamp(20px, 4vw, 48px);
  overflow-x: auto;
  scrollbar-width: none;
}

.cats::-webkit-scrollbar {
  display: none;
}

.cats__item {
  position: relative;
  flex: 0 0 auto;
  border: 0;
  background: transparent;
  min-width: 64px;
  padding: 12px 18px;
  font-size: 14px;
  color: var(--nav-muted);
  cursor: pointer;
  text-align: center;
  transition: color 0.2s;
}

.cats__item::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 0;
  height: 2px;
  border-radius: 2px;
  background: var(--brand);
  transform: translateX(-50%);
  transition: width 0.2s ease;
}

.cats__item:hover {
  color: var(--brand);
}

.cats__item.is-active {
  color: var(--brand);
  font-weight: 600;
}

.cats__item.is-active::after {
  width: 22px;
}

@media (max-width: 900px) {
  .header__bar {
    grid-template-columns: minmax(0, 1fr) minmax(200px, 1fr) minmax(0, 1fr);
    gap: 12px;
    padding: 0 16px;
  }

  .brand__sub {
    display: none;
  }

  .header__lead {
    gap: 20px;
  }

  .cats {
    justify-content: center;
    padding-inline: 16px;
  }

  .cats__item {
    padding: 11px 14px;
    min-width: 0;
  }
}

@media (max-width: 640px) {
  .actions .nav-item {
    display: none;
  }

  .user-name span {
    display: none;
  }

  .brand__name {
    font-size: 22px;
  }

  .search__type-btn {
    padding-left: 10px;
  }

  .search__btn {
    padding: 0 14px;
  }

  .login-btn {
    margin-left: 0;
    padding: 0 12px;
  }
}
</style>
