<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import Button from 'primevue/button'

type ButtonColor = 'primary' | 'neutral' | 'gray' | 'white' | 'error' | 'red' | 'success' | 'warning' | 'info'
type ButtonVariant = 'solid' | 'soft' | 'ghost' | 'outline' | 'link'
type ButtonSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl'

const props = withDefaults(defineProps<{
  color?: ButtonColor
  variant?: ButtonVariant
  loading?: boolean
  disabled?: boolean
  size?: ButtonSize
  type?: 'button' | 'submit' | 'reset'
  icon?: string
  label?: string
}>(), {
  color: 'primary',
  variant: 'solid',
  loading: false,
  disabled: false,
  size: 'md',
  type: 'button',
})

const attrs = useAttrs()

const sizeClass = computed(() => {
  switch (props.size) {
    case 'xs':
      return 'h-7 px-2.5 text-xs'
    case 'sm':
      return 'h-8 px-3 text-xs'
    case 'lg':
      return 'h-11 px-5 text-base'
    case 'xl':
      return 'h-12 px-6 text-base'
    default:
      return 'h-10 px-4 text-sm'
  }
})

const toneClass = computed(() => {
  const color = props.color
  const variant = props.variant

  if (variant === 'link') {
    return 'bg-transparent border-transparent underline-offset-2 hover:underline'
  }

  if (variant === 'ghost') {
    return 'bg-transparent border-transparent hover:bg-black/5 dark:hover:bg-white/10'
  }

  if (color === 'error' || color === 'red') {
    if (variant === 'outline') {
      return 'border border-red-500 text-red-600 bg-transparent hover:bg-red-50 dark:hover:bg-red-500/10'
    }
    if (variant === 'soft') {
      return 'bg-red-50 text-red-700 border border-transparent hover:bg-red-100 dark:bg-red-500/10 dark:text-red-300 dark:hover:bg-red-500/20'
    }
    return 'bg-red-600 text-white border border-transparent hover:bg-red-700 active:bg-red-800'
  }

  if (color === 'neutral' || color === 'gray' || color === 'white') {
    if (variant === 'outline') {
      return 'border border-slate-300 text-slate-700 bg-transparent hover:bg-slate-100 dark:border-white/15 dark:text-slate-100 dark:hover:bg-white/10'
    }
    if (variant === 'soft') {
      return 'bg-slate-100 text-slate-800 border border-transparent hover:bg-slate-200 dark:bg-white/10 dark:text-slate-100 dark:hover:bg-white/20'
    }
    return 'bg-slate-800 text-white border border-transparent hover:bg-slate-900 dark:bg-slate-200 dark:text-slate-900 dark:hover:bg-white'
  }

  if (variant === 'outline') {
    return 'border border-[rgb(var(--cf-color-primary-500-rgb)/1)] text-[rgb(var(--cf-color-primary-600-rgb)/1)] bg-transparent hover:bg-[rgb(var(--cf-color-primary-50-rgb)/1)] dark:hover:bg-white/10'
  }
  if (variant === 'soft') {
    return 'bg-[rgb(var(--cf-color-primary-50-rgb)/1)] text-[rgb(var(--cf-color-primary-600-rgb)/1)] border border-transparent hover:bg-[rgb(var(--cf-color-primary-100-rgb)/1)] dark:bg-[rgb(var(--cf-color-primary-500-rgb)/0.2)] dark:text-[rgb(var(--cf-color-primary-100-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-primary-500-rgb)/0.3)]'
  }
  return 'bg-[rgb(var(--cf-color-primary-500-rgb)/1)] text-white border border-transparent hover:bg-[rgb(var(--cf-color-primary-600-rgb)/1)] active:bg-[rgb(var(--cf-color-primary-600-rgb)/1)]'
})

const mergedClass = computed(() => [
  'inline-flex items-center justify-center gap-2 rounded-lg font-medium transition-colors duration-200 disabled:cursor-not-allowed disabled:opacity-60 cursor-pointer',
  sizeClass.value,
  toneClass.value,
  attrs.class,
])
</script>

<template>
  <Button
    :icon="icon"
    :label="label"
    :loading="loading"
    :disabled="disabled"
    :type="type"
    :class="mergedClass"
  >
    <slot>{{ label }}</slot>
  </Button>
</template>
