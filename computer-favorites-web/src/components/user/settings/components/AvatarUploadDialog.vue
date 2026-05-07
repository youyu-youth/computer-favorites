<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import {
  AlertCircle,
  Check,
  ImagePlus,
  Loader2,
  RefreshCw,
  Upload,
  UserRound,
  X,
} from 'lucide-vue-next'
import AvatarCropper from '@/components/user/settings/components/AvatarCropper.vue'

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

const ACCEPT_LIST = ['image/jpeg', 'image/png', 'image/webp']
const ACCEPT_ATTR = ACCEPT_LIST.join(',')
const MAX_SIZE_BYTES = 2 * 1024 * 1024

type Phase = 'pick' | 'crop'

const phase = ref<Phase>('pick')
const sourceDataUrl = ref('')
const sourceFileName = ref('')
const errorText = ref('')
const isReadingFile = ref(false)
const isCropping = ref(false)
const isDragOver = ref(false)
const cropperReady = ref(false)

const fileInputRef = ref<HTMLInputElement | null>(null)
const cropperRef = ref<InstanceType<typeof AvatarCropper> | null>(null)

let previousScrollY = 0

const formatSize = (bytes: number) => {
  if (bytes < 1024) {
    return `${bytes} B`
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`
  }
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

// 锁定页面滚动
const lockPageScroll = () => {
  if (typeof window === 'undefined') {
    return
  }
  previousScrollY = window.scrollY
  document.body.style.position = 'fixed'
  document.body.style.top = `-${previousScrollY}px`
  document.body.style.left = '0'
  document.body.style.right = '0'
  document.body.style.width = '100%'
}

const unlockPageScroll = () => {
  if (typeof window === 'undefined') {
    return
  }
  document.body.style.position = ''
  document.body.style.top = ''
  document.body.style.left = ''
  document.body.style.right = ''
  document.body.style.width = ''
  window.scrollTo({ top: previousScrollY, left: 0, behavior: 'auto' })
}

const resetState = () => {
  phase.value = 'pick'
  sourceDataUrl.value = ''
  sourceFileName.value = ''
  errorText.value = ''
  isReadingFile.value = false
  isDragOver.value = false
  cropperReady.value = false
}

const readFileAsDataURL = (file: File): Promise<string> =>
  new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result ?? ''))
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(file)
  })

// 校验并加载文件，进入裁剪阶段
const handleFile = async (file: File | null | undefined) => {
  if (!file) {
    return
  }
  errorText.value = ''
  if (!ACCEPT_LIST.includes(file.type)) {
    errorText.value = '仅支持 JPG / PNG / WebP 格式'
    return
  }
  if (file.size > MAX_SIZE_BYTES) {
    errorText.value = `文件不能超过 2 MB（当前 ${formatSize(file.size)}）`
    return
  }
  isReadingFile.value = true
  try {
    const dataUrl = await readFileAsDataURL(file)
    sourceDataUrl.value = dataUrl
    sourceFileName.value = file.name
    phase.value = 'crop'
    await nextTick()
  } catch (err) {
    errorText.value = '读取图片失败，请重试'
    console.error('[AvatarUploadDialog] readFileAsDataURL error', err)
  } finally {
    isReadingFile.value = false
  }
}

const triggerFilePicker = () => {
  if (props.loading || isReadingFile.value || isCropping.value) {
    return
  }
  fileInputRef.value?.click()
}

const handleInputChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  void handleFile(file)
  target.value = ''
}

const handleDrop = (event: DragEvent) => {
  isDragOver.value = false
  if (props.loading || isReadingFile.value) {
    return
  }
  const file = event.dataTransfer?.files?.[0]
  void handleFile(file)
}

const handleDragOver = (event: DragEvent) => {
  if (props.loading || isReadingFile.value) {
    return
  }
  event.preventDefault()
  isDragOver.value = true
}

const handleDragLeave = () => {
  isDragOver.value = false
}

const backToPick = () => {
  if (props.loading || isCropping.value) {
    return
  }
  phase.value = 'pick'
  sourceDataUrl.value = ''
  sourceFileName.value = ''
  cropperReady.value = false
}

const closeDialog = () => {
  if (props.loading || isCropping.value) {
    return
  }
  emit('update:open', false)
}

const handleSubmit = async () => {
  if (props.loading || isCropping.value || phase.value !== 'crop') {
    return
  }
  if (!cropperRef.value) {
    errorText.value = '裁剪器未就绪，请稍后再试'
    return
  }
  isCropping.value = true
  errorText.value = ''
  try {
    const file = await cropperRef.value.getCroppedFile('avatar')
    emit('submit', file)
  } catch (err) {
    errorText.value = err instanceof Error ? err.message : '裁剪失败，请重试'
    console.error('[AvatarUploadDialog] getCroppedFile error', err)
  } finally {
    isCropping.value = false
  }
}

const handleCropperReady = () => {
  cropperReady.value = true
}

const handleCropperError = (message: string) => {
  errorText.value = message
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

const canSubmit = computed(
  () =>
    phase.value === 'crop' &&
    cropperReady.value &&
    !isCropping.value &&
    !props.loading,
)

watch(
  () => props.open,
  (next) => {
    if (next) {
      resetState()
      lockPageScroll()
      document.addEventListener('keydown', onKeydown)
    } else {
      document.removeEventListener('keydown', onKeydown)
      unlockPageScroll()
    }
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown)
  unlockPageScroll()
})
</script>

<template>
  <Teleport to="body">
    <Transition name="aud-fade">
      <div
        v-if="open"
        class="aud-overlay fixed inset-0 z-[120] flex items-end justify-center sm:items-center"
        role="dialog"
        aria-modal="true"
        aria-labelledby="aud-title"
      >
        <div class="aud-mask absolute inset-0" @click="closeDialog" />

        <Transition name="aud-pop" appear>
          <div
            v-if="open"
            class="aud-card relative z-10 flex max-h-[94dvh] w-full max-w-lg flex-col overflow-hidden rounded-t-2xl border border-black/10 bg-white text-black sm:rounded-2xl dark:border-white/10 dark:bg-black dark:text-white"
            @click.stop
          >
            <!-- Header -->
            <header class="flex items-start gap-3 border-b border-black/[0.06] px-5 pt-5 pb-4 sm:px-6 sm:pt-6 dark:border-white/[0.06]">
              <span
                class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-black/10 bg-white text-black dark:border-white/15 dark:bg-black dark:text-white"
              >
                <ImagePlus :size="18" :stroke-width="1.75" />
              </span>
              <div class="min-w-0 flex-1 pt-0.5">
                <h4
                  id="aud-title"
                  class="text-[16px] leading-tight font-semibold tracking-tight sm:text-[17px]"
                >
                  {{ phase === 'pick' ? '更换头像' : '调整头像' }}
                </h4>
                <p class="mt-1 text-[12px] leading-snug text-black/55 dark:text-white/55">
                  <template v-if="phase === 'pick'">
                    支持 JPG / PNG / WebP，最大 2 MB；推荐 1:1 正方形 256×256 及以上
                  </template>
                  <template v-else>
                    拖动图片调整位置，缩放后裁出你想保留的部分
                  </template>
                </p>
              </div>
              <button
                type="button"
                aria-label="关闭"
                class="aud-icon-btn -mt-1 -mr-1 flex h-9 w-9 shrink-0 cursor-pointer items-center justify-center rounded-xl text-black/45 transition-colors hover:bg-black/[0.05] hover:text-black disabled:cursor-not-allowed disabled:opacity-40 dark:text-white/45 dark:hover:bg-white/[0.06] dark:hover:text-white"
                :disabled="loading || isCropping"
                @click="closeDialog"
              >
                <X :size="18" :stroke-width="2" />
              </button>
            </header>

            <!-- Body -->
            <div class="aud-body flex-1 overflow-y-auto px-5 py-5 sm:px-6">
              <!-- 阶段一：选图 -->
              <div v-if="phase === 'pick'" class="space-y-5">
                <!-- 当前头像展示 -->
                <div class="flex items-center gap-4 rounded-2xl border border-black/[0.08] bg-white px-4 py-3.5 dark:border-white/[0.08] dark:bg-black">
                  <div class="relative flex h-16 w-16 shrink-0 items-center justify-center overflow-hidden rounded-full border border-black/10 bg-black/[0.03] dark:border-white/15 dark:bg-white/[0.03]">
                    <img
                      v-if="currentAvatar"
                      :src="currentAvatar"
                      alt="当前头像"
                      class="h-full w-full object-cover"
                    />
                    <UserRound v-else :size="28" :stroke-width="1.5" class="text-black/30 dark:text-white/30" />
                  </div>
                  <div class="min-w-0 flex-1">
                    <p class="text-[11px] font-medium tracking-wider text-black/45 uppercase dark:text-white/45">
                      当前头像
                    </p>
                    <p class="mt-0.5 truncate text-[13px] text-black/70 dark:text-white/70">
                      {{ currentAvatar ? '已设置' : '未设置头像' }}
                    </p>
                  </div>
                </div>

                <!-- 拖入 / 选图 -->
                <button
                  type="button"
                  class="aud-dropzone group relative flex w-full cursor-pointer flex-col items-center justify-center gap-2.5 rounded-2xl border-2 border-dashed px-5 py-9 text-center transition-colors disabled:cursor-not-allowed"
                  :class="[
                    isDragOver
                      ? 'border-black bg-black/[0.03] dark:border-white dark:bg-white/[0.04]'
                      : 'border-black/15 hover:border-black/40 dark:border-white/15 dark:hover:border-white/40',
                  ]"
                  :disabled="isReadingFile || loading"
                  @click="triggerFilePicker"
                  @dragover="handleDragOver"
                  @dragleave="handleDragLeave"
                  @drop.prevent="handleDrop"
                >
                  <span class="flex h-12 w-12 items-center justify-center rounded-full border border-black/10 bg-white text-black transition-transform duration-200 group-hover:scale-105 dark:border-white/15 dark:bg-black dark:text-white">
                    <Loader2 v-if="isReadingFile" :size="20" :stroke-width="1.75" class="animate-spin" />
                    <Upload v-else :size="20" :stroke-width="1.75" />
                  </span>
                  <div class="space-y-0.5">
                    <p class="text-[13.5px] font-medium">
                      <template v-if="isReadingFile">读取图片中…</template>
                      <template v-else>点击或拖入图片到此处</template>
                    </p>
                    <p class="text-[11.5px] text-black/50 dark:text-white/50">
                      JPG · PNG · WebP &nbsp;·&nbsp; ≤ 2 MB
                    </p>
                  </div>
                </button>

                <input
                  ref="fileInputRef"
                  type="file"
                  class="hidden"
                  :accept="ACCEPT_ATTR"
                  @change="handleInputChange"
                />
              </div>

              <!-- 阶段二：裁剪 -->
              <div v-else class="space-y-3">
                <AvatarCropper
                  ref="cropperRef"
                  :src="sourceDataUrl"
                  output-type="jpeg"
                  :output-quality="0.92"
                  default-ratio="1:1"
                  @ready="handleCropperReady"
                  @error="handleCropperError"
                />

                <div class="flex items-center justify-between gap-3 rounded-xl border border-black/[0.08] bg-white px-3 py-2 dark:border-white/[0.08] dark:bg-black">
                  <span class="truncate text-[12px] text-black/55 dark:text-white/55">
                    <span class="font-mono">{{ sourceFileName || '已选图片' }}</span>
                  </span>
                  <button
                    type="button"
                    class="aud-link-btn inline-flex shrink-0 cursor-pointer items-center gap-1 rounded-lg px-2 py-1 text-[12px] font-medium disabled:cursor-not-allowed disabled:opacity-50"
                    :disabled="loading || isCropping"
                    @click="backToPick"
                  >
                    <RefreshCw :size="12" :stroke-width="1.75" />
                    <span>重选图片</span>
                  </button>
                </div>
              </div>

              <!-- 错误提示 -->
              <Transition name="aud-error">
                <p
                  v-if="errorText"
                  class="mt-3 flex items-start gap-1.5 rounded-lg border border-red-300/70 bg-red-50/70 px-2.5 py-2 text-[12px] leading-snug text-red-700 dark:border-red-500/30 dark:bg-red-500/[0.08] dark:text-red-300"
                >
                  <AlertCircle :size="13" :stroke-width="2" class="mt-0.5 shrink-0" />
                  <span>{{ errorText }}</span>
                </p>
              </Transition>
            </div>

            <!-- Footer -->
            <footer class="flex items-center justify-end gap-2 border-t border-black/[0.06] px-5 py-4 sm:px-6 dark:border-white/[0.06]">
              <button
                type="button"
                class="aud-cancel inline-flex h-10 cursor-pointer items-center justify-center rounded-xl px-4 text-[13.5px] font-medium disabled:cursor-not-allowed disabled:opacity-50"
                :disabled="loading || isCropping"
                @click="closeDialog"
              >
                取消
              </button>
              <button
                type="button"
                class="aud-primary inline-flex h-10 cursor-pointer items-center justify-center gap-1.5 rounded-xl px-5 text-[13.5px] font-semibold transition-all duration-200 disabled:cursor-not-allowed"
                :disabled="!canSubmit"
                @click="handleSubmit"
              >
                <Loader2
                  v-if="loading || isCropping"
                  :size="15"
                  :stroke-width="2.25"
                  class="animate-spin"
                />
                <Check v-else :size="15" :stroke-width="2.5" />
                <span>
                  <template v-if="loading">保存中</template>
                  <template v-else-if="isCropping">裁剪中</template>
                  <template v-else-if="phase === 'pick'">先选择图片</template>
                  <template v-else>确认上传</template>
                </span>
              </button>
            </footer>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* 遮罩：纯黑半透明，无毛玻璃 */
.aud-mask {
  background-color: rgb(0 0 0 / 0.6);
}
:where(html.dark) .aud-mask {
  background-color: rgb(0 0 0 / 0.78);
}

/* 卡片阴影 */
.aud-card {
  box-shadow: 0 24px 60px -20px rgb(0 0 0 / 0.35);
}
:where(html.dark) .aud-card {
  box-shadow: 0 24px 60px -20px rgb(0 0 0 / 0.85);
}

/* 主按钮：黑/白反色 */
.aud-primary {
  background: #000;
  color: #fff;
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.06),
    0 1px 2px rgb(0 0 0 / 0.18);
}
.aud-primary:hover:not(:disabled) {
  background: #1a1a1a;
  transform: translateY(-0.5px);
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.08),
    0 6px 14px -6px rgb(0 0 0 / 0.35);
}
.aud-primary:active:not(:disabled) {
  background: #000;
  transform: translateY(0);
}
.aud-primary:disabled {
  background: rgb(0 0 0 / 0.08);
  color: rgb(0 0 0 / 0.35);
  box-shadow: none;
}
:where(html.dark) .aud-primary {
  background: #fff;
  color: #000;
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.6),
    0 1px 2px rgb(0 0 0 / 0.4);
}
:where(html.dark) .aud-primary:hover:not(:disabled) {
  background: #f0f0f0;
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.7),
    0 6px 14px -6px rgb(0 0 0 / 0.5);
}
:where(html.dark) .aud-primary:active:not(:disabled) {
  background: #fff;
}
:where(html.dark) .aud-primary:disabled {
  background: rgb(255 255 255 / 0.08);
  color: rgb(255 255 255 / 0.35);
  box-shadow: none;
}

/* 取消按钮 */
.aud-cancel {
  color: rgb(0 0 0 / 0.65);
  transition:
    background-color 140ms ease,
    color 140ms ease;
}
.aud-cancel:hover:not(:disabled) {
  background: rgb(0 0 0 / 0.05);
  color: black;
}
:where(html.dark) .aud-cancel {
  color: rgb(255 255 255 / 0.6);
}
:where(html.dark) .aud-cancel:hover:not(:disabled) {
  background: rgb(255 255 255 / 0.06);
  color: white;
}

/* 链接样次按钮 */
.aud-link-btn {
  color: rgb(0 0 0 / 0.6);
  transition:
    background-color 140ms ease,
    color 140ms ease;
}
.aud-link-btn:hover:not(:disabled) {
  background: rgb(0 0 0 / 0.05);
  color: black;
}
:where(html.dark) .aud-link-btn {
  color: rgb(255 255 255 / 0.6);
}
:where(html.dark) .aud-link-btn:hover:not(:disabled) {
  background: rgb(255 255 255 / 0.06);
  color: white;
}

/* 滚动条 */
.aud-body {
  scrollbar-width: thin;
  scrollbar-color: rgb(0 0 0 / 0.18) transparent;
}
.aud-body::-webkit-scrollbar {
  width: 6px;
}
.aud-body::-webkit-scrollbar-thumb {
  background: rgb(0 0 0 / 0.18);
  border-radius: 3px;
}
:where(html.dark) .aud-body {
  scrollbar-color: rgb(255 255 255 / 0.15) transparent;
}
:where(html.dark) .aud-body::-webkit-scrollbar-thumb {
  background: rgb(255 255 255 / 0.15);
}

/* 动画 */
.aud-fade-enter-active,
.aud-fade-leave-active {
  transition: opacity 200ms ease;
}
.aud-fade-enter-from,
.aud-fade-leave-to {
  opacity: 0;
}

.aud-pop-enter-active {
  transition:
    opacity 220ms ease,
    transform 280ms cubic-bezier(0.22, 1, 0.36, 1);
}
.aud-pop-leave-active {
  transition:
    opacity 160ms ease,
    transform 200ms ease;
}
.aud-pop-enter-from {
  opacity: 0;
  transform: translateY(16px) scale(0.98);
}
.aud-pop-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.985);
}
@media (min-width: 640px) {
  .aud-pop-enter-from {
    transform: translateY(8px) scale(0.97);
  }
}

.aud-error-enter-active,
.aud-error-leave-active {
  transition:
    opacity 160ms ease,
    transform 160ms ease;
}
.aud-error-enter-from,
.aud-error-leave-to {
  opacity: 0;
  transform: translateY(-2px);
}

@media (prefers-reduced-motion: reduce) {
  .aud-fade-enter-active,
  .aud-fade-leave-active,
  .aud-pop-enter-active,
  .aud-pop-leave-active,
  .aud-error-enter-active,
  .aud-error-leave-active {
    transition: none;
  }
}
</style>
