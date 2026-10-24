import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export type AppTheme = 'light' | 'dark'

const KEY = 'yuejie-app-theme'

function apply(theme: AppTheme) {
  document.documentElement.dataset.theme = theme
}

function readStored(): AppTheme {
  try {
    return localStorage.getItem(KEY) === 'dark' ? 'dark' : 'light'
  } catch {
    return 'light'
  }
}

export const useThemeStore = defineStore('theme', () => {
  const mode = ref<AppTheme>(readStored())
  const label = computed(() => (mode.value === 'dark' ? '深色' : '浅色'))

  function hydrate() {
    mode.value = readStored()
    apply(mode.value)
  }

  function setMode(next: AppTheme) {
    mode.value = next
    try {
      localStorage.setItem(KEY, next)
    } catch {
      /* ignore */
    }
    apply(next)
  }

  hydrate()

  return { mode, label, setMode, hydrate }
})
