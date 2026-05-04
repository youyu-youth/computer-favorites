<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { Smile, Send } from 'lucide-vue-next'
import UTextarea from '@/components/ui-adapter/UTextarea.vue'
import UButton from '@/components/ui-adapter/UButton.vue'
import EmojiPicker from './EmojiPicker.vue'

const props = withDefaults(
  defineProps<{
    placeholder?: string
    submitLabel?: string
    avatar?: string
    maxLength?: number
  }>(),
  {
    placeholder: '写下你的评论...',
    submitLabel: '发表评论',
    avatar: '',
    maxLength: 500,
  }
)

const emit = defineEmits<{
  (e: 'submit', content: string): void
}>()

const content = ref('')
const showEmoji = ref(false)
const textareaRef = ref<HTMLElement | null>(null)

const remaining = computed(() => props.maxLength - content.value.length)
const isOverLimit = computed(() => remaining.value < 0)
const canSubmit = computed(() => content.value.trim().length > 0 && !isOverLimit.value)

const handleSubmit = () => {
  if (!canSubmit.value) return
  emit('submit', content.value.trim())
  content.value = ''
  showEmoji.value = false
}

const handleEmojiSelect = (emoji: string) => {
  if (content.value.length >= props.maxLength) return
  content.value += emoji
  nextTick(() => {
    const el = textareaRef.value?.querySelector('textarea')
    if (el) {
      el.focus()
      el.setSelectionRange(content.value.length, content.value.length)
    }
  })
}

const handleEmojiToggle = (event: MouseEvent) => {
  event.stopPropagation()
  showEmoji.value = !showEmoji.value
}
</script>

<template>
  <div class="flex gap-3">
    <img
      v-if="avatar"
      :src="avatar"
      alt="avatar"
      class="h-9 w-9 flex-shrink-0 rounded-full object-cover ring-1 ring-slate-200 dark:ring-white/10"
    />
    <div class="flex-1">
      <div ref="textareaRef">
        <UTextarea
          v-model="content"
          :placeholder="placeholder"
          :maxlength="maxLength"
          :rows="3"
          class="text-sm"
        />
      </div>

      <div class="mt-2 flex items-center justify-between">
        <div class="relative">
          <button
            class="flex items-center gap-1 rounded-md px-2 py-1 text-sm text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-700 dark:text-slate-400 dark:hover:bg-white/10 dark:hover:text-slate-200 cursor-pointer"
            @click="handleEmojiToggle"
          >
            <Smile class="h-4 w-4" />
            <span>表情</span>
          </button>
          <EmojiPicker v-model="showEmoji" @select="handleEmojiSelect" />
        </div>

        <div class="flex items-center gap-3">
          <span
            class="text-xs"
            :class="isOverLimit ? 'text-red-500' : 'text-slate-400 dark:text-slate-500'"
          >
            {{ content.length }} / {{ maxLength }}
          </span>
          <UButton
            size="sm"
            :disabled="!canSubmit"
            :loading="false"
            @click="handleSubmit"
          >
            <Send class="h-3.5 w-3.5" />
            {{ submitLabel }}
          </UButton>
        </div>
      </div>
    </div>
  </div>
</template>
