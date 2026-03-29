<script setup lang="ts">
const props = withDefaults(defineProps<{
  label?: string
  required?: boolean
  hint?: string
  error?: string
}>(), {
  label: '',
  required: false,
  hint: '',
  error: '',
})
</script>

<template>
  <div class="space-y-2">
    <label v-if="label" class="inline-flex items-center gap-1 text-sm font-medium text-slate-700 dark:text-slate-200">
      <span>{{ label }}</span>
      <span v-if="required" class="text-red-500">*</span>
    </label>
    <slot />
    <p v-if="hint" class="text-xs text-slate-500 dark:text-slate-400">{{ hint }}</p>
    <p v-if="error" class="text-xs text-red-500">{{ error }}</p>
  </div>
</template>
