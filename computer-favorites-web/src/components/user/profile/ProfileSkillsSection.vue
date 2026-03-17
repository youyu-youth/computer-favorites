<script setup lang="ts">
import type { ProfileSkill } from '@/types/profile'

defineProps<{
  skills: ProfileSkill[]
}>()

const levelColorMap: Record<ProfileSkill['level'], 'success' | 'warning' | 'neutral'> = {
  精通: 'success',
  熟练: 'warning',
  基础: 'neutral',
}
</script>

<template>
  <section class="space-y-4">
    <div class="flex items-center gap-2">
      <UIcon name="i-lucide-cpu" class="size-5 text-primary-500" />
      <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">技术栈</h3>
    </div>
    <UCard class="!ring-0 shadow-sm dark:shadow-md bg-white dark:bg-gray-900 rounded-xl">
      <div class="flex flex-wrap gap-2">
        <UBadge
          v-for="skill in skills"
          :key="skill.name"
          :color="levelColorMap[skill.level]"
          variant="soft"
        >
          {{ skill.name }} · {{ skill.level }}
        </UBadge>
      </div>
    </UCard>
  </section>
</template>
