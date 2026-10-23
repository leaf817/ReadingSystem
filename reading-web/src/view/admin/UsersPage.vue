<script setup lang="ts">
import { computed, onActivated, onMounted, onUnmounted, reactive, ref } from 'vue'
import { ApiError } from '@/api/request'
import { fetchUsersApi, createUserApi, updateUserApi, type ApiUser } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import type { AdminUser } from './adminData'
import './admin-shared.css'

const auth = useAuthStore()

type RoleTab = AdminUser['role']

const roleLabelMap: Record<ApiUser['role'], RoleTab> = {
  admin: '管理端',
  author: '作者端',
  reader: '读者端',
}

const roleApiMap: Record<RoleTab, ApiUser['role']> = {
  管理端: 'admin',
  作者端: 'author',
  读者端: 'reader',
}

const tabs: { key: RoleTab; label: string; tip: string }[] = [
  { key: '管理端', label: '管理端', tip: '平台运营与审核账号' },
  { key: '作者端', label: '作者端', tip: '签约作者与入驻申请' },
  { key: '读者端', label: '读者端', tip: '书城注册读者' },
]

const statusOptions: AdminUser['status'][] = ['正常', '禁用', '待审核']

const activeRole = ref<RoleTab>('读者端')
const keyword = ref('')
const list = ref<AdminUser[]>([])
const loading = ref(false)
const saving = ref(false)
const toast = ref('')
const tip = ref('')
let tipTimer: number | undefined
const editingId = ref<number | null>(null)
const creating = ref(false)

const editForm = reactive({
  name: '',
  account: '',
  password: '',
  status: '正常' as AdminUser['status'],
  avatar: '' as string,
})

const fileInput = ref<HTMLInputElement | null>(null)

const roleCounts = computed(() => {
  const counts: Record<RoleTab, number> = {
    管理端: 0,
    作者端: 0,
    读者端: 0,
  }
  for (const user of list.value) {
    counts[user.role] += 1
  }
  return counts
})

const filtered = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  return list.value.filter((u) => {
    if (u.role !== activeRole.value) return false
    if (!q) return true
    return u.name.toLowerCase().includes(q) || u.account.toLowerCase().includes(q)
  })
})

const activeTip = computed(() => tabs.find((t) => t.key === activeRole.value)?.tip ?? '')
const showDialog = computed(() => creating.value || editingId.value !== null)
const isCreateMode = computed(() => creating.value)
const editingUser = computed(() => list.value.find((u) => u.id === editingId.value) ?? null)
const dialogRole = computed(() => (isCreateMode.value ? activeRole.value : editingUser.value?.role ?? activeRole.value))
const dialogTitle = computed(() => (isCreateMode.value ? '新增用户' : '编辑用户'))

function mapUser(user: ApiUser): AdminUser {
  return {
    id: user.id,
    name: user.nickname,
    account: user.username,
    role: roleLabelMap[user.role] ?? '读者端',
    status: user.status,
    joinedAt: user.joinedAt,
    avatar: user.avatar || undefined,
  }
}

function applyUser(target: AdminUser, source: ApiUser) {
  const mapped = mapUser(source)
  Object.assign(target, mapped)
}

function showToast(message: string) {
  toast.value = message
  window.setTimeout(() => {
    toast.value = ''
  }, 1800)
}

function showTip(message: string) {
  tip.value = message
  if (tipTimer) window.clearTimeout(tipTimer)
  tipTimer = window.setTimeout(() => {
    tip.value = ''
    tipTimer = undefined
  }, 600)
}

async function loadUsers() {
  loading.value = true
  try {
    const users = await fetchUsersApi()
    list.value = users.map(mapUser)
  } catch (error) {
    const message = error instanceof ApiError ? error.message : '加载用户失败'
    showToast(message)
  } finally {
    loading.value = false
  }
}

async function persistUser(user: AdminUser, patch: Partial<AdminUser>) {
  const payload = {
    nickname: patch.name ?? user.name,
    username: patch.account ?? user.account,
    status: patch.status ?? user.status,
    avatar: patch.avatar === undefined ? user.avatar || null : patch.avatar || null,
  }
  const updated = await updateUserApi(user.id, payload)
  applyUser(user, updated)
  if (auth.user?.username === updated.username || auth.user?.username === user.account) {
    auth.patchProfile({
      username: updated.username,
      displayName: updated.nickname,
      avatar: updated.avatar || undefined,
    })
  }
  return user
}

