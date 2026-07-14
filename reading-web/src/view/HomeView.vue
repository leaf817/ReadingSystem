<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
import BannerCarousel from '@/components/BannerCarousel.vue'
import BookSection from '@/components/BookSection.vue'
import RankList from '@/components/RankList.vue'
import BookCard from '@/components/BookCard.vue'
import ChannelPanel from '@/components/ChannelPanel.vue'
import {
  banners,
  recommendBooks,
  finishedBooks,
  newBooks,
  hotRank,
  maleBooks,
  femaleBooks,
  guessBooks,
} from '@/data/mockBooks'

const highlights = [
  { label: '今日必读', value: '12 部上新' },
  { label: '连载更新', value: '86 章' },
  { label: '完结佳作', value: '320+' },
  { label: '在读用户', value: '1.2 万' },
]
</script>

<template>
  <div class="page">
    <AppHeader />

    <main class="container main">
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
          tone="linear-gradient(160deg, #f7fbff, #eef5ff)"
        />
        <ChannelPanel
          title="女生频道"
          subtitle="甜宠治愈 · 高糖言情"
          :books="femaleBooks"
          tone="linear-gradient(160deg, #fff7fa, #ffeef4)"
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
  background: #fff;
  border: 1px solid rgba(255, 120, 100, 0.1);
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
  background: linear-gradient(165deg, #fff, #fff5f1);
  border: 1px solid rgba(255, 77, 58, 0.08);
  box-shadow: var(--shadow-soft);
}

.shelf__track {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
  padding-bottom: 10px;
}

.footer {
  border-top: 1px solid rgba(120, 160, 220, 0.2);
  background: linear-gradient(180deg, #f3f8ff 0%, #eef5ff 55%, #e8f1ff 100%);
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
