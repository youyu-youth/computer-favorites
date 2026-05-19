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
import { buildAdminAuthHeaders } from '@/utils/http'

const ADMIN_BASE = '/api/admin/agent'

export const useAdminAgentChatStore = defineStore('adminAgentChat', () => {
  const sessions = ref<AgentSession[]>([])
  const sessionsLoading = ref(false)

  const loadSessions = async (keyword?: string) => {
    sessionsLoading.value = true
    try {
      const params = keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''
      const res = await fetch(`${ADMIN_BASE}/sessions${params}`, { headers: buildAdminAuthHeaders() })
      const json = await res.json()
      if (json.code === 200) sessions.value = json.data ?? []
    } finally {
      sessionsLoading.value = false
    }
  }

  const currentSessionId = ref<number | null>(null)
  const currentConversationId = ref<string | null>(null)
  const currentSkillCode = ref<string>('website_audit_assistant')
  const messages = ref<AgentMessage[]>([])
  const connectionState = ref<ConnectionState>('idle')
  const errorMessage = ref<string | null>(null)
  const pendingThinkingSteps = ref<ThinkingStep[]>([])

  const skills = ref<{ code: string; name: string }[]>([
    { code: 'website_audit_assistant', name: '网站审核助手' },
  ])

  const loadSkills = async () => {
    try {
      const res = await fetch(`${ADMIN_BASE}/skills`, { headers: buildAdminAuthHeaders() })
      const json = await res.json()
      if (json.code === 200 && json.data?.length > 0) {
        skills.value = json.data.map((s: any) => ({ code: s.skillCode, name: s.name }))
      }
    } catch { /* keep defaults */ }
  }

  const startNewChat = () => {
    currentSessionId.value = null
    currentConversationId.value = null
    messages.value = []
    pendingThinkingSteps.value = []
    connectionState.value = 'idle'
  }

  const switchToSession = async (session: AgentSession) => {
    currentSessionId.value = session.id
    currentConversationId.value = session.conversationId
    currentSkillCode.value = session.skillCode || 'website_audit_assistant'
    messages.value = []
    pendingThinkingSteps.value = []
    connectionState.value = 'idle'
  }

  const addUserMessage = (content: string): string => {
    const id = `msg-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
    messages.value.push({
      id, role: 'user', content, thinkingSteps: [], thinkingCollapsed: true,
      createdAt: new Date().toISOString(),
    })
    return id
  }

  const createAssistantMessage = (): string => {
    const id = `msg-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
    messages.value.push({
      id, role: 'assistant', content: '', thinkingSteps: [], thinkingCollapsed: true,
      createdAt: new Date().toISOString(),
    })
    pendingThinkingSteps.value = []
    return id
  }

  const appendAssistantContent = (delta: string) => {
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant') lastMsg.content += delta
  }

  const messageCount = computed(() => messages.value.length)

  const quota = ref<AgentQuota | null>(null)
  const quotaRemaining = computed(() => quota.value?.remainingMessages ?? null)
  const hasQuota = computed(() => quota.value ? quota.value.remainingMessages > 0 : true)

  return {
    sessions, sessionsLoading, loadSessions,
    currentSessionId, currentConversationId, currentSkillCode,
    messages, connectionState, errorMessage, pendingThinkingSteps,
    skills, loadSkills, startNewChat, switchToSession,
    addUserMessage, createAssistantMessage, appendAssistantContent, messageCount,
    quota, quotaRemaining, hasQuota,
  }
})
