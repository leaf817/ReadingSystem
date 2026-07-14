<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

type Mode = 'login' | 'register'

const router = useRouter()
const auth = useAuthStore()

const mode = ref<Mode>('login')
const error = ref('')
const success = ref('')
const loading = ref(false)
const showPassword = ref(false)

const form = reactive({
  nickname: '',
  username: '',
  password: '',
  confirm: '',
  remember: true,
})

const isLogin = computed(() => mode.value === 'login')
const title = computed(() => (isLogin.value ? '欢迎回来' : '加入阅界'))
const subtitle = computed(() =>
  isLogin.value ? '继续你的阅读旅程' : '填写信息，开启你的阅读之旅',
)
const submitText = computed(() => {
  if (loading.value) return isLogin.value ? '登录中…' : '注册中…'
  return isLogin.value ? '登录' : '注册并进入书城'
})

watch(mode, () => {
  error.value = ''
  success.value = ''
  form.confirm = ''
  form.nickname = ''
  showPassword.value = false
})

function switchMode(next: Mode) {
  mode.value = next
}

async function handleSubmit() {
  error.value = ''
  success.value = ''
  loading.value = true
  await new Promise((r) => setTimeout(r, 280))

  const result = isLogin.value
    ? auth.login(form.username, form.password)
    : auth.register(form.username, form.password, form.confirm, form.nickname)

  loading.value = false

  if (!result.ok) {
    error.value = result.message
    return
  }

  success.value = result.message
  router.push(result.redirectTo)
}
</script>

<template>
  <div class="page">
    <header class="top">
      <RouterLink to="/" class="brand" aria-label="阅界首页">
        <span class="brand__mark" aria-hidden="true">
          <svg viewBox="0 0 48 48" width="32" height="32">
            <defs>
              <linearGradient id="loginLogo" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="#ff6a4d" />
                <stop offset="100%" stop-color="#e63a28" />
              </linearGradient>
            </defs>
            <rect x="4" y="6" width="40" height="36" rx="10" fill="url(#loginLogo)" />
            <path
              d="M24 12c-6.2 3.2-10 4.6-14 5.2V34c4.2-.7 8.2-2.1 14-5.2 5.8 3.1 9.8 4.5 14 5.2V17.2C34 16.6 30.2 15.2 24 12z"
              fill="#fff"
              opacity="0.96"
            />
            <path d="M24 12v16.8" stroke="#ff4d3a" stroke-width="1.6" stroke-linecap="round" />
          </svg>
        </span>
        <span class="brand__name">阅界</span>
      </RouterLink>
      <RouterLink to="/" class="back">返回书城</RouterLink>
    </header>

    <main class="main">
      <div class="card">
        <header class="card__head">
          <h1>{{ title }}</h1>
          <p class="sub">{{ subtitle }}</p>
        </header>

        <div class="tabs">
          <button type="button" :class="{ 'is-active': isLogin }" @click="switchMode('login')">
            登录
          </button>
          <button type="button" :class="{ 'is-active': !isLogin }" @click="switchMode('register')">
            注册
          </button>
        </div>

        <form class="form" @submit.prevent="handleSubmit">
          <label v-if="!isLogin" class="field">
            <span class="field__label">昵称</span>
            <input
              v-model="form.nickname"
              type="text"
              autocomplete="nickname"
              placeholder="给自己起个昵称"
            />
          </label>

          <label class="field">
            <span class="field__label">账号</span>
            <input
              v-model="form.username"
              type="text"
              autocomplete="username"
              placeholder="手机号 / 邮箱 / 用户名"
            />
          </label>

          <label class="field">
            <span class="field__label">密码</span>
            <div class="password">
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                :autocomplete="isLogin ? 'current-password' : 'new-password'"
                placeholder="请输入密码"
              />
              <button type="button" class="eye" @click="showPassword = !showPassword">
                {{ showPassword ? '隐藏' : '显示' }}
              </button>
            </div>
          </label>

          <label v-if="!isLogin" class="field">
            <span class="field__label">确认密码</span>
            <input
              v-model="form.confirm"
              type="password"
              autocomplete="new-password"
              placeholder="再次输入密码"
            />
          </label>

          <div v-if="isLogin" class="tools">
            <label class="remember">
              <input v-model="form.remember" type="checkbox" />
              <span>记住我</span>
            </label>
            <a href="javascript:;" class="forgot">忘记密码？</a>
          </div>

          <p v-if="error" class="error" role="alert">{{ error }}</p>
          <p v-else-if="success" class="ok" role="status">{{ success }}</p>

          <button type="submit" class="submit" :disabled="loading">
            {{ submitText }}
          </button>
        </form>

        <p class="note">
          <template v-if="isLogin">
            没有账号？
            <button type="button" class="text-btn" @click="switchMode('register')">去注册</button>
          </template>
          <template v-else>
            已有账号？
            <button type="button" class="text-btn" @click="switchMode('login')">去登录</button>
          </template>
        </p>

        <p v-if="isLogin" class="demo">演示：admin / author / reader，密码 123456</p>
      </div>
    </main>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(ellipse 80% 50% at 8% -8%, rgba(255, 77, 58, 0.09), transparent 52%),
    radial-gradient(ellipse 55% 40% at 92% 0%, rgba(245, 166, 35, 0.07), transparent 48%),
    var(--page);
}

