<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { fetchBooks, fetchHomeFeed } from '@/api/book'
import { books as mockBooks, type Book } from '@/data/mockBooks'

type BoardId =
  | 'recommend'
  | 'finish'
  | 'new'
  | 'friends'
  | 'serialize'
  | 'darkhorse'
  | 'peak'
  | 'drought'
  | 'read'
  | 'authors'

interface Board {
  id: BoardId
  label: string
  hint: string
}

interface AuthorRow {
  author: string
  book: Book
}

const boards: Board[] = [
  { id: 'recommend', label: '推荐榜', hint: '综合热度推荐' },
  { id: 'finish', label: '完本榜', hint: '完结佳作' },
  { id: 'new', label: '新书榜', hint: '近期上新作品' },
  { id: 'friends', label: '书友榜', hint: '读者关注热度' },
  { id: 'serialize', label: '追更榜', hint: '连载更新热度' },
  { id: 'darkhorse', label: '黑马榜', hint: '潜力新作' },
  { id: 'peak', label: '巅峰榜', hint: '全站热度之巅' },
  { id: 'drought', label: '书荒榜', hint: '长篇厚读' },
  { id: 'read', label: '阅读榜', hint: '按阅读量排序' },
  { id: 'authors', label: '作者榜', hint: '作者热度' },
]

const validIds = new Set(boards.map((item) => item.id))

const route = useRoute()
const router = useRouter()
const catalog = ref<Book[]>([])
const recommend = ref<Book[]>([])
const newer = ref<Book[]>([])
const loadError = ref('')

const board = computed<BoardId>(() => {
  const raw = route.query.board
  if (typeof raw === 'string' && validIds.has(raw as BoardId)) return raw as BoardId
  return 'recommend'
})

const current = computed(() => boards.find((item) => item.id === board.value) ?? boards[0])

function heatOf(book: Book) {
  return Number.parseFloat(book.heat) || 0
}

function chaptersOf(book: Book) {
  return book.chapters ?? 0
}

function byHeat(list: Book[]) {
  return [...list].sort((a, b) => heatOf(b) - heatOf(a))
}

function pick(list: Book[]) {
  return list.slice(0, 50)
}

const rankedBooks = computed(() => {
  const all = catalog.value
  const finished = all.filter((book) => book.status === '完结')
  const serial = all.filter((book) => book.status === '连载')
  switch (board.value) {
    case 'recommend':
      return pick(recommend.value.length ? byHeat(recommend.value) : byHeat(all))
    case 'finish':
      return pick(byHeat(finished))
    case 'new':
      return pick(newer.value.length ? newer.value : [...all].reverse())
    case 'friends':
      return pick(byHeat(all))
    case 'serialize':
      return pick(byHeat(serial))
    case 'darkhorse': {
      const ranked = byHeat(all)
      const skip = Math.min(3, Math.floor(ranked.length / 4))
      return pick(
        [...ranked.slice(skip)].sort((a, b) => {
          const sa = heatOf(a) / Math.max(chaptersOf(a), 40)
          const sb = heatOf(b) / Math.max(chaptersOf(b), 40)
          return sb - sa
        }),
      )
    }
    case 'peak':
      return pick(byHeat(all))
    case 'drought':
      return pick(
        [...finished].sort((a, b) => chaptersOf(b) - chaptersOf(a) || heatOf(b) - heatOf(a)),
      )
    case 'read':
      return pick(
        [...all].sort((a, b) => chaptersOf(b) - chaptersOf(a) || heatOf(b) - heatOf(a)),
      )
    default:
      return []
  }
})

const rankedAuthors = computed<AuthorRow[]>(() => {
  if (board.value !== 'authors') return []
  const map = new Map<string, Book>()
  for (const book of byHeat(catalog.value)) {
    if (!map.has(book.author)) map.set(book.author, book)
  }
  return [...map.entries()].map(([author, book]) => ({ author, book }))
})

function metric(book: Book) {
  if (board.value === 'read' || board.value === 'drought') {
    return chaptersOf(book) ? `${chaptersOf(book)}章` : book.heat
  }
  if (board.value === 'serialize') {
    return book.status
  }
  return book.heat
}

function select(id: BoardId) {
  void router.replace({ name: 'ranks', query: id === 'recommend' ? {} : { board: id } })
}

function openBook(id: number) {
  void router.push({ name: 'book', params: { id: String(id) } })
}

function openAuthor(author: string) {
  void router.push({ name: 'search', query: { q: author, by: 'author' } })
}

watch(
  () => route.query.board,
  () => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  },
)

onMounted(async () => {
  try {
    const [list, feed] = await Promise.all([fetchBooks(), fetchHomeFeed()])
    catalog.value = list
    recommend.value = feed.recommendBooks
    newer.value = feed.newBooks
  } catch {
    catalog.value = mockBooks
    recommend.value = mockBooks.slice(0, 8)
    newer.value = [...mockBooks].reverse().slice(0, 8)
    loadError.value = '后端未连接，当前为本地示例结果'
  }
})
</script>

