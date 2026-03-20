<script setup lang="ts">
import type { ProfileData } from '@/types/profile'

defineProps<{
  profile: ProfileData
}>()
</script>

<template>
  <UCard class="!ring-0 shadow-sm dark:shadow-md bg-white dark:!bg-[#131418] rounded-xl">
    <div class="h-48" />
  </UCard>
</template>