function openCreate() {
  creating.value = true
  editingId.value = null
  editForm.name = ''
  editForm.account = ''
  editForm.password = ''
  editForm.status = '正常'
  editForm.avatar = ''
  if (fileInput.value) fileInput.value.value = ''
}

function openEdit(user: AdminUser) {
  creating.value = false
  editingId.value = user.id
  editForm.name = user.name
  editForm.account = user.account
  editForm.password = ''
  editForm.status = user.status
  editForm.avatar = user.avatar ?? ''
}

function closeEdit() {
  creating.value = false
  editingId.value = null
  editForm.password = ''
  if (fileInput.value) fileInput.value.value = ''
}

function triggerAvatarPick() {
  fileInput.value?.click()
}

function onAvatarFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    showTip('请选择图片文件')
    input.value = ''
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    showTip('头像大小不能超过 2MB')
    input.value = ''
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    editForm.avatar = String(reader.result ?? '')
  }
  reader.onerror = () => {
    showTip('头像读取失败，请重试')
  }
  reader.readAsDataURL(file)
}

function clearAvatar() {
  editForm.avatar = ''
  if (fileInput.value) fileInput.value.value = ''
}

async function saveEdit() {
  const name = editForm.name.trim()
  const account = editForm.account.trim()
  const password = editForm.password

  if (!name) {
    showTip('请输入昵称')
    return
  }
  if (name.length < 2) {
    showTip('昵称至少 2 个字符')
    return
  }
  if (!account) {
    showTip('请输入账号')
    return
  }
  if (account.length < 3) {
    showTip('账号至少 3 个字符')
    return
  }

  if (isCreateMode.value) {
    if (!password) {
      showTip('请输入密码')
      return
    }
    if (password.length < 6) {
      showTip('密码至少 6 位')
      return
    }

    saving.value = true
    try {
      const created = await createUserApi({
        nickname: name,
        username: account,
        password,
        role: roleApiMap[activeRole.value],
        status: activeRole.value === '管理端' ? '正常' : editForm.status,
        avatar: editForm.avatar || null,
      })
      list.value = [mapUser(created), ...list.value]
      closeEdit()
      showToast(`已新增「${created.nickname}」`)
    } catch (error) {
      showTip(error instanceof ApiError ? error.message : '新增失败')
    } finally {
      saving.value = false
    }
    return
  }

  const current = editingUser.value
  if (!current) return

  if (current.role === '管理端' && account !== 'admin') {
    showTip('管理员账号不可修改')
    return
  }

  saving.value = true
  try {
    await persistUser(current, {
      name,
      account: current.role === '管理端' ? current.account : account,
      status: current.role === '管理端' ? '正常' : editForm.status,
      avatar: editForm.avatar || undefined,
    })
    closeEdit()
    showToast(`已更新「${current.name}」`)
  } catch (error) {
    showTip(error instanceof ApiError ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(user: AdminUser) {
  if (user.role === '管理端') {
    showToast('管理员账号不可禁用')
    return
  }
  const next = user.status === '禁用' ? '正常' : '禁用'
  try {
    await persistUser(user, { status: next })
    showToast(`${user.name} 已设为「${user.status}」`)
  } catch (error) {
    showToast(error instanceof ApiError ? error.message : '更新状态失败')
  }
}

async function approve(user: AdminUser) {
  if (user.status !== '待审核') return
  try {
    await persistUser(user, { status: '正常' })
    showToast(`已通过 ${user.name} 的入驻申请`)
  } catch (error) {
    showToast(error instanceof ApiError ? error.message : '审核失败')
  }
}

function avatarText(name: string) {
  return name.trim().slice(0, 1) || '?'
}

function avatarTone(user: Pick<AdminUser, 'role'>) {
  if (user.role === '管理端') return 'tone-admin'
  if (user.role === '作者端') return 'tone-author'
  return 'tone-reader'
}

const avatarPreview = ref<{ src: string; name: string } | null>(null)

function openAvatarPreview(user: AdminUser) {
  if (!user.avatar) return
  avatarPreview.value = { src: user.avatar, name: user.name }
}

function closeAvatarPreview() {
  avatarPreview.value = null
}

function onPreviewKey(event: KeyboardEvent) {
  if (event.key === 'Escape') closeAvatarPreview()
}

onMounted(() => {
  void loadUsers()
  window.addEventListener('keydown', onPreviewKey)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onPreviewKey)
})

onActivated(() => {
  void loadUsers()
})
</script>

<template>
  <section class="users-page">
    <div class="role-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        class="role-tab"
        :class="{ 'is-active': activeRole === tab.key }"
        @click="activeRole = tab.key"
      >
        <strong>{{ tab.label }}</strong>
        <span>{{ roleCounts[tab.key] }} 人</span>
      </button>
    </div>

    <div class="admin-block">
      <div class="admin-block__head">
        <div>
          <h2>{{ activeRole }}用户</h2>
          <p class="sub">{{ activeTip }}</p>
        </div>
        <div class="admin-toolbar">
          <input
            v-model="keyword"
            class="admin-search"
            type="search"
            placeholder="搜索昵称 / 账号"
          />
          <button type="button" class="admin-btn admin-btn--primary" @click="openCreate">
            新增用户
          </button>
        </div>
      </div>

      <table class="user-table">
        <thead>
          <tr>
            <th class="col-avatar">头像</th>
            <th>昵称</th>
            <th>账号</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in filtered" :key="user.id">
            <td class="col-avatar">
              <span
                v-if="user.avatar"
                class="avatar avatar--img avatar--zoom"
                title="点击放大"
                @click="openAvatarPreview(user)"
              >
                <img :src="user.avatar" :alt="user.name" />
              </span>
              <span v-else class="avatar" :class="avatarTone(user)">{{ avatarText(user.name) }}</span>
            </td>
            <td class="col-name"><span class="cell-text">{{ user.name }}</span></td>
            <td class="col-account"><span class="cell-text">{{ user.account }}</span></td>
            <td class="col-status">
              <span
                class="status-pill"
                :class="{
                  'is-on': user.status === '正常',
                  'is-off': user.status === '禁用',
                  'is-warn': user.status === '待审核',
                }"
              >
                {{ user.status }}
              </span>
            </td>
            <td>{{ user.joinedAt }}</td>
            <td>
              <div class="ops">
                <button type="button" class="link-btn" @click="openEdit(user)">编辑</button>
                <template v-if="user.role !== '管理端'">
                  <button
                    v-if="user.status === '待审核'"
                    type="button"
                    class="link-btn"
                    @click="approve(user)"
                  >
                    通过
                  </button>
                  <button type="button" class="link-btn" @click="toggleStatus(user)">
                    {{ user.status === '禁用' ? '启用' : '禁用' }}
                  </button>
                </template>
                <span v-else class="muted">系统账号</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <p v-if="loading" class="admin-empty">正在同步用户...</p>
      <p v-else-if="!filtered.length" class="admin-empty">该角色下暂无匹配用户</p>
      <p v-if="toast" class="admin-toast">{{ toast }}</p>
    </div>

    <Teleport to="body">
      <div v-if="tip" class="tip-mask" aria-live="polite">
        <div class="tip-card">
          <p>{{ tip }}</p>
        </div>
      </div>
    </Teleport>

    <div v-if="showDialog" class="dialog-mask" @click.self="closeEdit">
      <div class="dialog" role="dialog" aria-modal="true" aria-labelledby="edit-user-title">
        <header class="dialog__head">
          <h3 id="edit-user-title">{{ dialogTitle }}</h3>
          <button type="button" class="dialog__close" @click="closeEdit">×</button>
        </header>

        <form class="dialog__body" @submit.prevent="saveEdit">
          <div class="dialog__avatar">
            <div class="avatar-edit">
              <button
                type="button"
                class="avatar-edit__pick"
                title="点击更换头像"
                @click="triggerAvatarPick"
              >
                <span v-if="editForm.avatar" class="avatar avatar--lg avatar--img">
                  <img :src="editForm.avatar" alt="头像预览" />
                </span>
                <span v-else class="avatar avatar--lg" :class="avatarTone({ role: dialogRole })">
                  {{ avatarText(editForm.name || dialogRole) }}
                </span>
                <span class="avatar-edit__hint">更换</span>
              </button>
              <button
                v-if="editForm.avatar"
                type="button"
                class="avatar-edit__remove"
                title="删除头像"
                @click.stop="clearAvatar"
              >
                ×
              </button>
            </div>
            <p>{{ dialogRole }}</p>
            <input
              ref="fileInput"
              class="sr-only"
              type="file"
              accept="image/png,image/jpeg,image/webp,image/gif"
              @change="onAvatarFileChange"
            />
          </div>

          <label class="field">
            <span>昵称</span>
            <input v-model="editForm.name" type="text" placeholder="请输入昵称" />
          </label>

          <label class="field">
            <span>账号</span>
            <input
              v-model="editForm.account"
              type="text"
              placeholder="请输入账号"
              :disabled="!isCreateMode && editingUser?.role === '管理端'"
            />
          </label>

          <label v-if="isCreateMode" class="field">
            <span>密码</span>
            <input
              v-model="editForm.password"
              type="password"
              placeholder="至少 6 位"
              autocomplete="new-password"
            />
          </label>

          <label class="field">
            <span>状态</span>
            <select
              v-model="editForm.status"
              :disabled="dialogRole === '管理端'"
            >
              <option v-for="item in statusOptions" :key="item" :value="item">
                {{ item }}
              </option>
            </select>
          </label>

          <div class="dialog__actions">
            <button type="button" class="btn-ghost" @click="closeEdit">取消</button>
            <button type="submit" class="btn-primary" :disabled="saving">
              {{ saving ? (isCreateMode ? '创建中...' : '保存中...') : isCreateMode ? '创建' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div
      v-if="avatarPreview"
      class="img-preview"
      role="dialog"
      aria-modal="true"
      :aria-label="avatarPreview.name"
      @click="closeAvatarPreview"
    >
      <img :src="avatarPreview.src" :alt="avatarPreview.name" />
      <p>{{ avatarPreview.name }}</p>
    </div>
  </section>
</template>

<style scoped>
.users-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.role-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.role-tab {
  border: 1px solid rgba(0, 0, 0, 0.05);
  background: #fff;
  border-radius: 14px;
  padding: 16px 18px;
  text-align: left;
  cursor: pointer;
  box-shadow: var(--shadow-soft);
  transition: border-color 0.2s, transform 0.2s, box-shadow 0.2s;
}

.role-tab strong {
  display: block;
  font-size: 16px;
  color: var(--ink);
}

.role-tab span {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: var(--ink-muted);
}

.role-tab:hover {
  transform: translateY(-1px);
  border-color: rgba(255, 77, 58, 0.25);
}

.role-tab.is-active {
  border-color: var(--brand);
  box-shadow: 0 10px 24px rgba(255, 77, 58, 0.12);
  background: linear-gradient(160deg, #fff7f5, #fff);
}

.role-tab.is-active strong {
  color: var(--brand);
}

.sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--ink-muted);
  font-weight: 400;
}

.muted {
  color: var(--ink-muted);
  font-size: 13px;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 13px;
  border: none;
}

.user-table th,
.user-table td {
  text-align: center;
  vertical-align: middle;
  padding: 14px 10px;
  border: none;
  border-bottom: 1px solid #f3f1ed;
}

.user-table th {
  color: var(--ink-muted);
  font-weight: 500;
  background: #faf9f7;
  padding-top: 12px;
  padding-bottom: 12px;
}

.user-table tbody tr:last-child td {
  border-bottom: none;
}

.user-table tbody tr:hover td {
  background: #fffaf8;
}

.user-table .col-avatar {
  width: 72px;
}

.user-table .col-name {
  font-weight: 600;
  color: var(--ink);
}

.user-table td {
  cursor: default;
}

.user-table .cell-text {
  cursor: pointer;
}

.user-table .col-account {
  color: var(--ink-secondary);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
}

.user-table .col-status {
  width: 100px;
}

.avatar {
  display: inline-grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.25);
  overflow: hidden;
}

.avatar--lg {
  width: 72px;
  height: 72px;
  font-size: 26px;
}

.avatar--img {
  background: #f0eeea;
  padding: 0;
}

.avatar--img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar--zoom {
  cursor: zoom-in;
}

.img-preview {
  position: fixed;
  inset: 0;
  z-index: 240;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 32px;
  background: rgba(18, 14, 12, 0.72);
  cursor: zoom-out;
}

.img-preview img {
  width: min(72vw, 360px);
  height: min(72vw, 360px);
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.45);
  background: #f0eeea;
}

