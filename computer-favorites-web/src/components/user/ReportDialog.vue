<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { X, AlertTriangle, ImagePlus, Trash2 } from 'lucide-vue-next'
import type { ReportFormData, ReportReasonOption } from '@/types/report'
import { ReportType } from '@/types/report'

interface ReportDialogProps {
  visible: boolean
  websiteId: number | null
  websiteName?: string
}

interface ReportDialogEmits {
  (e: 'update:visible', value: boolean): void
  (e: 'submit', data: ReportFormData): void
}

const props = defineProps<ReportDialogProps>()
const emit = defineEmits<ReportDialogEmits>()

const reportReasons: ReportReasonOption[] = [
  { value: 1, label: '虚假信息', description: '网站内容与实际不符或存在误导' },
  { value: 2, label: '违法违规', description: '包含违法、违规或不良内容' },
  { value: 3, label: '侵权内容', description: '侵犯知识产权或个人权益' },
  { value: 4, label: '恶意链接', description: '包含病毒、木马或钓鱼链接' },
  { value: 5, label: '广告垃圾', description: '过度营销或垃圾广告内容' },
  { value: 6, label: '其他问题', description: '其他需要说明的问题' },
]

const selectedReason = ref<number | null>(null)
const reasonDetail = ref('')
const uploadedImages = ref<string[]>([])
const isSubmitting = ref(false)

const canSubmit = computed(() => {
  return selectedReason.value !== null && reasonDetail.value.trim().length >= 10
})

const closeDialog = () => {
  emit('update:visible', false)
}

const handleReasonSelect = (value: number) => {
  selectedReason.value = value
}

const handleImageUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files || files.length === 0) return

  // Mock upload - in real implementation, upload to server
  Array.from(files).forEach((file) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      if (e.target?.result && uploadedImages.value.length < 5) {
        uploadedImages.value.push(e.target.result as string)
      }
    }
    reader.readAsDataURL(file)
  })

  // Reset input
  target.value = ''
}

const removeImage = (index: number) => {
  uploadedImages.value.splice(index, 1)
}

const handleSubmit = async () => {
  if (!canSubmit.value || !props.websiteId || selectedReason.value === null) return

  isSubmitting.value = true

  // Simulate API call
  await new Promise((resolve) => setTimeout(resolve, 1000))

  const formData: ReportFormData = {
    type: ReportType.WEBSITE,
    targetId: props.websiteId,
    reason: `${reportReasons.find((r) => r.value === selectedReason.value)?.label}: ${reasonDetail.value}`,
    images: uploadedImages.value,
  }

  emit('submit', formData)
  isSubmitting.value = false

  // Reset form
  selectedReason.value = null
  reasonDetail.value = ''
  uploadedImages.value = []
  closeDialog()
}

