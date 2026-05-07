<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { listEnabledTechStack } from '@/api/user-tech-stack'
import type { UserTechStackOption } from '@/types/user-tech-stack'

const props = withDefaults(
  defineProps<{
    open: boolean
    /** 当前用户已添加的技术栈名称列表（用于锁定勾选） */
    existing: string[]
  }>(),
  {
    existing: () => [],
  },
)

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', names: string[]): void
}>()

defineOptions({
  name: 'TechStackPickerDialog',
})

const options = ref<UserTechStackOption[]>([])
const loading = ref(false)
const errorText = ref('')
const keyword = ref('')
const selectedIds = ref<Set<number>>(new Set())

const existingLowerSet = computed(
  () => new Set(props.existing.map((n) => n.trim().toLowerCase()).filter(Boolean)),
)

const isLockedExisting = (option: UserTechStackOption) =>
  existingLowerSet.value.has(option.name.toLowerCase())

const filteredOptions = computed<UserTechStackOption[]>(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) {
    return options.value
  }
  return options.value.filter(
    (o) =>
      o.name.toLowerCase().includes(kw) ||
      (o.description ?? '').toLowerCase().includes(kw),
  )
})

const newSelectedCount = computed(() => {
  let n = 0
  for (const id of selectedIds.value) {
    const opt = options.value.find((o) => o.id === id)
    if (opt && !isLockedExisting(opt)) {
      n += 1
    }
  }
  return n
})

const totalSelectedCount = computed(() => selectedIds.value.size)

const fetchOptions = async () => {
  loading.value = true
  errorText.value = ''
  try {
    const list = await listEnabledTechStack()
    options.value = list
    // 重置勾选状态：默认锁定所有"已存在"的技术栈
    const initial = new Set<number>()
    for (const opt of list) {
      if (isLockedExisting(opt)) {
        initial.add(opt.id)
      }
    }
    selectedIds.value = initial
  } catch (err) {
    options.value = []
    errorText.value = err instanceof Error ? err.message : '加载失败'
  } finally {
    loading.value = false
  }
}

const toggleOption = (option: UserTechStackOption) => {
  if (isLockedExisting(option)) {
    return
  }
  const next = new Set(selectedIds.value)
  if (next.has(option.id)) {
    next.delete(option.id)
  } else {
    next.add(option.id)
  }
  selectedIds.value = next
}

const closeDialog = () => {
  emit('update:open', false)
}

const submitDialog = () => {
  if (newSelectedCount.value === 0) {
    return
  }
  const names: string[] = []
  for (const opt of options.value) {
    if (selectedIds.value.has(opt.id) && !isLockedExisting(opt)) {
      names.push(opt.name)
    }
  }
  emit('submit', names)
}

const onKeydown = (event: KeyboardEvent) => {
  if (!props.open) {
    return
  }
  if (event.key === 'Escape') {
    event.preventDefault()
    closeDialog()
  }
}

watch(
  () => props.open,
  (next) => {
    if (next) {
      keyword.value = ''
      void fetchOptions()
      document.addEventListener('keydown', onKeydown)
      // 锁定 body 滚动
      const root = document.documentElement
      root.dataset.cfDialogLock = '1'
      root.style.overflow = 'hidden'
    } else {
      document.removeEventListener('keydown', onKeydown)
      const root = document.documentElement
      delete root.dataset.cfDialogLock
      root.style.overflow = ''
    }
  },
  { immediate: false },
)

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown)
  const root = document.documentElement
  if (root.dataset.cfDialogLock) {
    delete root.dataset.cfDialogLock
    root.style.overflow = ''
  }
})

const initialLetter = (name: string) => name.trim().charAt(0).toUpperCase() || '#'

const onIconError = (event: Event) => {
  const target = event.target as HTMLImageElement | null
  if (target) {
    target.style.display = 'none'
    target.dataset.fallback = '1'
  }
}
</script>

