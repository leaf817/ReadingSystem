<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import type { BannerItem } from '@/data/mockBooks'

const props = defineProps<{
  items: BannerItem[]
}>()

const index = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

function go(i: number) {
  index.value = (i + props.items.length) % props.items.length
  restart()
}

function next() {
  index.value = (index.value + 1) % props.items.length
}

function restart() {
  if (timer) clearInterval(timer)
  timer = setInterval(next, 4500)
}

onMounted(() => {
  timer = setInterval(next, 4500)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <section class="banner" aria-label="精选推荐">
    <div
      v-for="(item, i) in items"
      :key="item.id"
      class="slide"
      :class="{ 'is-active': i === index }"
      :style="{ background: item.tone }"
    >
      <div class="slide__glow" aria-hidden="true" />
      <div class="slide__content">
        <p class="eyebrow">本周主推</p>
        <h2>{{ item.title }}</h2>
        <p class="sub">{{ item.subtitle }}</p>
        <button type="button" class="cta">立即阅读</button>
      </div>
      <div class="slide__book" aria-hidden="true">
        <div class="book-face">
          <span>{{ item.title.slice(0, 2) }}</span>
        </div>
      </div>
    </div>

    <button type="button" class="nav prev" aria-label="上一张" @click="go(index - 1)">‹</button>
    <button type="button" class="nav next" aria-label="下一张" @click="go(index + 1)">›</button>

    <div class="dots">
      <button
        v-for="(item, i) in items"
        :key="item.id"
        type="button"
        :class="{ 'is-active': i === index }"
        :aria-label="`切换到 ${item.title}`"
        @click="go(i)"
      />
    </div>
  </section>
</template>

<style scoped>
.banner {
  position: relative;
  height: 220px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--shadow-soft);
}

.slide {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.55s ease;
  pointer-events: none;
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 20px;
  padding: 0 36px;
}

.slide.is-active {
  opacity: 1;
  pointer-events: auto;
}

.slide__glow {
  position: absolute;
  right: -20%;
  top: -30%;
  width: 55%;
  height: 140%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.2), transparent 68%);
  pointer-events: none;
}

.slide__content {
  position: relative;
  z-index: 2;
  color: #fff;
  max-width: 420px;
}

.slide.is-active .slide__content {
  animation: rise 0.55s ease both;
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.eyebrow {
  font-size: 11px;
  letter-spacing: 0.24em;
  opacity: 0.78;
  margin-bottom: 8px;
}

.slide h2 {
  font-family: var(--font-display);
  font-size: 30px;
  font-weight: 400;
  letter-spacing: 0.06em;
  margin-bottom: 8px;
}

.sub {
  font-size: 13px;
  line-height: 1.55;
  opacity: 0.88;
  margin-bottom: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cta {
  border: 0;
  height: 34px;
  padding: 0 18px;
  border-radius: 999px;
  background: #fff;
  color: var(--brand);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.cta:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.18);
}

.slide__book {
  position: relative;
  z-index: 2;
  width: 96px;
}

.book-face {
  width: 96px;
  height: 128px;
  border-radius: 8px;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.28), rgba(255, 255, 255, 0.06)),
    rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.28);
  box-shadow: 10px 14px 28px rgba(0, 0, 0, 0.22);
  display: grid;
  place-items: center;
}

.book-face span {
  font-family: var(--font-display);
  font-size: 24px;
  letter-spacing: 0.1em;
  color: #fff;
}

.nav {
  position: absolute;
  top: 50%;
  z-index: 4;
  translate: 0 -50%;
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  backdrop-filter: blur(8px);
  opacity: 0;
  transition: opacity 0.2s, background 0.2s;
}

.banner:hover .nav {
  opacity: 1;
}

.nav:hover {
  background: rgba(255, 255, 255, 0.32);
}

.nav.prev {
  left: 10px;
}

.nav.next {
  right: 10px;
}

.dots {
  position: absolute;
  left: 36px;
  bottom: 14px;
  z-index: 3;
  display: flex;
  gap: 6px;
}

.dots button {
  width: 7px;
  height: 7px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  transition: width 0.25s, background 0.25s;
}

.dots button.is-active {
  width: 18px;
  background: #fff;
}

@media (max-width: 640px) {
  .banner {
    height: 190px;
  }

  .slide {
    padding: 0 20px;
    grid-template-columns: 1fr;
  }

  .slide__book,
  .nav {
    display: none;
  }

  .slide h2 {
    font-size: 24px;
  }

  .dots {
    left: 20px;
  }
}
</style>
