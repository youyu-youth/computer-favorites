<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Star } from 'lucide-vue-next'
import UModal from '@/components/ui-adapter/UModal.vue'

interface Props {
  open: boolean
  websiteName?: string
  currentRating?: number
}

const props = withDefaults(defineProps<Props>(), {
  websiteName: '',
  currentRating: 0,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', payload: { rating: number; comment?: string }): void
}>()

const MAX_STARS = 5

const hoverRating = ref(0)
const selectedRating = ref(props.currentRating)
const commentText = ref('')
let previousScrollY = 0

const ratingLabels: Record<number, string> = {
  1: '较差',
  2: '一般',
  3: '不错',
  4: '很好',
  5: '太棒了！',
}

const displayRating = computed(() => {
  if (hoverRating.value > 0) return hoverRating.value
  return selectedRating.value
})

const ratingLabel = computed(() => {
  const r = displayRating.value
  return r > 0 ? ratingLabels[r] : '点击星星评分'
})

const canSubmit = computed(() => selectedRating.value > 0)

function getStarState(index: number): 'full' | 'empty' {
  return index <= displayRating.value ? 'full' : 'empty'
}

function onStarEnter(index: number) {
  hoverRating.value = index
}

function onStarLeave() {
  hoverRating.value = 0
}

function onStarClick(index: number) {
  if (selectedRating.value === index) {
    selectedRating.value = 0
  } else {
    selectedRating.value = index
  }
}

function lockPageScroll() {
  if (typeof window === 'undefined') return
  previousScrollY = window.scrollY
  const scrollValue = `${previousScrollY}px`
  document.documentElement.classList.add('rating-modal-scroll-lock')
  document.body.classList.add('rating-modal-scroll-lock')
  document.body.style.setProperty('--rating-modal-scroll-y', scrollValue)
}

function unlockPageScroll() {
  if (typeof window === 'undefined') return
  document.documentElement.classList.remove('rating-modal-scroll-lock')
  document.body.classList.remove('rating-modal-scroll-lock')
  document.body.style.removeProperty('--rating-modal-scroll-y')
  window.scrollTo({ top: previousScrollY, left: 0, behavior: 'auto' })
}

function handleOpenChange(value: boolean) {
  emit('update:open', value)
}

function handleSubmit() {
  if (!canSubmit.value) return
  emit('submit', {
    rating: selectedRating.value,
    comment: commentText.value.trim() || undefined,
  })
  emit('update:open', false)
}

function resetForm() {
  selectedRating.value = props.currentRating
  hoverRating.value = 0
  commentText.value = ''
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      lockPageScroll()
      resetForm()
    } else {
      unlockPageScroll()
    }
  },
)

defineOptions({
  name: 'StarRatingDialog',
})
</script>

