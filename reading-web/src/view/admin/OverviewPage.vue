<script setup lang="ts">
import SideIcon, { type SideIconName } from '@/components/SideIcon.vue'
import { adminHotRank, adminStats, reviewQueue } from './adminData'
import './admin-shared.css'

const shortcuts: { to: string; label: string; icon: SideIconName }[] = [
  { to: '/admin/books', label: '书籍上下架', icon: 'books' },
  { to: '/admin/users', label: '用户与作者', icon: 'users' },
  { to: '/admin/ranks', label: '榜单运营', icon: 'ranks' },
  { to: '/admin/reviews', label: '内容审核', icon: 'reviews' },
]
</script>

<template>
  <div class="overview">
    <div class="stats">
      <article v-for="item in adminStats" :key="item.label" class="stat">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.tip }}</p>
      </article>
    </div>

    <div class="grid">
      <section class="admin-block">
        <div class="admin-block__head">
          <h2>热度榜速览</h2>
          <RouterLink class="admin-link" to="/admin/ranks">管理榜单</RouterLink>
        </div>
        <ul class="admin-list">
          <li v-for="(book, i) in adminHotRank.slice(0, 5)" :key="book.id">
            <span class="admin-idx">{{ i + 1 }}</span>
            <div>
              <strong>{{ book.title }}</strong>
              <p>{{ book.author }} · {{ book.heat }}</p>
            </div>
          </li>
        </ul>
      </section>

      <section class="admin-block">
        <div class="admin-block__head">
          <h2>待处理审核</h2>
          <RouterLink class="admin-link" to="/admin/reviews">全部审核</RouterLink>
        </div>
        <ul class="admin-list">
          <li v-for="item in reviewQueue.filter((r) => r.status === '待审核').slice(0, 4)" :key="item.id">
            <div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.author }} · {{ item.type }} · {{ item.time }}</p>
            </div>
            <RouterLink class="admin-mini" to="/admin/reviews">处理</RouterLink>
          </li>
        </ul>
      </section>
    </div>

    <section class="admin-block shortcuts">
      <div class="admin-block__head">
        <h2>快捷入口</h2>
      </div>
      <div class="cards">
        <RouterLink v-for="item in shortcuts" :key="item.to" :to="item.to">
          <span class="card-icon" aria-hidden="true">
            <SideIcon :name="item.icon" :size="16" />
          </span>
          {{ item.label }}
        </RouterLink>
      </div>
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
}

.stat p {
  margin-top: 6px;
  font-size: 12px;
  color: #a8a29a;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.shortcuts .cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.shortcuts a {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 88px;
  padding: 0 12px;
  border-radius: 12px;
  background: #f8f6f2;
  border: 1px solid transparent;
  color: var(--ink);
  font-size: 14px;
  font-weight: 600;
  overflow: hidden;
  transition: background 0.2s, border-color 0.2s, transform 0.2s, box-shadow 0.2s;
}

.shortcuts a::before {
  content: '';
  position: absolute;
  left: 0;
  top: 16px;
  bottom: 16px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: transparent;
  transition: background 0.2s;
}

.shortcuts a:hover {
  background: #fff;
  border-color: rgba(255, 77, 58, 0.18);
  box-shadow: 0 8px 20px rgba(255, 77, 58, 0.07);
  transform: translateY(-2px);
  color: var(--brand);
}

.shortcuts a:hover::before {
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

@media (max-width: 980px) {
  .stats,
  .grid,
  .shortcuts .cards {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .stats,
  .grid,
  .shortcuts .cards {
    grid-template-columns: 1fr;
  }
}
</style>
