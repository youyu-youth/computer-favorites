<script setup lang="ts">
import AppNavbar from '@/components/common/AppNavbar.vue'
import FloatingActionButton from '@/components/common/FloatingActionButton.vue'
import AppFooter from '@/components/common/AppFooter.vue'

const handleShare = () => {
  alert('分享功能即将上线')
}
</script>

<template>
  <div class="min-h-screen text-gray-900 transition-colors duration-300 dark:text-gray-200">
    <div class="neon-bg flex min-h-screen flex-col">
      <AppNavbar />
      <main class="flex-1 pt-16">
        <RouterView />
      </main>
      <AppFooter />
    </div>
    <FloatingActionButton @click="handleShare" />
  </div>
</template>
