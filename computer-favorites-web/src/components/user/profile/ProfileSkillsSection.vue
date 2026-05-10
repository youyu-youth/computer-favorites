<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * 技术栈区块：Top Languages 堆叠条 + 雷达图
 * 2026-05-08: 去 mock 对接后端 tech-radar API（user-15 M3）
 */
import { Cpu } from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import StatCard from './dashboard/StatCard.vue'
import SkillRadarCard from './dashboard/SkillRadarCard.vue'
import TopLanguagesCard from './dashboard/TopLanguagesCard.vue'
import { githubStatsPalette, chartCategorical } from './dashboard/theme'
import { listEnabledTechStack } from '@/api/user-tech-stack'
import { useProfileDashboardStore } from '@/stores/profileDashboard'
import type { ProfileSkill } from '@/types/profile'
import type { UserTechStackOption } from '@/types/user-tech-stack'
import type { LangShare, RadarSkill } from './dashboard/types'

const props = defineProps<{
  skills: ProfileSkill[]
}>()

const store = useProfileDashboardStore()
const { radar } = storeToRefs(store)
const techStackOptions = ref<UserTechStackOption[]>([])
const failedIconKeys = ref<Set<string>>(new Set())

const optionByName = computed(() => {
  const map = new Map<string, UserTechStackOption>()
  for (const option of techStackOptions.value) {
    map.set(option.name.trim().toLowerCase(), option)
  }
  return map
})

const safeColor = (value: string | undefined) => {
  const color = value?.trim()
  return color && /^#[0-9a-fA-F]{6}$/.test(color) ? color : githubStatsPalette.primary
}

const displaySkills = computed(() =>
  props.skills.map((skill) => {
    const option = optionByName.value.get(skill.name.trim().toLowerCase())
    const iconPng = skill.iconPng || option?.iconPng || ''
    const officialUrl = skill.officialUrl || option?.officialUrl || ''
    const color = safeColor(skill.color || option?.color)
    return {
      ...skill,
      iconPng,
      officialUrl,
      color,
      description: skill.description || option?.description || '',
    }
  }),
)

const initialLetter = (name: string) => name.trim().charAt(0).toUpperCase() || '#'

const iconKey = (skill: ProfileSkill) => `${skill.name}:${skill.iconPng ?? ''}`

const shouldShowIcon = (skill: ProfileSkill) =>
  Boolean(skill.iconPng) && !failedIconKeys.value.has(iconKey(skill))

const onIconError = (skill: ProfileSkill) => {
  const next = new Set(failedIconKeys.value)
  next.add(iconKey(skill))
  failedIconKeys.value = next
}

const loadTechStackOptions = async () => {
  techStackOptions.value = await listEnabledTechStack()
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
  void loadTechStackOptions()
})
</script>

<template>
  <section class="space-y-3">
    <header class="flex items-center gap-2">
      <Cpu class="size-4 text-amber-500" :stroke-width="2" />
      <h3
        class="text-sm font-semibold tracking-tight text-gray-900 sm:text-base dark:text-gray-100"
      >
        技术栈
      </h3>
      <span
        class="font-mono text-[11px] uppercase tracking-widest text-gray-500 dark:text-gray-400"
      >
        /tech-stack
      </span>
    </header>

    <StatCard title="技术栈图谱" dense>
      <ul v-if="displaySkills.length" class="flex flex-wrap gap-3 py-1">
        <li
          v-for="(skill, index) in displaySkills"
          :key="`${skill.name}-${index}`"
          class="relative"
        >
          <component
            :is="skill.officialUrl ? 'a' : 'div'"
            :href="skill.officialUrl || undefined"
            :target="skill.officialUrl ? '_blank' : undefined"
            :rel="skill.officialUrl ? 'noopener noreferrer' : undefined"
            :title="skill.name"
            :aria-label="skill.officialUrl ? `打开 ${skill.name} 官网` : skill.name"
            class="group/skill relative flex size-[76px] items-center justify-center rounded-2xl border bg-white/80 transition duration-200 dark:bg-white/[0.03]"
            :class="
              skill.officialUrl
                ? 'cursor-pointer hover:-translate-y-1 hover:shadow-lg focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400/70'
                : 'cursor-default'
            "
            :style="{
              borderColor: `${skill.color}55`,
              backgroundColor: `${skill.color}12`,
            }"
          >
            <span
              class="absolute -top-8 left-1/2 z-20 max-w-[9rem] -translate-x-1/2 whitespace-nowrap rounded-md border border-black/10 bg-white px-2 py-1 text-xs font-medium text-gray-900 opacity-0 shadow-sm transition-opacity duration-150 group-hover/skill:opacity-100 group-focus-visible/skill:opacity-100 dark:border-white/10 dark:bg-[#111113] dark:text-gray-100"
            >
              {{ skill.name }}
            </span>
            <span
              class="relative flex size-16 items-center justify-center rounded-xl border border-white/50 bg-white/80 font-mono text-2xl font-semibold tracking-tight text-gray-800 shadow-sm transition-transform duration-200 group-hover/skill:scale-105 dark:border-white/10 dark:bg-black/40 dark:text-gray-100"
            >
              <span class="absolute inset-0 flex items-center justify-center">
                {{ initialLetter(skill.name) }}
              </span>
              <img
                v-if="shouldShowIcon(skill)"
                :src="skill.iconPng"
                :alt="skill.name"
                class="relative z-10 size-12 object-contain transition-transform duration-200 group-hover/skill:scale-110"
                loading="lazy"
                decoding="async"
                @error="onIconError(skill)"
              />
            </span>
          </component>
        </li>
      </ul>
      <p v-else class="font-mono text-[12px] text-gray-500 dark:text-gray-400">暂无技术栈数据</p>
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

    <!-- <div v-else class="grid grid-cols-1 gap-3 lg:grid-cols-2">
      <SkillRadarCard title="六维技能雷达" :skills="radarSkills" />
      <TopLanguagesCard title="Top Languages" :langs="topLanguages" />
    </div> -->
  </section>
</template>
