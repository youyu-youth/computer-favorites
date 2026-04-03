import { defineStore } from 'pinia'
import { computed, reactive, shallowRef } from 'vue'

export type AdminMenuKey = 'websites' | 'tags' | 'dashboard' | 'users' | 'comments'

export type WebsiteCategoryNavItem = {
  id: number
  name: string
  count: number
}

type AdminMenuItem = {
  key: AdminMenuKey
  label: string
  icon: string
  mockOnly: boolean
}

const ALL_CATEGORY_ID = 0

const DEFAULT_MENU_ITEM: AdminMenuItem = {
  key: 'websites',
  label: '网站管理',
  icon: 'fas fa-globe',
  mockOnly: false,
}

const ADMIN_MENU_ITEMS: AdminMenuItem[] = [
  DEFAULT_MENU_ITEM,
  {
    key: 'tags',
    label: '标签管理',
    icon: 'fas fa-tags',
    mockOnly: false,
  },
  {
    key: 'dashboard',
    label: '仪表盘',
    icon: 'fas fa-chart-line',
    mockOnly: true,
  },
  {
    key: 'users',
    label: '用户管理',
    icon: 'fas fa-users',
    mockOnly: true,
  },
  {
    key: 'comments',
    label: '评论管理',
    icon: 'fas fa-comments',
    mockOnly: true,
  },
]

export const useAdminNavStore = defineStore('adminNav', () => {
  const activeMenu = shallowRef<AdminMenuKey>('websites')
  const websiteCategories = shallowRef<WebsiteCategoryNavItem[]>([])
  const selectedCategoryId = shallowRef<number>(ALL_CATEGORY_ID)
  const mobileSidebarOpen = shallowRef(false)
  const expandedKeys = reactive<Record<string, boolean>>({
    'menu:websites': true,
  })

  const menuItems = computed(() => ADMIN_MENU_ITEMS)

  const activeMenuMeta = computed<AdminMenuItem>(() => {
    return (
      menuItems.value.find((item) => item.key === activeMenu.value) ??
      DEFAULT_MENU_ITEM
    )
  })

  const activeMenuLabel = computed(() => activeMenuMeta.value.label)

  const isWebsiteMenuActive = computed(() => activeMenu.value === 'websites')

  const selectedCategory = computed(() => {
    return (
      websiteCategories.value.find((item) => item.id === selectedCategoryId.value) ?? null
    )
  })

  const selectedCategoryLabel = computed(() => {
    if (!selectedCategory.value) {
      return '全部分类'
    }
    return selectedCategory.value.name
  })

  const selectedTreeKey = computed(() => {
    if (activeMenu.value === 'websites') {
      return `category:${selectedCategoryId.value}`
    }
    return `menu:${activeMenu.value}`
  })

  const setActiveMenu = (menuKey: AdminMenuKey): void => {
    activeMenu.value = menuKey
    if (menuKey === 'websites') {
      expandedKeys['menu:websites'] = true
    }
  }

  const setWebsiteCategories = (categories: WebsiteCategoryNavItem[]): void => {
    websiteCategories.value = categories
    const exists = categories.some((item) => item.id === selectedCategoryId.value)
    if (!exists) {
      selectedCategoryId.value = ALL_CATEGORY_ID
    }
  }

  const setSelectedCategoryId = (categoryId: number): void => {
    selectedCategoryId.value = categoryId
    activeMenu.value = 'websites'
    expandedKeys['menu:websites'] = true
  }

  const setMobileSidebarOpen = (open: boolean): void => {
    mobileSidebarOpen.value = open
  }

  const toggleMobileSidebar = (): void => {
    mobileSidebarOpen.value = !mobileSidebarOpen.value
  }

  const closeMobileSidebar = (): void => {
    mobileSidebarOpen.value = false
  }

  const setExpandedKeys = (nextKeys: Record<string, boolean>): void => {
    Object.keys(expandedKeys).forEach((key) => {
      delete expandedKeys[key]
    })
    Object.entries(nextKeys).forEach(([key, value]) => {
      if (value) {
        expandedKeys[key] = true
      }
    })
  }

  const syncSelectionFromTreeKey = (treeKey: string): void => {
    if (treeKey.startsWith('menu:')) {
      const rawMenu = treeKey.replace('menu:', '')
      if (
        rawMenu === 'websites' ||
        rawMenu === 'tags' ||
        rawMenu === 'dashboard' ||
        rawMenu === 'users' ||
        rawMenu === 'comments'
      ) {
        setActiveMenu(rawMenu)
      }
      return
    }

    if (treeKey.startsWith('category:')) {
      const rawCategory = Number(treeKey.replace('category:', ''))
      if (!Number.isNaN(rawCategory)) {
        setSelectedCategoryId(rawCategory)
      }
    }
  }

  return {
    activeMenu,
    websiteCategories,
    selectedCategoryId,
    mobileSidebarOpen,
    expandedKeys,
    menuItems,
    activeMenuMeta,
    activeMenuLabel,
    isWebsiteMenuActive,
    selectedCategory,
    selectedCategoryLabel,
    selectedTreeKey,
    setActiveMenu,
    setWebsiteCategories,
    setSelectedCategoryId,
    setMobileSidebarOpen,
    toggleMobileSidebar,
    closeMobileSidebar,
    setExpandedKeys,
    syncSelectionFromTreeKey,
  }
})
