<script setup lang="ts">
import { computed } from 'vue'
import type { AdminCategoryFormModel, AdminCategoryItem, AdminCategoryStatus } from '@/types/category'

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
const modalDescription = computed(() => {
  return isCreate.value ? '创建新分类并设置基础信息。' : '编辑分类信息，保存后将即时生效。'
})

const parentOptions = computed(() => {
  const opts = props.categories.map((c) => ({
      label: `${'　'.repeat(c.depth ?? 0)}${c.name}`,
      value: String(c.id),
    }))
  return [{ label: '无 (顶级分类)', value: '' }, ...opts]
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

const statusOptions: Array<{ label: string; value: AdminCategoryStatus }> = [
  { label: '启用', value: 'ACTIVE' },
  { label: '禁用', value: 'DISABLED' },
]

const handleInput = (field: 'name' | 'description' | 'icon', event: Event): void => {
  const target = event.target as HTMLInputElement | HTMLTextAreaElement
  updateField(field, target.value)
}

const handleParentChange = (event: Event): void => {
  const target = event.target as HTMLSelectElement
  updateField('parentId', target.value ? Number(target.value) : null)
}

const handleSortChange = (event: Event): void => {
  const target = event.target as HTMLInputElement
  const parsedValue = Number.parseInt(target.value, 10)
  updateField('sort', Number.isNaN(parsedValue) ? 0 : parsedValue)
}

const handleStatusChange = (event: Event): void => {
  const target = event.target as HTMLSelectElement
  const nextStatus: AdminCategoryStatus = target.value === 'DISABLED' ? 'DISABLED' : 'ACTIVE'
  updateField('status', nextStatus)
}
</script>

<template>
  <Transition name="cf-category-modal-fade" appear>
    <div
      v-if="open"
      class="fixed inset-0 z-[130] flex items-center justify-center p-4 sm:p-6"
      @click.self="handleClose"
    >
      <div class="absolute inset-0 bg-black/45 backdrop-blur-[1px]" @click="handleClose"></div>

      <section
        class="relative z-[1] w-full max-w-[560px] overflow-hidden rounded-xl border border-gray-200 bg-white shadow-[0_22px_48px_rgba(15,23,42,0.3)] dark:border-dark-border dark:bg-dark-card dark:shadow-[0_30px_58px_rgba(2,6,23,0.7)]"
      >
        <button
          type="button"
          class="absolute right-4 top-4 inline-flex h-8 w-8 cursor-pointer items-center justify-center rounded-md border border-gray-200 bg-white text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:border-dark-border dark:bg-dark-bg dark:text-gray-300 dark:hover:bg-gray-700 dark:hover:text-gray-100"
          :disabled="submitting"
          @click="handleClose"
        >
          <i class="fas fa-times text-sm"></i>
        </button>

        <header class="border-b border-gray-200 bg-gray-50 px-5 py-4 pr-16 dark:border-dark-border dark:bg-dark-bg">
          <div class="flex items-center gap-3">
            <div
              class="flex h-9 w-9 items-center justify-center rounded-lg border border-blue-200 bg-blue-50 text-blue-600 dark:border-blue-900/50 dark:bg-blue-900/20 dark:text-blue-300"
            >
              <i class="fas fa-folder-open text-sm"></i>
            </div>
            <div>
              <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">{{ modalTitle }}</h3>
              <p class="mt-0.5 text-xs text-gray-500 dark:text-gray-400">{{ modalDescription }}</p>
            </div>
          </div>
        </header>

        <form class="space-y-5 px-5 py-5" @submit.prevent="handleSubmit">
          <div>
            <label
              for="category-name"
              class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              分类名称 <span class="text-red-500">*</span>
            </label>
            <input
              id="category-name"
              :value="modelValue.name"
              type="text"
              placeholder="例如：前端开发"
              class="h-10 w-full rounded-md border border-gray-300 bg-white px-3 text-sm text-gray-700 outline-none transition-colors placeholder:text-gray-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:placeholder:text-gray-500 dark:focus:border-blue-400"
              :class="errors.name ? 'border-red-400 focus:border-red-500 focus:ring-red-500/20' : ''"
              :disabled="submitting"
              @input="(event) => handleInput('name', event)"
            />
            <p v-if="errors.name" class="mt-1 text-xs text-red-500">{{ errors.name }}</p>
          </div>

          <div>
            <label
              for="category-parent"
              class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              父级分类
            </label>
            <div class="relative">
              <select
                id="category-parent"
                :value="modelValue.parentId === null ? '' : String(modelValue.parentId)"
                class="h-10 w-full appearance-none rounded-md border border-gray-300 bg-white px-3 pr-10 text-sm text-gray-700 outline-none transition-colors focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:focus:border-blue-400"
                :disabled="submitting"
                @change="handleParentChange"
              >
                <option v-for="option in parentOptions" :key="option.value || 'root'" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
              <i
                class="fas fa-chevron-down pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-xs text-gray-400"
              ></i>
            </div>
          </div>

          <div>
            <label
              for="category-icon"
              class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              图标类名
            </label>
            <div class="relative">
              <div v-if="modelValue.icon" class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                <i :class="modelValue.icon" class="text-gray-500 dark:text-gray-400"></i>
              </div>
              <input
                id="category-icon"
                :value="modelValue.icon"
                type="text"
                placeholder="例如：fas fa-code"
                class="h-10 w-full rounded-md border border-gray-300 bg-white px-3 text-sm text-gray-700 outline-none transition-colors placeholder:text-gray-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:placeholder:text-gray-500 dark:focus:border-blue-400"
                :class="modelValue.icon ? 'pl-10' : ''"
                :disabled="submitting"
                @input="(event) => handleInput('icon', event)"
              />
            </div>
            <p class="mt-1.5 text-xs text-gray-500 dark:text-gray-400">支持 FontAwesome 图标类名</p>
          </div>

          <div>
            <label
              for="category-description"
              class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              描述
            </label>
            <textarea
              id="category-description"
              :value="modelValue.description"
              rows="3"
              placeholder="分类描述信息"
              class="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm text-gray-700 outline-none transition-colors placeholder:text-gray-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:placeholder:text-gray-500 dark:focus:border-blue-400"
              :disabled="submitting"
              @input="(event) => handleInput('description', event)"
            ></textarea>
          </div>

          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label
                for="category-sort"
                class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
              >
                排序值
              </label>
              <input
                id="category-sort"
                :value="String(modelValue.sort)"
                type="number"
                class="h-10 w-full rounded-md border border-gray-300 bg-white px-3 text-sm text-gray-700 outline-none transition-colors focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:focus:border-blue-400"
                :disabled="submitting"
                @input="handleSortChange"
              />
              <p class="mt-1.5 text-xs text-gray-500 dark:text-gray-400">数字越小越靠前</p>
            </div>

            <div>
              <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">状态</label>
              <div class="relative">
                <select
                  :value="modelValue.status"
                  class="h-10 w-full appearance-none rounded-md border border-gray-300 bg-white px-3 pr-10 text-sm text-gray-700 outline-none transition-colors focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:focus:border-blue-400"
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
            class="flex flex-col-reverse gap-2 border-t border-gray-200 pt-4 dark:border-dark-border sm:flex-row sm:justify-end"
          >
            <button
              type="button"
              class="inline-flex h-9 cursor-pointer items-center justify-center rounded-md border border-gray-300 bg-white px-4 text-sm font-medium text-gray-700 shadow-sm transition-colors hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg"
              :disabled="submitting"
              @click="handleClose"
            >
              取消
            </button>
            <button
              type="submit"
              class="inline-flex h-9 cursor-pointer items-center justify-center rounded-md border border-transparent bg-blue-600 px-4 text-sm font-medium text-white shadow-sm transition-colors hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:bg-blue-500 dark:hover:bg-blue-600 dark:focus:ring-offset-gray-900"
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
.cf-category-modal-fade-enter-active,
.cf-category-modal-fade-leave-active {
  transition: opacity 0.18s ease;
}

.cf-category-modal-fade-enter-from,
.cf-category-modal-fade-leave-to {
  opacity: 0;
}
</style>
