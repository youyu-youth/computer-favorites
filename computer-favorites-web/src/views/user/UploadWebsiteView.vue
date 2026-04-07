<template>
  <div class="min-h-screen bg-zinc-50 dark:bg-black text-zinc-900 dark:text-white selection:bg-amber-500/30 font-sans p-4 sm:p-8 md:p-12 lg:p-16 transition-colors">
    <div class="max-w-6xl mx-auto mt-[-2rem] md:mt-[-4rem]">

      <!-- Main Heading -->
      <div class="mb-12 mt-8 grid grid-cols-1 md:grid-cols-12 gap-8 items-end">
        <div class="md:col-span-8">
          <h1 class="text-4xl md:text-5xl font-bold tracking-tight leading-snug">
            {{ isEditMode ? '编辑' : '上传' }}
            <span class="text-amber-500">网站</span>
          </h1>
          <p class="mt-4 text-sm text-zinc-500 dark:text-zinc-400">
            {{ isEditMode ? '仅待审核投稿可编辑，更新后继续进入审核流程。' : '填写网站信息并提交审核。' }}
          </p>
        </div>

        <div class="md:col-span-4 flex items-center justify-start md:justify-end">
          <button
            type="button"
            class="cursor-pointer inline-flex items-center gap-2 border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 px-4 py-2 text-sm text-zinc-700 dark:text-zinc-200 hover:border-amber-500 hover:text-amber-500 transition-colors rounded-none"
            @click="goBack"
          >
            <ArrowLeft class="h-4 w-4" />
            返回上页
          </button>
        </div>

      </div>

      <!-- Form Content -->
      <form @submit.prevent="handleSubmit" class="relative">
        <div
          v-if="isSubmissionDetailLoading"
          class="mb-6 rounded-sm border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700 dark:border-amber-900/50 dark:bg-amber-950/20 dark:text-amber-200"
        >
          正在加载投稿详情，请稍候...
        </div>

        <!-- Ambient Glow -->
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-2xl aspect-square bg-amber-500/5 rounded-full blur-[120px] pointer-events-none"></div>

        <div class="grid grid-cols-1 lg:grid-cols-12 gap-x-16 gap-y-12 pb-32">

          <!-- Left Column (Core Metadata) -->
          <div class="lg:col-span-5 space-y-10 relative z-10">

            <div class="group">
              <label class="flex items-center gap-2 text-xs font-mono text-zinc-500 mb-3 uppercase tracking-wider group-focus-within:text-amber-500 transition-colors">
                <span class="w-1.5 h-1.5 rounded-full bg-zinc-300 dark:bg-zinc-700 group-focus-within:bg-amber-500 transition-colors"></span>
                网站名称
              </label>
              <input
                v-model="form.name"
                type="text"
                required
                class="w-full bg-transparent border-0 border-b-2 border-zinc-200 dark:border-zinc-800 pb-3 text-2xl font-light text-zinc-900 dark:text-white placeholder-zinc-400 dark:placeholder-zinc-800 focus:ring-0 focus:border-amber-500 transition-colors outline-none"
                placeholder="例如: 电脑收藏夹"
              />
            </div>

            <div class="group">
              <label class="flex items-center gap-2 text-xs font-mono text-zinc-500 mb-3 uppercase tracking-wider group-focus-within:text-amber-500 transition-colors">
                <span class="w-1.5 h-1.5 rounded-full bg-zinc-300 dark:bg-zinc-700 group-focus-within:bg-amber-500 transition-colors"></span>
                网站地址
              </label>
              <input
                v-model="form.url"
                type="url"
                required
                class="w-full bg-transparent border-0 border-b-2 border-zinc-200 dark:border-zinc-800 pb-3 text-xl font-light text-zinc-900 dark:text-white placeholder-zinc-400 dark:placeholder-zinc-800 focus:ring-0 focus:border-amber-500 transition-colors outline-none font-mono"
                placeholder="https://..."
              />
            </div>

            <div class="grid grid-cols-2 gap-8">
              <div class="group">
                <label class="flex items-center gap-2 text-xs font-mono text-zinc-500 mb-3 uppercase tracking-wider group-focus-within:text-amber-500 transition-colors">
                  <span class="w-1.5 h-1.5 rounded-full bg-zinc-300 dark:bg-zinc-700 group-focus-within:bg-amber-500 transition-colors"></span>
                  网站图标
                </label>
                <AppImageUploadField
                  variant="minimal"
                  label=""
                  :maxFileSize="MAX_ICON_FILE_SIZE"
                  chooseLabel="上传图标"
                  chooseIcon="fas fa-image"
                  helperText="支持 JPG / PNG / WebP，文件不超过 1MB。"
                  uploadingText="上传中..."
                  deletingText="清理中..."
                  clearText="移除"
                  previewFallbackName="website-icon"
                  previewAlt="网站图标预览"
                  :logoUrl="form.icon"
                  :fileName="iconFileName"
                  :objectKey="iconObjectKey"
                  :uploading="isIconUploading"
                  :deleting="isIconDeleting"
                  :submitting="isSubmitting"
                  @upload="handleIconUpload"
                  @clear="clearIcon"
                />
              </div>

              <div class="group">
                <label class="flex items-center gap-2 text-xs font-mono text-zinc-500 mb-3 uppercase tracking-wider group-focus-within:text-amber-500 transition-colors">
                  <span class="w-1.5 h-1.5 rounded-full bg-zinc-300 dark:bg-zinc-700 group-focus-within:bg-amber-500 transition-colors"></span>
                  代码仓库
                </label>
                <div class="relative">
                  <div class="absolute left-0 bottom-3 text-zinc-400 dark:text-zinc-600">
                    <Github class="w-5 h-5" />
                  </div>
                  <input
                    v-model="form.githubUrl"
                    type="url"
                    class="w-full bg-transparent border-0 border-b-2 border-zinc-200 dark:border-zinc-800 pl-8 pb-3 text-sm font-light text-zinc-900 dark:text-white placeholder-zinc-400 dark:placeholder-zinc-800 focus:ring-0 focus:border-amber-500 transition-colors outline-none"
                    placeholder="GitHub 地址(选填)"
                  />
                </div>
              </div>
            </div>

            <div class="group">
              <label class="flex items-center gap-2 text-xs font-mono text-zinc-500 mb-3 uppercase tracking-wider group-focus-within:text-amber-500 transition-colors">
                <span class="w-1.5 h-1.5 rounded-full bg-zinc-300 dark:bg-zinc-700 group-focus-within:bg-amber-500 transition-colors"></span>
                分类与标签
              </label>
              <div class="grid grid-cols-2 gap-4">
                <div class="relative">
                  <AppWebsiteCategorySelect
                    v-model="form.categoryId"
                    :options="categoryOptions"
                    placeholder="所属分类"
                    filterPlaceholder="输入分类名进行搜索"
                    emptyText="暂无分类选项"
                    variant="minimal"
                    :disabled="isCategoryLoading"
                  />
                </div>
                <div class="relative">
                  <button
                    type="button"
                    :disabled="isTagLoading"
                    class="w-full cursor-pointer inline-flex items-center justify-center gap-2 bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 py-3 px-4 text-sm font-medium text-zinc-700 dark:text-zinc-200 hover:border-amber-500 hover:text-amber-500 transition-colors rounded-none disabled:cursor-not-allowed disabled:opacity-60"
                    @click="openTagDialog"
                  >
                    <Plus class="h-4 w-4" />
                    {{ isTagLoading ? '标签加载中...' : '添加标签' }}
                  </button>
                </div>
              </div>

              <div class="mt-4">
                <div class="flex items-center justify-between">
                  <p class="text-xs font-mono text-zinc-500">
                    已选标签 ({{ selectedTags.length }}/{{ MAX_SELECTED_TAGS }})
                  </p>
                  <button
                    v-if="selectedTags.length > 0"
                    type="button"
                    class="text-xs text-zinc-500 hover:text-amber-500 transition-colors cursor-pointer"
                    @click="selectedTags = []"
                  >
                    清空
                  </button>
                </div>

                <div v-if="selectedTags.length > 0" class="mt-2 flex flex-wrap gap-2">
                  <span
                    v-for="tag in selectedTags"
                    :key="tag.id"
                    class="inline-flex items-center gap-1.5 border-l-2 px-2.5 py-1 text-xs"
                    :style="buildTagColorStyle(tag.color)"
                  >
                    <span>{{ tag.name }}</span>
                    <button
                      type="button"
                      class="cursor-pointer text-current/80 hover:text-current transition-colors"
                      @click="removeSelectedTag(tag.id)"
                      :aria-label="`删除标签 ${tag.name}`"
                    >
                      <X class="h-3.5 w-3.5" />
                    </button>
                  </span>
                </div>

                <p v-else class="mt-2 text-xs text-zinc-400 dark:text-zinc-500">
                  暂未添加标签，点击“添加标签”进行选择。
                </p>
              </div>
            </div>

            <div class="group">
              <label class="flex items-center gap-2 text-xs font-mono text-zinc-500 mb-3 uppercase tracking-wider group-focus-within:text-amber-500 transition-colors">
                <span class="w-1.5 h-1.5 rounded-full bg-zinc-300 dark:bg-zinc-700 group-focus-within:bg-amber-500 transition-colors"></span>
                简介摘要
              </label>
              <textarea
                v-model="form.shortDescription"
                required
                rows="3"
                class="w-full bg-white dark:bg-zinc-900/50 border border-zinc-200 dark:border-zinc-800 p-4 text-sm font-light text-zinc-700 dark:text-zinc-300 placeholder-zinc-400 dark:placeholder-zinc-700 focus:ring-0 focus:border-amber-500 transition-colors outline-none rounded-none resize-none"
                placeholder="一两句话简要描述即可。"
              ></textarea>
            </div>

          </div>

          <!-- Right Column (Markdown Description) -->
          <div class="lg:col-span-7 flex flex-col relative z-10">
            <div class="group h-full flex flex-col">
              <label class="flex items-center justify-between text-xs font-mono text-zinc-500 mb-3 uppercase tracking-wider group-focus-within:text-amber-500 transition-colors">
                <span class="flex items-center gap-2">
                  <span class="w-1.5 h-1.5 rounded-full bg-zinc-300 dark:bg-zinc-700 group-focus-within:bg-amber-500 transition-colors"></span>
                  详细文档
                </span>
                <span class="text-zinc-500 dark:text-zinc-700 text-[10px]">支持 MARKDOWN 格式</span>
              </label>
              <div class="flex-grow border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 p-1 group-focus-within:border-amber-500/50 transition-colors min-h-[400px] mb-6">
                <UVditor
                  v-model="form.description"
                  class="h-full w-full"
                />
              </div>

              <!-- Submit Button placed right under the editor -->
              <div class="flex justify-end">
                <button
                  type="submit"
                  :disabled="isSubmitting || isSubmissionDetailLoading"
                  class="group cursor-pointer relative inline-flex items-center justify-center px-8 py-3.5 font-mono font-bold text-white dark:text-black bg-amber-500 overflow-hidden transition-all hover:bg-amber-400 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:ring-offset-2 focus:ring-offset-zinc-50 dark:focus:ring-offset-black rounded-sm disabled:cursor-not-allowed disabled:opacity-70"
                >
                  <span class="absolute w-0 h-0 transition-all duration-500 ease-out bg-white rounded-full group-hover:w-56 group-hover:h-56 opacity-20"></span>
                  <span class="relative flex items-center gap-3">
                    {{
                      isSubmitting
                        ? isEditMode
                          ? '更新中...'
                          : '提交中...'
                        : isEditMode
                          ? '更新投稿'
                          : '提交网站'
                    }}
                    <ArrowRight class="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                  </span>
                </button>
              </div>
            </div>
          </div>

        </div>

      </form>

      <Dialog
        v-model:visible="isTagDialogVisible"
        modal
        header="选择相关标签"
        :draggable="false"
        :pt="tagDialogPt"
      >
        <div class="p-4 border-b border-zinc-200 dark:border-zinc-800">
          <div class="relative">
            <Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-zinc-400" />
            <input
              v-model="tagKeyword"
              type="text"
              placeholder="搜索标签"
              class="w-full bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 py-2 pl-9 pr-3 text-sm text-zinc-900 dark:text-zinc-100 placeholder-zinc-400 dark:placeholder-zinc-600 focus:border-amber-500 outline-none transition-colors"
            />
          </div>
          <p class="mt-2 text-xs text-zinc-500">最多可选择 {{ MAX_SELECTED_TAGS }} 个标签</p>
        </div>

        <div class="max-h-[320px] overflow-y-auto p-4">
          <p v-if="isTagLoading" class="py-10 text-center text-sm text-zinc-500">标签加载中...</p>
          <div v-else-if="filteredTagOptions.length > 0" class="flex flex-wrap gap-2">
            <button
              v-for="tag in filteredTagOptions"
              :key="tag.id"
              type="button"
              class="cursor-pointer inline-flex items-center gap-1.5 border px-2.5 py-1.5 text-xs transition-colors"
              :class="
                isTagSelected(tag.id)
                  ? 'border-amber-500 bg-amber-50 text-amber-700 dark:bg-amber-500/20 dark:text-amber-300'
                  : 'border-zinc-200 text-zinc-600 hover:border-amber-500 hover:text-amber-500 dark:border-zinc-700 dark:text-zinc-300 dark:hover:border-amber-500 dark:hover:text-amber-300'
              "
              @click="toggleTagSelection(tag)"
            >
              <span
                class="h-2 w-2 rounded-full"
                :style="{ backgroundColor: normalizeTagColor(tag.color) }"
              ></span>
              <span>{{ tag.name }}</span>
              <Check v-if="isTagSelected(tag.id)" class="h-3.5 w-3.5" />
            </button>
          </div>

          <p v-else class="py-10 text-center text-sm text-zinc-500">暂无匹配标签</p>
        </div>

        <div class="flex justify-end gap-2 border-t border-zinc-200 dark:border-zinc-800 p-4">
          <button
            type="button"
            class="cursor-pointer px-4 py-2 text-sm text-zinc-600 dark:text-zinc-300 border border-zinc-200 dark:border-zinc-800 hover:text-amber-500 hover:border-amber-500 transition-colors"
            @click="isTagDialogVisible = false"
          >
            完成
          </button>
        </div>
      </Dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import Dialog from 'primevue/dialog'
