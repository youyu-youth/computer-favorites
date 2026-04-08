import { deleteJson, getJson, postJson, putJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import { buildAdminAuthHeaders } from '@/api/admin-auth-headers'
import type {
  AdminCategoryCreatePayload,
  AdminCategoryEditPayload,
  AdminCategorySortPayload,
  AdminCategoryStatusValue,
  AdminCategoryTreeNode,
} from '@/types/category'

function buildCategoryTreeQueryString(status?: AdminCategoryStatusValue): string {
  const params = new URLSearchParams()
  if (typeof status === 'number') {
    params.set('status', String(status))
  }
  return params.toString()
}

export async function getAdminCategoryTree(
  status?: AdminCategoryStatusValue,
): Promise<AdminCategoryTreeNode[]> {
  const queryString = buildCategoryTreeQueryString(status)
  const requestUrl = queryString ? `/api/admin/category/tree?${queryString}` : '/api/admin/category/tree'
  const res = await getJson<ApiResult<AdminCategoryTreeNode[]>>(requestUrl, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '查询分类树失败')
  }
  return res.data || []
}

export async function createAdminCategory(payload: AdminCategoryCreatePayload): Promise<number> {
  const res = await postJson<ApiResult<number>>('/api/admin/category', payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '创建分类失败')
  }
  if (typeof res.data !== 'number') {
    throw new Error('创建分类返回数据异常')
  }
  return res.data
}

export async function updateAdminCategory(
  categoryId: number,
  payload: AdminCategoryEditPayload,
): Promise<void> {
  const res = await putJson<ApiResult<unknown>>(`/api/admin/category/${categoryId}`, payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '编辑分类失败')
  }
}

export async function updateAdminCategorySort(
  categoryId: number,
  payload: AdminCategorySortPayload,
): Promise<void> {
  const res = await putJson<ApiResult<unknown>>(`/api/admin/category/${categoryId}/sort`, payload, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '更新分类排序失败')
  }
}

export async function deleteAdminCategory(categoryId: number): Promise<void> {
  const res = await deleteJson<ApiResult<unknown>>(`/api/admin/category/${categoryId}`, {
    headers: buildAdminAuthHeaders(),
  })
  if (res.code !== 200) {
    throw new Error(res.msg || '删除分类失败')
  }
}
