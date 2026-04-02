<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAdminWebsiteDetail } from '@/api/admin-website'
import { useToast } from '@/composables/useToast'
import type { AdminWebsiteDetail } from '@/types/admin-website'

const route = useRoute()
const router = useRouter()
const { add: showToast } = useToast()

const loading = ref(false)
const errorMessage = ref('')
const detail = ref<AdminWebsiteDetail | null>(null)
const iconLoadFailed = ref(false)

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

const toDisplayValue = (value: unknown): string => {
  if (value === null || value === undefined) {
    return '-'
  }
  const text = String(value).trim()
  return text ? text : '-'
}

const formatTime = (value?: string): string => {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '-'
  }
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

const resolveStatusText = (status?: number): string => {
  if (status === 1) {
    return '已上架'
  }
  if (status === 0) {
    return '已下架'
  }
  return '-'
}

const resolveAuditStatusText = (auditStatus?: number): string => {
  if (auditStatus === 1) {
    return '已通过'
  }
  if (auditStatus === 2) {
    return '已拒绝'
  }
  if (auditStatus === 0) {
    return '待审核'
  }
  return '-'
}

const resolveSourceText = (source?: number): string => {
  if (source === 0) {
    return '管理员录入'
  }
  if (source === 1) {
    return '用户投稿'
  }
  return '-'
}

const resolveDeletedText = (deleted?: number): string => {
  if (deleted === 1) {
    return '已删除'
  }
  if (deleted === 0) {
    return '正常'
  }
  return '-'
}

const resolveBoolFlagText = (value?: number): string => {
  if (value === 1) {
    return '是'
  }
  if (value === 0) {
    return '否'
  }
  return '-'
}

const parsedTags = computed<string[]>(() => {
  const rawTags = detail.value?.tags
  if (!rawTags) {
    return []
  }
  return rawTags
    .replace(/，/g, ',')
    .replace(/；/g, ';')
    .split(/[;,]/)
    .map((tag) => tag.trim())
    .filter((tag) => tag.length > 0)
})

const baseRows = computed(() => {
  if (!detail.value) {
    return [] as Array<{ label: string; value: string }>
  }
  return [
    { label: '网站ID', value: toDisplayValue(detail.value.id) },
    { label: '网站名称', value: toDisplayValue(detail.value.name) },
    { label: '网站URL', value: toDisplayValue(detail.value.url) },
    { label: '图标地址', value: toDisplayValue(detail.value.icon) },
    { label: '分类ID', value: toDisplayValue(detail.value.categoryId) },
    { label: '分类名称', value: toDisplayValue(detail.value.categoryName) },
  ]
})

