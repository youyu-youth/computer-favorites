<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 密码验证弹窗 — 用于隐藏、删除、显示隐藏收藏夹等敏感操作
 */
import { computed, ref, watch, onBeforeUnmount, onMounted, nextTick } from 'vue'
import { X, Lock } from 'lucide-vue-next'

interface Props {
  visible: boolean
  title: string
  description: string
  confirmText?: string
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  confirmText: '确认',
  loading: false,
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'confirm', password: string): void
}>()

const password = ref('')
const inputRef = ref<HTMLInputElement | null>(null)
const shake = ref(false)

const canSubmit = computed(() => {
  return password.value.trim().length > 0 && !props.loading
})

const closeDialog = () => {
  if (props.loading) return
  emit('update:visible', false)
}

const handleConfirm = () => {
  if (!canSubmit.value) {
    shake.value = true
    setTimeout(() => (shake.value = false), 400)
    return
  }
  emit('confirm', password.value.trim())
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    e.preventDefault()
    handleConfirm()
  }
  if (e.key === 'Escape') {
    closeDialog()
  }
}

let previousScrollY = 0

const lockPageScroll = () => {
  if (typeof window === 'undefined') return
  previousScrollY = window.scrollY
  document.documentElement.classList.add('pwd-modal-scroll-lock')
  document.body.classList.add('pwd-modal-scroll-lock')
  document.body.style.setProperty('--pwd-modal-scroll-y', previousScrollY + 'px')
}

const unlockPageScroll = () => {
  if (typeof window === 'undefined') return
  document.documentElement.classList.remove('pwd-modal-scroll-lock')
  document.body.classList.remove('pwd-modal-scroll-lock')
  document.body.style.removeProperty('--pwd-modal-scroll-y')
  window.scrollTo({ top: previousScrollY, left: 0, behavior: 'auto' })
}

watch(
  () => props.visible,
  async (val) => {
    if (val) {
      password.value = ''
      lockPageScroll()
      await nextTick()
      inputRef.value?.focus()
      document.addEventListener('keydown', handleKeydown)
    } else {
      unlockPageScroll()
      document.removeEventListener('keydown', handleKeydown)
    }
  },
)

