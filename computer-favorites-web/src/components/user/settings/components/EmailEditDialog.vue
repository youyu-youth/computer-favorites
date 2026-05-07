<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import {
  AlertCircle,
  AtSign,
  Check,
  KeyRound,
  Loader2,
  Mail,
  ShieldCheck,
  Timer,
  X,
} from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import { sendEmailUpdateCode } from '@/api/user'

interface Props {
  open: boolean
  currentEmail?: string
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  currentEmail: '',
  loading: false,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', payload: { email: string; emailCode: string }): void
}>()

const toast = useToast()
const email = ref('')
const emailCode = ref('')
const localError = ref('')
const sendCodeLoading = ref(false)
const sendCodeCountdown = ref(0)
const emailInputRef = ref<HTMLInputElement | null>(null)
let sendCodeTimer: number | null = null
let previousScrollY = 0

const isValidEmail = (value: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)

const trimmedEmail = computed(() => email.value.trim())
const trimmedCode = computed(() => emailCode.value.trim())

const canSendCode = computed(
  () =>
    !sendCodeLoading.value &&
    sendCodeCountdown.value === 0 &&
    !props.loading &&
    isValidEmail(trimmedEmail.value) &&
    trimmedEmail.value !== (props.currentEmail || '').trim(),
)

const canSubmit = computed(
  () =>
    !props.loading &&
    isValidEmail(trimmedEmail.value) &&
    trimmedEmail.value !== (props.currentEmail || '').trim() &&
    /^\d{6}$/.test(trimmedCode.value),
)

// 停止验证码倒计时，避免组件销毁后残留定时器
const stopSendCodeTimer = () => {
  if (sendCodeTimer === null) {
    return
  }
  window.clearInterval(sendCodeTimer)
  sendCodeTimer = null
}

// 启动验证码倒计时，防止短时间重复发送
const startSendCodeCountdown = () => {
  stopSendCodeTimer()
  sendCodeCountdown.value = 60
  sendCodeTimer = window.setInterval(() => {
    if (sendCodeCountdown.value <= 1) {
      sendCodeCountdown.value = 0
      stopSendCodeTimer()
      return
    }
    sendCodeCountdown.value -= 1
  }, 1000)
}

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

const validateForm = (): string => {
  const normalizedEmail = email.value.trim()
  const normalizedEmailCode = emailCode.value.trim()

  if (!normalizedEmail) {
    return '请输入新邮箱'
  }
  if (!isValidEmail(normalizedEmail)) {
    return '请输入有效的邮箱地址'
  }
  if (normalizedEmail === props.currentEmail) {
    return '新邮箱不能与当前邮箱相同'
  }
  if (!normalizedEmailCode) {
    return '请输入邮箱验证码'
  }
  if (!/^\d{6}$/.test(normalizedEmailCode)) {
    return '验证码必须为6位数字'
  }
  return ''
}

const handleSendCode = async () => {
  if (sendCodeLoading.value || sendCodeCountdown.value > 0 || props.loading) {
    return
  }

  const normalizedEmail = email.value.trim()
  if (!normalizedEmail) {
    localError.value = '请先输入新邮箱'
    return
  }
  if (!isValidEmail(normalizedEmail)) {
    localError.value = '请输入有效的邮箱地址'
    return
  }
  if (normalizedEmail === props.currentEmail) {
    localError.value = '新邮箱不能与当前邮箱相同'
    return
  }

  localError.value = ''
  sendCodeLoading.value = true
  try {
    await sendEmailUpdateCode({ email: normalizedEmail })
    startSendCodeCountdown()
    toast.add({
      title: '发送成功',
      description: '验证码已发送到新邮箱，请注意查收',
      type: 'success',
    })
  } catch (error) {
    localError.value = error instanceof Error ? error.message : '验证码发送失败，请稍后重试'
  } finally {
    sendCodeLoading.value = false
  }
}

const submitForm = () => {
  const validationError = validateForm()
  if (validationError) {
    localError.value = validationError
    return
  }
  localError.value = ''
  emit('submit', {
    email: email.value.trim(),
    emailCode: emailCode.value.trim(),
  })
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
      email.value = ''
      emailCode.value = ''
      localError.value = ''
      lockPageScroll()
      await nextTick()
      emailInputRef.value?.focus()
      return
    }
    stopSendCodeTimer()
    sendCodeCountdown.value = 0
    unlockPageScroll()
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  stopSendCodeTimer()
  unlockPageScroll()
})

defineOptions({
  name: 'EmailEditDialog',
})
</script>

