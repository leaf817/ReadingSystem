export interface Book {
  id: number
  title: string
  author: string
  cover: string
  category: string
  tags: string[]
  status: '连载' | '完结'
  shelf?: '上架' | '下架'
  heat: string
  desc: string
  chapters?: number
}

export interface BannerItem {
  id: number
  title: string
  subtitle: string
  tone: string
  bookId: number
}

export const categories = ['推荐', '男生', '女生', '玄幻', '都市', '言情', '历史', '科幻', '完结']

export const banners: BannerItem[] = [
  {
    id: 1,
    title: '星河尽头',
    subtitle: '穿越星际迷雾，寻一份不被遗忘的羁绊',
    tone: 'linear-gradient(135deg, #1c2b4a 0%, #3d5a80 55%, #ff6b5a 100%)',
    bookId: 1,
  },
  {
    id: 2,
    title: '京城夜雨',
    subtitle: '权谋与人心交织的古风长卷',
    tone: 'linear-gradient(135deg, #2b1d16 0%, #6b3e2e 50%, #c47a4a 100%)',
    bookId: 4,
  },
  {
    id: 3,
    title: '白日梦工厂',
    subtitle: '轻松都市，把平凡日子写成光',
    tone: 'linear-gradient(135deg, #1a3a2a 0%, #2d6a4f 50%, #ff8a65 100%)',
    bookId: 6,
  },
]

const cover = (seed: string, from: string, to: string) =>
  `data:image/svg+xml,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="240" height="320" viewBox="0 0 240 320">
  <defs>
    <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="${from}"/>
      <stop offset="100%" stop-color="${to}"/>
    </linearGradient>
  </defs>
  <rect width="240" height="320" fill="url(#g)"/>
  <circle cx="180" cy="60" r="48" fill="rgba(255,255,255,0.12)"/>
  <circle cx="40" cy="260" r="70" fill="rgba(0,0,0,0.12)"/>
  <text x="24" y="280" fill="rgba(255,255,255,0.92)" font-size="22" font-family="serif">${seed}</text>
</svg>`)}`

export const books: Book[] = [
  {
    id: 1,
    title: '星河尽头',
    author: '南柯一梦',
    cover: cover('星河', '#1c2b4a', '#ff6b5a'),
    category: '科幻',
    tags: ['星际', '成长'],
    status: '连载',
    heat: '128.6万',
    desc: '少年执一把锈蚀星图，踏入无人敢越的星海禁区。',
    chapters: 486,
  },
  {
    id: 2,
    title: '剑起苍穹',
    author: '青衫客',
    cover: cover('剑起', '#1f3a5f', '#4a90d9'),
    category: '玄幻',
    tags: ['热血', '修仙'],
    status: '连载',
    heat: '256.2万',
    desc: '一柄断剑，一段被掩埋的上古秘辛，少年从此逆天改命。',
    chapters: 920,
  },
  {
    id: 3,
    title: '余生请多指教',
    author: '林深见鹿',
    cover: cover('余生', '#5c2a4a', '#e87a9a'),
    category: '言情',
    tags: ['现代', '甜宠'],
    status: '完结',
    heat: '98.4万',
    desc: '一场阴差阳错的重逢，把错过的十年补成温柔长卷。',
    chapters: 312,
  },
  {
    id: 4,
    title: '京城夜雨',
    author: '墨染山河',
    cover: cover('京城', '#2b1d16', '#c47a4a'),
    category: '历史',
    tags: ['权谋', '古风'],
    status: '连载',
    heat: '176.8万',
    desc: '雨夜入京，刀光与朝堂同落，谁主沉浮。',
    chapters: 654,
  },
  {
    id: 5,
    title: '都市神医',
    author: '一碗汤',
    cover: cover('神医', '#1a3a3a', '#2bbbad'),
    category: '都市',
    tags: ['医术', '爽文'],
    status: '连载',
    heat: '312.1万',
    desc: '隐世医术重现人间，一手回春，一手翻云。',
    chapters: 1102,
  },
  {
    id: 6,
    title: '白日梦工厂',
    author: '慢慢来',
    cover: cover('白日', '#2d6a4f', '#ff8a65'),
    category: '都市',
    tags: ['轻松', '职场'],
    status: '连载',
    heat: '64.3万',
    desc: '普通打工人意外接手一座会实现愿望的工厂。',
    chapters: 188,
  },
  {
    id: 7,
    title: '月光旅店',
    author: '晚风起',
    cover: cover('月光', '#2a2a4a', '#8b7bff'),
    category: '言情',
    tags: ['治愈', '奇幻'],
    status: '完结',
    heat: '87.9万',
    desc: '每晚只住一位客人的旅店，收留所有说不出口的心事。',
    chapters: 246,
  },
  {
    id: 8,
    title: '诸天兑换商',
    author: '路过青山',
    cover: cover('诸天', '#3a1a1a', '#ff4d3a'),
    category: '玄幻',
    tags: ['穿越', '系统'],
    status: '连载',
    heat: '401.5万',
    desc: '一枚残破玉简，打通诸天万界的交易通道。',
    chapters: 1308,
  },
  {
    id: 9,
    title: '半城烟沙',
    author: '疏影横斜',
    cover: cover('烟沙', '#4a3a2a', '#d4a574'),
    category: '历史',
    tags: ['边塞', '家国'],
    status: '完结',
    heat: '112.0万',
    desc: '黄沙漫卷旌旗，少年将军一战成名。',
    chapters: 420,
  },
  {
    id: 10,
    title: '她从光里来',
    author: '七月微凉',
    cover: cover('光里', '#4a2a3a', '#ff6b9d'),
    category: '言情',
    tags: ['重生', '甜虐'],
    status: '连载',
    heat: '145.7万',
    desc: '重生归来，她只想把从前错过的光，一点点找回来。',
    chapters: 368,
  },
  {
    id: 11,
    title: '深空回响',
    author: '星尘',
    cover: cover('深空', '#0f1c2e', '#4ecdc4'),
    category: '科幻',
    tags: ['冒险', '硬核'],
    status: '连载',
    heat: '73.2万',
    desc: '失联舰队留下的最后一条信号，改写人类的边界。',
    chapters: 210,
  },
  {
    id: 12,
    title: '长安故人',
    author: '白衣卿相',
    cover: cover('长安', '#3a2a1a', '#e8b86d'),
    category: '历史',
    tags: ['朝堂', '友情'],
    status: '完结',
    heat: '199.4万',
    desc: '少年同窗入仕，半生荣辱，终不忘长安一诺。',
    chapters: 580,
  },
]

export const hotRank = books
  .slice()
  .sort((a, b) => parseFloat(b.heat) - parseFloat(a.heat))
  .slice(0, 10)

export const finishedBooks = books.filter((b) => b.status === '完结')
export const recommendBooks = books.slice(0, 8)
export const newBooks = [...books].reverse().slice(0, 6)

export const maleBooks = books.filter((b) =>
  ['玄幻', '都市', '历史', '科幻'].includes(b.category),
)
export const femaleBooks = books.filter((b) => ['言情'].includes(b.category) || b.tags.includes('甜宠') || b.tags.includes('治愈'))
export const guessBooks = books.slice(2, 8)