import type { FileUploadUploaderEvent } from 'primevue/fileupload'
import { computed, onMounted, ref } from 'vue'
import { ArrowLeft, ArrowRight, Check, Github, Plus, Search, X } from 'lucide-vue-next'
import { useRoute, useRouter } from 'vue-router'
import { getWebsiteCategories, getWebsiteTags } from '@/api/website'
import {
  deleteUserSubmissionIcon,
  getMyWebsiteSubmissionDetail,
  submitUserWebsite,
  updateMyWebsiteSubmission,
  uploadUserSubmissionIcon,
} from '@/api/user-website-submission'
import { useToast } from '@/composables/useToast'
import AppImageUploadField from '@/components/common/AppImageUploadField.vue'
import AppWebsiteCategorySelect from '@/components/common/AppWebsiteCategorySelect.vue'
import UVditor from '@/components/ui-adapter/UVditor.vue'
import { buildTagColorStyle, normalizeTagColor } from '@/utils/tag-color'
import type { PublicWebsiteCategory, PublicWebsiteTagRef } from '@/types/public-website'

type UploadTagOption = {
  id: number
  name: string
  color: string
}

const { add: showToast } = useToast()
const route = useRoute()
const router = useRouter()

const categoryOptions = ref<PublicWebsiteCategory[]>([])
const isCategoryLoading = ref(false)
const isTagLoading = ref(false)
const isSubmitting = ref(false)
const isSubmissionDetailLoading = ref(false)
const isIconUploading = ref(false)
const isIconDeleting = ref(false)
const MAX_SELECTED_TAGS = 3
const MAX_ICON_FILE_SIZE = 1024 * 1024

