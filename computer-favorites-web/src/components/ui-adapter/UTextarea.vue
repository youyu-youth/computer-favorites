<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import Textarea from 'primevue/textarea'

const props = withDefaults(
  defineProps<{
    modelValue?: string
    rows?: number
    placeholder?: string
    maxlength?: number | string
    readonly?: boolean
    disabled?: boolean
  }>(),
  {
    modelValue: '',
    rows: 3,
    placeholder: '',
    maxlength: undefined,
    readonly: false,
    disabled: false,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const attrs = useAttrs()

const model = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', String(value ?? '')),
})

const wrapperClass = computed(() => [attrs.class])
const inputClass = computed(() => [
  'w-full resize-none border-0 bg-transparent px-0 py-0 text-sm leading-6 text-slate-900 shadow-none outline-none focus:ring-0 dark:text-white',
  attrs.class,
])
</script>

<template>
  <div :class="wrapperClass">
    <div
      data-slot="base"
      class="rounded-lg border border-slate-200 bg-white px-3 py-2 dark:border-white/10 dark:bg-[#000000]"
    >
      <Textarea
        v-model="model"
        :rows="rows"
        :placeholder="placeholder"
        :maxlength="maxlength"
        :readonly="readonly"
        :disabled="disabled"
        :class="inputClass"
      />
    </div>
  </div>
</template>
