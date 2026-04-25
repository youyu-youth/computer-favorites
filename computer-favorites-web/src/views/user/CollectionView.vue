<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 收藏夹页面 — 响应式布局，暗黑模式支持
 */
import { onMounted, onUnmounted, ref, computed } from 'vue'
import Drawer from 'primevue/drawer'
import CollectionSidebar from '@/components/user/collection/CollectionSidebar.vue'
import CollectionContentHeader from '@/components/user/collection/CollectionContentHeader.vue'
import CollectionWebsiteCard from '@/components/user/collection/CollectionWebsiteCard.vue'
import CollectionDetailPanel from '@/components/user/collection/CollectionDetailPanel.vue'
import CollectionStatusBar from '@/components/user/collection/CollectionStatusBar.vue'
import ContextMenu from '@/components/user/collection/ContextMenu.vue'
import PasswordDialog from '@/components/user/collection/PasswordDialog.vue'
import CreateFolderDialog from '@/components/user/CreateFolderDialog.vue'
import { useCollectionManagement } from '@/composables/useCollectionManagement'
import { useToast } from '@/composables/useToast'
import type { CollectionCategory } from '@/types/collection'
import type { MenuItem } from '@/components/user/collection/ContextMenu.vue'

defineOptions({ name: 'CollectionView' })

const toast = useToast()

const {
  categories,
  quickAccessList,
  activeQuickAccess,
  activeCategoryId,
  filteredResources,
  searchQuery,
  selectedWebsite,
  detailPanelOpen,
  viewMode,
  zoomLevel,
  mobileSidebarOpen,
  mobileDetailOpen,
  totalCount,
  selectedCount,
  storagePercent,
  storageUsed,
  storageTotal,
  breadcrumbPath,

  setActiveQuickAccess,
  setActiveCategory,
  selectWebsite,
  toggleDetailPanel,
  closeDetailPanel,
  setViewMode,
  setZoomLevel,
  toggleStar,
} = useCollectionManagement()

const isDark = ref(false)
let darkModeObserver: MutationObserver | null = null

const updateDarkModeState = () => {
  isDark.value =
    document.documentElement.classList.contains('dark') || document.body.classList.contains('dark')
}

const gridColsClass = computed(() => {
  if (viewMode.value === 'list') return 'grid-cols-1'
  const z = zoomLevel.value
  if (z <= 70) return 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-4'
  if (z <= 90) return 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3'
  if (z <= 110) return 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
  if (z <= 130) return 'grid-cols-1 md:grid-cols-2'
  return 'grid-cols-1'
})

const cardScale = computed(() => {
  return zoomLevel.value / 100
})

const handleSelectWebsite = (website: any) => {
  selectWebsite(website)
  if (window.innerWidth < 1024) {
    mobileDetailOpen.value = true
  }
}

const handleVisit = (url: string) => {
  if (url) {
    window.open(url, '_blank', 'noopener,noreferrer')
  }
}

onMounted(() => {
  updateDarkModeState()
  darkModeObserver = new MutationObserver(updateDarkModeState)
  darkModeObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['class'],
  })
  darkModeObserver.observe(document.body, { attributes: true, attributeFilter: ['class'] })
})

onUnmounted(() => {
  darkModeObserver?.disconnect()
  darkModeObserver = null
})

/* ========== 上下文菜单状态 ========== */
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextMenuItems = ref<MenuItem[]>([])
const contextMenuTarget = ref<CollectionCategory | null>(null)
const isMobile = ref(false)

