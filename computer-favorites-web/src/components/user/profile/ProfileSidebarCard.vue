<script setup lang="ts">
import { DotLottieVue } from '@lottiefiles/dotlottie-vue'
import type { ProfileData } from '@/types/profile'
import penIcon from '@/assets/icons/svg/pen.svg'
import catPlayingAnimation from '@/assets/animation/Cat playing animation.lottie?url'

defineProps<{
  profile: ProfileData
}>()
</script>

<template>
  <aside class="w-full lg:w-80 shrink-0">
    <div class="lg:sticky lg:top-24 space-y-4">
      <UCard class="!ring-0 shadow-none bg-white dark:!bg-black rounded-xl">
        <div class="flex items-start gap-4">
          <UAvatar :src="profile.avatarUrl" :alt="profile.name" class="!w-28 !h-28 !rounded-none ring-2 ring-primary/30" :ui="{ rounded: 'rounded-none' }" />
          <div class="min-w-0 space-y-2 pt-1">
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white truncate">{{ profile.name }}</h2>
            <div class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
              <UIcon name="i-lucide-map-pin" class="size-4 text-primary-500 shrink-0" />
              <span class="truncate">{{ profile.location }}</span>
            </div>
            <div class="w-full max-w-[180px] sm:max-w-[220px]">
              <DotLottieVue
                :src="catPlayingAnimation"
                autoplay
                loop
                class="h-auto w-full"
              />
            </div>
          </div>
        </div>
      </UCard>

      <div class="space-y-4">
        <div class="flex items-start gap-2">
          <img :src="penIcon" class="size-5 shrink-0" alt="个性签名" />
          <p class="text-sm md:text-base leading-7 text-gray-600 dark:text-gray-400">
            {{ profile.signature }}
          </p>
        </div>

        <div class="flex flex-wrap gap-3">
          <UButton
            v-for="link in profile.socialLinks"
            :key="link.label"
            :to="link.url"
            color="neutral"
            variant="soft"
            size="sm"
            class="!bg-gray-100 hover:!bg-gray-200 !text-gray-900 dark:!bg-white/10 dark:hover:!bg-white/20 dark:!text-white !ring-0"
          >
            <img
              v-if="link.imageIcon"
              :src="link.imageIcon"
              :class="['size-4', ['博客', 'Gitee', '邮箱'].includes(link.label) ? '' : 'dark:grayscale dark:brightness-0 dark:invert']"
              :alt="link.label"
            />
            <UIcon v-else-if="link.icon" :name="link.icon" class="size-4" />
            <span>{{ link.label }}</span>
          </UButton>
        </div>
      </div>

      <UCard class="!ring-0 shadow-sm dark:shadow-md bg-white dark:!bg-[#131418] rounded-xl">
        <div class="flex flex-wrap gap-2">
          <UBadge
            v-for="tag in profile.tags"
            :key="tag"
            color="neutral"
            variant="soft"
            class="rounded-md !bg-gray-100 !text-gray-700 dark:!bg-white/10 dark:!text-gray-300"
          >
            {{ tag }}
          </UBadge>
        </div>
      </UCard>

      <section class="space-y-4">
        <div class="flex items-center gap-2">
          <UIcon name="i-lucide-folder-tree" class="size-5 text-primary-500" />
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">上传网站（文件夹）</h3>
        </div>
        <div class="space-y-3">
          <UCard
            v-for="item in profile.uploadedProjects"
            :key="item.name"
            class="!ring-0 shadow-sm dark:shadow-md bg-white dark:!bg-[#131418] rounded-xl"
          >
            <div class="space-y-2">
              <div class="flex items-center justify-between">
                <h4 class="text-sm font-semibold text-gray-900 dark:text-gray-100">{{ item.name }}</h4>
                <UIcon :name="item.icon" class="size-5 text-primary-500" />
              </div>
              <p class="text-xs leading-6 text-gray-600 dark:text-gray-400 break-all">{{ item.description }}</p>
            </div>
          </UCard>
          <p
            v-if="profile.uploadedProjects.length === 0"
            class="text-sm text-gray-600 dark:text-gray-400"
          >
            暂无上传网站
          </p>
        </div>
      </section>

      <section class="space-y-4">
        <div class="flex items-center gap-2">
          <UIcon name="i-lucide-grid-2x2" class="size-5 text-primary-500" />
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">收藏网站</h3>
        </div>
        <div class="space-y-3">
          <UCard
            v-for="item in profile.favoriteSites"
            :key="item.name"
            class="!ring-0 shadow-sm dark:shadow-md bg-white dark:!bg-[#131418] rounded-xl"
          >
            <div class="space-y-2">
              <div class="flex items-center justify-between">
                <h4 class="text-sm font-semibold text-gray-900 dark:text-gray-100">{{ item.name }}</h4>
                <UIcon :name="item.icon" class="size-5 text-primary-500" />
              </div>
              <p class="text-xs leading-6 text-gray-600 dark:text-gray-400 break-all">{{ item.description }}</p>
            </div>
          </UCard>
          <p
            v-if="profile.favoriteSites.length === 0"
            class="text-sm text-gray-600 dark:text-gray-400"
          >
            暂无收藏网站
          </p>
        </div>
      </section>
    </div>
  </aside>
</template>
