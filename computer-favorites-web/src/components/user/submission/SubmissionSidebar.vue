<script setup lang="ts">
import type { Component } from 'vue'
import { X, ArrowRight } from 'lucide-vue-next'
import Drawer from 'primevue/drawer'

interface NavItem {
  id: number | string
  name: string
  count: number
  icon: Component
  active: boolean
}

interface CategoryItem {
  id: number | string
  name: string
  count: number
  icon?: Component
}

defineProps<{
  mainNav: NavItem[]
  categories: CategoryItem[]
  activeNavId: number | string
  activeCategoryId: number | string
  mobileOpen: boolean
}>()

const emit = defineEmits<{
  (e: 'update:activeNavId', id: number | string): void
  (e: 'update:activeCategoryId', id: number | string): void
  (e: 'update:mobileOpen', value: boolean): void
}>()
</script>

<template>
  <aside
    class="fixed inset-y-0 left-0 z-50 hidden w-52 shrink-0 flex-col overflow-y-auto rounded-2xl bg-white/60 shadow-none backdrop-blur-xl dark:bg-[#0c0c0c]/60 md:relative md:z-auto md:m-4 md:flex md:h-[calc(100vh-2rem)]"
  >
    <div
      class="flex h-12 shrink-0 items-center px-4"
    >
      <span
        class="text-base font-bold tracking-wide text-gray-900 dark:text-white"
      >
        我的投稿网站
      </span>
    </div>

    <div class="flex-1 overflow-y-auto px-3 py-4">
      <nav class="mb-6 space-y-1">
        <button
          v-for="item in mainNav"
          :key="item.id"
          :class="[
            'flex w-full cursor-pointer items-center justify-between rounded-lg border-0 bg-transparent px-3 py-2 text-left text-sm transition-colors',
            item.active
              ? 'bg-primary-50 font-medium text-primary-500 dark:bg-primary-500/10 dark:text-primary-400'
              : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-white/5',
          ]"
          @click="emit('update:activeNavId', item.id)"
        >
          <div class="flex items-center">
            <component
              :is="item.icon"
              :class="[
                'mr-3 h-[18px] w-[18px]',
                item.active
                  ? 'text-primary-500'
                  : 'text-gray-500 group-hover:text-gray-800 dark:text-gray-400',
              ]"
            />
            {{ item.name }}
          </div>
          <span
            :class="[
              'text-xs font-semibold',
              item.active
                ? 'text-primary-500'
                : 'text-gray-400 dark:text-gray-500',
            ]"
          >
            {{ item.count }}
          </span>
        </button>
      </nav>

      <div>
        <h3
          class="mb-3 px-3 text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500"
        >
          类别
        </h3>
        <nav class="space-y-1">
          <button
            v-for="cat in categories"
            :key="cat.id"
            :class="[
              'flex w-full cursor-pointer items-center justify-between rounded-lg border-0 bg-transparent px-3 py-2 text-left text-sm transition-colors',
              String(activeCategoryId) === String(cat.id)
                ? 'bg-gray-100 font-medium text-gray-900 dark:bg-white/5 dark:text-white'
                : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-white/5',
            ]"
            @click="emit('update:activeCategoryId', cat.id)"
          >
            <div class="flex items-center">
              <component
                v-if="cat.icon"
                :is="cat.icon"
                class="mr-3 h-[18px] w-[18px] text-gray-500 dark:text-gray-400"
              />
              {{ cat.name }}
            </div>
            <span class="text-xs text-gray-400 dark:text-gray-500">
              {{ cat.count }}
            </span>
          </button>
        </nav>
      </div>
    </div>

    <div class="mt-auto p-3">
      <div
        class="rounded-xl bg-gray-100/80 p-4 transition-colors dark:bg-white/[0.03]"
      >
        <div class="mb-2 flex items-center font-medium text-primary-500">
          <span class="mr-2 text-lg">&#9733;</span>
          提升通过率
        </div>
        <p
          class="mb-3 text-xs leading-relaxed text-gray-500 dark:text-gray-400"
        >
          完善网站信息，上传清晰的截图可提升审核通过率
        </p>
        <button
          class="flex cursor-pointer items-center border-0 bg-transparent p-0 text-xs text-primary-500 transition-colors hover:text-primary-600"
        >
          查看攻略
          <ArrowRight class="ml-1 h-3 w-3" />
        </button>
      </div>
    </div>
  </aside>

  <Drawer
    :visible="mobileOpen"
    @update:visible="emit('update:mobileOpen', $event)"
    position="left"
    :pt="{
      mask: { class: 'fixed inset-0 z-[100] bg-black/60 backdrop-blur-sm' },
      root: { class: 'flex flex-col h-full w-[280px] bg-white/95 backdrop-bl-xl dark:bg-[#0a0a0a]/95' },
      header: { class: 'flex items-center justify-between px-6 py-4 shrink-0' },
      title: { class: 'text-lg font-bold text-gray-900 dark:text-white' },
      closeButton: { class: 'hidden' },
      content: { class: 'p-0 overflow-y-auto flex-1' },
    }"
  >
    <template #header>
      <span class="text-lg font-bold text-gray-900 dark:text-white">
        我的投稿网站
      </span>
      <button
        class="inline-flex h-8 w-8 cursor-pointer items-center justify-center rounded-lg border-0 bg-transparent p-0 text-gray-500 transition-colors hover:text-gray-800 dark:hover:text-white"
        @click="emit('update:mobileOpen', false)"
      >
        <X class="h-5 w-5" />
      </button>
    </template>

    <div class="px-4 py-6">
      <nav class="mb-8 space-y-1">
        <button
          v-for="item in mainNav"
          :key="'m-' + item.id"
          :class="[
            'flex w-full cursor-pointer items-center justify-between rounded-lg border-0 bg-transparent px-3 py-2 text-left text-sm transition-colors',
            item.active
              ? 'bg-primary-50 font-medium text-primary-500 dark:bg-primary-500/10 dark:text-primary-400'
              : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-white/5',
          ]"
          @click="
            emit('update:activeNavId', item.id);
            emit('update:mobileOpen', false);
          "
        >
          <div class="flex items-center">
            <component
              :is="item.icon"
              :class="[
                'mr-3 h-[18px] w-[18px]',
                item.active
                  ? 'text-primary-500'
                  : 'text-gray-500 dark:text-gray-400',
              ]"
            />
            {{ item.name }}
          </div>
          <span
            :class="[
              'text-xs font-semibold',
              item.active
                ? 'text-primary-500'
                : 'text-gray-400 dark:text-gray-500',
            ]"
          >
            {{ item.count }}
          </span>
        </button>
      </nav>

      <div>
        <h3
          class="mb-3 px-3 text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500"
        >
          类别
        </h3>
        <nav class="space-y-1">
          <button
            v-for="cat in categories"
            :key="'mc-' + cat.id"
            :class="[
              'flex w-full cursor-pointer items-center justify-between rounded-lg border-0 bg-transparent px-3 py-2 text-left text-sm transition-colors',
              String(activeCategoryId) === String(cat.id)
                ? 'bg-gray-100 font-medium text-gray-900 dark:bg-white/5 dark:text-white'
                : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-white/5',
            ]"
            @click="
              emit('update:activeCategoryId', cat.id);
              emit('update:mobileOpen', false);
            "
          >
            <div class="flex items-center">
              <component
                v-if="cat.icon"
                :is="cat.icon"
                class="mr-3 h-[18px] w-[18px] text-gray-500 dark:text-gray-400"
              />
              {{ cat.name }}
            </div>
            <span class="text-xs text-gray-400 dark:text-gray-500">
              {{ cat.count }}
            </span>
          </button>
        </nav>
      </div>
    </div>
  </Drawer>
</template>
