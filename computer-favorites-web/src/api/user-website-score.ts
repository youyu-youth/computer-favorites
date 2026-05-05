import { getJson, postJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type { MyScore, ScorePayload } from '@/types/website-like'

/** 网站评分 */
export async function scoreWebsite(websiteId: number, payload: ScorePayload): Promise<void> {
  const res = await postJson<ApiResult<null>>(`/api/user/website/${websiteId}/score`, payload)
  if (res.code !== 200) {
    throw new Error(res.msg || '评分失败')
  }
}

/** 查询我的评分 */
export async function getMyScore(websiteId: number): Promise<MyScore> {
  const res = await getJson<ApiResult<MyScore>>(`/api/user/website/${websiteId}/score/mine`)
  if (res.code !== 200) {
    throw new Error(res.msg || '查询评分失败')
  }
  return res.data ?? { score: null }
}
