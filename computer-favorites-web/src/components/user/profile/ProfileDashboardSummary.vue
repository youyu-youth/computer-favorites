<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-09
 * 上传内容数据看板 - 指标 summary 区块
 * 拆分自 ProfileHeroSection.vue：标题 + 时间范围切换 + ring/gauge + 6 个 sparkline 指标卡
 * 数据来源：useProfileDashboardStore（overview / trend / trendRange）
 */
import { Eye, Heart, Bookmark, MessageCircle, Activity, Sparkles } from 'lucide-vue-next'
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import RingGaugeCard from './dashboard/RingGaugeCard.vue'
import SparklineCard from './dashboard/SparklineCard.vue'
import { githubStatsPalette } from './dashboard/theme'
import { useProfileDashboardStore } from '@/stores/profileDashboard'
import type { TrendRange } from '@/api/user-profile-dashboard'
import type { SparklinePoint } from './dashboard/types'

const store = useProfileDashboardStore()
const { overview, trend, trendRange } = storeToRefs(store)

const numberFormatter = new Intl.NumberFormat('zh-CN')
const compactFormatter = new Intl.NumberFormat('zh-CN', {
  notation: 'compact',
  maximumFractionDigits: 1,
})

const rangeOptions: Array<{ label: string; value: TrendRange }> = [
  { label: '近7天', value: '7d' },
  { label: '近30天', value: '30d' },
  { label: '近90天', value: '90d' },
]

/** 把后端 TrendPoint(date,value) 映射为 SparklinePoint */
const trendSeries = computed<SparklinePoint[]>(() => {
  const points = trend.value.data?.points ?? []
  return points.map((p) => ({ date: p.date, value: Number(p.value) || 0 }))
})

/** 当前环比 delta（0 当不足 2 点） */
const trendDelta = computed(() => {
  const raw = trend.value.data?.delta
  if (raw === undefined || raw === null) return 0
  const num = Number(raw)
  return Number.isFinite(num) ? num : 0
})

const overviewData = computed(() => overview.value.data)

