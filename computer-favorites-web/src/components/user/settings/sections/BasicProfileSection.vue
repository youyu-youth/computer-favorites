<script setup lang="ts">
import { inject } from 'vue'
import { settingsStateKey } from '@/components/user/settings/context'
import nickNameIcon from '@/assets/icons/svg/nicheng.svg'
import penIcon from '@/assets/icons/svg/pen.svg'
import githubIcon from '@/assets/icons/svg/github.svg'
import giteeIcon from '@/assets/icons/svg/gitee.svg'
import blogIcon from '@/assets/icons/svg/blog.svg'

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
      <div class="space-y-8">

        <!-- 头像设置 -->
        <div class="flex items-center space-x-6">
          <UAvatar :src="basicInfo.avatar" :alt="basicInfo.nickname" class="h-[120px] w-[120px] ring-2 ring-white dark:ring-0" />
          <div class="space-y-2">
            <UButton color="white" variant="solid" size="sm" class="cursor-pointer dark:bg-white/10 dark:text-white dark:hover:bg-white/20 dark:ring-0">更改头像</UButton>
            <p class="text-xs text-slate-500 dark:text-slate-500">支持 JPG、PNG 格式，最大 2MB</p>
          </div>
        </div>

        <UDivider class="mt-2 dark:border-white/10" />

        <!-- 基础表单 -->
        <div class="grid grid-cols-1 gap-8 sm:grid-cols-2">
          <UFormGroup label="昵称" name="nickname">
            <div class="relative">
              <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400">
                <img :src="nickNameIcon" alt="昵称图标" class="h-4 w-4 object-contain" />
                昵称
              </span>
              <UInput v-model="basicInfo.nickname" placeholder="您的昵称" class="pl-24 dark:ring-0 dark:bg-white/5" />
            </div>
          </UFormGroup>

          <UFormGroup label="所在国家" name="country">
            <div class="relative">
              <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center text-xs text-slate-500 dark:text-slate-400">国家</span>
              <UInput v-model="profile.country" placeholder="例如：中国" class="pl-12 dark:ring-0 dark:bg-white/5" />
            </div>
          </UFormGroup>

          <UFormGroup label="所在城市" name="city">
            <div class="relative">
              <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center text-xs text-slate-500 dark:text-slate-400">城市</span>
              <UInput v-model="profile.city" placeholder="例如：深圳" class="pl-12 dark:ring-0 dark:bg-white/5" />
            </div>
          </UFormGroup>
        </div>

        <UFormGroup label="个性签名" name="signature">
          <div class="relative">
            <span class="pointer-events-none absolute left-3 top-3 flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400">
              <img :src="penIcon" alt="签名图标" class="h-4 w-4 object-contain" />
              签名
            </span>
            <UTextarea v-model="profile.signature" :rows="3" placeholder="介绍一下自己吧" class="pl-24 dark:ring-0 dark:bg-white/5" />
          </div>
        </UFormGroup>

        <UDivider class="pt-6 pb-2 dark:border-white/10" />

        <div class="grid grid-cols-1 gap-8 sm:grid-cols-2">
          <UFormGroup label="GitHub 主页" name="githubUrl">
            <div class="relative">
              <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400">
                <img :src="githubIcon" alt="GitHub图标" class="h-4 w-4 rounded-sm bg-white p-[1px] object-contain" />
                GitHub
              </span>
              <UInput v-model="profile.githubUrl" placeholder="https://github.com/..." class="pl-24 dark:ring-0 dark:bg-white/5" />
            </div>
          </UFormGroup>

          <UFormGroup label="Gitee 主页" name="giteeUrl">
            <div class="relative">
              <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400">
                <img :src="giteeIcon" alt="Gitee图标" class="h-4 w-4 object-contain" />
                Gitee
              </span>
              <UInput v-model="profile.giteeUrl" placeholder="https://gitee.com/..." class="pl-24 dark:ring-0 dark:bg-white/5" />
            </div>
          </UFormGroup>

          <UFormGroup label="个人博客" name="blogUrl">
            <div class="relative">
              <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400">
                <img :src="blogIcon" alt="博客图标" class="h-4 w-4 object-contain" />
                博客
              </span>
              <UInput v-model="profile.blogUrl" placeholder="https://..." class="pl-20 dark:ring-0 dark:bg-white/5" />
            </div>
          </UFormGroup>

          <UFormGroup label="爱好标签" name="hobbyTags">
            <div class="relative">
              <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center text-xs text-slate-500 dark:text-slate-400">爱好</span>
              <UInput v-model="profile.hobbyTags" placeholder="如：阅读,开源,设计" class="pl-12 dark:ring-0 dark:bg-white/5" />
            </div>
          </UFormGroup>
        </div>

        <div class="grid grid-cols-1 gap-8">
          <UFormGroup label="擅长技术栈" name="techStack">
            <div class="relative">
              <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center text-xs text-slate-500 dark:text-slate-400">技术栈</span>
              <UInput v-model="profile.techStack" placeholder="如：Vue.js,TypeScript,Spring Boot,MySQL" class="pl-16 dark:ring-0 dark:bg-white/5" />
            </div>
          </UFormGroup>
        </div>

      </div>
    </div>
  </div>
</template>
