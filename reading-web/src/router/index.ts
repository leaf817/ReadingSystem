import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/view/HomeView.vue'
import LoginView from '@/view/LoginView.vue'
import ReaderProfileView from '@/view/profile/ReaderProfileView.vue'
import AuthorProfileView from '@/view/profile/AuthorProfileView.vue'
import ReaderLayout from '@/view/mine/ReaderLayout.vue'
import MineOverviewPage from '@/view/mine/OverviewPage.vue'
import MinePlaceholderPage from '@/view/mine/PlaceholderPage.vue'
import MinePrefsPage from '@/view/mine/PrefsPage.vue'
import MineFollowPage from '@/view/mine/FollowPage.vue'
import AuthorLayout from '@/view/author/AuthorLayout.vue'
import AuthorOverviewPage from '@/view/author/OverviewPage.vue'
import AuthorPlaceholderPage from '@/view/author/PlaceholderPage.vue'
import AdminLayout from '@/view/admin/AdminLayout.vue'
import OverviewPage from '@/view/admin/OverviewPage.vue'
import BooksPage from '@/view/admin/BooksPage.vue'
import UsersPage from '@/view/admin/UsersPage.vue'
import RanksPage from '@/view/admin/RanksPage.vue'
import ReviewsPage from '@/view/admin/ReviewsPage.vue'
import ProfilePage from '@/view/admin/ProfilePage.vue'
import ReadView from '@/view/ReadView.vue'
import { getLastPath, setLastPath, useAuthStore, type UserRole } from '@/stores/auth'

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
      path: '/read/:id',
      name: 'read',
      component: ReadView,
      meta: { title: '阅界 - 阅读' },
    },
    {
      path: '/profile',
      redirect: '/mine/profile',
    },
    {
      path: '/mine',
      component: ReaderLayout,
      meta: { requiresAuth: true, role: 'reader' },
      children: [
        {
          path: '',
          name: 'mine',
          component: MineOverviewPage,
          meta: { title: '阅界 - 个人中心', requiresAuth: true, role: 'reader' },
        },
        {
          path: 'bookshelf',
          name: 'mine-bookshelf',
          component: MinePlaceholderPage,
          props: { title: '我的书架', tip: '书架能力将在阅读器上线后接入。' },
          meta: { title: '阅界 - 我的书架', requiresAuth: true, role: 'reader' },
        },
        {
          path: 'history',
          name: 'mine-history',
          component: MinePlaceholderPage,
          props: { title: '浏览历史', tip: '浏览历史将在阅读器上线后接入。' },
          meta: { title: '阅界 - 浏览历史', requiresAuth: true, role: 'reader' },
        },
        {
          path: 'likes',
          name: 'mine-likes',
          component: MinePlaceholderPage,
          props: { title: '我的点赞', tip: '点赞记录将在互动能力上线后接入。' },
          meta: { title: '阅界 - 我的点赞', requiresAuth: true, role: 'reader' },
        },
        {
          path: 'comments',
          name: 'mine-comments',
          component: MinePlaceholderPage,
          props: { title: '我的评论', tip: '评论记录将在互动能力上线后接入。' },
          meta: { title: '阅界 - 我的评论', requiresAuth: true, role: 'reader' },
        },
        {
          path: 'prefs',
          name: 'mine-prefs',
          component: MinePrefsPage,
          meta: { title: '阅界 - 阅读偏好', requiresAuth: true, role: 'reader' },
        },
        {
          path: 'become-author',
          name: 'mine-become-author',
          component: MinePlaceholderPage,
          props: {
            title: '成为作家',
            tip: '开通创作权限后可进入作者端工作台。申请流程将在后续版本接入。',
          },
          meta: { title: '阅界 - 成为作家', requiresAuth: true, role: 'reader' },
        },
        {
          path: 'follow',
          name: 'mine-follow',
          component: MineFollowPage,
          meta: { title: '阅界 - 关注', requiresAuth: true, role: 'reader' },
        },
        {
          path: 'profile',
          name: 'reader-profile',
          component: ReaderProfileView,
          meta: { title: '阅界 - 个人信息', requiresAuth: true, role: 'reader' },
        },
      ],
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, role: 'admin' },
      children: [
        {
          path: '',
          name: 'admin',
          component: OverviewPage,
          meta: { title: '阅界 - 管理端工作台', requiresAuth: true, role: 'admin' },
        },
        {
          path: 'books',
          name: 'admin-books',
          component: BooksPage,
          meta: { title: '阅界 - 书籍管理', requiresAuth: true, role: 'admin' },
        },
        {
          path: 'users',
          name: 'admin-users',
          component: UsersPage,
          meta: { title: '阅界 - 用户管理', requiresAuth: true, role: 'admin' },
        },
        {
          path: 'ranks',
          name: 'admin-ranks',
          component: RanksPage,
          meta: { title: '阅界 - 榜单运营', requiresAuth: true, role: 'admin' },
        },
        {
          path: 'reviews',
          name: 'admin-reviews',
          component: ReviewsPage,
          meta: { title: '阅界 - 内容审核', requiresAuth: true, role: 'admin' },
        },
        {
          path: 'profile',
          name: 'admin-profile',
          component: ProfilePage,
          meta: { title: '阅界 - 个人信息', requiresAuth: true, role: 'admin' },
        },
      ],
    },
    {
      path: '/author',
      component: AuthorLayout,
      meta: { requiresAuth: true, role: 'author' },
      children: [
        {
          path: '',
          name: 'author',
          component: AuthorOverviewPage,
          meta: { title: '阅界 - 作者端工作台', requiresAuth: true, role: 'author' },
        },
        {
          path: 'works',
          name: 'author-works',
          component: AuthorPlaceholderPage,
          meta: { title: '阅界 - 作品管理', requiresAuth: true, role: 'author' },
        },
        {
          path: 'chapters',
          name: 'author-chapters',
          component: AuthorPlaceholderPage,
          meta: { title: '阅界 - 章节连载', requiresAuth: true, role: 'author' },
        },
        {
          path: 'stats',
          name: 'author-stats',
          component: AuthorPlaceholderPage,
          meta: { title: '阅界 - 数据中心', requiresAuth: true, role: 'author' },
        },
        {
          path: 'profile',
          name: 'author-profile',
          component: AuthorProfileView,
          meta: { title: '阅界 - 个人信息', requiresAuth: true, role: 'author' },
        },
      ],
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
    const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : ''
    if (redirect && redirect !== '/login') return redirect
    const last = getLastPath()
    if (last && last !== '/login') return last
    return auth.roleHome[auth.role]
  }

  return true
})

router.afterEach((to) => {
  document.title = (to.meta.title as string) || '阅界'
  const auth = useAuthStore()
  if (auth.isLoggedIn && to.name !== 'login') {
    setLastPath(to.fullPath)
  }
})

export default router
