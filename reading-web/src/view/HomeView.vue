<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import BannerCarousel from '@/components/BannerCarousel.vue'
import BookSection from '@/components/BookSection.vue'
import RankList from '@/components/RankList.vue'
import BookCard from '@/components/BookCard.vue'
import ChannelPanel from '@/components/ChannelPanel.vue'
import { fetchHomeFeed } from '@/api/book'
import {
  banners as mockBanners,
  recommendBooks as mockRecommend,
  finishedBooks as mockFinished,
  newBooks as mockNew,
  hotRank as mockHotRank,
  maleBooks as mockMale,
  femaleBooks as mockFemale,
  guessBooks as mockGuess,
  type BannerItem,
  type Book,
} from '@/data/mockBooks'

const highlights = [
  { label: '今日必读', value: '12 部上新' },
  { label: '连载更新', value: '86 章' },
  { label: '完结佳作', value: '320+' },
  { label: '在读用户', value: '1.2 万' },
]

const loading = ref(true)
const loadError = ref('')
const banners = ref<BannerItem[]>([...mockBanners])
const recommendBooks = ref<Book[]>([...mockRecommend])
const finishedBooks = ref<Book[]>([...mockFinished])
const newBooks = ref<Book[]>([...mockNew])
const hotRank = ref<Book[]>([...mockHotRank])
const maleBooks = ref<Book[]>([...mockMale])
const femaleBooks = ref<Book[]>([...mockFemale])
const guessBooks = ref<Book[]>([...mockGuess])

onMounted(async () => {
  try {
    const data = await fetchHomeFeed()
    banners.value = data.banners
    recommendBooks.value = data.recommendBooks
    finishedBooks.value = data.finishedBooks
    newBooks.value = data.newBooks
    hotRank.value = data.hotRank
    maleBooks.value = data.maleBooks
    femaleBooks.value = data.femaleBooks
    guessBooks.value = data.guessBooks
    loadError.value = ''
  } catch {
    loadError.value = '后端未连接，当前展示本地示例数据'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page">
    <AppHeader :with-cats="true" />

    <main class="container main">
      <p v-if="loadError" class="api-tip">{{ loadError }}</p>
      <p v-else-if="loading" class="api-tip">正在同步书城数据…</p>

      <section class="top-row">
        <BannerCarousel :items="banners" />
        <RankList :books="hotRank" />
      </section>

      <section class="highlights">
        <div v-for="item in highlights" :key="item.label" class="highlight">
          <span class="highlight__label">{{ item.label }}</span>
          <strong class="highlight__value">{{ item.value }}</strong>
        </div>
      </section>

      <section class="guess">
        <div class="section-title">
          <h2>猜你喜欢</h2>
          <a href="javascript:;" class="more">换一批 ›</a>
        </div>
        <div class="guess__track">
          <BookCard v-for="book in guessBooks" :key="book.id" :book="book" compact />
        </div>
      </section>

      <section class="channels">
        <ChannelPanel
          title="男生频道"
          subtitle="玄幻都市 · 热血爽文"
          :books="maleBooks"
          tone="var(--channel-male)"
        />
        <ChannelPanel
          title="女生频道"
          subtitle="甜宠治愈 · 高糖言情"
          :books="femaleBooks"
          tone="var(--channel-female)"
        />
      </section>

      <BookSection title="编辑力荐" :books="recommendBooks" />

      <section class="shelf">
        <div class="section-title">
          <h2>新书速递</h2>
          <a href="javascript:;" class="more">查看更多 ›</a>
        </div>
        <div class="shelf__track">
          <BookCard v-for="book in newBooks" :key="book.id" :book="book" compact />
        </div>
      </section>

      <BookSection title="完结佳作" :books="finishedBooks" more-text="更多完结 ›" />
    </main>

    <footer class="footer">
      <div class="container footer__inner">
        <div class="footer__brand">
          <span class="footer__mark" aria-hidden="true">阅</span>
          <div>
            <p class="logo">阅界 · 小说阅读管理系统</p>
            <p class="hint">沉浸阅读 · 发现好书 · 管理你的书海</p>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main {
  flex: 1;
  padding: 20px 0 64px;
  width: min(1280px, calc(100% - 32px));
}

.api-tip {
  margin-bottom: 14px;
  padding: 10px 14px;
  border-radius: 10px;
  background: #fff6e8;
  color: #c47a4a;
  font-size: 13px;
}

.top-row {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(260px, 0.9fr);
  gap: 16px;
  align-items: stretch;
}

.highlights {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-top: 16px;
}

.highlight {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 12px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-soft);
}

.highlight__label {
  font-size: 12px;
  color: var(--ink-muted);
}

.highlight__value {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 400;
  color: var(--ink);
}

.guess {
  margin-top: 28px;
}

.guess__track {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
}

.channels {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 28px;
}

.shelf {
  margin-top: 40px;
  padding: 22px 20px 10px;
  border-radius: 16px;
  background: var(--shelf-bg);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-soft);
}

.shelf__track {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
  padding-bottom: 10px;
}

.footer {
  border-top: 1px solid var(--header-line);
  background: var(--header-bg);
  padding: 36px 0;
}

.footer__inner {
  width: min(1280px, calc(100% - 32px));
}

.footer__brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
}

.footer__mark {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: var(--brand);
  color: #fff;
  font-family: var(--font-display);
  font-size: 22px;
  box-shadow: 0 10px 24px rgba(255, 77, 58, 0.28);
}

.logo {
  font-family: var(--font-display);
  font-size: 18px;
  color: var(--ink-secondary);
  letter-spacing: 0.12em;
}

.hint {
  margin-top: 6px;
  font-size: 13px;
  color: var(--ink-muted);
}

@media (max-width: 1080px) {
  .top-row {
    grid-template-columns: 1fr;
  }

  .channels {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 980px) {
  .guess__track,
  .shelf__track {
    grid-template-columns: repeat(3, 1fr);
  }

  .highlights {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 560px) {
  .guess__track,
  .shelf__track {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
