import { request } from './request'
import type { UserRole } from '@/stores/auth'

export interface AuthResponse {
  username: string
  nickname: string
  displayName: string
  role: UserRole
  token: string
  avatar?: string | null
}

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload {
  username: string
  password: string
  confirmPassword: string
  nickname: string
}

export interface ProfileView {
  id: number
  username: string
  nickname: string
  role: UserRole
  status: string
  joinedAt: string
  avatar?: string | null
  bio?: string | null
}

export interface UpdateProfilePayload {
  nickname: string
  avatar?: string | null
  bio?: string | null
}

export interface ChangePasswordPayload {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export function loginApi(payload: LoginPayload) {
  return request<AuthResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function registerApi(payload: RegisterPayload) {
  return request<AuthResponse>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function fetchMeApi() {
  return request<ProfileView>('/api/auth/me')
}

export function updateMeApi(payload: UpdateProfilePayload) {
  return request<ProfileView>('/api/auth/me', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function changePasswordApi(payload: ChangePasswordPayload) {
  return request<null>('/api/auth/password', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}
