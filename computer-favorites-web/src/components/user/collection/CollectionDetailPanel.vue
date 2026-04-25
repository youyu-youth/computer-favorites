<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 右侧详情面板 — 网站详细信息（可切换显示/隐藏），暗黑模式纯黑
 */
import type { CollectionWebsite } from '@/types/collection'

defineOptions({ name: 'CollectionDetailPanel' })

defineProps<{
  website: CollectionWebsite | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'toggle-star', id: number): void
  (e: 'visit', url: string): void
}>()

const extractDomain = (url: string): string => {
  try {
    return new URL(url).hostname
  } catch {
    return url
  }
}
</script>

<template>
  <aside
    class="w-80 bg-white dark:bg-black border-l border-[#e5e7eb] dark:border-white/[0.06] flex flex-col shrink-0 overflow-y-auto h-full"
  >
    <template v-if="website">
      <div class="p-6">
        <!-- 顶部信息 -->
        <div class="flex items-start mb-6">
          <div
            class="w-16 h-16 bg-gray-100 dark:bg-white/[0.04] rounded-xl flex items-center justify-center mr-4 shrink-0 border border-gray-200 dark:border-white/[0.06] overflow-hidden"
          >
            <img
              v-if="website.icon"
              :src="website.icon"
              :alt="website.name"
              class="w-10 h-10 object-contain"
            />
            <i v-else class="fas fa-globe text-2xl text-gray-400 dark:text-gray-600"></i>
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex items-start justify-between">
              <h2 class="text-xl font-bold mb-1 text-gray-900 dark:text-gray-100">
                {{ website.name }}
              </h2>
              <button
                type="button"
                class="text-gray-400 dark:text-gray-600 hover:text-gray-700 dark:hover:text-gray-300 cursor-pointer ml-2 mt-0.5 p-1 rounded-md hover:bg-gray-100 dark:hover:bg-white/[0.04] transition-colors"
                title="关闭面板"
                @click="emit('close')"
              >
                <i class="fas fa-xmark text-lg"></i>
              </button>
            </div>
            <span class="text-sm text-gray-500 dark:text-gray-600 break-all">
              {{ extractDomain(website.url) }}
            </span>
          </div>
        </div>

        <!-- 访问按钮 -->
        <button
          type="button"
          class="w-full bg-blue-500 hover:bg-blue-600 dark:bg-blue-500 dark:hover:bg-blue-400 text-white font-medium py-2.5 px-4 rounded-lg flex items-center justify-center mb-4 transition-all duration-200 cursor-pointer active:scale-[0.97] shadow-sm dark:shadow-[0_2px_8px_rgba(59,130,246,0.2)]"
          @click="emit('visit', website.url)"
        >
          访问网站
          <i class="fas fa-external-link-alt text-[13px] ml-2"></i>
        </button>

        <!-- 操作按钮组 -->
        <div class="grid grid-cols-3 gap-2 mb-6 border-b border-[#e5e7eb] dark:border-white/[0.06] pb-6">
          <button
            type="button"
            class="flex items-center justify-center py-2.5 border border-[#e5e7eb] dark:border-white/[0.06] rounded-lg hover:bg-gray-50 dark:hover:bg-white/[0.04] cursor-pointer transition-all duration-200 active:scale-[0.95]"
            :class="website.isStarred ? 'text-red-500 dark:text-red-400' : 'text-gray-400 dark:text-gray-600'"
            @click="emit('toggle-star', website.id)"
          >
            <i :class="website.isStarred ? 'fas fa-heart' : 'far fa-heart'" class="text-[18px]"></i>
          </button>
          <button
            type="button"
            class="flex items-center justify-center py-2.5 border border-[#e5e7eb] dark:border-white/[0.06] rounded-lg hover:bg-gray-50 dark:hover:bg-white/[0.04] text-gray-400 dark:text-gray-600 cursor-pointer transition-all duration-200 active:scale-[0.95]"
          >
            <i class="far fa-bookmark text-[18px]"></i>
          </button>
          <button
            type="button"
            class="flex items-center justify-center py-2.5 border border-[#e5e7eb] dark:border-white/[0.06] rounded-lg hover:bg-gray-50 dark:hover:bg-white/[0.04] text-gray-400 dark:text-gray-600 cursor-pointer transition-all duration-200 active:scale-[0.95]"
          >
            <i class="fas fa-share-alt text-[18px]"></i>
          </button>
        </div>

        <!-- 详细信息 -->
        <div class="space-y-4 text-sm">
          <p class="text-gray-700 dark:text-gray-300 leading-relaxed">
            {{ website.description }}
          </p>

          <!-- 标签 -->
          <div>
            <h4
              class="text-gray-400 dark:text-gray-600 mb-2 text-xs font-medium uppercase tracking-wider"
            >
              标签
            </h4>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="tag in website.tags"
                :key="tag"
                class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-50 text-blue-700 dark:bg-blue-500/[0.08] dark:text-blue-400/80 dark:shadow-[inset_0_0_0_1px_rgba(59,130,246,0.12)]"
              >
                {{ tag }}
              </span>
            </div>
          </div>

          <!-- 分类 -->
          <div>
            <h4
              class="text-gray-400 dark:text-gray-600 mb-1 text-xs font-medium uppercase tracking-wider"
            >
              分类
            </h4>
            <p class="text-gray-900 dark:text-gray-300">{{ website.category }}</p>
          </div>

          <!-- 收藏日期 -->
          <div>
            <h4
              class="text-gray-400 dark:text-gray-600 mb-1 text-xs font-medium uppercase tracking-wider"
            >
              收藏日期
            </h4>
            <p class="text-gray-900 dark:text-gray-300 tabular-nums">{{ website.dateAdded }}</p>
          </div>

          <!-- 最后访问 -->
          <div>
            <h4
              class="text-gray-400 dark:text-gray-600 mb-1 text-xs font-medium uppercase tracking-wider"
            >
              最后访问
            </h4>
            <p class="text-gray-900 dark:text-gray-300">{{ website.lastVisited }}</p>
          </div>

          <!-- 热度 -->
          <div>
            <h4
              class="text-gray-400 dark:text-gray-600 mb-1 text-xs font-medium uppercase tracking-wider"
            >
              热度
            </h4>
            <p class="flex items-center text-orange-500 dark:text-orange-400 font-medium">
              <i class="fas fa-fire text-[14px] mr-1.5"></i>
              <span class="tabular-nums">{{ website.likeCount >= 1000 ? `${(website.likeCount / 1000).toFixed(1).replace(/\.0$/, '')}k` : website.likeCount }}</span>
            </p>
          </div>
        </div>

        <!-- 网站预览 -->
        <div
          class="mt-6 border border-[#e5e7eb] dark:border-white/[0.06] rounded-lg overflow-hidden bg-gray-50 dark:bg-white/[0.02]"
        >
          <div
            class="h-32 bg-gray-200 dark:bg-white/[0.03] flex items-center justify-center text-gray-400 dark:text-gray-600 text-xs relative"
          >
            <img
              v-if="website.icon"
              :src="website.icon"
              :alt="`${website.name} 预览`"
              class="absolute inset-0 w-full h-full object-cover opacity-30 dark:opacity-20"
            />
            <span
              class="relative z-10 font-medium bg-white/80 dark:bg-black/60 px-2.5 py-1 rounded-md backdrop-blur-sm"
            >
              <i class="fas fa-eye mr-1"></i>
              预览
            </span>
          </div>
        </div>
      </div>
    </template>

    <!-- 空状态 -->
    <template v-else>
      <div class="flex-1 flex flex-col items-center justify-center p-6 text-center">
        <div class="w-16 h-16 rounded-2xl bg-gray-100 dark:bg-white/[0.03] flex items-center justify-center mb-4 border border-gray-200 dark:border-white/[0.06]">
          <i class="fas fa-mouse-pointer text-2xl text-gray-300 dark:text-gray-700"></i>
        </div>
        <p class="text-sm text-gray-400 dark:text-gray-600 mb-1">选择一个资源查看详情</p>
        <p class="text-xs text-gray-300 dark:text-gray-700">点击左侧卡片即可预览</p>
        <button
          type="button"
          class="mt-6 text-xs text-gray-400 dark:text-gray-600 hover:text-gray-700 dark:hover:text-gray-300 cursor-pointer px-3 py-1.5 rounded-md hover:bg-gray-100 dark:hover:bg-white/[0.04] transition-colors"
          @click="emit('close')"
        >
          <i class="fas fa-chevron-right mr-1 text-[10px]"></i>
          隐藏面板
        </button>
      </div>
    </template>
  </aside>
</template>
