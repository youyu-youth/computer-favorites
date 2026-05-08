<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * 技术栈区块：Top Languages 堆叠条 + 雷达图
 */
import { Cpu } from 'lucide-vue-next'
import StatCard from './dashboard/StatCard.vue'
import { githubStatsPalette } from './dashboard/theme'
import type { ProfileSkill } from '@/types/profile'

defineProps<{
  skills: ProfileSkill[]
}>()

const levelColorMap: Record<ProfileSkill['level'], string> = {
  精通: githubStatsPalette.emerald,
  熟练: githubStatsPalette.primary,
  基础: githubStatsPalette.slate,
}
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
  </section>
</template>
