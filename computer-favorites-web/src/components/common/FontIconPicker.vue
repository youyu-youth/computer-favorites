<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import iconsData from '@/assets/data/fa-icons.json'

/**
 * @description Font Awesome 图标选择器组件
 * 支持分类浏览、关键字搜索、暗色模式
 */

const props = withDefaults(
  defineProps<{
    modelValue: string
    placeholder?: string
    disabled?: boolean
  }>(),
  {
    placeholder: '点击选择图标',
    disabled: false,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', val: string): void
}>()

// 弹窗开关
const open = ref(false)
// 搜索关键字
const searchQuery = ref('')
// 当前选中分类（空字符串代表全部）
const activeCategory = ref('')
// 弹窗内临时选中值（确认前）
const tempSelected = ref('')

// 所有分类名称
const categoryNames = computed(() => iconsData.categories.map((c) => c.name))

// 当前分类下的图标列表
const filteredIcons = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()

  let icons: string[] = []

  if (activeCategory.value === '') {
    // 全部
    icons = iconsData.categories.flatMap((c) => c.icons)
  } else {
    const found = iconsData.categories.find((c) => c.name === activeCategory.value)
    icons = found ? found.icons : []
  }

  if (!query) return icons

  // 去掉 "fa-solid fa-" 前缀后做关键字匹配
  return icons.filter((icon) => {
    const name = icon.replace('fa-solid fa-', '')
    return name.includes(query)
  })
})

// 从图标类名提取可读名称（fa-solid fa-house → house）
const getIconLabel = (iconClass: string) => {
  return iconClass.replace('fa-solid fa-', '').replace(/-/g, ' ')
}

const openPicker = () => {
  if (props.disabled) return
  tempSelected.value = props.modelValue ?? ''
  searchQuery.value = ''
  activeCategory.value = ''
  open.value = true
}

const closePicker = () => {
  open.value = false
}

const selectIcon = (iconClass: string) => {
  tempSelected.value = iconClass
}

const confirmSelection = () => {
  emit('update:modelValue', tempSelected.value)
  open.value = false
}

const clearIcon = () => {
  tempSelected.value = ''
}

const clearAndClose = () => {
  emit('update:modelValue', '')
  open.value = false
}

// 键盘 Esc 关闭
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape') closePicker()
}

watch(open, (val) => {
  if (val) {
    document.addEventListener('keydown', handleKeydown)
  } else {
    document.removeEventListener('keydown', handleKeydown)
  }
})
</script>

