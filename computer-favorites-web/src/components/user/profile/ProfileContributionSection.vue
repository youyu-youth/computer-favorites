<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * 贡献概览：A++ 评级主卡 + 多个紧凑 stat 卡 + 文本贡献说明
 */
import { BarChart3 } from 'lucide-vue-next'
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import ContributionRankCard from './dashboard/ContributionRankCard.vue'
import StatCard from './dashboard/StatCard.vue'
import { githubStatsPalette } from './dashboard/theme'
import { useProfileDashboardStore } from '@/stores/profileDashboard'
import type { ProfileContribution, ProfileStat } from '@/types/profile'
import type { RankCardStat } from './dashboard/types'

const props = defineProps<{
  stats: ProfileStat[]
  contributions: ProfileContribution[]
}>()

const store = useProfileDashboardStore()
const { overview } = storeToRefs(store)
const numberFormatter = new Intl.NumberFormat('zh-CN')

const parseNumber = (raw: string): number => {
  const match = raw.match(/-?\d+(\.\d+)?/)
  return match ? Number(match[0]) : 0
}

const toNum = (value: number | string | null | undefined) => {
  if (value === null || value === undefined) return 0
  const n = Number(value)
  return Number.isFinite(n) ? n : 0
}

const completionScore = computed(() => {
  const completion = props.stats.find((s) => s.label.includes('完整度'))
  if (!completion) return 70
  return Math.max(0, Math.min(100, parseNumber(completion.value)))
})

const rankScore = computed(() => {
  const raw = overview.value.data?.rankPercent
  const score = toNum(raw)
  return Math.max(0, Math.min(100, Math.round(score)))
})

const rankCardStats = computed<RankCardStat[]>(() => {
  const data = overview.value.data
  return [
    { label: '累计投稿', value: numberFormatter.format(toNum(data?.totalSubmit)), icon: 'star' },
    { label: '累计评论', value: numberFormatter.format(toNum(data?.totalComment)), icon: 'commit' },
    { label: '累计收藏', value: numberFormatter.format(toNum(data?.totalCollect)), icon: 'pr' },
    { label: '累计点赞', value: numberFormatter.format(toNum(data?.totalLike)), icon: 'issue' },
    { label: '累计浏览', value: numberFormatter.format(toNum(data?.totalBrowse)), icon: 'monitor' },
    { label: '连续活跃', value: `${numberFormatter.format(toNum(data?.streakDays))} 天`, icon: 'flame' },
  ]
})

const overviewStats = computed<ProfileStat[]>(() => {
  const data = overview.value.data
  if (!data) {
    return props.stats
  }
  return [
    { label: '累计投稿', value: numberFormatter.format(toNum(data.totalSubmit)) },
    { label: '累计评论', value: numberFormatter.format(toNum(data.totalComment)) },
    { label: '累计收藏', value: numberFormatter.format(toNum(data.totalCollect)) },
    { label: '累计点赞', value: numberFormatter.format(toNum(data.totalLike)) },
    { label: '累计评分', value: numberFormatter.format(toNum(data.totalScore)) },
    { label: '累计浏览', value: numberFormatter.format(toNum(data.totalBrowse)) },
  ]
})

const accentList = [
  githubStatsPalette.primary,
  githubStatsPalette.cyan,
  githubStatsPalette.emerald,
  githubStatsPalette.rose,
]

const getAccent = (idx: number) => accentList[idx % accentList.length] ?? githubStatsPalette.primary
</script>

<template>
  <section class="space-y-3">
    <header class="flex items-center gap-2">
      <BarChart3 class="size-4 text-amber-500" :stroke-width="2" />
      <h3 class="text-sm font-semibold tracking-tight text-gray-900 sm:text-base dark:text-gray-100">
        贡献概览
      </h3>
      <span class="font-mono text-[11px] uppercase tracking-widest text-gray-500 dark:text-gray-400">
        贡献概览
      </span>
    </header>

    <div class="grid grid-cols-1 gap-3 lg:grid-cols-2">
      <ContributionRankCard
        title="贡献等级"
        :score="rankScore"
        :stats="rankCardStats"
      />

      <div class="grid grid-cols-2 gap-3">
        <StatCard
          v-for="(stat, idx) in overviewStats"
          :key="stat.label"
          dense
          :accent="getAccent(idx)"
        >
          <div class="flex flex-col gap-1">
            <span class="font-mono text-[11px] uppercase tracking-wider text-gray-500 dark:text-gray-400">
              {{ stat.label }}
            </span>
            <span
              class="font-mono text-2xl font-semibold leading-none"
              :style="{ color: getAccent(idx) }"
            >
              {{ stat.value }}
            </span>
          </div>
        </StatCard>
        <StatCard dense :accent="githubStatsPalette.emerald">
          <div class="flex flex-col gap-1">
            <span class="font-mono text-[11px] uppercase tracking-wider text-gray-500 dark:text-gray-400">
              资料完整度
            </span>
            <div class="flex items-end gap-1.5">
              <span
                class="font-mono text-2xl font-semibold leading-none"
                :style="{ color: githubStatsPalette.emerald }"
              >
                {{ completionScore }}
              </span>
              <span class="font-mono text-[11px] text-gray-500 dark:text-gray-400">/100</span>
            </div>
            <div class="mt-1 h-1.5 overflow-hidden rounded-full bg-black/5 dark:bg-white/5">
              <div
                class="h-full rounded-full transition-[width]"
                :style="{
                  width: `${completionScore}%`,
                  backgroundColor: githubStatsPalette.emerald,
                }"
              />
            </div>
          </div>
        </StatCard>
      </div>
    </div>

    <StatCard title="贡献明细" dense>
      <ul class="space-y-2">
        <li
          v-for="item in contributions"
          :key="item.title"
          class="flex flex-col gap-1 rounded-sm border border-dashed border-black/10 p-2 dark:border-white/10"
        >
          <h4 class="text-[12.5px] font-semibold text-gray-900 dark:text-gray-100">
            {{ item.title }}
          </h4>
          <p class="text-[12.5px] leading-6 text-gray-600 dark:text-gray-400">
            {{ item.summary }}
          </p>
        </li>
      </ul>
    </StatCard>
  </section>
</template>
