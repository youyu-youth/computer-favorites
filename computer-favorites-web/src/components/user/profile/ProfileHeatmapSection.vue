<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * 贡献活跃图：GitHub Stats 风格紧凑热力图（纯黑底 + 4 阶梯 emerald）
 */
import { Activity } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import StatCard from './dashboard/StatCard.vue'

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
  if (value >= 8) return 'bg-emerald-400'
  if (value >= 6) return 'bg-emerald-500'
  if (value >= 4) return 'bg-emerald-600/80'
  if (value >= 2) return 'bg-emerald-700/60'
  return 'bg-black/5 dark:bg-white/5'
}

const getMonthOffsetStyle = (idx: number) => {
  const current = monthOffsets[idx] ?? 0
  if (idx === 0) {
    return { marginLeft: `${current * 2}px` }
  }
  const prev = monthOffsets[idx - 1] ?? 0
  return { marginLeft: `${(current - prev - 1) * 12}px` }
}
</script>

<template>
  <section class="space-y-3">
    <header class="flex flex-wrap items-center justify-between gap-2">
      <div class="flex items-center gap-2">
        <Activity class="size-4 text-amber-500" :stroke-width="2" />
        <h3 class="text-sm font-semibold tracking-tight text-gray-900 sm:text-base dark:text-gray-100">
          贡献活跃图
        </h3>
        <span class="font-mono text-[11px] uppercase tracking-widest text-gray-500 dark:text-gray-400">
          /heatmap
        </span>
      </div>
      <span class="font-mono text-[11px] text-gray-500 dark:text-gray-400">
        {{ contributionCount }} contributions in the last year
      </span>
    </header>

    <StatCard>
    <div class="flex flex-col gap-3 sm:flex-row">
      <div class="min-w-0 flex-1 space-y-2">
        <div class="overflow-x-auto pb-1">
          <div class="w-max space-y-1.5">
            <div class="flex pl-12 font-mono text-[10px] text-gray-500 dark:text-gray-400">
              <span
                v-for="(month, idx) in monthLabels"
                :key="month"
                class="leading-none"
                :style="getMonthOffsetStyle(idx)"
              >
                {{ month }}
              </span>
            </div>
            <div class="flex gap-1.5">
              <div class="w-10 space-y-1.5 font-mono text-[10px] text-gray-500 dark:text-gray-400">
                <div v-for="label in weekdayLabels" :key="label" class="h-[10px] flex items-center">
                  {{ label }}
                </div>
              </div>
              <div class="space-y-1">
                <div
                  v-for="(week, rowIndex) in contributionGrid"
                  :key="`week-${rowIndex}`"
                  class="flex gap-1"
                >
                  <div
                    v-for="(value, colIndex) in week"
                    :key="`cell-${rowIndex}-${colIndex}`"
                    class="size-[10px] rounded-[2px] transition-colors"
                    :class="getCellClass(value)"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="flex items-center justify-between font-mono text-[10.5px] text-gray-500 dark:text-gray-400">
          <span>Learn how we count contributions</span>
          <div class="flex items-center gap-1">
            <span>Less</span>
            <span class="size-[10px] rounded-[2px] bg-black/5 dark:bg-white/5" />
            <span class="size-[10px] rounded-[2px] bg-emerald-700/60" />
            <span class="size-[10px] rounded-[2px] bg-emerald-600/80" />
            <span class="size-[10px] rounded-[2px] bg-emerald-500" />
            <span class="size-[10px] rounded-[2px] bg-emerald-400" />
            <span>More</span>
          </div>
        </div>
      </div>
      <div class="sm:w-20">
        <div class="flex sm:flex-col gap-1.5">
          <button
            v-for="year in yearOptions"
            :key="year"
            type="button"
            class="cursor-pointer rounded-sm border px-2 py-1 font-mono text-[12px] transition-colors"
            :class="
              selectedYear === year
                ? 'border-amber-500/40 bg-amber-500/15 text-amber-600 dark:text-amber-400'
                : 'border-black/5 text-gray-600 hover:bg-black/5 dark:border-white/[0.06] dark:text-gray-400 dark:hover:bg-white/5'
            "
            @click="selectedYear = year"
          >
            {{ year }}
          </button>
        </div>
      </div>
    </div>
    </StatCard>
  </section>
</template>
