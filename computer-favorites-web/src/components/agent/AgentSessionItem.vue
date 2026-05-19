<template>
  <div
    class="cf-session-item group relative flex items-center gap-2 mx-2 px-3 py-2.5 rounded-xl cursor-pointer text-sm transition-all duration-200 ease-[cubic-bezier(0.16,1,0.3,1)]"
    :class="isActive
      ? 'bg-stone-900/[0.04] text-stone-900 dark:bg-white/[0.06] dark:text-stone-50'
      : 'text-stone-500 dark:text-stone-400 hover:bg-stone-900/[0.025] dark:hover:bg-white/[0.03] hover:text-stone-700 dark:hover:text-stone-200'"
    :data-active="isActive ? 'true' : 'false'"
    @click="$emit('select')"
  >
    <!-- 选中态左侧 brand rail -->
    <span
      v-if="isActive"
      aria-hidden="true"
      class="cf-session-rail absolute left-0 top-1/2 h-5 w-[2px] -translate-y-1/2 rounded-r-full bg-primary-500"
    />

    <!-- Pin badge（置顶标识） -->
    <Pin
      v-if="session.pinned"
      class="shrink-0 text-primary-500"
      :size="11"
      :stroke-width="2.25"
      fill="currentColor"
    />

    <!-- 标题 / 重命名输入 -->
    <input
      v-if="isRenaming"
      ref="renameInputRef"
      v-model="renameText"
      class="flex-1 text-sm px-1.5 py-0.5 rounded-md bg-white/95 dark:bg-stone-900 ring-1 ring-primary-400 outline-none text-stone-800 dark:text-stone-100 transition-shadow focus:ring-2 focus:ring-primary-500/40"
      @keydown.enter="confirmRename"
      @keydown.escape="cancelRename"
      @click.stop
    />
    <span
      v-else
      class="flex-1 truncate transition-colors"
      :class="isActive ? 'font-medium' : ''"
    >
      {{ session.title || '新对话' }}
    </span>

    <!-- Hover 操作槽：滑入 + fade in -->
    <div class="cf-session-actions pointer-events-none flex shrink-0 items-center gap-0.5 opacity-0 translate-x-1 transition-all duration-200 ease-[cubic-bezier(0.16,1,0.3,1)] group-hover:pointer-events-auto group-hover:opacity-100 group-hover:translate-x-0">
      <!-- Pin / Unpin -->
      <button
        type="button"
        class="cf-session-action"
        :class="session.pinned ? 'text-primary-500 hover:text-primary-600' : 'hover:text-primary-500'"
        :title="session.pinned ? '取消置顶' : '置顶'"
        @click.stop="$emit('togglePin', session.id, !session.pinned)"
      >
        <Pin :size="13" :stroke-width="1.75" :fill="session.pinned ? 'currentColor' : 'none'" />
      </button>
      <!-- Rename -->
      <button type="button" class="cf-session-action" title="重命名" @click.stop="startRename">
        <Pencil :size="13" :stroke-width="1.75" />
      </button>
      <!-- Delete -->
      <button type="button" class="cf-session-action hover:!text-rose-500" title="删除" @click.stop="$emit('delete', session.id)">
        <Trash2 :size="13" :stroke-width="1.75" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { Pin, Pencil, Trash2 } from 'lucide-vue-next'
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

<style scoped>
.cf-session-action {
  display: inline-grid;
  place-items: center;
  width: 22px;
  height: 22px;
  border-radius: 8px;
  color: rgb(168 162 158 / 1);
  cursor: pointer;
  transition:
    color 160ms cubic-bezier(0.16, 1, 0.3, 1),
    background-color 160ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 160ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cf-session-action:hover {
  background-color: rgb(0 0 0 / 0.04);
  color: rgb(63 63 70 / 1);
}
.cf-session-action:active {
  transform: scale(0.92);
}

.cf-session-rail {
  animation: cf-rail-in 320ms cubic-bezier(0.16, 1, 0.3, 1) both;
}
@keyframes cf-rail-in {
  from {
    transform: translate(-2px, -50%) scaleY(0.4);
    opacity: 0;
  }
  to {
    transform: translate(0, -50%) scaleY(1);
    opacity: 1;
  }
}
</style>

<style>
html.dark .cf-session-action {
  color: rgb(120 113 108 / 1);
}
html.dark .cf-session-action:hover {
  background-color: rgb(255 255 255 / 0.06);
  color: rgb(231 229 228 / 1);
}
</style>
