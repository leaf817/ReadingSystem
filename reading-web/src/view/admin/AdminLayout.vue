<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import SideIcon, { type SideIconName } from '@/components/SideIcon.vue'
import { fetchMeApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { adminStats } from './adminData'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const avatarOpen = ref(false)
const bio = ref(auth.user?.bio || '')

const tabs: { path: string; label: string; icon: SideIconName; exact?: boolean }[] = [
  { path: '/admin', label: '概览', icon: 'overview', exact: true },
  { path: '/admin/books', label: '书籍', icon: 'books' },
  { path: '/admin/users', label: '用户', icon: 'users' },
  { path: '/admin/ranks', label: '榜单', icon: 'ranks' },
  { path: '/admin/reviews', label: '审核', icon: 'reviews' },
  { path: '/admin/profile', label: '设置', icon: 'profile' },
]

const displayName = computed(() => auth.user?.displayName || '管理员')
const avatarText = computed(() => displayName.value.trim().slice(0, 1) || '管')
const hasBio = computed(() => Boolean(bio.value.trim()))
const signature = computed(() => bio.value.trim() || '编辑个性签名')
const snapshot = computed(() => adminStats.slice(0, 3))

function isActive(path: string, exact?: boolean) {
  return exact ? route.path === path : route.path.startsWith(path)
}

function goProfile() {
  router.push('/admin/profile')
}

function goStat(label: string) {
  if (label === '待审核') {
    router.push('/admin/reviews')
    return
  }
  if (label === '在架书籍') {
    router.push('/admin/books')
  }
}

function openAvatar() {
  avatarOpen.value = true
}

function closeAvatar() {
  avatarOpen.value = false
}

watch(
  () => auth.user?.bio,
  (value) => {
    bio.value = value || ''
  },
)

onMounted(async () => {
  try {
    const me = await fetchMeApi()
    bio.value = me.bio || ''
    auth.patchProfile({
      displayName: me.nickname,
      avatar: me.avatar || undefined,
      bio: me.bio || undefined,
      username: me.username,
    })
  } catch {
    bio.value = auth.user?.bio || ''
  }
})
</script>

<template>
  <div class="shell">
    <AppHeader :with-cats="false" />

    <section class="banner">
      <div class="banner__art" aria-hidden="true" />
      <div class="banner__inner">
        <div class="banner__left">
          <button type="button" class="avatar-btn" title="查看头像" @click="openAvatar">
            <span v-if="auth.user?.avatar" class="avatar avatar--img">
              <img :src="auth.user.avatar" :alt="displayName" />
            </span>
            <span v-else class="avatar">{{ avatarText }}</span>
          </button>
          <div class="who">
            <h1>{{ displayName }}</h1>
            <div class="meta">
              <button
                v-for="item in snapshot"
                :key="item.label"
                type="button"
                @click="goStat(item.label)"
              >
                {{ item.label }} <b>{{ item.value }}</b>
              </button>
            </div>
            <button
              type="button"
              class="sign"
              :class="{ 'is-empty': !hasBio }"
              :title="hasBio ? '编辑签名' : '填写签名'"
              @click="goProfile"
            >
              {{ signature }}
            </button>
          </div>
        </div>
        <button type="button" class="edit-btn" @click="goProfile">编辑资料</button>
      </div>
    </section>

    <nav class="tabs" aria-label="管理端导航">
      <div class="tabs__inner">
        <RouterLink
          v-for="item in tabs"
          :key="item.path"
          :to="item.path"
          class="tab"
          :class="{ 'is-on': isActive(item.path, item.exact) }"
        >
          <SideIcon :name="item.icon" :size="16" />
          {{ item.label }}
        </RouterLink>
      </div>
    </nav>

    <main class="main">
      <RouterView />
    </main>

    <div
      v-if="avatarOpen"
      class="avatar-preview"
      role="dialog"
      aria-modal="true"
      :aria-label="displayName"
      @click="closeAvatar"
    >
      <span v-if="auth.user?.avatar" class="avatar-preview__img">
        <img :src="auth.user.avatar" :alt="displayName" />
      </span>
      <span v-else class="avatar-preview__letter">{{ avatarText }}</span>
      <p>{{ displayName }}</p>
    </div>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  background:
    radial-gradient(ellipse 80% 50% at 8% -8%, rgba(255, 77, 58, 0.08), transparent 52%),
    radial-gradient(ellipse 55% 40% at 92% 0%, rgba(120, 170, 230, 0.12), transparent 48%),
    var(--page);
}

