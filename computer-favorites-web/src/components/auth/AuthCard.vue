<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  checkUsernameAvailable,
  login,
  loginByEmailCode,
  register,
  sendLoginCode,
  sendRegisterCode,
} from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

type Mode = 'login' | 'register'
type LoginMethod = 'password' | 'emailCode'
type SliderVerifyResult = {
  type: string
  message: string
  verify: boolean
}

const mode = ref<Mode>('login')
const loginMethod = ref<LoginMethod>('emailCode')
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const toast = useToast()

const form = reactive({
  account: '',
  password: '',
  confirmPassword: '',
  emailCode: '',
  loginEmailCode: '',
})

const loading = ref(false)
const errorText = ref<string | null>(null)
const showSliderVerify = ref(false)
const sliderVerified = ref(false)
const showUsernameDialog = ref(false)
const registerUsername = ref('')
const sendCodeLoading = ref(false)
const sendCodeCountdown = ref(0)
const usernameChecking = ref(false)
let sendCodeTimer: number | null = null
const isLoginMode = computed(() => mode.value === 'login')
const isRegisterMode = computed(() => mode.value === 'register')
const isPasswordLoginMethod = computed(() => isLoginMode.value && loginMethod.value === 'password')
const isEmailCodeLoginMethod = computed(
  () => isLoginMode.value && loginMethod.value === 'emailCode',
)

const title = computed(() => (isLoginMode.value ? '欢迎回来' : '创建账号'))
const subtitle = computed(() =>
  isLoginMode.value ? '登录以管理你的收藏夹' : '开始收藏你的宝藏站点',
)
const sliderVerifyImage = ref('')

const sliderImageFallback =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="640" height="360" viewBox="0 0 640 360"><rect width="640" height="360" fill="#f8fafc"/><rect x="40" y="40" width="220" height="90" fill="#94a3b8" fill-opacity="0.35"/><rect x="300" y="120" width="280" height="120" fill="#0ea5e9" fill-opacity="0.28"/><rect x="80" y="220" width="220" height="80" fill="#0f766e" fill-opacity="0.3"/><text x="36" y="70" fill="#0f172a" font-size="40" font-family="sans-serif" font-weight="700">CF</text><text x="36" y="104" fill="#334155" font-size="24" font-family="sans-serif">Slider Verify</text></svg>',
  )
const sliderImagePalette = [
  '#0f766e',
  '#0ea5e9',
  '#f59e0b',
  '#2563eb',
  '#ef4444',
  '#10b981',
  '#334155',
]

const getRandomInt = (min: number, max: number) => {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

const getRandomSliderColor = () => {
  return sliderImagePalette[getRandomInt(0, sliderImagePalette.length - 1)] ?? '#334155'
}

const createSliderVerifyImage = () => {
  if (typeof document === 'undefined') {
    return sliderImageFallback
  }
  const canvas = document.createElement('canvas')
  canvas.width = 640
  canvas.height = 360
  const context = canvas.getContext('2d')
  if (!context) {
    return sliderImageFallback
  }

  context.fillStyle = '#f8fafc'
  context.fillRect(0, 0, canvas.width, canvas.height)

  for (let index = 0; index < 24; index += 1) {
    const color = getRandomSliderColor()
    const x = getRandomInt(0, canvas.width - 80)
    const y = getRandomInt(0, canvas.height - 80)
    const width = getRandomInt(40, 180)
    const height = getRandomInt(30, 120)

    context.globalAlpha = 0.18 + Math.random() * 0.2
    context.fillStyle = color
    context.fillRect(x, y, width, height)
  }

  context.globalAlpha = 0.3
  for (let index = 0; index < 16; index += 1) {
    const color = getRandomSliderColor()
    const x = getRandomInt(20, canvas.width - 20)
    const y = getRandomInt(20, canvas.height - 20)
    const radius = getRandomInt(8, 24)

    context.fillStyle = color
    context.beginPath()
    context.arc(x, y, radius, 0, Math.PI * 2)
    context.fill()
  }

  context.globalAlpha = 1
  context.fillStyle = '#0f172a'
  context.font = '700 42px sans-serif'
  context.fillText('CF', 24, 58)

  context.fillStyle = '#334155'
  context.font = '24px sans-serif'
  context.fillText('Slider Verify', 24, 94)

  return canvas.toDataURL('image/png')
}

const refreshSliderVerifyImage = () => {
  sliderVerifyImage.value = createSliderVerifyImage()
}

const loginHintText = computed(() => {
  if (!isLoginMode.value) {
    return ''
  }
  return isEmailCodeLoginMethod.value ? '进行密码登录' : '进行邮箱验证登录'
})

const canSubmit = computed(() => {
  if (isLoginMode.value) {
    if (!form.account.trim()) return false
    if (loginMethod.value === 'password' && !form.password.trim()) return false
    if (loginMethod.value === 'emailCode') {
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.account.trim())) return false
      if (!form.loginEmailCode.trim()) return false
    }
    return true
  }
  if (!form.account.trim()) return false
  if (!form.password.trim()) return false
  if (!form.emailCode.trim()) return false
  if (form.password !== form.confirmPassword) return false
  return true
})

