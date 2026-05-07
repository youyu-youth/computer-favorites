<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import InputText from 'primevue/inputtext'

const props = withDefaults(
  defineProps<{
    modelValue?: string | number
    type?: string
    placeholder?: string
    maxlength?: number | string
    readonly?: boolean
    disabled?: boolean
    autocomplete?: string
  }>(),
  {
    modelValue: '',
    type: 'text',
    placeholder: '',
    maxlength: undefined,
    readonly: false,
    disabled: false,
    autocomplete: undefined,
  },
)

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
  'w-full border-0 bg-transparent px-0 py-0 text-base leading-6 text-slate-900 shadow-none outline-none focus:ring-0 dark:text-white',
])
</script>

<template>
  <div :class="wrapperClass">
    <div
      data-slot="base"
      class="rounded-lg border border-slate-200 bg-white px-3 py-2 transition-colors duration-150 focus-within:border-amber-400 focus-within:ring-1 focus-within:ring-amber-400/40 dark:border-white/[0.10] dark:bg-[#16161d] dark:focus-within:border-amber-400/60 dark:focus-within:bg-[#1c1c25] dark:focus-within:ring-amber-400/30"
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
        @keyup="(event) => emit('keyup', event as KeyboardEvent)"
        @keydown="(event) => emit('keydown', event as KeyboardEvent)"
      />
    </div>
  </div>
</template>
