<script setup lang="ts">
import { useToast } from '@/composables/useToast'

defineOptions({
  name: 'DataManagementSection',
})

const toast = useToast()

const handleExport = () => {
  toast.add({
    title: '导出请求已提交',
    description: '我们会在 24 小时内通过邮件发送数据下载链接',
    type: 'info',
  })
}

const handleClearCache = () => {
  toast.add({
    title: '缓存已清理',
    description: '本次释放约 12 MB 本地缓存空间',
    type: 'success',
  })
}

const handleDeactivate = () => {
  toast.add({
    title: '请先验证身份',
    description: '注销流程需要邮箱二次验证（开发中）',
    type: 'warning',
  })
}
</script>

<template>
  <div class="space-y-8">
    <!-- Section Header -->
    <header class="space-y-2">
      <div class="flex items-center gap-3">
        <span class="block h-6 w-1 rounded-full bg-amber-500"></span>
        <h3 class="text-xl font-semibold tracking-tight text-slate-900 dark:text-white md:text-2xl">数据管理</h3>
      </div>
      <p class="text-sm text-slate-500 dark:text-slate-400">导出个人数据、清理本地缓存或注销账号。</p>
    </header>

    <!-- 数据 -->
    <section class="space-y-4">
      <div class="text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">
        Data Operations · 数据操作
      </div>

      <div class="cf-row flex flex-col gap-4 rounded-xl border border-slate-200 bg-white/60 p-5 transition-colors hover:border-slate-300 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-white/20 dark:hover:bg-[#1c1c25] sm:flex-row sm:items-center sm:justify-between">
        <div class="flex min-w-0 items-start gap-3">
          <span class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-amber-200 bg-amber-50 text-amber-600 dark:border-amber-400/20 dark:bg-amber-500/10 dark:text-amber-400">
            <UIcon name="i-lucide-database" class="h-5 w-5" />
          </span>
          <div class="space-y-1.5">
            <div class="flex flex-wrap items-center gap-2">
              <p class="text-sm font-semibold text-slate-900 dark:text-white">导出个人数据</p>
              <span class="rounded-full border border-slate-200 px-2 py-0.5 font-mono text-[10px] tracking-wider text-slate-500 dark:border-white/10 dark:text-slate-400">JSON</span>
              <span class="rounded-full border border-slate-200 px-2 py-0.5 font-mono text-[10px] tracking-wider text-slate-500 dark:border-white/10 dark:text-slate-400">≈ 1.2 MB</span>
            </div>
            <p class="text-xs text-slate-500 dark:text-slate-400">下载你的所有收藏、投稿、个人资料与互动记录。</p>
          </div>
        </div>
        <button
          type="button"
          class="cf-action-btn shrink-0 inline-flex h-9 cursor-pointer items-center gap-1.5 self-stretch rounded-lg border border-slate-200 bg-white px-3.5 text-xs font-medium text-slate-700 transition-colors hover:border-amber-400 hover:text-amber-600 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-200 dark:hover:border-amber-400/40 dark:hover:text-amber-300 sm:self-auto"
          @click="handleExport"
        >
          <UIcon name="i-lucide-download" class="h-3.5 w-3.5" />
          <span>申请导出</span>
        </button>
      </div>

      <div class="cf-row flex flex-col gap-4 rounded-xl border border-slate-200 bg-white/60 p-5 transition-colors hover:border-slate-300 dark:border-white/[0.10] dark:bg-[#16161d] dark:hover:border-white/20 dark:hover:bg-[#1c1c25] sm:flex-row sm:items-center sm:justify-between">
        <div class="flex min-w-0 items-start gap-3">
          <span class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-slate-200 bg-slate-100 text-slate-600 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-300">
            <UIcon name="i-lucide-sparkles" class="h-5 w-5" />
          </span>
          <div class="space-y-1.5">
            <div class="flex flex-wrap items-center gap-2">
              <p class="text-sm font-semibold text-slate-900 dark:text-white">清理本地缓存</p>
              <span class="rounded-full border border-slate-200 px-2 py-0.5 font-mono text-[10px] tracking-wider text-slate-500 dark:border-white/10 dark:text-slate-400">≈ 12 MB</span>
              <span class="rounded-full border border-slate-200 px-2 py-0.5 font-mono text-[10px] tracking-wider text-slate-500 dark:border-white/10 dark:text-slate-400">3 项</span>
            </div>
            <p class="text-xs text-slate-500 dark:text-slate-400">清理浏览器中残留的图片缩略图、搜索历史与离线数据。</p>
          </div>
        </div>
        <button
          type="button"
          class="cf-action-btn shrink-0 inline-flex h-9 cursor-pointer items-center gap-1.5 self-stretch rounded-lg border border-slate-200 bg-white px-3.5 text-xs font-medium text-slate-700 transition-colors hover:border-amber-400 hover:text-amber-600 dark:border-white/[0.10] dark:bg-[#1c1c25] dark:text-slate-200 dark:hover:border-amber-400/40 dark:hover:text-amber-300 sm:self-auto"
          @click="handleClearCache"
        >
          <UIcon name="i-lucide-eraser" class="h-3.5 w-3.5" />
          <span>立即清理</span>
        </button>
      </div>
    </section>

    <!-- 危险区域 -->
    <section class="cf-danger-zone overflow-hidden rounded-xl border border-rose-200 bg-rose-50/40 p-5 dark:border-rose-500/20 dark:bg-rose-500/[0.04]">
      <div class="mb-4 flex items-center gap-3">
        <span class="flex h-9 w-9 items-center justify-center rounded-lg bg-rose-100 text-rose-600 dark:bg-rose-500/15 dark:text-rose-400">
          <UIcon name="i-lucide-triangle-alert" class="h-4 w-4" />
        </span>
        <div>
          <p class="text-sm font-semibold text-rose-700 dark:text-rose-300">危险操作区域</p>
          <p class="text-[11px] uppercase tracking-[0.18em] text-rose-500/80 dark:text-rose-400/70">Danger Zone</p>
        </div>
      </div>

      <div class="flex flex-col gap-3 rounded-lg border border-rose-200/80 bg-white/80 p-4 dark:border-rose-500/15 dark:bg-rose-950/20 sm:flex-row sm:items-center sm:justify-between">
        <div class="space-y-1">
          <p class="text-sm font-semibold text-slate-900 dark:text-white">注销账号</p>
          <p class="text-xs text-slate-500 dark:text-slate-400">
            永久删除账号、收藏、投稿与所有互动记录。
            <span class="font-semibold text-rose-600 dark:text-rose-400">此操作不可撤销</span>。
          </p>
        </div>
        <button
          type="button"
          class="shrink-0 inline-flex h-9 cursor-pointer items-center gap-1.5 rounded-lg border border-rose-300 bg-white px-3.5 text-xs font-semibold text-rose-600 transition-colors hover:border-rose-400 hover:bg-rose-50 dark:border-rose-500/30 dark:bg-rose-500/10 dark:text-rose-300 dark:hover:border-rose-400/60 dark:hover:bg-rose-500/15"
          @click="handleDeactivate"
        >
          <UIcon name="i-lucide-user-x" class="h-3.5 w-3.5" />
          <span>注销账号</span>
        </button>
      </div>
    </section>
  </div>
</template>