.banner {
  position: relative;
  overflow: hidden;
  background: linear-gradient(180deg, #eaf2ff 0%, #f3f7ff 48%, #fff4ee 100%);
  border-bottom: 1px solid var(--header-line);
}

.banner__art {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 12% 20%, rgba(255, 122, 89, 0.16), transparent 32%),
    radial-gradient(circle at 88% 10%, rgba(120, 170, 230, 0.22), transparent 34%),
    radial-gradient(circle at 78% 90%, rgba(255, 180, 140, 0.28), transparent 38%);
  pointer-events: none;
}

.banner__art::after {
  content: '阅';
  position: absolute;
  right: 5%;
  bottom: -28px;
  font-family: var(--font-display);
  font-size: 168px;
  line-height: 1;
  color: rgba(255, 77, 58, 0.08);
  letter-spacing: 0.08em;
}

.banner__inner {
  position: relative;
  z-index: 1;
  width: min(1440px, calc(100% - 48px));
  margin: 0 auto;
  min-height: 176px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 36px 0;
}

.banner__left {
  display: flex;
  align-items: center;
  gap: 18px;
  min-width: 0;
}

.edit-btn {
  position: absolute;
  right: 72px;
  bottom: 8px;
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 18px;
  border: 1px solid rgba(24, 25, 28, 0.18);
  border-radius: 10px;
  background: transparent;
  color: var(--ink);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: none;
  transition: background 0.2s, color 0.2s, border-color 0.2s;
}

.edit-btn:hover {
  background: rgba(255, 255, 255, 0.22);
  color: var(--brand);
}

.avatar-btn {
  position: relative;
  width: 84px;
  height: 84px;
  border: 0;
  padding: 0;
  background: transparent;
  cursor: pointer;
  border-radius: 50%;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 0 0 3px var(--surface), 0 8px 18px rgba(0, 0, 0, 0.12);
}

.avatar {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  display: inline-grid;
  place-items: center;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
  color: #fff;
  font-size: 30px;
  font-weight: 700;
  overflow: hidden;
}

.avatar--img {
  background: #f0eeea;
}

.avatar--img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.who {
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
}

.who h1 {
  margin: 0;
  color: var(--ink);
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.06em;
  line-height: 1.2;
}

.meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px 20px;
  color: var(--ink-secondary);
  font-size: 13px;
}

.meta button {
  border: 0;
  padding: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;
}

.meta b {
  margin-left: 4px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--ink);
}

.meta button:hover {
  color: var(--brand);
}

.sign {
  margin: 2px 0 0;
  border: 0;
  padding: 0;
  max-width: min(42em, 100%);
  background: transparent;
  color: var(--ink-secondary);
  font-size: 13px;
  line-height: 1.6;
  text-align: left;
  cursor: pointer;
  white-space: pre-wrap;
  word-break: break-word;
}

.sign.is-empty {
  color: var(--ink-muted);
}

.sign:hover {
  color: var(--brand);
}

.tabs {
  position: sticky;
  top: 60px;
  z-index: 40;
  background: var(--space-tabs);
  border-bottom: 1px solid var(--line);
}

.tabs__inner {
  width: min(1440px, calc(100% - 48px));
  margin: 0 auto;
  display: flex;
  align-items: stretch;
  gap: 4px;
  overflow-x: auto;
  scrollbar-width: none;
}

.tabs__inner::-webkit-scrollbar {
  display: none;
}

.tab {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 52px;
  padding: 0 16px;
  color: var(--space-tab-ink);
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;
}

.tab :deep(.side-icon) {
  opacity: 0.75;
}

.tab:hover,
.tab.is-on {
  color: var(--brand);
}

.tab:hover :deep(.side-icon),
.tab.is-on :deep(.side-icon) {
  opacity: 1;
}

.tab.is-on::after {
  content: '';
  position: absolute;
  left: 16px;
  right: 16px;
  bottom: 0;
  height: 3px;
  border-radius: 3px 3px 0 0;
  background: var(--brand);
}

.main {
  width: min(1440px, calc(100% - 48px));
  margin: 0 auto;
  padding: 22px 0 48px;
}

.avatar-preview {
  position: fixed;
  inset: 0;
  z-index: 80;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 32px;
  background: rgba(18, 14, 12, 0.72);
  cursor: pointer;
}

.avatar-preview__img,
.avatar-preview__letter {
  width: min(72vw, 360px);
  height: min(72vw, 360px);
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.45);
}

.avatar-preview__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  background: #f0eeea;
}

.avatar-preview__letter {
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
  color: #fff;
  font-size: 120px;
  font-weight: 700;
}

.avatar-preview p {
  margin: 0;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

@media (max-width: 800px) {
  .banner__inner {
    min-height: 152px;
    padding: 28px 0;
  }

  .who h1 {
    font-size: 20px;
  }

  .tab {
    padding: 0 12px;
    font-size: 14px;
  }
}
</style>
