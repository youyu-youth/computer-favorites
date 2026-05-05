import { computed, onBeforeUnmount, onMounted, reactive, shallowRef, watch } from 'vue'
import { useToast } from '@/composables/useToast'
import {
  batchDeleteAdminTechStack,
  createAdminTechStack,
  deleteAdminTechStack,
  deleteAdminTechStackIcon,
  getAdminTechStackPage,
  getAdminTechStackStats,
  updateAdminTechStack,
  updateAdminTechStackStatus,
  uploadAdminTechStackIcon,
} from '@/api/admin-tech-stack'
import type { FileUploadUploaderEvent } from 'primevue/fileupload'
import type {
  AdminTechStackFormModel,
  AdminTechStackItem,
  AdminTechStackSortField,
  AdminTechStackSortOrder,
  AdminTechStackStats,
  AdminTechStackStatus,
  AdminTechStackStatusValue,
} from '@/types/tech-stack'

type EditorMode = 'create' | 'edit'

const DEFAULT_PAGE_SIZE = 10

const resolveErrorMessage = (error: unknown, fallback: string): string => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallback
}

const mapStatusToView = (status: number): AdminTechStackStatus => {
  return status === 0 ? 'DISABLED' : 'ACTIVE'
}

const mapStatusToValue = (status: AdminTechStackStatus): AdminTechStackStatusValue => {
  return status === 'DISABLED' ? 0 : 1
}

const normalizeOptionalText = (value: string): string | undefined => {
  const normalizedValue = value.trim()
  return normalizedValue ? normalizedValue : undefined
}

const extractObjectKeyFromUrl = (url: string): string => {
  if (!url) {
    return ''
  }
  const idx = url.indexOf('/computer-favorites/')
  if (idx === -1) {
    return ''
  }
  return url.substring(idx + '/computer-favorites/'.length)
}

const mapRecordToItem = (record: Record<string, unknown>): AdminTechStackItem => ({
  id: record.id as number,
  name: record.name as string,
  iconPng: (record.iconPng as string) || '',
  officialUrl: (record.officialUrl as string) || '',
  description: (record.description as string) || '',
  color: (record.color as string) || '',
  status: mapStatusToView(record.status as number),
  sort: record.sort as number,
  createdAt: (record.createTime as string) || '',
  updatedAt: (record.updateTime as string) || '',
  userCount: (record.userCount as number) || 0,
})

