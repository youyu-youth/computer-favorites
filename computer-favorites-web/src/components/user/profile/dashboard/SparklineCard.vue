<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * 单数值 + 紧凑 area sparkline 趋势卡
 */
import type { EChartsOption } from 'echarts'
import type { Component } from 'vue'
import { computed } from 'vue'
import StatCard from './StatCard.vue'
import { useEchart } from './useEchart'
import { buildTooltip } from './theme'
import type { SparklinePoint } from './types'

const props = defineProps<{
  label: string
  value: string
  delta?: number
  data: SparklinePoint[]
  color: string
  icon?: Component
}>()

const trendIcon = computed(() => {
  if (typeof props.delta !== 'number') return ''
  if (props.delta > 0) return '▲'
  if (props.delta < 0) return '▼'
  return '–'
})

const trendColor = computed(() => {
  if (typeof props.delta !== 'number') return 'text-gray-500 dark:text-gray-400'
  if (props.delta > 0) return 'text-emerald-500'
  if (props.delta < 0) return 'text-rose-500'
  return 'text-gray-500 dark:text-gray-400'
})

const buildOption = (isDark: boolean): EChartsOption => ({
  grid: { left: 0, right: 0, top: 4, bottom: 0 },
  tooltip: {
    ...buildTooltip(isDark),
    trigger: 'axis',
    axisPointer: { type: 'line', lineStyle: { color: props.color, opacity: 0.4 } },
    formatter: (rawParams: unknown) => {
      const list = (rawParams as Array<{ axisValueLabel?: string; value?: number }>) || []
      const row = list[0]
      if (!row) return ''
      const v = typeof row.value === 'number' ? row.value : 0
      return `${row.axisValueLabel ?? ''}<br/>${props.label}：${v.toLocaleString('zh-CN')}`
    },
  },
  xAxis: {
    type: 'category',
    show: false,
    boundaryGap: false,
    data: props.data.map((d) => d.date),
  },
  yAxis: { type: 'value', show: false },
  series: [
    {
      type: 'line',
      smooth: true,
      symbol: 'none',
      lineStyle: { width: 1.5, color: props.color },
      areaStyle: {
        opacity: 1,
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: hexToRgba(props.color, 0.32) },
            { offset: 1, color: hexToRgba(props.color, 0) },
          ],
        },
      },
      data: props.data.map((d) => d.value),
    },
  ],
})

const hexToRgba = (hex: string, alpha: number): string => {
  const m = hex.replace('#', '')
  const bigint = parseInt(m, 16)
  const r = (bigint >> 16) & 255
  const g = (bigint >> 8) & 255
  const b = bigint & 255
  return `rgba(${r},${g},${b},${alpha})`
}

const { containerRef } = useEchart({ buildOption })
</script>

<template>
  <StatCard dense>
    <div class="flex flex-col gap-1.5">
      <div class="flex items-center justify-between gap-2">
        <span class="flex items-center gap-1.5 text-[11px] uppercase tracking-wider text-gray-500 dark:text-gray-400">
          <component
            :is="icon"
            v-if="icon"
            class="size-3.5"
            :style="{ color }"
            :stroke-width="2"
          />
          {{ label }}
        </span>
        <span
          v-if="typeof delta === 'number'"
          class="font-mono text-[11px]"
          :class="trendColor"
        >
          {{ trendIcon }} {{ Math.abs(delta).toFixed(1) }}%
        </span>
      </div>
      <div class="flex items-end justify-between gap-2">
        <span
          class="font-mono text-xl font-semibold leading-none text-gray-900 dark:text-gray-100 sm:text-2xl"
          :style="{ color }"
        >
          {{ value }}
        </span>
      </div>
      <div ref="containerRef" class="h-12 w-full sm:h-14" />
    </div>
  </StatCard>
</template>
