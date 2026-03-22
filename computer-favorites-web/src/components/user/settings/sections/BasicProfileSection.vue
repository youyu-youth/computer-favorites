<script setup lang="ts">
import { computed, inject, ref } from 'vue'
import { settingsStateKey } from '@/components/user/settings/context'
import HobbyAddDialog from '@/components/user/settings/components/HobbyAddDialog.vue'
import addressIcon from '@/assets/icons/svg/address2.svg'
import addIcon from '@/assets/icons/svg/add.svg'
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
const addressValue = computed({
  get: () => [profile.country, profile.city].filter(Boolean).join(' / '),
  set: (value: string) => {
    const parts = value
      .split(/[\/，,]/)
      .map((item) => item.trim())
      .filter(Boolean)
    profile.country = parts[0] ?? ''
    profile.city = parts.slice(1).join(' ')
  },
})
const hobbyTagList = computed({
  get: () => profile.hobbyTags.split(/[，,]/).map((item) => item.trim()).filter(Boolean),
  set: (value: string[]) => {
    profile.hobbyTags = value.map((item) => item.trim()).filter(Boolean).join(',')
  },
})
const addHobbyModalOpen = ref(false)
const newHobbyName = ref('')

const openAddHobbyDialog = () => {
  newHobbyName.value = ''
  addHobbyModalOpen.value = true
}

const closeAddHobbyModal = () => {
  addHobbyModalOpen.value = false
  newHobbyName.value = ''
}

