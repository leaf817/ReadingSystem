<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ApiError } from '@/api/request'
import { fetchMeApi, updateMeApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import ChangePasswordCard from '@/components/ChangePasswordCard.vue'

const auth = useAuthStore()

const loading = ref(true)
const saving = ref(false)
const editing = ref(false)
const passwordOpen = ref(false)
const error = ref('')
const toast = ref('')
let toastTimer = 0
const fileInput = ref<HTMLInputElement | null>(null)

const form = reactive({
  nickname: '',
  username: '',
  avatar: '',
  bio: '',
  joinedAt: '',
})

const snapshot = reactive({
  nickname: '',
  avatar: '',
  bio: '',
})

const avatarText = computed(() => form.nickname.trim().slice(0, 1) || '读')

async function loadProfile() {
  loading.value = true
  error.value = ''
  try {
    const me = await fetchMeApi()
    form.nickname = me.nickname
    form.username = me.username
    form.avatar = me.avatar || ''
    form.bio = me.bio || ''
    form.joinedAt = me.joinedAt
    auth.patchProfile({
      displayName: me.nickname,
      avatar: me.avatar || undefined,
      bio: me.bio || undefined,
      username: me.username,
    })
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '加载资料失败'
    if (auth.user) {
      form.nickname = auth.user.displayName
      form.username = auth.user.username
      form.avatar = auth.user.avatar || ''
      form.bio = auth.user.bio || ''
    }
  } finally {
    loading.value = false
  }
}

function triggerAvatar() {
  if (!editing.value) return
  fileInput.value?.click()
}

function startEdit() {
  snapshot.nickname = form.nickname
  snapshot.avatar = form.avatar
  snapshot.bio = form.bio
  editing.value = true
  error.value = ''
}

function cancelEdit() {
  form.nickname = snapshot.nickname
  form.avatar = snapshot.avatar
  form.bio = snapshot.bio
  if (fileInput.value) fileInput.value.value = ''
  editing.value = false
  error.value = ''
}

function openPassword() {
  passwordOpen.value = true
}

function onPasswordSuccess(message: string) {
  passwordOpen.value = false
  showToast(message)
}

function onAvatarChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    error.value = '请选择图片文件'
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    error.value = '头像不能超过 2MB'
    return
  }
  const reader = new FileReader()
  reader.onload = () => {
    form.avatar = String(reader.result ?? '')
    error.value = ''
  }
  reader.readAsDataURL(file)
}

function clearAvatar() {
  form.avatar = ''
  if (fileInput.value) fileInput.value.value = ''
}

function showToast(message: string) {
  toast.value = message
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toast.value = ''
  }, 2200)
}

async function save() {
  const nickname = form.nickname.trim()
  if (nickname.length < 2) {
    error.value = '昵称至少 2 个字符'
    return
  }
  if (form.bio.trim().length > 100) {
    error.value = '签名不能超过 100 字'
    return
  }
  saving.value = true
  error.value = ''
  try {
    const updated = await updateMeApi({
      nickname,
      avatar: form.avatar || null,
      bio: form.bio.trim() || null,
    })
    form.nickname = updated.nickname
    form.avatar = updated.avatar || ''
    form.bio = updated.bio || ''
    auth.patchProfile({
      displayName: updated.nickname,
      avatar: updated.avatar || undefined,
      bio: updated.bio || undefined,
    })
    editing.value = false
    showToast('资料已保存')
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '保存失败'
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  void loadProfile()
})
</script>

