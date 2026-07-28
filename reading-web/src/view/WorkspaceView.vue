<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore, type UserRole } from '@/stores/auth'

const props = defineProps<{
  role: UserRole
}>()

const auth = useAuthStore()
const router = useRouter()

const titleMap: Record<UserRole, string> = {
  admin: '管理端工作台',
  author: '作者端工作台',
  reader: '读者中心',
}

const descMap: Record<UserRole, string> = {
  admin: '内容审核、书籍上下架、用户与榜单运营。',
  author: '作品管理、章节连载、数据概览。',
  reader: '书架、阅读历史与个性推荐。',
}

const title = computed(() => titleMap[props.role])
const desc = computed(() => descMap[props.role])

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="workspace">
    <header class="bar">
      <RouterLink to="/" class="brand">阅界 · {{ auth.roleLabel[role] }}</RouterLink>
      <div class="actions">
        <span v-if="auth.user" class="user">{{ auth.user.displayName }}</span>
        <RouterLink v-if="role === 'author'" to="/author/profile" class="link">个人信息</RouterLink>
        <RouterLink to="/" class="link">返回书城</RouterLink>
        <button type="button" class="logout" @click="logout">退出登录</button>
      </div>
    </header>

    <main class="card">
      <p class="eyebrow">WORKSPACE</p>
      <h1>{{ title }}</h1>
      <p class="desc">{{ desc }}</p>
      <p class="tip">登录流程已接通，后续可在此扩展业务页面。</p>
    </main>
  </div>
</template>

<style scoped>
.workspace {
  min-height: 100vh;
  background:
    radial-gradient(ellipse 60% 40% at 10% 0%, rgba(120, 170, 255, 0.18), transparent 50%),
    var(--page);
}

.bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px clamp(16px, 4vw, 40px);
  background: linear-gradient(180deg, #f3f8ff, #eef5ff);
  border-bottom: 1px solid rgba(120, 160, 220, 0.22);
}

.brand {
  font-family: var(--font-display);
  font-size: 22px;
  color: var(--brand);
  letter-spacing: 0.06em;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}

.user {
  color: #5f6f86;
}

.link {
  color: #5f6f86;
}

.link:hover {
  color: var(--brand);
}

.logout {
  border: 0;
  height: 32px;
  padding: 0 14px;
  border-radius: 999px;
  background: #fff;
  color: var(--brand);
  cursor: pointer;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(255, 77, 58, 0.12);
}

.card {
  width: min(720px, calc(100% - 32px));
  margin: 48px auto;
  padding: 36px 32px;
  border-radius: 20px;
  background: #fff;
  box-shadow: var(--shadow-soft);
  border: 1px solid rgba(120, 160, 220, 0.14);
}

.eyebrow {
  font-size: 12px;
  letter-spacing: 0.24em;
  color: #8aa0bc;
  margin-bottom: 10px;
}

h1 {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 400;
  margin-bottom: 12px;
}

.desc {
  font-size: 15px;
  color: #5f6f86;
  line-height: 1.7;
}

.tip {
  margin-top: 22px;
  font-size: 13px;
  color: #8aa0bc;
}
</style>
