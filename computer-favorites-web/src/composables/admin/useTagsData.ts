import { computed, onBeforeUnmount, onMounted, reactive, shallowRef, watch } from 'vue'
import {
  batchDeleteAdminTag,
  createAdminTag,
  deleteAdminTag,
  getAdminTagPage,
  getAdminTagStats,
  updateAdminTag,
} from '@/api/admin-tag'
import { useToast } from '@/composables/useToast'
import type {
  AdminTagCreatePayload,
  AdminTagEditorMode,
  AdminTagFormErrors,
  AdminTagFormModel,
  AdminTagItem,
  AdminTagPage,
  AdminTagSortField,
  AdminTagSortOrder,
  AdminTagStats,
} from '@/types/admin-tag'

const DEFAULT_PAGE_SIZE = 8
const DEFAULT_COLOR = '#E95322'
const COLOR_HEX_PATTERN = /^#([0-9A-Fa-f]{6})$/
const SEARCH_DEBOUNCE_MS = 300

type LoadTagPageOptions = {
  silent?: boolean
}

type ReloadTagDataOptions = {
  silentListLoading?: boolean
}

const resolveErrorMessage = (error: unknown, fallback: string): string => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallback
}

export function useTagsData() {
  const { add: showToast } = useToast()

  const tags = shallowRef<AdminTagItem[]>([])
  const searchKeyword = shallowRef('')
  const currentPage = shallowRef(1)
  const pageSize = shallowRef(DEFAULT_PAGE_SIZE)
  const sortField = shallowRef<AdminTagSortField>('updateTime')
  const sortOrder = shallowRef<AdminTagSortOrder>(-1)
  const totalItems = shallowRef(0)
  const totalPages = shallowRef(1)
  const loading = shallowRef(false)

  const editorOpen = shallowRef(false)
  const editorSubmitting = shallowRef(false)
  const editorMode = shallowRef<AdminTagEditorMode>('create')
  const editingTagId = shallowRef<number | null>(null)

  const deleteDialogOpen = shallowRef(false)
  const deleteSubmitting = shallowRef(false)
  const deletingTag = shallowRef<AdminTagItem | null>(null)
  const selectedTagIds = shallowRef<number[]>([])
  const batchDeleteSubmitting = shallowRef(false)

  const stats = shallowRef<AdminTagStats>({
    total: 0,
    inUse: 0,
    unused: 0,
    updatedToday: 0,
  })

  const formModel = reactive<AdminTagFormModel>({
    name: '',
    color: DEFAULT_COLOR,
  })

  const formErrors = reactive<AdminTagFormErrors>({
    name: '',
    color: '',
  })

  const selectedTagCount = computed(() => selectedTagIds.value.length)

  let listRequestId = 0
  let searchTimer: ReturnType<typeof setTimeout> | null = null

  const pagedTags = computed(() => tags.value)

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

  const clearFormErrors = (): void => {
    formErrors.name = ''
    formErrors.color = ''
  }

  const resetFormModel = (): void => {
    formModel.name = ''
    formModel.color = DEFAULT_COLOR
    clearFormErrors()
  }

  const setSearchKeyword = (value: string): void => {
    searchKeyword.value = value
  }

  const setSort = (field: AdminTagSortField, order: AdminTagSortOrder): void => {
    if (sortField.value === field && sortOrder.value === order) {
      return
    }
    sortField.value = field
    sortOrder.value = order
    currentPage.value = 1
    selectedTagIds.value = []
    void loadTagPage()
  }

  const openCreateEditor = (): void => {
    editorMode.value = 'create'
    editingTagId.value = null
    resetFormModel()
    editorOpen.value = true
  }

  const openEditEditor = (tag: AdminTagItem): void => {
    editorMode.value = 'edit'
    editingTagId.value = tag.id
    formModel.name = tag.name
    formModel.color = tag.color
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
    const name = formModel.name.trim()
    if (!name) {
      formErrors.name = '标签名称不能为空'
      return false
    }
    if (name.length > 50) {
      formErrors.name = '标签名称长度不能超过 50 个字符'
      return false
    }

    formErrors.name = ''
    return true
  }

  const validateColor = (): boolean => {
    const color = formModel.color.trim()
    if (!COLOR_HEX_PATTERN.test(color)) {
      formErrors.color = '颜色格式应为 #RRGGBB'
      return false
    }
    formErrors.color = ''
    return true
  }

  const validateForm = (): boolean => {
    const validName = validateName()
    const validColor = validateColor()
    return validName && validColor
  }

  const buildTagPayload = (): AdminTagCreatePayload => {
    return {
      name: formModel.name.trim(),
      color: formModel.color.trim().toUpperCase(),
    }
  }

  const applyTagPage = (pageData: AdminTagPage): void => {
    tags.value = pageData.records || []
    totalItems.value = Number(pageData.total || 0)
    pageSize.value = Number(pageData.pageSize || DEFAULT_PAGE_SIZE)
    const backendTotalPages = Number(pageData.totalPages || 0)
    totalPages.value = backendTotalPages > 0 ? backendTotalPages : 1
  }

  const loadTagPage = async (options?: LoadTagPageOptions): Promise<void> => {
    const silent = options?.silent === true
    listRequestId += 1
    const requestId = listRequestId

    if (!silent) {
      loading.value = true
    }

    try {
      const pageData = await getAdminTagPage({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        keyword: searchKeyword.value,
        sortField: sortField.value,
        sortOrder: sortOrder.value,
      })

      if (requestId !== listRequestId) {
        return
      }

      applyTagPage(pageData)
      selectedTagIds.value = []

      if (totalItems.value > 0 && currentPage.value > totalPages.value) {
        currentPage.value = totalPages.value
        await loadTagPage({ silent: true })
      }
    } catch (error) {
      if (requestId !== listRequestId) {
        return
      }

      tags.value = []
      totalItems.value = 0
      totalPages.value = 1
      selectedTagIds.value = []

      if (!silent) {
        showToast({
          type: 'error',
          title: resolveErrorMessage(error, '标签列表加载失败'),
        })
      }
    } finally {
      if (requestId === listRequestId && !silent) {
        loading.value = false
      }
    }
  }

  const loadTagStats = async (silentErrorToast = false): Promise<void> => {
    try {
      stats.value = await getAdminTagStats()
    } catch (error) {
      if (!silentErrorToast) {
        showToast({
          type: 'error',
          title: resolveErrorMessage(error, '标签统计加载失败'),
        })
      }
    }
  }

  const reloadTagData = async (options?: ReloadTagDataOptions): Promise<void> => {
    await Promise.all([
      loadTagStats(true),
      loadTagPage({ silent: options?.silentListLoading === true }),
    ])
  }

  const saveTag = async (): Promise<void> => {
    if (editorSubmitting.value) {
      return
    }

    if (!validateForm()) {
      showToast({
        type: 'warning',
        title: '请先修正表单校验错误',
      })
      return
    }

    editorSubmitting.value = true
    const payload = buildTagPayload()

    try {
      if (editorMode.value === 'create') {
        await createAdminTag(payload)
        showToast({ type: 'success', title: '标签创建成功' })
      } else if (editingTagId.value !== null) {
        await updateAdminTag(editingTagId.value, payload)
        showToast({ type: 'success', title: '标签更新成功' })
      }

      editorOpen.value = false
      await reloadTagData({ silentListLoading: true })
    } catch (error) {
      showToast({
        type: 'error',
        title: resolveErrorMessage(error, editorMode.value === 'create' ? '创建标签失败' : '更新标签失败'),
      })
    } finally {
      editorSubmitting.value = false
    }
  }

  const refreshMockData = async (): Promise<void> => {
    await reloadTagData()
    showToast({ type: 'success', title: '标签数据已刷新' })
  }

  const requestDeleteTag = (tag: AdminTagItem): void => {
    deletingTag.value = tag
    deleteDialogOpen.value = true
  }

  const closeDeleteDialog = (): void => {
    if (deleteSubmitting.value) {
      return
    }
    deleteDialogOpen.value = false
    deletingTag.value = null
  }

  const confirmDeleteTag = async (): Promise<void> => {
    if (!deletingTag.value || deleteSubmitting.value) {
      return
    }

    deleteSubmitting.value = true
    const targetId = deletingTag.value.id

    try {
      await deleteAdminTag(targetId)
      showToast({ type: 'success', title: '标签已删除' })
      closeDeleteDialog()
      await reloadTagData({ silentListLoading: true })
    } catch (error) {
      showToast({
        type: 'error',
        title: resolveErrorMessage(error, '删除标签失败'),
      })
    } finally {
      deleteSubmitting.value = false
    }
  }

  const setSelectedTagIds = (tagIds: number[]): void => {
    selectedTagIds.value = tagIds
      .filter((id) => Number.isInteger(id) && id > 0)
      .filter((id, index, array) => array.indexOf(id) === index)
  }

  const batchDeleteSelectedTags = async (): Promise<void> => {
    if (batchDeleteSubmitting.value) {
      return
    }

    if (selectedTagIds.value.length === 0) {
      showToast({ type: 'warning', title: '请先勾选要删除的标签' })
      return
    }

    batchDeleteSubmitting.value = true
    try {
      const deletedCount = await batchDeleteAdminTag({
        tagIds: selectedTagIds.value,
      })
      showToast({
        type: 'success',
        title: '批量删除成功',
        description: `已删除 ${deletedCount} 个标签`,
      })
      selectedTagIds.value = []
      await reloadTagData({ silentListLoading: true })
    } catch (error) {
      showToast({
        type: 'error',
        title: resolveErrorMessage(error, '批量删除失败'),
      })
    } finally {
      batchDeleteSubmitting.value = false
    }
  }

  const prevPage = async (): Promise<void> => {
    if (currentPage.value <= 1) {
      return
    }
    currentPage.value -= 1
    await loadTagPage()
  }

  const nextPage = async (): Promise<void> => {
    if (currentPage.value >= totalPages.value) {
      return
    }
    currentPage.value += 1
    await loadTagPage()
  }

  const goToPage = async (page: number | string): Promise<void> => {
    if (page === '...' || typeof page !== 'number') {
      return
    }
    if (page < 1 || page > totalPages.value || page === currentPage.value) {
      return
    }
    currentPage.value = page
    await loadTagPage()
  }

  watch(searchKeyword, () => {
    currentPage.value = 1
    selectedTagIds.value = []
    if (searchTimer) {
      clearTimeout(searchTimer)
    }
    searchTimer = setTimeout(() => {
      void loadTagPage()
    }, SEARCH_DEBOUNCE_MS)
  })

  watch(totalPages, (nextValue) => {
    if (currentPage.value > nextValue) {
      currentPage.value = nextValue
    }
  })

  onMounted(() => {
    void reloadTagData()
  })

  onBeforeUnmount(() => {
    if (searchTimer) {
      clearTimeout(searchTimer)
      searchTimer = null
    }
  })

  return {
    searchKeyword,
    currentPage,
    pageSize,
    sortField,
    sortOrder,
    editorOpen,
    editorSubmitting,
    editorMode,
    formModel,
    formErrors,
    deleteDialogOpen,
    deleteSubmitting,
    deletingTag,
    selectedTagIds,
    selectedTagCount,
    batchDeleteSubmitting,
    pagedTags,
    totalItems,
    totalPages,
    loading,
    visiblePages,
    stats,
    setSearchKeyword,
    setSort,
    openCreateEditor,
    openEditEditor,
    closeEditor,
    saveTag,
    refreshMockData,
    requestDeleteTag,
    closeDeleteDialog,
    confirmDeleteTag,
    setSelectedTagIds,
    batchDeleteSelectedTags,
    prevPage,
    nextPage,
    goToPage,
  }
}
