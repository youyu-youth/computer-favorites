<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import websiteClickIcon from '@/assets/icons/svg/user-website-click.svg'
import websiteCollectionIcon from '@/assets/icons/svg/user-website-collection.svg'
import websiteLikeIcon from '@/assets/icons/svg/user-website-like.svg'
import WebsiteDetailPopover from '@/components/user/WebsiteDetailPopover.vue'
import { buildTagColorStyle } from '@/utils/tag-color'

const props = defineProps<{
  item: {
    id: number
    name: string
    desc: string
    downloads: string | number
    stars: string | number
    collections: string | number
    category: string
    url: string
    icon: string
    score: string | number
    tags: Array<{ id: number; label: string; color: string }>
  }
  rankIndex: number | string
}>()

const router = useRouter()

const cardRef = ref<HTMLElement | null>(null)
const showPopover = ref(false)
const popoverTargetRect = ref<DOMRect | null>(null)
let hoverTimer: number | undefined

const goDetail = () => {
  const websiteId = Number(props.item.id)
  if (!Number.isInteger(websiteId) || websiteId <= 0) {
    return
  }
  void router.push({
    name: 'websiteDetail',
    params: { id: String(websiteId) },
  })
}

const handleCardKeydown = (event: KeyboardEvent) => {
  if (event.key !== 'Enter' && event.key !== ' ') {
    return
  }
  event.preventDefault()
  goDetail()
}

const handleInteraction = () => {
  if (hoverTimer) {
    window.clearTimeout(hoverTimer)
  }
  showPopover.value = false
}

const handleMouseEnter = () => {
  // 悬停 2 秒后再展示详情浮层，减少误触发
  hoverTimer = window.setTimeout(() => {
    if (cardRef.value) {
      popoverTargetRect.value = cardRef.value.getBoundingClientRect()
      showPopover.value = true

      // 浮层显示后，监听一次触摸/滚动以便自动收起
      window.addEventListener('touchstart', handleInteraction, { passive: true, once: true })
      window.addEventListener('scroll', handleInteraction, { passive: true, once: true })
    }
  }, 2000)
}

const handleMouseLeave = () => {
  handleInteraction()
  window.removeEventListener('touchstart', handleInteraction)
  window.removeEventListener('scroll', handleInteraction)
}

onUnmounted(() => {
  handleInteraction()
  window.removeEventListener('touchstart', handleInteraction)
  window.removeEventListener('scroll', handleInteraction)
})
</script>

<template>
  <div>
    <article
      ref="cardRef"
      role="button"
      tabindex="0"
      class="group relative block cursor-pointer p-5 transition-all duration-200 hover:-translate-y-0.5 bg-white hover:bg-gray-50 dark:bg-[#062016]/60 dark:hover:bg-[#0a3324] border border-gray-200 dark:border-transparent backdrop-blur-sm overflow-hidden"
      @mouseenter="handleMouseEnter"
      @mouseleave="handleMouseLeave"
      @click="goDetail"
      @keydown="handleCardKeydown"
    >
      <div class="flex gap-4">
        <div
          class="text-xs font-mono text-gray-400 dark:text-gray-600 mt-1 w-4 text-right flex-shrink-0"
        >
          {{ rankIndex }}
        </div>
        <div class="flex-1 min-w-0">
          <div class="flex items-baseline gap-2 mb-1">
            <h3
              class="text-base sm:text-lg font-bold text-gray-900 dark:text-gray-200 group-hover:text-primary-500 dark:group-hover:text-white transition-colors font-mono truncate"
            >
              {{ item.name }}
            </h3>
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2 leading-relaxed mb-4">
            {{ item.desc }}
          </p>

          <div class="flex flex-wrap gap-2">
            <span
              v-for="tag in item.tags"
              :key="`${tag.id}-${tag.label}`"
              class="inline-flex items-center gap-1 px-2 py-0.5 text-[10px] font-mono rounded-sm border-l-2"
              :style="buildTagColorStyle(tag.color)"
            >
              {{ tag.label }}
            </span>
          </div>
        </div>

        <div class="flex flex-col items-end gap-2 text-xs font-mono flex-shrink-0">
          <div
            class="flex flex-col sm:flex-row items-end sm:items-center gap-1 sm:gap-3 text-gray-500 dark:text-gray-300"
          >
            <span class="inline-flex items-center justify-end gap-1 sm:w-[76px]">
              <img
                :src="websiteClickIcon"
                alt=""
                aria-hidden="true"
                class="w-3.5 h-3.5 opacity-80"
              />
              <span class="font-bold group-hover:text-primary-500 transition-colors text-right">{{
                item.downloads
              }}</span>
            </span>
            <span class="inline-flex items-center justify-end gap-1 sm:w-[68px]">
              <img
                :src="websiteLikeIcon"
                alt=""
                aria-hidden="true"
                class="w-3.5 h-3.5 opacity-80"
              />
              <span class="font-bold text-right">{{ item.stars }}</span>
            </span>
            <span class="inline-flex items-center justify-end gap-1 sm:w-[68px]">
              <img
                :src="websiteCollectionIcon"
                alt=""
                aria-hidden="true"
                class="w-3.5 h-3.5 opacity-80"
              />
              <span class="font-bold text-right">{{ item.collections }}</span>
            </span>
          </div>
          <div class="mt-auto flex items-center gap-2">
            <span
              class="px-2 py-0.5 bg-green-100 dark:bg-[#104d39]/30 text-[10px] text-green-700 dark:text-gray-500 rounded-sm"
            >
              {{ item.category }}
            </span>
            <a
              :href="item.url"
              target="_blank"
              rel="noopener noreferrer"
              class="px-2 py-0.5 rounded-sm border border-gray-300 text-[10px] text-gray-600 hover:text-primary-500 hover:border-primary-500 dark:border-gray-600 dark:text-gray-300 dark:hover:text-primary-400 dark:hover:border-primary-400"
              @click.stop
            >
              访问官网
            </a>
          </div>
        </div>
      </div>

      <div
        class="absolute bottom-0 left-0 h-[2px] bg-primary-500 w-0 group-hover:w-full transition-all duration-300"
      />
    </article>

    <WebsiteDetailPopover :visible="showPopover" :item="item" :target-rect="popoverTargetRect" />
  </div>
</template>
