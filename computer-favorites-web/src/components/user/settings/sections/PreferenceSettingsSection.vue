<script setup lang="ts">
import {
  computed,
  inject,
  ref,
  onMounted,
  onBeforeUnmount,
  type ComponentPublicInstance,
} from 'vue'
import { settingsStateKey } from '@/components/user/settings/context'
import { useAppStore, type ThemeMode } from '@/stores/app'
import { useI18n } from 'vue-i18n'

const settingsState = inject(settingsStateKey)
if (!settingsState) {
  throw new Error('Settings state is not provided')
}

const appStore = useAppStore()
const { t } = useI18n()
const setting = settingsState.setting

// 点击主题卡片后立即切换全局主题，保持设置值与页面表现一致
const updateTheme = (val: ThemeMode) => {
  setting.theme = val
  appStore.setThemeMode(val)
}

// 定义下拉选项类型
interface SelectOption<T> {
  label: string
  value: T
}

// 当前打开的下拉框 ID
const activeDropdownId = ref<string | null>(null)

// 下拉框 ID 列表
const DROPDOWN_IDS = {
  LANGUAGE: 'language',
  HOMEPAGE_STYLE: 'homepageStyle',
  PAGE_SIZE: 'pageSize',
} as const

// 自定义下拉选择器逻辑
const createDropdown = <T,>(id: string, modelValue: { value: T }, options: SelectOption<T>[]) => {
  const dropdownRef = ref<HTMLElement | null>(null)

  // 判断当前下拉框是否打开
  const isOpen = computed(() => activeDropdownId.value === id)

  // 获取当前选中项的标签
  const selectedLabel = computed(() => {
    const option = options.find((opt) => opt.value === modelValue.value)
    return option ? option.label : ''
  })

  // 切换下拉状态
  const toggle = () => {
    if (activeDropdownId.value === id) {
      activeDropdownId.value = null
    } else {
      activeDropdownId.value = id
    }
  }

  // 选择选项
  const select = (value: T) => {
    modelValue.value = value
    activeDropdownId.value = null
  }

  // 设置 ref 的辅助函数
  const setRef = (el: Element | ComponentPublicInstance | null) => {
    dropdownRef.value = el as HTMLElement | null
  }

  return {
    id,
    isOpen,
    dropdownRef,
    setRef,
    selectedLabel,
    toggle,
    select,
  }
}

// 语言下拉
const languageOptions: SelectOption<string>[] = [
  { label: '简体中文', value: 'zh-CN' },
  { label: 'English', value: 'en-US' },
]
const languageDropdown = createDropdown(
  DROPDOWN_IDS.LANGUAGE,
  computed({
    get: () => setting.language,
    set: (val) => {
      setting.language = val
    },
  }),
  languageOptions,
)

// 主页视图下拉
const homepageStyleOptions: SelectOption<string>[] = [
  { label: t('settings.preference.homepage.card'), value: 'card' },
  { label: t('settings.preference.homepage.list'), value: 'list' },
]
const homepageStyleDropdown = createDropdown(
  DROPDOWN_IDS.HOMEPAGE_STYLE,
  computed({
    get: () => setting.homepageStyle,
    set: (val) => {
      setting.homepageStyle = val
    },
  }),
  homepageStyleOptions,
)

// 分页大小下拉
const pageSizeOptions: SelectOption<number>[] = [
  { label: '10 条', value: 10 },
  { label: '20 条', value: 20 },
  { label: '50 条', value: 50 },
]
const pageSizeDropdown = createDropdown(
  DROPDOWN_IDS.PAGE_SIZE,
  computed({
    get: () => setting.pageSize,
    set: (val) => {
      setting.pageSize = val
    },
  }),
  pageSizeOptions,
)

// 所有下拉框的 ref
const allDropdownRefs = [languageDropdown, homepageStyleDropdown, pageSizeDropdown]

