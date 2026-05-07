<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import {
  AlertCircle,
  CalendarClock,
  Check,
  CircleUser,
  Loader2,
  Sparkles,
  X,
} from 'lucide-vue-next'

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
const inputRef = ref<HTMLInputElement | null>(null)

const usernameLength = computed(() => username.value.trim().length)

const isUnchanged = computed(
  () => username.value.trim() === (props.currentUsername || '').trim(),
)

const counterTone = computed(() => {
  if (usernameLength.value > 100) {
    return 'text-rose-600 dark:text-rose-400'
  }
  if (usernameLength.value > 0) {
    return 'text-stone-500 dark:text-stone-400'
  }
  return 'text-stone-400 dark:text-stone-500'
})

let previousScrollY = 0

// 锁定页面滚动，避免弹窗打开时背景跟随滚动
const lockPageScroll = () => {
  if (typeof window === 'undefined') {
    return
  }
  previousScrollY = window.scrollY
  document.body.style.position = 'fixed'
  document.body.style.top = `-${previousScrollY}px`
  document.body.style.left = '0'
  document.body.style.right = '0'
  document.body.style.width = '100%'
}

// 解锁页面滚动并恢复原滚动位置
const unlockPageScroll = () => {
  if (typeof window === 'undefined') {
    return
  }
  document.body.style.position = ''
  document.body.style.top = ''
  document.body.style.left = ''
  document.body.style.right = ''
  document.body.style.width = ''
  window.scrollTo({ top: previousScrollY, left: 0, behavior: 'auto' })
}

const closeDialog = () => {
  if (props.loading) {
    return
  }
  emit('update:open', false)
}

const submitForm = () => {
  const normalizedUsername = username.value.trim()
  if (normalizedUsername.length < 1 || normalizedUsername.length > 120) {
    localError.value = '用户名长度需在 1-120 之间'
    return
  }
  if (normalizedUsername === (props.currentUsername || '').trim()) {
    localError.value = '新用户名不能与当前用户名相同'
    return
  }
  localError.value = ''
  emit('submit', normalizedUsername)
}

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Escape') {
    closeDialog()
  }
}

