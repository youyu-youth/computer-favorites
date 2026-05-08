<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * 贡献概览：A++ 评级主卡 + 多个紧凑 stat 卡 + 文本贡献说明
 */
import { BarChart3 } from 'lucide-vue-next'
import { computed } from 'vue'
import ContributionRankCard from './dashboard/ContributionRankCard.vue'
import StatCard from './dashboard/StatCard.vue'
import { buildRankCardStats } from './dashboard/mock'
import { githubStatsPalette } from './dashboard/theme'
import type { ProfileContribution, ProfileStat } from '@/types/profile'

const props = defineProps<{
  stats: ProfileStat[]
  contributions: ProfileContribution[]
}>()

const parseNumber = (raw: string): number => {
  const match = raw.match(/-?\d+(\.\d+)?/)
  return match ? Number(match[0]) : 0
}

const completionScore = computed(() => {
  const completion = props.stats.find((s) => s.label.includes('完整度'))
  if (!completion) return 70
  return Math.max(0, Math.min(100, parseNumber(completion.value)))
})

const rankScore = computed(() => {
  const contribution = props.stats.find((s) => s.label.includes('贡献'))
  const v = contribution ? parseNumber(contribution.value) : 0
  return Math.max(60, Math.min(100, 60 + (v % 40)))
})

const rankCardStats = computed(() =>
  buildRankCardStats(parseNumber(props.stats.find((s) => s.label.includes('贡献'))?.value ?? '')),
)

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
        /contributions
      </span>
    </header>

    <div class="grid grid-cols-1 gap-3 lg:grid-cols-2">
      <ContributionRankCard
        title="GitHub Stats Rank"
        :score="rankScore"
        :stats="rankCardStats"
      />

      <div class="grid grid-cols-2 gap-3">
        <StatCard
          v-for="(stat, idx) in stats"
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
              Profile Health
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
