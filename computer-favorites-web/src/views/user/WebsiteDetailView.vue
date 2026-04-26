<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  SquareTerminal,
  Heart,
  ThumbsUp,
  Eye,
  BookOpen,
  Database,
  Download,
  Play,
  Globe,
  Flag,
  Calendar,
  History,
} from 'lucide-vue-next'

import { getWebsiteDetail } from '@/api/website'
import recommendIcon from '@/assets/icons/svg/tuijian.svg'
import MarkdownViewer from '@/components/user/MarkdownViewer.vue'
import StarRating from '@/components/common/StarRating.vue'
import ReportDialog from '@/components/user/ReportDialog.vue'
import CollectFolderDialog from '@/components/user/CollectFolderDialog.vue'
import type { PublicWebsiteTagRef, PublicWebsiteDetail } from '@/types/public-website'
import type { ReportFormData } from '@/types/report'
import { buildTagColorStyle, normalizeTagColor } from '@/utils/tag-color'

const route = useRoute()

const activeTab = ref('Overview')
const tabs = ['Overview', 'Schema', 'Related Servers', 'Score', 'Discussions']

const loading = ref(false)
const errorMessage = ref('')
const detail = ref<PublicWebsiteDetail | null>(null)
let latestRequestId = 0

const websiteId = computed<number | null>(() => {
  const parsedId = Number(route.params.id)
  if (!Number.isInteger(parsedId) || parsedId <= 0) {
    return null
  }
  return parsedId
})

const websiteName = computed(() => detail.value?.name || '未命名网站')

const websiteUrl = computed(() => detail.value?.url || '')

const markdownContent = computed(() => {
  const description = detail.value?.description?.trim()
  if (description) {
    return description
  }
  const summary = detail.value?.summary?.trim()
  if (summary) {
    return `## 网站简介\n\n${summary}`
  }
  return '## 暂无详细介绍\n\n该网站暂未提供详细描述。'
})

const summaryText = computed(() => {
  const summary = detail.value?.summary?.trim()
  if (summary) {
    return summary
  }
  return '该网站暂未提供概要介绍。'
})

const normalizeTags = (tags: PublicWebsiteTagRef[] | undefined) => {
  if (!Array.isArray(tags)) {
    return [] as Array<{ id: number; name: string; color: string }>
  }
  const normalizedTagMap = new Map<number, { id: number; name: string; color: string }>()
  tags.forEach((tag) => {
    const id = Number(tag?.id)
    const name = String(tag?.name || '').trim()
    if (!Number.isInteger(id) || id <= 0 || !name || normalizedTagMap.has(id)) {
      return
    }
    normalizedTagMap.set(id, {
      id,
      name,
      color: normalizeTagColor(tag.color),
    })
  })
  return Array.from(normalizedTagMap.values())
}

const displayTags = computed(() => normalizeTags(detail.value?.tags))

const formatCount = (value?: number): string => {
  if (typeof value !== 'number' || Number.isNaN(value) || value < 0) {
    return '0'
  }
  return value.toLocaleString('zh-CN')
}

const scoreValue = computed(() => {
  const score = Number(detail.value?.score)
  if (!Number.isFinite(score)) {
    return 0
  }
  return Math.max(0, Math.min(5, score))
})

const scoreBadge = computed(() => scoreValue.value.toFixed(1))

const scoreGrade = computed(() => {
  if (scoreValue.value >= 4.5) {
    return 'A'
  }
  if (scoreValue.value >= 4) {
    return 'B'
  }
  if (scoreValue.value >= 3) {
    return 'C'
  }
  if (scoreValue.value > 0) {
    return 'D'
  }
  return '-'
})

const categoryName = computed(() => detail.value?.categoryName || '未分类')

const scoreCountText = computed(() => `${formatCount(detail.value?.scoreCount)} 人评分`)

const providerText = computed(() => {
  const providerName = detail.value?.providerName?.trim()
  if (providerName) {
    return providerName
  }
  const submitterId = detail.value?.submitterId
  if (submitterId === null || submitterId === undefined) {
    return '-'
  }
  return String(submitterId)
})

const formatDate = (dateStr?: string | null): string => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return dateStr

  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')

  return `${y}-${m}-${d} ${h}:${min}:${s}`
}

const shelfTimeText = computed(() => {
  const time = detail.value?.shelfTime || detail.value?.createTime
  return formatDate(time)
})

