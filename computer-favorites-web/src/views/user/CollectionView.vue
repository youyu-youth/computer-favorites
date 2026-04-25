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
import { useCollectionManagement } from '@/composables/useCollectionManagement'

defineOptions({ name: 'CollectionView' })

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
          @select-quick-access="setActiveQuickAccess"
          @select-category="setActiveCategory"
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
          title: { class: 'text-lg font-bold text-gray-900 dark:text-white' },
          closeButton: {
            class:
              'flex items-center justify-center w-8 h-8 rounded-full bg-gray-100 dark:bg-white/5 text-gray-500 hover:bg-gray-200 dark:hover:bg-white/10 transition-colors cursor-pointer shrink-0',
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
            <span class="font-bold text-lg text-gray-900 dark:text-gray-100">ResourceHub</span>
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
          @select-quick-access="
            (key) => {
              setActiveQuickAccess(key)
              mobileSidebarOpen = false
            }
          "
          @select-category="
            (id) => {
              setActiveCategory(id)
              mobileSidebarOpen = false
            }
          "
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
            <!-- 区域标题 -->
            <div class="flex items-center justify-between mb-6">
              <div>
                <h1 class="text-xl font-bold flex items-center text-gray-900 dark:text-gray-100">
                  <i class="fas fa-star text-yellow-400 mr-2 text-[18px]"></i>
                  {{
                    activeQuickAccess
                      ? quickAccessList.find((q) => q.key === activeQuickAccess)?.label || '收藏资源'
                      : activeCategoryId
                        ? categories.find((c) => c.id === activeCategoryId)?.name || '收藏资源'
                        : '收藏资源'
                  }}
                </h1>
                <p class="text-sm text-gray-500 dark:text-gray-500 mt-1">
                  精心收集的高质量网站和工具
                </p>
              </div>
            </div>

            <!-- 卡片网格/列表 -->
            <div
              v-if="filteredResources.length > 0"
              class="grid gap-6"
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
              class="py-20 text-center text-gray-500 dark:text-gray-500"
            >
              <i class="fas fa-inbox text-4xl mb-4 text-gray-300 dark:text-gray-700"></i>
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