watch(
  () => props.open,
  async (open) => {
    if (open) {
      username.value = props.currentUsername || ''
      localError.value = ''
      lockPageScroll()
      await nextTick()
      inputRef.value?.focus()
      inputRef.value?.select()
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
  <Teleport to="body">
    <Transition name="ucd-fade">
      <div
        v-if="open"
        class="ucd-root fixed inset-0 z-[120] flex items-end justify-center sm:items-center"
        role="dialog"
        aria-modal="true"
        aria-labelledby="ucd-title"
        @keydown="handleKeydown"
      >
        <!-- 背景遮罩：暖色调 + 模糊，弱化塑料感 -->
        <div
          class="absolute inset-0 bg-stone-950/55 backdrop-blur-sm transition-opacity"
          @click="closeDialog"
        />

        <!-- 弹窗主体 -->
        <Transition name="ucd-pop" appear>
          <div
            v-if="open"
            class="ucd-card relative z-10 w-full max-w-md overflow-hidden rounded-t-3xl border border-stone-200/80 bg-white text-stone-900 shadow-[0_24px_60px_-20px_rgb(28_25_23_/_0.35)] sm:rounded-3xl dark:border-white/[0.08] dark:bg-stone-900 dark:text-stone-100 dark:shadow-[0_24px_60px_-20px_rgb(0_0_0_/_0.7)]"
            @click.stop
          >
            <!-- Header -->
            <header class="relative flex items-start gap-4 px-6 pt-6 pb-4 sm:px-7 sm:pt-7">
              <span
                class="ucd-icon-badge flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border border-stone-200 bg-stone-50 text-stone-700 dark:border-white/10 dark:bg-white/[0.04] dark:text-stone-200"
              >
                <CircleUser :size="20" :stroke-width="1.75" />
              </span>
              <div class="flex-1 pt-0.5">
                <h2
                  id="ucd-title"
                  class="text-[17px] leading-tight font-semibold tracking-tight text-stone-900 dark:text-stone-50"
                >
                  修改用户名
                </h2>
                <p class="mt-1.5 flex items-center gap-1.5 text-xs text-stone-500 dark:text-stone-400">
                  <CalendarClock :size="13" :stroke-width="1.75" />
                  <span>每个自然月仅允许修改一次</span>
                </p>
              </div>
              <button
                type="button"
                aria-label="关闭"
                class="-mt-1 -mr-1 flex h-9 w-9 shrink-0 cursor-pointer items-center justify-center rounded-xl text-stone-400 transition-colors hover:bg-stone-100 hover:text-stone-700 dark:text-stone-500 dark:hover:bg-white/[0.06] dark:hover:text-stone-200"
                @click="closeDialog"
              >
                <X :size="18" :stroke-width="2" />
              </button>
            </header>

            <!-- Body -->
            <div class="px-6 pb-2 sm:px-7">
              <!-- 提示卡：中性灰底色，极简 -->
              <div
                class="ucd-tip flex items-start gap-3 rounded-2xl border border-stone-200/80 bg-stone-50/80 px-3.5 py-3 dark:border-white/[0.06] dark:bg-white/[0.02]"
              >
                <Sparkles
                  :size="15"
                  :stroke-width="1.75"
                  class="mt-0.5 shrink-0 text-stone-500 dark:text-stone-400"
                />
                <p class="text-[12.5px] leading-relaxed text-stone-600 dark:text-stone-300">
                  建议使用 <span class="font-medium text-stone-800 dark:text-stone-100">6–32 位</span> 英文、数字或下划线组合。修改后将立即生效。
                </p>
              </div>

              <!-- 当前用户名展示 -->
              <div
                v-if="currentUsername"
                class="mt-4 flex items-center justify-between rounded-xl border border-stone-200/70 bg-stone-50/70 px-3.5 py-2.5 dark:border-white/[0.06] dark:bg-white/[0.02]"
              >
                <span class="text-[11px] font-medium tracking-wider text-stone-400 uppercase dark:text-stone-500">
                  当前
                </span>
                <span class="font-mono text-sm text-stone-700 dark:text-stone-300">
                  {{ currentUsername }}
                </span>
              </div>

              <!-- 输入区 -->
              <div class="mt-4">
                <label for="ucd-input" class="flex items-center gap-1.5 text-[13px] font-medium text-stone-700 dark:text-stone-200">
                  <span>新用户名</span>
                  <span class="text-rose-500 dark:text-rose-400">*</span>
                </label>
                <div
                  class="ucd-input-wrap mt-2 flex items-center rounded-xl border bg-white transition-all duration-200 dark:bg-white/[0.03]"
                  :class="[
                    localError
                      ? 'border-rose-300 ring-2 ring-rose-100 dark:border-rose-400/40 dark:ring-rose-500/15'
                      : 'border-stone-200 hover:border-stone-300 focus-within:border-stone-900 focus-within:ring-4 focus-within:ring-stone-900/[0.06] dark:border-white/[0.08] dark:hover:border-white/[0.16] dark:focus-within:border-white/40 dark:focus-within:ring-white/[0.06]',
                  ]"
                >
                  <input
                    id="ucd-input"
                    ref="inputRef"
                    v-model="username"
                    type="text"
                    autocomplete="username"
                    spellcheck="false"
                    placeholder="请输入新用户名"
                    :maxlength="120"
                    :disabled="loading"
                    class="w-full flex-1 rounded-xl bg-transparent px-3.5 py-2.5 text-[14.5px] text-stone-900 placeholder:text-stone-400 focus:outline-none disabled:cursor-not-allowed disabled:opacity-60 dark:text-stone-100 dark:placeholder:text-stone-500"
                    @keydown.enter="submitForm"
                  />
                </div>

                <!-- 计数器 + 错误提示 -->
                <div class="mt-2 flex min-h-[18px] items-start justify-between gap-3">
                  <Transition name="ucd-error">
                    <p
                      v-if="localError"
                      class="flex items-start gap-1.5 text-[12px] leading-snug text-rose-600 dark:text-rose-400"
                    >
                      <AlertCircle :size="13" :stroke-width="2" class="mt-0.5 shrink-0" />
                      <span>{{ localError }}</span>
                    </p>
                    <span v-else />
                  </Transition>
                  <span
                    class="font-mono text-[11px] tabular-nums whitespace-nowrap"
                    :class="counterTone"
                  >
                    {{ usernameLength }} / 120
                  </span>
                </div>
              </div>
            </div>

            <!-- Footer -->
            <footer class="flex items-center justify-end gap-2.5 px-6 pt-5 pb-6 sm:px-7 sm:pb-7">
              <button
                type="button"
                class="inline-flex h-10 cursor-pointer items-center justify-center rounded-xl px-5 text-[13.5px] font-medium text-stone-600 transition-colors hover:bg-stone-100 hover:text-stone-800 disabled:cursor-not-allowed disabled:opacity-50 dark:text-stone-400 dark:hover:bg-white/[0.05] dark:hover:text-stone-100"
                :disabled="loading"
                @click="closeDialog"
              >
                取消
              </button>
              <button
                type="button"
                class="ucd-primary inline-flex h-10 cursor-pointer items-center justify-center gap-1.5 rounded-xl px-5 text-[13.5px] font-semibold transition-all duration-200 disabled:cursor-not-allowed"
                :disabled="loading || isUnchanged || usernameLength < 1"
                @click="submitForm"
              >
                <Loader2 v-if="loading" :size="15" :stroke-width="2.25" class="animate-spin" />
                <Check v-else :size="15" :stroke-width="2.5" />
                <span>{{ loading ? '提交中' : '确认修改' }}</span>
              </button>
            </footer>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* 主按钮：纯黑/纯白反色，极简对比 */
.ucd-primary {
  background: #1c1917;
  color: #fafaf9;
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.06),
    0 1px 2px rgb(0 0 0 / 0.18);
}
.ucd-primary:hover:not(:disabled) {
  background: #292524;
  transform: translateY(-0.5px);
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.08),
    0 6px 14px -6px rgb(0 0 0 / 0.35);
}
.ucd-primary:active:not(:disabled) {
  background: #0c0a09;
  transform: translateY(0);
  box-shadow:
    inset 0 1px 1px rgb(0 0 0 / 0.18),
    0 1px 2px rgb(0 0 0 / 0.12);
}
.ucd-primary:disabled {
  background: rgb(231 229 228);
  color: rgb(168 162 158);
  box-shadow: none;
}
:where(html.dark) .ucd-primary {
  background: #fafaf9;
  color: #1c1917;
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.6),
    0 1px 2px rgb(0 0 0 / 0.4);
}
:where(html.dark) .ucd-primary:hover:not(:disabled) {
  background: #ffffff;
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.7),
    0 6px 14px -6px rgb(0 0 0 / 0.5);
}
:where(html.dark) .ucd-primary:active:not(:disabled) {
  background: #e7e5e4;
}
:where(html.dark) .ucd-primary:disabled {
  background: rgb(255 255 255 / 0.05);
  color: rgb(120 113 108);
  box-shadow: none;
}

