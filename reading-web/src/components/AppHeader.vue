<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { categories } from '@/data/mockBooks'
import { useAuthStore } from '@/stores/auth'

const keyword = ref('')
const activeCategory = ref('推荐')
const auth = useAuthStore()
const router = useRouter()

function goWorkspace() {
  if (!auth.role) return
  router.push(auth.roleHome[auth.role])
}

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <header class="header">
    <div class="header__bar">
      <RouterLink to="/" class="brand" aria-label="阅界首页">
        <span class="brand__mark" aria-hidden="true">
          <svg viewBox="0 0 48 48" width="36" height="36">
            <defs>
              <linearGradient id="logoBook" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="#ff6a4d" />
                <stop offset="100%" stop-color="#e63a28" />
              </linearGradient>
            </defs>
            <rect x="4" y="6" width="40" height="36" rx="10" fill="url(#logoBook)" />
            <path
              d="M24 12c-6.2 3.2-10 4.6-14 5.2V34c4.2-.7 8.2-2.1 14-5.2 5.8 3.1 9.8 4.5 14 5.2V17.2C34 16.6 30.2 15.2 24 12z"
              fill="#fff"
              opacity="0.96"
            />
            <path d="M24 12v16.8" stroke="#ff4d3a" stroke-width="1.6" stroke-linecap="round" />
            <circle cx="36" cy="12" r="5.5" fill="#fff3" />
          </svg>
        </span>
        <span class="brand__text">
          <span class="brand__name">阅界</span>
          <span class="brand__sub">NOVEL WORLD</span>
        </span>
      </RouterLink>

      <form class="search" @submit.prevent>
        <svg class="search__icon" viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
          <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2" />
          <path d="M16.5 16.5L21 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
        </svg>
        <input v-model="keyword" type="search" placeholder="搜索书名、作者、关键词" />
        <button type="submit" class="search__btn">搜索</button>
      </form>

      <nav class="actions">
        <a href="javascript:;" class="actions__link">书架</a>
        <a href="javascript:;" class="actions__link">历史</a>
        <template v-if="auth.isLoggedIn">
          <button type="button" class="actions__link" @click="goWorkspace">
            {{ auth.user?.displayName }}
          </button>
          <button type="button" class="login-btn" @click="logout">退出</button>
        </template>
        <RouterLink v-else to="/login" class="login-btn">登录</RouterLink>
      </nav>
    </div>

    <div class="header__cats">
      <nav class="cats" aria-label="分类导航">
        <button
          v-for="cat in categories"
          :key="cat"
          type="button"
          class="cats__item"
          :class="{ 'is-active': activeCategory === cat }"
          @click="activeCategory = cat"
        >
          {{ cat }}
        </button>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 50;
  width: 100%;
  background:
    linear-gradient(180deg, #f3f8ff 0%, #eef5ff 55%, #e8f1ff 100%);
  border-bottom: 1px solid rgba(120, 160, 220, 0.22);
  box-shadow: 0 8px 24px rgba(90, 130, 190, 0.08);
}

.header__bar {
  width: 100%;
  display: grid;
  grid-template-columns: auto minmax(240px, 640px) auto;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  height: 60px;
  padding: 0 clamp(20px, 4vw, 48px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.brand__mark {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  filter: drop-shadow(0 4px 10px rgba(255, 77, 58, 0.22));
  transition: transform 0.3s ease;
}

.brand:hover .brand__mark {
  transform: scale(1.04);
}

.brand__text {
  display: flex;
  flex-direction: column;
  line-height: 1.05;
}

.brand__name {
  font-family: var(--font-display);
  font-size: 26px;
  color: var(--brand);
  letter-spacing: 0.1em;
  white-space: nowrap;
}

.brand__sub {
  font-size: 9px;
  color: #c4a8a0;
  letter-spacing: 0.24em;
  margin-top: 2px;
  white-space: nowrap;
}

.search {
  display: flex;
  align-items: center;
  width: 100%;
  justify-self: center;
  height: 38px;
  border: 1px solid rgba(120, 160, 220, 0.4);
  border-radius: 999px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.92);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.search:focus-within {
  border-color: var(--brand);
  box-shadow: 0 0 0 3px rgba(255, 77, 58, 0.1);
}

.search__icon {
  margin-left: 14px;
  color: #8aa0bc;
  flex-shrink: 0;
}

.search input {
  flex: 1;
  min-width: 0;
  border: 0;
  outline: 0;
  padding: 0 10px;
  background: transparent;
  font-size: 13px;
}

.search__btn {
  border: 0;
  height: 100%;
  padding: 0 22px;
  background: var(--brand);
  color: #fff;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
  transition: background 0.2s;
}

.search__btn:hover {
  background: var(--brand-dark);
}

.actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  font-size: 13px;
  color: #5f6f86;
  white-space: nowrap;
}

.actions__link {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 12px;
  border-radius: 8px;
  transition: color 0.2s, background 0.2s;
}

.actions__link:hover {
  color: var(--brand);
  background: rgba(255, 255, 255, 0.7);
}

.login-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  height: 32px;
  margin-left: 6px;
  padding: 0 16px;
  border-radius: 999px;
  background: #fff;
  color: var(--brand);
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(255, 77, 58, 0.12);
  transition: background 0.2s, color 0.2s, box-shadow 0.2s;
}

.login-btn:hover {
  background: var(--brand);
  color: #fff;
  box-shadow: 0 6px 16px rgba(255, 77, 58, 0.22);
}

.header__cats {
  width: 100%;
  border-top: 1px solid rgba(120, 160, 220, 0.16);
  background: rgba(255, 255, 255, 0.35);
}

.cats {
  width: 100%;
  display: flex;
  align-items: stretch;
  justify-content: center;
  gap: 4px;
  padding: 0 clamp(20px, 4vw, 48px);
  overflow-x: auto;
  scrollbar-width: none;
}

.cats::-webkit-scrollbar {
  display: none;
}

.cats__item {
  position: relative;
  flex: 0 0 auto;
  border: 0;
  background: transparent;
  min-width: 64px;
  padding: 12px 18px;
  font-size: 14px;
  color: #5f6f86;
  cursor: pointer;
  text-align: center;
  transition: color 0.2s;
}

.cats__item::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 0;
  height: 2px;
  border-radius: 2px;
  background: var(--brand);
  transform: translateX(-50%);
  transition: width 0.2s ease;
}

.cats__item:hover {
  color: var(--brand);
}

.cats__item.is-active {
  color: var(--brand);
  font-weight: 600;
}

.cats__item.is-active::after {
  width: 22px;
}

@media (max-width: 900px) {
  .header__bar {
    grid-template-columns: auto 1fr auto;
    gap: 14px;
    padding: 0 16px;
  }

  .brand__sub {
    display: none;
  }

  .cats {
    justify-content: center;
    padding-inline: 16px;
  }

  .cats__item {
    padding: 11px 14px;
    min-width: 0;
  }
}

@media (max-width: 640px) {
  .actions__link {
    display: none;
  }

  .brand__name {
    font-size: 22px;
  }

  .search__icon {
    display: none;
  }

  .search__btn {
    padding: 0 14px;
  }

  .login-btn {
    margin-left: 0;
    padding: 0 12px;
  }
}
</style>