<template>
  <UModal
    :open="open"
    :portal="true"
    :title="websiteName ? `为 ${websiteName} 评分` : '评分'"
    :ui="{
      overlay:
        'rating-modal-overlay z-[120] bg-zinc-900/35 dark:bg-zinc-950/65 backdrop-blur-xs',
      content:
        'rating-modal-content z-[130] !m-0 flex h-auto max-h-[90vh] w-[min(92vw,26rem)] flex-col overflow-hidden rounded-[2rem] bg-white text-zinc-800 shadow-[0_20px_50px_-12px_rgba(0,0,0,0.08),inset_0_1px_0_rgba(255,255,255,0.6)] dark:bg-zinc-900 dark:text-zinc-200 dark:shadow-[0_24px_60px_rgba(0,0,0,0.5),inset_0_1px_0_rgba(255,255,255,0.06)]',
      header:
        'rating-modal-header px-6 pt-6 pb-1 bg-transparent',
      title:
        'rating-modal-title text-lg font-bold tracking-tight text-zinc-800 dark:text-zinc-100',
      description: 'hidden',
      body: 'rating-modal-body px-6 py-5 bg-transparent',
      footer:
        'rating-modal-footer mt-auto px-6 pb-6 pt-2 bg-transparent',
    }"
    @update:open="handleOpenChange"
  >
    <template #header>
      <div class="flex w-full items-center justify-between gap-3">
        <h2 class="text-lg font-bold tracking-tight text-zinc-800 dark:text-zinc-100">
          {{ websiteName ? `为 ${websiteName} 评分` : '评分' }}
        </h2>
        <button
          type="button"
          class="flex h-9 w-9 cursor-pointer items-center justify-center rounded-full text-zinc-400 transition-all duration-200 hover:bg-zinc-100 hover:text-zinc-600 active:scale-95 dark:text-zinc-500 dark:hover:bg-zinc-800 dark:hover:text-zinc-300"
          aria-label="关闭"
          @click="emit('update:open', false)"
        >
          <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </template>

    <template #body>
      <div class="flex flex-col items-center gap-6">
        <p
          v-if="websiteName"
          class="text-sm font-medium text-zinc-500 dark:text-zinc-400"
        >
          {{ websiteName }}
        </p>

        <div
          class="flex items-center gap-2.5"
          @mouseleave="onStarLeave"
        >
          <button
            v-for="index in MAX_STARS"
            :key="index"
            type="button"
            class="star-btn cursor-pointer border-0 bg-transparent p-0 outline-none transition-all duration-300"
            :class="getStarState(index) === 'full' ? 'scale-110' : 'scale-100 hover:scale-105'"
            :style="{ transitionTimingFunction: 'cubic-bezier(0.34, 1.56, 0.64, 1)' }"
            :aria-label="`${index} 星`"
            @mouseenter="onStarEnter(index)"
            @click="onStarClick(index)"
          >
            <Star
              class="star-icon h-10 w-10 transition-all duration-300"
              :class="
                getStarState(index) === 'full'
                  ? 'star-full'
                  : 'star-empty'
              "
              :fill="getStarState(index) === 'full' ? 'currentColor' : 'none'"
              :stroke-width="getStarState(index) === 'full' ? 0 : 1.25"
            />
          </button>
        </div>

        <p
          class="text-sm font-semibold transition-colors duration-200"
          :class="
            displayRating > 0
              ? 'text-amber-500 dark:text-amber-400'
              : 'text-zinc-300 dark:text-zinc-600'
          "
        >
          {{ ratingLabel }}
        </p>

        <div class="w-full space-y-2">
          <label
            for="rating-comment"
            class="block text-xs font-medium text-zinc-500 dark:text-zinc-400"
          >
            评价（可选）
          </label>
          <textarea
            id="rating-comment"
            v-model="commentText"
            rows="3"
            maxlength="300"
            class="w-full resize-none rounded-2xl bg-zinc-50 px-4 py-3 text-sm text-zinc-800 outline-none ring-0 transition-all duration-200 placeholder:text-zinc-400 focus:bg-zinc-100 dark:bg-zinc-800 dark:text-zinc-200 dark:placeholder:text-zinc-500 dark:focus:bg-zinc-800/70"
            placeholder="分享你的使用体验..."
          />
          <p class="text-right text-xs text-zinc-400 dark:text-zinc-500">
            {{ commentText.length }} / 300
          </p>
        </div>
      </div>
    </template>

    <template #footer>
      <div class="flex w-full items-center justify-end gap-3">
        <button
          type="button"
          class="cursor-pointer rounded-xl bg-transparent px-5 py-2.5 text-sm font-medium text-zinc-500 transition-all duration-200 hover:bg-zinc-100 hover:text-zinc-700 active:scale-[0.98] dark:text-zinc-400 dark:hover:bg-zinc-800 dark:hover:text-zinc-200"
          @click="emit('update:open', false)"
        >
          取消
        </button>
        <button
          type="button"
          class="cursor-pointer rounded-xl px-5 py-2.5 text-sm font-semibold transition-all duration-200 active:scale-[0.98]"
          :class="
            canSubmit
              ? 'bg-amber-500 text-white shadow-[0_2px_12px_rgba(245,158,11,0.25)] hover:bg-amber-400 hover:shadow-[0_4px_20px_rgba(245,158,11,0.35)] dark:bg-amber-400 dark:text-zinc-900 dark:hover:bg-amber-300'
              : 'cursor-not-allowed bg-zinc-200 text-zinc-400 dark:bg-zinc-800 dark:text-zinc-600'
          "
          :disabled="!canSubmit"
          @click="handleSubmit"
        >
          提交评分
        </button>
      </div>
    </template>
  </UModal>
</template>

<style scoped>
.star-full {
  color: #f59e0b;
}

:global(.dark) .star-full {
  color: #fbbf24;
}

.star-empty {
  color: #d4d4d8;
}

:global(.dark) .star-empty {
  color: #52525b;
}

:global(html.rating-modal-scroll-lock) {
  overflow: hidden;
}

:global(body.rating-modal-scroll-lock) {
  position: fixed;
  top: calc(var(--rating-modal-scroll-y, 0px) * -1);
  left: 0;
  right: 0;
  width: 100%;
  overflow: hidden;
  touch-action: none;
}
</style>
