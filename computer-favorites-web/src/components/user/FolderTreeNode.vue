<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-26
 *
 * 收藏夹树节点 — 递归组件
 * 支持嵌套层级、展开/折叠、单选、文件夹颜色与图标
 */
import { ref, computed } from 'vue'
import { ChevronRight } from 'lucide-vue-next'
import type { CollectionCategory } from '@/types/collection'

defineOptions({ name: 'FolderTreeNode' })

const props = withDefaults(
  defineProps<{
    node: CollectionCategory
    depth?: number
    selectedId?: number | null
  }>(),
  {
    depth: 0,
    selectedId: null,
  },
)

const emit = defineEmits<{
  (e: 'select', id: number): void
}>()

const expanded = ref(true)

const hasChildren = computed(() => Array.isArray(props.node.children) && props.node.children.length > 0)

const isSelected = computed(() => props.selectedId === props.node.id)

const toggleExpand = (event: Event) => {
  event.stopPropagation()
  expanded.value = !expanded.value
}

const handleSelect = () => {
  emit('select', props.node.id)
}

const handleChildSelect = (id: number) => {
  emit('select', id)
}
</script>

<template>
  <li class="folder-tree-node">
    <!-- 当前节点行 -->
    <button
      type="button"
      class="folder-row group flex w-full cursor-pointer items-center gap-3 rounded-lg px-3 py-2.5 text-left transition-all duration-150"
      :class="[
        isSelected
          ? 'is-selected'
          : 'is-default',
      ]"
      :style="{ paddingLeft: `${depth * 24 + 12}px` }"
      :aria-selected="isSelected"
      role="treeitem"
      @click="handleSelect"
    >
      <!-- 展开/折叠箭头 -->
      <span
        class="folder-toggle flex h-6 w-6 shrink-0 items-center justify-center rounded-md transition-transform duration-200"
        :class="[
          hasChildren ? 'cursor-pointer hover:bg-gray-200/60 dark:hover:bg-white/[0.06]' : 'invisible',
          expanded ? 'rotate-90' : '',
        ]"
        @click="hasChildren ? toggleExpand($event) : undefined"
      >
        <ChevronRight class="h-4 w-4" />
      </span>

      <!-- 文件夹颜色指示条 -->
      <span
        class="folder-color-bar h-6 w-[3.5px] shrink-0 rounded-full"
        :style="{ backgroundColor: node.color || '#d97706' }"
      ></span>

      <!-- 文件夹图标 -->
      <span
        class="folder-icon-wrap flex h-7 w-7 shrink-0 items-center justify-center rounded-md text-sm"
        :style="{
          backgroundColor: (node.color || '#d97706') + '18',
          color: node.color || '#d97706',
        }"
      >
        <i v-if="node.icon" :class="node.icon" class="text-sm"></i>
        <i v-else class="fas fa-folder text-sm"></i>
      </span>

      <!-- 文件夹名称 -->
      <span class="folder-name min-w-0 flex-1 truncate text-[15px] font-medium">
        {{ node.name }}
      </span>

      <!-- 收藏数量 -->
      <span
        v-if="node.websiteCount > 0"
        class="folder-count shrink-0 text-xs font-medium tabular-nums"
      >
        {{ node.websiteCount }}
      </span>

      <!-- 选中指示器 -->
      <span
        class="folder-check shrink-0 flex h-5 w-5 items-center justify-center rounded-full border-2 transition-all duration-200"
        :class="isSelected ? 'is-checked' : 'is-unchecked'"
      >
        <span
          v-if="isSelected"
          class="h-2 w-2 rounded-full bg-white"
        ></span>
      </span>
    </button>

    <!-- 子节点列表 -->
    <Transition
      enter-active-class="transition-all duration-200 ease-out"
      enter-from-class="opacity-0 -translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition-all duration-150 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 -translate-y-1"
    >
      <ul
        v-if="hasChildren && expanded"
        class="folder-tree-children mt-1 space-y-1"
        role="group"
      >
        <!-- 层级连接线 -->
        <div
          class="folder-depth-line"
          :style="{ left: `${depth * 24 + 26}px` }"
        ></div>

        <FolderTreeNode
          v-for="child in node.children"
          :key="child.id"
          :node="child"
          :depth="depth + 1"
          :selected-id="selectedId"
          @select="handleChildSelect"
        />
      </ul>
    </Transition>
  </li>
</template>

<style scoped>
/* 默认态 */
.folder-row.is-default {
  color: rgb(55 65 81);
}

.folder-row.is-default:hover {
  background-color: rgb(249 250 251 / 1);
}

/* 选中态 */
.folder-row.is-selected {
  background-color: rgb(245 158 11 / 0.08);
  color: rgb(146 64 14);
}

/* 展开箭头 */
.folder-toggle {
  color: rgb(156 163 175);
}

/* 文件夹名称 */
.folder-name {
  color: inherit;
}

/* 收藏数量 */
.folder-count {
  color: rgb(156 163 175);
}

.folder-row.is-selected .folder-count {
  color: rgb(217 119 6);
}

/* 选中圆点 — 未选中 */
.folder-check.is-unchecked {
  border-color: rgb(209 213 219);
  background-color: transparent;
}

/* 选中圆点 — 选中 */
.folder-check.is-checked {
  border-color: rgb(245 158 11);
  background-color: rgb(245 158 11);
}

/* 层级连接线 */
.folder-tree-children {
  position: relative;
}

.folder-depth-line {
  position: absolute;
  top: 0;
  bottom: 8px;
  width: 1px;
  background-color: rgb(229 231 235);
  pointer-events: none;
}

/* ============================================================
   Dark Mode — 与新深邃主题一致
   ============================================================ */

:root.dark .folder-row.is-default {
  color: rgb(190 195 210);
}

:root.dark .folder-row.is-default:hover {
  background-color: rgba(255 255 255 / 0.04);
}

:root.dark .folder-row.is-selected {
  background-color: rgba(245 158 11 / 0.1);
  color: rgb(251 191 36);
}

:root.dark .folder-toggle {
  color: rgb(90 95 110);
}

:root.dark .folder-toggle:hover {
  background-color: rgba(255 255 255 / 0.05);
}

:root.dark .folder-count {
  color: rgb(90 95 110);
}

:root.dark .folder-row.is-selected .folder-count {
  color: rgb(251 191 36);
}

:root.dark .folder-check.is-unchecked {
  border-color: rgba(255 255 255 / 0.1);
}

:root.dark .folder-depth-line {
  background-color: rgba(255 255 255 / 0.04);
}

/* 移动端增强触控区域 */
@media (max-width: 640px) {
  .folder-row {
    padding-top: 10px;
    padding-bottom: 10px;
  }
}
</style>
