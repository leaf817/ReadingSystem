<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchMeApi } from '@/api/auth'
import SideIcon, { type SideIconName } from '@/components/SideIcon.vue'
import { useAuthStore } from '@/stores/auth'
import { useReadingStore } from '@/stores/reading'

const auth = useAuthStore()
const reading = useReadingStore()
const router = useRouter()
const bio = ref(auth.user?.bio || '')

const displayName = computed(() => auth.user?.displayName || '读者')
const avatarText = computed(() => displayName.value.trim().slice(0, 1) || '读')
const signature = computed(() => bio.value.trim() || '这个人很懒，还没有写签名')
const lastReading = computed(() => reading.last)

const stats = [
  { key: 'following', label: '关注', value: 0, tab: 'following' as const },
  { key: 'followers', label: '粉丝', value: 0, tab: 'followers' as const },
  { key: 'likes', label: '获赞', value: 0 },
]

const shortcuts: { to: string; title: string; desc: string; icon: SideIconName }[] = [
  { to: '/mine/bookshelf', title: '我的书架', desc: '收藏与在读作品', icon: 'bookshelf' },
  { to: '/mine/history', title: '浏览历史', desc: '最近浏览与阅读进度', icon: 'history' },
  { to: '/mine/likes', title: '我的点赞', desc: '点赞过的作品与章节', icon: 'likes' },
  { to: '/mine/comments', title: '我的评论', desc: '发表过的书评与讨论', icon: 'comments' },
  { to: '/mine/prefs', title: '阅读偏好', desc: '字号、背景等设置', icon: 'prefs' },
  { to: '/mine/become-author', title: '成为作家', desc: '申请开通创作权限', icon: 'author' },
]

function goProfile() {
  router.push('/mine/profile')
}

function continueReading() {
  if (!lastReading.value) return
  router.push({
    name: 'read',
    params: { id: String(lastReading.value.bookId) },
    query: { chapter: String(lastReading.value.chapterIndex) },
  })
}

function onStatClick(event: Event, tab?: string) {
  if (!tab) return
  event.preventDefault()
  event.stopPropagation()
  router.push({ path: '/mine/follow', query: { tab } })
}

onMounted(async () => {
  reading.hydrate()
  try {
    const me = await fetchMeApi()
    bio.value = me.bio || ''
    auth.patchProfile({
      displayName: me.nickname,
      avatar: me.avatar || undefined,
      bio: me.bio || undefined,
      username: me.username,
    })
    reading.hydrate()
  } catch {
    bio.value = auth.user?.bio || ''
  }
})
</script>

<template>
  <div class="overview">
    <div class="profile-card" role="link" tabindex="0" @click="goProfile" @keydown.enter="goProfile">
      <span v-if="auth.user?.avatar" class="avatar avatar--img">
        <img :src="auth.user.avatar" :alt="displayName" />
      </span>
      <span v-else class="avatar">{{ avatarText }}</span>

      <div class="profile-card__body">
        <div class="profile-card__top">
          <strong class="name">{{ displayName }}</strong>
          <span class="profile-entry">
            个人信息
            <svg class="chevron" viewBox="0 0 12 12" width="12" height="12" aria-hidden="true">
              <path
                d="M4.2 2.5L7.8 6 4.2 9.5"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </span>
        </div>

        <div class="stats">
          <button
            v-for="item in stats"
            :key="item.key"
            type="button"
            class="stat"
            :class="{ 'is-link': Boolean(item.tab) }"
            :disabled="!item.tab"
            @click="onStatClick($event, item.tab)"
          >
            <b>{{ item.value }}</b>
            <span>{{ item.label }}</span>
          </button>
        </div>

        <p class="signature" :class="{ 'is-empty': !bio.trim() }">{{ signature }}</p>
      </div>
    </div>

    <button
      v-if="lastReading"
      type="button"
      class="continue"
      @click="continueReading"
    >
      <img :src="lastReading.cover" :alt="lastReading.title" class="continue__cover" />
      <div class="continue__meta">
        <strong>{{ lastReading.title }}</strong>
        <span>{{ lastReading.chapterTitle }}</span>
      </div>
      <span class="continue__action">
        继续阅读
        <svg viewBox="0 0 12 12" width="12" height="12" aria-hidden="true">
          <path
            d="M4.2 2.5L7.8 6 4.2 9.5"
            fill="none"
            stroke="currentColor"
            stroke-width="1.6"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </span>
    </button>

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
  </div>
