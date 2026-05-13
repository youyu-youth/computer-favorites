<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import Select from 'primevue/select'

interface SelectOption {
  label: string
  value: string | number | boolean
  disabled?: boolean
}

const props = withDefaults(
  defineProps<{
    modelValue?: string | number | boolean | null
    options?: SelectOption[]
    placeholder?: string
    disabled?: boolean
  }>(),
  {
    modelValue: null,
    options: () => [],
    placeholder: '请选择',
    disabled: false,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number | boolean | null): void
  (e: 'change', value: string | number | boolean | null): void
}>()

const attrs = useAttrs()

const model = computed({
  get: () => props.modelValue,
  set: (value) => {
    emit('update:modelValue', value as string | number | boolean | null)
    emit('change', value as string | number | boolean | null)
  },
})

const wrapperClass = computed(() => [attrs.class])

const ptConfig = computed(() => ({
  root: {
    class:
      'cf-select-root relative inline-flex w-full items-center gap-2 rounded-md border border-black/5 bg-white px-3 py-2 font-mono text-[12px] text-gray-900 transition-all duration-200 focus-within:border-amber-500/40 dark:border-white/[0.06] dark:bg-black dark:text-white dark:focus-within:border-amber-400/40',
  },
  label: {
    class: 'flex-1 leading-6 outline-none',
  },
  dropdown: {
    class:
      'flex h-4 w-4 items-center justify-center text-gray-400 transition-transform duration-200 group-data-[p-open]:rotate-180 dark:text-gray-500',
  },
  overlay: {
    class:
      'cf-select-overlay z-[200] mt-1 overflow-hidden rounded-md border border-black/5 bg-white shadow-[0_4px_12px_-6px_rgba(0,0,0,0.08)] ring-1 ring-black/[0.03] dark:border-white/[0.06] dark:bg-black dark:shadow-[0_4px_12px_-6px_rgba(0,0,0,0.5)] dark:ring-white/[0.03]',
  },
  listContainer: {
    class: 'max-h-60 overflow-auto py-1',
  },
  list: {
    class: 'm-0 list-none p-0',
  },
  option: {
    class:
      'cursor-pointer px-3 py-2 font-mono text-[12px] text-gray-700 transition-all duration-150 active:scale-[0.98] hover:bg-amber-50 hover:text-amber-700 data-[p-highlight=true]:bg-amber-100/70 data-[p-highlight=true]:text-amber-700 dark:text-gray-300 dark:hover:bg-amber-500/10 dark:hover:text-amber-300 dark:data-[p-highlight=true]:bg-amber-500/15 dark:data-[p-highlight=true]:text-amber-300',
  },
  emptyMessage: {
    class: 'px-3 py-2 text-[12px] text-gray-400 dark:text-gray-500',
  },
}))
</script>

<template>
  <div :class="wrapperClass">
    <Select
      v-model="model"
      :options="options"
      option-label="label"
      option-value="value"
      :placeholder="placeholder"
      :disabled="disabled"
      :pt="ptConfig"
    />
  </div>
</template>

<style scoped>
:deep(.cf-select-root) {
  cursor: pointer;
}

:deep(.cf-select-root:active) {
  transform: scale(0.99);
}

:deep(.cf-select-root[data-p-disabled='true']) {
  opacity: 0.45;
  cursor: not-allowed;
  transform: none;
}

:deep(.cf-select-root .p-select-label-empty) {
  color: rgb(156 163 175);
}

.dark :deep(.cf-select-root .p-select-label-empty) {
  color: rgb(100 116 139);
}

:deep(.cf-select-overlay) {
  animation: cfSelectOverlayIn 160ms cubic-bezier(0.16, 1, 0.3, 1);
  transform-origin: top center;
}

@keyframes cfSelectOverlayIn {
  from {
    opacity: 0;
    transform: translateY(-6px) scale(0.97);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>
