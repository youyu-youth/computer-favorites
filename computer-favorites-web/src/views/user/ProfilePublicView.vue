<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import ProfileDashboardCharts from '@/components/user/profile/ProfileDashboardCharts.vue'
import ProfileDashboardSummary from '@/components/user/profile/ProfileDashboardSummary.vue'
import ProfileHeatmapSection from '@/components/user/profile/ProfileHeatmapSection.vue'
import ProfileImpactSection from '@/components/user/profile/ProfileImpactSection.vue'
import ProfileSidebarCard from '@/components/user/profile/ProfileSidebarCard.vue'
import ProfileSkillsSection from '@/components/user/profile/ProfileSkillsSection.vue'
import PublicFolderDialog from '@/components/user/profile/PublicFolderDialog.vue'
import ProfilePrivateView from '@/views/user/ProfilePrivateView.vue'
import { useUserApprovedSubmissions } from '@/composables/useUserApprovedSubmissions'
import { useProfileDashboardStore } from '@/stores/profileDashboard'
import { useProfilePublicStore } from '@/stores/profilePublic'
import type { PublicFolderItem } from '@/api/user-profile-public'
import type { ProfileData } from '@/types/profile'

const props = defineProps<{
  username: string
}>()

const publicStore = useProfilePublicStore()
const dashboardStore = useProfileDashboardStore()
const router = useRouter()
const approvedSubmissions = useUserApprovedSubmissions({
  limit: 5,
  username: computed(() => props.username),
})
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
  publicFolders,
  publicFoldersLoading,
} = storeToRefs(publicStore)

const dialogOpen = ref(false)
const activeFolder = ref<PublicFolderItem | null>(null)

const handleOpenFolder = (folder: PublicFolderItem) => {
  activeFolder.value = folder
  dialogOpen.value = true
}

const handleViewAll = () => {
  router.push({
    name: 'profilePublicCollections',
    params: { username: props.username },
  })
}

const emptyReason = computed(() => {
  if (isLoginRequired.value) return 'login'
  if (isNotFound.value) return 'notFound'
  if (isPrivate.value) return 'private'
  return 'error'
})

const displayProfile = computed<ProfileData | null>(() => {
  if (!profileData.value) {
    return null
  }
  return {
    ...profileData.value,
    uploadedProjects: approvedSubmissions.items.value,
  }
})

const canRenderProfile = computed(() => Boolean(displayProfile.value))

const loadPublicProfile = async () => {
  dashboardStore.reset()
  approvedSubmissions.reset()
  try {
    await publicStore.load(props.username)
    void approvedSubmissions.load()
    // 主 profile 加载后并发拉公开收藏夹（showCollections=0 时后端会抛错，store 内部 swallow）
    if (isOwn.value || showCollections.value) {
      void publicStore.loadPublicFolders(props.username, 5)
    }
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

      <div
        v-else-if="canRenderProfile && displayProfile"
        class="flex flex-col gap-4 lg:flex-row lg:gap-5"
      >
        <ProfileSidebarCard
          :profile="displayProfile"
          :is-own="isOwn"
          :show-collections="showCollections"
          :public-folders="publicFolders"
          :public-folders-loading="publicFoldersLoading"
          @open-folder="handleOpenFolder"
          @view-all="handleViewAll"
        />

        <PublicFolderDialog
          v-model:open="dialogOpen"
          :username="username"
          :folder="activeFolder"
          @view-all="handleViewAll"
        />

        <main class="min-w-0 flex-1 space-y-4 lg:pt-3">
          <template v-if="showContribution">
            <ProfileDashboardCharts />
            <ProfileHeatmapSection />
            <ProfileImpactSection :username="username" />
          </template>
          <section
            v-else
            class="rounded-md border border-amber-500/20 bg-amber-500/10 p-4 text-sm text-amber-700 dark:text-amber-300"
          >
            该用户已关闭贡献数据展示。
          </section>
          <ProfileSkillsSection :skills="displayProfile.skills" />
          <ProfileDashboardSummary v-if="showContribution" />
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