const submitNewHobby = () => {
  const value = newHobbyName.value.trim()
  if (!value) {
    closeAddHobbyModal()
    return
  }
  const normalized = value.toLowerCase()
  const exists = hobbyTagList.value.some((item) => item.trim().toLowerCase() === normalized)
  if (!exists) {
    hobbyTagList.value = [...hobbyTagList.value, value]
  }
  closeAddHobbyModal()
}

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
        <div class="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-6">
          <div class="sm:w-28 shrink-0 text-lg font-extrabold text-slate-900 dark:text-white">头像</div>
          <div class="flex-1 flex items-center space-x-6">
            <UAvatar :src="basicInfo.avatar" :alt="basicInfo.nickname" class="h-[120px] w-[120px] ring-2 ring-white dark:ring-0" />
            <div class="space-y-2">
              <UButton color="white" variant="solid" size="sm" class="cursor-pointer dark:bg-white/10 dark:text-white dark:hover:bg-white/20 dark:ring-0">更改头像</UButton>
              <p class="text-xs text-slate-500 dark:text-slate-500">支持 JPG、PNG 格式，最大 2MB</p>
            </div>
          </div>
        </div>

        <USeparator class="dark:border-white/10" />

        <!-- 昵称 -->
        <div class="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-6">
          <div class="flex-1">
            <UFormGroup name="nickname">
              <div class="relative">
                <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center gap-2 text-sm font-bold text-slate-700 dark:text-slate-200">
                  <img :src="nickNameIcon" alt="昵称图标" class="h-5 w-5 object-contain" />
                  昵称
                </span>
                <UInput v-model="basicInfo.nickname" placeholder="您的昵称" class="pl-28 w-full ring-0 [&_[data-slot=base]]:!bg-white [&_[data-slot=base]]:text-slate-900 [&_[data-slot=base]]:placeholder:text-slate-500 dark:[&_[data-slot=base]]:!bg-[#000000] dark:[&_[data-slot=base]]:text-white dark:[&_[data-slot=base]]:placeholder:text-slate-400" />
              </div>
            </UFormGroup>
          </div>
        </div>

        <div class="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-6">
          <div class="flex-1">
            <UFormGroup name="address">
              <div class="relative">
                <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center gap-2 text-sm font-bold text-slate-700 dark:text-slate-200">
                  <img :src="addressIcon" alt="地址图标" class="h-5 w-5 object-contain" />
                  地址
                </span>
                <UInput v-model="addressValue" placeholder="例如：中国 / 深圳" class="pl-28 w-full ring-0 [&_[data-slot=base]]:!bg-white [&_[data-slot=base]]:text-slate-900 [&_[data-slot=base]]:placeholder:text-slate-500 dark:[&_[data-slot=base]]:!bg-[#000000] dark:[&_[data-slot=base]]:text-white dark:[&_[data-slot=base]]:placeholder:text-slate-400" />
              </div>
            </UFormGroup>
          </div>
        </div>

        <!-- 个性签名 -->
        <div class="flex flex-col sm:flex-row sm:items-start gap-2 sm:gap-6">
          <div class="flex-1">
            <UFormGroup name="signature">
              <div class="relative">
                <span class="pointer-events-none absolute left-3 top-3 flex items-center gap-2 text-sm font-bold text-slate-700 dark:text-slate-200">
                  <img :src="penIcon" alt="签名图标" class="h-5 w-5 object-contain" />
                  签名
                </span>
                <UTextarea v-model="profile.signature" :rows="3" placeholder="介绍一下自己吧" class="pl-28 w-full ring-0 [&_[data-slot=base]]:!bg-white [&_[data-slot=base]]:text-slate-900 [&_[data-slot=base]]:placeholder:text-slate-500 dark:[&_[data-slot=base]]:!bg-[#000000] dark:[&_[data-slot=base]]:text-white dark:[&_[data-slot=base]]:placeholder:text-slate-400" />
              </div>
            </UFormGroup>
          </div>
        </div>

        <USeparator class="dark:border-white/10" />

        <!-- GitHub 主页 -->
        <div class="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-6">
          <div class="flex-1">
            <UFormGroup name="githubUrl">
              <div class="relative">
                <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center gap-2 text-sm font-bold text-slate-700 dark:text-slate-200">
                  <img :src="githubIcon" alt="GitHub图标" class="h-5 w-5 rounded-sm bg-white p-[1px] object-contain" />
                  GitHub
                </span>
                <UInput v-model="profile.githubUrl" placeholder="https://github.com/..." class="pl-28 w-full ring-0 [&_[data-slot=base]]:!bg-white [&_[data-slot=base]]:text-slate-900 [&_[data-slot=base]]:placeholder:text-slate-500 dark:[&_[data-slot=base]]:!bg-[#000000] dark:[&_[data-slot=base]]:text-white dark:[&_[data-slot=base]]:placeholder:text-slate-400" />
              </div>
            </UFormGroup>
          </div>
        </div>

        <!-- Gitee 主页 -->
        <div class="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-6">
          <div class="flex-1">
            <UFormGroup name="giteeUrl">
              <div class="relative">
                <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center gap-2 text-sm font-bold text-slate-700 dark:text-slate-200">
                  <img :src="giteeIcon" alt="Gitee图标" class="h-5 w-5 object-contain" />
                  Gitee
                </span>
                <UInput v-model="profile.giteeUrl" placeholder="https://gitee.com/..." class="pl-28 w-full ring-0 [&_[data-slot=base]]:!bg-white [&_[data-slot=base]]:text-slate-900 [&_[data-slot=base]]:placeholder:text-slate-500 dark:[&_[data-slot=base]]:!bg-[#000000] dark:[&_[data-slot=base]]:text-white dark:[&_[data-slot=base]]:placeholder:text-slate-400" />
              </div>
            </UFormGroup>
          </div>
        </div>

        <!-- 个人博客 -->
        <div class="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-6">
          <div class="flex-1">
            <UFormGroup name="blogUrl">
              <div class="relative">
                <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center gap-2 text-sm font-bold text-slate-700 dark:text-slate-200">
                  <img :src="blogIcon" alt="博客图标" class="h-5 w-5 object-contain" />
                  博客
                </span>
                <UInput v-model="profile.blogUrl" placeholder="https://..." class="pl-24 w-full ring-0 [&_[data-slot=base]]:!bg-white [&_[data-slot=base]]:text-slate-900 [&_[data-slot=base]]:placeholder:text-slate-500 dark:[&_[data-slot=base]]:!bg-[#000000] dark:[&_[data-slot=base]]:text-white dark:[&_[data-slot=base]]:placeholder:text-slate-400" />
              </div>
            </UFormGroup>
          </div>
        </div>

        <!-- 爱好标签 -->
        <div class="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-6">
          <div class="flex-1">
            <UFormGroup name="hobbyTags">
              <div class="relative">
                <span class="pointer-events-none absolute inset-y-0 left-3 z-10 flex items-center text-sm font-bold text-slate-700 dark:text-slate-200">兴趣爱好</span>
                <UInputTags
                  v-model="hobbyTagList"
                  class="w-full pl-24 pr-12 ring-0 cursor-pointer"
                  :ui="{
                    base: '!bg-white text-slate-900 placeholder:text-slate-500 cursor-pointer dark:!bg-[#000000] dark:text-white dark:placeholder:text-slate-400',
                    input: 'cursor-pointer text-slate-900 dark:text-white',
                    item: 'bg-slate-100 text-slate-900 dark:bg-white/15 dark:text-white',
                    itemDelete: 'cursor-pointer',
                    itemDeleteIcon: 'cursor-pointer',
                  }"
                />
                <UButton
                  type="button"
                  color="primary"
                  variant="solid"
                  size="xs"
                  class="absolute right-2 top-1/2 z-10 -translate-y-1/2 p-1.5 shadow-sm"
                  @click="openAddHobbyDialog"
                >
                  <img :src="addIcon" alt="新增爱好" class="h-4 w-4 cursor-pointer object-contain" />
                </UButton>
                <HobbyAddDialog
                  :open="addHobbyModalOpen"
                  :model-value="newHobbyName"
                  @update:open="addHobbyModalOpen = $event"
                  @update:model-value="newHobbyName = $event"
                  @cancel="closeAddHobbyModal"
                  @submit="submitNewHobby"
                />
              </div>
            </UFormGroup>
          </div>
        </div>

        <!-- 擅长技术栈 -->
        <div class="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-6">
          <div class="flex-1">
            <UFormGroup name="techStack">
              <div class="relative">
                <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center text-sm font-bold text-slate-700 dark:text-slate-200">技术栈</span>
                <UInput v-model="profile.techStack" placeholder="如：Vue.js,TypeScript,Spring Boot,MySQL" class="pl-20 w-full ring-0 [&_[data-slot=base]]:!bg-white [&_[data-slot=base]]:text-slate-900 [&_[data-slot=base]]:placeholder:text-slate-500 dark:[&_[data-slot=base]]:!bg-[#000000] dark:[&_[data-slot=base]]:text-white dark:[&_[data-slot=base]]:placeholder:text-slate-400" />
              </div>
            </UFormGroup>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>
