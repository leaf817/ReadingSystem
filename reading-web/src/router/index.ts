import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/view/HomeView.vue'
import LoginView from '@/view/LoginView.vue'
import WorkspaceView from '@/view/WorkspaceView.vue'
import { useAuthStore, type UserRole } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { title: '阅界 - 书城' },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { title: '阅界 - 登录' },
    },
    {
      path: '/admin',
      name: 'admin',
      component: WorkspaceView,
      props: { role: 'admin' satisfies UserRole },
      meta: { title: '阅界 - 管理端', requiresAuth: true, role: 'admin' },
    },
    {
      path: '/author',
      name: 'author',
      component: WorkspaceView,
      props: { role: 'author' satisfies UserRole },
      meta: { title: '阅界 - 作者端', requiresAuth: true, role: 'author' },
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  const requiresAuth = Boolean(to.meta.requiresAuth)
  const needRole = to.meta.role as UserRole | undefined

  if (requiresAuth && !auth.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (needRole && auth.role && auth.role !== needRole) {
    return auth.roleHome[auth.role]
  }

  if (to.name === 'login' && auth.isLoggedIn && auth.role) {
    return auth.roleHome[auth.role]
  }

  return true
})

router.afterEach((to) => {
  document.title = (to.meta.title as string) || '阅界'
})

export default router
