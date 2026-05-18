<template>
  <div
    class="cf-bubble group/bubble flex gap-3 sm:gap-4"
    :class="msg.role === 'user' ? 'justify-end' : ''"
  >
    <template v-if="msg.role === 'assistant'">
      <div
        class="cf-ai-avatar relative mt-1 grid h-8 w-8 shrink-0 place-items-center rounded-full border border-primary-500/50 bg-primary-500/[0.12] text-primary-400 shadow-md shadow-orange-500/10 sm:h-9 sm:w-9"
        :class="{ 'cf-ai-avatar--breathing': isStreaming }"
      >
        <Sparkles :size="16" :stroke-width="2" />
      </div>
      <div class="min-w-0 flex-1 space-y-3">
        <AgentThinkingSteps v-if="msg.thinkingSteps.length > 0" :steps="msg.thinkingSteps" />
        <div
          v-if="msg.content"
          class="text-[15px] leading-relaxed text-stone-700 dark:text-stone-300"
        >
          <span class="whitespace-pre-wrap break-words">{{ msg.content }}</span>
          <span v-if="isStreaming" aria-hidden="true" class="cf-stream-cursor" />
        </div>
        <div
          v-if="!isStreaming && msg.content"
          class="flex items-center gap-2 opacity-0 transition-opacity duration-200 group-hover/bubble:opacity-100"
        >
          <button class="cf-action" title="复制" @click="copyContent">
            <component :is="copied ? Check : Copy" :size="15" :stroke-width="1.75" />
          </button>
          <button class="cf-action" title="重新生成" @click="$emit('regenerate')">
            <RotateCcw :size="15" :stroke-width="1.75" />
          </button>
          <button
            class="cf-action"
            :class="feedback === 'like' ? '!text-emerald-500' : ''"
            title="赞"
            @click="toggleFeedback('like')"
          >
            <ThumbsUp :size="15" :stroke-width="1.75" />
          </button>
          <button
            class="cf-action"
            :class="feedback === 'dislike' ? '!text-rose-500' : ''"
            title="踩"
            @click="toggleFeedback('dislike')"
          >
            <ThumbsDown :size="15" :stroke-width="1.75" />
          </button>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="flex max-w-[min(42rem,78%)] flex-col items-end gap-1.5">
        <div
          v-if="msg.content"
          class="rounded-2xl rounded-tr-sm border border-stone-200 bg-white px-5 py-3.5 text-[14px] leading-relaxed text-stone-800 shadow-sm dark:border-white/10 dark:bg-[#121214] dark:text-stone-200"
        >
          <span class="whitespace-pre-wrap break-words">{{ msg.content }}</span>
        </div>
        <div class="flex items-center gap-1.5 pr-1 text-[11px] font-medium text-stone-600">
          <span>{{ displayTime }}</span>
          <CheckCheck class="text-primary-400" :size="14" :stroke-width="1.75" />
        </div>
        <div v-if="isEditing" class="mt-2 flex w-full gap-2">
          <textarea
            ref="editInputRef"
            v-model="editContent"
            class="min-h-20 flex-1 resize-none rounded-xl border border-stone-200 bg-white px-3 py-2 text-sm text-stone-900 outline-none transition-colors focus:border-primary-500/60 dark:border-white/10 dark:bg-[#0f0f11] dark:text-stone-100"
            rows="2"
          />
          <div class="flex flex-col gap-1">
            <button class="cf-edit-btn cf-edit-btn--primary" @click="confirmEdit">发送</button>
            <button class="cf-edit-btn cf-edit-btn--ghost" @click="cancelEdit">取消</button>
          </div>
        </div>
        <button
          v-if="!isStreaming && !isEditing"
          class="cf-edit-trigger opacity-0 transition-opacity duration-200 group-hover/bubble:opacity-100"
          @click="startEdit"
        >
          <Pencil :size="12" :stroke-width="1.75" />
          编辑
        </button>
      </div>
      <div
        class="mt-1 grid h-8 w-8 shrink-0 place-items-center rounded-full border border-stone-200 bg-white text-stone-500 sm:h-9 sm:w-9 dark:border-white/10 dark:bg-[#171719] dark:text-stone-400"
      >
        <UserRound :size="16" :stroke-width="1.75" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, nextTick } from 'vue'
import {
  Sparkles,
  UserRound,
  Copy,
  Check,
  CheckCheck,
  RotateCcw,
  ThumbsUp,
  ThumbsDown,
  Pencil,
} from 'lucide-vue-next'
import type { AgentMessage } from '@/types/agent'
import AgentThinkingSteps from '@/components/agent/AgentThinkingSteps.vue'

defineOptions({ name: 'AgentMessageBubble' })

const props = defineProps<{
  msg: AgentMessage
  isStreaming: boolean
}>()

const emit = defineEmits<{
  regenerate: []
  editResend: [msgId: string, newContent: string]
}>()

const feedback = ref<'like' | 'dislike' | null>(null)
const isEditing = ref(false)
const editContent = ref('')
const editInputRef = ref<HTMLTextAreaElement | null>(null)

