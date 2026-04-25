import { computed, reactive, ref, shallowRef } from 'vue'
import { getAnnouncementPage } from '@/api/user-notification'
import type { UserAnnouncementItem, UserAnnouncementQuery } from '@/types/notification'
import {
  ANNOUNCEMENT_TYPE_LABEL_MAP,
  mapAnnouncementViewItem,
  resolveAnnouncementFilterLabel,
  type AnnouncementFilterValue,
  type AnnouncementViewItem,
  type AnnouncementType,
} from '@/types/user-announcement'

export function useUserAnnouncementList() {
  const records = ref<UserAnnouncementItem[]>([])
  const loading = shallowRef(false)
  const total = ref(0)
  const totalPages = ref(0)

  const query = reactive<UserAnnouncementQuery>({
    pageNum: 1,
    pageSize: 20,
    type: undefined,
    isTop: undefined,
  })

  const pagedAnnouncements = computed<AnnouncementViewItem[]>(() =>
    records.value.map(mapAnnouncementViewItem),
  )

  const pinnedAnnouncements = computed<AnnouncementViewItem[]>(() =>
    pagedAnnouncements.value.filter((item) => item.isTop === 1),
  )

  const hasMore = computed(() => (query.pageNum ?? 1) < totalPages.value)

  const activeType = computed<AnnouncementFilterValue>(() => {
    if (query.type !== undefined && query.type !== null) {
      return query.type as AnnouncementType
    }
    return 'all'
  })

  const activeTypeLabel = computed(() => resolveAnnouncementFilterLabel(activeType.value))

  const latestPublishTime = computed(() => {
    const latestItem = pagedAnnouncements.value[0]
    if (!latestItem) {
      return '-'
    }
    return `${latestItem.displayDate} ${latestItem.displayTime}`
  })

  async function refreshData(): Promise<void> {
    loading.value = true
    try {
      const result = await getAnnouncementPage(query)
      records.value = result.list
      total.value = result.total
      totalPages.value = result.totalPages
    } catch {
      records.value = []
      total.value = 0
      totalPages.value = 0
    } finally {
      loading.value = false
    }
  }

  function setFilterType(type: number | null): void {
    query.type = type ?? undefined
    query.pageNum = 1
    refreshData()
  }

  function setIsTop(value: boolean | null): void {
    query.isTop = value === true ? 1 : value === false ? 0 : undefined
    query.pageNum = 1
    refreshData()
  }

  function nextPage(): void {
    if (hasMore.value) {
      query.pageNum = (query.pageNum ?? 1) + 1
      refreshData()
    }
  }

  function prevPage(): void {
    if ((query.pageNum ?? 1) > 1) {
      query.pageNum = (query.pageNum ?? 1) - 1
      refreshData()
    }
  }

  function goToPage(page: number): void {
    if (page >= 1 && page <= totalPages.value) {
      query.pageNum = page
      refreshData()
    }
  }

  return {
    records,
    loading,
    total,
    totalPages,
    query,
    pagedAnnouncements,
    pinnedAnnouncements,
    hasMore,
    activeType,
    activeTypeLabel,
    latestPublishTime,
    refreshData,
    setFilterType,
    setIsTop,
    nextPage,
    prevPage,
    goToPage,
  }
}
