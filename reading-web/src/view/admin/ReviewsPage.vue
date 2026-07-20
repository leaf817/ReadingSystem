<script setup lang="ts">
import { computed, ref } from 'vue'
import { reviewQueue, type ReviewItem } from './adminData'
import './admin-shared.css'

const list = ref<ReviewItem[]>(reviewQueue.map((r) => ({ ...r })))
const filter = ref<'全部' | '待审核' | '已通过' | '已驳回'>('待审核')
const toast = ref('')

const filtered = computed(() => {
  if (filter.value === '全部') return list.value
  return list.value.filter((r) => r.status === filter.value)
})

function decide(item: ReviewItem, status: '已通过' | '已驳回') {
  item.status = status
  toast.value = `「${item.title}」${status}`
  window.setTimeout(() => {
    toast.value = ''
  }, 1800)
}
</script>

<template>
  <section class="admin-block">
    <div class="admin-block__head">
      <h2>内容审核队列</h2>
      <div class="admin-toolbar">
        <button
          v-for="tab in ['待审核', '已通过', '已驳回', '全部'] as const"
          :key="tab"
          type="button"
          class="admin-btn"
          :class="filter === tab ? 'admin-btn--primary' : 'admin-btn--ghost'"
          @click="filter = tab"
        >
          {{ tab }}
        </button>
      </div>
    </div>

    <ul class="admin-list">
      <li v-for="item in filtered" :key="item.id">
        <div>
          <strong>{{ item.title }}</strong>
          <p>{{ item.author }} · {{ item.type }} · {{ item.time }} · {{ item.status }}</p>
        </div>
        <div v-if="item.status === '待审核'" class="admin-ops">
          <button type="button" class="admin-mini admin-mini--ok" @click="decide(item, '已通过')">
            通过
          </button>
          <button type="button" class="admin-mini" @click="decide(item, '已驳回')">驳回</button>
        </div>
        <span
          v-else
          class="admin-tag"
          :class="item.status === '已通过' ? 'admin-tag--on' : 'admin-tag--off'"
        >
          {{ item.status }}
        </span>
      </li>
    </ul>

    <p v-if="!filtered.length" class="admin-empty">当前筛选下暂无内容</p>
    <p v-if="toast" class="admin-toast">{{ toast }}</p>
  </section>
</template>
