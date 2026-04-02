<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import WebsitesBreadcrumbs from '@/components/admin/websites/WebsitesBreadcrumbs.vue'
import WebsitesStatsToolbar from '@/components/admin/websites/WebsitesStatsToolbar.vue'
import WebsitesSearchArea from '@/components/admin/websites/WebsitesSearchArea.vue'
import WebsitesMiniCards from '@/components/admin/websites/WebsitesMiniCards.vue'
import WebsitesList from '@/components/admin/websites/WebsitesList.vue'
import WebsitesPagination from '@/components/admin/websites/WebsitesPagination.vue'
import AdminAddWebsite from '@/components/admin/websites/AdminAddWebsite.vue'
import { useWebsitesData } from '@/composables/admin/useWebsitesData'
import { useAdminNavStore } from '@/stores/adminNav'

const adminNavStore = useAdminNavStore()
const { activeMenu, activeMenuLabel } = storeToRefs(adminNavStore)
const router = useRouter()

const isWebsiteMenuActive = computed(() => activeMenu.value === 'websites')

const placeholderDescription = computed(() => {
  if (activeMenu.value === 'dashboard') {
    return '仪表盘能力正在开发中，当前版本先完成网站治理与分类筛选链路。'
  }
  if (activeMenu.value === 'users') {
    return '用户管理模块正在排期中，当前仅展示导航结构用于联调布局。'
  }
  if (activeMenu.value === 'comments') {
    return '评论管理模块正在排期中，后续会接入审核与举报处理流程。'
  }
  return '当前模块正在准备中。'
})

const {
  tools,
  connectors,
  searchQuery,
  viewMode,
  deletedFilter,
  stats,
  selectedIds,
  selectedCount,
  allSelectableSelected,
  updatingWebsiteIds,
  batchStatusUpdating,
  currentPage,
  totalItems,
  pageSize,
  loading,
  errorMessage,
  totalPages,
  visiblePages,
  prevPage,
  nextPage,
  goToPage,
  reloadData,
  filteredServers,
  toggleSelect,
  toggleSelectAll,
  clearSelection,
  updateWebsiteStatus,
  batchUpdateWebsiteStatus,
} = useWebsitesData()

const selectableTotal = computed(() => filteredServers.value.filter((item) => item.deleted !== 1).length)

const handleViewDetail = (websiteId: number) => {
  void router.push({
    name: 'adminWebsiteDetail',
    params: { id: String(websiteId) },
  })
}

</script>

<template>
  <main class="flex-grow w-full px-4 sm:px-6 lg:px-8 py-6">
    <div class="max-w-[1320px] mx-auto min-w-0">
      <WebsitesBreadcrumbs :viewMode="viewMode" />

      <template v-if="isWebsiteMenuActive">
        <Transition name="fade-slide" mode="out-in">
          <div v-if="viewMode === 'add'" key="add">
            <AdminAddWebsite @cancel="viewMode = 'grid'" />
          </div>

          <div v-else key="list">
            <WebsitesStatsToolbar
              :viewMode="viewMode"
              :deletedFilter="deletedFilter"
              :stats="stats"
              :selectedCount="selectedCount"
              :selectableTotal="selectableTotal"
              :allSelectableSelected="allSelectableSelected"
              :batchStatusUpdating="batchStatusUpdating"
              @update:viewMode="(value) => (viewMode = value)"
              @update:deletedFilter="(value) => (deletedFilter = value)"
              @refresh="reloadData"
              @toggle-select-all="toggleSelectAll"
              @clear-selection="clearSelection"
              @batch-status="batchUpdateWebsiteStatus"
            />

            <WebsitesSearchArea
            :searchQuery="searchQuery"
            @update:searchQuery="(value) => (searchQuery = value)"
          />

          <WebsitesMiniCards title="MCP tools" :items="tools" />

          <WebsitesMiniCards title="MCP Connectors" :items="connectors" is-connector />

          <div>
            <h2 class="text-gray-500 dark:text-gray-400 text-sm font-medium mb-4">Popular MCP servers</h2>

            <div
              v-if="loading && filteredServers.length > 0"
              class="mb-4 rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-xs text-gray-600 dark:border-dark-border dark:bg-dark-card dark:text-gray-300"
            >
              <i class="fas fa-sync-alt fa-spin mr-2"></i>
              正在同步最新网站状态...
            </div>

            <div v-if="loading && filteredServers.length === 0" class="text-center py-12 text-gray-500 dark:text-gray-400">
              <i class="fas fa-spinner fa-spin text-3xl mb-3 opacity-70"></i>
              <p>Loading website data...</p>
            </div>

            <div v-else-if="errorMessage && filteredServers.length === 0" class="text-center py-12 text-red-500 dark:text-red-400">
              <i class="fas fa-exclamation-circle text-3xl mb-3 opacity-80"></i>
              <p>{{ errorMessage }}</p>
            </div>

            <WebsitesList
              v-else
              :servers="filteredServers"
              :viewMode="viewMode"
              :selectedIds="selectedIds"
              :updatingWebsiteIds="updatingWebsiteIds"
              @toggle-select="toggleSelect"
              @update-status="({ websiteId, status }) => updateWebsiteStatus(websiteId, status)"
              @view-detail="handleViewDetail"
            />

            <div v-if="!loading && !errorMessage && filteredServers.length === 0" class="text-center py-12 text-gray-500 dark:text-gray-400">
              <i class="fas fa-search text-3xl mb-3 opacity-50"></i>
              <p>No servers found matching "{{ searchQuery }}"</p>
            </div>

            <WebsitesPagination
              v-if="!loading && !errorMessage && filteredServers.length > 0"
              :currentPage="currentPage"
              :totalPages="totalPages"
              :total="totalItems"
              :pageSize="pageSize"
              :visiblePages="visiblePages"
              @prev="prevPage"
              @next="nextPage"
              @goto="goToPage"
            />
          </div>
          </div>
        </Transition>
      </template>

      <div v-else class="rounded-xl border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card px-6 py-12 text-center">
        <div class="w-16 h-16 mx-auto rounded-full bg-orange-50 dark:bg-orange-900/10 text-brand-orange flex items-center justify-center mb-5">
          <i class="fas fa-screwdriver-wrench text-2xl"></i>
        </div>
        <h2 class="text-xl font-semibold text-gray-900 dark:text-gray-100">{{ activeMenuLabel }}</h2>
        <p class="mt-3 text-sm text-gray-600 dark:text-gray-300">
          {{ placeholderDescription }}
        </p>
        <p class="mt-5 inline-flex items-center rounded-full border border-orange-200 dark:border-orange-900/30 bg-orange-50 dark:bg-orange-900/10 text-brand-orange text-xs font-semibold px-3 py-1">
          页面功能开发中
        </p>
      </div>
    </div>
  </main>
</template>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(15px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-15px);
}
</style>

