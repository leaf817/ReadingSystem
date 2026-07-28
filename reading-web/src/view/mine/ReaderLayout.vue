<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SideIcon, { type SideIconName } from '@/components/SideIcon.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const menuOpen = ref(false)
const userMenu = ref<HTMLElement | null>(null)

const menus: { path: string; label: string; icon: SideIconName }[] = [
  { path: '/mine/bookshelf', label: '我的书架', icon: 'bookshelf' },
  { path: '/mine/history', label: '浏览历史', icon: 'history' },
  { path: '/mine/likes', label: '我的点赞', icon: 'likes' },
  { path: '/mine/comments', label: '我的评论', icon: 'comments' },
  { path: '/mine/prefs', label: '阅读偏好', icon: 'prefs' },
  { path: '/mine/become-author', label: '成为作家', icon: 'author' },
]

const pageTitle = computed(() => {
  if (route.path === '/mine') return '我的'
  if (route.path.startsWith('/mine/profile')) return '个人信息'
  if (route.path.startsWith('/mine/follow')) return '关注'
  const hit = menus.find((m) => route.path.startsWith(m.path))
  return hit?.label ?? '个人中心'
})

const showBack = computed(
  () => route.path.startsWith('/mine/follow') || route.path.startsWith('/mine/profile'),
)

const displayName = computed(() => auth.user?.displayName || '读者')
const avatarText = computed(() => displayName.value.trim().slice(0, 1) || '读')

function isActive(path: string, exact?: boolean) {
  return exact ? route.path === path : route.path.startsWith(path)
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/mine')
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
      <div class="side__foot">
        <RouterLink to="/mine" class="mine-btn" :class="{ 'is-active': route.path === '/mine' }">
          <SideIcon name="mine" :size="16" />
          我的
        </RouterLink>
      </div>

      <nav class="menu">
        <RouterLink
          v-for="item in menus"
          :key="item.path"
          :to="item.path"
          :class="{ 'is-active': isActive(item.path) }"
        >
          <SideIcon :name="item.icon" :size="16" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="brand">
        <span class="mark">阅</span>
        <div>
          <strong>阅界 · 个人中心</strong>
          <p>读者中心</p>
        </div>
      </div>
    </aside>

    <div class="body">
      <header class="top">
        <div>
          <div class="title-row">
            <button
              v-if="showBack"
              type="button"
              class="back-btn"
              title="返回"
              aria-label="返回"
              @click="goBack"
            >
              <svg viewBox="0 0 12 12" width="14" height="14" aria-hidden="true">
                <path
                  d="M7.8 2.5L4.2 6l3.6 3.5"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.8"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
            <h1>{{ pageTitle }}</h1>
          </div>
          <p>阅界小说阅读管理系统 · 读者端</p>
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
  grid-template-columns: 228px 1fr;
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
  gap: 24px;
  padding: 20px 12px;
  background: linear-gradient(180deg, #1a2435 0%, #24344d 100%);
  color: #fff;
}

.brand {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 10px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.04);
}

.mark {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  display: grid;
  place-items: center;
  background: var(--brand);
  font-family: var(--font-serif);
  font-size: 19px;
  box-shadow: 0 8px 18px rgba(255, 77, 58, 0.28);
}

.brand strong {
  display: block;
  font-size: 14px;
}

.brand p {
  margin-top: 3px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.menu {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu a {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 14px;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.68);
  font-size: 14px;
  transition: background 0.2s, color 0.2s;
}

.menu a :deep(.side-icon) {
  opacity: 0.78;
}

.menu a:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.menu a:hover :deep(.side-icon),
.menu a.is-active :deep(.side-icon) {
  opacity: 1;
}

.menu a.is-active {
  background: var(--brand);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 8px 16px rgba(255, 77, 58, 0.24);
}

.side__foot {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 0 4px;
}

.mine-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.88);
  font-size: 14px;
  font-weight: 600;
  transition: background 0.2s, color 0.2s, border-color 0.2s, box-shadow 0.2s;
}

.mine-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.mine-btn.is-active {
  border-color: transparent;
  background: var(--brand);
  color: #fff;
  box-shadow: 0 8px 16px rgba(255, 77, 58, 0.24);
}

.body {
  min-width: 0;
  padding: 24px 28px 40px;
}

.top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-btn {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--ink-secondary);
  cursor: pointer;
}

.back-btn:hover {
  background: rgba(0, 0, 0, 0.04);
  color: var(--ink);
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
  height: 36px;
  padding: 0 10px 0 6px;
  border-radius: 999px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: #fff;
  box-shadow: 0 4px 14px rgba(30, 40, 60, 0.06);
  cursor: pointer;
}

.user-chip.is-open,
.user-chip:hover {
  border-color: rgba(255, 77, 58, 0.28);
}

.caret {
  color: var(--ink-muted);
  transition: transform 0.2s;
}

.user-chip.is-open .caret {
  transform: rotate(180deg);
}

.user-dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  z-index: 20;
  min-width: 132px;
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

.user-dropdown button.is-danger:hover {
  background: #fff1ef;
  color: #d9382a;
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

  .mine-btn {
    width: auto;
    padding: 0 16px;
  }
}
</style>
