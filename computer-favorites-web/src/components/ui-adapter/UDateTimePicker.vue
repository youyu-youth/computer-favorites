<script setup lang="ts">
import { computed } from 'vue'
import DatePicker from 'primevue/datepicker'

const props = withDefaults(
  defineProps<{
    modelValue?: Date | null
    placeholder?: string
    disabled?: boolean
    showTime?: boolean
    hourFormat?: string
    showSeconds?: boolean
    showButtonBar?: boolean
    dateFormat?: string
  }>(),
  {
    placeholder: '选择日期与时间',
    disabled: false,
    showTime: true,
    hourFormat: '24',
    showSeconds: true,
    showButtonBar: true,
    dateFormat: 'yy-mm-dd',
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: Date | null): void
}>()

const model = computed<Date | null>({
  get: () => props.modelValue ?? null,
  set: (value) => emit('update:modelValue', value),
})

const pt = computed(() => ({
  root: ({ state }: any) => ({
    class: [
      'relative flex h-10 w-full min-w-0 items-center rounded-xl border bg-white pl-3 pr-10 text-sm text-gray-700 transition-colors dark:bg-dark-bg dark:text-gray-100',
      state.focused
        ? 'border-[rgb(var(--cf-color-primary-500-rgb)/1)] ring-2 ring-[rgb(var(--cf-color-primary-500-rgb)/0.16)]'
        : 'border-gray-200 dark:border-dark-border',
    ],
  }),
  pcInputText: {
    root: {
      class:
        'w-full border-0 bg-transparent p-0 text-sm text-gray-700 outline-none placeholder:text-gray-400 dark:text-gray-100 dark:placeholder:text-gray-500',
    },
  },
  dropdown: {
    class:
      'absolute right-3 top-1/2 flex -translate-y-1/2 cursor-pointer items-center justify-center text-gray-400 transition-colors hover:text-gray-600 dark:text-gray-500 dark:hover:text-gray-300',
  },
  dropdownIcon: {
    class: 'text-xs',
  },
  clearIcon: {
    class:
      'absolute right-9 top-1/2 -translate-y-1/2 flex h-6 w-6 cursor-pointer items-center justify-center rounded-full text-xs text-gray-400 transition-colors hover:bg-gray-200 hover:text-gray-600 dark:text-gray-500 dark:hover:bg-slate-700 dark:hover:text-gray-300',
  },
  panel: {
    class:
      'z-[140] mt-1 overflow-hidden rounded-xl border border-gray-200 bg-white shadow-[0_14px_34px_rgba(15,23,42,0.12)] dark:border-dark-border dark:bg-dark-card',
  },
  calendarContainer: {
    class: 'p-2',
  },
  calendar: {
    class: 'space-y-1',
  },
  header: {
    class: 'flex items-center justify-between px-1 py-1',
  },
  pcPrevButton: {
    root: {
      class:
        'inline-flex h-7 w-7 cursor-pointer items-center justify-center rounded-lg border-0 bg-transparent text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-slate-800 dark:hover:text-gray-200',
    },
    icon: {
      class: 'text-xs',
    },
  },
  pcNextButton: {
    root: {
      class:
        'inline-flex h-7 w-7 cursor-pointer items-center justify-center rounded-lg border-0 bg-transparent text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-slate-800 dark:hover:text-gray-200',
    },
    icon: {
      class: 'text-xs',
    },
  },
  title: {
    class: 'flex items-center gap-1 text-sm font-semibold text-gray-800 dark:text-gray-100',
  },
  selectMonth: {
    class:
      'cursor-pointer rounded-md px-2 py-1 text-sm font-semibold transition-colors hover:bg-gray-100 dark:hover:bg-slate-800',
  },
  selectYear: {
    class:
      'cursor-pointer rounded-md px-2 py-1 text-sm font-semibold transition-colors hover:bg-gray-100 dark:hover:bg-slate-800',
  },
  decade: {
    class: 'text-sm font-semibold text-gray-800 dark:text-gray-100',
  },
  dayView: {
    class: 'w-full border-collapse',
  },
  tableHeader: {
    class: '',
  },
  tableHeaderRow: {
    class: '',
  },
  tableHeaderCell: {
    class: 'p-0 text-center',
  },
  weekDay: {
    class: 'block py-1.5 text-xs font-medium text-gray-500 dark:text-gray-400',
  },
  tableBody: {
    class: '',
  },
  tableBodyRow: {
    class: '',
  },
  dayCell: {
    class: 'p-0.5 text-center',
  },
  day: ({ context }: any) => ({
    class: [
      'inline-flex h-8 w-8 cursor-pointer items-center justify-center rounded-lg text-sm transition-colors',
      context.selected
        ? 'bg-[rgb(var(--cf-color-primary-500-rgb)/1)] font-medium text-white'
        : context.today
          ? 'font-medium text-[rgb(var(--cf-color-primary-600-rgb)/1)] dark:text-[rgb(var(--cf-color-primary-100-rgb)/1)]'
          : context.otherMonth
            ? 'text-gray-300 dark:text-gray-600'
            : 'text-gray-700 hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-slate-800',
    ],
  }),
  monthView: {
    class: 'grid grid-cols-3 gap-2 p-3',
  },
  month: ({ context }: any) => ({
    class: [
      'cursor-pointer rounded-lg px-3 py-2 text-center text-sm transition-colors',
      context.selected
        ? 'bg-[rgb(var(--cf-color-primary-500-rgb)/1)] font-medium text-white'
        : 'text-gray-700 hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-slate-800',
    ],
  }),
  yearView: {
    class: 'grid grid-cols-4 gap-2 p-3',
  },
  year: ({ context }: any) => ({
    class: [
      'cursor-pointer rounded-lg px-3 py-2 text-center text-sm transition-colors',
      context.selected
        ? 'bg-[rgb(var(--cf-color-primary-500-rgb)/1)] font-medium text-white'
        : 'text-gray-700 hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-slate-800',
    ],
  }),
  timePicker: {
    class:
      'flex items-center justify-center gap-2 border-t border-gray-200 px-3 py-2.5 dark:border-dark-border',
  },
  hourPicker: {
    class: 'flex flex-col items-center gap-1',
  },
  minutePicker: {
    class: 'flex flex-col items-center gap-1',
  },
  secondPicker: {
    class: 'flex flex-col items-center gap-1',
  },
  hour: {
    class: 'text-sm font-medium tabular-nums text-gray-800 dark:text-gray-100',
  },
  minute: {
    class: 'text-sm font-medium tabular-nums text-gray-800 dark:text-gray-100',
  },
  second: {
    class: 'text-sm font-medium tabular-nums text-gray-800 dark:text-gray-100',
  },
  separatorContainer: {
    class: 'flex items-center',
  },
  separator: {
    class: 'text-sm font-medium text-gray-500 dark:text-gray-400',
  },
  pcIncrementButton: {
    root: {
      class:
        'inline-flex h-6 w-6 cursor-pointer items-center justify-center rounded border-0 bg-transparent text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-slate-800',
    },
    icon: {
      class: 'text-xs',
    },
  },
  pcDecrementButton: {
    root: {
      class:
        'inline-flex h-6 w-6 cursor-pointer items-center justify-center rounded border-0 bg-transparent text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-slate-800',
    },
    icon: {
      class: 'text-xs',
    },
  },
  buttonbar: {
    class:
      'flex items-center justify-between border-t border-gray-200 px-3 py-2 dark:border-dark-border',
  },
  pcTodayButton: {
    root: {
      class:
        'cursor-pointer rounded-lg border-0 bg-transparent px-3 py-1.5 text-xs font-medium text-[rgb(var(--cf-color-primary-600-rgb)/1)] transition-colors hover:bg-[rgb(var(--cf-color-primary-50-rgb)/1)] dark:text-[rgb(var(--cf-color-primary-100-rgb)/1)] dark:hover:bg-[rgb(var(--cf-color-primary-500-rgb)/0.18)]',
    },
  },
  pcClearButton: {
    root: {
      class:
        'cursor-pointer rounded-lg border-0 bg-transparent px-3 py-1.5 text-xs font-medium text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-slate-800 dark:hover:text-gray-200',
    },
  },
}))
</script>

<template>
  <DatePicker
    v-model="model"
    :placeholder="placeholder"
    :disabled="disabled"
    :showTime="showTime"
    :hourFormat="hourFormat"
    :showSeconds="showSeconds"
    :showButtonBar="showButtonBar"
    :dateFormat="dateFormat"
    :pt="pt"
    class="w-full"
  >
    <template #dropdownicon>
      <i class="fas fa-calendar-days"></i>
    </template>
    <template #clearicon="slotProps">
      <i class="fas fa-xmark" @click.stop="slotProps.clearCallback($event)"></i>
    </template>
  </DatePicker>
</template>
