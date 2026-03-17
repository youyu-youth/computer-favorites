import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { useAppStore } from '@/stores/app'

import App from './App.vue'
import router from './router'
import './assets/styles/tailwind.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
useAppStore(pinia).initTheme()

app.mount('#app')
