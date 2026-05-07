<script setup lang="ts">
import { computed, inject, ref } from 'vue'
import { settingsStateKey } from '@/components/user/settings/context'
import EditableInput from '@/components/user/settings/components/EditableInput.vue'
import TechStackPickerDialog from '@/components/user/settings/components/TechStackPickerDialog.vue'
import githubIcon from '@/assets/icons/svg/github.svg'
import giteeIcon from '@/assets/icons/svg/gitee.svg'

const settingsState = inject(settingsStateKey)
if (!settingsState) {
  throw new Error('Settings state is not provided')
}

const basicInfo = settingsState.basicInfo
const profile = settingsState.profile

const SIGNATURE_MAX = 80

const signatureLength = computed(() => (profile.signature ?? '').length)

const genderOptions = [
  { value: 1, label: '男', icon: 'i-lucide-mars' },
  { value: 2, label: '女', icon: 'i-lucide-venus' },
  { value: 3, label: '保密', icon: 'i-lucide-circle-help' },
] as const

const setGender = (value: number) => {
  profile.gender = value
}

// 字符串 <-> 数组双向转换，复用 UInputTags
const splitTags = (raw: string): string[] =>
  (raw ?? '')
    .split(/[,，]/)
    .map((item) => item.trim())
    .filter(Boolean)

const hobbyTagsArray = computed<string[]>({
  get: () => splitTags(profile.hobbyTags),
  set: (value: string[]) => {
    profile.hobbyTags = value.join(',')
  },
})

const techStackArray = computed<string[]>({
  get: () => splitTags(profile.techStack),
  set: (value: string[]) => {
    profile.techStack = value.join(',')
  },
})

const techDialogOpen = ref(false)

const handleAddTechStacks = (names: string[]) => {
  const existing = new Set(techStackArray.value.map((n) => n.toLowerCase()))
  const merged = [...techStackArray.value]
  for (const name of names) {
    const trimmed = name.trim()
    if (!trimmed) {
      continue
    }
    if (!existing.has(trimmed.toLowerCase())) {
      merged.push(trimmed)
      existing.add(trimmed.toLowerCase())
    }
  }
  techStackArray.value = merged
  techDialogOpen.value = false
}

defineOptions({
  name: 'BasicProfileSection',
})
</script>

