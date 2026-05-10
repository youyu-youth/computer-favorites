<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-10
 *
 * 公开收藏夹树行（user-15 PublicCollectionView 递归节点）。
 *
 * 设计原则（taste-sikll, DV=8 VD=4）：
 *   - 反卡片化：行不再 rounded-lg + bg；改 divide-y 与 hover 全宽底纹。
 *   - 编辑式索引：根级行以 mono `01.` 索引计数器开头，像目录条目。
 *   - 选中：左侧 2px amber 立柱 + 文字加粗；不使用阴影/塑料发光。
 *   - folder.png 28px（对比上一版 24px 再放大），按需 scale。
 *   - :active 触感：scale-[0.985] 物理按压。
 *   - 单一 accent 色：amber-500；hover 仅是 zinc 中性色微涂。
 */

import { computed } from 'vue'
import { ChevronRight } from 'lucide-vue-next'
import folderIcon from '@/assets/icons/png/folder.png'
import type { PublicFolderTreeNode } from '@/api/user-profile-public'

const props = withDefaults(
  defineProps<{
    node: PublicFolderTreeNode
    selectedId: number | null
    expandedIds: Set<number>
    depth?: number
    /** 同级索引（从 0 起），用于编辑式 mono 编号 + staggered enter 延迟 */
    index?: number
  }>(),
  {
    depth: 0,
    index: 0,
  },
)

const emit = defineEmits<{
  (e: 'toggle', id: number): void
  (e: 'select', node: PublicFolderTreeNode): void
}>()

const isSelected = computed(() => props.selectedId === props.node.id)
const isExpanded = computed(() => props.expandedIds.has(props.node.id))
const hasChildren = computed(() => !!(props.node.children && props.node.children.length))
const isRoot = computed(() => props.depth === 0)

/** 根级行展示 mono 编号 "01." "02."；子级用空白对齐保持网格感 */
const indexLabel = computed(() =>
  isRoot.value ? String(props.index + 1).padStart(2, '0') : '',
)

const enterDelayMs = computed(() => Math.min(props.index * 36, 320))

const handleSelect = () => emit('select', props.node)

const handleToggle = (ev: Event) => {
  ev.stopPropagation()
  emit('toggle', props.node.id)
}
</script>

<template>
  <div class="cf-tree-node flex flex-col" :style="{ animationDelay: `${enterDelayMs}ms` }">
    <div
      :class="[
        'cf-tree-row group relative flex cursor-pointer select-none items-center gap-3 py-2.5 pr-2 text-[13.5px] transition-colors duration-200 active:scale-[0.985]',
        isSelected
          ? 'text-amber-700 dark:text-amber-300'
          : 'text-zinc-700 hover:bg-zinc-100/60 dark:text-zinc-300 dark:hover:bg-white/[0.025]',
      ]"
      :style="{ paddingLeft: `${10 + depth * 18}px` }"
      @click="handleSelect"
    >
      <!-- 选中立柱（amber 2px） -->
      <span
        v-if="isSelected"
        class="cf-tree-bar pointer-events-none absolute inset-y-1 left-0 w-[2px] bg-amber-500 dark:bg-amber-400"
        aria-hidden="true"
      />

      <!-- 编辑式 mono 编号（仅根级） -->
      <span
        v-if="indexLabel"
        :class="[
          'w-6 shrink-0 font-mono text-[10.5px] tabular-nums tracking-wider transition-colors',
          isSelected
            ? 'text-amber-500 dark:text-amber-400'
            : 'text-zinc-400 dark:text-zinc-600',
        ]"
      >
        {{ indexLabel }}
      </span>

      <!-- 折叠箭头 -->
      <button
        v-if="hasChildren"
        type="button"
        :aria-label="isExpanded ? '折叠' : '展开'"
        class="flex size-4 shrink-0 cursor-pointer items-center justify-center rounded text-zinc-400 transition-transform duration-200 hover:text-amber-500 dark:text-zinc-600"
        :class="isExpanded ? 'rotate-90' : ''"
        @click="handleToggle"
      >
        <ChevronRight class="size-3.5" :stroke-width="2" />
      </button>
      <span v-else-if="!isRoot" class="inline-block size-4 shrink-0" />

      <!-- folder.png（再放大到 28px） -->
      <img
        :src="folderIcon"
        alt=""
        aria-hidden="true"
        class="cf-folder-img size-7 shrink-0 transition-transform duration-200 will-change-transform group-hover:scale-110 group-hover:rotate-[-4deg]"
        :class="isSelected ? 'scale-110' : ''"
        loading="lazy"
      />

      <span
        class="flex-1 truncate transition-[font-weight]"
        :class="isSelected ? 'font-semibold tracking-[0.005em]' : 'font-medium'"
      >
        {{ node.name }}
      </span>

      <!-- 数量：mono、不要胶囊；选中时 amber -->
      <span
        :class="[
          'shrink-0 font-mono text-[11px] tabular-nums leading-none transition-colors',
          isSelected
            ? 'text-amber-500 dark:text-amber-400'
            : 'text-zinc-400 dark:text-zinc-600',
        ]"
      >
        {{ node.websiteCount }}
      </span>
    </div>

    <transition name="cf-tree-children">
      <div v-if="hasChildren && isExpanded" class="flex flex-col">
        <FolderTreeRow
          v-for="(child, idx) in node.children"
          :key="child.id"
          :node="child"
          :selected-id="selectedId"
          :expanded-ids="expandedIds"
          :depth="depth + 1"
          :index="idx"
          @toggle="(id: number) => emit('toggle', id)"
          @select="(n: PublicFolderTreeNode) => emit('select', n)"
        />
      </div>
    </transition>
  </div>
</template>

<style scoped>
.cf-tree-node {
  animation: cfTreeRowIn 320ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes cfTreeRowIn {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.cf-tree-children-enter-active,
.cf-tree-children-leave-active {
  transition:
    max-height 240ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 200ms ease;
  overflow: hidden;
}

.cf-tree-children-enter-from,
.cf-tree-children-leave-to {
  max-height: 0;
  opacity: 0;
}

.cf-tree-children-enter-to,
.cf-tree-children-leave-from {
  max-height: 1200px;
  opacity: 1;
}

@media (prefers-reduced-motion: reduce) {
  .cf-tree-node,
  .cf-tree-row,
  .cf-folder-img {
    animation: none !important;
    transition: none !important;
  }
}
</style>
