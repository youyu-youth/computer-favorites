<script setup lang="ts">
import { computed, ref, useAttrs } from 'vue'
import Menu from 'primevue/menu'
import type { MenuItem } from 'primevue/menuitem'

const props = withDefaults(defineProps<{
  model: MenuItem[]
  popup?: boolean
  appendTo?: 'body' | 'self' | string
}>(), {
  popup: true,
  appendTo: 'body',
})

const attrs = useAttrs()
const menuRef = ref<InstanceType<typeof Menu> | null>(null)

const ptConfig = computed(() => ({
  root: {
    class:
      'min-w-52 overflow-hidden rounded-xl border border-[rgb(var(--cf-color-border-default-rgb)/1)] bg-[rgb(var(--cf-color-surface-card-rgb)/1)] p-1 shadow-[var(--cf-shadow-elevation-3)] dark:border-[rgb(var(--cf-color-border-muted-rgb)/1)]',
  },
  list: {
    class: 'm-0 flex list-none flex-col gap-1 p-0',
  },
  itemContent: {
    class:
      'rounded-lg transition-colors duration-200 data-[p-focused=true]:bg-[rgb(var(--cf-color-primary-50-rgb)/1)] dark:data-[p-focused=true]:bg-[rgb(var(--cf-color-primary-500-rgb)/0.2)]',
  },
  itemLink: {
    class:
      'flex min-h-[var(--cf-ui-touch-target)] cursor-pointer items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium text-[rgb(var(--cf-color-text-primary-rgb)/1)]',
  },
  itemIcon: {
    class: 'text-[rgb(var(--cf-color-text-secondary-rgb)/1)]',
  },
  separator: {
    class:
      'my-1 border-t border-[rgb(var(--cf-color-border-default-rgb)/1)] dark:border-[rgb(var(--cf-color-border-muted-rgb)/1)]',
  },
}))

const toggle = (event: Event) => {
  menuRef.value?.toggle(event)
}

const show = (event: Event) => {
  menuRef.value?.show(event)
}

const hide = () => {
  menuRef.value?.hide()
}

defineExpose({
  toggle,
  show,
  hide,
})
</script>

<template>
  <Menu
    ref="menuRef"
    :model="model"
    :popup="popup"
    :appendTo="appendTo"
    :pt="ptConfig"
    :class="attrs.class"
  />
</template>
