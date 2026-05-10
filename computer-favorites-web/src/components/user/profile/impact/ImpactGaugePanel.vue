<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * 上传影响力看板 — 左栏 270° 半圆 Gauge + 5 弧叠加 + 5 行 Legend。
 *
 * 几何：以 (cx, cy) 为圆心，半径 R 的圆环。
 *  - 角度系：0°=正上方，顺时针递增；
 *  - 弧起点 225°（左下方向）→ 终点 135°（右下方向），总弧长 270°，下方 90° 缺口；
 *  - 5 段彩色弧按 share 叠加（不是各自独立小弧），段顺序与 legend 一致：
 *    browse → like → collect → comment → score。
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useAppStore } from '@/stores/app'
import {
  IMPACT_PALETTE,
  METRIC_ORDER,
  metricLabel,
  type MetricKey,
} from './palette'
import type { ImpactRange, MetricGroup } from '@/api/user-profile-impact'

const props = defineProps<{
  totals: MetricGroup
  rangeCounts: MetricGroup
  range: ImpactRange
}>()

const appStore = useAppStore()

/* ---------- 几何常量 ---------- */
const VIEW_W = 160
const VIEW_H = 130
const CX = 80
const CY = 78
const R = 54
/** 弧总跨度（度） */
const ARC_SPAN_DEG = 270
/** 起始角（0°=正上方，顺时针正） */
const ARC_START_DEG = 225

/* ---------- 数据派生 ---------- */
const sourceGroup = computed<MetricGroup>(() =>
  props.range === 'all' ? props.totals : props.rangeCounts,
)

const sum = computed(() =>
  METRIC_ORDER.reduce((acc, m) => acc + (sourceGroup.value[m] || 0), 0),
)

interface Segment {
  metric: MetricKey
  count: number
  share: number
  spanDeg: number
  startDeg: number
  endDeg: number
  d: string
  color: string
}

/* ---------- 极坐标 → SVG 坐标 ---------- */
const polarToCartesian = (cx: number, cy: number, r: number, angleDeg: number) => {
  const a = ((angleDeg - 90) * Math.PI) / 180
  return { x: cx + r * Math.cos(a), y: cy + r * Math.sin(a) }
}

/**
 * 描述一段顺时针弧。
 * angle 系：0°=正上方，顺时针递增。
 */
const describeArc = (
  cx: number,
  cy: number,
  r: number,
  startAngle: number,
  endAngle: number,
): string => {
  const span = endAngle - startAngle
  const start = polarToCartesian(cx, cy, r, startAngle)
  const end = polarToCartesian(cx, cy, r, endAngle)
  const largeArc = span > 180 ? 1 : 0
  return `M ${start.x.toFixed(2)} ${start.y.toFixed(2)} A ${r} ${r} 0 ${largeArc} 1 ${end.x.toFixed(2)} ${end.y.toFixed(2)}`
}

const segments = computed<Segment[]>(() => {
  const total = sum.value
  if (total <= 0) {
    return []
  }
  let cursor = ARC_START_DEG
  const list: Segment[] = []
  METRIC_ORDER.forEach((metric, idx) => {
    const count = sourceGroup.value[metric] || 0
    const share = count / total
    const isLast = idx === METRIC_ORDER.length - 1
    const startDeg = cursor
    /** 末段直接吃掉浮点残量，避免累计误差导致缺口 */
    const endDeg = isLast ? ARC_START_DEG + ARC_SPAN_DEG : cursor + share * ARC_SPAN_DEG
    cursor = endDeg
    list.push({
      metric,
      count,
      share,
      spanDeg: endDeg - startDeg,
      startDeg,
      endDeg,
      d: describeArc(CX, CY, R, startDeg, endDeg),
      color: appStore.isDark
        ? IMPACT_PALETTE[metric].dark
        : IMPACT_PALETTE[metric].light,
    })
  })
  return list
})

const backgroundArcD = computed(() =>
  describeArc(CX, CY, R, ARC_START_DEG, ARC_START_DEG + ARC_SPAN_DEG),
)

/* ---------- 中心文字 ---------- */
const formatCompact = (n: number): string =>
  new Intl.NumberFormat('zh-CN', { notation: 'compact', maximumFractionDigits: 1 }).format(n)

const centerNumber = computed(() => formatCompact(sum.value))

