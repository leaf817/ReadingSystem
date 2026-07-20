import type { Book } from '@/data/mockBooks'
import { books as seedBooks, hotRank as seedHotRank } from '@/data/mockBooks'

export type AdminBook = Book & {
  shelf: '上架' | '下架'
}

export interface AdminUser {
  id: number
  name: string
  account: string
  role: '管理端' | '作者端' | '读者端'
  status: '正常' | '禁用' | '待审核'
  joinedAt: string
  /** 自定义头像（data URL），为空则使用昵称首字 */
  avatar?: string
}

export interface ReviewItem {
  id: number
  title: string
  author: string
  type: '章节' | '封面' | '资料'
  time: string
  status: '待审核' | '已通过' | '已驳回'
}

export const adminBooks: AdminBook[] = seedBooks.map((book) => ({
  ...book,
  shelf: '上架',
}))

export const adminUsers: AdminUser[] = [
  { id: 1, name: '管理员', account: 'admin', role: '管理端', status: '正常', joinedAt: '2025-01-08' },
  { id: 2, name: '签约作者', account: 'author', role: '作者端', status: '正常', joinedAt: '2025-03-12' },
  { id: 3, name: '普通读者', account: 'reader', role: '读者端', status: '正常', joinedAt: '2025-04-02' },
  { id: 4, name: '南柯一梦', account: 'nanke', role: '作者端', status: '待审核', joinedAt: '2026-06-18' },
  { id: 5, name: '林深见鹿', account: 'linshen', role: '读者端', status: '正常', joinedAt: '2026-06-22' },
  { id: 6, name: '青衫客', account: 'qingshan', role: '作者端', status: '禁用', joinedAt: '2026-05-09' },
]

export const reviewQueue: ReviewItem[] = [
  { id: 1, title: '星河尽头 · 第487章', author: '南柯一梦', type: '章节', time: '10 分钟前', status: '待审核' },
  { id: 2, title: '京城夜雨 · 封面更新', author: '墨染山河', type: '封面', time: '32 分钟前', status: '待审核' },
  { id: 3, title: '白日梦工厂 · 简介修改', author: '慢慢来', type: '资料', time: '1 小时前', status: '待审核' },
  { id: 4, title: '都市神医 · 第1103章', author: '一碗汤', type: '章节', time: '2 小时前', status: '待审核' },
  { id: 5, title: '她从光里来 · 封面更新', author: '七月微凉', type: '封面', time: '昨天', status: '待审核' },
]

export const adminHotRank = [...seedHotRank]

export const adminStats = [
  { label: '在架书籍', value: String(seedBooks.length), tip: '含连载与完结' },
  { label: '待审核', value: String(reviewQueue.filter((r) => r.status === '待审核').length), tip: '章节 / 封面 / 资料' },
  { label: '今日活跃', value: '1.2万', tip: '读者端访问' },
  { label: '作者入驻', value: '38', tip: '本月新增 4' },
]
