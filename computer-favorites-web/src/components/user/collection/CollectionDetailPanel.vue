<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 右侧详情面板 — 网站详细信息（可切换显示/隐藏），暗黑模式纯黑
 */
import { useRouter } from 'vue-router'
import type { CollectionWebsite } from '@/types/collection'

defineOptions({ name: 'CollectionDetailPanel' })

const router = useRouter()

defineProps<{
  website: CollectionWebsite | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'cancel-collect', websiteId: number): void
  (e: 'visit', url: string): void
}>()

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
              v-if="website.websiteIcon"
              :src="website.websiteIcon"
              :alt="website.websiteName"
              class="w-10 h-10 object-contain"
            />
            <i v-else class="fas fa-globe text-2xl text-gray-400 dark:text-gray-600"></i>
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex items-start justify-between">
              <h2 class="text-xl font-bold mb-1 text-gray-900 dark:text-gray-100">
                {{ website.websiteName }}
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
              {{ extractDomain(website.websiteUrl) }}
            </span>
          </div>
        </div>

        <!-- 查看详情按钮 -->
        <button
          type="button"
          class="w-full bg-blue-500 hover:bg-blue-600 dark:bg-blue-500 dark:hover:bg-blue-400 text-white font-medium py-2.5 px-4 rounded-lg flex items-center justify-center mb-4 transition-all duration-200 cursor-pointer active:scale-[0.97] shadow-sm dark:shadow-[0_2px_8px_rgba(59,130,246,0.2)]"
          @click="router.push(`/computer/website/${website.websiteId}`)"
        >
          查看详情
          <i class="fas fa-arrow-right text-[13px] ml-2"></i>
        </button>

        <!-- 操作按钮组 -->
        <div class="grid grid-cols-3 gap-2 mb-6 border-b border-[#e5e7eb] dark:border-white/[0.06] pb-6">
          <button
            type="button"
            class="flex items-center justify-center py-2.5 border border-[#e5e7eb] dark:border-white/[0.06] rounded-lg hover:bg-red-50 dark:hover:bg-red-500/10 text-red-500 dark:text-red-400 cursor-pointer transition-all duration-200 active:scale-[0.95]"
            @click="emit('cancel-collect', website.websiteId)"
          >
            <i class="fas fa-heart-broken text-[18px]"></i>
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
            {{ website.websiteSummary }}
          </p>

          <!-- 标签 -->
          <div v-if="website.websiteTags && website.websiteTags.length > 0">
            <h4
              class="text-gray-400 dark:text-gray-600 mb-2 text-xs font-medium uppercase tracking-wider"
            >
              标签
            </h4>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="tag in website.websiteTags"
                :key="tag.id"
                class="collect-tag inline-flex items-center px-2 py-0.5 rounded text-xs font-medium"
                :style="getTagStyleVars(tag.color)"
              >
                {{ tag.name }}
              </span>
            </div>
          </div>

          <!-- 所属收藏夹 -->
          <div v-if="website.folderName">
            <h4
              class="text-gray-400 dark:text-gray-600 mb-1 text-xs font-medium uppercase tracking-wider"
            >
              收藏夹
            </h4>
            <p class="text-gray-900 dark:text-gray-300">{{ website.folderName }}</p>
          </div>

          <!-- 收藏日期 -->
          <div v-if="website.collectTime">
            <h4
              class="text-gray-400 dark:text-gray-600 mb-1 text-xs font-medium uppercase tracking-wider"
            >
              收藏日期
            </h4>
            <p class="text-gray-900 dark:text-gray-300 tabular-nums">{{ website.collectTime }}</p>
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
              v-if="website.websiteIcon"
              :src="website.websiteIcon"
              :alt="`${website.websiteName} 预览`"
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
