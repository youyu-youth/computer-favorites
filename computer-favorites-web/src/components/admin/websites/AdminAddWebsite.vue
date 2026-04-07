<script setup lang="ts">
import type { FileUploadUploaderEvent } from 'primevue/fileupload'
import AppImageUploadField from '@/components/common/AppImageUploadField.vue'
import AppWebsiteCategorySelect from '@/components/common/AppWebsiteCategorySelect.vue'
import UVditor from '@/components/ui-adapter/UVditor.vue'
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useAdminNavStore } from '@/stores/adminNav'
import { useToast } from '@/composables/useToast'
import {
  createAdminWebsite,
  deleteAdminWebsiteLogo,
  getAdminWebsiteCategories,
  getAdminWebsiteDetail,
  updateAdminWebsite,
  uploadAdminWebsiteLogo,
} from '@/api/admin-website'
import type {
  AdminWebsiteCreatePayload,
  AdminWebsiteDetail,
  AdminWebsiteEditPayload,
  DeletedFilterValue,
} from '@/types/admin-website'

const props = withDefaults(
  defineProps<{
    mode?: 'add' | 'edit'
    websiteId?: number | null
  }>(),
  {
    mode: 'add',
    websiteId: null,
  },
)

const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'submit', data: AdminWebsiteCreatePayload | AdminWebsiteEditPayload): void
}>()

const adminNavStore = useAdminNavStore()
const { websiteCategories } = storeToRefs(adminNavStore)
const { add: showToast } = useToast()

const ALL_DELETED_FILTER: DeletedFilterValue = -1
const LOGO_OBJECT_KEY_PREFIX = 'admin/website/logo/'

const isEditMode = computed(() => props.mode === 'edit')

const pageCommand = computed(() => {
  return isEditMode.value ? './edit_website.sh' : './add_website.sh'
})

const pageTitle = computed(() => {
  return isEditMode.value ? '修改网站' : '添加新网站'
})

const pageDescription = computed(() => {
  if (isEditMode.value) {
    return '管理员可在此修改网站基础信息与运营信息。保存后会自动刷新修改时间并回显最新数据。'
  }
  return '管理员手动录入系统收录的推荐站点。请确保 URL 的有效性及分类归属的准确。提交后将默认标记为“已自动审核”。'
})

const validCategories = computed(() => {
  return websiteCategories.value.filter((c) => c.id !== 0)
})

const formData = ref<{
  name: string
  url: string
  icon: string
  summary: string
  description: string
  categoryId: number | ''
  tags: string
  isTop: boolean
  isRecommend: boolean
  sort: number
}>({
  name: '',
  url: '',
  icon: '',
  summary: '',
  description: '',
  categoryId: '',
  tags: '',
  isTop: false,
  isRecommend: false,
  sort: 0,
})

const MAX_LOGO_FILE_SIZE = 1024 * 1024
const isSubmitting = ref(false)
const isLogoUploading = ref(false)
const isLogoDeleting = ref(false)
const isDetailLoading = ref(false)
const logoFileName = ref('')
const logoObjectKey = ref('')
const originalLogoObjectKey = ref('')

const resolveErrorMessage = (error: unknown, fallbackMessage: string) => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallbackMessage
}

const resolveFileNameFromUrl = (url: string) => {
  if (!url) {
    return ''
  }
  const normalizedUrl = url.split('?')[0] ?? ''
  const fileName = normalizedUrl.substring(normalizedUrl.lastIndexOf('/') + 1)
  try {
    return decodeURIComponent(fileName)
  } catch {
    return fileName
  }
}

const extractObjectKeyFromLogoUrl = (url: string) => {
  if (!url) {
    return ''
  }
  const normalizedUrl = url.split('?')[0] ?? ''
  const markerIndex = normalizedUrl.indexOf(LOGO_OBJECT_KEY_PREFIX)
  if (markerIndex < 0) {
    return ''
  }

  const objectKey = normalizedUrl.substring(markerIndex)
  try {
    return decodeURIComponent(objectKey)
  } catch {
    return objectKey
  }
}

const clearLogoLocalState = () => {
  formData.value.icon = ''
  logoFileName.value = ''
  logoObjectKey.value = ''
}

