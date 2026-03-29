<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import * as LucideIcons from 'lucide-vue-next'

const props = defineProps<{
  name: string
}>()

const attrs = useAttrs()

const toPascalCase = (value: string) =>
  value
    .split('-')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join('')

const iconName = computed(() => props.name.replace(/^i-lucide-/, ''))

const iconComponent = computed(() => {
  const key = toPascalCase(iconName.value)
  const icons = LucideIcons as Record<string, unknown>
  return (icons[key] as object) || (icons.CircleHelp as object)
})
</script>

<template>
  <component :is="iconComponent" v-bind="attrs" />
</template>
