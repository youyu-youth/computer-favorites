<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  auditAdminWebsite,
  getAdminWebsiteDetail,
  getAdminWebsiteStats,
  updateAdminWebsite,
} from '@/api/admin-website'
import UTextarea from '@/components/ui-adapter/UTextarea.vue'
import { useToast } from '@/composables/useToast'
import { useAdminNavStore } from '@/stores/adminNav'
import type { AdminWebsiteDetail, AdminWebsiteEditPayload } from '@/types/admin-website'
import {
  formatDateTime,
  resolveBooleanFlagText,
  resolveDeletedText,
  resolveWebsiteAuditStatusText,
  resolveWebsiteSourceText,
  resolveWebsiteStatusText,
  toDisplayValue,
} from '@/utils/admin-website-display'

const route = useRoute()
const router = useRouter()
const { add: showToast } = useToast()
const adminNavStore = useAdminNavStore()

const loading = ref(false)
const auditSubmitting = ref(false)
const pendingFieldSubmitting = ref(false)
const errorMessage = ref('')
const detail = ref<AdminWebsiteDetail | null>(null)
const iconLoadFailed = ref(false)

const pendingForm = ref({
  isRecommend: false,
  isOfficial: false,
  auditRemark: '',
})

const pendingSnapshot = ref({
  isRecommend: false,
  isOfficial: false,
  auditRemark: '',
})

const websiteId = computed<number | null>(() => {
  const parsedId = Number(route.params.id)
  if (!Number.isInteger(parsedId) || parsedId <= 0) {
    return null
  }
  return parsedId
})

const resolveErrorMessage = (error: unknown, fallbackMessage: string): string => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallbackMessage
}

const syncPendingFormFromDetail = (record: AdminWebsiteDetail | null) => {
  if (!record) {
    pendingForm.value = {
      isRecommend: false,
      isOfficial: false,
      auditRemark: '',
    }
    pendingSnapshot.value = {
      isRecommend: false,
      isOfficial: false,
      auditRemark: '',
    }
    return
  }

  const normalizedForm = {
    isRecommend: record.isRecommend === 1,
    isOfficial: record.isOfficial === 1,
    auditRemark: String(record.auditRemark || '').trim(),
  }

  pendingForm.value = { ...normalizedForm }
  pendingSnapshot.value = { ...normalizedForm }
}

const canAudit = computed(() => {
  if (!detail.value) {
    return false
  }
  return detail.value.deleted === 0 && detail.value.source === 1 && detail.value.auditStatus === 0
})

const canEditPendingFields = computed(() => {
  if (!detail.value) {
    return false
  }
  return detail.value.deleted === 0 && detail.value.auditStatus === 0
})

const hasPendingFieldChanges = computed(() => {
  if (!canEditPendingFields.value) {
    return false
  }
  return (
    pendingForm.value.isRecommend !== pendingSnapshot.value.isRecommend ||
    pendingForm.value.isOfficial !== pendingSnapshot.value.isOfficial ||
    pendingForm.value.auditRemark.trim() !== pendingSnapshot.value.auditRemark
  )
})

const displayTagNames = computed(() => {
  if (!Array.isArray(detail.value?.tagNameList)) {
    return [] as string[]
  }
  return detail.value.tagNameList
    .map((tag) => String(tag || '').trim())
    .filter((tag) => tag.length > 0)
})

const operationRows = computed(() => {
  if (!detail.value) {
    return [] as Array<{ label: string; value: string }>
  }
  return [
    { label: '上架状态', value: resolveWebsiteStatusText(detail.value.status) },
    { label: '审核状态', value: resolveWebsiteAuditStatusText(detail.value.auditStatus) },
    { label: '来源类型', value: resolveWebsiteSourceText(detail.value.source) },
    { label: '删除状态', value: resolveDeletedText(detail.value.deleted) },
    { label: '置顶推荐', value: resolveBooleanFlagText(detail.value.isTop) },
    { label: '编辑精选', value: resolveBooleanFlagText(detail.value.isRecommend) },
    { label: '是否官方', value: resolveBooleanFlagText(detail.value.isOfficial) },
    { label: '排序值', value: toDisplayValue(detail.value.sort) },
  ]
})

