<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
// @ts-ignore
import { changePassword, sendPasswordChangeCode } from '@/api/auth'
// @ts-ignore
import { getCurrentUserProfile } from '@/api/user'
// @ts-ignore
import { useToast } from '@/composables/useToast'

defineOptions({
  name: 'PasswordChangeView',
})

const router = useRouter()
const toast = useToast()

const form = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
  email: '',
  emailCode: '',
})

const loading = ref(false)
const sendCodeLoading = ref(false)
const sendCodeCountdown = ref(0)
const formError = ref('')
let sendCodeTimer: number | null = null

const isValidEmail = (email: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)

const passwordStrength = computed(() => {
  const password = form.newPassword
  const hasMinLength = password.length >= 8
  const hasLetter = /[A-Za-z]/.test(password)
  const hasNumber = /\d/.test(password)
  const hasSpecial = /[^A-Za-z\d]/.test(password)
  const score = [hasMinLength, hasLetter, hasNumber, hasSpecial].filter(Boolean).length

  if (score <= 1) {
    return { score, text: '弱', color: 'bg-red-500' }
  }
  if (score <= 3) {
    return { score, text: '中', color: 'bg-amber-500' }
  }
  return { score, text: '强', color: 'bg-emerald-500' }
})

const canSubmit = computed(() => {
  if (!form.currentPassword || !form.newPassword || !form.confirmPassword) {
    return false
  }
  if (!form.email || !form.emailCode) {
    return false
  }
  return true
})

const stopSendCodeTimer = () => {
  if (sendCodeTimer === null) {
    return
  }
  window.clearInterval(sendCodeTimer)
  sendCodeTimer = null
}

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

const validateForm = (): string => {
  if (!form.currentPassword.trim()) {
    return '请输入当前密码'
  }
  if (!form.newPassword.trim()) {
    return '请输入新密码'
  }
  if (form.newPassword.length < 8 || form.newPassword.length > 64) {
    return '新密码长度需在8-64位之间'
  }
  if (
    !/[A-Za-z]/.test(form.newPassword) ||
    !/\d/.test(form.newPassword) ||
    !/[^A-Za-z\d]/.test(form.newPassword)
  ) {
    return '新密码需包含字母、数字和特殊字符'
  }
  if (!form.confirmPassword.trim()) {
    return '请输入确认新密码'
  }
  if (form.newPassword !== form.confirmPassword) {
    return '两次输入的新密码不一致'
  }
  if (!form.email.trim()) {
    return '请输入绑定邮箱'
  }
  if (!isValidEmail(form.email.trim())) {
    return '请输入有效的邮箱地址'
  }
  if (!form.emailCode.trim()) {
    return '请输入邮箱验证码'
  }
  if (form.emailCode.trim().length !== 6) {
    return '验证码必须为6位'
  }
  return ''
}

const handleSendCode = async () => {
  if (sendCodeLoading.value || sendCodeCountdown.value > 0) {
    return
  }
  if (!form.email.trim()) {
    formError.value = '请先输入绑定邮箱'
    return
  }
  if (!isValidEmail(form.email.trim())) {
    formError.value = '请输入有效的邮箱地址'
    return
  }

  formError.value = ''
  sendCodeLoading.value = true
  try {
    await sendPasswordChangeCode({ email: form.email.trim() })
    startSendCodeCountdown()
    toast.add({
      title: '验证码已发送',
      description: '请前往绑定邮箱查看验证码',
      type: 'success',
    })
  } catch (error) {
    formError.value = error instanceof Error ? error.message : '验证码发送失败，请稍后重试'
  } finally {
    sendCodeLoading.value = false
  }
}

