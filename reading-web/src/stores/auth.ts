import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { loginApi, registerApi } from '@/api/auth'
import { ApiError, setToken } from '@/api/request'

export type UserRole = 'admin' | 'author' | 'reader'

export interface AuthUser {
  username: string
  role: UserRole
  displayName: string
  avatar?: string
  bio?: string
}

const SESSION_KEY = 'yuejie-session'
const AVATAR_KEY = 'yuejie-avatar'
const LAST_PATH_KEY = 'yuejie-last-path'
/** 头像过大时不写入 localStorage，避免配额失败导致整份会话丢失 */
const MAX_AVATAR_PERSIST_CHARS = 120_000

const roleHome: Record<UserRole, string> = {
  admin: '/admin',
  author: '/author',
  reader: '/',
}

const roleProfile: Record<UserRole, string> = {
  admin: '/admin/profile',
  author: '/author/profile',
  reader: '/mine/profile',
}

const roleLabel: Record<UserRole, string> = {
  admin: '管理端',
  author: '作者端',
  reader: '读者端',
}

function loadSession(): AuthUser | null {
  try {
    const raw = localStorage.getItem(SESSION_KEY)
    if (!raw) return null
    const user = JSON.parse(raw) as AuthUser
    if (!user?.username || !user?.role) return null
    const avatar = localStorage.getItem(AVATAR_KEY)
    if (avatar) user.avatar = avatar
    return user
  } catch {
    return null
  }
}

function saveSession(user: AuthUser | null) {
  if (!user) {
    localStorage.removeItem(SESSION_KEY)
    localStorage.removeItem(AVATAR_KEY)
    return
  }

  const { avatar, ...core } = user
  try {
    localStorage.setItem(SESSION_KEY, JSON.stringify(core))
  } catch {
    try {
      localStorage.setItem(
        SESSION_KEY,
        JSON.stringify({
          username: core.username,
          role: core.role,
          displayName: core.displayName,
        }),
      )
    } catch {
      /* ignore */
    }
  }

  try {
    if (avatar && avatar.length <= MAX_AVATAR_PERSIST_CHARS) {
      localStorage.setItem(AVATAR_KEY, avatar)
    } else {
      localStorage.removeItem(AVATAR_KEY)
    }
  } catch {
    localStorage.removeItem(AVATAR_KEY)
  }
}

export function getLastPath() {
  try {
    return localStorage.getItem(LAST_PATH_KEY) || ''
  } catch {
    return ''
  }
}

export function setLastPath(path: string) {
  if (!path || path.startsWith('/login')) return
  try {
    localStorage.setItem(LAST_PATH_KEY, path)
  } catch {
    /* ignore */
  }
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<AuthUser | null>(loadSession())

  const isLoggedIn = computed(() => !!user.value)
  const role = computed(() => user.value?.role ?? null)

  function setUser(next: AuthUser | null) {
    user.value = next
    saveSession(next)
  }

  async function login(username: string, password: string) {
    const name = username.trim()
    if (!name) return { ok: false as const, message: '请输入账号' }
    if (!password) return { ok: false as const, message: '请输入密码' }

    try {
      const data = await loginApi({ username: name, password })
      setToken(data.token)
      setUser({
        username: data.username,
        role: data.role,
        displayName: data.displayName || data.nickname,
        avatar: data.avatar || undefined,
      })
      return {
        ok: true as const,
        redirectTo: roleHome[data.role],
        message: `已进入${roleLabel[data.role]}`,
      }
    } catch (error) {
      const message = error instanceof ApiError ? error.message : '登录失败，请检查后端是否启动'
      return { ok: false as const, message }
    }
  }

  async function register(username: string, password: string, confirm: string, nickname: string) {
    const name = username.trim()
    const nick = nickname.trim()
    if (!nick) return { ok: false as const, message: '请输入昵称' }
    if (nick.length < 2) return { ok: false as const, message: '昵称至少 2 个字符' }
    if (!name) return { ok: false as const, message: '请输入账号' }
    if (name.length < 3) return { ok: false as const, message: '账号至少 3 个字符' }
    if (!password) return { ok: false as const, message: '请输入密码' }
    if (password.length < 6) return { ok: false as const, message: '密码至少 6 位' }
    if (password !== confirm) return { ok: false as const, message: '两次密码不一致' }

    try {
      const data = await registerApi({
        username: name,
        password,
        confirmPassword: confirm,
        nickname: nick,
      })
      setToken(data.token)
      setUser({
        username: data.username,
        role: data.role,
        displayName: data.displayName || data.nickname,
        avatar: data.avatar || undefined,
      })
      return {
        ok: true as const,
        redirectTo: roleHome.reader,
        message: '注册成功，已进入书城',
      }
    } catch (error) {
      const message = error instanceof ApiError ? error.message : '注册失败，请检查后端是否启动'
      return { ok: false as const, message }
    }
  }

  function logout() {
    setToken(null)
    setUser(null)
    try {
      localStorage.removeItem(LAST_PATH_KEY)
    } catch {
      /* ignore */
    }
  }

  function patchProfile(
    patch: Partial<Pick<AuthUser, 'displayName' | 'avatar' | 'username' | 'bio'>>,
  ) {
    if (!user.value) return
    setUser({
      ...user.value,
      ...patch,
      avatar: patch.avatar === undefined ? user.value.avatar : patch.avatar || undefined,
      bio: patch.bio === undefined ? user.value.bio : patch.bio || undefined,
    })
  }

  return {
    user,
    isLoggedIn,
    role,
    login,
    register,
    logout,
    patchProfile,
    roleHome,
    roleProfile,
    roleLabel,
  }
})
