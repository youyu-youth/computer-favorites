<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { getWebsiteDetail } from '@/api/website'
import websiteClickIcon from '@/assets/icons/svg/user-website-click.svg'
import websiteCollectionIcon from '@/assets/icons/svg/user-website-collection.svg'
import websiteLikeIcon from '@/assets/icons/svg/user-website-like.svg'
import { buildTagColorStyle, normalizeTagColor } from '@/utils/tag-color'

const props = defineProps<{
  visible: boolean
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
  targetRect: DOMRect | null
}>()

const popoverRef = ref<HTMLElement | null>(null)
const popoverPosition = ref({ top: '0px', left: '0px' })
const isDetailLoading = ref(false)
const detailTags = ref<Array<{ id: number; label: string; color: string }>>([])
const hasDetailLoaded = ref(false)
let detailRequestId = 0

const displayTags = computed(() => {
  if (hasDetailLoaded.value) {
    return detailTags.value
  }
  return props.item.tags
})

const hiddenTagCount = computed(() => {
  return Math.max(0, displayTags.value.length - 3)
})

const normalizeTags = (
  tags: Array<{ id: number; name: string; color?: string }> | undefined,
): Array<{ id: number; label: string; color: string }> => {
  if (!Array.isArray(tags)) {
    return []
  }
  const normalizedTagMap = new Map<number, { id: number; label: string; color: string }>()
  tags.forEach((tag) => {
    const tagId = Number(tag?.id)
    const tagName = String(tag?.name || '').trim()
    if (!Number.isInteger(tagId) || tagId <= 0 || !tagName) {
      return
    }
    if (normalizedTagMap.has(tagId)) {
      return
    }
    normalizedTagMap.set(tagId, {
      id: tagId,
      label: tagName,
      color: normalizeTagColor(tag.color),
    })
  })
  return Array.from(normalizedTagMap.values())
}

const loadWebsiteDetailTags = async () => {
  const requestId = ++detailRequestId
  isDetailLoading.value = true
  hasDetailLoaded.value = false
  try {
    const detail = await getWebsiteDetail(props.item.id)
    if (requestId !== detailRequestId) {
      return
    }
    detailTags.value = normalizeTags(detail.tags)
    hasDetailLoaded.value = true
  } catch {
    if (requestId !== detailRequestId) {
      return
    }
    detailTags.value = []
    hasDetailLoaded.value = false
  } finally {
    if (requestId === detailRequestId) {
      isDetailLoading.value = false
    }
  }
}

watch(
  () => props.item.id,
  () => {
    detailRequestId += 1
    detailTags.value = []
    hasDetailLoaded.value = false
    isDetailLoading.value = false
  },
  { immediate: true },
)

watch(
  () => props.visible,
  async (newVal) => {
    if (newVal && props.targetRect) {
      void loadWebsiteDetailTags()
      await nextTick()
      if (popoverRef.value) {
        const pRect = popoverRef.value.getBoundingClientRect()
        const margin = 16

        // 默认浮层显示在卡片上方
        let top = props.targetRect.top - pRect.height - margin

        // 水平居中对齐卡片
        let left = props.targetRect.left + props.targetRect.width / 2 - pRect.width / 2

        // 如果上方空间不足则改为显示在下方
        if (top < margin) {
          top = props.targetRect.bottom + margin
        }

        // 限制在可视区域内，避免左右溢出
        if (left < margin) {
          left = margin
        } else if (left + pRect.width > window.innerWidth - margin) {
          left = window.innerWidth - pRect.width - margin
        }

        popoverPosition.value = {
          top: `${top}px`,
          left: `${left}px`,
        }
      }
    }
  },
)
</script>

