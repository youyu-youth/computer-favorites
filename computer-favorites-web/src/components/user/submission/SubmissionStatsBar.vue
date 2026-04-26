<script setup lang="ts">
import type { Component } from 'vue'

interface StatsItem {
  id: number | string
  label: string
  count: number
  icon: Component
  iconBgClass: string
  iconColorClass: string
  active: boolean
}

defineProps<{
  items: StatsItem[]
}>()

const emit = defineEmits<{
  (e: 'select', id: number | string): void
}>()
</script>

<template>
  <div
    class="-mx-4 flex gap-4 overflow-x-auto px-4 pb-2 sm:mx-0 sm:px-0"
    style="-ms-overflow-style: none; scrollbar-width: none"
  >
    <div
      v-for="item in items"
      :key="item.id"
      :class="[
        'min-w-[140px] flex-1 cursor-pointer rounded-xl bg-white/50 p-4 backdrop-blur-sm transition-all duration-300 hover:-translate-y-0.5 dark:bg-white/[0.03]',
        item.active
          ? 'bg-white/80 dark:bg-white/[0.06]'
          : 'hover:bg-white/70 dark:hover:bg-white/[0.05]',
      ]"
      @click="emit('select', item.id)"
    >
      <div class="flex items-center space-x-3">
        <div
          :class="[
            'flex h-10 w-10 shrink-0 items-center justify-center rounded-xl',
            item.iconBgClass,
          ]"
        >
          <component :is="item.icon" :class="['h-5 w-5', item.iconColorClass]" />
        </div>
        <div>
          <div
            class="text-2xl font-bold leading-tight text-gray-800 dark:text-white"
          >
            {{ item.count }}
          </div>
          <div class="text-xs text-gray-500 dark:text-gray-400">
            {{ item.label }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
