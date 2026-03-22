<script setup lang="ts">
import { computed, inject, ref } from 'vue'
import { useToast } from '@/composables/useToast'
import { useRouter } from 'vue-router'
import UsernameEditDialog from '@/components/user/settings/components/UsernameEditDialog.vue'
import { settingsStateKey } from '@/components/user/settings/context'
import type { SettingsStoreState } from '@/components/user/settings/mock'
import { updateCurrentUsername } from '@/services/profile'

const settingsState = inject<SettingsStoreState>(settingsStateKey)
if (!settingsState) {
  throw new Error('Settings state is not provided')
}

const router = useRouter()
const toast = useToast()
const basicInfo = settingsState.basicInfo
const profile = settingsState.profile
const usernameDialogOpen = ref(false)
const updatingUsername = ref(false)

// 使用数据库返回的更新时间回显密码最近修改时间
const passwordLastUpdateText = computed(() => {
  if (!profile.updateTime) {
    return '暂无修改记录'
  }
  const date = new Date(profile.updateTime)
  if (Number.isNaN(date.getTime())) {
    return profile.updateTime
  }
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  }).format(date)
})

const goPasswordChangePage = () => {
  void router.push({ name: 'passwordChange' })
}

const openUsernameDialog = () => {
  usernameDialogOpen.value = true
}

const handleUsernameSubmit = async (username: string) => {
  if (updatingUsername.value) {
    return
  }
  updatingUsername.value = true
  try {
    await updateCurrentUsername({ username })
    basicInfo.username = username
    usernameDialogOpen.value = false
    toast.add({
      title: '修改成功',
      description: '用户名已更新',
      type: 'success',
    })
  } catch (error) {
    const description = error instanceof Error ? error.message : '用户名修改失败，请稍后重试'
    toast.add({
      title: '修改失败',
      description,
      type: 'error',
    })
  } finally {
    updatingUsername.value = false
  }
}

defineOptions({
  name: 'AccountSettingsSection',
})
</script>

<template>
  <div class="space-y-6">
    <div class="space-y-1">
      <h3 class="text-xl font-medium text-slate-900 dark:text-white">账号设置</h3>
      <p class="text-sm text-slate-500 dark:text-slate-400">管理您的登录邮箱、手机号及账号安全。</p>
    </div>

    <div
      class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-0 dark:bg-black dark:shadow-none"
    >
      <h4 class="mb-4 text-base font-medium text-slate-900 dark:text-white">联系方式绑定</h4>

      <div class="space-y-6">
        <div
          class="flex items-center justify-between rounded-lg border border-slate-100 bg-slate-50/50 p-4 dark:border-white/5 dark:bg-white/5"
        >
          <div class="flex items-center space-x-4">
            <div
              class="flex h-10 w-10 items-center justify-center rounded-full bg-primary-100 text-primary-600 dark:bg-primary-500/20 dark:text-primary-300"
            >
              <UIcon name="i-lucide-mail" class="h-5 w-5" />
            </div>
            <div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">电子邮箱</p>
              <p class="mt-0.5 text-xs text-slate-500 dark:text-slate-400">
                {{ basicInfo.emailVerified === 1 ? basicInfo.email : '未绑定邮箱' }}
              </p>
            </div>
          </div>
          <UButton
            size="sm"
            variant="soft"
            color="gray"
            class="cursor-pointer bg-slate-100/70 text-slate-700 hover:bg-slate-200/70 dark:bg-white/10 dark:text-white dark:hover:bg-white/20"
          >
            {{ basicInfo.emailVerified === 1 ? '修改' : '绑定' }}
          </UButton>
        </div>

        <div
          class="flex items-center justify-between rounded-lg border border-slate-100 bg-slate-50/50 p-4 dark:border-white/5 dark:bg-white/5"
        >
          <div class="flex items-center space-x-4">
            <div
              class="flex h-10 w-10 items-center justify-center rounded-full bg-primary-100 text-primary-600 dark:bg-primary-500/20 dark:text-primary-300"
            >
              <UIcon name="i-lucide-smartphone" class="h-5 w-5" />
            </div>
            <div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">手机号码</p>
              <p class="mt-0.5 text-xs text-slate-500 dark:text-slate-400">
                {{ basicInfo.phoneVerified === 1 ? basicInfo.phone : '未绑定手机号，无法通过短信找回密码' }}
              </p>
            </div>
          </div>
          <UButton
            size="sm"
            variant="soft"
            color="gray"
            class="cursor-pointer bg-slate-100/70 text-slate-700 hover:bg-slate-200/70 dark:bg-white/10 dark:text-white dark:hover:bg-white/20"
          >
            {{ basicInfo.phoneVerified === 1 ? '修改' : '绑定' }}
          </UButton>
        </div>
      </div>
    </div>

    <div
      class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-0 dark:bg-black dark:shadow-none"
    >
      <h4 class="mb-4 text-base font-medium text-slate-900 dark:text-white">账号安全</h4>

      <div class="space-y-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-slate-900 dark:text-white">登录密码</p>
            <p class="mt-0.5 text-xs text-slate-500 dark:text-slate-400">上次修改时间：{{ passwordLastUpdateText }}</p>
          </div>
          <UButton
            size="sm"
            variant="soft"
            color="gray"
            class="cursor-pointer bg-slate-100/70 text-slate-700 hover:bg-slate-200/70 dark:bg-white/10 dark:text-white dark:hover:bg-white/20"
            @click="goPasswordChangePage"
          >
            修改密码
          </UButton>
        </div>

        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-slate-900 dark:text-white">用户名</p>
            <p class="mt-0.5 text-xs text-slate-500 dark:text-slate-400">
              当前用户名：{{ basicInfo.username || '未设置' }}
            </p>
          </div>
          <UButton
            size="sm"
            variant="soft"
            color="gray"
            class="cursor-pointer bg-slate-100/70 text-slate-700 hover:bg-slate-200/70 dark:bg-white/10 dark:text-white dark:hover:bg-white/20"
            @click="openUsernameDialog"
          >
            修改用户名
          </UButton>
        </div>

        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-slate-900 dark:text-white">第三方平台绑定</p>
            <p class="mt-0.5 text-xs text-slate-500 dark:text-slate-400">
              绑定 GitHub/Gitee 可实现快捷登录
            </p>
          </div>
          <UButton
            size="sm"
            variant="soft"
            color="gray"
            class="cursor-pointer bg-slate-100/70 text-slate-700 hover:bg-slate-200/70 dark:bg-white/10 dark:text-white dark:hover:bg-white/20"
          >
            管理绑定
          </UButton>
        </div>
      </div>
    </div>

    <UsernameEditDialog
      :open="usernameDialogOpen"
      :loading="updatingUsername"
      :current-username="basicInfo.username"
      @update:open="usernameDialogOpen = $event"
      @submit="handleUsernameSubmit"
    />
  </div>
</template>
