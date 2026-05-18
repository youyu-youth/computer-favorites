<template>
  <div class="cf-chat-shell relative flex h-full flex-col overflow-hidden bg-gradient-to-b from-stone-50 to-stone-100 dark:from-stone-950 dark:to-[#141110]">
    <!-- 装饰：背景柔光斑（仅视觉、pointer-events-none） -->
    <div class="cf-chat-aura pointer-events-none absolute -left-32 top-20 h-[460px] w-[460px] rounded-full" aria-hidden="true" />

    <!-- 顶部栏：liquid glass sticky -->
    <header class="cf-chat-topbar relative z-10 flex items-center gap-3 px-4 md:px-6 py-3 shrink-0 bg-white/60 dark:bg-stone-950/55 backdrop-blur-[18px] backdrop-saturate-[1.6]">
      <!-- 移动端侧边栏切换 -->
      <button
        type="button"
        class="md:hidden grid h-8 w-8 place-items-center rounded-lg text-stone-500 hover:text-stone-800 dark:text-stone-400 dark:hover:text-stone-100 hover:bg-stone-900/[0.04] dark:hover:bg-white/[0.06] transition-colors cursor-pointer"
        aria-label="切换会话列表"
        @click="$emit('toggleSidebar')"
      >
        <Menu :size="16" :stroke-width="1.75" />
      </button>

      <!-- 标题 + 状态 indicator -->
      <div class="flex items-center gap-2 min-w-0 flex-1">
        <span
          aria-hidden="true"
          class="h-1.5 w-1.5 shrink-0 rounded-full transition-colors duration-300"
          :class="connectionDotClass"
        />
        <h2 class="text-[13px] font-semibold tracking-tight text-stone-800 dark:text-stone-100 truncate">
          {{ currentTitle }}
        </h2>
        <span class="hidden sm:inline-flex items-center text-[10px] font-medium tracking-[0.08em] uppercase text-stone-400 dark:text-stone-500">
          {{ connectionLabel }}
        </span>
      </div>
    </header>

    <!-- 消息区域 -->
    <div
      ref="messageListRef"
      class="cf-chat-messages relative z-0 flex-1 overflow-y-auto cf-scroll px-4 md:px-8 py-6 space-y-6"
    >
      <!-- 空状态：占据整个滚动区域 -->
      <div v-if="store.messages.length === 0" class="h-full">
        <AgentWelcomeScreen @quick-prompt="handleQuickPrompt" />
      </div>

      <!-- 消息列表 -->
      <template v-for="msg in store.messages" :key="msg.id">
        <AgentPlanCard
          v-if="msg.role === 'plan' && msg.plan"
          :plan="msg.plan"
          @confirm="handlePlanConfirm"
          @reject="handlePlanReject"
        />
        <AgentMessageBubble
          v-else
          :msg="msg"
          :is-streaming="store.connectionState === 'streaming'
            && msg.role === 'assistant'
            && msg === store.messages[store.messages.length - 1]"
          @regenerate="handleRegenerate"
          @edit-resend="handleEditResend"
        />
      </template>

      <!-- 连接中：shimmer skeleton 段，比弹跳点更克制 -->
      <div v-if="store.connectionState === 'connecting'" class="cf-connecting flex items-start gap-3 pl-1">
        <span class="cf-ai-avatar grid h-8 w-8 shrink-0 place-items-center rounded-full text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.32),0_8px_18px_-8px_rgb(var(--cf-color-primary-500-rgb)/0.5)]">
          <Sparkles :size="14" :stroke-width="2" />
        </span>
        <div class="flex flex-col gap-1.5 pt-1">
          <span class="cf-skel-line h-2.5 w-[180px] rounded-full" />
          <span class="cf-skel-line h-2.5 w-[120px] rounded-full" style="--cf-i: 1" />
        </div>
      </div>

      <!-- 错误状态：左侧 rose rail 内联横幅 -->
      <div
        v-if="store.connectionState === 'error'"
        class="cf-error relative flex items-start gap-3 rounded-xl bg-rose-500/[0.06] dark:bg-rose-500/[0.1] px-4 py-3 ring-1 ring-inset ring-rose-500/15"
      >
        <span class="absolute left-0 top-3 bottom-3 w-[3px] rounded-r-full bg-rose-500" aria-hidden="true" />
        <AlertTriangle class="mt-0.5 shrink-0 text-rose-500" :size="14" :stroke-width="1.75" />
        <div class="flex-1 min-w-0">
          <p class="text-[13px] text-rose-700 dark:text-rose-300 leading-relaxed">
            {{ store.errorMessage || '连接出错，请重试' }}
          </p>
          <button
            type="button"
            class="mt-1 inline-flex items-center gap-1 text-[11px] font-medium text-rose-600 dark:text-rose-400 hover:text-rose-700 dark:hover:text-rose-300 transition-colors cursor-pointer"
            @click="store.connectionState = 'idle'; store.errorMessage = null"
          >
            知道了
          </button>
        </div>
      </div>

      <!-- 配额用尽提示 -->
      <div
        v-if="!store.hasQuota && store.quotaRemaining === 0"
        class="cf-quota-end relative flex items-center gap-2 rounded-xl bg-primary-500/[0.06] px-4 py-3 ring-1 ring-inset ring-primary-500/15"
      >
        <span class="absolute left-0 top-3 bottom-3 w-[3px] rounded-r-full bg-primary-500" aria-hidden="true" />
        <Sparkles class="shrink-0 text-primary-500" :size="14" :stroke-width="1.75" />
        <p class="text-[13px] text-stone-700 dark:text-stone-200">今日对话配额已用完，请明天再来</p>
      </div>
    </div>

    <!-- 输入区域 -->
    <AgentInputArea @send="handleSend" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { Menu, Sparkles, AlertTriangle } from 'lucide-vue-next'
