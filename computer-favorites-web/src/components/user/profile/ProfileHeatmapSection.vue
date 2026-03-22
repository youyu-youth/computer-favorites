<script setup lang="ts">
import { computed, ref } from 'vue'

const columns = 26
const rows = 7
const maxValue = 10
const yearOptions = [2026, 2025, 2024]
const selectedYear = ref(yearOptions[0] || new Date().getFullYear())
const monthLabels = [
  'Mar',
  'Apr',
  'May',
  'Jun',
  'Jul',
  'Aug',
  'Sep',
  'Oct',
  'Nov',
  'Dec',
  'Jan',
  'Feb',
]
const monthOffsets = [0, 2, 4, 6, 8, 10, 13, 15, 17, 19, 21, 23]
const weekdayLabels = ['Mon', 'Wed', 'Fri']

const yearSeed = computed(() => selectedYear.value % 97)

const contributionGrid = computed(() =>
  Array.from({ length: rows }, (_, row) =>
    Array.from(
      { length: columns },
      (_, col) => (row * 13 + col * 17 + row * col * 3 + yearSeed.value) % (maxValue + 1),
    ),
  ),
)

const contributionCount = computed(
  () => contributionGrid.value.flat().filter((value) => value > 0).length,
)

const getCellClass = (value: number) => {
  if (value >= 8) return 'bg-emerald-500'
  if (value >= 6) return 'bg-emerald-600'
  if (value >= 4) return 'bg-emerald-700'
  if (value >= 2) return 'bg-emerald-800/80'
  return 'bg-gray-200 dark:bg-gray-700'
}

const getMonthOffsetStyle = (idx: number) => {
  const current = monthOffsets[idx] ?? 0
  if (idx === 0) {
    return { marginLeft: `${current * 2}px` }
  }
  const prev = monthOffsets[idx - 1] ?? 0
  return { marginLeft: `${(current - prev - 1) * 14}px` }
}
</script>

<template>
  <UCard
    class="!ring-0 shadow-sm dark:shadow-md bg-white dark:!bg-[#131418] rounded-xl overflow-hidden"
  >
    <div class="space-y-4">
      <div class="flex items-center justify-between gap-2">
        <p class="text-sm sm:text-base font-semibold text-gray-900 dark:text-gray-100">
          {{ contributionCount }} contributions in the last year
        </p>
        <span class="text-xs text-gray-500 dark:text-gray-400">Contribution settings</span>
      </div>
      <div class="flex items-center gap-2">
        <UIcon name="i-lucide-chart-no-axes-combined" class="size-5 text-primary-500" />
        <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">贡献活跃图</h3>
      </div>
      <div class="flex flex-col sm:flex-row gap-4">
        <div class="min-w-0 flex-1 space-y-3">
          <div class="overflow-x-auto pb-1">
            <div class="w-max space-y-2">
              <div class="flex pl-14 text-xs text-gray-500 dark:text-gray-400">
                <span
                  v-for="(month, idx) in monthLabels"
                  :key="month"
                  class="leading-none"
                  :style="getMonthOffsetStyle(idx)"
                >
                  {{ month }}
                </span>
              </div>
              <div class="flex gap-2">
                <div class="w-10 space-y-2 text-xs text-gray-500 dark:text-gray-400">
                  <div v-for="label in weekdayLabels" :key="label" class="h-5 flex items-center">
                    {{ label }}
                  </div>
                </div>
                <div class="space-y-1.5">
                  <div
                    v-for="(week, rowIndex) in contributionGrid"
                    :key="`week-${rowIndex}`"
                    class="flex gap-1.5"
                  >
                    <div
                      v-for="(value, colIndex) in week"
                      :key="`cell-${rowIndex}-${colIndex}`"
                      class="size-3 sm:size-3.5 rounded-sm"
                      :class="getCellClass(value)"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
            <span>Learn how we count contributions</span>
            <div class="flex items-center gap-1">
              <span>Less</span>
              <span class="size-3 rounded-sm bg-gray-200 dark:bg-gray-700" />
              <span class="size-3 rounded-sm bg-emerald-800/80" />
              <span class="size-3 rounded-sm bg-emerald-700" />
              <span class="size-3 rounded-sm bg-emerald-600" />
              <span class="size-3 rounded-sm bg-emerald-500" />
              <span>More</span>
            </div>
          </div>
        </div>
        <div class="sm:w-24">
          <div class="flex sm:flex-col gap-2">
            <button
              v-for="year in yearOptions"
              :key="year"
              type="button"
              class="h-9 px-3 rounded-md text-sm transition-colors"
              :class="
                selectedYear === year
                  ? 'bg-primary-500 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200 dark:bg-white/10 dark:text-gray-300 dark:hover:bg-white/20'
              "
              @click="selectedYear = year"
            >
              {{ year }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </UCard>
</template>
