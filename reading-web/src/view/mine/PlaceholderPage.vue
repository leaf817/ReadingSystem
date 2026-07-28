<script setup lang="ts">
defineProps<{
  title?: string
  tip?: string
}>()
</script>

<template>
  <section class="placeholder">
    <h2>{{ title || '即将开放' }}</h2>
    <p>{{ tip || '该模块将在阅读器能力落地后接入，当前布局已预留入口。' }}</p>
  </section>
</template>

<style scoped>
.placeholder {
  background: #fff;
  border-radius: 14px;
  padding: 36px 28px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: var(--shadow-soft);
}

.placeholder h2 {
  font-size: 18px;
  font-weight: 600;
}

.placeholder p {
  margin-top: 10px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--ink-muted);
}
</style>
