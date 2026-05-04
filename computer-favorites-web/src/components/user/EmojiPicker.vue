<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'select', emoji: string): void
}>()

const pickerRef = ref<HTMLElement | null>(null)

const emojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂',
  '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩',
  '😘', '😗', '😚', '😙', '🥲', '😋', '😛', '😜',
  '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐',
  '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬',
  '🤥', '😌', '😔', '😪', '🤤', '😴', '😷', '🤒',
  '🤕', '🤢', '🤮', '🤧', '🥵', '🥶', '🥴', '😵',
  '👍', '👎', '👏', '🙌', '🤝', '💪', '🙏', '💯',
  '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍',
  '🔥', '⭐', '✨', '💡', '💻', '📱', '🎉', '🎊',
]

const handleSelect = (emoji: string) => {
  emit('select', emoji)
  emit('update:modelValue', false)
}

const handleClickOutside = (event: MouseEvent) => {
  if (pickerRef.value && !pickerRef.value.contains(event.target as Node)) {
    emit('update:modelValue', false)
  }
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      nextTick(() => {
        document.addEventListener('click', handleClickOutside)
      })
    } else {
      document.removeEventListener('click', handleClickOutside)
    }
  },
)
</script>

<template>
  <div
    v-if="modelValue"
    ref="pickerRef"
    class="absolute z-50 mt-2 w-72 rounded-xl border border-slate-200 bg-white p-3 shadow-lg dark:border-white/10 dark:bg-[#1a1a1e]"
    @click.stop
  >
    <div class="grid grid-cols-8 gap-1.5">
      <button
        v-for="emoji in emojis"
        :key="emoji"
        class="flex h-8 w-8 items-center justify-center rounded-md text-lg transition-colors hover:bg-slate-100 dark:hover:bg-white/10 cursor-pointer"
        @click="handleSelect(emoji)"
      >
        {{ emoji }}
      </button>
    </div>
  </div>
</template>