/* 进入/离开动画 */
.ucd-fade-enter-active,
.ucd-fade-leave-active {
  transition: opacity 200ms ease;
}
.ucd-fade-enter-from,
.ucd-fade-leave-to {
  opacity: 0;
}

.ucd-pop-enter-active {
  transition:
    opacity 220ms ease,
    transform 280ms cubic-bezier(0.22, 1, 0.36, 1);
}
.ucd-pop-leave-active {
  transition:
    opacity 160ms ease,
    transform 200ms ease;
}
.ucd-pop-enter-from {
  opacity: 0;
  transform: translateY(16px) scale(0.98);
}
.ucd-pop-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.985);
}

@media (min-width: 640px) {
  .ucd-pop-enter-from {
    transform: translateY(8px) scale(0.97);
  }
}

.ucd-error-enter-active,
.ucd-error-leave-active {
  transition:
    opacity 160ms ease,
    transform 160ms ease;
}
.ucd-error-enter-from,
.ucd-error-leave-to {
  opacity: 0;
  transform: translateY(-2px);
}

/* 减少动效偏好 */
@media (prefers-reduced-motion: reduce) {
  .ucd-fade-enter-active,
  .ucd-fade-leave-active,
  .ucd-pop-enter-active,
  .ucd-pop-leave-active,
  .ucd-error-enter-active,
  .ucd-error-leave-active {
    transition: none;
  }
}
</style>
