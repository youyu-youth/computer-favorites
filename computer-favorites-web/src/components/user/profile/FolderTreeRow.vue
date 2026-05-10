<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-10
 *
 * 公开收藏夹树行组件（user-15 PublicCollectionView 内部递归节点）。
 * 仅在该页面使用，不进 ui-adapter。
 */

import { Folder, FolderOpen, ChevronDown, ChevronRight } from 'lucide-vue-next'
import type { PublicFolderTreeNode } from '@/api/user-profile-public'

const props = withDefaults(
  defineProps<{
    node: PublicFolderTreeNode
    selectedId: number | null
    expandedIds: Set<number>
    depth?: number
  }>(),
  {
    depth: 0,
  },
)

const emit = defineEmits<{
  (e: 'toggle', id: number): void
  (e: 'select', node: PublicFolderTreeNode): void
}>()

const handleSelect = () => emit('select', props.node)

const handleToggle = (ev: Event) => {
  ev.stopPropagation()
  emit('toggle', props.node.id)
}
</script>

<template>
  <div class="flex flex-col">
    <div
      :class="[
        'group flex cursor-pointer items-center gap-1.5 rounded-md py-1.5 pr-2 text-[12.5px] transition',
        selectedId === node.id
          ? 'bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300'
          : 'text-gray-700 hover:bg-black/[0.03] dark:text-gray-300 dark:hover:bg-white/[0.04]',
      ]"
      :style="{ paddingLeft: `${8 + depth * 14}px` }"
      @click="handleSelect"
    >
      <button
        v-if="node.children && node.children.length"
        type="button"
        class="flex size-4 cursor-pointer items-center justify-center rounded text-gray-400 hover:text-amber-500"
        @click="handleToggle"
      >
        <ChevronDown v-if="expandedIds.has(node.id)" class="size-3.5" :stroke-width="2" />
        <ChevronRight v-else class="size-3.5" :stroke-width="2" />
      </button>
      <span v-else class="inline-block size-4" />

      <FolderOpen
        v-if="selectedId === node.id"
        class="size-3.5 shrink-0 text-amber-500"
        :stroke-width="2"
      />
      <Folder v-else class="size-3.5 shrink-0 text-amber-500" :stroke-width="2" />

      <span class="flex-1 truncate">{{ node.name }}</span>
      <span class="shrink-0 font-mono text-[10.5px] text-gray-400">
        {{ node.websiteCount }}
      </span>
    </div>

    <div
      v-if="node.children && node.children.length && expandedIds.has(node.id)"
      class="flex flex-col"
    >
      <FolderTreeRow
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :selected-id="selectedId"
        :expanded-ids="expandedIds"
        :depth="depth + 1"
        @toggle="(id: number) => emit('toggle', id)"
        @select="(n: PublicFolderTreeNode) => emit('select', n)"
      />
    </div>
  </div>
</template>