<template>
  <Transition name="cf-tsp-dialog">
    <div
      v-if="open"
      class="cf-tsp-overlay fixed inset-0 z-50 flex items-end justify-center px-3 py-4 sm:items-center sm:px-6 sm:py-8"
      role="dialog"
      aria-modal="true"
      aria-labelledby="cf-tsp-title"
      @click.self="closeDialog"
    >
      <div
        class="cf-tsp-dialog relative flex w-full max-w-3xl flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xl dark:border-white/10 dark:bg-[#0e0e10]"
        :style="{ maxHeight: '85dvh' }"
      >
        <div class="cf-tsp-accent" aria-hidden="true"></div>

        <!-- Header -->
        <header class="flex items-start gap-3 border-b border-slate-100 px-5 py-4 dark:border-white/[0.06] sm:px-6 sm:py-5">
          <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-amber-100 text-amber-600 dark:bg-amber-500/15 dark:text-amber-400">
            <UIcon name="i-lucide-cpu" class="h-4 w-4" />
          </span>
          <div class="min-w-0 flex-1 space-y-0.5">
            <h4
              id="cf-tsp-title"
              class="text-base font-semibold text-slate-900 dark:text-white sm:text-lg"
            >
              添加技术栈
            </h4>
            <p class="text-xs text-slate-500 dark:text-slate-400 sm:text-[13px]">
              从字典中选择你擅长的技术，可多选；已添加的项目会被锁定
            </p>
          </div>
          <button
            type="button"
            class="cf-tsp-close-btn flex h-8 w-8 shrink-0 cursor-pointer items-center justify-center rounded-lg text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-700 dark:text-slate-500 dark:hover:bg-white/[0.06] dark:hover:text-white"
            aria-label="关闭对话框"
            @click="closeDialog"
          >
            <UIcon name="i-lucide-x" class="h-4 w-4" />
          </button>
        </header>

        <!-- Search -->
        <div class="flex flex-col gap-2 border-b border-slate-100 px-5 py-3 dark:border-white/[0.06] sm:flex-row sm:items-center sm:px-6">
          <div class="cf-tsp-search relative flex-1">
            <UIcon
              name="i-lucide-search"
              class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400 dark:text-slate-500"
            />
            <input
              v-model="keyword"
              type="text"
              placeholder="搜索技术名称或简介…"
              class="cf-tsp-search-input h-10 w-full rounded-lg border border-slate-200 bg-white pl-9 pr-9 text-sm text-slate-900 outline-none transition-colors duration-150 placeholder:text-slate-400 focus:border-amber-400 focus:ring-1 focus:ring-amber-400/40 dark:border-white/[0.10] dark:bg-[#16161d] dark:text-white dark:placeholder:text-slate-500 dark:focus:border-amber-400/60 dark:focus:bg-[#1c1c25] dark:focus:ring-amber-400/30"
            />
            <button
              v-if="keyword"
              type="button"
              class="absolute right-2 top-1/2 flex h-6 w-6 -translate-y-1/2 cursor-pointer items-center justify-center rounded-md text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-700 dark:text-slate-500 dark:hover:bg-white/[0.06] dark:hover:text-white"
              aria-label="清除搜索"
              @click="keyword = ''"
            >
              <UIcon name="i-lucide-x" class="h-3.5 w-3.5" />
            </button>
          </div>
          <div class="flex shrink-0 items-center gap-2 text-[11px] font-medium tabular-nums text-slate-500 dark:text-slate-400">
            <span class="rounded-full border border-slate-200 bg-slate-50 px-2.5 py-1 dark:border-white/10 dark:bg-white/[0.04]">
              已选 <span class="text-amber-600 dark:text-amber-400">{{ totalSelectedCount }}</span> / 共 {{ options.length }}
            </span>
          </div>
        </div>

        <!-- Body -->
        <div class="cf-tsp-body flex-1 overflow-y-auto px-5 py-4 sm:px-6">
          <!-- Loading skeleton -->
          <div
            v-if="loading"
            class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3"
          >
            <div
              v-for="i in 6"
              :key="i"
              class="h-[88px] animate-pulse rounded-xl border border-slate-200 bg-slate-50 dark:border-white/[0.06] dark:bg-white/[0.03]"
            ></div>
          </div>

          <!-- Error -->
          <div
            v-else-if="errorText"
            class="flex flex-col items-center justify-center gap-3 py-16 text-center"
          >
            <span class="flex h-12 w-12 items-center justify-center rounded-full bg-rose-50 text-rose-500 dark:bg-rose-500/10 dark:text-rose-400">
              <UIcon name="i-lucide-triangle-alert" class="h-5 w-5" />
            </span>
            <div class="space-y-1">
              <p class="text-sm font-semibold text-slate-700 dark:text-slate-200">加载技术栈字典失败</p>
              <p class="text-xs text-slate-500 dark:text-slate-400">{{ errorText }}</p>
            </div>
            <button
              type="button"
              class="cf-tsp-retry-btn inline-flex h-8 cursor-pointer items-center gap-1.5 rounded-md border border-slate-200 bg-white px-3 text-xs font-medium text-slate-700 transition-colors hover:border-amber-300 hover:text-amber-700 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-200 dark:hover:border-amber-400/30 dark:hover:text-amber-300"
              @click="fetchOptions"
            >
              <UIcon name="i-lucide-rotate-cw" class="h-3.5 w-3.5" />
              <span>重试</span>
            </button>
          </div>

          <!-- Empty (filter no result) -->
          <div
            v-else-if="filteredOptions.length === 0"
            class="flex flex-col items-center justify-center gap-3 py-16 text-center"
          >
            <span class="flex h-12 w-12 items-center justify-center rounded-full bg-slate-100 text-slate-400 dark:bg-white/[0.06] dark:text-slate-500">
              <UIcon name="i-lucide-search-x" class="h-5 w-5" />
            </span>
            <div class="space-y-1">
              <p class="text-sm font-semibold text-slate-700 dark:text-slate-200">未找到匹配的技术栈</p>
              <p class="text-xs text-slate-500 dark:text-slate-400">
                换个关键词试试，或在标签输入框中手动输入
              </p>
            </div>
          </div>

          <!-- Grid -->
          <div
            v-else
            class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3"
          >
            <button
              v-for="(option, idx) in filteredOptions"
              :key="option.id"
              type="button"
              class="cf-tsp-card group relative flex items-start gap-3 rounded-xl border p-3 text-left transition-all duration-200"
              :class="[
                isLockedExisting(option)
                  ? 'cf-tsp-card-locked border-amber-300/50 bg-amber-50/60 dark:border-amber-400/20 dark:bg-amber-500/[0.06]'
                  : selectedIds.has(option.id)
                    ? 'cf-tsp-card-selected border-amber-400/70 bg-amber-50 dark:border-amber-400/40 dark:bg-amber-500/[0.08]'
                    : 'border-slate-200 bg-white hover:border-amber-300 hover:bg-slate-50 dark:border-white/[0.08] dark:bg-white/[0.02] dark:hover:border-amber-400/30 dark:hover:bg-white/[0.04]',
              ]"
              :style="{ '--idx': idx, '--brand': option.color }"
              :disabled="isLockedExisting(option)"
              :aria-pressed="selectedIds.has(option.id)"
              @click="toggleOption(option)"
            >
              <!-- Icon -->
              <span
                class="cf-tsp-icon-box flex h-9 w-9 shrink-0 items-center justify-center overflow-hidden rounded-lg"
                :style="{ backgroundColor: option.color + '1f' }"
              >
                <img
                  v-if="option.iconPng"
                  :src="option.iconPng"
                  :alt="option.name"
                  class="h-6 w-6 object-contain"
                  loading="lazy"
                  @error="onIconError"
                />
                <span
                  v-else
                  class="text-sm font-bold tabular-nums"
                  :style="{ color: option.color }"
                >{{ initialLetter(option.name) }}</span>
              </span>

              <!-- Text -->
              <div class="min-w-0 flex-1 pr-6">
                <p class="truncate text-sm font-semibold text-slate-900 dark:text-white">
                  {{ option.name }}
                </p>
                <p class="mt-0.5 line-clamp-2 text-[11px] leading-relaxed text-slate-500 dark:text-slate-400 sm:text-xs">
                  {{ option.description }}
                </p>
              </div>

              <!-- Right indicator: locked badge / check -->
              <span
                v-if="isLockedExisting(option)"
                class="cf-tsp-locked-badge absolute right-2 top-2 inline-flex items-center gap-1 rounded-full bg-amber-100 px-1.5 py-0.5 text-[10px] font-semibold text-amber-700 dark:bg-amber-500/15 dark:text-amber-300"
              >
                <UIcon name="i-lucide-check" class="h-3 w-3" />
                <span>已添加</span>
              </span>
              <span
                v-else
                class="cf-tsp-check absolute right-2 top-2 flex h-5 w-5 items-center justify-center rounded-full border transition-all duration-200"
                :class="
                  selectedIds.has(option.id)
                    ? 'border-amber-500 bg-amber-500 text-white shadow-sm'
                    : 'border-slate-300 bg-white text-transparent dark:border-white/15 dark:bg-white/[0.04]'
                "
              >
                <UIcon
                  name="i-lucide-check"
                  class="h-3 w-3 transition-transform"
                  :class="selectedIds.has(option.id) ? 'scale-100' : 'scale-0'"
                />
              </span>
            </button>
          </div>
        </div>

        <!-- Footer -->
        <footer class="flex flex-col-reverse items-stretch gap-2 border-t border-slate-100 px-5 py-3 dark:border-white/[0.06] sm:flex-row sm:items-center sm:justify-between sm:px-6">
          <p class="text-xs text-slate-500 dark:text-slate-400">
            <UIcon name="i-lucide-info" class="-mt-0.5 mr-1 inline h-3 w-3" />
            已勾选
            <span class="font-mono font-semibold tabular-nums text-slate-700 dark:text-slate-200">{{ totalSelectedCount }}</span>
            项，其中
            <span class="font-mono font-semibold tabular-nums text-amber-600 dark:text-amber-400">{{ newSelectedCount }}</span>
            项为新增
          </p>
          <div class="flex items-center justify-end gap-2">
            <button
              type="button"
              class="inline-flex h-9 cursor-pointer items-center justify-center rounded-lg border border-slate-200 bg-white px-4 text-sm font-medium text-slate-700 transition-colors hover:border-slate-300 hover:bg-slate-50 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-200 dark:hover:border-white/20 dark:hover:bg-white/[0.06]"
              @click="closeDialog"
            >
              取消
            </button>
            <button
              type="button"
              class="cf-tsp-submit inline-flex h-9 cursor-pointer items-center justify-center gap-1.5 rounded-lg px-4 text-sm font-semibold transition-all duration-200 disabled:cursor-not-allowed"
              :disabled="newSelectedCount === 0"
              @click="submitDialog"
            >
              <UIcon name="i-lucide-plus" class="h-4 w-4" />
              <span>添加 {{ newSelectedCount }} 项</span>
            </button>
          </div>
        </footer>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.cf-tsp-overlay {
  background-color: rgb(8 8 10 / 0.55);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.cf-tsp-dialog {
  box-shadow: 0 24px 56px -16px rgb(15 23 42 / 0.35);
}

:where(html.dark) .cf-tsp-dialog {
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.04) inset,
    0 24px 56px -16px rgb(0 0 0 / 0.7),
    0 0 0 1px rgb(255 255 255 / 0.02);
}

