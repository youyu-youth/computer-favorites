<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppNavbar from '@/components/common/AppNavbar.vue'
import FloatingActionButton from '@/components/common/FloatingActionButton.vue'
import AppFooter from '@/components/common/AppFooter.vue'

const route = useRoute()
const hideFab = computed(() => route.name === 'collection' || route.meta?.hideFab === true)

/** 路由 meta.hideFooter=true 时不渲染底部页脚 */
const hideFooter = computed(() => route.meta?.hideFooter === true)

/** 路由 meta.navbarAutoHide=true 时启用导航栏 hover-reveal 模式 */
const navbarAutoHide = computed(() => route.meta?.navbarAutoHide === true)
</script>

<template>
  <div class="min-h-screen text-gray-900 transition-colors duration-300 dark:text-gray-200">
    <div class="neon-bg flex min-h-screen flex-col">
      <AppNavbar :auto-hide="navbarAutoHide" />
      <main class="flex min-h-0 flex-1 flex-col" :class="navbarAutoHide ? '' : 'pt-16'">
        <div class="flex h-full min-h-0 flex-1 flex-col">
          <RouterView />
        </div>
      </main>
      <AppFooter v-if="!hideFooter" />
    </div>
    <FloatingActionButton v-if="!hideFab" />
  </div>
</template>