const updateTimeText = computed(() => formatDate(detail.value?.updateTime))

const showReportDialog = ref(false)

const showCollectDialog = ref(false)

const handleCollectClick = () => {
  showCollectDialog.value = true
}

const handleCollectConfirm = (_folderId: number, wid: number) => {
  console.log('收藏到文件夹成功:', wid)
}

const handleReportClick = () => {
  showReportDialog.value = true
}

const handleReportSubmit = (data: ReportFormData) => {
  console.log('举报数据:', data)
  // TODO: 调用后端API提交举报
  // 这里暂时只是打印数据
  alert('举报提交成功！我们会尽快处理您的举报。')
}

const resolveErrorMessage = (error: unknown, fallbackMessage: string): string => {
  if (error instanceof Error && error.message.trim()) {
    return error.message
  }
  return fallbackMessage
}

const loadDetail = async () => {
  if (!websiteId.value) {
    detail.value = null
    errorMessage.value = '网站ID不合法'
    return
  }

  const requestId = ++latestRequestId
  loading.value = true
  errorMessage.value = ''

  try {
    const detailData = await getWebsiteDetail(websiteId.value)
    if (requestId !== latestRequestId) {
      return
    }
    detail.value = detailData
  } catch (error) {
    if (requestId !== latestRequestId) {
      return
    }
    detail.value = null
    errorMessage.value = resolveErrorMessage(error, '加载网站详情失败')
  } finally {
    if (requestId === latestRequestId) {
      loading.value = false
    }
  }
}

watch(
  () => websiteId.value,
  () => {
    void loadDetail()
  },
  { immediate: true },
)
</script>

