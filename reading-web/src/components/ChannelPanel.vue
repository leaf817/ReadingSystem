<script setup lang="ts">
import type { Book } from '@/data/mockBooks'

defineProps<{
  title: string
  subtitle: string
  books: Book[]
  tone?: string
}>()
</script>

<template>
  <section class="channel" :style="{ '--tone': tone || 'linear-gradient(135deg, #fff5f1, #ffe8e1)' }">
    <div class="channel__head">
      <div>
        <h3>{{ title }}</h3>
        <p>{{ subtitle }}</p>
      </div>
      <a href="javascript:;" class="more">更多 ›</a>
    </div>
    <div class="channel__list">
      <article v-for="book in books.slice(0, 4)" :key="book.id" class="item">
        <img :src="book.cover" :alt="book.title" />
        <div class="meta">
          <h4>{{ book.title }}</h4>
          <p class="desc">{{ book.desc }}</p>
          <p class="author">{{ book.author }} · {{ book.category }}</p>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.channel {
  background: var(--tone);
  border-radius: 16px;
  padding: 16px 16px 8px;
  border: 1px solid rgba(255, 120, 100, 0.1);
  box-shadow: var(--shadow-soft);
}

.channel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.channel__head h3 {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 400;
  letter-spacing: 0.04em;
}

.channel__head p {
  margin-top: 4px;
  font-size: 12px;
  color: var(--ink-muted);
}

.more {
  font-size: 12px;
  color: var(--ink-muted);
  white-space: nowrap;
  padding-top: 4px;
}

.more:hover {
  color: var(--brand);
}

.channel__list {
  display: flex;
  flex-direction: column;
}

.item {
  display: grid;
  grid-template-columns: 48px 1fr;
  gap: 12px;
  align-items: center;
  padding: 10px 4px;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
}

.item:first-child {
  border-top: 0;
}

.item:hover {
  background: rgba(255, 255, 255, 0.55);
}

.item:hover h4 {
  color: var(--brand);
}

.item img {
  width: 48px;
  height: 64px;
  object-fit: cover;
  border-radius: 6px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
}

h4 {
  font-size: 14px;
  font-weight: 600;
  transition: color 0.2s;
}

.desc {
  margin-top: 4px;
  font-size: 12px;
  color: var(--ink-secondary);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.author {
  margin-top: 4px;
  font-size: 11px;
  color: var(--ink-muted);
}
</style>
