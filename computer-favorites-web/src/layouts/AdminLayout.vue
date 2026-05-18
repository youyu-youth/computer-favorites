<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import AdminHeader from '@/components/admin/layout/AdminHeader.vue'
import AdminSidebarTree from '@/components/admin/layout/AdminSidebarTree.vue'
import { useAdminNavStore } from '@/stores/adminNav'

const route = useRoute()
const adminNavStore = useAdminNavStore()

const showSidebar = computed(() => {
  return route.meta.hideAdminSidebar !== true
})

/** 路由 meta.headerAutoHide=true 时启用顶栏 hover-reveal 模式 */
const headerAutoHide = computed(() => route.meta?.headerAutoHide === true)

watch(
  () => route.meta.hideAdminSidebar,
  (hideSidebar) => {
    if (hideSidebar === true) {
      adminNavStore.closeMobileSidebar()
    }
  },
  { immediate: true },
)
</script>

<template>
  <div
    class="flex flex-col min-h-screen bg-gray-50 text-gray-900 dark:bg-dark-bg dark:text-gray-200 transition-colors duration-200 font-sans"
    data-cf-theme="admin"
  >
    <AdminHeader :auto-hide="headerAutoHide" />
    <div class="flex flex-1 min-h-0">
      <AdminSidebarTree v-if="showSidebar" />
      <div class="flex-1 min-w-0">
        <RouterView />
      </div>
    </div>
  </div>
</template>
