<script setup lang="ts">
import { inject } from 'vue'
import { settingsStateKey } from '../context'

const settingsState = inject(settingsStateKey)
if (!settingsState) {
  throw new Error('Settings state is not provided')
}

const basicInfo = settingsState.basicInfo
const profile = settingsState.profile

defineOptions({
  name: 'BasicProfileSection'
})
</script>

<template>
  <div class="space-y-6">
    <div class="space-y-1">
      <h3 class="text-xl font-medium text-slate-900 dark:text-white">基础资料</h3>
      <p class="text-sm text-slate-500 dark:text-slate-400">管理您的个人基础信息和公开展示的资料。</p>
    </div>
    
    <div class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-0 dark:bg-black dark:shadow-none">
      <div class="space-y-6">
        
        <!-- 头像设置 -->
        <div class="flex items-center space-x-6">
          <UAvatar :src="basicInfo.avatar" :alt="basicInfo.nickname" size="3xl" class="ring-2 ring-white dark:ring-0" />
          <div class="space-y-2">
            <UButton color="white" variant="solid" size="sm" class="dark:bg-white/10 dark:text-white dark:hover:bg-white/20 dark:ring-0">更改头像</UButton>
            <p class="text-xs text-slate-500 dark:text-slate-500">支持 JPG、PNG 格式，最大 2MB</p>
          </div>
        </div>

        <UDivider class="dark:border-white/10" />

        <!-- 基础表单 -->
        <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
          <UFormGroup label="昵称" name="nickname">
            <UInput v-model="basicInfo.nickname" placeholder="您的昵称" class="dark:ring-0 dark:bg-white/5" />
          </UFormGroup>
          
          <UFormGroup label="性别" name="gender">
            <USelect v-model="profile.gender" :options="[{ label: '未知', value: 0 }, { label: '男', value: 1 }, { label: '女', value: 2 }, { label: '保密', value: 3 }]" class="dark:ring-0 dark:bg-white/5" />
          </UFormGroup>

          <UFormGroup label="所在国家" name="country">
            <UInput v-model="profile.country" placeholder="例如：中国" class="dark:ring-0 dark:bg-white/5" />
          </UFormGroup>

          <UFormGroup label="所在城市" name="city">
            <UInput v-model="profile.city" placeholder="例如：深圳" class="dark:ring-0 dark:bg-white/5" />
          </UFormGroup>
        </div>

        <UFormGroup label="个性签名" name="signature">
          <UTextarea v-model="profile.signature" :rows="3" placeholder="介绍一下自己吧" class="dark:ring-0 dark:bg-white/5" />
        </UFormGroup>

        <UDivider class="dark:border-white/10" />

        <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
          <UFormGroup label="GitHub 主页" name="githubUrl">
            <UInput v-model="profile.githubUrl" icon="i-lucide-github" placeholder="https://github.com/..." class="dark:ring-0 dark:bg-white/5" />
          </UFormGroup>
          
          <UFormGroup label="个人博客" name="blogUrl">
            <UInput v-model="profile.blogUrl" icon="i-lucide-link" placeholder="https://..." class="dark:ring-0 dark:bg-white/5" />
          </UFormGroup>
        </div>

        <div class="flex justify-end pt-4">
          <UButton color="black" variant="solid" class="dark:bg-white dark:text-black dark:hover:bg-gray-200">保存更改</UButton>
        </div>
      </div>
    </div>
  </div>
</template>
