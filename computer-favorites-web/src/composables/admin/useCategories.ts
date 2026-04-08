import { computed, onMounted, reactive, shallowRef, watch } from 'vue'
import {
  createAdminCategory,
  deleteAdminCategory,
  getAdminCategoryTree,
  updateAdminCategory,
} from '@/api/admin-category'
import { useToast } from '@/composables/useToast'
import type {
  AdminCategoryCreatePayload,
  AdminCategoryEditPayload,
  AdminCategoryFormModel,
  AdminCategoryItem,
  AdminCategorySortField,
  AdminCategorySortOrder,
  AdminCategoryStatus,
  AdminCategoryStatusValue,
  AdminCategoryTreeNode,
} from '@/types/category'

type EditorMode = 'create' | 'edit'

const DEFAULT_PAGE_SIZE = 8

const resolveErrorMessage = (error: unknown, fallback: string): string => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallback
}

const mapStatusToView = (status: number | undefined): AdminCategoryStatus => {
  return status === 0 ? 'DISABLED' : 'ACTIVE'
}

const mapStatusToValue = (status: AdminCategoryStatus): AdminCategoryStatusValue => {
  return status === 'DISABLED' ? 0 : 1
}

const normalizeOptionalText = (value: string): string | undefined => {
  const normalizedValue = value.trim()
  return normalizedValue ? normalizedValue : undefined
}

const flattenTreeNodes = (
  nodes: AdminCategoryTreeNode[],
  depth = 0,
  output: AdminCategoryItem[] = [],
): AdminCategoryItem[] => {
  nodes.forEach((node) => {
    output.push({
      id: node.id,
      name: node.name,
      description: node.description,
      icon: node.icon,
      parentId: node.parentId === 0 ? null : node.parentId,
      sort: node.sort,
      status: mapStatusToView(node.status),
      createdAt: node.createTime || '',
      updatedAt: node.updateTime || '',
      depth,
      children: [],
    })

    if (node.children?.length) {
      flattenTreeNodes(node.children, depth + 1, output)
    }
  })
  return output
}

const findTreeNodeById = (
  nodes: AdminCategoryTreeNode[],
  categoryId: number,
): AdminCategoryTreeNode | null => {
  for (const node of nodes) {
    if (node.id === categoryId) {
      return node
    }
    if (node.children?.length) {
      const targetNode = findTreeNodeById(node.children, categoryId)
      if (targetNode) {
        return targetNode
      }
    }
  }
  return null
}

const containsNodeId = (nodes: AdminCategoryTreeNode[], categoryId: number): boolean => {
  for (const node of nodes) {
    if (node.id === categoryId) {
      return true
    }
    if (node.children?.length && containsNodeId(node.children, categoryId)) {
      return true
    }
  }
  return false
}