const checkMobile = () => {
  isMobile.value = window.innerWidth < 1024
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

/* ========== 密码弹窗状态 ========== */
const passwordDialogVisible = ref(false)
const passwordDialogTitle = ref('')
const passwordDialogDesc = ref('')
const passwordDialogLoading = ref(false)
let passwordConfirmCallback: ((pwd: string) => void) | null = null

const openPasswordDialog = (title: string, description: string, onConfirm: (pwd: string) => void) => {
  passwordDialogTitle.value = title
  passwordDialogDesc.value = description
  passwordConfirmCallback = onConfirm
  passwordDialogVisible.value = true
}

const handlePasswordConfirm = (pwd: string) => {
  passwordDialogLoading.value = true
  setTimeout(() => {
    passwordDialogLoading.value = false
    passwordConfirmCallback?.(pwd)
    passwordConfirmCallback = null
  }, 400)
}

/* ========== 隐藏/删除状态 ========== */
const hiddenCategoryIds = ref<Set<number>>(new Set())
const editingCategoryId = ref<number | null>(null)

/* ========== 新建收藏夹对话框 ========== */
const createFolderDialogVisible = ref(false)
const createFolderParentId = ref(0)

const openCreateFolder = (parentId: number) => {
  createFolderParentId.value = parentId
  createFolderDialogVisible.value = true
}

const handleFolderCreated = () => {
  toast.add({
    title: '创建成功',
    description: '收藏夹已创建',
    type: 'success',
  })
}

/* ========== 菜单构建 ========== */
const buildFolderMenuItems = (category: CollectionCategory): MenuItem[] => [
  {
    label: '新建收藏夹',
    icon: 'folder-plus',
    onClick: () => openCreateFolder(category.id),
  },
  {
    label: '重命名',
    icon: 'pencil',
    onClick: () => { editingCategoryId.value = category.id },
  },
  {
    label: '隐藏',
    icon: 'eye-off',
    onClick: () => {
      openPasswordDialog(
        '隐藏收藏夹',
        `请输入隐藏密码以隐藏「${category.name}」及其内容`,
        (pwd) => {
          if (pwd === '123456') {
            hiddenCategoryIds.value.add(category.id)
            if (activeCategoryId.value === category.id) {
              setActiveCategory(null)
            }
            toast.add({
              title: '已隐藏',
              description: `「${category.name}」及其内容已隐藏`,
              type: 'success',
            })
            passwordDialogVisible.value = false
          } else {
            toast.add({
              title: '密码错误',
              description: '隐藏密码不正确，请重试',
              type: 'error',
            })
          }
        },
      )
    },
  },
  {
    label: '删除',
    icon: 'trash-2',
    danger: true,
    onClick: () => {
      openPasswordDialog(
        '删除收藏夹',
        `请输入登录密码以删除「${category.name}」`,
        (pwd) => {
          if (pwd === '123456') {
            const idx = categories.value.findIndex((c) => c.id === category.id)
            if (idx !== -1) {
              categories.value.splice(idx, 1)
            }
            if (activeCategoryId.value === category.id) {
              setActiveCategory(null)
            }
            toast.add({
              title: '已删除',
              description: `「${category.name}」已删除`,
              type: 'success',
            })
            passwordDialogVisible.value = false
          } else {
            toast.add({
              title: '密码错误',
              description: '登录密码不正确，请重试',
              type: 'error',
            })
          }
        },
      )
    },
  },
]

const buildEmptyMenuItems = (): MenuItem[] => [
  {
    label: '新建收藏夹',
    icon: 'folder-plus',
    onClick: () => openCreateFolder(0),
  },
  {
    label: '显示隐藏收藏夹',
    icon: 'eye',
    onClick: () => {
      if (hiddenCategoryIds.value.size === 0) {
        toast.add({
          title: '提示',
          description: '当前没有隐藏的收藏夹',
          type: 'info',
        })
        return
      }
      openPasswordDialog(
        '显示隐藏收藏夹',
        '请输入隐藏密码以恢复所有隐藏的收藏夹',
        (pwd) => {
          if (pwd === '123456') {
            hiddenCategoryIds.value.clear()
            toast.add({
              title: '已恢复',
              description: '所有隐藏的收藏夹已恢复显示',
              type: 'success',
            })
            passwordDialogVisible.value = false
          } else {
            toast.add({
              title: '密码错误',
              description: '隐藏密码不正确，请重试',
              type: 'error',
            })
          }
        },
      )
    },
  },
]

/* ========== 菜单事件处理 ========== */
const handleContextMenuFolder = (event: MouseEvent | TouchEvent, category: CollectionCategory) => {
  contextMenuTarget.value = category
  contextMenuItems.value = buildFolderMenuItems(category)

  if (event instanceof MouseEvent) {
    contextMenuX.value = event.clientX
    contextMenuY.value = event.clientY
  } else {
    const touch = event.touches[0] || (event as any).changedTouches[0]
    contextMenuX.value = touch?.clientX ?? 0
    contextMenuY.value = touch?.clientY ?? 0
  }
  contextMenuVisible.value = true
}

const handleContextMenuEmpty = (event: MouseEvent | TouchEvent) => {
  contextMenuTarget.value = null
  contextMenuItems.value = buildEmptyMenuItems()

  if (event instanceof MouseEvent) {
    contextMenuX.value = event.clientX
    contextMenuY.value = event.clientY
  } else {
    const touch = event.touches[0] || (event as any).changedTouches[0]
    contextMenuX.value = touch?.clientX ?? 0
    contextMenuY.value = touch?.clientY ?? 0
  }
  contextMenuVisible.value = true
}

const handleRenameCategory = (id: number, newName: string) => {
  const cat = categories.value.find((c) => c.id === id)
  if (cat) {
    cat.name = newName
    toast.add({
      title: '重命名成功',
      description: `收藏夹已更名为「${newName}」`,
      type: 'success',
    })
  }
  editingCategoryId.value = null
}

const handleRenameCancel = () => {
  editingCategoryId.value = null
}

/* 用于 Drawer 内 Sidebar 的事件透传 */
const drawerSidebarEvents: Record<string, any> = {
  'select-quick-access': (key: string) => {
    setActiveQuickAccess(key)
    mobileSidebarOpen.value = false
  },
  'select-category': (id: number) => {
    setActiveCategory(id)
    mobileSidebarOpen.value = false
  },
  'context-menu-folder': handleContextMenuFolder,
  'context-menu-empty': handleContextMenuEmpty,
  'rename-category': handleRenameCategory,
  'rename-cancel': handleRenameCancel,
}
</script>

<template>
  <div
    class="collection-page bg-[#f3f4f6] dark:bg-black text-[#111827] dark:text-[#f0f0f0] h-[calc(100vh-64px)] overflow-hidden flex flex-col selection:bg-blue-500 selection:text-white transition-colors duration-300"
  >
    <!-- 主体三栏区域 -->
    <div class="flex flex-1 overflow-hidden">
      <!-- 桌面端左侧栏 -->
      <div class="hidden lg:block">
        <CollectionSidebar
          :quick-access-list="quickAccessList"
          :categories="categories"
          :active-quick-access="activeQuickAccess"
          :active-category-id="activeCategoryId"
          :storage-used="storageUsed"
          :storage-total="storageTotal"
          :storage-percent="storagePercent"
          :editing-category-id="editingCategoryId"
          :hidden-category-ids="hiddenCategoryIds"
          @select-quick-access="setActiveQuickAccess"
          @select-category="setActiveCategory"
          @context-menu-folder="handleContextMenuFolder"
          @context-menu-empty="handleContextMenuEmpty"
          @rename-category="handleRenameCategory"
          @rename-cancel="handleRenameCancel"
        />
      </div>

      <!-- 移动端左侧栏 Drawer -->
      <Drawer
        :visible="mobileSidebarOpen"
        position="left"
        class="bg-white dark:bg-black border-r border-[#e5e7eb] dark:border-[#1a1a1a] !w-[280px] flex flex-col h-full"
        :pt="{
          mask: { class: 'fixed inset-0 z-[100] bg-black/60 backdrop-blur-sm' },
          header: {
            class:
              'px-4 py-4 border-b border-[#e5e7eb] dark:border-[#1a1a1a] flex items-center justify-between shrink-0 box-border',
          },
          title: { class: 'text-lg font-bold text-[#111827] dark:text-white' },
          closeButton: {
            class:
              'flex items-center justify-center w-8 h-8 rounded-full bg-[#f3f4f6] dark:bg-white/5 text-[#9ca3af] hover:bg-[#e5e7eb] dark:hover:bg-white/10 transition-colors cursor-pointer shrink-0',
          },
          content: { class: 'p-0 overflow-y-auto flex-1' },
        }"
        @update:visible="mobileSidebarOpen = $event"
      >
        <template #header>
          <div class="flex items-center space-x-2">
            <div
              class="w-8 h-8 bg-blue-100 dark:bg-blue-500/10 rounded-lg flex items-center justify-center text-blue-500 dark:text-blue-400"
            >
              <i class="fas fa-folder-special text-sm"></i>
            </div>
            <span class="font-bold text-lg text-[#111827] dark:text-[#f0f0f0]">ResourceHub</span>
          </div>
        </template>
        <CollectionSidebar
          :quick-access-list="quickAccessList"
          :categories="categories"
          :active-quick-access="activeQuickAccess"
          :active-category-id="activeCategoryId"
          :storage-used="storageUsed"
          :storage-total="storageTotal"
          :storage-percent="storagePercent"
          :editing-category-id="editingCategoryId"
          :hidden-category-ids="hiddenCategoryIds"
          v-on="drawerSidebarEvents"
        />
      </Drawer>

      <!-- 中间主内容区 + 右侧详情面板 -->
      <div class="flex-1 flex flex-col min-w-0 bg-white dark:bg-black">
        <!-- 内容区顶部栏 -->
        <CollectionContentHeader
          :breadcrumb-path="breadcrumbPath"
          :search-query="searchQuery"
          @update:search-query="searchQuery = $event"
          @toggle-detail-panel="toggleDetailPanel"
          @add-resource="() => {}"
        />

        <!-- 中间内容 + 右侧面板 -->
        <div class="flex-1 overflow-auto flex">
          <!-- 网站卡片列表 -->
          <main class="flex-1 p-6 overflow-y-auto">
            <!-- 卡片网格/列表 -->
            <div
              v-if="filteredResources.length > 0"
              class="grid gap-3"
              :class="gridColsClass"
              :style="viewMode === 'grid' ? { transform: `scale(${cardScale})`, transformOrigin: 'top left' } : {}"
            >
              <CollectionWebsiteCard
                v-for="item in filteredResources"
                :key="item.id"
                :item="item"
                :view-mode="viewMode"
                :is-selected="selectedWebsite?.id === item.id"
                @select="handleSelectWebsite"
                @toggle-star="toggleStar"
              />
            </div>

            <!-- 空状态 -->
            <div
              v-else
              class="py-20 text-center text-[#9ca3af] dark:text-[#4b5563]"
            >
              <i class="fas fa-inbox text-4xl mb-4 text-[#d1d5db] dark:text-[#374151]"></i>
              <p>未找到符合条件的资源</p>
            </div>
          </main>

          <!-- 桌面端右侧详情面板 -->
          <div v-if="detailPanelOpen" class="hidden lg:block">
            <CollectionDetailPanel
              :website="selectedWebsite"
              @close="closeDetailPanel"
              @toggle-star="toggleStar"
              @visit="handleVisit"
            />
          </div>
        </div>

        <!-- 底部状态栏 -->
        <CollectionStatusBar
          :total-count="totalCount"
          :selected-count="selectedCount"
          :zoom-level="zoomLevel"
          :view-mode="viewMode"
          @update:zoom-level="setZoomLevel"
          @update:view-mode="setViewMode"
        />
      </div>

      <!-- 移动端右侧详情面板 Drawer -->
      <Drawer
        :visible="mobileDetailOpen"
        position="right"
        class="bg-white dark:bg-black border-l border-[#e5e7eb] dark:border-[#1a1a1a] !w-[min(92vw,360px)] flex flex-col h-full"
        :pt="{
          mask: { class: 'fixed inset-0 z-[100] bg-black/60 backdrop-blur-sm' },
          header: { class: 'hidden' },
          content: { class: 'p-0 overflow-y-auto flex-1' },
        }"
        @update:visible="mobileDetailOpen = $event"
      >
        <CollectionDetailPanel
          :website="selectedWebsite"
          @close="mobileDetailOpen = false"
          @toggle-star="toggleStar"
          @visit="handleVisit"
        />
      </Drawer>
    </div>

    <!-- 移动端底部浮动按钮：打开侧边栏 -->
    <button
      type="button"
      class="lg:hidden fixed bottom-6 left-4 z-40 w-12 h-12 rounded-full bg-blue-500 text-white shadow-lg flex items-center justify-center cursor-pointer hover:bg-blue-600 transition-colors"
      @click="mobileSidebarOpen = true"
    >
      <i class="fas fa-bars"></i>
    </button>

    <!-- 上下文菜单 -->
    <ContextMenu
      v-model:visible="contextMenuVisible"
      :x="contextMenuX"
      :y="contextMenuY"
      :items="contextMenuItems"
      :is-mobile="isMobile"
    />

    <!-- 密码验证弹窗 -->
    <PasswordDialog
      v-model:visible="passwordDialogVisible"
      :title="passwordDialogTitle"
      :description="passwordDialogDesc"
      :loading="passwordDialogLoading"
      @confirm="handlePasswordConfirm"
    />

    <!-- 新建收藏夹对话框 -->
    <CreateFolderDialog
      v-model:open="createFolderDialogVisible"
      :parent-id="createFolderParentId"
      @submit="handleFolderCreated"
    />
  </div>
</template>

<style scoped>
.collection-page {
  font-family: 'Inter', sans-serif;
  -webkit-font-smoothing: antialiased;
}

.collection-page::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.collection-page::-webkit-scrollbar-track {
  background: transparent;
}

.collection-page::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 0;
}
.dark .collection-page::-webkit-scrollbar-thumb {
  background: #222;
}

.collection-page::-webkit-scrollbar-thumb:hover {
  background: #3b82f6;
}
</style>
