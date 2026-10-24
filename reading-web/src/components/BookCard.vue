<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Book } from '@/data/mockBooks'
import { contentTagsOf } from '@/utils/bookSearch'

const props = defineProps<{
  book: Book
  compact?: boolean
  highlight?: string
}>()

const visibleTags = computed(() => contentTagsOf(props.book))

function tagHit(tag: string) {
  const q = props.highlight?.trim().toLowerCase()
  return Boolean(q) && tag.toLowerCase().includes(q)
}

const router = useRouter()

function openBook() {
  router.push({ name: 'book', params: { id: String(props.book.id) } })
}
</script>

<template>
  <article class="card" :class="{ 'is-compact': compact }" role="link" tabindex="0" @click="openBook" @keydown.enter="openBook">
    <div class="cover-wrap">
      <img :src="book.cover" :alt="book.title" class="cover" loading="lazy" />
      <span class="status" :class="book.status === '完结' ? 'is-done' : 'is-serial'">
        {{ book.status }}
      </span>
      <div class="cover-mask">
        <span>开始阅读</span>
      </div>
    </div>
    <div class="meta">
      <h3 class="title">{{ book.title }}</h3>
      <p class="author">{{ book.author }}</p>
      <p v-if="!compact" class="desc">{{ book.desc }}</p>
      <div class="tags">
        <span
          v-for="tag in visibleTags.slice(0, 4)"
          :key="tag"
          :class="{ 'is-hit': tagHit(tag) }"
        >{{ tag }}</span>
        <span class="heat">{{ book.heat }}热度</span>
      </div>
    </div>
  </article>
</template>

<style scoped>
.card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.28s ease;
}

.card:hover {
  transform: translateY(-6px);
}

.card:hover .title {
  color: var(--brand);
}

.card:hover .cover-mask {
  opacity: 1;
}

.cover-wrap {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  aspect-ratio: 3 / 4;
  box-shadow: var(--shadow-soft);
  background: #e8e4de;
}

.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.45s ease;
}

.card:hover .cover {
  transform: scale(1.06);
}

.cover-mask {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  background: linear-gradient(180deg, transparent 35%, rgba(0, 0, 0, 0.55));
  opacity: 0;
  transition: opacity 0.28s ease;
}

.cover-mask span {
  margin-top: auto;
  margin-bottom: 16px;
  padding: 8px 14px;
  border-radius: 999px;
  background: #fff;
  color: var(--brand);
  font-size: 12px;
  font-weight: 600;
}

.status {
  position: absolute;
  left: 8px;
  top: 8px;
  z-index: 1;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 6px;
  color: #fff;
  backdrop-filter: blur(4px);
}

.status.is-serial {
  background: rgba(40, 90, 180, 0.82);
}

.status.is-done {
  background: rgba(255, 77, 58, 0.92);
}

.title {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.35;
  transition: color 0.2s;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.author {
  font-size: 12px;
  color: var(--ink-muted);
  margin-top: 3px;
}

.desc {
  margin-top: 8px;
  font-size: 12px;
  color: var(--ink-secondary);
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.tags span {
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 6px;
  background: var(--chip);
  color: var(--ink-secondary);
}

.tags .heat,
.tags .is-hit {
  background: var(--brand-soft);
  color: var(--brand);
}

.is-compact .desc,
.is-compact .cover-mask {
  display: none;
}

.is-compact .cover-wrap {
  border-radius: 10px;
}
</style>
