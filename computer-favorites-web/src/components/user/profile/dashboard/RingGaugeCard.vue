<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * GitHub Stats 风格主卡：左侧统计列表 + 右侧 ring gauge 评级
 */
import type { EChartsOption } from 'echarts'
import { computed } from 'vue'
import StatCard from './StatCard.vue'
import { useEchart } from './useEchart'
import { computeRank } from './rank'
import { ringTrack } from './theme'

interface StatItem {
  label: string
  value: string
  color?: string
}

const props = defineProps<{
  title: string
  score: number
  stats: StatItem[]
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
        width: 6,
        roundCap: true,
        itemStyle: { color: rankInfo.value.color },
      },
      axisLine: {
        lineStyle: {
          width: 6,
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
</script>

<template>
  <StatCard :title="title">
    <div class="flex items-stretch gap-3">
      <ul class="flex flex-1 flex-col justify-center gap-1.5 text-sm">
        <li
          v-for="item in stats"
          :key="item.label"
          class="flex items-center justify-between gap-2 font-mono"
        >
          <span
            class="flex items-center gap-1.5 text-[12.5px] font-medium"
            :style="{ color: item.color ?? rankInfo.color }"
          >
            <span class="inline-block size-1.5 rounded-full" :style="{ backgroundColor: item.color ?? rankInfo.color }" />
            {{ item.label }}
          </span>
          <span class="font-mono text-[12.5px] text-gray-900 dark:text-gray-100">
            {{ item.value }}
          </span>
        </li>
      </ul>

      <div class="relative flex w-24 shrink-0 items-center justify-center sm:w-28">
        <div ref="containerRef" class="h-24 w-24 sm:h-28 sm:w-28" />
        <div class="absolute inset-0 flex flex-col items-center justify-center">
          <span
            class="font-mono text-xl font-semibold leading-none sm:text-2xl"
            :style="{ color: rankInfo.color }"
          >
            {{ rankInfo.rank }}
          </span>
          <span class="mt-0.5 font-mono text-[10px] uppercase tracking-widest text-gray-500 dark:text-gray-400">
            {{ score }}%
          </span>
        </div>
      </div>
    </div>
  </StatCard>
</template>
