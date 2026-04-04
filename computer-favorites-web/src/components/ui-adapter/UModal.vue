<script setup lang="ts">
import { computed } from 'vue'
import Dialog from 'primevue/dialog'

interface ModalUiConfig {
  overlay?: string
  content?: string
  header?: string
  title?: string
  description?: string
  close?: string
  body?: string
  footer?: string
}

const props = withDefaults(
  defineProps<{
    open: boolean
    portal?: boolean
    title?: string
    description?: string
    ui?: ModalUiConfig
  }>(),
  {
    portal: true,
    title: '',
    description: '',
    ui: () => ({}),
  },
)

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
}>()

const visible = computed({
  get: () => props.open,
  set: (value: boolean) => emit('update:open', value),
})

const ptConfig = computed(() => ({
  mask: { class: props.ui?.overlay },
  root: { class: props.ui?.content },
  header: {
    class: ['flex items-center justify-between', props.ui?.header].filter(Boolean).join(' '),
  },
  content: { class: props.ui?.body },
  footer: { class: props.ui?.footer },
}))
</script>

<template>
  <Dialog
    v-model:visible="visible"
    modal
    :dismissableMask="true"
    :draggable="false"
    :appendTo="portal ? 'body' : 'self'"
    :pt="ptConfig"
  >
    <template #header>
      <div class="space-y-1">
        <h3 :class="props.ui?.title">{{ title }}</h3>
        <p v-if="description" :class="props.ui?.description">{{ description }}</p>
      </div>
    </template>

    <slot name="body">
      <slot />
    </slot>

    <template v-if="$slots.footer" #footer>
      <slot name="footer" />
    </template>
  </Dialog>
</template>