const switchMode = (next: Mode) => {
  if (mode.value === next) return
  mode.value = next
  loginMethod.value = 'emailCode'
  errorText.value = null
  form.account = ''
  form.password = ''
  form.confirmPassword = ''
  form.emailCode = ''
  form.loginEmailCode = ''
  showSliderVerify.value = false
  sliderVerified.value = false
  sliderVerifyImage.value = ''
  showUsernameDialog.value = false
  registerUsername.value = ''
  sendCodeCountdown.value = 0
  stopSendCodeTimer()
}

const switchLoginMethodFromHint = () => {
  if (!isLoginMode.value || loading.value) {
    return
  }
  loginMethod.value = isEmailCodeLoginMethod.value ? 'password' : 'emailCode'
  errorText.value = null
  form.password = ''
  form.loginEmailCode = ''
  sendCodeCountdown.value = 0
  stopSendCodeTimer()
  showSliderVerify.value = false
  sliderVerified.value = false
  sliderVerifyImage.value = ''
}

const onSliderSuccess = (result: SliderVerifyResult) => {
  if (!result.verify) {
    sliderVerified.value = false
    return
  }
  sliderVerified.value = true
  showSliderVerify.value = false
  sliderVerifyImage.value = ''
  void submit()
}

const onSliderError = (result: SliderVerifyResult) => {
  sliderVerified.value = false
  errorText.value = result.message || '滑块验证失败，请重试'
  refreshSliderVerifyImage()
}

const resolveRedirect = () => {
  const redirect = route.query.redirect
  if (typeof redirect !== 'string') {
    return { name: 'home' as const }
  }
  if (!redirect.startsWith('/computer/')) {
    return { name: 'home' as const }
  }
  return redirect
}

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

const buildDefaultUsername = (email: string) => {
  const index = email.indexOf('@')
  const prefix = index > 0 ? email.slice(0, index) : email
  const normalized = prefix.replace(/[^a-zA-Z0-9_]/g, '_').slice(0, 24)
  if (normalized.length >= 4) {
    return normalized
  }
  return `user_${Date.now().toString().slice(-6)}`
}

const openUsernameDialog = () => {
  if (!registerUsername.value.trim()) {
    registerUsername.value = buildDefaultUsername(form.account.trim())
  }
  showUsernameDialog.value = true
}

const isValidEmail = (email: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)