const form = ref({
  name: '',
  url: '',
  icon: '',
  githubUrl: '',
  categoryId: null as number | null,
  shortDescription: '',
  description: ''
})

const isTagDialogVisible = ref(false)
const tagKeyword = ref('')
const selectedTags = ref<UploadTagOption[]>([])
const tagOptions = ref<UploadTagOption[]>([])
const iconFileName = ref('')
const iconObjectKey = ref('')

const editingSubmissionId = computed<number | null>(() => {
  const rawSubmissionId = route.query.submissionId
  const normalizedSubmissionId = Array.isArray(rawSubmissionId)
    ? rawSubmissionId[0]
    : rawSubmissionId
  const parsedSubmissionId = Number(normalizedSubmissionId)
  if (!Number.isInteger(parsedSubmissionId) || parsedSubmissionId <= 0) {
    return null
  }
  return parsedSubmissionId
})

const isEditMode = computed(() => editingSubmissionId.value !== null)

const filteredTagOptions = computed(() => {
  const keyword = tagKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return tagOptions.value
  }
  return tagOptions.value.filter((tag) => tag.name.toLowerCase().includes(keyword))
})

const tagDialogPt = {
  mask: {
    class: 'bg-black/45 backdrop-blur-[1px] z-[120]',
  },
  root: {
    class:
      'w-[min(94vw,560px)] rounded-none border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 shadow-[0_16px_36px_rgba(15,23,42,0.28)] dark:shadow-[0_22px_44px_rgba(2,6,23,0.62)]',
  },
  header: {
    class:
      'border-b border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950 px-5 py-4 text-zinc-900 dark:text-zinc-100',
  },
  title: {
    class: 'text-sm font-semibold tracking-wide',
  },
  content: {
    class: 'p-0 bg-white dark:bg-zinc-900',
  },
  closeButton: {
    class:
      'h-8 w-8 rounded-none text-zinc-500 hover:bg-zinc-100 hover:text-zinc-700 dark:text-zinc-400 dark:hover:bg-zinc-800 dark:hover:text-zinc-200 cursor-pointer transition-colors',
  },
}

