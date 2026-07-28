<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ApiError } from '@/api/request'
import { changePasswordApi } from '@/api/auth'

const emit = defineEmits<{
  success: [message: string]
}>()

const saving = ref(false)
const error = ref('')

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

async function submit() {
  error.value = ''
  if (!form.oldPassword) {
    error.value = '请输入当前密码'
    return
  }
  if (!form.newPassword) {
    error.value = '请输入新密码'
    return
  }
  if (form.newPassword.length < 6) {
    error.value = '新密码至少 6 位'
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    error.value = '两次新密码不一致'
    return
  }
  if (form.newPassword === form.oldPassword) {
    error.value = '新密码不能与当前密码相同'
    return
  }

  saving.value = true
  try {
    await changePasswordApi({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword,
    })
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
    emit('success', '密码已修改')
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '修改密码失败'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <section class="pwd-card">
    <header class="pwd-card__head">
      <h3>修改密码</h3>
      <p>验证当前密码后设置新密码</p>
    </header>

    <div class="fields">
      <label class="field">
        <span>当前密码</span>
        <input
          v-model="form.oldPassword"
          type="password"
          autocomplete="current-password"
          placeholder="请输入当前密码"
        />
      </label>
      <label class="field">
        <span>新密码</span>
        <input
          v-model="form.newPassword"
          type="password"
          autocomplete="new-password"
          placeholder="至少 6 位"
        />
      </label>
      <label class="field">
        <span>确认新密码</span>
        <input
          v-model="form.confirmPassword"
          type="password"
          autocomplete="new-password"
          placeholder="再次输入新密码"
        />
      </label>
    </div>

    <p v-if="error" class="msg err">{{ error }}</p>

    <footer class="actions">
      <button type="button" class="btn" :disabled="saving" @click="submit">
        {{ saving ? '提交中…' : '确认修改' }}
      </button>
    </footer>
  </section>
</template>

<style scoped>
.pwd-card {
  background: #fff;
  border-radius: 20px;
  padding: 28px 28px 24px;
  border: 1px solid #d8dde6;
  box-shadow:
    0 22px 48px rgba(20, 28, 40, 0.28),
    0 4px 12px rgba(20, 28, 40, 0.1);
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.pwd-card__head {
  padding-right: 36px;
}

.pwd-card__head h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: #141c28;
}

.pwd-card__head p {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.5;
  color: #5a6575;
}

.fields {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #2a3340;
}

.field input {
  height: 46px;
  padding: 0 14px;
  border-radius: 12px;
  border: 1.5px solid #c5ccd8;
  background: #eef1f5;
  outline: 0;
  font-size: 14px;
  font-weight: 500;
  color: #141c28;
  transition:
    border-color 0.18s ease,
    background 0.18s ease,
    box-shadow 0.18s ease;
}

.field input::placeholder {
  color: #7a8494;
  font-weight: 400;
}

.field input:hover {
  border-color: #9aa5b5;
  background: #e8ecf2;
}

.field input:focus {
  border-color: #24344d;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(36, 52, 77, 0.18);
}

.actions {
  display: flex;
  padding-top: 2px;
}

.btn {
  width: 100%;
  height: 46px;
  border: 0;
  border-radius: 12px;
  background: #1a2738;
  color: #fff;
  font-weight: 700;
  font-size: 15px;
  letter-spacing: 0.03em;
  cursor: pointer;
  box-shadow: 0 10px 22px rgba(26, 39, 56, 0.35);
  transition:
    transform 0.15s ease,
    background 0.15s ease,
    box-shadow 0.15s ease,
    opacity 0.15s ease;
}

.btn:hover:not(:disabled) {
  background: #24344d;
  transform: translateY(-1px);
  box-shadow: 0 12px 26px rgba(26, 39, 56, 0.42);
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.msg.err {
  margin: -6px 0 0;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.45;
  padding: 10px 12px;
  border-radius: 10px;
  color: #c0392b;
  background: #ffe8e4;
  border: 1px solid #f0a89c;
}
</style>
