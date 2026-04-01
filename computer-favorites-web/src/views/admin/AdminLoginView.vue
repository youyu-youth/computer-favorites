<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/adminAuth'
import { useAppStore } from '@/stores/app'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'

const router = useRouter()
const route = useRoute()
const adminAuthStore = useAdminAuthStore()
const appStore = useAppStore()

const username = ref('')
const password = ref('')
const isLoading = ref(false)
const errorText = ref('')
const formRenderKey = ref(0)

// 标题打字机效果
const typedTitle = ref('')
const fullTitle = '管理员访问终端'

// 终端日志
const terminalLogs = ref<string[]>([
  '[系统] 正在初始化安全协议...',
  '[系统] 正在等待管理员凭据...'
])

let typeInterval: ReturnType<typeof setInterval>
let logInterval: ReturnType<typeof setInterval>

const shouldResetFromLogout = () => typeof route.query.logoutReset === 'string'

const resetCredentialForm = async () => {
  username.value = ''
  password.value = ''
  errorText.value = ''
  formRenderKey.value += 1
  await nextTick()
}

onMounted(() => {
  if (shouldResetFromLogout()) {
    void resetCredentialForm()
  }

  // 标题动画
  let i = 0
  typeInterval = setInterval(() => {
    typedTitle.value += fullTitle.charAt(i)
    i++
    if (i >= fullTitle.length) clearInterval(typeInterval)
  }, 80)

  // 终端随机环境日志
  const ambientLogs = [
    '[网络] 正在监控代理网关...',
    '[安全] 防火墙配置已加载。',
    '[系统] 内存分配校验通过。',
    '[网络] 正在同步外部负载均衡...',
    '[安全] 威胁检测协议已激活。',
    '[系统] 正在等待用户输入...'
  ]

  logInterval = setInterval(() => {
    if (Math.random() > 0.6) {
      const idx = Math.floor(Math.random() * ambientLogs.length)
      terminalLogs.value.push(ambientLogs[idx] as string)
      if (terminalLogs.value.length > 8) {
        terminalLogs.value.shift()
      }
    }
  }, 1500)
})

watch(
  () => route.query.logoutReset,
  (value, oldValue) => {
    if (typeof value === 'string' && value !== oldValue) {
      void resetCredentialForm()
    }
  },
)

onUnmounted(() => {
  if (typeInterval) clearInterval(typeInterval)
  if (logInterval) clearInterval(logInterval)
})

const handleLogin = async () => {
  if (!username.value || !password.value) return
  isLoading.value = true
  errorText.value = ''

  terminalLogs.value.push('[认证] 正在校验管理员凭据...')
  if (terminalLogs.value.length > 8) {
    terminalLogs.value.shift()
  }

  try {
    await adminAuthStore.login(username.value.trim(), password.value, 'web')
    terminalLogs.value.push('[系统] 验证通过，正在跳转...')
    if (terminalLogs.value.length > 8) {
      terminalLogs.value.shift()
    }
    const redirect =
      typeof route.query.redirect === 'string' ? route.query.redirect : '/computer/admin/websites'
    const targetPath = redirect.startsWith('/computer/admin/')
      ? redirect
      : '/computer/admin/websites'
    await router.push(targetPath)
  } catch (error) {
    const message = error instanceof Error ? error.message : '管理员登录失败'
    errorText.value = message
    terminalLogs.value.push(`[认证] ${message}`)
    if (terminalLogs.value.length > 8) {
      terminalLogs.value.shift()
    }
  } finally {
    isLoading.value = false
  }
}

const toggleTheme = () => {
  const newMode = appStore.isDark ? 'light' : 'dark'
  appStore.setThemeMode(newMode)
}
</script>

