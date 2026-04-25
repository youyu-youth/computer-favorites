<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 网站卡片组件 — 支持网格/列表两种形态，暗黑模式纯黑 + 微光边框
 */
import type { CollectionWebsite, ViewMode } from '@/types/collection'

defineOptions({ name: 'CollectionWebsiteCard' })

const props = defineProps<{
  item: CollectionWebsite
  viewMode: ViewMode
  isSelected: boolean
}>()

const emit = defineEmits<{
  (e: 'select', website: CollectionWebsite): void
  (e: 'toggle-star', id: number): void
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
</script>

<template>
  <!-- 网格模式 -->
  <div
    v-if="viewMode === 'grid'"
    class="bg-white dark:bg-[#0a0a0a] rounded-xl shadow-sm hover:shadow-md dark:shadow-none dark:hover:shadow-[0_0_0_1px_rgba(59,130,246,0.15),0_4px_16px_rgba(0,0,0,0.4)] transition-all duration-200 relative p-5 flex flex-col cursor-pointer group"
    :class="
      isSelected
        ? 'border-2 border-blue-500 dark:border-blue-400 dark:shadow-[0_0_0_1px_rgba(59,130,246,0.3),0_4px_20px_rgba(59,130,246,0.15)]'
        : 'border border-[#e5e7eb] dark:border-white/[0.06] hover:border-gray-300 dark:hover:border-white/[0.1]'
    "
    @click="emit('select', item)"
  >
    <!-- 星标按钮 -->
    <button
      type="button"
      class="absolute top-4 right-4 cursor-pointer z-10"
      @click.stop="emit('toggle-star', item.id)"
    >
      <i
        :class="[
          item.isStarred ? 'fas fa-star text-yellow-400' : 'far fa-star text-gray-300 dark:text-gray-700',
          'text-[18px] hover:text-yellow-400 transition-colors',
        ]"
      ></i>
    </button>

    <!-- 图标区域 -->
    <div
      class="w-12 h-12 bg-gray-100 dark:bg-white/[0.04] rounded-lg flex items-center justify-center mb-4 overflow-hidden border border-gray-100 dark:border-white/[0.06]"
    >
      <img
        v-if="item.icon"
        :src="item.icon"
        :alt="item.name"
        class="w-8 h-8 object-contain"
      />
      <i v-else class="fas fa-globe text-xl text-gray-400 dark:text-gray-600"></i>
    </div>

    <!-- 名称 -->
    <h3 class="font-bold text-lg mb-1 text-gray-900 dark:text-gray-100 pr-8 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">{{ item.name }}</h3>

    <!-- 描述 -->
    <p class="text-sm text-gray-500 dark:text-gray-500 mb-4 flex-1 line-clamp-2 leading-relaxed">
      {{ item.description }}
    </p>

    <!-- 标签 -->
    <div class="flex flex-wrap gap-2 mb-4">
      <span
        v-for="tag in item.tags"
        :key="tag"
        class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-50 text-blue-700 dark:bg-blue-500/[0.08] dark:text-blue-400/80 dark:shadow-[inset_0_0_0_1px_rgba(59,130,246,0.12)]"
      >
        {{ tag }}
      </span>
    </div>

    <!-- 底部信息 -->
    <div
      class="flex items-center justify-between mt-auto pt-4 border-t border-gray-100 dark:border-white/[0.06]"
    >
      <span class="text-xs text-gray-400 dark:text-gray-600 hover:text-blue-500 dark:hover:text-blue-400 transition-colors cursor-pointer">
        {{ extractDomain(item.url) }}
      </span>
      <div class="flex items-center text-xs text-gray-400 dark:text-gray-600">
        <i
          :class="[
            item.isStarred ? 'fas fa-heart text-red-500 dark:text-red-400' : 'far fa-heart',
            'text-[13px] mr-1',
          ]"
        ></i>
        <span class="tabular-nums">{{ formatLikeCount(item.likeCount) }}</span>
      </div>
    </div>
  </div>

  <!-- 列表模式 -->
  <div
    v-else
    class="bg-white dark:bg-[#0a0a0a] rounded-lg border shadow-sm hover:shadow-md dark:shadow-none dark:hover:shadow-[0_0_0_1px_rgba(59,130,246,0.15),0_2px_12px_rgba(0,0,0,0.4)] transition-all duration-200 relative flex items-center px-5 py-3 cursor-pointer group"
    :class="
      isSelected
        ? 'border-2 border-blue-500 dark:border-blue-400 dark:shadow-[0_0_0_1px_rgba(59,130,246,0.3),0_4px_20px_rgba(59,130,246,0.15)]'
        : 'border-[#e5e7eb] dark:border-white/[0.06] hover:border-gray-300 dark:hover:border-white/[0.1]'
    "
    @click="emit('select', item)"
  >
    <!-- 图标 -->
    <div
      class="w-10 h-10 bg-gray-100 dark:bg-white/[0.04] rounded-lg flex items-center justify-center mr-4 shrink-0 overflow-hidden border border-gray-100 dark:border-white/[0.06]"
    >
      <img
        v-if="item.icon"
        :src="item.icon"
        :alt="item.name"
        class="w-6 h-6 object-contain"
      />
      <i v-else class="fas fa-globe text-lg text-gray-400 dark:text-gray-600"></i>
    </div>

    <!-- 名称 + 描述 -->
    <div class="flex-1 min-w-0">
      <h3 class="font-bold text-base text-gray-900 dark:text-gray-100 truncate group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
        {{ item.name }}
      </h3>
      <p class="text-sm text-gray-500 dark:text-gray-500 truncate">
        {{ item.description }}
      </p>
    </div>

    <!-- 标签 -->
    <div class="hidden lg:flex flex-wrap gap-1.5 mx-4 max-w-[200px]">
      <span
        v-for="tag in item.tags.slice(0, 2)"
        :key="tag"
        class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-50 text-blue-700 dark:bg-blue-500/[0.08] dark:text-blue-400/80 dark:shadow-[inset_0_0_0_1px_rgba(59,130,246,0.12)] whitespace-nowrap"
      >
        {{ tag }}
      </span>
      <span
        v-if="item.tags.length > 2"
        class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-500 dark:bg-white/[0.04] dark:text-gray-500 dark:shadow-[inset_0_0_0_1px_rgba(255,255,255,0.06)]"
      >
        +{{ item.tags.length - 2 }}
      </span>
    </div>

    <!-- 域名 -->
    <span class="hidden sm:block text-xs text-gray-400 dark:text-gray-600 mx-3 max-w-[120px] truncate">
      {{ extractDomain(item.url) }}
    </span>

    <!-- 点赞数 -->
    <div class="flex items-center text-xs text-gray-400 dark:text-gray-600 mx-3 whitespace-nowrap">
      <i
        :class="[
          item.isStarred ? 'fas fa-heart text-red-500 dark:text-red-400' : 'far fa-heart',
          'text-[13px] mr-1',
        ]"
      ></i>
      <span class="tabular-nums">{{ formatLikeCount(item.likeCount) }}</span>
    </div>

    <!-- 星标按钮 -->
    <button
      type="button"
      class="cursor-pointer ml-2 shrink-0"
      @click.stop="emit('toggle-star', item.id)"
    >
      <i
        :class="[
          item.isStarred ? 'fas fa-star text-yellow-400' : 'far fa-star text-gray-300 dark:text-gray-700',
          'text-[16px] hover:text-yellow-400 transition-colors',
        ]"
      ></i>
    </button>
  </div>
</template>
