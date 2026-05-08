<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * 上传内容数据看板：GitHub Stats 风格紧凑卡组（保留 donut + bar 主图 + 多个 sparkline 小卡）
 */
import { Eye, Heart, Bookmark, MessageCircle, Activity, Sparkles } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import RingGaugeCard from './dashboard/RingGaugeCard.vue'
import SparklineCard from './dashboard/SparklineCard.vue'
import CategoryDonutCard from './dashboard/CategoryDonutCard.vue'
import ContentTypeBarCard from './dashboard/ContentTypeBarCard.vue'
import { buildDashboardItems, toSparkline } from './dashboard/mock'
import { githubStatsPalette } from './dashboard/theme'
import type { RangeKey } from './dashboard/types'

const numberFormatter = new Intl.NumberFormat('zh-CN')
const compactFormatter = new Intl.NumberFormat('en-US', {
  notation: 'compact',
  maximumFractionDigits: 1,
})

const rangeOptions: Array<{ label: string; value: RangeKey }> = [
  { label: '近7天', value: '7d' },
  { label: '近30天', value: '30d' },
  { label: '近90天', value: '90d' },
  { label: '全部', value: 'all' },
]
const selectedRange = ref<RangeKey>('30d')
const selectedCategory = ref<string | null>(null)

const allItems = buildDashboardItems()
const today = new Date()

const daysMap: Record<Exclude<RangeKey, 'all'>, number> = {
  '7d': 7,
  '30d': 30,
  '90d': 90,
}

const filteredItems = computed(() => {
  if (selectedRange.value === 'all') {
    return allItems
  }
  const days = daysMap[selectedRange.value]
  const cutoff = new Date(today)
  cutoff.setDate(today.getDate() - (days - 1))
  const cutoffDay = cutoff.toISOString().slice(0, 10)
  return allItems.filter((item) => item.date >= cutoffDay)
})

const overview = computed(() => {
  const totalPv = filteredItems.value.reduce((sum, item) => sum + item.pv, 0)
  const totalLikes = filteredItems.value.reduce((sum, item) => sum + item.likes, 0)
  const totalFavorites = filteredItems.value.reduce((sum, item) => sum + item.favorites, 0)
  const totalComments = filteredItems.value.reduce(
    (sum, item) => sum + Math.round(item.likes * 0.42),
    0,
  )
  return { totalPv, totalLikes, totalFavorites, totalComments }
})

const computeDelta = (data: Array<{ value: number }>): number => {
  if (data.length < 4) return 0
  const half = Math.floor(data.length / 2)
  const recent = data.slice(half).reduce((s, d) => s + d.value, 0)
  const prev = data.slice(0, half).reduce((s, d) => s + d.value, 0)
  if (prev === 0) return 0
  return ((recent - prev) / prev) * 100
}

const pvSeries = computed(() => toSparkline(filteredItems.value, 'pv'))
const likesSeries = computed(() => toSparkline(filteredItems.value, 'likes'))
const favoritesSeries = computed(() => toSparkline(filteredItems.value, 'favorites'))
const commentSeries = computed(() =>
  pvSeries.value.map((point) => ({ date: point.date, value: Math.round(point.value * 0.085) })),
)

const pvDelta = computed(() => computeDelta(pvSeries.value))
const likesDelta = computed(() => computeDelta(likesSeries.value))
const favoritesDelta = computed(() => computeDelta(favoritesSeries.value))
const commentDelta = computed(() => computeDelta(commentSeries.value))

const ringScore = computed(() => {
  const days = filteredItems.value.length || 1
  const avgPv = overview.value.totalPv / days
  const score = Math.min(100, Math.round((avgPv / 4500) * 100))
  return Math.max(46, score)
})

const ringStats = computed(() => [
  { label: 'Total PV', value: numberFormatter.format(overview.value.totalPv), color: githubStatsPalette.primary },
  { label: 'Total Likes', value: numberFormatter.format(overview.value.totalLikes), color: githubStatsPalette.rose },
  { label: 'Total Favs', value: numberFormatter.format(overview.value.totalFavorites), color: githubStatsPalette.cyan },
  { label: 'Avg/Day PV', value: numberFormatter.format(Math.round(overview.value.totalPv / Math.max(1, filteredItems.value.length))), color: githubStatsPalette.emerald },
])

