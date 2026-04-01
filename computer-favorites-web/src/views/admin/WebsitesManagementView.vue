<script setup lang="ts">
import WebsitesSidebar from '@/components/admin/websites/WebsitesSidebar.vue'
import WebsitesBreadcrumbs from '@/components/admin/websites/WebsitesBreadcrumbs.vue'
import WebsitesStatsToolbar from '@/components/admin/websites/WebsitesStatsToolbar.vue'
import WebsitesSearchArea from '@/components/admin/websites/WebsitesSearchArea.vue'
import WebsitesMiniCards from '@/components/admin/websites/WebsitesMiniCards.vue'
import WebsitesList from '@/components/admin/websites/WebsitesList.vue'
import WebsitesPagination from '@/components/admin/websites/WebsitesPagination.vue'
import { useWebsitesData } from '@/composables/admin/useWebsitesData'

const {
    categories,
    tools,
    connectors,
    searchQuery,
    viewMode,
  deletedFilter,
  stats,
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
  selectCategory,
    filteredServers
} = useWebsitesData()

</script>

<template>
  <main class="flex-grow max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 flex flex-col md:flex-row gap-8">

    <!-- 左侧边栏 -->
    <WebsitesSidebar :categories="categories" @select="selectCategory" />

    <!-- 右侧主要内容区 -->
    <div class="flex-grow min-w-0">

      <!-- 面包屑与次级导航 -->
      <WebsitesBreadcrumbs />

      <!-- 统计与操作栏 -->
      <WebsitesStatsToolbar
        :viewMode="viewMode"
        :deletedFilter="deletedFilter"
        :stats="stats"
        @update:viewMode="(value) => (viewMode = value)"
        @update:deletedFilter="(value) => (deletedFilter = value)"
        @refresh="reloadData"
      />

      <!-- 搜索框与公告 -->
      <WebsitesSearchArea
        :searchQuery="searchQuery"
        @update:searchQuery="(value) => (searchQuery = value)"
      />

      <!-- 小工具区 -->
      <WebsitesMiniCards title="MCP tools" :items="tools" />

      <!-- 连接器区 -->
      <WebsitesMiniCards title="MCP Connectors" :items="connectors" is-connector />

      <!-- 主要服务器列表区 -->
      <div>
        <h2 class="text-gray-500 dark:text-gray-400 text-sm font-medium mb-4">Popular MCP servers</h2>

        <div v-if="loading" class="text-center py-12 text-gray-500 dark:text-gray-400">
          <i class="fas fa-spinner fa-spin text-3xl mb-3 opacity-70"></i>
          <p>Loading website data...</p>
        </div>

        <div v-else-if="errorMessage" class="text-center py-12 text-red-500 dark:text-red-400">
          <i class="fas fa-exclamation-circle text-3xl mb-3 opacity-80"></i>
          <p>{{ errorMessage }}</p>
        </div>

        <WebsitesList v-else :servers="filteredServers" :viewMode="viewMode" />

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
  </main>
</template>