<template>
  <div class="min-h-screen bg-gray-50 text-gray-900 dark:bg-black dark:text-gray-100">
    <main class="mx-auto flex-grow w-full max-w-7xl px-4 sm:px-6 lg:px-8 pb-12 pt-8">
      <section
        v-if="loading"
        class="rounded border border-gray-200 bg-white px-6 py-12 text-center text-sm text-gray-500 dark:border-gray-800 dark:bg-neutral-950 dark:text-gray-300"
      >
        正在加载网站详情...
      </section>

      <section
        v-else-if="errorMessage"
        class="rounded border border-red-200 bg-red-50 px-6 py-12 text-center text-sm text-red-600 dark:border-red-900/50 dark:bg-red-900/20 dark:text-red-300"
      >
        {{ errorMessage }}
      </section>

      <template v-else-if="detail">
        <header class="mb-8 flex flex-col justify-between gap-6 md:flex-row md:items-start">
          <div class="flex items-start gap-4">
            <div
              class="flex h-16 w-16 flex-shrink-0 items-center justify-center overflow-hidden rounded-sm border border-gray-700 bg-slate-800"
            >
              <img
                v-if="detail.icon"
                :src="detail.icon"
                :alt="websiteName"
                class="h-full w-full object-cover"
              />
              <SquareTerminal v-else class="h-10 w-10 text-primary-500" />
            </div>

            <div>
              <div class="mb-1 flex flex-wrap items-center gap-3">
                <h1 class="text-3xl font-bold tracking-tight text-gray-900 dark:text-white">
                  {{ websiteName }}
                </h1>
                <span
                  v-if="detail.isOfficial === 1"
                  class="rounded border border-blue-500/30 bg-blue-500/20 px-2 py-0.5 text-xs font-medium text-blue-400"
                >
                  官方
                </span>
                <button
                  class="flex items-center gap-1.5 rounded border border-gray-300 px-3 py-1 text-sm text-gray-700 dark:border-gray-700 dark:text-gray-200 hover:border-primary-500 hover:text-primary-500 transition-colors cursor-pointer"
                  @click="handleCollectClick"
                >
                  <Heart class="h-4 w-4" />
                  收藏 {{ formatCount(detail.collectCount) }}
                </button>
                <div class="ml-2 flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400">
                  <button class="flex items-center gap-1.5 hover:text-primary-500 transition-colors cursor-pointer"
                    ><ThumbsUp class="h-4 w-4" /> {{ formatCount(detail.likeCount) }}</button
                  >
                  <span class="flex items-center gap-1.5"
                    ><Eye class="h-4 w-4" /> {{ formatCount(detail.clickCount) }}</span
                  >
                </div>
              </div>

              <div class="mt-3 flex flex-wrap items-center gap-2">

                <span
                  v-for="tag in displayTags"
                  :key="tag.id"
                  class="rounded px-2 py-0.5 text-[11px] font-medium border-none"
                  :style="{
                    backgroundColor: buildTagColorStyle(tag.color).backgroundColor,
                    color: buildTagColorStyle(tag.color).color
                  }"
                >
                  {{ tag.name }}
                </span>
                <span
                  v-if="displayTags.length === 0"
                  class="rounded border-none bg-gray-100 px-2 py-0.5 text-[11px] font-medium text-gray-500 dark:bg-gray-800 dark:text-gray-400"
                >
                  暂无标签
                </span>
              </div>
            </div>
          </div>

          <div class="flex flex-wrap gap-3">
            <div
              class="flex items-center gap-2 rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-600 dark:border-gray-700 dark:text-gray-300"
            >
              <BookOpen class="h-4 w-4" /> 分类 {{ categoryName }}
            </div>
            <div
              class="flex items-center gap-2 rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-600 dark:border-gray-700 dark:text-gray-300"
            >
              <Database class="h-4 w-4" /> 评分 {{ scoreBadge }} / 5.0
            </div>
            <button
              class="flex items-center gap-2 rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-600 dark:border-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors cursor-pointer"
              @click="handleReportClick"
            >
              <Flag class="h-4 w-4" /> 举报
            </button>
          </div>
        </header>

        <div class="mb-6 flex flex-col gap-4 border-b border-gray-200 dark:border-gray-800 md:flex-row md:items-end md:justify-between md:gap-0">
          <nav class="no-scrollbar order-2 flex gap-6 overflow-x-auto md:order-1">
            <button
              v-for="tab in tabs"
              :key="tab"
              class="cursor-pointer whitespace-nowrap border-b-2 pb-3 text-sm font-medium transition-colors duration-200"
              :class="
                activeTab === tab
                  ? 'border-primary-500 text-primary-500'
                  : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 dark:text-gray-400 dark:hover:border-gray-700 dark:hover:text-gray-300'
              "
              @click="activeTab = tab"
            >
              {{ tab }}
            </button>
          </nav>
          <div class="order-1 flex flex-wrap items-center gap-3 text-[11px] text-gray-500 sm:text-xs md:order-2 md:gap-4 md:pb-3 dark:text-gray-400">
            <span class="flex items-center gap-1.5 cursor-pointer hover:text-primary-500 transition-colors">
              提供者: <span class="text-primary-500">{{ providerText }}</span>
            </span>
            <span class="flex items-center gap-1.5"
              ><span class="h-2 w-2 rounded-full bg-blue-400"></span> 评分 {{ scoreCountText }}</span
            >
            <span
              v-if="detail?.isRecommend === 1"
              class="flex items-center gap-1 rounded px-2 py-1 sm:py-1.5 text-[11px] font-medium sm:text-xs"
              style="color: #e95322; background-color: rgba(233, 83, 34, 0.15);"
            >
              <img :src="recommendIcon" alt="管理员推荐" class="h-3.5 w-3.5 sm:h-4 sm:w-4" />
              管理员推荐
            </span>
          </div>
        </div>

        <div class="grid grid-cols-1 gap-8 lg:grid-cols-3">
          <div class="lg:col-span-2">
            <blockquote
              class="flat-design mb-6 w-full rounded-r border-l-4 border-primary-500 bg-primary-500/10 p-4 dark:bg-primary-500/20"
            >
              <p class="text-sm italic leading-relaxed text-primary-600 md:text-base dark:text-amber-400">
                {{ summaryText }}
              </p>
            </blockquote>

            <MarkdownViewer :content="markdownContent" />
          </div>

          <div class="flex flex-col gap-6 lg:col-span-1">
            <div class="flex flex-col gap-3">
              <a
                :href="websiteUrl || undefined"
                class="flat-design flex w-full cursor-pointer items-center justify-center gap-2 rounded bg-primary-500 px-4 py-3 font-bold text-white transition-colors hover:bg-primary-600"
                :class="!websiteUrl ? 'pointer-events-none opacity-60' : ''"
                target="_blank"
                rel="noopener noreferrer"
              >
                <Download class="h-5 w-5" />
                访问网站
              </a>
              <a
                :href="websiteUrl || undefined"
                class="flat-design flex w-full cursor-pointer items-center justify-center gap-2 rounded border border-gray-300 bg-gray-200 px-4 py-3 font-bold text-gray-900 transition-colors hover:bg-gray-300 dark:border-[#1e4d42] dark:bg-[#112d26] dark:text-gray-100 dark:hover:bg-[#163a32]"
                :class="!websiteUrl ? 'pointer-events-none opacity-60' : ''"
                target="_blank"
                rel="noopener noreferrer"
              >
                <Play class="h-5 w-5" />
                新窗口打开
              </a>
            </div>

            <div>
              <h4 class="mb-3 text-lg font-bold text-gray-900 dark:text-white">开发资源</h4>
              <ul class="space-y-4 text-sm">
                <li>
                  <div class="flex items-center gap-2">
                    <StarRating :score="scoreValue" size-class="h-4 w-4" />
                    <span class="text-gray-600 dark:text-gray-400 ml-1">{{ scoreBadge }} / 5.0</span>
                  </div>
                </li>
                <li>
                  <a
                    :href="websiteUrl || undefined"
                    class="flex max-w-full cursor-pointer items-center gap-2 text-gray-600 transition-colors hover:text-primary-500 dark:text-gray-400"
                    :class="!websiteUrl ? 'pointer-events-none opacity-60' : ''"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    <Globe class="h-4 w-4 flex-shrink-0" />
                    <span class="truncate">{{ websiteUrl || '访问地址' }}</span>
                  </a>
                </li>
                <li>
                  <span class="flex items-center gap-2 text-gray-600 dark:text-gray-400">
                    <Calendar class="h-4 w-4" /> 上架时间 {{ shelfTimeText }}
                  </span>
                </li>
                <li>
                  <span class="flex items-center gap-2 text-gray-600 dark:text-gray-400">
                    <History class="h-4 w-4" /> 更新时间 {{ updateTimeText }}
                  </span>
                </li>
              </ul>
            </div>

            <div
              class="flat-design rounded border border-gray-200 bg-white p-4 dark:border-gray-800 dark:bg-black"
            >
              <ul class="space-y-4">
                <li class="flex items-center gap-3 text-sm">
                  <div
                    class="flex h-5 w-5 items-center justify-center rounded-full border-2 border-green-500 text-[10px] font-bold text-green-500"
                  >
                    {{ scoreGrade }}
                  </div>
                  <span class="text-gray-700 dark:text-gray-300">综合评分 – {{ scoreBadge }} / 5.0</span>
                </li>
                <li class="flex items-center gap-3 text-sm">
                  <div
                    class="flex h-5 w-5 items-center justify-center rounded-full border-2 border-green-500 text-[10px] font-bold text-green-500"
                  >
                    A
                  </div>
                  <span class="text-gray-700 dark:text-gray-300"
                    >评分人数 – {{ formatCount(detail.scoreCount) }} 人</span
                  >
                </li>
                <li class="flex items-center gap-3 text-sm">
                  <div
                    class="flex h-5 w-5 items-center justify-center rounded-full border-2 border-green-500 text-[10px] font-bold text-green-500"
                  >
                    A
                  </div>
                  <span class="text-gray-700 dark:text-gray-300"
                    >互动热度 – {{ formatCount(detail.clickCount) }} 浏览</span
                  >
                </li>
              </ul>
              <div class="mt-4 border-t border-gray-200 pt-3 text-center dark:border-gray-800">
                <a
                  :href="websiteUrl || undefined"
                  class="cursor-pointer text-xs text-gray-500 underline hover:text-primary-500 dark:text-gray-400"
                  :class="!websiteUrl ? 'pointer-events-none opacity-60' : ''"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  查看网站原始地址
                </a>
              </div>
            </div>
          </div>
        </div>
      </template>
  </main>

  <!-- Report Dialog -->
  <ReportDialog
    v-model:visible="showReportDialog"
    :website-id="websiteId"
    :website-name="websiteName"
    @submit="handleReportSubmit"
  />

  <!-- Collect Folder Dialog -->
  <CollectFolderDialog
    v-model:visible="showCollectDialog"
    :website-id="websiteId"
    :website-name="websiteName"
    @confirm="handleCollectConfirm"
  />
</div>
</template>

<style scoped>
/* 隐藏滚动条但保留功能 */
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 扁平化设计 */
.flat-design {
  box-shadow: none !important;
}
</style>
