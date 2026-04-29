<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 网站卡片组件 — 支持网格/列表两种形态，直角框，高对比度暗黑模式
 */
import type { CollectionWebsite, ViewMode } from '@/types/collection'

defineOptions({ name: 'CollectionWebsiteCard' })

defineProps<{
  item: CollectionWebsite
  viewMode: ViewMode
  isSelected: boolean
}>()

const emit = defineEmits<{
  (e: 'select', website: CollectionWebsite): void
  (e: 'cancel-collect', websiteId: number): void
}>()

const formatLikeCount = (count: number): string => {
  if (count >= 1000000) {
    return `${(count / 1000000).toFixed(1).replace(/\.0$/, '')}m`
  }
  if (count >= 1000) {
    return `${(count / 1000).toFixed(1).replace(/\.0$/, '')}k`
  }
  return String(count)
}

const extractDomain = (url: string): string => {
  try {
    return new URL(url).hostname
  } catch {
    return url
  }
}

const getTagStyleVars = (color: string): Record<string, string> => {
  const r = parseInt(color.slice(1, 3), 16)
  const g = parseInt(color.slice(3, 5), 16)
  const b = parseInt(color.slice(5, 7), 16)
  return {
    '--tag-bg': `rgba(${r}, ${g}, ${b}, 0.1)`,
    '--tag-text': color,
    '--tag-bg-dark': `rgba(${r}, ${g}, ${b}, 0.08)`,
    '--tag-text-dark': `rgba(${r}, ${g}, ${b}, 0.8)`,
    '--tag-border-dark': `rgba(${r}, ${g}, ${b}, 0.12)`,
  } as Record<string, string>
}
</script>

