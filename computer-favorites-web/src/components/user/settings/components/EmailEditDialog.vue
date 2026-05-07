<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'
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
let sendCodeTimer: number | null = null
let previousScrollY = 0

const isValidEmail = (value: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)

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

const lockPageScroll = () => {
  if (typeof window === 'undefined') {
    return
  }
  previousScrollY = window.scrollY
  const scrollValue = `${previousScrollY}px`
  document.documentElement.classList.add('email-modal-scroll-lock')
  document.body.classList.add('email-modal-scroll-lock')
  document.body.style.setProperty('--email-modal-scroll-y', scrollValue)
}

const unlockPageScroll = () => {
  if (typeof window === 'undefined') {
    return
  }
  document.documentElement.classList.remove('email-modal-scroll-lock')
  document.body.classList.remove('email-modal-scroll-lock')
  document.body.style.removeProperty('--email-modal-scroll-y')
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

watch(
  () => props.open,
  (open) => {
    if (open) {
      email.value = ''
      emailCode.value = ''
      localError.value = ''
      lockPageScroll()
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
  <UModal
    :open="open"
    :portal="true"
    title="修改邮箱"
    description="请输入新邮箱并完成验证码校验"
    :ui="{
      overlay: 'email-edit-modal-overlay z-[120]',
      content: 'email-edit-modal-content z-[130]',
      header: 'email-edit-modal-header',
      title: 'email-edit-modal-title flex items-center gap-2',
      description: 'email-edit-modal-description',
      close: 'email-edit-modal-close cursor-pointer',
      body: 'email-edit-modal-body',
      footer: 'email-edit-modal-footer',
    }"
    @update:open="emit('update:open', $event)"
  >
    <template #body>
      <div class="cf-dialog-body space-y-4">
        <div class="flex items-start gap-3 rounded-lg border border-amber-200 bg-amber-50/60 p-3 dark:border-amber-400/20 dark:bg-amber-500/[0.06]">
          <span class="mt-0.5 flex h-7 w-7 shrink-0 items-center justify-center rounded-md bg-amber-100 text-amber-600 dark:bg-amber-500/15 dark:text-amber-400">
            <UIcon name="i-lucide-mail" class="h-3.5 w-3.5" />
          </span>
          <p class="text-xs text-amber-800 dark:text-amber-200">
            修改邮箱后，下次登录请使用新邮箱。验证码将发送到新邮箱地址。
          </p>
        </div>

        <UFormField label="当前邮箱">
          <UInput :model-value="currentEmail || '未绑定邮箱'" readonly class="w-full font-mono text-sm" />
        </UFormField>

        <UFormField label="新邮箱" required>
          <UInput
            v-model="email"
            type="email"
            autocomplete="email"
            placeholder="请输入新的邮箱地址"
            class="w-full font-mono text-sm"
          />
        </UFormField>

        <UFormField label="邮箱验证码" required>
          <div class="flex flex-col gap-2 sm:flex-row">
            <UInput
              v-model="emailCode"
              maxlength="6"
              placeholder="6 位数字验证码"
              class="w-full font-mono tracking-[0.4em]"
            />
            <button
              type="button"
              class="inline-flex h-10 shrink-0 cursor-pointer items-center justify-center gap-1.5 rounded-lg border border-slate-200 bg-white px-4 text-xs font-medium text-slate-700 transition-colors hover:border-amber-400 hover:text-amber-600 disabled:cursor-not-allowed disabled:opacity-60 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-200 dark:hover:border-amber-400/40 dark:hover:text-amber-300"
              :disabled="sendCodeLoading || sendCodeCountdown > 0 || loading"
              @click="handleSendCode"
            >
              <UIcon
                v-if="sendCodeLoading"
                name="i-lucide-loader-2"
                class="h-3.5 w-3.5 animate-spin"
              />
              <UIcon
                v-else-if="sendCodeCountdown > 0"
                name="i-lucide-timer"
                class="h-3.5 w-3.5"
              />
              <UIcon v-else name="i-lucide-send-horizontal" class="h-3.5 w-3.5" />
              <span v-if="sendCodeLoading">发送中…</span>
              <span v-else-if="sendCodeCountdown > 0">{{ sendCodeCountdown }} s 后可重发</span>
              <span v-else>发送验证码</span>
            </button>
          </div>
        </UFormField>

        <p v-if="localError" class="flex items-center gap-1.5 rounded-md border border-rose-200 bg-rose-50/80 px-2.5 py-1.5 text-xs text-rose-600 dark:border-rose-500/20 dark:bg-rose-500/10 dark:text-rose-300">
          <UIcon name="i-lucide-alert-circle" class="h-3.5 w-3.5" />
          <span>{{ localError }}</span>
        </p>
      </div>
    </template>

    <template #footer>
      <div class="email-edit-modal-actions flex w-full justify-end gap-2">
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
