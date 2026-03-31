import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { useAppStore } from '@/stores/app'
import { useAdminAuthStore } from '@/stores/adminAuth'
import { useAuthStore } from '@/stores/auth'
import PrimeVue from 'primevue/config'
import ToastService from 'primevue/toastservice'
import sliderVerify from 'vue3-slider-verify'
import { registerUnauthorizedHandler } from '@/utils/http'
import { i18n } from '@/i18n'
import { applyThemeScope, resolveThemeScopeByPath } from '@/theme/scope'
import { registerUiAdapter } from '@/plugins/registerUiAdapter'

import App from '@/App.vue'
import router from '@/router'
import '@/assets/styles/tailwind.css'
import 'primeicons/primeicons.css'
import 'vue3-slider-verify/lib/style.css'

const app = createApp(App)
const pinia = createPinia()
const adminAuthStore = useAdminAuthStore(pinia)
const authStore = useAuthStore(pinia)

app.use(pinia)
app.use(router)
app.use(i18n)
app.use(sliderVerify)
app.use(PrimeVue, {
  unstyled: true,
  ripple: false,
})
app.use(ToastService)
registerUiAdapter(app)

// 初始化主题和语言
const appStore = useAppStore(pinia)
appStore.initTheme()
appStore.initLanguage()
applyThemeScope(resolveThemeScopeByPath(window.location.pathname))

registerUnauthorizedHandler(async () => {
  const currentRoute = router.currentRoute.value
  const currentPath = currentRoute.path || ''
  const browserPath = window.location.pathname || ''
  const isAdminRoute =
    currentPath.startsWith('/computer/admin') || browserPath.startsWith('/computer/admin')

  if (isAdminRoute) {
    if (!adminAuthStore.isAuthed) {
      return
    }
    adminAuthStore.clear()
    if (currentRoute.name === 'adminLogin') {
      return
    }
    if (!currentRoute.meta.requiresAdminAuth) {
      return
    }
    await router.replace({
      name: 'adminLogin',
      query: { redirect: currentRoute.fullPath },
    })
    return
  }

  if (!authStore.isAuthed) {
    return
  }
  authStore.clear()
  if (currentRoute.name === 'login') {
    return
  }
  if (!currentRoute.meta.requiresAuth) {
    return
  }
  await router.replace({
    name: 'login',
    query: { redirect: currentRoute.fullPath },
  })
})

app.mount('#app')
