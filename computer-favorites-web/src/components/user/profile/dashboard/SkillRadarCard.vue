<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * 技术栈雷达卡：6 维能力雷达图
 */
import type { EChartsOption } from 'echarts'
import { computed } from 'vue'
import StatCard from './StatCard.vue'
import { useEchart } from './useEchart'
import { buildTooltip, githubStatsPalette, splitDashed } from './theme'
import type { RadarSkill } from './types'

const props = defineProps<{
  title: string
  skills: RadarSkill[]
}>()

const indicators = computed(() =>
  props.skills.map((item) => ({ name: item.name, max: 100 })),
)

const buildOption = (isDark: boolean): EChartsOption => ({
  tooltip: {
    ...buildTooltip(isDark),
    formatter: (rawParams: unknown) => {
      const params = rawParams as { value?: number[] }
      const value = (params.value || []) as number[]
      return props.skills
        .map((item, idx) => `${item.name}：${value[idx] ?? 0}`)
        .join('<br/>')
    },
  },
  radar: {
    indicator: indicators.value,
    radius: '64%',
    center: ['50%', '54%'],
    splitNumber: 4,
    axisName: {
      color: isDark ? '#94a3b8' : '#475569',
      fontSize: 11,
      fontFamily: 'ui-monospace, SFMono-Regular, Menlo, monospace',
    },
    splitLine: {
      lineStyle: {
        color: isDark ? splitDashed.dark : splitDashed.light,
        type: 'dashed',
      },
    },
    splitArea: { show: false },
    axisLine: {
      lineStyle: {
        color: isDark ? splitDashed.dark : splitDashed.light,
      },
    },
  },
  series: [
    {
      type: 'radar',
      symbol: 'circle',
      symbolSize: 4,
      lineStyle: { color: githubStatsPalette.primary, width: 1.5 },
      itemStyle: { color: githubStatsPalette.primary },
      areaStyle: { color: 'rgba(245,158,11,0.16)' },
      data: [{ value: props.skills.map((item) => item.value), name: '能力' }],
    },
  ],
})

const { containerRef } = useEchart({ buildOption })
</script>

<template>
  <StatCard :title="title">
    <div ref="containerRef" class="h-48 w-full sm:h-56" />
  </StatCard>
</template>
