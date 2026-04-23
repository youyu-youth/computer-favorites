import { computed, reactive, ref, shallowRef, watch } from 'vue'
import {
  createAdminAnnouncement,
  deleteAdminAnnouncement,
  getAdminAnnouncementPage,
  updateAdminAnnouncement,
  updateAdminAnnouncementStatus,
} from '@/api/admin-announcement'
import type {
  AdminAnnouncementEditPayload,
  AdminAnnouncementFormModel,
  AdminAnnouncementPageQuery,
  AdminAnnouncementQuery,
  AdminAnnouncementRecord,
  AdminAnnouncementStatistics,
  AdminAnnouncementStatus,
  AdminAnnouncementType,
  AdminAnnouncementViewItem,
} from '@/types/admin-announcement'
import {
  createAdminAnnouncementFormModel,
  mapAdminAnnouncementViewItem,
} from '@/types/admin-announcement'

type RefreshResult = {
  ok: boolean
  message?: string
}

type FormDialogState = {
  open: boolean
  mode: 'create' | 'edit'
  editingId: number | null
  form: AdminAnnouncementFormModel
  errors: Record<string, string>
  submitting: boolean
}

const DEFAULT_PAGE_SIZE = 6

function createVisiblePages(totalPages: number, currentPage: number): Array<number | string> {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, index) => index + 1)
  }

  const pages: Array<number | string> = [1]

  if (currentPage > 3) {
    pages.push('...')
  }

  for (let page = Math.max(2, currentPage - 1); page <= Math.min(totalPages - 1, currentPage + 1); page += 1) {
    pages.push(page)
  }

  if (currentPage < totalPages - 2) {
    pages.push('...')
  }

  pages.push(totalPages)
  return pages
}

function buildApiQuery(query: AdminAnnouncementQuery): AdminAnnouncementPageQuery {
  return {
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    keyword: query.keyword.trim() || undefined,
    status: query.status ?? undefined,
    type: query.type ?? undefined,
    isTop: query.isTop === null ? undefined : query.isTop ? 1 : 0,
  }
}