<template>
  <!-- 网格模式 -->
  <div
    v-if="viewMode === 'grid'"
    class="bg-white dark:bg-[#121212] rounded-none shadow-sm hover:shadow-md dark:shadow-none dark:hover:shadow-[0_0_0_1px_rgba(59,130,246,0.2),0_4px_20px_rgba(0,0,0,0.5)] transition-all duration-200 relative p-5 flex flex-col cursor-pointer group"
    :class="
      isSelected
        ? 'border-2 border-blue-500 dark:border-blue-400 dark:shadow-[0_0_0_1px_rgba(59,130,246,0.5),0_0_24px_rgba(59,130,246,0.2)]'
        : 'border border-[#d4d4d4] dark:border-white/[0.1] hover:border-gray-400 dark:hover:border-white/[0.18] dark:hover:bg-[#161616]'
    "
    @click="emit('select', item)"
  >
    <!-- 取消收藏按钮 -->
    <button
      type="button"
      class="absolute top-3.5 right-3.5 cursor-pointer z-10"
      @click.stop="emit('cancel-collect', item.websiteId)"
    >
      <i
        class="fas fa-bookmark text-[17px] text-yellow-500 dark:text-yellow-400 hover:text-yellow-300 transition-colors"
      ></i>
    </button>

    <!-- 图标区域 -->
    <div
      class="w-11 h-11 bg-gray-100 dark:bg-white/[0.06] rounded-none flex items-center justify-center mb-4 overflow-hidden border border-gray-200 dark:border-white/[0.08]"
    >
      <img
        v-if="item.websiteIcon"
        :src="item.websiteIcon"
        :alt="item.websiteName"
        class="w-7 h-7 object-contain"
      />
      <i v-else class="fas fa-globe text-lg text-gray-400 dark:text-gray-500"></i>
    </div>

    <!-- 名称 -->
    <h3 class="font-bold text-lg mb-1 text-gray-900 dark:text-gray-100 pr-8 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">{{ item.websiteName }}</h3>

    <!-- 描述 -->
    <p class="text-sm text-gray-500 dark:text-gray-400 mb-4 flex-1 line-clamp-2 leading-relaxed">
      {{ item.websiteSummary }}
    </p>

    <!-- 标签 -->
    <div class="flex flex-wrap gap-2 mb-4">
      <span
        v-for="tag in item.websiteTags?.slice(0, 3)"
        :key="tag.id"
        class="collect-tag inline-flex items-center px-2 py-0.5 rounded-none text-xs font-medium"
        :style="getTagStyleVars(tag.color)"
      >
        {{ tag.name }}
      </span>
    </div>

    <!-- 底部信息 -->
    <div
      class="flex items-center justify-between mt-auto pt-4 border-t border-gray-200 dark:border-white/[0.08]"
    >
      <span class="text-xs text-gray-500 dark:text-gray-500 hover:text-blue-500 dark:hover:text-blue-400 transition-colors cursor-pointer">
        {{ extractDomain(item.websiteUrl) }}
      </span>

    </div>
  </div>

  <!-- 列表模式 -->
  <div
    v-else
    class="bg-white dark:bg-[#121212] rounded-none border shadow-sm hover:shadow-md dark:shadow-none dark:hover:shadow-[0_0_0_1px_rgba(59,130,246,0.2),0_2px_12px_rgba(0,0,0,0.5)] transition-all duration-200 relative flex items-center px-5 py-3.5 cursor-pointer group"
    :class="
      isSelected
        ? 'border-2 border-blue-500 dark:border-blue-400 dark:shadow-[0_0_0_1px_rgba(59,130,246,0.5),0_0_24px_rgba(59,130,246,0.2)]'
        : 'border-[#d4d4d4] dark:border-white/[0.1] hover:border-gray-400 dark:hover:border-white/[0.18] dark:hover:bg-[#161616]'
    "
    @click="emit('select', item)"
  >
    <!-- 图标 -->
    <div
      class="w-10 h-10 bg-gray-100 dark:bg-white/[0.06] rounded-none flex items-center justify-center mr-4 shrink-0 overflow-hidden border border-gray-200 dark:border-white/[0.08]"
    >
      <img
        v-if="item.websiteIcon"
        :src="item.websiteIcon"
        :alt="item.websiteName"
        class="w-6 h-6 object-contain"
      />
      <i v-else class="fas fa-globe text-lg text-gray-400 dark:text-gray-500"></i>
    </div>

    <!-- 名称 + 描述 -->
    <div class="flex-1 min-w-0">
      <h3 class="font-bold text-base text-gray-900 dark:text-gray-100 truncate group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
        {{ item.websiteName }}
      </h3>
      <p class="text-sm text-gray-500 dark:text-gray-400 truncate">
        {{ item.websiteSummary }}
      </p>
    </div>

    <!-- 标签 -->
    <div class="hidden lg:flex flex-wrap gap-1.5 mx-4 max-w-[200px]">
      <span
        v-for="tag in item.websiteTags?.slice(0, 2)"
        :key="tag.id"
        class="collect-tag inline-flex items-center px-2 py-0.5 rounded-none text-xs font-medium whitespace-nowrap"
        :style="getTagStyleVars(tag.color)"
      >
        {{ tag.name }}
      </span>
      <span
        v-if="(item.websiteTags?.length ?? 0) > 2"
        class="inline-flex items-center px-2 py-0.5 rounded-none text-xs font-medium bg-gray-100 text-gray-500 dark:bg-white/[0.05] dark:text-gray-500"
      >
        +{{ item.websiteTags.length - 2 }}
      </span>
    </div>

    <!-- 域名 -->
    <span class="hidden sm:block text-xs text-gray-500 dark:text-gray-500 mx-3 max-w-[120px] truncate">
      {{ extractDomain(item.websiteUrl) }}
    </span>

    <!-- 取消收藏按钮 -->
    <button
      type="button"
      class="cursor-pointer ml-2 shrink-0"
      @click.stop="emit('cancel-collect', item.websiteId)"
    >
      <i
        class="fas fa-bookmark text-[16px] text-yellow-500 dark:text-yellow-400 hover:text-yellow-300 transition-colors"
      ></i>
    </button>
  </div>
</template>

<style scoped>
.collect-tag {
  background-color: var(--tag-bg);
  color: var(--tag-text);
}

:root.dark .collect-tag {
  background-color: var(--tag-bg-dark);
  color: var(--tag-text-dark);
  box-shadow: inset 0 0 0 1px var(--tag-border-dark);
}
</style>
