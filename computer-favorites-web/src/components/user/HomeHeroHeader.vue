<script setup lang="ts"></script>

<template>
  <header
    class="grid grid-cols-1 lg:grid-cols-[1fr_1fr_1fr] border-b border-gray-200 dark:border-[#1f1f1f]"
  >
    <div
      class="p-8 lg:p-12 border-b lg:border-b-0 lg:border-r border-gray-200 dark:border-[#1f1f1f] flex flex-col justify-between"
    >
      <div>
        <div
          class="flex flex-wrap items-center gap-2 sm:gap-4 text-[10px] sm:text-xs font-mono text-gray-500 dark:text-gray-400 mb-2 uppercase"
        >
          <span
            ><span class="text-primary-500 mr-1">▶</span>
            <span class="text-primary-500">5,147</span> 已收录网站</span
          >
          <span class="text-gray-300 dark:text-gray-600 hidden sm:inline">◆</span>
          <span>30 个分类</span>
          <span class="text-gray-300 dark:text-gray-600 hidden sm:inline">◆</span>
        </div>
        <div
          class="text-[10px] sm:text-xs font-mono text-gray-600 dark:text-gray-400 uppercase mb-8"
        >
          2,917 位贡献者参与共建
        </div>

        <h1
          class="text-4xl sm:text-5xl lg:text-6xl font-mono tracking-tighter leading-[1.1] mb-8 flex flex-col"
        >
          <span class="hollow-text mix-blend-difference dark:mix-blend-normal">发现</span>
          <span class="hollow-text mix-blend-difference dark:mix-blend-normal">优质</span>
          <span class="text-primary-500" style="text-shadow: 0 0 10px rgba(245, 158, 11, 0.3)"
            >计算机</span
          >
          <span class="text-primary-500" style="text-shadow: 0 0 10px rgba(245, 158, 11, 0.3)"
            >资源</span
          >
        </h1>

        <p class="text-gray-600 dark:text-gray-400 text-sm max-w-sm mb-10 leading-relaxed">
          计算机收藏夹聚合开发工具、学习站点与开源项目，支持分类检索与标签筛选，帮助你更快找到真正有价值的网站资源。
        </p>
      </div>

      <div>
        <button
          class="border border-gray-300 dark:border-gray-600 hover:border-primary-500 dark:hover:border-primary-500 hover:text-primary-500 dark:hover:text-primary-500 transition-colors px-6 py-2 text-sm font-mono flex items-center justify-center sm:justify-start gap-2 bg-white/50 dark:bg-black/50 backdrop-blur w-full sm:w-auto"
        >
          浏览全部网站 ↓
        </button>
      </div>
    </div>

    <div
      class="p-8 lg:p-12 border-b lg:border-b-0 lg:border-r border-gray-200 dark:border-[#1f1f1f] flex flex-col justify-center"
    >
      <p class="text-gray-500 dark:text-gray-400 text-sm mb-8">
        平台持续清理失效条目并维护内容质量，以下是当前治理统计。
      </p>

      <div class="flex flex-col font-mono text-sm">
        <div class="flex justify-between py-4 border-b border-gray-200 dark:border-gray-800">
          <span class="text-gray-600 dark:text-gray-300">审核通过资源</span>
          <span class="text-gray-900 dark:text-white">4,065</span>
        </div>
        <div class="flex justify-between py-4 border-b border-gray-200 dark:border-gray-800">
          <span class="text-gray-600 dark:text-gray-300">去重条目</span>
          <span class="text-gray-900 dark:text-white">1,040</span>
        </div>
        <div class="flex justify-between py-4 border-b border-gray-200 dark:border-gray-800">
          <span class="text-gray-600 dark:text-gray-300">更新维护记录</span>
          <span class="text-gray-900 dark:text-white">851</span>
        </div>
        <div class="flex justify-between py-4 border-b border-gray-200 dark:border-gray-800">
          <span class="text-gray-600 dark:text-gray-300">标签关联关系</span>
          <span class="text-gray-900 dark:text-white">731</span>
        </div>
        <div class="flex justify-between py-4 border-b border-gray-200 dark:border-gray-800">
          <span class="text-gray-600 dark:text-gray-300">风险拦截记录</span>
          <span class="text-gray-900 dark:text-white">373</span>
        </div>
        <div class="flex justify-between py-4 mt-2 font-bold">
          <span class="text-gray-900 dark:text-white">累计治理条目</span>
          <span class="text-primary-500">7,060</span>
        </div>
      </div>
    </div>

    <div
      class="p-8 lg:p-12 hidden lg:flex flex-col justify-between items-end relative overflow-hidden bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-gray-100 via-white to-white dark:from-gray-900/40 dark:via-black dark:to-black"
    >
      <div class="text-primary-500/40 font-mono text-xs whitespace-pre select-none">
        █▀▀█ █ █ ▀▀█▀▀█▀▀ █▄▄█
      </div>
      <div
        class="text-green-500/30 font-mono text-xs whitespace-pre select-none absolute bottom-5 left-10"
      >
        █ █ ███ █████
      </div>
    </div>
  </header>
</template>

<style scoped>
.hollow-text {
  color: transparent;
  -webkit-text-stroke: 1px #888;
}
</style>