<template>
  <Teleport to="body">
    <Transition name="ecd-fade">
      <div
        v-if="open"
        class="ecd-root fixed inset-0 z-[120] flex items-end justify-center sm:items-center"
        role="dialog"
        aria-modal="true"
        aria-labelledby="ecd-title"
        @keydown="handleKeydown"
      >
        <!-- 背景遮罩 -->
        <div
          class="absolute inset-0 bg-stone-950/55 backdrop-blur-sm transition-opacity"
          @click="closeDialog"
        />

        <!-- 弹窗主体 -->
        <Transition name="ecd-pop" appear>
          <div
            v-if="open"
            class="ecd-card relative z-10 flex max-h-[92vh] w-full max-w-md flex-col overflow-hidden rounded-t-3xl border border-stone-200/80 bg-white text-stone-900 shadow-[0_24px_60px_-20px_rgb(28_25_23_/_0.35)] sm:rounded-3xl dark:border-white/[0.08] dark:bg-stone-900 dark:text-stone-100 dark:shadow-[0_24px_60px_-20px_rgb(0_0_0_/_0.7)]"
            @click.stop
          >
            <!-- Header -->
            <header class="relative flex items-start gap-4 px-6 pt-6 pb-4 sm:px-7 sm:pt-7">
              <span
                class="ecd-icon-badge flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border border-stone-200 bg-stone-50 text-stone-700 dark:border-white/10 dark:bg-white/[0.04] dark:text-stone-200"
              >
                <Mail :size="20" :stroke-width="1.75" />
              </span>
              <div class="flex-1 pt-0.5">
                <h2
                  id="ecd-title"
                  class="text-[17px] leading-tight font-semibold tracking-tight text-stone-900 dark:text-stone-50"
                >
                  修改邮箱
                </h2>
                <p class="mt-1.5 flex items-center gap-1.5 text-xs text-stone-500 dark:text-stone-400">
                  <ShieldCheck :size="13" :stroke-width="1.75" />
                  <span>需通过新邮箱验证码校验</span>
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

            <!-- Body：可滚动 -->
            <div class="ecd-body flex-1 overflow-y-auto px-6 pb-2 sm:px-7">
              <!-- 提示卡：中性灰底色，极简 -->
              <div
                class="ecd-tip flex items-start gap-3 rounded-2xl border border-stone-200/80 bg-stone-50/80 px-3.5 py-3 dark:border-white/[0.06] dark:bg-white/[0.02]"
              >
                <ShieldCheck
                  :size="15"
                  :stroke-width="1.75"
                  class="mt-0.5 shrink-0 text-stone-500 dark:text-stone-400"
                />
                <p class="text-[12.5px] leading-relaxed text-stone-600 dark:text-stone-300">
                  修改成功后，下次登录请使用新邮箱。验证码将发送至新邮箱地址。
                </p>
              </div>

              <!-- 当前邮箱（只读卡片，更精致） -->
              <div class="mt-4">
                <div class="text-[11px] font-medium tracking-wider text-stone-400 uppercase dark:text-stone-500">
                  当前邮箱
                </div>
                <div
                  class="mt-1.5 flex items-center gap-2.5 rounded-xl border border-stone-200/70 bg-stone-50/70 px-3.5 py-2.5 dark:border-white/[0.06] dark:bg-white/[0.02]"
                >
                  <AtSign
                    :size="15"
                    :stroke-width="1.75"
                    class="shrink-0 text-stone-400 dark:text-stone-500"
                  />
                  <span class="truncate font-mono text-[13.5px] text-stone-700 dark:text-stone-300">
                    {{ currentEmail || '未绑定邮箱' }}
                  </span>
                </div>
              </div>

              <!-- 新邮箱 -->
              <div class="mt-4">
                <label for="ecd-email" class="flex items-center gap-1.5 text-[13px] font-medium text-stone-700 dark:text-stone-200">
                  <span>新邮箱</span>
                  <span class="text-rose-500 dark:text-rose-400">*</span>
                </label>
                <div
                  class="ecd-input-wrap mt-2 flex items-center gap-2 rounded-xl border bg-white pl-3 transition-all duration-200 dark:bg-white/[0.03]"
                  :class="[
                    localError && !trimmedEmail
                      ? 'border-rose-300 ring-2 ring-rose-100 dark:border-rose-400/40 dark:ring-rose-500/15'
                      : 'border-stone-200 hover:border-stone-300 focus-within:border-stone-900 focus-within:ring-4 focus-within:ring-stone-900/[0.06] dark:border-white/[0.08] dark:hover:border-white/[0.16] dark:focus-within:border-white/40 dark:focus-within:ring-white/[0.06]',
                  ]"
                >
                  <AtSign
                    :size="15"
                    :stroke-width="1.75"
                    class="shrink-0 text-stone-400 dark:text-stone-500"
                  />
                  <input
                    id="ecd-email"
                    ref="emailInputRef"
                    v-model="email"
                    type="email"
                    autocomplete="email"
                    spellcheck="false"
                    placeholder="请输入新的邮箱地址"
                    :disabled="loading"
                    class="w-full flex-1 rounded-xl bg-transparent py-2.5 pr-3.5 font-mono text-[13.5px] text-stone-900 placeholder:text-stone-400 placeholder:font-sans focus:outline-none disabled:cursor-not-allowed disabled:opacity-60 dark:text-stone-100 dark:placeholder:text-stone-500"
                  />
                </div>
              </div>

              <!-- 验证码 -->
              <div class="mt-4">
                <label for="ecd-code" class="flex items-center gap-1.5 text-[13px] font-medium text-stone-700 dark:text-stone-200">
                  <span>邮箱验证码</span>
                  <span class="text-rose-500 dark:text-rose-400">*</span>
                </label>
                <div class="mt-2 flex flex-col gap-2 sm:flex-row sm:items-stretch">
                  <div
                    class="ecd-input-wrap flex flex-1 items-center gap-2 rounded-xl border bg-white pl-3 transition-all duration-200 dark:bg-white/[0.03]"
                    :class="[
                      localError && trimmedCode.length > 0 && !/^\d{6}$/.test(trimmedCode)
                        ? 'border-rose-300 ring-2 ring-rose-100 dark:border-rose-400/40 dark:ring-rose-500/15'
                        : 'border-stone-200 hover:border-stone-300 focus-within:border-stone-900 focus-within:ring-4 focus-within:ring-stone-900/[0.06] dark:border-white/[0.08] dark:hover:border-white/[0.16] dark:focus-within:border-white/40 dark:focus-within:ring-white/[0.06]',
                    ]"
                  >
                    <KeyRound
                      :size="15"
                      :stroke-width="1.75"
                      class="shrink-0 text-stone-400 dark:text-stone-500"
                    />
                    <input
                      id="ecd-code"
                      v-model="emailCode"
                      type="text"
                      inputmode="numeric"
                      autocomplete="one-time-code"
                      maxlength="6"
                      placeholder="6 位数字验证码"
                      :disabled="loading"
                      class="ecd-code-input w-full flex-1 rounded-xl bg-transparent py-2.5 pr-3.5 font-mono text-[14px] tracking-[0.32em] text-stone-900 placeholder:text-[12.5px] placeholder:tracking-normal placeholder:text-stone-400 placeholder:font-sans focus:outline-none disabled:cursor-not-allowed disabled:opacity-60 dark:text-stone-100 dark:placeholder:text-stone-500"
                      @keydown.enter="canSubmit && submitForm()"
                    />
                  </div>
                  <button
                    type="button"
                    class="ecd-send-btn inline-flex h-11 shrink-0 cursor-pointer items-center justify-center gap-1.5 rounded-xl border px-4 text-[12.5px] font-medium transition-all duration-200 disabled:cursor-not-allowed sm:h-auto"
                    :disabled="!canSendCode"
                    @click="handleSendCode"
                  >
                    <Loader2
                      v-if="sendCodeLoading"
                      :size="14"
                      :stroke-width="2"
                      class="animate-spin"
                    />
                    <Timer
                      v-else-if="sendCodeCountdown > 0"
                      :size="14"
                      :stroke-width="1.75"
                    />
                    <Mail v-else :size="14" :stroke-width="1.75" />
                    <span v-if="sendCodeLoading">发送中</span>
                    <span v-else-if="sendCodeCountdown > 0" class="font-mono tabular-nums">
                      {{ sendCodeCountdown }}s
                    </span>
                    <span v-else>发送验证码</span>
                  </button>
                </div>
              </div>

              <!-- 错误提示 -->
              <div class="mt-3 min-h-[18px]">
                <Transition name="ecd-error">
                  <p
                    v-if="localError"
                    class="flex items-start gap-1.5 text-[12px] leading-snug text-rose-600 dark:text-rose-400"
                  >
                    <AlertCircle :size="13" :stroke-width="2" class="mt-0.5 shrink-0" />
                    <span>{{ localError }}</span>
                  </p>
                </Transition>
              </div>
            </div>

            <!-- Footer -->
            <footer class="flex items-center justify-end gap-2.5 border-t border-stone-100 px-6 pt-4 pb-6 sm:px-7 sm:pb-7 dark:border-white/[0.04]">
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
                class="ecd-primary inline-flex h-10 cursor-pointer items-center justify-center gap-1.5 rounded-xl px-5 text-[13.5px] font-semibold transition-all duration-200 disabled:cursor-not-allowed"
                :disabled="loading || !canSubmit"
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
.ecd-primary {
  background: #1c1917;
  color: #fafaf9;
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.06),
    0 1px 2px rgb(0 0 0 / 0.18);
}
.ecd-primary:hover:not(:disabled) {
  background: #292524;
  transform: translateY(-0.5px);
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.08),
    0 6px 14px -6px rgb(0 0 0 / 0.35);
}
.ecd-primary:active:not(:disabled) {
  background: #0c0a09;
  transform: translateY(0);
  box-shadow:
    inset 0 1px 1px rgb(0 0 0 / 0.18),
    0 1px 2px rgb(0 0 0 / 0.12);
}
.ecd-primary:disabled {
  background: rgb(231 229 228);
  color: rgb(168 162 158);
  box-shadow: none;
}
:where(html.dark) .ecd-primary {
  background: #fafaf9;
  color: #1c1917;
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.6),
    0 1px 2px rgb(0 0 0 / 0.4);
}
:where(html.dark) .ecd-primary:hover:not(:disabled) {
  background: #ffffff;
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.7),
    0 6px 14px -6px rgb(0 0 0 / 0.5);
}
:where(html.dark) .ecd-primary:active:not(:disabled) {
  background: #e7e5e4;
}
:where(html.dark) .ecd-primary:disabled {
  background: rgb(255 255 255 / 0.05);
  color: rgb(120 113 108);
  box-shadow: none;
}

