<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 左侧导航栏 — 快捷访问 + 分类列表 + 存储条
 * 支持右键/长按上下文菜单、行内重命名编辑
 */
import { ref, watch, nextTick, onBeforeUnmount } from 'vue'
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
  editingCategoryId?: number | null
  hiddenCategoryIds?: Set<number>
}>()

const emit = defineEmits<{
  (e: 'select-quick-access', key: string): void
  (e: 'select-category', id: number): void
  (e: 'context-menu-folder', event: MouseEvent | TouchEvent, category: CollectionCategory): void
  (e: 'context-menu-empty', event: MouseEvent | TouchEvent): void
  (e: 'rename-category', id: number, newName: string): void
  (e: 'rename-cancel'): void
}>()

const renameInputRef = ref<HTMLInputElement | null>(null)
const localEditingName = ref('')
const visibleCategories = ref<CollectionCategory[]>([])

watch(
  () => [props.categories, props.hiddenCategoryIds] as const,
  ([cats, hidden]) => {
    if (hidden && hidden.size > 0) {
      visibleCategories.value = cats.filter((c) => !hidden.has(c.id))
    } else {
      visibleCategories.value = cats
    }
  },
  { immediate: true, deep: true },
)

watch(
  () => props.editingCategoryId,
  (id) => {
    if (id !== null && id !== undefined) {
      const cat = props.categories.find((c) => c.id === id)
      if (cat) {
        localEditingName.value = cat.name
        nextTick(() => {
          renameInputRef.value?.focus()
          renameInputRef.value?.select()
        })
      }
    } else {
      localEditingName.value = ''
    }
  },
)

const handleRenameSave = (id: number) => {
  const trimmed = localEditingName.value.trim()
  if (trimmed.length > 0 && trimmed.length <= 50) {
    emit('rename-category', id, trimmed)
  } else {
    emit('rename-cancel')
  }
}

const handleRenameKeydown = (e: KeyboardEvent, id: number) => {
  if (e.key === 'Enter') {
    e.preventDefault()
    handleRenameSave(id)
  } else if (e.key === 'Escape') {
    emit('rename-cancel')
  }
}

const handleRenameBlur = (id: number) => {
  handleRenameSave(id)
}

const handleContextMenuOnFolder = (event: MouseEvent, category: CollectionCategory) => {
  event.preventDefault()
  event.stopPropagation()
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

const handleTouchStartFolder = (event: TouchEvent, category: CollectionCategory) => {
  const touch = event.touches[0]
  if (!touch) return
  touchStartPos.value = { x: touch.clientX, y: touch.clientY }
  longPressTimer.value = setTimeout(() => {
    longPressTimer.value = null
    emit('context-menu-folder', event, category)
  }, LONG_PRESS_DURATION)
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

      <!-- 分类 -->
      <div>
        <div
          class="text-xs font-semibold text-[#9ca3af] dark:text-[#4b5563] uppercase tracking-wider mb-2 px-3"
        >
          Categories
        </div>
        <nav class="space-y-0.5">
          <div
            v-for="cat in visibleCategories"
            :key="cat.id"
            data-folder-item
            @contextmenu.prevent="(e: MouseEvent) => handleContextMenuOnFolder(e, cat)"
            @touchstart.passive="(e: TouchEvent) => handleTouchStartFolder(e, cat)"
            @touchmove.passive="handleTouchMove"
            @touchend="handleTouchEnd"
            @touchcancel="handleTouchEnd"
          >
            <button
              v-if="editingCategoryId !== cat.id"
              type="button"
              class="w-full flex items-center px-3 py-2 text-sm font-medium rounded-lg cursor-pointer transition-all duration-200"
              :class="
                activeCategoryId === cat.id
                  ? 'bg-blue-500/10 dark:bg-blue-500/[0.12] text-blue-600 dark:text-blue-400 shadow-[inset_0_0_0_1px_rgba(59,130,246,0.15)] dark:shadow-[inset_0_0_0_1px_rgba(59,130,246,0.12)]'
                  : 'text-[#374151] dark:text-[#9ca3af] hover:bg-[#e5e7eb]/60 dark:hover:bg-white/[0.04]'
              "
              @click="emit('select-category', cat.id)"
            >
              <i
                :class="[
                  cat.icon,
                  'text-[15px] mr-3',
                  activeCategoryId === cat.id
                    ? 'text-blue-500 dark:text-blue-400'
                    : 'text-[#9ca3af] dark:text-[#4b5563]',
                ]"
              ></i>
              <span class="truncate">{{ cat.name }}</span>
              <span class="ml-auto text-xs text-[#9ca3af] dark:text-[#4b5563] tabular-nums">{{ cat.count }}</span>
            </button>

            <!-- 行内重命名输入框 -->
            <div
              v-else
              class="w-full flex items-center px-3 py-1.5 bg-blue-500/10 dark:bg-blue-500/[0.12] rounded-lg shadow-[inset_0_0_0_1px_rgba(59,130,246,0.15)] dark:shadow-[inset_0_0_0_1px_rgba(59,130,246,0.12)]"
            >
              <i
                :class="[
                  cat.icon,
                  'text-[15px] mr-3 text-blue-500 dark:text-blue-400',
                ]"
              ></i>
              <input
                ref="renameInputRef"
                v-model="localEditingName"
                type="text"
                maxlength="50"
                class="flex-1 min-w-0 bg-transparent text-sm font-medium text-blue-600 dark:text-blue-400 outline-none border-b border-blue-500/30 dark:border-blue-400/30 pb-0.5"
                @keydown="(e: KeyboardEvent) => handleRenameKeydown(e, cat.id)"
                @blur="handleRenameBlur(cat.id)"
              />
            </div>
          </div>
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
    </div>

    <!-- 存储用量 -->
    <div class="p-6 border-t border-[#e5e7eb] dark:border-white/[0.06]">
      <div class="flex justify-between text-xs mb-1.5">
        <span class="text-[#9ca3af] dark:text-[#4b5563]">存储</span>
        <span class="text-[#9ca3af] dark:text-[#4b5563] tabular-nums"
          >{{ storageUsed }} GB / {{ storageTotal }} GB</span
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
