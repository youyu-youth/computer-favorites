<script setup lang="ts">
import type { Category, FilterLogic } from '@/types/website'

const props = defineProps<{
  categories: Category[]
  modelValue: string[]
  logic: FilterLogic
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
  'update:logic': [value: FilterLogic]
}>()

const toggleCategory = (catName: string) => {
  const currentValue = [...(props.modelValue || [])]
  const index = currentValue.indexOf(catName)

  if (index > -1) {
    currentValue.splice(index, 1)
  } else {
    currentValue.push(catName)
  }

  emit('update:modelValue', currentValue)
}

const isActive = (catName: string) => {
  return props.modelValue.includes(catName)
}
</script>

<template>
  <div
    class="mb-10 p-5 bg-white dark:bg-dark-card border border-gray-200 dark:border-dark-border rounded-xl shadow-sm"
  >
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
      <h2 class="text-xl font-bold text-gray-800 dark:text-gray-100 flex items-center gap-2">
        Filters
      </h2>
      <!-- Logic Toggle -->
      <div
        class="flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400 bg-gray-100 dark:bg-[#121212] p-1 rounded-lg"
      >
        <button
          :class="{
            'bg-white dark:bg-dark-card text-gray-900 dark:text-white shadow': logic === 'OR',
          }"
          class="px-3 py-1 rounded-md transition-all"
          @click="emit('update:logic', 'OR')"
        >
          OR
        </button>
        <button
          :class="{
            'bg-white dark:bg-dark-card text-gray-900 dark:text-white shadow': logic === 'AND',
          }"
          class="px-3 py-1 rounded-md transition-all"
          @click="emit('update:logic', 'AND')"
        >
          AND
        </button>
      </div>
    </div>

    <div class="flex flex-wrap gap-3">
      <button
        v-for="cat in categories"
        :key="cat.name"
        :class="[
          'flex items-center gap-2 px-3 py-1.5 rounded-full text-sm border transition-all duration-200',
          isActive(cat.name)
            ? 'border-primary-500 bg-primary-50 dark:bg-primary-500/10 text-primary-600 dark:text-primary-400'
            : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-500 text-gray-600 dark:text-gray-300 bg-gray-50 dark:bg-[#1a1a1c]',
        ]"
        @click="toggleCategory(cat.name)"
      >
        <span class="w-2 h-2 rounded-full" :class="cat.color"></span>
        {{ cat.name }}
      </button>
    </div>
  </div>
</template>