<template>
  <!-- 触发按钮 -->
  <button
    type="button"
    class="inline-flex h-10 w-full cursor-pointer items-center gap-2.5 rounded-md border border-gray-300 bg-white px-3 text-sm text-gray-700 transition-colors hover:border-blue-400 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500/20 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:hover:border-blue-500 dark:hover:bg-dark-card"
    :disabled="disabled"
    @click="openPicker"
  >
    <span
      v-if="modelValue"
      class="flex h-6 w-6 shrink-0 items-center justify-center rounded text-gray-600 dark:text-gray-300"
    >
      <i :class="modelValue"></i>
    </span>
    <span v-else class="flex h-6 w-6 shrink-0 items-center justify-center text-gray-300 dark:text-gray-600">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="h-4 w-4">
        <rect x="3" y="3" width="7" height="7" rx="1" />
        <rect x="14" y="3" width="7" height="7" rx="1" />
        <rect x="3" y="14" width="7" height="7" rx="1" />
        <rect x="14" y="14" width="7" height="7" rx="1" />
      </svg>
    </span>
    <span class="flex-1 truncate text-left text-sm" :class="modelValue ? 'text-gray-700 dark:text-gray-100' : 'text-gray-400 dark:text-gray-500'">
      {{ modelValue ? getIconLabel(modelValue) : placeholder }}
    </span>
    <i class="fas fa-chevron-down text-xs text-gray-400 dark:text-gray-500"></i>
  </button>

  <!-- 弹窗遮罩 + 弹窗 -->
  <Transition name="cf-icon-picker-fade" appear>
    <div
      v-if="open"
      class="fixed inset-0 z-[150] flex items-center justify-center p-4 sm:p-6"
      @click.self="closePicker"
    >
      <div class="absolute inset-0 bg-black/45 backdrop-blur-[1px]" @click="closePicker"></div>

      <section
        class="relative z-[1] flex w-full max-w-[680px] flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-[0_22px_48px_rgba(15,23,42,0.3)] dark:border-dark-border dark:bg-dark-card dark:shadow-[0_30px_58px_rgba(2,6,23,0.7)]"
        style="max-height: min(90vh, 640px)"
      >
        <!-- 弹窗头部 -->
        <header class="shrink-0 border-b border-gray-200 bg-gray-50 px-5 py-4 dark:border-dark-border dark:bg-dark-bg">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div
                class="flex h-9 w-9 items-center justify-center rounded-lg border border-blue-200 bg-blue-50 text-blue-600 dark:border-blue-900/50 dark:bg-blue-900/20 dark:text-blue-300"
              >
                <i class="fas fa-icons text-sm"></i>
              </div>
              <div>
                <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">选择图标</h3>
                <p class="mt-0.5 text-xs text-gray-500 dark:text-gray-400">从 Font Awesome 图标库中选择</p>
              </div>
            </div>
            <button
              type="button"
              class="inline-flex h-8 w-8 cursor-pointer items-center justify-center rounded-md border border-gray-200 bg-white text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:border-dark-border dark:bg-dark-bg dark:text-gray-300 dark:hover:bg-gray-700 dark:hover:text-gray-100"
              @click="closePicker"
            >
              <i class="fas fa-times text-sm"></i>
            </button>
          </div>

          <!-- 搜索框 -->
          <div class="relative mt-3">
            <i class="fas fa-magnifying-glass pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-xs text-gray-400 dark:text-gray-500"></i>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索图标，例如：code、folder、user..."
              class="h-9 w-full rounded-md border border-gray-300 bg-white pl-8 pr-3 text-sm text-gray-700 outline-none transition-colors placeholder:text-gray-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-dark-border dark:bg-dark-bg dark:text-gray-100 dark:placeholder:text-gray-500 dark:focus:border-blue-400"
            />
          </div>
        </header>

        <!-- 分类 Tabs -->
        <div class="shrink-0 border-b border-gray-100 bg-white px-4 py-2 dark:border-dark-border dark:bg-dark-card">
          <div class="flex flex-wrap gap-1">
            <button
              type="button"
              class="cursor-pointer rounded-md px-2.5 py-1 text-xs font-medium transition-colors"
              :class="
                activeCategory === ''
                  ? 'bg-blue-50 text-blue-600 dark:bg-blue-900/20 dark:text-blue-300'
                  : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-dark-bg'
              "
              @click="activeCategory = ''"
            >
              全部
            </button>
            <button
              v-for="cat in categoryNames"
              :key="cat"
              type="button"
              class="cursor-pointer rounded-md px-2.5 py-1 text-xs font-medium transition-colors"
              :class="
                activeCategory === cat
                  ? 'bg-blue-50 text-blue-600 dark:bg-blue-900/20 dark:text-blue-300'
                  : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-dark-bg'
              "
              @click="activeCategory = cat"
            >
              {{ cat }}
            </button>
          </div>
        </div>

        <!-- 图标网格 -->
        <div class="min-h-0 flex-1 overflow-y-auto p-4">
          <div v-if="filteredIcons.length === 0" class="flex flex-col items-center justify-center py-16 text-gray-400 dark:text-gray-600">
            <i class="fas fa-magnifying-glass mb-3 text-3xl"></i>
            <p class="text-sm">未找到匹配的图标</p>
          </div>
          <div v-else class="grid grid-cols-6 gap-1.5 sm:grid-cols-8 md:grid-cols-10">
            <button
              v-for="icon in filteredIcons"
              :key="icon"
              type="button"
              :title="getIconLabel(icon)"
              class="group flex cursor-pointer flex-col items-center justify-center gap-1 rounded-lg p-2 transition-colors"
              :class="
                tempSelected === icon
                  ? 'bg-blue-100 text-blue-600 ring-2 ring-blue-500 dark:bg-blue-900/30 dark:text-blue-300 dark:ring-blue-400'
                  : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-dark-bg'
              "
              @click="selectIcon(icon)"
            >
              <i :class="icon" class="text-base leading-none"></i>
              <span class="max-w-full truncate text-[9px] leading-tight text-gray-400 dark:text-gray-600 group-hover:text-gray-600 dark:group-hover:text-gray-400">
                {{ getIconLabel(icon) }}
              </span>
            </button>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <footer class="shrink-0 flex items-center justify-between border-t border-gray-200 bg-gray-50 px-5 py-4 dark:border-dark-border dark:bg-dark-bg">
          <div class="flex items-center gap-2">
            <span v-if="tempSelected" class="flex items-center gap-2 rounded-md border border-gray-200 bg-white px-3 py-1.5 dark:border-dark-border dark:bg-dark-card">
              <i :class="tempSelected" class="text-sm text-gray-600 dark:text-gray-300"></i>
              <span class="text-xs text-gray-500 dark:text-gray-400">{{ getIconLabel(tempSelected) }}</span>
            </span>
            <span v-else class="text-xs text-gray-400 dark:text-gray-600">未选择图标</span>
          </div>
          <div class="flex gap-2">
            <button
              v-if="tempSelected"
              type="button"
              class="inline-flex h-8 cursor-pointer items-center justify-center rounded-md border border-gray-300 bg-white px-3 text-xs font-medium text-gray-600 transition-colors hover:bg-gray-100 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg"
              @click="clearIcon"
            >
              <i class="fas fa-xmark mr-1 text-xs"></i>清除
            </button>
            <button
              type="button"
              class="inline-flex h-8 cursor-pointer items-center justify-center rounded-md border border-gray-300 bg-white px-3 text-xs font-medium text-gray-700 shadow-sm transition-colors hover:bg-gray-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg"
              @click="closePicker"
            >
              取消
            </button>
            <button
              type="button"
              class="inline-flex h-8 cursor-pointer items-center justify-center rounded-md border border-transparent bg-blue-600 px-4 text-xs font-medium text-white shadow-sm transition-colors hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50 dark:bg-blue-500 dark:hover:bg-blue-600"
              @click="confirmSelection"
            >
              确认选择
            </button>
          </div>
        </footer>
      </section>
    </div>
  </Transition>
</template>

<style scoped>
.cf-icon-picker-fade-enter-active,
.cf-icon-picker-fade-leave-active {
  transition: opacity 0.18s ease;
}

.cf-icon-picker-fade-enter-from,
.cf-icon-picker-fade-leave-to {
  opacity: 0;
}
</style>
