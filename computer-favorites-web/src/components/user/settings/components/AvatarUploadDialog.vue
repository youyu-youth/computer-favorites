<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'
import type { FileUploadUploaderEvent } from 'primevue/fileupload'
import AppImageUploadField from '@/components/common/AppImageUploadField.vue'

const props = defineProps<{
  open: boolean
  /** 当前已保存的头像 URL（仅作为对照预览展示） */
  currentAvatar: string
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', file: File): void
}>()

defineOptions({
  name: 'AvatarUploadDialog',
})

const ACCEPT = 'image/jpeg,image/png,image/webp'
const MAX_SIZE_BYTES = 2 * 1024 * 1024

const previewUrl = ref('')
const previewFileName = ref('')
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const errorText = ref('')

const readFileAsDataURL = (file: File): Promise<string> =>
  new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result ?? ''))
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(file)
  })

const handleUpload = async (event: FileUploadUploaderEvent) => {
  errorText.value = ''
  const filesProp = event.files as File | File[]
  const file = Array.isArray(filesProp) ? filesProp[0] : filesProp
  if (!file) {
    return
  }
  if (file.size > MAX_SIZE_BYTES) {
    errorText.value = `文件不能超过 2 MB（当前 ${(file.size / 1024 / 1024).toFixed(2)} MB）`
    return
  }
  if (!ACCEPT.split(',').includes(file.type)) {
    errorText.value = '仅支持 JPG / PNG / WebP 格式'
    return
  }
  uploading.value = true
  try {
    const dataUrl = await readFileAsDataURL(file)
    previewUrl.value = dataUrl
    previewFileName.value = file.name
    selectedFile.value = file
  } catch (err) {
    errorText.value = '读取图片失败，请重试'
    console.error('[AvatarUpload] readFileAsDataURL error', err)
  } finally {
    uploading.value = false
  }
}

const handleClearPreview = () => {
  previewUrl.value = ''
  previewFileName.value = ''
  selectedFile.value = null
  errorText.value = ''
}

const closeDialog = () => {
  emit('update:open', false)
}

const handleSubmit = () => {
  if (!selectedFile.value || uploading.value || props.loading) {
    return
  }
  emit('submit', selectedFile.value)
}

const onKeydown = (event: KeyboardEvent) => {
  if (!props.open) {
    return
  }
  if (event.key === 'Escape') {
    event.preventDefault()
    closeDialog()
  }
}

watch(
  () => props.open,
  (next) => {
    if (next) {
      previewUrl.value = ''
      previewFileName.value = ''
      selectedFile.value = null
      errorText.value = ''
      document.addEventListener('keydown', onKeydown)
      const root = document.documentElement
      root.dataset.cfDialogLock = '1'
      root.style.overflow = 'hidden'
    } else {
      document.removeEventListener('keydown', onKeydown)
      const root = document.documentElement
      delete root.dataset.cfDialogLock
      root.style.overflow = ''
    }
  },
)

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown)
  const root = document.documentElement
  if (root.dataset.cfDialogLock) {
    delete root.dataset.cfDialogLock
    root.style.overflow = ''
  }
})
</script>

