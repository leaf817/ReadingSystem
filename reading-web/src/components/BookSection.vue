<script setup lang="ts">
import BookCard from '@/components/BookCard.vue'
import type { Book } from '@/data/mockBooks'

defineProps<{
  title: string
  books: Book[]
  moreText?: string
}>()
</script>

<template>
  <section class="section">
    <div class="section-title">
      <h2>{{ title }}</h2>
      <a href="javascript:;" class="more">{{ moreText || '查看更多 ›' }}</a>
    </div>
    <div class="grid">
      <BookCard v-for="book in books" :key="book.id" :book="book" />
    </div>
  </section>
</template>

<style scoped>
.section {
  margin-top: 42px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 28px 20px;
}

@media (max-width: 980px) {
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 680px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px 12px;
  }
}
</style>
