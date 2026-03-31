<script setup lang="ts">
import Tag from 'primevue/tag'
import type { HomeResourceItem } from '@/components/user/home/types'

// 首页资源卡片：仅负责单卡展示
defineProps<{
  item: HomeResourceItem
}>()
</script>

<template>
  <article class="liquid-glass cf-surface-card group flex h-full flex-col rounded-lg p-4 transition-all duration-300 sm:p-6 lg:p-8">
    <div class="mb-6">
      <span class="material-symbols-outlined mb-4 text-4xl text-primary-500">{{ item.icon }}</span>
      <h3 class="mb-2 font-headline text-lg font-bold text-text-primary transition-colors group-hover:text-primary-500 sm:text-xl">
        {{ item.title }}
      </h3>
      <p class="text-sm leading-relaxed text-text-secondary">{{ item.desc }}</p>
    </div>

    <div class="mt-auto">
      <div class="mb-5 flex flex-wrap gap-2">
        <Tag
          unstyled
          :value="item.tag1"
          class="border border-border-default px-2 py-1 text-[10px] font-bold uppercase tracking-wide text-text-secondary"
        />
        <Tag
          unstyled
          :value="item.tag2"
          class="border border-primary-500/40 px-2 py-1 text-[10px] font-bold uppercase tracking-wide text-primary-500"
        />
      </div>

      <div class="flex items-center justify-between border-t border-border-default pt-4">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-xs text-primary-500">person</span>
          <span class="text-[10px] font-bold uppercase tracking-tight text-text-secondary">{{ item.author }}</span>
        </div>
        <div class="flex items-center gap-3 text-text-secondary">
          <div class="flex items-center gap-1">
            <span class="material-symbols-outlined text-xs">star</span>
            <span class="text-[10px]">{{ item.stars }}</span>
          </div>
          <div class="flex items-center gap-1">
            <span class="material-symbols-outlined text-xs">bookmark</span>
            <span class="text-[10px]">{{ item.bookmarks }}</span>
          </div>
        </div>
      </div>
    </div>
  </article>
</template>
