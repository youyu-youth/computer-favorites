// src/api/agent.ts
import { getJson, postJson, putJson, deleteJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type {
  AgentSession,
  AgentQuota,
  AgentChatRequest,
  AgentSkill,
} from '@/types/agent'

const BASE = '/api/agent/user'
const PUBLIC_BASE = '/api/agent'

/** 获取会话列表 */
export async function getSessions(keyword?: string): Promise<AgentSession[]> {
  const params = keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''
  const res = await getJson<ApiResult<AgentSession[]>>(`${BASE}/sessions${params}`)
  if (res.code !== 200) throw new Error(res.msg || '获取会话列表失败')
  return res.data ?? []
}

/** 获取会话消息历史 */
export async function getSessionMessages(sessionId: number): Promise<{ role: string; content: string }[]> {
  const res = await getJson<ApiResult<{ role: string; content: string }[]>>(`${BASE}/sessions/${sessionId}/messages`)
  if (res.code !== 200) throw new Error(res.msg || '获取消息失败')
  return res.data ?? []
}

/** 重命名会话 */
export async function renameSession(id: number, title: string): Promise<void> {
  const res = await putJson<ApiResult<null>>(`${BASE}/sessions/${id}`, { title })
  if (res.code !== 200) throw new Error(res.msg || '重命名失败')
}

/** 删除会话 */
export async function deleteSession(id: number): Promise<void> {
  const res = await deleteJson<ApiResult<null>>(`${BASE}/sessions/${id}`)
  if (res.code !== 200) throw new Error(res.msg || '删除失败')
}

/** 切换置顶 */
export async function togglePin(id: number, pinned: boolean): Promise<void> {
  const res = await putJson<ApiResult<null>>(`${BASE}/sessions/${id}/pin`, { pinned })
  if (res.code !== 200) throw new Error(res.msg || '操作失败')
}

/** 查询配额 */
export async function getQuota(): Promise<AgentQuota> {
  const res = await getJson<ApiResult<AgentQuota>>(`${BASE}/quota`)
  if (res.code !== 200) throw new Error(res.msg || '获取配额失败')
  if (!res.data) throw new Error('配额信息为空')
  return res.data
}

/** 获取可用技能列表 */
export async function getSkills(): Promise<AgentSkill[]> {
  const res = await getJson<ApiResult<AgentSkill[]>>(`${BASE}/skills`)
  if (res.code !== 200) throw new Error(res.msg || '获取技能列表失败')
  return res.data ?? []
}

/** 确认执行计划 */
export async function confirmPlan(planId: string): Promise<void> {
  const res = await postJson<ApiResult<null>>(`${PUBLIC_BASE}/plan/confirm`, { planId })
  if (res.code !== 200) throw new Error(res.msg || '确认失败')
}

/** 拒绝执行计划 */
export async function rejectPlan(planId: string): Promise<void> {
  const res = await postJson<ApiResult<null>>(`${PUBLIC_BASE}/plan/reject`, { planId })
  if (res.code !== 200) throw new Error(res.msg || '拒绝失败')
}

/**
 * SSE 流式对话请求
 * 返回 fetch Response 对象和 abort 方法，由调用方直接从 response.body 读取流
 */
export async function chatStream(
  request: AgentChatRequest,
): Promise<{ response: Response; abort: () => void }> {
  const token = localStorage.getItem('accessToken')
  const tokenName = localStorage.getItem('tokenName') || 'satoken'
  const controller = new AbortController()

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  if (token) {
    headers[tokenName] = token
  }

  const url = `${BASE}/chat`
  console.log('[AgentChat API] 发起请求:', url, JSON.stringify(request))

  let response: Response
  try {
    response = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(request),
      signal: controller.signal,
    })
  } catch (e: any) {
    console.error('[AgentChat API] fetch 失败:', e.name, e.message)
    throw e
  }

  console.log('[AgentChat API] 响应状态:', response.status, response.statusText)

  if (!response.ok) {
    let errorBody = ''
    try { errorBody = await response.text() } catch { /* ignore */ }
    console.error('[AgentChat API] 响应错误:', response.status, errorBody)
    throw new Error(errorBody || `HTTP ${response.status}: ${response.statusText}`)
  }

  if (!response.body) {
    throw new Error('浏览器不支持 ReadableStream')
  }

  console.log('[AgentChat API] 开始读取 SSE 流')
  return { response, abort: () => controller.abort() }
}
