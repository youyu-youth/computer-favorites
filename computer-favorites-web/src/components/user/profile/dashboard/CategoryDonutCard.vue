<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * 分类访问量占比 donut（GitHub Stats 风格紧凑配色）
 */
import type { EChartsOption } from 'echarts'
import { computed } from 'vue'
import StatCard from './StatCard.vue'
import { useEchart } from './useEchart'
import { buildTooltip, chartCategorical } from './theme'

interface PieDatum {
  name: string
  value: number
}

const props = defineProps<{
  title: string
  data: PieDatum[]
  selected?: string | null
}>()

const emit = defineEmits<{
  (e: 'select', name: string | null): void
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
    color: chartCategorical,
    tooltip: {
      ...buildTooltip(isDark),
      trigger: 'item',
      formatter: (rawParams: unknown) => {
        const point = rawParams as { name: string; value: number; percent: number }
        return `${point.name}<br/>访问量：${numberFormatter.format(point.value)}<br/>占比：${point.percent}%`
      },
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      itemGap: 12,
      textStyle: {
        color: isDark ? '#cbd5e1' : '#475569',
        fontSize: 11,
        fontFamily: 'ui-monospace, SFMono-Regular, Menlo, monospace',
      },
    },
    series: [
      {
        name: '分类访问占比',
        type: 'pie',
        radius: ['54%', '78%'],
        center: ['50%', '46%'],
        avoidLabelOverlap: true,
        padAngle: 1.5,
        itemStyle: {
          borderColor: isDark ? '#000' : '#fff',
          borderWidth: 2,
          borderRadius: 2,
        },
        label: {
          show: false,
        },
        labelLine: { show: false },
        emphasis: {
          scale: true,
          scaleSize: 4,
          itemStyle: { shadowBlur: 8, shadowColor: 'rgba(0,0,0,0.25)' },
        },
        data: props.data.map((item) => ({
          ...item,
          selected: item.name === props.selected,
        })),
      },
    ],
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: '40%',
        style: {
          text: numberFormatter.format(total.value),
          fill: isDark ? '#f1f5f9' : '#0f172a',
          fontSize: 18,
          fontWeight: 600,
          fontFamily: 'ui-monospace, SFMono-Regular, Menlo, monospace',
        },
      },
      {
        type: 'text',
        left: 'center',
        top: '53%',
        style: {
          text: 'Total PV',
          fill: isDark ? '#64748b' : '#94a3b8',
          fontSize: 10,
          fontFamily: 'ui-monospace, SFMono-Regular, Menlo, monospace',
        },
      },
    ],
  }
}

const { containerRef } = useEchart({
  buildOption,
  onClick: (params: unknown) => {
    const payload = params as { name?: string }
    if (typeof payload.name !== 'string') {
      return
    }
    emit('select', props.selected === payload.name ? null : payload.name)
  },
})
</script>

<template>
  <StatCard :title="title">
    <template #actions>
      <button
        v-if="selected"
        type="button"
        class="cursor-pointer rounded-sm border border-black/10 px-2 py-0.5 text-[11px] font-medium text-gray-600 transition-colors hover:bg-black/5 dark:border-white/10 dark:text-gray-300 dark:hover:bg-white/5"
        @click="emit('select', null)"
      >
        恢复默认
      </button>
    </template>
    <div ref="containerRef" class="h-56 w-full sm:h-60" />
  </StatCard>
</template>