// 点击外部关闭下拉框
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as Node
  const isInsideAnyDropdown = allDropdownRefs.some((dropdown) =>
    dropdown.dropdownRef.value?.contains(target),
  )
  if (!isInsideAnyDropdown) {
    activeDropdownId.value = null
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

defineOptions({
  name: 'PreferenceSettingsSection',
})
</script>

<template>
  <div class="space-y-6">
    <div class="space-y-1">
      <h3 class="text-xl font-medium text-slate-900 dark:text-white">
        {{ t('settings.preference.title') }}
      </h3>
      <p class="text-sm text-slate-500 dark:text-slate-400">
        {{ t('settings.preference.subtitle') }}
      </p>
    </div>

    <div
      class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-0 dark:bg-black dark:shadow-none"
    >
      <div class="space-y-8">
        <!-- 外观设置 -->
        <div>
          <h4 class="mb-4 text-base font-medium text-slate-900 dark:text-white">系统主题</h4>
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <div
              class="relative cursor-pointer rounded-xl border-2 p-4 text-center transition-all"
              :class="
                setting.theme === 'light'
                  ? 'border-primary-600 bg-primary-50 dark:bg-primary-500/20 dark:border-white'
                  : 'border-slate-200 hover:border-slate-300 dark:border-white/10 dark:bg-white/5 dark:hover:border-white/20'
              "
              @click="updateTheme('light')"
            >
              <div
                class="mx-auto mb-2 flex h-8 w-8 items-center justify-center rounded-full bg-primary-100 text-primary-600 dark:bg-primary-500/20 dark:text-primary-300"
              >
                <UIcon name="i-lucide-sun" class="h-5 w-5" />
              </div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">
                {{ t('settings.preference.theme.light') }}
              </p>
            </div>

            <div
              class="relative cursor-pointer rounded-xl border-2 p-4 text-center transition-all"
              :class="
                setting.theme === 'dark'
                  ? 'border-primary-600 bg-primary-50 dark:bg-white/10 dark:border-white'
                  : 'border-slate-200 hover:border-slate-300 dark:border-white/10 dark:bg-white/5 dark:hover:border-white/20'
              "
              @click="updateTheme('dark')"
            >
              <div
                class="mx-auto mb-2 flex h-8 w-8 items-center justify-center rounded-full bg-slate-800 text-white dark:bg-black dark:text-white dark:ring-1 dark:ring-white/20"
              >
                <UIcon name="i-lucide-moon" class="h-5 w-5" />
              </div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">
                {{ t('settings.preference.theme.dark') }}
              </p>
            </div>

            <div
              class="relative cursor-pointer rounded-xl border-2 p-4 text-center transition-all"
              :class="
                setting.theme === 'system'
                  ? 'border-primary-600 bg-primary-50 dark:bg-primary-500/20 dark:border-white'
                  : 'border-slate-200 hover:border-slate-300 dark:border-white/10 dark:bg-white/5 dark:hover:border-white/20'
              "
              @click="updateTheme('system')"
            >
              <div
                class="mx-auto mb-2 flex h-8 w-8 items-center justify-center rounded-full bg-primary-100 text-primary-600 dark:bg-primary-500/20 dark:text-primary-300"
              >
                <UIcon name="i-lucide-monitor" class="h-5 w-5" />
              </div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">
                {{ t('settings.preference.theme.system') }}
              </p>
            </div>
          </div>
        </div>

        <div class="h-px bg-slate-200 dark:bg-white/10"></div>

        <!-- 其他偏好 -->
        <div class="space-y-6">
          <!-- 系统语言 -->
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">
                {{ t('settings.preference.language.title') }}
              </p>
              <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                {{ t('settings.preference.language.subtitle') }}
              </p>
            </div>
            <div :ref="languageDropdown.setRef" class="relative">
              <button
                type="button"
                class="flex w-32 cursor-pointer items-center justify-between rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm transition-colors hover:border-slate-300 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-white/10 dark:!bg-slate-900 dark:text-white dark:hover:border-white/20 dark:focus:border-primary-400 dark:focus:ring-primary-400"
                @click.stop="languageDropdown.toggle()"
              >
                <span>{{ languageDropdown.selectedLabel.value }}</span>
                <svg
                  class="h-4 w-4 text-slate-400 transition-transform dark:text-slate-500"
                  :class="{ 'rotate-180': languageDropdown.isOpen.value }"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 9l-7 7-7-7"
                  />
                </svg>
              </button>
              <Transition
                enter-active-class="transition ease-out duration-100"
                enter-from-class="transform opacity-0 scale-95"
                enter-to-class="transform opacity-100 scale-100"
                leave-active-class="transition ease-in duration-75"
                leave-from-class="transform opacity-100 scale-100"
                leave-to-class="transform opacity-0 scale-95"
              >
                <div
                  v-if="languageDropdown.isOpen.value"
                  class="absolute right-0 z-50 mt-1 w-full overflow-hidden rounded-lg border border-slate-200 bg-white shadow-lg dark:border-white/10 dark:!bg-slate-900"
                >
                  <button
                    v-for="option in languageOptions"
                    :key="option.value"
                    type="button"
                    class="block w-full cursor-pointer px-3 py-2 text-left text-sm transition-colors"
                    :class="
                      setting.language === option.value
                        ? 'bg-primary-50 text-primary-600 hover:!bg-primary-100 dark:bg-primary-500/20 dark:text-primary-300 dark:hover:!bg-primary-500/35 dark:hover:!text-primary-200'
                        : 'text-slate-700 hover:!bg-slate-100 hover:!text-slate-900 dark:text-slate-200 dark:hover:!bg-slate-800 dark:hover:!text-white'
                    "
                    @click="languageDropdown.select(option.value)"
                  >
                    {{ option.label }}
                  </button>
                </div>
              </Transition>
            </div>
          </div>

          <!-- 默认主页视图 -->
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">
                {{ t('settings.preference.homepage.title') }}
              </p>
              <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                {{ t('settings.preference.homepage.subtitle') }}
              </p>
            </div>
            <div :ref="homepageStyleDropdown.setRef" class="relative">
              <button
                type="button"
                class="flex w-32 cursor-pointer items-center justify-between rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm transition-colors hover:border-slate-300 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-white/10 dark:!bg-slate-900 dark:text-white dark:hover:border-white/20 dark:focus:border-primary-400 dark:focus:ring-primary-400"
                @click.stop="homepageStyleDropdown.toggle()"
              >
                <span>{{ homepageStyleDropdown.selectedLabel.value }}</span>
                <svg
                  class="h-4 w-4 text-slate-400 transition-transform dark:text-slate-500"
                  :class="{ 'rotate-180': homepageStyleDropdown.isOpen.value }"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 9l-7 7-7-7"
                  />
                </svg>
              </button>
              <Transition
                enter-active-class="transition ease-out duration-100"
                enter-from-class="transform opacity-0 scale-95"
                enter-to-class="transform opacity-100 scale-100"
                leave-active-class="transition ease-in duration-75"
                leave-from-class="transform opacity-100 scale-100"
                leave-to-class="transform opacity-0 scale-95"
              >
                <div
                  v-if="homepageStyleDropdown.isOpen.value"
                  class="absolute right-0 z-50 mt-1 w-full overflow-hidden rounded-lg border border-slate-200 bg-white shadow-lg dark:border-white/10 dark:!bg-slate-900"
                >
                  <button
                    v-for="option in homepageStyleOptions"
                    :key="option.value"
                    type="button"
                    class="block w-full cursor-pointer px-3 py-2 text-left text-sm transition-colors"
                    :class="
                      setting.homepageStyle === option.value
                        ? 'bg-primary-50 text-primary-600 hover:!bg-primary-100 dark:bg-primary-500/20 dark:text-primary-300 dark:hover:!bg-primary-500/35 dark:hover:!text-primary-200'
                        : 'text-slate-700 hover:!bg-slate-100 hover:!text-slate-900 dark:text-slate-200 dark:hover:!bg-slate-800 dark:hover:!text-white'
                    "
                    @click="homepageStyleDropdown.select(option.value)"
                  >
                    {{ option.label }}
                  </button>
                </div>
              </Transition>
            </div>
          </div>

          <!-- 默认分页大小 -->
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-900 dark:text-white">
                {{ t('settings.preference.pageSize.title') }}
              </p>
              <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                {{ t('settings.preference.pageSize.subtitle') }}
              </p>
            </div>
            <div :ref="pageSizeDropdown.setRef" class="relative">
              <button
                type="button"
                class="flex w-32 cursor-pointer items-center justify-between rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm transition-colors hover:border-slate-300 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-white/10 dark:!bg-slate-900 dark:text-white dark:hover:border-white/20 dark:focus:border-primary-400 dark:focus:ring-primary-400"
                @click.stop="pageSizeDropdown.toggle()"
              >
                <span>{{ pageSizeDropdown.selectedLabel.value }}</span>
                <svg
                  class="h-4 w-4 text-slate-400 transition-transform dark:text-slate-500"
                  :class="{ 'rotate-180': pageSizeDropdown.isOpen.value }"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 9l-7 7-7-7"
                  />
                </svg>
              </button>
              <Transition
                enter-active-class="transition ease-out duration-100"
                enter-from-class="transform opacity-0 scale-95"
                enter-to-class="transform opacity-100 scale-100"
                leave-active-class="transition ease-in duration-75"
                leave-from-class="transform opacity-100 scale-100"
                leave-to-class="transform opacity-0 scale-95"
              >
                <div
                  v-if="pageSizeDropdown.isOpen.value"
                  class="absolute right-0 z-50 mt-1 w-full overflow-hidden rounded-lg border border-slate-200 bg-white shadow-lg dark:border-white/10 dark:!bg-slate-900"
                >
                  <button
                    v-for="option in pageSizeOptions"
                    :key="option.value"
                    type="button"
                    class="block w-full cursor-pointer px-3 py-2 text-left text-sm transition-colors"
                    :class="
                      setting.pageSize === option.value
                        ? 'bg-primary-50 text-primary-600 hover:!bg-primary-100 dark:bg-primary-500/20 dark:text-primary-300 dark:hover:!bg-primary-500/35 dark:hover:!text-primary-200'
                        : 'text-slate-700 hover:!bg-slate-100 hover:!text-slate-900 dark:text-slate-200 dark:hover:!bg-slate-800 dark:hover:!text-white'
                    "
                    @click="pageSizeDropdown.select(option.value)"
                  >
                    {{ option.label }}
                  </button>
                </div>
              </Transition>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
