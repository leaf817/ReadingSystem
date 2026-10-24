<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import BookCard from '@/components/BookCard.vue'
import { fetchBooks } from '@/api/book'
import { books as mockBooks, categories, type Book } from '@/data/mockBooks'
import { contentTagsOf } from '@/utils/bookSearch'

const route = useRoute()
const router = useRouter()
const catalog = ref<Book[]>([])
const loadError = ref('')

const current = computed(() => {
  const cat = route.query.cat
  return typeof cat === 'string' && cat.trim() ? cat.trim() : '推荐'
})

function matchCategory(book: Book, cat: string) {
  if (!cat || cat === '推荐') return true
  if (cat === '完结') return book.status === '完结'
  if (cat === '男生') return ['玄幻', '都市', '历史', '科幻'].includes(book.category)
  if (cat === '女生') {
    return book.category === '言情' || book.tags.includes('甜宠') || book.tags.includes('治愈')
  }
  return book.category === cat || contentTagsOf(book).includes(cat)
}

const filtered = computed(() => catalog.value.filter((book) => matchCategory(book, current.value)))

function select(cat: string) {
  void router.push(cat === '推荐' ? { name: 'category' } : { name: 'category', query: { cat } })
}

onMounted(async () => {
  try {
    catalog.value = await fetchBooks()
  } catch {
    catalog.value = mockBooks
    loadError.value = '后端未连接，当前为本地示例结果'
  }
})
</script>

<template>
  <div class="page">
    <AppHeader />
    <main class="container main">
      <div class="section-title">
        <h2>分类</h2>
        <span>{{ current }} · {{ filtered.length }} 部</span>
      </div>
      <div class="chips">
        <button
          v-for="cat in categories"
          :key="cat"
          type="button"
          class="chip"
          :class="{ 'is-on': current === cat }"
          @click="select(cat)"
        >
          {{ cat }}
        </button>
      </div>
      <p v-if="loadError" class="tip">{{ loadError }}</p>
      <p v-else-if="!catalog.length" class="tip">正在加载作品…</p>
      <p v-else-if="!filtered.length" class="tip">这个分类暂时没有作品</p>
      <div v-else class="grid">
        <BookCard v-for="book in filtered" :key="book.id" :book="book" />
      </div>
    </main>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
}

.main {
  padding: 28px 0 64px;
}

.section-title span {
  font-size: 14px;
  color: var(--ink-muted);
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 8px 0 8px;
}

.chip {
  height: 34px;
  padding: 0 14px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: var(--surface);
  color: var(--nav-muted);
  font-size: 13px;
  cursor: pointer;
}

.chip.is-on,
.chip:hover {
  color: var(--brand);
  border-color: rgba(255, 77, 58, 0.35);
  background: var(--brand-soft);
}

.tip {
  margin-top: 16px;
  color: var(--ink-muted);
  font-size: 14px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 28px 20px;
  margin-top: 20px;
}

@media (max-width: 980px) {
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 720px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
