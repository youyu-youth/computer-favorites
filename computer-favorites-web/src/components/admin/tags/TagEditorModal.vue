<script setup lang="ts">
import { computed } from 'vue'
import type { AdminTagEditorMode, AdminTagFormErrors, AdminTagFormModel } from '@/types/admin-tag'

const props = defineProps<{
  open: boolean
  mode: AdminTagEditorMode
  modelValue: AdminTagFormModel
  errors: AdminTagFormErrors
  submitting: boolean
  colorPalette: string[]
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'update:modelValue', value: AdminTagFormModel): void
  (e: 'submit'): void
}>()

const title = computed(() => {
  return props.mode === 'create' ? '新建标签' : '编辑标签'
})

const description = computed(() => {
  return props.mode === 'create'
    ? '填写标签名称和颜色，保存后会立即出现在列表中。'
    : '修改标签信息后，列表会实时更新。'
})

const previewStyle = computed(() => {
  return {
    backgroundColor: props.modelValue.color || '#CBD5E1',
  }
})

const updateName = (value: string): void => {
  emit('update:modelValue', {
    ...props.modelValue,
    name: value,
  })
}

const updateColor = (value: string): void => {
  emit('update:modelValue', {
    ...props.modelValue,
    color: value,
  })
}

const selectColor = (color: string): void => {
  emit('update:modelValue', {
    ...props.modelValue,
    color,
  })
}

const handleOpenChange = (value: boolean): void => {
  emit('update:open', value)
}
</script>

<template>
  <UModal
    :open="open"
    :title="title"
    :description="description"
    :ui="{
      overlay: 'bg-black/45 backdrop-blur-[1px] z-[120]',
      content: 'w-[min(94vw,520px)] rounded-none border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card shadow-[0_16px_36px_rgba(15,23,42,0.28)] dark:shadow-[0_22px_44px_rgba(2,6,23,0.62)] overflow-hidden',
      header: 'border-b border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg px-5 py-4',
      title: 'text-base font-semibold text-gray-900 dark:text-gray-100',
      description: 'mt-1 text-sm text-gray-600 dark:text-gray-300',
      body: 'px-5 py-4',
      footer: 'px-5 py-4 border-t border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg flex flex-col-reverse gap-2 sm:flex-row sm:justify-end',
    }"
    @update:open="handleOpenChange"
  >
    <template #body>
      <div class="space-y-4">
        <div>
          <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-200">标签名称</label>
          <UInput
            :modelValue="modelValue.name"
            placeholder="请输入标签名称"
            maxlength="50"
            @update:modelValue="updateName"
          />
          <p v-if="errors.name" class="mt-1 text-xs text-red-500">{{ errors.name }}</p>
        </div>

        <div>
          <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-200">标签颜色</label>
          <UInput
            :modelValue="modelValue.color"
            placeholder="格式：#RRGGBB"
            maxlength="7"
            @update:modelValue="updateColor"
          />
          <p v-if="errors.color" class="mt-1 text-xs text-red-500">{{ errors.color }}</p>
          <div class="mt-2 flex flex-wrap gap-2">
            <button
              v-for="color in colorPalette"
              :key="color"
              type="button"
              class="h-6 w-6 rounded-none border-2 transition-transform hover:scale-105 focus:outline-none focus-visible:ring-2 focus-visible:ring-[#e95322]/50"
              :class="modelValue.color === color ? 'border-gray-900 dark:border-white' : 'border-white dark:border-dark-border'"
              :style="{ backgroundColor: color }"
              @click="selectColor(color)"
            ></button>
          </div>
        </div>

        <div class="rounded-none border border-gray-200 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60">
          <p class="text-xs text-gray-500 dark:text-gray-400">实时预览</p>
          <div class="mt-2 inline-flex items-center gap-2 rounded-none px-3 py-1.5 text-sm font-semibold text-white" :style="previewStyle">
            <span class="h-2 w-2 rounded-none bg-white/80"></span>
            {{ modelValue.name || '标签预览' }}
          </div>
        </div>
      </div>
    </template>

    <template #footer>
      <UButton class="rounded-none" color="neutral" variant="soft" :disabled="submitting" @click="emit('update:open', false)">
        取消
      </UButton>
      <UButton class="rounded-none" color="primary" :loading="submitting" :disabled="submitting" @click="emit('submit')">
        {{ mode === 'create' ? '创建标签' : '保存修改' }}
      </UButton>
    </template>
  </UModal>
</template>

<style scoped>
:deep([data-slot='base']) {
  border-radius: 0;
}
</style>