<template>
  <Transition name="cf-avatar-dialog">
    <div
      v-if="open"
      class="cf-avatar-overlay fixed inset-0 z-50 flex items-end justify-center px-3 py-4 sm:items-center sm:px-6 sm:py-8"
      role="dialog"
      aria-modal="true"
      aria-labelledby="cf-avatar-dialog-title"
      @click.self="closeDialog"
    >
      <div
        class="cf-avatar-dialog relative flex w-full max-w-md flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xl dark:border-white/10 dark:bg-[#0e0e10]"
        :style="{ maxHeight: '90dvh' }"
      >
        <div class="cf-avatar-accent" aria-hidden="true"></div>

        <!-- Header -->
        <header class="flex items-start gap-3 border-b border-slate-100 px-5 py-4 dark:border-white/[0.06] sm:px-6 sm:py-5">
          <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-amber-100 text-amber-600 dark:bg-amber-500/15 dark:text-amber-400">
            <UIcon name="i-lucide-image-up" class="h-4 w-4" />
          </span>
          <div class="min-w-0 flex-1 space-y-0.5">
            <h4
              id="cf-avatar-dialog-title"
              class="text-base font-semibold text-slate-900 dark:text-white sm:text-lg"
            >
              更换头像
            </h4>
            <p class="text-xs text-slate-500 dark:text-slate-400 sm:text-[13px]">
              支持 JPG / PNG / WebP，最大 2 MB；推荐 1:1 正方形 256×256 及以上
            </p>
          </div>
          <button
            type="button"
            class="cf-avatar-close-btn flex h-8 w-8 shrink-0 cursor-pointer items-center justify-center rounded-lg text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-700 dark:text-slate-500 dark:hover:bg-white/[0.06] dark:hover:text-white"
            aria-label="关闭对话框"
            @click="closeDialog"
          >
            <UIcon name="i-lucide-x" class="h-4 w-4" />
          </button>
        </header>

        <!-- Body -->
        <div class="cf-avatar-body flex flex-1 flex-col gap-5 overflow-y-auto px-5 py-5 sm:px-6">
          <!-- Round preview -->
          <div class="flex flex-col items-center gap-2.5">
            <div class="relative">
              <div class="absolute -inset-1 rounded-full bg-amber-400/15 blur-md" aria-hidden="true"></div>
              <div
                class="relative flex h-[120px] w-[120px] items-center justify-center overflow-hidden rounded-full bg-slate-100 ring-2 ring-amber-400/40 ring-offset-4 ring-offset-white dark:bg-white/[0.04] dark:ring-amber-400/30 dark:ring-offset-[#0e0e10]"
              >
                <img
                  v-if="previewUrl || currentAvatar"
                  :src="previewUrl || currentAvatar"
                  alt="头像预览"
                  class="h-full w-full object-cover"
                />
                <UIcon
                  v-else
                  name="i-lucide-user-round"
                  class="h-12 w-12 text-slate-400 dark:text-slate-500"
                />
              </div>
              <span
                v-if="previewUrl"
                class="cf-avatar-new-badge absolute -right-1 top-1 inline-flex items-center gap-1 rounded-full bg-amber-500 px-2 py-0.5 text-[10px] font-bold text-white shadow-sm"
              >
                <UIcon name="i-lucide-sparkles" class="h-3 w-3" />
                <span>新</span>
              </span>
              <button
                v-if="previewUrl"
                type="button"
                class="cf-avatar-clear-btn absolute -right-2 -bottom-1 flex h-7 w-7 cursor-pointer items-center justify-center rounded-full border-2 border-white bg-slate-900 text-white shadow-md transition-all hover:bg-rose-600 hover:scale-110 dark:border-[#0e0e10] dark:bg-white/[0.12] dark:hover:bg-rose-500"
                aria-label="移除当前预览"
                @click="handleClearPreview"
              >
                <UIcon name="i-lucide-x" class="h-3.5 w-3.5" />
              </button>
            </div>
            <p class="text-xs font-medium tabular-nums text-slate-500 dark:text-slate-400">
              <template v-if="previewUrl">
                <UIcon name="i-lucide-eye" class="-mt-0.5 mr-1 inline h-3 w-3" />
                <span class="text-amber-600 dark:text-amber-400">新头像预览</span>
                <span v-if="previewFileName" class="ml-2 truncate align-middle text-slate-400 dark:text-slate-500">{{ previewFileName }}</span>
              </template>
              <template v-else>
                <UIcon name="i-lucide-image" class="-mt-0.5 mr-1 inline h-3 w-3" />
                <span>当前头像</span>
              </template>
            </p>
          </div>

          <!-- Upload field -->
          <div class="cf-avatar-upload-wrap">
            <AppImageUploadField
              variant="default"
              :logoUrl="''"
              :maxFileSize="MAX_SIZE_BYTES"
              :accept="ACCEPT"
              choose-label="选择头像图片"
              choose-icon="fas fa-cloud-arrow-up"
              helper-text="点击上方按钮选择本地图片"
              uploading-text="读取中…"
              :uploading="uploading"
              @upload="handleUpload"
              @clear="handleClearPreview"
            />
          </div>

          <!-- Error -->
          <div
            v-if="errorText"
            class="flex items-start gap-2 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700 dark:border-rose-500/30 dark:bg-rose-500/10 dark:text-rose-300"
          >
            <UIcon name="i-lucide-triangle-alert" class="mt-0.5 h-3.5 w-3.5 shrink-0" />
            <span>{{ errorText }}</span>
          </div>
        </div>

        <!-- Footer -->
        <footer class="flex flex-col-reverse items-stretch gap-2 border-t border-slate-100 px-5 py-3 dark:border-white/[0.06] sm:flex-row sm:items-center sm:justify-end sm:px-6">
          <button
            type="button"
            class="inline-flex h-9 cursor-pointer items-center justify-center rounded-lg border border-slate-200 bg-white px-4 text-sm font-medium text-slate-700 transition-colors hover:border-slate-300 hover:bg-slate-50 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-200 dark:hover:border-white/20 dark:hover:bg-white/[0.06]"
            :disabled="loading"
            @click="closeDialog"
          >
            取消
          </button>
          <button
            type="button"
            class="cf-avatar-submit inline-flex h-9 cursor-pointer items-center justify-center gap-1.5 rounded-lg px-4 text-sm font-semibold transition-all duration-200 disabled:cursor-not-allowed"
            :disabled="!previewUrl || uploading || loading"
            @click="handleSubmit"
          >
            <UIcon
              :name="loading ? 'i-lucide-loader-2' : 'i-lucide-check'"
              class="h-4 w-4"
              :class="loading ? 'animate-spin' : ''"
            />
            <span>{{ loading ? '保存中…' : '保存为新头像' }}</span>
          </button>
        </footer>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.cf-avatar-overlay {
  background-color: rgb(8 8 10 / 0.55);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.cf-avatar-dialog {
  box-shadow: 0 24px 56px -16px rgb(15 23 42 / 0.35);
}

:where(html.dark) .cf-avatar-dialog {
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.04) inset,
    0 24px 56px -16px rgb(0 0 0 / 0.7),
    0 0 0 1px rgb(255 255 255 / 0.02);
}