<template>
  <div class="page">
    <AppHeader />
    <main class="container main">
      <div class="layout">
        <aside class="side">
          <button
            v-for="item in boards"
            :key="item.id"
            type="button"
            class="side__item"
            :class="{ 'is-on': board === item.id }"
            @click="select(item.id)"
          >
            {{ item.label }}
          </button>
        </aside>

        <section class="panel">
          <div class="section-title">
            <h2>{{ current.label }}</h2>
            <span>{{ current.hint }}</span>
          </div>
          <p v-if="loadError" class="tip">{{ loadError }}</p>
          <template v-else-if="board === 'authors'">
            <p v-if="!rankedAuthors.length" class="tip">这个榜单暂时没有作者</p>
            <ol v-else class="list">
              <li v-for="(row, index) in rankedAuthors" :key="row.author">
                <button type="button" @click="openAuthor(row.author)">
                  <em :class="{ gold: index === 0, silver: index === 1, bronze: index === 2 }">
                    {{ index + 1 }}
                  </em>
                  <span class="avatar">{{ row.author.slice(0, 1) }}</span>
                  <div>
                    <strong>{{ row.author }}</strong>
                    <p>代表作 · {{ row.book.title }}</p>
                  </div>
                  <span class="heat">{{ row.book.heat }}</span>
                </button>
              </li>
            </ol>
          </template>
          <template v-else>
            <p v-if="!rankedBooks.length" class="tip">这个榜单暂时没有作品</p>
            <ol v-else class="list">
              <li v-for="(book, index) in rankedBooks" :key="book.id">
                <button type="button" @click="openBook(book.id)">
                  <em :class="{ gold: index === 0, silver: index === 1, bronze: index === 2 }">
                    {{ index + 1 }}
                  </em>
                  <img v-if="book.cover" :src="book.cover" :alt="book.title" />
                  <span v-else class="fallback">{{ book.title.slice(0, 1) }}</span>
                  <div>
                    <strong>{{ book.title }}</strong>
                    <p>{{ book.author }} · {{ book.category }} · {{ book.status }}</p>
                  </div>
                  <span class="heat">{{ metric(book) }}</span>
                </button>
              </li>
            </ol>
          </template>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
}

.main {
  padding: 20px 0 64px;
}

.layout {
  display: grid;
  grid-template-columns: 148px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.side {
  position: sticky;
  top: calc(var(--header-total) + 12px);
  padding: 10px 0;
  background: var(--surface);
  border-radius: 16px;
  box-shadow: var(--shadow-soft);
}

.side__item {
  display: block;
  width: 100%;
  padding: 13px 22px;
  border: 0;
  background: transparent;
  color: var(--ink-muted);
  font-size: 15px;
  letter-spacing: 0.04em;
  text-align: left;
  cursor: pointer;
  transition: color 0.15s, font-weight 0.15s;
}

.side__item:hover {
  color: var(--ink-secondary);
}

.side__item.is-on {
  color: var(--ink);
  font-weight: 700;
}

.panel {
  min-width: 0;
}

.section-title {
  margin-bottom: 12px;
}

.section-title span {
  font-size: 14px;
  color: var(--ink-muted);
}

.tip {
  margin-top: 16px;
  color: var(--ink-muted);
  font-size: 14px;
}

.list {
  list-style: none;
  background: var(--surface);
  border-radius: 16px;
  padding: 8px 12px;
  box-shadow: var(--shadow-soft);
}

.list button {
  display: grid;
  grid-template-columns: 36px 56px 1fr auto;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 12px 8px;
  border: 0;
  border-bottom: 1px solid #f3f0ec;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.list li:last-child button {
  border-bottom: 0;
}

.list button:hover strong {
  color: var(--brand);
}

.list em {
  font-style: normal;
  font-size: 16px;
  font-weight: 800;
  text-align: center;
  color: #b8bcc4;
}

.list em.gold {
  color: #e8a317;
}

.list em.silver {
  color: #8f9aa8;
}

.list em.bronze {
  color: #c47a4a;
}

.list img,
.fallback,
.avatar {
  width: 56px;
  height: 74px;
  border-radius: 6px;
  object-fit: cover;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
}

.fallback,
.avatar {
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 700;
}

.avatar {
  height: 56px;
  width: 56px;
  border-radius: 50%;
  font-size: 18px;
}

.list strong {
  display: block;
  font-size: 16px;
}

.list p {
  margin-top: 6px;
  font-size: 13px;
  color: var(--ink-muted);
}

.heat {
  font-size: 13px;
  font-weight: 700;
  color: var(--brand);
  white-space: nowrap;
}

@media (max-width: 760px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .side {
    position: static;
    display: flex;
    overflow-x: auto;
    padding: 6px 8px;
  }

  .side__item {
    flex: 0 0 auto;
    padding: 10px 14px;
    white-space: nowrap;
  }
}
</style>
