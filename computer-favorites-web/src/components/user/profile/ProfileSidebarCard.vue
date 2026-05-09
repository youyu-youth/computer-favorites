<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * Profile 侧边栏（GitHub Stats 风格）：头像 + 编辑入口 + 签名 + 社交链接 + 标签 + 上传 / 收藏文件夹
 */
import { DotLottieVue } from '@lottiefiles/dotlottie-vue'
import { useRouter } from 'vue-router'
import { Folder, FolderTree, MapPin, Pencil, Star } from 'lucide-vue-next'
import StatCard from './dashboard/StatCard.vue'
import type { ProfileData } from '@/types/profile'
import catPlayingAnimation from '@/assets/animation/Cat playing animation.lottie?url'

withDefaults(
  defineProps<{
    profile: ProfileData
    isOwn?: boolean
    showCollections?: boolean
  }>(),
  {
    isOwn: true,
    showCollections: true,
  },
)

const router = useRouter()

const handleEdit = () => {
  router.push('/computer/settings')
}

const isExternalUrl = (url: string) => /^https?:\/\//i.test(url)
const isMailto = (url: string) => url.startsWith('mailto:')
const handleSocialClick = (url: string) => {
  if (isExternalUrl(url)) {
    window.open(url, '_blank', 'noopener')
    return
  }
  if (isMailto(url)) {
    window.location.href = url
    return
  }
  router.push(url)
}
</script>

