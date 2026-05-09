<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * 内容类型占比纤细柱图（GitHub Stats 风格）
 */
import type { EChartsOption } from 'echarts'
import { computed, watch } from 'vue'
import StatCard from './StatCard.vue'
import { useEchart } from './useEchart'
import { buildAxisLabel, buildTooltip, githubStatsPalette, splitDashed } from './theme'

interface BarDatum {
  name: string
  value: number
}

const props = defineProps<{
  title: string
  data: BarDatum[]
}>()

const numberFormatter = new Intl.NumberFormat('zh-CN')

const total = computed(() => props.data.reduce((sum, item) => sum + item.value, 0))

const buildOption = (isDark: boolean): EChartsOption => {
  if (!props.data.length) {
    return {
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: {
          color: isDark ? '#94a3b8' : '#64748b',
          fontSize: 12,
          fontWeight: 400,
        },
      },
    }
  }
  return {
    grid: { left: 8, right: 8, top: 12, bottom: 28, containLabel: true },
    tooltip: {
      ...buildTooltip(isDark),
      trigger: 'axis',
      axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(245,158,11,0.06)' } },
      formatter: (rawParams: unknown) => {
        const rows = (rawParams as Array<{ name: string; value: number }>) || []
        const row = rows[0]
        if (!row) return ''
        const percent = total.value > 0 ? ((row.value / total.value) * 100).toFixed(1) : '0.0'
        return `${row.name}<br/>内容数量：${numberFormatter.format(row.value)}<br/>占比：${percent}%`
      },
    },
    xAxis: {
      type: 'category',
      data: props.data.map((item) => item.name),
      axisLabel: {
        ...buildAxisLabel(isDark),
        interval: 0,
        rotate: 18,
      },
      axisLine: { lineStyle: { color: isDark ? splitDashed.dark : splitDashed.light } },
      axisTick: { show: false },
    },
    yAxis: {
      type: 'value',
      axisLabel: buildAxisLabel(isDark),
      splitLine: {
        lineStyle: {
          color: isDark ? splitDashed.dark : splitDashed.light,
          type: 'dashed',
        },
      },
    },
    series: [
      {
        name: '内容数量',
        type: 'bar',
        barWidth: 12,
        itemStyle: {
          color: githubStatsPalette.primary,
          borderRadius: [3, 3, 0, 0],
        },
        emphasis: {
          itemStyle: { color: githubStatsPalette.amber },
        },
        data: props.data.map((item) => item.value),
      },
    ],
  }
}

const { containerRef, rerender } = useEchart({ buildOption })

watch(() => props.data, () => {
  void rerender()
}, { deep: true })
</script>

<template>
  <StatCard :title="title">
    <div ref="containerRef" class="h-56 w-full min-w-0 sm:h-60" />
  </StatCard>
</template>
