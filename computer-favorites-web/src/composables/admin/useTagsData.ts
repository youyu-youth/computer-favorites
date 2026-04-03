import { computed, reactive, shallowRef, watch } from 'vue'
import { useToast } from '@/composables/useToast'
import type {
  AdminTagEditorMode,
  AdminTagFormErrors,
  AdminTagFormModel,
  AdminTagItem,
  AdminTagSortField,
  AdminTagSortOrder,
  AdminTagStats,
} from '@/types/admin-tag'

const DEFAULT_PAGE_SIZE = 8
const DEFAULT_COLOR = '#E95322'
const COLOR_HEX_PATTERN = /^#([0-9A-Fa-f]{6})$/

const COLOR_PALETTE = [
  '#E95322',
  '#0EA5E9',
  '#10B981',
  '#F59E0B',
  '#EF4444',
  '#6366F1',
  '#14B8A6',
  '#84CC16',
]

const now = (): string => {
  return new Date().toISOString()
}

const createMockTags = (): AdminTagItem[] => {
  return [
    {
      id: 1,
      name: 'Vue 3',
      color: '#10B981',
      useCount: 34,
      createTime: '2026-03-01T09:10:00.000Z',
      updateTime: '2026-04-02T10:30:00.000Z',
      deleted: 0,
    },
    {
      id: 2,
      name: 'TypeScript',
      color: '#0EA5E9',
      useCount: 26,
      createTime: '2026-03-02T08:20:00.000Z',
      updateTime: '2026-04-03T08:55:00.000Z',
      deleted: 0,
    },
    {
      id: 3,
      name: 'Spring Boot',
      color: '#84CC16',
      useCount: 19,
      createTime: '2026-03-05T05:10:00.000Z',
      updateTime: '2026-04-01T07:20:00.000Z',
      deleted: 0,
    },
    {
      id: 4,
      name: 'MyBatis Plus',
      color: '#F59E0B',
      useCount: 14,
      createTime: '2026-03-08T12:30:00.000Z',
      updateTime: '2026-03-29T16:10:00.000Z',
      deleted: 0,
    },
    {
      id: 5,
      name: 'PrimeVue',
      color: '#6366F1',
      useCount: 22,
      createTime: '2026-03-12T02:40:00.000Z',
      updateTime: '2026-04-03T03:25:00.000Z',
      deleted: 0,
    },
    {
      id: 6,
      name: 'Sa-Token',
      color: '#EF4444',
      useCount: 11,
      createTime: '2026-03-16T11:10:00.000Z',
      updateTime: '2026-03-30T12:50:00.000Z',
      deleted: 0,
    },
    {
      id: 7,
      name: 'Redis',
      color: '#14B8A6',
      useCount: 17,
      createTime: '2026-03-17T08:40:00.000Z',
      updateTime: '2026-04-02T01:35:00.000Z',
      deleted: 0,
    },
    {
      id: 8,
      name: 'MinIO',
      color: '#E95322',
      useCount: 7,
      createTime: '2026-03-20T03:15:00.000Z',
      updateTime: '2026-03-31T06:45:00.000Z',
      deleted: 0,
    },
    {
      id: 9,
      name: 'Playwright',
      color: '#0EA5E9',
      useCount: 9,
      createTime: '2026-03-23T09:30:00.000Z',
      updateTime: '2026-04-03T05:05:00.000Z',
      deleted: 0,
    },
    {
      id: 10,
      name: 'Tailwind CSS',
      color: '#10B981',
      useCount: 28,
      createTime: '2026-03-26T07:55:00.000Z',
      updateTime: '2026-04-02T17:15:00.000Z',
      deleted: 0,
    },
  ]
}

const resolveTimestamp = (value: string): number => {
  const timestamp = new Date(value).getTime()
  if (Number.isNaN(timestamp)) {
    return 0
  }
  return timestamp
}

const isToday = (value: string): boolean => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return false
  }
  const nowDate = new Date()
  return (
    date.getFullYear() === nowDate.getFullYear()
    && date.getMonth() === nowDate.getMonth()
    && date.getDate() === nowDate.getDate()
  )
}

const normalizeName = (name: string): string => {
  return name.trim().toLowerCase()
}

