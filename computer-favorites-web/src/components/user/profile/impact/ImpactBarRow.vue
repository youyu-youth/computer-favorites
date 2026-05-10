<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * 上传影响力看板 — 右栏单行总额柱组件。
 *
 * 行布局：[● 指标名 80px] [横向比例柱 1fr] [数值 72px] [delta 56px]
 * 行高 24px；柱宽 = (value / globalMax) × 100%（CSS 原生实现，无 ECharts）。
 *
 * 数据口径与 range 联动：
 *  - 7d   → 7 天 rangeCounts.metric  总和
 *  - 30d  → 30 天 rangeCounts.metric 总和
 *  - 90d  → 90 天 rangeCounts.metric 总和
 *  - all  → totals.metric 全时累计
 *
 * 始终只渲染 5 行（每个指标 1 行）。
 */
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import { getMetricHex, metricLabel, withAlpha, type MetricKey } from './palette'

const props = defineProps<{
  metric: MetricKey
  /** 当前 range 内累计 / 全时累计 */
  total: number
  /** 5 指标共享归一化最大值（globalMax） */
  max: number
  /** 环比百分比；range=all 时为 null */
  delta: number | null
}>()

const appStore = useAppStore()

const formatCompact = (n: number): string =>
  new Intl.NumberFormat('zh-CN', { notation: 'compact', maximumFractionDigits: 1 }).format(n)

const formattedTotal = computed(() => formatCompact(props.total))

const dotColor = computed(() => getMetricHex(props.metric, appStore.isDark))

/** 柱填充宽度百分比；max=0 时退化为 0 */
const fillPercent = computed(() => {
  const m = Math.max(0, props.max)
  if (m <= 0) {
    return 0
  }
  return Math.min(100, (props.total / m) * 100)
})

/** 柱底色（轨道）：当前指标色 12~14% alpha，统一霓虹辉光感 */
const trackBgColor = computed(() => withAlpha(dotColor.value, appStore.isDark ? 0.14 : 0.1))

/** 柱填充色：当前指标实色 */
const fillBgColor = computed(() => dotColor.value)

/** 柱填充辉光：用 box-shadow 模拟霓虹光晕 */
const fillBoxShadow = computed(() =>
  props.total > 0 ? `0 0 6px 0 ${withAlpha(dotColor.value, 0.55)}` : 'none',
)

const deltaText = computed(() => {
  if (props.delta === null || props.delta === undefined) {
    return ''
  }
  if (props.delta === 0) {
    return '0%'
  }
  const arrow = props.delta > 0 ? '↑' : '↓'
  return `${arrow}${Math.abs(props.delta).toFixed(0)}%`
})

const deltaClass = computed(() => {
  if (props.delta === null || props.delta === undefined) {
    return ''
  }
  if (props.delta > 0) {
    return 'text-emerald-500/90 dark:text-emerald-400/90'
  }
  if (props.delta < 0) {
    return 'text-red-500/90 dark:text-red-400/90'
  }
  return 'text-zinc-500/80 dark:text-zinc-400/80'
})

const titleAttr = computed(
  () => `${metricLabel[props.metric]}：${props.total.toLocaleString('zh-CN')}`,
)
</script>

<template>
  <div
    class="grid items-center gap-x-2"
    :style="{ gridTemplateColumns: '80px 1fr 72px 56px', height: '24px' }"
    :title="titleAttr"
  >
    <!-- 指标名 + 色块 -->
    <div class="flex items-center gap-1.5 text-[12px] text-zinc-700 dark:text-zinc-300">
      <span
        class="inline-block size-2 rounded-full"
        :style="{ backgroundColor: dotColor }"
        aria-hidden="true"
      />
      <span class="truncate">{{ metricLabel[props.metric] }}</span>
    </div>

    <!-- 横向比例柱 -->
    <div
      class="impact-bar-track h-2 w-full overflow-hidden rounded-full"
      :style="{ backgroundColor: trackBgColor }"
    >
      <div
        class="impact-bar-fill h-full rounded-full"
        :style="{
          width: `${fillPercent}%`,
          backgroundColor: fillBgColor,
          boxShadow: fillBoxShadow,
        }"
      />
    </div>

    <!-- 数值 -->
    <div class="text-right font-mono text-[12px] tabular-nums text-zinc-800 dark:text-zinc-200">
      {{ formattedTotal }}
    </div>

    <!-- delta -->
    <div class="text-right font-mono text-[11px] tabular-nums" :class="deltaClass">
      <template v-if="deltaText">{{ deltaText }}</template>
    </div>
  </div>
</template>

<style scoped>
.impact-bar-fill {
  transition:
    width 700ms cubic-bezier(0.22, 1, 0.36, 1),
    background-color 200ms ease,
    box-shadow 200ms ease;
}
</style>