/** 复制成功反馈：图标短暂切换为 Check，1.6s 后回弹 */
const copied = ref(false)
let copiedTimer: ReturnType<typeof setTimeout> | null = null

const displayTime = computed(() => {
  const date = new Date(props.msg.createdAt)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
})

async function copyContent() {
  try {
    await navigator.clipboard.writeText(props.msg.content)
    copied.value = true
    if (copiedTimer) clearTimeout(copiedTimer)
    copiedTimer = setTimeout(() => {
      copied.value = false
      copiedTimer = null
    }, 1600)
  } catch {
    /* ignore */
  }
}

function toggleFeedback(type: 'like' | 'dislike') {
  feedback.value = feedback.value === type ? null : type
}

function startEdit() {
  isEditing.value = true
  editContent.value = props.msg.content
  nextTick(() => editInputRef.value?.focus())
}

function cancelEdit() {
  isEditing.value = false
  editContent.value = ''
}

function confirmEdit() {
  if (!editContent.value.trim()) return
  emit('editResend', props.msg.id, editContent.value.trim())
  isEditing.value = false
}
</script>

<style scoped>
.cf-bubble {
  animation: cf-bubble-in 360ms cubic-bezier(0.16, 1, 0.3, 1) both;
}
@keyframes cf-bubble-in {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.cf-stream-cursor {
  display: inline-block;
  width: 1.5px;
  height: 1em;
  margin-left: 2px;
  vertical-align: text-bottom;
  background-color: rgb(var(--cf-color-primary-500-rgb) / 1);
  border-radius: 999px;
  animation: cf-stream-cursor 1s cubic-bezier(0.16, 1, 0.3, 1) infinite;
}
@keyframes cf-stream-cursor {
  0%,
  100% {
    opacity: 1;
    transform: scaleY(1);
  }
  50% {
    opacity: 0.25;
    transform: scaleY(0.8);
  }
}

.cf-ai-avatar--breathing::after {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 999px;
  border: 1.5px solid rgb(var(--cf-color-primary-500-rgb) / 0.5);
  animation: cf-breathe 1.6s cubic-bezier(0.16, 1, 0.3, 1) infinite;
}
@keyframes cf-breathe {
  0%,
  100% {
    opacity: 0;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.15);
  }
}

.cf-action {
  display: inline-grid;
  place-items: center;
  width: 26px;
  height: 26px;
  border-radius: 8px;
  border: 1px solid rgb(214 211 209 / 0.9);
  color: rgb(120 113 108 / 1);
  cursor: pointer;
  transition:
    color 160ms cubic-bezier(0.16, 1, 0.3, 1),
    background-color 160ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 160ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cf-action:hover {
  color: rgb(28 25 23 / 1);
  background-color: rgb(0 0 0 / 0.05);
}
:global(html.dark) .cf-action {
  border: 1px solid rgb(39 39 42 / 0.8);
}
:global(html.dark) .cf-action:hover {
  color: rgb(231 229 228 / 1);
  background-color: rgb(255 255 255 / 0.06);
}
.cf-action:active {
  transform: scale(0.94);
}

.cf-edit-trigger {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.125rem 0.5rem;
  border-radius: 999px;
  font-size: 11px;
  color: rgb(168 162 158 / 1);
  cursor: pointer;
  transition:
    color 160ms cubic-bezier(0.16, 1, 0.3, 1),
    background-color 160ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cf-edit-trigger:hover {
  color: rgb(28 25 23 / 1);
  background-color: rgb(0 0 0 / 0.05);
}
:global(html.dark) .cf-edit-trigger:hover {
  color: rgb(231 229 228 / 1);
  background-color: rgb(255 255 255 / 0.06);
}

.cf-edit-btn {
  padding: 0.25rem 0.625rem;
  border-radius: 0.5rem;
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  transition: all 160ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cf-edit-btn--primary {
  color: white;
  background-color: rgb(var(--cf-color-primary-500-rgb) / 1);
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.22),
    0 4px 10px -4px rgb(var(--cf-color-primary-500-rgb) / 0.4);
}
.cf-edit-btn--primary:hover {
  background-color: rgb(var(--cf-color-primary-600-rgb) / 1);
}
.cf-edit-btn--ghost {
  color: rgb(87 83 78 / 1);
  background-color: transparent;
  box-shadow: inset 0 0 0 1px rgb(0 0 0 / 0.08);
}
.cf-edit-btn--ghost:hover {
  background-color: rgb(0 0 0 / 0.04);
  color: rgb(28 25 23 / 1);
}
:global(html.dark) .cf-edit-btn--ghost {
  color: rgb(168 162 158 / 1);
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 0.1);
}
:global(html.dark) .cf-edit-btn--ghost:hover {
  background-color: rgb(255 255 255 / 0.06);
  color: rgb(231 229 228 / 1);
}

@media (prefers-reduced-motion: reduce) {
  .cf-bubble,
  .cf-stream-cursor,
  .cf-ai-avatar--breathing::after {
    animation: none;
    opacity: 1;
    transform: none;
  }
}
</style>
