<template>
  <div
    class="cf-bubble group/bubble flex items-start gap-3"
    :class="msg.role === 'user' ? 'flex-row-reverse' : ''"
  >
    <!-- Message body -->
    <div
      class="cf-bubble-body max-w-[82%] sm:max-w-[78%] md:max-w-[70%] min-w-0"
      :class="msg.role === 'user' ? 'cf-bubble-body--user' : ''"
    >
      <!-- Thinking steps (AI only) -->
      <AgentThinkingSteps
        v-if="msg.role === 'assistant' && msg.thinkingSteps.length > 0"
        :steps="msg.thinkingSteps"
        class="mb-2"
      />

      <!-- 气泡主体 -->
      <div
        v-if="msg.content"
        class="cf-bubble-content text-[14px] leading-[1.72] break-words"
        :class="msg.role === 'user' ? 'cf-bubble--user' : 'cf-bubble--assistant'"
      >
        <MarkdownRender
          :content="msg.content"
          custom-id="agent-bubble"
        />
      </div>

      <!-- 工具补全卡片 -->
      <AgentToolCompletionCard
        v-if="msg.role === 'assistant' && msg.toolIncomplete && !isStreaming"
        :data="msg.toolIncomplete"
        class="mt-2"
        @submit="handleCompletionSubmit"
        @close="handleCompletionClose"
      />

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
import { Copy, Check, RotateCcw, ThumbsUp, ThumbsDown, Pencil } from 'lucide-vue-next'
import MarkdownRender from 'markstream-vue'
import type { AgentMessage } from '@/types/agent'
import AgentThinkingSteps from '@/components/agent/AgentThinkingSteps.vue'
import AgentToolCompletionCard from '@/components/agent/AgentToolCompletionCard.vue'

defineOptions({ name: 'AgentMessageBubble' })

const props = defineProps<{
  msg: AgentMessage
  isStreaming: boolean
}>()