const operationRows = computed(() => {
  if (!detail.value) {
    return [] as Array<{ label: string; value: string }>
  }
  return [
    { label: '上架状态', value: resolveStatusText(detail.value.status) },
    { label: '审核状态', value: resolveAuditStatusText(detail.value.auditStatus) },
    { label: '来源类型', value: resolveSourceText(detail.value.source) },
    { label: '删除状态', value: resolveDeletedText(detail.value.deleted) },
    { label: '置顶推荐', value: resolveBoolFlagText(detail.value.isTop) },
    { label: '编辑精选', value: resolveBoolFlagText(detail.value.isRecommend) },
    { label: '排序值', value: toDisplayValue(detail.value.sort) },
    { label: '审核备注', value: toDisplayValue(detail.value.auditRemark) },
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

const identityRows = computed(() => {
  if (!detail.value) {
    return [] as Array<{ label: string; value: string }>
  }
  return [
    { label: '提交用户ID', value: toDisplayValue(detail.value.submitterId) },
    { label: '审核管理员ID', value: toDisplayValue(detail.value.auditAdminId) },
    { label: '创建时间', value: formatTime(detail.value.createTime) },
    { label: '更新时间', value: formatTime(detail.value.updateTime) },
    { label: '上架时间', value: formatTime(detail.value.shelfTime) },
    { label: '下架时间', value: formatTime(detail.value.takedownTime) },
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

watch(
  () => detail.value?.icon,
  () => {
    iconLoadFailed.value = false
  }
)

watch(
  () => websiteId.value,
  () => {
    void loadDetail()
  },
  { immediate: true }
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
</script>

<template>
  <main class="flex-grow w-full px-4 sm:px-6 lg:px-8 py-6">
    <div class="max-w-[1320px] mx-auto min-w-0">
      <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
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

      <div
        v-else-if="detail"
        class="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm dark:border-dark-border dark:bg-dark-card"
      >
        <div class="flex flex-col lg:flex-row">
          <aside class="w-full border-b border-gray-800 bg-gray-900 p-6 text-gray-300 lg:w-1/3 lg:border-b-0 lg:border-r md:p-8">
            <div class="flex gap-2">
              <span class="h-3 w-3 rounded-full bg-red-500"></span>
              <span class="h-3 w-3 rounded-full bg-yellow-500"></span>
              <span class="h-3 w-3 rounded-full bg-green-500"></span>
            </div>

            <div class="mt-6">
              <h1 class="break-all text-xl font-bold text-white">{{ toDisplayValue(detail.name) }}</h1>
              <p class="mt-2 break-all text-xs text-gray-400">{{ toDisplayValue(detail.url) }}</p>
            </div>

            <div class="mt-4 flex flex-wrap gap-2">
              <span class="rounded-full border border-emerald-700 bg-emerald-900/20 px-2.5 py-1 text-xs text-emerald-200">{{ resolveStatusText(detail.status) }}</span>
              <span class="rounded-full border border-blue-700 bg-blue-900/20 px-2.5 py-1 text-xs text-blue-200">{{ resolveAuditStatusText(detail.auditStatus) }}</span>
              <span class="rounded-full border border-gray-700 bg-gray-800/60 px-2.5 py-1 text-xs text-gray-200">{{ resolveSourceText(detail.source) }}</span>
            </div>

            <div class="mt-8">
              <div
                v-if="detail.icon && !iconLoadFailed"
                class="inline-flex h-16 w-16 items-center justify-center overflow-hidden rounded-lg border border-gray-700 bg-white"
              >
                <img
                  :src="detail.icon"
                  :alt="`${toDisplayValue(detail.name)} 图标`"
                  class="h-14 w-14 object-cover"
                  @error="iconLoadFailed = true"
                >
              </div>
              <div
                v-else
                class="inline-flex h-16 w-16 items-center justify-center rounded-lg border border-gray-700 bg-gray-800 text-xl text-gray-400"
              >
                <i class="fas fa-globe"></i>
              </div>
              <p class="mt-2 text-xs text-gray-400">Logo 预览</p>
            </div>
          </aside>

          <section class="w-full space-y-6 bg-gray-50 p-6 dark:bg-dark-bg/50 md:p-8 lg:w-2/3">
            <div class="rounded-lg border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card">
              <h2 class="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-200">基础信息</h2>
              <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                <div v-for="item in baseRows" :key="item.label" class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60">
                  <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.label }}</p>
                  <p class="mt-1 break-all text-sm font-medium text-gray-800 dark:text-gray-100">{{ item.value }}</p>
                </div>
              </div>
            </div>

            <div class="rounded-lg border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card">
              <h2 class="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-200">简介与描述</h2>
              <div class="space-y-4">
                <div class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60">
                  <p class="text-xs text-gray-500 dark:text-gray-400">一句话简介</p>
                  <p class="mt-1 whitespace-pre-wrap break-words text-sm text-gray-800 dark:text-gray-100">{{ toDisplayValue(detail.summary) }}</p>
                </div>
                <div class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60">
                  <p class="text-xs text-gray-500 dark:text-gray-400">详细描述</p>
                  <p class="mt-1 whitespace-pre-wrap break-words text-sm text-gray-800 dark:text-gray-100">{{ toDisplayValue(detail.description) }}</p>
                </div>
              </div>
            </div>

            <div class="rounded-lg border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card">
              <h2 class="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-200">标签信息</h2>
              <div v-if="parsedTags.length > 0" class="flex flex-wrap gap-2">
                <span
                  v-for="tag in parsedTags"
                  :key="tag"
                  class="rounded-full border border-gray-200 bg-gray-100 px-2.5 py-1 text-xs text-gray-700 dark:border-dark-border dark:bg-dark-bg dark:text-gray-300"
                >
                  {{ tag }}
                </span>
              </div>
              <p v-else class="text-sm text-gray-500 dark:text-gray-400">-</p>
            </div>

            <div class="rounded-lg border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card">
              <h2 class="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-200">运营与审核</h2>
              <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                <div v-for="item in operationRows" :key="item.label" class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60">
                  <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.label }}</p>
                  <p class="mt-1 break-all text-sm font-medium text-gray-800 dark:text-gray-100">{{ item.value }}</p>
                </div>
              </div>
            </div>

            <div class="rounded-lg border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card">
              <h2 class="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-200">统计数据</h2>
              <div class="grid grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-3">
                <div v-for="item in statsRows" :key="item.label" class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60">
                  <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.label }}</p>
                  <p class="mt-1 break-all text-sm font-medium text-gray-800 dark:text-gray-100">{{ item.value }}</p>
                </div>
              </div>
            </div>

            <div class="rounded-lg border border-gray-200 bg-white p-4 dark:border-dark-border dark:bg-dark-card">
              <h2 class="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-200">时间与身份</h2>
              <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                <div v-for="item in identityRows" :key="item.label" class="rounded-md border border-gray-100 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60">
                  <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.label }}</p>
                  <p class="mt-1 break-all text-sm font-medium text-gray-800 dark:text-gray-100">{{ item.value }}</p>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  </main>
</template>
