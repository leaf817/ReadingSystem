<script setup lang="ts">
export type SideIconName =
  | 'mine'
  | 'bookshelf'
  | 'history'
  | 'likes'
  | 'comments'
  | 'prefs'
  | 'author'
  | 'overview'
  | 'books'
  | 'users'
  | 'ranks'
  | 'reviews'
  | 'works'
  | 'chapters'
  | 'stats'
  | 'profile'

const icons: Record<SideIconName, string> = {
  mine: 'M12 12a3.5 3.5 0 1 0-3.5-3.5A3.5 3.5 0 0 0 12 12zm0 2c-3.3 0-7 1.7-7 5v1h14v-1c0-3.3-3.7-5-7-5z',
  bookshelf: 'M5 4h3.5v16H5zm5.25 0H13.75v16h-3.5zm5.25 0H19v16h-3.5z',
  history:
    'M12 4a8 8 0 1 0 8 8 8 8 0 0 0-8-8zm1 4h-2v5l4.2 2.5.9-1.5L13 12.2z',
  likes: 'M12 20s-7-4.4-7-9.2A4.2 4.2 0 0 1 12 7.2a4.2 4.2 0 0 1 7 3.6C19 15.6 12 20 12 20z',
  comments:
    'M5 5h14a1.5 1.5 0 0 1 1.5 1.5v8A1.5 1.5 0 0 1 19 16h-5l-4 3.5V16H5A1.5 1.5 0 0 1 3.5 14.5v-8A1.5 1.5 0 0 1 5 5z',
  prefs:
    'M10.2 4.5h3.6l.4 2.1a6 6 0 0 1 1.5.8l2-1.1 2.5 2.5-1.1 2a6 6 0 0 1 .8 1.5l2.1.4v3.6l-2.1.4a6 6 0 0 1-.8 1.5l1.1 2-2.5 2.5-2-1.1a6 6 0 0 1-1.5.8l-.4 2.1h-3.6l-.4-2.1a6 6 0 0 1-1.5-.8l-2 1.1-2.5-2.5 1.1-2a6 6 0 0 1-.8-1.5l-2.1-.4v-3.6l2.1-.4a6 6 0 0 1 .8-1.5l-1.1-2 2.5-2.5 2 1.1a6 6 0 0 1 1.5-.8zm1.8 5a2.5 2.5 0 1 0 2.5 2.5A2.5 2.5 0 0 0 12 9.5z',
  author:
    'M5 19h14v1.5H5zm2-2.2 8.5-8.5 2.2 2.2L9.2 19H7zm10.2-10.2 1.4-1.4a1.1 1.1 0 0 1 1.6 0l1.1 1.1a1.1 1.1 0 0 1 0 1.6L18.9 10z',
  overview: 'M4 4h7v7H4zm9 0h7v5h-7zm0 7h7v9h-7zM4 13h7v7H4z',
  books: 'M6 4h10a2 2 0 0 1 2 2v14H8a2 2 0 0 1-2-2zm0 0v14',
  users:
    'M9 11a3.2 3.2 0 1 0-3.2-3.2A3.2 3.2 0 0 0 9 11zm7 0a2.8 2.8 0 1 0-2.8-2.8A2.8 2.8 0 0 0 16 11zM3.2 19.5c0-2.7 2.6-4.3 5.8-4.3s5.8 1.6 5.8 4.3v.7H3.2zm11.4-3.8c1.8.3 3.7 1.5 3.7 3.6v.9h-3',
  ranks: 'M5.5 15h3.5v5H5.5zm5-5h3.5v10H10.5zm5-4H19v14h-3.5zM4.5 5h15v2h-15z',
  reviews:
    'M5 5.5h14v2H5zm0 4.5h14v2H5zm0 4.5h8v2H5zm9.5-.2 1.2 2.4 2.7.4-2 1.9.5 2.6-2.4-1.3-2.4 1.3.5-2.6-2-1.9 2.7-.4z',
  works: 'M5 4.5h14v15H5zm3.2 3.5h7.6v1.6H8.2zm0 3.5h7.6v1.6H8.2zm0 3.5h5.2v1.6H8.2z',
  chapters: 'M6 4h10a2 2 0 0 1 2 2v14H8a2 2 0 0 1-2-2zm2.8 4.2h7v1.5h-7zm0 3.3h7v1.5h-7zm0 3.3h5v1.5h-5z',
  stats: 'M4.5 18.5h15v1.5h-15zm2-3h2.8V9H6.5zm4.6 0h2.8V6h-2.8zm4.6 0H18.5v-5h-2.8z',
  profile:
    'M12 12a3.5 3.5 0 1 0-3.5-3.5A3.5 3.5 0 0 0 12 12zm0 2c-3.3 0-7 1.7-7 5v1h14v-1c0-3.3-3.7-5-7-5z',
}

withDefaults(
  defineProps<{
    name: SideIconName
    size?: number
  }>(),
  { size: 18 },
)
</script>

<template>
  <svg
    class="side-icon"
    viewBox="0 0 24 24"
    :width="size"
    :height="size"
    aria-hidden="true"
  >
    <path :d="icons[name]" fill="currentColor" />
  </svg>
</template>

<style scoped>
.side-icon {
  display: block;
  flex-shrink: 0;
}
</style>