.cf-tsp-accent {
  position: absolute;
  inset: 0 0 auto 0;
  height: 1px;
  background-image: linear-gradient(
    90deg,
    transparent 0%,
    rgb(245 158 11 / 0.55) 50%,
    transparent 100%
  );
  z-index: 1;
}

/* Cards stagger reveal */
.cf-tsp-card {
  animation: cf-tsp-card-in 0.32s cubic-bezier(0.16, 1, 0.3, 1) both;
  animation-delay: calc(var(--idx, 0) * 28ms);
}

@keyframes cf-tsp-card-in {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.cf-tsp-card:not(.cf-tsp-card-locked):hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px -12px rgb(245 158 11 / 0.35);
}

.cf-tsp-card:not(.cf-tsp-card-locked):active {
  transform: scale(0.985);
}

.cf-tsp-card-locked {
  cursor: not-allowed;
}

.cf-tsp-card:focus-visible {
  outline: 2px solid rgb(245 158 11 / 0.6);
  outline-offset: 2px;
}

/* Submit button */
.cf-tsp-submit {
  background: #f59e0b;
  color: #1f1300;
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.35) inset,
    0 6px 14px -8px rgb(245 158 11 / 0.55);
}
.cf-tsp-submit:hover:not(:disabled) {
  background: #fbbf24;
  transform: translateY(-1px);
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.45) inset,
    0 10px 20px -10px rgb(245 158 11 / 0.65);
}
.cf-tsp-submit:active:not(:disabled) {
  background: #d97706;
  transform: translateY(0);
}
.cf-tsp-submit:disabled {
  background: rgb(120 113 108 / 0.45);
  color: rgb(214 211 209);
  box-shadow: none;
}
:where(html.dark) .cf-tsp-submit:disabled {
  background: rgb(255 255 255 / 0.06);
  color: rgb(148 163 184);
}

