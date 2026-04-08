import { ref, computed, reactive } from 'vue'
import type {
  AdminCategoryItem,
  AdminCategoryFormModel,
  AdminCategorySortField,
  AdminCategorySortOrder,
} from '@/types/category'

type EditorMode = 'create' | 'edit'

export function useCategories() {
  const categories = ref<AdminCategoryItem[]>([
    {
      id: 1,
      name: '前端开发',
      description: '前端相关技术框架',
      icon: 'fas fa-code',
      parentId: null,
      sort: 1,
      status: 'ACTIVE',
      createdAt: '2023-01-01T10:00:00Z',
      updatedAt: '2023-01-01T10:00:00Z',
      children: [
        {
          id: 11,
          name: 'Vue.js',
          description: 'Vue.js 及其生态',
          icon: 'fas fa-v',
          parentId: 1,
          sort: 1,
          status: 'ACTIVE',
          createdAt: '2023-01-02T10:00:00Z',
          updatedAt: '2023-01-02T10:00:00Z',
        },
        {
          id: 12,
          name: 'React',
          description: 'React 技术栈',
          icon: 'fas fa-brands fa-react',
          parentId: 1,
          sort: 2,
          status: 'ACTIVE',
          createdAt: '2023-01-03T10:00:00Z',
          updatedAt: '2023-01-03T10:00:00Z',
        },
      ],
    },
    {
      id: 2,
      name: '后端开发',
      description: '后端架构及语言',
      icon: 'fas fa-server',
      parentId: null,
      sort: 2,
      status: 'ACTIVE',
      createdAt: '2023-01-05T10:00:00Z',
      updatedAt: '2023-01-05T10:00:00Z',
      children: [
        {
          id: 21,
          name: 'Java',
          description: 'Java 及 Spring Boot',
          icon: 'fas fa-mug-hot',
          parentId: 2,
          sort: 1,
          status: 'ACTIVE',
          createdAt: '2023-01-06T10:00:00Z',
          updatedAt: '2023-01-06T10:00:00Z',
        },
      ],
    },
  ])

  const searchKeyword = ref('')
  const sortField = ref<AdminCategorySortField>('sort')
  const sortOrder = ref<AdminCategorySortOrder>(1)

  const editorOpen = ref(false)
  const editorMode = ref<EditorMode>('create')
  const editorSubmitting = ref(false)
  const editingCategoryId = ref<number | null>(null)

  const formModel = reactive<AdminCategoryFormModel>({
    name: '',
    description: '',
    icon: '',
    parentId: null,
    sort: 0,
    status: 'ACTIVE',
  })
  const formErrors = reactive<Record<string, string>>({})

  const deleteDialogOpen = ref(false)
  const deleteSubmitting = ref(false)
  const deletingCategory = ref<AdminCategoryItem | null>(null)

  const selectedCategoryIds = ref<number[]>([])

  const filteredCategories = computed(() => {
    let result = [...categories.value]
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      // A simple flat search for mock data since we might need to filter children too.
      // But for simplicity, we mock a basic search over the primary list
      result = result.filter(
        (c) => c.name.toLowerCase().includes(keyword) || c.description?.toLowerCase().includes(keyword)
      )
    }

    result.sort((a, b) => {
      const valA = a[sortField.value]
      const valB = b[sortField.value]
      const orderMulti = sortOrder.value
      
      if (valA == null) return -1 * orderMulti
      if (valB == null) return 1 * orderMulti
      if (valA < valB) return -1 * orderMulti
      if (valA > valB) return 1 * orderMulti
      return 0
    })

    return result
  })

  const stats = computed(() => {
    let total = 0
    let active = 0
    let disabled = 0

    const countNode = (node: AdminCategoryItem) => {
      total++
      if (node.status === 'ACTIVE') active++
      else disabled++
      node.children?.forEach(countNode)
    }

    categories.value.forEach(countNode)

    return { total, active, disabled }
  })

  const setSearchKeyword = (keyword: string) => {
    searchKeyword.value = keyword
  }

  const setSort = (field: AdminCategorySortField, order: AdminCategorySortOrder) => {
    sortField.value = field
    sortOrder.value = order
  }

  const openCreateEditor = () => {
    editorMode.value = 'create'
    editingCategoryId.value = null
    formModel.name = ''
    formModel.description = ''
    formModel.icon = ''
    formModel.parentId = null
    formModel.sort = 0
    formModel.status = 'ACTIVE'
    Object.keys(formErrors).forEach((key) => delete formErrors[key])
    editorOpen.value = true
  }

  const openEditEditor = (category: AdminCategoryItem) => {
    editorMode.value = 'edit'
    editingCategoryId.value = category.id
    formModel.name = category.name
    formModel.description = category.description || ''
    formModel.icon = category.icon || ''
    formModel.parentId = category.parentId
    formModel.sort = category.sort
    formModel.status = category.status
    Object.keys(formErrors).forEach((key) => delete formErrors[key])
    editorOpen.value = true
  }

  const closeEditor = () => {
    editorOpen.value = false
  }

  const saveCategory = () => {
    // Validate
    if (!formModel.name.trim()) {
      formErrors.name = '请输入分类名称'
      return
    }

    editorSubmitting.value = true
    setTimeout(() => {
      if (editorMode.value === 'create') {
        console.log('Creates category', { ...formModel })
      } else {
        console.log('Edits category', editingCategoryId.value, { ...formModel })
      }
      editorSubmitting.value = false
      closeEditor()
    }, 500)
  }

  const requestDeleteCategory = (category: AdminCategoryItem) => {
    deletingCategory.value = category
    deleteDialogOpen.value = true
  }

  const closeDeleteDialog = () => {
    deleteDialogOpen.value = false
    deletingCategory.value = null
  }

  const confirmDeleteCategory = () => {
    if (!deletingCategory.value) return
    deleteSubmitting.value = true
    setTimeout(() => {
      console.log('Deletes category', deletingCategory.value?.id)
      deleteSubmitting.value = false
      closeDeleteDialog()
    }, 500)
  }

  const setSelectedCategoryIds = (ids: number[]) => {
    selectedCategoryIds.value = ids
  }

  return {
    categories: filteredCategories,
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
    allCategories: categories,

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
  }
}