export function useTagsData() {
  const { add: showToast } = useToast()

  const tags = shallowRef<AdminTagItem[]>(createMockTags())
  const searchKeyword = shallowRef('')
  const currentPage = shallowRef(1)
  const pageSize = shallowRef(DEFAULT_PAGE_SIZE)
  const sortField = shallowRef<AdminTagSortField>('updateTime')
  const sortOrder = shallowRef<AdminTagSortOrder>(-1)

  const editorOpen = shallowRef(false)
  const editorSubmitting = shallowRef(false)
  const editorMode = shallowRef<AdminTagEditorMode>('create')
  const editingTagId = shallowRef<number | null>(null)

  const deleteDialogOpen = shallowRef(false)
  const deleteSubmitting = shallowRef(false)
  const deletingTag = shallowRef<AdminTagItem | null>(null)

  const formModel = reactive<AdminTagFormModel>({
    name: '',
    color: DEFAULT_COLOR,
  })

  const formErrors = reactive<AdminTagFormErrors>({
    name: '',
    color: '',
  })

  const colorPalette = shallowRef<string[]>([...COLOR_PALETTE])

  const activeTags = computed(() => {
    return tags.value.filter((item) => item.deleted === 0)
  })

  const normalizedSearchKeyword = computed(() => {
    return searchKeyword.value.trim().toLowerCase()
  })

  const filteredTags = computed(() => {
    if (!normalizedSearchKeyword.value) {
      return activeTags.value
    }
    return activeTags.value.filter((item) => {
      return normalizeName(item.name).includes(normalizedSearchKeyword.value)
    })
  })

  const sortedTags = computed(() => {
    const list = [...filteredTags.value]
    const order = sortOrder.value
    const field = sortField.value

    list.sort((left, right) => {
      if (field === 'id' || field === 'useCount') {
        const leftValue = Number(left[field])
        const rightValue = Number(right[field])
        return order === 1 ? leftValue - rightValue : rightValue - leftValue
      }

      if (field === 'createTime' || field === 'updateTime') {
        const leftValue = resolveTimestamp(left[field])
        const rightValue = resolveTimestamp(right[field])
        return order === 1 ? leftValue - rightValue : rightValue - leftValue
      }

      const leftValue = String(left[field] || '')
      const rightValue = String(right[field] || '')
      const compared = leftValue.localeCompare(rightValue, 'zh-CN', {
        sensitivity: 'base',
      })
      return order === 1 ? compared : -compared
    })

    return list
  })

  const totalItems = computed(() => sortedTags.value.length)

  const totalPages = computed(() => {
    if (totalItems.value <= 0) {
      return 1
    }
    return Math.ceil(totalItems.value / pageSize.value)
  })

  const pagedTags = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return sortedTags.value.slice(start, end)
  })

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

  const stats = computed<AdminTagStats>(() => {
    const inUse = activeTags.value.filter((item) => item.useCount > 0).length
    const updatedToday = activeTags.value.filter((item) => isToday(item.updateTime)).length
    return {
      total: activeTags.value.length,
      inUse,
      unused: activeTags.value.length - inUse,
      updatedToday,
    }
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
    sortField.value = field
    sortOrder.value = order
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

    const duplicated = activeTags.value.some((item) => {
      if (editingTagId.value !== null && item.id === editingTagId.value) {
        return false
      }
      return normalizeName(item.name) === normalizeName(name)
    })

    if (duplicated) {
      formErrors.name = '标签名称已存在，请使用其他名称'
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

  const buildTagPayload = (): Pick<AdminTagItem, 'name' | 'color'> => {
    return {
      name: formModel.name.trim(),
      color: formModel.color.trim().toUpperCase(),
    }
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
        const nextId = tags.value.reduce((max, item) => Math.max(max, item.id), 0) + 1
        const timestamp = now()
        tags.value = [
          {
            id: nextId,
            name: payload.name,
            color: payload.color,
            useCount: 0,
            createTime: timestamp,
            updateTime: timestamp,
            deleted: 0,
          },
          ...tags.value,
        ]
        showToast({ type: 'success', title: '标签创建成功' })
      } else if (editingTagId.value !== null) {
        const timestamp = now()
        tags.value = tags.value.map((item) => {
          if (item.id !== editingTagId.value) {
            return item
          }
          return {
            ...item,
            name: payload.name,
            color: payload.color,
            updateTime: timestamp,
          }
        })
        showToast({ type: 'success', title: '标签更新成功' })
      }

      editorOpen.value = false
    } finally {
      editorSubmitting.value = false
    }
  }

  const refreshMockData = (): void => {
    showToast({
      type: 'info',
      title: 'Mock 数据已是最新状态',
    })
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
      tags.value = tags.value.map((item) => {
        if (item.id !== targetId) {
          return item
        }
        return {
          ...item,
          deleted: 1,
          updateTime: now(),
        }
      })

      showToast({ type: 'success', title: '标签已删除' })
      closeDeleteDialog()
    } finally {
      deleteSubmitting.value = false
    }
  }

  const prevPage = (): void => {
    if (currentPage.value <= 1) {
      return
    }
    currentPage.value -= 1
  }

  const nextPage = (): void => {
    if (currentPage.value >= totalPages.value) {
      return
    }
    currentPage.value += 1
  }

  const goToPage = (page: number | string): void => {
    if (page === '...' || typeof page !== 'number') {
      return
    }
    if (page < 1 || page > totalPages.value) {
      return
    }
    currentPage.value = page
  }

  watch(searchKeyword, () => {
    currentPage.value = 1
  })

  watch(totalPages, (nextValue) => {
    if (currentPage.value > nextValue) {
      currentPage.value = nextValue
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
    colorPalette,
    pagedTags,
    totalItems,
    totalPages,
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
    prevPage,
    nextPage,
    goToPage,
  }
}
