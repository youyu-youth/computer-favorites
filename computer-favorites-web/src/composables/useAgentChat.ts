// src/composables/useAgentChat.ts
import { useAgentChatStore } from '@/stores/agentChat'
import type { ThinkingStep } from '@/types/agent'
import * as agentApi from '@/api/agent'

export function useAgentChat() {
  const store = useAgentChatStore()
  let abortController: { abort: () => void } | null = null

  /**
   * 分发 SSE 事件到 store
   */
  function handleSSEEvent(
    eventName: string,
    data: Record<string, any>,
    currentAssistantMsgId: string | null,
  ): { currentAssistantMsgId: string | null; shouldStop: boolean } {
    let msgId = currentAssistantMsgId
    let shouldStop = false

    switch (eventName) {
      case 'heartbeat':
        break

      case 'thinking': {
        const step: ThinkingStep = {
          type: 'thinking',
          step: data.step as number,
          timestamp: Date.now(),
        }
        store.addThinkingStep(step)
        break
      }

      case 'message':
        if (!msgId) {
          msgId = store.createAssistantMessage()
        }
        store.appendAssistantContent(data.delta as string)
        break

      case 'tool_call': {
        const step: ThinkingStep = {
          type: 'tool_call',
          toolName: data.toolName as string,
          args: typeof data.args === 'string' ? data.args : JSON.stringify(data.args),
          timestamp: Date.now(),
        }
        store.addThinkingStep(step)
        break
      }

      case 'tool_result': {
        const step: ThinkingStep = {
          type: 'tool_result',
          toolName: data.toolName as string,
          result: typeof data.result === 'string' ? data.result : JSON.stringify(data.result),
          timestamp: Date.now(),
        }
        store.addThinkingStep(step)
        break
      }

      case 'plan':
        store.insertPlan({
          planId: data.planId as string,
          summary: data.summary as string,
          riskLevel: (data.risk as 'low' | 'medium' | 'high') || 'medium',
          status: 'wait_confirm',
          expireTime: data.expireTime as string | undefined,
        })
        break

      case 'done':
        store.connectionState = 'done'
        shouldStop = true
        if (data.sessionId) {
          store.currentSessionId = data.sessionId as number
        }
        break

      case 'error':
        store.connectionState = 'error'
        store.errorMessage = (data.msg as string) || '对话出错'
        throw new Error(store.errorMessage)

      case 'quota_exceeded':
        store.connectionState = 'error'
        store.errorMessage = `今日配额已用完（${data.limit ?? '?'}条）`
        store.quota = {
          dailyMessageLimit: data.limit as number,
          usedMessageCount: data.used as number,
          remainingMessages: 0,
          dailyTokenLimit: store.quota?.dailyTokenLimit ?? 0,
          usedTokens: store.quota?.usedTokens ?? 0,
        }
        shouldStop = true
        break
    }

    return { currentAssistantMsgId: msgId, shouldStop }
  }

  /**
   * 发送消息并建立 SSE 连接
   * 读取流，解析 SSE 事件行，并分发到 store
   */
  async function sendMessage(message: string): Promise<void> {
    if (!message.trim()) return
    if (!store.hasQuota) return

    store.connectionState = 'connecting'
    store.errorMessage = null
    store.addUserMessage(message)

    try {
      const { stream, abort } = agentApi.chatStream({
        conversationId: store.currentConversationId || undefined,
        message,
        skillCode: store.currentSkillCode,
      })
      abortController = { abort }

      const reader = stream.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      let currentEventName = ''
      let assistantMsgId: string | null = null

      store.connectionState = 'streaming'

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const rawLine of lines) {
          const line = rawLine.trim()

          // 解析 "event: <name>" 行
          if (line.startsWith('event:')) {
            currentEventName = line.slice(6).trim()
            continue
          }

          // 解析 "data: <json>" 行
          if (line.startsWith('data:')) {
            const dataStr = line.slice(5).trim()
            if (!dataStr) continue

            try {
              const data = JSON.parse(dataStr)
              const result = handleSSEEvent(currentEventName, data, assistantMsgId)
              assistantMsgId = result.currentAssistantMsgId
              if (result.shouldStop) {
                reader.cancel()
                return
              }
            } catch (e) {
              store.connectionState = 'error'
              throw e
            }
          }
        }
      }

      if (store.connectionState === 'streaming') {
        store.connectionState = 'done'
      }
    } catch (e: any) {
      if (e.name === 'AbortError') {
        store.connectionState = 'idle'
        return
      }
      store.connectionState = 'error'
      store.errorMessage = e.message || '未知错误'
      throw e
    } finally {
      store.loadQuota()
      store.loadSessions()
    }
  }

  /** 取消当前流式对话 */
  function cancelChat() {
    if (abortController) {
      abortController.abort()
      abortController = null
    }
    store.connectionState = 'idle'
  }

  /** 重新生成最后一条 AI 回复 */
  async function regenerate(message: string) {
    const msgs = store.messages
    const lastMsg = msgs[msgs.length - 1]
    if (lastMsg && lastMsg.role === 'assistant') {
      msgs.pop()
    }
    await sendMessage(message)
  }

  /** 编辑用户消息并重新发送，移除该消息之后的所有消息 */
  async function editAndResend(msgId: string, newContent: string) {
    const idx = store.messages.findIndex((m) => m.id === msgId)
    if (idx === -1) return

    store.messages.splice(idx)
    await sendMessage(newContent)
  }

  function isStreaming(): boolean {
    return store.connectionState === 'streaming'
  }

  return {
    sendMessage,
    cancelChat,
    regenerate,
    editAndResend,
    isStreaming,
  }
}
