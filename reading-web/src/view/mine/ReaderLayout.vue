<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import SideIcon, { type SideIconName } from '@/components/SideIcon.vue'
import { fetchMeApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const SPACE_SKINS = [
  { id: 'dawn', label: '晨雾' },
  { id: 'rose', label: '蔷薇' },
  { id: 'dusk', label: '暮色' },
  { id: 'sea', label: '海盐' },
  { id: 'ink', label: '墨夜' },
] as const

type SpaceSkin = (typeof SPACE_SKINS)[number]['id']

function skinKey(username?: string) {
  return `yuejie-space-skin:${username || 'guest'}`
}

function readSkin(username?: string): SpaceSkin {
  const raw = localStorage.getItem(skinKey(username))
  return SPACE_SKINS.some((item) => item.id === raw) ? (raw as SpaceSkin) : 'dawn'
}

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const avatarOpen = ref(false)
const skinOpen = ref(false)
const bannerSkin = ref<SpaceSkin>(readSkin(auth.user?.username))
const bio = ref(auth.user?.bio || '')

const tabs: { path: string; label: string; icon: SideIconName; exact?: boolean }[] = [
  { path: '/mine', label: '主页', icon: 'mine', exact: true },
  { path: '/mine/bookshelf', label: '书架', icon: 'bookshelf' },
  { path: '/mine/history', label: '历史', icon: 'history' },
  { path: '/mine/likes', label: '点赞', icon: 'likes' },
  { path: '/mine/comments', label: '评论', icon: 'comments' },
  { path: '/mine/prefs', label: '偏好', icon: 'prefs' },
  { path: '/mine/become-author', label: '作家', icon: 'author' },
  { path: '/mine/profile', label: '设置', icon: 'profile' },
]

const displayName = computed(() => auth.user?.displayName || '读者')
const avatarText = computed(() => displayName.value.trim().slice(0, 1) || '读')
const hasBio = computed(() => Boolean(bio.value.trim()))
const signature = computed(() => bio.value.trim() || '编辑个性签名')

function isActive(path: string, exact?: boolean) {
  return exact ? route.path === path : route.path.startsWith(path)
}

function goFollow(tab: string) {
  router.push({ path: '/mine/follow', query: { tab } })
}

function goProfile() {
  router.push('/mine/profile')
}

function openAvatar() {
  avatarOpen.value = true
}

function closeAvatar() {
  avatarOpen.value = false
}

function toggleSkinPanel() {
  skinOpen.value = !skinOpen.value
}

function pickSkin(id: SpaceSkin) {
  bannerSkin.value = id
  localStorage.setItem(skinKey(auth.user?.username), id)
  skinOpen.value = false
}

watch(
  () => auth.user?.username,
  (username) => {
    bannerSkin.value = readSkin(username)
  },
)

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
  <div class="shell" @click="skinOpen = false">
    <AppHeader :with-cats="false" />

    <section class="banner" :class="`skin-${bannerSkin}`">
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
              <button type="button" @click="goFollow('following')">关注 <b>0</b></button>
              <button type="button" @click="goFollow('followers')">粉丝 <b>0</b></button>
              <span>获赞 <b>0</b></span>
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
        <div class="banner__actions" @click.stop>
          <button type="button" class="edit-btn" @click="goProfile">编辑资料</button>
          <div class="skin-wrap">
            <button type="button" class="skin-btn" title="更换皮肤" @click="toggleSkinPanel">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" aria-hidden="true">
                <path
                  d="M8.6 5.4c1.05 1.15 2.2 1.75 3.4 1.75s2.35-.6 3.4-1.75l2.45 1.05 1.75 3.2-2.7.7v8.45c0 .75-.55 1.3-1.3 1.3H8.4c-.75 0-1.3-.55-1.3-1.3V10.4l-2.7-.7 1.75-3.2L8.6 5.4Z"
                  stroke="currentColor"
                  stroke-width="1.7"
                  stroke-linejoin="round"
                  stroke-linecap="round"
                />
                <path
                  d="M9.15 6.7c.85 1.15 1.8 1.75 2.85 1.75s2-.6 2.85-1.75"
                  stroke="currentColor"
                  stroke-width="1.7"
                  stroke-linecap="round"
                />
              </svg>
            </button>
            <div v-if="skinOpen" class="skin-panel" role="menu">
              <p>更换皮肤</p>
              <div class="skin-panel__list">
                <button
                  v-for="item in SPACE_SKINS"
                  :key="item.id"
                  type="button"
                  class="swatch"
                  :class="[`swatch--${item.id}`, { 'is-on': bannerSkin === item.id }]"
                  :title="item.label"
                  @click="pickSkin(item.id)"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <nav class="tabs" aria-label="个人中心导航">
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

.banner.skin-dawn {
  background: linear-gradient(180deg, #eaf2ff 0%, #f3f7ff 48%, #fff4ee 100%);
}

.banner.skin-rose {
  background: linear-gradient(180deg, #fde7ee 0%, #f8f1f4 52%, #fff6f0 100%);
}

.banner.skin-dusk {
  background: linear-gradient(180deg, #d9d4f0 0%, #ece8f7 48%, #f6f0ea 100%);
}

.banner.skin-sea {
  background: linear-gradient(180deg, #d5eaf5 0%, #e8f3f8 50%, #f4f7f4 100%);
}

.banner.skin-ink {
  background: linear-gradient(180deg, #2a3140 0%, #3a4254 48%, #4a4550 100%);
}

.banner:not(.skin-dawn) .banner__art {
  opacity: 0.42;
}

.banner.skin-ink .who h1,
.banner.skin-ink .meta b,
.banner.skin-ink .edit-btn,
.banner.skin-ink .skin-btn {
  color: #f3efe8;
}

.banner.skin-ink .meta,
.banner.skin-ink .sign {
  color: rgba(243, 239, 232, 0.78);
}

.banner.skin-ink .edit-btn,
.banner.skin-ink .skin-btn {
  border-color: rgba(255, 255, 255, 0.28);
}

.banner.skin-ink .banner__art {
  opacity: 0.22;
}

.banner.skin-ink .banner__art::after {
  color: rgba(255, 255, 255, 0.08);
}

.banner__actions {
  position: absolute;
  left: calc(100% - 280px);
  right: auto;
  bottom: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.edit-btn {
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 18px;
  border: 1px solid rgba(24, 25, 28, 0.18);
  border-radius: 10px;
  background: transparent;
  color: #1a1a1a;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0;
  cursor: pointer;
  box-shadow: none;
  transition: background 0.2s, color 0.2s, border-color 0.2s;
}

.edit-btn:hover {
  background: rgba(255, 255, 255, 0.22);
  color: var(--brand);
}

.skin-wrap {
  position: relative;
}

.skin-btn {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  padding: 0;
  border: 1px solid rgba(24, 25, 28, 0.18);
  border-radius: 10px;
  background: transparent;
  color: #1a1a1a;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.skin-btn:hover {
  background: rgba(255, 255, 255, 0.22);
  color: var(--brand);
}

.skin-panel {
  position: absolute;
  right: 0;
  bottom: calc(100% + 10px);
  z-index: 6;
  width: 196px;
  padding: 12px;
  border-radius: 14px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: 0 12px 28px rgba(30, 40, 60, 0.16);
}

.skin-panel p {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--ink-muted);
}

.skin-panel__list {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.swatch {
  width: 28px;
  height: 28px;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  padding: 0;
}

.swatch.is-on {
  border-color: var(--brand);
}

.swatch--dawn {
  background: linear-gradient(180deg, #eaf2ff, #fff4ee);
}

.swatch--rose {
  background: linear-gradient(180deg, #fde7ee, #fff6f0);
}

.swatch--dusk {
  background: linear-gradient(180deg, #d9d4f0, #f6f0ea);
}

.swatch--sea {
  background: linear-gradient(180deg, #d5eaf5, #f4f7f4);
}

.swatch--ink {
  background: linear-gradient(180deg, #2a3140, #4a4550);
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
  color: #1a1a1a;
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
  color: #666;
  font-size: 13px;
}

.meta button,
.meta span {
  border: 0;
  padding: 0;
  background: transparent;
  color: inherit;
  font: inherit;
}

.meta button {
  cursor: pointer;
}

.meta b {
  margin-left: 4px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: #1a1a1a;
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
  color: #666;
  font-size: 13px;
  line-height: 1.6;
  text-align: left;
  cursor: pointer;
  white-space: pre-wrap;
  word-break: break-word;
}

.sign.is-empty {
  color: #999;
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

@media (max-width: 1100px) {
  .banner__actions {
    left: auto;
    right: 0;
  }
}

@media (max-width: 800px) {
  .banner__inner {
    min-height: 152px;
    padding: 28px 0;
  }

  .who h1 {
    font-size: 20px;
  }

  .meta {
    gap: 4px 14px;
  }

  .tab {
    padding: 0 12px;
    font-size: 14px;
  }
}
</style>
