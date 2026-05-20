<template>
  <div
    class="cf-chat-shell relative flex h-full min-h-0 flex-col overflow-hidden bg-stone-50 text-stone-900 dark:bg-black dark:text-stone-200"
  >
    <!-- 顶部栏：liquid glass sticky -->
    <header
      class="cf-chat-topbar relative z-10 flex shrink-0 items-center gap-3 border-b border-stone-200/80 bg-white/80 px-4 py-3 backdrop-blur-xl md:px-6 dark:border-white/10 dark:bg-black"
    >
      <!-- 移动端侧边栏切换 -->
      <button
        type="button"
        class="grid h-8 w-8 cursor-pointer place-items-center rounded-lg text-stone-500 transition-colors hover:bg-stone-900/[0.05] hover:text-stone-900 md:hidden dark:text-stone-400 dark:hover:bg-white/[0.06] dark:hover:text-stone-100"
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
        <h2 class="truncate text-[13px] font-semibold tracking-tight text-stone-900 dark:text-stone-100">
          {{ currentTitle }}
        </h2>
        <span
          class="hidden sm:inline-flex items-center text-[10px] font-medium tracking-[0.08em] uppercase text-stone-400 dark:text-stone-500"
        >
          {{ connectionLabel }}
        </span>
      </div>
    </header>

    <!-- 消息区域 -->
    <div
      ref="messageListRef"
      class="cf-chat-messages cf-scroll relative z-0 min-h-0 flex-1 overflow-y-auto"
    >
      <!-- 加载中遮罩：覆盖在内容上方，阻断交互的同时提供视觉反馈 -->
      <Transition name="cf-overlay-leave">
        <div
          v-if="store.messagesLoading"
          class="cf-loading-overlay absolute inset-0 z-10 flex items-start justify-center pt-20 bg-stone-50/60 dark:bg-black/50 backdrop-blur-[1px]"
        >
        <div class="flex flex-col items-center gap-2.5">
          <span
            class="cf-loading-spinner h-5 w-5 rounded-full border-2 border-stone-300 dark:border-stone-600 border-t-primary-500 animate-spin"
          />
          <span class="text-[11px] text-stone-400 dark:text-stone-500 font-medium tracking-wide">加载中</span>
        </div>
      </div>
      </Transition>

      <!-- 内容区：加载中时降低透明度，但始终保持渲染避免 DOM 交换闪烁 -->
      <div class="cf-session-inner min-h-full transition-opacity duration-200 ease-out" :class="store.messagesLoading ? 'opacity-0' : 'opacity-100'">
        <!-- 空状态 -->
        <div v-if="store.messages.length === 0 && !store.messagesLoading" class="min-h-full w-full">
          <AgentWelcomeScreen @quick-prompt="handleQuickPrompt" />
        </div>

        <!-- 消息列表 -->
        <div v-else-if="store.messages.length > 0" class="space-y-6 px-4 py-6 md:px-8">
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
              :is-streaming="
                store.connectionState === 'streaming' &&
                msg.role === 'assistant' &&
                msg === store.messages[store.messages.length - 1]
              "
              @regenerate="handleRegenerate"
              @edit-resend="handleEditResend"
            />
          </template>

          <!-- 连接中 -->
          <div
            v-if="store.connectionState === 'connecting'"
            class="cf-connecting flex items-start gap-3 pl-1"
          >
            <span
              class="cf-ai-avatar grid h-8 w-8 shrink-0 place-items-center rounded-full text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.32),0_8px_18px_-8px_rgb(var(--cf-color-primary-500-rgb)/0.5)]"
            >
              <Sparkles :size="14" :stroke-width="2" />
            </span>
            <div class="flex flex-col gap-1.5 pt-1">
              <span class="cf-skel-line h-2.5 w-[180px] rounded-full" />
              <span class="cf-skel-line h-2.5 w-[120px] rounded-full" style="--cf-i: 1" />
            </div>
          </div>

          <!-- 错误状态 -->
          <div
            v-if="store.connectionState === 'error'"
            class="cf-error relative flex items-start gap-3 rounded-xl bg-rose-500/[0.06] dark:bg-rose-500/[0.1] px-4 py-3 ring-1 ring-inset ring-rose-500/15"
          >
            <span
              class="absolute left-0 top-3 bottom-3 w-[3px] rounded-r-full bg-rose-500"
              aria-hidden="true"
            />
            <AlertTriangle class="mt-0.5 shrink-0 text-rose-500" :size="14" :stroke-width="1.75" />
            <div class="flex-1 min-w-0">
              <p class="text-[13px] text-rose-700 dark:text-rose-300 leading-relaxed">
                {{ store.errorMessage || '连接出错，请重试' }}
              </p>
              <button
                type="button"
                class="mt-1 inline-flex items-center gap-1 text-[11px] font-medium text-rose-600 dark:text-rose-400 hover:text-rose-700 dark:hover:text-rose-300 transition-colors cursor-pointer"
                @click="clearError"
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
            <span
              class="absolute left-0 top-3 bottom-3 w-[3px] rounded-r-full bg-primary-500"
              aria-hidden="true"
            />
            <Sparkles class="shrink-0 text-primary-500" :size="14" :stroke-width="1.75" />
            <p class="text-[13px] text-stone-700 dark:text-stone-200">今日对话配额已用完，请明天再来</p>
          </div>
        </div>
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
import { confirmPlan, rejectPlan } from '@/api/agent'
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
    case 'streaming':
      return '生成中'
    case 'connecting':
      return '连接中'
    case 'error':
      return '已断开'
    default:
      return '在线'
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
/** 会话切换加载完成后滚动到底部 */
watch(
  () => store.messagesLoading,
  (loading) => {
    if (!loading) scrollToBottom()
  },
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

function clearError() {
  store.connectionState = 'idle'
  store.errorMessage = null
}

async function handlePlanConfirm(planId: string) {
  try {
    await confirmPlan(planId)
    const msg = store.messages.find(m => m.role === 'plan' && m.plan?.planId === planId)
    if (msg?.plan) msg.plan.status = 'approved'
  } catch (e: any) {
    store.errorMessage = e.message || '确认失败'
  }
}

async function handlePlanReject(planId: string) {
  try {
    await rejectPlan(planId)
    const msg = store.messages.find(m => m.role === 'plan' && m.plan?.planId === planId)
    if (msg?.plan) msg.plan.status = 'rejected'
  } catch (e: any) {
    store.errorMessage = e.message || '拒绝失败'
  }
}
</script>

<style scoped>
.cf-chat-topbar {
  box-shadow: inset 0 -1px 0 0 rgb(255 255 255 / 0.05);
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
  0%,
  100% {
    box-shadow: 0 0 0 0 rgb(var(--cf-color-primary-500-rgb) / 0.55);
  }
  60% {
    box-shadow: 0 0 0 6px rgb(var(--cf-color-primary-500-rgb) / 0);
  }
}

/* 会话切换加载遮罩：进入 + 离开均有过渡，与内容淡入重叠形成 crossfade */
.cf-loading-overlay {
  animation: cf-overlay-in 160ms ease-out both;
}
@keyframes cf-overlay-in {
  from { opacity: 0; }
  to { opacity: 1; }
}
.cf-overlay-leave-leave-active {
  transition: opacity 150ms ease-in;
}
.cf-overlay-leave-leave-to {
  opacity: 0;
}

/* 加载中旋转器 */
.cf-loading-spinner {
  animation: cf-spinner-enter 240ms ease-out both, spin 1s linear infinite;
}
@keyframes cf-spinner-enter {
  from { opacity: 0; transform: scale(0.6); }
  to { opacity: 1; transform: scale(1); }
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
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* connecting / error / quota-end 入场动画 */
.cf-connecting,
.cf-error,
.cf-quota-end {
  animation: cf-msg-in 360ms cubic-bezier(0.16, 1, 0.3, 1) both;
}
@keyframes cf-msg-in {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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
  .cf-quota-end,
  .cf-loading-overlay,
  .cf-loading-spinner {
    animation: none;
    opacity: 1;
    transform: none;
  }
  .cf-session-inner,
  .cf-overlay-leave-leave-active {
    transition: none;
  }
}
</style>
