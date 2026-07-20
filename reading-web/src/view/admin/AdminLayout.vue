<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const menuOpen = ref(false)
const userMenu = ref<HTMLElement | null>(null)

const menus = [
  { path: '/admin', label: '数据概览', exact: true },
  { path: '/admin/books', label: '书籍管理' },
  { path: '/admin/users', label: '用户管理' },
  { path: '/admin/ranks', label: '榜单运营' },
  { path: '/admin/reviews', label: '内容审核' },
]

const pageTitle = computed(() => {
  const hit = menus.find((m) =>
    m.exact ? route.path === m.path : route.path.startsWith(m.path),
  )
  return hit?.label ?? '管理端工作台'
})

const displayName = computed(() => auth.user?.displayName || '管理员')
const avatarText = computed(() => displayName.value.trim().slice(0, 1) || '管')

function isActive(path: string, exact?: boolean) {
  return exact ? route.path === path : route.path.startsWith(path)
}

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function closeMenu() {
  menuOpen.value = false
}

function goBookstore() {
  closeMenu()
  router.push('/')
}

function logout() {
  closeMenu()
  auth.logout()
  router.push('/login')
}

function onDocClick(event: MouseEvent) {
  if (!menuOpen.value || !userMenu.value) return
  if (!userMenu.value.contains(event.target as Node)) {
    closeMenu()
  }
}

onMounted(() => {
  document.addEventListener('click', onDocClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
})
</script>

<template>
  <div class="layout">
    <aside class="side">
      <div class="brand">
        <span class="mark">阅</span>
        <div>
          <strong>阅界管理端</strong>
          <p>运营工作台</p>
        </div>
      </div>

      <nav class="menu">
        <RouterLink
          v-for="item in menus"
          :key="item.path"
          :to="item.path"
          :class="{ 'is-active': isActive(item.path, item.exact) }"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="side__foot">
        <div class="who">
          <span v-if="auth.user?.avatar" class="avatar avatar--sm avatar--img">
            <img :src="auth.user.avatar" :alt="displayName" />
          </span>
          <span v-else class="avatar avatar--sm">{{ avatarText }}</span>
          <p>{{ displayName }}</p>
        </div>
        <button type="button" class="logout" @click="logout">退出登录</button>
      </div>
    </aside>

    <div class="body">
      <header class="top">
        <div>
          <h1>{{ pageTitle }}</h1>
          <p>阅界小说阅读管理系统 · 管理端</p>
        </div>
        <div class="actions">
          <div ref="userMenu" class="user-menu">
            <button
              type="button"
              class="user-chip"
              :class="{ 'is-open': menuOpen }"
              aria-haspopup="menu"
              :aria-expanded="menuOpen"
              @click.stop="toggleMenu"
            >
              <span v-if="auth.user?.avatar" class="avatar avatar--img">
                <img :src="auth.user.avatar" :alt="displayName" />
              </span>
              <span v-else class="avatar">{{ avatarText }}</span>
              <span class="badge">{{ displayName }}</span>
              <svg class="caret" viewBox="0 0 12 12" width="10" height="10" aria-hidden="true">
                <path
                  d="M2.5 4.2L6 7.8l3.5-3.6"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.6"
                  stroke-linecap="round"
                />
              </svg>
            </button>
            <div v-if="menuOpen" class="user-dropdown" role="menu">
              <button type="button" role="menuitem" @click="goBookstore">查看书城</button>
              <button type="button" role="menuitem" class="is-danger" @click="logout">退出</button>
            </div>
          </div>
        </div>
      </header>

      <main class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 232px 1fr;
  background:
    radial-gradient(ellipse 80% 50% at 8% -8%, rgba(255, 77, 58, 0.07), transparent 52%),
    radial-gradient(ellipse 55% 40% at 92% 0%, rgba(245, 166, 35, 0.05), transparent 48%),
    var(--page);
}

.side {
  position: sticky;
  top: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding: 22px 14px;
  background: linear-gradient(180deg, #1f2a3d 0%, #24344d 100%);
  color: #fff;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 10px;
}

.mark {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: var(--brand);
  font-family: var(--font-serif);
  font-size: 20px;
  box-shadow: 0 8px 18px rgba(255, 77, 58, 0.28);
}

.brand strong {
  display: block;
  font-size: 15px;
}

.brand p {
  margin-top: 3px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
}

.menu {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.menu a {
  padding: 12px 14px;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 14px;
  transition: background 0.2s, color 0.2s;
}

.menu a:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.menu a.is-active {
  background: var(--brand);
  color: #fff;
  font-weight: 600;
}

.side__foot {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 0 6px;
}

.who {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
}

.who p {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logout {
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: transparent;
  color: rgba(255, 255, 255, 0.85);
  height: 40px;
  border-radius: 10px;
  cursor: pointer;
}

.logout:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.body {
  min-width: 0;
  padding: 22px 24px 36px;
}

.top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.top h1 {
  font-family: var(--font-serif);
  font-size: 28px;
  font-weight: 600;
  letter-spacing: 0.06em;
}

.top p {
  margin-top: 6px;
  font-size: 13px;
  color: var(--ink-muted);
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-menu {
  position: relative;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 34px;
  padding: 0 10px 0 4px;
  border: 0;
  border-radius: 999px;
  background: var(--brand-soft);
  cursor: pointer;
  transition: background 0.2s, box-shadow 0.2s;
}

.user-chip:hover,
.user-chip.is-open {
  background: #ffe8e3;
  box-shadow: 0 2px 8px rgba(255, 77, 58, 0.12);
}

.caret {
  color: var(--brand);
  transition: transform 0.2s;
}

.user-chip.is-open .caret {
  transform: rotate(180deg);
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  z-index: 60;
  width: 100%;
  box-sizing: border-box;
  padding: 5px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 8px 20px rgba(30, 40, 60, 0.12);
}

.user-dropdown button {
  display: block;
  width: 100%;
  border: 0;
  background: transparent;
  text-align: center;
  padding: 9px 12px;
  border-radius: 7px;
  font-size: 13px;
  line-height: 1.2;
  color: var(--ink-secondary);
  cursor: pointer;
  white-space: nowrap;
}

.user-dropdown button:hover {
  background: #fff5f3;
  color: var(--brand);
}

.avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: inline-grid;
  place-items: center;
  flex-shrink: 0;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  overflow: hidden;
}

.avatar--sm {
  width: 28px;
  height: 28px;
  font-size: 12px;
}

.avatar--img {
  background: #f0eeea;
  padding: 0;
}

.avatar--img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.badge {
  color: var(--brand);
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
}

.content {
  min-width: 0;
}

@media (max-width: 980px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .side {
    position: static;
    height: auto;
    gap: 16px;
  }

  .menu {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .side__foot {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }

  .logout {
    width: auto;
    padding: 0 16px;
  }
}
</style>
