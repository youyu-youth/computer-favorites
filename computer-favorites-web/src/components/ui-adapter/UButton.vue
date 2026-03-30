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

const THEME_TONE_MAP: Record<ButtonColor, Record<ButtonVariant, string>> = {
  primary: {
    solid:
      'bg-[rgb(var(--cf-color-primary-500-rgb)/1)] text-white border border-transparent hover:bg-[rgb(var(--cf-color-primary-600-rgb)/1)] active:bg-[rgb(var(--cf-color-primary-600-rgb)/1)]',
    soft:
      'bg-[rgb(var(--cf-color-primary-50-rgb)/1)] text-[rgb(var(--cf-color-primary-600-rgb)/1)] border border-transparent hover:bg-[rgb(var(--cf-color-primary-100-rgb)/1)] dark:bg-[rgb(var(--cf-color-primary-500-rgb)/0.2)] dark:text-[rgb(var(--cf-color-primary-100-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-primary-500-rgb)/0.3)]',
    ghost:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-primary-600-rgb)/1)] hover:bg-[rgb(var(--cf-color-primary-50-rgb)/1)] dark:text-[rgb(var(--cf-color-primary-100-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-primary-500-rgb)/0.2)]',
    outline:
      'border border-[rgb(var(--cf-color-primary-500-rgb)/1)] text-[rgb(var(--cf-color-primary-600-rgb)/1)] bg-transparent hover:bg-[rgb(var(--cf-color-primary-50-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-primary-500-rgb)/0.2)]',
    link:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-primary-600-rgb)/1)] underline-offset-2 hover:underline dark:text-[rgb(var(--cf-color-primary-100-rgb)/1)]',
  },
  neutral: {
    solid:
      'bg-[rgb(var(--cf-color-neutral-600-rgb)/1)] text-white border border-transparent hover:bg-[rgb(var(--cf-color-neutral-500-rgb)/1)] active:bg-[rgb(var(--cf-color-neutral-500-rgb)/1)] dark:bg-[rgb(var(--cf-color-neutral-500-rgb)/1)] dark:text-[rgb(var(--cf-color-text-primary-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-neutral-400-rgb)/1)]',
    soft:
      'bg-[rgb(var(--cf-color-neutral-100-rgb)/1)] text-[rgb(var(--cf-color-text-primary-rgb)/1)] border border-transparent hover:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.38)] dark:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.16)] dark:text-[rgb(var(--cf-color-text-primary-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.26)]',
    ghost:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-text-primary-rgb)/1)] hover:bg-[rgb(var(--cf-color-neutral-100-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.2)]',
    outline:
      'border border-[rgb(var(--cf-color-border-default-rgb)/1)] text-[rgb(var(--cf-color-text-primary-rgb)/1)] bg-transparent hover:bg-[rgb(var(--cf-color-neutral-50-rgb)/1)] dark:border-[rgb(var(--cf-color-border-muted-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.2)]',
    link:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-text-primary-rgb)/1)] underline-offset-2 hover:underline',
  },
  gray: {
    solid:
      'bg-[rgb(var(--cf-color-neutral-600-rgb)/1)] text-white border border-transparent hover:bg-[rgb(var(--cf-color-neutral-500-rgb)/1)] active:bg-[rgb(var(--cf-color-neutral-500-rgb)/1)] dark:bg-[rgb(var(--cf-color-neutral-500-rgb)/1)] dark:text-[rgb(var(--cf-color-text-primary-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-neutral-400-rgb)/1)]',
    soft:
      'bg-[rgb(var(--cf-color-neutral-100-rgb)/1)] text-[rgb(var(--cf-color-text-primary-rgb)/1)] border border-transparent hover:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.38)] dark:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.16)] dark:text-[rgb(var(--cf-color-text-primary-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.26)]',
    ghost:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-text-primary-rgb)/1)] hover:bg-[rgb(var(--cf-color-neutral-100-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.2)]',
    outline:
      'border border-[rgb(var(--cf-color-border-default-rgb)/1)] text-[rgb(var(--cf-color-text-primary-rgb)/1)] bg-transparent hover:bg-[rgb(var(--cf-color-neutral-50-rgb)/1)] dark:border-[rgb(var(--cf-color-border-muted-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.2)]',
    link:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-text-primary-rgb)/1)] underline-offset-2 hover:underline',
  },
  white: {
    solid:
      'bg-white text-[rgb(var(--cf-color-text-primary-rgb)/1)] border border-[rgb(var(--cf-color-border-default-rgb)/1)] hover:bg-[rgb(var(--cf-color-neutral-50-rgb)/1)] active:bg-[rgb(var(--cf-color-neutral-100-rgb)/1)] dark:bg-[rgb(var(--cf-color-neutral-100-rgb)/1)] dark:text-[rgb(var(--cf-color-text-primary-rgb)/1)]',
    soft:
      'bg-[rgb(var(--cf-color-neutral-100-rgb)/1)] text-[rgb(var(--cf-color-text-primary-rgb)/1)] border border-transparent hover:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.38)] dark:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.16)] dark:text-[rgb(var(--cf-color-text-primary-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.26)]',
    ghost:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-text-primary-rgb)/1)] hover:bg-[rgb(var(--cf-color-neutral-100-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.2)]',
    outline:
      'border border-[rgb(var(--cf-color-border-default-rgb)/1)] text-[rgb(var(--cf-color-text-primary-rgb)/1)] bg-transparent hover:bg-[rgb(var(--cf-color-neutral-50-rgb)/1)] dark:border-[rgb(var(--cf-color-border-muted-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-neutral-500-rgb)/0.2)]',
    link:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-text-primary-rgb)/1)] underline-offset-2 hover:underline',
  },
  error: {
    solid:
      'bg-[rgb(var(--cf-color-error-rgb)/1)] text-white border border-transparent hover:brightness-95 active:brightness-90',
    soft:
      'bg-[rgb(var(--cf-color-error-rgb)/0.14)] text-[rgb(var(--cf-color-error-rgb)/1)] border border-transparent hover:bg-[rgb(var(--cf-color-error-rgb)/0.22)]',
    ghost:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-error-rgb)/1)] hover:bg-[rgb(var(--cf-color-error-rgb)/0.12)]',
    outline:
      'border border-[rgb(var(--cf-color-error-rgb)/1)] text-[rgb(var(--cf-color-error-rgb)/1)] bg-transparent hover:bg-[rgb(var(--cf-color-error-rgb)/0.12)]',
    link:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-error-rgb)/1)] underline-offset-2 hover:underline',
  },
  red: {
    solid:
      'bg-[rgb(var(--cf-color-error-rgb)/1)] text-white border border-transparent hover:brightness-95 active:brightness-90',
    soft:
      'bg-[rgb(var(--cf-color-error-rgb)/0.14)] text-[rgb(var(--cf-color-error-rgb)/1)] border border-transparent hover:bg-[rgb(var(--cf-color-error-rgb)/0.22)]',
    ghost:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-error-rgb)/1)] hover:bg-[rgb(var(--cf-color-error-rgb)/0.12)]',
    outline:
      'border border-[rgb(var(--cf-color-error-rgb)/1)] text-[rgb(var(--cf-color-error-rgb)/1)] bg-transparent hover:bg-[rgb(var(--cf-color-error-rgb)/0.12)]',
    link:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-error-rgb)/1)] underline-offset-2 hover:underline',
  },
  success: {
    solid:
      'bg-[rgb(var(--cf-color-success-rgb)/1)] text-white border border-transparent hover:brightness-95 active:brightness-90',
    soft:
      'bg-[rgb(var(--cf-color-success-rgb)/0.14)] text-[rgb(var(--cf-color-success-rgb)/1)] border border-transparent hover:bg-[rgb(var(--cf-color-success-rgb)/0.22)]',
    ghost:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-success-rgb)/1)] hover:bg-[rgb(var(--cf-color-success-rgb)/0.12)]',
    outline:
      'border border-[rgb(var(--cf-color-success-rgb)/1)] text-[rgb(var(--cf-color-success-rgb)/1)] bg-transparent hover:bg-[rgb(var(--cf-color-success-rgb)/0.12)]',
    link:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-success-rgb)/1)] underline-offset-2 hover:underline',
  },
  warning: {
    solid:
      'bg-[rgb(var(--cf-color-warning-rgb)/1)] text-white border border-transparent hover:brightness-95 active:brightness-90',
    soft:
      'bg-[rgb(var(--cf-color-warning-rgb)/0.14)] text-[rgb(var(--cf-color-warning-rgb)/1)] border border-transparent hover:bg-[rgb(var(--cf-color-warning-rgb)/0.22)]',
    ghost:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-warning-rgb)/1)] hover:bg-[rgb(var(--cf-color-warning-rgb)/0.12)]',
    outline:
      'border border-[rgb(var(--cf-color-warning-rgb)/1)] text-[rgb(var(--cf-color-warning-rgb)/1)] bg-transparent hover:bg-[rgb(var(--cf-color-warning-rgb)/0.12)]',
    link:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-warning-rgb)/1)] underline-offset-2 hover:underline',
  },
  info: {
    solid:
      'bg-[rgb(var(--cf-color-info-rgb)/1)] text-white border border-transparent hover:brightness-95 active:brightness-90',
    soft:
      'bg-[rgb(var(--cf-color-info-rgb)/0.14)] text-[rgb(var(--cf-color-info-rgb)/1)] border border-transparent hover:bg-[rgb(var(--cf-color-info-rgb)/0.22)]',
    ghost:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-info-rgb)/1)] hover:bg-[rgb(var(--cf-color-info-rgb)/0.12)]',
    outline:
      'border border-[rgb(var(--cf-color-info-rgb)/1)] text-[rgb(var(--cf-color-info-rgb)/1)] bg-transparent hover:bg-[rgb(var(--cf-color-info-rgb)/0.12)]',
    link:
      'bg-transparent border border-transparent text-[rgb(var(--cf-color-info-rgb)/1)] underline-offset-2 hover:underline',
  },
}

const sizeClass = computed(() => {
  switch (props.size) {
    case 'xs':
      return 'h-8 px-2.5 text-xs'
    case 'sm':
      return 'h-9 px-3 text-xs'
    case 'lg':
      return 'h-11 px-5 text-base'
    case 'xl':
      return 'h-12 px-6 text-base'
    default:
      return 'h-10 px-4 text-sm'
  }
})

const toneClass = computed(() => {
  return THEME_TONE_MAP[props.color][props.variant]
})

const mergedClass = computed(() => [
  'inline-flex min-h-[var(--cf-ui-touch-target)] items-center justify-center gap-2 rounded-lg font-medium transition-colors duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[rgb(var(--cf-color-primary-500-rgb)/0.35)] focus-visible:ring-offset-2 focus-visible:ring-offset-[rgb(var(--cf-color-surface-page-rgb)/1)] disabled:cursor-not-allowed disabled:opacity-60 cursor-pointer md:min-h-0',
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