.top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px clamp(16px, 4vw, 40px);
  background: linear-gradient(180deg, #f3f8ff 0%, #eef5ff 55%, #e8f1ff 100%);
  border-bottom: 1px solid rgba(120, 160, 220, 0.22);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.brand__mark {
  display: grid;
  place-items: center;
  filter: drop-shadow(0 4px 10px rgba(255, 77, 58, 0.22));
}

.brand__name {
  font-family: var(--font-display);
  font-size: 24px;
  color: var(--brand);
  letter-spacing: 0.12em;
}

.back {
  font-size: 13px;
  color: #5f6f86;
  padding: 7px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(120, 160, 220, 0.2);
  transition: color 0.2s, background 0.2s;
}

.back:hover {
  color: var(--brand);
  background: #fff;
}

.main {
  flex: 1;
  display: grid;
  place-items: center;
  padding: 32px 16px 48px;
}

.card {
  width: min(400px, 100%);
  padding: 40px 36px 32px;
  border-radius: 20px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 16px 48px rgba(26, 26, 26, 0.08);
}

.card__head {
  text-align: center;
  margin-bottom: 28px;
}

.card__head h1 {
  font-family: var(--font-serif);
  font-size: 34px;
  font-weight: 700;
  letter-spacing: 0.22em;
  line-height: 1.35;
  background: linear-gradient(135deg, #2a3344 18%, #ff4d3a 88%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-indent: 0.22em;
}

.sub {
  margin-top: 12px;
  font-size: 13px;
  color: var(--ink-muted);
  line-height: 1.6;
  letter-spacing: 0.06em;
}

.tabs {
  display: flex;
  gap: 28px;
  justify-content: center;
  margin-bottom: 28px;
  border-bottom: 1px solid var(--line);
}

.tabs button {
  position: relative;
  border: 0;
  background: transparent;
  padding: 0 4px 12px;
  font-size: 15px;
  color: var(--ink-muted);
  cursor: pointer;
  transition: color 0.2s;
}

.tabs button.is-active {
  color: var(--brand);
  font-weight: 600;
}

.tabs button.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 2px;
  border-radius: 2px;
  background: var(--brand);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field__label {
  font-size: 13px;
  color: var(--ink-secondary);
}

.field input,
.password {
  height: 46px;
  border-radius: 10px;
  border: 1px solid #e5e2dc;
  background: #faf9f7;
}

.field input {
  padding: 0 14px;
  outline: 0;
  font-size: 14px;
  color: var(--ink);
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}

.field input::placeholder {
  color: #b0aaa2;
}

.field input:focus,
.password:focus-within {
  border-color: rgba(255, 77, 58, 0.5);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(255, 77, 58, 0.1);
}

.password {
  display: flex;
  align-items: center;
  overflow: hidden;
}

.password input {
  flex: 1;
  height: 100%;
  border: 0;
  background: transparent;
  box-shadow: none !important;
}

.eye {
  border: 0;
  background: transparent;
  color: var(--ink-muted);
  height: 100%;
  padding: 0 14px;
  cursor: pointer;
  font-size: 12px;
}

.eye:hover {
  color: var(--brand);
}

.tools {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

.remember {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--ink-secondary);
  cursor: pointer;
}

.remember input {
  accent-color: var(--brand);
}

.forgot {
  color: var(--ink-muted);
}

.forgot:hover {
  color: var(--brand);
}

.error,
.ok {
  font-size: 13px;
  border-radius: 10px;
  padding: 10px 12px;
}

.error {
  color: var(--brand);
  background: var(--brand-soft);
}

.ok {
  color: #2f6b4f;
  background: #eef8f2;
}

.submit {
  margin-top: 4px;
  height: 48px;
  border: 0;
  border-radius: 10px;
  background: var(--brand);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
}

.submit:hover:not(:disabled) {
  background: var(--brand-dark);
  transform: translateY(-1px);
}

.submit:disabled {
  opacity: 0.7;
  cursor: wait;
}

.note {
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  color: var(--ink-muted);
}

.text-btn {
  border: 0;
  background: transparent;
  color: var(--brand);
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  padding: 0;
}

.demo {
  margin-top: 10px;
  text-align: center;
  font-size: 12px;
  color: #b0aaa2;
}

@media (max-width: 480px) {
  .card {
    padding: 32px 22px 26px;
  }
}
</style>
