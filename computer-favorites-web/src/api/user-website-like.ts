import { deleteJson, getJson, postJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type { LikeStatus, LikedWebsitePage, LikedWebsitePageQuery } from '@/types/website-like'

function buildLikePageQueryString(query: LikedWebsitePageQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))
  return params.toString()
}

/** 网站点赞 */
export async function likeWebsite(websiteId: number): Promise<void> {
  const res = await postJson<ApiResult<null>>(`/api/user/website/${websiteId}/like`)
  if (res.code !== 200) {
    throw new Error(res.msg || '点赞失败')
  }
}

/** 取消点赞 */
export async function unlikeWebsite(websiteId: number): Promise<void> {
  const res = await deleteJson<ApiResult<null>>(`/api/user/website/${websiteId}/like`)
  if (res.code !== 200) {
    throw new Error(res.msg || '取消点赞失败')
  }
}

/** 查询当前用户是否已点赞 */
export async function getLikeStatus(websiteId: number): Promise<LikeStatus> {
  const res = await getJson<ApiResult<LikeStatus>>(`/api/user/website/${websiteId}/like/status`)
  if (res.code !== 200) {
    throw new Error(res.msg || '查询点赞状态失败')
  }
  return res.data ?? { isLiked: false }
}

/** 我点赞的网站列表 */
export async function getMyLikedWebsites(query: LikedWebsitePageQuery): Promise<LikedWebsitePage> {
  const queryString = buildLikePageQueryString(query)
  const res = await getJson<ApiResult<LikedWebsitePage>>(`/api/user/website/my/likes?${queryString}`)
  if (res.code !== 200) {
    throw new Error(res.msg || '查询点赞列表失败')
  }
  if (!res.data) {
    throw new Error('点赞列表为空')
  }
  return res.data
}