import { useAgentChatStore } from '@/stores/agentChat'
import { useAgentChat } from '@/composables/useAgentChat'
import AgentWelcomeScreen from '@/components/agent/AgentWelcomeScreen.vue'
import AgentMessageBubble from '@/components/agent/AgentMessageBubble.vue'
import AgentPlanCard from '@/components/agent/AgentPlanCard.vue'
import AgentInputArea from '@/components/agent/AgentInputArea.vue'

defineOptions({ name: 'AgentChatUserView' })

defineEmits<{
  toggleSidebar: []
}>()

const store = useAgentChatStore()
const { sendMessage, regenerate, editAndResend } = useAgentChat()

const messageListRef = ref<HTMLElement | null>(null)

/** 当前会话标题 */
const currentTitle = computed(() => {
  const session = store.sessions.find((s) => s.id === store.currentSessionId)
  return session?.title || '新对话'
})

/** 状态指示点配色：streaming/connecting 主色脉动；error 玫红；其他 stone 静态 */
const connectionDotClass = computed(() => {
  switch (store.connectionState) {
    case 'streaming':
    case 'connecting':
      return 'bg-primary-500 cf-state-dot--pulse'
    case 'error':
      return 'bg-rose-500'
    default:
      return 'bg-stone-300 dark:bg-stone-600'
  }
})

/** 状态文案 */
const connectionLabel = computed(() => {
  switch (store.connectionState) {
    case 'streaming': return '生成中'
    case 'connecting': return '连接中'
    case 'error': return '已断开'
    default: return '在线'
  }
})

/** 新消息到达或内容流式更新时自动滚动到底部 */
watch(
  () => store.messages.length,
  () => scrollToBottom(),
)
watch(
  () => store.messages[store.messages.length - 1]?.content,
  () => scrollToBottom(),
)

/** 滚动到消息列表底部 */
function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

/** 发送消息 */
async function handleSend(message: string, _files: File[]) {
  let fullMessage = message
  if (_files.length > 0) {
    const fileNames = _files.map((f) => f.name).join(', ')
    fullMessage = `[上传文件: ${fileNames}]\n${message}`
  }
  try {
    await sendMessage(fullMessage)
    await store.loadSessions()
  } catch (e) {
    console.error('发送消息失败', e)
  }
}

/** 重新生成最后一条 AI 回复 */
async function handleRegenerate() {
  const lastUserMsg = [...store.messages].reverse().find((m) => m.role === 'user')
  if (lastUserMsg) {
    await regenerate(lastUserMsg.content)
  }
}

