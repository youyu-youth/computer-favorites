import { getJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import { buildAdminAuthHeaders } from '@/api/admin-auth-headers'
import type {
  AdminWebsiteCategory,
  AdminWebsiteListQuery,
  AdminWebsitePage,
  AdminWebsiteStats,
  DeletedFilterValue,
} from '@/types/admin-website'

function buildListQueryString(query: AdminWebsiteListQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))
  params.set('deleted', String(query.deleted))

  if (typeof query.categoryId === 'number' && query.categoryId > 0) {
    params.set('categoryId', String(query.categoryId))
  }

  if (query.keyword && query.keyword.trim()) {
    params.set('keyword', query.keyword.trim())
  }

  return params.toString()
}

export async function getAdminWebsitePage(query: AdminWebsiteListQuery): Promise<AdminWebsitePage> {
  const queryString = buildListQueryString(query)
  const res = await getJson<ApiResult<AdminWebsitePage>>(`/api/admin/website/list?${queryString}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询网站列表失败')
  }
  if (!res.data) {
    throw new Error('网站列表为空')
  }
  return res.data
}

export async function getAdminWebsiteCategories(
  deleted: DeletedFilterValue,
): Promise<AdminWebsiteCategory[]> {
  const res = await getJson<ApiResult<AdminWebsiteCategory[]>>(`/api/admin/website/categories?deleted=${deleted}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询分类统计失败')
  }
  return res.data || []
}

export async function getAdminWebsiteStats(deleted: DeletedFilterValue): Promise<AdminWebsiteStats> {
  const res = await getJson<ApiResult<AdminWebsiteStats>>(`/api/admin/website/stats?deleted=${deleted}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询网站统计失败')
  }
  if (!res.data) {
    throw new Error('网站统计数据为空')
  }
  return res.data
}
