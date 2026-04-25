<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 底部状态栏 — 资源计数 + 缩放滑块 + 视图切换
 */
import type { ViewMode } from '@/types/collection'

defineOptions({ name: 'CollectionStatusBar' })

defineProps<{
  totalCount: number
  selectedCount: number
  zoomLevel: number
  viewMode: ViewMode
}>()

const emit = defineEmits<{
  (e: 'update:zoomLevel', value: number): void
  (e: 'update:viewMode', mode: ViewMode): void
}>()
</script>

<template>
  <footer
    class="h-10 border-t border-[#e5e7eb] dark:border-white/[0.06] bg-white dark:bg-black flex items-center justify-between px-6 shrink-0 text-xs text-gray-500 dark:text-gray-600 select-none"
  >
    <!-- 左侧计数 -->
    <div class="flex space-x-4 tabular-nums">
      <span>{{ totalCount }} 个资源</span>
      <span v-if="selectedCount > 0" class="text-blue-500 dark:text-blue-400">已选 {{ selectedCount }} 个</span>
    </div>

    <!-- 右侧控件 -->
    <div class="flex items-center space-x-4">
      <!-- 缩放滑块 -->
      <div class="hidden sm:flex items-center space-x-2">
        <button
          type="button"
          class="cursor-pointer hover:text-gray-700 dark:hover:text-gray-300 transition-colors p-0.5"
          @click="emit('update:zoomLevel', Math.max(50, zoomLevel - 10))"
        >
          <i class="fas fa-minus text-[11px]"></i>
        </button>
        <input
          type="range"
          :value="zoomLevel"
          min="50"
          max="150"
          step="10"
          class="w-24 accent-blue-500 dark:accent-blue-400 cursor-pointer"
          @input="emit('update:zoomLevel', Number(($event.target as HTMLInputElement).value))"
        />
        <button
          type="button"
          class="cursor-pointer hover:text-gray-700 dark:hover:text-gray-300 transition-colors p-0.5"
          @click="emit('update:zoomLevel', Math.min(150, zoomLevel + 10))"
        >
          <i class="fas fa-plus text-[11px]"></i>
        </button>
        <span class="w-9 text-center tabular-nums">{{ zoomLevel }}%</span>
      </div>

      <!-- 视图切换 -->
      <div class="flex border border-[#e5e7eb] dark:border-white/[0.06] rounded-md overflow-hidden">
        <button
          type="button"
          class="p-1.5 cursor-pointer transition-all duration-200"
          :class="
            viewMode === 'grid'
              ? 'bg-gray-100 dark:bg-white/[0.06] text-gray-700 dark:text-gray-300'
              : 'hover:bg-gray-50 dark:hover:bg-white/[0.03] text-gray-400 dark:text-gray-600'
          "
          title="网格视图"
          @click="emit('update:viewMode', 'grid')"
        >
          <i class="fas fa-th-large text-[13px]"></i>
        </button>
        <button
          type="button"
          class="p-1.5 cursor-pointer transition-all duration-200"
          :class="
            viewMode === 'list'
              ? 'bg-gray-100 dark:bg-white/[0.06] text-gray-700 dark:text-gray-300'
              : 'hover:bg-gray-50 dark:hover:bg-white/[0.03] text-gray-400 dark:text-gray-600'
          "
          title="列表视图"
          @click="emit('update:viewMode', 'list')"
        >
          <i class="fas fa-list text-[13px]"></i>
        </button>
      </div>
    </div>
  </footer>
</template>
