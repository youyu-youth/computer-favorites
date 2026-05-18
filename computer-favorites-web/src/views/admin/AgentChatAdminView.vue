<template>
  <div
    class="cf-chat-shell relative flex h-full flex-col overflow-hidden bg-[#000000] text-stone-200"
  >
    <header
      class="cf-chat-topbar z-10 flex shrink-0 items-center gap-3 border-b border-white/10 bg-[#050505] px-4 py-3 md:px-6"
    >
      <button
        type="button"
        class="grid h-8 w-8 cursor-pointer place-items-center rounded-lg text-stone-400 transition-colors hover:bg-white/[0.06] hover:text-stone-100 md:hidden"
        aria-label="切换会话列表"
        @click="$emit('toggleSidebar')"
      >
        <Menu :size="18" :stroke-width="1.75" />
      </button>

      <div class="flex min-w-0 flex-1 items-center gap-2">
        <span
          aria-hidden="true"
          class="h-1.5 w-1.5 shrink-0 rounded-full transition-colors duration-300"
          :class="connectionDotClass"
        />
        <h1
          class="m-0 flex min-w-0 items-center gap-2 truncate text-[15px] font-semibold text-stone-100"
        >
          <span class="truncate">{{ currentTitle }}</span>
        </h1>
        <span
          class="hidden items-center text-[10px] font-medium tracking-[0.08em] text-stone-600 sm:inline-flex"
        >
          {{ connectionLabel }}
        </span>
      </div>
    </header>

    <div
      ref="messageListRef"
      class="cf-chat-messages cf-scroll relative z-0 flex-1 overflow-y-auto px-4 py-6 sm:px-8"
    >
      <div v-if="store.messages.length === 0" class="h-full">
        <AgentWelcomeScreen @quick-prompt="handleQuickPrompt" />
      </div>

      <div v-else class="mx-auto max-w-4xl space-y-8">
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

        <div
          v-if="store.connectionState === 'connecting'"
          class="cf-connecting flex items-start gap-3 sm:gap-4"
        >
          <span
            class="grid h-8 w-8 shrink-0 place-items-center rounded-full border border-primary-500/50 bg-primary-500/[0.12] text-primary-400 sm:h-9 sm:w-9"
          >
            <Sparkles :size="16" :stroke-width="2" />
          </span>
          <div class="flex flex-col gap-2 pt-2">
            <span class="cf-skel-line h-2.5 w-[180px] rounded-full" />
            <span class="cf-skel-line h-2.5 w-[120px] rounded-full" style="--cf-i: 1" />
          </div>
        </div>

        <div
          v-if="store.connectionState === 'error'"
          class="cf-error relative flex items-start gap-3 rounded-xl border border-rose-500/15 bg-rose-500/[0.08] px-4 py-3"
        >
          <span
            class="absolute bottom-3 left-0 top-3 w-[3px] rounded-r-full bg-rose-500"
            aria-hidden="true"
          />
          <AlertTriangle class="mt-0.5 shrink-0 text-rose-500" :size="15" :stroke-width="1.75" />
          <div class="min-w-0 flex-1">
            <p class="m-0 text-[13px] leading-relaxed text-rose-300">
              {{ store.errorMessage || '连接出错，请重试' }}
            </p>
            <button
              type="button"
              class="mt-1 inline-flex cursor-pointer items-center gap-1 text-[11px] font-medium text-rose-400 transition-colors hover:text-rose-300"
              @click="clearError"
            >
              知道了
            </button>
          </div>
        </div>

        <div
          v-if="!store.hasQuota && store.quotaRemaining === 0"
          class="cf-quota-end relative flex items-center gap-2 rounded-xl border border-primary-500/15 bg-primary-500/[0.08] px-4 py-3"
        >
          <span
            class="absolute bottom-3 left-0 top-3 w-[3px] rounded-r-full bg-primary-500"
            aria-hidden="true"
          />
          <Sparkles class="shrink-0 text-primary-400" :size="15" :stroke-width="1.75" />
          <p class="m-0 text-[13px] text-stone-300">今日对话配额已用完，请明天再来</p>
        </div>
      </div>
    </div>

    <div class="shrink-0 bg-[#000000]">
      <AgentInputArea @send="handleSend" />
    </div>
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

defineOptions({ name: 'AgentChatAdminView' })

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

/** 状态指示点配色 */
const connectionDotClass = computed(() => {
  switch (store.connectionState) {
    case 'streaming':
    case 'connecting':
      return 'bg-primary-500 cf-state-dot--pulse'
    case 'error':
      return 'bg-rose-500'
    default:
      return 'bg-stone-600'
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
.cf-chat-topbar {
  box-shadow: inset 0 -1px 0 0 rgb(255 255 255 / 0.05);
}

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

.cf-skel-line {
  background-color: rgb(255 255 255 / 0.08);
  animation: cf-skel-pulse 1.4s ease-in-out infinite;
  animation-delay: calc(var(--cf-i, 0) * 120ms);
}
@keyframes cf-skel-pulse {
  0%,
  100% {
    opacity: 0.45;
  }
  50% {
    opacity: 1;
  }
}

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
