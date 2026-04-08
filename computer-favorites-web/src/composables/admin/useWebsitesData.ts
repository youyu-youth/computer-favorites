import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import {
  batchAuditAdminWebsite,
  batchUpdateAdminWebsiteStatus,
  deleteAdminWebsite,
  getAdminWebsiteCategories,
  getAdminWebsitePage,
  getAdminWebsiteStats,
  updateAdminWebsiteStatus,
} from '@/api/admin-website'
import { useAdminNavStore } from '@/stores/adminNav'
import { useToast } from '@/composables/useToast'
import type {
  AdminWebsiteAuditActionValue,
  AdminWebsiteListItem,
  AdminWebsiteStatusValue,
  AdminWebsiteStats,
  DeletedFilterValue,
} from '@/types/admin-website'

type WebsiteTagStatus = 'good' | 'warning'

type WebsiteTag = {
  name: string
  status: WebsiteTagStatus
}

type WebsiteCardItem = {
  id: number
  title: string
  author: string
  isOfficial: boolean
  icon: string
  fallbackIcon: string
  iconBg: string
  description: string
  tags: WebsiteTag[]
  status: AdminWebsiteStatusValue
  deleted: number
}

type WebsiteCategoryItem = {
  id: number
  name: string
  count: number
}

const ALL_CATEGORY_ID = 0
const DEFAULT_PAGE_SIZE = 12
const SEARCH_DEBOUNCE_MS = 300

type LoadWebsitePageOptions = {
  silent?: boolean
}

type ReloadDataOptions = {
  silentListLoading?: boolean
}

function resolveErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallback
}