<template>
  <div class="min-h-screen flex flex-col md:flex-row bg-surface-page dark:bg-dark-bg font-mono transition-colors duration-300 relative overflow-hidden">

    <!-- Background Grid Pattern -->
    <div class="absolute inset-0 pointer-events-none opacity-[0.03] dark:opacity-[0.05] z-0"
         style="background-image: linear-gradient(to right, currentColor 1px, transparent 1px), linear-gradient(to bottom, currentColor 1px, transparent 1px); background-size: 40px 40px;">
    </div>

    <!-- Theme Toggle (Hacker style) -->
    <button @click="toggleTheme"
            class="absolute top-6 right-6 z-20 flex items-center justify-center p-2 rounded-sm border border-gray-300 dark:border-dark-border bg-white dark:bg-dark-card text-gray-600 dark:text-gray-300 hover:text-brand-orange dark:hover:text-brand-orange hover:border-brand-orange transition-all duration-200 shadow-sm focus:outline-none focus:ring-1 focus:ring-brand-orange">
      <i class="pi" :class="appStore.isDark ? 'pi-moon' : 'pi-sun'"></i>
    </button>

    <!-- Left Side: Terminal Visuals -->
    <div class="hidden md:flex flex-1 flex-col justify-between p-10 lg:p-16 border-r border-gray-200 dark:border-dark-border bg-white/50 dark:bg-dark-card/30 backdrop-blur-sm z-10 relative">
      <!-- Glow effect behind terminal text -->
      <div class="absolute top-1/3 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 bg-brand-orange/10 dark:bg-brand-orange/5 blur-3xl rounded-full pointer-events-none"></div>

      <div>
        <div class="flex items-center gap-3 mb-6">
          <div class="w-10 h-10 rounded-sm bg-brand-orange flex items-center justify-center text-white shadow-[0_0_15px_rgba(233,83,34,0.4)]">
            <i class="pi pi-server text-xl"></i>
          </div>
          <h1 class="text-2xl font-bold tracking-tight text-gray-900 dark:text-gray-100 flex items-center">
            CF_控制台
            <span class="w-2 h-5 bg-brand-orange ml-2 animate-pulse inline-block"></span>
          </h1>
        </div>
        <p class="text-sm text-gray-500 dark:text-gray-400 mb-12">
          仅限严格授权访问。 <br/>
          所有连接请求都会被监控并记录。
        </p>

        <!-- Dynamic Terminal Window -->
        <div class="font-mono text-xs sm:text-sm bg-gray-50 dark:bg-black/30 border border-gray-200 dark:border-dark-border p-4 rounded-sm shadow-inner h-64 overflow-hidden flex flex-col justify-end text-gray-600 dark:text-[#a0a5aa]">
          <div v-for="(log, idx) in terminalLogs" :key="idx" class="mb-1 last:mb-0"
            :class="{'text-brand-orange dark:text-brand-orange font-bold': log.includes('[认证]')}">
             {{ log }}
          </div>
          <div class="mt-2 flex items-center text-brand-orange">
             <span class="mr-2">&gt;</span>
             <span class="w-1.5 h-4 bg-brand-orange animate-pulse"></span>
          </div>
        </div>
      </div>

      <div class="text-xs text-gray-400 dark:text-gray-500 font-mono mt-8 uppercase tracking-widest flex justify-between">
        <span>V 3.4.12 // 协同</span>
        <span>系统正常</span>
      </div>
    </div>

    <!-- Right Side: Login Form -->
    <div class="flex-1 flex items-center justify-center p-6 sm:p-12 z-10">
      <div class="w-full max-w-md bg-white dark:bg-dark-card border border-gray-200 dark:border-dark-border rounded-sm shadow-2xl overflow-hidden group">

        <!-- Top bar (mimics a window frame) -->
        <div class="h-8 border-b border-gray-100 dark:border-dark-border bg-gray-50 dark:bg-black/20 flex items-center px-4 gap-2">
          <div class="w-2.5 h-2.5 rounded-full bg-red-400/80"></div>
          <div class="w-2.5 h-2.5 rounded-full bg-yellow-400/80"></div>
          <div class="w-2.5 h-2.5 rounded-full bg-green-400/80"></div>
          <span class="ml-auto text-[10px] text-gray-400 font-sans tracking-wide uppercase">认证模块</span>
        </div>

        <div class="p-8 sm:p-10">
          <div class="mb-10 text-center md:text-left">
            <h2 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white uppercase tracking-wider min-h-[32px]">
              {{ typedTitle }}<span class="animate-pulse" v-if="typedTitle.length < fullTitle.length">_</span>
            </h2>
            <p class="text-gray-500 dark:text-gray-400 text-sm mt-2 font-mono">
              请输入凭据以继续。
            </p>
            <p v-if="errorText" class="text-red-500 dark:text-red-400 text-xs mt-2 font-mono">
              {{ errorText }}
            </p>
          </div>

          <form :key="formRenderKey" autocomplete="off" @submit.prevent="handleLogin" class="space-y-6">
            <!-- Username Input -->
            <div class="space-y-2">
              <label for="username" class="block text-xs font-bold text-gray-700 dark:text-gray-300 uppercase tracking-widest">
                <i class="pi pi-user mr-1 text-brand-orange"></i> 账号
              </label>
              <div class="relative flex items-center isolate">
                <span class="absolute left-3 text-gray-400 dark:text-gray-500 font-bold select-none pointer-events-none z-[1]">&gt;</span>
                <!-- Unstyled PrimeVue Component replaced via tailwind pt/class -->
                <InputText id="username"
                           v-model="username"
                           class="w-full pl-10 pr-4 py-3 bg-gray-50 dark:bg-black/20 border border-gray-200 dark:border-dark-border text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-brand-orange focus:border-brand-orange transition-colors font-mono text-sm placeholder-gray-400/50 rounded-sm relative z-[2]"
                           :pt="{ root: { class: '!border-gray-200 dark:!border-dark-border focus:!border-brand-orange outline-none shadow-none' } }"
                           placeholder="请输入管理员账号"
                           name="admin-login-username"
                           autocomplete="off" />
              </div>
            </div>

            <!-- Password Input -->
            <div class="space-y-2">
              <div class="flex justify-between items-center">
                <label for="password" class="block text-xs font-bold text-gray-700 dark:text-gray-300 uppercase tracking-widest">
                  <i class="pi pi-key mr-1 text-brand-orange"></i> 密码
                </label>
                <a href="#" class="text-[10px] text-gray-500 hover:text-brand-orange transition-colors uppercase">绕过?</a>
              </div>
              <div class="admin-login-password-field relative flex items-center isolate">
                <span class="absolute left-3 text-gray-400 dark:text-gray-500 font-bold select-none z-[1] pointer-events-none">&gt;</span>
                <Password inputId="password"
                          v-model="password"
                          :feedback="false"
                          toggleMask
                          class="w-full flex !relative !z-[2]"
                          autocomplete="new-password"
                          :pt="{
                            root: { class: 'w-full' },
                            input: {
                              class: 'w-full pl-10 pr-10 py-3 bg-gray-50 dark:bg-black/20 border border-gray-200 dark:border-dark-border text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-brand-orange focus:border-brand-orange transition-colors font-mono text-sm placeholder-gray-400/50 rounded-sm !shadow-none',
                              autocomplete: 'new-password',
                              name: 'admin-login-password'
                            },
                            showicon: { class: 'text-gray-400 absolute right-3 cursor-pointer hover:text-brand-orange z-[3] text-sm mt-0.5' },
                            hideicon: { class: 'text-gray-400 absolute right-3 cursor-pointer hover:text-brand-orange z-[3] text-sm mt-0.5' }
                          }"
                          placeholder="••••••••" />
              </div>
            </div>

            <!-- Submit Action -->
            <div class="pt-4">
              <Button type="submit"
                      :disabled="isLoading || !username || !password"
                      class="w-full py-3 px-4 bg-transparent border-2 border-brand-orange text-brand-orange font-bold uppercase tracking-widest hover:bg-brand-orange hover:text-white transition-all duration-300 flex items-center justify-center gap-2 group disabled:opacity-50 disabled:cursor-not-allowed rounded-sm relative overflow-hidden">

                 <!-- Hover Scanline effect -->
                 <span class="absolute top-0 left-0 w-full h-[2px] bg-white opacity-50 -translate-x-full group-hover:animate-[scanline_1s_ease-in-out_infinite]"></span>

                  <span v-if="!isLoading">发起连接</span>
                 <span v-else class="flex flex-col items-center justify-center">
                    执行中...
                 </span>
                 <i v-if="!isLoading" class="pi pt-0.5" :class="'pi-arrow-right group-hover:translate-x-1 transition-transform'"></i>
                 <i v-else class="pi pi-spinner pi-spin"></i>
              </Button>
            </div>
          </form>

        </div>
      </div>

      <!-- Mobile Terminal Logs Area -->
      <div class="absolute bottom-4 left-4 right-4 md:hidden pointer-events-none">
         <div class="font-mono text-[10px] text-gray-500 opacity-60">
            <div v-for="log in terminalLogs.slice(-2)" :key="log" class="truncate">{{ log }}</div>
         </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
@keyframes scanline {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* 修复浏览器默认的表单自动填充导致的白色或黄色背景 */
:deep(input:-webkit-autofill),
:deep(input:-webkit-autofill:hover),
:deep(input:-webkit-autofill:focus),
:deep(input:-webkit-autofill:active) {
  transition: background-color 5000s ease-in-out 0s;
  -webkit-text-fill-color: currentColor !important;
}

/* Ensure PrimeVue Password component container wraps properly since we override base classes */
:deep(.p-password) {
  width: 100%;
  display: block;
  position: relative;
}

/* Ensure the inner input for password field has enough padding so dots don't cover the > chevron */
.admin-login-password-field :deep(input) {
  padding-left: 2.5rem !important; /* pl-10 roughly equals 40px */
  padding-right: 2.5rem !important;
}
</style>
