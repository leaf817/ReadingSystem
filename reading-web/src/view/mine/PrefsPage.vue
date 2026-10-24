<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'

const PREF_KEY = 'yuejie-reader-prefs'
const toast = ref('')
let toastTimer = 0

const prefs = reactive({
  fontSize: '中',
  theme: '护眼',
})

function showToast(message: string) {
  toast.value = message
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toast.value = ''
  }, 1800)
}

function loadPrefs() {
  try {
    const raw = localStorage.getItem(PREF_KEY)
    if (!raw) return
    const data = JSON.parse(raw) as { fontSize?: string; theme?: string }
    if (data.fontSize) prefs.fontSize = data.fontSize
    if (data.theme) prefs.theme = data.theme
  } catch {
    /* ignore */
  }
}

function savePrefs() {
  localStorage.setItem(PREF_KEY, JSON.stringify({ ...prefs }))
  showToast('阅读偏好已保存')
}

onMounted(loadPrefs)
</script>

<template>
  <section class="panel">
    <div class="panel__head">
      <h3>阅读偏好</h3>
      <p>偏好保存在本机，阅读器上线后生效</p>
    </div>

    <div class="fields">
      <label class="field">
        <span>字号</span>
        <select v-model="prefs.fontSize" @change="savePrefs">
          <option>小</option>
          <option>中</option>
          <option>大</option>
        </select>
      </label>
      <label class="field">
        <span>背景</span>
        <select v-model="prefs.theme" @change="savePrefs">
          <option>护眼</option>
          <option>浅色</option>
          <option>深色</option>
        </select>
      </label>
    </div>

    <Teleport to="body">
      <div v-if="toast" class="toast-mask">
        <div class="toast" role="status">
          <span class="toast__icon">✓</span>
          <p>{{ toast }}</p>
        </div>
      </div>
    </Teleport>
  </section>
</template>

<style scoped>
.panel {
  background: var(--surface);
  border-radius: 18px;
  padding: 24px 26px;
  border: 1.5px solid var(--line);
  box-shadow: var(--shadow-soft);
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 520px;
}

.panel__head h3 {
  font-size: 18px;
  font-weight: 700;
}

.panel__head p {
  margin-top: 6px;
  font-size: 13px;
  color: var(--ink-secondary);
}

.fields {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #2a3340;
}

.field select {
  height: 44px;
  padding: 0 14px;
  border-radius: 12px;
  border: 1.5px solid var(--line);
  background: var(--chip);
  outline: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--ink);
}

.toast-mask {
  position: fixed;
  inset: 0 0 auto;
  z-index: 200;
  display: flex;
  justify-content: center;
  padding: 18px 20px 0;
  pointer-events: none;
}

.toast {
  width: max-content;
  padding: 10px 16px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid rgba(255, 77, 58, 0.12);
  box-shadow: 0 10px 28px rgba(255, 77, 58, 0.12);
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.toast__icon {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, #3ecf8e, #2f6b4f);
  color: #fff;
  font-weight: 700;
}

.toast p {
  font-size: 14px;
  font-weight: 600;
  color: #2a3340;
}
</style>
