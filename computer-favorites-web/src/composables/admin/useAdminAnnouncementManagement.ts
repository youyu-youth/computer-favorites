import { computed, reactive, ref, shallowRef, watch } from 'vue'
import type {
  AdminAnnouncementFormModel,
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

const ANNOUNCEMENT_MOCK_DATA: AdminAnnouncementRecord[] = [
  {
    id: 3001,
    title: '平台更新：管理端公告工作台即将上线',
    content:
      '公告管理页将统一承载公告筛选、查看、编辑、置顶与批量操作能力，帮助运营在同一页面内完成日常维护。',
    type: 3,
    isTop: 1,
    status: 1,
    publishTime: '2026-04-18 18:00:00',
    createTime: '2026-04-18 17:20:00',
    updateTime: '2026-04-18 17:20:00',
  },
  {
    id: 3002,
    title: 'Bug 修复：移动端筛选工具栏在窄屏下挤压重叠',
    content:
      '修复了在 390px 宽度设备下，筛选按钮文字压缩、换行和对齐不一致的问题，移动端操作体验更稳定。',
    type: 2,
    isTop: 0,
    status: 1,
    publishTime: '2026-04-17 11:12:00',
    createTime: '2026-04-17 10:40:00',
    updateTime: '2026-04-17 11:12:00',
  },
  {
    id: 3003,
    title: '新增内容：用户端消息中心支持更清晰的状态反馈',
    content:
      '消息中心现在会区分未读、已读和处理中三种状态，并在列表上提供更稳定的高亮反馈，减少重复点击。',
    type: 1,
    isTop: 0,
    status: 1,
    publishTime: '2026-04-16 09:24:00',
    createTime: '2026-04-16 08:50:00',
    updateTime: '2026-04-16 09:24:00',
  },
  {
    id: 3004,
    title: '系统更新：亮暗主题切换链路完成统一治理',
    content:
      '平台已统一主题切换链路，页面外壳、卡片、抽屉和状态标签在亮色与暗色模式下都将使用一致的设计令牌。',
    type: 3,
    isTop: 1,
    status: 1,
    publishTime: '2026-04-15 20:08:00',
    createTime: '2026-04-15 19:36:00',
    updateTime: '2026-04-15 20:08:00',
  },
  {
    id: 3005,
    title: '维护通知：本周五晚间将进行缓存服务例行巡检',
    content:
      '巡检期间部分非核心功能可能存在短时波动，若出现异常，请刷新页面后重试。如仍未恢复，可通过反馈入口联系管理员。',
    type: 3,
    isTop: 0,
    status: 0,
    publishTime: '2026-04-14 21:30:00',
    createTime: '2026-04-14 20:40:00',
    updateTime: '2026-04-14 21:30:00',
  },
  {
    id: 3006,
    title: '新增内容：资源详情页补充相关推荐区域',
    content:
      '资源详情页新增相关推荐模块，会根据分类与标签提供更贴近上下文的站点推荐，帮助用户减少二次搜索成本。',
    type: 1,
    isTop: 0,
    status: 1,
    publishTime: '2026-04-13 15:12:00',
    createTime: '2026-04-13 14:46:00',
    updateTime: '2026-04-13 15:12:00',
  },
  {
    id: 3007,
    title: 'Bug 修复：部分长标题公告在列表中被错误截断',
    content:
      '针对较长标题场景补齐了多行截断和详情预览同步逻辑，避免列表与详情对同一条公告展示不一致。',
    type: 2,
    isTop: 0,
    status: 1,
    publishTime: '2026-04-12 10:18:00',
    createTime: '2026-04-12 09:52:00',
    updateTime: '2026-04-12 10:18:00',
  },
  {
    id: 3008,
    title: '灰度通知：公告管理页面进入设计验证阶段',
    content:
      '该公告当前仅用于内部灰度验证，暂不对外展示，因此保留为隐藏状态，后续确认交互完整后再切换为显示。',
    type: 3,
    isTop: 0,
    status: 0,
    publishTime: null,
    createTime: '2026-04-11 16:04:00',
    updateTime: '2026-04-11 16:04:00',
  },
  {
    id: 3009,
    title: '新增内容：管理员资料页补充更直观的登录设备提示',
    content:
      '管理员资料页新增最近登录设备摘要，帮助后台运维识别风险登录与异常环境访问，提升安全感知效率。',
    type: 1,
    isTop: 0,
    status: 1,
    publishTime: '2026-04-10 13:26:00',
    createTime: '2026-04-10 12:58:00',
    updateTime: '2026-04-10 13:26:00',
  },
  {
    id: 3010,
    title: 'Bug 修复：深色模式下部分按钮 hover 对比度不足',
    content:
      '已调整 hover 背景与边框对比度，深色模式下的按钮、分段操作和筛选控件在夜间环境中更易识别。',
    type: 2,
    isTop: 0,
    status: 1,
    publishTime: '2026-04-09 08:42:00',
    createTime: '2026-04-09 08:15:00',
    updateTime: '2026-04-09 08:42:00',
  },
  {
    id: 3011,
    title: '运营公告：本月精选站点专题页面将于下周上线',
    content:
      '为提升内容聚合效率，我们将推出精选站点专题页，支持按分类集中展示优质资源，并联动公告与首页推荐位。',
    type: 1,
    isTop: 1,
    status: 1,
    publishTime: '2026-04-08 18:20:00',
    createTime: '2026-04-08 17:36:00',
    updateTime: '2026-04-08 18:20:00',
  },
  {
    id: 3012,
    title: '维护通知：搜索索引将在今晚进行例行重建',
    content:
      '搜索索引重建期间，部分关键字可能返回较少结果。预计影响时间不超过 15 分钟，完成后系统会自动恢复。',
    type: 3,
    isTop: 0,
    status: 0,
    publishTime: '2026-04-07 22:00:00',
    createTime: '2026-04-07 21:22:00',
    updateTime: '2026-04-07 22:00:00',
  },
]

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

function resolveNowText(): string {
  const now = new Date()
  const year = now.getFullYear()
  const month = `${now.getMonth() + 1}`.padStart(2, '0')
  const day = `${now.getDate()}`.padStart(2, '0')
  const hours = `${now.getHours()}`.padStart(2, '0')
  const minutes = `${now.getMinutes()}`.padStart(2, '0')
  const seconds = `${now.getSeconds()}`.padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

function createRecordFromForm(
  id: number,
  form: AdminAnnouncementFormModel,
  createdAt: string,
  updatedAt: string,
): AdminAnnouncementRecord {
  return {
    id,
    title: form.title.trim(),
    content: form.content.trim(),
    type: form.type,
    isTop: form.isTop,
    status: form.status,
    publishTime: form.publishTime.trim() || null,
    createTime: createdAt,
    updateTime: updatedAt,
  }
}

export function useAdminAnnouncementManagement() {
  const records = ref<AdminAnnouncementRecord[]>([...ANNOUNCEMENT_MOCK_DATA])
  const loading = shallowRef(false)
  const selectedIds = ref<number[]>([])
  const detailOpen = shallowRef(true)
  const detailId = shallowRef<number | null>(ANNOUNCEMENT_MOCK_DATA[0]?.id ?? null)
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

  const statistics = computed<AdminAnnouncementStatistics>(() => ({
    total: records.value.length,
    visible: records.value.filter((item) => item.status === 1).length,
    hidden: records.value.filter((item) => item.status === 0).length,
    top: records.value.filter((item) => item.isTop === 1).length,
  }))

  const filteredAnnouncements = computed<AdminAnnouncementViewItem[]>(() => {
    const keyword = query.keyword.trim().toLowerCase()

    return records.value
      .filter((item) => {
        if (typeof query.status === 'number' && item.status !== query.status) {
          return false
        }

        if (typeof query.type === 'number' && item.type !== query.type) {
          return false
        }

        if (typeof query.isTop === 'boolean' && Boolean(item.isTop) !== query.isTop) {
          return false
        }

        if (!keyword) {
          return true
        }

        return [String(item.id), item.title, item.content].some((value) =>
          value.toLowerCase().includes(keyword),
        )
      })
      .map(mapAdminAnnouncementViewItem)
      .sort((left, right) => {
        if (left.isTop !== right.isTop) {
          return right.isTop - left.isTop
        }
        return right.publishTimeText.localeCompare(left.publishTimeText)
      })
  })

  const totalItems = computed(() => filteredAnnouncements.value.length)
  const totalPages = computed(() => Math.max(1, Math.ceil(totalItems.value / query.pageSize)))
  const visiblePages = computed(() => createVisiblePages(totalPages.value, query.pageNum))

  const pagedAnnouncements = computed(() => {
    const start = (query.pageNum - 1) * query.pageSize
    return filteredAnnouncements.value.slice(start, start + query.pageSize)
  })

  const selectedCount = computed(() => selectedIds.value.length)
  const detailRecord = computed<AdminAnnouncementViewItem | null>(() => {
    if (detailId.value === null) {
      return filteredAnnouncements.value[0] ?? null
    }
    return filteredAnnouncements.value.find((item) => item.id === detailId.value) ?? null
  })

  const resetToFirstPage = () => {
    query.pageNum = 1
  }

  const normalizePageNum = () => {
    if (query.pageNum > totalPages.value) {
      query.pageNum = totalPages.value
    }
    if (query.pageNum < 1) {
      query.pageNum = 1
    }
  }

  const syncDetailRecord = () => {
    const visibleIds = new Set(filteredAnnouncements.value.map((item) => item.id))
    if (detailId.value !== null && visibleIds.has(detailId.value)) {
      return
    }
    detailId.value = filteredAnnouncements.value[0]?.id ?? null
  }

  const syncSelectedIds = () => {
    const allIds = new Set(records.value.map((item) => item.id))
    selectedIds.value = selectedIds.value.filter((id) => allIds.has(id))
  }

  watch(
    filteredAnnouncements,
    () => {
      normalizePageNum()
      syncDetailRecord()
    },
    { immediate: true },
  )

  watch(
    records,
    () => {
      syncSelectedIds()
    },
    { deep: true },
  )

  const refreshData = async (): Promise<RefreshResult> => {
    loading.value = true
    try {
      await Promise.resolve()
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
    resetToFirstPage()
  }

  const setStatus = (status: AdminAnnouncementStatus | null) => {
    query.status = status
    resetToFirstPage()
  }

  const setType = (type: AdminAnnouncementType | null) => {
    query.type = type
    resetToFirstPage()
  }

  const setIsTop = (value: boolean | null) => {
    query.isTop = value
    resetToFirstPage()
  }

  const prevPage = () => {
    if (query.pageNum <= 1) {
      return
    }
    query.pageNum -= 1
  }

  const nextPage = () => {
    if (query.pageNum >= totalPages.value) {
      return
    }
    query.pageNum += 1
  }

  const goToPage = (page: number | string) => {
    const nextValue = Number(page)
    if (!Number.isFinite(nextValue)) {
      return
    }
    query.pageNum = Math.min(totalPages.value, Math.max(1, Math.trunc(nextValue)))
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

  const submitForm = async () => {
    if (!validateForm()) {
      return {
        ok: false as const,
        message: '请先修正公告表单中的校验问题。',
      }
    }

    formDialog.submitting = true
    try {
      const nowText = resolveNowText()

      if (formDialog.mode === 'create') {
        const nextId = records.value.reduce((maxId, item) => Math.max(maxId, item.id), 3000) + 1
        const nextRecord = createRecordFromForm(nextId, formDialog.form, nowText, nowText)
        records.value = [nextRecord, ...records.value]
        detailId.value = nextRecord.id
      } else if (formDialog.editingId !== null) {
        const editingId = formDialog.editingId
        records.value = records.value.map((item) =>
          item.id === editingId
            ? createRecordFromForm(editingId, formDialog.form, item.createTime, nowText)
            : item,
        )
        detailId.value = editingId
      }

      closeFormDialog()
      return {
        ok: true as const,
        message: formDialog.mode === 'create' ? '公告已创建。' : '公告已更新。',
      }
    } finally {
      formDialog.submitting = false
    }
  }

  const mutateRecords = (ids: number[], updater: (item: AdminAnnouncementRecord) => AdminAnnouncementRecord) => {
    const targetIds = new Set(ids)
    records.value = records.value.map((item) => (targetIds.has(item.id) ? updater(item) : item))
  }

  const toggleAnnouncementStatus = (announcementId: number) => {
    const matched = records.value.find((item) => item.id === announcementId)
    if (!matched) {
      return {
        ok: false as const,
        message: '未找到对应的公告记录。',
      }
    }

    const nextStatus: AdminAnnouncementStatus = matched.status === 1 ? 0 : 1
    mutateRecords([announcementId], (item) => ({
      ...item,
      status: nextStatus,
      updateTime: resolveNowText(),
    }))

    return {
      ok: true as const,
      message: nextStatus === 1 ? '公告已切换为显示。' : '公告已切换为隐藏。',
    }
  }

  const toggleAnnouncementTop = (announcementId: number) => {
    const matched = records.value.find((item) => item.id === announcementId)
    if (!matched) {
      return {
        ok: false as const,
        message: '未找到对应的公告记录。',
      }
    }

    const nextTop: 0 | 1 = matched.isTop === 1 ? 0 : 1
    mutateRecords([announcementId], (item) => ({
      ...item,
      isTop: nextTop,
      updateTime: resolveNowText(),
    }))

    return {
      ok: true as const,
      message: nextTop === 1 ? '公告已置顶。' : '公告已取消置顶。',
    }
  }

  const batchShow = () => {
    if (selectedIds.value.length === 0) {
      return {
        ok: false as const,
        message: '请先勾选需要批量显示的公告。',
      }
    }
    const count = selectedIds.value.length
    mutateRecords(selectedIds.value, (item) => ({
      ...item,
      status: 1,
      updateTime: resolveNowText(),
    }))
    selectedIds.value = []
    return {
      ok: true as const,
      message: `已批量显示 ${count} 条公告。`,
    }
  }

  const batchHide = () => {
    if (selectedIds.value.length === 0) {
      return {
        ok: false as const,
        message: '请先勾选需要批量隐藏的公告。',
      }
    }
    const count = selectedIds.value.length
    mutateRecords(selectedIds.value, (item) => ({
      ...item,
      status: 0,
      updateTime: resolveNowText(),
    }))
    selectedIds.value = []
    return {
      ok: true as const,
      message: `已批量隐藏 ${count} 条公告。`,
    }
  }

  const batchCancelTop = () => {
    if (selectedIds.value.length === 0) {
      return {
        ok: false as const,
        message: '请先勾选需要取消置顶的公告。',
      }
    }
    const count = selectedIds.value.length
    mutateRecords(selectedIds.value, (item) => ({
      ...item,
      isTop: 0,
      updateTime: resolveNowText(),
    }))
    selectedIds.value = []
    return {
      ok: true as const,
      message: `已批量取消 ${count} 条公告的置顶状态。`,
    }
  }

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
    prevPage,
    nextPage,
    goToPage,
  }
}
