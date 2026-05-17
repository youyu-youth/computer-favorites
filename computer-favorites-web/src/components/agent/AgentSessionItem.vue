<template>
  <div
    class="group flex items-center gap-2 px-3 py-2.5 mx-1 rounded-lg cursor-pointer transition-colors text-sm"
    :class="isActive
      ? 'bg-gray-100 dark:bg-gray-800 text-gray-900 dark:text-gray-100'
      : 'text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800/50'"
    @click="$emit('select')"
  >
    <!-- Pin icon -->
    <svg
      v-if="session.pinned"
      class="w-3 h-3 shrink-0 text-amber-500"
      fill="currentColor" viewBox="0 0 24 24"
    >
      <path d="M16 9V4h1V2H7v2h1v5l-2 4v2h5v6l1 1 1-1v-6h5v-2l-2-4z" />
    </svg>

    <!-- Title or rename input -->
    <input
      v-if="isRenaming"
      ref="renameInputRef"
      v-model="renameText"
      class="flex-1 text-sm px-1.5 py-0.5 rounded bg-white dark:bg-gray-900
             border border-amber-400 outline-none text-gray-800 dark:text-gray-200"
      @keydown.enter="confirmRename"
      @keydown.escape="cancelRename"
      @click.stop
    />
    <span v-else class="flex-1 truncate">
      {{ session.title || '新对话' }}
    </span>

    <!-- Hover actions -->
    <div class="hidden group-hover:flex items-center gap-0.5 shrink-0">
      <!-- Toggle pin -->
      <button
        v-if="session.pinned"
        class="p-0.5 text-amber-500 hover:text-amber-600 rounded cursor-pointer"
        title="取消置顶"
        @click.stop="$emit('togglePin', session.id, false)"
      >
        <svg class="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 24 24">
          <path d="M16 9V4h1V2H7v2h1v5l-2 4v2h5v6l1 1 1-1v-6h5v-2l-2-4z" />
        </svg>
      </button>
      <button
        v-else
        class="p-0.5 text-gray-400 hover:text-amber-500 rounded cursor-pointer"
        title="置顶"
        @click.stop="$emit('togglePin', session.id, true)"
      >
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M5 15l7-7 7 7" />
        </svg>
      </button>
      <!-- Rename -->
      <button
        class="p-0.5 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 rounded cursor-pointer"
        title="重命名"
        @click.stop="startRename"
      >
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
        </svg>
      </button>
      <!-- Delete -->
      <button
        class="p-0.5 text-gray-400 hover:text-red-500 rounded cursor-pointer"
        title="删除"
        @click.stop="$emit('delete', session.id)"
      >
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import type { AgentSession } from '@/types/agent'

defineOptions({ name: 'AgentSessionItem' })

const props = defineProps<{
  session: AgentSession
  isActive: boolean
}>()

const emit = defineEmits<{
  select: []
  togglePin: [id: number, pinned: boolean]
  delete: [id: number]
  rename: [id: number, title: string]
}>()

const isRenaming = ref(false)
const renameText = ref('')
const renameInputRef = ref<HTMLInputElement | null>(null)

function startRename() {
  renameText.value = props.session.title || ''
  isRenaming.value = true
  nextTick(() => renameInputRef.value?.focus())
}

function confirmRename() {
  if (renameText.value.trim()) {
    emit('rename', props.session.id, renameText.value.trim())
  }
  isRenaming.value = false
}

function cancelRename() {
  isRenaming.value = false
}
</script>
