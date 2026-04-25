<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 左侧导航栏 — 快捷访问 + 分类列表 + 存储条
 */
import type { CollectionQuickAccess, CollectionCategory } from '@/types/collection'

defineOptions({ name: 'CollectionSidebar' })

const props = defineProps<{
  quickAccessList: CollectionQuickAccess[]
  categories: CollectionCategory[]
  activeQuickAccess: string | null
  activeCategoryId: number | null
  storageUsed: number
  storageTotal: number
  storagePercent: number
}>()

const emit = defineEmits<{
  (e: 'select-quick-access', key: string): void
  (e: 'select-category', id: number): void
}>()
</script>

<template>
  <aside
    class="w-64 bg-[#f8f9fa] dark:bg-black border-r border-[#e5e7eb] dark:border-white/[0.06] flex flex-col shrink-0 h-full"
  >
    <!-- Logo 区域 -->
    <div class="h-16 flex items-center px-6">
      <div class="flex items-center space-x-2">
        <div
          class="w-8 h-8 bg-blue-100 dark:bg-blue-500/10 rounded-lg flex items-center justify-center text-blue-500 dark:text-blue-400"
        >
          <i class="fas fa-folder-special text-sm"></i>
        </div>
        <span class="font-bold text-lg text-gray-900 dark:text-gray-100">我的收藏夹</span>
      </div>
    </div>

    <!-- 导航内容 -->
    <div class="flex-1 overflow-y-auto scrollbar-hide px-3 py-4 space-y-6">
      <!-- 快捷访问 -->
      <div>
        <div
          class="text-xs font-semibold text-gray-400 dark:text-gray-600 uppercase tracking-wider mb-2 px-3"
        >
          Quick Access
        </div>
        <nav class="space-y-0.5">
          <button
            v-for="item in quickAccessList"
            :key="item.key"
            type="button"
            class="w-full flex items-center px-3 py-2 text-sm font-medium rounded-lg cursor-pointer transition-all duration-200"
            :class="
              activeQuickAccess === item.key
                ? 'bg-blue-500/10 dark:bg-blue-500/[0.12] text-blue-600 dark:text-blue-400 shadow-[inset_0_0_0_1px_rgba(59,130,246,0.15)] dark:shadow-[inset_0_0_0_1px_rgba(59,130,246,0.12)]'
                : 'text-gray-700 dark:text-gray-400 hover:bg-gray-200/60 dark:hover:bg-white/[0.04]'
            "
            @click="emit('select-quick-access', item.key)"
          >
            <i
              :class="[
                item.icon,
                'text-[15px] mr-3',
                activeQuickAccess === item.key
                  ? 'text-blue-500 dark:text-blue-400'
                  : 'text-gray-400 dark:text-gray-600',
              ]"
            ></i>
            {{ item.label }}
          </button>
        </nav>
      </div>

      <!-- 分类 -->
      <div>
        <div
          class="text-xs font-semibold text-gray-400 dark:text-gray-600 uppercase tracking-wider mb-2 px-3"
        >
          Categories
        </div>
        <nav class="space-y-0.5">
          <button
            v-for="cat in categories"
            :key="cat.id"
            type="button"
            class="w-full flex items-center px-3 py-2 text-sm font-medium rounded-lg cursor-pointer transition-all duration-200"
            :class="
              activeCategoryId === cat.id
                ? 'bg-blue-500/10 dark:bg-blue-500/[0.12] text-blue-600 dark:text-blue-400 shadow-[inset_0_0_0_1px_rgba(59,130,246,0.15)] dark:shadow-[inset_0_0_0_1px_rgba(59,130,246,0.12)]'
                : 'text-gray-700 dark:text-gray-400 hover:bg-gray-200/60 dark:hover:bg-white/[0.04]'
            "
            @click="emit('select-category', cat.id)"
          >
            <i
              :class="[
                cat.icon,
                'text-[15px] mr-3',
                activeCategoryId === cat.id
                  ? 'text-blue-500 dark:text-blue-400'
                  : 'text-gray-400 dark:text-gray-600',
              ]"
            ></i>
            {{ cat.name }}
            <span class="ml-auto text-xs text-gray-400 dark:text-gray-600 tabular-nums">{{ cat.count }}</span>
          </button>
        </nav>
      </div>
    </div>

    <!-- 存储用量 -->
    <div class="p-6 border-t border-[#e5e7eb] dark:border-white/[0.06]">
      <div class="flex justify-between text-xs mb-1.5">
        <span class="text-gray-400 dark:text-gray-600">存储</span>
        <span class="text-gray-400 dark:text-gray-600 tabular-nums"
          >{{ storageUsed }} GB / {{ storageTotal }} GB</span
        >
      </div>
      <div class="w-full bg-gray-200 dark:bg-white/[0.06] rounded-full h-1.5">
        <div
          class="bg-blue-500 dark:bg-blue-400 h-1.5 rounded-full transition-all duration-300"
          :style="{ width: `${storagePercent}%` }"
        ></div>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
