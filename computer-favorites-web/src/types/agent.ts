/** SSE 连接状态 */
export type ConnectionState =
  | 'idle'
  | 'connecting'
  | 'streaming'
  | 'done'
  | 'error'

/** 会话摘要（用于列表展示） */
export interface AgentSession {
  id: number
  conversationId: string
  title: string | null
  skillCode: string
  status: string
  pinned: number
  lastMessageAt: string | null
  createTime: string
}

/** SSE 思考步骤 */
export interface ThinkingStep {
  type: 'thinking' | 'tool_call' | 'tool_result'
  step?: number
  toolName?: string
  args?: string
  result?: string
  timestamp: number
}

/** 单条对话消息 */
export interface AgentMessage {
  id: string
  role: 'user' | 'assistant' | 'plan'
  content: string
  thinkingSteps: ThinkingStep[]
  thinkingCollapsed: boolean
  createdAt: string
  /** 仅 plan 消息使用 */
  plan?: AgentPlan
  /** 点赞/踩 */
  feedback?: 'like' | 'dislike' | null
}

/** 执行计划（需确认的高风险操作） */
export interface AgentPlan {
  planId: string
  summary: string
  riskLevel: 'low' | 'medium' | 'high'
  status: 'wait_confirm' | 'approved' | 'rejected' | 'executed' | 'expired'
  expireTime?: string
}

/** 配额信息 */
export interface AgentQuota {
  dailyMessageLimit: number
  usedMessageCount: number
  remainingMessages: number
  dailyTokenLimit: number
  usedTokens: number
}

/** SSE 对话请求 */
export interface AgentChatRequest {
  conversationId?: string
  message: string
  skillCode: string
}

/** SSE 对话连接响应元信息 */
export interface AgentChatVO {
  conversationId: string
  sessionId: number
  skillName: string
}

export interface AgentSkill {
  skillCode: string
  name: string
}