/** 编辑用户消息并重新发送 */
async function handleEditResend(msgId: string, newContent: string) {
  await editAndResend(msgId, newContent)
}

/** 快捷提示 */
function handleQuickPrompt(prompt: string) {
  handleSend(prompt, [])
}

/**
 * 确认执行计划
 * TODO: 待后端 API 就绪后替换为真实接口调用
 */
async function handlePlanConfirm(planId: string) {
  console.log('confirmPlan:', planId)
}

/**
 * 拒绝执行计划
 * TODO: 待后端 API 就绪后替换为真实接口调用
 */
async function handlePlanReject(planId: string) {
  console.log('rejectPlan:', planId)
}
</script>

<style scoped>
/* 顶部 liquid glass：仅保留 inset hairline，背景由 Tailwind dark: 工具类驱动 */
.cf-chat-topbar {
  box-shadow:
    inset 0 -1px 0 0 rgb(15 23 42 / 0.06),
    inset 0 1px 0 0 rgb(255 255 255 / 0.55);
}
:global(html.dark) .cf-chat-topbar {
  box-shadow:
    inset 0 -1px 0 0 rgb(255 255 255 / 0.05),
    inset 0 1px 0 0 rgb(255 255 255 / 0.04);
}

/* 装饰光斑：基于主色 token，单一不透明度在浅 / 深底色上都自然 */
.cf-chat-aura {
  background: radial-gradient(closest-side, rgb(var(--cf-color-primary-500-rgb) / 0.16), transparent 70%);
  filter: blur(2px);
  z-index: 0;
}

/* 自定义滚动条 */
.cf-scroll {
  scrollbar-width: thin;
  scrollbar-color: rgb(120 113 108 / 0.3) transparent;
}
.cf-scroll::-webkit-scrollbar {
  width: 6px;
}
.cf-scroll::-webkit-scrollbar-thumb {
  background-color: rgb(120 113 108 / 0.3);
  border-radius: 999px;
}
.cf-scroll::-webkit-scrollbar-thumb:hover {
  background-color: rgb(120 113 108 / 0.5);
}

/* 状态点脉动 */
.cf-state-dot--pulse {
  animation: cf-state-pulse 1.6s cubic-bezier(0.16, 1, 0.3, 1) infinite;
  box-shadow: 0 0 0 0 rgb(var(--cf-color-primary-500-rgb) / 0.55);
}
@keyframes cf-state-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgb(var(--cf-color-primary-500-rgb) / 0.55); }
  60% { box-shadow: 0 0 0 6px rgb(var(--cf-color-primary-500-rgb) / 0); }
}

/* 连接中 skeleton 行：使用 stone-500 半透明，浅 / 深底色都自然 */
.cf-skel-line {
  position: relative;
  overflow: hidden;
  background: linear-gradient(
    90deg,
    rgb(120 113 108 / 0.14) 0%,
    rgb(120 113 108 / 0.26) 50%,
    rgb(120 113 108 / 0.14) 100%
  );
  background-size: 200% 100%;
  animation: cf-shimmer 1.6s linear infinite;
  animation-delay: calc(var(--cf-i, 0) * 120ms);
}
@keyframes cf-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* connecting / error / quota-end 入场动画 */
.cf-connecting,
.cf-error,
.cf-quota-end {
  animation: cf-msg-in 360ms cubic-bezier(0.16, 1, 0.3, 1) both;
}
@keyframes cf-msg-in {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}

/* AI 头像渐变（与 MessageBubble 共用视觉，子作用域内复制定义） */
.cf-ai-avatar {
  background: linear-gradient(
    135deg,
    rgb(var(--cf-color-primary-400-rgb) / 1) 0%,
    rgb(var(--cf-color-primary-500-rgb) / 1) 50%,
    rgb(var(--cf-color-primary-600-rgb) / 1) 100%
  );
}

@media (prefers-reduced-motion: reduce) {
  .cf-state-dot--pulse,
  .cf-skel-line,
  .cf-connecting,
  .cf-error,
  .cf-quota-end {
    animation: none;
    opacity: 1;
    transform: none;
  }
}
</style>