const statsRows = computed(() => {
  if (!detail.value) {
    return [] as Array<{ label: string; value: string }>
  }
  return [
    { label: '点击量', value: toDisplayValue(detail.value.clickCount) },
    { label: '点赞量', value: toDisplayValue(detail.value.likeCount) },
    { label: '收藏量', value: toDisplayValue(detail.value.collectCount) },
    { label: '评论量', value: toDisplayValue(detail.value.commentCount) },
    { label: '平均评分', value: toDisplayValue(detail.value.score) },
    { label: '评分人数', value: toDisplayValue(detail.value.scoreCount) },
  ]
})

const timelineRows = computed(() => {
  if (!detail.value) {
    return [] as Array<{ label: string; value: string }>
  }
  return [
    { label: '创建时间', value: formatDateTime(detail.value.createTime) },
    { label: '更新时间', value: formatDateTime(detail.value.updateTime) },
    { label: '上架时间', value: formatDateTime(detail.value.shelfTime) },
    { label: '下架时间', value: formatDateTime(detail.value.takedownTime) },
  ]
})

const relationRows = computed(() => {
  if (!detail.value) {
    return [] as Array<{ label: string; value: string }>
  }
  return [
    { label: '分类', value: toDisplayValue(detail.value.categoryName) },
    { label: '提交用户', value: toDisplayValue(detail.value.submitterName) },
    { label: '审核管理员', value: toDisplayValue(detail.value.auditAdminName) },
    { label: '审核备注', value: toDisplayValue(detail.value.auditRemark) },
    { label: 'Github 地址', value: toDisplayValue(detail.value.githubUrl) },
  ]
})

const loadDetail = async () => {
  if (!websiteId.value) {
    detail.value = null
    errorMessage.value = '网站ID不合法'
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    detail.value = await getAdminWebsiteDetail(websiteId.value)
  } catch (error) {
    detail.value = null
    errorMessage.value = resolveErrorMessage(error, '网站详情加载失败')
    showToast({ type: 'error', title: errorMessage.value })
  } finally {
    loading.value = false
  }
}

const syncPendingAuditCount = async () => {
  try {
    const pendingStats = await getAdminWebsiteStats(0)
    adminNavStore.setPendingAuditCount(Number(pendingStats.pendingAudit || 0))
  } catch {
    // 角标同步失败不阻断主流程。
  }
}

watch(
  () => detail.value?.icon,
  () => {
    iconLoadFailed.value = false
  },
)

watch(
  () => detail.value,
  (record) => {
    syncPendingFormFromDetail(record)
  },
  { immediate: true },
)

watch(
  () => websiteId.value,
  () => {
    void loadDetail()
  },
  { immediate: true },
)

const goBack = () => {
  void router.push({ name: 'adminWebsites' })
}

const retryLoad = () => {
  void loadDetail()
}

const goEdit = () => {
  if (!websiteId.value) {
    showToast({ type: 'error', title: '网站ID不合法，无法进入编辑页面' })
    return
  }

  void router.push({
    name: 'adminWebsiteEdit',
    params: { id: String(websiteId.value) },
  })
}

const handleApprove = async () => {
  if (!websiteId.value || auditSubmitting.value || !canAudit.value) {
    return
  }

  auditSubmitting.value = true
  try {
    await auditAdminWebsite(websiteId.value, { action: 1 })
    adminNavStore.decreasePendingAuditCount(1)
    showToast({ type: 'success', title: '审核通过成功，网站已自动上架' })
    await Promise.all([loadDetail(), syncPendingAuditCount()])
  } catch (error) {
    showToast({ type: 'error', title: resolveErrorMessage(error, '网站审核失败') })
  } finally {
    auditSubmitting.value = false
  }
}

const handleReject = async () => {
  if (!websiteId.value || auditSubmitting.value || !canAudit.value) {
    return
  }

  const remark = window.prompt('请输入驳回原因')
  if (remark === null) {
    return
  }

  const normalizedRemark = remark.trim()
  if (!normalizedRemark) {
    showToast({ type: 'warning', title: '驳回原因不能为空' })
    return
  }

  auditSubmitting.value = true
  try {
    await auditAdminWebsite(websiteId.value, { action: 2, remark: normalizedRemark })
    adminNavStore.decreasePendingAuditCount(1)
    showToast({ type: 'success', title: '已完成驳回审核' })
    await Promise.all([loadDetail(), syncPendingAuditCount()])
  } catch (error) {
    showToast({ type: 'error', title: resolveErrorMessage(error, '网站审核失败') })
  } finally {
    auditSubmitting.value = false
  }
}