const centerLabel = computed(() => {
  if (props.range === 'all') {
    return '累计互动'
  }
  const days = props.range === '7d' ? 7 : props.range === '30d' ? 30 : 90
  return `近 ${days} 天互动`
})

const trackColor = computed(() => (appStore.isDark ? '#27272a' : '#e5e7eb'))

/* ---------- 入场动画：stagger 揭示 5 段 ---------- */
const revealed = ref(false)

const triggerReveal = () => {
  revealed.value = false
  /** 下一帧再设为 true，确保 transition 生效 */
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      revealed.value = true
    })
  })
}

onMounted(() => {
  triggerReveal()
})

watch(
  () => [props.range, props.totals, props.rangeCounts] as const,
  () => {
    triggerReveal()
  },
  { deep: true },
)

const segStyle = (idx: number) => {
  const visible = revealed.value
  return {
    strokeDasharray: visible ? '100 0' : '0 100',
    transition: `stroke-dasharray 800ms cubic-bezier(0.22, 1, 0.36, 1) ${idx * 100}ms`,
  }
}

/* ---------- Legend ---------- */
interface LegendItem {
  metric: MetricKey
  label: string
  count: number
  pctText: string
  color: string
}

const legendList = computed<LegendItem[]>(() => {
  const total = sum.value
  return METRIC_ORDER.map((metric) => {
    const count = sourceGroup.value[metric] || 0
    const pct = total > 0 ? (count / total) * 100 : 0
    const pctText = total <= 0 ? '<1%' : pct < 1 ? '<1%' : `${pct.toFixed(1)}%`
    return {
      metric,
      label: metricLabel[metric],
      count,
      pctText,
      color: appStore.isDark
        ? IMPACT_PALETTE[metric].dark
        : IMPACT_PALETTE[metric].light,
    }
  })
})
</script>

<template>
  <div
    class="flex h-full flex-col gap-3 px-2 py-1 md:flex-row md:items-center md:gap-4"
  >
    <!-- Legend（桌面：左栏；移动：下） -->
    <div class="order-2 flex w-full flex-col gap-y-1 md:order-1 md:flex-1">
      <div
        v-for="item in legendList"
        :key="item.metric"
        class="grid items-center gap-x-2 text-[12px]"
        :style="{ gridTemplateColumns: '64px 1fr 56px', height: '24px' }"
      >
        <div class="flex items-center gap-1.5 text-zinc-700 dark:text-zinc-300">
          <span
            class="inline-block size-2 rounded-full"
            :style="{ backgroundColor: item.color }"
            aria-hidden="true"
          />
          <span class="truncate">{{ item.label }}</span>
        </div>
        <div
          class="text-right font-mono text-[12px] tabular-nums text-zinc-800 dark:text-zinc-200"
        >
          {{ item.count.toLocaleString('zh-CN') }}
        </div>
        <div class="text-right font-mono text-[11px] tabular-nums text-zinc-500 dark:text-zinc-400">
          {{ item.pctText }}
        </div>
      </div>
    </div>

    <!-- 270° gauge SVG（桌面：右栏；移动：上） -->
    <div
      class="relative order-1 mx-auto w-full max-w-[180px] md:order-2 md:mx-0 md:w-[160px] md:flex-shrink-0"
    >
      <svg
        :viewBox="`0 0 ${VIEW_W} ${VIEW_H}`"
        class="block w-full"
        role="img"
        aria-label="上传网站累计互动占比"
      >
        <!-- 背景灰弧 -->
        <path
          :d="backgroundArcD"
          :stroke="trackColor"
          fill="none"
          stroke-width="8"
          stroke-linecap="round"
        />
        <!-- 5 段彩色弧（按 share 叠加） -->
        <path
          v-for="(seg, idx) in segments"
          :key="seg.metric"
          :d="seg.d"
          :stroke="seg.color"
          fill="none"
          stroke-width="8"
          stroke-linecap="round"
          pathLength="100"
          :style="segStyle(idx)"
        />
      </svg>
      <!-- 中心文字（绝对定位覆盖 SVG） -->
      <div
        class="pointer-events-none absolute inset-0 flex flex-col items-center justify-center pb-3"
      >
        <div
          class="font-mono text-[18px] font-semibold leading-none tabular-nums text-zinc-900 dark:text-zinc-100"
        >
          {{ centerNumber }}
        </div>
        <div class="mt-1 text-[11px] text-zinc-500 dark:text-zinc-400">
          {{ centerLabel }}
        </div>
      </div>
    </div>
  </div>
</template>
