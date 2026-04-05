<template>
  <div class="min-h-screen bg-zinc-50 dark:bg-black text-zinc-900 dark:text-white selection:bg-amber-500/30 font-sans p-4 sm:p-8 md:p-12 lg:p-16 transition-colors">
    <div class="max-w-6xl mx-auto mt-[-2rem] md:mt-[-4rem]">

      <!-- Main Heading -->
      <div class="mb-12 mt-8 grid grid-cols-1 md:grid-cols-12 gap-8 items-end">
        <div class="md:col-span-8">
          <h1 class="text-4xl md:text-5xl font-bold tracking-tight leading-snug">
            上传
            <span class="text-amber-500">网站</span>
          </h1>
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
                  图标地址
                </label>
                <div class="relative">
                  <div class="absolute left-0 bottom-3 text-zinc-400 dark:text-zinc-600">
                    <Image class="w-5 h-5" />
                  </div>
                  <input
                    v-model="form.icon"
                    type="url"
                    required
                    class="w-full bg-transparent border-0 border-b-2 border-zinc-200 dark:border-zinc-800 pl-8 pb-3 text-sm font-light text-zinc-900 dark:text-white placeholder-zinc-400 dark:placeholder-zinc-800 focus:ring-0 focus:border-amber-500 transition-colors outline-none"
                    placeholder="URL格式"
                  />
                </div>
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
                    class="w-full cursor-pointer inline-flex items-center justify-center gap-2 bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 py-3 px-4 text-sm font-medium text-zinc-700 dark:text-zinc-200 hover:border-amber-500 hover:text-amber-500 transition-colors rounded-none"
                    @click="openTagDialog"
                  >
                    <Plus class="h-4 w-4" />
                    添加标签
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
                  class="group cursor-pointer relative inline-flex items-center justify-center px-8 py-3.5 font-mono font-bold text-white dark:text-black bg-amber-500 overflow-hidden transition-all hover:bg-amber-400 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:ring-offset-2 focus:ring-offset-zinc-50 dark:focus:ring-offset-black rounded-sm"
                >
                  <span class="absolute w-0 h-0 transition-all duration-500 ease-out bg-white rounded-full group-hover:w-56 group-hover:h-56 opacity-20"></span>
                  <span class="relative flex items-center gap-3">
                    提交网站
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
          <div v-if="filteredTagOptions.length > 0" class="flex flex-wrap gap-2">
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
import { computed, onMounted, ref } from 'vue'
import { ArrowLeft, ArrowRight, Check, Github, Image, Plus, Search, X } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import { getWebsiteCategories } from '@/api/website'
import { useToast } from '@/composables/useToast'
import AppWebsiteCategorySelect from '@/components/common/AppWebsiteCategorySelect.vue'
import UVditor from '@/components/ui-adapter/UVditor.vue'
import { buildTagColorStyle, normalizeTagColor } from '@/utils/tag-color'
import type { PublicWebsiteCategory } from '@/types/public-website'

type UploadTagOption = {
  id: number
  name: string
  color: string
}

const { add: showToast } = useToast()
const router = useRouter()

const categoryOptions = ref<PublicWebsiteCategory[]>([])
const isCategoryLoading = ref(false)
const MAX_SELECTED_TAGS = 3

// Avoid specific submission logics, just mockup console logs for demonstration and API integrations will be implemented later.
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

const mockTagOptions = ref<UploadTagOption[]>([
  { id: 1, name: '前端', color: '#f59e0b' },
  { id: 2, name: '后端', color: '#3b82f6' },
  { id: 3, name: 'AI', color: '#10b981' },
  { id: 4, name: '开发工具', color: '#ef4444' },
  { id: 5, name: '学习资源', color: '#8b5cf6' },
  { id: 6, name: '云服务', color: '#14b8a6' },
  { id: 7, name: '数据库', color: '#0ea5e9' },
  { id: 8, name: '设计灵感', color: '#f97316' },
  { id: 9, name: '效率办公', color: '#22c55e' },
  { id: 10, name: '安全', color: '#eab308' },
  { id: 11, name: '开源社区', color: '#6366f1' },
  { id: 12, name: '运维', color: '#06b6d4' },
])

const filteredTagOptions = computed(() => {
  const keyword = tagKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return mockTagOptions.value
  }
  return mockTagOptions.value.filter((tag) => tag.name.toLowerCase().includes(keyword))
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

const handleSubmit = () => {
  if (!form.value.categoryId) {
    showToast({ type: 'warning', title: '请选择所属分类' })
    return
  }

  const payload = {
    ...form.value,
    tags: selectedTags.value.map((tag) => tag.name),
  }

  console.log('Transmitting Record:', payload)
  // Integrate the service layer or store action here
}

onMounted(() => {
  void loadCategoryOptions()
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