export function useTechStack() {
  const { add: showToast } = useToast()

  const techStacks = shallowRef<AdminTechStackItem[]>([])
  const searchKeyword = shallowRef('')
  const sortField = shallowRef<AdminTechStackSortField>('sort')
  const sortOrder = shallowRef<AdminTechStackSortOrder>(1)
  const currentPage = shallowRef(1)
  const pageSize = shallowRef(DEFAULT_PAGE_SIZE)
  const totalItems = shallowRef(0)
  const totalPages = shallowRef(1)
  const loading = shallowRef(false)

  const editorOpen = shallowRef(false)
  const editorMode = shallowRef<EditorMode>('create')
  const editorSubmitting = shallowRef(false)
  const editingId = shallowRef<number | null>(null)

  const formModel = reactive<AdminTechStackFormModel>({
    name: '',
    iconPng: '',
    officialUrl: '',
    description: '',
    color: '',
    sort: 0,
    status: 'ACTIVE',
  })
  const formErrors = reactive<Record<string, string>>({})

  const deleteDialogOpen = shallowRef(false)
  const deleteSubmitting = shallowRef(false)
  const deletingItem = shallowRef<AdminTechStackItem | null>(null)

  const selectedIds = shallowRef<number[]>([])
  const batchDeleteDialogOpen = shallowRef(false)
  const batchDeleteSubmitting = shallowRef(false)

  const iconUploading = shallowRef(false)
  const iconObjectKey = shallowRef('')

  const stats = shallowRef<AdminTechStackStats>({ total: 0, enabled: 0, disabled: 0 })

  let listRequestId = 0
  let searchTimer: ReturnType<typeof setTimeout> | null = null

  const visiblePages = computed<Array<number | string>>(() => {
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

  const loadTechStackPage = async (options?: { silent?: boolean }): Promise<void> => {
    const requestId = ++listRequestId
    if (!options?.silent) {
      loading.value = true
    }

    try {
      const res = await getAdminTechStackPage({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        keyword: searchKeyword.value.trim() || undefined,
        sortField: sortField.value,
        sortOrder: sortOrder.value,
      })

      if (requestId !== listRequestId) {
        return
      }

      techStacks.value = res.records.map(mapRecordToItem)
      totalItems.value = res.total
      totalPages.value = res.totalPages || Math.max(1, Math.ceil(res.total / pageSize.value))

      if (currentPage.value > totalPages.value && totalPages.value > 0) {
        currentPage.value = totalPages.value
      }
    } catch (error) {
      if (requestId !== listRequestId) {
        return
      }
      showToast({
        type: 'error',
        title: resolveErrorMessage(error, '技术栈列表加载失败'),
      })
    } finally {
      if (requestId === listRequestId) {
        loading.value = false
      }
    }
  }

  const loadTechStackStats = async (silentErrorToast = false): Promise<void> => {
    try {
      const res = await getAdminTechStackStats()
      stats.value = res
    } catch (error) {
      if (!silentErrorToast) {
        showToast({
          type: 'error',
          title: resolveErrorMessage(error, '技术栈统计加载失败'),
        })
      }
    }
  }

  const reloadTechStackData = async (options?: { silent?: boolean }): Promise<void> => {
    await Promise.all([
      loadTechStackPage(options),
      loadTechStackStats(true),
    ])
  }

  const clearFormErrors = (): void => {
    Object.keys(formErrors).forEach((field) => {
      delete formErrors[field]
    })
  }

  const resetFormModel = (): void => {
    formModel.name = ''
    formModel.iconPng = ''
    formModel.officialUrl = ''
    formModel.description = ''
    formModel.color = ''
    formModel.sort = 0
    formModel.status = 'ACTIVE'
    clearFormErrors()
  }

  const refreshTechStacks = async (silent = false): Promise<void> => {
    try {
      selectedIds.value = []
      await reloadTechStackData({ silent })
      if (!silent) {
        showToast({ type: 'success', title: '技术栈数据已更新' })
      }
    } catch (error) {
      showToast({
        type: 'error',
        title: resolveErrorMessage(error, '技术栈数据加载失败'),
      })
    }
  }

  const setSearchKeyword = (keyword: string): void => {
    searchKeyword.value = keyword
    currentPage.value = 1
    selectedIds.value = []

    if (searchTimer) {
      clearTimeout(searchTimer)
    }
    searchTimer = setTimeout(() => {
      void loadTechStackPage()
    }, 300)
  }

  const setSort = (field: AdminTechStackSortField, order: AdminTechStackSortOrder): void => {
    sortField.value = field
    sortOrder.value = order
    currentPage.value = 1
    selectedIds.value = []
    void loadTechStackPage()
  }

  const openCreateEditor = (): void => {
    editorMode.value = 'create'
    editingId.value = null
    iconObjectKey.value = ''
    resetFormModel()
    editorOpen.value = true
  }

  const openEditEditor = (item: AdminTechStackItem): void => {
    editorMode.value = 'edit'
    editingId.value = item.id
    iconObjectKey.value = extractObjectKeyFromUrl(item.iconPng)
    formModel.name = item.name
    formModel.iconPng = item.iconPng || ''
    formModel.officialUrl = item.officialUrl || ''
    formModel.description = item.description || ''
    formModel.color = item.color || ''
    formModel.sort = item.sort
    formModel.status = item.status
    clearFormErrors()
    editorOpen.value = true
  }

  const closeEditor = (): void => {
    if (editorSubmitting.value) {
      return
    }
    editorOpen.value = false
  }

  const validateName = (): boolean => {
    const normalizedName = formModel.name.trim()
    if (!normalizedName) {
      formErrors.name = '请输入技术栈名称'
      return false
    }
    if (normalizedName.length > 100) {
      formErrors.name = '技术栈名称长度不能超过 100 个字符'
      return false
    }
    delete formErrors.name
    return true
  }

  const validateSort = (): boolean => {
    if (!Number.isFinite(formModel.sort) || formModel.sort < 0) {
      formErrors.sort = '排序值不能小于 0'
      return false
    }
    delete formErrors.sort
    return true
  }

  const validateUrl = (field: 'iconPng' | 'officialUrl', label: string): boolean => {
    const value = formModel[field].trim()
    if (!value) {
      delete formErrors[field]
      return true
    }
    try {
      new URL(value)
      delete formErrors[field]
      return true
    } catch {
      formErrors[field] = `${label}格式不正确`
      return false
    }
  }

  const validateColor = (): boolean => {
    const value = formModel.color.trim()
    if (!value) {
      delete formErrors.color
      return true
    }
    if (!/^#[0-9A-Fa-f]{6}$/.test(value)) {
      formErrors.color = '请输入有效的十六进制颜色值，如 #61DAFB'
      return false
    }
    delete formErrors.color
    return true
  }

  const validateForm = (): boolean => {
    const validName = validateName()
    const validSort = validateSort()
    const validIconUrl = validateUrl('iconPng', '图标 URL')
    const validOfficialUrl = validateUrl('officialUrl', '官网 URL')
    const validColor = validateColor()
    return validName && validSort && validIconUrl && validOfficialUrl && validColor
  }

  const buildCreatePayload = () => ({
    name: formModel.name.trim(),
    iconPng: normalizeOptionalText(formModel.iconPng),
    officialUrl: normalizeOptionalText(formModel.officialUrl),
    description: normalizeOptionalText(formModel.description),
    color: normalizeOptionalText(formModel.color),
    sort: formModel.sort,
  })

  const handleIconUpload = async (event: FileUploadUploaderEvent): Promise<void> => {
    const selectedFile = event.files[0]
    if (!selectedFile) {
      return
    }

    iconUploading.value = true
    try {
      if (iconObjectKey.value) {
        try {
          await deleteAdminTechStackIcon(iconObjectKey.value)
        } catch {
          // 旧图标删除失败不阻塞上传
        }
        iconObjectKey.value = ''
      }

      const uploadResult = await uploadAdminTechStackIcon(selectedFile)
      formModel.iconPng = uploadResult.iconUrl
      iconObjectKey.value = uploadResult.objectKey
    } catch (error) {
      showToast({
        type: 'error',
        title: resolveErrorMessage(error, '图标上传失败'),
      })
    } finally {
      iconUploading.value = false
    }
  }

  const handleIconClear = async (): Promise<void> => {
    if (iconObjectKey.value) {
      try {
        await deleteAdminTechStackIcon(iconObjectKey.value)
      } catch {
        // 删除失败不阻塞前端清理
      }
      iconObjectKey.value = ''
    }
    formModel.iconPng = ''
  }

  const saveItem = async (): Promise<void> => {
    if (editorSubmitting.value) {
      return
    }

    if (!validateForm()) {
      showToast({ type: 'warning', title: '请先修正表单校验错误' })
      return
    }

    editorSubmitting.value = true

    try {
      if (editorMode.value === 'create') {
        await createAdminTechStack(buildCreatePayload())
        showToast({ type: 'success', title: '技术栈创建成功' })
      } else if (editingId.value !== null) {
        const payload = {
          ...buildCreatePayload(),
          status: mapStatusToValue(formModel.status),
        }
        await updateAdminTechStack(editingId.value, payload)
        showToast({ type: 'success', title: '技术栈更新成功' })
      }

      editorOpen.value = false
      await reloadTechStackData()
    } catch (error) {
      showToast({
        type: 'error',
        title: resolveErrorMessage(
          error,
          editorMode.value === 'create' ? '创建技术栈失败' : '更新技术栈失败',
        ),
      })
    } finally {
      editorSubmitting.value = false
    }
  }

  const requestDeleteItem = (item: AdminTechStackItem): void => {
    deletingItem.value = item
    deleteDialogOpen.value = true
  }

  const closeDeleteDialog = (): void => {
    if (deleteSubmitting.value) {
      return
    }
    deleteDialogOpen.value = false
    deletingItem.value = null
  }

  const confirmDeleteItem = async (): Promise<void> => {
    if (!deletingItem.value || deleteSubmitting.value) {
      return
    }

    deleteSubmitting.value = true
    try {
      await deleteAdminTechStack(deletingItem.value.id)
      selectedIds.value = selectedIds.value.filter((id) => id !== deletingItem.value!.id)
      showToast({ type: 'success', title: '技术栈删除成功' })
      closeDeleteDialog()
      await reloadTechStackData()
    } catch (error) {
      showToast({
        type: 'error',
        title: resolveErrorMessage(error, '删除技术栈失败'),
      })
    } finally {
      deleteSubmitting.value = false
    }
  }

  const openBatchDeleteDialog = (): void => {
    if (selectedIds.value.length === 0) {
      return
    }
    batchDeleteDialogOpen.value = true
  }

  const closeBatchDeleteDialog = (): void => {
    if (batchDeleteSubmitting.value) {
      return
    }
    batchDeleteDialogOpen.value = false
  }

  const confirmBatchDelete = async (): Promise<void> => {
    if (batchDeleteSubmitting.value || selectedIds.value.length === 0) {
      return
    }

    batchDeleteSubmitting.value = true
    try {
      const deletedCount = await batchDeleteAdminTechStack({ techStackIds: selectedIds.value })
      selectedIds.value = []
      showToast({ type: 'success', title: `已删除 ${deletedCount} 个技术栈` })
      closeBatchDeleteDialog()
      await reloadTechStackData()
    } catch (error) {
      showToast({
        type: 'error',
        title: resolveErrorMessage(error, '批量删除失败'),
      })
    } finally {
      batchDeleteSubmitting.value = false
    }
  }

  const toggleStatus = async (item: AdminTechStackItem): Promise<void> => {
    const targetStatus: AdminTechStackStatusValue = item.status === 'ACTIVE' ? 0 : 1
    try {
      await updateAdminTechStackStatus(item.id, targetStatus)
      showToast({
        type: 'success',
        title: targetStatus === 1 ? '技术栈已启用' : '技术栈已禁用',
      })
      await reloadTechStackData()
    } catch (error) {
      showToast({
        type: 'error',
        title: resolveErrorMessage(error, '状态切换失败'),
      })
    }
  }

  const setSelectedIds = (ids: number[]): void => {
    selectedIds.value = ids
      .filter((id) => Number.isInteger(id) && id > 0)
      .filter((id, index, array) => array.indexOf(id) === index)
  }

  const prevPage = (): void => {
    if (currentPage.value <= 1) {
      return
    }
    currentPage.value -= 1
    selectedIds.value = []
    void loadTechStackPage()
  }

  const nextPage = (): void => {
    if (currentPage.value >= totalPages.value) {
      return
    }
    currentPage.value += 1
    selectedIds.value = []
    void loadTechStackPage()
  }

  const goToPage = (page: number | string): void => {
    if (page === '...' || typeof page !== 'number') {
      return
    }
    if (page < 1 || page > totalPages.value || page === currentPage.value) {
      return
    }
    currentPage.value = page
    selectedIds.value = []
    void loadTechStackPage()
  }

  watch(totalPages, (nextTotalPages) => {
    if (currentPage.value > nextTotalPages) {
      currentPage.value = nextTotalPages
      selectedIds.value = []
    }
  })

  onMounted(() => {
    void reloadTechStackData()
  })

  onBeforeUnmount(() => {
    if (searchTimer) {
      clearTimeout(searchTimer)
      searchTimer = null
    }
  })

  return {
    techStacks,
    currentPage,
    pageSize,
    totalItems,
    totalPages,
    visiblePages,
    searchKeyword,
    sortField,
    sortOrder,
    loading,
    editorOpen,
    editorMode,
    editorSubmitting,
    formModel,
    formErrors,
    deleteDialogOpen,
    deleteSubmitting,
    deletingItem,
    selectedIds,
    batchDeleteDialogOpen,
    batchDeleteSubmitting,
    iconUploading,
    iconObjectKey,
    stats,

    refreshTechStacks,
    setSearchKeyword,
    setSort,
    openCreateEditor,
    openEditEditor,
    closeEditor,
    saveItem,
    requestDeleteItem,
    closeDeleteDialog,
    confirmDeleteItem,
    openBatchDeleteDialog,
    closeBatchDeleteDialog,
    confirmBatchDelete,
    toggleStatus,
    setSelectedIds,
    prevPage,
    nextPage,
    goToPage,
    handleIconUpload,
    handleIconClear,
  }
}
