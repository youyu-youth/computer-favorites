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
    currentPage,
    totalPages,
    visiblePages,
    prevPage,
    nextPage,
    goToPage,
    filteredServers
} = useWebsitesData()

</script>

<template>
  <main class="flex-grow max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 flex flex-col md:flex-row gap-8">

    <!-- 左侧边栏 -->
    <WebsitesSidebar :categories="categories" />

    <!-- 右侧主要内容区 -->
    <div class="flex-grow min-w-0">

      <!-- 面包屑与次级导航 -->
      <WebsitesBreadcrumbs />

      <!-- 统计与操作栏 -->
      <WebsitesStatsToolbar v-model:viewMode="viewMode" />

      <!-- 搜索框与公告 -->
      <WebsitesSearchArea v-model:searchQuery="searchQuery" />

      <!-- 小工具区 -->
      <WebsitesMiniCards title="MCP tools" :items="tools" />

      <!-- 连接器区 -->
      <WebsitesMiniCards title="MCP Connectors" :items="connectors" is-connector />

      <!-- 主要服务器列表区 -->
      <div>
        <h2 class="text-gray-500 dark:text-gray-400 text-sm font-medium mb-4">Popular MCP servers</h2>

        <WebsitesList :servers="filteredServers" :viewMode="viewMode" />

        <div v-if="filteredServers.length === 0" class="text-center py-12 text-gray-500 dark:text-gray-400">
            <i class="fas fa-search text-3xl mb-3 opacity-50"></i>
            <p>No servers found matching "{{ searchQuery }}"</p>
        </div>

        <WebsitesPagination
            v-if="filteredServers.length > 0"
            :currentPage="currentPage"
            :totalPages="totalPages"
            :visiblePages="visiblePages"
            @prev="prevPage"
            @next="nextPage"
            @goto="goToPage"
        />
      </div>

    </div>
  </main>
</template>

