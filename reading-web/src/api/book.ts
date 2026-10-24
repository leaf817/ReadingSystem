import { request } from './request'
import type { BannerItem, Book } from '@/data/mockBooks'
import type { SearchBy } from '@/utils/bookSearch'

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

export function fetchBooks(keyword?: string, by?: SearchBy) {
  const params = new URLSearchParams()
  if (keyword) params.set('keyword', keyword)
  if (by) params.set('by', by)
  const query = params.toString()
  return request<Book[]>(`/api/books${query ? `?${query}` : ''}`)
}

export function fetchBook(id: number) {
  return request<Book>(`/api/books/${id}`)
}

export interface ChapterSummary {
  seq: number
  title: string
  wordCount: number
}

export interface ChapterContent {
  bookId: number
  seq: number
  title: string
  content: string
  wordCount: number
  prevSeq: number | null
  nextSeq: number | null
  total: number
}

export function fetchBookChapters(id: number) {
  return request<ChapterSummary[]>(`/api/books/${id}/chapters`)
}

export function fetchBookChapter(id: number, seq: number) {
  return request<ChapterContent>(`/api/books/${id}/chapters/${seq}`)
}

export interface UpdateBookPayload {
  title: string
  author: string
  cover?: string
  category: string
  tags: string
  status: '连载' | '完结'
  shelf: '上架' | '下架'
  heat: string
  desc: string
  chapters: number
}

export function uploadBookCover(id: number, file: File) {
  const body = new FormData()
  body.append('file', file)
  return request<Book>(`/api/admin/books/${id}/cover`, {
    method: 'POST',
    body,
  })
}

export function updateBook(id: number, payload: UpdateBookPayload) {
  return request<Book>(`/api/admin/books/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function toggleBookShelf(id: number) {
  return request<Book>(`/api/admin/books/${id}/shelf`, { method: 'PUT' })
}

export function deleteBook(id: number) {
  return request<null>(`/api/admin/books/${id}`, { method: 'DELETE' })
}

export function fetchHotRank() {
  return request<Book[]>('/api/ranks/hot')
}
