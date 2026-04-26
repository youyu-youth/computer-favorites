import { deleteJson, getJson, postJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type { CollectPageResult, CollectStats } from '@/types/collection'

export interface CollectCreatePayload {
  websiteId: number
  folderId?: number
}

export interface CollectPageQuery {
  folderId?: number
  keyword?: string
  pageNum: number
  pageSize: number
}

function buildCollectPageQueryString(query: CollectPageQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))
  if (query.folderId && query.folderId > 0) {
    params.set('folderId', String(query.folderId))
  }
  if (query.keyword && query.keyword.trim()) {
    params.set('keyword', query.keyword.trim())
  }
  return params.toString()
}

export async function collectWebsite(payload: CollectCreatePayload): Promise<number> {
  const res = await postJson<ApiResult<number>>('/api/user/collect', payload)
  if (res.code !== 200) {
    throw new Error(res.msg || '收藏失败')
  }
  return res.data ?? 0
}

export async function cancelCollect(websiteId: number): Promise<void> {
  const res = await deleteJson<ApiResult<null>>(`/api/user/collect/${websiteId}`)
  if (res.code !== 200) {
    throw new Error(res.msg || '取消收藏失败')
  }
}

export async function getCollectPage(query: CollectPageQuery): Promise<CollectPageResult> {
  const queryString = buildCollectPageQueryString(query)
  const res = await getJson<ApiResult<CollectPageResult>>(`/api/user/collect/list?${queryString}`)
  if (res.code !== 200) {
    throw new Error(res.msg || '查询收藏列表失败')
  }
  if (!res.data) {
    throw new Error('收藏列表为空')
  }
  return res.data
}

export async function getCollectStats(): Promise<CollectStats> {
  const res = await getJson<ApiResult<CollectStats>>('/api/user/collect/stats')
  if (res.code !== 200) {
    throw new Error(res.msg || '查询收藏统计失败')
  }
  return res.data ?? { collectCount: 0, folderCount: 0 }
}