/* Body inner scrollbar polish */
.cf-tsp-body {
  scrollbar-width: thin;
  scrollbar-color: rgb(148 163 184 / 0.4) transparent;
}
.cf-tsp-body::-webkit-scrollbar {
  width: 8px;
}
.cf-tsp-body::-webkit-scrollbar-thumb {
  background: rgb(148 163 184 / 0.35);
  border-radius: 4px;
}
.cf-tsp-body::-webkit-scrollbar-thumb:hover {
  background: rgb(148 163 184 / 0.55);
}
:where(html.dark) .cf-tsp-body::-webkit-scrollbar-thumb {
  background: rgb(255 255 255 / 0.1);
}

/* Dialog enter/leave */
.cf-tsp-dialog-enter-active,
.cf-tsp-dialog-leave-active {
  transition: opacity 0.22s ease;
}
.cf-tsp-dialog-enter-active .cf-tsp-dialog,
.cf-tsp-dialog-leave-active .cf-tsp-dialog {
  transition:
    transform 0.24s cubic-bezier(0.16, 1, 0.3, 1),
    opacity 0.22s ease;
}

.cf-tsp-dialog-enter-from,
.cf-tsp-dialog-leave-to {
  opacity: 0;
}

.cf-tsp-dialog-enter-from .cf-tsp-dialog {
  opacity: 0;
  transform: translateY(12px) scale(0.98);
}
.cf-tsp-dialog-leave-to .cf-tsp-dialog {
  opacity: 0;
  transform: translateY(6px);
}

/* Mobile: dialog enters from bottom */
@media (max-width: 639px) {
  .cf-tsp-dialog-enter-from .cf-tsp-dialog {
    transform: translateY(24px) scale(1);
  }
}
</style>
