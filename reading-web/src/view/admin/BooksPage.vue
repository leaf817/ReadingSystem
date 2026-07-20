<script setup lang="ts">
import { computed, ref } from 'vue'
import { adminBooks, type AdminBook } from './adminData'
import './admin-shared.css'

const keyword = ref('')
const list = ref<AdminBook[]>(adminBooks.map((b) => ({ ...b })))
const toast = ref('')

const filtered = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  if (!q) return list.value
  return list.value.filter(
    (b) =>
      b.title.toLowerCase().includes(q) ||
      b.author.toLowerCase().includes(q) ||
      b.category.includes(q),
  )
})

function toggleShelf(book: AdminBook) {
  book.shelf = book.shelf === '上架' ? '下架' : '上架'
  toast.value = `《${book.title}》已${book.shelf}`
  window.setTimeout(() => {
    toast.value = ''
  }, 1800)
}
</script>

<template>
  <section class="admin-block">
    <div class="admin-block__head">
      <h2>书籍列表</h2>
      <div class="admin-toolbar">
        <input v-model="keyword" class="admin-search" type="search" placeholder="搜索书名 / 作者 / 分类" />
        <button type="button" class="admin-btn admin-btn--primary">新增书籍</button>
      </div>
    </div>

    <div class="admin-table-wrap">
      <table class="admin-table">
        <thead>
          <tr>
            <th>书名</th>
            <th>作者</th>
            <th>分类</th>
            <th>连载状态</th>
            <th>上架状态</th>
            <th>热度</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="book in filtered" :key="book.id">
            <td>{{ book.title }}</td>
            <td>{{ book.author }}</td>
            <td>{{ book.category }}</td>
            <td>
              <span class="admin-tag" :class="book.status === '完结' ? 'admin-tag--done' : 'admin-tag--serial'">
                {{ book.status }}
              </span>
            </td>
            <td>
              <span class="admin-tag" :class="book.shelf === '上架' ? 'admin-tag--on' : 'admin-tag--off'">
                {{ book.shelf }}
              </span>
            </td>
            <td>{{ book.heat }}</td>
            <td class="admin-ops">
              <button type="button">编辑</button>
              <button type="button" @click="toggleShelf(book)">
                {{ book.shelf === '上架' ? '下架' : '上架' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p v-if="!filtered.length" class="admin-empty">没有匹配的书籍</p>
    <p v-if="toast" class="admin-toast">{{ toast }}</p>
  </section>
</template>