// Reset form when dialog closes
watch(
  () => props.visible,
  (newVal) => {
    if (!newVal) {
      selectedReason.value = null
      reasonDetail.value = ''
      uploadedImages.value = []
    }
  },
)
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-300"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-200"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="visible"
        class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/60 backdrop-blur-sm px-4"
        @click.self="closeDialog"
      >
        <Transition
          enter-active-class="transition-all duration-300 ease-out"
          enter-from-class="opacity-0 scale-95 translate-y-4"
          enter-to-class="opacity-100 scale-100 translate-y-0"
          leave-active-class="transition-all duration-200 ease-in"
          leave-from-class="opacity-100 scale-100 translate-y-0"
          leave-to-class="opacity-0 scale-95 translate-y-4"
        >
          <div
            v-if="visible"
            class="relative w-full max-w-2xl max-h-[90vh] overflow-hidden rounded-lg shadow-2xl bg-white dark:bg-[#1f1f1f]"
            @click.stop
          >
            <!-- Header -->
            <div
              class="relative flex items-center justify-between border-b px-6 py-4 bg-gray-50 border-gray-200 dark:bg-[#1a1a1a] dark:border-[#d97706]/20"
            >
              <div class="flex items-center gap-3">
                <div
                  class="flex h-10 w-10 items-center justify-center rounded-full bg-[#d97706]/10 dark:bg-[#d97706]/15"
                >
                  <AlertTriangle class="h-5 w-5 text-[#d97706]" />
                </div>
                <div>
                  <h2 class="text-lg font-bold text-gray-900 dark:text-white">举报网站</h2>
                  <p v-if="websiteName" class="text-xs text-gray-600 dark:text-gray-400">
                    {{ websiteName }}
                  </p>
                </div>
              </div>
              <button
                class="flex h-8 w-8 cursor-pointer items-center justify-center rounded-full transition-colors text-gray-600 hover:bg-gray-200 dark:text-gray-400 dark:hover:bg-gray-700/50"
                @click="closeDialog"
              >
                <X class="h-5 w-5" />
              </button>
            </div>

            <!-- Content -->
            <div class="overflow-y-auto p-6 bg-white dark:bg-[#1f1f1f]" style="max-height: calc(90vh - 180px)">
              <!-- Reason Selection -->
              <div class="mb-6">
                <label class="mb-3 block text-sm font-semibold text-gray-900 dark:text-white">
                  举报原因 <span class="text-red-500">*</span>
                </label>
                <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <button
                    v-for="reason in reportReasons"
                    :key="reason.value"
                    class="group cursor-pointer rounded-lg border-2 p-4 text-left transition-all"
                    :class="
                      selectedReason === reason.value
                        ? 'border-[#d97706] bg-[#d97706]/5 dark:bg-[#d97706]/10'
                        : 'border-gray-200 bg-white hover:border-[#d97706]/50 dark:border-gray-700 dark:bg-[#262626] dark:hover:border-[#d97706]/50'
                    "
                    @click="handleReasonSelect(reason.value)"
                  >
                    <div class="mb-1 flex items-center justify-between">
                      <span
                        class="font-medium"
                        :class="
                          selectedReason === reason.value
                            ? 'text-[#d97706]'
                            : 'text-gray-900 dark:text-white'
                        "
                      >
                        {{ reason.label }}
                      </span>
                      <div
                        class="h-4 w-4 rounded-full border-2 transition-all"
                        :class="
                          selectedReason === reason.value
                            ? 'border-[#d97706] bg-[#d97706]'
                            : 'border-gray-300 dark:border-gray-600'
                        "
                      >
                        <div
                          v-if="selectedReason === reason.value"
                          class="h-full w-full rounded-full bg-white dark:bg-[#1f1f1f]"
                          style="transform: scale(0.5)"
                        ></div>
                      </div>
                    </div>
                    <p class="text-xs text-gray-600 dark:text-gray-400">
                      {{ reason.description }}
                    </p>
                  </button>
                </div>
              </div>

              <!-- Detail Description -->
              <div class="mb-6">
                <label class="mb-2 block text-sm font-semibold text-gray-900 dark:text-white">
                  详细说明 <span class="text-red-500">*</span>
                  <span class="ml-2 text-xs font-normal text-gray-500 dark:text-gray-400">
                    (至少10个字符)
                  </span>
                </label>
                <textarea
                  v-model="reasonDetail"
                  placeholder="请详细描述您举报的原因，以便我们更好地处理..."
                  rows="5"
                  class="w-full rounded-lg border-2 px-4 py-3 text-sm transition-colors focus:outline-none border-gray-200 bg-white text-gray-900 placeholder-gray-400 focus:border-[#d97706] dark:border-gray-700 dark:bg-[#262626] dark:text-white dark:placeholder-gray-500 dark:focus:border-[#d97706]"
                  maxlength="500"
                ></textarea>
                <div class="mt-1 text-right text-xs text-gray-500 dark:text-gray-400">
                  {{ reasonDetail.length }} / 500
                </div>
              </div>

              <!-- Image Upload -->
              <div class="mb-4">
                <label class="mb-2 block text-sm font-semibold text-gray-900 dark:text-white">
                  上传截图证据
                  <span class="ml-2 text-xs font-normal text-gray-500 dark:text-gray-400">
                    (选填，最多5张)
                  </span>
                </label>
                <div class="grid grid-cols-3 gap-3 sm:grid-cols-5">
                  <div
                    v-for="(image, index) in uploadedImages"
                    :key="index"
                    class="group relative aspect-square overflow-hidden rounded-lg border-2 border-gray-200 dark:border-gray-700"
                  >
                    <img :src="image" alt="证据截图" class="h-full w-full object-cover" />
                    <button
                      class="absolute right-1 top-1 flex h-6 w-6 cursor-pointer items-center justify-center rounded-full bg-red-500 opacity-0 transition-opacity group-hover:opacity-100"
                      @click="removeImage(index)"
                    >
                      <Trash2 class="h-3 w-3 text-white" />
                    </button>
                  </div>
                  <label
                    v-if="uploadedImages.length < 5"
                    class="flex aspect-square cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed transition-colors border-gray-300 bg-gray-50 hover:border-[#d97706] hover:bg-[#d97706]/5 dark:border-gray-600 dark:bg-[#262626] dark:hover:border-[#d97706] dark:hover:bg-[#d97706]/10"
                  >
                    <ImagePlus class="mb-1 h-6 w-6 text-gray-400 dark:text-gray-500" />
                    <span class="text-xs text-gray-500 dark:text-gray-400">上传</span>
                    <input
                      type="file"
                      accept="image/*"
                      multiple
                      class="hidden"
                      @change="handleImageUpload"
                    />
                  </label>
                </div>
              </div>

              <!-- Notice -->
              <div
                class="rounded-lg border p-4 border-blue-200 bg-blue-50 dark:border-blue-500/30 dark:bg-blue-500/10"
              >
                <p class="text-xs leading-relaxed text-blue-800 dark:text-blue-300">
                  <strong>温馨提示：</strong>
                  我们会认真审核每一条举报信息。恶意举报或虚假举报可能会影响您的账号信誉。感谢您为社区建设做出的贡献！
                </p>
              </div>
            </div>

            <!-- Footer -->
            <div
              class="flex items-center justify-end gap-3 border-t px-6 py-4 bg-gray-50 border-gray-200 dark:bg-[#1a1a1a] dark:border-[#d97706]/20"
            >
              <button
                class="cursor-pointer rounded-lg border-2 px-6 py-2.5 text-sm font-medium transition-colors border-gray-300 bg-white text-gray-700 hover:bg-gray-100 dark:border-gray-600 dark:bg-[#262626] dark:text-gray-200 dark:hover:bg-gray-700"
                @click="closeDialog"
              >
                取消
              </button>
              <button
                class="cursor-pointer rounded-lg px-6 py-2.5 text-sm font-bold text-white transition-all"
                :class="
                  canSubmit && !isSubmitting
                    ? 'bg-[#d97706] hover:bg-[#b45309]'
                    : 'bg-gray-300 cursor-not-allowed dark:bg-gray-700'
                "
                :disabled="!canSubmit || isSubmitting"
                @click="handleSubmit"
              >
                {{ isSubmitting ? '提交中...' : '提交举报' }}
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* Smooth scrollbar */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(156, 163, 175, 0.5);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(156, 163, 175, 0.7);
}

.dark ::-webkit-scrollbar-thumb {
  background: rgba(75, 85, 99, 0.5);
}

.dark ::-webkit-scrollbar-thumb:hover {
  background: rgba(75, 85, 99, 0.7);
}
</style>
