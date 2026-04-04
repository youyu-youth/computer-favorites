import { getJson } from '@/utils/http'
import type { ApiResult } from '@/api/types'
import type {
  PublicWebsiteCategory,
  PublicWebsiteDetail,
  PublicWebsiteListQuery,
  PublicWebsitePage,
  PublicWebsiteTagItem,
  PublicWebsiteTagPage,
  PublicWebsiteTagQuery,
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

  if (Array.isArray(query.tagIds) && query.tagIds.length > 0) {
    const normalizedTagIds = query.tagIds
      .map((tagId) => Number(tagId))
      .filter((tagId) => Number.isInteger(tagId) && tagId > 0)
    if (normalizedTagIds.length > 0) {
      params.set('tagIds', normalizedTagIds.join(','))
    }
  }

  return params.toString()
}

function buildTagQueryString(query: PublicWebsiteTagQuery): string {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))

  if (query.keyword && query.keyword.trim()) {
    params.set('keyword', query.keyword.trim())
  }

  if (query.sortField) {
    params.set('sortField', query.sortField)
  }

  if (typeof query.sortOrder === 'number') {
    params.set('sortOrder', String(query.sortOrder))
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

export async function getWebsiteTagPage(query: PublicWebsiteTagQuery): Promise<PublicWebsiteTagPage> {
  const queryString = buildTagQueryString(query)
  const res = await getJson<ApiResult<PublicWebsiteTagPage>>(`/api/tag/list?${queryString}`)
  if (res.code !== 200) {
    throw new Error(res.msg || '查询标签列表失败')
  }
  if (!res.data) {
    throw new Error('标签列表为空')
  }
  return res.data
}

export async function getWebsiteTags(): Promise<PublicWebsiteTagItem[]> {
  const pageSize = 100
  const firstPage = await getWebsiteTagPage({
    pageNum: 1,
    pageSize,
    sortField: 'useCount',
    sortOrder: -1,
  })

  const allRecords = [...(firstPage.records || [])]
  const totalPages = Number(firstPage.totalPages || 1)

  if (totalPages > 1) {
    for (let pageNum = 2; pageNum <= totalPages; pageNum += 1) {
      const nextPage = await getWebsiteTagPage({
        pageNum,
        pageSize,
        sortField: 'useCount',
        sortOrder: -1,
      })
      allRecords.push(...(nextPage.records || []))
    }
  }

  const uniqueTagMap = new Map<number, PublicWebsiteTagItem>()
  allRecords.forEach((item) => {
    const tagId = Number(item.id)
    if (!Number.isInteger(tagId) || tagId <= 0 || uniqueTagMap.has(tagId)) {
      return
    }
    uniqueTagMap.set(tagId, item)
  })

  return Array.from(uniqueTagMap.values())
}