.cf-avatar-accent {
  position: absolute;
  inset: 0 0 auto 0;
  height: 1px;
  background-image: linear-gradient(
    90deg,
    transparent 0%,
    rgb(245 158 11 / 0.55) 50%,
    transparent 100%
  );
  z-index: 1;
}

/* New badge bounce-in */
.cf-avatar-new-badge {
  animation: cf-avatar-badge-in 0.32s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

@keyframes cf-avatar-badge-in {
  from {
    opacity: 0;
    transform: scale(0.4);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* Submit button (consistent with project amber primary) */
.cf-avatar-submit {
  background: #f59e0b;
  color: #1f1300;
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.35) inset,
    0 6px 14px -8px rgb(245 158 11 / 0.55);
}
.cf-avatar-submit:hover:not(:disabled) {
  background: #fbbf24;
  transform: translateY(-1px);
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.45) inset,
    0 10px 20px -10px rgb(245 158 11 / 0.65);
}
.cf-avatar-submit:active:not(:disabled) {
  background: #d97706;
  transform: translateY(0);
}
.cf-avatar-submit:disabled {
  background: rgb(120 113 108 / 0.45);
  color: rgb(214 211 209);
  box-shadow: none;
}
:where(html.dark) .cf-avatar-submit:disabled {
  background: rgb(255 255 255 / 0.06);
  color: rgb(148 163 184);
}

/* Body scrollbar polish */
.cf-avatar-body {
  scrollbar-width: thin;
  scrollbar-color: rgb(148 163 184 / 0.4) transparent;
}
.cf-avatar-body::-webkit-scrollbar {
  width: 8px;
}
.cf-avatar-body::-webkit-scrollbar-thumb {
  background: rgb(148 163 184 / 0.35);
  border-radius: 4px;
}
.cf-avatar-body::-webkit-scrollbar-thumb:hover {
  background: rgb(148 163 184 / 0.55);
}
:where(html.dark) .cf-avatar-body::-webkit-scrollbar-thumb {
  background: rgb(255 255 255 / 0.1);
}

/* Dialog enter / leave */
.cf-avatar-dialog-enter-active,
.cf-avatar-dialog-leave-active {
  transition: opacity 0.22s ease;
}
.cf-avatar-dialog-enter-active .cf-avatar-dialog,
.cf-avatar-dialog-leave-active .cf-avatar-dialog {
  transition:
    transform 0.24s cubic-bezier(0.16, 1, 0.3, 1),
    opacity 0.22s ease;
}

.cf-avatar-dialog-enter-from,
.cf-avatar-dialog-leave-to {
  opacity: 0;
}

.cf-avatar-dialog-enter-from .cf-avatar-dialog {
  opacity: 0;
  transform: translateY(12px) scale(0.98);
}
.cf-avatar-dialog-leave-to .cf-avatar-dialog {
  opacity: 0;
  transform: translateY(6px);
}

/* Mobile bottom-sheet entrance */
@media (max-width: 639px) {
  .cf-avatar-dialog-enter-from .cf-avatar-dialog {
    transform: translateY(24px) scale(1);
  }
}

/* Tweak AppImageUploadField default helper-text alignment inside dialog */
.cf-avatar-upload-wrap :deep(label) {
  display: none;
}
</style>
