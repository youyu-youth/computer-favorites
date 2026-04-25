<script setup lang="ts">
import type { AuditLogStatistics } from '@/types/audit-log'

defineProps<{
  statistics: AuditLogStatistics
}>()
</script>

<template>
  <div class="mb-6 grid grid-cols-2 gap-4 sm:grid-cols-4">
    <!-- 今日操作 -->
    <div
      class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm dark:border-dark-border dark:bg-dark-card"
    >
      <div class="flex items-center gap-3">
        <div
          class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-blue-50 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400"
        >
          <i class="fas fa-clock"></i>
        </div>
        <div>
          <p class="text-2xl font-bold text-gray-900 dark:text-white">
            {{ statistics.todayCount }}
          </p>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">今日操作</p>
        </div>
      </div>
    </div>

    <!-- 成功率 -->
    <div
      class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm dark:border-dark-border dark:bg-dark-card"
    >
      <div class="flex items-center gap-3">
        <div
          class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-green-50 text-green-600 dark:bg-green-900/20 dark:text-green-400"
        >
          <i class="fas fa-circle-check"></i>
        </div>
        <div>
          <p class="text-2xl font-bold text-gray-900 dark:text-white">
            {{ statistics.successRate + '%' }}
          </p>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">成功率</p>
        </div>
      </div>
    </div>

    <!-- 失败数 -->
    <div
      class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm dark:border-dark-border dark:bg-dark-card"
    >
      <div class="flex items-center gap-3">
        <div
          class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-red-50 text-red-600 dark:bg-red-900/20 dark:text-red-400"
        >
          <i class="fas fa-circle-xmark"></i>
        </div>
        <div>
          <p class="text-2xl font-bold text-gray-900 dark:text-white">
            {{ statistics.failCount }}
          </p>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">失败数</p>
        </div>
      </div>
    </div>

    <!-- 7日总量 -->
    <div
      class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm dark:border-dark-border dark:bg-dark-card"
    >
      <div class="flex items-center gap-3">
        <div
          class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-amber-50 text-amber-600 dark:bg-amber-900/20 dark:text-amber-400"
        >
          <i class="fas fa-chart-line"></i>
        </div>
        <div>
          <p class="text-2xl font-bold text-gray-900 dark:text-white">
            {{ statistics.weekCount }}
          </p>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">7日总量</p>
        </div>
      </div>
    </div>
  </div>
</template>
