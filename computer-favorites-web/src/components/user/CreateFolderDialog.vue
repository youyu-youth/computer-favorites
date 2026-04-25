<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { X, FolderPlus, ChevronDown } from 'lucide-vue-next'
import FontIconPicker from '@/components/common/FontIconPicker.vue'
import { createUserFolder } from '@/api/user-folder'
import { useToast } from '@/composables/useToast'
import type { FolderFormData, FolderOption, ColorPreset } from '@/types/folder'

/**
 * @description 创建收藏文件夹对话框
 * 支持图标选择、颜色选择、父级文件夹选择、排序
 * 遵循用户端主题色（琥珀/橙色调）
 */

interface Props {
  open: boolean
  parentId?: number
  parentOptions?: FolderOption[]
}

const props = withDefaults(defineProps<Props>(), {
  parentId: 0,
  parentOptions: () => [],
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', data: FolderFormData, result: { id: number }): void
}>()

const toast = useToast()

const colorPresets: ColorPreset[] = [
  { label: '琥珀', value: '#f59e0b' },
  { label: '橙色', value: '#f97316' },
  { label: '红色', value: '#ef4444' },
  { label: '玫瑰', value: '#f43f5e' },
  { label: '粉色', value: '#ec4899' },
  { label: '青色', value: '#06b6d4' },
  { label: '蓝色', value: '#3b82f6' },
  { label: '靛蓝', value: '#6366f1' },
  { label: '翠绿', value: '#10b981' },
  { label: '石灰', value: '#84cc16' },
  { label: '石板', value: '#64748b' },
  { label: '锌色', value: '#71717a' },
]

const folderName = ref('')
const folderIcon = ref('')
const folderColor = ref('#f59e0b')
const selectedParentId = ref(0)
const sortValue = ref(0)
const customColorInput = ref('')
const showCustomColor = ref(false)
const parentDropdownOpen = ref(false)
const isSubmitting = ref(false)

let previousScrollY = 0

const nameError = computed(() => {
  const val = folderName.value.trim()
  if (!val) return ''
  if (val.length > 50) return '名称不能超过50个字符'
  return ''
})

const canSubmit = computed(() => {
  return !isSubmitting.value && folderName.value.trim().length > 0 && !nameError.value
})

const isBusy = computed(() => isSubmitting.value)

const selectedParentName = computed(() => {
  if (selectedParentId.value === 0) return '无（顶级文件夹）'
  const found = props.parentOptions.find((opt) => opt.id === selectedParentId.value)
  return found ? found.name : '无（顶级文件夹）'
})

const isPresetColor = computed(() => {
  return colorPresets.some((p) => p.value === folderColor.value)
})

const handlePresetColorSelect = (color: string) => {
  folderColor.value = color
  customColorInput.value = ''
  showCustomColor.value = false
}

const handleCustomColorToggle = () => {
  showCustomColor.value = !showCustomColor.value
  if (showCustomColor.value && !isPresetColor.value) {
    customColorInput.value = folderColor.value
  }
}

const handleCustomColorConfirm = () => {
  const raw = customColorInput.value.trim()
  const hex = raw.startsWith('#') ? raw : '#' + raw
  if (/^#[0-9a-fA-F]{6}$/.test(hex)) {
    folderColor.value = hex
  }
}

const handleParentSelect = (id: number) => {
  selectedParentId.value = id
  parentDropdownOpen.value = false
}

const toggleParentDropdown = () => {
  parentDropdownOpen.value = !parentDropdownOpen.value
}

const closeParentDropdown = () => {
  parentDropdownOpen.value = false
}

const resetForm = () => {
  folderName.value = ''
  folderIcon.value = ''
  folderColor.value = '#f59e0b'
  selectedParentId.value = props.parentId || 0
  sortValue.value = 0
  customColorInput.value = ''
  showCustomColor.value = false
  parentDropdownOpen.value = false
  isSubmitting.value = false
}

const setDialogOpen = (value: boolean) => {
  emit('update:open', value)
}

const closeDialog = () => {
  if (isBusy.value) return
  setDialogOpen(false)
}

const lockPageScroll = () => {
  if (typeof window === 'undefined') return
  previousScrollY = window.scrollY
  document.documentElement.classList.add('folder-modal-scroll-lock')
  document.body.classList.add('folder-modal-scroll-lock')
  document.body.style.setProperty('--folder-modal-scroll-y', previousScrollY + 'px')
}

const unlockPageScroll = () => {
  if (typeof window === 'undefined') return
  document.documentElement.classList.remove('folder-modal-scroll-lock')
  document.body.classList.remove('folder-modal-scroll-lock')
  document.body.style.removeProperty('--folder-modal-scroll-y')
  window.scrollTo({ top: previousScrollY, left: 0, behavior: 'auto' })
}

const handleSubmit = async () => {
  if (!canSubmit.value) return

  const formData: FolderFormData = {
    name: folderName.value.trim(),
    icon: folderIcon.value,
    color: folderColor.value,
    parentId: selectedParentId.value,
    sort: sortValue.value,
  }

  isSubmitting.value = true

  try {
    const result = await createUserFolder(formData)
    emit('submit', formData, result)
    toast.add({
      title: '创建成功',
      description: '文件夹「' + formData.name + '」已创建',
      type: 'success',
    })
    resetForm()
    setDialogOpen(false)
  } catch (error) {
    toast.add({
      title: '创建失败',
      description: error instanceof Error ? error.message : '请稍后重试',
      type: 'error',
    })
  } finally {
    isSubmitting.value = false
  }
}

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      lockPageScroll()
      resetForm()
      selectedParentId.value = props.parentId || 0
    } else {
      unlockPageScroll()
    }
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  unlockPageScroll()
})

