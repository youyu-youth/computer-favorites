<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * Profile 侧边栏（GitHub Stats 风格）：头像 + 编辑入口 + 签名 + 社交链接 + 标签 + 上传 / 收藏文件夹
 */
import { reactive, type Component } from 'vue'
import { DotLottieVue } from '@lottiefiles/dotlottie-vue'
import { useRouter } from 'vue-router'
import {
  BookOpen,
  Folder,
  FolderTree,
  GitBranch,
  Github,
  Globe,
  Link as LinkIcon,
  Mail,
  MapPin,
  Pencil,
  Star,
} from 'lucide-vue-next'
import StatCard from './dashboard/StatCard.vue'
import type { ProfileData } from '@/types/profile'
import type { PublicFolderItem } from '@/api/user-profile-public'
import catPlayingAnimation from '@/assets/animation/Cat playing animation.lottie?url'

withDefaults(
  defineProps<{
    profile: ProfileData
    isOwn?: boolean
    showCollections?: boolean
    /** user-15 公开主页顶层收藏夹列表（本人会看到全部，访客仅看到公开的） */
    publicFolders?: PublicFolderItem[]
    publicFoldersLoading?: boolean
  }>(),
  {
    isOwn: true,
    showCollections: true,
    publicFolders: () => [],
    publicFoldersLoading: false,
  },
)

const emit = defineEmits<{
  (e: 'open-folder', folder: PublicFolderItem): void
  (e: 'view-all'): void
}>()

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

const socialIconMap: Record<string, Component> = {
  GitHub: Github,
  Gitee: GitBranch,
  博客: BookOpen,
  个人网站: Globe,
  邮箱: Mail,
}

const resolveSocialIcon = (label: string): Component => socialIconMap[label] ?? LinkIcon

// 基于标签文本哈希出一个稳定的色相（0~359），让每个标签拥有随机但可复现的背景色
const tagHue = (tag: string): number => {
  let hash = 0
  for (let i = 0; i < tag.length; i++) {
    hash = (hash * 31 + tag.charCodeAt(i)) >>> 0
  }
  return hash % 360
}

/**
 * 记录加载失败的远程 icon URL，渲染时回退为 Folder 图标
 */
const iconErrorMap = reactive<Set<string>>(new Set())
const handleIconError = (url?: string) => {
  if (url) {
    iconErrorMap.add(url)
  }
}
const shouldUseRemoteIcon = (url?: string): boolean => Boolean(url && !iconErrorMap.has(url))

const buildWebsiteHref = (websiteId?: number): string =>
  websiteId ? `/computer/website/${websiteId}` : ''
</script>

