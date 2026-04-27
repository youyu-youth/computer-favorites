<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-26
 *
 * 侧边栏收藏夹递归节点 — 支持父子层级嵌套、展开折叠、行内重命名、右键菜单
 */
import { ref, computed, watch, nextTick } from 'vue'
import { ChevronRight } from 'lucide-vue-next'
import type { CollectionCategory } from '@/types/collection'

defineOptions({ name: 'SidebarFolderItem' })

const props = withDefaults(
  defineProps<{
    category: CollectionCategory
    depth?: number
    activeCategoryId: number | null
    editingCategoryId?: number | null
  }>(),
  {
    depth: 0,
    editingCategoryId: null,
  },
)

const emit = defineEmits<{
  (e: 'select-category', id: number): void
  (e: 'context-menu-folder', event: MouseEvent | TouchEvent, category: CollectionCategory): void
  (e: 'rename-category', id: number, newName: string): void
  (e: 'rename-cancel'): void
  (e: 'touchstart-folder', event: TouchEvent, category: CollectionCategory): void
  (e: 'touchmove', event: TouchEvent): void
  (e: 'touchend'): void
  (e: 'touchcancel'): void
}>()

const expanded = ref(true)
const renameInputRef = ref<HTMLInputElement | null>(null)
const localEditingName = ref('')

const hasChildren = computed(() => Array.isArray(props.category.children) && props.category.children.length > 0)
const isActive = computed(() => props.activeCategoryId === props.category.id)
const isEditing = computed(() => props.editingCategoryId === props.category.id)

watch(
  () => props.editingCategoryId,
  (id) => {
    if (id === props.category.id) {
      localEditingName.value = props.category.name
      nextTick(() => {
        renameInputRef.value?.focus()
        renameInputRef.value?.select()
      })
    } else if (localEditingName.value) {
      localEditingName.value = ''
    }
  },
)

const toggleExpand = (event: Event) => {
  event.stopPropagation()
  expanded.value = !expanded.value
}

const handleClick = () => {
  emit('select-category', props.category.id)
}

const handleContextMenu = (event: MouseEvent) => {
  event.preventDefault()
  event.stopPropagation()
  emit('context-menu-folder', event, props.category)
}

const handleTouchStart = (event: TouchEvent) => {
  emit('touchstart-folder', event, props.category)
}

const handleRenameSave = () => {
  const trimmed = localEditingName.value.trim()
  if (trimmed.length > 0 && trimmed.length <= 50) {
    emit('rename-category', props.category.id, trimmed)
  } else {
    emit('rename-cancel')
  }
}

const handleRenameKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    e.preventDefault()
    handleRenameSave()
  } else if (e.key === 'Escape') {
    emit('rename-cancel')
  }
}

const handleRenameBlur = () => {
  handleRenameSave()
}
</script>

