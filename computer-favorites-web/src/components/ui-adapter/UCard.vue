<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import Card from 'primevue/card'

const attrs = useAttrs()

const mergedClass = computed(() => [
  'rounded-xl border border-slate-200 bg-white p-4 dark:border-white/10 dark:bg-[#131418]',
  attrs.class,
])
</script>

<template>
  <Card
    :class="mergedClass"
    :pt="{
      root: { class: 'overflow-hidden' },
      body: { class: 'p-0' },
      content: { class: 'p-0' },
      header: { class: 'p-0' },
      footer: { class: 'p-0' }
    }"
  >
    <template v-if="$slots.header" #header>
      <slot name="header" />
    </template>
    <template v-if="$slots.title" #title>
      <slot name="title" />
    </template>
    <template v-if="$slots.subtitle" #subtitle>
      <slot name="subtitle" />
    </template>
    <template #content>
      <slot />
    </template>
    <template v-if="$slots.footer" #footer>
      <slot name="footer" />
    </template>
  </Card>
</template>
