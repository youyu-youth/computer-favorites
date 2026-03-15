<script setup lang="ts">
import AppNavbar from '@/components/common/AppNavbar.vue'
import FloatingActionButton from '@/components/common/FloatingActionButton.vue'

const handleShare = () => {
  alert('分享功能即将上线')
}
</script>

<template>
  <div class="min-h-screen text-gray-900 dark:text-gray-200 transition-colors duration-300 neon-bg">
    <AppNavbar />
    <RouterView />
    <FloatingActionButton @click="handleShare" />
  </div>
</template>