const emit = defineEmits<{
  regenerate: []
  editResend: [msgId: string, newContent: string]
  completionSubmit: [message: string]
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

function handleCompletionSubmit(message: string) {
  emit('completionSubmit', message)
}

function handleCompletionClose() {
  if (props.msg.toolIncomplete) {
    props.msg.toolIncomplete = undefined
  }
}
</script>

<style scoped>
/* 进场动画：所有气泡 fade-up */
.cf-bubble {
  align-items: flex-start;
  animation: cf-bubble-in 360ms cubic-bezier(0.16, 1, 0.3, 1) both;
}
@keyframes cf-bubble-in {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

.cf-bubble-body {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.cf-bubble-body--user {
  align-items: flex-end;
}

/* 气泡主体共用基底 */
.cf-bubble-content {
  position: relative;
  display: flow-root;
  max-width: 100%;
  padding: 0.625rem 0.875rem;
  isolation: isolate;
  letter-spacing: 0;
  transform-origin: top;
  transition:
    transform 180ms cubic-bezier(0.16, 1, 0.3, 1),
    box-shadow 180ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cf-bubble-content:hover {
  transform: translateY(-1px);
}
.cf-bubble-content::before {
  content: '';
  position: absolute;
  inset: 1px;
  z-index: 0;
  border-radius: inherit;
  pointer-events: none;
}
.cf-bubble-content :where([data-custom-id='agent-bubble']) {
  position: relative;
  z-index: 1;
}

/* 用户气泡：brand 渐变 + 内嵌高光 + 染色阴影 + 不对称尾角 */
.cf-bubble--user {
  color: white;
  border-radius: 1rem 1rem 0.375rem 1rem;
  background:
    radial-gradient(circle at 18% 0%, rgb(255 255 255 / 0.28), transparent 34%),
    linear-gradient(
      180deg,
      rgb(var(--cf-color-primary-400-rgb) / 1) 0%,
      rgb(var(--cf-color-primary-500-rgb) / 1) 58%,
      rgb(var(--cf-color-primary-600-rgb) / 1) 100%
    );
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.32),
    inset 0 -1px 0 0 rgba(0, 0, 0, 0.1),
    0 14px 28px -10px rgb(var(--cf-color-primary-500-rgb) / 0.45);
}
.cf-bubble--user::before {
  background: linear-gradient(135deg, rgb(255 255 255 / 0.16), transparent 42%);
}

/* 助手气泡：纸质 surface + ring + inset hairline + tinted shadow */
.cf-bubble--assistant {
  color: rgb(41 37 36 / 1);
  background:
    linear-gradient(180deg, rgb(255 255 255 / 0.96), rgb(250 250 249 / 0.92));
  border-radius: 1rem 1rem 1rem 0.375rem;
  box-shadow:
    inset 0 0 0 1px rgb(0 0 0 / 0.05),
    inset 0 1px 0 0 rgba(255, 255, 255, 0.8),
    0 12px 24px -16px rgba(15, 23, 42, 0.18);
}
.cf-bubble--assistant::before {
  background:
    linear-gradient(135deg, rgb(var(--cf-color-primary-500-rgb) / 0.08), transparent 34%),
    linear-gradient(180deg, rgb(255 255 255 / 0.5), transparent 40%);
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

@media (prefers-reduced-motion: reduce) {
  .cf-bubble,
  .cf-bubble-content {
    animation: none;
    transition: none;
    opacity: 1;
    transform: none;
  }
}
</style>

<!-- markstream-vue 内容主题覆盖：非 scoped 块以穿透动态渲染的 DOM -->
<style>
/*
 * 适配策略：用 [data-custom-id] 覆盖 markstream-vue 内部的全套 CSS 变量。
 * markstream-vue 在 .markstream-vue 上定义主变量（--ms-*）和派生变量（--code-bg 等），
 * 然后 .dark .markstream-vue 仅改写主变量。派生变量通过 var(--ms-*) 间接引用，
 * 主变量改变后派生变量自动更新。我们通过更高特异性选择器全量覆盖这三层。
 */

/* ===== 根容器透明 ===== */
[data-custom-id='agent-bubble'],
[data-custom-id='agent-bubble'] .markdown-renderer,
[data-custom-id='agent-bubble'] .markstream-vue {
  background: transparent !important;
  margin: 0 !important;
  padding: 0 !important;
  line-height: inherit;
}

/* ===== 浅色模式 — 主变量 + 派生变量 (覆盖 markstream-vue 默认值) ===== */
[data-custom-id='agent-bubble'] {
  /* 聊天气泡内的 Markdown 间距要比正文文档更紧，首行才能贴近顶部。 */
  --ms-text-body: 0.875rem;
  --ms-leading-body: 1.72;
  --ms-flow-paragraph-y: 0.2rem;
  --ms-flow-list-y: 0.35rem;
  --ms-flow-list-item-y: 0.08rem;
  --ms-flow-blockquote-y: 0.45rem;
  --ms-flow-codeblock-y: 0.5rem;
  --ms-flow-table-y: 0.5rem;
  --ms-flow-hr-y: 0.65rem;
  /* 主变量 */
  --ms-background: 0 0% 100%;
  --ms-foreground: 0 0% 10%;
  --ms-muted: 0 0% 96.5%;
  --ms-muted-foreground: 0 0% 55%;
  --ms-secondary: 0 0% 91%;
  --ms-accent: 0 0% 88%;
  --ms-border: 0 0% 87%;
  --ms-ring: 217 119 6;
  --ms-info: 217 119 6;
  --ms-success: 152 56% 39%;
  --ms-warning: 38 64% 46%;
  --ms-destructive: 0 62% 52%;
  --ms-diff-added: 152 50% 36%;
  --ms-diff-removed: 0 58% 48%;
  --ms-highlight: 45 93% 57%;
  --ms-popover: 0 0% 100%;
  /* 派生变量 */
  --inline-code-bg: hsl(var(--ms-secondary));
  --inline-code-fg: hsl(var(--ms-foreground) / 0.75);
  --code-bg: hsl(var(--ms-muted));
  --code-fg: hsl(var(--ms-foreground));
  --code-border: hsl(var(--ms-border));
  --code-header-bg: hsl(var(--ms-secondary));
  --code-action-fg: hsl(var(--ms-muted-foreground));
  --code-action-hover-bg: hsl(var(--ms-accent));
  --code-line-number: hsl(var(--ms-muted-foreground));
  --table-border: hsl(var(--ms-border));
  --table-header-bg: hsl(var(--ms-muted));
  --blockquote-border: hsl(var(--ms-muted-foreground) / 0.2);
  --blockquote-fg: hsl(var(--ms-muted-foreground));
  --hr-border: hsl(var(--ms-border));
  --highlight-bg: hsl(var(--ms-highlight) / 0.35);
  --link-color: rgb(var(--cf-color-primary-500-rgb) / 1);
  --list-marker: hsl(var(--ms-muted-foreground) / 0.5);
  --list-counter-marker: hsl(var(--ms-muted-foreground));
  --tooltip-bg: hsl(0 0% 18%);
  --tooltip-fg: hsl(0 0% 88%);
  --diagram-bg: hsl(var(--ms-muted));
  --diagram-border: hsl(var(--ms-border));
  --diagram-header-bg: hsl(var(--ms-muted));
  --loading-shimmer: hsl(var(--ms-muted) / 0.5);
  --loading-spinner: hsl(var(--ms-muted-foreground));
  --focus-ring: hsl(var(--ms-ring));
}

/* ===== 暗黑模式 — 全量覆盖 ===== */
html.dark [data-custom-id='agent-bubble'] {
  /* 主变量 — 深色纸面调色 */
  --ms-background: 0 0% 7%;
  --ms-foreground: 0 0% 93%;
  --ms-muted: 0 0% 12%;
  --ms-muted-foreground: 0 0% 60%;
  --ms-secondary: 0 0% 16%;
  --ms-accent: 0 0% 22%;
  --ms-border: 0 0% 20%;
  --ms-ring: 245 158 11;
  --ms-info: 245 158 11;
  --ms-success: 152 48% 50%;
  --ms-warning: 32 65% 54%;
  --ms-destructive: 0 60% 50%;
  --ms-diff-added: 152 42% 55%;
  --ms-diff-removed: 0 58% 55%;
  --ms-highlight: 48 65% 50%;
  --ms-popover: 0 0% 9%;
  /* 派生变量自动通过 var(--ms-*) 继承新值 */
  --code-bg: hsl(var(--ms-muted));
  --code-fg: hsl(var(--ms-foreground));
  --code-border: hsl(var(--ms-border));
  --code-header-bg: hsl(var(--ms-secondary));
  --code-action-fg: hsl(var(--ms-muted-foreground));
  --code-action-hover-bg: hsl(var(--ms-accent));
  --code-line-number: hsl(var(--ms-muted-foreground));
  --table-border: hsl(var(--ms-border));
  --table-header-bg: hsl(var(--ms-muted));
  --blockquote-border: hsl(var(--ms-muted-foreground) / 0.2);
  --blockquote-fg: hsl(var(--ms-muted-foreground));
  --hr-border: hsl(var(--ms-border));
  --highlight-bg: hsl(var(--ms-highlight) / 0.28);
  --list-marker: hsl(var(--ms-muted-foreground) / 0.5);
  --list-counter-marker: hsl(var(--ms-muted-foreground));
  --tooltip-bg: hsl(0 0% 12%);
  --tooltip-fg: hsl(0 0% 72%);
  --diagram-bg: hsl(var(--ms-muted));
  --diagram-border: hsl(var(--ms-border));
  --diagram-header-bg: hsl(var(--ms-muted));
  --loading-shimmer: hsl(var(--ms-muted) / 0.5);
  --loading-spinner: hsl(var(--ms-muted-foreground));
  --focus-ring: hsl(var(--ms-ring));
  /* 阴影：暗色模式更深 */
  --ms-shadow-subtle: 0 1px 3px 0 hsl(0 0% 0% / 0.25);
  --ms-shadow-popover: 0 4px 6px -1px hsl(0 0% 0% / 0.2), 0 2px 4px -2px hsl(0 0% 0% / 0.15);
  --ms-shadow-modal: 0 10px 15px -3px hsl(0 0% 0% / 0.5), 0 4px 6px -4px hsl(0 0% 0% / 0.4);
}

/* ===== 用户气泡（brand 渐变底 + 白字）===== */
.cf-bubble--user [data-custom-id='agent-bubble'] {
  --ms-background: 0 0% 10%;
  --ms-foreground: 0 0% 100%;
  --ms-muted: 0 0% 100% / 0.12;
  --ms-muted-foreground: 0 0% 100% / 0.7;
  --ms-secondary: 0 0% 100% / 0.1;
  --ms-accent: 0 0% 100% / 0.18;
  --ms-border: 0 0% 100% / 0.15;
  --ms-ring: 255 255 255;
  --ms-info: 245 158 11;
  --ms-popover: 0 0% 10%;
  --code-bg: hsl(var(--ms-muted));
  --code-fg: hsl(var(--ms-foreground));
  --code-border: hsl(var(--ms-border));
  --code-header-bg: hsl(var(--ms-secondary));
  --code-action-fg: hsl(var(--ms-muted-foreground));
  --code-action-hover-bg: hsl(var(--ms-accent));
  --code-line-number: hsl(var(--ms-muted-foreground));
  --table-border: hsl(var(--ms-border));
  --table-header-bg: hsl(var(--ms-muted));
  --blockquote-border: hsl(var(--ms-muted-foreground) / 0.3);
  --blockquote-fg: hsl(var(--ms-foreground) / 0.75);
  --hr-border: hsl(var(--ms-border));
  --highlight-bg: hsl(var(--ms-highlight) / 0.3);
  --link-color: rgb(255 255 255 / 0.9);
  --list-marker: hsl(var(--ms-muted-foreground) / 0.5);
  --list-counter-marker: hsl(var(--ms-muted-foreground));
  --tooltip-bg: hsl(0 0% 12%);
  --tooltip-fg: hsl(0 0% 88%);
  --diagram-bg: hsl(var(--ms-muted));
  --diagram-border: hsl(var(--ms-border));
  --diagram-header-bg: hsl(var(--ms-muted));
  --loading-shimmer: hsl(var(--ms-muted) / 0.4);
  --loading-spinner: hsl(var(--ms-muted-foreground));
  --focus-ring: hsl(var(--ms-ring));
}

/* ===== 元素级覆盖 — 标题/段落/代码/链接/引用/列表/表格/图片 ===== */

[data-custom-id='agent-bubble'] :is(.paragraph-node, .heading-node, .list-node, .blockquote, .code-block-container, .table-node-wrapper, .hr-node, .vmr-container):first-child,
[data-custom-id='agent-bubble'] :is(.markdown-renderer, .markstream-vue, .node-slot, .node-content) > :is(.paragraph-node, .heading-node, .list-node, .blockquote, .code-block-container, .table-node-wrapper, .hr-node, .vmr-container):first-child {
  margin-top: 0 !important;
}

[data-custom-id='agent-bubble'] :is(.paragraph-node, .heading-node, .list-node, .blockquote, .code-block-container, .table-node-wrapper, .hr-node, .vmr-container):last-child,
[data-custom-id='agent-bubble'] :is(.markdown-renderer, .markstream-vue, .node-slot, .node-content) > :is(.paragraph-node, .heading-node, .list-node, .blockquote, .code-block-container, .table-node-wrapper, .hr-node, .vmr-container):last-child {
  margin-bottom: 0 !important;
}

[data-custom-id='agent-bubble'] .paragraph-node {
  margin: 0 0 0.5rem !important;
  color: inherit;
}

[data-custom-id='agent-bubble'] h1 { font-size:1.25rem; font-weight:600; margin:1.25rem 0 0.5rem; color:inherit; }
[data-custom-id='agent-bubble'] h2 { font-size:1.125rem; font-weight:600; margin:1.1rem 0 0.45rem; color:inherit; }
[data-custom-id='agent-bubble'] h3 { font-size:1.05rem; font-weight:600; margin:1rem 0 0.4rem; color:inherit; }
[data-custom-id='agent-bubble'] h4,
[data-custom-id='agent-bubble'] h5,
[data-custom-id='agent-bubble'] h6 { font-size:0.95rem; font-weight:600; margin:0.85rem 0 0.35rem; color:inherit; }

[data-custom-id='agent-bubble'] p { margin:0 0 0.5rem; color:inherit; }
[data-custom-id='agent-bubble'] p:last-child { margin-bottom:0; }

[data-custom-id='agent-bubble'] code:not(pre code) {
  padding:0.12em 0.4em; font-size:0.9em; border-radius:0.375rem;
  background-color:var(--inline-code-bg); color:var(--inline-code-fg);
}

[data-custom-id='agent-bubble'] pre {
  margin:0.5rem 0; padding:0.75rem 1rem; border-radius:0.75rem;
  background-color:var(--code-bg); overflow-x:auto; font-size:0.875rem; line-height:1.55;
}
[data-custom-id='agent-bubble'] pre code { background:none; padding:0; font-size:inherit; color:inherit; }

[data-custom-id='agent-bubble'] a { color:var(--link-color); text-decoration:underline; text-underline-offset:2px; transition:opacity 160ms ease; }
[data-custom-id='agent-bubble'] a:hover { opacity:0.8; }

[data-custom-id='agent-bubble'] blockquote {
  margin:0.5rem 0; padding:0.35rem 0 0.35rem 0.75rem;
  border-left:3px solid var(--blockquote-border); border-radius:0 0.375rem 0.375rem 0;
  background-color:hsl(var(--ms-muted) / 0.3); color:var(--blockquote-fg);
}

[data-custom-id='agent-bubble'] ul,
[data-custom-id='agent-bubble'] ol { margin:0.35rem 0; padding-left:1.35rem; }
[data-custom-id='agent-bubble'] li { margin-bottom:0.15rem; color:inherit; }

[data-custom-id='agent-bubble'] hr { margin:0.75rem 0; border:none; border-top:1px solid var(--hr-border); }

[data-custom-id='agent-bubble'] table { width:100%; margin:0.5rem 0; border-collapse:collapse; font-size:0.875rem; }
[data-custom-id='agent-bubble'] th,
[data-custom-id='agent-bubble'] td { padding:0.4rem 0.65rem; text-align:left; border:1px solid var(--table-border); background:transparent; }
[data-custom-id='agent-bubble'] th { font-weight:600; background-color:var(--table-header-bg); }

[data-custom-id='agent-bubble'] strong { font-weight:600; color:inherit; }
[data-custom-id='agent-bubble'] img { max-width:100%; border-radius:0.5rem; margin:0.5rem 0; }

[data-custom-id='agent-bubble'] .table { overflow-x:auto; }

/* ===== 组件暗黑模式覆盖（Vue scoped 的 :global(html.dark) 编译有 bug, 移到非 scoped 块） ===== */
html.dark .cf-bubble--assistant {
  color: rgb(231 229 228 / 1);
  background:
    linear-gradient(180deg, rgb(33 30 28 / 0.94), rgb(22 20 19 / 0.9));
  box-shadow:
    inset 0 0 0 1px rgb(255 255 255 / 0.08),
    inset 0 1px 0 0 rgb(255 255 255 / 0.06),
    0 14px 28px -16px rgba(0, 0, 0, 0.55);
}
html.dark .cf-bubble--assistant::before {
  background:
    linear-gradient(135deg, rgb(var(--cf-color-primary-500-rgb) / 0.11), transparent 36%),
    linear-gradient(180deg, rgb(255 255 255 / 0.05), transparent 42%);
}

html.dark .cf-action {
  color: rgb(120 113 108 / 1);
}
html.dark .cf-action:hover {
  color: rgb(231 229 228 / 1);
  background-color: rgb(255 255 255 / 0.06);
}

html.dark .cf-edit-trigger {
  color: rgb(120 113 108 / 1);
}
html.dark .cf-edit-trigger:hover {
  color: rgb(231 229 228 / 1);
  background-color: rgb(255 255 255 / 0.06);
}

html.dark .cf-edit-btn--ghost {
  color: rgb(168 162 158 / 1);
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 0.1);
}
html.dark .cf-edit-btn--ghost:hover {
  background-color: rgb(255 255 255 / 0.06);
  color: rgb(231 229 228 / 1);
}
</style>