<template>
  <Teleport to="body">
    <Transition name="popover">
      <div
        v-if="visible && targetRect"
        ref="popoverRef"
        class="fixed z-[9999] w-[320px] sm:w-[380px] p-5 bg-white/95 dark:bg-[#0a0a0a]/95 backdrop-blur-xl border border-gray-200 dark:border-gray-800 shadow-2xl dark:shadow-[0_10px_40px_rgba(0,0,0,0.8)] rounded-xl pointer-events-none flex flex-col gap-4 font-mono transition-colors"
        :style="popoverPosition"
      >
        <!-- 头部信息：图标、标题、评分 -->
        <div class="flex items-start gap-4">
          <div
            class="w-12 h-12 rounded-lg bg-gray-100 dark:bg-white/5 flex items-center justify-center flex-shrink-0 overflow-hidden border border-gray-200 dark:border-[#1f1f1f]"
          >
            <img
              v-if="item.icon"
              :src="item.icon"
              :alt="item.name"
              class="w-full h-full object-cover"
            />
            <span v-else class="text-xl font-bold text-gray-400 dark:text-gray-500">{{
              item.name.charAt(0).toUpperCase()
            }}</span>
          </div>
          <div class="flex-1 min-w-0">
            <h3 class="text-lg font-bold text-gray-900 dark:text-white truncate">
              {{ item.name }}
            </h3>
            <span
              class="text-xs text-primary-500 dark:text-primary-400 truncate block mt-0.5 line-clamp-1 break-all mr-2"
            >
              {{ item.url }}
            </span>
          </div>
          <div class="flex flex-col items-end flex-shrink-0">
            <span
              class="flex items-center justify-center bg-orange-100 dark:bg-amber-500/20 text-orange-600 dark:text-amber-400 px-2 py-1 rounded-md text-lg font-bold"
            >
              {{ item.score }}
            </span>
            <span class="text-[10px] text-gray-400 dark:text-gray-500 mt-1">评分</span>
          </div>
        </div>

        <!-- 描述信息 -->
        <div
          class="text-sm text-gray-600 dark:text-gray-400 leading-relaxed border-y border-gray-100 dark:border-gray-800/60 py-3"
        >
          {{ item.desc || '暂无详细介绍' }}
        </div>

        <!-- 底部信息：指标与标签 -->
        <div class="flex flex-col gap-3">
          <div class="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
            <span class="flex items-center gap-1">
              <img
                :src="websiteClickIcon"
                alt=""
                aria-hidden="true"
                class="w-3.5 h-3.5 opacity-70"
              />
              {{ item.downloads }} 浏览
            </span>
            <span class="flex items-center gap-1">
              <img
                :src="websiteLikeIcon"
                alt=""
                aria-hidden="true"
                class="w-3.5 h-3.5 opacity-70"
              />
              {{ item.stars }} 点赞
            </span>
            <span class="flex items-center gap-1">
              <img
                :src="websiteCollectionIcon"
                alt=""
                aria-hidden="true"
                class="w-3.5 h-3.5 opacity-70"
              />
              {{ item.collections }} 收藏
            </span>
          </div>

          <div class="flex flex-wrap gap-1.5 mt-1">
            <span
              class="px-2 py-0.5 bg-green-100 dark:bg-[#104d39]/40 text-green-700 dark:text-gray-300 text-[10px] rounded-sm"
            >
              {{ item.category }}
            </span>
            <span
              v-for="tag in displayTags.slice(0, 3)"
              :key="`${tag.id}-${tag.label}`"
              class="px-2 py-0.5 text-[10px] rounded-sm border-l-2"
              :style="buildTagColorStyle(tag.color)"
            >
              {{ tag.label }}
            </span>
            <span
              v-if="hiddenTagCount > 0"
              class="px-2 py-0.5 bg-gray-100 dark:bg-white/10 text-[10px] text-gray-500 dark:text-gray-400 rounded-sm"
            >
              +{{ hiddenTagCount }}
            </span>
            <span
              v-if="isDetailLoading"
              class="px-2 py-0.5 bg-gray-100 dark:bg-white/10 text-[10px] text-gray-500 dark:text-gray-400 rounded-sm"
            >
              标签同步中
            </span>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.popover-enter-active,
.popover-leave-active {
  transition:
    opacity 0.25s cubic-bezier(0.4, 0, 0.2, 1),
    transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.popover-enter-from,
.popover-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(10px);
}
</style>