const deleteLogoByObjectKey = async (objectKey: string) => {
  if (!objectKey) {
    return
  }
  await deleteAdminWebsiteLogo(objectKey)
}

const resetLogo = () => {
  clearLogoLocalState()
}

const clearLogo = async () => {
  const currentObjectKey = logoObjectKey.value

  if (isEditMode.value) {
    if (currentObjectKey && currentObjectKey !== originalLogoObjectKey.value) {
      isLogoDeleting.value = true
      try {
        await deleteLogoByObjectKey(currentObjectKey)
      } catch (error) {
        showToast({ type: 'error', title: resolveErrorMessage(error, 'Logo 删除失败，请稍后重试') })
        return
      } finally {
        isLogoDeleting.value = false
      }
    }

    resetLogo()
    return
  }

  if (!currentObjectKey) {
    resetLogo()
    return
  }

  isLogoDeleting.value = true
  try {
    await deleteLogoByObjectKey(currentObjectKey)
    resetLogo()
    showToast({ type: 'success', title: 'Logo 已删除' })
  } catch (error) {
    showToast({ type: 'error', title: resolveErrorMessage(error, 'Logo 删除失败，请稍后重试') })
  } finally {
    isLogoDeleting.value = false
  }
}

const handleCancel = async () => {
  if (isEditMode.value) {
    if (logoObjectKey.value && logoObjectKey.value !== originalLogoObjectKey.value) {
      try {
        await deleteLogoByObjectKey(logoObjectKey.value)
      } catch {
        showToast({ type: 'warning', title: '临时Logo清理失败，可稍后手动处理' })
      }
    }
    emit('cancel')
    return
  }

  await clearLogo()
  emit('cancel')
}

const handleLogoUpload = async (event: FileUploadUploaderEvent) => {
  const selectedFile = Array.isArray(event.files) ? event.files[0] : event.files
  if (!selectedFile) {
    return
  }

  if (!selectedFile.type.startsWith('image/')) {
    showToast({ type: 'warning', title: '仅支持图片格式的 Logo 文件' })
    return
  }

  if (selectedFile.size > MAX_LOGO_FILE_SIZE) {
    showToast({ type: 'warning', title: 'Logo 文件不能超过 1MB' })
    return
  }

  if (isLogoDeleting.value) {
    showToast({ type: 'warning', title: 'Logo 正在删除，请稍后重试' })
    return
  }

  if (logoObjectKey.value && logoObjectKey.value !== originalLogoObjectKey.value) {
    isLogoDeleting.value = true
    try {
      await deleteLogoByObjectKey(logoObjectKey.value)
      resetLogo()
    } catch (error) {
      showToast({ type: 'error', title: resolveErrorMessage(error, '旧Logo清理失败，请稍后重试') })
      return
    } finally {
      isLogoDeleting.value = false
    }
  }

  if (!isEditMode.value && logoObjectKey.value) {
    await clearLogo()
  }

  isLogoUploading.value = true
  try {
    const uploadResult = await uploadAdminWebsiteLogo(selectedFile)
    logoFileName.value = selectedFile.name
    logoObjectKey.value = uploadResult.objectKey
    formData.value.icon = uploadResult.logoUrl
    showToast({
      type: 'success',
      title: 'Logo 上传成功',
    })
  } catch (error) {
    resetLogo()
    showToast({
      type: 'error',
      title: resolveErrorMessage(error, 'Logo 上传失败，请稍后重试'),
    })
  } finally {
    isLogoUploading.value = false
  }
}

const selectedCategoryId = computed<number | null>({
  get: () => {
    return formData.value.categoryId === '' ? null : formData.value.categoryId
  },
  set: (value) => {
    formData.value.categoryId = value ?? ''
  },
})

const buildCreatePayload = (): AdminWebsiteCreatePayload => {
  return {
    name: formData.value.name.trim(),
    url: formData.value.url.trim(),
    icon: formData.value.icon.trim() || undefined,
    summary: formData.value.summary.trim() || undefined,
    description: formData.value.description.trim() || undefined,
    categoryId: Number(formData.value.categoryId),
    tags: formData.value.tags.trim() || undefined,
    isTop: formData.value.isTop,
    isRecommend: formData.value.isRecommend,
    sort: Number.isFinite(formData.value.sort) ? formData.value.sort : 0,
  }
}

