<script setup lang="ts">
import { computed } from 'vue'
import ColorPicker from 'primevue/colorpicker'
import Dialog from 'primevue/dialog'
import type { AdminTagEditorMode, AdminTagFormErrors, AdminTagFormModel } from '@/types/admin-tag'

const DEFAULT_HEX_COLOR = 'E95322'

const props = defineProps<{
  open: boolean
  mode: AdminTagEditorMode
  modelValue: AdminTagFormModel
  errors: AdminTagFormErrors
  submitting: boolean
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

const dialogPt = {
  mask: {
    class: 'bg-black/45 backdrop-blur-[1px] z-[120]',
  },
  root: {
    class:
      'w-[min(94vw,520px)] rounded-none border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card shadow-[0_16px_36px_rgba(15,23,42,0.28)] dark:shadow-[0_22px_44px_rgba(2,6,23,0.62)] overflow-visible',
  },
  header: {
    class: 'border-b border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg px-5 py-4',
  },
  content: {
    class: 'px-5 py-4',
  },
  footer: {
    class:
      'px-5 py-4 border-t border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg flex flex-col-reverse gap-2 sm:flex-row sm:justify-end',
  },
} as const

const previewStyle = computed(() => {
  return {
    backgroundColor: props.modelValue.color || '#CBD5E1',
  }
})

const colorPickerPt = {
  root: {
    class: 'relative block w-full',
  },
  preview: {
    class:
      'h-10 w-full rounded-none border border-gray-300 bg-transparent dark:border-dark-border focus:outline-none focus:ring-2 focus:ring-[#e95322]/40 cursor-pointer',
  },
  panel: {
    class:
      'absolute left-0 top-full z-[80] mt-2 h-[166px] w-[193px] rounded-none border border-gray-200 bg-white shadow-[0_14px_28px_rgba(15,23,42,0.2)] dark:border-dark-border dark:bg-dark-card dark:shadow-[0_18px_36px_rgba(2,6,23,0.52)]',
  },
  content: {
    class: 'relative h-[166px] w-[193px]',
  },
  colorSelector: {
    class:
      'absolute left-2 top-2 h-[150px] w-[150px] cursor-crosshair overflow-hidden rounded-none border border-gray-200 dark:border-dark-border',
  },
  colorBackground: {
    class: 'h-full w-full',
  },
  colorHandle: {
    class:
      'absolute h-3 w-3 -translate-x-1/2 -translate-y-1/2 rounded-none border border-white shadow',
  },
  hue: {
    class:
      'absolute left-[167px] top-2 h-[150px] w-[17px] cursor-row-resize rounded-none border border-gray-200 bg-[linear-gradient(0deg,red_0,#ff0_17%,#0f0_33%,#0ff_50%,#00f_67%,#f0f_83%,red)] dark:border-dark-border',
  },
  hueHandle: {
    class:
      'absolute left-0 top-[150px] ml-[-2px] mt-[-5px] h-[10px] w-[21px] rounded-none border-2 border-white bg-black/30 shadow',
  },
} as const

const normalizePickerHex = (value: string | undefined): string => {
  if (!value) {
    return DEFAULT_HEX_COLOR
  }

  const normalizedValue = value.trim().replace(/^#/, '').toUpperCase()
  if (!/^[0-9A-F]{6}$/.test(normalizedValue)) {
    return DEFAULT_HEX_COLOR
  }
  return normalizedValue
}

const colorPickerModel = computed({
  get: () => normalizePickerHex(props.modelValue.color),
  set: (value: string) => {
    emit('update:modelValue', {
      ...props.modelValue,
      color: `#${normalizePickerHex(value)}`,
    })
  },
})

const updateName = (value: string): void => {
  emit('update:modelValue', {
    ...props.modelValue,
    name: value,
  })
}

const handleOpenChange = (value: boolean): void => {
  emit('update:open', value)
}
</script>

<template>
  <Dialog
    :visible="open"
    modal
    :dismissableMask="true"
    :draggable="false"
    :pt="dialogPt"
    @update:visible="handleOpenChange"
  >
    <template #header>
      <div class="space-y-1">
        <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">{{ title }}</h3>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-300">{{ description }}</p>
      </div>
    </template>

    <div class="space-y-4">
      <div>
        <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-200"
          >标签名称</label
        >
        <UInput
          :modelValue="modelValue.name"
          placeholder="请输入标签名称"
          maxlength="50"
          @update:modelValue="updateName"
        />
        <p v-if="errors.name" class="mt-1 text-xs text-red-500">{{ errors.name }}</p>
      </div>

      <div>
        <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-200"
          >标签颜色</label
        >
        <ColorPicker
          v-model="colorPickerModel"
          format="hex"
          class="relative block w-full"
          appendTo="self"
          :pt="colorPickerPt"
        />
        <p class="mt-2 text-xs text-gray-500 dark:text-gray-400">
          当前颜色：<span class="font-semibold text-gray-700 dark:text-gray-200">{{
            modelValue.color
          }}</span>
        </p>
        <p v-if="errors.color" class="mt-1 text-xs text-red-500">{{ errors.color }}</p>
      </div>

      <div
        class="rounded-none border border-gray-200 bg-gray-50 p-3 dark:border-dark-border dark:bg-dark-bg/60"
      >
        <p class="text-xs text-gray-500 dark:text-gray-400">实时预览</p>
        <div
          class="mt-2 inline-flex items-center gap-2 rounded-none px-3 py-1.5 text-sm font-semibold text-white"
          :style="previewStyle"
        >
          <span class="h-2 w-2 rounded-none bg-white/80"></span>
          {{ modelValue.name || '标签预览' }}
        </div>
      </div>
    </div>

    <template #footer>
      <UButton
        class="rounded-none"
        color="neutral"
        variant="soft"
        :disabled="submitting"
        @click="emit('update:open', false)"
      >
        取消
      </UButton>
      <UButton
        class="rounded-none"
        color="primary"
        :loading="submitting"
        :disabled="submitting"
        @click="emit('submit')"
      >
        {{ mode === 'create' ? '创建标签' : '保存修改' }}
      </UButton>
    </template>
  </Dialog>
</template>

<style scoped>
:deep([data-slot='base']) {
  border-radius: 0;
}
</style>
