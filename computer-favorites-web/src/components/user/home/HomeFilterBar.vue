<script setup lang="ts">
import Button from 'primevue/button'

// 首页筛选条：由父组件驱动当前分类
const props = withDefaults(defineProps<{
  categories: string[]
  modelValue: string
}>(), {
  categories: () => [],
  modelValue: 'All Treasures',
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const handleSelect = (category: string) => {
  if (category === props.modelValue) {
    return
  }
  emit('update:modelValue', category)
}

const isActive = (category: string) => props.modelValue === category
</script>

<template>
  <section class="mt-10 sm:mt-12">
    <div class="overflow-x-auto border-b border-border-default pb-4">
      <div class="flex w-max min-w-full items-center gap-2 sm:gap-4">
        <Button
          v-for="category in categories"
          :key="category"
          unstyled
          :class="[
            'cursor-pointer whitespace-nowrap border px-4 py-2 font-label text-xs uppercase tracking-widest transition-colors sm:px-6',
            isActive(category)
              ? 'border-primary-500 bg-primary-500/5 text-primary-500'
              : 'border-transparent text-text-secondary hover:text-text-primary',
          ]"
          @click="handleSelect(category)"
        >
          {{ category }}
        </Button>
      </div>
    </div>
  </section>
</template>
