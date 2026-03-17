<script setup lang="ts">
const currentYear = new Date().getFullYear()
</script>

<template>
  <footer class="border-t border-slate-200 bg-white/90 backdrop-blur dark:border-white/10 dark:bg-black/70">
    <div class="mx-auto w-full max-w-7xl px-4 py-5 sm:px-6 lg:px-8">
      <div class="flex flex-col items-center justify-between gap-3 text-xs text-slate-500 dark:text-slate-400 sm:flex-row">
        <div>© {{ currentYear }} 编程爱好者的宝藏网站 · 保留所有权利</div>
        <div class="flex items-center gap-4">
          <a href="#" class="transition-colors hover:text-slate-700 dark:hover:text-slate-200">隐私政策</a>
          <a href="#" class="transition-colors hover:text-slate-700 dark:hover:text-slate-200">服务条款</a>
          <a href="#" class="transition-colors hover:text-slate-700 dark:hover:text-slate-200">法律声明</a>
        </div>
      </div>
    </div>
  </footer>
</template>