const resolveErrorMessage = (error: unknown, fallbackMessage: string) => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallbackMessage
}

const loadCategoryOptions = async () => {
  isCategoryLoading.value = true
  try {
    categoryOptions.value = await getWebsiteCategories()
  } catch (error) {
    showToast({
      type: 'error',
      title: resolveErrorMessage(error, '网站分类加载失败，请稍后重试'),
    })
  } finally {
    isCategoryLoading.value = false
  }
}

const loadTagOptions = async () => {
  isTagLoading.value = true
  try {
    const tags = await getWebsiteTags()
    tagOptions.value = tags.map((tag) => ({
      id: Number(tag.id),
      name: tag.name,
      color: normalizeTagColor(tag.color),
    }))
  } catch (error) {
    showToast({
      type: 'error',
      title: resolveErrorMessage(error, '网站标签加载失败，请稍后重试'),
    })
  } finally {
    isTagLoading.value = false
  }
}

const openTagDialog = () => {
  isTagDialogVisible.value = true
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  void router.push('/computer/home')
}

const isTagSelected = (tagId: number) => {
  return selectedTags.value.some((tag) => tag.id === tagId)
}

const toggleTagSelection = (tag: UploadTagOption) => {
  const selectedIndex = selectedTags.value.findIndex((item) => item.id === tag.id)
  if (selectedIndex >= 0) {
    selectedTags.value.splice(selectedIndex, 1)
    return
  }

  if (selectedTags.value.length >= MAX_SELECTED_TAGS) {
    showToast({ type: 'warning', title: `最多选择 ${MAX_SELECTED_TAGS} 个标签` })
    return
  }

  selectedTags.value.push(tag)
}

