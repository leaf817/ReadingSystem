<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import BookCard from '@/components/BookCard.vue'
import { fetchBooks } from '@/api/book'
import { books as mockBooks, type Book } from '@/data/mockBooks'
import { matchBookBy, parseSearchBy, searchByMeta } from '@/utils/bookSearch'

const route = useRoute()
const loading = ref(false)
const loadError = ref('')
const results = ref<Book[]>([])

const keyword = computed(() => {
  const q = route.query.q
  return typeof q === 'string' ? q.trim() : ''
})

const searchBy = computed(() => parseSearchBy(route.query.by))
const searchLabel = computed(() => searchByMeta(searchBy.value).label)

async function search() {
  const q = keyword.value
  if (!q) {
    results.value = []
    loadError.value = ''
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    results.value = await fetchBooks(q, searchBy.value)
  } catch {
    results.value = mockBooks.filter((book) => matchBookBy(book, q, searchBy.value))
    loadError.value = '后端未连接，当前为本地示例结果'
  } finally {
    loading.value = false
  }
}

watch(
  () => [keyword.value, searchBy.value],
  () => {
    void search()
  },
  { immediate: true },
)
</script>

<template>
  <div class="page">
    <AppHeader />
    <main class="container main">
      <div class="section-title">
        <h2>搜索结果</h2>
        <span v-if="keyword">{{ searchLabel }} “{{ keyword }}”</span>
      </div>
      <p v-if="loadError" class="tip">{{ loadError }}</p>
      <p v-else-if="loading" class="tip">正在搜索…</p>
      <p v-else-if="!keyword" class="tip">请选择搜索方式并输入关键词</p>
      <p v-else-if="!results.length" class="tip">没有找到与“{{ keyword }}”相关的作品</p>
      <div v-else class="grid">
        <BookCard v-for="book in results" :key="book.id" :book="book" :highlight="keyword" />
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

.tip {
  margin-top: 8px;
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
