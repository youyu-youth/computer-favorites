<script setup lang="ts">
import { computed } from 'vue'
import Button from 'primevue/button'
import HomeResourceCard from '@/components/user/home/HomeResourceCard.vue'
import type { HomeResourceItem } from '@/components/user/home/types'

// 首页资源网格与分页容器
const props = withDefaults(defineProps<{
  items: HomeResourceItem[]
  currentPage: number
  totalPages: number
}>(), {
  items: () => [],
  currentPage: 1,
  totalPages: 1,
})

const emit = defineEmits<{
  (e: 'page-change', page: number): void
}>()

const canPrev = computed(() => props.currentPage > 1)
const canNext = computed(() => props.currentPage < props.totalPages)

const visiblePages = computed(() => {
  const total = props.totalPages
  const current = props.currentPage

  if (total <= 5) {
    return Array.from({ length: total }, (_, index) => index + 1)
  }

  if (current <= 3) {
    return [1, 2, 3, -1, total]
  }

  if (current >= total - 2) {
    return [1, -1, total - 2, total - 1, total]
  }

  return [1, -1, current - 1, current, current + 1, -1, total]
})

const goPage = (page: number) => {
  if (page < 1 || page > props.totalPages || page === props.currentPage) {
    return
  }
  emit('page-change', page)
}

const formatPage = (page: number) => String(page).padStart(2, '0')
</script>

<template>
  <section class="mt-8 pb-20 sm:pb-24 lg:pb-32">
    <div class="mb-10 grid grid-cols-1 gap-4 sm:mb-12 sm:grid-cols-2 sm:gap-5 lg:mb-16 lg:grid-cols-3 lg:gap-6">
      <HomeResourceCard v-for="item in items" :key="item.id" :item="item" />
    </div>

    <div class="flex items-center justify-center gap-2">
      <Button
        unstyled
        class="flex h-10 w-10 cursor-pointer items-center justify-center rounded-sm border border-border-default text-text-secondary transition-colors hover:border-text-primary hover:text-text-primary disabled:cursor-not-allowed disabled:opacity-50"
        :disabled="!canPrev"
        @click="goPage(currentPage - 1)"
      >
        <span class="material-symbols-outlined">chevron_left</span>
      </Button>

      <template v-for="(page, index) in visiblePages" :key="`${page}-${index}`">
        <span v-if="page === -1" class="px-2 text-text-secondary">...</span>
        <Button
          v-else
          unstyled
          :class="[
            'flex h-10 min-w-10 cursor-pointer items-center justify-center rounded-sm border px-2 font-mono text-sm transition-colors',
            page === currentPage
              ? 'border-primary-500 bg-primary-500/10 text-primary-500'
              : 'border-border-default text-text-primary hover:border-text-primary',
          ]"
          @click="goPage(page)"
        >
          {{ formatPage(page) }}
        </Button>
      </template>

      <Button
        unstyled
        class="flex h-10 w-10 cursor-pointer items-center justify-center rounded-sm border border-border-default text-text-secondary transition-colors hover:border-text-primary hover:text-text-primary disabled:cursor-not-allowed disabled:opacity-50"
        :disabled="!canNext"
        @click="goPage(currentPage + 1)"
      >
        <span class="material-symbols-outlined">chevron_right</span>
      </Button>
    </div>
  </section>
</template>
