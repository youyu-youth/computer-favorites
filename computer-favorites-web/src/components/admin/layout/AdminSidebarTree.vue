<script setup lang="ts">
import { ref, computed, watch, shallowRef, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import type { AdminMenuKey } from '@/stores/adminNav'
import { useAdminNavStore } from '@/stores/adminNav'
import AdminSidebarCategoryList from '@/components/admin/layout/AdminSidebarCategoryList.vue'

const navStore = useAdminNavStore()
const router = useRouter()
const route = useRoute()
const {
  activeMenu,
  menuItems,
  websiteCategories,
  selectedTreeKey,
  mobileSidebarOpen,
} = storeToRefs(navStore)

// 控制当前侧边栏展示的是第一级功能(main)还是第二级子项(sub)
const currentSidebarLevel = ref<'main' | 'sub'>('main')

const categoryKeyword = shallowRef('')

const resetCategoryKeyword = (): void => {
  categoryKeyword.value = ''
}

const syncSidebarLevelByActiveMenu = (): void => {
  currentSidebarLevel.value = activeMenu.value === 'websites' ? 'sub' : 'main'
}

// 监听活动菜单的变化。如果直接进入的是 websites 且带有分类选中状态，直接呈现 sub 层
watch(
  () => activeMenu.value,
  (newMenu) => {
    syncSidebarLevelByActiveMenu()
    if (newMenu !== 'websites') {
      resetCategoryKeyword()
    }
  },
  { immediate: true }
)

watch(
  () => mobileSidebarOpen.value,
  (open) => {
    if (open) {
      // 每次打开移动端抽屉都根据当前菜单同步层级，避免“网站管理已选中但仍停留在主菜单层”
      syncSidebarLevelByActiveMenu()
    }
  }
)

const normalizedKeyword = computed(() => {
  return categoryKeyword.value.trim().toLowerCase()
})

const filteredCategories = computed(() => {
  if (!normalizedKeyword.value) {
    return websiteCategories.value
  }
  return websiteCategories.value.filter((item) => {
    return item.name.toLowerCase().includes(normalizedKeyword.value)
  })
})

const closeMobileSidebar = (): void => {
  resetCategoryKeyword()
  navStore.closeMobileSidebar()
}

const goToMainLevel = () => {
  resetCategoryKeyword()
  currentSidebarLevel.value = 'main'
}

const navigateByMenu = async (menuKey: AdminMenuKey): Promise<void> => {
  if (menuKey === 'websites') {
    if (route.name !== 'adminWebsites') {
      await router.push({ name: 'adminWebsites' })
    }
    return
  }

  if (menuKey === 'tags' && route.name !== 'adminTags') {
    await router.push({ name: 'adminTags' })
  }
}

const handleMainMenuClick = (key: AdminMenuKey) => {
  navStore.setActiveMenu(key)
  if (key === 'websites') {
    resetCategoryKeyword()
    currentSidebarLevel.value = 'sub'
  } else {
    currentSidebarLevel.value = 'main'
  }

  void navigateByMenu(key)

  if (typeof window !== 'undefined' && window.innerWidth < 768 && key !== 'websites') {
    navStore.closeMobileSidebar()
  }
}

const handleSubMenuClick = (categoryId: number) => {
  navStore.syncSelectionFromTreeKey(`category:${categoryId}`)
  if (route.name !== 'adminWebsites') {
    void router.push({ name: 'adminWebsites' })
  }
}

// 可拖拽改变侧栏宽度逻辑
const sidebarWidth = ref(288) // 默认 width: 288px (w-72)
const isDragging = ref(false)

const startDrag = (e: MouseEvent) => {
  isDragging.value = true
  document.body.style.userSelect = 'none'
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

const onDrag = (e: MouseEvent) => {
  if (!isDragging.value) return
  let newWidth = e.clientX
  // 限制宽度的范围 (最小 200px, 最大 500px)
  if (newWidth < 200) newWidth = 200
  if (newWidth > 500) newWidth = 500
  sidebarWidth.value = newWidth
}

const stopDrag = () => {
  isDragging.value = false
  document.body.style.userSelect = ''
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
})
</script>

<template>
  <div>
    <!-- Desktop Sidebar -->
    <aside
      class="hidden md:flex relative flex-col flex-shrink-0 border-r border-gray-200 dark:border-dark-border bg-white dark:bg-dark-bg/50 h-[calc(100vh-4rem)] sticky top-16 transition-colors duration-300"
      :style="{ width: sidebarWidth + 'px' }"
    >
      <!-- Resize Handle -->
      <div
        class="absolute top-0 bottom-0 -right-1 w-2 cursor-col-resize z-10 transition-colors flex items-center justify-center group"
        :class="isDragging ? 'bg-[#f78166]/50' : 'hover:bg-gray-300 dark:hover:bg-gray-600'"
        @mousedown.prevent="startDrag"
      >
        <div class="h-8 w-1 rounded-full bg-gray-400 dark:bg-gray-500 opacity-0 group-hover:opacity-100 transition-opacity" :class="isDragging ? 'opacity-100 bg-[#f78166]' : ''"></div>
      </div>

      <!-- Primary Main Level View -->
      <div v-if="currentSidebarLevel === 'main'" class="flex-1 overflow-y-auto px-3 py-4 w-full">
        <h2 class="text-xs font-semibold tracking-wider text-gray-500 dark:text-gray-400 mb-4 px-3 uppercase">
          导航主页
        </h2>

        <div class="space-y-1">
          <div
            v-for="menu in menuItems"
            :key="menu.key"
            @click="handleMainMenuClick(menu.key)"
            class="group flex items-center justify-between px-3 py-2 cursor-pointer rounded-lg transition-colors border-l-2"
            :class="activeMenu === menu.key ? 'border-[#f78166] bg-orange-50/50 text-[#f78166] dark:bg-[#f78166]/10' : 'border-transparent text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800/50'"
          >
            <div class="flex items-center gap-3">
              <i :class="[menu.icon, activeMenu === menu.key ? 'text-[#f78166]' : 'text-gray-400 group-hover:text-gray-600 dark:group-hover:text-gray-300']" class="w-5 text-center text-lg"></i>
              <span class="text-sm font-medium">{{ menu.label }}</span>
            </div>
            <!-- mock提示 -->
            <span v-if="menu.mockOnly" class="text-[10px] px-1.5 py-0.5 rounded-md bg-gray-100 dark:bg-dark-card text-gray-400 dark:text-gray-500">模拟</span>
            <i v-else class="fas fa-chevron-right text-[10px] opacity-0 group-hover:opacity-100 transition-opacity" :class="activeMenu === menu.key ? 'text-[#f78166]' : 'text-gray-400'"></i>
          </div>
        </div>
      </div>

      <!-- Secondary Sub-Level View (Categories like the provided image) -->
      <div v-else-if="currentSidebarLevel === 'sub'" class="flex-1 flex flex-col min-h-0 w-full animate-fade-in-left">
        <!-- Back Button Header -->
        <div class="px-4 py-3 border-b border-gray-100 dark:border-dark-border/50">
          <button
            @click="goToMainLevel"
            class="flex items-center text-sm font-medium text-gray-600 dark:text-[#8b949e] hover:text-[#f78166] dark:hover:text-[#f78166] transition-colors"
          >
            <i class="fas fa-arrow-left mr-2"></i> 返回主菜单
          </button>
        </div>

        <div class="flex-1 overflow-y-auto px-2 py-3 space-y-3">
          <div class="px-2">
            <!-- Search categories -->
            <div class="relative mb-3">
              <i class="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-xs"></i>
              <input
                v-model="categoryKeyword"
                type="text"
                placeholder="搜索分类..."
                class="w-full rounded-md border border-gray-200 dark:border-dark-card/60 bg-gray-50 dark:bg-[#0d1117] text-gray-900 dark:text-white pl-8 pr-3 py-1.5 text-[13px] outline-none transition-colors focus:border-[#f78166] focus:ring-1 focus:ring-[#f78166]/30"
              >
            </div>
          </div>

          <AdminSidebarCategoryList
            class="px-1"
            :categories="filteredCategories"
            :selectedTreeKey="selectedTreeKey"
            @select="handleSubMenuClick"
          />
        </div>
      </div>
    </aside>

    <!-- Mobile Drawer (Matches the exact same logic) -->
    <div v-if="mobileSidebarOpen" class="fixed inset-0 z-40 md:hidden">
      <div class="absolute inset-0 bg-black/45 backdrop-blur-sm" @click="closeMobileSidebar"></div>

      <aside class="absolute left-0 top-16 h-[calc(100vh-4rem)] w-[84vw] max-w-xs flex flex-col border-r border-gray-200 dark:border-dark-border bg-white dark:bg-dark-bg overflow-hidden shadow-2xl transition-transform">
        <div class="flex items-center justify-between px-4 py-3 border-b border-gray-100 dark:border-dark-border/50">
          <h2 class="text-sm font-semibold text-gray-800 dark:text-gray-200">
            {{ currentSidebarLevel === 'main' ? '导航主页' : '网站分类' }}
          </h2>
          <button
            type="button"
            class="w-8 h-8 flex items-center justify-center rounded-md text-gray-500 hover:bg-gray-100 dark:hover:bg-dark-card transition-colors"
            @click="closeMobileSidebar"
          >
            <i class="fas fa-xmark"></i>
          </button>
        </div>

        <div v-if="currentSidebarLevel === 'main'" class="flex-1 overflow-y-auto px-3 py-4 w-full">
          <div class="space-y-1">
            <div
              v-for="menu in menuItems"
              :key="menu.key"
              @click="handleMainMenuClick(menu.key)"
              class="group flex items-center justify-between px-3 py-2 cursor-pointer rounded-lg border-l-[3px]"
              :class="activeMenu === menu.key ? 'border-[#f78166] bg-orange-50/50 text-[#f78166] dark:bg-[#f78166]/10' : 'border-transparent text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800/50'"
            >
              <div class="flex items-center gap-3">
                <i :class="[menu.icon, activeMenu === menu.key ? 'text-[#f78166]' : 'text-gray-400 group-hover:text-gray-600 dark:group-hover:text-gray-300']" class="w-5 text-center text-lg"></i>
                <span class="text-sm font-medium">{{ menu.label }}</span>
              </div>
              <span v-if="menu.mockOnly" class="text-[10px] px-1.5 py-0.5 rounded-md bg-gray-100 dark:bg-dark-card text-gray-400 dark:text-gray-500">模拟</span>
              <i v-else class="fas fa-chevron-right text-[10px] opacity-0 group-hover:opacity-100 transition-opacity" :class="activeMenu === menu.key ? 'text-[#f78166]' : 'text-gray-400'"></i>
            </div>
          </div>
        </div>

        <div v-else-if="currentSidebarLevel === 'sub'" class="flex-1 overflow-y-auto flex flex-col min-h-0 w-full animate-fade-in-left">
          <div class="px-4 py-3 bg-gray-50/50 dark:bg-dark-card/30 border-b border-gray-100 dark:border-dark-border/50">
            <button
              @click="goToMainLevel"
              class="flex items-center text-sm font-medium text-gray-600 dark:text-gray-400 hover:text-[#f78166] transition-colors"
            >
              <i class="fas fa-arrow-left mr-2"></i> 返回主菜单
            </button>
          </div>

          <div class="px-3 py-3">
            <div class="relative mb-4">
              <i class="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-xs"></i>
              <input
                v-model="categoryKeyword"
                type="text"
                placeholder="搜索分类..."
                class="w-full rounded-md border border-gray-200 dark:border-dark-card/60 bg-white dark:bg-[#0d1117] text-gray-900 dark:text-white pl-8 pr-3 py-1.5 text-[13px] outline-none transition-colors focus:border-[#f78166] focus:ring-1 focus:ring-[#f78166]/30"
              >
            </div>

            <AdminSidebarCategoryList
              :categories="filteredCategories"
              :selectedTreeKey="selectedTreeKey"
              @select="handleSubMenuClick"
            />
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in-left {
  animation: fadeInLeft 0.25s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