/* 发送验证码：次级按钮，中性灰颜色 */
.ecd-send-btn {
  border-color: rgb(231 229 228);
  background: white;
  color: rgb(87 83 78);
}
.ecd-send-btn:hover:not(:disabled) {
  border-color: rgb(168 162 158);
  background: rgb(245 245 244);
  color: rgb(28 25 23);
}
.ecd-send-btn:active:not(:disabled) {
  background: rgb(231 229 228);
}
.ecd-send-btn:disabled {
  opacity: 0.55;
}
:where(html.dark) .ecd-send-btn {
  border-color: rgb(255 255 255 / 0.08);
  background: rgb(255 255 255 / 0.02);
  color: rgb(214 211 209);
}
:where(html.dark) .ecd-send-btn:hover:not(:disabled) {
  border-color: rgb(255 255 255 / 0.2);
  background: rgb(255 255 255 / 0.05);
  color: rgb(250 250 249);
}

/* Body 滚动条美化 */
.ecd-body {
  scrollbar-width: thin;
  scrollbar-color: rgb(231 229 228) transparent;
}
.ecd-body::-webkit-scrollbar {
  width: 6px;
}
.ecd-body::-webkit-scrollbar-thumb {
  background: rgb(231 229 228);
  border-radius: 3px;
}
:where(html.dark) .ecd-body {
  scrollbar-color: rgb(255 255 255 / 0.08) transparent;
}
:where(html.dark) .ecd-body::-webkit-scrollbar-thumb {
  background: rgb(255 255 255 / 0.08);
}

