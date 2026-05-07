<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    open: boolean
    modelValue: string
    title?: string
    description?: string
  }>(),
  {
    title: '新增兴趣爱好',
    description: '输入一个兴趣，提交后会自动追加到标签列表。',
  },
)

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'update:modelValue', value: string): void
  (e: 'cancel'): void
  (e: 'submit'): void
}>()

const inputValue = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value),
})

const closeDialog = () => {
  emit('update:open', false)
  emit('cancel')
}

const submitDialog = () => {
  emit('submit')
}
</script>

<template>
  <Transition name="dialog-fade">
    <div
      v-if="open"
      class="cf-hobby-overlay fixed inset-0 z-50 flex items-center justify-center px-4"
      @click.self="closeDialog"
      @keydown.esc.prevent.stop="closeDialog"
    >
      <div
        class="cf-hobby-dialog w-full max-w-md overflow-hidden rounded-2xl border border-slate-200 bg-white p-5 shadow-xl dark:border-white/10 dark:bg-[#0e0e10]"
      >
        <div class="cf-hobby-accent" aria-hidden="true"></div>

        <div class="flex items-start gap-3">
          <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-amber-100 text-amber-600 dark:bg-amber-500/15 dark:text-amber-400">
            <UIcon name="i-lucide-tag" class="h-4 w-4" />
          </span>
          <div class="space-y-1">
            <h4 class="text-base font-semibold text-slate-900 dark:text-white">{{ title }}</h4>
            <p class="text-xs text-slate-500 dark:text-slate-400">{{ description }}</p>
          </div>
        </div>

        <div class="mt-4">
          <UInput
            v-model="inputValue"
            placeholder="例如：摄影、马拉松、机械键盘"
            autofocus
            class="w-full"
            @keyup.enter="submitDialog"
          />
          <p class="mt-2 text-[11px] text-slate-400 dark:text-slate-500">
            <UIcon name="i-lucide-corner-down-left" class="-mt-0.5 mr-1 inline h-3 w-3" />
            按回车快速提交
          </p>
        </div>

        <div class="mt-5 flex items-center justify-end gap-2">
          <button
            type="button"
            class="inline-flex h-9 cursor-pointer items-center justify-center rounded-lg border border-slate-200 bg-white px-4 text-sm font-medium text-slate-700 transition-colors hover:border-slate-300 hover:bg-slate-50 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-200 dark:hover:border-white/20 dark:hover:bg-white/[0.06]"
            @click="closeDialog"
          >
            取消
          </button>
          <button
            type="button"
            class="cf-dialog-primary inline-flex h-9 cursor-pointer items-center justify-center gap-1.5 rounded-lg px-4 text-sm font-semibold transition-all duration-200"
            @click="submitDialog"
          >
            <UIcon name="i-lucide-plus" class="h-4 w-4" />
            <span>添加</span>
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.cf-hobby-overlay {
  background-color: rgb(8 8 10 / 0.55);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.cf-hobby-dialog {
  position: relative;
  box-shadow: 0 24px 48px -16px rgb(15 23 42 / 0.35);
}

:where(html.dark) .cf-hobby-dialog {
  box-shadow: 0 24px 48px -16px rgb(0 0 0 / 0.7);
}

.cf-hobby-accent {
  position: absolute;
  inset: 0 0 auto 0;
  height: 1px;
  background-image: linear-gradient(
    90deg,
    transparent 0%,
    rgb(245 158 11 / 0.5) 50%,
    transparent 100%
  );
}

.cf-dialog-primary {
  background: #f59e0b;
  color: #1f1300;
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.35) inset,
    0 6px 14px -8px rgb(245 158 11 / 0.55);
}
.cf-dialog-primary:hover {
  background: #fbbf24;
  transform: translateY(-1px);
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.45) inset,
    0 10px 20px -10px rgb(245 158 11 / 0.65);
}
.cf-dialog-primary:active {
  background: #d97706;
  transform: translateY(0);
}

.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.2s ease;
}
.dialog-fade-enter-active .cf-hobby-dialog,
.dialog-fade-leave-active .cf-hobby-dialog {
  transition: transform 0.22s ease, opacity 0.2s ease;
}

.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}
.dialog-fade-enter-from .cf-hobby-dialog {
  opacity: 0;
  transform: translateY(8px) scale(0.98);
}
.dialog-fade-leave-to .cf-hobby-dialog {
  opacity: 0;
  transform: translateY(4px);
}
</style>
