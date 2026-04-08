<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <nav class="mb-6 flex items-center text-sm font-medium" aria-label="Breadcrumb">
    <ol class="flex min-w-0 items-center space-x-2 text-gray-500 dark:text-gray-400">
      <li>
        <RouterLink
          to="/admin"
          class="hover:text-blue-600 dark:hover:text-blue-400 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 rounded-sm inline-flex items-center"
        >
          <i class="fas fa-home mr-1.5 text-[13px] opacity-80" aria-hidden="true"></i>
          控制台
        </RouterLink>
      </li>

      <li class="flex items-center" aria-hidden="true">
        <i class="fas fa-chevron-right text-[10px] text-gray-400 opacity-60 mx-1"></i>
      </li>

      <li class="text-gray-900 dark:text-gray-100 font-semibold" aria-current="page">
        分类管理
      </li>
    </ol>
  </nav>
</template>
