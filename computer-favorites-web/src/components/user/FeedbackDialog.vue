<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import UModal from '@/components/ui-adapter/UModal.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from 'vue-i18n'
import type { UserFeedbackCreateRequest } from '@/types/notification'

interface Props {
  open: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submitted', payload: UserFeedbackCreateRequest): void
}>()

const { t } = useI18n()
const toast = useToast()

const feedbackTypeOptions: Array<{ value: 1 | 2 | 3 | 4; labelKey: string }> = [
  { value: 1, labelKey: 'user.home.feedback.typeSuggestion' },
  { value: 2, labelKey: 'user.home.feedback.typeBug' },
  { value: 3, labelKey: 'user.home.feedback.typeComplaint' },
  { value: 4, labelKey: 'user.home.feedback.typeExperience' },
]

const selectedType = ref<1 | 2 | 3 | 4>(1)
const feedbackContent = ref('')
const contact = ref('')
const imageUrls = ref<string[]>([])
const isSubmitting = ref(false)
const contentMaxLength = 500
const contentMinLength = 10
const contactMaxLength = 100
const maxImageCount = 5
let previousScrollY = 0

const contentLength = computed(() => feedbackContent.value.trim().length)
const canSubmit = computed(() => {
  return (
    !isSubmitting.value &&
    contentLength.value >= contentMinLength &&
    contentLength.value <= contentMaxLength &&
    contact.value.trim().length <= contactMaxLength
  )
})

const resetForm = () => {
  selectedType.value = 1
  feedbackContent.value = ''
  contact.value = ''
  imageUrls.value.forEach((url) => URL.revokeObjectURL(url))
  imageUrls.value = []
}

const closeDialog = () => {
  emit('update:open', false)
}

const lockPageScroll = () => {
  if (typeof window === 'undefined') {
    return
  }
  previousScrollY = window.scrollY
  const scrollValue = `${previousScrollY}px`
  document.documentElement.classList.add('feedback-modal-scroll-lock')
  document.body.classList.add('feedback-modal-scroll-lock')
  document.body.style.setProperty('--feedback-modal-scroll-y', scrollValue)
}

const unlockPageScroll = () => {
  if (typeof window === 'undefined') {
    return
  }
  document.documentElement.classList.remove('feedback-modal-scroll-lock')
  document.body.classList.remove('feedback-modal-scroll-lock')
  document.body.style.removeProperty('--feedback-modal-scroll-y')
  window.scrollTo({ top: previousScrollY, left: 0, behavior: 'auto' })
}

const handleImageUpload = (event: Event) => {
  const input = event.target as HTMLInputElement
  const files = input.files
  if (!files || files.length === 0) {
    return
  }

  const remainingCount = Math.max(0, maxImageCount - imageUrls.value.length)
  const selectedFiles = Array.from(files).slice(0, remainingCount)
  selectedFiles.forEach((file) => {
    const objectUrl = URL.createObjectURL(file)
    imageUrls.value.push(objectUrl)
  })

  input.value = ''
}

const removeImage = (index: number) => {
  const removed = imageUrls.value.splice(index, 1)
  removed.forEach((url) => URL.revokeObjectURL(url))
}

const submitFeedbackMock = async (): Promise<void> => {
  await new Promise((resolve) => window.setTimeout(resolve, 600))
}

const handleSubmit = async () => {
  if (!canSubmit.value) {
    return
  }

  const payload: UserFeedbackCreateRequest = {
    type: selectedType.value,
    content: feedbackContent.value.trim(),
    contact: contact.value.trim() || undefined,
    images: imageUrls.value.length > 0 ? [...imageUrls.value] : undefined,
  }

  isSubmitting.value = true
  try {
    await submitFeedbackMock()
    toast.add({
      title: t('common.success'),
      description: t('user.home.feedback.submitSuccess'),
      type: 'success',
    })
    emit('submitted', payload)
    resetForm()
    closeDialog()
  } catch {
    toast.add({
      title: t('common.error'),
      description: t('user.home.feedback.submitFailed'),
      type: 'error',
    })
  } finally {
    isSubmitting.value = false
  }
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      lockPageScroll()
      return
    }
    unlockPageScroll()
    if (!open) {
      resetForm()
    }
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  unlockPageScroll()
})

defineOptions({
  name: 'FeedbackDialog',
})
</script>

