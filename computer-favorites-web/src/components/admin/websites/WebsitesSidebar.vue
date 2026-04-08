<script setup lang="ts">
import { computed, shallowRef } from 'vue'
import UInput from '@/components/ui-adapter/UInput.vue'

type WebsiteCategoryItem = {
  id: number
  name: string
  count: number
  active: boolean
}

const props = withDefaults(
  defineProps<{
    categories: WebsiteCategoryItem[]
    showSearch?: boolean
    dense?: boolean
  }>(),
  {
    showSearch: true,
    dense: false,
  },
)

const emit = defineEmits<{
  (e: 'select', categoryId: number): void
}>()

const keyword = shallowRef('')

const normalizedKeyword = computed(() => {
  return keyword.value.trim().toLowerCase()
})

const filteredCategories = computed(() => {
  if (!normalizedKeyword.value) {
    return props.categories
  }
  return props.categories.filter((category) => {
    return category.name.toLowerCase().includes(normalizedKeyword.value)
  })
})

const formatCount = (count: number): string => {
  return new Intl.NumberFormat('zh-CN').format(count)
}
</script>

<template>
  <div class="w-full">
    <div v-if="props.showSearch" class="mb-2">
      <UInput v-model="keyword" placeholder="搜索分类..." class="w-full" />
    </div>

    <nav class="space-y-1">
      <button
        v-for="category in filteredCategories"
        :key="category.id"
        :data-testid="`admin-website-category-${category.id}`"
        type="button"
        @click="emit('select', category.id)"
        class="w-full flex items-center justify-between rounded-md group transition-colors cursor-pointer border-l-2"
        :class="[
          props.dense ? 'px-2.5 py-1.5 text-xs' : 'px-3 py-2 text-sm',
          category.active
            ? 'text-brand-orange bg-orange-50 dark:bg-orange-900/10 border-brand-orange'
            : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-dark-card hover:text-gray-900 dark:hover:text-gray-200 border-transparent',
        ]"
      >
        <div class="flex items-center gap-2.5 min-w-0 text-left">
          <i
            :class="[
              'fas fa-folder-tree',
              props.dense ? 'text-xs' : 'text-sm',
              category.active
                ? 'text-brand-orange'
                : 'text-gray-400 group-hover:text-gray-500 dark:group-hover:text-gray-300',
            ]"
          ></i>
          <span class="truncate">{{ category.name }}</span>
        </div>
        <span
          :class="category.active ? 'text-brand-orange' : 'text-gray-400 dark:text-gray-500'"
          class="text-[11px]"
          >{{ formatCount(category.count) }}</span
        >
      </button>

      <div
        v-if="filteredCategories.length === 0"
        class="rounded-md border border-dashed border-gray-200 px-3 py-4 text-center text-xs text-gray-500 dark:border-dark-border dark:text-gray-400"
      >
        未找到匹配分类
      </div>
    </nav>
  </div>
</template>