export function useCategories() {
  const { add: showToast } = useToast()

  const treeCategories = shallowRef<AdminCategoryTreeNode[]>([])
  const searchKeyword = shallowRef('')
  const sortField = shallowRef<AdminCategorySortField>('sort')
  const sortOrder = shallowRef<AdminCategorySortOrder>(1)
  const currentPage = shallowRef(1)
  const pageSize = shallowRef(DEFAULT_PAGE_SIZE)

  const editorOpen = shallowRef(false)
  const editorMode = shallowRef<EditorMode>('create')
  const editorSubmitting = shallowRef(false)
  const editingCategoryId = shallowRef<number | null>(null)

  const formModel = reactive<AdminCategoryFormModel>({
    name: '',
    description: '',
    icon: '',
    parentId: null,
    sort: 0,
    status: 'ACTIVE',
  })
  const formErrors = reactive<Record<string, string>>({})

  const deleteDialogOpen = shallowRef(false)
  const deleteSubmitting = shallowRef(false)
  const deletingCategory = shallowRef<AdminCategoryItem | null>(null)

  const selectedCategoryIds = shallowRef<number[]>([])

  const flatCategories = computed<AdminCategoryItem[]>(() => {
    return flattenTreeNodes(treeCategories.value)
  })

  const filteredCategories = computed(() => {
    const normalizedKeyword = searchKeyword.value.trim().toLowerCase()
    let result = [...flatCategories.value]

    if (normalizedKeyword) {
      result = result.filter((item) => {
        const nameMatched = item.name.toLowerCase().includes(normalizedKeyword)
        const descriptionMatched = (item.description || '').toLowerCase().includes(normalizedKeyword)
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

  const totalItems = computed(() => filteredCategories.value.length)

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

  const pagedCategories = computed(() => {
    const startIndex = (currentPage.value - 1) * pageSize.value
    const endIndex = startIndex + pageSize.value
    return filteredCategories.value.slice(startIndex, endIndex)
  })

  const stats = computed(() => {
    let total = 0
    let active = 0
    let disabled = 0

    flatCategories.value.forEach((item) => {
      total += 1
      if (item.status === 'ACTIVE') {
        active += 1
      } else {
        disabled += 1
      }
    })

    return { total, active, disabled }
  })

  const clearFormErrors = (): void => {
    Object.keys(formErrors).forEach((field) => {
      delete formErrors[field]
    })
  }

  const resetFormModel = (): void => {
    formModel.name = ''
    formModel.description = ''
    formModel.icon = ''
    formModel.parentId = null
    formModel.sort = 0
    formModel.status = 'ACTIVE'
    clearFormErrors()
  }

  const loadCategoryTree = async (silent = false): Promise<void> => {
    try {
      const treeList = await getAdminCategoryTree()
      treeCategories.value = treeList
      selectedCategoryIds.value = []

      if (currentPage.value > totalPages.value) {
        currentPage.value = totalPages.value
      }

      if (!silent) {
        showToast({
          type: 'success',
          title: '分类数据已更新',
        })
      }
    } catch (error) {
      treeCategories.value = []
      selectedCategoryIds.value = []
      showToast({
        type: 'error',
        title: resolveErrorMessage(error, '分类数据加载失败'),
      })
    }
  }

  const refreshCategories = async (): Promise<void> => {
    await loadCategoryTree()
  }

  const setSearchKeyword = (keyword: string): void => {
    searchKeyword.value = keyword
    currentPage.value = 1
    selectedCategoryIds.value = []
  }

  const setSort = (field: AdminCategorySortField, order: AdminCategorySortOrder): void => {
    sortField.value = field
    sortOrder.value = order
    currentPage.value = 1
    selectedCategoryIds.value = []
  }

  const openCreateEditor = (): void => {
    editorMode.value = 'create'
    editingCategoryId.value = null
    resetFormModel()
    editorOpen.value = true
  }

  const openEditEditor = (category: AdminCategoryItem): void => {
    editorMode.value = 'edit'
    editingCategoryId.value = category.id
    formModel.name = category.name
    formModel.description = category.description || ''
    formModel.icon = category.icon || ''
    formModel.parentId = category.parentId
    formModel.sort = category.sort
    formModel.status = category.status
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
      formErrors.name = '请输入分类名称'
      return false
    }
    if (normalizedName.length > 50) {
      formErrors.name = '分类名称长度不能超过 50 个字符'
      return false
    }

    const duplicateCategory = flatCategories.value.find((item) => {
      const sameParent = item.parentId === formModel.parentId
      const sameName = item.name.trim().toLowerCase() === normalizedName.toLowerCase()
      const notCurrentCategory = item.id !== editingCategoryId.value
      return sameParent && sameName && notCurrentCategory
    })

    if (duplicateCategory) {
      formErrors.name = '同级分类名称已存在'
      return false
    }

    formErrors.name = ''
    return true
  }

  const validateParent = (): boolean => {
    if (formModel.parentId === null) {
      formErrors.parentId = ''
      return true
    }

    if (editingCategoryId.value !== null && formModel.parentId === editingCategoryId.value) {
      formErrors.parentId = '父分类不能是当前分类'
      return false
    }

    const parentCategory = flatCategories.value.find((item) => item.id === formModel.parentId)
    if (!parentCategory) {
      formErrors.parentId = '父分类不存在'
      return false
    }

    if (parentCategory.status !== 'ACTIVE') {
      formErrors.parentId = '父分类已禁用，无法挂载'
      return false
    }

    if (editingCategoryId.value !== null) {
      const editingNode = findTreeNodeById(treeCategories.value, editingCategoryId.value)
      if (editingNode && containsNodeId(editingNode.children || [], formModel.parentId)) {
        formErrors.parentId = '父子关系不合法，请重新选择父分类'
        return false
      }
    }

    formErrors.parentId = ''
    return true
  }

  const validateSort = (): boolean => {
    if (!Number.isFinite(formModel.sort) || formModel.sort < 0) {
      formErrors.sort = '排序值不能小于 0'
      return false
    }
    formErrors.sort = ''
    return true
  }

  const validateForm = (): boolean => {
    const validName = validateName()
    const validParent = validateParent()
    const validSort = validateSort()
    return validName && validParent && validSort
  }

  const buildCreatePayload = (): AdminCategoryCreatePayload => {
    return {
      name: formModel.name.trim(),
      description: normalizeOptionalText(formModel.description),
      icon: normalizeOptionalText(formModel.icon),
      parentId: formModel.parentId ?? 0,
      sort: formModel.sort,
    }
  }

  const buildEditPayload = (): AdminCategoryEditPayload => {
    return {
      ...buildCreatePayload(),
      status: mapStatusToValue(formModel.status),
    }
  }

  const saveCategory = async (): Promise<void> => {
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

    try {
      if (editorMode.value === 'create') {
        await createAdminCategory(buildCreatePayload())
        showToast({ type: 'success', title: '分类创建成功' })
      } else if (editingCategoryId.value !== null) {
        await updateAdminCategory(editingCategoryId.value, buildEditPayload())
        showToast({ type: 'success', title: '分类更新成功' })
      }

      editorOpen.value = false
      await loadCategoryTree(true)
    } catch (error) {
      showToast({
        type: 'error',
        title: resolveErrorMessage(
          error,
          editorMode.value === 'create' ? '创建分类失败' : '更新分类失败',
        ),
      })
    } finally {
      editorSubmitting.value = false
    }
  }

  const requestDeleteCategory = (category: AdminCategoryItem): void => {
    deletingCategory.value = category
    deleteDialogOpen.value = true
  }

  const closeDeleteDialog = (): void => {
    if (deleteSubmitting.value) {
      return
    }
    deleteDialogOpen.value = false
    deletingCategory.value = null
  }

  const confirmDeleteCategory = async (): Promise<void> => {
    if (!deletingCategory.value || deleteSubmitting.value) {
      return
    }

    deleteSubmitting.value = true
    try {
      await deleteAdminCategory(deletingCategory.value.id)
      showToast({ type: 'success', title: '分类删除成功' })
      closeDeleteDialog()
      await loadCategoryTree(true)
    } catch (error) {
      showToast({
        type: 'error',
        title: resolveErrorMessage(error, '删除分类失败'),
      })
    } finally {
      deleteSubmitting.value = false
    }
  }

  const setSelectedCategoryIds = (ids: number[]): void => {
    selectedCategoryIds.value = ids
      .filter((id) => Number.isInteger(id) && id > 0)
      .filter((id, index, array) => array.indexOf(id) === index)
  }

  const prevPage = (): void => {
    if (currentPage.value <= 1) {
      return
    }
    currentPage.value -= 1
    selectedCategoryIds.value = []
  }

  const nextPage = (): void => {
    if (currentPage.value >= totalPages.value) {
      return
    }
    currentPage.value += 1
    selectedCategoryIds.value = []
  }

  const goToPage = (page: number | string): void => {
    if (page === '...' || typeof page !== 'number') {
      return
    }
    if (page < 1 || page > totalPages.value || page === currentPage.value) {
      return
    }
    currentPage.value = page
    selectedCategoryIds.value = []
  }

  watch(totalPages, (nextTotalPages) => {
    if (currentPage.value > nextTotalPages) {
      currentPage.value = nextTotalPages
      selectedCategoryIds.value = []
    }
  })

  onMounted(() => {
    void loadCategoryTree(true)
  })

  return {
    categories: pagedCategories,
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
    deletingCategory,
    selectedCategoryIds,
    stats,
    allCategories: flatCategories,

    refreshCategories,
    setSearchKeyword,
    setSort,
    openCreateEditor,
    openEditEditor,
    closeEditor,
    saveCategory,
    requestDeleteCategory,
    closeDeleteDialog,
    confirmDeleteCategory,
    setSelectedCategoryIds,
    prevPage,
    nextPage,
    goToPage,
  }
}
