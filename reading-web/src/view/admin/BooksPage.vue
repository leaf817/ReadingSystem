<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ApiError } from '@/api/request'
import { deleteBook, fetchBooks, toggleBookShelf, updateBook, uploadBookCover } from '@/api/book'
import type { AdminBook } from './adminData'
import './admin-shared.css'

const router = useRouter()

const DEFAULT_CATEGORIES = ['现代言情', '古代言情', '奇幻言情', '仙侠言情']

const PAGE_SIZE = 10
const titleCollator = new Intl.Collator('zh-CN', { sensitivity: 'base' })

const keyword = ref('')
const list = ref<AdminBook[]>([])
const loading = ref(false)
const toast = ref('')
const loadError = ref('')
const page = ref(1)
const editingId = ref<number | null>(null)
const saving = ref(false)
const coverInput = ref<HTMLInputElement | null>(null)
const pendingCoverFile = ref<File | null>(null)
const coverPreviewUrl = ref('')
const editForm = reactive({
  title: '',
  author: '',
  category: '',
  tags: '',
  status: '连载' as '连载' | '完结',
  shelf: '上架' as '上架' | '下架',
  heat: '',
  chapters: 0,
  desc: '',
  cover: '',
})

const editingBook = computed(() => list.value.find((b) => b.id === editingId.value) ?? null)
const showEdit = computed(() => editingId.value !== null)
const categoryOptions = computed(() => {
  const set = new Set(DEFAULT_CATEGORIES)
  list.value.forEach((b) => {
    if (b.category) set.add(b.category)
  })
  if (editForm.category) set.add(editForm.category)
  return [...set]
})

const filtered = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  const items = q
    ? list.value.filter(
        (b) =>
          b.title.toLowerCase().includes(q) ||
          b.author.toLowerCase().includes(q) ||
          b.category.includes(q),
      )
    : list.value
  return [...items].sort((a, b) => titleCollator.compare(a.title, b.title))
})

const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / PAGE_SIZE)))

const paged = computed(() => {
  const start = (page.value - 1) * PAGE_SIZE
  return filtered.value.slice(start, start + PAGE_SIZE)
})

const pageInfo = computed(() => {
  if (!filtered.value.length) return '共 0 本'
  const start = (page.value - 1) * PAGE_SIZE + 1
  const end = Math.min(page.value * PAGE_SIZE, filtered.value.length)
  return `第 ${start}-${end} 本，共 ${filtered.value.length} 本`
})

const stats = computed(() => {
  const total = list.value.length
  const onShelf = list.value.filter((b) => b.shelf === '上架').length
  const finished = list.value.filter((b) => b.status === '完结').length
  return [
    { label: '全部书籍', value: String(total) },
    { label: '已上架', value: String(onShelf) },
    { label: '已完结', value: String(finished) },
    { label: '当前筛选', value: String(filtered.value.length) },
  ]
})

watch(keyword, () => {
  page.value = 1
})

watch(totalPages, (n) => {
  if (page.value > n) page.value = n
})

function goPage(next: number) {
  if (next < 1 || next > totalPages.value) return
  page.value = next
}

async function loadBooks() {
  loading.value = true
  loadError.value = ''
  try {
    const books = await fetchBooks()
    list.value = books.map((b) => ({
      ...b,
      shelf: b.shelf === '下架' ? '下架' : '上架',
    }))
    page.value = 1
  } catch (e) {
    loadError.value = e instanceof ApiError ? e.message : '加载书籍失败'
    list.value = []
  } finally {
    loading.value = false
  }
}

function showToast(message: string) {
  toast.value = message
  window.setTimeout(() => {
    toast.value = ''
  }, 1800)
}

function revokeCoverPreview() {
  if (coverPreviewUrl.value) {
    URL.revokeObjectURL(coverPreviewUrl.value)
    coverPreviewUrl.value = ''
  }
}

function openEdit(book: AdminBook) {
  revokeCoverPreview()
  pendingCoverFile.value = null
  editingId.value = book.id
  editForm.title = book.title
  editForm.author = book.author
  editForm.category = book.category
  editForm.tags = (book.tags ?? []).join('，')
  editForm.status = book.status
  editForm.shelf = book.shelf === '下架' ? '下架' : '上架'
  editForm.heat = book.heat
  editForm.chapters = book.chapters ?? 0
  editForm.desc = book.desc ?? ''
  editForm.cover = book.cover ?? ''
}

