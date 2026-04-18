<script setup lang="ts">
import { computed, shallowRef } from 'vue'
import AnnouncementFilterBar from '@/components/user/announcement/AnnouncementFilterBar.vue'
import AnnouncementHero from '@/components/user/announcement/AnnouncementHero.vue'
import AnnouncementPinnedList from '@/components/user/announcement/AnnouncementPinnedList.vue'
import AnnouncementTimeline from '@/components/user/announcement/AnnouncementTimeline.vue'
import {
  ANNOUNCEMENT_FILTER_OPTIONS,
  ANNOUNCEMENT_MOCK_LIST,
  resolveAnnouncementFilterLabel,
  resolveVisibleAnnouncements,
  type AnnouncementFilterValue,
} from '@/components/user/announcement/announcement.mock'

defineOptions({
  name: 'AnnouncementView',
})

const activeType = shallowRef<AnnouncementFilterValue>('all')
const onlyTop = shallowRef(false)

const allAnnouncements = computed(() => resolveVisibleAnnouncements(ANNOUNCEMENT_MOCK_LIST))

const filteredAnnouncements = computed(() => {
  return allAnnouncements.value.filter((item) => {
    const matchesType = activeType.value === 'all' ? true : item.type === activeType.value
    const matchesTop = onlyTop.value ? item.isTop === 1 : true

    return matchesType && matchesTop
  })
})

const pinnedAnnouncements = computed(() => {
  return filteredAnnouncements.value.filter((item) => item.isTop === 1)
})

const activeTypeLabel = computed(() => resolveAnnouncementFilterLabel(activeType.value))

const latestPublishTime = computed(() => {
  const latestItem = filteredAnnouncements.value[0] || allAnnouncements.value[0]

  if (!latestItem) {
    return '-'
  }

  return `${latestItem.displayDate} ${latestItem.displayTime}`
})
</script>

<template>
  <div
    class="announcement-page min-h-screen bg-gray-50 text-gray-900 transition-colors duration-300 dark:bg-black dark:text-gray-100"
  >
    <div class="mx-auto flex w-full max-w-6xl flex-col gap-6 p-4 sm:p-6 lg:p-8">
      <AnnouncementHero
        :total-count="filteredAnnouncements.length"
        :latest-publish-time="latestPublishTime"
        :active-type-label="activeTypeLabel"
      />

      <AnnouncementFilterBar
        v-model="activeType"
        :only-top="onlyTop"
        :result-count="filteredAnnouncements.length"
        :options="ANNOUNCEMENT_FILTER_OPTIONS"
        @update:only-top="onlyTop = $event"
      />

      <AnnouncementPinnedList v-if="pinnedAnnouncements.length > 0" :items="pinnedAnnouncements" />

      <AnnouncementTimeline v-if="filteredAnnouncements.length > 0" :items="filteredAnnouncements" />

      <section
        v-else
        class="border border-dashed border-gray-300 bg-white px-6 py-14 text-center text-sm leading-7 text-gray-500 dark:border-gray-700 dark:bg-black dark:text-gray-400"
      >
        当前筛选条件下暂无公告，试试切换公告类型或关闭“仅看置顶”。
      </section>
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