<template>
  <aside class="w-full shrink-0 lg:-ml-4 lg:w-[19rem] xl:-ml-6">
    <div class="space-y-3 lg:sticky lg:top-24">
      <StatCard dense>
        <div class="space-y-2">
          <div class="flex items-start gap-3">
            <div
              class="relative aspect-square w-28 shrink-0 overflow-hidden rounded-sm border border-black/5 dark:border-white/[0.06]"
            >
              <img
                :src="profile.avatarUrl"
                :alt="profile.name"
                class="h-full w-full object-cover"
                referrerpolicy="no-referrer"
              />
            </div>

            <div class="flex min-h-28 min-w-0 flex-1 flex-col justify-between">
              <div class="space-y-1.5">
                <h2
                  class="truncate text-base font-semibold tracking-tight text-gray-900 dark:text-white"
                >
                  {{ profile.name }}
                </h2>
                <div
                  class="flex items-center gap-1.5 font-mono text-[11.5px] text-gray-500 dark:text-gray-400"
                >
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
        <ul class="grid grid-cols-4 gap-2">
          <li v-for="link in profile.socialLinks" :key="link.label">
            <button
              type="button"
              :title="link.label"
              :aria-label="link.label"
              class="flex aspect-square w-full cursor-pointer items-center justify-center rounded-md bg-gray-100 transition-colors hover:bg-gray-200 dark:bg-white/[0.06] dark:hover:bg-white/[0.1]"
              @click="handleSocialClick(link.url)"
            >
              <component
                :is="resolveSocialIcon(link.label)"
                class="size-6 shrink-0"
                :stroke-width="2"
                :style="{ color: '#f59e0b' }"
              />
            </button>
          </li>
        </ul>
      </StatCard>

      <StatCard v-if="profile.tags.length" dense title="个人标签">
        <ul class="flex flex-wrap gap-2">
          <li
            v-for="tag in profile.tags"
            :key="tag"
            class="profile-tag rounded-full px-3 py-1 text-[12.5px] font-medium italic tracking-wide"
            :style="{ '--tag-hue': tagHue(tag).toString() }"
          >
            {{ tag }}
          </li>
        </ul>
      </StatCard>

      <StatCard dense>
        <template #header>
          <div class="flex w-full items-center justify-between gap-2">
            <span
              class="flex items-center gap-1.5 text-[12.5px] font-semibold tracking-tight text-gray-900 dark:text-gray-100"
            >
              <FolderTree class="size-3.5 text-amber-500" :stroke-width="2" />
              上传网站（文件夹）
            </span>
            <span class="font-mono text-[10.5px] text-gray-500 dark:text-gray-400">
              {{ profile.uploadedProjects.length >= 5 ? '5+' : profile.uploadedProjects.length }}
              个网站
            </span>
          </div>
        </template>
        <ul class="space-y-1.5">
          <li v-for="item in profile.uploadedProjects" :key="item.websiteId ?? item.name">
            <component
              :is="item.websiteId ? 'a' : 'div'"
              :href="item.websiteId ? buildWebsiteHref(item.websiteId) : undefined"
              :target="item.websiteId ? '_blank' : undefined"
              :rel="item.websiteId ? 'noopener noreferrer' : undefined"
              :title="item.name"
              class="flex items-start gap-1.5 rounded-sm border border-black/5 bg-black/[0.02] p-1.5 transition-colors dark:border-white/[0.06] dark:bg-white/[0.02]"
              :class="
                item.websiteId
                  ? 'cursor-pointer hover:border-amber-500/30 hover:bg-amber-500/5 dark:hover:border-amber-400/30 dark:hover:bg-amber-400/5'
                  : ''
              "
            >
              <img
                v-if="shouldUseRemoteIcon(item.iconUrl)"
                :src="item.iconUrl"
                :alt="item.name"
                class="mt-0.5 size-3.5 shrink-0 rounded-[2px] object-cover"
                referrerpolicy="no-referrer"
                @error="handleIconError(item.iconUrl)"
              />
              <Folder v-else class="mt-0.5 size-3.5 shrink-0 text-amber-500" :stroke-width="2" />
              <div class="min-w-0 flex-1">
                <p class="truncate text-[12.5px] font-semibold text-gray-900 dark:text-gray-100">
                  {{ item.name }}
                </p>
                <p
                  class="break-all font-mono text-[11px] leading-5 text-gray-500 dark:text-gray-400"
                >
                  {{ item.description }}
                </p>
              </div>
            </component>
          </li>
          <li
            v-if="!profile.uploadedProjects.length"
            class="font-mono text-[12px] text-gray-500 dark:text-gray-400"
          >
            暂无上传网站
          </li>
        </ul>
        <router-link
          v-if="isOwn && profile.uploadedProjects.length"
          :to="{ name: 'websitesUpload' }"
          class="mt-2 inline-flex items-center gap-1 font-mono text-[11px] text-amber-600 transition-colors hover:text-amber-700 dark:text-amber-400 dark:hover:text-amber-300"
        >
          查看全部 →
        </router-link>
      </StatCard>

      <StatCard v-if="isOwn || showCollections" dense>
        <template #header>
          <div class="flex w-full items-center justify-between gap-2">
            <span
              class="flex items-center gap-1.5 text-[12.5px] font-semibold tracking-tight text-gray-900 dark:text-gray-100"
            >
              <FolderTree class="size-3.5 text-amber-500" :stroke-width="2" />
              {{ isOwn ? '收藏夹' : '公开收藏夹' }}
            </span>
            <span class="font-mono text-[10.5px] text-gray-500 dark:text-gray-400">
              {{ publicFolders.length }} 个
            </span>
          </div>
        </template>

        <ul v-if="publicFoldersLoading" class="space-y-1.5">
          <li
            v-for="i in 3"
            :key="i"
            class="h-9 animate-pulse rounded-sm bg-black/[0.04] dark:bg-white/[0.04]"
          />
        </ul>

        <ul v-else-if="publicFolders.length" class="space-y-1.5">
          <li v-for="folder in publicFolders" :key="folder.id">
            <button
              type="button"
              class="group flex w-full cursor-pointer items-start gap-1.5 rounded-sm border border-black/5 bg-black/[0.02] p-1.5 text-left transition hover:border-amber-300 hover:bg-amber-50/60 dark:border-white/[0.06] dark:bg-white/[0.02] dark:hover:border-amber-500/40 dark:hover:bg-amber-950/20"
              @click="emit('open-folder', folder)"
            >
              <Folder class="mt-0.5 size-3.5 shrink-0 text-amber-500" :stroke-width="2" />
              <div class="min-w-0 flex-1">
                <p class="truncate text-[12.5px] font-semibold text-gray-900 dark:text-gray-100">
                  {{ folder.name }}
                </p>
                <p class="font-mono text-[10.5px] leading-5 text-gray-500 dark:text-gray-400">
                  {{ folder.websiteCount }} 个网站<span v-if="folder.childrenCount > 0">
                    · {{ folder.childrenCount }} 个子夹</span
                  >
                </p>
              </div>
            </button>
          </li>
        </ul>

        <p v-else class="font-mono text-[12px] leading-6 text-gray-500 dark:text-gray-400">
          {{ isOwn ? '暂无公开收藏夹，可在收藏夹页设为公开' : '该用户未公开收藏夹' }}
        </p>

        <button
          v-if="publicFolders.length"
          type="button"
          class="mt-2 inline-flex cursor-pointer items-center gap-1 font-mono text-[11px] text-amber-600 transition-colors hover:text-amber-700 dark:text-amber-400 dark:hover:text-amber-300"
          @click="emit('view-all')"
        >
          查看全部 →
        </button>
      </StatCard>
    </div>
  </aside>
</template>

<style scoped>
.profile-tag {
  font-family:
    'Caveat', 'Segoe Script', 'Comic Sans MS', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
    system-ui, sans-serif;
  background-color: hsla(var(--tag-hue), 75%, 55%, 0.12);
  color: hsla(var(--tag-hue), 65%, 32%, 1);
  transition:
    background-color 200ms ease,
    color 200ms ease;
}

.profile-tag:hover {
  background-color: hsla(var(--tag-hue), 75%, 55%, 0.2);
}

:global(.dark) .profile-tag {
  background-color: hsla(var(--tag-hue), 70%, 65%, 0.18);
  color: hsla(var(--tag-hue), 90%, 82%, 1);
}

:global(.dark) .profile-tag:hover {
  background-color: hsla(var(--tag-hue), 70%, 65%, 0.28);
}
</style>
