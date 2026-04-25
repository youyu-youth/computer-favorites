/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 收藏夹页面状态管理 composable
 */

import { ref, computed } from 'vue'
import type {
  CollectionWebsite,
  CollectionCategory,
  CollectionQuickAccess,
  ViewMode,
} from '@/types/collection'

/** 快捷访问配置 */
const QUICK_ACCESS_LIST: CollectionQuickAccess[] = [
  { key: 'recent', label: '最近访问', icon: 'fas fa-clock' },
  { key: 'favorites', label: '星标收藏', icon: 'fas fa-star' },
  { key: 'readlater', label: '稍后阅读', icon: 'fas fa-bookmark' },
]

/** 分类配置 */
const CATEGORY_LIST: CollectionCategory[] = [
  { id: 1, name: '推荐', icon: 'fas fa-thumbs-up', count: 12 },
  { id: 2, name: '前端', icon: 'fas fa-code', count: 18 },
  { id: 3, name: '后端', icon: 'fas fa-terminal', count: 9 },
  { id: 4, name: 'AI 工具', icon: 'fas fa-robot', count: 15 },
  { id: 5, name: '开发工具', icon: 'fas fa-wrench', count: 22 },
  { id: 6, name: '设计', icon: 'fas fa-paintbrush', count: 7 },
]

/** Mock 网站数据 */
const MOCK_WEBSITES: CollectionWebsite[] = [
  {
    id: 1,
    name: 'Notion',
    description: 'All-in-one workspace for notes, tasks, wikis, and databases.',
    url: 'https://notion.so',
    icon: 'https://www.notion.so/images/meta/default.png',
    tags: ['效率', '笔记'],
    category: '推荐',
    isStarred: true,
    likeCount: 12400,
    dateAdded: '2024-05-20 14:30',
    lastVisited: '今天 10:24',
  },
  {
    id: 2,
    name: 'Vercel',
    description: 'Frontend development and deployment platform.',
    url: 'https://vercel.com',
    icon: '',
    tags: ['开发工具', '部署'],
    category: '开发工具',
    isStarred: true,
    likeCount: 9800,
    dateAdded: '2024-06-10 09:15',
    lastVisited: '昨天 16:42',
  },
  {
    id: 3,
    name: 'Supabase',
    description: 'The open source Firebase alternative.',
    url: 'https://supabase.com',
    icon: '',
    tags: ['后端', '数据库'],
    category: '后端',
    isStarred: false,
    likeCount: 8600,
    dateAdded: '2024-07-03 11:20',
    lastVisited: '3天前',
  },
  {
    id: 4,
    name: 'Tailwind CSS',
    description: 'Utility-first CSS framework.',
    url: 'https://tailwindcss.com',
    icon: '',
    tags: ['前端', 'CSS'],
    category: '前端',
    isStarred: false,
    likeCount: 16700,
    dateAdded: '2024-04-15 08:00',
    lastVisited: '今天 09:10',
  },
  {
    id: 5,
    name: 'GitHub',
    description: '全球最大的开源代码托管平台，开发者发现、分享和构建优秀软件的首选。',
    url: 'https://github.com',
    icon: '',
    tags: ['开发工具', '开源'],
    category: '开发工具',
    isStarred: true,
    likeCount: 45000,
    dateAdded: '2024-01-05 10:00',
    lastVisited: '今天 08:30',
  },
  {
    id: 6,
    name: 'ChatGPT',
    description: 'OpenAI 训练的大型语言模型，能够理解和生成自然语言文本。',
    url: 'https://chat.openai.com',
    icon: '',
    tags: ['AI 工具', '对话'],
    category: 'AI 工具',
    isStarred: true,
    likeCount: 120000,
    dateAdded: '2024-02-20 14:00',
    lastVisited: '今天 11:05',
  },
  {
    id: 7,
    name: 'Figma',
    description: '基于浏览器的协作式 UI 设计工具，实时协作提升设计效率。',
    url: 'https://figma.com',
    icon: '',
    tags: ['设计', 'UI'],
    category: '设计',
    isStarred: false,
    likeCount: 35000,
    dateAdded: '2024-03-08 16:30',
    lastVisited: '上周',
  },
  {
    id: 8,
    name: 'Vue.js',
    description: '渐进式 JavaScript 框架，易学易用，性能出色。',
    url: 'https://vuejs.org',
    icon: '',
    tags: ['前端', '框架'],
    category: '前端',
    isStarred: false,
    likeCount: 52000,
    dateAdded: '2024-01-10 09:00',
    lastVisited: '2天前',
  },
]

