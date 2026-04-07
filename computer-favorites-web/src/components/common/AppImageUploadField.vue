<script setup lang="ts">
import FileUpload from 'primevue/fileupload'
import type { FileUploadUploaderEvent } from 'primevue/fileupload'
import { computed } from 'vue'

type UploadFieldVariant = 'default' | 'minimal'

const props = withDefaults(
  defineProps<{
    label?: string
    variant?: UploadFieldVariant
    uploadName?: string
    accept?: string
    maxFileSize: number
    chooseLabel?: string
    chooseIcon?: string
    helperText?: string
    uploadingText?: string
    deletingText?: string
    clearText?: string
    previewFallbackName?: string
    previewAlt?: string
    logoUrl: string
    fileName?: string
    objectKey?: string
    uploading?: boolean
    deleting?: boolean
    submitting?: boolean
  }>(),
  {
    label: 'Upload',
    variant: 'default',
    uploadName: 'file',
    accept: 'image/*',
    chooseLabel: 'Upload',
    chooseIcon: 'fas fa-cloud-arrow-up',
    helperText: '',
    uploadingText: 'Uploading...',
    deletingText: 'Deleting...',
    clearText: 'Clear',
    previewFallbackName: 'file-preview',
    previewAlt: 'File preview',
    fileName: '',
    objectKey: '',
    uploading: false,
    deleting: false,
    submitting: false,
  },
)

const emit = defineEmits<{
  (e: 'upload', event: FileUploadUploaderEvent): void
  (e: 'clear'): void
}>()

const chooseButtonClass = computed(() => {
  if (props.variant === 'minimal') {
    return 'w-full cursor-pointer justify-center rounded-none border border-zinc-200 bg-white px-4 py-2 text-sm font-medium text-zinc-700 transition-colors hover:border-amber-500 hover:text-amber-500 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-200 dark:hover:border-amber-500 dark:hover:text-amber-400'
  }
  return 'w-full cursor-pointer justify-center rounded-lg border border-dashed border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:border-brand-orange hover:text-brand-orange dark:border-dark-border dark:bg-dark-card dark:text-gray-200 dark:hover:border-brand-orange dark:hover:text-brand-orange'
})

const helperTextClass = computed(() => {
  if (props.variant === 'minimal') {
    return 'text-xs text-zinc-500 dark:text-zinc-400'
  }
  return 'text-xs text-gray-500 dark:text-gray-400'
})

const statusTextClass = computed(() => {
  if (props.variant === 'minimal') {
    return 'ml-1 text-amber-500'
  }
  return 'ml-1 text-brand-orange'
})

const previewContainerClass = computed(() => {
  if (props.variant === 'minimal') {
    return 'rounded-none border border-zinc-200 bg-white p-3 dark:border-zinc-800 dark:bg-zinc-900/60'
  }
  return 'rounded-lg border border-gray-200 bg-white p-3 dark:border-dark-border dark:bg-dark-card'
})

const previewImageClass = computed(() => {
  if (props.variant === 'minimal') {
    return 'h-12 w-12 shrink-0 rounded-none border border-zinc-200 object-cover dark:border-zinc-700'
  }
  return 'h-12 w-12 shrink-0 rounded-md border border-gray-200 object-cover dark:border-dark-border'
})

const previewNameClass = computed(() => {
  if (props.variant === 'minimal') {
    return 'truncate text-sm font-medium text-zinc-700 dark:text-zinc-200'
  }
  return 'truncate text-sm font-medium text-gray-700 dark:text-gray-200'
})

const previewUrlClass = computed(() => {
  if (props.variant === 'minimal') {
    return 'mt-1 truncate text-xs text-zinc-500 dark:text-zinc-400'
  }
  return 'mt-1 truncate text-xs text-gray-500 dark:text-gray-400'
})

const objectKeyClass = computed(() => {
  if (props.variant === 'minimal') {
    return 'mt-1 truncate text-xs text-zinc-400 dark:text-zinc-500'
  }
  return 'mt-1 truncate text-xs text-gray-400 dark:text-gray-500'
})

const clearButtonClass = computed(() => {
  if (props.variant === 'minimal') {
    return 'shrink-0 cursor-pointer rounded-none border border-zinc-200 px-2.5 py-1 text-xs font-medium text-zinc-600 transition-colors hover:border-amber-500 hover:text-amber-500 disabled:cursor-not-allowed disabled:opacity-60 dark:border-zinc-700 dark:text-zinc-300 dark:hover:border-amber-500 dark:hover:text-amber-400'
  }
  return 'shrink-0 cursor-pointer rounded-md border border-gray-300 px-2.5 py-1 text-xs font-medium text-gray-600 transition-colors hover:border-red-300 hover:text-red-500 disabled:cursor-not-allowed disabled:opacity-60 dark:border-dark-border dark:text-gray-300 dark:hover:border-red-400 dark:hover:text-red-400'
})

const chooseButtonProps = computed(() => {
  return {
    type: 'button',
    class: chooseButtonClass.value,
  }
})

const isDisabled = computed(() => props.uploading || props.deleting || props.submitting)

const handleUpload = (event: FileUploadUploaderEvent) => {
  emit('upload', event)
}

const handleClear = () => {
  emit('clear')
}
</script>

<template>
  <div class="space-y-2 min-w-0">
    <label v-if="label" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
      {{ label }}
    </label>

    <FileUpload
      mode="basic"
      :name="uploadName"
      :accept="accept"
      :maxFileSize="maxFileSize"
      :auto="true"
      :disabled="isDisabled"
      customUpload
      :chooseLabel="chooseLabel"
      :chooseIcon="chooseIcon"
      :chooseButtonProps="chooseButtonProps"
      class="cf-logo-upload w-full"
      @uploader="handleUpload"
    />

    <p :class="helperTextClass">
      {{ helperText }}
      <span v-if="uploading" :class="statusTextClass">{{ uploadingText }}</span>
      <span v-if="deleting" :class="statusTextClass">{{ deletingText }}</span>
    </p>

    <div
      v-if="logoUrl"
      :class="previewContainerClass"
    >
      <div class="flex items-start gap-3 min-w-0">
        <img
          :src="logoUrl"
          :alt="previewAlt"
          :class="previewImageClass"
        />
        <div class="min-w-0 flex-1">
          <p :class="previewNameClass">
            {{ fileName || previewFallbackName }}
          </p>
          <p :class="previewUrlClass">
            {{ logoUrl }}
          </p>
          <p v-if="objectKey" :class="objectKeyClass">
            {{ objectKey }}
          </p>
        </div>
        <button
          type="button"
          :disabled="isDisabled"
          @click="handleClear"
          :class="clearButtonClass"
        >
          {{ clearText }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.cf-logo-upload button) {
  width: 100%;
}

:deep(.cf-logo-upload input[type='file']) {
  display: none !important;
}

@media (max-width: 640px) {
  :deep(.cf-logo-upload button) {
    min-height: 2.5rem;
  }
}
</style>