export function useAdminAnnouncementManagement() {
  const records = ref<AdminAnnouncementRecord[]>([])
  const loading = shallowRef(false)
  const selectedIds = ref<number[]>([])
  const detailOpen = shallowRef(true)
  const detailId = shallowRef<number | null>(null)
  const total = ref(0)
  const totalPages = ref(1)
  const statistics = ref<AdminAnnouncementStatistics>({ total: 0, visible: 0, hidden: 0, top: 0 })

  const query = reactive<AdminAnnouncementQuery>({
    pageNum: 1,
    pageSize: DEFAULT_PAGE_SIZE,
    keyword: '',
    status: null,
    type: null,
    isTop: null,
  })

  const formDialog = reactive<FormDialogState>({
    open: false,
    mode: 'create',
    editingId: null,
    form: createAdminAnnouncementFormModel(),
    errors: {},
    submitting: false,
  })

  const visiblePages = computed(() => createVisiblePages(totalPages.value, query.pageNum))

  const pagedAnnouncements = computed<AdminAnnouncementViewItem[]>(() =>
    records.value.map(mapAdminAnnouncementViewItem),
  )

  const totalItems = computed(() => total.value)

  const selectedCount = computed(() => selectedIds.value.length)

  const detailRecord = computed<AdminAnnouncementViewItem | null>(() => {
    if (detailId.value === null) {
      return pagedAnnouncements.value[0] ?? null
    }
    return pagedAnnouncements.value.find((item) => item.id === detailId.value) ?? null
  })

  const normalizePageNum = () => {
    if (query.pageNum > totalPages.value) {
      query.pageNum = totalPages.value
    }
    if (query.pageNum < 1) {
      query.pageNum = 1
    }
  }

  const syncDetailRecord = () => {
    const visibleIds = new Set(pagedAnnouncements.value.map((item) => item.id))
    if (detailId.value !== null && visibleIds.has(detailId.value)) {
      return
    }
    detailId.value = pagedAnnouncements.value[0]?.id ?? null
  }

  const refreshData = async (): Promise<RefreshResult> => {
    loading.value = true
    try {
      const pageData = await getAdminAnnouncementPage(buildApiQuery(query))
      records.value = pageData.records
      total.value = pageData.total
      totalPages.value = pageData.totalPages
      if (pageData.stats) {
        statistics.value = pageData.stats
      }
      normalizePageNum()
      syncDetailRecord()
      return { ok: true }
    } catch (error) {
      return {
        ok: false,
        message: error instanceof Error ? error.message : '公告数据刷新失败。',
      }
    } finally {
      loading.value = false
    }
  }

  const setKeyword = (keyword: string) => {
    query.keyword = keyword
    query.pageNum = 1
    refreshData()
  }

  const setStatus = (status: AdminAnnouncementStatus | null) => {
    query.status = status
    query.pageNum = 1
    refreshData()
  }

  const setType = (type: AdminAnnouncementType | null) => {
    query.type = type
    query.pageNum = 1
    refreshData()
  }

  const setIsTop = (value: boolean | null) => {
    query.isTop = value
    query.pageNum = 1
    refreshData()
  }

  const prevPage = () => {
    if (query.pageNum <= 1) {
      return
    }
    query.pageNum -= 1
    refreshData()
  }

  const nextPage = () => {
    if (query.pageNum >= totalPages.value) {
      return
    }
    query.pageNum += 1
    refreshData()
  }

  const goToPage = (page: number | string) => {
    const nextValue = Number(page)
    if (!Number.isFinite(nextValue)) {
      return
    }
    query.pageNum = Math.min(totalPages.value, Math.max(1, Math.trunc(nextValue)))
    refreshData()
  }

  const setSelectedIds = (ids: number[]) => {
    selectedIds.value = [...new Set(ids)]
  }

  const openDetail = (announcementId: number) => {
    detailId.value = announcementId
    detailOpen.value = true
  }

  const closeDetail = () => {
    detailOpen.value = false
  }

  const openCreateDialog = () => {
    formDialog.open = true
    formDialog.mode = 'create'
    formDialog.editingId = null
    formDialog.form = createAdminAnnouncementFormModel()
    formDialog.errors = {}
  }

  const openEditDialog = (announcementId: number) => {
    const matched = records.value.find((item) => item.id === announcementId)
    if (!matched) {
      return
    }
    formDialog.open = true
    formDialog.mode = 'edit'
    formDialog.editingId = matched.id
    formDialog.form = {
      title: matched.title,
      content: matched.content,
      type: matched.type,
      isTop: matched.isTop,
      status: matched.status,
      publishTime: matched.publishTime ?? '',
    }
    formDialog.errors = {}
  }

  const closeFormDialog = () => {
    formDialog.open = false
    formDialog.editingId = null
    formDialog.errors = {}
    formDialog.submitting = false
  }

  const updateForm = (payload: Partial<AdminAnnouncementFormModel>) => {
    formDialog.form = {
      ...formDialog.form,
      ...payload,
    }
  }

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {}
    const trimmedTitle = formDialog.form.title.trim()
    const trimmedContent = formDialog.form.content.trim()

    if (!trimmedTitle) {
      errors.title = '请输入公告标题'
    } else if (trimmedTitle.length > 200) {
      errors.title = '公告标题不能超过 200 个字符'
    }

    if (!trimmedContent) {
      errors.content = '请输入公告内容'
    }

    formDialog.errors = errors
    return Object.keys(errors).length === 0
  }

  const buildEditPayloadFromForm = (form: AdminAnnouncementFormModel): AdminAnnouncementEditPayload => ({
    title: form.title.trim(),
    content: form.content.trim(),
    type: form.type,
    isTop: form.isTop,
    status: form.status,
    publishTime: form.publishTime.trim(),
  })

  const submitForm = async () => {
    if (!validateForm()) {
      return {
        ok: false as const,
        message: '请先修正公告表单中的校验问题。',
      }
    }

    formDialog.submitting = true
    try {
      const payload = buildEditPayloadFromForm(formDialog.form)

      if (formDialog.mode === 'create') {
        const newId = await createAdminAnnouncement(payload)
        detailId.value = newId
      } else if (formDialog.editingId !== null) {
        await updateAdminAnnouncement(formDialog.editingId, payload)
        detailId.value = formDialog.editingId
      }

      closeFormDialog()
      await refreshData()
      return {
        ok: true as const,
        message: formDialog.mode === 'create' ? '公告已创建。' : '公告已更新。',
      }
    } catch (error) {
      return {
        ok: false as const,
        message: error instanceof Error ? error.message : '操作失败。',
      }
    } finally {
      formDialog.submitting = false
    }
  }

  const toggleAnnouncementStatus = async (announcementId: number): Promise<RefreshResult> => {
    const matched = records.value.find((item) => item.id === announcementId)
    if (!matched) {
      return { ok: false, message: '未找到对应的公告记录。' }
    }

    const nextStatus: AdminAnnouncementStatus = matched.status === 1 ? 0 : 1
    try {
      await updateAdminAnnouncementStatus(announcementId, { status: nextStatus })
      await refreshData()
      return {
        ok: true,
        message: nextStatus === 1 ? '公告已切换为显示。' : '公告已切换为隐藏。',
      }
    } catch (error) {
      return {
        ok: false,
        message: error instanceof Error ? error.message : '状态切换失败。',
      }
    }
  }

  const toggleAnnouncementTop = async (announcementId: number): Promise<RefreshResult> => {
    const matched = records.value.find((item) => item.id === announcementId)
    if (!matched) {
      return { ok: false, message: '未找到对应的公告记录。' }
    }

    const nextTop: 0 | 1 = matched.isTop === 1 ? 0 : 1
    const payload: AdminAnnouncementEditPayload = {
      title: matched.title,
      content: matched.content,
      type: matched.type,
      isTop: nextTop,
      status: matched.status,
      publishTime: matched.publishTime ?? '',
    }

    try {
      await updateAdminAnnouncement(announcementId, payload)
      await refreshData()
      return {
        ok: true,
        message: nextTop === 1 ? '公告已置顶。' : '公告已取消置顶。',
      }
    } catch (error) {
      return {
        ok: false,
        message: error instanceof Error ? error.message : '置顶操作失败。',
      }
    }
  }

  const batchShow = async (): Promise<RefreshResult> => {
    if (selectedIds.value.length === 0) {
      return { ok: false, message: '请先勾选需要批量显示的公告。' }
    }

    const ids = [...selectedIds.value]
    let failCount = 0

    for (const id of ids) {
      try {
        await updateAdminAnnouncementStatus(id, { status: 1 })
      } catch {
        failCount += 1
      }
    }

    selectedIds.value = []
    await refreshData()

    if (failCount > 0) {
      return { ok: false, message: `${failCount} 条公告显示失败，请重试。` }
    }
    return { ok: true, message: `已批量显示 ${ids.length} 条公告。` }
  }

  const batchHide = async (): Promise<RefreshResult> => {
    if (selectedIds.value.length === 0) {
      return { ok: false, message: '请先勾选需要批量隐藏的公告。' }
    }

    const ids = [...selectedIds.value]
    let failCount = 0

    for (const id of ids) {
      try {
        await updateAdminAnnouncementStatus(id, { status: 0 })
      } catch {
        failCount += 1
      }
    }

    selectedIds.value = []
    await refreshData()

    if (failCount > 0) {
      return { ok: false, message: `${failCount} 条公告隐藏失败，请重试。` }
    }
    return { ok: true, message: `已批量隐藏 ${ids.length} 条公告。` }
  }

  const batchCancelTop = async (): Promise<RefreshResult> => {
    if (selectedIds.value.length === 0) {
      return { ok: false, message: '请先勾选需要取消置顶的公告。' }
    }

    const ids = [...selectedIds.value]
    let failCount = 0

    for (const id of ids) {
      const matched = records.value.find((item) => item.id === id)
      if (!matched) {
        failCount += 1
        continue
      }

      try {
        const payload: AdminAnnouncementEditPayload = {
          title: matched.title,
          content: matched.content,
          type: matched.type,
          isTop: 0,
          status: matched.status,
          publishTime: matched.publishTime ?? '',
        }
        await updateAdminAnnouncement(id, payload)
      } catch {
        failCount += 1
      }
    }

    selectedIds.value = []
    await refreshData()

    if (failCount > 0) {
      return { ok: false, message: `${failCount} 条公告取消置顶失败，请重试。` }
    }
    return { ok: true, message: `已批量取消 ${ids.length} 条公告的置顶状态。` }
  }

  const deleteAnnouncement = async (announcementId: number): Promise<RefreshResult> => {
    try {
      await deleteAdminAnnouncement(announcementId)
      await refreshData()
      return { ok: true, message: '公告已删除。' }
    } catch (error) {
      return {
        ok: false,
        message: error instanceof Error ? error.message : '删除公告失败。',
      }
    }
  }

  watch(
    records,
    () => {
      const allIds = new Set(records.value.map((item) => item.id))
      selectedIds.value = selectedIds.value.filter((id) => allIds.has(id))
    },
    { deep: true },
  )

  return {
    loading,
    query,
    pagedAnnouncements,
    statistics,
    totalItems,
    totalPages,
    visiblePages,
    selectedIds,
    selectedCount,
    detailOpen,
    detailRecord,
    formDialog,
    setKeyword,
    setStatus,
    setType,
    setIsTop,
    refreshData,
    setSelectedIds,
    openDetail,
    closeDetail,
    openCreateDialog,
    openEditDialog,
    closeFormDialog,
    updateForm,
    submitForm,
    toggleAnnouncementStatus,
    toggleAnnouncementTop,
    batchShow,
    batchHide,
    batchCancelTop,
    deleteAnnouncement,
    prevPage,
    nextPage,
    goToPage,
  }
}
