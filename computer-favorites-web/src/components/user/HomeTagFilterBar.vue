<script setup lang="ts">
import { computed } from 'vue'
import type { PublicWebsiteTagItem } from '@/types/public-website'
import { normalizeTagColor } from '@/utils/tag-color'

const props = defineProps<{
  modelValue: number[]
  tags: PublicWebsiteTagItem[]
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [tagIds: number[]]
}>()

const selectedTagIds = computed<number[]>({
  get: () => props.modelValue,
  set: (value) => {
    emit('update:modelValue', value)
  },
})

const selectedCount = computed(() => selectedTagIds.value.length)

const isTagSelected = (tagId: number) => {
  return selectedTagIds.value.includes(tagId)
}

const toggleTagSelection = (tagId: number) => {
  if (isTagSelected(tagId)) {
    selectedTagIds.value = selectedTagIds.value.filter((selectedId) => selectedId !== tagId)
    return
  }
  selectedTagIds.value = [...selectedTagIds.value, tagId]
}

const clearSelectedTags = () => {
  selectedTagIds.value = []
}
</script>

<template>
  <section
    class="mb-6 border border-gray-200 dark:border-[#1f1f1f] bg-white/80 dark:bg-black/50 backdrop-blur-sm px-4 py-3 sm:px-5 sm:py-4"
  >
    <div class="flex items-center justify-between gap-3 mb-3">
      <div class="flex items-center gap-2">
        <h3 class="font-mono text-sm font-bold text-gray-900 dark:text-gray-100">Tag Filter</h3>
        <span class="text-xs text-gray-500 dark:text-gray-400">{{ selectedCount }} selected</span>
      </div>
      <button
        v-if="selectedCount > 0"
        type="button"
        class="cursor-pointer text-xs font-mono px-2.5 py-1 border border-gray-300 dark:border-gray-700 text-gray-700 dark:text-gray-300 hover:border-primary-500 hover:text-primary-600 dark:hover:text-primary-300 transition-colors"
        @click="clearSelectedTags"
      >
        Clear
      </button>
    </div>

    <div v-if="loading" class="font-mono text-xs text-gray-500 dark:text-gray-400 py-2">
      Loading tags...
    </div>

    <div v-else-if="tags.length === 0" class="font-mono text-xs text-gray-500 dark:text-gray-400 py-2">
      No tags available.
    </div>

    <div v-else class="w-full overflow-x-auto pb-1">
      <div class="flex sm:flex-wrap gap-2 min-w-max sm:min-w-0">
        <button
          v-for="tag in tags"
          :key="tag.id"
          type="button"
          class="cursor-pointer inline-flex items-center gap-2 px-3 py-1.5 border text-xs font-mono transition-colors whitespace-nowrap"
          :class="isTagSelected(tag.id)
            ? 'border-primary-500 bg-primary-50 text-primary-700 dark:border-primary-400 dark:bg-primary-500/20 dark:text-primary-300'
            : 'border-gray-300 text-gray-600 hover:border-primary-300 hover:text-primary-600 dark:border-gray-700 dark:text-gray-300 dark:hover:border-primary-400 dark:hover:text-primary-300'"
          @click="toggleTagSelection(tag.id)"
        >
          <span
            class="h-2.5 w-2.5 rounded-full border border-black/10 dark:border-white/20"
            :style="{ backgroundColor: normalizeTagColor(tag.color) }"
          />
          <span>{{ tag.name }}</span>
          <span class="text-[10px] opacity-70">{{ tag.useCount }}</span>
        </button>
      </div>
    </div>
  </section>
</template>
