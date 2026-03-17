<script setup lang="ts">
import ProfileContributionSection from '@/components/user/profile/ProfileContributionSection.vue'
import ProfileHeatmapSection from '@/components/user/profile/ProfileHeatmapSection.vue'
import ProfileHeroSection from '@/components/user/profile/ProfileHeroSection.vue'
import ProfileProjectGridSection from '@/components/user/profile/ProfileProjectGridSection.vue'
import ProfileSidebarCard from '@/components/user/profile/ProfileSidebarCard.vue'
import ProfileSiteGridSection from '@/components/user/profile/ProfileSiteGridSection.vue'
import ProfileSkillsSection from '@/components/user/profile/ProfileSkillsSection.vue'
import { getHeatValue, heatmapCols, heatmapRows, profileMockData } from './mock/profile.mock'

defineOptions({
  name: 'ProfileView',
})
</script>

<template>
  <UContainer class="py-8 md:py-10">
    <div class="flex flex-col lg:flex-row gap-6 lg:gap-8">
      <ProfileSidebarCard :profile="profileMockData" />

      <main class="flex-1 min-w-0 space-y-6">
        <ProfileHeroSection :profile="profileMockData" />
        <ProfileHeatmapSection :rows="heatmapRows" :cols="heatmapCols" :get-heat-value="getHeatValue" />
        <ProfileSiteGridSection title="收藏网站" :items="profileMockData.favoriteSites" />
        <ProfileProjectGridSection title="上传网站（文件夹）" :items="profileMockData.uploadedProjects" />
        <ProfileSkillsSection :skills="profileMockData.skills" />
        <ProfileContributionSection :stats="profileMockData.stats" :contributions="profileMockData.contributions" />
      </main>
    </div>
  </UContainer>
</template>
