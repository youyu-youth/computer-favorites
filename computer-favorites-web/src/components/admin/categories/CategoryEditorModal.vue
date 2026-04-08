<script setup lang="ts">
import { computed } from 'vue'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import type { AdminCategoryFormModel, AdminCategoryItem } from '@/types/category'

const props = defineProps<{
  open: boolean
  mode: 'create' | 'edit'
  modelValue: AdminCategoryFormModel
  errors: Record<string, string>
  submitting: boolean
  categories: AdminCategoryItem[]
}>()

const emit = defineEmits<{
  (e: 'update:open', val: boolean): void
  (e: 'update:modelValue', val: AdminCategoryFormModel): void
  (e: 'submit'): void
}>()

const isCreate = computed(() => props.mode === 'create')
const modalTitle = computed(() => (isCreate.value ? '新建分类' : '编辑分类'))

const parentOptions = computed(() => {
  const opts = props.categories
    .filter((c) => c.parentId === null) // only top level
    .map((c) => ({
      label: c.name,
      value: c.id,
    }))
  return [{ label: '无 (顶级分类)', value: null }, ...opts]
})

const handleClose = () => {
  if (!props.submitting) {
    emit('update:open', false)
  }
}

const handleSubmit = () => {
  emit('submit')
}

const updateField = (field: keyof AdminCategoryFormModel, value: unknown) => {
  const newValue = { ...props.modelValue, [field]: value }
  emit('update:modelValue', newValue)
}

const statusOptions = [
  { label: '启用', value: 'ACTIVE' },
  { label: '禁用', value: 'DISABLED' },
]

const modalPt = {
  mask: { class: 'bg-black/45 backdrop-blur-[1px] z-[120]' },
  root: {
    class:
      'w-[min(92vw,500px)] rounded-lg border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card shadow-[0_16px_36px_rgba(15,23,42,0.28)] dark:shadow-[0_22px_44px_rgba(2,6,23,0.62)] overflow-hidden',
  },
  header: {
    class: 'border-b border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg px-5 py-4',
  },
  content: {
    class: 'px-5 py-6',
  },
  footer: {
    class:
      'px-5 py-4 border-t border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg flex flex-col-reverse gap-2 sm:flex-row sm:justify-end',
  },
} as const
</script>

<template>
  <Dialog
    :visible="open"
    modal
    :draggable="false"
    :dismissableMask="false"
    :pt="modalPt"
    @update:visible="(val: boolean) => emit('update:open', val)"
  >
    <template #header>
      <div class="flex items-center gap-2 text-gray-900 dark:text-gray-100">
        <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400">
          <i class="fas fa-folder-open text-sm"></i>
        </div>
        <h3 class="text-base font-semibold">{{ modalTitle }}</h3>
      </div>
    </template>

    <form @submit.prevent="handleSubmit" class="space-y-5">
      <div>
        <label for="category-name" class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
          分类名称 <span class="text-red-500">*</span>
        </label>
        <InputText
          id="category-name"
          :model-value="modelValue.name"
          @update:model-value="(val) => updateField('name', val)"
          class="w-full"
          :class="{ 'p-invalid': errors.name }"
          placeholder="例如：前端开发"
        />
        <small v-if="errors.name" class="mt-1 text-red-500">{{ errors.name }}</small>
      </div>

      <div>
        <label for="category-parent" class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
          父级分类
        </label>
        <Dropdown
          id="category-parent"
          :model-value="modelValue.parentId"
          @update:model-value="(val: any) => updateField('parentId', val)"
          :options="parentOptions"
          optionLabel="label"
          optionValue="value"
          placeholder="选择父级分类"
          class="w-full"
        />
      </div>

      <div>
        <label for="category-icon" class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
          图标类名
        </label>
        <div class="relative">
          <div v-if="modelValue.icon" class="absolute inset-y-0 left-0 flex items-center pl-3">
            <i :class="modelValue.icon" class="text-gray-500"></i>
          </div>
          <InputText
            id="category-icon"
            :model-value="modelValue.icon"
            @update:model-value="(val) => updateField('icon', val)"
            class="w-full"
            :class="{ 'pl-10': !!modelValue.icon }"
            placeholder="例如：fas fa-code"
          />
        </div>
        <p class="mt-1.5 text-xs text-gray-500 dark:text-gray-400">支持 FontAwesome 图标类名</p>
      </div>

      <div>
        <label for="category-description" class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
          描述
        </label>
        <InputText
          id="category-description"
          :model-value="modelValue.description"
          @update:model-value="(val) => updateField('description', val)"
          class="w-full"
          placeholder="分类描述信息"
        />
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <label for="category-sort" class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
            排序值
          </label>
          <InputText
            id="category-sort"
            type="number"
            :model-value="String(modelValue.sort)"
            @update:model-value="(val) => updateField('sort', Number(val) || 0)"
            class="w-full"
          />
          <p class="mt-1.5 text-xs text-gray-500 dark:text-gray-400">数字越小越靠前</p>
        </div>

        <div>
          <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
            状态
          </label>
          <div class="flex items-center py-2 h-10 gap-3">
            <Dropdown
              :model-value="modelValue.status"
          @update:model-value="(val: any) => updateField('status', val)"
              :options="statusOptions"
              optionLabel="label"
              optionValue="value"
              class="w-full shrink"
              :pt="{
                root: { class: 'h-auto py-1 shadow-sm' },
                input: { class: 'py-1.5 text-sm' }
              }"
            />
          </div>
        </div>
      </div>
    </form>

    <template #footer>
      <button
        type="button"
        class="inline-flex h-9 items-center justify-center rounded-md border border-gray-300 bg-white px-4 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg"
        :disabled="submitting"
        @click="handleClose"
      >
        取消
      </button>
      <button
        type="submit"
        class="inline-flex h-9 items-center justify-center rounded-md border border-transparent bg-blue-600 px-4 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:bg-blue-500 dark:hover:bg-blue-600 dark:focus:ring-offset-gray-900"
        :disabled="submitting"
        @click="handleSubmit"
      >
        <i v-if="submitting" class="fas fa-spinner fa-spin mr-2"></i>
        <span>{{ isCreate ? '确认创建' : '保存修改' }}</span>
      </button>
    </template>
  </Dialog>
</template>
