<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  label?: string
  icon?: string
  severity?: 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'primary' | string
  text?: boolean
  rounded?: boolean
  size?: 'small' | 'large' | string
  iconClass?: string | any[] | Record<string, any>
  disabled?: boolean
}>()

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

const buttonClasses = computed(() => {
  const classes = ['inline-flex', 'items-center', 'justify-center', 'transition-all', 'font-medium', 'cursor-pointer', 'focus:outline-none', 'disabled:opacity-50', 'disabled:cursor-not-allowed']

  // Size
  if (props.size === 'small') {
    classes.push('text-[11px]', 'px-2', 'py-1')
  } else if (props.size === 'large') {
    classes.push('text-base', 'px-4', 'py-2')
  } else if (!props.rounded && !props.text) {
    classes.push('text-sm', 'px-3', 'py-1.5')
  } else if (!props.rounded && props.text) {
    classes.push('text-sm', 'px-2', 'py-1')
  }

  if (props.rounded && !props.label) {
    if (props.size === 'small') classes.push('w-6', 'h-6')
    else classes.push('w-8', 'h-8')
    classes.push('p-0', 'rounded-full')
  } else {
    classes.push('rounded-md')
  }

  // Severity and text variant
  if (props.text) {
    if (props.severity === 'secondary') {
      classes.push('text-gray-500', 'dark:text-zinc-400', 'bg-transparent', 'hover:bg-zinc-100', 'dark:hover:bg-zinc-800')
    } else if (props.severity === 'danger') {
      classes.push('text-rose-600', 'dark:text-rose-400', 'bg-transparent', 'hover:bg-rose-50', 'dark:hover:bg-rose-500/10')
    } else {
      classes.push('text-emerald-600', 'dark:text-emerald-400', 'bg-transparent', 'hover:bg-emerald-50', 'dark:hover:bg-emerald-500/10')
    }
  } else {
    if (props.severity === 'secondary') {
      classes.push('bg-gray-200', 'text-gray-800', 'hover:bg-gray-300', 'dark:bg-zinc-800', 'dark:text-zinc-200', 'dark:hover:bg-zinc-700')
    } else if (props.severity === 'danger') {
      classes.push('bg-rose-500', 'text-white', 'hover:bg-rose-600')
    } else {
      classes.push('bg-emerald-500', 'text-white', 'hover:bg-emerald-600')
    }
  }

  return classes
})
</script>

<template>
  <button :class="buttonClasses" :disabled="disabled" @click="emit('click', $event)">
    <i v-if="icon" :class="[icon, props.label ? 'mr-1' : '', iconClass || (props.size === 'small' ? 'text-[10px]' : 'text-[12px]')]" />
    <span v-if="label">{{ label }}</span>
    <slot></slot>
  </button>
</template>