const sendCode = async () => {
  if (sendCodeLoading.value || sendCodeCountdown.value > 0) {
    return
  }
  if (!isRegisterMode.value && !isEmailCodeLoginMethod.value) {
    return
  }
  const email = form.account.trim()
  if (!email) {
    errorText.value = '请先输入邮箱'
    return
  }
  if (!isValidEmail(email)) {
    errorText.value = '请输入有效邮箱地址'
    return
  }
  sendCodeLoading.value = true
  errorText.value = null
  try {
    if (isRegisterMode.value) {
      await sendRegisterCode(email)
    } else {
      await sendLoginCode(email)
    }
    startSendCodeCountdown()
    toast.add({
      title: '验证码已发送',
      description: isRegisterMode.value ? '请前往邮箱查看注册验证码' : '请前往邮箱查看登录验证码',
      type: 'success',
    })
  } catch (e) {
    errorText.value = e instanceof Error ? e.message : '验证码发送失败，请稍后重试'
  } finally {
    sendCodeLoading.value = false
  }
}

const completeRegister = async () => {
  if (usernameChecking.value || loading.value) {
    return
  }
  const username = registerUsername.value.trim().toLowerCase()
  if (username.length < 4 || username.length > 24) {
    errorText.value = '用户名长度需在4-24之间'
    return
  }
  if (!/^[a-zA-Z0-9_]+$/.test(username)) {
    errorText.value = '用户名仅支持字母、数字和下划线'
    return
  }
  usernameChecking.value = true
  loading.value = true
  errorText.value = null
  try {
    const available = await checkUsernameAvailable(username)
    if (!available) {
      throw new Error('该用户名已被使用，请更换')
    }
    await register({
      username,
      email: form.account.trim(),
      password: form.password,
      emailCode: form.emailCode.trim(),
    })
    const loginRes = await login({
      username: form.account.trim(),
      password: form.password,
      deviceType: 'web',
    })
    authStore.setToken(loginRes.accessToken, loginRes.tokenName || 'satoken')
    const validSession = await authStore.loadCurrentUser()
    if (!validSession) {
      throw new Error('注册成功，但登录态初始化失败，请重新登录')
    }
    showUsernameDialog.value = false
    form.account = ''
    form.password = ''
    form.confirmPassword = ''
    form.emailCode = ''
    registerUsername.value = ''
    sendCodeCountdown.value = 0
    stopSendCodeTimer()
    sessionStorage.setItem(
      'registerSuccessMessage',
      JSON.stringify({
        title: '注册成功',
        description: `欢迎加入，${username}`,
      }),
    )
    await router.push({ name: 'home' })
  } catch (e) {
    errorText.value = e instanceof Error ? e.message : '注册失败，请稍后重试'
  } finally {
    loading.value = false
    usernameChecking.value = false
  }
}

const submit = async () => {
  if (loading.value) return
  if (isLoginMode.value) {
    if (!canSubmit.value) {
      if (!form.account.trim()) {
        errorText.value = isEmailCodeLoginMethod.value ? '请输入邮箱' : '请输入用户名或邮箱'
      } else if (isEmailCodeLoginMethod.value && !isValidEmail(form.account.trim())) {
        errorText.value = '请输入有效的邮箱地址'
      } else if (isPasswordLoginMethod.value && !form.password.trim()) {
        errorText.value = '请输入密码'
      } else if (isEmailCodeLoginMethod.value && !form.loginEmailCode.trim()) {
        errorText.value = '请输入邮箱验证码'
      } else {
        errorText.value = '请完善登录信息'
      }
      return
    }
    if (!sliderVerified.value) {
      errorText.value = null
      refreshSliderVerifyImage()
      showSliderVerify.value = true
      return
    }
    sliderVerified.value = false
    loading.value = true
    errorText.value = null
    try {
      const res = isPasswordLoginMethod.value
        ? await login({ username: form.account.trim(), password: form.password, deviceType: 'web' })
        : await loginByEmailCode({
            email: form.account.trim(),
            emailCode: form.loginEmailCode.trim(),
            deviceType: 'web',
          })
      authStore.setToken(res.accessToken, res.tokenName || 'satoken')
      const validSession = await authStore.loadCurrentUser()
      if (!validSession) {
        throw new Error('登录态校验失败，请重新登录')
      }
      toast.add({ title: '登录成功', description: '欢迎回来，开启你的探索之旅', type: 'success' })
      await router.push(resolveRedirect())
    } catch (e) {
      errorText.value = e instanceof Error ? e.message : '请求失败，请稍后重试'
    } finally {
      loading.value = false
    }
    return
  }
  if (!canSubmit.value) {
    if (form.password !== form.confirmPassword) {
      errorText.value = '两次输入的密码不一致'
    } else if (!form.emailCode.trim()) {
      errorText.value = '请输入邮箱验证码'
    } else if (!form.account.trim()) {
      errorText.value = '请输入邮箱'
    } else if (!isValidEmail(form.account.trim())) {
      errorText.value = '请输入有效的邮箱地址'
    } else {
      errorText.value = '请完善注册信息'
    }
    return
  }
  openUsernameDialog()
}