defineOptions({
  name: 'CreateFolderDialog',
})
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="open"
        class="fixed inset-0 z-[9999] flex items-center justify-center px-4 sm:px-6"
      >
        <div
          class="absolute inset-0 bg-black/50 backdrop-blur-sm dark:bg-black/70"
          @click="closeDialog"
        ></div>

        <Transition
          enter-active-class="transition-all duration-200 ease-out"
          enter-from-class="opacity-0 scale-95 translate-y-3"
          enter-to-class="opacity-100 scale-100 translate-y-0"
          leave-active-class="transition-all duration-150 ease-in"
          leave-from-class="opacity-100 scale-100 translate-y-0"
          leave-to-class="opacity-0 scale-95 translate-y-3"
        >
          <section
            v-if="open"
            class="folder-dialog-panel relative z-[1] flex w-full flex-col overflow-hidden rounded-xl border bg-white sm:w-[min(92vw,28rem)]"
            role="dialog"
            aria-modal="true"
            aria-labelledby="create-folder-title"
            @click.stop
          >
            <header class="flex shrink-0 items-center justify-between border-b px-5 py-4">
              <div class="flex items-center gap-3">
                <div class="folder-icon-box flex h-9 w-9 items-center justify-center rounded-lg border">
                  <FolderPlus class="h-4 w-4" />
                </div>
                <div>
                  <h2 id="create-folder-title" class="text-base font-semibold">创建收藏夹</h2>
                  <p class="folder-subtitle mt-0.5 text-xs">新建一个文件夹来整理你的收藏</p>
                </div>
              </div>
              <button
                type="button"
                class="close-btn flex h-8 w-8 cursor-pointer items-center justify-center rounded-md transition-colors"
                aria-label="关闭对话框"
                :disabled="isBusy"
                @click="closeDialog"
              >
                <X class="h-4 w-4" />
              </button>
            </header>

            <div class="flex-1 overflow-y-auto px-5 py-5" style="max-height: calc(90vh - 8rem)">
              <div class="flex flex-col gap-5">
                <!-- 文件夹名称 -->
                <div class="space-y-1.5">
                  <label for="folder-name" class="label-text block text-sm font-medium">
                    文件夹名称
                    <span class="text-red-500">*</span>
                  </label>
                  <input
                    id="folder-name"
                    v-model="folderName"
                    type="text"
                    maxlength="50"
                    placeholder="输入文件夹名称，例如：前端开发"
                    class="form-input w-full rounded-lg border px-3.5 py-2.5 text-sm outline-none transition-all"
                    @keydown.enter.prevent="handleSubmit"
                  />
                  <div class="flex items-center justify-between px-0.5">
                    <p v-if="nameError && folderName.length > 0" class="text-xs text-red-500">
                      {{ nameError }}
                    </p>
                    <p v-else class="text-xs text-transparent">-</p>
                    <span class="count-text text-xs tabular-nums">
                      {{ folderName.length }} / 50
                    </span>
                  </div>
                </div>

                <!-- 图标选择 -->
                <div class="space-y-1.5">
                  <label class="label-text block text-sm font-medium">文件夹图标</label>
                  <FontIconPicker v-model="folderIcon" placeholder="点击选择图标（可选）" />
                </div>

                <!-- 颜色选择 -->
                <div class="space-y-2">
                  <label class="label-text block text-sm font-medium">文件夹颜色</label>
                  <div class="flex flex-wrap gap-2">
                    <button
                      v-for="preset in colorPresets"
                      :key="preset.value"
                      type="button"
                      :title="preset.label"
                      class="color-swatch group relative flex h-8 w-8 cursor-pointer items-center justify-center rounded-lg border-2 transition-all duration-150"
                      :class="folderColor === preset.value ? 'scale-110 shadow-sm' : 'hover:scale-105'"
                      :style="{ backgroundColor: preset.value }"
                      @click="handlePresetColorSelect(preset.value)"
                    >
                      <svg
                        v-if="folderColor === preset.value"
                        class="h-3.5 w-3.5 text-white drop-shadow-sm"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
                      </svg>
                    </button>
                  </div>
                  <div class="flex items-center gap-2 pt-1">
                    <button
                      type="button"
                      class="custom-color-btn inline-flex cursor-pointer items-center gap-1.5 rounded-md px-2.5 py-1.5 text-xs font-medium transition-colors"
                      @click="handleCustomColorToggle"
                    >
                      <div
                        class="h-3.5 w-3.5 rounded-sm border"
                        :style="{ backgroundColor: folderColor }"
                      ></div>
                      自定义颜色
                      <ChevronDown class="h-3 w-3 transition-transform" :class="showCustomColor ? 'rotate-180' : ''" />
                    </button>
                    <div class="color-preview ml-auto flex items-center gap-1.5 rounded-md border px-2 py-1">
                      <div class="h-3 w-3 rounded-full" :style="{ backgroundColor: folderColor }"></div>
                      <span class="font-mono text-xs">{{ folderColor }}</span>
                    </div>
                  </div>
                  <Transition
                    enter-active-class="transition-all duration-160 ease-out"
                    enter-from-class="opacity-0 -translate-y-1"
                    enter-to-class="opacity-100 translate-y-0"
                    leave-active-class="transition-all duration-120 ease-in"
                    leave-from-class="opacity-100 translate-y-0"
                    leave-to-class="opacity-0 -translate-y-1"
                  >
                    <div v-if="showCustomColor" class="flex items-center gap-2">
                      <div class="relative flex-1">
                        <span class="hash-prefix pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-sm">#</span>
                        <input
                          v-model="customColorInput"
                          type="text"
                          maxlength="7"
                          placeholder="f59e0b"
                          class="form-input w-full rounded-md border py-2 pl-7 pr-3 font-mono text-sm outline-none transition-colors"
                          @keydown.enter.prevent="handleCustomColorConfirm"
                        />
                      </div>
                      <button
                        type="button"
                        class="apply-color-btn shrink-0 cursor-pointer rounded-md border px-3 py-2 text-xs font-medium transition-colors"
                        @click="handleCustomColorConfirm"
                      >
                        应用
                      </button>
                    </div>
                  </Transition>
                </div>
                <!-- 父级文件夹 -->
                <div class="space-y-1.5" @mouseleave="closeParentDropdown">
                  <label class="label-text block text-sm font-medium">父级文件夹</label>
                  <div class="relative">
                    <button
                      type="button"
                      class="form-input parent-select flex w-full cursor-pointer items-center justify-between rounded-lg border px-3.5 py-2.5 text-sm transition-colors"
                      @click="toggleParentDropdown"
                    >
                      <span class="truncate">{{ selectedParentName }}</span>
                      <ChevronDown class="h-3.5 w-3.5 shrink-0 transition-transform" :class="parentDropdownOpen ? 'rotate-180' : ''" />
                    </button>
                    <Transition
                      enter-active-class="transition-all duration-160 ease-out"
                      enter-from-class="opacity-0 -translate-y-1"
                      enter-to-class="opacity-100 translate-y-0"
                      leave-active-class="transition-all duration-100 ease-in"
                      leave-from-class="opacity-100 translate-y-0"
                      leave-to-class="opacity-0 -translate-y-1"
                    >
                      <div
                        v-if="parentDropdownOpen"
                        class="parent-dropdown absolute left-0 top-full z-10 mt-1 w-full overflow-hidden rounded-lg border shadow-lg"
                      >
                        <div class="max-h-48 overflow-y-auto py-1">
                          <button
                            type="button"
                            class="dropdown-item flex w-full cursor-pointer items-center gap-2 px-3.5 py-2.5 text-left text-sm transition-colors"
                            :class="selectedParentId === 0 ? 'active' : ''"
                            @click="handleParentSelect(0)"
                          >
                            无（顶级文件夹）
                          </button>
                          <button
                            v-for="opt in parentOptions"
                            :key="opt.id"
                            type="button"
                            class="dropdown-item flex w-full cursor-pointer items-center gap-2 px-3.5 py-2.5 text-left text-sm transition-colors"
                            :class="selectedParentId === opt.id ? 'active' : ''"
                            @click="handleParentSelect(opt.id)"
                          >
                            <div v-if="opt.color" class="h-2.5 w-2.5 shrink-0 rounded-full" :style="{ backgroundColor: opt.color }"></div>
                            <i v-if="opt.icon" :class="opt.icon" class="shrink-0 text-xs"></i>
                            <span class="truncate">{{ opt.name }}</span>
                          </button>
                        </div>
                      </div>
                    </Transition>
                  </div>
                </div>

                <!-- 排序值 -->
                <div class="space-y-1.5">
                  <label for="folder-sort" class="label-text block text-sm font-medium">
                    排序值
                    <span class="ml-1 text-xs font-normal opacity-50">（数值越小越靠前）</span>
                  </label>
                  <input
                    id="folder-sort"
                    v-model.number="sortValue"
                    type="number"
                    min="0"
                    max="9999"
                    class="form-input w-full rounded-lg border px-3.5 py-2.5 text-sm outline-none transition-all"
                  />
                </div>

                <!-- 预览 -->
                <div class="folder-preview rounded-lg border p-4">
                  <p class="preview-label mb-3 text-xs font-medium uppercase tracking-wider">预览效果</p>
                  <div class="flex items-center gap-3">
                    <div
                      class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg"
                      :style="{ backgroundColor: folderColor + '18', border: '1.5px solid ' + folderColor + '40' }"
                    >
                      <i v-if="folderIcon" :class="folderIcon" :style="{ color: folderColor }" class="text-base"></i>
                      <FolderPlus v-else class="h-4 w-4" :style="{ color: folderColor }" />
                    </div>
                    <div class="min-w-0 flex-1">
                      <p class="truncate text-sm font-medium" :class="folderName.trim() ? '' : 'opacity-40'">
                        {{ folderName.trim() || '文件夹名称' }}
                      </p>
                      <p class="preview-sub mt-0.5 text-xs">
                        {{ selectedParentId === 0 ? '顶级文件夹' : selectedParentName }}
                      </p>
                    </div>
                    <span class="preview-sort shrink-0 font-mono text-xs">{{ sortValue }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 底部操作栏 -->
            <footer class="flex shrink-0 items-center justify-end gap-3 border-t px-5 py-4">
              <button
                type="button"
                class="cancel-btn cursor-pointer rounded-lg border px-5 py-2.5 text-sm font-medium transition-colors"
                :disabled="isBusy"
                @click="closeDialog"
              >
                取消
              </button>
              <button
                type="button"
                class="submit-btn cursor-pointer rounded-lg px-5 py-2.5 text-sm font-semibold transition-all disabled:cursor-not-allowed"
                :disabled="!canSubmit"
                @click="handleSubmit"
              >
                {{ isBusy ? '创建中...' : '创建文件夹' }}
              </button>
            </footer>
          </section>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* 对话框面板 */
.folder-dialog-panel {
  border-color: rgb(229 231 235 / 0.8);
  box-shadow: 0 20px 50px rgb(245 158 11 / 0.1), 0 4px 16px rgb(0 0 0 / 0.06);
}

:root.dark .folder-dialog-panel {
  border-color: rgb(45 45 45);
  box-shadow: 0 24px 56px rgb(0 0 0 / 0.7);
  background-color: rgb(28 28 30);
}

/* 头部图标容器 */
.folder-icon-box {
  border-color: rgb(245 158 11 / 0.2);
  background-color: rgb(255 251 235);
  color: rgb(217 119 6);
}

:root.dark .folder-icon-box {
  border-color: rgb(245 158 11 / 0.25);
  background-color: rgb(245 158 11 / 0.1);
  color: rgb(251 191 36);
}

/* 头部边框 */
.folder-dialog-panel > header {
  border-color: rgb(229 231 235);
}

:root.dark .folder-dialog-panel > header {
  border-color: rgb(45 45 45);
}

/* 副标题 */
.folder-subtitle {
  color: rgb(107 114 128);
}

:root.dark .folder-subtitle {
  color: rgb(156 163 175);
}

/* 标题文字 */
.folder-dialog-panel h2 {
  color: rgb(17 24 39);
}

:root.dark .folder-dialog-panel h2 {
  color: rgb(243 244 246);
}

/* 关闭按钮 */
.close-btn {
  color: rgb(156 163 175);
}

.close-btn:hover {
  background-color: rgb(243 244 246);
  color: rgb(75 85 99);
}

:root.dark .close-btn {
  color: rgb(107 114 128);
}

:root.dark .close-btn:hover {
  background-color: rgb(45 45 45);
  color: rgb(209 213 219);
}

.close-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

/* 标签文字 */
.label-text {
  color: rgb(55 65 81);
}

:root.dark .label-text {
  color: rgb(209 213 219);
}

/* 表单输入框 */
.form-input {
  border-color: rgb(229 231 235);
  background-color: rgb(255 255 255);
  color: rgb(17 24 39);
}

.form-input::placeholder {
  color: rgb(156 163 175);
}

.form-input:focus {
  border-color: rgb(245 158 11);
  box-shadow: 0 0 0 3px rgb(245 158 11 / 0.12);
}

:root.dark .form-input {
  border-color: rgb(45 45 45);
  background-color: rgb(18 18 18);
  color: rgb(243 244 246);
}

:root.dark .form-input::placeholder {
  color: rgb(107 114 128);
}

:root.dark .form-input:focus {
  border-color: rgb(245 158 11);
  box-shadow: 0 0 0 3px rgb(245 158 11 / 0.15);
}

/* 字符计数 */
.count-text {
  color: rgb(156 163 175);
}

:root.dark .count-text {
  color: rgb(107 114 128);
}

/* 颜色色块选中态 */
.color-swatch[style] {
  border-color: transparent;
}

.color-swatch.scale-110 {
  border-color: rgb(17 24 39);
}

:root.dark .color-swatch.scale-110 {
  border-color: rgb(243 244 246);
}

.color-swatch:hover {
  border-color: rgb(209 213 219);
}

:root.dark .color-swatch:hover {
  border-color: rgb(75 85 99);
}

/* 自定义颜色按钮 */
.custom-color-btn {
  color: rgb(75 85 99);
}

.custom-color-btn:hover {
  background-color: rgb(243 244 246);
}

:root.dark .custom-color-btn {
  color: rgb(156 163 175);
}

:root.dark .custom-color-btn:hover {
  background-color: rgb(45 45 45);
}

.custom-color-btn .border {
  border-color: rgb(209 213 219);
}

:root.dark .custom-color-btn .border {
  border-color: rgb(75 85 99);
}

/* 颜色预览 */
.color-preview {
  border-color: rgb(243 244 246);
  background-color: rgb(249 250 251);
}

.color-preview span {
  color: rgb(107 114 128);
}

:root.dark .color-preview {
  border-color: rgb(45 45 45);
  background-color: rgb(18 18 18);
}

:root.dark .color-preview span {
  color: rgb(156 163 175);
}

/* # 前缀 */
.hash-prefix {
  color: rgb(156 163 175);
}

:root.dark .hash-prefix {
  color: rgb(107 114 128);
}

/* 应用颜色按钮 */
.apply-color-btn {
  border-color: rgb(245 158 11 / 0.3);
  background-color: rgb(255 251 235);
  color: rgb(217 119 6);
}

.apply-color-btn:hover {
  background-color: rgb(254 243 199);
}

:root.dark .apply-color-btn {
  border-color: rgb(245 158 11 / 0.25);
  background-color: rgb(245 158 11 / 0.1);
  color: rgb(251 191 36);
}

:root.dark .apply-color-btn:hover {
  background-color: rgb(245 158 11 / 0.2);
}

/* 父级下拉面板 */
.parent-dropdown {
  border-color: rgb(229 231 235);
  background-color: rgb(255 255 255);
}

:root.dark .parent-dropdown {
  border-color: rgb(45 45 45);
  background-color: rgb(28 28 30);
}

/* 下拉项 */
.dropdown-item {
  color: rgb(55 65 81);
}

.dropdown-item:hover {
  background-color: rgb(249 250 251);
}

.dropdown-item.active {
  background-color: rgb(255 251 235);
  color: rgb(217 119 6);
  font-weight: 500;
}

:root.dark .dropdown-item {
  color: rgb(209 213 219);
}

:root.dark .dropdown-item:hover {
  background-color: rgb(45 45 45);
}

:root.dark .dropdown-item.active {
  background-color: rgb(245 158 11 / 0.1);
  color: rgb(251 191 36);
}

/* 预览区域 */
.folder-preview {
  border-color: rgb(229 231 235);
  background-color: rgb(249 250 251);
}

:root.dark .folder-preview {
  border-color: rgb(45 45 45);
  background-color: rgb(18 18 18);
}

.preview-label {
  color: rgb(156 163 175);
}

:root.dark .preview-label {
  color: rgb(107 114 128);
}

.preview-sub {
  color: rgb(107 114 128);
}

:root.dark .preview-sub {
  color: rgb(156 163 175);
}

.preview-sort {
  color: rgb(156 163 175);
}

:root.dark .preview-sort {
  color: rgb(107 114 128);
}

/* 底部边框 */
.folder-dialog-panel > footer {
  border-color: rgb(229 231 235);
}

:root.dark .folder-dialog-panel > footer {
  border-color: rgb(45 45 45);
}

/* 取消按钮 */
.cancel-btn {
  border-color: rgb(229 231 235);
  background-color: rgb(255 255 255);
  color: rgb(55 65 81);
}

.cancel-btn:hover {
  background-color: rgb(249 250 251);
}

:root.dark .cancel-btn {
  border-color: rgb(45 45 45);
  background-color: rgb(28 28 30);
  color: rgb(209 213 219);
}

:root.dark .cancel-btn:hover {
  background-color: rgb(45 45 45);
}

.cancel-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

/* 提交按钮 */
.submit-btn {
  background-color: rgb(245 158 11);
  color: rgb(255 255 255);
}

.submit-btn:hover:not(:disabled) {
  background-color: rgb(217 119 6);
  transform: translateY(-1px);
}

.submit-btn:disabled {
  background-color: rgb(245 158 11 / 0.4);
  color: rgb(255 255 255 / 0.6);
}

:root.dark .submit-btn {
  background-color: rgb(245 158 11);
  color: rgb(17 24 39);
}

:root.dark .submit-btn:hover:not(:disabled) {
  background-color: rgb(251 191 36);
}

:root.dark .submit-btn:disabled {
  background-color: rgb(245 158 11 / 0.3);
  color: rgb(17 24 39 / 0.5);
}

/* 滚动锁定 */
:global(html.folder-modal-scroll-lock) {
  overflow: hidden;
}

:global(body.folder-modal-scroll-lock) {
  position: fixed;
  top: calc(var(--folder-modal-scroll-y, 0px) * -1);
  left: 0;
  right: 0;
  width: 100%;
  overflow: hidden;
  touch-action: none;
}

/* 移动端适配 */
@media (max-width: 640px) {
  .folder-dialog-panel {
    border-radius: 1rem;
    margin: 0.5rem;
  }
}
</style>
