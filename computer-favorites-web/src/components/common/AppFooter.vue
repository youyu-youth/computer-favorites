<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const currentYear = new Date().getFullYear()
</script>

<template>
  <footer class="w-full shrink-0 border-t border-border-default bg-surface-page">
    <div class="mx-auto flex w-full max-w-7xl flex-col gap-8 px-4 py-8 sm:px-6 lg:flex-row lg:items-center lg:justify-between lg:px-8 lg:py-10">
      <div class="flex flex-col gap-2">
        <span class="font-headline text-xl font-bold tracking-tight text-text-primary">Monolith Pro</span>
        <p class="font-body text-[10px] uppercase tracking-[0.2em] text-text-secondary">
          © {{ currentYear }} {{ t('user.home.hero.title') }} · {{ t('settings.account.danger.deleteConfirm') }}
        </p>
      </div>

      <div class="flex flex-wrap gap-10 lg:gap-16">
        <div class="flex min-w-[160px] flex-col gap-3">
          <span class="font-body text-xs font-bold uppercase tracking-widest text-primary-500">Navigation</span>
          <a href="#" class="cursor-pointer font-body text-xs uppercase tracking-widest text-text-secondary transition-colors hover:text-text-primary">
            {{ t('settings.message.email.collect') }}
          </a>
          <a href="#" class="cursor-pointer font-body text-xs uppercase tracking-widest text-text-secondary transition-colors hover:text-text-primary">
            {{ t('settings.message.push.system') }}
          </a>
        </div>

        <div class="flex min-w-[160px] flex-col gap-3">
          <span class="font-body text-xs font-bold uppercase tracking-widest text-primary-500">Resources</span>
          <a href="#" class="cursor-pointer font-body text-xs uppercase tracking-widest text-text-secondary transition-colors hover:text-text-primary">
            {{ t('settings.message.push.update') }}
          </a>
          <a href="#" class="cursor-pointer font-body text-xs uppercase tracking-widest text-text-secondary transition-colors hover:text-text-primary">
            {{ t('common.info') }}
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>