function closeEdit() {
  editingId.value = null
  saving.value = false
  pendingCoverFile.value = null
  revokeCoverPreview()
  if (coverInput.value) coverInput.value.value = ''
}

function triggerCoverPick() {
  coverInput.value?.click()
}

function onCoverFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    showToast('请选择图片文件')
    input.value = ''
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    showToast('封面大小不能超过 2MB')
    input.value = ''
    return
  }

  revokeCoverPreview()
  pendingCoverFile.value = file
  coverPreviewUrl.value = URL.createObjectURL(file)
  editForm.cover = coverPreviewUrl.value
}

function clearCover() {
  pendingCoverFile.value = null
  revokeCoverPreview()
  editForm.cover = ''
  if (coverInput.value) coverInput.value.value = ''
}

async function saveEdit() {
  const current = editingBook.value
  if (!current) return

  const title = editForm.title.trim()
  const author = editForm.author.trim()
  const category = editForm.category.trim()
  if (!title) {
    showToast('请输入书名')
    return
  }
  if (!author) {
    showToast('请输入作者')
    return
  }
  if (!category) {
    showToast('请选择分类')
    return
  }

  const chapters = Number(editForm.chapters)
  if (!Number.isFinite(chapters) || chapters < 0) {
    showToast('章节数无效')
    return
  }

  saving.value = true
  try {
    if (pendingCoverFile.value) {
      await uploadBookCover(current.id, pendingCoverFile.value)
    }

    const cover = pendingCoverFile.value
      ? undefined
      : editForm.cover.trim()
        ? undefined
        : ''

    const updated = await updateBook(current.id, {
      title,
      author,
      ...(cover !== undefined ? { cover } : {}),
      category,
      tags: editForm.tags
        .split(/[,，]/)
        .map((item) => item.trim())
        .filter(Boolean)
        .join(','),
      status: editForm.status,
      shelf: editForm.shelf,
      heat: editForm.heat.trim() || '0',
      desc: editForm.desc.trim(),
      chapters: Math.floor(chapters),
    })
    const index = list.value.findIndex((b) => b.id === current.id)
    if (index >= 0) {
      list.value[index] = {
        ...list.value[index],
        ...updated,
        shelf: updated.shelf === '下架' ? '下架' : '上架',
      }
    }
    closeEdit()
    showToast(`《${updated.title}》已保存`)
  } catch (e) {
    showToast(e instanceof ApiError ? e.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleShelf(book: AdminBook) {
  try {
    const updated = await toggleBookShelf(book.id)
    book.shelf = updated.shelf === '下架' ? '下架' : '上架'
    showToast(`《${book.title}》已${book.shelf}`)
  } catch (e) {
    showToast(e instanceof ApiError ? e.message : '操作失败')
  }
}

async function removeBook(book: AdminBook) {
  if (!window.confirm(`确认删除《${book.title}》？删除后不可恢复。`)) return
  try {
    await deleteBook(book.id)
    list.value = list.value.filter((b) => b.id !== book.id)
    showToast(`《${book.title}》已删除`)
  } catch (e) {
    showToast(e instanceof ApiError ? e.message : '删除失败')
  }
}

const coverPreview = ref<{ src: string; title: string } | null>(null)

function openCoverPreview(book: AdminBook) {
  if (!book.cover) return
  coverPreview.value = { src: book.cover, title: book.title }
}

function openBook(book: AdminBook) {
  void router.push({ name: 'book', params: { id: String(book.id) } })
}

function closeCoverPreview() {
  coverPreview.value = null
}

function onPreviewKey(event: KeyboardEvent) {
  if (event.key === 'Escape') closeCoverPreview()
}

onMounted(() => {
  void loadBooks()
  window.addEventListener('keydown', onPreviewKey)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onPreviewKey)
})
</script>

<template>
  <div class="books-page">
    <div class="summary">
      <article v-for="item in stats" :key="item.label" class="summary__card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </article>
    </div>

    <section class="admin-block panel">
      <div class="admin-block__head">
        <div class="panel__title">
          <h2>书籍列表</h2>
          <p>来自书库同步 · 悬停书名可查看全文</p>
        </div>
        <div class="admin-toolbar">
          <input
            v-model="keyword"
            class="admin-search"
            type="search"
            placeholder="搜索书名 / 作者 / 分类"
          />
          <button
            type="button"
            class="admin-btn admin-btn--ghost"
            :disabled="loading"
            @click="loadBooks"
          >
            刷新
          </button>
          <button type="button" class="admin-btn admin-btn--primary">新增书籍</button>
        </div>
      </div>

      <div class="admin-table-wrap table-shell">
        <table class="admin-table books-table">
          <thead>
            <tr>
              <th class="col-seq">序号</th>
              <th class="col-title">书名</th>
              <th class="col-author">作者</th>
              <th class="col-cat">分类</th>
              <th class="col-status">连载状态</th>
              <th class="col-shelf">上架状态</th>
              <th class="col-heat">热度</th>
              <th class="col-ops">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(book, index) in paged" :key="book.id">
              <td class="col-seq">{{ (page - 1) * PAGE_SIZE + index + 1 }}</td>
              <td class="col-title">
                <div class="book-cell">
                  <img
                    v-if="book.cover"
                    :src="book.cover"
                    :alt="book.title"
                    class="book-cover"
                    title="点击放大"
                    @click="openCoverPreview(book)"
                  />
                  <span v-else class="book-cover book-cover--fallback">{{ book.title.slice(0, 1) }}</span>
                  <span
                    class="book-name name-tip"
                    :data-tip="book.title"
                    title="点击查看目录"
                    role="link"
                    tabindex="0"
                    @click="openBook(book)"
                    @keydown.enter.prevent="openBook(book)"
                  >
                    <span class="book-name__text">{{ book.title }}</span>
                  </span>
                </div>
              </td>
              <td class="col-author">
                <span class="cell-text name-tip" :data-tip="book.author">{{ book.author }}</span>
              </td>
              <td class="col-cat" :title="book.category">
                <span class="cell-tag cat-chip">{{ book.category }}</span>
              </td>
              <td class="col-status">
                <span
                  class="cell-tag admin-tag"
                  :class="book.status === '完结' ? 'admin-tag--done' : 'admin-tag--serial'"
                >
                  {{ book.status }}
                </span>
              </td>
              <td class="col-shelf">
                <span
                  class="cell-tag admin-tag"
                  :class="book.shelf === '上架' ? 'admin-tag--on' : 'admin-tag--off'"
                >
                  {{ book.shelf }}
                </span>
              </td>
              <td class="col-heat">
                <span class="cell-text" :title="book.heat">{{ book.heat }}</span>
              </td>
              <td class="col-ops">
                <div class="ops-row">
                  <button type="button" class="op-btn" @click="openEdit(book)">编辑</button>
                  <button type="button" class="op-btn op-btn--accent" @click="toggleShelf(book)">
                    {{ book.shelf === '上架' ? '下架' : '上架' }}
                  </button>
                  <button type="button" class="op-btn op-btn--danger" @click="removeBook(book)">
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="!loading && !loadError && filtered.length" class="pager">
        <span class="pager__info">{{ pageInfo }}</span>
        <div class="pager__actions">
          <button type="button" class="pager__btn" :disabled="page <= 1" @click="goPage(page - 1)">
            上一页
          </button>
          <button
            v-for="n in totalPages"
            :key="n"
            type="button"
            class="pager__num"
            :class="{ 'is-active': n === page }"
            @click="goPage(n)"
          >
            {{ n }}
          </button>
          <button
            type="button"
            class="pager__btn"
            :disabled="page >= totalPages"
            @click="goPage(page + 1)"
          >
            下一页
          </button>
        </div>
      </div>

      <p v-if="loading" class="admin-empty">正在同步书籍...</p>
      <p v-else-if="loadError" class="admin-empty">{{ loadError }}</p>
      <p v-else-if="!filtered.length" class="admin-empty">没有匹配的书籍</p>
      <p v-if="toast" class="admin-toast">{{ toast }}</p>
    </section>

    <div v-if="showEdit" class="dialog-mask" @click.self="closeEdit">
      <div class="dialog" role="dialog" aria-modal="true" aria-labelledby="edit-book-title">
        <header class="dialog__head">
          <h3 id="edit-book-title">编辑书籍</h3>
          <button type="button" class="dialog__close" @click="closeEdit">×</button>
        </header>
        <form class="dialog__body" @submit.prevent="saveEdit">
          <div class="cover-preview">
            <div class="cover-edit">
              <button type="button" class="cover-edit__pick" title="点击更换封面" @click="triggerCoverPick">
                <img v-if="editForm.cover" :src="editForm.cover" :alt="editForm.title" />
                <span v-else>{{ editForm.title.slice(0, 1) || '书' }}</span>
                <em class="cover-edit__hint">更换</em>
              </button>
              <button
                v-if="editForm.cover"
                type="button"
                class="cover-edit__remove"
                title="恢复默认封面"
                @click.stop="clearCover"
              >
                ×
              </button>
            </div>
            <p>点击封面更换图片</p>
            <input
              ref="coverInput"
              class="sr-only"
              type="file"
              accept="image/png,image/jpeg,image/webp,image/gif"
              @change="onCoverFileChange"
            />
          </div>

          <label class="field">
            <span>书名</span>
            <input v-model="editForm.title" type="text" maxlength="128" placeholder="请输入书名" />
          </label>
          <label class="field">
            <span>作者</span>
            <input v-model="editForm.author" type="text" maxlength="64" placeholder="请输入作者" />
          </label>
          <div class="field-row">
            <label class="field">
              <span>分类</span>
              <select v-model="editForm.category">
                <option v-for="item in categoryOptions" :key="item" :value="item">{{ item }}</option>
              </select>
            </label>
            <label class="field">
              <span>连载状态</span>
              <select v-model="editForm.status">
                <option value="连载">连载</option>
                <option value="完结">完结</option>
              </select>
            </label>
          </div>
          <div class="field-row">
            <label class="field">
              <span>上架状态</span>
              <select v-model="editForm.shelf">
                <option value="上架">上架</option>
                <option value="下架">下架</option>
              </select>
            </label>
            <label class="field">
              <span>章节数</span>
              <input v-model.number="editForm.chapters" type="number" min="0" step="1" />
            </label>
          </div>
          <label class="field">
            <span>热度</span>
            <input v-model="editForm.heat" type="text" maxlength="32" placeholder="如 190.8万" />
          </label>
          <label class="field">
            <span>标签</span>
            <input v-model="editForm.tags" type="text" maxlength="255" placeholder="多个标签用逗号分隔" />
          </label>
          <label class="field">
            <span>简介</span>
            <textarea v-model="editForm.desc" rows="3" maxlength="512" placeholder="请输入简介" />
          </label>
          <div class="dialog__actions">
            <button type="button" class="btn-ghost" @click="closeEdit">取消</button>
            <button type="submit" class="btn-primary" :disabled="saving">
              {{ saving ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div
      v-if="coverPreview"
      class="img-preview"
      role="dialog"
      aria-modal="true"
      :aria-label="coverPreview.title"
      @click="closeCoverPreview"
    >
      <img :src="coverPreview.src" :alt="coverPreview.title" />
      <p>{{ coverPreview.title }}</p>
    </div>
  </div>
</template>

<style scoped>
.books-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  min-height: calc(100vh - 140px);
}

.summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  width: 100%;
}

.summary__card {
  padding: 16px 18px;
  border-radius: 14px;
  background:
    linear-gradient(135deg, rgba(255, 77, 58, 0.06), transparent 48%),
    #fff;
  border: 1px solid rgba(0, 0, 0, 0.045);
  box-shadow: var(--shadow-soft);
}

.summary__card span {
  display: block;
  font-size: 12px;
  color: var(--ink-muted);
}

.summary__card strong {
  display: block;
  margin-top: 8px;
  font-family: var(--font-serif);
  font-size: 26px;
  font-weight: 600;
  line-height: 1;
}

.panel :deep(.admin-block__head) {
  align-items: flex-start;
  margin-bottom: 18px;
}

.panel.admin-block {
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 20px 0 16px;
  box-sizing: border-box;
}

.panel :deep(.admin-block__head),
.panel .pager,
.panel .admin-empty,
.panel .admin-toast {
  padding-left: 20px;
  padding-right: 20px;
}

.panel__title h2 {
  margin: 0;
}

.panel__title p {
  margin-top: 6px;
  font-size: 12px;
  color: var(--ink-muted);
}

.table-shell {
  width: 100%;
  flex: 1;
  border: 0;
  border-radius: 0;
  border-top: 1px solid #ece8e1;
  border-bottom: 1px solid #ece8e1;
  background: #fcfbf9;
  overflow: auto;
  padding-bottom: 8px;
}

.books-table {
  width: 100%;
  min-width: 100%;
  table-layout: fixed;
  --seq-w: 6%;
  --title-w: 20%;
  --author-w: 11%;
  --cat-w: 12%;
  --status-w: 11%;
  --shelf-w: 11%;
  --heat-w: 10%;
  --ops-w: 19%;
}

.books-table thead th {
  background: #f4f1ec;
  font-size: 12px;
  letter-spacing: 0.02em;
}

.books-table tbody tr {
  position: relative;
  transition: background 0.2s ease, box-shadow 0.2s ease;
}

.books-table tbody tr:hover {
  z-index: 2;
  background: linear-gradient(90deg, rgba(255, 77, 58, 0.06) 0%, rgba(255, 247, 244, 0.95) 18%, #fff 100%);
  box-shadow: inset 3px 0 0 var(--brand);
}

.books-table tbody tr:hover td.col-title {
  overflow: visible;
}

.books-table tbody tr:hover .book-name {
  color: var(--brand);
}

.books-table tbody tr:hover .book-cover {
  transform: translateY(-1px) scale(1.04);
  box-shadow: 0 6px 14px rgba(255, 77, 58, 0.22);
}

.books-table :deep(th),
.books-table :deep(td) {
  padding: 12px 14px;
  text-align: center;
  vertical-align: middle;
}

.books-table :deep(th:first-child),
.books-table :deep(td:first-child) {
  padding-left: 20px;
}

.books-table :deep(th:last-child),
.books-table :deep(td:last-child) {
  padding-right: 20px;
}

.books-table :deep(.col-seq) {
  width: var(--seq-w);
  color: var(--ink-muted);
  font-variant-numeric: tabular-nums;
}

.books-table :deep(.col-title) {
  width: var(--title-w);
  text-align: left;
  overflow: visible;
}

.books-table :deep(.col-author) {
  width: var(--author-w);
  overflow: visible;
}

.books-table :deep(.col-cat) {
  width: var(--cat-w);
}

.books-table :deep(.col-status) {
  width: var(--status-w);
}

.books-table :deep(.col-shelf) {
  width: var(--shelf-w);
}

.books-table :deep(.col-heat) {
  width: var(--heat-w);
  color: #c45d2a;
  font-variant-numeric: tabular-nums;
  font-weight: 600;
}

.books-table :deep(th.col-ops),
.books-table :deep(td.col-ops) {
  width: var(--ops-w);
  overflow: visible;
  text-overflow: clip;
  white-space: nowrap;
}

.book-cell {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  min-width: 0;
  width: 100%;
}

.book-name {
  position: relative;
  min-width: 0;
  flex: 0 1 auto;
  max-width: 100%;
  overflow: visible;
  font-weight: 600;
  color: var(--ink);
  text-align: left;
  transition: color 0.2s ease;
  cursor: pointer;
}

.book-name__text {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.name-tip {
  position: relative;
}

.name-tip:hover::after {
  content: attr(data-tip);
  position: absolute;
  left: 0;
  bottom: calc(100% + 8px);
  z-index: 40;
  padding: 6px 12px;
  border-radius: 8px;
  background: #1a2435;
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.4;
  white-space: nowrap;
  box-shadow: 0 10px 28px rgba(20, 28, 40, 0.28);
  pointer-events: none;
  animation: tip-rise 0.18s ease-out;
}

.name-tip:hover::before {
  content: '';
  position: absolute;
  left: 12px;
  bottom: calc(100% + 2px);
  z-index: 41;
  border: 6px solid transparent;
  border-top-color: #1a2435;
  pointer-events: none;
  animation: tip-rise 0.18s ease-out;
}

.col-author .name-tip {
  display: inline-block;
}

.col-author .name-tip:hover::after {
  left: 50%;
  transform: translateX(-50%);
}

.col-author .name-tip:hover::before {
  left: 50%;
  margin-left: -6px;
}

@keyframes tip-rise {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.cell-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1.2;
  vertical-align: middle;
  cursor: pointer;
}

.ops-row {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
}

.book-cover {
  width: 28px;
  height: 38px;
  border-radius: 5px;
  object-fit: cover;
  flex-shrink: 0;
  box-shadow: 0 3px 8px rgba(26, 26, 26, 0.12);
  background: #ece7e0;
  cursor: zoom-in;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
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
  max-width: min(88vw, 420px);
  max-height: 78vh;
  border-radius: 10px;
  object-fit: contain;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.45);
  background: #ece7e0;
}

.img-preview p {
  margin: 0;
  max-width: min(88vw, 420px);
  color: #fff;
  font-size: 14px;
  text-align: center;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.4);
}

.book-cover--fallback {
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
}

.cat-chip {
  max-width: 100%;
  padding: 3px 8px;
  border-radius: 999px;
  background: #f3efe8;
  color: #6b6570;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.op-btn {
  border: 0;
  background: transparent;
  color: var(--ink-secondary);
  cursor: pointer;
  font-size: 13px;
  padding: 0 6px;
  white-space: nowrap;
  line-height: 1.4;
}

.op-btn:hover {
  color: var(--brand);
}

.op-btn--accent {
  color: var(--brand);
  font-weight: 600;
}

.op-btn--danger {
  color: #b42318;
}

.op-btn--danger:hover {
  color: #912018;
}

.pager {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.pager__info {
  font-size: 12px;
  color: var(--ink-muted);
}

.pager__actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.pager__btn,
.pager__num {
  height: 32px;
  min-width: 32px;
  padding: 0 10px;
  border-radius: 8px;
  border: 1px solid #e5e2dc;
  background: #fff;
  color: var(--ink-secondary);
  font-size: 13px;
  cursor: pointer;
}

.pager__btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.pager__num.is-active,
.pager__btn:not(:disabled):hover,
.pager__num:hover {
  border-color: rgba(255, 77, 58, 0.35);
  color: var(--brand);
}

.pager__num.is-active {
  background: #fff5f3;
  font-weight: 700;
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
  width: min(520px, 100%);
  max-height: min(88vh, 760px);
  overflow: auto;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.18);
}

.dialog__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid var(--line);
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 1;
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

.cover-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.cover-edit {
  position: relative;
  width: 72px;
  height: 96px;
}

.cover-edit__pick {
  display: block;
  width: 72px;
  height: 96px;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  border-radius: 10px;
}

.cover-edit__pick img,
.cover-edit__pick span {
  width: 72px;
  height: 96px;
  border-radius: 10px;
  object-fit: cover;
  box-shadow: 0 6px 16px rgba(20, 28, 40, 0.16);
}

.cover-edit__pick span {
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 700;
  background: linear-gradient(145deg, #ff7a59, #ff4d3a);
}

.cover-edit__pick:hover .cover-edit__hint,
.cover-edit__pick:focus-visible .cover-edit__hint {
  opacity: 1;
}

.cover-edit__hint {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: rgba(20, 28, 40, 0.48);
  color: #fff;
  font-size: 12px;
  font-style: normal;
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.2s;
  pointer-events: none;
}

.cover-edit__remove {
  position: absolute;
  right: -6px;
  top: -6px;
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

.cover-preview p {
  margin: 0;
  font-size: 12px;
  color: var(--ink-muted);
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

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
  color: var(--ink-secondary);
}

.field input,
.field select,
.field textarea {
  padding: 0 12px;
  border-radius: 10px;
  border: 1px solid #e5e2dc;
  background: #faf9f7;
  outline: 0;
  font-size: 14px;
  color: var(--ink);
}

.field input,
.field select {
  height: 40px;
}

.field textarea {
  padding: 10px 12px;
  resize: vertical;
  min-height: 84px;
  line-height: 1.5;
  font-family: inherit;
}

.field input:focus,
.field select:focus,
.field textarea:focus {
  border-color: rgba(255, 77, 58, 0.45);
  background: #fff;
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

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 980px) {
  .summary {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .summary {
    grid-template-columns: 1fr;
  }
}
</style>
