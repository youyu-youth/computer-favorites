<script setup lang="ts">
const currentYear = new Date().getFullYear()
</script>

<template>
  <UFooter
    class="mt-auto shrink-0 border-t border-slate-200 bg-white dark:border-white/10 dark:bg-[#121212]"
    :ui="{ container: 'mx-auto w-full max-w-7xl px-4 py-5 sm:px-6 lg:px-8' }"
  >
    <template #left>
      <p class="text-xs text-slate-500 dark:text-slate-400">© {{ currentYear }} 编程爱好者的宝藏网站 · 保留所有权利</p>
    </template>
    <div class="flex items-center gap-4 text-xs text-slate-500 dark:text-slate-400">
      <a href="#" class="transition-colors hover:text-slate-700 dark:hover:text-slate-200">隐私政策</a>
      <a href="#" class="transition-colors hover:text-slate-700 dark:hover:text-slate-200">服务条款</a>
      <a href="#" class="transition-colors hover:text-slate-700 dark:hover:text-slate-200">法律声明</a>
    </div>
  </UFooter>
</template>