<template>
  <aside class="w-full shrink-0 lg:-ml-4 lg:w-[19rem] xl:-ml-6">
    <div class="space-y-3 lg:sticky lg:top-24">
      <StatCard dense>
        <div class="space-y-2">
          <div class="flex items-start gap-3">
            <div class="relative aspect-square w-28 shrink-0 overflow-hidden rounded-sm border border-black/5 dark:border-white/[0.06]">
              <img
                :src="profile.avatarUrl"
                :alt="profile.name"
                class="h-full w-full object-cover"
                referrerpolicy="no-referrer"
              />
            </div>

            <div class="flex min-h-28 min-w-0 flex-1 flex-col justify-between">
              <div class="space-y-1.5">
                <h2 class="truncate text-base font-semibold tracking-tight text-gray-900 dark:text-white">
                  {{ profile.name }}
                </h2>
                <div class="flex items-center gap-1.5 font-mono text-[11.5px] text-gray-500 dark:text-gray-400">
                  <MapPin class="size-3.5 shrink-0 text-amber-500" :stroke-width="2" />
                  <span class="truncate">{{ profile.location }}</span>
                </div>
              </div>
              <div class="-ml-1 w-full max-w-[176px]">
                <DotLottieVue :src="catPlayingAnimation" autoplay loop class="h-auto w-full" />
              </div>
            </div>
          </div>

          <button
            v-if="isOwn"
            type="button"
            class="inline-flex w-28 cursor-pointer items-center justify-center gap-1 whitespace-nowrap rounded-sm bg-amber-500/10 px-1.5 py-1.5 font-mono text-[11px] text-amber-600 transition-colors hover:bg-amber-500/20 dark:text-amber-400"
            @click="handleEdit"
          >
            <Pencil class="size-3" :stroke-width="2" />
            编辑资料
          </button>
        </div>
      </StatCard>

      <StatCard dense>
        <div class="flex items-start gap-2">
          <Pencil class="mt-0.5 size-3.5 shrink-0 text-amber-500" :stroke-width="2" />
          <p class="text-[12.5px] leading-6 text-gray-600 dark:text-gray-400">
            {{ profile.signature }}
          </p>
        </div>
      </StatCard>

      <StatCard v-if="profile.socialLinks.length" dense title="社交链接">
        <ul class="grid grid-cols-2 gap-1.5">
          <li v-for="link in profile.socialLinks" :key="link.label">
            <button
              type="button"
              class="flex w-full cursor-pointer items-center gap-1.5 rounded-sm border border-black/5 px-2 py-1.5 font-mono text-[11.5px] text-gray-700 transition-colors hover:bg-black/5 dark:border-white/[0.06] dark:text-gray-300 dark:hover:bg-white/5"
              @click="handleSocialClick(link.url)"
            >
              <img
                v-if="link.imageIcon"
                :src="link.imageIcon"
                :alt="link.label"
                :class="[
                  'size-3.5 shrink-0',
                  ['博客', 'Gitee', '邮箱'].includes(link.label)
                    ? ''
                    : 'dark:invert dark:brightness-0',
                ]"
              />
              <span class="truncate">{{ link.label }}</span>
            </button>
          </li>
        </ul>
      </StatCard>

      <StatCard v-if="profile.tags.length" dense title="个人标签">
        <ul class="flex flex-wrap gap-1.5">
          <li
            v-for="tag in profile.tags"
            :key="tag"
            class="rounded-sm border border-black/5 bg-black/[0.03] px-1.5 py-0.5 font-mono text-[11px] text-gray-700 dark:border-white/[0.06] dark:bg-white/[0.03] dark:text-gray-300"
          >
            {{ tag }}
          </li>
        </ul>
      </StatCard>

      <StatCard dense>
        <template #header>
          <div class="flex w-full items-center justify-between gap-2">
            <span class="flex items-center gap-1.5 text-[12.5px] font-semibold tracking-tight text-gray-900 dark:text-gray-100">
              <FolderTree class="size-3.5 text-amber-500" :stroke-width="2" />
              上传网站（文件夹）
            </span>
            <span class="font-mono text-[10.5px] text-gray-500 dark:text-gray-400">
              {{ profile.uploadedProjects.length }} 个网站
            </span>
          </div>
        </template>
        <ul class="space-y-1.5">
          <li
            v-for="item in profile.uploadedProjects"
            :key="item.name"
            class="flex items-start gap-1.5 rounded-sm border border-black/5 bg-black/[0.02] p-1.5 dark:border-white/[0.06] dark:bg-white/[0.02]"
          >
            <Folder class="mt-0.5 size-3.5 shrink-0 text-amber-500" :stroke-width="2" />
            <div class="min-w-0 flex-1">
              <p class="truncate text-[12.5px] font-semibold text-gray-900 dark:text-gray-100">
                {{ item.name }}
              </p>
              <p class="break-all font-mono text-[11px] leading-5 text-gray-500 dark:text-gray-400">
                {{ item.description }}
              </p>
            </div>
          </li>
          <li
            v-if="!profile.uploadedProjects.length"
            class="font-mono text-[12px] text-gray-500 dark:text-gray-400"
          >
            暂无上传网站
          </li>
        </ul>
      </StatCard>

      <StatCard v-if="isOwn || showCollections" dense>
        <template #header>
          <div class="flex w-full items-center justify-between gap-2">
            <span class="flex items-center gap-1.5 text-[12.5px] font-semibold tracking-tight text-gray-900 dark:text-gray-100">
              <Star class="size-3.5 text-amber-500" :stroke-width="2" />
              收藏网站
            </span>
            <span class="font-mono text-[10.5px] text-gray-500 dark:text-gray-400">
              {{ profile.favoriteSites.length }} 个收藏
            </span>
          </div>
        </template>
        <ul class="space-y-1.5">
          <li
            v-for="item in profile.favoriteSites"
            :key="item.name"
            class="flex items-start gap-1.5 rounded-sm border border-black/5 bg-black/[0.02] p-1.5 dark:border-white/[0.06] dark:bg-white/[0.02]"
          >
            <Star class="mt-0.5 size-3.5 shrink-0 text-amber-500" :stroke-width="2" />
            <div class="min-w-0 flex-1">
              <p class="truncate text-[12.5px] font-semibold text-gray-900 dark:text-gray-100">
                {{ item.name }}
              </p>
              <p class="break-all font-mono text-[11px] leading-5 text-gray-500 dark:text-gray-400">
                {{ item.description }}
              </p>
            </div>
          </li>
          <li
            v-if="!profile.favoriteSites.length"
            class="font-mono text-[12px] text-gray-500 dark:text-gray-400"
          >
            暂无收藏网站
          </li>
        </ul>
      </StatCard>
    </div>
  </aside>
</template>