/* 动画 */
.ecd-fade-enter-active,
.ecd-fade-leave-active {
  transition: opacity 200ms ease;
}
.ecd-fade-enter-from,
.ecd-fade-leave-to {
  opacity: 0;
}

.ecd-pop-enter-active {
  transition:
    opacity 220ms ease,
    transform 280ms cubic-bezier(0.22, 1, 0.36, 1);
}
.ecd-pop-leave-active {
  transition:
    opacity 160ms ease,
    transform 200ms ease;
}
.ecd-pop-enter-from {
  opacity: 0;
  transform: translateY(16px) scale(0.98);
}
.ecd-pop-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.985);
}
@media (min-width: 640px) {
  .ecd-pop-enter-from {
    transform: translateY(8px) scale(0.97);
  }
}

.ecd-error-enter-active,
.ecd-error-leave-active {
  transition:
    opacity 160ms ease,
    transform 160ms ease;
}
.ecd-error-enter-from,
.ecd-error-leave-to {
  opacity: 0;
  transform: translateY(-2px);
}

@media (prefers-reduced-motion: reduce) {
  .ecd-fade-enter-active,
  .ecd-fade-leave-active,
  .ecd-pop-enter-active,
  .ecd-pop-leave-active,
  .ecd-error-enter-active,
  .ecd-error-leave-active {
    transition: none;
  }
}
</style>
