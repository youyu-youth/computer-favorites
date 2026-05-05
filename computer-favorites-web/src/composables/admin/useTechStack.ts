import { computed, onMounted, reactive, shallowRef, watch } from 'vue'
import { useToast } from '@/composables/useToast'
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

const MOCK_TECH_STACKS: AdminTechStackItem[] = [
  {
    id: 1,
    name: 'Java',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg',
    officialUrl: 'https://www.java.com',
    description: '广泛使用的面向对象编程语言，企业级开发首选',
    color: '#ED8B00',
    status: 'ACTIVE',
    sort: 1,
    createdAt: '2025-01-10 09:00:00',
    updatedAt: '2025-03-15 14:30:00',
    userCount: 128,
  },
  {
    id: 2,
    name: 'Spring Boot',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg',
    officialUrl: 'https://spring.io/projects/spring-boot',
    description: '基于 Spring 的快速开发框架，简化企业应用开发',
    color: '#6DB33F',
    status: 'ACTIVE',
    sort: 2,
    createdAt: '2025-01-10 09:05:00',
    updatedAt: '2025-03-15 14:30:00',
    userCount: 96,
  },
  {
    id: 3,
    name: 'Vue.js',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/vuejs/vuejs-original.svg',
    officialUrl: 'https://vuejs.org',
    description: '渐进式 JavaScript 框架，用于构建用户界面',
    color: '#4FC08D',
    status: 'ACTIVE',
    sort: 3,
    createdAt: '2025-01-12 10:00:00',
    updatedAt: '2025-02-20 11:00:00',
    userCount: 85,
  },
  {
    id: 4,
    name: 'React',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/react/react-original.svg',
    officialUrl: 'https://react.dev',
    description: '用于构建用户界面的 JavaScript 库',
    color: '#61DAFB',
    status: 'ACTIVE',
    sort: 4,
    createdAt: '2025-01-12 10:05:00',
    updatedAt: '2025-02-20 11:00:00',
    userCount: 72,
  },
  {
    id: 5,
    name: 'Python',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/python/python-original.svg',
    officialUrl: 'https://www.python.org',
    description: '简洁优雅的通用编程语言，AI/数据科学首选',
    color: '#3776AB',
    status: 'ACTIVE',
    sort: 5,
    createdAt: '2025-01-15 08:00:00',
    updatedAt: '2025-03-01 16:00:00',
    userCount: 110,
  },
  {
    id: 6,
    name: 'Docker',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg',
    officialUrl: 'https://www.docker.com',
    description: '容器化平台，简化应用部署和运维',
    color: '#2496ED',
    status: 'ACTIVE',
    sort: 6,
    createdAt: '2025-01-20 09:00:00',
    updatedAt: '2025-02-28 10:00:00',
    userCount: 64,
  },
  {
    id: 7,
    name: 'MySQL',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg',
    officialUrl: 'https://www.mysql.com',
    description: '最流行的开源关系型数据库管理系统',
    color: '#4479A1',
    status: 'ACTIVE',
    sort: 7,
    createdAt: '2025-02-01 10:00:00',
    updatedAt: '2025-03-10 15:00:00',
    userCount: 88,
  },
  {
    id: 8,
    name: 'Redis',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/redis/redis-original.svg',
    officialUrl: 'https://redis.io',
    description: '高性能内存键值数据库，常用于缓存和消息队列',
    color: '#DC382D',
    status: 'ACTIVE',
    sort: 8,
    createdAt: '2025-02-01 10:10:00',
    updatedAt: '2025-03-10 15:00:00',
    userCount: 56,
  },
  {
    id: 9,
    name: 'Go',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/go/go-original-wordmark.svg',
    officialUrl: 'https://go.dev',
    description: 'Google 推出的静态编译型语言，高并发场景首选',
    color: '#00ADD8',
    status: 'DISABLED',
    sort: 9,
    createdAt: '2025-02-10 11:00:00',
    updatedAt: '2025-03-20 09:00:00',
    userCount: 34,
  },
  {
    id: 10,
    name: 'TypeScript',
    iconPng: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/typescript/typescript-original.svg',
    officialUrl: 'https://www.typescriptlang.org',
    description: 'JavaScript 的超集，添加静态类型支持',
    color: '#3178C6',
    status: 'DISABLED',
    sort: 10,
    createdAt: '2025-02-10 11:05:00',
    updatedAt: '2025-03-20 09:00:00',
    userCount: 45,
  },
]

let nextId = 11