const handleSubmit = async () => {
  if (loading.value) {
    return
  }

  const validationError = validateForm()
  if (validationError) {
    formError.value = validationError
    return
  }

  loading.value = true
  formError.value = ''
  try {
    await changePassword({
      currentPassword: form.currentPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword,
      email: form.email.trim(),
      emailCode: form.emailCode.trim(),
    })

    toast.add({
      title: '修改成功',
      description: '密码已更新，请使用新密码登录',
      type: 'success',
    })

    await router.push({ name: 'settings' })
  } catch (error) {
    formError.value = error instanceof Error ? error.message : '密码修改失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const handleBack = async () => {
  await router.push({ name: 'settings' })
}

onMounted(async () => {
  try {
    const profile = await getCurrentUserProfile()
    if (profile.user?.email) {
      form.email = profile.user.email
    }
  } catch (error) {
    console.error(error)
  }
})

onBeforeUnmount(() => {
  stopSendCodeTimer()
})
</script>

<template>
  <div
    class="min-h-[calc(100vh-8rem)] bg-slate-50 px-4 py-6 transition-colors duration-300 dark:bg-black md:px-6 md:py-10"
  >
    <div class="mx-auto w-full max-w-2xl">
      <div class="mb-4 flex items-center justify-between">
        <h1 class="text-2xl font-semibold text-slate-900 dark:text-white md:text-3xl">修改密码</h1>
        <button
          type="button"
          class="cursor-pointer rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-600 transition-colors hover:bg-slate-50 dark:border-white/10 dark:bg-white/5 dark:text-slate-300 dark:hover:bg-white/10"
          @click="handleBack"
        >
          返回设置
        </button>
      </div>

      <div
        class="mb-6 rounded-xl border border-amber-200 bg-amber-50/80 p-4 text-sm text-amber-800 dark:border-amber-500/30 dark:bg-amber-500/10 dark:text-amber-200"
      >
        为保障账号安全，修改密码前需验证绑定邮箱。验证码5分钟内有效。
      </div>

      <div
        class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-white/10 dark:bg-zinc-900 md:p-6"
      >
        <div
          v-if="formError"
          class="mb-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700 dark:border-red-500/30 dark:bg-red-500/10 dark:text-red-200"
        >
          {{ formError }}
        </div>

        <form class="space-y-4" autocomplete="off" @submit.prevent="handleSubmit">
          <div>
            <label
              class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300"
              for="currentPassword"
              >当前密码</label
            >
            <input
              id="currentPassword"
              v-model="form.currentPassword"
              type="password"
              autocomplete="current-password"
              class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 outline-none transition-colors focus:border-amber-400 dark:border-white/10 dark:bg-black/30 dark:text-white dark:focus:border-amber-400"
              placeholder="请输入当前密码"
            />
          </div>

          <div>
            <label
              class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300"
              for="newPassword"
              >新密码</label
            >
            <input
              id="newPassword"
              v-model="form.newPassword"
              type="password"
              autocomplete="new-password"
              class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 outline-none transition-colors focus:border-amber-400 dark:border-white/10 dark:bg-black/30 dark:text-white dark:focus:border-amber-400"
              placeholder="8-64位，需含字母、数字和特殊字符"
            />
            <div class="mt-2">
              <div
                class="mb-1 flex items-center justify-between text-xs text-slate-500 dark:text-slate-400"
              >
                <span>密码强度</span>
                <span>{{ passwordStrength.text }}</span>
              </div>
              <div class="h-1.5 overflow-hidden rounded-full bg-slate-200 dark:bg-white/10">
                <div
                  class="h-full transition-all duration-300"
                  :class="passwordStrength.color"
                  :style="{ width: `${(passwordStrength.score / 4) * 100}%` }"
                ></div>
              </div>
            </div>
          </div>

          <div>
            <label
              class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300"
              for="confirmPassword"
              >确认新密码</label
            >
            <input
              id="confirmPassword"
              v-model="form.confirmPassword"
              type="password"
              autocomplete="new-password"
              class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 outline-none transition-colors focus:border-amber-400 dark:border-white/10 dark:bg-black/30 dark:text-white dark:focus:border-amber-400"
              placeholder="请再次输入新密码"
            />
          </div>

          <div>
            <label
              class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300"
              for="email"
              >绑定邮箱</label
            >
            <input
              id="email"
              v-model="form.email"
              type="email"
              autocomplete="email"
              class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 outline-none transition-colors focus:border-amber-400 dark:border-white/10 dark:bg-black/30 dark:text-white dark:focus:border-amber-400"
              placeholder="请输入账号绑定邮箱"
            />
          </div>

          <div>
            <label
              class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300"
              for="emailCode"
              >邮箱验证码</label
            >
            <div class="flex flex-col gap-2 sm:flex-row">
              <input
                id="emailCode"
                v-model="form.emailCode"
                type="text"
                maxlength="6"
                class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 outline-none transition-colors focus:border-amber-400 dark:border-white/10 dark:bg-black/30 dark:text-white dark:focus:border-amber-400"
                placeholder="请输入6位验证码"
              />
              <button
                type="button"
                :disabled="sendCodeLoading || sendCodeCountdown > 0 || loading"
                class="h-11 shrink-0 cursor-pointer rounded-lg border border-amber-400 bg-amber-100 px-4 text-sm font-medium text-amber-800 transition-colors hover:bg-amber-200 disabled:cursor-not-allowed disabled:opacity-60 dark:border-amber-500/40 dark:bg-amber-500/20 dark:text-amber-200 dark:hover:bg-amber-500/30"
                @click="handleSendCode"
              >
                <span v-if="sendCodeLoading">发送中...</span>
                <span v-else-if="sendCodeCountdown > 0">{{ sendCodeCountdown }}s</span>
                <span v-else>发送验证码</span>
              </button>
            </div>
          </div>

          <div class="flex flex-col-reverse gap-3 pt-2 sm:flex-row sm:justify-end">
            <button
              type="button"
              class="h-11 cursor-pointer rounded-lg border border-slate-200 bg-white px-5 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50 dark:border-white/10 dark:bg-white/5 dark:text-slate-300 dark:hover:bg-white/10"
              :disabled="loading"
              @click="handleBack"
            >
              取消
            </button>
            <button
              type="submit"
              :disabled="!canSubmit || loading"
              class="h-11 cursor-pointer rounded-lg bg-amber-500 px-5 text-sm font-semibold text-white transition-colors hover:bg-amber-600 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-amber-500 dark:hover:bg-amber-600"
            >
              {{ loading ? '提交中...' : '确认修改' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
