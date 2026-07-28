<script setup lang="ts">
import SideIcon, { type SideIconName } from '@/components/SideIcon.vue'

const stats = [
  { label: '在架作品', value: '0', tip: '作品落库后同步' },
  { label: '累计字数', value: '0', tip: '含草稿与已发章节' },
  { label: '本月更新', value: '0 章', tip: '按自然月统计' },
  { label: '总阅读量', value: '0', tip: '全站累计阅读' },
]

const shortcuts: { to: string; title: string; desc: string; icon: SideIconName }[] = [
  { to: '/author/works', title: '作品管理', desc: '新建作品、编辑简介与状态', icon: 'works' },
  { to: '/author/chapters', title: '章节连载', desc: '写作、发布与定时更新', icon: 'chapters' },
  { to: '/author/stats', title: '数据中心', desc: '阅读与收藏趋势概览', icon: 'stats' },
  { to: '/author/profile', title: '个人信息', desc: '笔名、简介与账号安全', icon: 'profile' },
]
</script>

<template>
  <div class="overview">
    <div class="stats">
      <article v-for="item in stats" :key="item.label" class="stat">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.tip }}</p>
      </article>
    </div>

    <section class="block">
      <div class="block__head">
        <h2>快捷入口</h2>
      </div>
      <div class="cards">
        <RouterLink v-for="item in shortcuts" :key="item.to" :to="item.to">
          <span class="card-icon" aria-hidden="true">
            <SideIcon :name="item.icon" :size="16" />
          </span>
          <strong>{{ item.title }}</strong>
          <span>{{ item.desc }}</span>
        </RouterLink>
      </div>
    </section>

    <section class="block tip">
      <h2>创作提示</h2>
      <p>登录与资料能力已接通。作品、章节与数据模块可在此布局内继续扩展，风格与读者端、管理端保持一致。</p>
    </section>
  </div>
</template>

<style scoped>
.overview {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat {
  background:
    linear-gradient(135deg, rgba(255, 77, 58, 0.05), transparent 48%),
    #fff;
  border-radius: 16px;
  padding: 18px 16px;
  border: 1px solid rgba(0, 0, 0, 0.045);
  box-shadow: var(--shadow-soft);
}

.stat span {
  font-size: 12px;
  color: var(--ink-muted);
}

.stat strong {
  display: block;
  margin-top: 8px;
  font-family: var(--font-serif);
  font-size: 28px;
  font-weight: 600;
  color: var(--ink);
}

.stat p {
  margin-top: 8px;
  font-size: 12px;
  color: var(--ink-muted);
}

.block {
  background: #fff;
  border-radius: 16px;
  padding: 22px 24px 24px;
  border: 1px solid rgba(0, 0, 0, 0.045);
  box-shadow: var(--shadow-soft);
}

.block__head h2,
.tip h2 {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.cards {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.cards a {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px 16px 16px 18px;
  border-radius: 12px;
  background: #f8f6f2;
  border: 1px solid transparent;
  overflow: hidden;
  transition: background 0.2s, border-color 0.2s, transform 0.2s, box-shadow 0.2s;
}

.cards a::before {
  content: '';
  position: absolute;
  left: 0;
  top: 14px;
  bottom: 14px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: transparent;
  transition: background 0.2s;
}

.cards a:hover {
  background: #fff;
  border-color: rgba(255, 77, 58, 0.18);
  box-shadow: 0 8px 20px rgba(255, 77, 58, 0.07);
  transform: translateY(-2px);
}

.cards a:hover::before {
  background: var(--brand);
}

.card-icon {
  width: 32px;
  height: 32px;
  border-radius: 9px;
  display: grid;
  place-items: center;
  background: rgba(255, 77, 58, 0.1);
  color: var(--brand);
}

.cards strong {
  font-size: 15px;
  color: var(--ink);
}

.cards span {
  font-size: 13px;
  color: var(--ink-muted);
  line-height: 1.5;
}

.tip p {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--ink-secondary);
}

@media (max-width: 980px) {
  .stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .cards {
    grid-template-columns: 1fr;
  }
}
</style>
