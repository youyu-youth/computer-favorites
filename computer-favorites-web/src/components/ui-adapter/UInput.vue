<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import InputText from 'primevue/inputtext'

const props = withDefaults(defineProps<{
  modelValue?: string | number
  type?: string
  placeholder?: string
  maxlength?: number | string
  readonly?: boolean
  disabled?: boolean
  autocomplete?: string
}>(), {
  modelValue: '',
  type: 'text',
  placeholder: '',
  maxlength: undefined,
  readonly: false,
  disabled: false,
  autocomplete: undefined,
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'keyup', event: KeyboardEvent): void
  (e: 'keydown', event: KeyboardEvent): void
}>()

const attrs = useAttrs()

const model = computed<string>({
  get: () => String(props.modelValue ?? ''),
  set: (value) => emit('update:modelValue', String(value ?? '')),
})

const wrapperClass = computed(() => [attrs.class])
const inputClass = computed(() => [
  'w-full border-0 bg-transparent px-0 py-0 text-sm leading-6 text-[rgb(var(--cf-color-text-primary-rgb)/1)] shadow-none outline-none focus:ring-0',
])

const handleKeyup = (event: Event) => {
  emit('keyup', event as KeyboardEvent)
}

const handleKeydown = (event: Event) => {
  emit('keydown', event as KeyboardEvent)
}
</script>

<template>
  <div :class="wrapperClass">
    <div
      data-slot="base"
      class="rounded-lg border border-[rgb(var(--cf-color-border-default-rgb)/1)] bg-[rgb(var(--cf-color-surface-card-rgb)/1)] px-3 py-2 transition-colors duration-200 focus-within:border-[rgb(var(--cf-color-primary-500-rgb)/1)] dark:border-[rgb(var(--cf-color-border-muted-rgb)/1)]"
    >
      <InputText
        v-model="model"
        :type="type"
        :placeholder="placeholder"
        :maxlength="maxlength"
        :readonly="readonly"
        :disabled="disabled"
        :autocomplete="autocomplete"
        :class="inputClass"
        @keyup="handleKeyup"
        @keydown="handleKeydown"
      />
    </div>
  </div>
</template>
