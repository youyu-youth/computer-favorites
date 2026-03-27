<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const currentYear = new Date().getFullYear()
</script>

<template>
  <UFooter
    class="mt-auto shrink-0 border-t border-slate-200 bg-white dark:border-white/10 dark:bg-[#121212]"
    :ui="{ container: 'mx-auto w-full max-w-7xl px-4 py-5 sm:px-6 lg:px-8' }"
  >
    <template #left>
      <p class="text-xs text-slate-500 dark:text-slate-400">© {{ currentYear }} {{ t('user.home.hero.title') }} · {{ t('settings.account.danger.deleteConfirm') }}</p>
    </template>
    <div class="flex items-center gap-4 text-xs text-slate-500 dark:text-slate-400">
      <a href="#" class="transition-colors hover:text-slate-700 dark:hover:text-slate-200">{{ t('settings.message.email.collect') }}</a>
      <a href="#" class="transition-colors hover:text-slate-700 dark:hover:text-slate-200">{{ t('settings.message.push.system') }}</a>
      <a href="#" class="transition-colors hover:text-slate-700 dark:hover:text-slate-200">{{ t('settings.message.push.update') }}</a>
    </div>
  </UFooter>
</template>
