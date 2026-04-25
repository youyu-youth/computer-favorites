<script setup lang="ts">
import { onMounted } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import AnnouncementFilterBar from '@/components/user/announcement/AnnouncementFilterBar.vue'
import AnnouncementHero from '@/components/user/announcement/AnnouncementHero.vue'
import AnnouncementPinnedList from '@/components/user/announcement/AnnouncementPinnedList.vue'
import AnnouncementTimeline from '@/components/user/announcement/AnnouncementTimeline.vue'
import { useUserAnnouncementList } from '@/composables/useUserAnnouncementList'
import {
  ANNOUNCEMENT_FILTER_OPTIONS,
  type AnnouncementFilterValue,
} from '@/types/user-announcement'

defineOptions({
  name: 'AnnouncementView',
})

const {
  loading,
  total,
  totalPages,
  query,
  pagedAnnouncements,
  pinnedAnnouncements,
  hasMore,
  activeType,
  activeTypeLabel,
  latestPublishTime,
  refreshData,
  setFilterType,
  setIsTop,
  nextPage,
  prevPage,
  goToPage,
} = useUserAnnouncementList()

function handleFilterTypeChange(value: AnnouncementFilterValue) {
  setFilterType(value === 'all' ? null : value)
}

function handleOnlyTopChange(value: boolean) {
  setIsTop(value ? true : null)
}

function handlePageClick(page: number) {
  goToPage(page)
}

onMounted(() => {
  refreshData()
})
</script>

<template>
  <div
    class="announcement-page min-h-screen bg-gray-50 text-gray-900 transition-colors duration-300 dark:bg-black dark:text-gray-100"
  >
    <div class="mx-auto flex w-full max-w-6xl flex-col gap-6 p-4 sm:p-6 lg:p-8">
      <AnnouncementHero
        :total-count="total"
        :latest-publish-time="latestPublishTime"
        :active-type-label="activeTypeLabel"
      />

      <AnnouncementFilterBar
        :model-value="activeType"
        :only-top="query.isTop === 1"
        :result-count="total"
        :options="ANNOUNCEMENT_FILTER_OPTIONS"
        @update:model-value="handleFilterTypeChange"
        @update:only-top="handleOnlyTopChange"
      />

      <div v-if="loading" class="flex items-center justify-center py-20">
        <div
          class="h-8 w-8 animate-spin rounded-full border-2 border-amber-500 border-t-transparent"
        />
      </div>

      <template v-else>
        <AnnouncementPinnedList v-if="pinnedAnnouncements.length > 0" :items="pinnedAnnouncements" />

        <AnnouncementTimeline v-if="pagedAnnouncements.length > 0" :items="pagedAnnouncements" />

        <section
          v-if="pagedAnnouncements.length === 0"
          class="border border-dashed border-gray-300 bg-white px-6 py-14 text-center text-sm leading-7 text-gray-500 dark:border-gray-700 dark:bg-black dark:text-gray-400"
        >
          当前筛选条件下暂无公告，试试切换公告类型或关闭"仅看置顶"。
        </section>

        <section
          v-if="totalPages > 1"
          class="flex items-center justify-center gap-3 py-4 text-sm text-gray-600 dark:text-gray-400"
        >
          <button
            class="flex cursor-pointer items-center gap-1 rounded-md px-3 py-1.5 transition-colors hover:bg-gray-200 disabled:cursor-not-allowed disabled:opacity-40 dark:hover:bg-gray-800"
            :disabled="(query.pageNum ?? 1) <= 1"
            @click="prevPage"
          >
            <ChevronLeft class="h-4 w-4" />
            上一页
          </button>

          <template v-for="page in totalPages" :key="page">
            <button
              v-if="page === 1 || page === totalPages || Math.abs(page - (query.pageNum ?? 1)) <= 1"
              class="h-8 w-8 cursor-pointer rounded-md text-sm transition-colors"
              :class="
                page === (query.pageNum ?? 1)
                  ? 'bg-amber-500 font-medium text-white'
                  : 'hover:bg-gray-200 dark:hover:bg-gray-800'
              "
              @click="handlePageClick(page)"
            >
              {{ page }}
            </button>
            <span
              v-else-if="
                page === 2 && (query.pageNum ?? 1) > 3 ||
                page === totalPages - 1 && (query.pageNum ?? 1) < totalPages - 2
              "
              class="px-1 text-gray-400"
            >
              ...
            </span>
          </template>

          <button
            class="flex cursor-pointer items-center gap-1 rounded-md px-3 py-1.5 transition-colors hover:bg-gray-200 disabled:cursor-not-allowed disabled:opacity-40 dark:hover:bg-gray-800"
            :disabled="!hasMore"
            @click="nextPage"
          >
            下一页
            <ChevronRight class="h-4 w-4" />
          </button>
        </section>
      </template>
    </div>
  </div>
</template>

<style scoped>
.announcement-page {
  font-family:
    'JetBrains Mono',
    'IBM Plex Sans',
    'SFMono-Regular',
    'Fira Code',
    'PingFang SC',
    'Microsoft YaHei',
    'Noto Sans SC',
    monospace;
}
</style>