<template>
  <section class="profile-page">
    <p v-if="loading" class="hint">正在加载…</p>

    <div v-else class="shell">
      <aside class="identity">
        <div class="identity__top">
          <div class="avatar-wrap">
            <button
              type="button"
              class="avatar-btn"
              :class="{ 'avatar-btn--locked': !editing }"
              :title="editing ? '点击更换头像' : '请先点击编辑'"
              @click="triggerAvatar"
            >
              <span v-if="form.avatar" class="avatar avatar--img">
                <img :src="form.avatar" alt="头像" />
              </span>
              <span v-else class="avatar">{{ avatarText }}</span>
              <span v-if="editing" class="avatar-hint">更换</span>
            </button>
            <button
              v-if="editing && form.avatar"
              type="button"
              class="avatar-del"
              title="删除头像"
              @click="clearAvatar"
            >
              ×
            </button>
          </div>
          <h2>{{ form.nickname || '读者' }}</h2>
          <span class="role-badge">读者</span>
        </div>

        <ul class="meta-list">
          <li>
            <span>登录账号</span>
            <strong>{{ form.username || '—' }}</strong>
          </li>
          <li>
            <span>加入时间</span>
            <strong>{{ form.joinedAt || '—' }}</strong>
          </li>
          <li>
            <span>账号类型</span>
            <strong>读者账号</strong>
          </li>
        </ul>

        <div class="account-mgmt">
          <p class="account-mgmt__label">账号管理</p>
          <button type="button" class="account-mgmt__btn" @click="openPassword">修改密码</button>
        </div>

        <input
          ref="fileInput"
          class="sr-only"
          type="file"
          accept="image/png,image/jpeg,image/webp,image/gif"
          @change="onAvatarChange"
        />
      </aside>

      <div class="main-col">
        <section class="panel">
          <div class="panel__head">
            <h3>账号资料</h3>
            <p>{{ editing ? '可修改昵称、签名与头像，账号不可更改' : '点击编辑后可修改昵称、签名与头像' }}</p>
          </div>

          <div class="fields">
            <label class="field field--full">
              <span>昵称</span>
              <input
                v-model="form.nickname"
                type="text"
                maxlength="64"
                placeholder="请输入昵称"
                :disabled="!editing"
              />
            </label>
            <label class="field">
              <span>登录账号</span>
              <input :value="form.username" type="text" disabled />
            </label>
            <label class="field">
              <span>角色</span>
              <input value="读者" type="text" disabled />
            </label>
            <label class="field field--full">
              <span>签名</span>
              <textarea
                v-model="form.bio"
                rows="3"
                maxlength="100"
                placeholder="一句话介绍自己（最多 100 字）"
                :disabled="!editing"
              />
              <em class="field-count">{{ form.bio.length }}/100</em>
            </label>
          </div>

          <p v-if="error" class="msg err">{{ error }}</p>

          <div class="actions">
            <template v-if="editing">
              <button type="button" class="ghost-btn" :disabled="saving" @click="cancelEdit">取消</button>
              <button type="button" class="primary" :disabled="saving" @click="save">
                {{ saving ? '保存中…' : '保存' }}
              </button>
            </template>
            <button v-else type="button" class="primary" @click="startEdit">编辑</button>
          </div>
        </section>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="passwordOpen" class="pwd-mask" @click="passwordOpen = false">
        <div class="pwd-dialog" role="dialog" aria-modal="true" aria-label="修改密码" @click.stop>
          <button type="button" class="pwd-close" title="关闭" @click="passwordOpen = false">×</button>
          <ChangePasswordCard @success="onPasswordSuccess" />
        </div>
      </div>

      <div v-if="toast" class="toast-mask" @click="toast = ''">
        <div class="toast" role="status" @click.stop>
          <span class="toast__icon" aria-hidden="true">✓</span>
          <p>{{ toast }}</p>
        </div>
      </div>
    </Teleport>
  </section>
</template>

<style scoped>
.profile-page {
  min-height: 420px;
}

.hint {
  color: #5a6575;
  padding: 24px 0;
}

.shell {
  display: grid;
  grid-template-columns: minmax(260px, 300px) 1fr;
  gap: 18px;
  align-items: start;
}

.identity {
  border-radius: 18px;
  padding: 28px 22px;
  background:
    linear-gradient(180deg, #fff8f5 0%, #fff 42%);
  color: var(--ink);
  display: flex;
  flex-direction: column;
  gap: 22px;
  border: 1px solid rgba(255, 77, 58, 0.1);
  box-shadow: var(--shadow-soft);
  min-height: 100%;
}

.identity__top {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
}

.avatar-wrap {
  position: relative;
  width: 96px;
  height: 96px;
}

.avatar-btn {
  position: relative;
  border: 0;
  padding: 0;
  background: transparent;
  border-radius: 50%;
  cursor: pointer;
}

.avatar-btn--locked {
  cursor: default;
}

.avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  display: inline-grid;
  place-items: center;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
  color: #fff;
  font-size: 34px;
  font-weight: 700;
  overflow: hidden;
  box-shadow: 0 0 0 4px #fff, 0 8px 18px rgba(255, 77, 58, 0.2);
}

