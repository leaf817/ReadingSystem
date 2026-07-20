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
