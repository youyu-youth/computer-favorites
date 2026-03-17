<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '@/services/auth'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

type Mode = 'login' | 'register'

const mode = ref<Mode>('login')
const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const form = reactive({
  account: '',
  password: '',
  confirmPassword: '',
})

const loading = ref(false)
const errorText = ref<string | null>(null)

const title = computed(() => (mode.value === 'login' ? '欢迎回来' : '创建账号'))
const subtitle = computed(() => (mode.value === 'login' ? '登录以管理你的收藏夹' : '开始收藏你的宝藏站点'))

const canSubmit = computed(() => {
  if (!form.account.trim()) return false
  if (!form.password.trim()) return false
  if (mode.value === 'register' && form.password !== form.confirmPassword) return false
  return true
})

const switchMode = (next: Mode) => {
  if (mode.value === next) return
  mode.value = next
  errorText.value = null
  form.password = ''
  form.confirmPassword = ''
}

const submit = async () => {
  if (!canSubmit.value || loading.value) return
  loading.value = true
  errorText.value = null
  try {
    if (mode.value === 'login') {
      const res = await login({ username: form.account.trim(), password: form.password, deviceType: 'web' })
      authStore.setToken(res.accessToken)
      toast.add({
        title: '登录成功',
        description: '欢迎回来，开启你的探索之旅',
        type: 'success'
      })
      await router.push({ name: 'home' })
      return
    }

    await register({ email: form.account.trim(), password: form.password })
    toast.add({
      title: '注册成功',
      description: '请使用新账号登录',
      type: 'success'
    })
    switchMode('login')
  } catch (e) {
    if (e instanceof Error) {
      errorText.value = e.message
    } else {
      errorText.value = '请求失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div
    class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-[0_24px_80px_-56px_rgba(15,23,42,0.45)] dark:border-white/10 dark:bg-zinc-900"
  >
    <div class="px-6 pt-6">
      <div class="flex items-center justify-between">
        <div>
          <div class="text-2xl font-semibold tracking-tight text-slate-900 dark:text-white">{{ title }}</div>
          <div class="mt-2 text-sm text-slate-600 dark:text-slate-400">{{ subtitle }}</div>
        </div>
      </div>

      <div
        class="mt-6 grid grid-cols-2 rounded-xl border border-slate-200 bg-slate-50 p-1 text-sm dark:border-white/10 dark:bg-black/40"
      >
        <button
          type="button"
          class="rounded-lg px-3 py-2 font-medium transition-colors"
          :class="mode === 'login' ? 'bg-emerald-600 text-white shadow-sm hover:bg-emerald-700 dark:bg-emerald-600 dark:text-white dark:hover:bg-emerald-700' : 'text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white'"
          @click="switchMode('login')"
        >
          登录
        </button>
        <button
          type="button"
          class="rounded-lg px-3 py-2 font-medium transition-colors"
          :class="mode === 'register' ? 'bg-emerald-600 text-white shadow-sm hover:bg-emerald-700 dark:bg-emerald-600 dark:text-white dark:hover:bg-emerald-700' : 'text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white'"
          @click="switchMode('register')"
        >
          注册
        </button>
      </div>
    </div>

    <form class="px-6 pb-6 pt-6" @submit.prevent="submit">
      <div v-if="errorText" class="mb-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700 dark:border-red-500/20 dark:bg-red-500/10 dark:text-red-200">
        {{ errorText }}
      </div>

      <div class="space-y-4">
        <div>
          <label for="account" class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
            {{ mode === 'login' ? '用户名 / 邮箱' : '邮箱' }}
          </label>
          <input
            id="account"
            v-model="form.account"
            :type="mode === 'login' ? 'text' : 'email'"
            :autocomplete="mode === 'login' ? 'username' : 'email'"
            required
            class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 outline-none transition-colors focus:border-slate-300 dark:border-white/10 dark:bg-black/40 dark:text-white dark:focus:border-white/20"
            :placeholder="mode === 'login' ? '请输入用户名或邮箱' : 'name@example.com'"
          />
        </div>

        <div>
          <label for="password" class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">密码</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            required
            class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 outline-none transition-colors focus:border-slate-300 dark:border-white/10 dark:bg-black/40 dark:text-white dark:focus:border-white/20"
            placeholder="请输入密码"
          />
        </div>

        <div
          class="overflow-hidden transition-all duration-200"
          :class="mode === 'register' ? 'max-h-24 opacity-100' : 'max-h-0 opacity-0'"
        >
          <label for="confirm" class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">确认密码</label>
          <input
            id="confirm"
            v-model="form.confirmPassword"
            type="password"
            autocomplete="new-password"
            :required="mode === 'register'"
            class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 outline-none transition-colors focus:border-slate-300 dark:border-white/10 dark:bg-black/40 dark:text-white dark:focus:border-white/20"
            placeholder="再次输入密码"
          />
        </div>
      </div>

      <button
        type="submit"
        :disabled="!canSubmit || loading"
        class="mt-6 inline-flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-emerald-700 px-4 text-sm font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:bg-emerald-800 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-emerald-600 dark:hover:bg-emerald-700"
      >
        <svg v-if="loading" class="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
          <path
            class="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          />
        </svg>
        <span>{{ mode === 'login' ? '登录' : '注册' }}</span>
      </button>

      <div class="relative mt-6">
        <div class="absolute inset-0 flex items-center" aria-hidden="true">
          <div class="w-full border-t border-slate-200 dark:border-white/10"></div>
        </div>
        <div class="relative flex justify-center text-sm">
          <span class="bg-white px-2 text-slate-500 dark:bg-zinc-900 dark:text-slate-400">或者</span>
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
          <svg class="h-3.5 w-3.5 text-amber-600 dark:text-amber-300" viewBox="0 0 20 20" fill="currentColor">
            <path
              d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
            />
          </svg>
        </div>
      </div>
    </form>
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