.avatar--img {
  background: #f0eeea;
}

.avatar--img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-hint {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: rgba(20, 28, 40, 0.48);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.2s;
}

.avatar-btn:hover .avatar-hint {
  opacity: 1;
}

.avatar-del {
  position: absolute;
  right: -2px;
  bottom: -2px;
  z-index: 2;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 2px solid #fff;
  background: #ff4d3a;
  color: #fff;
  cursor: pointer;
  font-size: 15px;
  line-height: 1;
  padding: 0;
}

.identity__top h2 {
  font-size: 22px;
  font-weight: 600;
}

.role-badge {
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  background: var(--brand-soft);
  color: var(--brand);
  font-size: 12px;
  font-weight: 600;
}

.meta-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meta-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f7f4f0;
}

.meta-list span {
  font-size: 12px;
  color: var(--ink-muted);
}

.meta-list strong {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink);
}

.account-mgmt {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.account-mgmt__label {
  font-size: 12px;
  color: var(--ink-muted);
}

.account-mgmt__btn {
  height: 40px;
  border: 1px solid rgba(255, 77, 58, 0.18);
  border-radius: 10px;
  background: #fff;
  color: var(--brand);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.account-mgmt__btn:hover {
  background: var(--brand-soft);
  border-color: var(--brand);
}

.main-col {
  min-width: 0;
}

.panel {
  background: #fff;
  border-radius: 18px;
  padding: 24px 26px;
  border: 1px solid rgba(0, 0, 0, 0.045);
  box-shadow: var(--shadow-soft);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel__head h3 {
  font-size: 18px;
  font-weight: 700;
  color: #141c28;
}

.panel__head p {
  margin-top: 6px;
  font-size: 13px;
  color: #5a6575;
}

.fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #2a3340;
}

.field--full {
  grid-column: 1 / -1;
}

.field input {
  height: 44px;
  padding: 0 14px;
  border-radius: 12px;
  border: 1px solid #e6e1db;
  background: #f7f4f0;
  outline: 0;
  font-size: 14px;
  font-weight: 500;
  color: #141c28;
}

.field textarea {
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid #e6e1db;
  background: #f7f4f0;
  outline: 0;
  font-size: 14px;
  font-weight: 500;
  color: #141c28;
  font: inherit;
  resize: vertical;
  min-height: 88px;
}

.field-count {
  align-self: flex-end;
  font-size: 12px;
  font-style: normal;
  font-weight: 500;
  color: #7a8494;
}

.field input:disabled,
.field textarea:disabled {
  color: #7a8494;
  cursor: not-allowed;
}

.field input:focus,
.field textarea:focus {
  border-color: var(--brand);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(255, 77, 58, 0.12);
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.ghost-btn {
  height: 42px;
  padding: 0 18px;
  border-radius: 10px;
  border: 1.5px solid #c5ccd8;
  background: #fff;
  color: #2a3340;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
}

.primary {
  height: 42px;
  padding: 0 22px;
  border: 0;
  border-radius: 10px;
  background: var(--brand);
  color: #fff;
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(255, 77, 58, 0.22);
}

.primary:disabled,
.ghost-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  box-shadow: none;
}

.msg.err {
  font-size: 13px;
  font-weight: 600;
  padding: 10px 12px;
  border-radius: 10px;
  color: #c0392b;
  background: #ffe8e4;
  border: 1px solid #f0a89c;
}

.pwd-mask {
  position: fixed;
  inset: 0;
  z-index: 190;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(14, 20, 30, 0.62);
  backdrop-filter: blur(3px);
}

.pwd-dialog {
  position: relative;
  width: min(440px, 100%);
}

.pwd-close {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 2;
  width: 32px;
  height: 32px;
  border: 1px solid #c5ccd8;
  border-radius: 50%;
  background: #eef1f5;
  color: #3a4556;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
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
  pointer-events: auto;
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

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
}

@media (max-width: 900px) {
  .shell {
    grid-template-columns: 1fr;
  }

  .fields {
    grid-template-columns: 1fr;
  }

  .actions {
    justify-content: stretch;
  }

  .ghost-btn,
  .primary {
    flex: 1;
  }
}
</style>
