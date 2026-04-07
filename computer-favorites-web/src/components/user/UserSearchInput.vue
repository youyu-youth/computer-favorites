<script setup lang="ts">
defineProps<{
  modelValue: string
  placeholder?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()
</script>

<template>
  <div class="relative h-[38px] w-full sm:w-[260px]">
    <i class="pi pi-search absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 dark:text-zinc-500 z-10 text-sm"></i>
    <input
      :value="modelValue"
      @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      :placeholder="placeholder"
      class="w-full h-full pl-10 pr-4 bg-white dark:bg-[#111111] border border-gray-200 dark:border-zinc-800 rounded-lg hover:border-gray-300 dark:hover:border-zinc-600 focus:border-emerald-500 dark:focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-all shadow-sm text-sm outline-none"
    />
  </div>
</template>
