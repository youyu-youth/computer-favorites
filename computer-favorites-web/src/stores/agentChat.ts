import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  AgentSession,
  AgentMessage,
  AgentQuota,
  AgentPlan,
  ThinkingStep,
  ConnectionState,
} from '@/types/agent'
import * as agentApi from '@/api/agent'

export const useAgentChatStore = defineStore('agentChat', () => {
  // === 会话列表 ===
  const sessions = ref<AgentSession[]>([])
  const sessionsLoading = ref(false)

  const loadSessions = async (keyword?: string) => {
    sessionsLoading.value = true
    try {
      sessions.value = await agentApi.getSessions(keyword)
    } finally {
      sessionsLoading.value = false
    }
  }

  const prependSession = (session: AgentSession) => {
    sessions.value.unshift(session)
  }

  const updateSession = (id: number, patch: Partial<AgentSession>) => {
    const idx = sessions.value.findIndex((s) => s.id === id)
    if (idx !== -1) {
      sessions.value[idx] = { ...sessions.value[idx], ...patch }
    }
  }

  const removeSession = (id: number) => {
    sessions.value = sessions.value.filter((s) => s.id !== id)
  }

  // === 当前对话 ===
  const currentSessionId = ref<number | null>(null)
  const currentConversationId = ref<string | null>(null)
  const currentSkillCode = ref<string>('general_assistant')
  const messages = ref<AgentMessage[]>([])
  const connectionState = ref<ConnectionState>('idle')

  const pendingThinkingSteps = ref<ThinkingStep[]>([])

  const startNewChat = () => {
    currentSessionId.value = null
    currentConversationId.value = null
    messages.value = []
    pendingThinkingSteps.value = []
    connectionState.value = 'idle'
  }

  const switchToSession = (session: AgentSession) => {
    currentSessionId.value = session.id
    currentConversationId.value = session.conversationId
    currentSkillCode.value = session.skillCode || 'general_assistant'
    messages.value = []
    pendingThinkingSteps.value = []
    connectionState.value = 'idle'
  }

  const addUserMessage = (content: string): string => {
    const id = `msg-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
    messages.value.push({
      id,
      role: 'user',
      content,
      thinkingSteps: [],
      thinkingCollapsed: true,
      createdAt: new Date().toISOString(),
    })
    return id
  }

  const createAssistantMessage = (): string => {
    const id = `msg-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
    messages.value.push({
      id,
      role: 'assistant',
      content: '',
      thinkingSteps: [],
      thinkingCollapsed: true,
      createdAt: new Date().toISOString(),
    })
    pendingThinkingSteps.value = []
    return id
  }

  const appendAssistantContent = (delta: string) => {
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant') {
      lastMsg.content += delta
    }
  }

  const addThinkingStep = (step: ThinkingStep) => {
    pendingThinkingSteps.value.push(step)
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant') {
      lastMsg.thinkingSteps = [...pendingThinkingSteps.value]
    }
  }

  const toggleThinking = (msgId: string) => {
    const msg = messages.value.find((m) => m.id === msgId)
    if (msg) {
      msg.thinkingCollapsed = !msg.thinkingCollapsed
    }
  }

  const insertPlan = (plan: AgentPlan) => {
    messages.value.push({
      id: `plan-${plan.planId}`,
      role: 'plan',
      content: '',
      thinkingSteps: [],
      thinkingCollapsed: true,
      createdAt: new Date().toISOString(),
      plan,
    })
  }

  const updateMessageContent = (msgId: string, newContent: string) => {
    const msg = messages.value.find((m) => m.id === msgId)
    if (msg) {
      msg.content = newContent
    }
  }

  const setMessageFeedback = (msgId: string, feedback: 'like' | 'dislike' | null) => {
    const msg = messages.value.find((m) => m.id === msgId)
    if (msg) {
      msg.feedback = feedback
    }
  }

  // === 配额 ===
  const quota = ref<AgentQuota | null>(null)

  const loadQuota = async () => {
    try {
      quota.value = await agentApi.getQuota()
    } catch {
      quota.value = null
    }
  }

  // === 技能列表 ===
  const skills = ref<{ code: string; name: string }[]>([
    { code: 'general_assistant', name: '通用助手' },
    { code: 'code_assistant', name: '编程助手' },
    { code: 'website_assistant', name: '网站推荐' },
  ])

  const currentSkillName = computed(() => {
    const s = skills.value.find((s) => s.code === currentSkillCode.value)
    return s?.name ?? '通用助手'
  })

  // === 计算属性 ===
  const quotaRemaining = computed(() => {
    if (!quota.value) return null
    return quota.value.remainingMessages
  })

  const hasQuota = computed(() => {
    if (!quota.value) return true
    return quota.value.remainingMessages > 0
  })

  return {
    sessions,
    sessionsLoading,
    loadSessions,
    prependSession,
    updateSession,
    removeSession,
    currentSessionId,
    currentConversationId,
    currentSkillCode,
    messages,
    connectionState,
    pendingThinkingSteps,
    startNewChat,
    switchToSession,
    addUserMessage,
    createAssistantMessage,
    appendAssistantContent,
    addThinkingStep,
    toggleThinking,
    insertPlan,
    updateMessageContent,
    setMessageFeedback,
    quota,
    loadQuota,
    skills,
    currentSkillName,
    quotaRemaining,
    hasQuota,
  }
})
