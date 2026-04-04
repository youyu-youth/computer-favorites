<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppNavbar from '@/components/common/AppNavbar.vue'
import FloatingActionButton from '@/components/common/FloatingActionButton.vue'
import AppFooter from '@/components/common/AppFooter.vue'

const route = useRoute()
const isHomeRoute = computed(() => route.name === 'home' || route.path === '/computer/home')

const handleShare = () => {
  alert('分享功能即将上线')
}
</script>

<template>
  <div class="min-h-screen text-gray-900 transition-colors duration-300 dark:text-gray-200">
    <div
      :class="['neon-bg flex min-h-screen flex-col', { 'cf-home-black-dark-scope': isHomeRoute }]"
      :data-testid="isHomeRoute ? 'home-dark-scope' : undefined"
    >
      <AppNavbar />
      <main class="flex min-h-0 flex-1 flex-col pt-16">
        <!-- 路由容器使用纵向弹性布局，确保子页面可撑满主内容区并贴合页脚 -->
        <div class="flex h-full min-h-0 flex-1 flex-col">
          <RouterView />
        </div>
      </main>
      <AppFooter />
    </div>
    <FloatingActionButton @click="handleShare" />
  </div>
</template>
