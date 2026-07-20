import { request } from './request'
import type { BannerItem, Book } from '@/data/mockBooks'

export interface HomeFeed {
  banners: BannerItem[]
  recommendBooks: Book[]
  newBooks: Book[]
  finishedBooks: Book[]
  hotRank: Book[]
  maleBooks: Book[]
  femaleBooks: Book[]
  guessBooks: Book[]
}

export function fetchHomeFeed() {
  return request<HomeFeed>('/api/home')
}

export function fetchBooks(keyword?: string) {
  const query = keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''
  return request<Book[]>(`/api/books${query}`)
}

export function fetchHotRank() {
  return request<Book[]>('/api/ranks/hot')
}
