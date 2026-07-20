<script setup lang="ts">
import { adminHotRank, adminStats, reviewQueue } from './adminData'
import './admin-shared.css'
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
        <RouterLink to="/admin/books">书籍上下架</RouterLink>
        <RouterLink to="/admin/users">用户与作者</RouterLink>
        <RouterLink to="/admin/ranks">榜单运营</RouterLink>
        <RouterLink to="/admin/reviews">内容审核</RouterLink>
      </div>
    </section>
  </div>
</template>

<style scoped>
.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 16px;
}

.stat {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  border: 1px solid rgba(0, 0, 0, 0.04);
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
  margin-bottom: 16px;
}

.shortcuts .cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.shortcuts a {
  display: grid;
  place-items: center;
  height: 72px;
  border-radius: 12px;
  background: linear-gradient(160deg, #fff7f5, #fff);
  border: 1px solid rgba(255, 77, 58, 0.12);
  color: var(--ink);
  font-size: 14px;
  font-weight: 600;
  transition: transform 0.2s, border-color 0.2s;
}

.shortcuts a:hover {
  transform: translateY(-2px);
  border-color: rgba(255, 77, 58, 0.35);
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