const removeSelectedTag = (tagId: number) => {
  selectedTags.value = selectedTags.value.filter((tag) => tag.id !== tagId)
}

const extractFileNameFromUrl = (url: string) => {
  if (!url.trim()) {
    return ''
  }

  const normalizedUrl = (url.trim().split('?')[0] || '').trim()
  if (!normalizedUrl) {
    return ''
  }
  const pathSegments = normalizedUrl.split('/')
  return pathSegments[pathSegments.length - 1] || ''
}

const buildSelectedTagsFromDetail = (tags?: PublicWebsiteTagRef[]) => {
  if (!Array.isArray(tags) || tags.length === 0) {
    return [] as UploadTagOption[]
  }

  const tagOptionMap = new Map<number, UploadTagOption>()
  tagOptions.value.forEach((tag) => {
    tagOptionMap.set(tag.id, tag)
  })

  return tags
    .map((tag) => {
      const tagId = Number(tag.id)
      if (!Number.isInteger(tagId) || tagId <= 0) {
        return null
      }

      const matchedTag = tagOptionMap.get(tagId)
      if (matchedTag) {
        return matchedTag
      }

      return {
        id: tagId,
        name: tag.name,
        color: normalizeTagColor(tag.color),
      }
    })
    .filter((tag): tag is UploadTagOption => tag !== null)
}