<template>
  <UModal
    :open="open"
    :portal="true"
    :title="t('user.home.feedback.dialogTitle')"
    :description="''"
    :ui="{
      overlay: 'feedback-modal-overlay z-[120] bg-black/45 dark:bg-black/75 backdrop-blur-sm',
      content:
        'feedback-modal-content z-[130] !m-0 flex h-[calc(100dvh-1rem)] max-h-[calc(100dvh-1rem)] w-screen flex-col overflow-hidden rounded-t-2xl border border-[#f59e0b]/20 bg-[#fffdf7] text-[#2a1b05] shadow-[0_24px_80px_rgba(245,158,11,0.2)] sm:!h-auto sm:!max-h-[85vh] sm:!w-[min(92vw,46rem)] sm:rounded-2xl dark:border-[#f59e0b]/28 dark:bg-black dark:text-[#ffe6b0] dark:shadow-[0_20px_80px_rgba(0,0,0,0.9)]',
      header:
        'feedback-modal-header px-4 pt-5 pb-2 sm:px-8 bg-[#fffdf7] dark:bg-black',
      title: 'feedback-modal-title text-xl font-bold text-[#1f1404] dark:text-[#fff8e3]',
      description: 'hidden',
      body: 'feedback-modal-body overflow-y-auto flex-1 px-4 py-4 sm:px-8 bg-[#fffdf7] dark:bg-black',
      footer:
        'feedback-modal-footer mt-auto px-4 pb-4 pt-2 sm:px-8 sm:pb-6 bg-[#fffdf7] dark:bg-black',
    }"
    @update:open="emit('update:open', $event)"
  >
    <template #header>
      <div class="flex w-full items-center justify-between gap-3">
        <h2 class="text-xl font-bold text-[#1f1404] dark:text-[#fff8e3]">
          {{ t('user.home.feedback.dialogTitle') }}
        </h2>
        <button
          type="button"
          class="flex h-9 w-9 cursor-pointer items-center justify-center rounded-full text-[#8b5b10] transition-colors hover:bg-[#f59e0b]/12 hover:text-[#7a4b00] dark:text-[#d6ad59] dark:hover:bg-[#111] dark:hover:text-[#ffd37f]"
          :aria-label="t('common.close')"
          @click="closeDialog"
        >
          <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </template>
    <template #body>
      <div class="flex flex-col gap-6">
        <div
          class="flex items-start gap-3 rounded-xl border border-[#f59e0b]/25 bg-[#f59e0b]/10 p-4 text-[#7a4b00] dark:border-[#f59e0b]/30 dark:bg-[#f59e0b]/12 dark:text-[#e4b35a]"
        >
          <div class="mt-0.5 text-[#f59e0b]">
            <svg class="h-5 w-5 fill-current" viewBox="0 0 24 24">
              <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 10H6v-2h12v2zm0-3H6V7h12v2z" />
            </svg>
          </div>
          <div>
            <p class="text-sm font-semibold text-[#8a5200] dark:text-[#f0c467]">
              欢迎提供反馈
            </p>
            <p class="mt-1 text-xs leading-relaxed text-[#8a5a10] dark:text-[#bc9345]">
              {{ t('user.home.feedback.dialogDescription') }}
            </p>
          </div>
        </div>

        <div class="space-y-2">
          <label
            for="feedback-type"
            class="block text-sm font-medium text-[#6d4510] dark:text-[#dcb066]"
          >
            {{ t('user.home.feedback.typeLabel') }}
            <span class="required-asterisk">*</span>
          </label>
          <div id="feedback-type" class="grid grid-cols-2 gap-3 sm:grid-cols-4">
            <button
              v-for="option in feedbackTypeOptions"
              :key="option.value"
              type="button"
              class="cursor-pointer rounded-xl border px-3 py-2.5 text-center text-sm font-medium transition-all duration-200"
              :class="
                selectedType === option.value
                  ? 'border-[#f59e0b] bg-[#f59e0b] text-white shadow-[0_8px_24px_rgba(245,158,11,0.35)] dark:text-black'
                  : 'border-[#f59e0b]/24 bg-transparent text-[#8a5a10] hover:border-[#f59e0b]/60 hover:text-[#6f4300] dark:border-[#2b2b2b] dark:text-[#a98647] dark:hover:border-[#f59e0b]/55 dark:hover:text-[#f4c66f]'
              "
              @click="selectedType = option.value"
            >
              {{ t(option.labelKey) }}
            </button>
          </div>
        </div>

        <div class="space-y-2">
          <label
            for="feedback-content"
            class="block text-sm font-medium text-[#6d4510] dark:text-[#dcb066]"
          >
            {{ t('user.home.feedback.contentLabel') }}
            <span class="required-asterisk">*</span>
          </label>
          <textarea
            id="feedback-content"
            v-model="feedbackContent"
            rows="5"
            :maxlength="contentMaxLength"
            class="w-full rounded-xl border border-[#f59e0b]/25 bg-transparent p-4 text-sm text-[#2a1b05] outline-none transition-all placeholder:text-[#b6863b] focus:border-[#f59e0b] focus:ring-1 focus:ring-[#f59e0b] dark:border-[#2c2c2c] dark:text-[#f2d39b] dark:placeholder:text-[#6e572f] dark:focus:border-[#f59e0b]"
            :placeholder="t('user.home.feedback.contentPlaceholder')"
          />
          <div class="mt-2 flex items-center justify-between px-1">
            <p
              class="text-xs"
              :class="
                contentLength < contentMinLength
                  ? 'text-red-500'
                  : 'text-[#9a6509] dark:text-[#c3994a]'
              "
            >
              {{
                t('user.home.feedback.contentCount', {
                  current: contentLength,
                  min: contentMinLength,
                  max: contentMaxLength,
                })
              }}
            </p>
            <span class="text-xs font-mono text-[#c39850] dark:text-[#7d6338]">
              {{ feedbackContent.length }} / {{ contentMaxLength }}
            </span>
          </div>
        </div>

        <div class="space-y-2">
          <label
            for="feedback-contact"
            class="block text-sm font-medium text-[#6d4510] dark:text-[#dcb066]"
          >
            {{ t('user.home.feedback.contactLabel') }}
          </label>
          <input
            id="feedback-contact"
            v-model="contact"
            type="text"
            :maxlength="contactMaxLength"
            class="w-full rounded-xl border border-[#f59e0b]/25 bg-transparent px-4 py-3.5 text-sm text-[#2a1b05] outline-none transition-all placeholder:text-[#b6863b] focus:border-[#f59e0b] focus:ring-1 focus:ring-[#f59e0b] dark:border-[#2c2c2c] dark:text-[#f2d39b] dark:placeholder:text-[#6e572f] dark:focus:border-[#f59e0b]"
            :placeholder="t('user.home.feedback.contactPlaceholder')"
          />
        </div>

        <div class="space-y-2">
          <p class="text-sm font-medium text-[#6d4510] dark:text-[#dcb066]">
            {{ t('user.home.feedback.imagesLabel') }}
          </p>
          <div class="flex flex-wrap gap-2">
            <div
              v-for="(url, index) in imageUrls"
              :key="url"
              class="group relative h-[4.5rem] w-[4.5rem] overflow-hidden rounded-xl border border-[#f59e0b]/30 dark:border-[#2c2c2c]"
            >
              <img
                :src="url"
                :alt="t('user.home.feedback.imageAlt', { index: index + 1 })"
                class="h-full w-full object-cover"
              />
              <button
                type="button"
                :aria-label="t('user.home.feedback.removeImage', { index: index + 1 })"
                class="absolute right-1 top-1 hidden h-5 w-5 cursor-pointer items-center justify-center rounded-full bg-black/75 text-[10px] text-white group-hover:flex group-focus-within:flex"
                @click="removeImage(index)"
              >
                x
              </button>
            </div>
            <label
              v-if="imageUrls.length < maxImageCount"
              class="flex h-[4.5rem] w-[4.5rem] cursor-pointer items-center justify-center rounded-xl border border-dashed border-[#f59e0b]/35 text-center text-xs font-medium text-[#a06d1c] transition-colors hover:border-[#f59e0b] hover:bg-[#f59e0b]/10 dark:border-[#3a2d13] dark:text-[#b38d4a] dark:hover:border-[#f59e0b]/55 dark:hover:bg-[#111]"
            >
              <span>{{ t('user.home.feedback.uploadAction') }}</span>
              <input type="file" accept="image/*" multiple class="hidden" @change="handleImageUpload" />
            </label>
          </div>
        </div>
      </div>
    </template>

    <template #footer>
      <div class="flex w-full flex-col-reverse items-stretch justify-end gap-2 sm:flex-row sm:items-center">
        <button
          type="button"
          class="rounded-xl border border-[#f59e0b]/24 bg-transparent px-6 py-2.5 text-sm font-medium text-[#8a5a10] transition-colors hover:bg-[#f59e0b]/8 dark:border-[#2c2c2c] dark:text-[#b8924d] dark:hover:bg-[#101010]"
          :disabled="isSubmitting"
          @click="closeDialog"
        >
          {{ t('common.cancel') }}
        </button>
        <button
          type="button"
          class="rounded-xl bg-[#f59e0b] px-6 py-2.5 text-sm font-semibold text-white transition-all hover:translate-y-[-1px] hover:bg-[#e58f00] disabled:cursor-not-allowed disabled:bg-[#f59e0b]/35 disabled:text-[#fff3d6] dark:text-black dark:disabled:text-[#8a6a27]"
          :disabled="!canSubmit"
          @click="handleSubmit"
        >
          {{ isSubmitting ? t('common.loading') : t('user.home.feedback.submitAction') }}
        </button>
      </div>
    </template>
  </UModal>
</template>

<style scoped>
:global(html.feedback-modal-scroll-lock) {
  overflow: hidden;
}

:global(body.feedback-modal-scroll-lock) {
  position: fixed;
  top: calc(var(--feedback-modal-scroll-y, 0px) * -1);
  left: 0;
  right: 0;
  width: 100%;
  overflow: hidden;
  touch-action: none;
}

:global(.dark .feedback-modal-content) {
  color: #fff !important;
}

:global(.dark .feedback-modal-content :is(h1, h2, h3, h4, h5, h6, p, label, span, button, input, textarea)) {
  color: #fff !important;
}

:global(.dark .feedback-modal-content :is(div, button, input, textarea, label, p, span)) {
  border-color: transparent !important;
}

:global(.dark .feedback-modal-content input::placeholder),
:global(.dark .feedback-modal-content textarea::placeholder) {
  color: rgb(255 255 255 / 0.62) !important;
}

.required-asterisk {
  color: #ef4444 !important;
}

:global(.dark .feedback-modal-content span.required-asterisk) {
  color: #ef4444 !important;
}
</style>
