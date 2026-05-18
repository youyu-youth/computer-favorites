<template>
  <div
    class="cf-session-item group relative flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2.5 text-sm transition-colors duration-200"
    :class="
      isActive
        ? 'border border-primary-500/50 bg-primary-500/[0.08] text-primary-700 dark:text-white'
        : 'border border-transparent text-stone-500 hover:bg-stone-900/[0.05] hover:text-stone-900 dark:hover:bg-white/[0.05] dark:hover:text-stone-200'
    "
    :data-active="isActive ? 'true' : 'false'"
    @click="$emit('select')"
  >
    <MessageCircle
      class="shrink-0"
      :class="
        isActive
          ? 'text-primary-500'
          : 'text-stone-500 group-hover:text-stone-700 dark:text-stone-600 dark:group-hover:text-stone-300'
      "
      :size="16"
      :stroke-width="1.75"
      :fill="isActive ? 'currentColor' : 'none'"
    />

    <input
      v-if="isRenaming"
      ref="renameInputRef"
      v-model="renameText"
      class="min-w-0 flex-1 rounded-md border border-primary-500/50 bg-white px-2 py-1 text-[13px] text-stone-900 outline-none dark:bg-[#0f0f11] dark:text-stone-100"
      @keydown.enter="confirmRename"
      @keydown.escape="cancelRename"
      @click.stop
    />
    <span
      v-else
      class="min-w-0 flex-1 truncate text-[13px] transition-colors"
      :class="isActive ? 'font-semibold' : 'font-medium'"
    >
      {{ session.title || '新对话' }}
    </span>

    <span
      v-if="!isRenaming"
      class="shrink-0 text-[11px] font-medium text-stone-600 group-hover:hidden"
    >
      {{ displayTime }}
    </span>

    <div
      class="cf-session-actions pointer-events-none hidden shrink-0 items-center gap-0.5 opacity-0 transition-opacity duration-200 group-hover:pointer-events-auto group-hover:flex group-hover:opacity-100"
    >
      <button
        type="button"
        class="cf-session-action"
        :class="
          session.pinned ? 'text-primary-500 hover:text-primary-600' : 'hover:text-primary-500'
        "
        :title="session.pinned ? '取消置顶' : '置顶'"
        @click.stop="$emit('togglePin', session.id, !session.pinned)"
      >
        <Pin :size="13" :stroke-width="1.75" :fill="session.pinned ? 'currentColor' : 'none'" />
      </button>
      <button type="button" class="cf-session-action" title="重命名" @click.stop="startRename">
        <Pencil :size="13" :stroke-width="1.75" />
      </button>
      <button
        type="button"
        class="cf-session-action hover:!text-rose-500"
        title="删除"
        @click.stop="$emit('delete', session.id)"
      >
        <Trash2 :size="13" :stroke-width="1.75" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, nextTick } from 'vue'
import { MessageCircle, Pin, Pencil, Trash2 } from 'lucide-vue-next'
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

const displayTime = computed(() => {
  const raw = props.session.lastMessageAt || props.session.createTime
  if (!raw) return ''
  const date = new Date(raw)
  if (Number.isNaN(date.getTime())) return ''
  const now = new Date()
  const sameDay = date.toDateString() === now.toDateString()
  if (sameDay) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
})

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
  color: rgb(120 113 108 / 1);
  cursor: pointer;
  transition:
    color 160ms cubic-bezier(0.16, 1, 0.3, 1),
    background-color 160ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 160ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cf-session-action:hover {
  background-color: rgb(0 0 0 / 0.05);
  color: rgb(28 25 23 / 1);
}
:global(html.dark) .cf-session-action:hover {
  background-color: rgb(255 255 255 / 0.06);
  color: rgb(231 229 228 / 1);
}
.cf-session-action:active {
  transform: scale(0.92);
}
</style>
