<template>
  <div
    class="cf-bubble group/bubble flex gap-3"
    :class="msg.role === 'user' ? 'flex-row-reverse' : ''"
  >
    <!-- Avatar -->
    <div class="shrink-0">
      <!-- User avatar：inset highlight 圆形纸面 -->
      <div
        v-if="msg.role === 'user'"
        class="grid h-8 w-8 place-items-center rounded-full bg-stone-100 dark:bg-stone-800 ring-1 ring-inset ring-stone-900/[0.06] dark:ring-white/[0.08] shadow-[inset_0_1px_0_rgba(255,255,255,0.6)] dark:shadow-[inset_0_1px_0_rgba(255,255,255,0.06)]"
      >
        <UserRound :size="14" :stroke-width="1.75" class="text-stone-500 dark:text-stone-400" />
      </div>
      <!-- AI avatar：呼吸态 + brand 渐变 -->
      <div
        v-else
        class="cf-ai-avatar relative grid h-8 w-8 place-items-center rounded-full text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.32),0_8px_18px_-8px_rgb(var(--cf-color-primary-500-rgb)/0.5)]"
        :class="{ 'cf-ai-avatar--breathing': isStreaming }"
      >
        <Sparkles :size="14" :stroke-width="2" />
      </div>
    </div>

    <!-- Message body -->
    <div class="max-w-[78%] md:max-w-[70%] min-w-0">
      <!-- Thinking steps (AI only) -->
      <AgentThinkingSteps
        v-if="msg.role === 'assistant' && msg.thinkingSteps.length > 0"
        :steps="msg.thinkingSteps"
        class="mb-2"
      />

      <!-- 气泡主体 -->
      <div
        v-if="msg.content"
        class="cf-bubble-content text-[14px] leading-relaxed whitespace-pre-wrap break-words"
        :class="msg.role === 'user' ? 'cf-bubble--user' : 'cf-bubble--assistant'"
      >
        {{ msg.content }}
        <!-- Streaming cursor：1px brand 高光条，平滑闪烁 -->
        <span
          v-if="isStreaming && msg.role === 'assistant'"
          aria-hidden="true"
          class="cf-stream-cursor"
        />
      </div>

      <!-- Action bar (AI 已完成且有内容) -->
      <div
        v-if="msg.role === 'assistant' && !isStreaming && msg.content"
        class="cf-action-bar mt-1.5 flex items-center gap-1 opacity-0 group-hover/bubble:opacity-100 transition-opacity duration-200 ease-[cubic-bezier(0.16,1,0.3,1)]"
      >
        <button class="cf-action" title="复制" @click="copyContent">
          <component :is="copied ? Check : Copy" :size="13" :stroke-width="1.75" />
        </button>
        <button class="cf-action" title="重新生成" @click="$emit('regenerate')">
          <RotateCcw :size="13" :stroke-width="1.75" />
        </button>
        <button
          class="cf-action"
          :class="feedback === 'like' ? '!text-emerald-500' : ''"
          title="赞"
          @click="toggleFeedback('like')"
        >
          <ThumbsUp :size="13" :stroke-width="1.75" />
        </button>
        <button
          class="cf-action"
          :class="feedback === 'dislike' ? '!text-rose-500' : ''"
          title="踩"
          @click="toggleFeedback('dislike')"
        >
          <ThumbsDown :size="13" :stroke-width="1.75" />
        </button>
      </div>

      <!-- Edit mode (user) -->
      <div v-if="isEditing" class="mt-2 flex gap-2">
        <textarea
          ref="editInputRef"
          v-model="editContent"
          class="flex-1 text-sm px-3 py-2 rounded-xl bg-white dark:bg-stone-900 ring-1 ring-stone-200 dark:ring-stone-700 text-stone-800 dark:text-stone-100 resize-none outline-none transition-shadow focus:ring-2 focus:ring-primary-400"
          rows="2"
        />
        <div class="flex flex-col gap-1">
          <button class="cf-edit-btn cf-edit-btn--primary" @click="confirmEdit">发送</button>
          <button class="cf-edit-btn cf-edit-btn--ghost" @click="cancelEdit">取消</button>
        </div>
      </div>

      <!-- 编辑入口：仅 user 完成态 -->
      <div v-if="msg.role === 'user' && !isStreaming && !isEditing" class="flex justify-end mt-1 opacity-0 group-hover/bubble:opacity-100 transition-opacity duration-200">
        <button class="cf-edit-trigger" @click="startEdit">
          <Pencil :size="11" :stroke-width="1.75" />
          编辑
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { Sparkles, UserRound, Copy, Check, RotateCcw, ThumbsUp, ThumbsDown, Pencil } from 'lucide-vue-next'
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

