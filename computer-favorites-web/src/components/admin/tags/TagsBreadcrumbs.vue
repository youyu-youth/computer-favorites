<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminNavStore } from '@/stores/adminNav'

const router = useRouter()
const adminNavStore = useAdminNavStore()

type BreadcrumbItem = {
  key: string
  label: string
  level: number
  current: boolean
  clickable: boolean
  onClick?: () => void | Promise<void>
}

const goAdminRoot = async (): Promise<void> => {
  adminNavStore.setActiveMenu('websites')
  adminNavStore.setSelectedCategoryId(0)
  if (router.currentRoute.value.name === 'adminWebsites') {
    return
  }
  await router.push({ name: 'adminWebsites' })
}

const items = computed<BreadcrumbItem[]>(() => {
  return [
    {
      key: 'admin-root',
      label: '管理后台',
      level: 1,
      current: false,
      clickable: true,
      onClick: () => goAdminRoot(),
    },
    {
      key: 'admin-tags',
      label: '标签管理',
      level: 2,
      current: true,
      clickable: false,
    },
  ]
})
</script>

<template>
  <div class="mb-5 md:mb-6">
    <UBreadcrumb :items="items" className="mb-4" />
    <div class="border-b border-gray-200 dark:border-dark-border pb-3">
      <h1 class="text-xl md:text-2xl font-semibold text-gray-900 dark:text-gray-100">标签管理</h1>
      <p class="mt-1 text-sm text-gray-600 dark:text-gray-300">基于 t_tag 的 Mock 数据管理，支持搜索、分页和标签编辑。</p>
    </div>
  </div>
</template>