const buildEditPayload = (record: AdminWebsiteDetail): AdminWebsiteEditPayload => {
  const normalizedName = String(record.name || '').trim()
  const normalizedUrl = String(record.url || '').trim()
  if (!normalizedName || !normalizedUrl) {
    throw new Error('网站基础信息不完整，无法保存待审核配置')
  }

  const categoryId = Number(record.categoryId)
  if (!Number.isInteger(categoryId) || categoryId <= 0) {
    throw new Error('分类信息缺失，无法保存待审核配置')
  }

  return {
    name: normalizedName,
    url: normalizedUrl,
    icon: String(record.icon || ''),
    summary: String(record.summary || ''),
    description: String(record.description || ''),
    categoryId,
    tags: String(record.tags || ''),
    isTop: record.isTop === 1,
    isRecommend: pendingForm.value.isRecommend,
    isOfficial: pendingForm.value.isOfficial,
    auditRemark: pendingForm.value.auditRemark.trim(),
    sort: Number.isInteger(record.sort) ? record.sort : 0,
  }
}

const handleSavePendingFields = async () => {
  if (!websiteId.value || !detail.value || !canEditPendingFields.value || pendingFieldSubmitting.value) {
    return
  }

  pendingFieldSubmitting.value = true
  try {
    const payload = buildEditPayload(detail.value)
    await updateAdminWebsite(websiteId.value, payload)
    showToast({ type: 'success', title: '待审核配置保存成功' })
    await Promise.all([loadDetail(), syncPendingAuditCount()])
  } catch (error) {
    showToast({ type: 'error', title: resolveErrorMessage(error, '待审核配置保存失败') })
  } finally {
    pendingFieldSubmitting.value = false
  }
}
</script>

