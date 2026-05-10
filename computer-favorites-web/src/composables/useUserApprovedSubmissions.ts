import { computed, ref, shallowRef } from 'vue'
import { getMyWebsiteSubmissionPage } from '@/api/user-website-submission'
import type { UserWebsiteSubmissionListItem } from '@/types/user-website-submission'
import type { ProfileSiteItem } from '@/types/profile'

/**
 * 当前登录用户「审核通过」的投稿网站（侧边栏卡片用）。
 *
 * 数据来源：GET /api/user/website/submission/list?auditStatus=1
 * 默认拉取最近更新的前 N 条；失败静默降级为空数组，不打 toast。
 */
export interface UseUserApprovedSubmissionsOptions {
  /** 拉取条数，默认 5 */
  limit?: number
}

const APPROVED_AUDIT_STATUS = 1

const mapToProfileSiteItem = (record: UserWebsiteSubmissionListItem): ProfileSiteItem => {
  const description = record.summary?.trim() || record.url?.trim() || record.categoryName?.trim() || ''
  const iconUrl = record.icon?.trim() || ''
  return {
    name: record.name,
    description,
    icon: 'i-lucide-folder',
    websiteId: record.id,
    iconUrl: iconUrl || undefined,
  }
}

export function useUserApprovedSubmissions(options: UseUserApprovedSubmissionsOptions = {}) {
  const pageSize = options.limit ?? 5

  const items = ref<ProfileSiteItem[]>([])
  const total = ref(0)
  const loading = shallowRef(false)
  const error = shallowRef<Error | null>(null)

  const hasMore = computed(() => total.value > items.value.length)

  const load = async () => {
    loading.value = true
    error.value = null
    try {
      const page = await getMyWebsiteSubmissionPage({
        pageNum: 1,
        pageSize,
        auditStatus: APPROVED_AUDIT_STATUS,
      })
      items.value = (page.records || []).map(mapToProfileSiteItem)
      total.value = page.total ?? items.value.length
    } catch (err) {
      error.value = err instanceof Error ? err : new Error('获取审核通过的投稿失败')
      items.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    items.value = []
    total.value = 0
    error.value = null
  }

  return {
    items,
    total,
    loading,
    error,
    hasMore,
    load,
    reset,
  }
}
