// src/api/agent.ts
import { getJson, putJson, deleteJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type {
  AgentSession,
  AgentQuota,
  AgentChatRequest,
} from '@/types/agent'

const BASE = '/api/agent/user'

/** 获取会话列表 */
export async function getSessions(keyword?: string): Promise<AgentSession[]> {
  const params = keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''
  const res = await getJson<ApiResult<AgentSession[]>>(`${BASE}/sessions${params}`)
  if (res.code !== 200) throw new Error(res.msg || '获取会话列表失败')
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

/**
 * SSE 流式对话
 * 使用 fetch + ReadableStream（EventSource 不支持 POST）
 * 直接读取 response.body 流，不做额外包装
 */
export function chatStream(
  request: AgentChatRequest,
): { stream: ReadableStream<Uint8Array>; abort: () => void } {
  const token = localStorage.getItem('accessToken')
  const tokenName = localStorage.getItem('tokenName') || 'satoken'
  const controller = new AbortController()

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  if (token) {
    headers[tokenName] = token
  }

  // 先发起 fetch，拿到 response 后直接返回 response.body
  const fetchPromise = fetch(`${BASE}/chat`, {
    method: 'POST',
    headers,
    body: JSON.stringify(request),
    signal: controller.signal,
  })

  const stream = new ReadableStream<Uint8Array>({
    async start(streamController) {
      try {
        const response = await fetchPromise
        if (!response.ok) {
          // 尝试读取后端返回的错误信息
          let errorBody = ''
          try {
            errorBody = await response.text()
          } catch { /* ignore */ }
          const msg = errorBody || `HTTP ${response.status}: ${response.statusText}`
          streamController.error(new Error(msg))
          return
        }
        if (!response.body) {
          streamController.error(new Error('浏览器不支持 ReadableStream'))
          return
        }
        const reader = response.body.getReader()
        while (true) {
          const { done, value } = await reader.read()
          if (done) {
            streamController.close()
            break
          }
          streamController.enqueue(value)
        }
      } catch (e: any) {
        // AbortError 正常关闭，其他错误上报
        if (e.name === 'AbortError') {
          streamController.close()
        } else {
          streamController.error(e)
        }
      }
    },
  })

  return { stream, abort: () => controller.abort() }
}
