import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores/auth'
import '@/assets/styles/main.css'

const BOOT_KEY = 'yuejie-frontend-boot-id'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

const auth = useAuthStore()
const prevBootId = localStorage.getItem(BOOT_KEY)
const currentBootId = __FRONTEND_BOOT_ID__
const frontendRestarted = !prevBootId || prevBootId !== currentBootId

// 仅「重启前端服务」时要求重新登录；浏览器刷新 / 重启后端保持登录与当前页
if (frontendRestarted) {
  auth.logout()
  localStorage.setItem(BOOT_KEY, currentBootId)
  router.isReady().then(() => {
    if (router.currentRoute.value.name !== 'login') {
      void router.replace({ name: 'login' })
    }
  })
} else {
  localStorage.setItem(BOOT_KEY, currentBootId)
}

app.mount('#app')
