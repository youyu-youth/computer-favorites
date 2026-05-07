<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'

interface Props {
  open: boolean
  currentUsername?: string
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  currentUsername: '',
  loading: false,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', value: string): void
}>()

const username = ref('')
const localError = ref('')

const usernameLength = computed(() => username.value.trim().length)

// 记录页面原始滚动位置，确保关闭弹窗后还原
let previousScrollY = 0

const lockPageScroll = () => {
  if (typeof window === 'undefined') {
    return
  }
  previousScrollY = window.scrollY
  const scrollValue = `${previousScrollY}px`
  document.documentElement.classList.add('username-modal-scroll-lock')
  document.body.classList.add('username-modal-scroll-lock')
  document.body.style.setProperty('--username-modal-scroll-y', scrollValue)
}

const unlockPageScroll = () => {
  if (typeof window === 'undefined') {
    return
  }
  document.documentElement.classList.remove('username-modal-scroll-lock')
  document.body.classList.remove('username-modal-scroll-lock')
  document.body.style.removeProperty('--username-modal-scroll-y')
  window.scrollTo({ top: previousScrollY, left: 0, behavior: 'auto' })
}

const closeDialog = () => {
  emit('update:open', false)
}

const submitForm = () => {
  const normalizedUsername = username.value.trim()
  if (normalizedUsername.length < 1 || normalizedUsername.length > 120) {
    localError.value = '用户名长度需在1-120之间'
    return
  }
  localError.value = ''
  emit('submit', normalizedUsername)
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      username.value = props.currentUsername || ''
      localError.value = ''
      lockPageScroll()
      return
    }
    unlockPageScroll()
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  unlockPageScroll()
})

defineOptions({
  name: 'UsernameEditDialog',
})
</script>

<template>
  <UModal
    :open="open"
    :portal="true"
    title="修改用户名"
    description="每个自然月仅允许修改一次"
    :ui="{
      overlay: 'username-edit-modal-overlay z-[120]',
      content: 'username-edit-modal-content z-[130]',
      header: 'username-edit-modal-header',
      title: 'username-edit-modal-title',
      description: 'username-edit-modal-description',
      close: 'username-edit-modal-close cursor-pointer',
      body: 'username-edit-modal-body',
      footer: 'username-edit-modal-footer',
    }"
    @update:open="emit('update:open', $event)"
  >
    <template #body>
      <div class="cf-dialog-body space-y-3">
        <div class="flex items-start gap-3 rounded-lg border border-amber-200 bg-amber-50/60 p-3 dark:border-amber-400/20 dark:bg-amber-500/[0.06]">
          <span class="mt-0.5 flex h-7 w-7 shrink-0 items-center justify-center rounded-md bg-amber-100 text-amber-600 dark:bg-amber-500/15 dark:text-amber-400">
            <UIcon name="i-lucide-id-card" class="h-3.5 w-3.5" />
          </span>
          <p class="text-xs text-amber-800 dark:text-amber-200">
            每个自然月仅允许修改一次。建议使用 6–32 位的英文 / 数字 / 下划线组合。
          </p>
        </div>

        <UFormField label="新用户名" required>
          <UInput v-model="username" placeholder="请输入新用户名" :maxlength="120" class="w-full" />
        </UFormField>

        <div class="flex items-center justify-between">
          <p
            class="font-mono text-[11px] tabular-nums"
            :class="usernameLength > 100 ? 'text-amber-600 dark:text-amber-400' : 'text-slate-400 dark:text-slate-500'"
          >
            {{ usernameLength }} / 120
          </p>
          <p
            v-if="localError"
            class="flex items-center gap-1.5 text-xs text-rose-600 dark:text-rose-400"
          >
            <UIcon name="i-lucide-alert-circle" class="h-3.5 w-3.5" />
            <span>{{ localError }}</span>
          </p>
        </div>
      </div>
    </template>

    <template #footer>
      <div class="username-edit-modal-actions flex w-full justify-end gap-2">
        <button
          type="button"
          class="inline-flex h-10 cursor-pointer items-center justify-center rounded-lg border border-slate-200 bg-white px-4 text-sm font-medium text-slate-700 transition-colors hover:border-slate-300 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-200 dark:hover:border-white/20 dark:hover:bg-white/[0.06]"
          :disabled="loading"
          @click="closeDialog"
        >
          取消
        </button>
        <button
          type="button"
          class="cf-dialog-primary inline-flex h-10 cursor-pointer items-center justify-center gap-1.5 rounded-lg px-5 text-sm font-semibold transition-all duration-200 disabled:cursor-not-allowed"
          :disabled="loading"
          @click="submitForm"
        >
          <UIcon
            v-if="loading"
            name="i-lucide-loader-2"
            class="h-4 w-4 animate-spin"
          />
          <UIcon v-else name="i-lucide-check" class="h-4 w-4" />
          <span>{{ loading ? '提交中…' : '确认修改' }}</span>
        </button>
      </div>
    </template>
  </UModal>
</template>

<style scoped>
.cf-dialog-primary {
  background: #f59e0b;
  color: #1f1300;
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.35) inset,
    0 8px 18px -10px rgb(245 158 11 / 0.55);
}
.cf-dialog-primary:hover:not(:disabled) {
  background: #fbbf24;
  transform: translateY(-1px);
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.45) inset,
    0 12px 22px -10px rgb(245 158 11 / 0.65);
}
.cf-dialog-primary:active:not(:disabled) {
  background: #d97706;
  transform: translateY(0);
}
.cf-dialog-primary:disabled {
  background: rgb(120 113 108 / 0.5);
  color: rgb(214 211 209);
  box-shadow: none;
}

:where(html.dark) .cf-dialog-primary:disabled {
  background: rgb(255 255 255 / 0.06);
  color: rgb(148 163 184);
}
</style>
