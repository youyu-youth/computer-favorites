<script setup lang="ts">
import { computed } from 'vue'
import Select from 'primevue/select'

type AdminSelectValue = string | number | null

export interface AdminSelectOption {
  label: string
  value: AdminSelectValue
}

const props = withDefaults(
  defineProps<{
    modelValue: AdminSelectValue
    options: AdminSelectOption[]
    placeholder?: string
    disabled?: boolean
  }>(),
  {
    placeholder: '请选择',
    disabled: false,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: AdminSelectValue): void
}>()

const selectedValue = computed<AdminSelectValue>({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const selectPt = computed(() => ({
  root: ({ state }: any) => ({
    class: [
      'relative flex h-10 w-full min-w-0 items-center rounded-xl border bg-white pl-3 pr-10 text-sm text-gray-700 transition-colors dark:bg-dark-bg dark:text-gray-100',
      state.focused
        ? 'border-[rgb(var(--cf-color-primary-500-rgb)/1)] ring-2 ring-[rgb(var(--cf-color-primary-500-rgb)/0.16)]'
        : 'border-gray-200 dark:border-dark-border',
    ],
  }),
  label: {
    class: 'truncate text-sm',
  },
  dropdown: {
    class:
      'absolute right-3 top-1/2 flex -translate-y-1/2 items-center justify-center text-gray-400',
  },
  dropdownIcon: {
    class: 'text-xs',
  },
  clearIcon: {
    class:
      'absolute right-8 top-1/2 -translate-y-1/2 cursor-pointer text-gray-400 transition-colors hover:text-gray-600 dark:text-gray-500 dark:hover:text-gray-300',
  },
  overlay: {
    class:
      'cf-admin-select-panel z-[120] mt-1 overflow-hidden rounded-xl border border-gray-200 bg-white shadow-[0_14px_34px_rgba(15,23,42,0.12)] dark:border-dark-border dark:bg-dark-card',
  },
  listContainer: {
    class: 'cf-admin-select-scroll max-h-56 overflow-y-auto',
  },
  list: {
    class: 'py-1',
  },
  option: ({ context }: any) => ({
    class: [
      'cursor-pointer px-3 py-2 text-sm transition-colors',
      context.selected
        ? 'bg-[rgb(var(--cf-color-primary-50-rgb)/1)] font-medium text-[rgb(var(--cf-color-primary-600-rgb)/1)] dark:bg-[rgb(var(--cf-color-primary-500-rgb)/0.18)] dark:text-[rgb(var(--cf-color-primary-100-rgb)/1)]'
        : 'text-gray-700 hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-dark-bg',
    ],
  }),
  optionLabel: {
    class: 'truncate',
  },
  emptyMessage: {
    class: 'px-3 py-3 text-center text-sm text-gray-500 dark:text-gray-400',
  },
}))
</script>

<template>
  <Select
    v-model="selectedValue"
    :options="options"
    optionLabel="label"
    optionValue="value"
    :placeholder="placeholder"
    :disabled="disabled"
    showClear
    appendTo="self"
    scrollHeight="14rem"
    :overlayStyle="{ width: '100%', maxWidth: '100%', minWidth: '0' }"
    :pt="selectPt"
    class="w-full"
  >
    <template #value="slotProps">
      <span
        v-if="slotProps.value == null || slotProps.value === ''"
        class="truncate text-gray-500 dark:text-gray-400"
      >
        {{ slotProps.placeholder || placeholder }}
      </span>
      <span v-else class="truncate">
        {{ options.find((item) => item.value === slotProps.value)?.label }}
      </span>
    </template>

    <template #dropdownicon>
      <i class="fas fa-chevron-down"></i>
    </template>

    <template #clearicon="slotProps">
      <i class="fas fa-xmark" @click.stop="slotProps.clearCallback($event)"></i>
    </template>
  </Select>
</template>

<style scoped>
:deep(.cf-admin-select-panel) {
  max-width: min(100%, calc(100vw - 2rem));
}

:deep(.cf-admin-select-panel .cf-admin-select-scroll) {
  scrollbar-width: thin;
  scrollbar-color: rgb(148 163 184 / 0.72) transparent;
}

:deep(.cf-admin-select-panel .cf-admin-select-scroll::-webkit-scrollbar) {
  width: 8px;
}

:deep(.cf-admin-select-panel .cf-admin-select-scroll::-webkit-scrollbar-track) {
  background: transparent;
}

:deep(.cf-admin-select-panel .cf-admin-select-scroll::-webkit-scrollbar-thumb) {
  border-radius: 9999px;
  background: rgb(148 163 184 / 0.68);
}

:deep(.cf-admin-select-panel .cf-admin-select-scroll::-webkit-scrollbar-thumb:hover) {
  background: rgb(100 116 139 / 0.86);
}
</style>