.img-preview p {
  margin: 0;
  color: #fff;
  font-size: 14px;
  text-align: center;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.4);
}

.avatar.tone-admin {
  background: linear-gradient(145deg, #5b6cff, #3d4fd8);
}

.avatar.tone-author {
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
}

.avatar.tone-reader {
  background: linear-gradient(145deg, #3db8a0, #2a9a84);
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 52px;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
}

.status-pill.is-on {
  color: #2f6b4f;
  background: #eaf6ef;
}

.status-pill.is-off {
  color: #888;
  background: #f2f2f2;
}

.status-pill.is-warn {
  color: #c47a4a;
  background: #fff3e8;
}

.ops {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.link-btn {
  border: 0;
  background: transparent;
  color: var(--brand);
  cursor: pointer;
  font-size: 13px;
  padding: 0;
}

.link-btn:hover {
  text-decoration: underline;
}

.dialog-mask {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgba(20, 28, 40, 0.45);
}

.dialog {
  width: min(420px, 100%);
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.18);
  overflow: hidden;
}

.dialog__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid var(--line);
}

.dialog__head h3 {
  font-size: 16px;
  font-weight: 600;
}

.dialog__close {
  border: 0;
  background: transparent;
  font-size: 22px;
  line-height: 1;
  color: var(--ink-muted);
  cursor: pointer;
}

.dialog__body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px;
}

