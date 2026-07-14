import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export type UserRole = 'admin' | 'author' | 'reader'

export interface AuthUser {
  username: string
  role: UserRole
  displayName: string
}

interface Account {
  username: string
  password: string
  role: UserRole
  displayName: string
}

const STORAGE_KEY = 'yuejie-accounts'

const roleHome: Record<UserRole, string> = {
  admin: '/admin',
  author: '/author',
  reader: '/',
}

const roleLabel: Record<UserRole, string> = {
  admin: '管理端',
  author: '作者端',
  reader: '读者端',
}

const seedAccounts: Account[] = [
  { username: 'admin', password: '123456', role: 'admin', displayName: '管理员' },
  { username: 'author', password: '123456', role: 'author', displayName: '签约作者' },
  { username: 'reader', password: '123456', role: 'reader', displayName: '普通读者' },
]

function loadAccounts(): Account[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return [...seedAccounts]
    const parsed = JSON.parse(raw) as Account[]
    const names = new Set(parsed.map((a) => a.username))
    const merged = [...parsed]
    for (const seed of seedAccounts) {
      if (!names.has(seed.username)) merged.push(seed)
    }
    return merged
  } catch {
    return [...seedAccounts]
  }
}

function saveAccounts(list: Account[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<AuthUser | null>(null)
  const accounts = ref<Account[]>(loadAccounts())

  const isLoggedIn = computed(() => !!user.value)
  const role = computed(() => user.value?.role ?? null)

  function persist() {
    saveAccounts(accounts.value)
  }

  function login(username: string, password: string) {
    const name = username.trim()
    if (!name) return { ok: false as const, message: '请输入账号' }
    if (!password) return { ok: false as const, message: '请输入密码' }

    const found = accounts.value.find((a) => a.username === name)
    if (!found) {
      return { ok: false as const, message: '账号不存在，请先注册' }
    }
    if (found.password !== password) {
      return { ok: false as const, message: '密码错误' }
    }

    user.value = {
      username: found.username,
      role: found.role,
      displayName: found.displayName,
    }

    return {
      ok: true as const,
      redirectTo: roleHome[found.role],
      message: `已进入${roleLabel[found.role]}`,
    }
  }

  function register(username: string, password: string, confirm: string, nickname: string) {
    const name = username.trim()
    const nick = nickname.trim()
    if (!nick) return { ok: false as const, message: '请输入昵称' }
    if (nick.length < 2) return { ok: false as const, message: '昵称至少 2 个字符' }
    if (!name) return { ok: false as const, message: '请输入账号' }
    if (name.length < 3) return { ok: false as const, message: '账号至少 3 个字符' }
    if (!password) return { ok: false as const, message: '请输入密码' }
    if (password.length < 6) return { ok: false as const, message: '密码至少 6 位' }
    if (password !== confirm) return { ok: false as const, message: '两次密码不一致' }
    if (accounts.value.some((a) => a.username === name)) {
      return { ok: false as const, message: '账号已存在，请直接登录' }
    }

    const account: Account = {
      username: name,
      password,
      role: 'reader',
      displayName: nick,
    }
    accounts.value.push(account)
    persist()

    user.value = {
      username: account.username,
      role: account.role,
      displayName: account.displayName,
    }

    return {
      ok: true as const,
      redirectTo: roleHome.reader,
      message: '注册成功，已进入书城',
    }
  }

  function logout() {
    user.value = null
  }

  return {
    user,
    isLoggedIn,
    role,
    login,
    register,
    logout,
    roleHome,
    roleLabel,
  }
})