const loadCategoriesIfNeed = async () => {
  if (websiteCategories.value.length > 1) {
    return
  }

  const categoryList = await getAdminWebsiteCategories(ALL_DELETED_FILTER)
  const totalCount = categoryList.reduce((sum, item) => sum + Number(item.count || 0), 0)
  const mappedList = [
    {
      id: 0,
      name: '全部分类',
      count: totalCount,
    },
    ...categoryList.map((item) => ({
      id: item.id,
      name: item.name,
      count: Number(item.count || 0),
    })),
  ]
  adminNavStore.setWebsiteCategories(mappedList)
}

const mapDetailToForm = (detail: AdminWebsiteDetail) => {
  formData.value.name = detail.name || ''
  formData.value.url = detail.url || ''
  formData.value.icon = (detail.icon || '').trim()
  formData.value.summary = detail.summary || ''
  formData.value.description = detail.description || ''
  formData.value.categoryId = typeof detail.categoryId === 'number' ? detail.categoryId : ''
  formData.value.tags = detail.tags || ''
  formData.value.isTop = detail.isTop === 1
  formData.value.isRecommend = detail.isRecommend === 1
  formData.value.sort = Number.isFinite(detail.sort) ? detail.sort : 0

  logoFileName.value = resolveFileNameFromUrl(formData.value.icon)
  logoObjectKey.value = extractObjectKeyFromLogoUrl(formData.value.icon)
  originalLogoObjectKey.value = logoObjectKey.value
}

const loadEditDetail = async () => {
  if (!isEditMode.value) {
    return
  }

  if (!props.websiteId || props.websiteId <= 0) {
    showToast({ type: 'error', title: '网站ID不合法，无法加载编辑数据' })
    return
  }

  isDetailLoading.value = true
  try {
    const detail = await getAdminWebsiteDetail(props.websiteId)
    mapDetailToForm(detail)
  } catch (error) {
    showToast({
      type: 'error',
      title: resolveErrorMessage(error, '网站详情加载失败，无法编辑'),
    })
  } finally {
    isDetailLoading.value = false
  }
}

const handleSubmit = async () => {
  if (
    !formData.value.name.trim() ||
    !formData.value.url.trim() ||
    formData.value.categoryId === ''
  ) {
    showToast({ type: 'warning', title: '请填写带 * 号的必填项' })
    return
  }

  if (isLogoUploading.value) {
    showToast({ type: 'warning', title: 'Logo 正在上传，请稍后再提交' })
    return
  }

  if (isLogoDeleting.value) {
    showToast({ type: 'warning', title: 'Logo 正在删除，请稍后再提交' })
    return
  }

  if (isEditMode.value && isDetailLoading.value) {
    showToast({ type: 'warning', title: '编辑数据加载中，请稍后再提交' })
    return
  }

  isSubmitting.value = true
  try {
    const payload = buildCreatePayload()
    if (isEditMode.value) {
      if (!props.websiteId || props.websiteId <= 0) {
        throw new Error('网站ID不合法，无法提交修改')
      }
      await updateAdminWebsite(props.websiteId, payload)
      showToast({ type: 'success', title: '修改网站成功' })
    } else {
      await createAdminWebsite(payload)
      showToast({ type: 'success', title: '添加网站成功' })
    }
    emit('submit', payload)
    emit('cancel')
  } catch (error) {
    showToast({
      type: 'error',
      title: resolveErrorMessage(
        error,
        isEditMode.value ? '修改网站失败，请重试' : '添加网站失败，请重试',
      ),
    })
  } finally {
    isSubmitting.value = false
  }
}

onMounted(async () => {
  try {
    await loadCategoriesIfNeed()
  } catch (error) {
    showToast({ type: 'error', title: resolveErrorMessage(error, '分类数据加载失败') })
  }
  if (isEditMode.value) {
    await loadEditDetail()
  }
})
</script>

