<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 主内容区顶部 — 面包屑 + 搜索框 + 添加按钮 + 通知 + 头像
 */
import InputText from 'primevue/inputtext'

defineOptions({ name: 'CollectionContentHeader' })

defineProps<{
  breadcrumbPath: string[]
  searchQuery: string
}>()

const emit = defineEmits<{
  (e: 'update:searchQuery', value: string): void
  (e: 'toggle-detail-panel'): void
  (e: 'add-resource'): void
}>()
</script>

<template>
  <header
    class="h-16 border-b border-[#e5e7eb] dark:border-white/[0.06] flex items-center justify-between px-6 shrink-0 bg-white dark:bg-black"
  >
    <div class="flex items-center space-x-4 flex-1">
      <!-- 前进/后退 -->
      <div class="flex items-center space-x-1 text-gray-400 dark:text-gray-600">
        <button type="button" class="p-1.5 hover:bg-gray-100 dark:hover:bg-white/[0.04] rounded cursor-pointer transition-colors">
          <i class="fas fa-chevron-left text-[13px]"></i>
        </button>
        <button type="button" class="p-1.5 hover:bg-gray-100 dark:hover:bg-white/[0.04] rounded cursor-pointer transition-colors">
          <i class="fas fa-chevron-right text-[13px]"></i>
        </button>
      </div>

      <!-- 面包屑 -->
      <div class="text-sm font-medium text-gray-900 dark:text-gray-300">
        <template v-for="(part, index) in breadcrumbPath" :key="index">
          <span v-if="index > 0" class="text-gray-300 dark:text-gray-700 mx-1.5">/</span>
          <span
            :class="
              index === breadcrumbPath.length - 1
                ? 'text-gray-400 dark:text-gray-600'
                : 'text-gray-900 dark:text-gray-300'
            "
          >
            {{ part }}
          </span>
        </template>
      </div>

      <!-- 搜索框 -->
      <div class="max-w-md w-full ml-8 hidden md:block">
        <div class="relative">
          <div
            class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none"
          >
            <i class="fas fa-search text-[13px] text-gray-400 dark:text-gray-600"></i>
          </div>
          <InputText
            :model-value="searchQuery"
            placeholder="搜索资源..."
            class="block w-full pl-10 pr-12 py-2 border border-[#e5e7eb] dark:border-white/[0.08] dark:bg-white/[0.03] rounded-lg leading-5 bg-white text-gray-900 dark:text-gray-200 placeholder-gray-400 dark:placeholder-gray-600 focus:outline-none focus:ring-1 focus:ring-blue-500/50 focus:border-blue-500/50 sm:text-sm transition-colors"
            @update:model-value="emit('update:searchQuery', $event)"
          />
          <div
            class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none"
          >
            <span
              class="text-[11px] text-gray-400 dark:text-gray-600 font-mono bg-gray-100 dark:bg-white/[0.04] px-1.5 py-0.5 rounded border border-gray-200 dark:border-white/[0.06]"
              >⌘K</span
            >
          </div>
        </div>
      </div>
    </div>

    <div class="flex items-center space-x-3">
      <!-- 添加资源按钮 -->
      <button
        type="button"
        class="flex items-center px-4 py-2 bg-blue-500 hover:bg-blue-600 dark:bg-blue-500 dark:hover:bg-blue-400 text-white rounded-lg text-sm font-medium transition-all duration-200 cursor-pointer focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:focus:ring-offset-black active:scale-[0.97]"
        @click="emit('add-resource')"
      >
        <i class="fas fa-plus text-[12px] mr-1.5"></i>
        添加资源
      </button>

      <!-- 通知按钮 -->
      <button
        type="button"
        class="text-gray-400 dark:text-gray-600 hover:text-gray-700 dark:hover:text-gray-300 relative p-1.5 cursor-pointer transition-colors"
      >
        <i class="fas fa-bell text-[15px]"></i>
        <span
          class="absolute top-1 right-1 block h-2 w-2 rounded-full bg-red-500 ring-2 ring-white dark:ring-black"
        ></span>
      </button>

      <!-- 面板切换按钮 -->
      <button
        type="button"
        class="text-gray-400 dark:text-gray-600 hover:text-gray-700 dark:hover:text-gray-300 p-1.5 cursor-pointer transition-colors"
        title="切换详情面板"
        @click="emit('toggle-detail-panel')"
      >
        <i class="fas fa-columns text-[15px]"></i>
      </button>
    </div>
  </header>
</template>
