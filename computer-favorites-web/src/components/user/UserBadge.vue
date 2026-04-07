<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  severity?: 'success' | 'info' | 'warn' | 'danger' | 'secondary' | string
  value: string
}>()

const severityClasses = computed(() => {
  switch (props.severity) {
    case 'success':
      return 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/20 dark:text-emerald-400'
    case 'warn':
      return 'bg-orange-100 text-orange-700 dark:bg-orange-500/20 dark:text-orange-400'
    case 'danger':
      return 'bg-rose-100 text-rose-700 dark:bg-rose-500/20 dark:text-rose-400'
    case 'info':
      return 'bg-blue-100 text-blue-700 dark:bg-blue-500/20 dark:text-blue-400'
    case 'secondary':
      return 'bg-gray-100 text-gray-700 dark:bg-zinc-800 dark:text-zinc-300'
    default:
      return 'bg-gray-100 text-gray-700 dark:bg-zinc-800 dark:text-zinc-300'
  }
})
</script>

<template>
  <span class="inline-flex items-center justify-center px-2 py-0.5 rounded text-[10px] font-semibold tracking-wider uppercase border border-transparent" :class="severityClasses">
    {{ value }}
  </span>
</template>
