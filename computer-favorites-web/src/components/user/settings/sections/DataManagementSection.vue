<script setup lang="ts">
defineOptions({
  name: 'DataManagementSection'
})

const handleExport = () => {
  alert('导出功能开发中')
}

const handleClearCache = () => {
  alert('缓存已清理')
}
</script>

<template>
  <div class="space-y-6">
    <div class="space-y-1">
      <h3 class="text-xl font-medium text-slate-900 dark:text-white">数据管理</h3>
      <p class="text-sm text-slate-500 dark:text-slate-400">导出您的数据、清理本地缓存或注销您的账号。</p>
    </div>

    <!-- 数据与缓存 -->
    <div class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-0 dark:bg-black dark:shadow-none">
      <div class="space-y-6">
        
        <div class="flex items-center justify-between">
          <div class="space-y-1">
            <h4 class="text-base font-medium text-slate-900 dark:text-white">导出个人数据</h4>
            <p class="text-sm text-slate-500 dark:text-slate-400">以 JSON 格式下载您的所有收藏、投稿和个人资料。</p>
          </div>
          <UButton @click="handleExport" variant="soft" color="gray" class="cursor-pointer dark:bg-white/10 dark:text-white dark:hover:bg-white/20">
            申请导出
          </UButton>
        </div>

        <UDivider class="dark:border-white/10" />

        <div class="flex items-center justify-between">
          <div class="space-y-1">
            <h4 class="text-base font-medium text-slate-900 dark:text-white">清理本地缓存</h4>
            <p class="text-sm text-slate-500 dark:text-slate-400">释放系统存储空间（约 12MB）。</p>
          </div>
          <UButton @click="handleClearCache" variant="soft" color="gray" class="cursor-pointer dark:bg-white/10 dark:text-white dark:hover:bg-white/20">
            立即清理
          </UButton>
        </div>

      </div>
    </div>

    <!-- 危险区域 -->
    <div class="rounded-2xl border border-red-200 bg-white p-6 shadow-sm dark:border-0 dark:bg-black dark:ring-1 dark:ring-red-900/50 dark:shadow-none">
      <h4 class="mb-4 text-base font-medium text-red-600 dark:text-red-500">危险操作区域</h4>
      
      <div class="space-y-6">
        <div class="flex items-center justify-between">
          <div class="space-y-1">
            <p class="text-sm font-medium text-slate-900 dark:text-white">注销账号</p>
            <p class="text-xs text-slate-500 dark:text-slate-400">
              永久删除账号及所有相关数据，<span class="font-semibold text-red-600 dark:text-red-500">此操作不可恢复</span>。
            </p>
          </div>
          <UButton color="red" variant="soft" class="cursor-pointer">
            注销账号
          </UButton>
        </div>
      </div>
    </div>

  </div>
</template>
