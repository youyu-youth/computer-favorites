/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 收藏夹页面状态管理 composable — 对接真实后端 API
 * 文件夹相关状态由 useFolderStore 统一管理，本 composable 负责收藏页面特有逻辑
 */

import { ref, computed, onMounted } from 'vue'
import type {
  CollectionWebsite,
  CollectionCategory,
  CollectionQuickAccess,
  CollectStats,
  ViewMode,
} from '@/types/collection'
import type { FolderOption } from '@/types/folder'
import { getCollectPage, cancelCollect } from '@/api/user-collect'
import { verifyPassword } from '@/api/user-password'
import { useToast } from '@/composables/useToast'
import { useFolderStore } from '@/stores/folder'

const COLLECT_LIMIT = 500

const QUICK_ACCESS_LIST: CollectionQuickAccess[] = [
  { key: 'recent', label: '最近访问', icon: 'fas fa-clock' },
  { key: 'favorites', label: '稍后阅读', icon: 'fas fa-bookmark' },
]

export function useCollectionManagement() {
  const toast = useToast()
  const folderStore = useFolderStore()

  const quickAccessList = ref<CollectionQuickAccess[]>(QUICK_ACCESS_LIST)
  const activeQuickAccess = ref<string | null>(null)
  const activeCategoryId = ref<number | null>(null)
  const resources = ref<CollectionWebsite[]>([])
  const searchQuery = ref('')
  const selectedWebsite = ref<CollectionWebsite | null>(null)
  const detailPanelOpen = ref(true)
  const viewMode = ref<ViewMode>('grid')
  const zoomLevel = ref(100)
  const mobileSidebarOpen = ref(false)
  const mobileDetailOpen = ref(false)
  const isLoading = ref(false)
  const currentPage = ref(1)
  const pageSize = ref(20)
  const totalPages = ref(1)
  const totalCollectCount = ref(0)
  const hasMore = ref(true)

  const filteredResources = computed(() => {
    if (!searchQuery.value.trim()) return resources.value
    const keyword = searchQuery.value.trim().toLowerCase()
    return resources.value.filter(
      (w) =>
        w.websiteName.toLowerCase().includes(keyword) ||
        (w.websiteSummary && w.websiteSummary.toLowerCase().includes(keyword)) ||
        (w.websiteTags && w.websiteTags.some(t => t.name.toLowerCase().includes(keyword))),
    )
  })

  const totalCount = computed(() => totalCollectCount.value)
  const selectedCount = computed(() => (selectedWebsite.value ? 1 : 0))
  const storagePercent = computed(() => Math.min(100, Math.round((folderStore.collectStats.collectCount / COLLECT_LIMIT) * 100)))

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
      const cat = findCategoryById(folderStore.categories, activeCategoryId.value)
      if (cat) {
        parts.push(cat.name)
      }
    }
    return parts
  })

  const findCategoryById = (cats: CollectionCategory[], id: number): CollectionCategory | undefined => {
    for (const cat of cats) {
      if (cat.id === id) return cat
      const found = findCategoryById(cat.children || [], id)
      if (found) return found
    }
    return undefined
  }

  const loadCollectPage = async (reset = true) => {
    if (isLoading.value) return
    isLoading.value = true
    try {
      let queryFolderId: number | undefined
      if (activeCategoryId.value !== null) {
        queryFolderId = activeCategoryId.value
      }

      const result = await getCollectPage({
        folderId: queryFolderId,
        pageNum: currentPage.value,
        pageSize: pageSize.value,
      })

      if (reset) {
        resources.value = result.records
      } else {
        resources.value = [...resources.value, ...result.records]
      }
      totalCollectCount.value = result.total
      totalPages.value = result.totalPages
      hasMore.value = currentPage.value < result.totalPages
    } catch {
      toast.add({ title: '加载失败', description: '收藏列表加载失败', type: 'error' })
    } finally {
      isLoading.value = false
    }
  }

  const refreshAll = async () => {
    await folderStore.refreshFolderData()
    currentPage.value = 1
    await loadCollectPage(true)
  }

  const setActiveQuickAccess = (key: string | null) => {
    activeQuickAccess.value = key
    activeCategoryId.value = null
    currentPage.value = 1
    loadCollectPage(true)
  }

  const setActiveCategory = (id: number | null) => {
    activeCategoryId.value = id
    activeQuickAccess.value = null
    currentPage.value = 1
    loadCollectPage(true)
  }

  const selectWebsite = (website: CollectionWebsite) => {
    selectedWebsite.value = website
    detailPanelOpen.value = true
  }

  const toggleDetailPanel = () => {
    detailPanelOpen.value = !detailPanelOpen.value
  }

  const closeDetailPanel = () => {
    detailPanelOpen.value = false
  }

  const setViewMode = (mode: ViewMode) => {
    viewMode.value = mode
  }

  const setZoomLevel = (level: number) => {
    zoomLevel.value = Math.min(150, Math.max(50, level))
  }

  const handleCancelCollect = async (websiteId: number) => {
    try {
      await cancelCollect(websiteId)
      toast.add({ title: '取消收藏', description: '已取消收藏', type: 'success' })
      refreshAll()
    } catch (e) {
      toast.add({ title: '操作失败', description: e instanceof Error ? e.message : '请稍后重试', type: 'error' })
    }
  }

  const handleVerifyPassword = async (password: string): Promise<boolean> => {
    try {
      return await verifyPassword(password)
    } catch {
      return false
    }
  }

  onMounted(() => {
    refreshAll()
  })

  return {
    categories: computed(() => folderStore.categories),
    visibleCategories: computed(() => folderStore.visibleCategories),
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
    collectStats: computed(() => folderStore.collectStats),
    hiddenCategoryIds: computed(() => folderStore.hiddenCategoryIds),
    folderOptions: computed(() => folderStore.folderOptions),
    totalCount,
    selectedCount,
    storagePercent,
    breadcrumbPath,
    collectLimit: COLLECT_LIMIT,

    setActiveQuickAccess,
    setActiveCategory,
    selectWebsite,
    toggleDetailPanel,
    closeDetailPanel,
    setViewMode,
    setZoomLevel,
    handleCancelCollect,
    handleRenameCategory: folderStore.handleRenameCategory,
    handleDeleteFolder: folderStore.handleDeleteFolder,
    handleHideFolder: folderStore.handleHideFolder,
    handleShowHiddenFolders: folderStore.handleShowHiddenFolders,
    handleVerifyPassword,
    handleFolderCreated: folderStore.handleFolderCreated,
    refreshAll,
    loadFolderOptions: folderStore.loadFolderOptions,
  }
}
