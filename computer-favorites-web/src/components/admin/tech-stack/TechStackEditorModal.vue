<script setup lang="ts">
import { computed } from 'vue'
import type { FileUploadUploaderEvent } from 'primevue/fileupload'
import AppImageUploadField from '@/components/common/AppImageUploadField.vue'
import type { AdminTechStackFormModel, AdminTechStackStatus } from '@/types/tech-stack'

const props = defineProps<{
  open: boolean
  mode: 'create' | 'edit'
  modelValue: AdminTechStackFormModel
  errors: Record<string, string>
  submitting: boolean
}>()

const emit = defineEmits<{
  (e: 'update:open', val: boolean): void
  (e: 'update:modelValue', val: AdminTechStackFormModel): void
  (e: 'submit'): void
}>()

const isCreate = computed(() => props.mode === 'create')
const modalTitle = computed(() => (isCreate.value ? '新建技术栈' : '编辑技术栈'))
const modalDescription = computed(() => {
  return isCreate.value ? '添加新技术栈到字典库，保存后即时生效。' : '编辑技术栈信息，保存后将即时生效。'
})

const handleClose = () => {
  if (!props.submitting) {
    emit('update:open', false)
  }
}

const handleSubmit = () => {
  emit('submit')
}

const updateField = (field: keyof AdminTechStackFormModel, value: unknown) => {
  const newValue = { ...props.modelValue, [field]: value }
  emit('update:modelValue', newValue)
}

const statusOptions: Array<{ label: string; value: AdminTechStackStatus }> = [
  { label: '启用', value: 'ACTIVE' },
  { label: '禁用', value: 'DISABLED' },
]

const handleInput = (field: 'name' | 'officialUrl' | 'description', event: Event): void => {
  const target = event.target as HTMLInputElement | HTMLTextAreaElement
  updateField(field, target.value)
}

const handleIconUpload = (_event: FileUploadUploaderEvent): void => {
  // TODO: 对接 MinIO 上传，目前使用假数据模拟
  const mockUrl = 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/javascript/javascript-original.svg'
  updateField('iconPng', mockUrl)
}

const handleIconClear = (): void => {
  updateField('iconPng', '')
}

const handleColorInput = (event: Event): void => {
  const target = event.target as HTMLInputElement
  updateField('color', target.value)
}

const handleColorPickerChange = (event: Event): void => {
  const target = event.target as HTMLInputElement
  if (target.value) {
    updateField('color', target.value.toUpperCase())
  }
}

const handleSortChange = (event: Event): void => {
  const target = event.target as HTMLInputElement
  const parsedValue = Number.parseInt(target.value, 10)
  updateField('sort', Number.isNaN(parsedValue) ? 0 : parsedValue)
}

const handleStatusChange = (event: Event): void => {
  const target = event.target as HTMLSelectElement
  const nextStatus: AdminTechStackStatus = target.value === 'DISABLED' ? 'DISABLED' : 'ACTIVE'
  updateField('status', nextStatus)
}

const isValidColor = computed(() => {
  const value = props.modelValue.color.trim()
  if (!value) return true
  return /^#[0-9A-Fa-f]{6}$/.test(value)
})

const colorPreviewStyle = computed(() => {
  if (!isValidColor.value || !props.modelValue.color.trim()) {
    return {}
  }
  return { backgroundColor: props.modelValue.color.trim() }
})

const inputClass = (hasError: boolean) => [
  'h-10 w-full rounded-lg border bg-white px-3 text-sm text-gray-700 outline-none transition-all placeholder:text-gray-400 focus:ring-2 focus:ring-[#f55911]/15 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:placeholder:text-gray-500',
  hasError
    ? 'border-red-400 focus:border-red-500 focus:ring-red-500/15'
    : 'border-gray-300 focus:border-[#f55911]/50 dark:focus:border-[#f78166]/50',
]
</script>

