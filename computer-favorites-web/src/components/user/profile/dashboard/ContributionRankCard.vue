<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * 贡献概览主卡：左侧 6 项 stat 列表 + 右侧 ring "A++" 评级
 */
import { Star, GitCommit, GitPullRequest, AlertCircle, Monitor, Flame } from 'lucide-vue-next'
import type { EChartsOption } from 'echarts'
import { computed } from 'vue'
import StatCard from './StatCard.vue'
import { useEchart } from './useEchart'
import { computeRank } from './rank'
import { ringTrack } from './theme'
import type { RankCardStat } from './types'

const iconMap = {
  star: Star,
  commit: GitCommit,
  pr: GitPullRequest,
  issue: AlertCircle,
  monitor: Monitor,
  flame: Flame,
}

const props = defineProps<{
  title: string
  score: number
  stats: RankCardStat[]
}>()

const rankInfo = computed(() => computeRank(props.score))

const buildOption = (isDark: boolean): EChartsOption => ({
  series: [
    {
      type: 'gauge',
      startAngle: 225,
      endAngle: -45,
      radius: '92%',
      center: ['50%', '54%'],
      progress: {
        show: true,
        width: 7,
        roundCap: true,
        itemStyle: { color: rankInfo.value.color },
      },
      axisLine: {
        lineStyle: {
          width: 7,
          color: [[1, isDark ? ringTrack.dark : ringTrack.light]],
        },
      },
      pointer: { show: false },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      anchor: { show: false },
      title: { show: false },
      detail: { show: false },
      data: [{ value: Math.max(0, Math.min(100, props.score)) }],
    },
  ],
})

const { containerRef } = useEchart({ buildOption })

const accentStyle = computed(() => ({ color: rankInfo.value.color }))
</script>

<template>
  <StatCard :title="title" :accent="rankInfo.color">
    <div class="flex items-stretch gap-4">
      <ul class="grid flex-1 grid-cols-1 content-center gap-2 sm:grid-cols-2">
        <li
          v-for="item in stats"
          :key="item.label"
          class="flex items-center gap-2 text-[12.5px]"
        >
          <component
            :is="iconMap[item.icon]"
            class="size-3.5 shrink-0"
            :style="accentStyle"
            :stroke-width="2"
          />
          <span class="truncate text-gray-700 dark:text-gray-300" :style="accentStyle">
            {{ item.label }}:
          </span>
          <span class="ml-auto font-mono text-gray-900 dark:text-gray-100">{{ item.value }}</span>
        </li>
      </ul>

      <div class="relative flex w-28 shrink-0 items-center justify-center sm:w-32">
        <div ref="containerRef" class="h-28 w-28 sm:h-32 sm:w-32" />
        <div class="absolute inset-0 flex flex-col items-center justify-center">
          <span class="font-mono text-2xl font-semibold leading-none" :style="accentStyle">
            {{ rankInfo.rank }}
          </span>
          <span class="mt-1 font-mono text-[10px] uppercase tracking-widest text-gray-500 dark:text-gray-400">
            {{ score }}%
          </span>
        </div>
      </div>
    </div>
  </StatCard>
</template>
