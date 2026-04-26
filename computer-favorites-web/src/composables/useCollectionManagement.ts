/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 收藏夹页面状态管理 composable — 对接真实后端 API
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
import { getFolderTree, getFolderOptions, updateFolder, deleteFolder, toggleFolderHide } from '@/api/user-folder'
import { getCollectPage, getCollectStats, cancelCollect } from '@/api/user-collect'
import { verifyPassword } from '@/api/user-password'
import { useToast } from '@/composables/useToast'

const COLLECT_LIMIT = 500

const QUICK_ACCESS_LIST: CollectionQuickAccess[] = [
  { key: 'recent', label: '最近访问', icon: 'fas fa-clock' },
  { key: 'favorites', label: '稍后阅读', icon: 'fas fa-bookmark' },
]

export function useCollectionManagement() {
  const toast = useToast()

  const categories = ref<CollectionCategory[]>([])
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
  const collectStats = ref<CollectStats>({ collectCount: 0, folderCount: 0 })
  const folderOptions = ref<FolderOption[]>([])
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
        (w.websiteTags && w.websiteTags.toLowerCase().includes(keyword)),
    )
  })

  const totalCount = computed(() => totalCollectCount.value)
  const selectedCount = computed(() => (selectedWebsite.value ? 1 : 0))
  const storagePercent = computed(() => Math.min(100, Math.round((collectStats.value.collectCount / COLLECT_LIMIT) * 100)))

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
      const cat = findCategoryById(categories.value, activeCategoryId.value)
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

  const visibleCategories = computed(() => {
    const filterHidden = (cats: CollectionCategory[]): CollectionCategory[] => {
      return cats
        .filter((c) => !c.isHide)
        .map((c) => ({
          ...c,
          children: c.children ? filterHidden(c.children) : [],
        }))
    }
    return filterHidden(categories.value)
  })

  const loadFolderTree = async () => {
    try {
      categories.value = await getFolderTree()
    } catch {
      toast.add({ title: '加载失败', description: '文件夹加载失败，请刷新重试', type: 'error' })
    }
  }

  const loadFolderOptions = async () => {
    try {
      folderOptions.value = await getFolderOptions()
    } catch {
      folderOptions.value = []
    }
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

  const loadCollectStats = async () => {
    try {
      collectStats.value = await getCollectStats()
    } catch {
      collectStats.value = { collectCount: 0, folderCount: 0 }
    }
  }

  const refreshAll = async () => {
    await Promise.all([loadFolderTree(), loadFolderOptions(), loadCollectStats()])
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

  const handleRenameCategory = async (id: number, newName: string) => {
    try {
      await updateFolder(id, { name: newName })
      toast.add({ title: '重命名成功', description: `收藏夹已更名为「${newName}」`, type: 'success' })
      await loadFolderTree()
    } catch (e) {
      toast.add({ title: '重命名失败', description: e instanceof Error ? e.message : '请稍后重试', type: 'error' })
    }
  }

  const handleDeleteFolder = async (id: number) => {
    try {
      await deleteFolder(id)
      toast.add({ title: '已删除', description: '收藏夹已删除', type: 'success' })
      if (activeCategoryId.value === id) {
        setActiveCategory(null)
      }
      await loadFolderTree()
      await loadCollectStats()
    } catch (e) {
      toast.add({ title: '删除失败', description: e instanceof Error ? e.message : '请稍后重试', type: 'error' })
    }
  }

  const handleHideFolder = async (id: number) => {
    try {
      await toggleFolderHide(id, true)
      toast.add({ title: '已隐藏', description: '收藏夹及其内容已隐藏', type: 'success' })
      if (activeCategoryId.value === id) {
        setActiveCategory(null)
      }
      await loadFolderTree()
    } catch (e) {
      toast.add({ title: '操作失败', description: e instanceof Error ? e.message : '请稍后重试', type: 'error' })
    }
  }

  const handleShowHiddenFolders = async (hiddenIds: number[]) => {
    try {
      await Promise.all(hiddenIds.map((id) => toggleFolderHide(id, false)))
      toast.add({ title: '已恢复', description: '所有隐藏的收藏夹已恢复显示', type: 'success' })
      await loadFolderTree()
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

  const handleFolderCreated = async () => {
    toast.add({ title: '创建成功', description: '收藏夹已创建', type: 'success' })
    await loadFolderTree()
    await loadFolderOptions()
    await loadCollectStats()
  }

  onMounted(() => {
    refreshAll()
  })

  return {
    categories,
    visibleCategories,
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
    collectStats,
    folderOptions,
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
    handleRenameCategory,
    handleDeleteFolder,
    handleHideFolder,
    handleShowHiddenFolders,
    handleVerifyPassword,
    handleFolderCreated,
    refreshAll,
    loadFolderOptions,
  }
}
