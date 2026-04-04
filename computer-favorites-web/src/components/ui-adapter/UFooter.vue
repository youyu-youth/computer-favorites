<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    ui?: {
      container?: string
    }
  }>(),
  {
    ui: () => ({}),
  },
)
</script>

<template>
  <footer>
    <div :class="ui.container || 'mx-auto w-full px-4 py-5 sm:px-6 lg:px-8'">
      <div class="flex flex-col items-start justify-between gap-3 sm:flex-row sm:items-center">
        <slot name="left" />
        <slot />
      </div>
    </div>
  </footer>
</template>
