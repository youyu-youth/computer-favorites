<script setup lang="ts">
import PrimeBreadcrumb from 'primevue/breadcrumb'
import { useRouter } from 'vue-router'
import type { RouteLocationRaw } from 'vue-router'

type UIBreadcrumbItem = {
  key?: string
  label: string
  level?: number
  current?: boolean
  clickable?: boolean
  to?: RouteLocationRaw
  onClick?: () => void | Promise<void>
}

const props = withDefaults(
  defineProps<{
    items: UIBreadcrumbItem[]
    className?: string
  }>(),
  {
    className: '',
  },
)

const router = useRouter()

const toUIBreadcrumbItem = (item: unknown): UIBreadcrumbItem => {
  const source = (item as UIBreadcrumbItem) || {}
  return {
    key: source.key,
    label: source.label || '',
    level: source.level,
    current: source.current,
    clickable: source.clickable,
    to: source.to,
    onClick: source.onClick,
  }
}

const handleItemClick = async (item: unknown): Promise<void> => {
  const currentItem = toUIBreadcrumbItem(item)
  if (currentItem.clickable === false) {
    return
  }
  if (currentItem.onClick) {
    await Promise.resolve(currentItem.onClick())
    return
  }
  if (currentItem.to) {
    try {
      await router.push(currentItem.to)
    } catch (error) {
      // 同路由重复跳转等异常在此忽略，避免干扰交互流程。
    }
  }
}
</script>

<template>
  <nav
    class="menu-scroll overflow-x-auto whitespace-nowrap"
    :class="className"
    aria-label="breadcrumb"
    data-testid="admin-breadcrumb"
  >
    <PrimeBreadcrumb
      :model="items"
      :pt="{
        root: { class: 'border-0 bg-transparent p-0' },
        list: {
          class: 'flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400',
        },
        separator: { class: 'm-0 p-0' },
      }"
    >
      <template #item="{ item }">
        <button
          type="button"
          class="cursor-pointer rounded-sm px-0.5 font-medium transition-colors hover:text-[#e95322] focus:outline-none focus-visible:ring-2 focus-visible:ring-[#e95322]/60"
          :class="
            item.current ? 'text-gray-900 dark:text-gray-100' : 'text-gray-600 dark:text-gray-300'
          "
          :aria-current="item.current ? 'page' : undefined"
          :data-breadcrumb-key="item.key"
          :data-breadcrumb-level="item.level"
          @click="handleItemClick(item)"
        >
          {{ item.label }}
        </button>
      </template>

      <template #separator>
        <i
          class="fas fa-chevron-right text-xs text-gray-400 dark:text-gray-500"
          aria-hidden="true"
        ></i>
      </template>
    </PrimeBreadcrumb>
  </nav>
</template>

<style scoped>
.menu-scroll {
  scrollbar-width: none;
}

.menu-scroll::-webkit-scrollbar {
  display: none;
}
</style>
