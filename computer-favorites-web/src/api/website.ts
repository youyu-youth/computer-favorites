import { getJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type {
  PublicWebsiteCategory,
  PublicWebsiteDetail,
  PublicWebsiteListQuery,
  PublicWebsitePage,
} from '@/types/public-website'

function buildListQueryString(query: PublicWebsiteListQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))

  if (typeof query.categoryId === 'number' && query.categoryId > 0) {
    params.set('categoryId', String(query.categoryId))
  }

  if (query.keyword && query.keyword.trim()) {
    params.set('keyword', query.keyword.trim())
  }

  return params.toString()
}

export async function getWebsitePage(query: PublicWebsiteListQuery): Promise<PublicWebsitePage> {
  const queryString = buildListQueryString(query)
  const res = await getJson<ApiResult<PublicWebsitePage>>(`/api/website/list?${queryString}`)
  if (res.code !== 200) {
    throw new Error(res.msg || '查询网站列表失败')
  }
  if (!res.data) {
    throw new Error('网站列表为空')
  }
  return res.data
}

export async function getWebsiteCategories(): Promise<PublicWebsiteCategory[]> {
  const res = await getJson<ApiResult<PublicWebsiteCategory[]>>('/api/website/categories')
  if (res.code !== 200) {
    throw new Error(res.msg || '查询网站分类失败')
  }
  return res.data || []
}

export async function getWebsiteDetail(websiteId: number): Promise<PublicWebsiteDetail> {
  const res = await getJson<ApiResult<PublicWebsiteDetail>>(`/api/website/${websiteId}`)
  if (res.code !== 200) {
    throw new Error(res.msg || '查询网站详情失败')
  }
  if (!res.data) {
    throw new Error('网站详情为空')
  }
  return res.data
}
