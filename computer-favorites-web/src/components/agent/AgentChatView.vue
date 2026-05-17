<template>
  <div class="flex flex-col h-full bg-white dark:bg-gray-950">
    <!-- 顶部栏 -->
    <div class="flex items-center gap-3 px-4 py-2.5 border-b border-gray-200 dark:border-gray-700 shrink-0">
      <!-- 移动端侧边栏切换按钮 -->
      <button
        class="md:hidden p-1 text-gray-500 hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer"
        @click="$emit('toggleSidebar')"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
        </svg>
      </button>

      <h2 class="text-sm font-semibold text-gray-800 dark:text-gray-100 truncate flex-1">
        {{ currentTitle }}
      </h2>
    </div>

    <!-- 消息区域 -->
    <div ref="messageListRef" class="flex-1 overflow-y-auto px-4 py-6 space-y-6">
      <!-- 空状态 -->
      <AgentWelcomeScreen
        v-if="store.messages.length === 0"
        @quick-prompt="handleQuickPrompt"
      />

      <!-- 消息列表 -->
      <template v-for="msg in store.messages" :key="msg.id">
        <!-- 计划卡片 -->
        <AgentPlanCard
          v-if="msg.role === 'plan' && msg.plan"
          :plan="msg.plan"
          @confirm="handlePlanConfirm"
          @reject="handlePlanReject"
        />
        <!-- 普通消息 -->
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

      <!-- 连接中指示器 -->
      <div v-if="store.connectionState === 'connecting'" class="flex items-center gap-2 text-sm text-gray-400 pl-11">
        <div class="w-2 h-2 bg-amber-500 rounded-full animate-bounce" style="animation-delay: 0ms" />
        <div class="w-2 h-2 bg-amber-500 rounded-full animate-bounce" style="animation-delay: 150ms" />
        <div class="w-2 h-2 bg-amber-500 rounded-full animate-bounce" style="animation-delay: 300ms" />
      </div>

      <!-- 错误状态 -->
      <div v-if="store.connectionState === 'error'" class="text-center py-4 px-4">
        <p class="text-sm text-red-500">{{ store.errorMessage || '连接出错，请重试' }}</p>
        <button
          class="mt-2 text-xs text-amber-500 hover:text-amber-600 cursor-pointer underline"
          @click="store.connectionState = 'idle'; store.errorMessage = null"
        >
          关闭
        </button>
      </div>

      <!-- 配额用尽 -->
      <div v-if="!store.hasQuota && store.quotaRemaining === 0" class="text-center text-sm text-amber-500 py-4">
        今日对话配额已用完，请明天再来
      </div>
    </div>

    <!-- 输入区域 -->
    <AgentInputArea @send="handleSend" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useAgentChatStore } from '@/stores/agentChat'
import { useAgentChat } from '@/composables/useAgentChat'
import AgentWelcomeScreen from '@/components/agent/AgentWelcomeScreen.vue'
import AgentMessageBubble from '@/components/agent/AgentMessageBubble.vue'
import AgentPlanCard from '@/components/agent/AgentPlanCard.vue'
import AgentInputArea from '@/components/agent/AgentInputArea.vue'

defineOptions({ name: 'AgentChatView' })

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
