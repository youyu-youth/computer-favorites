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
      'cf-select-root relative inline-flex w-full items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 transition-colors duration-150 focus-within:border-amber-400 focus-within:ring-1 focus-within:ring-amber-400/40 dark:border-white/[0.10] dark:bg-[#16161d] dark:text-white dark:focus-within:border-amber-400/60 dark:focus-within:bg-[#1c1c25] dark:focus-within:ring-amber-400/30',
  },
  label: {
    class: 'flex-1 text-sm leading-6 outline-none',
  },
  dropdown: {
    class: 'flex h-5 w-5 items-center justify-center text-slate-400 dark:text-slate-500',
  },
  overlay: {
    class:
      'cf-select-overlay z-[200] mt-1 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-xl ring-1 ring-black/5 dark:border-white/[0.12] dark:bg-[#1c1c25] dark:shadow-black/60 dark:ring-white/5',
  },
  listContainer: {
    class: 'max-h-60 overflow-auto py-1',
  },
  list: {
    class: 'm-0 list-none p-0',
  },
  option: {
    class:
      'cursor-pointer px-3 py-2 text-sm text-slate-700 transition-colors hover:bg-amber-50 hover:text-amber-700 data-[p-highlight=true]:bg-amber-100/70 data-[p-highlight=true]:text-amber-700 dark:text-slate-200 dark:hover:bg-amber-500/10 dark:hover:text-amber-300 dark:data-[p-highlight=true]:bg-amber-500/15 dark:data-[p-highlight=true]:text-amber-300',
  },
  emptyMessage: {
    class: 'px-3 py-2 text-sm text-slate-500 dark:text-slate-400',
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

:deep(.cf-select-root[data-p-disabled='true']) {
  opacity: 0.55;
  cursor: not-allowed;
}

:deep(.cf-select-root .p-select-label-empty) {
  color: rgb(148 163 184);
}

.dark :deep(.cf-select-root .p-select-label-empty) {
  color: rgb(100 116 139);
}

:deep(.cf-select-overlay) {
  animation: cfSelectOverlayIn 140ms ease-out;
}

@keyframes cfSelectOverlayIn {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
