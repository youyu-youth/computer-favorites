<script setup lang="ts">
import Select from 'primevue/select'
import { ref, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useAdminNavStore } from '@/stores/adminNav'
import { useToast } from '@/composables/useToast'

const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'submit', data: any): void
}>()

const adminNavStore = useAdminNavStore()
const { websiteCategories } = storeToRefs(adminNavStore)
const { add: showToast } = useToast()

const validCategories = computed(() => {
  return websiteCategories.value.filter((c) => c.id !== 0)
})

const formData = ref({
  name: '',
  url: '',
  icon: '',
  summary: '',
  description: '',
  category_id: '' as number | '',
  tags: '',
  is_top: false,
  is_recommend: false,
  sort: 0
})

const isSubmitting = ref(false)

const selectedCategoryId = computed<number | null>({
  get: () => {
    return formData.value.category_id === '' ? null : formData.value.category_id
  },
  set: (value) => {
    formData.value.category_id = value ?? ''
  }
})

const categorySelectPt = {
  root: ({ state }: any) => ({
    class: [
      'relative flex w-full min-w-0 items-center rounded-lg border bg-white pl-4 pr-14 py-2 text-sm leading-5 text-gray-900 transition-all dark:bg-dark-card dark:text-gray-100',
      state.focused
        ? 'border-brand-orange ring-2 ring-brand-orange'
        : 'border-gray-300 dark:border-dark-border'
    ]
  }),
  label: {
    class: 'flex-1 truncate'
  },
  dropdown: {
    class: 'absolute right-3 top-1/2 flex -translate-y-1/2 items-center justify-center text-gray-400'
  },
  clearIcon: {
    class: 'absolute right-8 top-1/2 -translate-y-1/2 cursor-pointer text-gray-400 transition-colors hover:text-gray-600 dark:text-gray-500 dark:hover:text-gray-300'
  },
  dropdownIcon: {
    class: 'text-xs'
  },
  overlay: {
    class: 'cf-category-select-panel z-[120] mt-1 w-full min-w-0 max-w-full overflow-hidden rounded-lg border border-gray-200 bg-white shadow-lg dark:border-dark-border dark:bg-dark-card'
  },
  header: {
    class: 'border-b border-gray-100 px-2 pb-2 pt-2 dark:border-dark-border'
  },
  pcFilterContainer: {
    root: {
      class: 'relative w-full'
    }
  },
  pcFilter: {
    root: {
      class: 'cf-category-select-filter w-full rounded-md border border-gray-300 bg-gray-50 px-3 py-1.5 pr-8 text-sm text-gray-900 outline-none transition-colors focus:border-brand-orange focus:ring-1 focus:ring-brand-orange dark:border-dark-border dark:bg-dark-bg dark:text-gray-100'
    }
  },
  pcFilterIconContainer: {
    root: {
      class: 'pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400'
    }
  },
  filterIcon: {
    class: 'text-xs'
  },
  listContainer: {
    class: 'cf-select-scroll max-h-56 overflow-y-auto'
  },
  list: {
    class: 'py-1'
  },
  option: ({ context }: any) => ({
    class: [
      'cursor-pointer truncate px-4 py-2 text-sm transition-colors',
      context.selected
        ? 'bg-orange-50 font-medium text-brand-orange dark:bg-orange-900/20 dark:text-brand-orange'
        : 'text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-dark-border'
    ]
  }),
  optionLabel: {
    class: 'truncate'
  },
  emptyMessage: {
    class: 'px-4 py-3 text-center text-sm text-gray-500 dark:text-gray-400'
  }
}