const loadSubmissionDetail = async () => {
  if (!isEditMode.value || !editingSubmissionId.value) {
    return
  }

  isSubmissionDetailLoading.value = true
  try {
    const detail = await getMyWebsiteSubmissionDetail(editingSubmissionId.value)
    if (detail.auditStatus !== 0) {
      showToast({
        type: 'warning',
        title: '仅待审核的投稿可编辑',
      })
      await router.push({ name: 'websiteSubmissions' })
      return
    }

    form.value = {
      name: detail.name?.trim() || '',
      url: detail.url?.trim() || '',
      icon: detail.icon?.trim() || '',
      githubUrl: detail.githubUrl?.trim() || '',
      categoryId: detail.categoryId ? Number(detail.categoryId) : null,
      shortDescription: detail.summary?.trim() || '',
      description: detail.description || '',
    }
    selectedTags.value = buildSelectedTagsFromDetail(detail.tags)
    iconObjectKey.value = ''
    iconFileName.value = detail.icon ? extractFileNameFromUrl(detail.icon) : ''
  } catch (error) {
    showToast({
      type: 'error',
      title: resolveErrorMessage(error, '投稿详情加载失败，请稍后重试'),
    })
    await router.push({ name: 'websiteSubmissions' })
  } finally {
    isSubmissionDetailLoading.value = false
  }
}

const clearIconLocalState = () => {
  form.value.icon = ''
  iconFileName.value = ''
  iconObjectKey.value = ''
}

const clearIcon = async () => {
  if (isIconUploading.value || isIconDeleting.value) {
    return
  }

  const currentObjectKey = iconObjectKey.value
  if (!currentObjectKey) {
    clearIconLocalState()
    return
  }

  isIconDeleting.value = true
  try {
    await deleteUserSubmissionIcon(currentObjectKey)
    clearIconLocalState()
    showToast({ type: 'success', title: '网站图标已移除' })
  } catch (error) {
    showToast({
      type: 'error',
      title: resolveErrorMessage(error, '图标清理失败，请稍后重试'),
    })
  } finally {
    isIconDeleting.value = false
  }
}

