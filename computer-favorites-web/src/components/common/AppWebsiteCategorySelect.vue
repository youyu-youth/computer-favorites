<script setup lang="ts">
import Select from 'primevue/select'
import { computed } from 'vue'

type CategoryOption = {
  id: number
  name: string
  count?: number
}

const props = withDefaults(
  defineProps<{
    modelValue: number | null
    options: CategoryOption[]
    placeholder?: string
    filterPlaceholder?: string
    emptyText?: string
    disabled?: boolean
    variant?: 'default' | 'minimal'
  }>(),
  {
    placeholder: '选择分类',
    filterPlaceholder: '输入分类名进行搜索',
    emptyText: '暂无分类选项',
    disabled: false,
    variant: 'default',
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: number | null): void
}>()

const selectedValue = computed<number | null>({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const isMinimalVariant = computed(() => props.variant === 'minimal')

const selectPt = computed(() => {
  const rootBaseClass = isMinimalVariant.value
    ? 'relative flex w-full min-w-0 items-center rounded-none border bg-white pl-4 pr-14 py-2 text-sm leading-5 text-zinc-900 transition-all dark:border-zinc-800 dark:bg-zinc-900 dark:text-white'
    : 'relative flex w-full min-w-0 items-center rounded-lg border bg-white pl-4 pr-14 py-2 text-sm leading-5 text-gray-900 transition-all dark:bg-dark-card dark:text-gray-100'

  const rootFocusClass = isMinimalVariant.value
    ? 'border-amber-500 ring-0'
    : 'border-brand-orange ring-2 ring-brand-orange'

  const rootIdleClass = isMinimalVariant.value
    ? 'border-zinc-200 dark:border-zinc-800'
    : 'border-gray-300 dark:border-dark-border'

  const overlayClass = isMinimalVariant.value
    ? 'cf-category-select-panel z-[120] mt-1 w-full min-w-0 max-w-full overflow-hidden rounded-none border border-zinc-200 bg-white shadow-lg dark:border-zinc-800 dark:bg-zinc-900'
    : 'cf-category-select-panel z-[120] mt-1 w-full min-w-0 max-w-full overflow-hidden rounded-lg border border-gray-200 bg-white shadow-lg dark:border-dark-border dark:bg-dark-card'

  const headerClass = isMinimalVariant.value
    ? 'border-b border-zinc-100 px-2 pb-2 pt-2 dark:border-zinc-800'
    : 'border-b border-gray-100 px-2 pb-2 pt-2 dark:border-dark-border'

  const filterClass = isMinimalVariant.value
    ? 'cf-category-select-filter w-full rounded-none border border-zinc-200 bg-white px-3 py-1.5 pr-8 text-sm text-zinc-900 outline-none transition-colors focus:border-amber-500 focus:ring-0 dark:border-zinc-800 dark:bg-zinc-900 dark:text-white'
    : 'cf-category-select-filter w-full rounded-md border border-gray-300 bg-gray-50 px-3 py-1.5 pr-8 text-sm text-gray-900 outline-none transition-colors focus:border-brand-orange focus:ring-1 focus:ring-brand-orange dark:border-dark-border dark:bg-dark-bg dark:text-gray-100'

  const optionSelectedClass = isMinimalVariant.value
    ? 'bg-amber-50 font-medium text-amber-700 dark:bg-amber-500/20 dark:text-amber-300'
    : 'bg-orange-50 font-medium text-brand-orange dark:bg-orange-900/20 dark:text-brand-orange'

  const optionUnselectedClass = isMinimalVariant.value
    ? 'text-zinc-700 hover:bg-zinc-100 dark:text-zinc-300 dark:hover:bg-zinc-800'
    : 'text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-dark-border'

  return {
    root: ({ state }: any) => ({
      class: [rootBaseClass, state.focused ? rootFocusClass : rootIdleClass],
    }),
    label: {
      class: 'flex-1 truncate',
    },
    dropdown: {
      class:
        'absolute right-3 top-1/2 flex -translate-y-1/2 items-center justify-center text-gray-400',
    },
    clearIcon: {
      class:
        'absolute right-8 top-1/2 -translate-y-1/2 cursor-pointer text-gray-400 transition-colors hover:text-gray-600 dark:text-gray-500 dark:hover:text-gray-300',
    },
    dropdownIcon: {
      class: 'text-xs',
    },
    overlay: {
      class: overlayClass,
    },
    header: {
      class: headerClass,
    },
    pcFilterContainer: {
      root: {
        class: 'relative w-full',
      },
    },
    pcFilter: {
      root: {
        class: filterClass,
      },
    },
    pcFilterIconContainer: {
      root: {
        class: 'pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400',
      },
    },
    filterIcon: {
      class: 'text-xs',
    },
    listContainer: {
      class: 'cf-select-scroll max-h-56 overflow-y-auto',
    },
    list: {
      class: 'py-1',
    },
    option: ({ context }: any) => ({
      class: [
        'cursor-pointer truncate px-4 py-2 text-sm transition-colors',
        context.selected ? optionSelectedClass : optionUnselectedClass,
      ],
    }),
    optionLabel: {
      class: 'truncate',
    },
    emptyMessage: {
      class: 'px-4 py-3 text-center text-sm text-gray-500 dark:text-gray-400',
    },
  }
})
</script>

<template>
  <Select
    v-model="selectedValue"
    :options="options"
    optionLabel="name"
    optionValue="id"
    :placeholder="placeholder"
    :disabled="disabled"
    showClear
    filter
    :filterPlaceholder="filterPlaceholder"
    :filterFields="['name']"
    filterMatchMode="contains"
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
        {{ options.find((item) => item.id === slotProps.value)?.name }}
      </span>
    </template>
    <template #option="slotProps">
      <div class="flex min-w-0 items-center justify-between gap-3">
        <span class="truncate">{{ slotProps.option.name }}</span>
        <span class="shrink-0 text-xs text-gray-400">{{ slotProps.option.count }}</span>
      </div>
    </template>
    <template #dropdownicon>
      <i class="fas fa-chevron-down"></i>
    </template>
    <template #clearicon="slotProps">
      <i class="fas fa-xmark" @click.stop="slotProps.clearCallback($event)"></i>
    </template>
    <template #filtericon>
      <i class="fas fa-search"></i>
    </template>
    <template #empty>
      <div class="px-4 py-3 text-center text-sm text-gray-500 dark:text-gray-400">
        {{ emptyText }}
      </div>
    </template>
  </Select>
</template>

<style scoped>
:deep(.cf-category-select-panel) {
  max-width: min(100%, calc(100vw - 2rem));
}

:deep(.cf-category-select-panel .cf-select-scroll) {
  scrollbar-width: thin;
  scrollbar-color: rgb(148 163 184 / 0.7) transparent;
}

:deep(.cf-category-select-panel .cf-select-scroll::-webkit-scrollbar) {
  width: 8px;
}

:deep(.cf-category-select-panel .cf-select-scroll::-webkit-scrollbar-track) {
  background: transparent;
}

:deep(.cf-category-select-panel .cf-select-scroll::-webkit-scrollbar-thumb) {
  border-radius: 9999px;
  background: rgb(148 163 184 / 0.65);
}

:deep(.cf-category-select-panel .cf-select-scroll::-webkit-scrollbar-thumb:hover) {
  background: rgb(100 116 139 / 0.85);
}
</style>
