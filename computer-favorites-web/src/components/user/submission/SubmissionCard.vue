<script setup lang="ts">
import { computed } from 'vue'
import { Star, MoreVertical, ExternalLink } from 'lucide-vue-next'
import type { UserWebsiteSubmissionListItem } from '@/types/user-website-submission'

const props = defineProps<{
  item: UserWebsiteSubmissionListItem
  starred: boolean
}>()

const emit = defineEmits<{
  (e: 'star', id: number): void
  (e: 'edit', item: UserWebsiteSubmissionListItem): void
  (e: 'cancel', item: UserWebsiteSubmissionListItem): void
  (e: 'visit', item: UserWebsiteSubmissionListItem): void
}>()

const logoText = computed(() => {
  const name = (props.item.name || '').trim()
  return name ? name.slice(0, 2).toUpperCase() : 'NA'
})

const statusClass = computed(() => {
  switch (props.item.auditStatus) {
    case 1:
      return 'bg-green-100/70 text-green-600 dark:bg-green-500/10 dark:text-green-400'
    case 0:
      return 'bg-yellow-100/70 text-yellow-600 dark:bg-yellow-500/10 dark:text-yellow-400'
    case 2:
      return 'bg-red-100/70 text-red-600 dark:bg-red-500/10 dark:text-red-400'
    default:
      return 'bg-gray-100/70 text-gray-500 dark:bg-white/5 dark:text-gray-400'
  }
})

const statusText = computed(() => {
  switch (props.item.auditStatus) {
    case 0:
      return '待审核'
    case 1:
      return '已通过'
    case 2:
      return '已拒绝'
    default:
      return '未知'
  }
})

const formatDate = (time?: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').slice(0, 10)
}
</script>

<template>
  <div
    class="group relative flex flex-col overflow-hidden rounded-xl bg-white/70 p-4 backdrop-blur-md transition-all duration-300 hover:bg-white/80 dark:bg-[#091a30]/50 dark:hover:bg-[#091a30]/70"
  >
    <div class="flex items-start justify-between mb-4">
      <div class="flex items-center space-x-3 overflow-hidden">
        <div
          class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-[#06326d] text-base font-bold text-white"
        >
          {{ logoText }}
        </div>
        <div class="min-w-0">
          <h3
            class="truncate text-base font-semibold text-gray-800 transition-colors group-hover:text-[#06326d] dark:text-gray-100 dark:group-hover:text-[#4a90d9]"
          >
            {{ item.name }}
          </h3>
          <a
            v-if="item.url"
            :href="item.url"
            target="_blank"
            rel="noopener noreferrer"
            class="block truncate text-xs text-gray-500 no-underline transition-colors hover:text-[#06326d] dark:text-gray-400 dark:hover:text-[#4a90d9]"
            @click.stop
          >
            {{ item.url }}
          </a>
        </div>
      </div>
    </div>

    <div class="mb-6 flex-1">
      <span
        class="inline-block rounded-md bg-gray-100/80 px-2.5 py-1 text-xs text-gray-600 dark:bg-white/[0.05] dark:text-gray-400"
      >
        {{ item.categoryName || '未分类' }}
      </span>
    </div>

    <div
      class="mt-auto flex items-center justify-between pt-3"
    >
      <div class="flex items-center space-x-3">
        <span
          :class="[
            'rounded px-2 py-0.5 text-xs font-medium',
            statusClass,
          ]"
        >
          {{ statusText }}
        </span>
        <span
          class="font-mono text-xs text-gray-400 dark:text-gray-500"
        >
          {{ formatDate(item.updateTime) }}
        </span>
      </div>
      <div class="flex items-center space-x-1 text-gray-400">
        <button
          :class="[
            'cursor-pointer rounded p-1.5 transition-colors border-0 bg-transparent hover:bg-black/5 dark:hover:bg-white/10',
            starred ? 'text-[#06326d] dark:text-[#4a90d9]' : '',
          ]"
          title="收藏"
          @click.stop="emit('star', item.id)"
        >
          <Star
            :class="['h-[18px] w-[18px]', starred ? 'fill-[#06326d] dark:fill-[#4a90d9]' : '']"
          />
        </button>
        <button
          v-if="item.auditStatus === 0"
          class="cursor-pointer rounded p-1.5 transition-colors border-0 bg-transparent text-gray-400 hover:bg-black/5 hover:text-[#06326d] dark:hover:bg-white/10 dark:hover:text-[#4a90d9]"
          title="编辑"
          @click.stop="emit('edit', item)"
        >
          <ExternalLink class="h-[18px] w-[18px]" />
        </button>
        <button
          class="cursor-pointer rounded p-1.5 transition-colors border-0 bg-transparent text-gray-400 hover:bg-black/5 dark:hover:bg-white/10"
          title="更多"
          @click.stop
        >
          <MoreVertical class="h-[18px] w-[18px]" />
        </button>
      </div>
    </div>
  </div>
</template>