onBeforeUnmount(() => {
  stopSendCodeTimer()
})
</script>

<template>
  <div
    class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-[0_24px_80px_-56px_rgba(15,23,42,0.45)] dark:border-white/10 dark:bg-zinc-900"
  >
    <div class="px-6 pt-6">
      <div class="flex items-center justify-between">
        <div class="min-h-[3.5rem]">
          <div class="text-2xl font-semibold tracking-tight text-slate-900 dark:text-white">
            {{ title }}
          </div>
          <div class="mt-2 text-sm text-slate-600 dark:text-slate-400">{{ subtitle }}</div>
        </div>
      </div>

      <div
        class="mt-6 grid grid-cols-2 rounded-xl border border-slate-200 bg-slate-50 p-1 text-sm dark:border-white/10 dark:bg-black/40"
      >
        <button
          type="button"
          class="rounded-lg px-3 py-2 font-medium transition-colors"
          :class="
            isLoginMode
              ? 'bg-emerald-600 text-white shadow-sm hover:bg-emerald-700 dark:bg-emerald-600 dark:text-white dark:hover:bg-emerald-700'
              : 'text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white'
          "
          @click="switchMode('login')"
        >
          登录
        </button>
        <button
          type="button"
          class="rounded-lg px-3 py-2 font-medium transition-colors"
          :class="
            isRegisterMode
              ? 'bg-emerald-600 text-white shadow-sm hover:bg-emerald-700 dark:bg-emerald-600 dark:text-white dark:hover:bg-emerald-700'
              : 'text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white'
          "
          @click="switchMode('register')"
        >
          注册
        </button>
      </div>
    </div>

    <form class="px-6 pb-6 pt-6" autocomplete="off" @submit.prevent="submit">
      <div
        v-if="errorText"
        class="mb-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700 dark:border-red-500/20 dark:bg-red-500/10 dark:text-red-200"
      >
        {{ errorText }}
      </div>

      <div class="space-y-4">
        <div>
          <label
            for="account"
            class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300"
          >
            {{ isRegisterMode || isEmailCodeLoginMethod ? '邮箱' : '用户名 / 邮箱' }}
          </label>
          <input
            id="account"
            v-model="form.account"
            :type="isRegisterMode || isEmailCodeLoginMethod ? 'email' : 'text'"
            :autocomplete="isRegisterMode || isEmailCodeLoginMethod ? 'email' : 'off'"
            required
            class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 placeholder:text-slate-400 outline-none transition-colors focus:border-slate-300 dark:border-white/10 dark:bg-black/40 dark:text-white dark:placeholder:text-slate-500 dark:focus:border-white/20"
            :placeholder="
              isRegisterMode || isEmailCodeLoginMethod ? 'name@example.com' : '请输入用户名或邮箱'
            "
          />
        </div>

        <div v-if="isRegisterMode || isPasswordLoginMethod">
          <label
            for="password"
            class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300"
            >密码</label
          >
          <input
            id="password"
            v-model="form.password"
            type="password"
            autocomplete="new-password"
            :required="isRegisterMode || isPasswordLoginMethod"
            class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 placeholder:text-slate-400 outline-none transition-colors focus:border-slate-300 dark:border-white/10 dark:bg-black/40 dark:text-white dark:placeholder:text-slate-500 dark:focus:border-white/20"
            placeholder="请输入密码"
          />
        </div>

        <template v-if="isRegisterMode">
          <div>
            <label
              for="confirm"
              class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300"
              >确认密码</label
            >
            <input
              id="confirm"
              v-model="form.confirmPassword"
              type="password"
              autocomplete="new-password"
              required
              class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 placeholder:text-slate-400 outline-none transition-colors focus:border-slate-300 dark:border-white/10 dark:bg-black/40 dark:text-white dark:placeholder:text-slate-500 dark:focus:border-white/20"
              placeholder="再次输入密码"
            />
          </div>

          <div>
            <label
              for="emailCode"
              class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300"
              >邮箱验证码</label
            >
            <div class="flex items-center gap-2">
              <input
                id="emailCode"
                v-model="form.emailCode"
                type="text"
                maxlength="6"
                required
                class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 placeholder:text-slate-400 outline-none transition-colors focus:border-slate-300 dark:border-white/10 dark:bg-black/40 dark:text-white dark:placeholder:text-slate-500 dark:focus:border-white/20"
                placeholder="请输入6位验证码"
              />
              <button
                type="button"
                :disabled="sendCodeLoading || sendCodeCountdown > 0 || loading"
                class="inline-flex h-11 shrink-0 items-center justify-center rounded-lg border border-slate-200 px-3 text-xs font-medium text-slate-700 transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60 dark:border-white/10 dark:text-slate-200 dark:hover:bg-white/10"
                @click="sendCode"
              >
                <span v-if="sendCodeLoading">发送中...</span>
                <span v-else-if="sendCodeCountdown > 0">{{ sendCodeCountdown }}s</span>
                <span v-else>发送验证码</span>
              </button>
            </div>
          </div>
        </template>

        <template v-else-if="isEmailCodeLoginMethod">
          <div>
            <label
              for="loginEmailCode"
              class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300"
              >邮箱验证码</label
            >
            <div class="flex items-center gap-2">
              <input
                id="loginEmailCode"
                v-model="form.loginEmailCode"
                type="text"
                maxlength="6"
                required
                class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 placeholder:text-slate-400 outline-none transition-colors focus:border-slate-300 dark:border-white/10 dark:bg-black/40 dark:text-white dark:placeholder:text-slate-500 dark:focus:border-white/20"
                placeholder="请输入6位验证码"
              />
              <button
                type="button"
                :disabled="sendCodeLoading || sendCodeCountdown > 0 || loading"
                class="inline-flex h-11 shrink-0 items-center justify-center rounded-lg border border-slate-200 px-3 text-xs font-medium text-slate-700 transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60 dark:border-white/10 dark:text-slate-200 dark:hover:bg-white/10"
                @click="sendCode"
              >
                <span v-if="sendCodeLoading">发送中...</span>
                <span v-else-if="sendCodeCountdown > 0">{{ sendCodeCountdown }}s</span>
                <span v-else>发送验证码</span>
              </button>
            </div>
          </div>
        </template>
      </div>

      <button
        type="submit"
        :disabled="!canSubmit || loading"
        class="mt-6 inline-flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-emerald-700 px-4 text-sm font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:bg-emerald-800 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-emerald-600 dark:hover:bg-emerald-700"
      >
        <svg v-if="loading" class="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
          <circle
            class="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            stroke-width="4"
          />
          <path
            class="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          />
        </svg>
        <span>{{ isLoginMode ? '登录' : '注册' }}</span>
      </button>
      <div
        class="mt-3 h-4 cursor-pointer text-center text-xs text-slate-500 transition-opacity duration-200 dark:text-slate-400"
        :class="isLoginMode ? 'opacity-100' : 'opacity-0'"
        role="button"
        tabindex="0"
        @click="switchLoginMethodFromHint"
      >
        {{ loginHintText }}
      </div>

      <div class="relative mt-6">
        <div class="absolute inset-0 flex items-center" aria-hidden="true">
          <div class="w-full border-t border-slate-200 dark:border-white/10"></div>
        </div>
        <div class="relative flex justify-center text-sm">
          <span class="bg-white px-2 text-slate-500 dark:bg-zinc-900 dark:text-slate-400"
            >或者</span
          >
        </div>
      </div>

      <div class="mt-6 grid grid-cols-2 gap-3">
        <button
          type="button"
          class="inline-flex w-full items-center justify-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 transition-all hover:bg-slate-50 hover:text-slate-900 dark:border-white/10 dark:bg-white/5 dark:text-slate-300 dark:hover:bg-white/10 dark:hover:text-white"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="currentColor">
            <path
              fill-rule="evenodd"
              clip-rule="evenodd"
              d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.87 8.17 6.84 9.5.5.08.66-.23.66-.5v-1.69c-2.77.6-3.36-1.34-3.36-1.34-.46-1.16-1.11-1.47-1.11-1.47-.91-.62.07-.6.07-.6 1 .07 1.53 1.03 1.53 1.03.87 1.52 2.34 1.07 2.91.83.09-.65.35-1.09.63-1.34-2.22-.25-4.55-1.11-4.55-4.92 0-1.11.38-2 1.03-2.71-.1-.25-.45-1.29.1-2.64 0 0 .84-.27 2.75 1.02.79-.22 1.65-.33 2.5-.33.85 0 1.71.11 2.5.33 1.91-1.29 2.75-1.02 2.75-1.02.55 1.35.2 2.39.1 2.64.65.71 1.03 1.6 1.03 2.71 0 3.82-2.34 4.66-4.57 4.91.36.31.69.92.69 1.85V21c0 .27.16.59.67.5C19.14 20.16 22 16.42 22 12A10 10 0 0012 2z"
            />
          </svg>
          GitHub
        </button>
        <button
          type="button"
          class="inline-flex w-full items-center justify-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 transition-all hover:bg-slate-50 hover:text-slate-900 dark:border-white/10 dark:bg-white/5 dark:text-slate-300 dark:hover:bg-white/10 dark:hover:text-white"
        >
          <svg class="h-5 w-5 text-red-600" viewBox="0 0 24 24" fill="currentColor">
            <path
              d="M11.984 0C5.363 0 0 5.363 0 11.984c0 6.621 5.363 11.984 11.984 11.984 6.621 0 11.984-5.363 11.984-11.984C23.969 5.363 18.605 0 11.984 0zm5.898 9.844c.328 1.945-1.242 3.844-3.141 4.594-1.289.516-2.695.539-4.055.164-.305-.07-.586.187-.516.492.211 1.148 1.102 2.156 2.25 2.508 1.195.375 2.508.117 3.539-.633.398-.281.938-.117 1.172.305.234.398.07.914-.328 1.172-1.5 1.078-3.469 1.453-5.227.914-1.664-.516-2.953-1.969-3.258-3.633-.07-.352-.117-.703-.117-1.078 0-2.484 1.734-4.594 4.078-5.18 1.711-.422 3.516.141 4.711 1.359.328.328.867.305 1.172-.023.305-.328.281-.867-.047-1.172-1.641-1.664-4.125-2.438-6.469-1.852-3.211.82-5.578 3.703-5.578 7.055 0 .539.07 1.055.164 1.547.445 2.414 2.297 4.5 4.805 5.25 2.531.773 5.367.234 7.523-1.313.375-.258.469-.773.211-1.148-.258-.375-.773-.469-1.148-.211-1.547 1.102-3.586 1.477-5.391.938-1.781-.539-3.094-2.063-3.422-3.82-.07-.375.258-.703.633-.609 1.875.516 3.82.492 5.578-.211 2.648-1.055 4.828-3.703 4.383-6.422-.094-.609-.656-1.031-1.266-.938z"
            />
          </svg>
          Gitee
        </button>
      </div>

      <div class="mt-4 flex items-center justify-center">
        <div
          class="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs text-slate-600 dark:border-white/10 dark:bg-black/40 dark:text-slate-300"
        >
          <span>已为</span>
          <span class="font-semibold text-slate-900 dark:text-white">12,000+</span>
          <span>开发者提供收藏服务</span>
          <svg
            class="h-3.5 w-3.5 text-amber-600 dark:text-amber-300"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
            />
          </svg>
        </div>
      </div>
    </form>
    <div
      v-if="showUsernameDialog && isRegisterMode"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/45 px-4"
    >
      <div
        class="w-full max-w-sm rounded-xl border border-slate-200 bg-white p-5 shadow-lg dark:border-white/10 dark:bg-zinc-900"
      >
        <div class="mb-3 text-base font-semibold text-slate-900 dark:text-white">
          设置你的用户名
        </div>
        <div class="text-xs text-slate-500 dark:text-slate-400">
          用户名需唯一，仅支持字母、数字和下划线
        </div>
        <input
          v-model="registerUsername"
          type="text"
          maxlength="24"
          class="mt-4 h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 outline-none transition-colors focus:border-slate-300 dark:border-white/10 dark:bg-black/40 dark:text-white dark:focus:border-white/20"
          placeholder="请输入用户名"
        />
        <div class="mt-4 grid grid-cols-2 gap-2">
          <button
            type="button"
            :disabled="loading || usernameChecking"
            class="inline-flex h-10 items-center justify-center rounded-lg border border-slate-200 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60 dark:border-white/10 dark:text-slate-200 dark:hover:bg-white/10"
            @click="showUsernameDialog = false"
          >
            取消
          </button>
          <button
            type="button"
            :disabled="loading || usernameChecking"
            class="inline-flex h-10 items-center justify-center rounded-lg bg-emerald-700 text-sm font-semibold text-white transition-colors hover:bg-emerald-800 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-emerald-600 dark:hover:bg-emerald-700"
            @click="completeRegister"
          >
            {{ usernameChecking ? '校验中...' : '确认注册' }}
          </button>
        </div>
      </div>
    </div>
    <div
      v-if="showSliderVerify && isLoginMode"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/45 px-4"
    >
      <div
        class="w-full max-w-sm rounded-xl border border-slate-200 bg-white p-5 shadow-lg dark:border-white/10 dark:bg-zinc-900"
      >
        <div class="mb-3 flex items-center justify-between">
          <h3 class="text-sm font-semibold text-slate-900 dark:text-white">滑块验证</h3>
          <button
            type="button"
            class="inline-flex h-7 w-7 items-center justify-center rounded-md border border-slate-200 text-slate-500 transition-colors hover:bg-slate-50 dark:border-white/10 dark:text-slate-300 dark:hover:bg-white/10"
            @click="showSliderVerify = false"
          >
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>
        <div class="text-xs text-slate-500 dark:text-slate-400">请完成滑块验证后继续登录</div>
        <div class="mt-4 overflow-x-auto">
          <slider-verify
            :img="sliderVerifyImage"
            :width="320"
            :height="180"
            @onSuccess="onSliderSuccess"
            @onError="onSliderError"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style>
.dark #account:-webkit-autofill,
.dark #account:-webkit-autofill:hover,
.dark #account:-webkit-autofill:focus,
.dark #password:-webkit-autofill,
.dark #password:-webkit-autofill:hover,
.dark #password:-webkit-autofill:focus,
.dark #confirm:-webkit-autofill,
.dark #confirm:-webkit-autofill:hover,
.dark #confirm:-webkit-autofill:focus {
  -webkit-text-fill-color: #ffffff !important;
  /* Use a solid dark color to override the white/yellow browser default */
  -webkit-box-shadow: 0 0 0 1000px #09090b inset !important;
  box-shadow: 0 0 0 1000px #09090b inset !important;
  background-color: transparent !important;
  caret-color: #ffffff !important;
  transition: background-color 999999s ease-out 0s;
}
</style>