onMounted(() => {
  if (props.visible) document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  unlockPageScroll()
  document.removeEventListener('keydown', handleKeydown)
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
        v-if="visible"
        class="fixed inset-0 z-[9998] flex items-center justify-center px-4 sm:px-6"
      >
        <div
          class="absolute inset-0 bg-black/50 backdrop-blur-sm dark:bg-black/70"
          @click="closeDialog"
        />

        <Transition
          enter-active-class="transition-all duration-200 ease-out"
          enter-from-class="opacity-0 scale-95 translate-y-3"
          enter-to-class="opacity-100 scale-100 translate-y-0"
          leave-active-class="transition-all duration-150 ease-in"
          leave-from-class="opacity-100 scale-100 translate-y-0"
          leave-to-class="opacity-0 scale-95 translate-y-3"
        >
          <section
            v-if="visible"
            class="pwd-dialog-panel relative z-[1] flex w-full flex-col overflow-hidden rounded-xl border sm:w-[min(92vw,24rem)]"
            :class="shake ? 'animate-shake' : ''"
            role="dialog"
            aria-modal="true"
            @click.stop
          >
            <header class="flex shrink-0 items-center justify-between border-b px-5 py-4">
              <div class="flex items-center gap-3">
                <div class="pwd-icon-box flex h-9 w-9 items-center justify-center rounded-lg border">
                  <Lock class="h-4 w-4" />
                </div>
                <div>
                  <h2 class="text-base font-semibold">{{ title }}</h2>
                  <p class="pwd-subtitle mt-0.5 text-xs">{{ description }}</p>
                </div>
              </div>
              <button
                type="button"
                class="pwd-close-btn flex h-8 w-8 cursor-pointer items-center justify-center rounded-md transition-colors"
                aria-label="关闭对话框"
                :disabled="loading"
                @click="closeDialog"
              >
                <X class="h-4 w-4" />
              </button>
            </header>

            <div class="px-5 py-5">
              <div class="space-y-1.5">
                <label for="pwd-input" class="pwd-label block text-sm font-medium">
                  密码
                </label>
                <input
                  id="pwd-input"
                  ref="inputRef"
                  v-model="password"
                  type="password"
                  placeholder="请输入密码"
                  class="pwd-input w-full rounded-lg border px-3.5 py-2.5 text-sm outline-none transition-all"
                  :disabled="loading"
                  @keydown.enter.prevent="handleConfirm"
                />
              </div>
            </div>

            <footer class="flex shrink-0 items-center justify-end gap-3 border-t px-5 py-4">
              <button
                type="button"
                class="pwd-cancel-btn cursor-pointer rounded-lg border px-5 py-2.5 text-sm font-medium transition-colors"
                :disabled="loading"
                @click="closeDialog"
              >
                取消
              </button>
              <button
                type="button"
                class="pwd-submit-btn cursor-pointer rounded-lg px-5 py-2.5 text-sm font-semibold transition-all disabled:cursor-not-allowed"
                :disabled="!canSubmit"
                @click="handleConfirm"
              >
                {{ loading ? '验证中...' : confirmText }}
              </button>
            </footer>
          </section>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.pwd-dialog-panel {
  border-color: rgb(229 231 235 / 0.8);
  background-color: rgb(255 255 255);
  box-shadow: 0 20px 50px rgb(59 130 246 / 0.1), 0 4px 16px rgb(0 0 0 / 0.06);
}

:root.dark .pwd-dialog-panel {
  border-color: rgb(45 45 45);
  box-shadow: 0 24px 56px rgb(0 0 0 / 0.7);
  background-color: rgb(28 28 30);
}

.pwd-icon-box {
  border-color: rgb(59 130 246 / 0.2);
  background-color: rgb(239 246 255);
  color: rgb(37 99 235);
}

:root.dark .pwd-icon-box {
  border-color: rgb(59 130 246 / 0.25);
  background-color: rgb(59 130 246 / 0.1);
  color: rgb(96 165 250);
}

.pwd-dialog-panel > header {
  border-color: rgb(229 231 235);
}

:root.dark .pwd-dialog-panel > header {
  border-color: rgb(45 45 45);
}

.pwd-subtitle {
  color: rgb(107 114 128);
}

:root.dark .pwd-subtitle {
  color: rgb(156 163 175);
}

.pwd-dialog-panel h2 {
  color: rgb(17 24 39);
}

:root.dark .pwd-dialog-panel h2 {
  color: rgb(243 244 246);
}

.pwd-close-btn {
  color: rgb(156 163 175);
}

.pwd-close-btn:hover {
  background-color: rgb(243 244 246);
  color: rgb(75 85 99);
}

:root.dark .pwd-close-btn {
  color: rgb(107 114 128);
}

:root.dark .pwd-close-btn:hover {
  background-color: rgb(45 45 45);
  color: rgb(209 213 219);
}

.pwd-close-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.pwd-label {
  color: rgb(55 65 81);
}

:root.dark .pwd-label {
  color: rgb(209 213 219);
}

.pwd-input {
  border-color: rgb(229 231 235);
  background-color: rgb(255 255 255);
  color: rgb(17 24 39);
}

.pwd-input::placeholder {
  color: rgb(156 163 175);
}

.pwd-input:focus {
  border-color: rgb(59 130 246);
  box-shadow: 0 0 0 3px rgb(59 130 246 / 0.12);
}

.pwd-input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

:root.dark .pwd-input {
  border-color: rgb(45 45 45);
  background-color: rgb(18 18 18);
  color: rgb(243 244 246);
}

:root.dark .pwd-input::placeholder {
  color: rgb(107 114 128);
}

:root.dark .pwd-input:focus {
  border-color: rgb(59 130 246);
  box-shadow: 0 0 0 3px rgb(59 130 246 / 0.15);
}

.pwd-dialog-panel > footer {
  border-color: rgb(229 231 235);
}

:root.dark .pwd-dialog-panel > footer {
  border-color: rgb(45 45 45);
}

.pwd-cancel-btn {
  border-color: rgb(229 231 235);
  background-color: rgb(255 255 255);
  color: rgb(55 65 81);
}

.pwd-cancel-btn:hover {
  background-color: rgb(249 250 251);
}

.pwd-cancel-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

:root.dark .pwd-cancel-btn {
  border-color: rgb(45 45 45);
  background-color: rgb(28 28 30);
  color: rgb(209 213 219);
}

:root.dark .pwd-cancel-btn:hover {
  background-color: rgb(45 45 45);
}

.pwd-submit-btn {
  background-color: rgb(59 130 246);
  color: rgb(255 255 255);
}

.pwd-submit-btn:hover:not(:disabled) {
  background-color: rgb(37 99 235);
  transform: translateY(-1px);
}

.pwd-submit-btn:disabled {
  background-color: rgb(59 130 246 / 0.4);
  color: rgb(255 255 255 / 0.6);
}

:root.dark .pwd-submit-btn {
  background-color: rgb(59 130 246);
  color: rgb(17 24 39);
}

:root.dark .pwd-submit-btn:hover:not(:disabled) {
  background-color: rgb(96 165 250);
}

:root.dark .pwd-submit-btn:disabled {
  background-color: rgb(59 130 246 / 0.3);
  color: rgb(17 24 39 / 0.5);
}

.animate-shake {
  animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-3px, 0, 0); }
  40%, 60% { transform: translate3d(3px, 0, 0); }
}

:global(html.pwd-modal-scroll-lock) {
  overflow: hidden;
}

:global(body.pwd-modal-scroll-lock) {
  position: fixed;
  top: calc(var(--pwd-modal-scroll-y, 0px) * -1);
  left: 0;
  right: 0;
  width: 100%;
  overflow: hidden;
  touch-action: none;
}

@media (max-width: 640px) {
  .pwd-dialog-panel {
    border-radius: 1rem;
    margin: 0.5rem;
  }
}
</style>
