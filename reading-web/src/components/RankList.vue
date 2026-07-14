<script setup lang="ts">
import type { Book } from '@/data/mockBooks'

withDefaults(
  defineProps<{
    books: Book[]
    title?: string
    limit?: number
  }>(),
  {
    title: '热度榜',
    limit: 8,
  },
)
</script>

<template>
  <aside class="rank">
    <div class="rank__head">
      <h2>{{ title }}</h2>
      <span class="live">实时</span>
    </div>
    <ol>
      <li v-for="(book, i) in books.slice(0, limit)" :key="book.id">
        <span class="idx" :class="{ gold: i === 0, silver: i === 1, bronze: i === 2 }">
          {{ i + 1 }}
        </span>
        <div class="info">
          <p class="title">{{ book.title }}</p>
          <p class="sub">{{ book.author }}</p>
        </div>
        <span class="heat">{{ book.heat }}</span>
      </li>
    </ol>
  </aside>
</template>

<style scoped>
.rank {
  height: 220px;
  background: var(--surface);
  border-radius: 16px;
  padding: 14px 14px 10px;
  box-shadow: var(--shadow-soft);
  border: 1px solid rgba(0, 0, 0, 0.03);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.rank__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 0 4px 8px;
  border-bottom: 1px solid var(--line);
}

.rank__head h2 {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 400;
}

.live {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--brand-soft);
  color: var(--brand);
  font-weight: 600;
}

ol {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: auto;
  flex: 1;
  scrollbar-width: thin;
}

li {
  display: grid;
  grid-template-columns: 22px 1fr auto;
  gap: 8px;
  align-items: center;
  padding: 5px 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

li:hover {
  background: var(--brand-soft);
}

li:hover .title {
  color: var(--brand);
}

.idx {
  font-size: 13px;
  font-weight: 700;
  text-align: center;
  color: var(--ink-muted);
}

.idx.gold {
  color: #e8a317;
}

.idx.silver {
  color: #8f9aa8;
}

.idx.bronze {
  color: #c47a4a;
}

.title {
  font-size: 13px;
  font-weight: 600;
  line-height: 1.3;
  transition: color 0.2s;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.sub {
  margin-top: 1px;
  font-size: 11px;
  color: var(--ink-muted);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.heat {
  font-size: 11px;
  color: var(--brand);
  white-space: nowrap;
}
</style>