<template>
  <div class="sidebar-folder-item">
    <!-- 正常态 -->
    <button
      v-if="!isEditing"
      type="button"
      class="folder-row group w-full flex items-center cursor-pointer rounded-lg transition-all duration-200"
      :class="[
        isActive
          ? 'bg-blue-500/10 dark:bg-blue-500/[0.12] text-blue-600 dark:text-blue-400 shadow-[inset_0_0_0_1px_rgba(59,130,246,0.15)] dark:shadow-[inset_0_0_0_1px_rgba(59,130,246,0.12)]'
          : 'text-[#374151] dark:text-[#9ca3af] hover:bg-[#e5e7eb]/60 dark:hover:bg-white/[0.04]',
      ]"
      :style="{ paddingLeft: `${depth * 16 + 12}px`, paddingRight: '12px', paddingTop: '8px', paddingBottom: '8px' }"
      @click="handleClick"
      @contextmenu.prevent="handleContextMenu"
      @touchstart.passive="handleTouchStart"
      @touchmove.passive="(e: TouchEvent) => emit('touchmove', e)"
      @touchend="emit('touchend')"
      @touchcancel="emit('touchcancel')"
    >
      <!-- 展开/折叠箭头 -->
      <span
        class="flex h-5 w-5 shrink-0 items-center justify-center rounded transition-transform duration-200"
        :class="[
          hasChildren ? 'cursor-pointer hover:bg-gray-200/60 dark:hover:bg-white/[0.06]' : 'invisible',
          expanded ? 'rotate-90' : '',
        ]"
        @click="hasChildren ? toggleExpand($event) : undefined"
      >
        <ChevronRight class="h-3.5 w-3.5" />
      </span>

      <!-- 文件夹图标 -->
      <i
        v-if="category.icon"
        :class="[
          category.icon,
          'text-[15px] mr-3',
          isActive ? 'text-blue-500 dark:text-blue-400' : 'text-[#9ca3af] dark:text-[#4b5563]',
        ]"
      ></i>
      <i
        v-else
        :class="[
          'fas fa-folder text-[15px] mr-3',
          isActive ? 'text-blue-500 dark:text-blue-400' : 'text-[#9ca3af] dark:text-[#4b5563]',
        ]"
      ></i>

      <!-- 文件夹名称 -->
      <span class="truncate text-sm font-medium flex-1 min-w-0">{{ category.name }}</span>

      <!-- 网站数量 -->
      <span class="ml-auto text-xs text-[#9ca3af] dark:text-[#4b5563] tabular-nums shrink-0 pl-2">{{ category.websiteCount }}</span>
    </button>

    <!-- 重命名态 -->
    <div
      v-else
      class="w-full flex items-center bg-blue-500/10 dark:bg-blue-500/[0.12] rounded-lg shadow-[inset_0_0_0_1px_rgba(59,130,246,0.15)] dark:shadow-[inset_0_0_0_1px_rgba(59,130,246,0.12)]"
      :style="{ paddingLeft: `${depth * 16 + 12}px`, paddingRight: '12px', paddingTop: '6px', paddingBottom: '6px' }"
    >
      <!-- 展开箭头占位 -->
      <span
        class="flex h-5 w-5 shrink-0 items-center justify-center"
        :class="hasChildren ? '' : 'invisible'"
      >
        <ChevronRight class="h-3.5 w-3.5 text-blue-500 dark:text-blue-400" />
      </span>

      <i
        v-if="category.icon"
        :class="[category.icon, 'text-[15px] mr-3 text-blue-500 dark:text-blue-400']"
      ></i>
      <i
        v-else
        class="fas fa-folder text-[15px] mr-3 text-blue-500 dark:text-blue-400"
      ></i>

      <input
        ref="renameInputRef"
        v-model="localEditingName"
        type="text"
        maxlength="50"
        class="flex-1 min-w-0 bg-transparent text-sm font-medium text-blue-600 dark:text-blue-400 outline-none border-b border-blue-500/30 dark:border-blue-400/30 pb-0.5"
        @keydown="handleRenameKeydown"
        @blur="handleRenameBlur"
      />
    </div>

    <!-- 子节点 -->
    <Transition
      enter-active-class="transition-all duration-200 ease-out"
      enter-from-class="opacity-0 -translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition-all duration-150 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 -translate-y-1"
    >
      <div v-if="hasChildren && expanded" class="sidebar-folder-children relative mt-0.5 space-y-0.5">
        <!-- 层级连接线 -->
        <div
          class="sidebar-depth-line"
          :style="{ left: `${depth * 16 + 22}px` }"
        ></div>

        <SidebarFolderItem
          v-for="child in category.children"
          :key="child.id"
          :category="child"
          :depth="depth + 1"
          :active-category-id="activeCategoryId"
          :editing-category-id="editingCategoryId"
          @select-category="(id) => emit('select-category', id)"
          @context-menu-folder="(e, cat) => emit('context-menu-folder', e, cat)"
          @rename-category="(id, name) => emit('rename-category', id, name)"
          @rename-cancel="emit('rename-cancel')"
          @touchstart-folder="(e, cat) => emit('touchstart-folder', e, cat)"
          @touchmove="(e) => emit('touchmove', e)"
          @touchend="emit('touchend')"
          @touchcancel="emit('touchcancel')"
        />
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.sidebar-folder-children {
  position: relative;
}

.sidebar-depth-line {
  position: absolute;
  top: 0;
  bottom: 4px;
  width: 1px;
  background-color: rgb(229 231 235);
  pointer-events: none;
}

:root.dark .sidebar-depth-line {
  background-color: rgba(255 255 255 / 0.04);
}

@media (max-width: 640px) {
  .folder-row {
    padding-top: 10px;
    padding-bottom: 10px;
  }
}
</style>
