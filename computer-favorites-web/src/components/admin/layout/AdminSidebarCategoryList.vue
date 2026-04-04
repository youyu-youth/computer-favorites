<script setup lang="ts">
import type { WebsiteCategoryNavItem } from '@/stores/adminNav'

type Props = {
  categories: WebsiteCategoryNavItem[]
  selectedTreeKey: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (event: 'select', categoryId: number): void
}>()

const formatCount = (count: number): string => {
  return new Intl.NumberFormat('en-US').format(count)
}

const isCategoryActive = (categoryId: number): boolean => {
  return (
    props.selectedTreeKey === `category:${categoryId}` ||
    (props.selectedTreeKey === 'menu:websites' && categoryId === 0)
  )
}

const resolveCategoryIcon = (name: string, isActive: boolean) => {
  const n = name.toLowerCase()
  const activeColor = 'text-[#e95322]'
  const inactiveColor = 'text-gray-400 dark:text-gray-500'
  const colorClass = isActive ? activeColor : inactiveColor

  if (n.includes('python')) return { icon: 'fab fa-python', color: colorClass }
  if (n.includes('typescript') || n.includes('javascript'))
    return { icon: 'fab fa-js', color: colorClass }
  if (n.includes('developer tools') || n.includes('tools') || n.includes('工具'))
    return { icon: 'fas fa-hammer', color: colorClass }
  if (n.includes('claimed')) return { icon: 'fas fa-check-circle', color: colorClass }
  if (n.includes('automation') || n.includes('自动化'))
    return { icon: 'fas fa-dharmachakra', color: colorClass }
  if (n.includes('search') || n.includes('搜索'))
    return { icon: 'fas fa-search', color: colorClass }
  if (n.includes('database') || n.includes('数据库'))
    return { icon: 'fas fa-database', color: colorClass }
  if (
    n.includes('rag') ||
    n.includes('knowledge') ||
    n.includes('documentation') ||
    n.includes('知识')
  )
    return { icon: 'fas fa-book', color: colorClass }
  if (n.includes('agent') || n.includes('orchestration') || n.includes('智能体'))
    return { icon: 'fas fa-robot', color: colorClass }
  if (n.includes('code execution') || n.includes('代码'))
    return { icon: 'fas fa-code', color: colorClass }
  if (n.includes('hybrid') || n.includes('混合')) return { icon: 'fas fa-link', color: colorClass }

  return { icon: 'fas fa-hashtag', color: colorClass }
}
</script>

<template>
  <div class="space-y-0.5 pb-4">
    <div
      v-if="categories.length === 0"
      class="rounded-md border border-dashed border-gray-200 dark:border-dark-border/60 px-3 py-4 text-center"
    >
      <p class="text-sm text-gray-600 dark:text-gray-300">未找到匹配分类</p>
      <p class="mt-1 text-xs text-gray-400 dark:text-gray-500">请尝试其他关键词或清空搜索</p>
    </div>

    <template v-else>
      <button
        v-for="cat in categories"
        :key="cat.id"
        type="button"
        @click="emit('select', cat.id)"
        class="group w-full flex items-center justify-between px-2.5 py-1.5 rounded-md cursor-pointer transition-colors border-l-[3px]"
        :class="
          isCategoryActive(cat.id)
            ? 'border-[#e95322] bg-orange-50 dark:bg-[#161b22]'
            : 'border-transparent hover:bg-gray-100 dark:hover:bg-[#161b22]'
        "
      >
        <div class="flex items-center gap-2.5 min-w-0">
          <i
            :class="[
              resolveCategoryIcon(cat.name, isCategoryActive(cat.id)).icon,
              resolveCategoryIcon(cat.name, isCategoryActive(cat.id)).color,
            ]"
            class="text-[13px] w-4 text-center"
          ></i>
          <span
            class="text-[14px] truncate tracking-wide text-left"
            :class="
              isCategoryActive(cat.id)
                ? 'font-medium text-gray-900 dark:text-white'
                : 'text-gray-600 dark:text-[#8b949e] group-hover:text-gray-900 dark:group-hover:text-gray-200'
            "
          >
            {{ cat.name }}
          </span>
        </div>
        <span
          class="text-[13px] ml-3 tracking-wide"
          :class="
            isCategoryActive(cat.id)
              ? 'text-gray-700 dark:text-gray-300 font-medium'
              : 'text-gray-400 dark:text-[#8b949e]'
          "
        >
          {{ formatCount(cat.count) }}
        </span>
      </button>
    </template>
  </div>
</template>
