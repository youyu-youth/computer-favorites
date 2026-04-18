<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const currentYear = new Date().getFullYear()
</script>

<template>
  <footer
    data-testid="app-footer"
    class="mt-auto shrink-0 border-t border-slate-200 bg-white dark:border-white/10 dark:bg-black"
  >
    <div
      class="mx-auto flex w-full max-w-7xl flex-col items-start justify-between gap-3 px-4 py-5 sm:flex-row sm:items-center sm:px-6 lg:px-8"
    >
      <p class="text-xs text-slate-500 dark:text-slate-400">
        © {{ currentYear }} {{ t('user.home.hero.title') }} ·
        {{ t('settings.account.danger.deleteConfirm') }}
      </p>
      <div class="flex flex-wrap items-center gap-4 text-xs text-slate-500 dark:text-slate-400">
        <a
          href="#"
          class="cursor-pointer transition-colors hover:text-slate-700 dark:hover:text-slate-200"
          >{{ t('settings.message.email.collect') }}</a
        >
        <a
          href="#"
          class="cursor-pointer transition-colors hover:text-slate-700 dark:hover:text-slate-200"
          >{{ t('settings.message.push.system') }}</a
        >
        <a
          href="#"
          class="cursor-pointer transition-colors hover:text-slate-700 dark:hover:text-slate-200"
          >{{ t('settings.message.push.update') }}</a
        >
        <RouterLink
          to="/computer/announcement"
          class="cursor-pointer transition-colors hover:text-slate-700 dark:hover:text-slate-200"
          >平台公告</RouterLink
        >
      </div>
    </div>
  </footer>
</template>