export function useTechStack() {
  const { add: showToast } = useToast()

  const allTechStacks = shallowRef<AdminTechStackItem[]>([...MOCK_TECH_STACKS])
  const searchKeyword = shallowRef('')
  const sortField = shallowRef<AdminTechStackSortField>('sort')
  const sortOrder = shallowRef<AdminTechStackSortOrder>(1)
  const currentPage = shallowRef(1)
  const pageSize = shallowRef(DEFAULT_PAGE_SIZE)

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

  const filteredTechStacks = computed(() => {
    const normalizedKeyword = searchKeyword.value.trim().toLowerCase()
    let result = [...allTechStacks.value]

    if (normalizedKeyword) {
      result = result.filter((item) => {
        const nameMatched = item.name.toLowerCase().includes(normalizedKeyword)
        const descriptionMatched = item.description.toLowerCase().includes(normalizedKeyword)
        return nameMatched || descriptionMatched
      })
    }

    result.sort((leftItem, rightItem) => {
      const leftValue = leftItem[sortField.value]
      const rightValue = rightItem[sortField.value]
      const orderValue = sortOrder.value

      if (leftValue == null) {
        return -1 * orderValue
      }
      if (rightValue == null) {
        return 1 * orderValue
      }
      if (leftValue < rightValue) {
        return -1 * orderValue
      }
      if (leftValue > rightValue) {
        return 1 * orderValue
      }
      return 0
    })

    return result
  })

  const totalItems = computed(() => filteredTechStacks.value.length)

  const totalPages = computed(() => {
    return Math.max(1, Math.ceil(totalItems.value / pageSize.value))
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

  const pagedTechStacks = computed(() => {
    const startIndex = (currentPage.value - 1) * pageSize.value
    const endIndex = startIndex + pageSize.value
    return filteredTechStacks.value.slice(startIndex, endIndex)
  })

  const stats = computed<AdminTechStackStats>(() => {
    let total = 0
    let enabled = 0
    let disabled = 0

    allTechStacks.value.forEach((item) => {
      total += 1
      if (item.status === 'ACTIVE') {
        enabled += 1
      } else {
        disabled += 1
      }
    })

    return { total, enabled, disabled }
  })

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
      if (currentPage.value > totalPages.value) {
        currentPage.value = totalPages.value
      }
      if (!silent) {
        showToast({ type: 'success', title: '技术栈数据已更新' })
      }
    } catch (error) {
      selectedIds.value = []
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
  }

  const setSort = (field: AdminTechStackSortField, order: AdminTechStackSortOrder): void => {
    sortField.value = field
    sortOrder.value = order
    currentPage.value = 1
    selectedIds.value = []
  }

  const openCreateEditor = (): void => {
    editorMode.value = 'create'
    editingId.value = null
    resetFormModel()
    editorOpen.value = true
  }

  const openEditEditor = (item: AdminTechStackItem): void => {
    editorMode.value = 'edit'
    editingId.value = item.id
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

    const duplicateItem = allTechStacks.value.find((item) => {
      const sameName = item.name.trim().toLowerCase() === normalizedName.toLowerCase()
      const notCurrentItem = item.id !== editingId.value
      return sameName && notCurrentItem
    })

    if (duplicateItem) {
      formErrors.name = '技术栈名称已存在'
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
      const now = new Date().toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      }).replace(/\//g, '-')

      if (editorMode.value === 'create') {
        const newItem: AdminTechStackItem = {
          id: nextId++,
          name: formModel.name.trim(),
          iconPng: normalizeOptionalText(formModel.iconPng) || '',
          officialUrl: normalizeOptionalText(formModel.officialUrl) || '',
          description: formModel.description.trim(),
          color: formModel.color.trim(),
          status: 'ACTIVE',
          sort: formModel.sort,
          createdAt: now,
          updatedAt: now,
          userCount: 0,
        }
        allTechStacks.value = [newItem, ...allTechStacks.value]
        showToast({ type: 'success', title: '技术栈创建成功' })
      } else if (editingId.value !== null) {
        allTechStacks.value = allTechStacks.value.map((item) => {
          if (item.id !== editingId.value) {
            return item
          }
          return {
            ...item,
            name: formModel.name.trim(),
            iconPng: normalizeOptionalText(formModel.iconPng) || '',
            officialUrl: normalizeOptionalText(formModel.officialUrl) || '',
            description: formModel.description.trim(),
            color: formModel.color.trim(),
            sort: formModel.sort,
            status: formModel.status,
            updatedAt: now,
          }
        })
        showToast({ type: 'success', title: '技术栈更新成功' })
      }

      editorOpen.value = false
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
      allTechStacks.value = allTechStacks.value.filter(
        (item) => item.id !== deletingItem.value!.id,
      )
      selectedIds.value = selectedIds.value.filter((id) => id !== deletingItem.value!.id)
      showToast({ type: 'success', title: '技术栈删除成功' })
      closeDeleteDialog()
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
      const idsToDelete = new Set(selectedIds.value)
      allTechStacks.value = allTechStacks.value.filter((item) => !idsToDelete.has(item.id))
      const deletedCount = idsToDelete.size
      selectedIds.value = []
      showToast({ type: 'success', title: `已删除 ${deletedCount} 个技术栈` })
      closeBatchDeleteDialog()
    } catch (error) {
      showToast({
        type: 'error',
        title: resolveErrorMessage(error, '批量删除失败'),
      })
    } finally {
      batchDeleteSubmitting.value = false
    }
  }

  const toggleStatus = (item: AdminTechStackItem): void => {
    const newStatus: AdminTechStackStatus = item.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
    allTechStacks.value = allTechStacks.value.map((stack) => {
      if (stack.id !== item.id) {
        return stack
      }
      return { ...stack, status: newStatus }
    })
    showToast({
      type: 'success',
      title: newStatus === 'ACTIVE' ? '技术栈已启用' : '技术栈已禁用',
    })
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
  }

  const nextPage = (): void => {
    if (currentPage.value >= totalPages.value) {
      return
    }
    currentPage.value += 1
    selectedIds.value = []
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
  }

  watch(totalPages, (nextTotalPages) => {
    if (currentPage.value > nextTotalPages) {
      currentPage.value = nextTotalPages
      selectedIds.value = []
    }
  })

  onMounted(() => {
    void refreshTechStacks(true)
  })

  return {
    techStacks: pagedTechStacks,
    currentPage,
    pageSize,
    totalItems,
    totalPages,
    visiblePages,
    searchKeyword,
    sortField,
    sortOrder,
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
  }
}
