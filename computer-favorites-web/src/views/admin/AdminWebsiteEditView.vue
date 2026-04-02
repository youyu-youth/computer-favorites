<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import WebsitesBreadcrumbs from '@/components/admin/websites/WebsitesBreadcrumbs.vue'
import AdminAddWebsite from '@/components/admin/websites/AdminAddWebsite.vue'
import { useAdminNavStore } from '@/stores/adminNav'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const adminNavStore = useAdminNavStore()
const { add: showToast } = useToast()

const websiteId = computed<number | null>(() => {
  const parsedId = Number(route.params.id)
  if (!Number.isInteger(parsedId) || parsedId <= 0) {
    return null
  }
  return parsedId
})

onMounted(() => {
  adminNavStore.setActiveMenu('websites')
  if (!websiteId.value) {
    showToast({ type: 'error', title: '网站ID不合法，无法进入编辑页面' })
  }
})

const handleCancel = () => {
  const hasRouterBack = Boolean(window.history.state && window.history.state.back)
  if (hasRouterBack) {
    router.back()
    return
  }

  void router.push({ name: 'adminWebsites' })
}
</script>

<template>
  <main class="flex-grow w-full px-4 sm:px-6 lg:px-8 py-6">
    <div class="max-w-[1320px] mx-auto min-w-0">
      <WebsitesBreadcrumbs viewMode="edit" />

      <div
        v-if="!websiteId"
        class="rounded-xl border border-red-200 bg-red-50 px-6 py-12 text-center text-red-500 dark:border-red-900/40 dark:bg-red-900/10 dark:text-red-300"
      >
        <i class="fas fa-circle-exclamation text-3xl"></i>
        <p class="mt-3 text-sm">网站ID不合法，无法加载编辑页面。</p>
      </div>

      <AdminAddWebsite
        v-else
        mode="edit"
        :websiteId="websiteId"
        @cancel="handleCancel"
      />
    </div>
  </main>
</template>
