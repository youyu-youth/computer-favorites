<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import Tag from 'primevue/tag'

const props = withDefaults(defineProps<{
  color?: 'primary' | 'neutral' | 'success' | 'info' | 'warn' | 'error' | 'danger'
  variant?: 'solid' | 'soft' | 'outline'
  value?: string
}>(), {
  color: 'neutral',
  variant: 'soft',
  value: '',
})

const attrs = useAttrs()

const severity = computed(() => {
  if (props.color === 'error') {
    return 'danger'
  }
  if (props.color === 'neutral') {
    return 'secondary'
  }
  return props.color
})

const mergedClass = computed(() => [
  'inline-flex items-center rounded-md px-2 py-0.5 text-xs font-medium',
  attrs.class,
])
</script>

<template>
  <Tag :severity="severity" :value="value" :class="mergedClass">
    <slot>{{ value }}</slot>
  </Tag>
</template>
