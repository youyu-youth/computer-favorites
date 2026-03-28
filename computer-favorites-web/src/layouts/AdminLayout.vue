<script setup lang="ts">
import AdminHeader from '@/components/admin/layout/AdminHeader.vue'
</script>

<template>
  <div class="flex flex-col min-h-screen bg-gray-50 text-gray-900 dark:bg-dark-bg dark:text-gray-200 transition-colors duration-200 font-sans" data-cf-theme="admin">
    <AdminHeader />
    <RouterView />
  </div>
</template>

