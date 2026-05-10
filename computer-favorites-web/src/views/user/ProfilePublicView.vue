<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import ProfileContributionSection from '@/components/user/profile/ProfileContributionSection.vue'
import ProfileHeatmapSection from '@/components/user/profile/ProfileHeatmapSection.vue'
import ProfileHeroSection from '@/components/user/profile/ProfileHeroSection.vue'
import ProfileImpactSection from '@/components/user/profile/ProfileImpactSection.vue'
import ProfileSidebarCard from '@/components/user/profile/ProfileSidebarCard.vue'
import ProfileSkillsSection from '@/components/user/profile/ProfileSkillsSection.vue'
import ProfilePrivateView from '@/views/user/ProfilePrivateView.vue'
import { useProfileDashboardStore } from '@/stores/profileDashboard'
import { useProfilePublicStore } from '@/stores/profilePublic'

const props = defineProps<{
  username: string
}>()

const publicStore = useProfilePublicStore()
const dashboardStore = useProfileDashboardStore()
const {
  profileData,
  loading,
  errorMessage,
  isOwn,
  showContribution,
  showCollections,
  isPrivate,
  isLoginRequired,
  isNotFound,
} = storeToRefs(publicStore)

const emptyReason = computed(() => {
  if (isLoginRequired.value) return 'login'
  if (isNotFound.value) return 'notFound'
  if (isPrivate.value) return 'private'
  return 'error'
})

const canRenderProfile = computed(() => Boolean(profileData.value))

const loadPublicProfile = async () => {
  dashboardStore.reset()
  try {
    await publicStore.load(props.username)
    if (showContribution.value) {
      await dashboardStore.loadInitial({ username: props.username })
      return
    }
    dashboardStore.setContext({ username: props.username })
    await Promise.allSettled([dashboardStore.loadCategory(), dashboardStore.loadRadar()])
  } catch {
    dashboardStore.reset()
  }
}

onMounted(loadPublicProfile)

watch(
  () => props.username,
  () => {
    loadPublicProfile()
  },
)
</script>

<template>
  <div class="min-h-screen bg-gray-50 transition-colors duration-300 dark:bg-black">
    <div class="mx-auto max-w-7xl px-3 py-6 sm:px-4 sm:py-8 lg:px-6 lg:py-10">
      <div v-if="loading" class="space-y-3">
        <div class="h-44 w-full animate-pulse rounded-md bg-black/5 dark:bg-white/5" />
        <div class="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
          <div
            v-for="i in 6"
            :key="i"
            class="h-28 w-full animate-pulse rounded-md bg-black/5 dark:bg-white/5"
          />
        </div>
        <div class="h-40 w-full animate-pulse rounded-md bg-black/5 dark:bg-white/5" />
      </div>

      <div v-else-if="canRenderProfile && profileData" class="flex flex-col gap-4 lg:flex-row lg:gap-5">
        <ProfileSidebarCard
          :profile="profileData"
          :is-own="isOwn"
          :show-collections="showCollections"
        />

        <main class="min-w-0 flex-1 space-y-4 lg:pt-3">
          <ProfileImpactSection :username="username" />
          <template v-if="showContribution">
            <ProfileHeroSection />
            <ProfileHeatmapSection />
          </template>
          <section
            v-else
            class="rounded-md border border-amber-500/20 bg-amber-500/10 p-4 text-sm text-amber-700 dark:text-amber-300"
          >
            该用户已关闭贡献数据展示。
          </section>
          <ProfileSkillsSection :skills="profileData.skills" />
          <ProfileContributionSection
            v-if="showContribution"
            :stats="profileData.stats"
            :contributions="profileData.contributions"
          />
        </main>
      </div>

      <ProfilePrivateView
        v-else
        :username="username"
        :reason="emptyReason"
        :message="errorMessage"
      />
    </div>
  </div>
</template>
