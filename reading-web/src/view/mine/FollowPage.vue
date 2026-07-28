<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

type TabKey = 'visited' | 'recommend' | 'following' | 'followers'

const route = useRoute()
const router = useRouter()

const tabs: { key: TabKey; label: string }[] = [
  { key: 'visited', label: '看过的人' },
  { key: 'recommend', label: '推荐' },
  { key: 'following', label: '关注' },
  { key: 'followers', label: '粉丝' },
]

const emptyTips: Record<TabKey, string> = {
  visited: '还没有看过的人，去书城逛逛吧',
  recommend: '暂无推荐用户，稍后再来看看',
  following: '还没有关注任何人',
  followers: '还没有粉丝',
}

const activeTab = ref<TabKey>('following')

function resolveTab(raw: unknown): TabKey {
  const value = typeof raw === 'string' ? raw : ''
  if (tabs.some((t) => t.key === value)) return value as TabKey
  return 'following'
}

watch(
  () => route.query.tab,
  (tab) => {
    activeTab.value = resolveTab(tab)
  },
  { immediate: true },
)

const emptyTip = computed(() => emptyTips[activeTab.value])

function switchTab(key: TabKey) {
  activeTab.value = key
  router.replace({ query: { ...route.query, tab: key } })
}
</script>

<template>
  <section class="follow-page">
    <div class="tabs" role="tablist">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        role="tab"
        class="tab"
        :class="{ 'is-active': activeTab === tab.key }"
        :aria-selected="activeTab === tab.key"
        @click="switchTab(tab.key)"
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="panel" role="tabpanel">
      <p class="empty">{{ emptyTip }}</p>
    </div>
  </section>
</template>

<style scoped>
.follow-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 6px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: var(--shadow-soft);
}

.tab {
  height: 36px;
  padding: 0 16px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: #5a6575;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.tab:hover {
  color: var(--brand);
  background: #fff5f3;
}

.tab.is-active {
  color: #fff;
  background: var(--brand);
}

.panel {
  min-height: 280px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: var(--shadow-soft);
  display: grid;
  place-items: center;
  padding: 40px 24px;
}

.empty {
  font-size: 14px;
  color: #8a8f98;
}
</style>
