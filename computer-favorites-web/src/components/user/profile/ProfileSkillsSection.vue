<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * 技术栈区块：Top Languages 堆叠条 + 雷达图
 * 2026-05-08: 去 mock 对接后端 tech-radar API（user-15 M3）
 */
import { Cpu } from 'lucide-vue-next'
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import StatCard from './dashboard/StatCard.vue'
import SkillRadarCard from './dashboard/SkillRadarCard.vue'
import TopLanguagesCard from './dashboard/TopLanguagesCard.vue'
import { githubStatsPalette, chartCategorical } from './dashboard/theme'
import { useProfileDashboardStore } from '@/stores/profileDashboard'
import type { ProfileSkill } from '@/types/profile'
import type { LangShare, RadarSkill } from './dashboard/types'

defineProps<{
  skills: ProfileSkill[]
}>()

const store = useProfileDashboardStore()
const { radar } = storeToRefs(store)

const levelColorMap: Record<ProfileSkill['level'], string> = {
  精通: githubStatsPalette.emerald,
  熟练: githubStatsPalette.primary,
  基础: githubStatsPalette.slate,
}

/** 6 维雷达数据（后端未返回时给空数组） */
const radarSkills = computed<RadarSkill[]>(() => {
  const dims = radar.value.data?.dimensions ?? []
  return dims.map((d) => ({ name: d.name, value: d.value }))
})

/** Top Languages 堆叠数据（后端返回 pct） */
const topLanguages = computed<LangShare[]>(() => {
  const langs = radar.value.data?.languages ?? []
  return langs.map((l, idx) => ({
    name: l.name,
    pct: Number(l.pct) || 0,
    color: chartCategorical[idx % chartCategorical.length] ?? githubStatsPalette.primary,
  }))
})

const isLoading = computed(() => radar.value.state === 'loading')
const errorText = computed(() => radar.value.error)

onMounted(() => {
  if (radar.value.state === 'idle') store.loadRadar()
})
</script>

<template>
  <section class="space-y-3">
    <header class="flex items-center gap-2">
      <Cpu class="size-4 text-amber-500" :stroke-width="2" />
      <h3 class="text-sm font-semibold tracking-tight text-gray-900 sm:text-base dark:text-gray-100">
        技术栈
      </h3>
      <span class="font-mono text-[11px] uppercase tracking-widest text-gray-500 dark:text-gray-400">
        /tech-stack
      </span>
    </header>

    <StatCard title="技能标签" dense>
      <ul class="flex flex-wrap gap-1.5">
        <li
          v-for="skill in skills"
          :key="skill.name"
          class="flex items-center gap-1.5 rounded-sm border border-black/5 px-2 py-1 font-mono text-[11.5px] dark:border-white/[0.06]"
        >
          <span
            class="inline-block size-1.5 rounded-full"
            :style="{ backgroundColor: levelColorMap[skill.level] }"
          />
          <span class="text-gray-800 dark:text-gray-200">{{ skill.name }}</span>
          <span class="text-gray-500 dark:text-gray-400">·</span>
          <span class="text-gray-500 dark:text-gray-400">{{ skill.level }}</span>
        </li>
        <li
          v-if="!skills.length"
          class="font-mono text-[12px] text-gray-500 dark:text-gray-400"
        >
          暂无技术栈数据
        </li>
      </ul>
    </StatCard>

    <div v-if="isLoading" class="grid grid-cols-1 gap-3 lg:grid-cols-2">
      <div class="h-56 animate-pulse rounded-md bg-black/5 dark:bg-white/5" />
      <div class="h-56 animate-pulse rounded-md bg-black/5 dark:bg-white/5" />
    </div>

    <div
      v-else-if="errorText"
      class="rounded-md border border-red-200 bg-red-50 p-3 font-mono text-[12px] text-red-600 dark:border-red-400/30 dark:bg-red-400/10 dark:text-red-300"
    >
      {{ errorText }}
    </div>

    <div v-else class="grid grid-cols-1 gap-3 lg:grid-cols-2">
      <SkillRadarCard title="六维技能雷达" :skills="radarSkills" />
      <TopLanguagesCard title="Top Languages" :langs="topLanguages" />
    </div>
  </section>
</template>