function formatTimeDisplay(timeValue?: string): string {
  if (!timeValue) {
    return '-'
  }
  const date = new Date(timeValue)
  if (Number.isNaN(date.getTime())) {
    return '-'
  }
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

function resolveCardIcon(record: AdminWebsiteListItem): string {
  if (record.source === 1) {
    return 'fas fa-user-edit'
  }
  if (record.isRecommend === 1) {
    return 'fas fa-star'
  }
  return 'fas fa-globe'
}

function resolveCardIconBg(record: AdminWebsiteListItem): string {
  if (record.deleted === 1) {
    return 'bg-gray-500'
  }
  if (record.status === 1) {
    return 'bg-emerald-600'
  }
  return 'bg-amber-600'
}

function resolveCardTags(record: AdminWebsiteListItem): WebsiteTag[] {
  const statusTag: WebsiteTag = {
    name: record.deleted === 1 ? '已删除' : record.status === 1 ? '已上架' : '已下架',
    status: record.deleted === 1 ? 'warning' : 'good',
  }

  const auditTag: WebsiteTag = {
    name: record.auditStatus === 0 ? '待审核' : record.auditStatus === 2 ? '已拒绝' : '已通过',
    status: record.auditStatus === 2 ? 'warning' : 'good',
  }

  const recommendTag: WebsiteTag = {
    name: record.isRecommend === 1 ? '已推荐' : '普通',
    status: record.isRecommend === 1 ? 'good' : 'warning',
  }

  return [statusTag, auditTag, recommendTag]
}

function mapRecordToCard(record: AdminWebsiteListItem): WebsiteCardItem {
  return {
    id: record.id,
    title: record.name,
    author: record.categoryName || '未分类',
    isOfficial: record.source === 0,
    icon: (record.icon || '').trim(),
    fallbackIcon: resolveCardIcon(record),
    iconBg: resolveCardIconBg(record),
    description: record.summary || record.description || record.url || '-',
    tags: resolveCardTags(record),
    status: record.status,
    deleted: record.deleted,
  }
}

export function useWebsitesData() {
  const adminNavStore = useAdminNavStore()
  const { add: showToast } = useToast()
  const { selectedCategoryId, selectedWebsiteAuditTab, activeMenu } = storeToRefs(adminNavStore)

  const viewMode = ref('grid')
  const searchQuery = ref('')
  const deletedFilter = ref<DeletedFilterValue>(-1)

  const tools = ref([
    { title: 'API-delete-a-block', subtitle: 'Notion MCP Server' },
    { title: 'API-retrieve-a-database', subtitle: 'Notion ReadOnly MCP Server' },
    { title: 'API-retrieve-a-page', subtitle: 'Notion ReadOnly MCP Server' },
    { title: 'API-retrieve-a-database', subtitle: 'Notion MCP Server' },
  ])

  const connectors = ref([
    { title: '0nMCP — Universal AI AP...', subtitle: 'io.github.0nork' },
    { title: '123elec-mcp', subtitle: 'io.github.Servicedsi' },
    { title: '1stay', subtitle: 'com.stayker' },
    { title: '1stDibs', subtitle: 'com.1stdibs' },
  ])

  const servers = ref<WebsiteCardItem[]>([])
  const selectedIds = ref<number[]>([])
  const updatingWebsiteIds = ref<number[]>([])
  const batchStatusUpdating = ref(false)
  const batchAuditUpdating = ref(false)
  const currentPage = ref(1)
  const totalItems = ref(0)
  const loading = ref(false)
  const errorMessage = ref('')
  const stats = ref<AdminWebsiteStats>({
    total: 0,
    online: 0,
    offline: 0,
    pendingAudit: 0,
    rejectedAudit: 0,
    deleted: 0,
    latestUpdateTime: '',
  })

  let listRequestId = 0
  let searchTimer: ReturnType<typeof setTimeout> | null = null
  let syncingCategorySelection = false

  const pageSize = DEFAULT_PAGE_SIZE

  const totalPages = computed(() => {
    if (totalItems.value <= 0) {
      return 1
    }
    return Math.ceil(totalItems.value / pageSize)
  })

  const visiblePages = computed(() => {
    if (totalPages.value <= 7) {
      return Array.from({ length: totalPages.value }, (_, index) => index + 1)
    }
    if (currentPage.value <= 3) {
      return [1, 2, 3, 4, '...', totalPages.value - 1, totalPages.value]
    }
    if (currentPage.value >= totalPages.value - 2) {
      return [
        1,
        2,
        '...',
        totalPages.value - 3,
        totalPages.value - 2,
        totalPages.value - 1,
        totalPages.value,
      ]
    }
    return [
      1,
      '...',
      currentPage.value - 1,
      currentPage.value,
      currentPage.value + 1,
      '...',
      totalPages.value,
    ]
  })

  const filteredServers = computed(() => servers.value)

  const selectedCount = computed(() => selectedIds.value.length)

  const allSelectableSelected = computed(() => {
    const selectableIds = servers.value.filter((item) => item.deleted !== 1).map((item) => item.id)
    if (selectableIds.length === 0) {
      return false
    }
    return selectableIds.every((id) => selectedIds.value.includes(id))
  })

  const formattedStats = computed(() => ({
    total: stats.value.total,
    online: stats.value.online,
    offline: stats.value.offline,
    pendingAudit: stats.value.pendingAudit,
    rejectedAudit: stats.value.rejectedAudit,
    deleted: stats.value.deleted,
    latestUpdateTime: formatTimeDisplay(stats.value.latestUpdateTime),
  }))

  const loadCategories = async () => {
    const categoryList = await getAdminWebsiteCategories(deletedFilter.value)
    const totalCount = categoryList.reduce((sum, item) => sum + Number(item.count || 0), 0)
    const mappedList: WebsiteCategoryItem[] = [
      {
        id: ALL_CATEGORY_ID,
        name: '全部分类',
        count: totalCount,
      },
      ...categoryList.map((item) => ({
        id: item.id,
        name: item.name,
        count: Number(item.count || 0),
      })),
    ]
    adminNavStore.setWebsiteCategories(mappedList)
  }

  const loadStats = async () => {
    stats.value = await getAdminWebsiteStats(deletedFilter.value)
  }

  const syncPendingAuditBadgeCount = async () => {
    try {
      const pendingStats = await getAdminWebsiteStats(0)
      adminNavStore.setPendingAuditCount(Number(pendingStats.pendingAudit || 0))
    } catch {
      // 静默失败，避免因角标同步失败影响主列表加载。
    }
  }

  const loadWebsitePage = async (options?: LoadWebsitePageOptions) => {
    if (activeMenu.value !== 'websites') {
      return
    }

    const silent = options?.silent === true

    listRequestId += 1
    const requestId = listRequestId
    if (!silent) {
      loading.value = true
      errorMessage.value = ''
    }
    try {
      const pageData = await getAdminWebsitePage({
        pageNum: currentPage.value,
        pageSize,
        deleted: deletedFilter.value,
        categoryId:
          selectedCategoryId.value === ALL_CATEGORY_ID ? undefined : selectedCategoryId.value,
        auditBucket: selectedWebsiteAuditTab.value === 'pending' ? 0 : 1,
        keyword: searchQuery.value,
      })

      if (requestId !== listRequestId) {
        return
      }

      totalItems.value = Number(pageData.total || 0)
      servers.value = (pageData.records || []).map(mapRecordToCard)
      selectedIds.value = selectedIds.value.filter((id) =>
        servers.value.some((item) => item.id === id && item.deleted !== 1),
      )

      if (totalItems.value > 0 && currentPage.value > totalPages.value) {
        currentPage.value = totalPages.value
        await loadWebsitePage()
      }
    } catch (error) {
      if (requestId !== listRequestId) {
        return
      }
      if (!silent) {
        servers.value = []
        totalItems.value = 0
        errorMessage.value = resolveErrorMessage(error, '网站列表加载失败')
        return
      }
      throw error
    } finally {
      if (requestId === listRequestId && !silent) {
        loading.value = false
      }
    }
  }

  const prevPage = async () => {
    if (currentPage.value <= 1) {
      return
    }
    currentPage.value -= 1
    await loadWebsitePage()
  }

  const nextPage = async () => {
    if (currentPage.value >= totalPages.value) {
      return
    }
    currentPage.value += 1
    await loadWebsitePage()
  }

  const goToPage = async (page: number | string) => {
    if (page === '...' || typeof page !== 'number') {
      return
    }
    if (page < 1 || page > totalPages.value || page === currentPage.value) {
      return
    }
    currentPage.value = page
    await loadWebsitePage()
  }

  const reloadData = async (options?: ReloadDataOptions) => {
    if (activeMenu.value !== 'websites') {
      return
    }

    const silentListLoading = options?.silentListLoading === true

    if (!silentListLoading) {
      errorMessage.value = ''
    }
    try {
      await Promise.all([loadCategories(), loadStats(), syncPendingAuditBadgeCount()])
      await loadWebsitePage({ silent: silentListLoading })
    } catch (error) {
      if (!silentListLoading) {
        errorMessage.value = resolveErrorMessage(error, '网站管理数据加载失败')
        return
      }
      throw error
    }
  }

  const clearSelection = () => {
    selectedIds.value = []
  }

  const toggleSelect = (websiteId: number) => {
    const targetWebsite = servers.value.find((item) => item.id === websiteId)
    if (!targetWebsite || targetWebsite.deleted === 1) {
      return
    }

    if (selectedIds.value.includes(websiteId)) {
      selectedIds.value = selectedIds.value.filter((id) => id !== websiteId)
      return
    }
    selectedIds.value = [...selectedIds.value, websiteId]
  }

  const toggleSelectAll = () => {
    if (allSelectableSelected.value) {
      clearSelection()
      return
    }
    selectedIds.value = servers.value.filter((item) => item.deleted !== 1).map((item) => item.id)
  }

  const updateWebsiteStatus = async (websiteId: number, status: AdminWebsiteStatusValue) => {
    const targetWebsite = servers.value.find((item) => item.id === websiteId)
    if (!targetWebsite) {
      return
    }
    if (targetWebsite.deleted === 1) {
      showToast({ type: 'warning', title: '已删除网站不可修改上架状态' })
      return
    }
    if (targetWebsite.status === status) {
      showToast({ type: 'info', title: status === 1 ? '该网站已是上架状态' : '该网站已是下架状态' })
      return
    }
    if (updatingWebsiteIds.value.includes(websiteId) || batchStatusUpdating.value) {
      return
    }

    updatingWebsiteIds.value = [...updatingWebsiteIds.value, websiteId]
    try {
      await updateAdminWebsiteStatus({ websiteId, status })
      showToast({ type: 'success', title: status === 1 ? '网站上架成功' : '网站下架成功' })
      selectedIds.value = selectedIds.value.filter((id) => id !== websiteId)
      await reloadData({ silentListLoading: true })
    } catch (error) {
      showToast({ type: 'error', title: resolveErrorMessage(error, '更新网站状态失败') })
    } finally {
      updatingWebsiteIds.value = updatingWebsiteIds.value.filter((id) => id !== websiteId)
    }
  }

  const batchUpdateWebsiteStatus = async (status: AdminWebsiteStatusValue) => {
    if (batchStatusUpdating.value) {
      return
    }
    const websiteIds = selectedIds.value.filter((id) =>
      servers.value.some((item) => item.id === id && item.deleted !== 1),
    )
    if (websiteIds.length === 0) {
      showToast({ type: 'warning', title: '请先选择要操作的网站' })
      return
    }

    batchStatusUpdating.value = true
    try {
      const updatedCount = await batchUpdateAdminWebsiteStatus({ websiteIds, status })
      clearSelection()
      showToast({
        type: 'success',
        title: status === 1 ? '批量上架成功' : '批量下架成功',
        description: `共更新 ${updatedCount} 条网站记录`,
      })
      await reloadData({ silentListLoading: true })
    } catch (error) {
      showToast({ type: 'error', title: resolveErrorMessage(error, '批量更新网站状态失败') })
    } finally {
      batchStatusUpdating.value = false
    }
  }

  const batchAuditWebsite = async (action: AdminWebsiteAuditActionValue, remark?: string) => {
    if (batchAuditUpdating.value) {
      return
    }

    const websiteIds = selectedIds.value.filter((id) =>
      servers.value.some((item) => item.id === id && item.deleted !== 1),
    )
    if (websiteIds.length === 0) {
      showToast({ type: 'warning', title: '请先选择要审核的网站' })
      return
    }

    batchAuditUpdating.value = true
    try {
      const result = await batchAuditAdminWebsite({ websiteIds, action, remark })
      clearSelection()

      if (result.successCount > 0) {
        adminNavStore.decreasePendingAuditCount(result.successCount)
      }

      if (result.failedCount > 0) {
        const failedPreview = result.failItems
          .slice(0, 3)
          .map((item) => `#${item.websiteId} ${item.reason}`)
          .join('；')
        showToast({
          type: 'warning',
          title: '批量审核已完成（含部分失败）',
          description: `成功 ${result.successCount} 条，失败 ${result.failedCount} 条。${failedPreview}`,
        })
      } else {
        showToast({
          type: 'success',
          title: action === 1 ? '批量审核通过成功' : '批量驳回成功',
          description: `共处理 ${result.successCount} 条网站记录`,
        })
      }

      await reloadData({ silentListLoading: true })
    } catch (error) {
      showToast({ type: 'error', title: resolveErrorMessage(error, '批量审核失败') })
    } finally {
      batchAuditUpdating.value = false
    }
  }

  const deleteWebsite = async (websiteId: number) => {
    const targetWebsite = servers.value.find((item) => item.id === websiteId)
    if (!targetWebsite) {
      return
    }
    if (targetWebsite.deleted === 1) {
      showToast({ type: 'warning', title: '该网站已在垃圾桶中' })
      return
    }

    try {
      await deleteAdminWebsite(websiteId)
      selectedIds.value = selectedIds.value.filter((id) => id !== websiteId)
      showToast({ type: 'success', title: '网站已放入垃圾桶' })
      await reloadData({ silentListLoading: true })
    } catch (error) {
      showToast({ type: 'error', title: resolveErrorMessage(error, '删除网站失败') })
    }
  }

  watch(deletedFilter, async () => {
    currentPage.value = 1
    syncingCategorySelection = true
    adminNavStore.setSelectedCategoryId(ALL_CATEGORY_ID)
    syncingCategorySelection = false
    clearSelection()
    await reloadData()
  })

  watch(selectedCategoryId, async (nextValue, previousValue) => {
    if (syncingCategorySelection) {
      return
    }
    if (nextValue === previousValue) {
      return
    }
    if (activeMenu.value !== 'websites') {
      return
    }
    currentPage.value = 1
    clearSelection()
    await loadWebsitePage()
  })

  watch(selectedWebsiteAuditTab, async (nextValue, previousValue) => {
    if (nextValue === previousValue) {
      return
    }
    if (activeMenu.value !== 'websites') {
      return
    }

    currentPage.value = 1
    clearSelection()
    await loadWebsitePage()
  })

  watch(activeMenu, async (nextMenu, previousMenu) => {
    if (nextMenu !== 'websites') {
      return
    }
    if (previousMenu === 'websites') {
      return
    }
    await reloadData()
  })

  watch(searchQuery, () => {
    currentPage.value = 1
    if (searchTimer) {
      clearTimeout(searchTimer)
    }
    searchTimer = setTimeout(() => {
      clearSelection()
      void loadWebsitePage()
    }, SEARCH_DEBOUNCE_MS)
  })

  onMounted(() => {
    if (activeMenu.value !== 'websites') {
      return
    }
    void reloadData()
  })

  onBeforeUnmount(() => {
    if (searchTimer) {
      clearTimeout(searchTimer)
      searchTimer = null
    }
  })

  return {
    viewMode,
    searchQuery,
    deletedFilter,
    tools,
    connectors,
    filteredServers,
    currentPage,
    totalPages,
    totalItems,
    pageSize,
    loading,
    errorMessage,
    stats: formattedStats,
    selectedIds,
    selectedCount,
    allSelectableSelected,
    updatingWebsiteIds,
    batchStatusUpdating,
    batchAuditUpdating,
    toggleSelect,
    toggleSelectAll,
    clearSelection,
    updateWebsiteStatus,
    batchUpdateWebsiteStatus,
    batchAuditWebsite,
    deleteWebsite,
    prevPage,
    nextPage,
    goToPage,
    reloadData,
    visiblePages,
  }
}