<template>
  <main class="flex-grow w-full px-4 py-6 sm:px-6 lg:px-8">
    <div class="mx-auto w-full max-w-[1320px] min-w-0">
      <div class="mb-5 flex flex-wrap items-center justify-between gap-3">
        <button
          type="button"
          class="inline-flex cursor-pointer items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-200 dark:hover:bg-dark-border"
          @click="goBack"
        >
          <i class="fas fa-arrow-left"></i>
          <span>返回网站列表</span>
        </button>

        <div class="flex flex-wrap items-center gap-3">
          <button
            v-if="canAudit"
            type="button"
            :disabled="auditSubmitting"
            class="inline-flex cursor-pointer items-center gap-2 rounded-lg border border-sky-300 bg-sky-50 px-4 py-2 text-sm font-medium text-sky-700 transition-colors hover:bg-sky-100 disabled:cursor-not-allowed disabled:opacity-60 dark:border-sky-700 dark:bg-sky-900/20 dark:text-sky-200 dark:hover:bg-sky-900/35"
            @click="handleApprove"
          >
            <i class="fas fa-circle-check"></i>
            <span>{{ auditSubmitting ? '处理中...' : '审核通过' }}</span>
          </button>

          <button
            v-if="canAudit"
            type="button"
            :disabled="auditSubmitting"
            class="inline-flex cursor-pointer items-center gap-2 rounded-lg border border-rose-300 bg-rose-50 px-4 py-2 text-sm font-medium text-rose-700 transition-colors hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-60 dark:border-rose-700 dark:bg-rose-900/20 dark:text-rose-200 dark:hover:bg-rose-900/35"
            @click="handleReject"
          >
            <i class="fas fa-circle-xmark"></i>
            <span>{{ auditSubmitting ? '处理中...' : '审核驳回' }}</span>
          </button>

          <button
            type="button"
            class="inline-flex cursor-pointer items-center gap-2 rounded-lg border border-brand-orange/35 bg-orange-50 px-4 py-2 text-sm font-medium text-brand-orange transition-colors hover:bg-orange-100 dark:border-brand-orange/40 dark:bg-brand-orange/10 dark:text-brand-orange dark:hover:bg-brand-orange/20"
            @click="goEdit"
          >
            <i class="fas fa-pen-to-square"></i>
            <span>修改网站</span>
          </button>

          <button
            type="button"
            class="inline-flex cursor-pointer items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-200 dark:hover:bg-dark-border"
            @click="retryLoad"
          >
            <i class="fas fa-rotate-right"></i>
            <span>刷新详情</span>
          </button>
        </div>
      </div>

      <div
        v-if="loading"
        class="rounded-xl border border-gray-200 bg-white py-16 text-center text-gray-500 dark:border-dark-border dark:bg-dark-card dark:text-gray-300"
      >
        <i class="fas fa-spinner fa-spin text-3xl"></i>
        <p class="mt-3 text-sm">正在加载网站详情...</p>
      </div>

      <div
        v-else-if="errorMessage"
        class="rounded-xl border border-red-200 bg-red-50 px-6 py-12 text-center text-red-500 dark:border-red-900/40 dark:bg-red-900/10 dark:text-red-300"
      >
        <i class="fas fa-circle-exclamation text-3xl"></i>
        <p class="mt-3 text-sm">{{ errorMessage }}</p>
      </div>

      <div v-else-if="detail" class="space-y-5">
        <section
          class="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm dark:border-dark-border dark:bg-dark-card"
        >
          <div class="flex flex-col gap-5 p-5 md:flex-row md:items-start md:justify-between">
            <div class="flex min-w-0 flex-1 items-start gap-4">
              <div
                v-if="detail.icon && !iconLoadFailed"
                class="inline-flex h-16 w-16 shrink-0 items-center justify-center overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-dark-border dark:bg-dark-bg"
              >
                <img
                  :src="detail.icon"
                  :alt="`${toDisplayValue(detail.name)} 图标`"
                  class="h-14 w-14 object-cover"
                  @error="iconLoadFailed = true"
                />
              </div>
              <div
                v-else
                class="inline-flex h-16 w-16 shrink-0 items-center justify-center rounded-xl border border-gray-200 bg-gray-100 text-xl text-gray-500 dark:border-dark-border dark:bg-dark-bg dark:text-gray-300"
              >
                <i class="fas fa-globe"></i>
              </div>

              <div class="min-w-0 flex-1">
                <h1 class="break-all text-xl font-semibold text-gray-900 dark:text-gray-50">
                  {{ toDisplayValue(detail.name) }}
                </h1>
                <a
                  :href="detail.url"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="mt-1 inline-flex cursor-pointer items-center gap-2 break-all text-sm text-cyan-700 underline-offset-2 transition-colors hover:underline dark:text-cyan-300"
                >
                  <i class="fas fa-arrow-up-right-from-square text-xs"></i>
                  <span>{{ toDisplayValue(detail.url) }}</span>
                </a>

                <div class="mt-3 flex flex-wrap gap-2">
                  <span
                    class="rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-1 text-xs text-emerald-700 dark:border-emerald-800/60 dark:bg-emerald-900/20 dark:text-emerald-200"
                  >
                    {{ resolveWebsiteStatusText(detail.status) }}
                  </span>
                  <span
                    class="rounded-full border border-blue-200 bg-blue-50 px-2.5 py-1 text-xs text-blue-700 dark:border-blue-800/60 dark:bg-blue-900/20 dark:text-blue-200"
                  >
                    {{ resolveWebsiteAuditStatusText(detail.auditStatus) }}
                  </span>
                  <span
                    class="rounded-full border border-amber-200 bg-amber-50 px-2.5 py-1 text-xs text-amber-700 dark:border-amber-800/60 dark:bg-amber-900/20 dark:text-amber-200"
                  >
                    {{ resolveWebsiteSourceText(detail.source) }}
                  </span>
                </div>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-2 md:min-w-[260px]">
              <div
                v-for="item in statsRows.slice(0, 4)"
                :key="`head-${item.label}`"
                class="rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 dark:border-dark-border dark:bg-dark-bg/50"
              >
                <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.label }}</p>
                <p class="mt-1 text-sm font-semibold text-gray-900 dark:text-gray-100">
                  {{ item.value }}
                </p>
              </div>
            </div>
          </div>
        </section>

        <div class="grid grid-cols-1 gap-5 xl:grid-cols-[1.15fr_0.85fr]">
          <section class="space-y-5">
            <div class="rounded-xl border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card">
              <h2 class="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-200">基础信息</h2>
              <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                <div class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60">
                  <p class="text-xs text-gray-500 dark:text-gray-400">分类名称</p>
                  <p class="mt-1 break-all text-sm font-medium text-gray-800 dark:text-gray-100">
                    {{ toDisplayValue(detail.categoryName) }}
                  </p>
                </div>
                <div class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60">
                  <p class="text-xs text-gray-500 dark:text-gray-400">Github 地址</p>
                  <p class="mt-1 break-all text-sm font-medium text-gray-800 dark:text-gray-100">
                    {{ toDisplayValue(detail.githubUrl) }}
                  </p>
                </div>
              </div>
            </div>

            <div class="rounded-xl border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card">
              <h2 class="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-200">简介与描述</h2>
              <div class="space-y-4">
                <div class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60">
                  <p class="text-xs text-gray-500 dark:text-gray-400">一句话简介</p>
                  <p class="mt-1 whitespace-pre-wrap break-words text-sm text-gray-800 dark:text-gray-100">
                    {{ toDisplayValue(detail.summary) }}
                  </p>
                </div>
                <div class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60">
                  <p class="text-xs text-gray-500 dark:text-gray-400">详细描述</p>
                  <p class="mt-1 whitespace-pre-wrap break-words text-sm text-gray-800 dark:text-gray-100">
                    {{ toDisplayValue(detail.description) }}
                  </p>
                </div>
              </div>
            </div>

            <div class="rounded-xl border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card">
              <h2 class="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-200">标签信息</h2>
              <div v-if="displayTagNames.length > 0" class="flex flex-wrap gap-2">
                <span
                  v-for="tag in displayTagNames"
                  :key="tag"
                  class="rounded-full border border-gray-200 bg-gray-100 px-2.5 py-1 text-xs text-gray-700 dark:border-dark-border dark:bg-dark-bg dark:text-gray-300"
                >
                  {{ tag }}
                </span>
              </div>
              <p v-else class="text-sm text-gray-500 dark:text-gray-400">暂无可展示标签</p>
            </div>

            <div class="rounded-xl border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card">
              <h2 class="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-200">审核配置</h2>

              <div
                v-if="canEditPendingFields"
                data-testid="admin-detail-pending-form"
                class="rounded-lg border border-cyan-200 bg-cyan-50/60 p-3 dark:border-cyan-900/40 dark:bg-cyan-900/10"
              >
                <p class="text-xs text-cyan-700 dark:text-cyan-200">
                  当前为待审核网站，可在通过/驳回前先调整推荐、官方标识与审核备注。
                </p>

                <div class="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div class="rounded-md border border-gray-200 bg-white p-3 dark:border-dark-border dark:bg-dark-bg/60">
                    <p class="text-xs text-gray-500 dark:text-gray-400">编辑精选</p>
                    <div class="mt-2 inline-flex overflow-hidden rounded-lg border border-gray-300 dark:border-dark-border">
                      <button
                        type="button"
                        data-testid="admin-detail-recommend-yes"
                        class="cursor-pointer px-3 py-1.5 text-xs font-medium transition-colors"
                        :class="pendingForm.isRecommend ? 'bg-emerald-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-100 dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg'"
                        @click="pendingForm.isRecommend = true"
                      >
                        是
                      </button>
                      <button
                        type="button"
                        data-testid="admin-detail-recommend-no"
                        class="cursor-pointer border-l border-gray-300 px-3 py-1.5 text-xs font-medium transition-colors dark:border-dark-border"
                        :class="!pendingForm.isRecommend ? 'bg-rose-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-100 dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg'"
                        @click="pendingForm.isRecommend = false"
                      >
                        否
                      </button>
                    </div>
                  </div>

                  <div class="rounded-md border border-gray-200 bg-white p-3 dark:border-dark-border dark:bg-dark-bg/60">
                    <p class="text-xs text-gray-500 dark:text-gray-400">是否官方</p>
                    <div class="mt-2 inline-flex overflow-hidden rounded-lg border border-gray-300 dark:border-dark-border">
                      <button
                        type="button"
                        data-testid="admin-detail-official-yes"
                        class="cursor-pointer px-3 py-1.5 text-xs font-medium transition-colors"
                        :class="pendingForm.isOfficial ? 'bg-emerald-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-100 dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg'"
                        @click="pendingForm.isOfficial = true"
                      >
                        是
                      </button>
                      <button
                        type="button"
                        data-testid="admin-detail-official-no"
                        class="cursor-pointer border-l border-gray-300 px-3 py-1.5 text-xs font-medium transition-colors dark:border-dark-border"
                        :class="!pendingForm.isOfficial ? 'bg-rose-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-100 dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg'"
                        @click="pendingForm.isOfficial = false"
                      >
                        否
                      </button>
                    </div>
                  </div>
                </div>

                <div class="mt-3">
                  <p class="mb-1 text-xs text-gray-500 dark:text-gray-400">审核备注</p>
                  <UTextarea
                    v-model="pendingForm.auditRemark"
                    :rows="4"
                    placeholder="请输入审核备注（拒绝时建议明确原因）"
                  />
                </div>

                <div class="mt-3 flex flex-wrap items-center justify-between gap-3">
                  <p class="text-xs text-gray-500 dark:text-gray-400">
                    {{ hasPendingFieldChanges ? '存在未保存变更' : '当前无待保存变更' }}
                  </p>
                  <button
                    type="button"
                    data-testid="admin-detail-pending-save"
                    :disabled="pendingFieldSubmitting || !hasPendingFieldChanges"
                    class="inline-flex cursor-pointer items-center gap-2 rounded-lg border border-cyan-300 bg-cyan-600 px-4 py-2 text-xs font-semibold text-white transition-colors hover:bg-cyan-700 disabled:cursor-not-allowed disabled:opacity-60 dark:border-cyan-700"
                    @click="handleSavePendingFields"
                  >
                    <i class="fas fa-floppy-disk"></i>
                    <span>{{ pendingFieldSubmitting ? '保存中...' : '保存待审核配置' }}</span>
                  </button>
                </div>
              </div>

              <div v-else class="grid grid-cols-1 gap-3 md:grid-cols-2">
                <div
                  v-for="item in operationRows"
                  :key="item.label"
                  class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60"
                >
                  <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.label }}</p>
                  <p class="mt-1 break-all text-sm font-medium text-gray-800 dark:text-gray-100">
                    {{ item.value }}
                  </p>
                </div>
              </div>
            </div>
          </section>

          <section class="space-y-5">
            <div class="rounded-xl border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card">
              <h2 class="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-200">运营字段</h2>
              <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                <div
                  v-for="item in operationRows"
                  :key="`op-${item.label}`"
                  class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60"
                >
                  <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.label }}</p>
                  <p class="mt-1 break-all text-sm font-medium text-gray-800 dark:text-gray-100">
                    {{ item.value }}
                  </p>
                </div>
              </div>
            </div>

            <div class="rounded-xl border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card">
              <h2 class="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-200">统计数据</h2>
              <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                <div
                  v-for="item in statsRows"
                  :key="`stats-${item.label}`"
                  class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60"
                >
                  <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.label }}</p>
                  <p class="mt-1 break-all text-sm font-medium text-gray-800 dark:text-gray-100">
                    {{ item.value }}
                  </p>
                </div>
              </div>
            </div>

            <div class="rounded-xl border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card">
              <h2 class="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-200">时间轴</h2>
              <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                <div
                  v-for="item in timelineRows"
                  :key="`timeline-${item.label}`"
                  class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60"
                >
                  <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.label }}</p>
                  <p class="mt-1 break-all text-sm font-medium text-gray-800 dark:text-gray-100">
                    {{ item.value }}
                  </p>
                </div>
              </div>
            </div>

            <div class="rounded-xl border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card">
              <h2 class="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-200">关联信息</h2>
              <div class="space-y-3">
                <div
                  v-for="item in relationRows"
                  :key="`relation-${item.label}`"
                  class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60"
                >
                  <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.label }}</p>
                  <p class="mt-1 break-all text-sm font-medium text-gray-800 dark:text-gray-100">
                    {{ item.value }}
                  </p>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  </main>
</template>
