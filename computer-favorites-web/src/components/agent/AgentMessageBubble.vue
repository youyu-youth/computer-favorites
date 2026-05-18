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
        class="cf-bubble-content text-[14px] leading-relaxed break-words"
        :class="msg.role === 'user' ? 'cf-bubble--user' : 'cf-bubble--assistant'"
      >
        <MarkdownRender
          :content="msg.content"
          custom-id="agent-bubble"
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
import MarkdownRender from 'markstream-vue'
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
  .cf-ai-avatar--breathing::after {
    animation: none;
    opacity: 1;
    transform: none;
  }
}
</style>

<!-- markstream-vue 内容主题覆盖：非 scoped 块以穿透动态渲染的 DOM -->
<style>
[data-custom-id='agent-bubble'] {
  color: inherit;
}

/* 标题层级 */
[data-custom-id='agent-bubble'] h1 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-top: 1.25rem;
  margin-bottom: 0.5rem;
  color: inherit;
}
[data-custom-id='agent-bubble'] h2 {
  font-size: 1.125rem;
  font-weight: 600;
  margin-top: 1.1rem;
  margin-bottom: 0.45rem;
  color: inherit;
}
[data-custom-id='agent-bubble'] h3 {
  font-size: 1.05rem;
  font-weight: 600;
  margin-top: 1rem;
  margin-bottom: 0.4rem;
  color: inherit;
}
[data-custom-id='agent-bubble'] h4,
[data-custom-id='agent-bubble'] h5,
[data-custom-id='agent-bubble'] h6 {
  font-size: 0.95rem;
  font-weight: 600;
  margin-top: 0.85rem;
  margin-bottom: 0.35rem;
  color: inherit;
}

/* 段落 */
[data-custom-id='agent-bubble'] p {
  margin-top: 0;
  margin-bottom: 0.5rem;
}
[data-custom-id='agent-bubble'] p:last-child {
  margin-bottom: 0;
}

/* 行内代码 */
[data-custom-id='agent-bubble'] code:not(pre code) {
  padding: 0.12em 0.4em;
  font-size: 0.9em;
  border-radius: 0.375rem;
  background-color: rgb(231 229 228 / 0.55);
  color: rgb(41 37 36 / 0.95);
}
html.dark [data-custom-id='agent-bubble'] code:not(pre code) {
  background-color: rgb(41 37 36 / 0.6);
  color: rgb(231 229 228 / 0.95);
}

/* 用户气泡内的行内代码 */
.cf-bubble--user [data-custom-id='agent-bubble'] code:not(pre code) {
  background-color: rgb(255 255 255 / 0.2);
  color: rgb(255 255 255 / 0.9);
}

/* 代码块 */
[data-custom-id='agent-bubble'] pre {
  margin: 0.5rem 0;
  padding: 0.75rem 1rem;
  border-radius: 0.75rem;
  background-color: rgb(245 245 244 / 0.9);
  overflow-x: auto;
  font-size: 0.875rem;
  line-height: 1.55;
}
html.dark [data-custom-id='agent-bubble'] pre {
  background-color: rgb(28 25 23 / 0.7);
}
/* 用户气泡的代码块继承其渐变底色氛围 */
.cf-bubble--user [data-custom-id='agent-bubble'] pre {
  background-color: rgb(0 0 0 / 0.12);
}

[data-custom-id='agent-bubble'] pre code {
  background: none;
  padding: 0;
  font-size: inherit;
  color: inherit;
}

/* 链接 */
[data-custom-id='agent-bubble'] a {
  color: rgb(var(--cf-color-primary-500-rgb) / 1);
  text-decoration: underline;
  text-underline-offset: 2px;
  transition: opacity 160ms ease;
}
[data-custom-id='agent-bubble'] a:hover {
  opacity: 0.8;
}
/* 用户气泡内链接反白 */
.cf-bubble--user [data-custom-id='agent-bubble'] a {
  color: rgb(255 255 255 / 0.9);
}

/* 引用块 */
[data-custom-id='agent-bubble'] blockquote {
  margin: 0.5rem 0;
  padding: 0.35rem 0 0.35rem 0.75rem;
  border-left: 3px solid rgb(var(--cf-color-primary-500-rgb) / 0.5);
  background-color: rgb(0 0 0 / 0.02);
  border-radius: 0 0.375rem 0.375rem 0;
  color: rgb(82 82 91 / 0.9);
}
html.dark [data-custom-id='agent-bubble'] blockquote {
  border-left-color: rgb(var(--cf-color-primary-500-rgb) / 0.4);
  background-color: rgb(255 255 255 / 0.02);
  color: rgb(168 162 158 / 0.9);
}

/* 无序/有序列表 */
[data-custom-id='agent-bubble'] ul,
[data-custom-id='agent-bubble'] ol {
  margin: 0.35rem 0;
  padding-left: 1.35rem;
}
[data-custom-id='agent-bubble'] li {
  margin-bottom: 0.15rem;
}
[data-custom-id='agent-bubble'] ul {
  list-style-type: disc;
}
[data-custom-id='agent-bubble'] ol {
  list-style-type: decimal;
}

/* 水平线 */
[data-custom-id='agent-bubble'] hr {
  margin: 0.75rem 0;
  border: none;
  border-top: 1px solid rgb(168 162 158 / 0.2);
}
html.dark [data-custom-id='agent-bubble'] hr {
  border-top-color: rgb(255 255 255 / 0.08);
}

/* 表格 */
[data-custom-id='agent-bubble'] table {
  width: 100%;
  margin: 0.5rem 0;
  border-collapse: collapse;
  font-size: 0.875rem;
}
[data-custom-id='agent-bubble'] th,
[data-custom-id='agent-bubble'] td {
  padding: 0.4rem 0.65rem;
  text-align: left;
  border: 1px solid rgb(168 162 158 / 0.18);
}
[data-custom-id='agent-bubble'] th {
  font-weight: 600;
  background-color: rgb(0 0 0 / 0.03);
}
html.dark [data-custom-id='agent-bubble'] th {
  background-color: rgb(255 255 255 / 0.04);
}
html.dark [data-custom-id='agent-bubble'] th,
html.dark [data-custom-id='agent-bubble'] td {
  border-color: rgb(255 255 255 / 0.07);
}

/* 强调 */
[data-custom-id='agent-bubble'] strong {
  font-weight: 600;
}

/* 图片 */
[data-custom-id='agent-bubble'] img {
  max-width: 100%;
  border-radius: 0.5rem;
  margin: 0.5rem 0;
}
</style>
