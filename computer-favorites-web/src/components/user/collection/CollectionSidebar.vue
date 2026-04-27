<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 左侧导航栏 — 快捷访问 + 分类列表（层级嵌套） + 收藏统计条
 * 支持右键/长按上下文菜单
 */
import { ref, onBeforeUnmount } from 'vue'
import SidebarFolderItem from '@/components/user/collection/SidebarFolderItem.vue'
import type { CollectionQuickAccess, CollectionCategory } from '@/types/collection'

defineOptions({ name: 'CollectionSidebar' })

const props = defineProps<{
  quickAccessList: CollectionQuickAccess[]
  categories: CollectionCategory[]
  visibleCategories: CollectionCategory[]
  activeQuickAccess: string | null
  activeCategoryId: number | null
  collectCount: number
  collectLimit: number
  storagePercent: number
  editingCategoryId?: number | null
  isLoading?: boolean
}>()

const emit = defineEmits<{
  (e: 'select-quick-access', key: string): void
  (e: 'select-category', id: number): void
  (e: 'context-menu-folder', event: MouseEvent | TouchEvent, category: CollectionCategory): void
  (e: 'context-menu-empty', event: MouseEvent | TouchEvent): void
  (e: 'rename-category', id: number, newName: string): void
  (e: 'rename-cancel'): void
}>()

const handleContextMenuOnFolder = (event: MouseEvent | TouchEvent, category: CollectionCategory) => {
  if (event instanceof MouseEvent) {
    event.preventDefault()
    event.stopPropagation()
  }
  emit('context-menu-folder', event, category)
}

const handleContextMenuOnEmpty = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (target.closest('[data-folder-item]')) return
  event.preventDefault()
  emit('context-menu-empty', event)
}

/* 移动端长按逻辑 */
const longPressTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const touchStartPos = ref({ x: 0, y: 0 })
const LONG_PRESS_DURATION = 800
const TOUCH_MOVE_THRESHOLD = 10

const clearLongPress = () => {
  if (longPressTimer.value) {
    clearTimeout(longPressTimer.value)
    longPressTimer.value = null
  }
}

const handleTouchStartEmpty = (event: TouchEvent) => {
  const touch = event.touches[0]
  if (!touch) return
  touchStartPos.value = { x: touch.clientX, y: touch.clientY }
  longPressTimer.value = setTimeout(() => {
    longPressTimer.value = null
    emit('context-menu-empty', event)
  }, LONG_PRESS_DURATION)
}

const handleTouchMove = (event: TouchEvent) => {
  if (!longPressTimer.value) return
  const touch = event.touches[0]
  if (!touch) return
  const dx = Math.abs(touch.clientX - touchStartPos.value.x)
  const dy = Math.abs(touch.clientY - touchStartPos.value.y)
  if (dx > TOUCH_MOVE_THRESHOLD || dy > TOUCH_MOVE_THRESHOLD) {
    clearLongPress()
  }
}

const handleTouchEnd = () => {
  clearLongPress()
}

onBeforeUnmount(() => {
  clearLongPress()
})
</script>

<template>
  <aside
    class="w-64 bg-[#f8f9fa] dark:bg-black border-r border-[#e5e7eb] dark:border-white/[0.06] flex flex-col shrink-0 h-full select-none"
  >
    <!-- Logo 区域 -->
    <div class="h-16 flex items-center px-6">
      <div class="flex items-center space-x-2">
        <div
          class="w-8 h-8 bg-blue-100 dark:bg-blue-500/10 rounded-lg flex items-center justify-center text-blue-500 dark:text-blue-400"
        >
          <i class="fas fa-folder-special text-sm"></i>
        </div>
        <span class="font-bold text-lg text-[#111827] dark:text-[#f0f0f0]">我 的 收 藏 夹</span>
      </div>
    </div>

    <!-- 导航内容 -->
    <div
      class="flex-1 overflow-y-auto scrollbar-hide px-3 py-4 space-y-6"
      @contextmenu.prevent="handleContextMenuOnEmpty"
      @touchstart.passive="handleTouchStartEmpty"
      @touchmove.passive="handleTouchMove"
      @touchend="handleTouchEnd"
      @touchcancel="handleTouchEnd"
    >
      <!-- 加载状态 -->
      <div v-if="isLoading" class="py-8 text-center text-[#9ca3af] dark:text-[#4b5563]">
        <i class="fas fa-spinner fa-spin text-lg mb-2"></i>
        <p class="text-xs">加载中...</p>
      </div>

      <template v-else>
        <!-- 快捷访问 -->
        <div>
          <div
            class="text-xs font-semibold text-[#9ca3af] dark:text-[#4b5563] uppercase tracking-wider mb-2 px-3"
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
                  : 'text-[#374151] dark:text-[#9ca3af] hover:bg-[#e5e7eb]/60 dark:hover:bg-white/[0.04]'
              "
              @click="emit('select-quick-access', item.key)"
            >
              <i
                :class="[
                  item.icon,
                  'text-[15px] mr-3',
                  activeQuickAccess === item.key
                    ? 'text-blue-500 dark:text-blue-400'
                    : 'text-[#9ca3af] dark:text-[#4b5563]',
                ]"
              ></i>
              {{ item.label }}
            </button>
          </nav>
        </div>

        <!-- 分类 — 使用递归组件展示层级 -->
        <div>
          <div
            class="text-xs font-semibold text-[#9ca3af] dark:text-[#4b5563] uppercase tracking-wider mb-2 px-3"
          >
            Categories
          </div>
          <nav class="space-y-0.5" data-folder-item>
            <SidebarFolderItem
              v-for="cat in visibleCategories"
              :key="cat.id"
              :category="cat"
              :depth="0"
              :active-category-id="activeCategoryId"
              :editing-category-id="editingCategoryId"
              @select-category="(id) => emit('select-category', id)"
              @context-menu-folder="(e, cat) => handleContextMenuOnFolder(e, cat)"
              @rename-category="(id, name) => emit('rename-category', id, name)"
              @rename-cancel="emit('rename-cancel')"
              @touchstart-folder="(e, cat) => emit('context-menu-folder', e, cat)"
              @touchmove="handleTouchMove"
              @touchend="handleTouchEnd"
              @touchcancel="handleTouchEnd"
            />
          </nav>
        </div>

        <!-- 可长按/右击的空白区域 -->
        <div
          class="min-h-[120px] rounded-xl border border-dashed border-[#e5e7eb] dark:border-[#2d2d2d] flex items-center justify-center"
          @contextmenu.prevent="handleContextMenuOnEmpty"
          @touchstart.passive="handleTouchStartEmpty"
          @touchmove.passive="handleTouchMove"
          @touchend="handleTouchEnd"
          @touchcancel="handleTouchEnd"
        >
          <span class="text-xs text-[#9ca3af] dark:text-[#4b5563]">长按或右击管理收藏夹</span>
        </div>
      </template>
    </div>

    <!-- 收藏统计 -->
    <div class="p-6 border-t border-[#e5e7eb] dark:border-white/[0.06]">
      <div class="flex justify-between text-xs mb-1.5">
        <span class="text-[#9ca3af] dark:text-[#4b5563]">收藏</span>
        <span class="text-[#9ca3af] dark:text-[#4b5563] tabular-nums"
          >{{ collectCount }} / {{ collectLimit }}</span
        >
      </div>
      <div class="w-full bg-[#e5e7eb] dark:bg-white/[0.06] rounded-full h-1.5">
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