.dialog__avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 4px 0 6px;
}

.dialog__avatar p {
  font-size: 12px;
  color: var(--ink-muted);
}

.avatar-edit {
  position: relative;
  width: 72px;
  height: 72px;
}

.avatar-edit__pick {
  display: block;
  border: 0;
  padding: 0;
  background: transparent;
  cursor: pointer;
  border-radius: 50%;
}

.avatar-edit__pick:hover .avatar-edit__hint,
.avatar-edit__pick:focus-visible .avatar-edit__hint {
  opacity: 1;
}

.avatar-edit__hint {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: rgba(20, 28, 40, 0.48);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.2s;
  pointer-events: none;
}

.avatar-edit__remove {
  position: absolute;
  right: -2px;
  bottom: -2px;
  z-index: 2;
  width: 22px;
  height: 22px;
  border: 2px solid #fff;
  border-radius: 50%;
  background: #ff4d3a;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
  cursor: pointer;
  display: grid;
  place-items: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.18);
  padding: 0;
}

.avatar-edit__remove:hover {
  background: #e43d2c;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
  color: var(--ink-secondary);
}

.field input,
.field select {
  height: 40px;
  padding: 0 12px;
  border-radius: 10px;
  border: 1px solid #e5e2dc;
  background: #faf9f7;
  outline: 0;
  font-size: 14px;
  color: var(--ink);
}