const handleSubmit = async () => {
  if (!formData.value.name || !formData.value.url || formData.value.category_id === '') {
    showToast({ type: 'warning', title: '请填写带 * 号的必填项' })
    return
  }

  isSubmitting.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    showToast({ type: 'success', title: '网站添加成功！' })
    emit('submit', formData.value)
    emit('cancel')
  } catch (error) {
    showToast({ type: 'error', title: '操作失败，请重试' })
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="bg-white dark:bg-dark-card border border-gray-200 dark:border-dark-border rounded-xl shadow-sm overflow-hidden flex flex-col md:flex-row">
    <!-- 左侧介绍区 (终端风格) -->
    <div class="bg-gray-900 text-gray-300 w-full md:w-1/3 p-6 md:p-8 flex flex-col justify-between border-b md:border-b-0 md:border-r border-gray-800 relative overflow-hidden group">
      <!-- 终端点缀 -->
      <div class="absolute top-4 left-4 flex gap-2">
        <div class="w-3 h-3 rounded-full bg-red-500"></div>
        <div class="w-3 h-3 rounded-full bg-yellow-500"></div>
        <div class="w-3 h-3 rounded-full bg-green-500"></div>
      </div>

      <div class="mt-8 font-mono text-sm space-y-4 relative z-10">
        <div>
          <span class="text-green-400">admin@system</span><span class="text-blue-400">:</span><span class="text-purple-400">~/websites</span>$ ./add_website.sh
        </div>
        <div class="text-gray-400">
          > 初始化录入环境...<br>
          > 加载网站分类字典... [OK]<br>
          > 准备接收网站元数据...
        </div>
        <div class="animate-pulse">_</div>
      </div>

      <div class="mt-12 relative z-10">
        <h3 class="text-white text-xl font-bold mb-2">添加新网站</h3>
        <p class="text-gray-400 text-sm leading-relaxed">
          管理员手动录入系统收录的推荐站点。请确保 URL 的有效性及分类归属的准确。提交后将默认标记为“已自动审核”。
        </p>
      </div>

      <!-- 装饰背景字符 -->
      <div class="absolute bottom-0 right-0 opacity-5 text-[8rem] font-bold font-mono leading-none select-none group-hover:scale-110 transition-transform duration-700 ease-out">
        { }
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="w-full md:w-2/3 p-6 md:p-8 bg-gray-50 dark:bg-dark-bg/50">
      <form @submit.prevent="handleSubmit" class="space-y-6">

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
            <Select
              v-model="selectedCategoryId"
              :options="validCategories"
              optionLabel="name"
              optionValue="id"
              placeholder="选择分类"
              showClear
              filter
              filterPlaceholder="输入分类名进行搜索"
              :filterFields="['name']"
              filterMatchMode="contains"
              appendTo="self"
              scrollHeight="14rem"
              :overlayStyle="{ width: '100%', maxWidth: '100%', minWidth: '0' }"
              :pt="categorySelectPt"
              class="w-full"
            >
              <template #value="slotProps">
                <span v-if="slotProps.value == null || slotProps.value === ''" class="truncate text-gray-500 dark:text-gray-400">
                  {{ slotProps.placeholder || '选择分类' }}
                </span>
                <span v-else class="truncate">
                  {{ validCategories.find((item) => item.id === slotProps.value)?.name }}
                </span>
              </template>
              <template #option="slotProps">
                <div class="flex min-w-0 items-center justify-between gap-3">
                  <span class="truncate">{{ slotProps.option.name }}</span>
                  <span class="shrink-0 text-xs text-gray-400">{{ slotProps.option.count }}</span>
                </div>
              </template>
              <template #dropdownicon>
                <i class="fas fa-chevron-down"></i>
              </template>
              <template #clearicon="slotProps">
                <i class="fas fa-xmark" @click.stop="slotProps.clearCallback($event)"></i>
              </template>
              <template #filtericon>
                <i class="fas fa-search"></i>
              </template>
              <template #empty>
                <div class="px-4 py-3 text-center text-sm text-gray-500 dark:text-gray-400">暂无分类选项</div>
              </template>
            </Select>
          </div>

          <!-- 网站图标URL -->
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">
              Logo URL
            </label>
            <input
              v-model="formData.icon"
              type="url"
              placeholder="图标链接..."
              class="w-full px-4 py-2 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-lg text-sm focus:ring-2 focus:ring-brand-orange focus:border-brand-orange dark:text-white transition-all outline-none"
            />
          </div>
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
          <textarea
            v-model="formData.description"
            rows="3"
            placeholder="支持输入更详细的站点介绍..."
            class="w-full px-4 py-2 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-lg text-sm focus:ring-2 focus:ring-brand-orange focus:border-brand-orange dark:text-white transition-all outline-none resize-none"
          ></textarea>
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
              v-model="formData.sort"
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
              <input type="checkbox" v-model="formData.is_top" class="sr-only peer">
              <div class="w-9 h-5 bg-gray-200 peer-focus:outline-none rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all dark:border-gray-600 peer-checked:bg-brand-orange"></div>
              <span class="ml-3 text-sm font-medium text-gray-700 dark:text-gray-300">置顶推荐</span>
            </label>

            <label class="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" v-model="formData.is_recommend" class="sr-only peer">
              <div class="w-9 h-5 bg-gray-200 peer-focus:outline-none rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all dark:border-gray-600 peer-checked:bg-emerald-500"></div>
              <span class="ml-3 text-sm font-medium text-gray-700 dark:text-gray-300">编辑精选</span>
            </label>
          </div>
        </div>

        <!-- 提交动作区域 -->
        <div class="flex items-center justify-end gap-3 pt-4">
          <button
            type="button"
            @click="emit('cancel')"
            class="px-5 py-2 rounded-lg text-sm font-medium text-gray-600 dark:text-gray-300 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border hover:bg-gray-50 dark:hover:bg-dark-border transition-colors"
          >
            取消录入
          </button>
          <button
            type="submit"
            :disabled="isSubmitting"
            class="px-6 py-2 rounded-lg text-sm font-medium text-white bg-brand-orange hover:bg-orange-600 focus:ring-2 focus:ring-brand-orange focus:ring-offset-2 dark:focus:ring-offset-gray-900 transition-all disabled:opacity-70 disabled:cursor-not-allowed flex items-center gap-2"
          >
            <i v-if="isSubmitting" class="fas fa-spinner fa-spin"></i>
            <i v-else class="fas fa-check"></i>
            <span>{{ isSubmitting ? '执行中...' : '确认并保存' }}</span>
          </button>
        </div>

      </form>
    </div>
  </div>
</template>

<style scoped>
:deep(.cf-category-select-panel) {
  max-width: min(100%, calc(100vw - 2rem));
}

:deep(.cf-category-select-panel .cf-select-scroll) {
  scrollbar-width: thin;
  scrollbar-color: rgb(148 163 184 / 0.7) transparent;
}

:deep(.cf-category-select-panel .cf-select-scroll::-webkit-scrollbar) {
  width: 8px;
}

:deep(.cf-category-select-panel .cf-select-scroll::-webkit-scrollbar-track) {
  background: transparent;
}

:deep(.cf-category-select-panel .cf-select-scroll::-webkit-scrollbar-thumb) {
  border-radius: 9999px;
  background: rgb(148 163 184 / 0.65);
}

:deep(.cf-category-select-panel .cf-select-scroll::-webkit-scrollbar-thumb:hover) {
  background: rgb(100 116 139 / 0.85);
}
</style>
