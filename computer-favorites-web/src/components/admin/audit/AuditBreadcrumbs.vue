<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <div class="mb-6 flex items-center text-sm font-medium">
    <i class="fas fa-shield-halved mr-2 text-gray-400 dark:text-gray-500" aria-hidden="true"></i>
    <RouterLink
      to="/computer/admin"
      class="text-[rgb(var(--cf-color-primary-500-rgb)/1)] hover:underline"
    >
      管理后台
    </RouterLink>
    <span class="mx-2 text-gray-400 dark:text-gray-500" aria-hidden="true">/</span>
    <span class="text-gray-900 dark:text-gray-100 font-semibold" aria-current="page">
      审计日志
    </span>
  </div>
</template>