.field input:focus,
.field select:focus {
  border-color: rgba(255, 77, 58, 0.45);
  background: #fff;
}

.field input:disabled,
.field select:disabled {
  color: var(--ink-muted);
  cursor: not-allowed;
}

.tip-mask {
  position: fixed;
  inset: 0;
  z-index: 400;
  display: grid;
  place-items: center;
  pointer-events: none;
  background: rgba(20, 28, 40, 0.18);
}

.tip-card {
  pointer-events: none;
  min-width: 200px;
  max-width: min(320px, calc(100vw - 48px));
  padding: 18px 22px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid rgba(255, 77, 58, 0.12);
  box-shadow: 0 16px 40px rgba(20, 28, 40, 0.18);
  text-align: center;
  animation: tip-in 0.2s ease-out;
}

.tip-card p {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #2a3340;
  line-height: 1.45;
}

@keyframes tip-in {
  from {
    opacity: 0;
    transform: translateY(6px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.dialog__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 4px;
}

.btn-ghost,
.btn-primary {
  height: 36px;
  padding: 0 16px;
  border-radius: 8px;
  border: 0;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
}

.btn-ghost {
  background: #f3f3f3;
  color: var(--ink-secondary);
}

.btn-primary {
  background: var(--brand);
  color: #fff;
}

@media (max-width: 720px) {
  .role-tabs {
    grid-template-columns: 1fr;
  }
}
</style>