</template>

<style scoped>
.overview {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.profile-card {
  display: flex;
  align-items: flex-start;
  gap: 18px;
  padding: 26px 28px;
  min-height: 140px;
  border-radius: 16px;
  background:
    linear-gradient(135deg, rgba(255, 77, 58, 0.06), transparent 42%),
    #fff;
  border: 1px solid rgba(0, 0, 0, 0.045);
  box-shadow: var(--shadow-soft);
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.profile-card:hover {
  border-color: rgba(255, 77, 58, 0.22);
  box-shadow: 0 12px 28px rgba(255, 77, 58, 0.08);
  transform: translateY(-1px);
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: inline-grid;
  place-items: center;
  flex-shrink: 0;
  margin-top: 2px;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
  color: #fff;
  font-size: 26px;
  font-weight: 700;
  overflow: hidden;
  box-shadow: 0 8px 18px rgba(255, 77, 58, 0.22);
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

.profile-card__body {
  flex: 1;
  min-width: 0;
}

.profile-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.name {
  display: block;
  font-family: var(--font-serif);
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: #1a1a1a;
  line-height: 1.2;
}

.stats {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.stat {
  position: relative;
  border: 0;
  padding: 0 18px;
  background: transparent;
  display: inline-flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 3px;
  cursor: default;
}

.stat:first-child {
  padding-left: 0;
}

.stat + .stat::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 1px;
  background: #ebe7e0;
}

.stat b {
  font-size: 20px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--ink);
  line-height: 1.1;
}

.stat span {
  font-size: 12px;
  color: #8a8f98;
}

.stat.is-link {
  cursor: pointer;
}

.stat.is-link:hover b,
.stat.is-link:hover span {
  color: var(--brand);
}

.stat:disabled {
  opacity: 1;
}

.signature {
  margin-top: 14px;
  max-width: 48em;
  font-size: 13px;
  line-height: 1.7;
  color: #6a7380;
  white-space: normal;
  word-break: break-word;
}

.signature.is-empty {
  color: #a8afbb;
}

.profile-entry {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 600;
  color: #9aa1ab;
}

.profile-card:hover .profile-entry {
  color: var(--brand);
}

.chevron {
  color: currentColor;
}

.continue {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 12px 16px;
  border: 0;
  border-radius: 12px;
  background: #f5f5f5;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s;
}

.continue:hover {
  background: #efefef;
}

.continue__cover {
  width: 42px;
  height: 56px;
  border-radius: 4px;
  object-fit: cover;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.continue__meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.continue__meta strong {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.continue__meta span {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.continue__action {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  font-size: 13px;
  color: #888;
}

.continue:hover .continue__action {
  color: var(--brand);
}

.block {
  background: #fff;
  border-radius: 16px;
  padding: 22px 24px 24px;
  border: 1px solid rgba(0, 0, 0, 0.045);
  box-shadow: var(--shadow-soft);
}

.block__head h2 {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.cards {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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

@media (max-width: 980px) {
  .cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 720px) {
  .cards {
    grid-template-columns: 1fr;
  }

  .profile-card {
    padding: 20px;
  }

  .name {
    font-size: 20px;
  }

  .stat {
    padding: 0 14px;
  }

  .stat b {
    font-size: 18px;
  }

  .signature {
    max-width: 100%;
  }

  .continue__action {
    font-size: 12px;
  }
}
</style>
