<script setup lang="ts">
defineOptions({
  name: 'AccountView',
})
</script>

<template>
  <section class="min-h-[calc(100vh-8rem)] bg-slate-50/70 px-4 py-6 dark:bg-slate-950/40 sm:px-6 md:py-8">
    <UContainer>
      <UCard class="rounded-2xl border border-slate-200 bg-white/90 shadow-sm dark:border-slate-800 dark:bg-slate-900/80">
        <div class="space-y-3">
          <h1 class="text-xl font-semibold text-slate-900 dark:text-slate-100 sm:text-2xl">账户信息</h1>
          <p class="text-sm text-slate-600 dark:text-slate-300">
            账户信息页面正在建设中，后续会在这里展示登录设备、安全记录与账户资料详情。
          </p>
          <p class="text-xs text-slate-500 dark:text-slate-400">访问路径：/computer/account</p>
        </div>
      </UCard>
    </UContainer>
  </section>
</template>
