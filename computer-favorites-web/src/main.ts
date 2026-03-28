import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import sliderVerify from 'vue3-slider-verify'
import { registerUnauthorizedHandler } from '@/utils/http'
import { i18n } from '@/i18n'
import { applyThemeScope, resolveThemeScopeByPath } from '@/theme/scope'

import App from '@/App.vue'
import router from '@/router'
import '@/assets/styles/tailwind.css'
import 'vue3-slider-verify/lib/style.css'

const app = createApp(App)
const pinia = createPinia()
const authStore = useAuthStore(pinia)

app.use(pinia)
app.use(router)
app.use(i18n)
app.use(sliderVerify)

// 初始化主题和语言
const appStore = useAppStore(pinia)
appStore.initTheme()
appStore.initLanguage()
applyThemeScope(resolveThemeScopeByPath(window.location.pathname))

registerUnauthorizedHandler(async () => {
  if (!authStore.isAuthed) {
    return
  }
  authStore.clear()
  const currentRoute = router.currentRoute.value
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