const handleIconUpload = async (event: FileUploadUploaderEvent) => {
  const selectedFile = Array.isArray(event.files) ? event.files[0] : event.files
  if (!selectedFile) {
    return
  }

  if (!selectedFile.type.startsWith('image/')) {
    showToast({ type: 'warning', title: '仅支持图片格式文件' })
    return
  }

  if (selectedFile.size > MAX_ICON_FILE_SIZE) {
    showToast({ type: 'warning', title: '图标文件不能超过 1MB' })
    return
  }

  if (isIconDeleting.value) {
    showToast({ type: 'warning', title: '图标正在清理中，请稍后重试' })
    return
  }

  if (iconObjectKey.value) {
    isIconDeleting.value = true
    try {
      await deleteUserSubmissionIcon(iconObjectKey.value)
      clearIconLocalState()
    } catch (error) {
      showToast({
        type: 'error',
        title: resolveErrorMessage(error, '旧图标清理失败，请稍后重试'),
      })
      return
    } finally {
      isIconDeleting.value = false
    }
  }

  isIconUploading.value = true
  try {
    const uploadResult = await uploadUserSubmissionIcon(selectedFile)
    form.value.icon = uploadResult.iconUrl
    iconObjectKey.value = uploadResult.objectKey
    iconFileName.value = selectedFile.name
    showToast({ type: 'success', title: '网站图标上传成功' })
  } catch (error) {
    clearIconLocalState()
    showToast({
      type: 'error',
      title: resolveErrorMessage(error, '网站图标上传失败，请稍后重试'),
    })
  } finally {
    isIconUploading.value = false
  }
}

const handleSubmit = async () => {
  if (isSubmitting.value) {
    return
  }

  if (isSubmissionDetailLoading.value) {
    showToast({ type: 'warning', title: '投稿详情加载中，请稍后再试' })
    return
  }

  if (isIconUploading.value || isIconDeleting.value) {
    showToast({ type: 'warning', title: '图标处理进行中，请稍后再提交' })
    return
  }

  if (!form.value.categoryId) {
    showToast({ type: 'warning', title: '请选择所属分类' })
    return
  }

  if (!form.value.icon.trim()) {
    showToast({ type: 'warning', title: '请上传网站图标' })
    return
  }

  const payload = {
    name: form.value.name.trim(),
    url: form.value.url.trim(),
    icon: form.value.icon.trim(),
    githubUrl: form.value.githubUrl.trim(),
    summary: form.value.shortDescription.trim(),
    description: form.value.description,
    categoryId: form.value.categoryId,
    tags: selectedTags.value.map((tag) => tag.id).join(','),
  }

  isSubmitting.value = true
  try {
    if (isEditMode.value && editingSubmissionId.value) {
      await updateMyWebsiteSubmission(editingSubmissionId.value, payload)
      showToast({
        type: 'success',
        title: '投稿更新成功，已返回投稿列表',
      })
      await router.push({ name: 'websiteSubmissions' })
      return
    }

    const websiteId = await submitUserWebsite(payload)
    showToast({
      type: 'success',
      title: '投稿提交成功，已进入审核队列',
      description: `投稿编号 #${websiteId}`,
    })

    form.value = {
      name: '',
      url: '',
      icon: '',
      githubUrl: '',
      categoryId: null,
      shortDescription: '',
      description: '',
    }
    selectedTags.value = []
    iconFileName.value = ''
    iconObjectKey.value = ''
    isTagDialogVisible.value = false
    tagKeyword.value = ''
  } catch (error) {
    showToast({
      type: 'error',
      title: resolveErrorMessage(error, isEditMode.value ? '投稿更新失败，请稍后重试' : '投稿提交失败，请稍后重试'),
    })
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  void (async () => {
    await Promise.all([loadCategoryOptions(), loadTagOptions()])
    await loadSubmissionDetail()
  })()
})
</script>

<style scoped>
/* Apply specific styles for Vditor inside the brutalist layout */
:deep(.vditor) {
  --panel-background-color: transparent !important;
  --textarea-background-color: transparent !important;
  --toolbar-background-color: #09090b !important;
  border: none !important;
}

:deep(.vditor-toolbar) {
  border-bottom: 1px solid #27272a !important;
  padding: 8px !important;
}

:deep(.vditor-content) {
  background-color: transparent !important;
}
</style>
