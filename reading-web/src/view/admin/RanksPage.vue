<script setup lang="ts">
import { ref } from 'vue'
import { adminHotRank } from './adminData'
import type { Book } from '@/data/mockBooks'
import './admin-shared.css'

const rank = ref<Book[]>([...adminHotRank])
const toast = ref('')

function pinTop(index: number) {
  if (index <= 0) return
  const next = [...rank.value]
  const [item] = next.splice(index, 1)
  if (!item) return
  next.unshift(item)
  rank.value = next
  toast.value = `已将《${item.title}》置顶`
  window.setTimeout(() => {
    toast.value = ''
  }, 1800)
}

function refresh() {
  rank.value = [...adminHotRank]
  toast.value = '榜单已按热度重新排序'
  window.setTimeout(() => {
    toast.value = ''
  }, 1800)
}
</script>

<template>
  <section class="admin-block">
    <div class="admin-block__head">
      <h2>热度榜配置</h2>
      <button type="button" class="admin-btn admin-btn--primary" @click="refresh">刷新榜单</button>
    </div>

    <ul class="admin-list">
      <li v-for="(book, i) in rank" :key="book.id">
        <span class="admin-idx">{{ i + 1 }}</span>
        <div>
          <strong>{{ book.title }}</strong>
          <p>{{ book.author }} · {{ book.heat }}热度 · {{ book.category }}</p>
        </div>
        <button type="button" class="admin-mini" :disabled="i === 0" @click="pinTop(i)">
          {{ i === 0 ? '已置顶' : '置顶' }}
        </button>
      </li>
    </ul>

    <p v-if="toast" class="admin-toast">{{ toast }}</p>
  </section>
</template>