const toNum = (v: number | string | null | undefined) => {
  if (v === null || v === undefined) return 0
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

/** 平均 PV/天（基于 overview.totalBrowse + trend 时间范围的天数） */
const daysInRange = computed(() => {
  const r = trendRange.value
  return r === '7d' ? 7 : r === '30d' ? 30 : 90
})

const avgPvPerDay = computed(() => {
  const base = trendSeries.value
  if (base.length === 0) return 0
  const sum = base.reduce((s, p) => s + p.value, 0)
  return Math.round(sum / Math.max(1, base.length))
})

/** 环形中心得分：基于当前等级映射（S=100 / A=80 / B=60 / C=40） */
const ringScore = computed(() => {
  const code = overviewData.value?.levelCode ?? 'C'
  const table: Record<string, number> = { S: 100, A: 80, B: 60, C: 40 }
  return table[code] ?? 40
})

const ringStats = computed(() => [
  {
    label: '累计浏览',
    value: numberFormatter.format(toNum(overviewData.value?.totalBrowse)),
    color: githubStatsPalette.primary,
  },
  {
    label: '累计点赞',
    value: numberFormatter.format(toNum(overviewData.value?.totalLike)),
    color: githubStatsPalette.rose,
  },
  {
    label: '累计收藏',
    value: numberFormatter.format(toNum(overviewData.value?.totalCollect)),
    color: githubStatsPalette.cyan,
  },
  {
    label: '贡献值',
    value: numberFormatter.format(toNum(overviewData.value?.totalContribution)),
    color: githubStatsPalette.emerald,
  },
])

/** Engagement = (likes + favorites) / browse * 100 */
const engagementPct = computed(() => {
  const pv = toNum(overviewData.value?.totalBrowse)
  const likes = toNum(overviewData.value?.totalLike)
  const favs = toNum(overviewData.value?.totalCollect)
  if (pv === 0) return '0.0'
  return (((likes + favs) / pv) * 100).toFixed(1)
})

const handleRangeChange = (r: TrendRange) => {
  store.setTrend(r, 'pv')
}

const formatCompact = (n: number) => compactFormatter.format(n)

const isTrendLoading = computed(() => trend.value.state === 'loading')

onMounted(() => {
  if (overview.value.state === 'idle') store.loadOverview()
  if (trend.value.state === 'idle') store.loadTrend()
})
</script>

<template>
  <section class="space-y-3">
    <header class="flex flex-wrap items-center justify-between gap-3">
      <div class="flex items-center gap-2">
        <Activity class="size-4 text-amber-500" :stroke-width="2" />
        <h3 class="text-sm font-semibold tracking-tight text-gray-900 sm:text-base dark:text-gray-100">
          上传内容数据看板
        </h3>
        <span class="font-mono text-[11px] uppercase tracking-widest text-gray-500 dark:text-gray-400">
          数据看板
        </span>
      </div>
      <div class="flex items-center gap-1 rounded-md border border-black/5 bg-white p-0.5 dark:border-white/5 dark:bg-black">
        <button
          v-for="item in rangeOptions"
          :key="item.value"
          type="button"
          class="cursor-pointer rounded-sm px-2.5 py-1 font-mono text-[11px] transition-colors"
          :class="
            trendRange === item.value
              ? 'bg-amber-500/15 text-amber-600 dark:bg-amber-500/20 dark:text-amber-400'
              : 'text-gray-600 hover:bg-black/5 dark:text-gray-400 dark:hover:bg-white/5'
          "
          @click="handleRangeChange(item.value)"
        >
          {{ item.label }}
        </button>
      </div>
    </header>

    <div
      v-if="overview.state === 'loading' && !overviewData"
      class="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4"
    >
      <div
        v-for="i in 6"
        :key="i"
        class="h-28 w-full animate-pulse rounded-md bg-black/5 dark:bg-white/5"
      />
    </div>

    <div
      v-else-if="overview.state === 'error'"
      class="rounded-md border border-red-200 bg-red-50 p-3 font-mono text-[12px] text-red-600 dark:border-red-400/30 dark:bg-red-400/10 dark:text-red-300"
    >
      {{ overview.error || '概览数据加载失败' }}
    </div>

    <div v-else class="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
      <RingGaugeCard
        class="md:col-span-2 xl:col-span-2"
        title="贡献概览"
        :score="ringScore"
        :stats="ringStats"
      />
      <SparklineCard
        :icon="Eye"
        label="累计浏览"
        :value="formatCompact(toNum(overviewData?.totalBrowse))"
        :delta="trendDelta"
        :data="trendSeries"
        :color="githubStatsPalette.primary"
      />
      <SparklineCard
        :icon="Heart"
        label="累计点赞"
        :value="formatCompact(toNum(overviewData?.totalLike))"
        :data="trendSeries"
        :color="githubStatsPalette.rose"
      />
      <SparklineCard
        :icon="Bookmark"
        label="累计收藏"
        :value="formatCompact(toNum(overviewData?.totalCollect))"
        :data="trendSeries"
        :color="githubStatsPalette.cyan"
      />
      <SparklineCard
        :icon="MessageCircle"
        label="累计评论"
        :value="formatCompact(toNum(overviewData?.totalComment))"
        :data="trendSeries"
        :color="githubStatsPalette.emerald"
      />
      <SparklineCard
        :icon="Activity"
        :label="`日均浏览（近${daysInRange}天）`"
        :value="formatCompact(avgPvPerDay)"
        :data="trendSeries"
        :color="githubStatsPalette.blue"
      />
      <SparklineCard
        :icon="Sparkles"
        label="互动率"
        :value="`${engagementPct}%`"
        :data="trendSeries"
        :color="githubStatsPalette.amber"
      />
    </div>

    <div
      v-if="isTrendLoading && trendSeries.length === 0"
      class="h-2 w-full animate-pulse rounded-full bg-black/5 dark:bg-white/5"
    />
  </section>
</template>
