import { request } from './request'

export type ApiUserRole = 'admin' | 'author' | 'reader'
export type ApiUserStatus = '正常' | '禁用' | '待审核'

export interface ApiUser {
  id: number
  username: string
  nickname: string
  role: ApiUserRole
  status: ApiUserStatus
  joinedAt: string
  avatar?: string | null
}

export interface UpdateUserPayload {
  nickname: string
  username: string
  status: ApiUserStatus
  avatar?: string | null
}

export interface CreateUserPayload {
  nickname: string
  username: string
  password: string
  role: ApiUserRole
  status?: ApiUserStatus
  avatar?: string | null
}

export function fetchUsersApi() {
  return request<ApiUser[]>('/api/admin/users')
}

export function createUserApi(payload: CreateUserPayload) {
  return request<ApiUser>('/api/admin/users', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateUserApi(id: number, payload: UpdateUserPayload) {
  return request<ApiUser>(`/api/admin/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}