export function useCollectionManagement() {
  /** 分类列表 */
  const categories = ref<CollectionCategory[]>(CATEGORY_LIST)

  /** 快捷访问列表 */
  const quickAccessList = ref<CollectionQuickAccess[]>(QUICK_ACCESS_LIST)

  /** 当前激活的快捷访问 key */
  const activeQuickAccess = ref<string | null>(null)

  /** 当前激活的分类 ID */
  const activeCategoryId = ref<number | null>(1)

  /** 网站列表 */
  const resources = ref<CollectionWebsite[]>(MOCK_WEBSITES)

  /** 搜索关键词 */
  const searchQuery = ref('')

  /** 当前选中的网站 */
  const selectedWebsite = ref<CollectionWebsite | null>(null)

  /** 详情面板是否打开 */
  const detailPanelOpen = ref(true)

  /** 视图模式 */
  const viewMode = ref<ViewMode>('grid')

  /** 缩放级别 50-150 */
  const zoomLevel = ref(100)

  /** 移动端侧边栏是否打开 */
  const mobileSidebarOpen = ref(false)

  /** 移动端详情面板是否打开 */
  const mobileDetailOpen = ref(false)

  /** 加载状态 */
  const isLoading = ref(false)

  /** 存储用量（GB） */
  const storageUsed = ref(2.45)
  const storageTotal = ref(10)

  /** 根据筛选条件过滤后的网站列表 */
  const filteredResources = computed(() => {
    let result = resources.value

    if (activeQuickAccess.value === 'favorites') {
      result = result.filter((w) => w.isStarred)
    } else if (activeQuickAccess.value === 'recent') {
      result = [...result].sort((a, b) => b.likeCount - a.likeCount)
    }

    if (activeCategoryId.value !== null) {
      const activeCategory = categories.value.find((c) => c.id === activeCategoryId.value)
      if (activeCategory) {
        result = result.filter((w) => w.category === activeCategory.name)
      }
    }

    if (searchQuery.value.trim()) {
      const keyword = searchQuery.value.trim().toLowerCase()
      result = result.filter(
        (w) =>
          w.name.toLowerCase().includes(keyword) ||
          w.description.toLowerCase().includes(keyword) ||
          w.tags.some((t) => t.toLowerCase().includes(keyword)),
      )
    }

    return result
  })

  /** 总资源数 */
  const totalCount = computed(() => resources.value.length)

  /** 选中数量 */
  const selectedCount = computed(() => (selectedWebsite.value ? 1 : 0))

  /** 存储使用百分比 */
  const storagePercent = computed(() => Math.round((storageUsed.value / storageTotal.value) * 100))

  /** 当前面包屑路径 */
  const breadcrumbPath = computed(() => {
    const parts = ['Home']
    if (activeQuickAccess.value) {
      const qa = quickAccessList.value.find((q) => q.key === activeQuickAccess.value)
      if (qa) {
        parts.push(qa.label)
        return parts
      }
    }
    if (activeCategoryId.value !== null) {
      const cat = categories.value.find((c) => c.id === activeCategoryId.value)
      if (cat) {
        parts.push(cat.name)
      }
    }
    return parts
  })

  /** 设置激活的快捷访问 */
  const setActiveQuickAccess = (key: string | null) => {
    activeQuickAccess.value = key
    activeCategoryId.value = null
  }

  /** 设置激活的分类 */
  const setActiveCategory = (id: number | null) => {
    activeCategoryId.value = id
    activeQuickAccess.value = null
  }

  /** 选中一个网站 */
  const selectWebsite = (website: CollectionWebsite) => {
    selectedWebsite.value = website
    detailPanelOpen.value = true
  }

  /** 切换详情面板 */
  const toggleDetailPanel = () => {
    detailPanelOpen.value = !detailPanelOpen.value
  }

  /** 关闭详情面板 */
  const closeDetailPanel = () => {
    detailPanelOpen.value = false
  }

  /** 切换视图模式 */
  const setViewMode = (mode: ViewMode) => {
    viewMode.value = mode
  }

  /** 设置缩放级别 */
  const setZoomLevel = (level: number) => {
    zoomLevel.value = Math.min(150, Math.max(50, level))
  }

  /** 切换星标 */
  const toggleStar = (websiteId: number) => {
    const target = resources.value.find((w) => w.id === websiteId)
    if (target) {
      target.isStarred = !target.isStarred
    }
  }

  return {
    categories,
    quickAccessList,
    activeQuickAccess,
    activeCategoryId,
    resources,
    filteredResources,
    searchQuery,
    selectedWebsite,
    detailPanelOpen,
    viewMode,
    zoomLevel,
    mobileSidebarOpen,
    mobileDetailOpen,
    isLoading,
    storageUsed,
    storageTotal,
    totalCount,
    selectedCount,
    storagePercent,
    breadcrumbPath,

    setActiveQuickAccess,
    setActiveCategory,
    selectWebsite,
    toggleDetailPanel,
    closeDetailPanel,
    setViewMode,
    setZoomLevel,
    toggleStar,
  }
}