<template>
  <div class="space-y-8">
    <!-- Section Header -->
    <header class="space-y-2">
      <div class="flex items-center gap-3">
        <span class="block h-6 w-1 rounded-full bg-amber-500"></span>
        <h3 class="text-xl font-semibold tracking-tight text-slate-900 dark:text-white md:text-2xl">基础资料</h3>
      </div>
      <p class="text-sm text-slate-500 dark:text-slate-400">管理你的个人基础信息和公开展示的资料。</p>
    </header>

    <!-- 头像 -->
    <section class="cf-block flex flex-col gap-6 rounded-xl border border-slate-200/80 bg-white/60 p-5 dark:border-white/[0.10] dark:bg-[#16161d] sm:flex-row sm:items-center">
      <div class="relative shrink-0">
        <div class="absolute -inset-1 rounded-full bg-amber-400/15 blur-md" aria-hidden="true"></div>
        <UAvatar
          :src="basicInfo.avatar"
          :alt="basicInfo.nickname"
          class="relative h-[112px] w-[112px] ring-2 ring-amber-400/40 ring-offset-4 ring-offset-white dark:ring-amber-400/30 dark:ring-offset-[#16161d]"
        />
        <span class="absolute -bottom-0.5 -right-0.5 flex h-6 w-6 items-center justify-center rounded-full border-2 border-white bg-amber-500 text-white shadow dark:border-[#16161d]">
          <UIcon name="i-lucide-camera" class="h-3 w-3" />
        </span>
      </div>

      <div class="flex flex-1 flex-col gap-3">
        <div class="space-y-1">
          <p class="text-sm font-semibold text-slate-900 dark:text-white">头像</p>
          <p class="text-xs text-slate-500 dark:text-slate-400">
            <UIcon name="i-lucide-info" class="-mt-0.5 mr-1 inline h-3 w-3" />
            支持 JPG / PNG / WebP，最大 2 MB；推荐 1:1 正方形 256×256 及以上
          </p>
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <button
            type="button"
            class="cf-action-btn inline-flex h-9 cursor-pointer items-center gap-1.5 rounded-lg border border-amber-400/40 bg-amber-50 px-3.5 text-xs font-medium text-amber-700 transition-colors hover:border-amber-400 hover:bg-amber-100/70 dark:border-amber-400/30 dark:bg-amber-500/10 dark:text-amber-300 dark:hover:bg-amber-500/15"
          >
            <UIcon name="i-lucide-upload" class="h-3.5 w-3.5" />
            <span>更换头像</span>
          </button>
          <button
            type="button"
            class="cf-action-btn inline-flex h-9 cursor-pointer items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3.5 text-xs font-medium text-slate-600 transition-colors hover:border-slate-300 hover:text-slate-900 dark:border-white/10 dark:bg-[#1c1c25] dark:text-slate-400 dark:hover:border-white/20 dark:hover:text-white"
          >
            <UIcon name="i-lucide-trash-2" class="h-3.5 w-3.5" />
            <span>移除</span>
          </button>
        </div>
      </div>
    </section>

    <div class="cf-hairline" aria-hidden="true"></div>

    <!-- 身份信息 -->
    <section class="space-y-5">
      <div class="text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">
        Identity · 身份信息
      </div>

      <div class="grid grid-cols-1 gap-5 sm:grid-cols-2">
        <UFormGroup label="昵称">
          <EditableInput v-model="basicInfo.nickname" placeholder="你希望被如何称呼" :maxlength="32" />
        </UFormGroup>

        <UFormGroup label="性别">
          <div class="grid grid-cols-3 gap-2 rounded-lg border border-slate-200 bg-slate-50 p-1 dark:border-white/10 dark:bg-[#1c1c25]">
            <button
              v-for="opt in genderOptions"
              :key="opt.value"
              type="button"
              class="flex h-9 cursor-pointer items-center justify-center gap-1.5 rounded-md text-xs font-medium transition-all duration-150"
              :class="profile.gender === opt.value
                ? 'bg-white text-amber-600 shadow-sm ring-1 ring-amber-400/40 dark:bg-amber-500/10 dark:text-amber-300 dark:ring-amber-400/30'
                : 'text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white'"
              @click="setGender(opt.value)"
            >
              <UIcon :name="opt.icon" class="h-3.5 w-3.5" />
              <span>{{ opt.label }}</span>
            </button>
          </div>
        </UFormGroup>

        <UFormGroup label="所在国家">
          <EditableInput v-model="profile.country" placeholder="例如：中国" />
        </UFormGroup>

        <UFormGroup label="所在城市">
          <EditableInput v-model="profile.city" placeholder="例如：深圳" />
        </UFormGroup>
      </div>

      <UFormGroup>
        <div class="flex items-center justify-between">
          <label class="text-sm font-medium text-slate-700 dark:text-slate-200">个性签名</label>
          <span
            class="font-mono text-[11px] tabular-nums"
            :class="signatureLength >= SIGNATURE_MAX ? 'text-amber-500' : 'text-slate-400 dark:text-slate-500'"
          >
            {{ signatureLength }} / {{ SIGNATURE_MAX }}
          </span>
        </div>
        <EditableInput
          v-model="profile.signature"
          as="textarea"
          :rows="3"
          :maxlength="SIGNATURE_MAX"
          placeholder="一句话介绍自己，将展示在你的个人主页"
        />
      </UFormGroup>
    </section>

    <div class="cf-hairline" aria-hidden="true"></div>

    <!-- 链接与标签 -->
    <section class="space-y-5">
      <div class="text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">
        Links & Tags · 链接与标签
      </div>

      <div class="grid grid-cols-1 gap-5 sm:grid-cols-2">
        <UFormGroup>
          <label class="flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-200">
            <img :src="githubIcon" alt="GitHub" class="h-4 w-4 rounded-sm bg-white p-[1px] object-contain dark:bg-white/90" />
            <span>GitHub</span>
          </label>
          <EditableInput v-model="profile.githubUrl" placeholder="https://github.com/your-name" input-class="font-mono text-sm" autocomplete="off" />
        </UFormGroup>

        <UFormGroup>
          <label class="flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-200">
            <img :src="giteeIcon" alt="Gitee" class="h-4 w-4 object-contain" />
            <span>Gitee</span>
          </label>
          <EditableInput v-model="profile.giteeUrl" placeholder="https://gitee.com/your-name" input-class="font-mono text-sm" autocomplete="off" />
        </UFormGroup>

        <UFormGroup>
          <label class="flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-200">
            <UIcon name="i-lucide-globe" class="h-4 w-4 text-slate-500 dark:text-slate-400" />
            <span>个人博客</span>
          </label>
          <EditableInput v-model="profile.blogUrl" placeholder="https://your-blog.com" input-class="font-mono text-sm" autocomplete="off" />
        </UFormGroup>

        <UFormGroup>
          <label class="flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-200">
            <UIcon name="i-lucide-heart" class="h-4 w-4 text-amber-500" />
            <span>爱好标签</span>
          </label>
          <UInputTags
            v-model="hobbyTagsArray"
            placeholder="输入后回车添加（如：阅读、开源、设计）"
            class="w-full"
          />
          <p class="text-[11px] text-slate-400 dark:text-slate-500">回车或逗号确认 · 退格删除最近一项</p>
        </UFormGroup>
      </div>

      <UFormGroup>
        <label class="flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-200">
          <UIcon name="i-lucide-cpu" class="h-4 w-4 text-amber-500" />
          <span>擅长技术栈</span>
        </label>
        <UInputTags
          v-model="techStackArray"
          placeholder="如：Vue.js、TypeScript、Spring Boot、MySQL"
          class="w-full"
        />
        <button
          type="button"
          class="cf-tech-add-btn group mt-2 inline-flex w-full cursor-pointer items-center justify-center gap-2 rounded-lg border border-dashed border-slate-300 bg-slate-50/50 px-4 py-2.5 text-sm font-medium text-slate-600 transition-all duration-200 hover:border-amber-400/60 hover:bg-amber-50/40 hover:text-amber-700 dark:border-white/10 dark:bg-white/[0.02] dark:text-slate-400 dark:hover:border-amber-400/40 dark:hover:bg-amber-500/[0.06] dark:hover:text-amber-300"
          @click="techDialogOpen = true"
        >
          <UIcon name="i-lucide-plus" class="h-4 w-4 transition-transform duration-200 group-hover:rotate-90" />
          <span>从技术栈库中选择添加</span>
        </button>
        <p class="text-[11px] text-slate-400 dark:text-slate-500">将展示在个人主页和投稿署名上</p>
      </UFormGroup>
    </section>

    <TechStackPickerDialog
      :open="techDialogOpen"
      :existing="techStackArray"
      @update:open="techDialogOpen = $event"
      @submit="handleAddTechStacks"
    />
  </div>
</template>

<style scoped>
.cf-hairline {
  height: 1px;
  background-image: linear-gradient(
    90deg,
    transparent 0%,
    rgb(15 23 42 / 0.08) 20%,
    rgb(245 158 11 / 0.25) 50%,
    rgb(15 23 42 / 0.08) 80%,
    transparent 100%
  );
}

:where(html.dark) .cf-hairline {
  background-image: linear-gradient(
    90deg,
    transparent 0%,
    rgb(255 255 255 / 0.06) 20%,
    rgb(245 158 11 / 0.35) 50%,
    rgb(255 255 255 / 0.06) 80%,
    transparent 100%
  );
}
</style>