const categoryPvList = computed(() => {
  const map = new Map<string, number>()
  filteredItems.value.forEach((item) => {
    map.set(item.category, (map.get(item.category) || 0) + item.pv)
  })
  return [...map.entries()]
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
})

const typeCountMerged = computed(() => {
  const map = new Map<string, number>()
  filteredItems.value.forEach((item) => {
    map.set(item.contentType, (map.get(item.contentType) || 0) + 1)
  })
  const sorted = [...map.entries()]
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
  const topN = 6
  if (sorted.length <= topN) {
    return sorted
  }
  const top = sorted.slice(0, topN)
  const other = sorted.slice(topN).reduce((sum, item) => sum + item.value, 0)
  return [...top, { name: '其他', value: other }]
})

const handleCategorySelect = (name: string | null) => {
  selectedCategory.value = name
}

const formatCompact = (n: number) => compactFormatter.format(n)
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
          /dashboard
        </span>
      </div>
      <div class="flex items-center gap-1 rounded-md border border-black/5 bg-white p-0.5 dark:border-white/5 dark:bg-black">
        <button
          v-for="item in rangeOptions"
          :key="item.value"
          type="button"
          class="cursor-pointer rounded-sm px-2.5 py-1 font-mono text-[11px] transition-colors"
          :class="
            selectedRange === item.value
              ? 'bg-amber-500/15 text-amber-600 dark:bg-amber-500/20 dark:text-amber-400'
              : 'text-gray-600 hover:bg-black/5 dark:text-gray-400 dark:hover:bg-white/5'
          "
          @click="selectedRange = item.value"
        >
          {{ item.label }}
        </button>
      </div>
    </header>

    <div class="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
      <RingGaugeCard
        class="md:col-span-2 xl:col-span-2"
        title="GitHub-like Stats Overview"
        :score="ringScore"
        :stats="ringStats"
      />
      <SparklineCard
        :icon="Eye"
        label="Total PV"
        :value="formatCompact(overview.totalPv)"
        :delta="pvDelta"
        :data="pvSeries"
        :color="githubStatsPalette.primary"
      />
      <SparklineCard
        :icon="Heart"
        label="Total Likes"
        :value="formatCompact(overview.totalLikes)"
        :delta="likesDelta"
        :data="likesSeries"
        :color="githubStatsPalette.rose"
      />
      <SparklineCard
        :icon="Bookmark"
        label="Total Favorites"
        :value="formatCompact(overview.totalFavorites)"
        :delta="favoritesDelta"
        :data="favoritesSeries"
        :color="githubStatsPalette.cyan"
      />
      <SparklineCard
        :icon="MessageCircle"
        label="Total Comments"
        :value="formatCompact(overview.totalComments)"
        :delta="commentDelta"
        :data="commentSeries"
        :color="githubStatsPalette.emerald"
      />
      <SparklineCard
        :icon="Activity"
        label="Avg PV/Day"
        :value="formatCompact(Math.round(overview.totalPv / Math.max(1, filteredItems.length)))"
        :data="pvSeries"
        :color="githubStatsPalette.blue"
      />
      <SparklineCard
        :icon="Sparkles"
        label="Engagement %"
        :value="`${(((overview.totalLikes + overview.totalFavorites) / Math.max(1, overview.totalPv)) * 100).toFixed(1)}%`"
        :data="likesSeries"
        :color="githubStatsPalette.amber"
      />
    </div>

    <div class="grid grid-cols-1 gap-3 lg:grid-cols-2">
      <CategoryDonutCard
        title="各分类访问量占比"
        :data="categoryPvList"
        :selected="selectedCategory"
        @select="handleCategorySelect"
      />
      <ContentTypeBarCard
        title="内容类型占比（前6 + 其他）"
        :data="typeCountMerged"
      />
    </div>
  </section>
</template>
