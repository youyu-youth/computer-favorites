<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * 贡献活跃图：GitHub Stats 风格紧凑热力图（纯黑底 + 4 阶梯 emerald）
 * 2026-05-08: 去 mock 对接后端 ContributionGraph API（user-15 M3）
 */
import { Activity } from 'lucide-vue-next'
import { computed, onMounted } from 'vue'
import StatCard from './dashboard/StatCard.vue'
import { storeToRefs } from 'pinia'
import { useProfileDashboardStore } from '@/stores/profileDashboard'
import type { ContributionCell } from '@/api/user-profile-dashboard'

const store = useProfileDashboardStore()
const { graph, currentYear } = storeToRefs(store)

const nowYear = new Date().getFullYear()
const yearOptions = [nowYear, nowYear - 1, nowYear - 2]
const weekdayLabels = ['Mon', 'Wed', 'Fri']

/** 把后端 cells 转为 7 行 × N 列 grid，第 0 行=周一...第 6 行=周日（ISO） */
const gridColumns = computed<Array<Array<ContributionCell | null>>>(() => {
  const g = graph.value.data
  if (!g || !g.cells || g.cells.length === 0) return []

  const first = new Date(`${g.cells[0]!.date}T00:00:00`)
  // JavaScript getDay: 0=Sun, 1=Mon ... 6=Sat；ISO 周一=0，周日=6
  const jsDay = first.getDay()
  const isoRow = jsDay === 0 ? 6 : jsDay - 1

  const columns: Array<Array<ContributionCell | null>> = []
  let col: Array<ContributionCell | null> = new Array(7).fill(null)
  let rowCursor = isoRow
  for (const cell of g.cells) {
    col[rowCursor] = cell
    rowCursor += 1
    if (rowCursor >= 7) {
      columns.push(col)
      col = new Array(7).fill(null)
      rowCursor = 0
    }
  }
  // 推入剩余不满 7 格的最后一列
  if (col.some((c) => c !== null)) columns.push(col)
  return columns
})

/** 月份标签基于后端返回的 colOffset */
const monthBadges = computed(() => graph.value.data?.months ?? [])

const contributionCount = computed(() => graph.value.data?.total ?? 0)

const isLoading = computed(() => graph.value.state === 'loading')
const errorText = computed(() => graph.value.error)

const getCellClass = (cell: ContributionCell | null) => {
  if (!cell || cell.level === 0) return 'bg-black/5 dark:bg-white/5'
  if (cell.level === 1) return 'bg-emerald-700/60'
  if (cell.level === 2) return 'bg-emerald-600/80'
  if (cell.level === 3) return 'bg-emerald-500'
  return 'bg-emerald-400'
}

/** 根据月份 colOffset 计算标签的绝对像素左位置（cell 10px + gap 4px ≈ 14px/列） */
const COL_WIDTH = 14
const getMonthLeftStyle = (colOffset: number) => ({
  left: `${colOffset * COL_WIDTH}px`,
})

const handleYearChange = (year: number) => {
  store.setGraphYear(year)
}

onMounted(() => {
  if (graph.value.state === 'idle') {
    store.loadGraph()
  }
})
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
        {{ contributionCount }} contributions in {{ currentYear }}
      </span>
    </header>

    <StatCard>
    <div v-if="isLoading" class="h-32 w-full animate-pulse rounded-sm bg-black/5 dark:bg-white/5" />
    <div v-else-if="errorText" class="py-6 text-center font-mono text-[12px] text-red-500 dark:text-red-400">
      {{ errorText }}
    </div>
    <div v-else class="flex flex-col gap-3 sm:flex-row">
      <div class="min-w-0 flex-1 space-y-2">
        <div class="overflow-x-auto pb-1">
          <div class="w-max space-y-1.5">
            <div class="relative pl-12 font-mono text-[10px] text-gray-500 dark:text-gray-400 h-3">
              <span
                v-for="(m, idx) in monthBadges"
                :key="`m-${idx}`"
                class="absolute top-0 leading-none"
                :style="getMonthLeftStyle(m.colOffset)"
              >
                {{ m.label }}
              </span>
            </div>
            <div class="flex gap-1.5">
              <div class="w-10 space-y-1.5 font-mono text-[10px] text-gray-500 dark:text-gray-400">
                <div v-for="label in weekdayLabels" :key="label" class="h-[10px] flex items-center">
                  {{ label }}
                </div>
              </div>
              <div class="flex gap-1">
                <div
                  v-for="(col, colIndex) in gridColumns"
                  :key="`col-${colIndex}`"
                  class="flex flex-col gap-1"
                >
                  <div
                    v-for="(cell, rowIndex) in col"
                    :key="`cell-${colIndex}-${rowIndex}`"
                    class="size-[10px] rounded-[2px] transition-colors"
                    :class="getCellClass(cell)"
                    :title="cell ? `${cell.date}: ${cell.count}` : ''"
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
              currentYear === year
                ? 'border-amber-500/40 bg-amber-500/15 text-amber-600 dark:text-amber-400'
                : 'border-black/5 text-gray-600 hover:bg-black/5 dark:border-white/[0.06] dark:text-gray-400 dark:hover:bg-white/5'
            "
            @click="handleYearChange(year)"
          >
            {{ year }}
          </button>
        </div>
      </div>
    </div>
    </StatCard>
  </section>
</template>