<template>
  <div
    class="bg-white dark:bg-dark-card border border-gray-200 dark:border-dark-border rounded-xl shadow-sm overflow-hidden flex flex-col md:flex-row"
  >
    <!-- 左侧介绍区 (终端风格) -->
    <div
      class="bg-gray-900 text-gray-300 w-full md:w-1/3 p-6 md:p-8 flex flex-col justify-between border-b md:border-b-0 md:border-r border-gray-800 relative overflow-hidden group"
    >
      <!-- 终端点缀 -->
      <div class="absolute top-4 left-4 flex gap-2">
        <div class="w-3 h-3 rounded-full bg-red-500"></div>
        <div class="w-3 h-3 rounded-full bg-yellow-500"></div>
        <div class="w-3 h-3 rounded-full bg-green-500"></div>
      </div>

      <div class="mt-8 font-mono text-sm space-y-4 relative z-10">
        <div>
          <span class="text-green-400">admin@system</span><span class="text-blue-400">:</span
          ><span class="text-cyan-400">~/websites</span>$ {{ pageCommand }}
        </div>
        <div class="text-gray-400">
          > 初始化录入环境...<br />
          > 加载网站分类字典... [OK]<br />
          > 准备接收网站元数据...
        </div>
        <div class="animate-pulse">_</div>
      </div>

      <div class="mt-12 relative z-10">
        <h3 class="text-white text-xl font-bold mb-2">{{ pageTitle }}</h3>
        <p class="text-gray-400 text-sm leading-relaxed">
          {{ pageDescription }}
        </p>
      </div>

      <!-- 装饰背景字符 -->
      <div
        class="absolute bottom-0 right-0 opacity-5 text-[8rem] font-bold font-mono leading-none select-none group-hover:scale-110 transition-transform duration-700 ease-out"
      >
        { }
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="w-full md:w-2/3 p-6 md:p-8 bg-gray-50 dark:bg-dark-bg/50">
      <div
        v-if="isEditMode && isDetailLoading"
        class="rounded-lg border border-gray-200 bg-white py-14 text-center text-gray-500 dark:border-dark-border dark:bg-dark-card dark:text-gray-300"
      >
        <i class="fas fa-spinner fa-spin text-2xl"></i>
        <p class="mt-3 text-sm">正在回显网站数据...</p>
      </div>

      <form v-if="!isEditMode || !isDetailLoading" @submit.prevent="handleSubmit" class="space-y-6">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <!-- 必填：网站名称 -->
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">
              网站名称 <span class="text-red-500">*</span>
            </label>
            <input
              v-model="formData.name"
              type="text"
              placeholder="例如: Vue.js 官网"
              required
              class="w-full px-4 py-2 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-lg text-sm focus:ring-2 focus:ring-brand-orange focus:border-brand-orange dark:text-white transition-all outline-none"
            />
          </div>

          <!-- 必填：网站URL -->
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">
              网站URL <span class="text-red-500">*</span>
            </label>
            <input
              v-model="formData.url"
              type="url"
              placeholder="https://..."
              required
              class="w-full px-4 py-2 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-lg text-sm focus:ring-2 focus:ring-brand-orange focus:border-brand-orange dark:text-white transition-all outline-none"
            />
          </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <!-- 必填：分类 -->
          <div class="space-y-2 min-w-0">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">
              所属分类 <span class="text-red-500">*</span>
            </label>
            <AppWebsiteCategorySelect
              v-model="selectedCategoryId"
              :options="validCategories"
              placeholder="选择分类"
              filterPlaceholder="输入分类名进行搜索"
              emptyText="暂无分类选项"
            />
          </div>

          <!-- 网站图标上传 -->
          <AppImageUploadField
            label="Logo 上传"
            uploadName="websiteLogo"
            accept="image/*"
            :maxFileSize="MAX_LOGO_FILE_SIZE"
            chooseLabel="上传 Logo"
            chooseIcon="fas fa-cloud-arrow-up"
            helperText="支持 JPG/PNG/WebP，单文件不超过 1MB。"
            uploadingText="上传中..."
            deletingText="删除中..."
            clearText="清除"
            previewFallbackName="logo-preview"
            previewAlt="Logo 预览"
            :logoUrl="formData.icon"
            :fileName="logoFileName"
            :objectKey="logoObjectKey"
            :uploading="isLogoUploading"
            :deleting="isLogoDeleting"
            :submitting="isSubmitting"
            @upload="handleLogoUpload"
            @clear="clearLogo"
          />
        </div>

        <!-- 网站简介 -->
        <div class="space-y-2">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">
            一句话简介
          </label>
          <input
            v-model="formData.summary"
            type="text"
            maxlength="200"
            placeholder="简短描述该网站的作用..."
            class="w-full px-4 py-2 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-lg text-sm focus:ring-2 focus:ring-brand-orange focus:border-brand-orange dark:text-white transition-all outline-none"
          />
        </div>

        <!-- 详细描述 -->
        <div class="space-y-2">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">
            详细描述
          </label>
          <UVditor
            v-model="formData.description"
            placeholder="支持输入更详细的站点介绍..."
            :minHeight="220"
          />
        </div>

        <!-- 标签与排序 -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">
              标签 (以逗号分隔)
            </label>
            <input
              v-model="formData.tags"
              type="text"
              placeholder="如: 前端, Vue, 框架"
              class="w-full px-4 py-2 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-lg text-sm focus:ring-2 focus:ring-brand-orange focus:border-brand-orange dark:text-white transition-all outline-none"
            />
          </div>
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">
              排序值 (越小越靠前)
            </label>
            <input
              v-model.number="formData.sort"
              type="number"
              placeholder="0"
              class="w-full px-4 py-2 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-lg text-sm focus:ring-2 focus:ring-brand-orange focus:border-brand-orange dark:text-white transition-all outline-none"
            />
          </div>
        </div>

        <!-- 运营选项 -->
        <div class="border-t border-gray-200 dark:border-dark-border pt-6 pb-2">
          <div class="flex items-center gap-6">
            <label class="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" v-model="formData.isTop" class="sr-only peer" />
              <div
                class="w-9 h-5 bg-gray-200 peer-focus:outline-none rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all dark:border-gray-600 peer-checked:bg-brand-orange"
              ></div>
              <span class="ml-3 text-sm font-medium text-gray-700 dark:text-gray-300"
                >置顶推荐</span
              >
            </label>

            <label class="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" v-model="formData.isRecommend" class="sr-only peer" />
              <div
                class="w-9 h-5 bg-gray-200 peer-focus:outline-none rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all dark:border-gray-600 peer-checked:bg-emerald-500"
              ></div>
              <span class="ml-3 text-sm font-medium text-gray-700 dark:text-gray-300"
                >编辑精选</span
              >
            </label>
          </div>
        </div>

        <!-- 提交动作区域 -->
        <div class="flex items-center justify-end gap-3 pt-4">
          <button
            type="button"
            :disabled="isLogoUploading || isLogoDeleting || isSubmitting"
            @click="handleCancel"
            class="cursor-pointer px-5 py-2 rounded-lg text-sm font-medium text-gray-600 dark:text-gray-300 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border hover:bg-gray-50 dark:hover:bg-dark-border transition-colors disabled:cursor-not-allowed disabled:opacity-60"
          >
            {{ isEditMode ? '取消修改' : '取消录入' }}
          </button>
          <button
            type="submit"
            :disabled="isSubmitting || isLogoUploading || isLogoDeleting"
            class="cursor-pointer px-6 py-2 rounded-lg text-sm font-medium text-white bg-brand-orange hover:bg-orange-600 focus:ring-2 focus:ring-brand-orange focus:ring-offset-2 dark:focus:ring-offset-gray-900 transition-all disabled:opacity-70 disabled:cursor-not-allowed flex items-center gap-2"
          >
            <i
              v-if="isSubmitting || isLogoUploading || isLogoDeleting"
              class="fas fa-spinner fa-spin"
            ></i>
            <i v-else class="fas fa-check"></i>
            <span>{{
              isSubmitting
                ? isEditMode
                  ? '更新中...'
                  : '执行中...'
                : isLogoUploading
                  ? '上传中...'
                  : isLogoDeleting
                    ? '删除中...'
                    : isEditMode
                      ? '确认并更新'
                      : '确认并保存'
            }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

