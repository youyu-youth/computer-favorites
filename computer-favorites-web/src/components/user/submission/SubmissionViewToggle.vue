<script setup lang="ts">
import { LayoutGrid, List } from 'lucide-vue-next'

defineProps<{
  modelValue: 'card' | 'list'
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: 'card' | 'list'): void
}>()
</script>

<template>
  <div
    class="inline-flex items-center rounded-lg bg-white/50 p-0.5 backdrop-blur-sm dark:bg-white/[0.03]"
  >
    <button
      :class="[
        'inline-flex h-7 w-7 cursor-pointer items-center justify-center rounded-md border-0 p-0 transition-colors',
        modelValue === 'card'
          ? 'bg-[#06326d] text-white'
          : 'bg-transparent text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200',
      ]"
      title="卡片视图"
      @click="emit('update:modelValue', 'card')"
    >
      <LayoutGrid class="h-4 w-4" />
    </button>
    <button
      :class="[
        'inline-flex h-7 w-7 cursor-pointer items-center justify-center rounded-md border-0 p-0 transition-colors',
        modelValue === 'list'
          ? 'bg-[#06326d] text-white'
          : 'bg-transparent text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200',
      ]"
      title="列表视图"
      @click="emit('update:modelValue', 'list')"
    >
      <List class="h-4 w-4" />
    </button>
  </div>
</template>
