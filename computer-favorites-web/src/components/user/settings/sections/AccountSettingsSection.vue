<script setup lang="ts">
import { inject } from 'vue'
import { settingsStateKey } from '@/components/user/settings/context'

const settingsState = inject(settingsStateKey)
if (!settingsState) {
  throw new Error('Settings state is not provided')
}

const basicInfo = settingsState.basicInfo

defineOptions({
  name: 'AccountSettingsSection'
})
</script>

<template>
  <div class="space-y-6">
    <div class="space-y-1">
      <h3 class="text-xl font-medium text-slate-900 dark:text-white">账号设置</h3>
      <p class="text-sm text-slate-500 dark:text-slate-400">管理您的登录邮箱、手机号及账号安全。</p>
    </div>

    <!-- 邮箱与手机号 -->
    <div class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-0 dark:bg-black dark:shadow-none">
      <h4 class="mb-4 text-base font-medium text-slate-900 dark:text-white">联系方式绑定</h4>
      
      <div class="space-y-6">
        <div class="flex items-center justify-between rounded-lg border border-slate-100 bg-slate-50/50 p-4 dark:border-white/5 dark:bg-white/5">
          <div class="flex items-center space-x-4">
            <div class="flex h-10 w-10 items-center justify-center rounded-full bg-primary-100 text-primary-600 dark:bg-primary-500/20 dark:text-primary-300">
              <UIcon name="i-lucide-mail" class="h-5 w-5" />
            </div>
            <div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">电子邮箱</p>
              <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                {{ basicInfo.emailVerified === 1 ? basicInfo.email : '未绑定邮箱' }}
              </p>
            </div>
          </div>
          <UButton size="sm" variant="soft" color="gray" class="cursor-pointer dark:bg-white/10 dark:text-white dark:hover:bg-white/20">
            {{ basicInfo.emailVerified === 1 ? '修改' : '绑定' }}
          </UButton>
        </div>

        <div class="flex items-center justify-between rounded-lg border border-slate-100 bg-slate-50/50 p-4 dark:border-white/5 dark:bg-white/5">
          <div class="flex items-center space-x-4">
            <div class="flex h-10 w-10 items-center justify-center rounded-full bg-primary-100 text-primary-600 dark:bg-primary-500/20 dark:text-primary-300">
              <UIcon name="i-lucide-smartphone" class="h-5 w-5" />
            </div>
            <div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">手机号码</p>
              <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                {{ basicInfo.phoneVerified === 1 ? basicInfo.phone : '未绑定手机号，无法通过短信找回密码' }}
              </p>
            </div>
          </div>
          <UButton size="sm" variant="soft" color="gray" class="cursor-pointer dark:bg-white/10 dark:text-white dark:hover:bg-white/20">
            {{ basicInfo.phoneVerified === 1 ? '修改' : '绑定' }}
          </UButton>
        </div>
      </div>
    </div>

    <!-- 账号安全 -->
    <div class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-0 dark:bg-black dark:shadow-none">
      <h4 class="mb-4 text-base font-medium text-slate-900 dark:text-white">账号安全</h4>
      
      <div class="space-y-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-slate-900 dark:text-white">登录密码</p>
            <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">上次修改时间：2026-02-15</p>
          </div>
          <UButton size="sm" variant="soft" color="gray" class="cursor-pointer dark:bg-white/10 dark:text-white dark:hover:bg-white/20">
            修改密码
          </UButton>
        </div>
        
        <UDivider class="dark:border-white/10" />

        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-slate-900 dark:text-white">第三方平台绑定</p>
            <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">绑定 GitHub/Gitee 可实现快捷登录</p>
          </div>
          <UButton size="sm" variant="soft" color="gray" class="cursor-pointer dark:bg-white/10 dark:text-white dark:hover:bg-white/20">
            管理绑定
          </UButton>
        </div>
      </div>
    </div>
  </div>
</template>