async function copyContent() {
  try {
    await navigator.clipboard.writeText(props.msg.content)
    copied.value = true
    if (copiedTimer) clearTimeout(copiedTimer)
    copiedTimer = setTimeout(() => {
      copied.value = false
      copiedTimer = null
    }, 1600)
  } catch { /* ignore */ }
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
/* 进场动画：所有气泡 fade-up */
.cf-bubble {
  animation: cf-bubble-in 360ms cubic-bezier(0.16, 1, 0.3, 1) both;
}
@keyframes cf-bubble-in {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 气泡主体共用基底 */
.cf-bubble-content {
  position: relative;
  padding: 0.625rem 0.875rem;
}

/* 用户气泡：brand 渐变 + 内嵌高光 + 染色阴影 + 不对称尾角 */
.cf-bubble--user {
  color: white;
  border-radius: 1rem 1rem 0.375rem 1rem;
  background: linear-gradient(
    180deg,
    rgb(var(--cf-color-primary-400-rgb) / 1) 0%,
    rgb(var(--cf-color-primary-500-rgb) / 1) 60%,
    rgb(var(--cf-color-primary-600-rgb) / 1) 100%
  );
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.32),
    inset 0 -1px 0 0 rgba(0, 0, 0, 0.1),
    0 14px 28px -10px rgb(var(--cf-color-primary-500-rgb) / 0.45);
}

/* 助手气泡：纸质 surface + ring + inset hairline + tinted shadow */
.cf-bubble--assistant {
  color: rgb(41 37 36 / 1);
  background-color: rgb(255 255 255 / 0.95);
  border-radius: 1rem 1rem 1rem 0.375rem;
  box-shadow:
    inset 0 0 0 1px rgb(0 0 0 / 0.05),
    inset 0 1px 0 0 rgba(255, 255, 255, 0.8),
    0 12px 24px -16px rgba(15, 23, 42, 0.18);
}
:global(html.dark) .cf-bubble--assistant {
  color: rgb(231 229 228 / 1);
  background-color: rgb(28 25 23 / 0.85);
  box-shadow:
    inset 0 0 0 1px rgb(255 255 255 / 0.06),
    inset 0 1px 0 0 rgba(255, 255, 255, 0.04),
    0 14px 28px -16px rgba(0, 0, 0, 0.55);
}

/* 流式光标：1.5px 高亮条 + 平滑闪烁 */
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
  0%, 100% { opacity: 1; transform: scaleY(1); }
  50% { opacity: 0.25; transform: scaleY(0.8); }
}

/* AI 头像：brand 渐变 */
.cf-ai-avatar {
  background: linear-gradient(
    135deg,
    rgb(var(--cf-color-primary-400-rgb) / 1) 0%,
    rgb(var(--cf-color-primary-500-rgb) / 1) 50%,
    rgb(var(--cf-color-primary-600-rgb) / 1) 100%
  );
}
/* 流式时呼吸 ring */
.cf-ai-avatar--breathing::after {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 999px;
  border: 1.5px solid rgb(var(--cf-color-primary-500-rgb) / 0.5);
  animation: cf-breathe 1.6s cubic-bezier(0.16, 1, 0.3, 1) infinite;
}
@keyframes cf-breathe {
  0%, 100% { opacity: 0; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.15); }
}

/* Action bar 圆形按钮 */
.cf-action {
  display: inline-grid;
  place-items: center;
  width: 26px;
  height: 26px;
  border-radius: 8px;
  color: rgb(168 162 158 / 1);
  cursor: pointer;
  transition:
    color 160ms cubic-bezier(0.16, 1, 0.3, 1),
    background-color 160ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 160ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cf-action:hover {
  color: rgb(63 63 70 / 1);
  background-color: rgb(0 0 0 / 0.04);
}
:global(html.dark) .cf-action {
  color: rgb(120 113 108 / 1);
}
:global(html.dark) .cf-action:hover {
  color: rgb(231 229 228 / 1);
  background-color: rgb(255 255 255 / 0.06);
}
.cf-action:active {
  transform: scale(0.94);
}

/* 编辑入口（user 气泡下方） */
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
  color: rgb(63 63 70 / 1);
  background-color: rgb(0 0 0 / 0.04);
}
:global(html.dark) .cf-edit-trigger {
  color: rgb(120 113 108 / 1);
}
:global(html.dark) .cf-edit-trigger:hover {
  color: rgb(231 229 228 / 1);
  background-color: rgb(255 255 255 / 0.06);
}

/* 编辑模式按钮 */
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
  color: rgb(82 82 91 / 1);
  background-color: transparent;
  box-shadow: inset 0 0 0 1px rgb(0 0 0 / 0.08);
}
.cf-edit-btn--ghost:hover {
  background-color: rgb(0 0 0 / 0.04);
  color: rgb(24 24 27 / 1);
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