<template>
  <Transition name="cf-tech-modal-fade" appear>
    <div
      v-if="open"
      class="fixed inset-0 z-[130] flex items-center justify-center p-4 sm:p-6"
      @click.self="handleClose"
    >
      <div class="absolute inset-0 bg-black/50 backdrop-blur-[2px]" @click="handleClose"></div>

      <section
        class="relative z-[1] flex w-full max-w-[600px] flex-col overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-[0_24px_48px_rgba(15,23,42,0.24)] dark:border-dark-border dark:bg-dark-card dark:shadow-[0_32px_64px_rgba(2,6,23,0.7)] max-h-[85vh]"
      >
        <button
          type="button"
          class="absolute right-4 top-4 z-10 inline-flex h-8 w-8 cursor-pointer items-center justify-center rounded-lg text-gray-400 transition-all hover:bg-gray-100 hover:text-gray-600 focus:outline-none focus:ring-2 focus:ring-[#f55911]/30 dark:hover:bg-gray-700 dark:hover:text-gray-200"
          :disabled="submitting"
          @click="handleClose"
        >
          <i class="fas fa-times text-sm"></i>
        </button>

        <header class="border-b border-gray-200 bg-gray-50/80 px-6 py-5 dark:border-dark-border dark:bg-dark-bg/80">
          <div class="flex items-center gap-3">
            <div
              class="flex h-10 w-10 items-center justify-center rounded-xl bg-[#f55911]/10 text-[#f55911] dark:bg-[#f55911]/15 dark:text-[#f78166]"
            >
              <i class="fas fa-layer-group"></i>
            </div>
            <div>
              <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">{{ modalTitle }}</h3>
              <p class="mt-0.5 text-xs text-gray-500 dark:text-gray-400">{{ modalDescription }}</p>
            </div>
          </div>
        </header>

        <form class="space-y-4 overflow-y-auto px-6 py-5" @submit.prevent="handleSubmit">
          <div>
            <label
              for="tech-name"
              class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              技术栈名称 <span class="text-red-500">*</span>
            </label>
            <input
              id="tech-name"
              :value="modelValue.name"
              type="text"
              placeholder="例如：Vue.js"
              :class="inputClass(!!errors.name)"
              :disabled="submitting"
              @input="(event) => handleInput('name', event)"
            />
            <p v-if="errors.name" class="mt-1.5 text-xs text-red-500">{{ errors.name }}</p>
          </div>

          <div>
            <AppImageUploadField
              label="技术栈图标"
              :logoUrl="modelValue.iconPng"
              :fileName="modelValue.iconPng ? modelValue.name : ''"
              previewFallbackName="技术栈图标"
              previewAlt="技术栈图标预览"
              chooseLabel="上传图标"
              chooseIcon="fas fa-cloud-arrow-up"
              helperText="支持 PNG、SVG 格式，建议 64x64 以上"
              :maxFileSize="2 * 1024 * 1024"
              :submitting="submitting"
              @upload="handleIconUpload"
              @clear="handleIconClear"
            />
            <p v-if="errors.iconPng" class="mt-1.5 text-xs text-red-500">{{ errors.iconPng }}</p>
          </div>

          <div>
            <label
              for="tech-url"
              class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              官网地址
            </label>
            <input
              id="tech-url"
              :value="modelValue.officialUrl"
              type="text"
              placeholder="https://vuejs.org"
              :class="inputClass(!!errors.officialUrl)"
              :disabled="submitting"
              @input="(event) => handleInput('officialUrl', event)"
            />
            <p v-if="errors.officialUrl" class="mt-1.5 text-xs text-red-500">{{ errors.officialUrl }}</p>
          </div>

          <div>
            <label
              for="tech-description"
              class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              描述
            </label>
            <textarea
              id="tech-description"
              :value="modelValue.description"
              rows="2"
              placeholder="技术栈简要描述"
              class="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-700 outline-none transition-all placeholder:text-gray-400 focus:border-[#f55911]/50 focus:ring-2 focus:ring-[#f55911]/15 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:placeholder:text-gray-500 dark:focus:border-[#f78166]/50"
              :disabled="submitting"
              @input="(event) => handleInput('description', event)"
            ></textarea>
          </div>

          <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <div>
              <label
                for="tech-color"
                class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300"
              >
                主题色
              </label>
              <div class="flex items-center gap-2">
                <label
                  class="relative flex h-10 w-10 shrink-0 cursor-pointer items-center justify-center rounded-lg border border-gray-300 transition-all hover:border-[#f55911]/50 dark:border-dark-border dark:hover:border-[#f78166]/50"
                  :title="isValidColor && modelValue.color.trim() ? modelValue.color : '选择颜色'"
                >
                  <span
                    v-if="isValidColor && modelValue.color.trim()"
                    class="h-6 w-6 rounded-md"
                    :style="colorPreviewStyle"
                  ></span>
                  <i v-else class="fas fa-palette text-gray-400 text-sm"></i>
                  <input
                    type="color"
                    class="absolute inset-0 cursor-pointer opacity-0"
                    :value="isValidColor && modelValue.color.trim() ? modelValue.color : '#f55911'"
                    :disabled="submitting"
                    @input="handleColorPickerChange"
                  />
                </label>
                <input
                  id="tech-color"
                  :value="modelValue.color"
                  type="text"
                  placeholder="#61DAFB"
                  class="h-10 w-full rounded-lg border border-gray-300 bg-white px-3 text-sm font-mono text-gray-700 outline-none transition-all placeholder:text-gray-400 focus:border-[#f55911]/50 focus:ring-2 focus:ring-[#f55911]/15 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:placeholder:text-gray-500 dark:focus:border-[#f78166]/50"
                  :class="errors.color ? 'border-red-400 focus:border-red-500 focus:ring-red-500/15' : ''"
                  :disabled="submitting"
                  @input="handleColorInput"
                />
              </div>
              <p v-if="errors.color" class="mt-1.5 text-xs text-red-500">{{ errors.color }}</p>
            </div>

            <div>
              <label
                for="tech-sort"
                class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300"
              >
                排序值
              </label>
              <input
                id="tech-sort"
                :value="String(modelValue.sort)"
                type="number"
                :class="inputClass(!!errors.sort)"
                :disabled="submitting"
                @input="handleSortChange"
              />
              <p v-if="errors.sort" class="mt-1.5 text-xs text-red-500">{{ errors.sort }}</p>
              <p class="mt-1.5 text-xs text-gray-400 dark:text-gray-500">数字越小越靠前</p>
            </div>

            <div>
              <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">状态</label>
              <div class="relative">
                <select
                  :value="modelValue.status"
                  class="h-10 w-full appearance-none rounded-lg border border-gray-300 bg-white px-3 pr-10 text-sm text-gray-700 outline-none transition-all focus:border-[#f55911]/50 focus:ring-2 focus:ring-[#f55911]/15 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:focus:border-[#f78166]/50"
                  :disabled="submitting"
                  @change="handleStatusChange"
                >
                  <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
                <i
                  class="fas fa-chevron-down pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-xs text-gray-400"
                ></i>
              </div>
            </div>
          </div>

          <footer
            class="flex flex-col-reverse gap-2 border-t border-gray-200 pt-5 dark:border-dark-border sm:flex-row sm:justify-end"
          >
            <button
              type="button"
              class="inline-flex h-10 cursor-pointer items-center justify-center rounded-lg border border-gray-300 bg-white px-5 text-sm font-medium text-gray-700 transition-all hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-[#f55911]/30 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg dark:focus:ring-offset-gray-900"
              :disabled="submitting"
              @click="handleClose"
            >
              取消
            </button>
            <button
              type="submit"
              class="inline-flex h-10 cursor-pointer items-center justify-center rounded-lg border border-transparent bg-[#f55911] px-5 text-sm font-semibold text-white shadow-sm transition-all hover:bg-[#e04e0a] hover:shadow focus:outline-none focus:ring-2 focus:ring-[#f55911]/40 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 active:scale-[0.98] dark:focus:ring-offset-gray-900"
              :disabled="submitting"
            >
              <i v-if="submitting" class="fas fa-spinner fa-spin mr-2"></i>
              <span>{{ isCreate ? '确认创建' : '保存修改' }}</span>
            </button>
          </footer>
        </form>
      </section>
    </div>
  </Transition>
</template>

<style scoped>
.cf-tech-modal-fade-enter-active,
.cf-tech-modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.cf-tech-modal-fade-enter-from,
.cf-tech-modal-fade-leave-to {
  opacity: 0;
}
</style>
